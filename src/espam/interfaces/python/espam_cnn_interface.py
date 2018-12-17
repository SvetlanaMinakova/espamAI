import os
import sys
from math import ceil
import json
import xml.etree.ElementTree as ET
sys.path.append('/vol/home/minakovas/darts_git/darts-master/darts')
from CSDFParser import CSDFParser

def main():
    import argparse
    parser = argparse.ArgumentParser(description='Evaluates CSDF graph in terms of power/performance')
    parser.add_argument('c', metavar='c', type=str, action='store', help='command')
    parser.add_argument('d', metavar='d', type=str, action='store', help='path to source SDF models directory')
    parser.add_argument('f', metavar='f', type=str, action='store', help='source SDF model file')
    args = parser.parse_args()
    try:
        command = args.c
        command_recognized = False
        # load SDF graph
        graph = load_graph(args.d, args.f)
        if command == "eval":
            eval_graph(graph)
            command_recognized = True
        if command == "rep_vec":
            calc_rep_vec(graph)
            command_recognized = True
        if command == "bottleneck":
            eval_graph(graph, 1, 1.0, False, False)
            get_bottleneck_actor(graph)
            command_recognized = True
        if command == "utilization" :
            eval_graph(graph, 1, 1.0, False, False)
            get_utilization(graph)
            command_recognized = True
        if  command_recognized == False:
            raise Exception("unrecognized command")

    except FileNotFoundError:
        sys.stdout.write("Error: SDF model file not found")
        # in case of any DARTS internal errors
    except Exception:
        sys.stdout.write("Error: Darts internal exception ")


def print_error(msg):
    sys.stdout.write("Error: "+ msg)


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
        if (wcet_seq.__len__() == 0):
            sys.stdout.write(actor_name + " empty wcets!!!")
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
        self.graph.add_channel(edge_id, edge_name, src_actor, self.graph.get_actor(src_actor).get_outport(src_port),
                               dst_actor, self.graph.get_actor(dst_actor).get_inport(dst_port))
        channel = self.graph.get_channel(edge_id)
        #set initial tokens for self-loops
        try:
            initial_tokens = edge["initial_tokens"]
            channel.set_initial_tokens(initial_tokens)
        except Exception:
            pass

    return self.graph

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

""" Loading .json (analogue for .gph is ACSDFModel.load_from_file"""
# parameters:
# dir - file source directory
# filename - name of file
# scaling factor - by default = 1
# deadline_factor - the deadline factor of the graph. Must be real-valued
# and between [0,1]. by default deadline factor = 1.0
# boolean verbose - if there is a need to print details


def load_graph(dir,filename):
    file = get_file(dir,filename)
    if file is None:
        raise FileNotFoundError

    #sys.stdout.write("Graph file loaded")
    """ Parsing .json file to obtain ACSDF model"""
    #create parser instance
    splittedfilename = filename.split(".")
    model_name = splittedfilename[0]

    # create JSON parser instance
    parserInstance = CSDFParser(model_name, file)

    # parse graph from loaded file
    graph = parse_graph(parserInstance)
    return graph

def eval_graph(graph, scaling_factor=1, deadline_factor=1.0, verbose=False, printDetails=True):
    """Calculate and set loaded graph parameters for
     parsed graph (analogue for .gph is ACSDFModel.load_from_file)"""
    graph.set_deadline_factor(deadline_factor)
    graph.compute_repetition_vector()
    if verbose:
        sys.stdout.write("Computing the minimum period vector...\n")
    graph.find_minimum_repetition_vector()

    graph.set_scaling_factor(scaling_factor)
    graph.find_minimum_period_vector(graph.get_scaling_factor())

    for a in graph.get_actors():    # Initial value of deadline
        a.set_deadline(a.get_period())
    # Updates the start time and deadline
    graph.find_earliest_start_time_vector()

    """Calculate start time buffer vector """
    graph.calc_start_time_buffer_vector()

    if(verbose):
        print_parameters(graph)

    """Total buffer size """
    total_buffer_size = 0
    for c in graph.get_channels():
        total_buffer_size += c.get_buffer_size()

    if(verbose):
        print_total_buffer_size(total_buffer_size)

    # if graph was sucessfully loaded, evaluate it and print results to output stream
    if(printDetails):
        print_evaluation_results(graph)

    return graph

def calc_rep_vec(graph, deadline_factor=1.0, verbose=False, printDetails=True):
    """Calculate and set loaded graph parameters for
     parsed graph (analogue for .gph is ACSDFModel.load_from_file)"""
    graph.set_deadline_factor(deadline_factor)
    if verbose:
        sys.stdout.write("Computing the repetition vector...\n")
    graph.compute_repetition_vector()
    graph.find_minimum_repetition_vector()

    # if graph was sucessfully loaded, evaluate it and print results to output stream
    if(printDetails):
        sys.stdout.write(get_repetition_vector_str_with_ids(graph))

    return graph


