

ROOT  = ../../..

SUBPACKAGES = \

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

# Java files in this directory
JSRCS = \
    FileWorker.java \
	JSONFileWorker.java \
	ONNXFileWorker.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

