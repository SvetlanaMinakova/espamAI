ESPAM: Embedded System-level Platform synthesis and Application Mapping Tool
============================================================================

This directory contains the ESPAM tool.


Compilation Instructions
------------------------
To build ESPAM, you need to execute the following two commands:

	./configure --prefix=`pwd` [--with-java=$JAVA_PATH --with-systemc=$SYSTEMC_HOME]
	make

The prefix is set to the espam directory.
Optionally, you can pass `$JAVA_PATH` which is the full path to your Java SDK installation.
If this option is not given, ESPAM will look for the JDK that corresponds to the javac
found in /usr/bin/javac. Additionally, you can pass `$SYSTEMC_HOME` which is full path to 
the SystemC installation on your system.If this option is not given, ESPAM assumes 
SystemC is installed in `$HOME/systemc-2.2.0`.


Running ESPAM
-------------
After building ESPAM successfully, you can invoke it by running:

	./bin/espam


License
-------
This tool is released under the license shown in LICENSE


Bugs/Questions
--------------
In case of bugs/questions, please contact [Todor Stefanov](mailto:stefanov@liacs.nl)
