ESPAMAI: Embedded System-level Platform synthesis and Application Mapping Tool, extended with tools for Artificial Intelligence
==============================================================================================================================

This directory contains the source code of libraries, required both for original ESPAM and ESPAMAI

Compilation Instructions for polylib
-------------------------------------
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

b) Install Espam and build the necessary libraries:

b.1) _First_ install the Espam tool

- download ESPAM from the GIT repo as follows ->

git clone csartem@ssh.liacs.nl:espam.git

- configure and install Espam as follows ->

cd .../espam/
./configure --prefix=`pwd`
            --with-java=[path where Java is installed]	
            --with-systemc=[path where SystemC is installed] (if you want to generate and run SystemC simulations)
make

b.2) Build the Espam related libraries

- configure and build the libraries as follows ->

cd .../espam/src_lib
./configure --prefix=`pwd`
            --with-java=[path where Java is installed]
            --with-espam=[path where Espam is installed]
            --with-gmp=[path where GMP is installed]
            --with-polylib=[path where Polylib is installed]
make

- copy manually the library files from ".../espam/src_lib/lib" to the directory ".../espam/lib"

cp .../espam/src_lib/lib/*.so  .../espam/lib/ 
chmod 755 .../espam/lib/*.so

Now, you need to modify LD_LIBRARY_PATH to point to the polylib and gmp libraries:
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/path/to/polylib/library:/path/to/gmp/library

III. You can run ESPAM by running the executable script ".../espam/bin/espam"

Compilation Instructions for onnx.jar 
-------------------------------------
onnx.jar contains java classes:
1. From Core Protocol Buffers library: https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
	The 3.5.1 version of protocol buffers is provided with espamAI by default. 
	The archive with the protocol buffer classes should be unzipped and placed to src_lib/onnx. 
	The directory, generated after protobuf-java<version>.jar archive is unzipped should be named 'protobuf-java'.
	
2. Java ONNX.java and corresponding ONNX.class, generated from src_lib/onnx/com/onnx.in 
The ONNX.java generated from proto2 IR VERSION 0.0.3 is provided with espamAI by default. 
To get a new version of onnx.in visit ONNX oficial repo https://github.com/onnx/onnx/tree/master/onnx and place it to src_lib/onnx/com/
Then gerenate ONNX.java using https://developers.google.com/protocol-buffers/docs/javatutorial 

After you have protobuf-java directory with Core Protocol Buffers classes and ONNX.java

1. cd src_lib/onnx
2. make jar
3. take onnx.java file and put it to your libraries directory (espam/lib by default)


