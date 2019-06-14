#!/usr/bin/python

__author__ = "Mohamed A. Bamakhrama"

import math
import os
import sys
import subprocess
import argparse
import hashlib

sys.path.append("../darts/")

from ACSDFModel import ACSDFModel
import PlatformGenerator
import Allocation
from EDF import EDF
from FPPS import FPPS
import Utilities


def benchmark_oneproc(benchmarks_dir, sched_type):

    os.chdir(benchmarks_dir)

    child = subprocess.Popen(["ls -1 *.gph"], shell=True, stdout=subprocess.PIPE)
    gph_files = child.stdout.readlines()

    for gp in gph_files:
        gph = gp.decode().rstrip('\n')
        sys.stdout.write(gph + ": \n")

        scaling_factor = 1
        minimum_sink_period = 0

        g = ACSDFModel.load_graph_from_file(gph, scaling_factor, 1)

        ac = g.get_sink_actor()
        minimum_sink_period = ac.get_period()

        if sched_type == "EDF":
            UTILIZATION_BOUND = 1.0
        if sched_type == "RM":
            UTILIZATION_BOUND = FPPS.rate_monotonic_bound(g.get_num_of_actors())

        while g.get_utilization() > UTILIZATION_BOUND:
            scaling_factor += 1
            g = ACSDFModel.load_graph_from_file(gph, scaling_factor)

        # Find the start time and buffer size once we reached the required utilization        
        g.calc_start_time_buffer_vector()

        sys.stdout.write("\tUtilization = %s\n" % g.get_utilization())

        ac = g.get_sink_actor()
        sys.stdout.write("\tActual period = %s\n" % ac.get_period())
        sys.stdout.write("\tMinimum period = %s\n" % minimum_sink_period)
        sys.stdout.write("\tScaling factor = %s\n" % scaling_factor)

        total_buffer_size = 0
        for c in g.get_channels():
            total_buffer_size += c.get_buffer_size()
        sys.stdout.write("\tTotal buffer size = %s\n" % total_buffer_size) 

def benchmark_opt(benchmarks_dir, deadline_factor):

    os.chdir(benchmarks_dir)

    child = subprocess.Popen(["ls -1 *.gph"], shell=True, stdout=subprocess.PIPE)
    gph_files = child.stdout.readlines()

    for gp in gph_files:
        gph = gp.decode().rstrip('\n')
        sys.stdout.write(gph + ": \n")

        g = ACSDFModel.load_graph_from_file(gph, 1, deadline_factor)
        g.calc_start_time_buffer_vector()

        sys.stdout.write("\tUtilization = %s\n" % g.get_utilization())

        total_buffer_size = 0
        h = hashlib.sha256()
        for c in g.get_channels():
            total_buffer_size += c.get_buffer_size()
            h.update(str(c.get_buffer_size()).encode())
        sys.stdout.write("\tTotal buffer size = %s\n" % total_buffer_size)
        sys.stdout.write("\tBuffer size hash = %s\n" % h.hexdigest())

def benchmark_qpa(benchmarks_dir, deadline_factor):

    os.chdir(benchmarks_dir)

    child = subprocess.Popen(["ls -1 *.gph"], shell=True, stdout=subprocess.PIPE)
    gph_files = child.stdout.readlines()

    for gp in gph_files:
        gph = gp.decode().rstrip('\n')
        sys.stdout.write(gph + ": \n")

        g = ACSDFModel.load_graph_from_file(gph, 1, deadline_factor)
        g.calc_start_time_buffer_vector()

        sys.stdout.write("\tUtilization = %s\n" % g.get_utilization())

        total_buffer_size = 0
#        h = hashlib.sha256()
        for c in g.get_channels():
            total_buffer_size += c.get_buffer_size()
#            h.update(str(c.get_buffer_size()).encode())
        sys.stdout.write("\tTotal latency = %s\n" % g.get_latency())
        sys.stdout.write("\tTotal buffer size = %s\n" % total_buffer_size)
#        sys.stdout.write("\tBuffer size hash = %s\n" % h.hexdigest())

        pg = PlatformGenerator.make_ready_graphs([g])
        pg.set_alloc_algo("FFD")
        pg.set_sched_algo("EDF")
        pg.set_constrained_deadline(True)
        partition = Allocation.FFD(pg)
        sys.stdout.write("\tNumber of processor required by EDF (QPA test) and FFD allocation = %s\n" % len(partition))

# def benchmark_phrt(benchmarks_dir, nr_proc):

#     os.chdir(benchmarks_dir)

#     child = subprocess.Popen(["ls -1 *.gph"], shell=True, stdout=subprocess.PIPE)
#     gph_files = child.stdout.readlines()



if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Performs tests on the benchmarks", epilog="Author: %s" % __author__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-s", nargs=1, type=str, metavar=("algorithm"), help="Schedule on a single processor using the specified algorithm")
    group.add_argument("-o", nargs=1, type=float, metavar=("deadline_factor"), help="Schedule on the minimum number of processors needed by an optimal algorithm using the specified deadline factor")
    group.add_argument("-q", nargs=1, type=float, metavar=("deadline_factor"), help="Schedule on the minimum number of processors needed by EDF using the specified deadline factor and assuming QPA test and FFD allocation")
    parser.add_argument("-d", nargs=1, required=True, type=str, metavar="directory", help="The path to the directory containing the benchmarks")
    args = parser.parse_args()

    if not os.path.isdir(args.d[0]):
        sys.stderr.write("Error! the provided path does not exist!\n")
        sys.exit(-1)

    benchmarks_dir = os.path.abspath(args.d[0])

    if type(args.o) == list:
        benchmark_opt(benchmarks_dir, args.o[0])
    elif type(args.q) == list:
        benchmark_qpa(benchmarks_dir, args.q[0])
    else:
        if args.s[0] != "EDF" and args.s[0] != "RM":
            sys.stderr.write("Error! Only EDF and RM are supported\n")
            sys.exit(-1)

        sched_type = args.s[0]
        print(sched_type)
        benchmark_oneproc(benchmarks_dir, sched_type)

