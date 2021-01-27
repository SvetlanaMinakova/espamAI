#
# $Id: makefile,v 1.1 2007/12/07 22:09:09 stefanov Exp $
#

ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../../..

SUBPACKAGES = \
	adg \
	csdf \
	cnn \
	sbrs

# Java files in this directory
JSRCS = \
	NPort.java \
	Edge.java \
	Node.java \
	Graph.java


# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

