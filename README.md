ESPAMAI: Embedded System-level Platform synthesis and Application Mapping Tool, extended with tools for Artificial Intelligence
==============================================================================================================================

This directory contains the original ESPAM tool and tools, designed for for Artificial Intelligence applications processing


Compilation Instructions
------------------------
To build ESPAMAI, you need to execute the following two commands:

	./configure --prefix=`pwd` [--with-java=$JAVA_PATH --with-python=$PYTHONPATH --with-systemc=$SYSTEMC_HOME --with-darts=$DARTS_HOME ]
	make

The prefix is set to the espam directory.
Optionally, you can pass
1. `$JAVA_PATH` which is the full path to your Java SDK installation.
If this option is not given, ESPAMAI will look for the JDK that corresponds to the javac
found in /usr/bin/javac. 
2. `$PYTHONPATH` which is the full path to python installation.
If this option is not given, ESPAMAI will look for the current python installation, using which python command.
3. `$SYSTEMC_HOME` which is full path to 
the SystemC installation on your system.If this option is not given, ESPAM assumes 
SystemC is installed in `$HOME/systemc-2.2.0`.

4. `$DARTS_HOME` which is full path to 
the DARTS (http://daedalus.liacs.nl/daedalus-rt.html) tool
installation on your system.If this option is not given, ESPAM assumes 
SystemC is installed in `$HOME/darts-4.0`.

Running ESPAM and ESPAMAI
-------------
After building ESPAMAI successfully, you can invoke it by running:

	./bin/espam

the console interface is common for original ESPAM and ESPAM AI tools

License
-------
This tool is released under the license shown in LICENSE


Bugs/Questions
--------------
In case of bugs/questions, please contact the ESPAMAI team (mailto:csartem@liacs.nl) with
a short decription. The decription should include:

        1. Output by the framework and expected ouput
        2. C code for application specification [if application generated]
        3. Corresponding platform and mapping specification [if mapping is used]


