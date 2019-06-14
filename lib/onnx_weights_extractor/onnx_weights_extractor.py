import onnx
from onnx import numpy_helper
import numpy
import json
import os
import math

""" To use this script you should have python onnx and python json installed """

def main():
    import argparse
    parser = argparse.ArgumentParser(description='extracts ONNX model initializers and saves them as .npy files')
    parser.add_argument('d', metavar='d', type=str, action='store', help='path to dnn.onnx source file')
    parser.add_argument('m', metavar='m', type=str, action='store', help='path to metadata .json file')
    parser.add_argument('o', metavar='o', type=str, action='store', help='output files directory')
    args = parser.parse_args()
    try:
        extract_weights(args.d, args.m, args.o, print_details=False)

    except Exception:
        print("Error: ONNX weights extraction exception ")

""" extract initializers from onnx model and save them as .npy files
    :param dnn: path to dnn.onnx source file
    :param dnn_metadata: path to metadata .json file
    :param out_dir: output files directory
    :param print_details: if the parameters extraction details should be printed
"""
def extract_weights(dnn, dnn_metadata, out_dir, print_details=False):
    try:
        if not os.path.exists(out_dir):
            os.makedirs(out_dir)

        m = onnx.load(dnn)
    except FileNotFoundError:
        print("Error: ONNX file not found")
        return

    initializers = m.graph.initializer
    nodes = m.graph.node

    try:
        with open(dnn_metadata) as json_file:
            data = json.load(json_file)
            conv_weights = data['conv_weights']
            dense_weights = data['dense_weights']
            biases = data['biases']

            conv_weights_nodes = data['conv_weights_nodes']
            dense_weights_nodes = data['dense_weights_nodes']
            biases_nodes = data['biases_nodes']

            dense_partition = data['dense_partition']
            if dense_partition is None:
                dense_partition = 100

            """process convolutional weights"""
            for weight in conv_weights.items():
                init = get_initializer(initializers, weight[1])
                if init is None:
                    print("Error:", weight[1], "weight initializer not found")
                else:
                    npinit = numpy_helper.to_array(init)
                    filename = weight[0] + "_w"
                    save_conv_weights(out_dir, filename, npinit, init.dims, print_details)

            for weight in conv_weights_nodes.items():
                wnode = get_node(nodes, weight[1])
                if wnode is None:
                    print("Error:", weight[1], "weight node not found")
                else:
                    val = get_value(wnode)
                    npval = numpy_helper.to_array(val)
                    filename = weight[0] + "_w"
                    print(filename,"dims=",val.dims)
                    save_conv_weights(out_dir, filename, npval, val.dims, print_details)

            """process dense weights"""
            for weight in dense_weights.items():
                init = get_initializer(initializers, weight[1])
                if init is None:
                    print("Error:", weight[1], "weight initializer not found")
                else:
                    npinit = numpy_helper.to_array(init)
                    filename = weight[0] + "_w"
                    save_dense_weights(out_dir, filename, npinit, init.dims, dense_partition, print_details)

            for weight in dense_weights_nodes.items():
                wnode = get_node(nodes, weight[1])
                if wnode is None:
                    print("Error:", weight[1], "weight node not found")
                else:
                    val = get_value(wnode)
                    npval = numpy_helper.to_array(val)
                    filename = weight[0] + "_w"
                    save_dense_weights(out_dir, filename, npval, val.dims, dense_partition, print_details)

            """process biases"""
            for bias in biases.items():
                init = get_initializer(initializers, bias[1])
                if init is None:
                    print("Error:", bias[1], "bias initializer not found")
                else:
                    npinit = numpy_helper.to_array(init)
                    filename = bias[0] + "_b"
                    save_bias(out_dir, filename, npinit, print_details)


            for bias in biases_nodes.items():
                bnode = get_node(nodes, bias[1])
                if bnode is None:
                    print("Error:", bias[1], "bias node not found")
                else:
                    val = get_value(bnode)
                    npval = numpy_helper.to_array(val)
                    filename = bias[0] + "_b"
                    save_bias(out_dir, filename, npval, print_details)

    except FileNotFoundError:
        print("Error: JSON metadata file not found")
    return

""" find initializer by its name
    :param initializers: list of initializers
    :param name: initializer name
    :param nparr: numpy array
"""
def get_initializer (initializers, name):
    for init in initializers:
        if init.name == name:
            return init
    return None

""" find node by its name
    :param nodes: list of nodes
    :param name: node name
"""
def get_node (nodes, name):
   try:
        for node in nodes:
            if node.name == name:
                return node
        return None
   except Exception:
       print(Exception)


""" find node attribute, called 'value'
    :param node: onnx node
"""
def get_value(node):
    attrs = node.attribute
    if attrs is None:
        print("node", node.name, "does not have value!")
        return
    for attr in attrs:
        if attr.name == "value":
            tensor_attr = attr.t
            return tensor_attr

    print("value is not found in node", node.name)
    return None


""" save convolutional weights as 4d numpy array (.npy) file
    Note: Convolutional nodes should have 4 dimensionsional weights!
    :param directory: file directory
    :param filename: name of the file 
    :param nparr: numpy array
    :param dims: array dimensions
    :param print_details: if the parameters extraction details should be printed
"""
def save_conv_weights(directory, filename, nparr, dims, print_details=False):
    if(len(dims)!=4):
        print(filename, "wrong weights shape", dims,", 4 dimensions expected")
    else:
        save4dArr(directory, filename, nparr, dims, print_details)

""" save dense weights as 2d numpy array (.npy) file
    Note: Gemm/MatMul nodes should have 2 dimensionsional weights!
    :param directory: file directory
    :param filename: name of the file 
    :param nparr: numpy array
    :param dims: array dimensions
    :param print_details: if the parameters extraction details should be printed
"""
def save_dense_weights(directory, filename, nparr, dims, partition_size, print_details=False):
    if(len(dims)==2):
        save2dArr(directory, filename, nparr, dims, partition_size, print_details)
        return
    if(len(dims)<2):
        print(filename, "wrong weights shape", dims, ", 2 dimensions expected")
        return

    linear_dims = []
    lin_inputs = 1
    for i in range(len(dims)-1):
        lin_inputs *= dims[i]
    linear_dims.append(lin_inputs)

    neurons = dims[len(dims)-1]
    linear_dims.append(neurons)

    print("linear dims", linear_dims)

    lin_arr = nparr.reshape(linear_dims)
    save2dArr(directory, filename, lin_arr, linear_dims, partition_size, print_details)

    if(print_details):
        print(filename," weights linearized: ", dims, "-->", linear_dims)


""" check if start dimensions can be shrinked"""
def start_dims_to_shrink(dims):
    to_shrink = 0
    for dim in dims:
        if dim == 1:
            to_shrink = to_shrink+1
        else:
            return to_shrink
    return to_shrink


""" save bias as 1d numpy array (.npy) file
    Note: all biases are saved as 1d arrays!
    :param directory: file directory
    :param filename: name of the file 
    :param nparr: numpy array
    :param dims: array dimensions
    :param print_details: if the parameters extraction details should be printed
"""
def save_bias(directory, filename, nparr,print_details=False):
    saveAs1dArr(directory, filename, nparr, print_details)

""" Save 4-dimensional numpy array as .npy file
    :param directory: file directory
    :param name: name of the file 
    :param nparr: numpy array
"""
def save4dArr(directory, filename, nparr, dims, print_details=False):
    wpart = 0
    for d0 in range(0, dims[0]):
        fn = filename + str(wpart)
        save_as_npy(directory,fn,nparr[d0])
        wpart = wpart + 1
    if print_details:
        print(filename, "saved")

""" Save 2-dimensional numpy array as .npy file
    :param directory: file directory
    :param directory: file directory
    :param name: name of the file 
    :param nparr: numpy array
"""
def save2dArr(directory, filename, nparr, dims, partition_size, print_details=False):
    wpart = 0
    neurs = dims[0]

    if neurs > partition_size:
        partitions_num = math.floor(neurs/partition_size)
        print(filename,"has",partitions_num,"partitions")
        partition_sizes = []
        for i in range(0, partitions_num):
            partition_sizes.append(partition_size)

        # data tail
        if neurs%partition_size != 0:
            partition_sizes.append(neurs - (partition_size * (partitions_num)))
            partitions_num = partitions_num + 1

        start = 0
        end = partition_size

        for i in range(0, partitions_num):
            fn = filename + str(wpart)
            #print(fn,"from", start, "to", end)
            save_as_npy(directory, fn, nparr[start:end])
            #print("arr partition", nparr[start:end])
            wpart = wpart + 1
            start += partition_sizes[i]
            if i==0:
                start+=1
            if i!=partitions_num - 1 :
                end +=partition_sizes[i+1]

        pass

    else:
        fn = filename + str(wpart)
        save_as_npy(directory, fn, nparr)

    if print_details:
        print(filename, "saved")

""" Save 1-dimensional numpy array as .npy file
    :param directory: file directory
    :param name: name of the file 
    :param nparr: numpy array
"""
def save1dArr(directory, filename, nparr, print_details = False):
    save_as_npy(directory, filename, nparr)

    if print_details:
        print(filename, "saved")

""" Save 1-dimensional numpy array as .npy file
    :param directory: file directory
    :param name: name of the file 
    :param nparr: numpy array
"""
def saveAs1dArr(directory, filename, nparr, print_details = False):
    nparr_lin = nparr.reshape(1,-1)
    save_as_npy(directory, filename, nparr_lin)

    if print_details:
        print(filename, "saved")


""" Save numpy array as .npy file
    :param directory: file directory
    :param name: name of the file 
    :param nparr: numpy array
"""
def save_as_npy(directory, filename, nparr):
    file_path = directory + os.sep + filename
    numpy.save(file_path, nparr)


if __name__ == "__main__":
    main()

def print_error(msg):
    print("Error: " + msg)
