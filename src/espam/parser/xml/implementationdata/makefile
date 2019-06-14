ROOT  = ../../../..

SUBPACKAGES =

PARSER = $(ROOT)/lib/parser.jar
JAXP = $(ROOT)/lib/jaxp.jar
AUXCLASSPATH = $(PARSER)$(CLASSPATHSEPARATOR)$(JAXP)

# Java files in this directory
JSRCS = \
	ResolveEntityHandler.java \
	XmlErrorHandler.java \
	Xml2ImplementationTable.java \
	XmlImplementationDataParser.java

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk
