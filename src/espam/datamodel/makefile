#
# $Id: makefile,v 1.1 2007/12/07 22:09:02 stefanov Exp $
#

GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
ONNX = $(ROOT)/../lib/protobuf-java.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(GRAPHVIZ)$(CLASSPATHSEPARATOR)$(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ../..

# Java packages in this directory
SUBPACKAGES = \
	platform \
	domain \
	graph \
	pn \
	parsetree \
	mapping \
	implementationdata

# Java files in this directory
JSRCS = \
	EspamException.java \
	LinearizationType.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk


