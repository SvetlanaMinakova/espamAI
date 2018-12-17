#
# $Id: makefile,v 1.2 2011/10/05 15:03:46 nikolov Exp $
#

ROOT  = ../../../..

ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

SUBPACKAGES = \
    datastructures

# Java files in this directory
JSRCS = \
	CSDFGraph.java \
	CSDFGNode.java \
	CSDFGEdge.java \
	CSDFGPort.java \
	CSDFGPortType.java \
	SDFGType.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

