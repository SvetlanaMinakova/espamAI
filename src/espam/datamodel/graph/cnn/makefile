#
# $Id: makefile,v 1.2 2011/10/05 15:03:46 nikolov Exp $
#

ROOT  = ../../../..

GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(GSON)

SUBPACKAGES = \
    connections \
    neurons

# Java files in this directory
JSRCS = \
	Network.java \
	NetworkTraverser.java \
	Layer.java \
	Neuron.java \
	BoundaryMode.java \
	Filtreable.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

