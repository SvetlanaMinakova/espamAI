#
# $Id: makefile,v 1.1 2007/12/07 22:07:48 stefanov Exp $
#

ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../../../..

# Java files in this directory
JSRCS = \
	EnergyEvaluator.java \
	CSDFGMemoryRefiner.java \
	CSDFTimingRefiner.java

SUBPACKAGES =

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

