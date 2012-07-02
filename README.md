ESPAM: Embedded System-level Platform synthesis and Application Mapping Tool
============================================================================

This directory contains the ESPAM tool.


Compilation Instructions
------------------------
To build ESPAM, you need to execute the following two commands:

	./configure --prefix=`pwd` --with-java=$JAVA_PATH
	make

where `$JAVA_PATH` is the full path to your Java SDK installation. 
On Ubuntu machines, SDK installations are usually located under: `/usr/lib/jvm/`


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
