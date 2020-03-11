#
# $Id: makefile,v 1.1 2007/12/07 22:07:34 stefanov Exp $
#

ROOT  = ../../..

GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(GSON)

SUBPACKAGES = \
    refinement

# Java files in this directory
JSRCS = \
    CNNJSONVisitor.java \
    CSDFGraphJSONVisitor.java \
    CNNTopologyJSONVisitor.java \
    CSDFGraphShortJSONVisitor.java \
    CNNEvaluationJSONVisitor.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

