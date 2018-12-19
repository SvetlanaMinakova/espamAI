#
# $Id: makefile,v 1.1 2007/12/07 22:06:42 stefanov Exp $
#
# makefile for the diva top-level package. The rules
# implemented by this makefile are:
#
# realclean:Remove all class files and junk from mp
#           including all subdirectories.
# package:  Compile the classes in all diva packages and in their
#           sub-packages, not including test files.
# all:      Compile the classes in all diva packages and in all
#           sub-packages, including the test files.
# install:  Do $(MAKE) install in the source directories. Then
#           do $(MAKE) install in the documentation directories.
#           This will generate all necessary documentation.
# jar:      Build the Diva jar file.
#
# The intent is that a user or casual developer can run "install"
# to generate class files, JAR files, API docs, and so on. A
# serious developer will need to run "all" to $(MAKE) everything,
# including the tests, but will not get the JAR files.

PACKAGEDIRS = 	\
		src


PFLAGS=""

all:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) -w all;\
			) \
		    fi ; \
		done ; \
	fi

test:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) -w -k test;\
			) \
		    fi ; \
		done ; \
	fi

clean:
	@if [ "x$(PACKAGEDIRS)" != "x" ]; then \
		set $(PACKAGEDIRS); \
		for x do \
		    if [ -r $$x ] && [ -r $$x/makefile ] ; then \
			( cd $$x ; \
			$(MAKE) -w -k realclean;\
			) \
		    fi ; \
		done ; \
	fi

docall:
	(cd ./src; ${MAKE} javadoc )
	
jar:
	#create .jar with all the classes	
	(cd ./src; $(JAR) -cvf ../espam.jar `find -name "*.class"` )

jar-exec:
	#create executable .jar with all the classes and manifest
	#create manifest with all dependencies
	(python manifestgenerator.py )	
	#create .jar with all the classes
	(cd ./src; $(JAR) -cvf ../espam.jar `find -name "*.class"` )
	#add manifest to .jar
	(jar umf Manifest.txt espam.jar)
	

#release:
#	( cd ./compaan; \
#	  make release \
#	)

.SUFFIXES: .class .java .h

# The default compile rule
.java.class:
	rm -f `basename $< .java`.class
	CLASSPATH="./pd" $(JAVAC) $<

# Check the make file definition
include ./src/vars.mk

