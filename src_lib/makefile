#
# $Id: makefile,v 1.1 2007/12/07 22:09:22 stefanov Exp $
#
# Makefile for the diva top-level package. The rules
# implemented by this makefile are:
#
# clean:    Remove all class files and junk from Diva
#           including all subdirectories.
# package:  Compile the classes in all diva packages and in their
#           sub-packages, not including test files.
# all:      Compile the classes in all diva packages and in all
#           sub-packages, including the test files.
# install:  Do make install in the source directories. Then
#           do make install in the documentation directories.
#           This will generate all necessary documentation.
# jar:      Build the Diva jar file.
# doc:      Build the HTML documentation.
#
# The intent is that a user or casual developer can run "install"
# to generate class files, JAR files, API docs, and so on. A
# serious developer will need to run "all" to make everything,
# including the tests, but will not get the JAR files.


PACKAGEDIRS = 	\
		polylib \
		onnx

# list of packages for which javadoc will
# generated documentation
include vars.mk

# The all rule does "all" in all source directories
all:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) all;\
			) \
		    fi ; \
		done ; \
	fi

# "packages" is a synonym for "package"
packages: package

# Make Java classes in all packages
package:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) package ;\
			) \
		    fi ; \
		done ; \
	fi

# Make Java classes in all packages
clean:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) clean ;\
			) \
		    fi ; \
		done ; \
	fi
# Make Java classes in all packages
realfast:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) fast ;\
			) \
		    fi ; \
		done ; \
	fi

# $(MAKE) tags to be used in vi or (x)emacs
tags:
	if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS) ; \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) tags ;\
			) \
		    fi ; \
		done ; \
	fi

# Build a JAR file of all the classes.
#jar:
#        cd ..; sh diva/bin/makeJar $(JAVAHOME) diva diva.jar $(PACKAGES) \
#                $(AUXILIARY_PACKAGES) $(UNRELEASED_PACKAGES)

configure: configure.in
	@echo "configure is out of date"
	@echo "Please run autoconf and rerun configure if needed"
	@false
