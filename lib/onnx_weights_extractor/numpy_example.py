import numpy
import os


def save_as_npy(directory, filename, nparr):
    if not os.path.exists(directory):
        os.makedirs(directory)
    file_path = directory + os.sep + filename
    numpy.save(file_path, nparr)

#print(arr2.shape)

dir_to_save ="./"
file_name = "arr_3_4_5"

inp_path = "/vol/home/minakovas/espam2/output_models/CNTKGraph/data/inputs/input0.npy"
weights_path = "./Convolution28_w.npy"

#save_as_npy(dir_to_save,file_name,arr2)
inp = numpy.load(inp_path)
weights = numpy.load(weights_path)

print(inp)
