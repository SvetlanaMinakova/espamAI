# $Id: default_functions.mk,v 1.1 2007/12/07 22:09:21 stefanov Exp $
#
# Shared compilation rules for Diva
#
# Note that all "-w's" have been changed to "-r's" for TCSH support.
#

# This rules assumes that the following variables have been
# set, in addition to the variables that point to the Java
# installation:
#
# SUBPACKAGES:  The sub-packages that are to be compiled in the default
#            compilation. This should not include test/.
# SUBDIRS:      All sub-directories of this directory, for use by
#            the clean and install rules.
#
# The rules implemented by this file are:
#
# clean:    Remove all class files and junk from this package
#           including all subdirectories.
# classes:  Compile the classes in this directory only.
# depend:   Rebuild java dependencies
# package:  Compile the classes in this directory and all
#           directories in SUBPACKAGES.
# all:      Compile the classes in this directory and all
#           directories in SUBDIRS.
# install:  In source directories, this is the same as make package.
#
# The intent is that a user or casual developer can run "install"
# to generate class files, JAR files, API docs, and so on. A
# serious developer will need to run "all" to make everything,
# including the tests, but will not get the JAR files.


# Compile Java files
JCLASS = $(JSRCS:%.java=%.class)
#PARSER = $(ROOT)/lib/parser.jar
#JAXP = $(ROOT)/lib/jaxp.jar
#AUXCLASSPATH = $(PARSER)$(CLASSPATHSEPARATOR)$(JAXP)
CLASSPATH = $(ROOT)

.SUFFIXES: .class .java .h .jj .jjt


# The default compile rule
.java.class:
	rm -f `basename $< .java`.class
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)$(JAVASCOPECLASSPATH)$(CLASSPATHSEPARATOR)$(ESPAM_SRC)" $(JAVAC) $(JFLAGS) $<

# The default compile rule
.java.h:
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" $(JAVAH) -classpath $(CLASSPATH) $<

.jjt.jj:
	CLASSPATH="$(CLASSPATH)" $(JJTREE) $<

.jj.java:
	CLASSPATH="$(CLASSPATH)" $(JAVACC) $<
	perl -pi -e 's/\benum\b/e/g' $@

# The default rule is "package."
default: package

# Compiling classes compiles just this directory
classes: $(JCLASS)

# Compile subpackages
package:
	@if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) package ;\
			) \
		    fi ; \
		done ; \
	fi
	make classes

# Compile subpackages
fast:
	@if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) fast ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ "x$(JSRCS)" != "x" ]; then \
		echo "fast build with 'CLASSPATH=\"$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)\" $(JAVAC) $(JFLAGS) *.java' in `pwd`"; \
		CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" $(JAVAC) $(JFLAGS) *.java; \
	fi

# In jtest we need to set the LD_LIBRARY_PATH or PATH to make sure
# the shared library can be loaded 
jtest:
	@if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jtest ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ -w test ]; then \
		( cd ./test ; \
		echo "Make tests using TCL in `pwd`"; \
		echo "Make sure Shared Library can be accessed"; \
		echo "Using LD_LIBRARY_PATH (UNIX) or PATH (WINDOWS)"; \
		make tests; ) \
	fi	


# Compile this directories and all sub-directories.
# If SUBDIRS is not set, then add test to SUBPACKAGES and use that.
all: classes
	@if [ "x$(SUBDIRS)" != "x" ]; then \
		set $(SUBDIRS) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) package ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES); \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) package ;\
			) \
		    fi ; \
		done ; \
	fi

# The install rule is the same as package
install: package

# Remove classes from this directory then subdirectories
clean:
	rm -f *.class core patch.exe.core 
	rm -f #*# *.*~ *~ *.html *.css 
	rm -f tmpfile junk* *.o TAGS
	@if [ "x$(SUBDIRS)" != "x" ]; then \
		set $(SUBDIRS) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) clean ;\
			) \
		    fi ; \
		done ; \
	fi ; \
	if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) clean ;\
			) \
		    fi ; \
		done ; \
	fi

tags:
	if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) tags ;\
			) \
		    fi ; \
		done ; \
	fi
	make tag

tag:
	if [ "x$(JSRCS)" != "x" ]; then \
		etags $(JSRCS) --append --output=$(ROOT)/TAGS ;\
	fi


