#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'JSON Handler'

import os.path

import Utilities

class JSONHandler:
    """JSON Handler"""

    def __init__(self, pg_instance, output_dir):
        self.pg_instance = pg_instance
        self.output_dir = output_dir
        self.partition = pg_instance.get_partition()
        self.num_of_apps = self.pg_instance.get_num_of_graphs()

    def generate_mapping_file(self):
        mapping = os.path.join(self.output_dir, "mapping.json")
        fp = open(mapping, 'w')
        tn = 1
        tabs = Utilities.tabs
        partition = self.partition
        fp.write("{\n")
        fp.write(tabs(tn) + "\"number_of_processors\" : %s,\n" % len(partition))
        proc_cnt = 0
        task_cnt = 0
        fp.write("%s\"processors\" : [\n" % tabs(tn))
        tn += 1
        for p in partition:
            fp.write(tabs(tn) + "{\n")
            tn += 1
            fp.write("%s\"id\" : %s,\n" % (tabs(tn), proc_cnt))
            u_sum = 0.0
            u_sum += p.get_utilization()
            fp.write("%s\"utilization\" : %s,\n" % (tabs(tn), u_sum))
            fp.write("%s\"tasks\" : [\n" % tabs(tn))
            tn += 1
            t_cnt = 0
            for t in p.get_actors():
                fp.write(tabs(tn) + "{\n")
                tn += 1
                if self.num_of_apps > 1:
                    fp.write("%s\"name\" : \"%s\",\n" % (tabs(tn), t.get_full_name()))
                else:
                    fp.write("%s\"name\" : \"%s\",\n" % (tabs(tn), t.get_name()))
                fp.write("%s\"function\" : \"%s\",\n" % (tabs(tn), t.get_function()))
                fp.write("%s\"wcet\" : %s,\n" % (tabs(tn), t.get_wcet()))
                fp.write("%s\"period\" : %s,\n" % (tabs(tn), t.get_period()))
                fp.write("%s\"deadline\" : %s,\n" % (tabs(tn), t.get_deadline()))
                fp.write("%s\"start_time\" : %s,\n" % (tabs(tn), t.get_start_time()))
                fp.write("%s\"utilization\" : %s,\n" % (tabs(tn), t.get_utilization()))
                fp.write("%s\"priority\" : %s\n" % (tabs(tn), t.get_priority()))
                tn -= 1
                task_cnt += 1
                t_cnt += 1
                fp.write(tabs(tn) + "}")
                if t_cnt < len(p.get_actors()):
                    fp.write(",")
                fp.write("\n")
            tn -= 1
            fp.write(tabs(tn) + "]\n")
            tn -= 1
            proc_cnt += 1
            fp.write(tabs(tn) + "}")
            if proc_cnt < len(partition):
                fp.write(",")
            fp.write("\n")
        tn -= 1
        fp.write(tabs(tn) + "],\n")
        num_of_chan = 0
        for g in self.pg_instance.get_graphs():
            num_of_chan += len(g.get_channels())
        fp.write("%s\"number_of_channels\" : %s,\n" % (tabs(tn), num_of_chan))
        fp.write(tabs(tn) + "\"channels\" : [\n")
        tn += 1
        chan_cnt = 0
        total_chan_cnt = 0
        for actor in self.pg_instance.get_actors():
            total_chan_cnt += len(actor.get_outchannels())

        for actor in self.pg_instance.get_actors():
            for c in actor.get_outchannels():
                ch = actor.get_graph().get_channel(c)
                successor = actor.get_graph().get_actor(ch.get_destination())
                fp.write("%s{\n" % tabs(tn))
                tn += 1
                fp.write("%s\"id\" : %s,\n" % (tabs(tn), chan_cnt))
                fp.write("%s\"size\" : %s,\n" % (tabs(tn), ch.get_buffer_size()))
                if self.num_of_apps > 1:
                    fp.write("%s\"source_task\" : \"%s\",\n" % (tabs(tn), actor.get_full_name()))
                    fp.write("%s\"destination_task\" : \"%s\",\n" % (tabs(tn), successor.get_full_name()))
                else:
                    fp.write("%s\"source_task\" : \"%s\",\n" % (tabs(tn), actor.get_name()))
                    fp.write("%s\"destination_task\" : \"%s\",\n" % (tabs(tn), successor.get_name()))
                fp.write("%s\"source_processor\" : %s,\n" % (tabs(tn), actor.get_affinity()))
                fp.write("%s\"destination_processor\" : %s,\n" % (tabs(tn), successor.get_affinity()))
                prd_tokens = sum(ch.get_production_sequence())
                prd_len = len(ch.get_production_sequence())
                cns_tokens = sum(ch.get_consumption_sequence())
                cns_len = len(ch.get_consumption_sequence())
                fp.write("%s\"write_rate\" : \"%s/%s\",\n" % (tabs(tn), prd_tokens, prd_len * actor.get_period()))
                fp.write("%s\"read_rate\" : \"%s/%s\"\n" % (tabs(tn), cns_tokens, cns_len * successor.get_period()))
                tn -= 1
                fp.write(tabs(tn) + "}");
                chan_cnt += 1
                if chan_cnt < total_chan_cnt:
                    fp.write(",")
                fp.write("\n")
        tn -= 1
        fp.write(tabs(tn) + "]\n")
        fp.write("}")
        fp.flush()
        fp.close()

