#
# $Id: makefile,v 1.1 2007/12/07 22:06:44 stefanov Exp $
#

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)

ROOT  = ..

SUBPACKAGES = \
	main \
	datamodel \
	visitor \
	parser \
	operations \
	utils

# Java files in this directory
JSRCS = 

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

