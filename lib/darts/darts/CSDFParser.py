#!/usr/bin/env python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'A tool to parse and convert different CSDF graph formats'

import sys
import argparse
import json
import xml.etree.ElementTree as ET

import Utilities    

class CSDFParser:

    def __init__(self, name, input_file):
        self.input_file = input_file
        self.graph_name = name
        self.graph_type = "csdf"
        self.graph = None

    @staticmethod
    def sdf3_to_gph(sdf3):
        out = sys.stdout
        out.flush()
        g = CSDFParser.parse_sdf3(sdf3)
        CSDFParser.dump_gph(g, out)
    
    @staticmethod
    def dump_gph(g, out):
        objs = dict()
        objs["name"] = g.get_graph_name()
        objs["type"] = "csdf"
        objs["node_number"] = g.get_num_of_actors()
        objs["nodes"] = list()
        for actor in g.get_actors():
            node = dict()
            node["id"] = actor.get_actor_id()
            node["name"] = actor.get_name()
            node["function"] = actor.get_function()
            node["length"] = len(actor.wcet_sequence)
            node["wcet"] = list(actor.wcet_sequence)
            node["stateful"] = bool(actor.get_stateful())
            node["code_size"] = actor.code_size
            node["port_number"] = len(actor.inports) + len(actor.outports)
            node["ports"] = list()
            for xport in actor.inports:
                port = dict()
                port["type"] = xport.port_type
                port["id"] = xport.port_id
                port["rate"] = list(xport.sequence)
                assert len(port["rate"]) == len(node["wcet"])
                node["ports"].append(port)
            for xport in actor.outports:
                port = dict()
                port["type"] = xport.port_type
                port["id"] = xport.port_id
                port["rate"] = list(xport.sequence)
                assert len(port["rate"]) == len(node["wcet"])
                node["ports"].append(port)
            objs["nodes"].append(node)
        objs["edge_number"] = g.get_num_of_channels()
        objs["edges"] = list()
        for chan in g.get_channels():
            edge = dict()
            edge["id"] = chan.channel_id
            edge["name"] = chan.name
            edge["src"] = list()
            edge["src"].append(chan.source)
            edge["src"].append(chan.source_port.port_id)
            edge["dst"] = list()
            edge["dst"].append(chan.destination)
            edge["dst"].append(chan.destination_port.port_id)
            objs["edges"].append(edge)
        out.write(json.dumps(objs, indent=4, separators=(',', ': ')))
    
    @staticmethod
    def parse_sdf3(sdf3):
        tree = ET.parse(sdf3)
        root = tree.getroot()

        from ACSDFModel import ACSDFModel
        g = ACSDFModel()
        graph_type = "csdf"
        gt = root.find("./applicationGraph/csdf")
        if gt == None:
            graph_type = "sdf"
            gt = root.find("./applicationGraph/sdf")
            if gt == None:
                sys.stderr.write("Error! Invalid graph specified!\n")
                sys.exit(-1)

        graph_name = root.find("./applicationGraph/" + graph_type).get("name")        
        g.set_graph_name(graph_name)

