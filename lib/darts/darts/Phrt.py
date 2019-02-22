#!/usr/bin/python

__author__         = 'Teddy Zhai'
__description__    = "Find minimum unfolding factors for a given SDFG to achieve shortest period"
"on a given number of processors"
"This implements the algorithms proposed in"
"J. T. Zhai, M. A. Bamakhrama, T. Stefanov."
"Exploiting Just-enough Parallelism when Mapping Streaming Applications in Hard Real-time Systems,"
"in the Proceeding of the 50th IEEE/ACM Design Automation Conference (DAC'13),"
"Austin, TX, USA, June 2-6, 2013."

import os 
import sys
import shutil
import math
import argparse

import Utilities
from ACSDFModel import ACSDFModel
import Allocation
import PlatformParameters
import PlatformGenerator

# TODO Make sure that the following paths are set properly
# $PN_DIR,  $PNTOOLS_DIR, $CSDF_RTSCHEDTOOLS_DIR
PN_DIR = os.getenv('PN_DIR')
PNTOOLS_DIR = os.getenv('PNTOOLS_DIR')
CSDF_RTSCHEDTOOLS_DIR = os.getenv('CSDF_RTSCHEDTOOLS_DIR')


def phrt(base_dir, source_file, nr_proc, quality_factor, 
         latency_aware = False, latency_tolerance = 0.0001):
    # Put all the generated files in a folder called source_file_graphs
    shutil.rmtree(os.path.join(base_dir, source_file + "_graphs"), ignore_errors=True)
    os.mkdir(os.path.join(base_dir, source_file + "_graphs"))
    shutil.copy(os.path.join(base_dir, source_file + ".gph"), \
            os.path.join(base_dir, source_file + "_graphs/"))
    os.chdir(os.path.join(base_dir, source_file + "_graphs"))
    
    # A list which stores 1 ) current shortest sink period, 2) current highest utilization,
    # and 3) the corresponding CSDFG with its associated list of unfolding factors
    best = []
    
    g_init = ACSDFModel.load_graph_from_file(source_file + ".gph", 1)
    
    # Get the upper bounds of all unfolding factors
    unfold_bound = g_init.find_upper_bounds_unfolding_factors()
    #print("f bound: %r" % (unfold_bound))
    
    # FIXME currently we assume that the application has only one sink actor
    sink_ac = g_init.get_sink_actor()
    sink_id = sink_ac.get_actor_id()
    
    # A dictionary {actor id : unfold factor}
    # Set all initial unfolding factors by 1
    unfold_factors = {i : 1 for i in unfold_bound}
    
    # Keep track of the bottleneck actor
    # First initialize them as in the initial SDFG
    b_ac_id = g_init.get_b_ac_id()
    
    # Initialize the current best solution based on the initial SDF
    s_l_bound = int(math.ceil(g_init.get_utilization() / nr_proc))
    s_u_bound = int(math.ceil(Allocation.FFD_APPROX_RATIO * g_init.get_utilization() / nr_proc))
    g_init.set_scaling_factor(s_l_bound)
    g_init.update_parameters(PlatformParameters.AXI_CB, 
                        PlatformParameters.T_RD_AXI_CB,
                        PlatformParameters.T_WR_AXI_CB)
    scaling_factor = sys.maxsize
    
    
    for s in range(s_l_bound, s_u_bound + 1, 1):
        g_init.set_scaling_factor(s)
        graphs = []
        graphs.append(g_init)
        
        pg = PlatformGenerator.make_ready_graphs(graphs)
        pg.set_sched_algo("EDF")
        partition = Allocation.ffd(pg)
        if len(partition) <= nr_proc: # schedulable
            scaling_factor = s
            break
    if scaling_factor == sys.maxsize:
        scaling_factor = s_u_bound + 1
        
    g_init.set_scaling_factor(scaling_factor)
    g_init.update_parameters(PlatformParameters.AXI_CB,
                        PlatformParameters.T_RD_AXI_CB,
                        PlatformParameters.T_WR_AXI_CB)
    
    best.append(sink_ac.get_period())
    best.append(g_init.get_utilization())
    best.append(scaling_factor)
    best.append(unfold_factors)
    
    
    ################################################################################################
    # Start the exploration
    end = False
    while(not end):
        # 1) Termination criteria: a given utilization has been achieved
        #print("\nbest U: %s; target U: %s" % (best[1], nr_proc * quality_factor))
        if best[1] >= nr_proc * quality_factor:
            end = True
            break
        # 2) Termination criteria: upper bound of unfolding factor is reached and the 
        # current actor is still the bottleneck.  
        for i, j in unfold_factors.items():
            # termination criteria 2): upper bound of unfolding factor is reached and 
            # the current actor is still the bottleneck.  
            if j == unfold_bound[i] and i == b_ac_id:
                end = True
                break
        if end == True:
            break
        
        # Increment the unfolding factor of the bottleneck actor by 1
        unfold_factors[b_ac_id] += 1
        
        # Unfold SDF in darts
        g = g_init.unfold_sdf(unfold_factors)
        g.find_minimum_period_vector(1)
        for ac in g.get_actors():
            ac.set_deadline(ac.get_period())
        
        # Get new bottleneck actor in the initial SDF graph
        b_ac_id = g_init.get_b_ac_id_under_unfolding(unfold_factors)
        
        if g.get_utilization() <= best[1]:
            continue
        
        # Compute the range of the scaling factor for this CSDF graph
        s_l_bound = int(math.ceil(g.get_utilization() / nr_proc))
        s_u_bound = int(math.ceil(Allocation.FFD_APPROX_RATIO * g.get_utilization() / nr_proc))
        g.set_scaling_factor(s_l_bound)
        g.update_parameters(PlatformParameters.AXI_CB, 
                            PlatformParameters.T_RD_AXI_CB,
                            PlatformParameters.T_WR_AXI_CB)
        
        if g.get_utilization() <= best[1]:
            continue
        
        # find the minimum scaling factor under FFD allocation
        scaling_factor = sys.maxsize
        # in case of latency-aware optimization, use CDP model to reduce latency
        # initialize deadline factor
        d = 1
        is_feasible = False
        for s in range(s_l_bound, s_u_bound + 1, 1):
            g.set_scaling_factor(s)
            
            if latency_aware == True:
                # perform binary search on finding the minimum deadline factor
                l = 0.0; r = 1.0
                is_feasible = False  
                while l + latency_tolerance <= r and is_feasible == False:
                    d = math.floor((l+r) / 2)
                    g.set_deadline_factor(d)
                    graphs = []
                    graphs.append(g)
                    pg = PlatformGenerator.make_ready_graphs(graphs)
                    pg.set_sched_algo("EDF")
                    
                    partition = Allocation.FFD(pg)
                    
                    if len(partition) > nr_proc:  # not feasible
                        l = d
                        is_feasible = False
                    else:                   # feasible, 
                        r = d
                        is_feasible = True                    
            else:   # without reducing latency, using IDP by default             
                graphs = []
                graphs.append(g)
                pg = PlatformGenerator.make_ready_graphs(graphs)
                pg.set_sched_algo("EDF")
                pg.set_alloc_algo("FFD")
                partition = Allocation.ffd(pg)
                
                if len(partition) <= nr_proc: # schedulable
                    scaling_factor = s
                    is_feasilbe = True
            if is_feasilbe == True:
                break
                    
        # The maximum scaling factor according to the approximation ratio of FFD
        if scaling_factor == sys.maxsize:
            scaling_factor = s_u_bound + 1
        
        g.set_scaling_factor(scaling_factor)
        g.update_parameters(PlatformParameters.AXI_CB,
                            PlatformParameters.T_RD_AXI_CB,
                            PlatformParameters.T_WR_AXI_CB)
        
        #print("sink id: %s" % (Utilities.translate_id(sink_id, 1)))
        #print("best T: %s; sink T: %s" % (best[0], sink_a.get_period()))
        #print("best U: %s; sink U: %s" % (best[1], g.get_utilization()))
        #print("vec f: %r; " % ( unfold_factors ))
        
        sink_a = g.get_actor(Utilities.translate_id(sink_id, 1)) # Assuming that 
                                                                 # the sink actor is not unfolded
        if g.get_utilization() > best[1] and sink_a.get_period() < best[0]:
            best[0] = sink_a.get_period()
            best[1] = g.get_utilization()
            best[2] = scaling_factor
            best[3] = unfold_factors
    
    return best

