#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'ESPAM Handler'

import os.path
from operator import attrgetter

def ML605(output_dir, m):
    platform_file = os.path.join(output_dir, "platform.pla")
    fp = open(platform_file, 'w')
    fp.write("""<?xml version="1.0" standalone="no"?>
<!DOCTYPE platform PUBLIC "-//LIACS//DTD ESPAM 1//EN" "http://www.liacs.nl/~cserc/dtd/espam_1.dtd">
<platform name="myPlatform">

""")
    for i in range(m):
        fp.write("""
\t<processor name="mb_%s" type="MB" data_memory="131072" program_memory="131072">
\t\t<port name="IO_0" />
\t</processor>
""" % (i))

    fp.write("\n")
    fp.write("""\t<network name="CS" type="AXICrossbarSwitch">\n""")
    for i in range(m):
        fp.write("""\t\t<port name="IO_%s" />\n""" % (i))
    fp.write("\t</network>\n")
    fp.write("\n")
    fp.write("\t<host_interface name=\"HOST_IF\" type=\"ML605\" interface=\"Combo\">\n")
    fp.write("\t</host_interface>\n")
    fp.write("\n")
    for i in range(m):
        fp.write("""
\t<link name="BUS%s">
\t\t<resource name="mb_%s" port="IO_0" />
\t\t<resource name="CS" port="IO_%s" />
\t</link>
""" % (i, i, i))
    fp.write("\n")

    fp.write("</platform>")
    fp.close()


class EspamHandler:
    """ESPAM Handler"""

    sched_map = {
                'EDF' : "dynamic-edf",
                'FPPS' : "dynamic-freertos"
            }

    supported_boards = {'ML605' : ML605}

    def __init__(self, pg_instance, board_type, output_dir):
        self.pg_instance = pg_instance
        self.num_of_apps = pg_instance.get_num_of_graphs()
        self.partition = pg_instance.get_partition()
        self.num_of_processors = len(self.partition)
        self.output_dir = output_dir
        self.board_handler = self.supported_boards[board_type]

    def generate_platform_file(self):
        self.board_handler(self.output_dir, self.num_of_processors)

    def generate_mapping_file(self):
        mapping_file = os.path.join(self.output_dir, "mapping.map")
        fp = open(mapping_file, 'w')
        fp.write("""<?xml version="1.0" standalone="no"?>
<!DOCTYPE mapping PUBLIC "-//LIACS//DTD ESPAM 1//EN" "http://www.liacs.nl/~cserc/dtd/espam_1.dtd">
<mapping name="myMapping">
""")
        partition = self.partition
        proc_cnt = 0
        for p in partition:
            fp.write('\t<processor name="mb_%s" scheduleType="%s">\n' % (proc_cnt, self.sched_map[self.pg_instance.get_sched_algo()]))
            proc_cnt += 1
            if self.pg_instance.get_sched_algo() == "FPPS":
                sorted_tasks = sorted(p.get_actors(), key = attrgetter('deadline'), reverse = True)
            else:
                sorted_tasks = p.get_actors()
            priority = 1
            for t in sorted_tasks:
                t.set_priority(priority)
                priority = priority + 1
                name = t.get_name()
                if self.num_of_apps > 1:
                    name = t.get_full_name()
                if self.pg_instance.get_sched_algo() == "FPPS":
                    fp.write('\t\t<process name="%s" period="%s" startTime="%s" priority="%s"/>\n' % (name, t.get_period(), t.get_start_time(), t.get_priority()))
                else:
                    fp.write('\t\t<process name="%s" execution="%s" period="%s" deadline="%s" startTime="%s" priority="%s"/>\n' % (name, t.get_wcet(), t.get_period(), t.get_deadline(), t.get_start_time(), t.get_priority()))
            fp.write("\t</processor>\n")
        fp.write("\n")
        graphs = self.pg_instance.get_graphs()
        for g in graphs:
            channels = g.get_channels()
            for c in channels:
                if self.num_of_apps > 1:
                    fp.write('\t<fifo name="%s_%s" size="%s" />\n' % (g.get_graph_name(), c.get_name(), c.get_buffer_size()))
                else:
                    fp.write('\t<fifo name="%s" size="%s" />\n' % (c.get_name(), c.get_buffer_size()))
        fp.write("\n")
        fp.write("</mapping>")
        fp.close()

