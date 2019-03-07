#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama and Teddy Zhai'
__description__ = 'Derives a periodic task-set from acyclic CSDF graph model'

import sys
import math
import os.path
import argparse
import fractions
import copy

from CSDFParser import CSDFParser
from ActorModel import ActorModel
from ChannelModel import ChannelModel
import Utilities
import Tarjan


verbose = False

class ACSDFModel:
    """This class represents an acyclic Cyclo-Static DataFlow (CSDF) graph.
    CSDF is originally defined in [BELP1996]_

    .. [BELP1996] 
        Greet Bilsen and Marc Engels and Rudy Lauwereins and Jean Peperstraete, 
        Cycle-static dataflow, IEEE Transactions on Signal Processing, 
        vol. 44, no. 2, pp. 397-408, February 1996. 
        DOI: `10.1109/78.485935 <http://dx.doi.org/10.1109/78.485935>`__
    """

    def __init__(self, deadline_factor=1.0):
        """The Constructor
        
        Args:
        deadline_factor: The deadline factor of the graph. Must be real-valued 
        and between [0,1]

        """
        self.name = None
        self.actors = []
        self.channels = []
        self.latency = 0
        self.alpha = 0
        self.lcm = 0
        self.max_q_C = 0
        self.b_ac_id = -1 # id of the bottleneck actor
        self.scaling_factor = 1
        self.max_token_size = 0
        self.major_cycle = 0
        self.minor_cycle = 0
        self.deadline_factor = deadline_factor

    def get_code_size(self):
        code_size = 0
        for a in self.actors:
            code_size += a.get_code_size()
        return code_size

    def get_max_token_size(self):
        """Returns the maximum token size
        """
        return self.max_token_size

    def set_max_token_size(self, max_token_size):
        """Sets the maximum token size to *max_token_size*
        """
        self.max_token_size = max_token_size

    def get_deadline_factor(self):
        """Returns the deadline factor of the graph
        """
        return self.deadline_factor

    def set_deadline_factor(self, deadline_factor):
        """Sets the deadline factor. 
        *deadline_factor* must be a real-value in the range [0,1]
        """
        assert deadline_factor >= 0.0 and deadline_factor <= 1.0
        self.deadline_factor = deadline_factor

    def add_actor(self, actor_id, name, function, wcet):
        """Adds an actor to the graph.
        
        Args:
            *actor_id*: The actor ID (integer).
            *name*: The actor name (string).
            *function*: The function name (string).
            *wcet*: The Worst-Case Execution Time (WCET) of the actor (integer).

        """
        a = ActorModel(self, actor_id, name, function, wcet)
        self.actors.append(a)

    def add_actor_ac(self, ac):
        """Adds an actor to the graph.
        
        Args:
            *actor*: the actor to be added

        """
        self.actors.append(ac)

    def add_channel(self, channel_id, name, source_id, source_port,
                    destination_id, destination_port):
        """Adds a channel to the graph.
    
        Args:
            *channel_id*: The channel ID (integer).
            *name*: The channel name (string).
            *source_id*: The source actor ID (integer).
            *source_port*: The source port connected to the channel (:class:`ActorModel.Port` object)
            *destination_id*: The destination actor ID (integer).
            *destination_port*: The destination port connected to the channel (:class:`ActorModel.Port` object)
        """
        c = ChannelModel(channel_id, name, source_id, source_port, 
                         destination_id, destination_port)
        self.channels.append(c)
        for i in self.actors:
            if i.get_actor_id() == source_id:
                i.add_outchannel(c.get_channel_id())
            if i.get_actor_id() == destination_id:
                i.add_inchannel(c.get_channel_id())

    def get_actor(self, actor_id):
        """Returns the actor object with the given actor_id.

        Returns:
            The actor object with actor_id. None if no match is found.
        """
        for i in self.actors:
            if (i.get_actor_id() == actor_id):
                return i
        return None
    
    def get_b_ac_id(self):
        """
        Returns id of the actor object with max_q_C, referred as the bottneck actor
        """
        assert self.b_ac_id != -1, "bottelneck actor has not been initialized!"
        return self.b_ac_id
    
    def get_b_ac_id_under_unfolding(self, unfold_factors):
        """
        Return id of the bottleneck actor with max_q_C, after applying
        the unfolding_sdf algorithm.
        
        Clearly, this function should be ONLY called by the initial SDF graph.
        """
        lst_factors = [unfold_factors[i] for i in unfold_factors]
        lcm_unfolding_factor = Utilities.lcmv(lst_factors)  		
        
        q_C_max = -1
        bac_id = -1
        for ac in self.actors:
            rep = ac.get_repetition() * lcm_unfolding_factor / unfold_factors[ac.get_actor_id()]
            q_C = rep * ac.get_wcet()
            
            if q_C > q_C_max:
                q_C_max = q_C
                bac_id = ac.get_actor_id()
        assert bac_id != -1, "A bottleneck actor was not found under unfolding."
        
        return bac_id
            
    
    def get_bottelneck_actor(self):
        """
        Returns the actor object with max_q_C.
        """
        assert self.b_ac_id != -1, "bottelneck actor has not been initialized!"
        
        b_ac = self.get_actor(self.b_ac_id)
        return b_ac
    
    def get_max_q_C(self):
        """Returns : the maximum workload per graph iteration defined as
                     math:`\max_{\tau_i \in \tau}{q_iC_i}`
        """
        if self.max_q_C == 0:
            for i in self.actors:
                val = i.get_wcet() * i.get_repetition()
                if val > self.max_q_C:
                    self.max_q_C = val
                    self.b_ac_id = i.get_actor_id()

        return self.max_q_C
    
    def get_channel(self, channel_id):
        """Returns the channel object with given channel_id. 

        Returns:
            The channel object with channel_id. None if no match is found.
        """
        for i in self.channels:
            if (i.get_channel_id() == channel_id):
                return i
        return None

    def get_actors(self):
        """Returns the list of actor objects
        """
        return self.actors

    def get_sink_actor(self):
        """Returns the sink actor in the graph

        For now, assume that there is only one sink actor and
        it is the last actor in the topological sorted actors.
        """
        components = self.get_components()
        return self.get_actor(components[-1][0])

    def get_channels(self):
        """Returns the list of channel objects
        """
        return self.channels

    def get_num_of_actors(self):
        """Returns the number of actors in the graph
        """
        return len(self.actors)

    def get_num_of_channels(self):
        """"Returns the number of channels in the graph
        """
        return len(self.channels)

    def get_graph_name(self):
        """Returns the graph name"""
        return self.name

    def set_graph_name(self, name):
        """Set the graph name"""
        self.name = name

    def get_scaling_factor(self):
        return self.scaling_factor

    def set_scaling_factor(self, factor):
        assert type(factor) == int
        self.scaling_factor = factor

    def get_latency(self):
        """Returns the graph latency"""
        return self.latency

    def update_parameters(self, interconnect_type, word_read_time, word_write_time):
        """Updates the execution times and periods of the actors according to 
        the given *interconnect_type*, *word_read_time* and *word_write_time*
        """
        for a in self.get_actors():
            a.update_execution_time(interconnect_type, word_read_time, word_write_time)
        self.find_minimum_period_vector(self.get_scaling_factor())

    def find_minor_cycle(self, lb, major_cycle):
        """Determines the minor cycle (Used for cyclic executive scheduling)
        """
        minor_cycle = lb
        while minor_cycle <= major_cycle:
            if major_cycle % minor_cycle == 0:
                found = True
                for a in self.get_actors():
                    val = minor_cycle + (minor_cycle - Utilities.gcd(minor_cycle, a.get_period()))
                    if val > a.get_deadline():
                        found = False
                        break
                if found:
                    return minor_cycle
            minor_cycle += 1
        return 0

    def compute_cyclic_executive_parameters(self):
        """Computes the Cycluc Executive Scheduling parameters"""
        major_cycle = Utilities.lcmv(self.get_period_vector())
        lb = max(self.get_execution_vector())
        minor_cycle = self.find_minor_cycle(lb, major_cycle)
        if minor_cycle != 0:
            assert major_cycle % minor_cycle == 0
            for a in self.get_actors():
                val = minor_cycle + (minor_cycle - Utilities.gcd(minor_cycle, a.get_period()))
                assert val <= a.get_deadline()
            self.major_cycle = major_cycle
            self.minor_cycle = minor_cycle
        else:
            # Unable to find a minor cycle with no preemption
            self.major_cycle = major_cycle
            self.minor_cycle = minor_cycle

    def get_components(self):
        """
        Returns the components of the graph after performing topological sort
        """
        graph = {}
        for a in self.get_actors():
            successors = []
            for oc in a.get_outchannels():
                successors.append(self.get_channel(oc).get_destination())
            graph[a.get_actor_id()] = successors
        components = Tarjan.robust_topological_sort(graph)
        return components

    def find_minimum_period_vector(self, factor):
        """Finds the minimum period vector and then multiplies it with factor"""
        if self.max_q_C == 0:
            self.get_max_q_C()
        
        self.lcm = Utilities.lcmv(self.get_repetition_vector())
        self.alpha = int(self.lcm * int(math.ceil(float(self.max_q_C)/float(self.lcm))) * factor)
        for i in self.actors:
            period = int(self.alpha / i.get_repetition())
            i.set_period(period)
            assert i.get_period() * i.get_repetition() == self.alpha

        if self.max_q_C % self.lcm == 0:
            self.is_matched_io_rates = True
        else:
            self.is_matched_io_rates = False

    def get_alpha(self):
        """Returns the actor iteration period"""
        return self.alpha

    def get_matched_io_type(self):
        return self.is_matched_io_rates

    def find_earliest_start_time(self, actor):
        """Finds the earliest start time of an actor. Invoked when self.method = MIN"""
        #print("T_i = %s, r_i = %s, alpha = %s" % (actor.get_period(), actor.get_repetition(), self.alpha))
        #assert actor.get_period() * actor.get_repetition() == self.alpha
        v_m = None
        start_time = 0
        if actor.get_inchannels() == [] or actor.all_inchannels_self_loops():
            actor.set_start_time(0)
            return actor # In this case, return yourself
        for c in actor.get_inchannels():
            channel = self.get_channel(c)
            assert channel != None
            if channel.get_source() == channel.get_destination():
                # Self-loop, skip it
                continue
            predecessor = self.get_actor(channel.get_source())
            assert predecessor != None
            if predecessor.get_start_time() == -1:
                return None
            candidates = []
            # Use binary search
            t_i = predecessor.get_start_time()
            t_min = 0
            alpha = max(predecessor.get_repetition() * predecessor.get_period(), \
                        actor.get_repetition() * actor.get_period())
            t_max = t_i + alpha
            candidates.append(t_max) # fail-safe value
            if 0 < t_i and actor.cnsS(channel, 0, t_i) > 0:
                t_min = t_i
            while t_min < t_max:
                found = True
                t_j = (t_min + t_max)//2 # t_j is the pivot value
                t_prime = max(t_i, t_j)
                for t_hat in range(0, alpha + 1, actor.get_period()):
                    prd_i = predecessor.prdS(channel, t_i , t_prime + t_hat)
                    cns_j = actor.cnsS(channel, t_j, t_prime + t_hat )
                    net_tokens = prd_i - cns_j
                    if net_tokens < 0:
                        found = False
                        break
                if not found:
                    t_min = t_j + 1
                else:
                    candidates.append(t_j)
                    t_max = t_j
            st = min(candidates)
            if st > start_time:
                start_time = st
                v_m = predecessor
        
        assert v_m != None, "ERROR: predecessor cannot be found!"
        actor.set_start_time(start_time)
        return v_m

    def find_earliest_start_time_vector(self):
        """Computes the earliest start time of all the actors.

        This function implements Algorithm 2 in [BS2012]_
        
        .. [BS2012]
            Mohamed A. Bamakhrama and Todor Stefanov. *Managing Latency in Embedded 
            Streaming Applications under Hard-Real-Time Scheduling*. 
            In Proceedings of the 10th IEEE/ACM/IFIP International Conference on 
            Hardware/Software Codesign and System Synthesis (CODES+ISSS 2012), 
            pp. 83-92, October 7-12, 2012, Tampere, Finland. 
            DOI: http://dx.doi.org/10.1145/2380445.2380464
        """
        done = False
        while not done:
            for actor in self.get_actors():
                if actor.get_start_time() == -1:
                    v_m = None
                    while True:
                        v_m = self.find_earliest_start_time(actor)
                        if v_m != None:
                            if v_m.get_actor_id() == actor.get_actor_id():
                                break
                            else:
                                v_m.set_deadline(v_m.get_wcet() + int(self.get_deadline_factor() * (v_m.get_period() - v_m.get_wcet())))
                                v_k = self.find_earliest_start_time(actor)
                                assert v_k != None
                                if v_k.get_actor_id() == v_m.get_actor_id():
                                    break
                        else:
                            break
            done = True
            for actor in self.get_actors():
                if actor.get_start_time() == -1:
                    done = False
                    break

        for a in self.get_actors():
            if a.get_outchannels() == []:
                a.set_deadline(a.get_wcet() + int(self.get_deadline_factor() * (a.get_period() - a.get_wcet())))
        self.compute_latency()

    def get_paths(self,input_actor):
        """Returns a list of all the paths starting from input_actor

        Returns:
            A list of the paths. Each entry in the list is a list of tuples, 
            where each tuple is *(channel_id, source_id, destination_id)*.
        """
        done = False
        paths = [[] for i in range(len(input_actor.get_outchannels()))]
        actors = []
        actors.append(input_actor)
        while not done:
            cactors = list(actors)
            for a in cactors:
                for c in a.get_outchannels():
                    ch = self.get_channel(c)
                    # Exclude self-loops from the paths traversal
                    if ch.get_source() == ch.get_destination():
                        continue
                    actors.append(self.get_actor(ch.get_destination()))
                    for p in paths:
                        if p == [] or p[-1][2] == ch.get_source():
                            p.append((ch.get_channel_id(), ch.get_source(), ch.get_destination()))
                            break
                if a.get_outchannels() != []:
                    actors.remove(a)
            done = True
            for ac in actors:
                if ac.get_outchannels() != []:
                    done = False
                    break
        return paths
        
    def find_all_paths(self, start, end, path = []):
        """Find all paths between two nodes
        input: the start node
               the end node
        output: all paths
        This function is called recursively.
        """
        path = path + [start]
        if start==end:
            return [path]
        paths=[]
        if start.get_outchannels()==[]:
            return []
        for c in start.get_outchannels():
            ch = self.get_channel(c)
            succesor = self.get_actor(ch.get_destination())
            if succesor not in path:
                newpaths=self.find_all_paths(succesor, end, path)
                for newpath in newpaths:
                    paths.append(newpath)
        return paths


    def get_paths_v(self):
        """Return a list of all the paths between the input actor and the output actor

        modified by Di Liu
        
        """
        input_actor=[]
        output_actor=[]
        for a in self.get_actors():
            if a.get_inchannels() == []:
                input_actor = a
            if a.get_outchannels() == []:
                output_actor = a

        paths = self.find_all_paths(input_actor,output_actor)
        allpaths = []
        for p in paths:
            path = []
            for ac in p:
                if ac == output_actor:
                    break
                for c in ac.get_outchannels():
                    ch = self.get_channel(c)
                    if self.get_actor(ch.get_destination()) in p:
                        path.append((ch.get_channel_id(),ch.get_source(),ch.get_destination()))
            allpaths.append(path)
        return allpaths
        

    def compute_latency(self):
        """Computes the maximum latency of the graph and stores it in 
        :code:`self.latency`
        """
        self.latency = 0
        for a in self.get_actors():
            if a.get_outchannels() == [] or a.all_outchannels_self_loops():
                for b in self.get_actors():
                    if b.get_inchannels() == [] or b.all_inchannels_self_loops():
                        # Check if there is a path w_{a->b}
                        paths = self.get_paths(b)
                        for path in paths:
                            if path[-1][2] == a.get_actor_id():
                                # Bingo! Output path
                                first_ch = self.get_channel(path[0][0])
                                last_ch = self.get_channel(path[-1][0])
                                g_i = ChannelModel.empty_firings(first_ch.get_production_sequence())
                                f_j = ChannelModel.empty_firings(last_ch.get_consumption_sequence())
                                input_actor = self.get_actor(path[0][1])
                                output_actor = self.get_actor(path[-1][2])
                                latency = output_actor.get_start_time() + output_actor.get_period() * f_j + output_actor.get_deadline() - (input_actor.get_start_time() + g_i * input_actor.get_period())
                                if latency > self.latency:
                                    self.latency = latency

    def find_minimum_buffer_sizes(self):
        """Finds the minimum buffer size vector under the CDP model. Invoked when self.method = MIN"""
        # The following is an optimization to speed-up buffer size computation 
        # in case of implicit-deadline tasks
        if self.get_utilization() == self.get_density(): # Implicit-deadline
            optimize = True
        else:
            optimize = False

        for ch in self.channels:
            source = self.get_actor(ch.get_source())
            destination = self.get_actor(ch.get_destination())
            if source.get_actor_id() == destination.get_actor_id():
                ch.set_buffer_size(ch.get_initial_tokens())
                continue
            buffer_size_candidates = list()
            buffer_size_candidates.append(1)
            t_max = max(source.get_start_time(), destination.get_start_time())
            init_val = 0
            if source.get_start_time() <= destination.get_start_time():
                init_val = source.prdB(ch, source.get_start_time(), destination.get_start_time())
            if init_val > 0:
                buffer_size_candidates.append(init_val)

            if optimize:
                inc_val = Utilities.gcd(source.get_period(), destination.get_period())
            else:
                #inc_val = 1
                # Validated with StreamIt benchmarks
                inc_val = Utilities.gcdv([source.get_period(), 
                                          source.get_deadline(), 
                                          destination.get_period(), 
                                          destination.get_deadline()])
            for k in range(0, self.alpha + 1, inc_val):
                prd = source.prdB(ch, source.get_start_time(), t_max + k)
                cns = destination.cnsB(ch, destination.get_start_time(), t_max + k)
                unconsumed = prd - cns
                if unconsumed > 0:
                    buffer_size_candidates.append(unconsumed)
            min_buf_size = max(buffer_size_candidates)
            ch.set_buffer_size(min_buf_size)

    def calc_start_time_buffer_vector(self):
        """Computes the start time and buffer size vector"""

        if verbose:
            sys.stdout.write("Computing the earliest start time vector [CDP - Minimal]...\n")
        self.find_earliest_start_time_vector()

        if verbose:
            sys.stdout.write("Computing the minimum buffer sizes [CDP - Minimal]...\n")
        self.find_minimum_buffer_sizes()

