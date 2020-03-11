#
# $Id: makefile,v 1.1 2007/12/07 22:07:47 stefanov Exp $
#

ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../../../..

SUBPACKAGES = \

# Java files in this directory
JSRCS = \
	dnnScheduler.java\
	layerFiring

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

