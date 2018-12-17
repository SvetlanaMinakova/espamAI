#
# $Id: makefile,v 1.1 2007/12/07 22:06:45 stefanov Exp $
#

GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(GRAPHVIZ)$(CLASSPATHSEPARATOR)$(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../..

SUBPACKAGES = \
	polylib \
	symbolic \
	util \
	fileworker

# Java files in this directory
JSRCS = 

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

