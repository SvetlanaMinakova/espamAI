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

PARSER = $(ROOT)/lib/parser.jar
JAXP = $(ROOT)/lib/jaxp.jar
AUXCLASSPATH = $(PARSER)$(CLASSPATHSEPARATOR)$(JAXP)

# Java files in this directory
JSRCS = \
	Copyright.java \
	UserInterface.java \
	Options.java \
	Main.java


# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk
