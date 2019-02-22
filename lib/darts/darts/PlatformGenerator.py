#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama and Jiali Teddy Zhai'
__description__ = "Generates the taskset representation of a set of graphs and "
"allocates it onto the minimum required number of processors"

import sys
import os
import math
import argparse

from ACSDFModel import ACSDFModel
import Allocation
from EspamHandler import EspamHandler
#from TextHandler import TextHandler
from DotHandler import DotHandler
from JSONHandler import JSONHandler
from Schedulers import EDF, FPPS

import Utilities
import PlatformParameters

# Change verbose to True to print detailed information while running
verbose=False

class PlatformGenerator:

    # Supported scheduling and allocation algorithms by PlatformGenerator
    allocation_algorithms = {
        "FFD"       : ("First-Fit Decreasing", Allocation.ffd),
        "FF"        : ("First-Fit", Allocation.ff),
        "WF"        : ("Worst-Fit", Allocation.wf)
    }

    scheduling_algorithms = {
        "EDF"   : "Earliest-Deadline-First",
        "FPPS"  : "Fixed-Priority Preemptive Scheduling"
    }

    def __init__(self):
        """Constructor"""
        self.partition = []
        self.alloc_algo = "FFD"
        self.sched_algo = "EDF"
        self.graphs = []
        self.actors = []
        self.total_utilization = 0.0
        self.max_utilization = 0.0
        self.total_density = 0.0
        self.max_density = 0.0
        # Set to True if any graph has deadline_factor < 1.0
        self.constrained_deadline = False 
        self.num_of_apps = len(self.graphs)
        self.total_utilization = 0.0
        # beta is not used anymore.
        self.beta = 0.0
        self.min_num_of_processors = -1
    
    def get_min_num_of_processors(self):
        """Returns the lower bound on the number of processors needed to 
        schedule the applications. 
        This value is the ceil of the total utilization
        """
        return self.min_num_of_processors

    def get_num_of_actors(self):
        """Returns the total number of actors"""
        return len(self.actors)

    def get_actors(self):
        """Returns the list of actors"""
        return self.actors

    def get_sched_algo(self):
        """Returns the scheduling algorithm name
        """
        return self.sched_algo

    def is_constrained_deadline(self):
        """Returns :code:`True` if any graph uses constrained-deadlines. 
        Otherwise, it returns :code:`False` 
        """
        return self.constrained_deadline

    def set_constrained_deadline(self, constrained_deadline):
        """Sets the constrained deadline flag to *constrained_deadline*
        """
        self.constrained_deadline = constrained_deadline

    def set_sched_algo(self, sched_algo):
        """Sets the scheduling algorithm name to *sched_algo*
        """
        assert sched_algo in self.scheduling_algorithms
        self.sched_algo = sched_algo

    def get_alloc_algo(self):
        """Returns the allocation algorithm name
        """
        return self.alloc_algo
    
    def set_alloc_algo(self, alloc_algo):
        """Sets the allocation algorithm name to *alloc_algo*
        """
        assert alloc_algo in self.allocation_algorithms
        self.alloc_algo = alloc_algo

    def get_graphs(self):
        """Returns the list of graphs
        """
        return self.graphs

    def get_num_of_graphs(self):
        """Returns the number of graphs"""
        return len(self.graphs)

    def get_partition(self):
        """Returns the partition of actors over the processors"""
        return self.partition

    def set_partition(self, partition):
        """Sets the partition to *partition*"""
        self.partition = partition

    def get_actor_by_name(self, actor_name):
        """Returns the actor with the given *actor_name*. :code:`None` if no 
        match is found"""
        for a in self.get_actors():
            if a.get_name() == actor_name:
                return a
        return None

    def get_actor_by_global_id(self, global_id):
        """Returns the actor with the given *global_id*. :code:`None` if no 
        match is found"""
        for a in self.get_actors():
            if a.get_global_id() == global_id:
                return a
        return None

    def update_graphs(self, partition, optimized=True):
        """
        Updates the actors affinities, builds the contention matrix, and updates
        the WCET of each actor based on the contention resulting from the given
        partition
        """
        processor_id = 0
        for processor in partition:
            for actor in processor.get_actors():
                actor.set_affinity(processor_id)
            processor_id += 1

        channels = self.get_mapped_channels()
        gamma =[[0 for i in range(len(partition))] 
                   for j in range(len(partition))]
        for c in channels:
            gamma[c[0]][c[1]] = 1

        # Set the contention
        for actor in self.get_actors():
            if optimized:
                contention = sum(gamma[actor.get_affinity()])
            else:
                contention = len(partition)
            actor.set_contention(contention)

        for g in self.get_graphs():
            g.update_parameters(PlatformParameters.AXI_CB, 
                                PlatformParameters.T_RD_AXI_CB, 
                                PlatformParameters.T_WR_AXI_CB)

    def get_mapped_channels(self):
        """Returns a list of the channels, where each source and destination 
        actor is replaced by the processor on which it is mapped"""
        channels = []
        chan_cnt = 0
        for actor in self.get_actors():
            for c in actor.get_outchannels():
                ch = actor.get_graph().get_channel(c)
                successor = actor.get_graph().get_actor(ch.get_destination())
                channels.append((actor.get_affinity(), 
                                 successor.get_affinity()))
                chan_cnt += 1
        return channels

    def process_graphs(self, apps, scaling_factors, deadline_factors, 
                        token_sizes, ignorestartbuf, printinfo=False):
        """
        Takes a set of .gph files and generates the ACSDFModel objects 
        corresponding to them
        """
        graphs = list()
        for i in range(0, len(apps)):

            if deadline_factors[i] < 1.0:
                self.set_constrained_deadline(True)

            g = ACSDFModel.load_graph_from_file(apps[i], scaling_factors[i], 
                                                deadline_factors[i])
            g.set_max_token_size(token_sizes[i])
            if printinfo:
                sys.stdout.write("\n")
                sys.stdout.write(" - Processing application %s...\n" % 
                                (os.path.basename(apps[i])))
                sys.stdout.write(" -- Number of actors = %s\n" % 
                                (g.get_num_of_actors()))
                sys.stdout.write(" -- Repetition vector = %s\n" % 
                                (g.get_repetition_vector_str()))
                sys.stdout.write(" -- Worst-case execution time vector = %s\n" % 
                                (g.get_execution_vector_str()))
                sys.stdout.write(" -- Minimum period vector = %s\n" % 
                                (g.get_period_vector_str()))
                sys.stdout.write(" -- Deadline vector = %s\n" % 
                                (g.get_deadline_vector_str()))
            if verbose:
                sys.stdout.write("\n")
                sys.stdout.write(" -- Least common multiple of the repetitions "
                                 "(Q) = %s\n" % (g.lcm)) 
                sys.stdout.write(" -- max(WCET_i * q_i) (eta) = %s\n" % 
                                (g.max_q_C)) 
                sys.stdout.write(" -- Iteration period = %s\n" % 
                                (g.alpha))
                if g.get_matched_io_type():
                    sys.stdout.write(" -- %s is a matched I/O rates graph\n" % 
                                    (g.get_graph_name()))
                else:
                    sys.stdout.write(" -- %s is a mismatched I/O rates graph\n" 
                                     % (g.get_graph_name()))
                if g.get_minor_cycle() != 0:
                    sys.stdout.write(" -- Cyclic executive major cycle = " + 
                                    str(g.get_major_cycle()) + 
                                    ", Cyclic Executive Minor Cycle = " + 
                                    str(g.get_minor_cycle()) + "\n")
                else:
                    sys.stdout.write(" -- Unable to find a minor cycle with "
                                     "no preemptions!\n")            
                sys.stdout.write(" -- Hyperperiod = %s\n" % 
                                (Utilities.lcmv(g.get_period_vector())))

            g.update_parameters(PlatformParameters.AXI_CB, 
                                PlatformParameters.T_RD_AXI_CB, 
                                PlatformParameters.T_WR_AXI_CB)
            if not ignorestartbuf:
                g.calc_start_time_buffer_vector()
            graphs.append(g)

        if printinfo:
            sys.stdout.write("\n")
        
        assert len(graphs) > 0
        self.graphs = graphs
        self.num_of_apps = len(self.graphs)
        num_of_actors = 0
        for g in self.graphs:
            for a in g.get_actors():
                a.set_global_id(num_of_actors)
                self.total_utilization = self.total_utilization + \
                                         a.get_utilization()
                self.total_density = self.total_density + a.get_density()
                self.actors.append(a)
                num_of_actors += 1
                if a.get_utilization() > self.max_utilization:
                    self.max_utilization = a.get_utilization()
                if a.get_density() > self.max_density:
                    self.max_density = a.get_density()
        self.beta = int(math.floor(1.0/self.max_utilization))
        self.min_num_of_processors = int(math.ceil(self.total_utilization))

    def process_ready_graphs(self, graphs, scaling_factor = 1):
        """
        Takes a set of ACSDFModel objects
        """
        for g in graphs:
            if scaling_factor != 1:
                g.set_scaling_factor(scaling_factor)
            g.update_parameters(PlatformParameters.AXI_CB, 
                                PlatformParameters.T_RD_AXI_CB, 
                                PlatformParameters.T_WR_AXI_CB)
        assert len(graphs) > 0
        self.graphs = graphs
        self.num_of_apps = len(self.graphs)
        num_of_actors = 0
        for g in self.graphs:
            for a in g.get_actors():
                a.set_global_id(num_of_actors)
                self.total_utilization = self.total_utilization + \
                                         a.get_utilization()
                self.total_density = self.total_density + a.get_density()
                self.actors.append(a)
                num_of_actors += 1
                if a.get_utilization() > self.max_utilization:
                    self.max_utilization = a.get_utilization()
                if a.get_density() > self.max_density:
                    self.max_density = a.get_density()
        self.beta = int(math.floor(1.0/self.max_utilization))
        self.min_num_of_processors = int(math.ceil(self.total_utilization))


