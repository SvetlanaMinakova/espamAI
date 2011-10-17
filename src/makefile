#
# $Id: makefile,v 1.2 2011/10/17 13:17:48 tzhai Exp $
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
# javadoc:      Build the HTML documentation.
#
# The intent is that a user or casual developer can run "install"
# to generate class files, JAR files, API docs, and so on. A
# serious developer will need to run "all" to make everything,
# including the tests, but will not get the JAR files.

ROOT	= .

PACKAGEDIRS = 	\
		espam

# list of packages for which javadoc will
# generated documentation
include javaclasses.mk
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
realclean:
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


# create the javadoc documentation. Relies on javaclasses.mk 
# for the packages to generate documentation for
javadoc: 
	if [ ! -d ./doc/codeDoc ]; then mkdir -p ./doc/codeDoc; fi; \
		$(JAVADOC) -link http://java.sun.com/j2se/1.4.2/docs/api -breakiterator -d ./doc/codeDoc -private -version -author -quiet -classpath "." -sourcepath . -subpackages $(JAVACLASSES)

# Make Java classes in all packages
jtest:
	@echo "#"
	@echo "# Check the java Dg files"
	@echo "#"
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) jtest ;\
			) \
		    fi ; \
		done ; \
	fi

test::
	( cd ./test; make test; )

test_panda::
	( cd ./test/panda; make test; )

# If necessary, instrument the classes, then rebuild, then run the tests
jsall:
	if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) jsall ;\
			) \
		    fi ; \
		done ; \
	fi

# If necessary, instrument the classes, then rebuild, then run the tests
jsbuild:
	if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) jsbuild ;\
			) \
		    fi ; \
		done ; \
	fi

# If necessary, instrument the classes, then rebuild, then run the tests
jsrestore:
	if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) jsrestore ;\
			) \
		    fi ; \
		done ; \
	fi

# If necessary, instrument the classes, then rebuild, then run the tests
jstest:
	if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) jstest ;\
			) \
		    fi ; \
		done ; \
	fi

jtext: junit/AllTests.class
	$(JAVA) -classpath ".$(CLASSPATHSEPARATOR)./lib/junit.jar" junit.AllTests

# Build a JAR file of all the classes.
jgui: junit/AllTests.class
	$(JAVA) -classpath ".$(CLASSPATHSEPARATOR)./lib/junit.jar" junit.swingui.TestRunner junit.AllTests

# Build a JAR file of all the classes.
#jar:
#        cd ..; sh diva/bin/makeJar $(JAVAHOME) diva diva.jar $(PACKAGES) \
#                $(AUXILIARY_PACKAGES) $(UNRELEASED_PACKAGES)
