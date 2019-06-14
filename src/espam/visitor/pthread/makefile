#
#
#

ROOT  = ../../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

SUBPACKAGES = \
    cpp \
    h \
    weights

# Java files in this directory
JSRCS = \
	PthreadSDFGVisitor.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