def make_graphs(apps, scaling_factors, deadline_factors, token_sizes, 
                ignorestartbuf = False, printinfo=False):
    """
    Takes a set of .gph files and returns a PlatformGenerator corresponding 
    to them
    """
    assert len(scaling_factors) == len(apps) == \
            len(token_sizes) == len(deadline_factors)    
    pg = PlatformGenerator()
    pg.process_graphs(apps, scaling_factors, deadline_factors, token_sizes, 
                      ignorestartbuf, printinfo)
    return pg

def make_ready_graphs(graphs):
    """
    Takes a set of ACSDFModel objects and returns a PlatformGenerator
    instance for them
    """
    pg = PlatformGenerator()
    pg.process_ready_graphs(graphs)
    return pg    

def make_platform(output_dir, pg, alloc_algo, sched_algo, ignorestartbuf=False, 
                  printinfo=False):
    """
    Takes a PlatformGenerator instance and generates the platform and mapping 
    specifications needed by ESPAM
    """
    pg.set_alloc_algo(alloc_algo)
    pg.set_sched_algo(sched_algo)

    # Allocation phase
    partition = PlatformGenerator.allocation_algorithms[pg.get_alloc_algo()][1](pg)

    # TODO: Build an offline schedule. Commented out for now.
#    if pg.get_sched_algo() == "EDF":
#        proc_id = 1
#        for proc in partition:
#            edf = EDF(output_dir, proc, proc_id)
#            edf.schedule()
#            proc_id += 1
#    if pg.get_sched_algo() == "FPPS":
#        proc_id = 1
#        for proc in partition:
#            edf = FPPS(output_dir, proc, proc_id)
#            edf.schedule()
#            proc_id += 1

    # In case of exhaustive search, the results is actually a list of the best mappings
    if pg.get_alloc_algo() == "LCE":
        partition = partition[-1]

