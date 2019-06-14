#
# $Id: makefile,v 1.1 2007/12/07 22:07:34 stefanov Exp $
#

ROOT  = ../../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

SUBPACKAGES = \
	adg \
	cdpn \
	csdf

# Java files in this directory
JSRCS = \

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

