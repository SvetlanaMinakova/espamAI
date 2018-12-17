

ROOT  = ../../..

SUBPACKAGES = \

GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
ONNX = $(ROOT)/../lib/protobuf-java.jar
AUXCLASSPATH = $(GRAPHVIZ)$(CLASSPATHSEPARATOR)$(ONNX)

# Java files in this directory
JSRCS = \
    FileWorker.java \
	JSONFileWorker.java \
	ONNXFileWorker.java \
	DotFileWorker.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

