/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.ymlPN;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.graph.adg.ADGParameter;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDOutGate;

import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGNode;

import espam.main.UserInterface;
import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
////YmlNetworkVisitor

/**
 *  This class ...
 *
 * @author  Hristo Nikolov, Todor Stefanov
 * @version  $Id: YapiNetworkVisitor.java,v 1.3 2002/06/12 18:28:59 sjain
 *      Exp $
 */

public class YmlNetworkVisitor extends CDPNVisitor {

    /**
     *  Constructor for the YapiNetworkVisitor object
     *
     * @param  printStream Description of the Parameter
     */
    public YmlNetworkVisitor( PrintStream printStream ) {
        _printStream = printStream;
	_prefix = "   ";
	_cntr = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {

        _pn = x;
        _printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _printStream.println("<network xmlns=\"http://sesamesim.sourceforge.net/YML\" name=\"application\" class=\"KPN\">");
        _printStream.println("");
	_printStream.println(_prefix + "<property name=\"library\" value=\"lib" + _pn.getName() + ".so\"/>");
        _printStream.println("");

        CDProcess process;
        CDChannel channel;

	// Visit Processes
        Iterator i = x.getProcessList().iterator();
        while( i.hasNext() ) {
            process = (CDProcess) i.next();
            process.accept(this);
        }

	// Visit Links
        i = x.getChannelList().iterator();
        while( i.hasNext() ) {
            channel = (CDChannel) i.next();
            channel.accept(this);
        }

        _prefixDec();

	_printStream.println(_prefix + "<property name=\"op_EndLoop\" value=\""+ _cntr +"\" />" );
        _printStream.println("</network>");

        YmlProcessVisitor pt = new YmlProcessVisitor();
        x.accept( pt );

        _writeMakeFile();
        _writeSourceFile();
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcess x ) {

        String name = ((ADGNode)x.getAdgNodeList().get(0)).getName();
        _printStream.println(_prefix + "<node name=\"" + name + "\" class=\"CPP_Process\""  + ">");
        _prefixInc();
        _printStream.println(_prefix + "<property name=\"class\" value=\"" + name + "\"/>");
	_printStream.println(_prefix + "<property name=\"pos\" value=\"\"/>");
	_printStream.println(_prefix + "<property name=\"header\" value=\"" + "aux_func.h\"/>");
	_printStream.println(_prefix + "<property name=\"header\" value=\"" + name + ".h\"/>");
	_printStream.println(_prefix + "<property name=\"source\" value=\"" + name + ".cpp\"/>");
	_printStream.println("");

	//visit the list of ports of this CDProcess
	String dir = "";
	Vector gateList = (Vector) x.getGateList();
	if( gateList != null ) {
           Iterator i = gateList.iterator();
	   while( i.hasNext() ) {
	      CDGate gate = (CDGate) i.next();
	      if( gate instanceof CDInGate ) {
                 dir = "in";
              } else if( gate instanceof CDOutGate ) {
                 dir = "out";
	      }

              ADGPort port = (ADGPort) gate.getAdgPortList().get(0);
	      String type = ((ADGEdge) port.getEdge()).getFromPort().getBindVariables().get(0).getDataType();

	      String t = "char";
              if (type != null) {
		  if (!type.equals("")) {
		          t = type;
		 }
             }

	      _printStream.println(_prefix + "<port name=\"" + gate.getName() + "\" dir=\"" + dir + "\">");
	      _prefixInc();
	      _printStream.println(_prefix + "<property name=\"type\" value=\"" + t + "\"/>");
	      _printStream.println(_prefix + "<property name=\"pos\" value=\"\"/>");
	      _prefixDec();
	      _printStream.println(_prefix + "</port>");
	   }
	}

	_prefixDec();
	_printStream.println(_prefix + "</node>");

	String operation = ((ADGNode)x.getAdgNodeList().get(0)).getFunction().getName();
	_printStream.println(_prefix + "<property name=\"operation:op_" + operation + "\" value=\"" + _cntr++ + "\" />" );

	_printStream.println("");
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDChannel x ) {

       CDOutGate fromGate = x.getFromGate();
       String nameFromGate = fromGate.getName();
       String nameFromProcess = ((ADGNode) ((CDProcess) fromGate.getProcess()).getAdgNodeList().get(0)).getName();

       CDInGate toGate = x.getToGate();
       String nameToGate = toGate.getName();
       String nameToProcess = ((ADGNode) ((CDProcess) toGate.getProcess()).getAdgNodeList().get(0)).getName();

       _printStream.println(_prefix + "<link innode=\""    + nameFromProcess + "\" " +
	   			            "inport=\""    + nameFromGate    + "\" " +
                                            "outnode=\""   + nameToProcess   + "\" " +
				            "outport=\""   + nameToGate      + "\" " + ">");

       _prefixInc();
       _printStream.println(_prefix + "<property name=\"pos\" value=\"\"/>");
       _prefixDec();
       _printStream.println(_prefix + "</link>");
       _printStream.println("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     */
    private void _writeMakeFile() {
        try {
            // create the makefile
            PrintStream mf = _openMakefileFile();

            mf.println("#@Author: ESPAM");
            mf.println("");
	    mf.println("include ./sources");
            mf.println("");
            mf.println("NAME=" + _pn.getName() );
            mf.println("");
	    mf.println("YML=$(NAME)_app.yml");
	    mf.println("APPVIRTUAL_MAP=../$(NAME)_appvirt_map.yml");
            mf.println("");
	    mf.println("APP_LIB=lib$(NAME).so");
	    mf.println("STUB=$(NAME)_stub");
            mf.println("");
	    mf.println("NODESRCS := $(shell PNRunnerYMLTool --sources $(YML))");
	    mf.println("CLASSES := $(shell PNRunnerYMLTool --classes $(YML))");
	    mf.println("BASES := $(patsubst %,%_Base.h,$(CLASSES))");
	    mf.println("SOURCES := $(STUB).cpp $(NODESRCS)");
	    mf.println("OBJS := $(patsubst %.cpp,%.o,$(SOURCES)) $(OBJ_FILES)");
            mf.println("");
	    mf.println("CXX=g++");
	    mf.println("CFLAGS += `sesamesim-config --PNRunner-cflags` -Wall");
	    mf.println("LIBS += `sesamesim-config --PNRunner-libs`");
	    mf.println("APPFLAGS := $(APPFLAGS) -L . -l $(APP_LIB)");
            mf.println("");
	    mf.println("all: build");
            mf.println("");
	    mf.println("build: base_classes $(APP_LIB)");
            mf.println("");
	    mf.println("base_classes: $(YML)");
	    mf.println("\tfor i in $(CLASSES); do\\");
	    mf.println("\t  PNRunnerYMLTool --create-base-class $(YML) $$i >$${i}_Base.tmp;\\");
            mf.println("\t  diff $${i}_Base.h $${i}_Base.tmp 2>/dev/null >/dev/null;\\");
	    mf.println("\t  if [ $$? -ne 0 ]; then mv $${i}_Base.tmp $${i}_Base.h;\\");
	    mf.println("\t  else rm -f $${i}_Base.tmp; fi;\\");
	    mf.println("\tdone");
            mf.println("");
	    mf.println("$(APP_LIB): $(OBJS)");
	    mf.println("\t$(CXX) $(CFLAGS) $(OBJS) $(LIBS) -o $@");
            mf.println("");
	    mf.println("$(STUB).cpp: $(YML)");
	    mf.println("\tPNRunnerYMLTool --create-stubs $(YML) >$@.tmp");
	    mf.println("\tdiff $@ $@.tmp 2>/dev/null >/dev/null;\\");
	    mf.println("\tif [ $$? -ne 0 ]; then mv $@.tmp $@;\\");
	    mf.println("\telse rm -f $@.tmp; fi");
            mf.println("");
	    mf.println("%.o: %.cpp %.h %_Base.h");
	    mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
	    mf.println("%.o: %.cpp %.h");
	    mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
	    mf.println("%.o: %.cpp");
	    mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
	    mf.println("run: build");
	    mf.println("\tPNRunner $(APPFLAGS) $(YML)");
            mf.println("");
	    mf.println("runmap: build");
	    mf.println("\tPNRunner $(APPFLAGS) -m $(APPVIRTUAL_MAP) $(YML)");
            mf.println("");
	    mf.println("runtrace: build");
	    mf.println("\tPNRunner $(APPFLAGS) -T ../trace -m $(APPVIRTUAL_MAP) $(YML)");
            mf.println("");
	    mf.println("clean:");
	    mf.println("\trm -rf *.o *~ \\#* *.jpg core*\\");
	    mf.println("\t$(APP_LIB) $(STUB).cpp $(BASES)");
            mf.println("");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the default makefile");
            System.out.println("please supply your own makefile");
        }
    }

    /**
     */
    private void _writeSourceFile() {
        try {
            // create the makefile
            PrintStream sf = _openSourceFile();

            //System.out.print("Generating config file .........");

            sf.println("#source file for YML Process Networks");
            sf.println("#@Author: ESPAM");
            sf.println(" ");
            sf.println("OBJ_FILES = ");

            //System.out.println("[Done]");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannt create the default source file");
            System.out.println("please supply your own config file");
        }
    }

    /**
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openMakefileFile()
             throws FileNotFoundException {

        PrintStream printStream = null;
        UserInterface ui = UserInterface.getInstance();
        String fullFileName = "";
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename. (REFACTOR)
        if( ui.getOutputFileName() == "" ) {
            fullFileName =
                    ui.getBasePath() + "/" +
                    ui.getFileName() + "/app/Makefile";
        } else {
            fullFileName =
                    ui.getBasePath() + "/" +
                    ui.getOutputFileName() + "/app/Makefile";
        }
        OutputStream file = null;

	System.out.println(" -- OPEN FILE: " + fullFileName);
        file = new FileOutputStream( fullFileName );
        printStream = new PrintStream( file );
        return printStream;
    }

    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openSourceFile()
             throws FileNotFoundException {

        PrintStream printStream = null;
        UserInterface ui = UserInterface.getInstance();
        String fullFileName = "";
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename. (REFACTOR)
        if( ui.getOutputFileName() == "" ) {
            fullFileName =
                    ui.getBasePath() + "/" +
                    ui.getFileName() + "/app/sources";
        } else {
            fullFileName =
                    ui.getBasePath() + "/" +
                    ui.getOutputFileName() + "/app/sources";
        }
        OutputStream file = null;

        System.out.println(" -- OPEN FILE: " + fullFileName);
        file = new FileOutputStream( fullFileName );
        printStream = new PrintStream( file );
        return printStream;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     */
    private CDProcessNetwork _pn = null;

    private int _cntr = 0;
}



