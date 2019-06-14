# Copyright (c) 1999 Leiden University (LIACS)
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice appears in all copies of this software. the
# software provided hereunder is on an "as is" basis, and the
# copyright holder has no obligation to provide maintenance, support,
# updates, enhancements, or modifications.
#
# @Author Todor Stefanov
# $Id: makefile,v 1.1 2007/12/07 22:06:44 stefanov Exp $
#

ROOT  = ../..

ONNX = $(ROOT)/../lib/onnx.jar
GSON = $(ROOT)/../lib/gson.jar
GRAPHVIZ = $(ROOT)/../lib/graphviz.jar
AUXCLASSPATH = $(ONNX)$(CLASSPATHSEPARATOR)$(GSON)$(CLASSPATHSEPARATOR)$(GRAPHVIZ)

# Java files in this directory
JSRCS = \
	Copyright.java \
	UserInterface.java \
	Options.java \
	Config.java \
	Main.java \
	ExtensionFilter.java

SUBPACKAGES = \
    cnnUI

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk
