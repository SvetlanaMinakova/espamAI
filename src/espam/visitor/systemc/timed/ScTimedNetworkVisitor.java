/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2010 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.systemc.timed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.main.UserInterface;
import espam.visitor.CDPNVisitor;

import espam.datamodel.EspamException;


import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.AssignStatement;
//////////////////////////////////////////////////////////////////////////
////ScTimedNetworkVisitor

/**
 * This class generates a timed SystemC model from a CDPN. It is based on the YAPI
 * visitor.
 *
 * @author  Hristo Nikolov, Todor Stefanov, Sven van Haastregt, Teddy Zhai
 * @version  $Id: ScTimedNetworkVisitor.java,v 1.3 2011/03/04 09:48:25 tzhai Exp $
 */

public class ScTimedNetworkVisitor extends CDPNVisitor {

    /**
     *  Constructor for the SystemCNetworkVisitor object
     *
     * @param  printStream Description of the Parameter
     */
    //public ScTimedNetworkVisitor(Mapping mapping, PrintStream printStream) {
    public ScTimedNetworkVisitor(Mapping mapping) throws EspamException {
      _mapping = mapping;

      // Use the directory indicated by the '-o' option. Otherwise
      // select the orignal filename. (REFACTOR)
      UserInterface ui = UserInterface.getInstance();
      if( ui.getOutputFileName() == "" ) {
        _outputDir = ui.getBasePath() + "/" + ui.getFileName();
      }
      else {
        _outputDir = ui.getBasePath() + "/" + ui.getOutputFileName();
      }
      File dir = new File(_outputDir);
      if( !dir.exists() ) {
        if( !dir.mkdirs() ) {
          throw new EspamException("could not create directory '" + dir.getPath() + "'.");
        }
      }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {
        _pn = x;

        ScTimedProcessVisitor pt = new ScTimedProcessVisitor(_mapping);
        x.accept( pt );

        _writeMakeFile();
        _writeConfigFile();
        _writeMainFile();
        
        _writeWorkloadHeader();
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDChannel x ) {

       //_printStream.println(_prefix + "fsl<t"+x.getName()+"> " + x.getName() + ";");
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcess x ) {

       //_printStream.println(_prefix + x.getName() + " i" + x.getName() + ";");
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _printNetwork(PrintStream ps) {
      String fifoConnects = "";
      ps.println(_prefix + "// Channels");

      Iterator i = _pn.getChannelList().iterator();
      while( i.hasNext() ) {
        CDChannel channel = (CDChannel) i.next();
        if (channel.getAdgEdgeList().size() != 1) {
          System.err.println("Warning: multiple ADG edges per channel not supported!");
        }
//         int chSize = ((ADGEdge)channel.getAdgEdgeList().get(0)).getSize();
	// quick hack: first assume that buffer size does not play roll in performance
	int chSize = 10000;
        ps.println(_prefix + "fsl<t"+channel.getName()+"> " + channel.getName() + "(\"" + channel.getName() + "\", " + chSize + ", tf);");
        fifoConnects += _prefix + channel.getName() + ".clk(sysClk);\n";
      }

      ps.println("");
      ps.println(fifoConnects);
      ps.println("");
      ps.println(_prefix + "// Processes");

      i = _pn.getProcessList().iterator();
      while( i.hasNext() ) {
        CDProcess process = (CDProcess) i.next();
        ps.println(_prefix + process.getName() + " i" + process.getName() + "(\"" + process.getName() + "\", tf);");
        ps.println(_prefix + "i" + process.getName() + ".clk(sysClk);");

        // Connect inputs
        Iterator g = process.getInGates().iterator();
        while (g.hasNext()) {
          CDGate gate = (CDGate) g.next();
          ps.println(_prefix + "i" + process.getName() + "." + gate.getName() + "(" + gate.getChannel().getName() + ");");
          
        }

        // Connect outputs
        g = process.getOutGates().iterator();
        while (g.hasNext()) {
          CDGate gate = (CDGate) g.next();
          ps.println(_prefix + "i" + process.getName() + "." + gate.getName() + "(" + gate.getChannel().getName() + ");");
          
        }
        ps.println("");
      }

      ps.println("");
    }


    /**
     * Prints the actual network components.
     */
    private void _printConstructor(PrintStream ps) {
      ps.println(_prefix + "// Constructor");
      ps.println(_prefix + _pn.getName() + "(sc_clock &clk) {");
      _prefixInc();

      // Connect FIFOs
      Iterator i = _pn.getChannelList().iterator();
      while (i.hasNext()) {
        CDChannel channel = (CDChannel) i.next();
        ps.println(_prefix + channel.getName() + ".clk(clk)");
      }

      ps.println("");

      // Connect Processes
      i = _pn.getProcessList().iterator();
      while (i.hasNext()) {
        CDProcess process = (CDProcess) i.next();
        ps.println(_prefix + "i" + process.getName() + ".clk(clk)");
      }
      _prefixDec();
      ps.println(_prefix + "}");
    }


    /**
     */
    private void _writeConfigFile() {
        try {
            String configFilename = _outputDir + "/config.mk";
            File f = new File(configFilename);
            if (f.exists() == false) {
                // Only write the file if it did not exist (so existing config is kept).
                PrintStream cf = _openFile(configFilename);

                cf.println("# Makefile config for SystemC Process Networks");
                cf.println("");
                cf.println("CC = gcc");
                cf.println("CXX = g++");
                cf.println("SYS_LIBS =");
                cf.println("SYSTEMC = $(HOME)/TOOLS/systemc-2.2.0");
            }
            else {
                System.out.println(" -- Preserving " + configFilename);
            }

        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Can't create the default config file");
        }
    }



    /**
     */
    private void _writeMainFile() {
        try {
            PrintStream maf = _openFile(_outputDir + "/main.cc");

            maf.println("//main.cc file for SystemC Process Networks");
            maf.println("//Automatically generated by ESPAM");
            maf.println("");
            maf.println("#include <fstream>");
            maf.println("#include \"workload.h\"");
            maf.println("#include \"fifo_fsl.h\"");
            Iterator i = _pn.getProcessList().iterator();
            while( i.hasNext() ) {
                CDProcess process = (CDProcess) i.next();
                maf.println("#include \"" + process.getName() + ".h\"");
            }
            maf.println("");
            maf.println("using namespace std;");
            maf.println("");
            maf.println("int sc_main(int argc , char *argv[]) {");
            _prefixInc();
            maf.println(_prefix + "sc_set_time_resolution(0.1, SC_NS);");
            maf.println(_prefix + "sc_time start_time    (0,   SC_NS);");
            maf.println(_prefix + "sc_time period        (1,   SC_NS);");
            maf.println(_prefix + "sc_clock sysClk(\"Oscillator\", period, 0.5, start_time, true);");
            maf.println(_prefix + "sc_trace_file *tf = sc_create_vcd_trace_file(\"dump\");");
            maf.println(_prefix + "sc_trace(tf, sysClk, \"Clock\");");
            maf.println("");

            _printNetwork(maf);

            maf.println(_prefix + "sc_start(6000, SC_NS);");
            maf.println(_prefix + "cerr << \"Warning: hardcoded simulation timeout - process network may not have finished yet.\" << endl;");
            maf.println("");
            maf.println(_prefix + "sc_close_vcd_trace_file(tf);");
            maf.println("");
            maf.println(_prefix + "return 0;");
            _prefixDec();
            maf.println("}");

        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Can't create the default main.cc file");
        }
    }

    /**
     */
    private void _writeMakeFile() {
        try {
            // create the makefile
            PrintStream mf = _openFile(_outputDir + "/Makefile");

            mf.println("include config.mk\n");
            mf.println("SRC_DIR=.");
            mf.println("INC_DIR=.");
            mf.println("OBJ_DIR=.");
            mf.println("BIN_DIR=.\n");
            mf.println("EXEC= $(BIN_DIR)/sim\n");
            mf.println("COMP_FLAGS= -Wall -c -g -I$(INC_DIR) -I$(SYSTEMC)/include");
            mf.println("BUILD_FLAGS= -g -L$(SYSTEMC)/lib-linux");
            mf.println("DEFINES= -DPLATFORM_X86\n");
            mf.println("HEADER= $(wildcard $(INC_DIR)/*.h)");
            mf.println("SRC=    $(wildcard $(SRC_DIR)/*.cc)"); 
            mf.println("OBJ=    $(SRC:$(SRC_DIR)/%.cc=$(OBJ_DIR)/%.o) ");
            mf.println("LIBS=-lsystemc\n");
            mf.println("default: $(EXEC)\n");
            mf.println("$(EXEC): $(OBJ) Makefile");
            mf.println("\t$(CXX) $(BUILD_FLAGS) -o $@  $(OBJ) $(LIBS)");
            mf.println("");
            mf.println("$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cc Makefile *.h");
            mf.println("\t$(CXX) -o $@ $(COMP_FLAGS) $(DEFINES) $<\n");
            mf.println("run:");
            mf.println("\t${EXEC}\n");
            mf.println("clean:");
            mf.println("\trm -f $(OBJ_DIR)/*.o $(EXEC) ");

        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the default makefile");
        }
    }



    /**
     * Open a file for writing.
     *
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openFile(String fullFileName)
             throws FileNotFoundException {

        PrintStream printStream = null;
        OutputStream file = null;
        System.out.println(" -- OPEN FILE: " + fullFileName);
        file = new FileOutputStream( fullFileName );
        printStream = new PrintStream( file );
        return printStream;
    }
    
      
    private void _getFunctionNames(ParserNode p) {
      if (p instanceof AssignStatement) {
        AssignStatement s = (AssignStatement) p;
//         System.out.println("debug: " + s.getFunctionName());
        if (_functionNames.contains(s.getFunctionName()) == false) {
          _functionNames.add(s.getFunctionName());
        }
      }
      Iterator i = p.getChildren();
      while (i.hasNext()) {
        _getFunctionNames((ParserNode) i.next());
      }
    }
    
    
    /**
     * Open a file for writing workload of all function calls and communication cost.
     */
     /**
     */
    private void _writeWorkloadHeader() {
        try {
            // create the makefile
            PrintStream mf = _openFile(_outputDir + "/workload.h");
            
            mf.println("#ifndef " + "workload_H");
	    mf.println("#define " + "workload_H");
            
            // 	// FIFO read/write latency
	    // Currently we assume that communication cost is constant and equal for all
	    mf.println("extern const int latRead  = 1;     // Latency of FIFO read operation");
	    mf.println("extern const int latWrite  = 1;     // Latency of FIFO read operation");
	    
	    
	    // iterate over all processes to write latency of function calls
	    _functionNames = new Vector<String>();
	    Iterator i = _pn.getProcessList().iterator();
	    while( i.hasNext() ) {
	      CDProcess process = (CDProcess) i.next();
	      ParserNode parserNode = (ParserNode) process.getSchedule().get(0);
	      _getFunctionNames(parserNode);
	    } // end processes
	    
	    for(int j=0; j< _functionNames.size();j++){
	      mf.println("extern const int lat_" + _functionNames.get(j) + " = 1;     // latency of " + _functionNames.get(j));
	    }
	    
	    mf.println("#endif");
	}
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the workload header");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     */
    private CDProcessNetwork _pn = null;

    private Mapping _mapping = null;

    private String _outputDir = null;
    
    private Vector<String> _functionNames = null;  // The function names used in this CDPN
    
}

