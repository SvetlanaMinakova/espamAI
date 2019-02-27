ESPAMAI: Embedded System-level Platform synthesis and Application Mapping Tool, extended with tools for Artificial Intelligence
================================================================================================================================
Installation instructions for ESPAMAI

I. Installing Java and Python
--------------------------------------------------------------------------------------------------------------------------------- 

Before installing ESPAMAI, you need Java SDK 8.0+ and Python 2.7. If you do not have these tools installed:

#### Installing Java SDK:

- download JAVA JDK (J2SE Development Kit) version 8.0 or later from
http://java.sun.com/javase/downloads/index.jsp
- follow the installation instructions on the web-page

#### Installing Python 2.7

- select latest 2.7 python release from https://www.python.org/downloads/
- follow the installation instructions on the release web-page

II. Installing ESPAMAI 
---------------------------------------------------------------------------------------------------------------------------------
-- There are several options listed below. Select one of them:

#### OPTION 1  

The procedure below describes how to install EspamAI with 
pre-build EspamAI related libraries. 

- download ESPAMAI from the GIT repo as follows ->

git clone https://gitlab.com/aloha.eu/power_perf_sources.git

cd ./espam 

NOTE: The directory "./lib/" should contain at least the following 
pre-build libraries:

	libpandapolylib.so  
	onnx.jar  
	gson.jar  

- configure and install EspamAI as follows ->

autoconf  
./configure --prefix=`pwd`
            --with-java=[path where Java is installed](If path not specified, the common paths to java/javac will be checked automatically) 
            --with-python=[path to executable python](If path not specified, the common paths to python will be checked automatically) 
            --with-systemc=[path where SystemC is installed] (if you want to generate and run SystemC simulations)
            --with-darts=[path to DARTS] (if you want use espamAI options)
	
make

NOTE:If ESPAMAI does not work after installation, 
the reason might be that the pre-build EspamAI related libraries mentioned above
are very old or incompatible. In such case try to install ESPAMAI with OPTION 2 below. 

#### OPTION 2

The procedure below describes how to build the libraries used by ESPAMAI
and how to install ESPAMAI.

a) First, you need to install the GMP multi-precision library and Polylib library. See below:

a.1) Installing GMP library:
- download the GMP library version 4.3.1 from http://gmplib.org/  
- configure and install GMP as follows ->  

./configure --prefix=[path where GMP is to be installed]
make  
make check  
make install  

- for more information see the documentation available on the
web-page;

a.2) Installing Polylib library:
- download Polylib version 5.22.4 from  
http://icps.u-strasbg.fr/polylib/  
- configure and install Polylib as follows ->

./configure --prefix=[path where Polylib is to be installed]
            --enable-longlongint-lib
            --with-libgmp=[path where GMP is installed]
make  
make check  
make install  

NOTE: The "make install" works correctly only if the directory where
you build PolyLib (where you run the "./configure" script) is
different than the directory where you install PolyLib (the directory
in the "--prefix" option). So, do not use --prefix=`pwd`.

b)Second, you might need to build onnx.jar. See below:

b.1) Get compiled Google protocol buffers library.
You can download the library from https://mvnrepository.com/artifact/com.google.protobuf/
The downloaded jar should be renamed to "protobuf-java.jar" and placed to .../espam/lib

b.2) Unzip protobuf-java.jar into src_lib/onnx:
	protobuf-java.jar -d /path-to-espam/espam/lib
	
b.3) Get ONNX.java
The ONNX.java can be extracted from default onnx.jar or generated from onnx.in protocol buffers file of ONNX oficial repo: 
https://github.com/onnx/onnx/tree/master/onnx and place it to src_lib/onnx/com/onnx.in
To generate ONNX.java from onnx.in follow the instructions on the web page 
https://developers.google.com/protocol-buffers/docs/javatutorial
The extracted/generated ONNX.java should be placed in .../espam/src_lib/onnx/com/onnx/

b.4) As soon as you have compiled protocol buffer classes from b.1 in .../espam/src_lib/protobuf-java
and compiled ONNX.java in .../espam/src_lib/onnx/com/onnx/  you can build onnx.jar:
	cd src_lib/onnx
	make jar

c)Third, you might need to download gson from https://mvnrepository.com/artifact/com.google.code.gson/gson
The downloaded librarty should be renamed to "gson.jar" and moved to .../espam/lib

d) Install Espam and build the necessary libraries:

d.1) _First_ install the Espam tool

- download ESPAM from the GIT repo as follows ->

git clone https://gitlab.com/aloha.eu/power_perf_sources.git

- configure and install Espam as follows ->

cd .../espam/  
./configure --prefix=`pwd`  
            --with-java=[path where Java is installed](If path not specified, the common paths to java/javac will be checked automatically) 
            --with-python=[path to executable python](If path not specified, the common paths to python will be checked automatically) 
            --with-systemc=[path where SystemC is installed] (if you want to generate and run SystemC simulations)
            --with-darts=[path to DARTS] (if you want use espamAI options)
make

c.2) Build the Espam related libraries

- configure and build the libraries as follows ->

cd .../espam/src_lib
./configure --prefix=`pwd`
            --with-java=[path where Java is installed]
            --with-espam=[path where Espam is installed]
            --with-gmp=[path where GMP is installed]
            --with-polylib=[path where Polylib is installed]
            --with-darts=[path to DARTS]
make

- copy manually the library files from ".../espam/src_lib/lib" to the directory ".../espam/lib"

cp .../espam/src_lib/lib/*.so  .../espam/lib.  
chmod 755 .../espam/lib/*.so  

cp .../espam/src_lib/lib/onnx.jar  .../espam/lib.  
chmod 755 .../espam/lib/onnx.jar  

Alternatively, you can modify LD_LIBRARY_PATH to point to the required libraries (polylib/gmp/onnx):  
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/path/to/polylib/library:/path/to/gmp/library:/path/to/onnx.jar

III. Running ESPAMAI
---------------------------------------------------------------------------------------------------------------------------------
Now you can run ESPAM by running the executable script ".../espam/bin/espam"

