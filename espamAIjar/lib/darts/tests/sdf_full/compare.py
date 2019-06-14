#!/usr/bin/env python

__author__ = "Mohamed A. Bamakhrama"
__description__ = """A script to compare applications throughput and latency
under both strictly periodic scheduling using EDF and TDM"""

import os
import sys
import math
import time
import shutil
import argparse
import subprocess

sys.path.append("/home/mohamed/work/code/darts/darts")

from CSDFParser import CSDFParser
from ACSDFModel import ACSDFModel


def dump_app_file(g, throughput):
    sdf = open("app.xml", "w")
    CSDFParser.dump_sdf3_full(sdf, g, throughput)

def dump_arch_file(arch_filename, m):
    arch_file = open(arch_filename, "w")
    arch_file.write("""<?xml version="1.0"?>\n<sdf3 type='sdf' version='1.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='http://www.es.ele.tue.nl/sdf3/xsd/sdf3-sdf.xsd'>\n""")
    arch_file.write("""  <architectureGraph name="arch">\n""")
    for i in range(m):
        arch_file.write("""    <tile name="t""" + str(i) + """">\n""")
        arch_file.write("""      <processor name="proc_""" + str(i) + """" type="p1">\n""")
        arch_file.write("""        <arbitration type="TDMA" wheelsize="10"/>\n""")
        arch_file.write("""      </processor>\n""")
        arch_file.write("""      <memory name="mem" size="32000"/>\n""")
        arch_file.write("""      <networkInterface name="ni" nrConnections="10" inBandwidth="96" outBandwidth="96"/>\n""")
        arch_file.write("""    </tile>\n""")
    c = 0
    for i in range(m):
        for j in range(m):
            if j != i:
                arch_file.write("""    <connection name="con_""" + str(c) + """"   srcTile="t""" + str(i)  + """" dstTile="t""" + str(j)  + """" delay="3"/>\n""")
                c += 1
    arch_file.write("""    <network slotTableSize='8' packetHeaderSize='32' flitSize='96' reconfigurationTimeNI='32'>\n""")
    for i in range(m):
        arch_file.write("""      <tile name="t""" + str(i) + """"/>\n""")
    arch_file.write("    </network>\n")
    arch_file.write("  </architectureGraph>\n</sdf3>")
    arch_file.flush()

def dump_sdf3_flow_settings(filename):
    fp = open(filename, "w")
    fp.write("""<?xml version='1.0' encoding='UTF-8'?>
<sdf3 type='sdf' version='1.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='http://www.es.ele.tue.nl/sdf3/xsd/sdf3-sdf.xsd'>
    <settings type='flow'>
        <applicationGraph file='app.xml'/>
        <architectureGraph file='arch.xml'/>
    </settings>
</sdf3>
""")
    fp.close()

def run_and_wait(m, seconds):
    proc = subprocess.Popen(["sdf3flow-sdf --output out_" + str(m) + ".xml --html"],shell=True) 
    # stdout=subprocess.PIPE, stderr=subprocess.PIPE
    start = time.time()
    end = start + seconds
    while True:
        result = proc.poll()
        if result is not None:
            return result
        if time.time() >= end:
            # The process has timed out
            sys.stderr.write("Timeout!\n")
            proc.kill()
            return -1
        time.sleep(1)

def process_application(filename, gph_dir, out_dir):
    app_name = os.path.basename(filename).split(".")[0] 
    if os.path.exists(os.path.join(out_dir, app_name)):
        shutil.rmtree(os.path.join(out_dir, app_name), ignore_errors=True)
    os.mkdir(os.path.join(out_dir, app_name))
    os.chdir(os.path.join(out_dir, app_name))
    fp = open(os.path.join(gph_dir, filename), 'r')
    cp = CSDFParser(app_name, fp)
    g = cp.parse_graph()
    g.set_scaling_factor(1)
    g.set_deadline_factor(1.0)
    g.compute_repetition_vector()
    g.find_minimum_period_vector(g.get_scaling_factor())
    for a in g.get_actors():    # Initial value of deadline
        a.set_deadline(a.get_period())
    g.calc_start_time_buffer_vector()
    sink_actor = g.get_sink_actor()
    print("T_snk = " + str(sink_actor.get_period()) + ", q_snk = " + str(sink_actor.get_repetition()))
    graph_period = sink_actor.get_period() * sink_actor.get_repetition()
    throughput = float(1.0) / float(graph_period)
    dump_app_file(g, throughput)
    dump_sdf3_flow_settings("sdf3.opt")
    m = int(math.ceil(g.get_utilization()))
    while m <= g.get_num_of_actors():
        dump_arch_file("arch.xml", m)
        result = run_and_wait(m, 60*2) 
        if result != 0:
            sys.stderr.write("sdf3flow-sdf did not find a valid binding!\n")
            m += 1
        else:
            sys.stdout.write("sdf3flow-sdf found a valid binding!\n")
            subprocess.Popen(["touch done_" + str(m) + ""],shell=True) 
            print(">>>>> " + app_name + " : " + str(m))
            break

def main():
    parser = argparse.ArgumentParser(description=__description__,
        epilog="Author: " + __author__)
    parser.add_argument("-i", nargs=1, type=str, required=True,
        metavar="gph_dir", help="The directory containing the .gph files")
    parser.add_argument("-o", nargs=1, type=str, required=True,
        metavar="output_dir", help="The output directory")
    args = parser.parse_args()

    assert os.path.isdir(args.i[0])
    assert os.path.isdir(args.o[0])

    gph_dir = os.path.abspath(args.i[0])
    out_dir = os.path.abspath(args.o[0])

    for f in os.listdir(gph_dir):
        if f.endswith(".gph"):
            print("Processing " + f )
            process_application(f, gph_dir, out_dir)


if __name__ == "__main__":
    main()

