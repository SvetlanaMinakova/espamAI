#!/usr/bin/python

__author__         = 'Emanuele Cannella'
__description__    = "Plot utilities to visualize the periodic task set"

import os 
import sys
import shutil
import math
import argparse
#import subprocess

import Utilities
from ACSDFModel import ACSDFModel

def dump_periodic_schedule_plot_data(g):
    """File-dumps the whole periodic taskset in a format compatible with GRASP
    and buffer levels compatible with gnuplot"""

    # Dump GRASP header which defines task names
    grasp_file = open(g.get_graph_name()+'.grasp', 'w')
    for act in g.get_actors():
        grasp_file.write("newTask task_%d -priority %d -name \"task_%d\" -color grey%d\n" % \
                  (act.get_actor_id(), act.get_actor_id(), act.get_actor_id(), \
                  act.get_actor_id()*100/(g.get_num_of_actors())))
    grasp_file.write("\n")

    # Create the 'ASAP' schedule events of the periodic taskset
    schedule = []
    for act in g.get_actors():
        update_schedule(act, schedule)

    # Sort the events to get a chronological order
    schedule.sort(key=lambda event: event[0])
 
    # Dump periodic schedule to GRASP
    dump_taskset_schedule(schedule, grasp_file)

    # Dump channel levels to .dat files
    for ch in g.get_channels():
        dump_buffer_level(schedule, ch.get_channel_id())

    grasp_file.close()


def update_schedule(act, schedule):
    """Updates the list of events with the events of actor 'act'"""
    # Assumes ASAP scheduling of the periodic actors
    # (actor is executed as soon as it is released)
    # However, to find worst-case buffer size, the consumption happens as late as possible
    period = act.get_period()
    act_id = act.get_actor_id()
    for i in range(0, 4*act.get_repetition()):
        t_rel = act.get_start_time() + period*i
        t_compl = act.get_start_time() + period*i + act.get_wcet()
        # worst-case write (as soon as possible, for buffer size)
        t_worst_wr = act.get_start_time() + period*i
        # worst-case read (as late as possible, for buffer size)
        t_worst_rd = act.get_start_time() + period*(i+1)

        schedule.append((t_rel, "REL", act_id, i));
        schedule.append((t_rel, "EXE", act_id, i));

        for c in act.outchannels:
            channel = act.get_graph().get_channel(c)
            prod_sequence = channel.get_production_sequence()
            schedule.append((t_worst_wr, "PRD", channel.get_channel_id(), \
                            prod_sequence[i%len(prod_sequence)]))
        
        schedule.append((t_compl, "FIN", act_id, i))

        for c in act.inchannels:
            channel = act.get_graph().get_channel(c)
            cons_sequence = channel.get_consumption_sequence()
            schedule.append((t_worst_rd, "CNS", channel.get_channel_id(), \
                            cons_sequence[i%len(cons_sequence)]))


def dump_taskset_schedule(schedule, out_file):
    """GRASP-file-dumps the global schedule of the periodic taskset"""
    for event in schedule:
        if event[1]=='REL':
            out_file.write("plot %d jobArrived job%d.%d task_%d\n" % \
                      (event[0], event[2], event[3], event[2]))
        elif event[1]=='EXE':
            out_file.write("plot %d jobResumed job%d.%d\n" % \
                      (event[0], event[2], event[3]))
        elif event[1]=='FIN':
            out_file.write("plot %d jobCompleted job%d.%d\n" % \
                      (event[0], event[2], event[3]))


def dump_buffer_level(schedule, ch_id):
    """Dumps the 'filling level' of a channel as the periodic schedule execute"""
    buffer_events = []
    buffer_level = 0
    buffer_events.append((0, buffer_level))

    for event in schedule:
        if event[1]=='PRD' or event[1]=='CNS':
            if event[2]==ch_id:
                if event[1]=='PRD':
                    buffer_level += event[3]
                else:
                    buffer_level -= event[3]
                buffer_events.append((event[0], buffer_level))

    # Removing double entries (if any) - only latest value at (time) event[0] remains
    for event in buffer_events:
        event_index = buffer_events.index(event)
        if event_index == len(buffer_events)-1: break
        if event[0] == buffer_events[event_index+1][0]:
            buffer_events.remove(event)
        
    # The out_file can be read with gnuplot ex:'plot "ch_0.dat" with steps'
    out_file = open('ch_'+str(ch_id)+'.dat', 'w')
    for event in buffer_events:
        out_file.write("%d %d\n" % (event[0], event[1]))
    out_file.close()


if __name__ == "__main__":
    #base_dir = "../tests/benchmarks/gph"
    #source_file = "MPEG2noparser"
    base_dir = "../tests/synthetic"
    #source_file = "chain121_src_snk"
    source_file = "emsoft11"

    assert os.path.isdir(base_dir)
    assert os.path.isfile(os.path.join(base_dir, source_file + ".gph"))

    # Put all the generated files in a folder called SOURCE_FILE_plots
    shutil.rmtree(os.path.join(base_dir, source_file + "_plots"), ignore_errors=True)
    os.mkdir(os.path.join(base_dir, source_file + "_plots"))
    shutil.copy(os.path.join(base_dir, source_file + ".gph"), \
            os.path.join(base_dir, source_file + "_plots/"))
    os.chdir(os.path.join(base_dir, source_file + "_plots"))

    # Generate the periodic task set
    g = ACSDFModel.load_graph_from_file(source_file + ".gph")
    g.calc_start_time_buffer_vector()
    #g.find_earliest_start_time_vector()
    
    print(" -- Number of actors = %s\n" % (g.get_num_of_actors()))
    print(" -- Repetition vector = %s\n" % (g.get_repetition_vector_str()))
    print(" -- Worst-case execution time vector = %s\n" % (g.get_execution_vector_str()))
    print(" -- Minimum period vector = %s\n" % (g.get_period_vector_str()))
    print(" -- Start time vector = %s\n" % (g.get_start_time_vector_str()))
    print(" -- Buffer size vector = %s\n" % (g.get_buffer_size_vector_str()))
    print(" -- Total Utilization  = %s\n" % (g.get_utilization()))
    total_buffer_size = 0
    for c in g.get_channels():
        total_buffer_size += c.get_buffer_size()
    print(" -- Total buffer size = %s\n" % (total_buffer_size))

    # Dump periodic taskset plot data
    dump_periodic_schedule_plot_data(g)