#
# $Id: makefile,v 1.1 2007/12/07 22:07:48 stefanov Exp $
#

ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../../../..

# Java files in this directory
JSRCS = \
	OperatorsComplexityEvaluator.java \
	DNNComponentsTimeEvaluator.java \
	DNNTimeEvaluator.java \
	DNNComponentsMemoryEvaluator.java \
	DNNMemoryEvaluator.java \
	DNNComponentsEnergyEvaluator.java \
	DNNEnergyEvaluator.java \
	ProcNumEvaluator.java \


SUBPACKAGES = \
	roofline \
	benchmarkbased \

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

