#
# $Id: makefile,v 1.1 2007/12/07 22:07:05 stefanov Exp $
#

ROOT  = ../../../..

SUBPACKAGES =

PARSER = $(ROOT)/lib/parser.jar
JAXP = $(ROOT)/lib/jaxp.jar
AUXCLASSPATH = $(PARSER)$(CLASSPATHSEPARATOR)$(JAXP)

# Java files in this directory
JSRCS = \
	ResolveEntityHandler.java \
	XmlErrorHandler.java \
	XmlSADGParser.java \
	Xml2SADG.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