#    pg.update_graphs(partition)
    pg.set_partition(partition)

    # Update start times and buffer sizes
    for g in pg.get_graphs():
        if not ignorestartbuf:
            g.calc_start_time_buffer_vector()
    
    # Print results
    print_info(pg, ignorestartbuf, printinfo)

    # Call ESPAM handler
    eh = EspamHandler(pg, 'ML605', output_dir)
    eh.generate_platform_file()
    eh.generate_mapping_file()

    ## Call text handler
    #th = TextHandler(pg, output_dir)
    #th.generate_mapping_file()

    # Call JSON handler
    jh = JSONHandler(pg, output_dir)
    jh.generate_mapping_file()

    dh = DotHandler(pg, output_dir)
    dh.generate_mapping_file()

    return pg

def make_platform_m_pe(output_dir, pg, m, alloc_algo, sched_algo, ignorestartbuf=False, 
                  printinfo=False):
    """
    Takes a PlatformGenerator instance and generates the platform with m processors and mapping 
    specifications needed by ESPAM

    Args:
        A :class:`PlatformGenerator.PlatformGenerator` object containing 
        the graphs to be mapped onto the platform.
        
        m: m processors

    Returns:
        A partition describing the mapping of actors to processors.
        The partition is a list of m :class:`Processor.Processor` objects. 
        The minimum scaling factor is computed such that all the actors are allocatable on m processors
        using the allocation and scheduling algorithms.
    """
    
    # make sure that the scaling factor equal to 1
    pg.process_ready_graphs(pg.get_graphs())
        
    s_min = int(math.ceil(pg.get_utilization() / m))
    if pg.get_alloc_algo() == "FFD":
        s_max = int(math.ceil(FFD_APPROX_RATIO * pg.get_utilization() / m))
    # Here add all other known approximation factors
    #elif
    else:
        s_max = sys.maxsize
    
    graphs = pg.get_graphs()
    scaling_factor = sys.maxsize
    for s in range(s_min, s_max + 1, 1):
        pg.process_ready_graphs(graphs, s)
        
        partition = PlatformGenerator.allocation_algorithms[pg.get_alloc_algo()][1](pg)
        if len(partition) <= m:
            scaling_factor = s
            break
    if scaling_factor == sys.maxsize:
        pg.process_ready_graphs(graphs, s_max + 1)
        partition = PlatformGenerator.allocation_algorithms[pg.get_alloc_algo()][1](pg)
    
    # In case of exhaustive search, the results is actually a list of the best mappings
    if pg.get_alloc_algo() == "LCE":
        partition = partition[-1]

    pg.update_graphs(partition)
    pg.set_partition(partition)

    if not ignorestartbuf:
        for g in pg.get_graphs():
            g.calc_start_time_buffer_vector()
    
    print_info(pg, ignorestartbuf, printinfo)
    
    # Call ESPAM handler
    eh = EspamHandler(pg, 'ML605', output_dir)
    eh.generate_platform_file()
    eh.generate_mapping_file()

    # Call text handler
    th = TextHandler(pg, output_dir)
    th.generate_mapping_file()

    dh = DotHandler(pg, output_dir)
    dh.generate_mapping_file()

    return pg

def print_info(pg, ignorestartbuf=False, printinfo=False):
    if verbose:
        sys.stdout.write(" - Contention of the mapping = %s\n" % 
                        (Allocation.contention(partition, pg)))
        sys.stdout.write(" - Mapping is %s" % (partition))
        sys.stdout.write("\n\n")
        
    # print start times and buffer sizes
    if printinfo:
        for g in pg.get_graphs():
            sys.stdout.write(" - Dumping %s information...\n" % 
                            (g.get_graph_name()))
            sys.stdout.write(" -- WCET vector = %s\n" % 
                            (g.get_execution_vector_str()))
            # sys.stdout.write(" -- Sink actors periods:")
            # ac = g.get_sink_actors()
            # sys.stdout.write(" %s (%s) : %s\n" % (ac.get_full_name(), 
            #                  ac.get_function(), ac.get_period()))
            sys.stdout.write(" -- Period vector = %s\n" % 
                            (g.get_period_vector_str()))
            sys.stdout.write(" -- Deadline vector = %s\n" % 
                            (g.get_deadline_vector_str()))
            sys.stdout.write(" -- Start time vector = %s\n" % 
                            (g.get_start_time_vector_str()))
            sys.stdout.write(" -- Total buffer size = %s\n" % 
                            (g.get_total_buffer_size()))
            sys.stdout.write(" -- Buffer size vector = %s\n" % 
                            (g.get_buffer_size_vector_str()))
            sys.stdout.write(" -- Total Utilization  = %s\n" % 
                            (g.get_utilization()))
            sys.stdout.write(" -- Total Density  = %s\n" % 
                            (g.get_density()))
            sys.stdout.write(" -- Graph maximum latency = %s\n" % 
                            (g.get_latency()))
            sys.stdout.write("\n")

    if printinfo:
        sys.stdout.write(" - Constrained Deadline = %s\n" % 
                        (pg.is_constrained_deadline()))
        sys.stdout.write(" - ceil(Total Utilization) = %s\n" % 
                        (pg.get_min_num_of_processors()))
        
        sched_test = "Liu and Layland Utilization Bounds"
        if pg.get_sched_algo() == "FPPS" and pg.is_constrained_deadline():
            sched_test = "Response Time Analysis (RTA)"
        elif pg.get_sched_algo() == "EDF" and pg.is_constrained_deadline():
            sched_test = "Quick-convergence Processor-demand Analysis (QPA)"
        
        sys.stdout.write(" - Schedulability Test = %s\n" % (sched_test))
        sys.stdout.write(" - Minimum number of processors needed to schedule "
                         "the applications using ")
        sys.stdout.write("%s + %s = %s\n" % 
              (PlatformGenerator.allocation_algorithms[pg.get_alloc_algo()][0], 
               pg.get_sched_algo(), len(pg.get_partition())))
        sys.stdout.write("\n")
        
