#
# $Id: makefile,v 1.2 2012/01/16 16:00:21 nikolov Exp $
#

ROOT  = ../../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

SUBPACKAGES = \
	platform \
	cdpn \
	cnn \
	sdfg

# Java files in this directory
JSRCS = \

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

