#
# $Id: makefile,v 1.2 2011/10/05 15:03:46 nikolov Exp $
#

ROOT  = ../../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

SUBPACKAGES = \
    cpp \
    h \
    ymlSDF

# Java files in this directory
JSRCS = \
	SesameSDFGVisitor.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