def main():
    """Main function used to run the tool in standalone mode"""        

    parser = argparse.ArgumentParser(description=__description__, 
                                     epilog="Authors: %s" % __author__)
    required = parser.add_argument_group("required arguments")
    required.add_argument("-o", required=True, nargs=1, type=str, 
                          metavar="directory", help="The output directory " +
                          "where the platform and mapping specifications " + 
                          "will be generated")
    parser.add_argument("-c", action="store_true", 
        help="Output the time values in cycles instead of OS clock ticks")
    required.add_argument("-a", required=True, nargs=1, metavar="alloc_algo", 
                          type=str, help="The allocation algorithm")
    required.add_argument("-s", required=True, nargs=1, metavar="sched_algo", 
                          type=str, help="The scheduling algorithm")
    required.add_argument("-g", required=True, action="append", nargs=4, 
     metavar=("scaling-factor", "deadline-factor", "max-token-size", "graph"),
     help="""Specifies an application that will be processed. 
scaling-factor is an integer >=1 that is used to scale the minimum period. 
deadline-factor is a value between 0 and 1 that specifies the deadline factor. 
max-token-size is the maximum token size (in 32-bit words). 
graph is the path to the .gph file containing the CSDF model of the application.
Multiple applications can be specified by passing -a multiple times. 
For example: -g 1 1 10 app1.gph -g 2 1 1 app2.gph -g 10 0.5 1 app3.gph""")
    
    args = parser.parse_args()

    if not os.path.isdir(args.o[0]):
        sys.stderr.write("Error! The specified directory does not exist!\n")
        sys.exit(-1)
    
    output_dir = args.o[0]

    if not args.c:
        PlatformParameters.TIME_IN_CYCLES = False

    if args.a[0] not in PlatformGenerator.allocation_algorithms:
        sys.stderr.write("ERROR! Invalid allocation algorithm specified\n")
        sys.exit(-1)

    alloc_algo = args.a[0]

    if args.s[0] not in PlatformGenerator.scheduling_algorithms:
        sys.stderr.write("ERROR! Invalid scheduling algorithm specified\n")
        sys.exit(-1)

    sched_algo = args.s[0]

    scaling_factors = []
    deadline_factors = []
    token_sizes = []
    apps = []

    for g in args.g:
        scaling_factors.append(int(g[0]))
        deadline_factors.append(float(g[1]))
        token_sizes.append(int(g[2]))
        apps.append(g[3])
    
    pg = make_graphs(apps, scaling_factors, deadline_factors, token_sizes)
    make_platform(output_dir, pg, alloc_algo, sched_algo, printinfo=True)

# Entry point
if __name__ == "__main__":
    main()