#        self.compute_cyclic_executive_parameters()

    def clear(self):
        """Clears the start time and buffer vectors of the graph"""
        for a in self.get_actors():
            a.set_start_time(0)
            a.set_deadline(a.get_period())
        for c in self.get_channels():
            c.set_buffer_size(sys.maxsize)

    def get_execution_vector(self):
        """Return a list containing all the execution times"""
        execution_vector = list()
        for i in self.actors:
            execution_vector.append(i.get_wcet())
        return execution_vector

    def get_execution_vector_str(self):
        """Return the execution time vector as a string"""
        execution_vector = ["["]
        for i in self.actors:
            execution_vector.append("%s_%s = %s, " % (i.get_name(), i.get_function(), i.get_wcet()))
        execution_vector.append("]")
        return ''.join(execution_vector)

    def get_period_vector(self):
        """Return a list containing all the periods"""
        period_vector = list()
        for i in self.actors:
            period_vector.append(i.get_period())
        return period_vector

    def get_period_vector_str(self):
        """Return the period vector as a string"""
        period_vector = ["["]
        for i in self.actors:
            period_vector.append("%s_%s = %s, " % (i.get_name(), i.get_function(), i.get_period()))
        period_vector.append("]")
        return ''.join(period_vector)

    def get_total_buffer_size(self):
        """Return total buffer size"""
        buffer_size = 0
        for ch in self.channels:
            buffer_size += ch.get_buffer_size()
        return buffer_size
        
    def get_buffer_size_vector(self):
        """Return a list containing all the buffer sizes"""
        buffer_vector = list()
        for c in self.channels:
            buffer_vector.append(c.get_buffer_size())
        return buffer_vector

    def get_buffer_size_vector_str(self):
        """Return the buffer size vector as a string"""
        buffer_vector = ["["]
        for c in self.channels:
            buffer_vector.append("%s = %s, " % (c.get_name(), c.get_buffer_size()))
        buffer_vector += "]"
        return ''.join(buffer_vector)

    def get_start_time_vector(self):
        """Return a list containing all the start times"""
        start_time_vector = list()
        for i in self.actors:
            start_time_vector.append(i.get_start_time())
        return start_time_vector

    def get_start_time_vector_str(self):
        """Return the start time vector as a string"""
        start_time_vector = ["["]
        for i in self.actors:
            start_time_vector.append("%s_%s = %s, " % (i.get_name(), i.get_function(), i.get_start_time()))
        start_time_vector.append("]")
        return ''.join(start_time_vector)

    def get_deadline_vector(self):
        """Return a list containing all the deadlines"""
        deadline_vector = list()
        for i in self.actors:
            deadline_vector.append(i.get_deadline())
        return deadline_vector

    def get_deadline_vector_str(self):
        """Return the deadline vector as a string"""
        deadline_vector = ["["]
        for i in self.actors:
            deadline_vector.append("%s_%s = %s, " % (i.get_name(), i.get_function(), i.get_deadline()))
        deadline_vector.append("]")
        return ''.join(deadline_vector)

    def get_repetition_vector(self):
        """Return a list containing all the repetitions"""
        repetition_vector = list()
        for i in self.actors:
            repetition_vector.append(i.get_repetition())
        return repetition_vector

    def get_repetition_vector_str(self):
        """Return the repetition vecotr as a string"""
        repetition_vector = ["["]
        for i in self.actors:
            repetition_vector.append("%s_%s = %s, " % (i.get_name(), i.get_function(), i.get_repetition()))
        repetition_vector.append("]")
        return ''.join(repetition_vector)

    def find_minimum_repetition_vector(self):
        """Reduces the repetition entries to the minimum ones"""
        q_vec = []
        for i in self.actors:
            q_vec.append(i.get_repetition())
        gcd = Utilities.gcdv(q_vec)
        for i in self.actors:
            r = i.get_repetition()/gcd
            i.set_repetition(r)

    def get_major_cycle(self):
        """Returns the major cycle (Used for cyclic executive scheduling)"""
        return self.major_cycle

    def get_minor_cycle(self):
        """Returns the minor cycle (Used for cyclic executive scheduling)"""
        return self.minor_cycle

    def get_utilization(self):
        """Returns the total utilization of the actors in the graph.
        """
        utilization = 0.0
        for i in self.actors:
            utilization = utilization + float(i.get_wcet())/float(i.get_period())
        return utilization

    def get_density(self):
        """Return the total density of the actors in the graph"""
        density = 0.0
        for i in self.actors:
            density = density + float(i.get_wcet())/min(float(i.get_period()), float(i.get_deadline()))
        return density

    def get_max_utilization(self):
        """Returns the maximum utilization factor"""
        max_utilization = 0.0
        for i in self.actors:
            utilization = float(i.get_wcet())/float(i.get_period())
            if (utilization > max_utilization):
                max_utilization = utilization
        return max_utilization
   
    def copy_actor(self, actor_id):
        """Copies the actor into a new one"""
        ac = self.get_actor(actor_id)
        new_ac = ActorModel(self, len(self.actors) + 1, ac.name, ac.function, ac.wcet)
        #new_ac.inports = list(ac.inports)
        #new_ac.outports = list(ac.outports)
        #new_ac.set_repetition(ac.get_repetition())
        
        return new_ac

    def compute_repetition_vector(self):
        """Compute the repetition vector according to the linear time algorithm
        given in page 48 of [BML1996]_
        
        .. [BML1996]
            Shuvra S. Bhattacharyya and Praveen K. Murthy and Edward A. Lee. 
            Software Synthesis from Dataflow Graphs. Vol. 360. Springer, 1996.
        """
        reps = dict({i:fractions.Fraction(0,1) for i in self.get_actors()})
        a_prime = self.get_actors()[0]
        self.set_reps(reps, a_prime, fractions.Fraction(1,1))
        x_l = []
        for key, val in reps.items():
            x_l.append(val.denominator)
        x = fractions.Fraction(int(Utilities.lcmv(x_l)), 1)
        for a in self.get_actors():
            reps[a] *= x
        for e in self.get_channels():
            if reps[self.get_actor(e.get_source())] * sum(e.get_production_sequence()) \
                != reps[self.get_actor(e.get_destination())] * sum(e.get_consumption_sequence()):
                sys.stderr.write("Error! Inconsistent graph\n")
                sys.exit(-1)
        for a in self.get_actors():
            a.set_repetition(reps[a].numerator * a.get_P())

    def set_reps(self, reps, a, n):
        """Used by *compute_repetition_vector*
        """
        reps[a] = n
        for e in self.get_channels():
            if sum(e.get_consumption_sequence()) == 0:
                continue

            if e.get_source() == a.get_actor_id():
                if reps[self.get_actor(e.get_destination())] == fractions.Fraction(0,1):
                    self.set_reps(reps, self.get_actor(e.get_destination()),
                                  fractions.Fraction(n * sum(e.get_production_sequence()),
                                                     sum(e.get_consumption_sequence())))
        
        for e in self.get_channels():
            if sum(e.get_production_sequence()) == 0:
                continue

            if e.get_destination() == a.get_actor_id():
                if reps[self.get_actor(e.get_source())] == fractions.Fraction(0,1):
                    self.set_reps(reps, self.get_actor(e.get_source()),
                        fractions.Fraction(n * sum(e.get_consumption_sequence()),
                                           sum(e.get_production_sequence())))

    @staticmethod
    def load_graph_from_file(filename, scaling_factor=1, deadline_factor=1.0):
        """Loads an ACSDF graph from an gph file

        Returns:
            An ACSDFModel object containing the CSDF graph

        """
        name = os.path.basename(filename).split(".")[0]
        fp = open(filename, 'r')
        cp = CSDFParser(name, fp)
        g = cp.parse_graph()
        g.set_deadline_factor(deadline_factor)
        g.compute_repetition_vector()
        if verbose:
            sys.stdout.write("Computing the minimum period vector...\n")
        #g.find_minimum_repetition_vector()
        g.set_scaling_factor(scaling_factor)
        g.find_minimum_period_vector(g.get_scaling_factor())
        for a in g.get_actors():    # Initial value of deadline
            a.set_deadline(a.get_period())
        # Updates the start time and deadline 
        g.find_earliest_start_time_vector() 

        return g

    @staticmethod
    def print_graph_dot(g, output_dir):
        """Dumps the given graph *g* in dot format into *output_dir*
        """
        graph = os.path.join(output_dir, g.get_graph_name() + ".dot")
        fp = open(graph, 'w')
        fp.write("digraph %s{\n" % (g.get_graph_name()))
        fp.write("rankdir=TB;\n")

        for a in g.get_actors():
            actor = g.get_actor(a.get_actor_id())
            for c in actor.get_outchannels():
                ch = g.get_channel(c)
                successor = g.get_actor(ch.get_destination())
                fp.write("%s_%s_%s_%s [style=filled,color=\"lightblue\"];\n" \
                        % (g.get_graph_name(), actor.get_name(),\
                        actor.get_function(), actor.get_actor_id()))
                fp.write("%s_%s_%s_%s [style=filled,color=\"lightblue\"];\n"\
                         % (g.get_graph_name(), successor.get_name(),\
                         successor.get_function(), successor.get_actor_id()))
                fp.write("%s_%s_%s_%s -> %s_%s_%s_%s;\n" % (g.get_graph_name(),\
                        actor.get_name(), actor.get_function(), actor.get_actor_id(),\
                        g.get_graph_name(), successor.get_name(),\
                        successor.get_function(), successor.get_actor_id()))
        fp.write('label=\"%s\";\n' % (g.get_graph_name()))
        fp.write("}\n")
        fp.flush()
        fp.close()
    
    
    def unfold_sdf(self, unfold_factors = {}, ignorestartbuf = False):
        """
        Unfold an SDF graph with a vector of unfolding factors.
        
        This function implements Algorithm 1 in [ZBS013]_
        
        .. [ZBS013]
            J. T. Zhai, M. A. Bamakhrama, T. Stefanov. 
            "Exploiting Just-enough Parallelism when Mapping Streaming Applications in Hard Real-time Systems". 
            in the Procedding of the 50th IEEE/ACM Design Automation Conference (DAC'13),
            Austin, TX, USA, June 2-6, 2013.
        
        
        Args:
            *graph*: an input SDF graph (:class:`ACSDFModel.ACSDFModel` object).
            *unfold_factors*: a dictionary containing the unfolding factors. 
                              The dictionary structure is as follows:
                              {actor id: unfolding factor, ...}
                              If *unfold_factors* = {}, then the graph is not 
                              unfolded.

        Returns:
            The unfolded graph (a CSDF graph)

        """
        # the given SDF graph is not unfolded
        if unfold_factors == {}:
            return self
        
        unfold_g = ACSDFModel()
        unfold_g.set_deadline_factor(1)
        unfold_g.set_scaling_factor(1)
    
        # Fill unfold_factors with '1' for actors whose unfolding factors are not specified
        for a in self.actors:
            if a.get_actor_id() not in unfold_factors:
                unfold_factors[a.get_actor_id()] = 1
        
        lst_factors = [unfold_factors[i] for i in unfold_factors]
        lcm_unfolding_factor = Utilities.lcmv(lst_factors)  		
        
        # Add all original actors
        for a in self.actors:
            # Intialize a new actor with a new base id
            copy_ac = ActorModel(unfold_g, Utilities.translate_id(a.get_actor_id(),
                                unfold_factors[a.get_actor_id()]),
                                a.get_name(), a.get_function(), a.get_wcet())
            copy_ac.set_repetition(a.get_repetition() * lcm_unfolding_factor)
            copy_ac.set_code_size(a.get_code_size())
            unfold_g.add_actor_ac(copy_ac)
		        
        # Add copies for each actor
        for a_id, f_i in unfold_factors.items():
            if f_i == 1:
                continue

            ac_base_id = Utilities.translate_id(a_id, f_i) # id of first replica or not unfolded actors
            ac = unfold_g.get_actor(ac_base_id)
            ac_rep = ac.get_repetition()
            ac.set_repetition(int(ac_rep / f_i))    # New repetition
            
            # Add f-1 copies excluding the original actor
            for f in range(1, f_i):
                # Set id of replicas in a particular way: new base id + f_i.
                # This guarantees all ids are unique
                new_ac_id = ac_base_id + f
                new_ac = ActorModel(unfold_g, new_ac_id, ac.get_name(),
                                    ac.get_function(), ac.get_wcet())
                new_ac.set_repetition(int(ac_rep / f_i))
                new_ac.set_code_size(ac.get_code_size())
                unfold_g.add_actor_ac(new_ac)
                #print("size unfold_g: " + str(len(unfold_g.get_actors())))

        # Do not construct channels 
        if ignorestartbuf == True:
            return unfold_g
        
        def add_ports_and_channel(graph, ch, src_new_id, snk_new_id, prd_seq, cns_seq):
            # Add input/output ports
            src_port_id = ch.get_source_port().get_port_id()
            snk_port_id = ch.get_destination_port().get_port_id()

            graph.get_actor(src_new_id).add_outport("op" + str(src_port_id),
                                                    src_port_id,
                                                    prd_seq
                                                   )
            graph.get_actor(snk_new_id).add_inport("ip" + str(snk_port_id),
                                                   snk_port_id,
                                                   cns_seq
                                                  )
            ch_new_id = len(graph.get_channels()) + 1
            graph.add_channel(ch_new_id, ch.get_name(),\
                              src_new_id, \
                              graph.get_actor(src_new_id).get_outport(src_port_id),
                              snk_new_id,\
                              graph.get_actor(snk_new_id).get_inport(snk_port_id),
                             )

        # For each channel in the original SDF, add new channels in the unfolded graph
        for ch in self.channels:
            # It has to be an SDF channel
            assert len(ch.get_production_sequence()) == 1 and\
                   len(ch.get_production_sequence()) == 1
            
            src_ac_id = ch.get_source()
            snk_ac_id = ch.get_destination()
            src_base_new_id = Utilities.translate_id(src_ac_id, unfold_factors[src_ac_id])
            snk_base_new_id = Utilities.translate_id(snk_ac_id, unfold_factors[snk_ac_id])

            # Both source and sink actors are not unfolded
            # Add original channel and upate source and sink actors 
            # (mainly due to different id in the unfolded graph)
            if unfold_factors[src_ac_id] == 1 and unfold_factors[snk_ac_id] == 1:
                add_ports_and_channel(unfold_g, ch, src_base_new_id, snk_base_new_id,
                                      ch.get_production_sequence(), ch.get_consumption_sequence())
                continue

            prd = ch.get_production_sequence()[0]
            cns = ch.get_consumption_sequence()[0]
            lcm_pc = Utilities.lcm(prd, cns)
            
            # Determine the number of Output Ports (OP) for each source replica and 
            # number of Input Ports (IP) for each sink replica.
            # We perform optimization on #channels if #source replicas
            # is not co-prime to #sink replicas.
            nr_OP = -1
            nr_IP = -1
            if unfold_factors[src_ac_id] % unfold_factors[snk_ac_id] == 0: 
                # If the number of source replicas is dividable by the number of sink replicas,
                # each source replica is only connected only one sink replica, namely with one OP
                nr_OP = 1
                # each sink replica is connected to nr_IP source replicas, namely with nr_IP IPs 
                nr_IP = int(unfold_factors[src_ac_id] / unfold_factors[snk_ac_id])
            elif unfold_factors[snk_ac_id] % unfold_factors[src_ac_id] == 0:
                # If the number of sink replicas is dividable by the number of source replicas,
                # each source replica is connected to the following number of sink replicas
                nr_OP = int(unfold_factors[snk_ac_id] / unfold_factors[src_ac_id])
                nr_IP = 1
                #print(str(unfold_factors[snk_ac_id]) + ", src f: " + str(unfold_factors[src_ac_id]))
            else:
                # Each source replica is connected to all sink replicas
                nr_OP = unfold_factors[snk_ac_id]
                nr_IP = unfold_factors[src_ac_id]
            assert(nr_OP > 0 and nr_IP > 0)
            
            # for each replica of the source actor
            for f in range(0, unfold_factors[src_ac_id]):
                # for each newly added channel
                for k in range(0, nr_OP):   
                    # Compute new source replica id
                    src_new_id = src_base_new_id + f
                    # Source port id
                    src_port_id = len(unfold_g.get_actor(src_new_id).get_outports())
                    
                    # Generate a production sequence of length rep(A_src)
                    rep_src = unfold_g.get_actor(Utilities.translate_id(src_ac_id,
                                                unfold_factors[src_ac_id])).get_repetition()
                    prd_seq = [0] * rep_src
                    nr_prd = int(lcm_pc / prd)      # number of production rates in one occurrence 
                    nr_occur = int(rep_src / (nr_OP*nr_prd))  # number of occurence in the production sequence
                    # Essentially we need to fill "nr_prd" times "prd" in one so-called occurrence,
                    # and "nr_occur" times occurrences should appear in the final production sequence
                    # assume that prd = 2, nr_OP = 2, nr_prd = 3, and rep(A_src) = 12, thus nr_occur = 2
                    # one occurrence looks like: for 1st new port with [2, 2, 2, 0, 0, 0], and 
                    # for 2nd new channel with [0, 0, 0, 2, 2, 2]
                    # But this pattern repeats twice (nr_occur = 2) in the final production sequence
                    # of length 12 (rep(A_src) = 12)
                    # That is, e.g., for the 1st new channel: [2, 2, 2, 0, 0, 0, 2, 2, 2, 0, 0, 0]
                    for r in range(0, nr_occur):
                        for pd in range(0, nr_prd):
                            # Fill nr_prd locations with "prd"
                            # k = 1 from 0 to nr_prd-1, k = 2 from nr_prd to 2*nr_prd - 1
                            prd_seq[r*nr_OP*nr_prd + k*nr_prd + pd] = prd
                    
                    # Compute sink actor id 
                    snk_new_id = -1
                    if unfold_factors[src_ac_id] % unfold_factors[snk_ac_id] == 0:
                        snk_new_id = Utilities.translate_id(snk_ac_id, unfold_factors[snk_ac_id])\
                                     + int(f/nr_IP)
                    elif unfold_factors[snk_ac_id] % unfold_factors[src_ac_id] == 0:
                        snk_new_id = Utilities.translate_id(snk_ac_id, unfold_factors[snk_ac_id])\
                                     + f*nr_OP + k
                    else:
                        snk_new_id = Utilities.translate_id(snk_ac_id, unfold_factors[snk_ac_id])\
                                     + k
                    assert(snk_new_id != -1)
                    # sink port id
                    snk_port_id = len(unfold_g.get_actor(snk_new_id).get_inports())
                    
                    # Generate a consumption sequence of length rep(A_snk)
                    snk_ac = unfold_g.get_actor(Utilities.translate_id(snk_ac_id,
                                                                       unfold_factors[snk_ac_id]))
                    rep_snk = snk_ac.get_repetition()
                    cns_seq = [0] * rep_snk
                    nr_cns = int(lcm_pc / cns)
                    nr_occur = int(rep_snk / (nr_IP*nr_cns))
                    # print("snk port id = %r, nr_IP = %r, nr_cns = %r" %\
                    #      (snk_port_id, nr_IP, nr_cns))
                    for r in range(0, nr_occur):
                        for cs in range(0, nr_cns):
                            cns_seq[r*nr_IP*nr_cns + snk_port_id*nr_cns + cs] = cns
                    
                    # print("src = %r, snk = %r" % (unfold_g.get_actor(src_new_id),\
                    #       unfold_g.get_actor(snk_new_id)))
                    # print("prd = %r, cns = %r" % (prd_seq, cns_seq))
                    
                    add_ports_and_channel(unfold_g, ch, src_new_id, snk_new_id, prd_seq, cns_seq)
        
        return unfold_g
    
    # def __check_graph(self):


    def find_upper_bounds_unfolding_factors(self):
        """
        Find the upper bound of unfolding factor for each actor as proposed in [ZBS013]_.
        
        Args:

        Returns:
            a dictionary {actor id : upper bound of unfolding factor}

        """
        # a dictionary {actor id : upper bound of unfolding factor}
        rt_dic = {}
        
        # TODO: we can have more efficient implementation using vector 
        q_C_vec = []
        for ac in self.actors:
            assert len(ac.get_wcet_sequence()) == 1, "Error: Unfolding can only be applied on SDF graphs."
            q_C_vec.append(ac.get_repetition() * ac.get_wcet()) 
            #rt_dic[ac.get_actor_id()] = ac.get_repetition() * ac.get_wcet()
        lcm_q_C = Utilities.lcmv(q_C_vec)
        
        # compute x for each actor
        x_vec = []
        for ac in self.actors:
            x_vec.append(lcm_q_C / (ac.get_repetition() * ac.get_wcet())) 
            rt_dic[ac.get_actor_id()] = lcm_q_C / (ac.get_repetition() * ac.get_wcet())
        lcm_x = Utilities.lcmv(x_vec)
        
        for ac in self.actors:
            rt_dic[ac.get_actor_id()] = int(lcm_x / rt_dic[ac.get_actor_id()]) # lcm(x_vec) / x_i
        
        return rt_dic
        
    def __repr__(self):
        s = []
        for i in self.actors:
            s.append("%s\n" % (repr(i)))
        for i in self.channels:
            s.append("%s\n" % (repr(i)))
        return ''.join(s)

def main():
    """Main method in ACSDFModel. Used to run the script in standalone mode"""

    parser = argparse.ArgumentParser(description=__description__, epilog="Author: %s" % __author__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-m", nargs=1, type=float, default=1.0, metavar="deadline_factor", help="Compute the minimum start time and buffer size using the given deadline factor. The factor value must be in the range [0,1]. The deadline is computed as follows: D_i = C_i + factor*(T_i - C_i), where D_i is the deadline of task f_i, T_i is the period of f_i, and C_i is the WCET of f_i")
    group.add_argument("-s", action="store_true", help="Compute the sufficient start time and buffer size")
    parser.add_argument("-f", nargs=1, type=int, default=1, metavar="period_factor", help="Scale the periods with the given period_factor")
    parser.add_argument("graph", help="The path to the CSDF graph")
    args = parser.parse_args()

    if not os.path.exists(args.graph):
        sys.stderr.write("Error! the provided path does not exist!\n")
        sys.exit(-1)

    if args.s:
        deadline_factor = 0 # Unused in the sufficient case
    else:
        deadline_factor = args.m[0]
        if deadline_factor < 0.0 or deadline_factor > 1.0:
            sys.stderr.write("Invalid value for deadline factor! Must be value between 0.0 and 1.0\n")
            sys.exit(-1)

    if type(args.f) == int:
        period_factor = int(args.f)
    else:
        period_factor = int(args.f[0])

    sys.stdout.write(" - Processing application " + args.graph + "...\n")
    g = ACSDFModel.load_graph_from_file(args.graph, period_factor, deadline_factor)
    g.calc_start_time_buffer_vector()

    sys.stdout.write(" -- Number of actors = %s\n" % (g.get_num_of_actors()))
    sys.stdout.write(" -- Repetition vector = %s\n" % (g.get_repetition_vector_str()))
    sys.stdout.write(" -- Worst-case execution time vector = %s\n" % (g.get_execution_vector_str()))
    sys.stdout.write(" -- Minimum period vector = %s\n" % (g.get_period_vector_str()))
    sys.stdout.write(" -- Deadline vector = %s\n" % (g.get_deadline_vector_str()))
    sys.stdout.write(" -- Start time vector = %s\n" % (g.get_start_time_vector_str()))
    sys.stdout.write(" -- Buffer size vector = %s\n" % (g.get_buffer_size_vector_str()))
    sys.stdout.write(" -- Total Utilization  = %s\n" % (g.get_utilization()))
    sys.stdout.write(" -- Total Density = %s\n" % (g.get_density()))
    sys.stdout.write(" -- Graph maximum latency = %s\n" % (g.get_latency()))

    total_buffer_size = 0
    for c in g.get_channels():
        total_buffer_size += c.get_buffer_size()
    sys.stdout.write(" -- Total buffer size = %s\n" % (total_buffer_size))

    ACSDFModel.print_graph_dot(g,os.getcwd())
        
# Entry point
if __name__ == "__main__":
    main()