#        num_of_actors = len(root.findall("./applicationGraph/" + graph_type + "/actor"))
#        num_of_channels = len(root.findall("./applicationGraph/" + graph_type + "/channel"))

        # The following dict is needed because SDF^3 uses the name as the 
        # indexing parameter instead of the id as used in the .gph format
        actor_dict = dict()

        actor_id = 0
        for a in root.findall("./applicationGraph/" + graph_type + "/actor"):
            actor_name = a.get("name")
            g.add_actor(actor_id, actor_name, actor_name, 0)
            actor_dict[actor_name] = actor_id
            ports = a.findall("port")
            port_id = 0

            for p in ports:
                port_name = p.get("name")
                port_type = p.get("type")
                port_rate = list(map(int, p.get("rate").split(","))) 
                if port_type == "in":
                    g.get_actor(actor_id).add_inport(port_name, port_id, port_rate)
                elif port_type == "out":
                    g.get_actor(actor_id).add_outport(port_name, port_id, port_rate)
                port_id += 1
            actor_id += 1

        ch_id = 0
        for c in root.findall("./applicationGraph/" + graph_type + "/channel"):
            ch_name = c.get("name")
            src_actor = c.get("srcActor")
            src_port = g.get_actor(actor_dict[src_actor]).get_outport_by_name(c.get("srcPort"))
            dst_actor = c.get("dstActor")
            dst_port = g.get_actor(actor_dict[dst_actor]).get_inport_by_name(c.get("dstPort"))
            init_tokens = c.get("initialTokens")
            g.add_channel(ch_id, ch_name, actor_dict[src_actor], src_port, actor_dict[dst_actor], dst_port)
            if init_tokens != None:
                g.get_channel(ch_id).set_initial_tokens(int(init_tokens))
            ch_id += 1
        
        actor_id = 0    
        for ap in root.findall("./applicationGraph/" + graph_type + "Properties/actorProperties"):
            et_seq = list(map(int, ap.find("./processor/executionTime").get("time").split(",")))
            g.get_actor(actor_id).set_wcet_sequence(et_seq)
            g.get_actor(actor_id).set_wcet(max(et_seq))
            actor_id += 1
        return g

    @staticmethod
    def dump_sdf3(sdf3, g):
        sdf3.flush()
        t = 0
        tabs = Utilities.tabs
        # Preamble
        sdf3.write("""<?xml version="1.0" encoding="UTF-8"?>\n""")
        sdf3.write("""<sdf3 type="csdf" version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.es.ele.tue.nl/sdf3/xsd/sdf3-csdf.xsd">\n""")
        t += 1
        sdf3.write("""%s<applicationGraph name=\"%s\">\n""" % (tabs(t), g.get_graph_name()))
        t += 1
        sdf3.write("""%s<csdf name=\"%s\" type=\"%s\">\n""" % (tabs(t), g.get_graph_name(), g.get_graph_name()))
        t += 1
        # Actors
        for node in g.get_actors():
            sdf3.write("\n%s<actor name=\"%s\" type=\"Node\">\n" % (tabs(t), node.get_name()))
            t += 1
            for ip in node.get_inports():
                sdf3.write("%s<port name=\"%s\" type=\"in\" rate=\"" % (tabs(t), ip.get_name()))
                for i in range(0,len(ip.get_sequence())):
                    if i < (len(ip.get_sequence()) - 1):
                        sdf3.write("%s," % (ip.get_sequence()[i]))
                    else:
                        sdf3.write("%s" % (ip.get_sequence()[i]))
                sdf3.write("\"/>\n")
            for op in node.get_outports():
                sdf3.write("%s<port name=\"%s\" type=\"out\" rate=\"" % (tabs(t), op.get_name()))
                for i in range(0,len(op.get_sequence())):
                    if i < (len(op.get_sequence()) - 1):
                        sdf3.write("%s," % (op.get_sequence()[i]))
                    else:
                        sdf3.write("%s" % (op.get_sequence()[i]))
                sdf3.write("\"/>\n")
            sdf3.write("""%s<port name="_p1" type="out" rate="1"/>\n%s<port name="_p2" type="in" rate="1"/>\n""" % (tabs(t), tabs(t)))
            t -= 1
            sdf3.write("%s</actor>\n" % (tabs(t)))
        sdf3.write("\n")
        # Channels
        i = 1
        for e in g.get_channels():
            sdf3.write("%s<channel name=\"%s\" srcActor=\"%s\" srcPort=\"%s" % (tabs(t), e.get_name(), g.get_actor(e.get_source()).get_name(), e.get_source_port().get_name()))
            sdf3.write("\" dstActor=\"%s\" dstPort=\"%s" % (g.get_actor(e.get_destination()).get_name(), e.get_destination_port().get_name()))
            sdf3.write("\"/>\n")
            i = i + 1
        # Back edges
        i = 1
        for n in g.get_actors():
            sdf3.write("%s<channel name=\"_ch%s\" srcActor=\"%s\" srcPort=\"_p1\" dstActor=\"%s\" dstPort=\"_p2\" initialTokens='1'/>\n" % (tabs(t), i, n.get_name(), n.get_name()))
            i = i + 1
        t -= 1
        sdf3.write("%s</csdf>\n" % (tabs(t)))
        sdf3.write("%s<csdfProperties>\n" % (tabs(t)))
        # Execution times
        t += 1
        for node in g.get_actors():
            sdf3.write("%s<actorProperties actor=\"%s\">\n" % (tabs(t), node.get_name()))
            t += 1
            sdf3.write("%s<processor type=\"p1\" default=\"true\">\n" % (tabs(t)))
            t += 1
            sdf3.write("%s<executionTime time=\"" % (tabs(t)))
            for i in range(0, len(node.get_wcet_sequence())):
                if i < (len(node.get_wcet_sequence())-1):
                    sdf3.write("%s," % (node.get_wcet_sequence()[i]))
                else:
                    sdf3.write("%s" % node.get_wcet_sequence()[i])
            sdf3.write("\"/>\n")
            t -= 1
            sdf3.write("%s</processor>\n" % (tabs(t)))
            t -= 1
            sdf3.write("%s</actorProperties>\n" % (tabs(t)))
        # Closeing part
        t -= 1
        sdf3.write("%s</csdfProperties>\n" % (tabs(t)))
        t -= 1
        sdf3.write("%s</applicationGraph>\n" % (tabs(t)))
        sdf3.write("</sdf3>\n")
        sdf3.flush()

    @staticmethod
    def dump_sdf3_full(sdf3, g, throughput):
        sdf3.flush()
        t = 0
        tabs = Utilities.tabs
        # Preamble
        sdf3.write("""<?xml version="1.0" encoding="UTF-8"?>\n""")
        sdf3.write("""<sdf3 type="sdf" version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.es.ele.tue.nl/sdf3/xsd/sdf3-sdf.xsd">\n""")
        t += 1
        sdf3.write("""%s<applicationGraph name=\"%s\">\n""" % (tabs(t), g.get_graph_name()))
        t += 1
        sdf3.write("""%s<sdf name=\"%s\" type=\"%s\">\n""" % (tabs(t), g.get_graph_name(), g.get_graph_name()))
        t += 1
        # Actors
        for node in g.get_actors():
            sdf3.write("\n%s<actor name=\"%s\" type=\"Node\">\n" % (tabs(t), node.get_name()))
            t += 1
            for ip in node.get_inports():
                sdf3.write("%s<port name=\"%s\" type=\"in\" rate=\"" % (tabs(t), ip.get_name()))
                for i in range(0,len(ip.get_sequence())):
                    if i < (len(ip.get_sequence()) - 1):
                        sdf3.write("%s," % (ip.get_sequence()[i]))
                    else:
                        sdf3.write("%s" % (ip.get_sequence()[i]))
                sdf3.write("\"/>\n")
            for op in node.get_outports():
                sdf3.write("%s<port name=\"%s\" type=\"out\" rate=\"" % (tabs(t), op.get_name()))
                for i in range(0,len(op.get_sequence())):
                    if i < (len(op.get_sequence()) - 1):
                        sdf3.write("%s," % (op.get_sequence()[i]))
                    else:
                        sdf3.write("%s" % (op.get_sequence()[i]))
                sdf3.write("\"/>\n")
            sdf3.write("""%s<port name="slop" type="out" rate="1"/>\n%s<port name="slip" type="in" rate="1"/>\n""" % (tabs(t), tabs(t)))
            t -= 1
            sdf3.write("%s</actor>\n" % (tabs(t)))
        sdf3.write("\n")
        # Channels
        i = 1
        for e in g.get_channels():
            sdf3.write("%s<channel name=\"%s\" srcActor=\"%s\" srcPort=\"%s" % (tabs(t), e.get_name(), g.get_actor(e.get_source()).get_name(), e.get_source_port().get_name()))
            sdf3.write("\" dstActor=\"%s\" dstPort=\"%s" % (g.get_actor(e.get_destination()).get_name(), e.get_destination_port().get_name()))
            sdf3.write("\"/>\n")
            i = i + 1
        # Back edges
        for n in g.get_actors():
            sdf3.write(tabs(t) + "<channel name=\"" + n.get_name() + "2" +\
                    n.get_name() + "\" srcActor=\"" + n.get_name() + \
                    "\" srcPort=\"slop\" dstActor=\"" + n.get_name() + \
                    "\" dstPort=\"slip\" initialTokens='1'/>\n")
        t -= 1
        sdf3.write("%s</sdf>\n" % (tabs(t)))
        sdf3.write("%s<sdfProperties>\n" % (tabs(t)))
        # Execution times
        t += 1
        for node in g.get_actors():
            sdf3.write("%s<actorProperties actor=\"%s\">\n" % (tabs(t), node.get_name()))
            t += 1
            sdf3.write("%s<processor type=\"p1\" default=\"true\">\n" % (tabs(t)))
            t += 1
            sdf3.write("%s<executionTime time=\"" % (tabs(t)))
            for i in range(0, len(node.get_wcet_sequence())):
                if i < (len(node.get_wcet_sequence())-1):
                    sdf3.write("%s," % (node.get_wcet_sequence()[i]))
                else:
                    sdf3.write("%s" % node.get_wcet_sequence()[i])
            sdf3.write("\"/>\n")
            sdf3.write(tabs(t) + "<memory>\n")
            t += 1
            sdf3.write(tabs(t) + "<stateSize max='" + str(node.get_code_size()) +"'/>\n")
            t -= 1
            sdf3.write(tabs(t) + "</memory>\n")
            t -= 1
            sdf3.write("%s</processor>\n" % (tabs(t)))
            t -= 1
            sdf3.write("%s</actorProperties>\n" % (tabs(t)))
        for edge in g.get_channels():
            sdf3.write(tabs(t) + "<channelProperties channel=\"" +\
                edge.get_name() + "\">\n")
            t += 1
            sdf3.write(tabs(t) + "<tokenSize sz=\"0\"/>\n")
            t -= 1
            sdf3.write(tabs(t) + "</channelProperties>\n")
        for actor in g.get_actors():
            sdf3.write(tabs(t) + "<channelProperties channel=\"" +\
                actor.get_name() + "2" + actor.get_name() + "\">\n")
            t += 1
            sdf3.write(tabs(t) + "<tokenSize sz=\"1\"/>\n")
            t -= 1
            sdf3.write(tabs(t) + "</channelProperties>\n")
           
        sdf3.write(tabs(t) + "<graphProperties>\n")
        t += 1
        sdf3.write(tabs(t) + "<timeConstraints>\n")
        t += 1
        sdf3.write(tabs(t) + "<throughput>" + str(throughput) + "</throughput>\n")
        t -= 1
        sdf3.write(tabs(t) + "</timeConstraints>\n")
        t -= 1
        sdf3.write(tabs(t) + "</graphProperties>\n")
        # Closeing part
        t -= 1
        sdf3.write("%s</sdfProperties>\n" % (tabs(t)))
        t -= 1
        sdf3.write("%s</applicationGraph>\n" % (tabs(t)))
        sdf3.write("</sdf3>\n")
        sdf3.flush()

    @staticmethod
    def dump_fsmsadf(sadf, g, throughput):
        t = 0
        tabs = Utilities.tabs
        sadf.write("""<?xml version='1.0' encoding='UTF-8'?>\n
<sdf3 type='fsmsadf' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns='uri:sdf3' version='1.0' xsi:schemaLocation='uri:sdf3 http://www.es.ele.tue.nl/sdf3/xsd/fsmsadf.xsd'>\n""")
        t += 1
        sadf.write(tabs(t) + "<applicationGraph name='" + g.get_graph_name() + "'>\n")
        t += 1
        sadf.write(tabs(t) + "<fsmsadf>\n")
        t += 1
        sadf.write(tabs(t) + "<scenariograph name='" + g.get_graph_name() + "-s1' type='" + g.get_graph_name() + "-S1'>\n")
        t += 1
        for actor in g.get_actors():
            sadf.write(tabs(t) + "<actor name='" + actor.get_name() + "' type='" + actor.get_name().capitalize() + "'>\n")
            t += 1
            for ip in actor.get_inports():
                sadf.write(tabs(t) + "<port name='" + ip.get_name() + "' type='in' rate='")
                for i in range(0,len(ip.get_sequence())):
                    if i < (len(ip.get_sequence()) - 1):
                        sadf.write("%s," % (ip.get_sequence()[i]))
                    else:
                        sadf.write("%s" % (ip.get_sequence()[i]))
                sadf.write("'/>\n")
            for op in actor.get_outports():
                sadf.write(tabs(t) + "<port name='" + op.get_name() + "' type='out' rate='")
                for i in range(0,len(op.get_sequence())):
                    if i < (len(op.get_sequence()) - 1):
                        sadf.write("%s," % (op.get_sequence()[i]))
                    else:
                        sadf.write("%s" % (op.get_sequence()[i]))
                sadf.write("'/>\n")
            sadf.write("""%s<port name="slop" type="out" rate="1"/>\n%s<port name="slip" type="in" rate="1"/>\n""" % (tabs(t), tabs(t)))
            t -= 1
            sadf.write(tabs(t) + "</actor>\n")

        for ch in g.get_channels():
            sadf.write(tabs(t) + "<channel name='" + ch.get_name() + "' srcActor='" + g.get_actor(ch.get_source()).get_name() + "' srcPort='" + ch.get_source_port().get_name() + "' dstActor='" + g.get_actor(ch.get_destination()).get_name() + "' dstPort='" + ch.get_destination_port().get_name() + "'/>\n")

        # Back edges
        for actor in g.get_actors():
            sadf.write("%s<channel name=\"%s2%s\" srcActor=\"%s\" srcPort=\"slop\" dstActor=\"%s\" dstPort=\"slip\" initialTokens='1'/>\n" % (tabs(t), actor.get_name(), actor.get_name(), actor.get_name(), actor.get_name()))

        t -= 1
        sadf.write(tabs(t) + "</scenariograph>\n")
        t -= 1
        sadf.write(tabs(t) + "</fsmsadf>\n")
        sadf.write(tabs(t) + "<fsmsadfProperties>\n")
        t += 1
        sadf.write(tabs(t) + "<defaultProperties>\n")
        t += 1
        for actor in g.get_actors():
            sadf.write(tabs(t) + "<actorProperties actor='" + actor.get_name() + "'>\n")
            t += 1 
            sadf.write(tabs(t) + "<processor type='arm7' default='true'>\n")
            t += 1
            sadf.write(tabs(t) + "<executionTime time='" + str(actor.get_wcet()) + "'/>\n")
            sadf.write(tabs(t) + "<memory>\n")
            t += 1
            sadf.write(tabs(t) + "<stateSize max='" + str(actor.get_code_size()) +"'/>\n")
            t -= 1
            sadf.write(tabs(t) + "</memory>\n")
            t -= 1
            sadf.write(tabs(t) + "</processor>\n")
            t -= 1
            sadf.write(tabs(t) + "</actorProperties>\n")

        for ch in g.get_channels():
            sadf.write(tabs(t) + "<channelProperties channel='" + ch.get_name() + "'>\n")
            t += 1
            sadf.write(tabs(t) + "<tokenSize sz='1'/>\n")
            t -= 1
            sadf.write(tabs(t) + "</channelProperties>\n")

        for actor in g.get_actors():
            sadf.write(tabs(t) + "<channelProperties channel='" + actor.get_name() + "2" + actor.get_name() + "'>\n")
            t += 1
            sadf.write(tabs(t) + "<tokenSize sz='1'/>\n")
            t -= 1
            sadf.write(tabs(t) + "</channelProperties>\n")

        t -= 1
        sadf.write(tabs(t) + "</defaultProperties>\n")
        sadf.write(tabs(t) + "<scenarios>\n")
        t += 1
        sadf.write(tabs(t) + "<scenario name='s1' graph='" + g.get_graph_name() + "-s1'>\n")
        sadf.write(tabs(t) + "</scenario>\n")
        t -= 1
        sadf.write(tabs(t) + "</scenarios>\n")
        sadf.write(tabs(t) + "<graphProperties>\n")
        t += 1
        sadf.write(tabs(t) + "<timeConstraints>\n")
        t += 1
        sadf.write(tabs(t) + "<throughput>" + str(throughput) + "</throughput>\n")
        t -= 1
        sadf.write(tabs(t) + "</timeConstraints>\n")
        t -= 1
        sadf.write(tabs(t) + "</graphProperties>\n")
        t -= 1
        sadf.write(tabs(t) + "</fsmsadfProperties>\n")
        sadf.write(tabs(t) + "<fsm initialstate='q1'>\n")
        t += 1
        sadf.write(tabs(t) + "<state name='q1' scenario='s1'>\n")
        t += 1
        sadf.write(tabs(t) + "<transition destination='q1'/>\n")
        t -= 1
        sadf.write(tabs(t) + "</state>\n")
        t -= 1
        sadf.write(tabs(t) + "</fsm>\n")
        t -= 1
        sadf.write(tabs(t) + "</applicationGraph>\n")
        t -=1 
        sadf.write(tabs(t) + "</sdf3>")
        sadf.flush()

    def parse_graph(self):
        """
            Parse the given graph in JSON format.
        """
        from ACSDFModel import ACSDFModel
        self.graph = ACSDFModel()

        objs = json.load(self.input_file)

        if objs["type"] != "csdf" and objs["type"] != "sdf":
            raise Exception("The graph type is not sdf or csdf")

        self.graph.set_graph_name(objs["name"])

        total_nodes = objs["node_number"]
        for node in objs["nodes"]:
            actor_id = node["id"]
            actor_name = node["name"]
            function = node["function"]
            seq_length = node["length"]
            wcet_seq = list(node["wcet"])
            wcet = max(wcet_seq)
            self.graph.add_actor(actor_id, actor_name, function, wcet)
            self.graph.get_actor(actor_id).set_wcet_sequence(wcet_seq)
            try:
                stateful = node["stateful"]
                code_size = node["code_size"]
                self.graph.get_actor(actor_id).set_code_size(code_size)
                self.graph.get_actor(actor_id).set_stateful(stateful)
            except Exception:
                pass

            num_of_ports = node["port_number"]
            for port in node["ports"]:
                port_type = port["type"]
                port_id = port["id"]
                port_name = "p%s" % port_id
                port_rate = list(port["rate"])
                assert len(port_rate) == seq_length
                if port_type == "out":
                    self.graph.get_actor(actor_id).add_outport(port_name, port_id, port_rate) 
                elif port_type == "in":
                    self.graph.get_actor(actor_id).add_inport(port_name, port_id, port_rate)
                else:
                    sys.stderr.write("Invalid port type!")
                    raise Exception("Invalid port type!")
            
        total_edges = objs["edge_number"]
        for edge in objs["edges"]:
            edge_id = edge["id"]
            edge_name = edge["name"]
            src_actor = edge["src"][0]
            src_port = edge["src"][1]
            dst_actor = edge["dst"][0]
            dst_port = edge["dst"][1]
            self.graph.add_channel(edge_id, edge_name, src_actor, self.graph.get_actor(src_actor).get_outport(src_port), dst_actor, self.graph.get_actor(dst_actor).get_inport(dst_port))

        return self.graph


if __name__ == '__main__':

    name = "myApplication"

    parser = argparse.ArgumentParser(description="Provides a way to convert between different graph formats", epilog="Author: %s" % __author__)
    group = parser.add_mutually_exclusive_group(required=False)
    group.add_argument("-s", action="store_true", help="Read a .gph and convert it to the SDF^3 XML format")
    group.add_argument("-g", action="store_true", help="Read an SDF^3 .xml and convert it to the .gph format")
    args = parser.parse_args()

    if args.g:
        CSDFParser.sdf3_to_gph(sys.stdin)
    else:
        cp = CSDFParser(name, sys.stdin)
        g = cp.parse_graph()

        if g == None:
            sys.stderr.write("Error encountered while processing the graph!\n")
            sys.exit(-1)

        if args.s:
            CSDFParser.dump_sdf3(sys.stdout, g)
        else:
            print(g)

    sys.exit(0)

