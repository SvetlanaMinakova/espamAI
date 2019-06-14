#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Channel Model for CSDF Graph'

import sys

class ChannelModel:
    """
    ChannelModel class.

    Represents a communication channel in a CSDF graph
    """

    def __init__(self, channel_id, name, source, source_port, 
                 destination, destination_port):
        """Constructor.

        Args:
            *channel_id*: The channel ID (integer).
            *name*: The channel name (string).
            *source*: The source actor ID (integer).
            *source_port*: The source actor port 
                            (:class:`ActorModel.Port` object)
            *destination*: The destination actor ID (integer)
            *destination_port*: The destination actor port
                                (:class:`ActorModel.Port` object)

        """
        self.name = name
        self.channel_id = channel_id
        self.source = source
        self.source_port = source_port
        self.destination= destination
        self.destination_port = destination_port
        self.buffer_size = sys.maxsize
        self.initial_tokens = 0

    def set_id(self, ch_id):
        """Set the id of the channel"""
        self.channel_id = ch_id

    def set_source(self, source):
        """Get the source actor of the channel"""
        self.source = source

    def set_destination(self, destination):
        """Get the destination actor"""
        self.destination = destination

    def set_source_port(self, source_port):
        """Set the source port"""
        self.source_port = source_port

    def set_destination_port(self, destination_port):
        """Set the destination port"""
        self.destination_port = destination_port

    def set_production_sequence(self, production_rate):
        """Set the production rates sequence"""
        self.source_port.set_sequence(production_rate)

    def set_consumption_sequence(self, consumption_sequence):
        """Set the consumption rates sequence"""
        self.destination_port.set_sequence(consumption_sequence)

    def set_buffer_size(self, buffer_size):
        """Set the channel buffer size"""
        assert buffer_size > 0
        self.buffer_size = buffer_size

    def set_initial_tokens(self, initial_tokens):
        """Set the value of initial tokens"""
        assert type(initial_tokens) == int and initial_tokens >= 0
        self.initial_tokens = initial_tokens

    def set_name(self, name):
        """Set the name of the channel"""
        self.name = name

    @staticmethod
    def empty_firings(sequence):
        """Finds the index of the first zero rate in a prod/cons. sequence"""
        index = -1
        for i in range(0, len(sequence)):
            if sequence[i] > 0:
                index = i
                break
        return index

    def is_self_channel(self):
        """Returns :code:`True` if the channel is a self-channel (i.e., 
        The source and destination actors are the same)
        """
        if self.source == self.destination:
            return True
        else:
            return False

    def get_initial_tokens(self):
        """Returns the value of initial tokens"""
        return self.initial_tokens

    def get_name(self):
        """Returns the channel name"""
        return self.name

    def get_channel_id(self):
        """Returns the channel ID"""
        return self.channel_id

    def get_buffer_size(self):
        """Returns the channel buffer size"""
        return self.buffer_size

    def get_source(self):
        """Returns the source actor ID"""
        return self.source

    def get_destination(self):
        """Returns the destination actor ID"""
        return self.destination

    def get_source_port(self):
        """Returns the source actor port object"""
        return self.source_port

    def get_destination_port(self):
        """Returns the destination actor port object"""
        return self.destination_port

    def get_production_sequence(self):
        """Returns the production rates sequence"""
        return self.source_port.get_sequence()

    def get_consumption_sequence(self):
        """Returns the consumption rates sequence"""
        return self.destination_port.get_sequence()

    def __repr__(self):
        return "Channel %s : source = %s, destination = %s, " \
                "production seq. = %s, consumption seq. = %s, init_tokens = %s" % \
                (self.name, self.source, self.destination, 
                self.get_production_sequence(), self.get_consumption_sequence(), 
                self.get_initial_tokens())