def print_solution(solution):
    sys.stdout.write("Resulting period of the sink actor: %s\n" % (solution[0]))
    sys.stdout.write("Resulting utilization: %s\n" % (solution[1]))
    sys.stdout.write("Period scaling factor: %r\n" % (solution[2]))
    sys.stdout.write("Unfolding factors {actor id: unfolding factor}: %r\n" % (solution[3]))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__description__,\
            epilog="Author: %s" % __author__)
    parser.add_argument("-o", nargs=1, type=str,
            default="../tests/synthetic/",
            metavar="output_dir",
            help="The output directory")
    parser.add_argument("-i", nargs=1, type=str, default="chain121_src_snk",
            metavar="input_file", 
            help="The input file name (without .gph)")
    parser.add_argument("-m", nargs=1, type=int, default=2,
            metavar="num_of_proc",
            help="Number of processors onto which the application is mapped")
    parser.add_argument("-q", nargs=1, type=float, default=0.999,
            metavar="quality_factor", help="Quality factor")
    args = parser.parse_args()

    if type(args.o) == str:
        base_dir = args.o
    else:
        base_dir = args.o[0]
    if type(args.i) == str:
        source_file = args.i
    else:
        source_file = args.i[0]
    if type(args.m) == int:
        nr_proc = args.m
    else:
        nr_proc = args.m[0]
    if type(args.q) == float:
        quality_factor = args.q
    else:
        quality_factor = args.q[0]

    assert os.path.isdir(base_dir)
    assert os.path.isfile(os.path.join(base_dir, source_file + ".gph"))
    assert type(nr_proc) == int and nr_proc > 0
    assert type(quality_factor) == float
    assert quality_factor >= 0.0 and quality_factor <= 1.0

    solution = []
    solution = phrt(base_dir, source_file, nr_proc, quality_factor)
    
    print_solution(solution)
    
