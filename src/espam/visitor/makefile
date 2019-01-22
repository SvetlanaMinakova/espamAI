#
# $Id: makefile,v 1.5 2010/11/05 16:23:58 svhaastr Exp $
#

ROOT  = ../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

SUBPACKAGES = \
	dot \
	xml \
	xps \
	ise \
	ipxact \
	systemc \
	hdpc \
	yapiPN \
	ymlPN \
	expression \
	json \
	onnx \ 
        sesame \
    	pthread

# Java files in this directory
JSRCS = \
	Visitor.java \
	PlatformVisitor.java \
	GraphVisitor.java \
	ADGraphVisitor.java \
	PNVisitor.java \
	CDPNVisitor.java \
	StatementVisitor.java \
	MappingVisitor.java \
	ExpressionVisitor.java \
	SDFGraphVisitor.java \
	CNNGraphVisitor.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

