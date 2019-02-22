#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Text Handler'

import os.path

import Utilities

class TextHandler:
    """Text Handler"""

    def __init__(self, pg_instance, output_dir):
        self.pg_instance = pg_instance
        self.output_dir = output_dir
        self.partition = pg_instance.get_partition()
        self.num_of_apps = self.pg_instance.get_num_of_graphs()

    def generate_mapping_file(self):
        mapping = os.path.join(self.output_dir, "mapping.txt")
        fp = open(mapping, 'w')
        tn = 0
        tabs = Utilities.tabs
        partition = self.partition
        fp.write("number_of_processors:%s\n" % len(partition))
        proc_cnt = 0
        task_cnt = 0
        for p in partition:
            fp.write("%sprocessor:\n" % tabs(tn))
            tn += 1
            fp.write("%sid:%s\n" % (tabs(tn), proc_cnt))
            u_sum = 0.0
            u_sum += p.get_utilization()
            fp.write("%sutilization:%s\n" % (tabs(tn), u_sum))
            for t in p.get_actors():
                fp.write("%stask:\n" % tabs(tn))
                tn += 1
                if self.num_of_apps > 1:
                    fp.write("%sname:%s\n" % (tabs(tn), t.get_full_name()))
                else:
                    fp.write("%sname:%s\n" % (tabs(tn), t.get_name()))
                fp.write("%sfunction:%s\n" % (tabs(tn), t.get_function()))
                fp.write("%swcet:%s\n" % (tabs(tn), t.get_wcet()))
                fp.write("%speriod:%s\n" % (tabs(tn), t.get_period()))
                fp.write("%sdeadline:%s\n" % (tabs(tn), t.get_deadline()))
                fp.write("%sstart_time:%s\n" % (tabs(tn), t.get_start_time()))
                fp.write("%sutilization:%s\n" % (tabs(tn), t.get_utilization()))
                fp.write("%spriority:%s\n" % (tabs(tn), t.get_priority()))
                tn -= 1
                task_cnt += 1
            tn -= 1
            proc_cnt += 1
        tn -= 1
        num_of_chan = 0
        for g in self.pg_instance.get_graphs():
            num_of_chan += len(g.get_channels())
        fp.write("%snumber_of_channels:%s\n" % (tabs(tn), num_of_chan))
        tn += 1
        chan_cnt = 0
        for actor in self.pg_instance.get_actors():
            for c in actor.get_outchannels():
                ch = actor.get_graph().get_channel(c)
                successor = actor.get_graph().get_actor(ch.get_destination())
                fp.write("%schannel:\n" % tabs(tn))
                tn += 1
                fp.write("%sid:%s\n" % (tabs(tn), chan_cnt))
                fp.write("%ssize:%s\n" % (tabs(tn), ch.get_buffer_size()))
                if self.num_of_apps > 1:
                    fp.write("%ssource_task:%s\n" % (tabs(tn), actor.get_full_name()))
                    fp.write("%sdestination_task:%s\n" % (tabs(tn), successor.get_full_name()))
                else:
                    fp.write("%ssource_task:%s\n" % (tabs(tn), actor.get_name()))
                    fp.write("%sdestination_task:%s\n" % (tabs(tn), successor.get_name()))
                fp.write("%ssource_processor:%s \n" % (tabs(tn), actor.get_affinity()))
                fp.write("%sdestination_processor:%s\n" % (tabs(tn), successor.get_affinity()))
                prd_tokens = sum(ch.get_production_sequence())
                prd_len = len(ch.get_production_sequence())
                cns_tokens = sum(ch.get_consumption_sequence())
                cns_len = len(ch.get_consumption_sequence())
                fp.write("%swrite_rate:%s/%s\n" % (tabs(tn), prd_tokens, prd_len * actor.get_period()))
                fp.write("%sread_rate:%s/%s\n" % (tabs(tn), cns_tokens, cns_len * successor.get_period()))
                tn -= 1
                chan_cnt += 1
        fp.flush()
        fp.close()