doc:
	if [ "x$(JSRCS)" != "x" ]; then \
	if [ ! -d $(MP)/doc/codeDoc ]; then mkdir -p $(MP)/doc/codeDoc; fi; \
		javadoc -d $(MP)/doc/codeDoc -private -classpath $(CLASSPATH) -author -version $(JSRCS) ;\
        for x in doc/codeDoc/*.html; do \
                echo "Fixing paths in $(ME)/$$x"; \
                sed -e 's|<a href="java|<a href="$(MP)/doc/codeDoc/java|g' \
                -e 's|<img src="images/|<img src="$(MP)/doc/codeDoc/images/|g' \
                        $$x > $$x.bak; \
                mv $$x.bak $$x; \
        done; \
	fi

# alljtests.tcl is used to source all the tcl files that use Java
alljtests.tcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in ptcommon.mk' > $@
	echo '# This file will source all the Tcl files that use Java. ' >> $@ 
	echo '# This file will source the tcl files list in the' >> $@
	echo '# makefile SIMPLE_JTESTS and GRAPHICAL_JTESTS variables' >> $@ 
	echo '# This file is different from all.itcl in that all.itcl' >> $@ 
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# Exiting when there are no more windows is wrong' >> $@
	echo "#::tycho::TopLevel::exitWhenNoMoreWindows 0" >> $@
	echo '# If there is no update command, define a dummy proc.  Jacl needs this' >> $@
	echo 'if {[info command update] == ""} then { ' >> $@
	echo '    proc update {} {}' >> $@
	echo '}' >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(JSIMPLE_TESTS)\" != \"\"} {foreach i [list $(JSIMPLE_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] {source \$$i}}}" >> $@
	if [ "x$(JGRAPHICAL_TESTS)" != "x" ]; then \
		for x in $(JGRAPHICAL_TESTS); do \
			echo "puts stderr $$x" >> $@; \
			echo "cd \"\$$savedir\"" >> $@; \
			echo "if [ file exists $$x ] {source $$x}" >> $@; \
		done; \
	fi
	echo "catch {doneTests}" >> $@
	echo "exit" >> $@


# Instrument Java code for use with JavaScope.
# If necessary, instrument the classes, then rebuild, then run the tests
# Instrument Java code for use with JavaScope.
jsall:
	if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jsall ;\
			) \
		    fi ; \
		done ; \
	fi
	make jsinstr

# If the jsoriginal directory does not exist, then instrument the Java files.
jsoriginal:
	@if [ ! -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)"; \
		$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS); \
	fi

#instrument the classes
jsinstr:
	if [ ! -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)"; \
		$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS); \
	fi


# Back out the instrumentation.
jsrestore:
	@if [ "x$(SUBPACKAGES)" != "x" ]; then \
		set $(SUBPACKAGES) ; \
		for x do \
		    if [ -r $$x ] ; then \
			( cd $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jsrestore ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "Running jsrestore in `pwd`"; \
		$(JSRESTORE) $(JSRCS); \
		rm -f jsoriginal/README; \
		rmdir jsoriginal; \
		$(MAKE) clean; \
	else \
		echo "no jsoriginal directory, or no java sources"; \
	fi

# Compile the instrumented Java classes and include JavaScope.zip
jsbuild:
	$(MAKE) JAVASCOPECLASSPATH="$(JSCLASSPATH)" package

jstest:
	if [ -w test ] ; then \
	   (cd test; $(MAKE) jstest_jsimple); \
	fi


# Run the test_jsimple rule with the proper classpath  
jstest_jsimple:
	$(MAKE) JAVASCOPECLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		test_jsimple
	@echo "To view code coverage results, run javascope or jsreport"
	@echo "To get a summary, run jsreport or jsreport -HTML or" 
	@echo "jssummary -HTML -PROGRESS -OUTFILE=\$$HOME/public_html/private/js/coverage.html"
	@echo "jsreport -HTML -PROGRESS -RECURSIVE -OUTDIR=\$$HOME/public_html/private/js"

# Run the test_jgraphical rule with the proper classpath  
jstest_jgraphical:
	$(MAKE) JAVASCOPECLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		test_jgraphical
	@echo "To view code coverage results, run javascope or jsreport"
	@echo "To get a summary, run jssummary or jssummary -HTML" 
	@echo "Note that output sometimes ends up in ~/jsreport"

# open up an interactive TCL shell
shell:
	$(JAVA) -classpath "${CLASSPATH}$(CLASSPATHSEPARATOR)$(ROOT)/lib/ptjacl.jar" tcl.lang.Shell

# Build the jar file

# Directory to unjar things in.
# Be very careful here, we rely on relative paths
PTJAR_TMPDIR =  ptjar_tmpdir

# OTHER_FILES_TO_BE_JARED is used in ptolemy/vergil/lib/makefile
# We need to use PTJAR_TMPDIR because not all directories
# have OTHER_FILES_TO_BE_JARED set, so we need to copy
# rather than refer to $(ME)/$(OTHER_FILES_TO_BE_JARED)
jars: $(PTCLASSJAR) $(PTAUXALLJAR) \
		$(OTHER_FILES_TO_BE_JARED) $(OTHER_JARS)
$(PTCLASSJAR): $(JCLASS)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp *.class $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	echo "Creating $@"
	( cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar . )
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

junit:
	$(JAVA) -classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/lib/junit.jar" junit.swingui.TestRunner junit.AllTests

jtext:
	$(JAVA) -classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/lib/junit.jar" junit.AllTests

