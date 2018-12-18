# Makefile for Panda distribution
#
# Version identification:
# $Id: vars.mk.in,v 1.1 2007/12/07 22:09:13 stefanov Exp $
# Date of creation: 7/31/96
# Author: Bart Kienhuis

# NOTE: Don't edit this file if it is called ptII.mk, instead
# edit ptII.mk.in, which is read by configure

# Default top-level directory.  Usually this is the same as $PTII
prefix =	/vol/home/minakovas/espam_cnn_merge

# Usually the same as prefix.  exec_prefix is part of the autoconf standard.
exec_prefix =	${prefix}

# Source directory we are building from.
srcdir =	.

# The home of the Java Developer's Kit (JDK)
# Generating Java documentation uses this makefile variable
# The line below gets subsituted by the configure script
JAVA_DIR = 	/usr/lib/jvm/java-8-oracle

# JDK Version from the java.version property
JVERSION =	@JVERSION@

# Java CLASSPATH separator
# For Unix, this would be :
# For Cygwin, this would be ;
CLASSPATHSEPARATOR = :

########## You should not have to change anything below this line ######

# The 'javac' compiler.
JAVAC = 	$(JAVA_DIR)/bin/javac

# The 'javah' compiler.
JAVAH = 	$(JAVA_DIR)/bin/javah

# Flags to pass to javac.  Usually something like '-g'
# This line gets substituted by configure
JDEBUG =	-g
JOPTIMIZE =	#-O
JFLAGS = 	$(JDEBUG) $(JOPTIMIZE) -source 1.8

# The 'javadoc' program
JAVADOC = 	$(JAVA_DIR)/bin/javadoc -source 1.8

# Flags to pass to javadoc.
JDOCFLAGS = 	-author -version

# The jar command, used to produce jar files, which are similar to tar files
JAR =		$(JAVA_DIR)/bin/jar

# The 'java' interpreter.
JAVA =		$(JAVA_DIR)/bin/java

# JavaCC is the Java Compiler Compiler which is used by ptolemy.data.expr
# The default location is $(PTII)/vendors/sun/JavaCC
JAVACC_DIR = 	@JAVACC_DIR@

# Under Unix:
# JJTREE =	$(JAVACC_DIR)/bin/jjtree
# JAVACC =	$(JAVACC_DIR)/bin/javacc
# Under Cygwin32 NT the following should be used and JavaCC.zip must be in
# the CLASSPATH
# JJTREE = 	$(JAVA) -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(JAVACC_DIR)/bin/lib/javacc.jar" org.javacc.jjtree.Main
# JAVACC = 	$(JAVA) -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(JAVACC_DIR)/bin/lib/javacc.jar" org.javacc.parser.Main
JJTREE = 	@JJTREE@
JAVACC = 	@JAVACC@

# Jar file that contains Jacl
PTJACL_JAR =	$(ROOT)/lib/ptjacl.jar

# Flags to pass java when we start Jacl.
PTJACL_JFLAG =  @TCLFLAGS@

# jtclsh script to run Jacl for the test suite.
# We could use bin/ptjacl here, but instead we start it from within
# make and avoid problems
JTCLSH =	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)$(PTJACL_JAR)$(CLASSPATHSEPARATOR)$(JAVASCOPECLASSPATH)" $(JAVA) $(PTJACL_JFLAG) tcl.lang.Shell

# Commands used to install scripts and data
INSTALL =		$(PANDA)/config/install-sh -c
INSTALL_PROGRAM =	${INSTALL}
INSTALL_DATA =		${INSTALL} -m 644
