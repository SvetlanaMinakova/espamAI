#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Actor Model for CSDF Graph'


import math

import PlatformParameters
import Utilities

class Port:
    """
    Port class

    Represents a port in a CSDF actor.
    """
    def __init__(self, name, port_id, port_type, sequence):
        """Constructor.
        
        Args:
            *name*: the port name (string)
            *port_id*: the port ID (integer)
            *port_type*: a string that can be either :code:`"in"` or 
                        :code:`"out"`
            *sequence*: a list containing the production/consumption rates
        """
        assert type(name) == str
        assert type(port_id) == int
        assert type(port_type) == str and \
                (port_type == "in" or port_type == "out")
        assert type(sequence) == list
        self.name = name
        self.port_id = port_id
        self.port_type = port_type
        self.sequence = sequence

    def get_name(self):
        """Returns the port name"""
        return self.name

    def get_port_id(self):
        """Returns the port ID"""
        return self.port_id

    def get_port_type(self):
        """Returns the port type"""
        return self.port_type

    def get_sequence(self):
        """Returns the rates sequence associated with the port"""
        return self.sequence

    def set_port_name(self, port_name):
        """Sets the port name to *port_name*"""
        self.name = port_name

    def set_port_id(self, port_id):
        """Sets the port ID to *port_id*"""
        self.port_id = port_id

    def set_port_type(self, port_type):
        """Sets the port type to *port_type*"""
        self.port_type = port_type

    def set_sequence(self, port_sequence):
        """Sets the port rates sequence to *port_sequence*"""
        self.sequence = port_sequence


class ActorModel:
    """ActorModel class.

    Represents an actor in a CSDF graph together with its real-time scheduling
    parameters (e.g., period, deadline)
    """

    def __init__(self, graph, actor_id, name, function, wcet):
        """Constructor.

        Args:
            *graph*: The graph to which the actor belongs 
                    (:class:`ACSDFModel.ACSDFModel` object)
            *actor_id*: The actor ID (integer)
            *name*: The actor name (string)
            *function*: The name of the function associated with the actor (str)
            *wcet*: The worst-case execution time of the actor (integer)

        """
        self.graph = graph
        self.actor_id = actor_id
        # global_id is used in PlatformGenerator when mapping multiple applications
        self.global_id = actor_id
        self.name = name
        self.function = function
        self.wcet = wcet
        # io_time is used to calibrate the total WCET based on the actual
        # mapping
        self.io_time = 0        
        self.period = 0
        # start time (in absolute time)
        self.start_time = -1
        # Offset used in scenario change
        self.offset = 0
        # relative deadline
        self.deadline = 0
        # absolute release time (in absolute time). Used by Schedulers only
        self.abs_release = 0
        # absolute deadline time (in absolute time). Used by Schedulers only
        self.abs_deadline = 0
        # received cpu time (in time units). Used by Schedulers only
        self.received_time = 0
        self.utilization = 0.0
        self.density = 0.0
        # Used in EspamHandler to assign priorities
        self.priority = 0
        self.repetition = 0
        # wcet_sequence is set by CSDFParser. self.wcet = max(self.wcet_sequence)
        self.wcet_sequence = []
        # The length of the actor sequences
        self.P = 0
        self.contention = 0
        # The processor on which the actor is mapped
        self.affinity = -1
        # The total code size of the actor
        self.code_size = 0
        # Indicate whether the actor is stateful or not. 
        # Used in the unfolding algorithm and PHRT
        self.stateful = False
        # The list of input ports (list of Port objects)
        self.inports = []
        # The list of output ports (list of Port objects)
        self.outports = []      
        # The list of input channels IDs (list of integers)
        self.inchannels = []
        # The list of output channels IDs (list of integers)
        self.outchannels = []
        
    def get_actor_id(self):
        """Returns the actor ID"""
        return self.actor_id

    def get_global_id(self):
        """Returns the global actor ID"""
        return self.global_id

    def get_graph(self):
        """Returns the graph to which the actor belongs"""
        return self.graph

    def get_name(self):
        """Returns the actor name"""
        return self.name

    def get_full_name(self):
        """Returns the full actor name.

        Returns:
            A string composed of: graphname_actorname
        """
        return "%s_%s" % (self.get_graph().get_graph_name(), self.name)

    def get_function(self):
        """Returns the function name"""
        return self.function

    def get_start_time(self):
        """Returns the start time of the actor"""
        return self.start_time

    def get_offset(self):
        """Returns offset of the actor"""
        return self.offset

    def get_wcet_sequence(self):
        """Returns the WCET sequence"""
        return self.wcet_sequence

    def set_code_size(self, code_size):
        """Sets the code size of the actor"""
        self.code_size = code_size
    
    def get_code_size(self):
        """Returns the code size of the actor"""
        return self.code_size

    def set_stateful(self, stateful):
        """Sets the stateful status to *stateful*"""
        self.stateful = stateful

    def get_stateful(self):
        """Returns the stateful status"""
        return self.stateful

    def get_inport(self, port_id):
        """Returns the input port with ID equal to the given *port_id*

        Returns:
            An input port with ID equal to *port_id*. :code:`None` if no 
            match is found
        """
        for p in self.inports:
            if p.get_port_id() == port_id:
                return p
        return None

    def get_outport(self, port_id):
        """Returns the output port with ID equal to the given *port_id*

        Returns:
            An output port with ID equal to *port_id*. :code:`None` if no 
            match is found
        """
        for p in self.outports:
            if p.get_port_id() == port_id:
                return p
        return None

    def get_inport_by_name(self, port_name):
        """Returns the input port with name equal to the given *port_name*

        Returns:
            An input port with name equal to *port_name*. :code:`None` if no 
            match is found
        """
        for p in self.inports:
            if p.get_name() == port_name:
                return p
        return None

    def get_outport_by_name(self, port_name):
        """Returns the output port with name equal to the given *port_name*

        Returns:
            An output port with name equal to *port_name*. :code:`None` if no 
            match is found
        """
        for p in self.outports:
            if p.get_name() == port_name:
                return p
        return None

    def get_inports(self):
        """Returns the list of input ports"""
        return self.inports

    def get_outports(self):
        """Returns the list of output ports"""
        return self.outports

    def get_inchannels(self):
        """Returns the list of input channels IDs"""
        return self.inchannels

    def get_outchannels(self):
        """Returns the list of output channels IDs"""
        return self.outchannels

    def get_period(self):
        """Returns the actor period"""
        return self.period

    def get_repetition(self):
        """Returns the actor repetition"""
        return int(self.repetition)

    def get_wcet(self):
        """Returns the actor WCET.
        
        Returns:
            WCET in clock cycles if :class:`PlatformParameters.TIME_IN_CYCLES`
            is set to :code:`True`. Otherwise, it returns the WCET is OS clock
            ticks. 
            The returned WCET is equal to :code:`self.wcet + self.io_time`

        """
        if PlatformParameters.TIME_IN_CYCLES:
            time_in_cycles = self.wcet + self.io_time
            return time_in_cycles
        else:
            time_in_ticks = int(math.ceil(float(self.wcet + self.io_time) / \
                            float(PlatformParameters.CYCLES_IN_TICK)))
            return time_in_ticks

    def all_inchannels_self_loops(self):
        """Returns True if all the incoming channels are self-loops"""
        for c in self.get_inchannels():
            src = self.get_graph().get_channel(c).get_source()
            dst = self.get_graph().get_channel(c).get_destination()
            assert dst == self.get_actor_id()
            if src != dst:
                return False
        return True

    def all_outchannels_self_loops(self):
        """Returns True if all the outgoing channels are self-loops"""
        for c in self.get_outchannels():
            src = self.get_graph().get_channel(c).get_source()
            dst = self.get_graph().get_channel(c).get_destination()
            assert src == self.get_actor_id()
            if src != dst:
                return False
        return True

    def get_deadline(self):
        """Returns the actor deadline"""
        return self.deadline

    def get_priority(self):
        """Returns the actor priority"""
        return self.priority

    def get_utilization(self):
        """Returns the utilization of the actor"""
        self.utilization = float(self.get_wcet())/float(self.get_period())
        return self.utilization

    def get_density(self):
        """Returns the density of the actor"""
        self.density = float(self.get_wcet())/min(float(self.get_period()), 
                            float(self.get_deadline()))
        return self.density

    def get_contention(self):
        """Returns the contention encountered by the actor"""
        return self.contention

    def get_affinity(self):
        """Returns the processor on which the actor is located"""
        return self.affinity

    def set_affinity(self, affinity):
        """Sets the actor affinity to *affinity*"""
        self.affinity = affinity

    def set_contention(self, contention):
        """Sets the contention to *contention*"""
        self.contention = contention

    def set_wcet_sequence(self, wcet_sequence):
        """Sets the WCET sequence to *wcet_sequence*"""
        self.wcet_sequence = wcet_sequence

    def set_global_id(self, global_id):
        """Sets the global ID to *global_id*"""
        self.global_id = global_id

    def set_priority(self, priority):
        """Sets the priority to *priority*"""
        self.priority = priority

    def set_start_time(self, start_time):
        """Sets the start time to *start_time*"""
        self.start_time = start_time

    def set_offset(self, offset):
        """Sets offset to *offset*"""
        self.offset = offset

    def set_period(self, period):
        """Sets the period to *period*"""
        self.period = period

    def set_repetition(self, repetition):
        """Sets the repetition to *repetition*"""
        self.repetition = int(repetition)

    def set_wcet(self, wcet):
        """Sets the WCET to *wcet*"""
        self.wcet = wcet

    def set_deadline(self, deadline):
        """Sets the deadline to *deadline*"""
        self.deadline = deadline

    def add_inport(self, inport_name, inport_id, inport_sequence):
        """Adds input port to the actor

        Args:
            *inport_name*: The input port name (string).
            *inport_id*: The input port ID (integer).
            *inport_sequence*: The consumption rates sequence
        
        """
        inport = Port(inport_name, inport_id, "in", inport_sequence)
        self.inports.append(inport)

    def add_outport(self, outport_name, outport_id, outport_sequence):
        """Adds output port to the actor

        Args:
            *outport_name*: The output port name (string).
            *outport_id*: The output port ID (integer).
            *outport_sequence*: The production rates sequence
        
        """
        outport = Port(outport_name, outport_id, "out", outport_sequence)
        self.outports.append(outport)

    def add_inchannel(self, inchannel_id):
        """Add *inchannel_id* to the list of input channels"""
        assert self.get_graph().get_channel(inchannel_id).get_destination() == \
                self.get_actor_id()
        assert self.get_graph().get_channel(inchannel_id).get_destination_port() \
                in self.get_inports()
        self.inchannels.append(inchannel_id)

    def add_outchannel(self, outchannel_id):
        """Add *outchannel_id* to the list of output channels"""
        assert self.get_graph().get_channel(outchannel_id).get_source() == \
                self.get_actor_id()
        assert self.get_graph().get_channel(outchannel_id).get_source_port() \
                in self.get_outports()
        self.outchannels.append(outchannel_id)

    def remove_inchannel(self, inchannel_id):
        """Remove *inchannel_id* from the list of input channels"""
        self.inchannels.remove(inchannel_id)

    def remove_outchannel(self, outchannel_id):
        """Remove *outchannel_id* from the list of output channels"""
        self.outchannels.remove(outchannel_id)

    def prdS(self, channel, start_time, end_time):
        """Returns the cumulative number of produced tokens in the 
        interval [start_time, end_time) for computing start time"""
        if end_time <= start_time:
            return channel.get_initial_tokens()
        firings = int(math.floor(float(end_time - start_time) / \
                     float(self.get_period())))
        if (end_time - start_time) % self.get_period() >= self.get_deadline():
            firings = firings + 1
        produced_tokens = channel.get_initial_tokens()
        prd_seq = channel.get_production_sequence()
        prd_seq_len = len(prd_seq)
        for k in range(0,firings,1):
            produced_tokens = produced_tokens + prd_seq[k % prd_seq_len]
        return produced_tokens

    def prdB(self, channel, start_time, end_time):
        """Returns the cumulative number of produced tokens in the 
        interval [start_time, end_time) for computing buffer size"""
        if end_time <= start_time:
            return channel.get_initial_tokens()
        firings = int(math.ceil(float(end_time - start_time) / \
                     float(self.get_period())))
        if (end_time - start_time) % self.get_period() == 0:
            firings = firings + 1
        produced_tokens = channel.get_initial_tokens()
        prd_seq = channel.get_production_sequence()
        prd_seq_len = len(prd_seq)
        for k in range(0,firings,1):
            produced_tokens = produced_tokens + prd_seq[k % prd_seq_len]
        return produced_tokens

    def cnsS(self, channel,  start_time, end_time):
        """Returns the cumulative number of consumed tokens under in the 
        interval [start_time, end_time] for computing start time"""
        if end_time < start_time:
            return 0
        firings = int(math.ceil(float((end_time - start_time)) / \
                      float(self.get_period())))
        if ((end_time - start_time) % self.get_period() == 0 and \
            firings < self.get_repetition()):
            firings = firings + 1
        consumed_tokens = 0
        cns_seq = channel.get_consumption_sequence()
        cns_seq_len = len(cns_seq)
        for k in range(0,firings,1):
            consumed_tokens = consumed_tokens + cns_seq[k % cns_seq_len]
        return consumed_tokens

    def cnsB(self, channel,  start_time, end_time):
        """Returns the cumulative number of consumed tokens under in the 
        interval [start_time, end_time) for computing buffer size"""
        if end_time < start_time:
            return 0
        firings = int(math.floor(float((end_time - start_time)) / \
                      float(self.get_period())))
        if (end_time - start_time) % self.get_period() >= self.get_deadline():
            firings = firings + 1
        consumed_tokens = 0
        cns_seq = channel.get_consumption_sequence()
        cns_seq_len = len(cns_seq)
        for k in range(0,firings,1):
            consumed_tokens = consumed_tokens + cns_seq[k % cns_seq_len]
        return consumed_tokens

    def get_P(self):
        """Returns the least-common-multiple of the length of all production
        and consumption sequences"""
        if self.P == 0:
            l = []
            for c in self.get_inchannels():
                ch = self.get_graph().get_channel(c)
                l.append(len(ch.get_consumption_sequence()))
            for c in self.get_outchannels():
                ch = self.get_graph().get_channel(c)
                l.append(len(ch.get_production_sequence()))
            self.P = int(Utilities.lcmv(l))
        return self.P

    def update_execution_time(self, interconnect_type, word_read_time, 
                                word_write_time):
        """Updates the WCET of the actor based on the given *interconnect_type*,
        *word_read_time*, and *word_write_time*

        Args:
            *interconnect_type*: Can be either :class:`PlatformParameters.P2P` 
                                or :class:`PlatformParameters.AXI_CB`
            *word_read_time*: The time (in clock cycles) needed to read a 32-bit
                                over the specified interconnect
            *word_write_time*: The time (in clock cycles) needed to write a 
                                32-bit over the specified interconnect

        """
        P = self.get_P()
        total_time = 0
        for k in range(P):
            read_tokens = 0
            written_tokens = 0
            for c in self.get_inchannels():
                ch = self.get_graph().get_channel(c)
                cs = ch.get_consumption_sequence()
                read_tokens += cs[k % len(cs)]
            for c in self.get_outchannels():
                ch = self.get_graph().get_channel(c)
                ps = ch.get_production_sequence()
                written_tokens += ps[k % len(ps)]
            if interconnect_type == PlatformParameters.P2P:
                read_time = read_tokens * self.get_graph().get_max_token_size() * \
                            word_read_time
                write_time = written_tokens * \
                             self.get_graph().get_max_token_size() * word_write_time
                total = read_time + write_time
                if total > total_time:
                    total_time = total
            elif interconnect_type == PlatformParameters.AXI_CB:
                read_time = read_tokens * self.get_graph().get_max_token_size() * \
                            (self.get_contention() + 1) * word_read_time
                # No contention in writes on AXI crossbar
                write_time = written_tokens * \
                             self.get_graph().get_max_token_size() * word_write_time
                total = read_time + write_time
                if total > total_time:
                    total_time = total
            else:
                assert False
        if total_time > 0:
            self.io_time = total_time
        else:
            self.io_time = 0

    def __repr__(self):
        s = "Actor %s : ID = %s, repetition = %s, wcet = %s" % (self.name, self.actor_id, 
            self.repetition, self.wcet)
        return s

