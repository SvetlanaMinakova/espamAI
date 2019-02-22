#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Dot Handler'

import os.path

class DotHandler:
    """Dot Handler"""

    colors = ["red", "lightblue", "green", "yellow", "orange", "lightgrey", "yellow"]

    def __init__(self, pg_instance, output_dir):
        self.pg_instance = pg_instance
        self.output_dir = output_dir
        self.partition = pg_instance.get_partition()
        self.num_of_apps = self.pg_instance.get_num_of_graphs()

    def generate_mapping_file(self):
        mapping = os.path.join(self.output_dir, "mapping.dot")
        fp = open(mapping, 'w')
        fp.write("digraph mapping {\n")
        fp.write("rankdir=TB;\n")
        partition = self.partition
        proc_cnt = 0
        task_cnt = 0
        for p in partition:
            fp.write("subgraph cluster%s {\n" % (proc_cnt))
            u_sum = 0.0
            for t in p.get_actors():
                if self.num_of_apps > 1:
                    fp.write("\t%s_%s [ label = \"%s - U = %s\"];\n" % (t.get_full_name(), t.get_function(), t.get_full_name(), t.get_utilization()))
                else:
                    fp.write("\t%s_%s [ label = \"%s - U = %s\"];\n" % (t.get_name(), t.get_function(), t.get_full_name(), t.get_utilization()))
                task_cnt += 1
                u_sum += t.get_utilization()
            fp.write('\tlabel=\"Processor%s - U = %s\";\n' % (proc_cnt, u_sum))
            proc_cnt += 1
            fp.write("}\n")

        # Done with processors. Print apps edges
        app_cnt = 0
        for g in self.pg_instance.get_graphs():
            for actor in g.get_actors():
#                actor = g.get_actor(a.get_actor_id())
                for c in actor.get_outchannels():
                    ch = actor.get_graph().get_channel(c)
                    successor = g.get_actor(ch.get_destination())
                    if self.num_of_apps > 1:
                        fp.write("%s_%s_%s[style=filled,color=\"%s\"];\n" % (g.get_graph_name(), actor.get_name(), actor.get_function(), self.colors[app_cnt % len(self.colors)]))
                        fp.write("%s_%s_%s[style=filled,color=\"%s\"];\n" % (g.get_graph_name(), successor.get_name(), successor.get_function(), self.colors[app_cnt % len(self.colors)]))
                        fp.write("%s_%s_%s -> %s_%s_%s;\n" % (g.get_graph_name(), actor.get_name(), actor.get_function(), g.get_graph_name(), successor.get_name(), successor.get_function()))
                    else:
                        fp.write("%s_%s[style=filled,color=\"%s\"];\n" % (actor.get_name(), actor.get_function(), self.colors[app_cnt % len(self.colors)]))
                        fp.write("%s_%s[style=filled,color=\"%s\"];\n" % (successor.get_name(), successor.get_function(), self.colors[app_cnt % len(self.colors)]))
                        fp.write("%s_%s -> %s_%s;\n" % (actor.get_name(), actor.get_function(), successor.get_name(), successor.get_function()))
            app_cnt += 1
        fp.write('label=\"Mapping\";\n')
        fp.write("}\n")
        fp.flush()
        fp.close()