"""get repetition vector of the graph as list"""
def get_repetition_vector_str_with_ids(graph):
    """Return the repetition vector as a string, using acto node ids"""
    repetition_vector = ["["]
    for i in graph.actors:
        repetition_vector.append("%s = %s, " % (i.get_actor_id(), i.get_repetition()))
    repetition_vector.append("]")
    return ''.join(repetition_vector)


"""get bottleneck actor of the graph"""
def get_bottleneck_actor(graph):
   #sys.stdout.write("Bottleneck actor: ")
   actor = graph.get_bottelneck_actor()
   sys.stdout.write(actor.get_name())

"""get actors utilization"""
def get_utilization(graph):
    """Return the repetition vector as a string, using acto node ids"""
    utilization_vector = ["["]
    for i in graph.actors:
        utilization_vector.append("%s = %s, " % (i.get_actor_id(), i.get_utilization()))
    utilization_vector.append("]")
    util_vec = ''.join(utilization_vector)
    sys.stdout.write(util_vec)


"""Writes parameters, obtained afther SDFG evaluation to specified file"""
# parameters:
# dir - SDF model file source directory
# filename - name of file with SDF model
# scaling factor - by default = 1
# deadline_factor - the deadline factor of the graph. Must be real-valued
# and between [0,1]. by default deadline factor = 1.0
# boolean verbose - if there is a need to print details
def write_evaluation_results(graph, dir):
    #sys.stdout.write("evaluation results writing...")
    #result_file_path = dir + os.sep + graph.name+"_result.json"
    result_file_path = dir
    mode = 'w' if os.path.exists(result_file_path) else 'a'
    with open(result_file_path, mode) as file:
        file.write(' {\n')
        file.write(' "performance": ' + str(graph.get_latency()) + ' ,\n')
        file.write(' "power": ' + str(graph.get_utilization()) + ' ,\n')
        file.write(' "memory": ' + str(graph.get_total_buffer_size()) + ' ,\n')
        file.write(' "processors": ' + str(ceil(graph.get_utilization())))
        file.write("\n}\n")
    #sys.stdout.write("evaluation results are written")



"""Printss parameters, obtained afther SDFG evaluation to output stream"""
# parameters:
# dir - SDF model file source directory
# filename - name of file with SDF model
# scaling factor - by default = 1
# deadline_factor - the deadline factor of the graph. Must be real-valued
# and between [0,1]. by default deadline factor = 1.0
def print_evaluation_results(graph):
    sys.stdout.write(' {\n')
    sys.stdout.write(' "performance": ' + str(graph.get_latency()) + ' ,\n')
    sys.stdout.write(' "power": ' + str(graph.get_utilization()) + ' ,\n')
    sys.stdout.write(' "memory": ' + str(graph.get_total_buffer_size()) + ' ,\n'),
    sys.stdout.write(' "processors": ' + str(ceil(graph.get_utilization())))
    sys.stdout.write("\n}\n")

def print_total_buffer_size (total_buffer_size):
    sys.stdout.write(" -- Total buffer size = %s\n" % (total_buffer_size))


def print_to_dot(graph, dir):
    """Print .dot representation of the graph into file """
    graph.print_graph_dot(graph, dir)


# get file from directory
def get_file(dir,name):
    fullpath = os.path.join(dir, name)
    file = None
    if os.path.isfile(fullpath):
        file = open(fullpath)

    return file

# read file
def read_file(fullpath):
    file = open(fullpath)
    for line in file:
        sys.stdout.write(line)
    file.close()

# print whole bunch of the parametes calculated by DARTS
def print_parameters (graph):
    sys.stdout.write(" -- Number of actors = %s\n" % (graph.get_num_of_actors()))
    sys.stdout.write(" -- Repetition vector = %s\n" % (graph.get_repetition_vector_str()))
    sys.stdout.write(" -- Worst-case execution time vector = %s\n" % (graph.get_execution_vector_str()))
    sys.stdout.write(" -- Minimum period vector = %s\n" % (graph.get_period_vector_str()))
    sys.stdout.write(" -- Deadline vector = %s\n" % (graph.get_deadline_vector_str()))
    sys.stdout.write(" -- Start time vector = %s\n" % (graph.get_start_time_vector_str()))
    sys.stdout.write(" -- Buffer size vector = %s\n" % (graph.get_buffer_size_vector_str()))
    sys.stdout.write(" -- Total buffer size = %s\n" % str(graph.get_total_buffer_size()))
    sys.stdout.write(" -- Total Utilization  = %s\n" % (graph.get_utilization()))
    sys.stdout.write(" -- Total Density = %s\n" % (graph.get_density()))
    sys.stdout.write(" -- Graph maximum latency = %s\n" % (graph.get_latency()))

if __name__ == "__main__":
    main()
