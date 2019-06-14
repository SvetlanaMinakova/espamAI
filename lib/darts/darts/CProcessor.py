#!/usr/bin/python

__author__ = "Mohamed A. Bamakhrama"
__description__ = """Processes a C program and generates the
corresponding PPN model, CSDF model, and periodic task-set"""

import os
import sys
import shutil
import subprocess
import argparse

from CSDFParser import CSDFParser
from ACSDFModel import ACSDFModel


# TODO Make sure that the following paths are set properly
# $PN_DIR,  $PNTOOLS_DIR, $CSDF_RTSCHEDTOOLS_DIR
PN_DIR = os.getenv('PN_DIR')
PNTOOLS_DIR = os.getenv('PNTOOLS_DIR')
DARTS_DIR = os.getenv('DARTS_DIR')


def process_program(base_dir, source_file, scaling_factor=1, 
                    deadline_factor=1.0):

    sys.stdout.write(" - base_dir = %s\n" % base_dir)
    sys.stdout.write(" - source_file = %s\n" % source_file)

    shutil.rmtree(os.path.join(base_dir, source_file + "_graphs"), 
                               ignore_errors=True)
    os.mkdir(os.path.join(base_dir, source_file + "_graphs"))

    shutil.copy(os.path.join(base_dir, source_file + ".c"), 
                os.path.join(base_dir, source_file + "_graphs"))
    try:
        shutil.copy(os.path.join(base_dir, source_file + ".h"), 
                    os.path.join(base_dir, source_file + "_graphs"))
    except Exception:
        pass
    shutil.copy(os.path.join(base_dir, "impldata.xml"), 
                os.path.join(base_dir, source_file + "_graphs"))

    os.chdir(os.path.join(base_dir, source_file + "_graphs"))
    
    infile = source_file + ".c"
    outfile = open(source_file + ".yaml", "w")
    child = subprocess.check_call([PN_DIR + os.sep + "c2pdg", 
                                  "-func", "main", infile], stdout=outfile)
    outfile.close()

    infile = open(source_file + ".yaml", "r")
    outfile = open(source_file + "-pn.yaml", "w")
    child = subprocess.check_call([PN_DIR + os.sep + "pn"], 
                                  stdin=infile, stdout=outfile)
    infile.close(); outfile.close()

    infile = open(source_file + "-pn.yaml", "r")
    outfile = open(source_file + "-adg.yaml", "w")
    child = subprocess.check_call([PN_DIR + os.sep + "pn2adg"], 
                                  stdin=infile, stdout=outfile)
    infile.close(); outfile.close()

    infile = open(source_file + "-adg.yaml", "r")
    outfile = open(source_file + ".gph", "w")
    child = subprocess.check_call([PNTOOLS_DIR + os.sep + "adg2csdf"], 
                                  stdin=infile, stdout=outfile)
    infile.close(); outfile.close()

    infile = open(source_file + ".gph", "r")
    outfile = open(source_file + ".xml", "w")
    cp = CSDFParser(source_file, infile)
    xmlg = cp.parse_graph()
    if xmlg != None:
        CSDFParser.dump_sdf3(outfile, xmlg)
    infile.close(); outfile.close()

    g = ACSDFModel.load_graph_from_file(source_file + ".gph", 
                                        scaling_factor, deadline_factor)
    g.calc_start_time_buffer_vector()
    ACSDFModel.print_graph_dot(g, os.getcwd())

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
    sys.stdout.write(" -- Start time vector = %s\n" % 
                    (g.get_start_time_vector_str()))
    sys.stdout.write(" -- Buffer size vector = %s\n" % 
                    (g.get_buffer_size_vector_str()))
    sys.stdout.write(" -- Total Utilization  = %s\n" % 
                    (g.get_utilization()))
    sys.stdout.write(" -- Total Density = %s\n" % 
                    (g.get_density()))
    sys.stdout.write(" -- Graph maximum latency = %s\n" % 
                    (g.get_latency()))
  

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__description__, 
                                     epilog="Author: %s" % __author__)
    required = parser.add_argument_group("required arguments")
    required.add_argument("-p", nargs=1, required=True, 
                          type=str, metavar=("program"), 
                          help="The path to the .c program")
    parser.add_argument("-d", nargs=1, required=True, default=-1.0, 
                        type=float, metavar=("deadline_factor"), 
                        help="Derive the task-set using the given deadline " 
                        "factor. The deadline factor is a value between " 
                        "0.0 and 1.0")
    parser.add_argument("-s", nargs=1, required=True, default=-1, 
                        type=int, metavar=("scaling_factor"), 
                        help="Derive the task-set using the given scaling "
                        "factor. The scaling factor is an integer value "
                        "greater than or equal to 1")
    args = parser.parse_args()

    if not os.path.exists(args.p[0]):
        sys.stderr.write("Error! the provided path does not exist!\n")
        sys.exit(-1)
        
    abs_path = os.path.abspath(args.p[0])
    base_dir = os.path.dirname(abs_path)
    source_file = os.path.basename(abs_path).split(".")[0]

    deadline_factor = args.d[0]
    scaling_factor = args.s[0]

    process_program(base_dir, source_file, scaling_factor, deadline_factor)
    sys.stdout.write("Done...\n")

