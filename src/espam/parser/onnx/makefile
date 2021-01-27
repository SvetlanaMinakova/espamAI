#
# $Id: makefile,v 1.1 2007/12/07 22:07:04 stefanov Exp $
#

ROOT  = ../../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
PROTOBUF = $(ROOT)/../lib/potobuf-java.jar
AUXCLASSPATH = $(GSON)$(CLASSPATHSEPARATOR)$(PROTOBUF)$(CLASSPATHSEPARATOR)$(ONNX)$(CLASSPATHSEPARATOR)

SUBPACKAGES = \

# Java files in this directory
JSRCS = \
	ONNX2CNNConverter.java \
	ONNXtoESPAMNetworkTraverser.java \
	InferenceDNNOptimizer.java \
	initializersMetaData.java \
	SupportedONNXOpsList.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

