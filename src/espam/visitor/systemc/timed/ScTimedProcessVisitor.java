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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGInVar;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDOutGate;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.processors.*;

import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.AssignStatement;

import espam.datamodel.domain.Polytope;
import espam.datamodel.domain.ControlExpression;

import espam.datamodel.EspamException;

import espam.main.UserInterface;
import espam.datamodel.LinearizationType;

import espam.utils.symbolic.expression.Expression;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// SystemC Process Visitor

/**
 * This class generates a timed SystemC model from a CDPN process.
 *
 * @author  Hristo Nikolov, Todor Stefanov, Sven van Haastregt, Teddy Zhai
 * @version  $Id: ScTimedProcessVisitor.java,v 1.20 2012/05/16 15:25:50 nikolov Exp $
 */

public class ScTimedProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the ScTimedProcessVisitor object
     */
    public ScTimedProcessVisitor(Mapping mapping, boolean scTimedPeriod) {
      _mapping = mapping;
      _scTimedPeriod = scTimedPeriod;
      
      _ui = UserInterface.getInstance();
      if(_ui.getADGFileNames().size() > 1) {
            _bMultiApp = true;
      }
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {
        // Generate the individual processes
        try {

            _pn = x;
            _printStreamFunc = _openFile("aux_func", "h");
            _printStreamFunc.println("#ifndef " + "aux_func_H");
            _printStreamFunc.println("#define " + "aux_func_H");
            _printStreamFunc.println("");
            _printStreamFunc.println("#include <math.h>");
            _printStreamFunc.println("#include \"" + x.getName() + "_func.h\"");
            _printStreamFunc.println("#include \"systemc.h\"");
            _printStreamFunc.println("");

            _printStreamFunc.println("#define numProcs " + x.getProcessList().size());

            //------------------------------
            // define the parameters
            //------------------------------
            ADGParameter parameter;
            Iterator j = _pn.getAdg().getParameterList().iterator();
            if( j.hasNext() ) {
                _printStreamFunc.println("");
            }
            while (j.hasNext()) {
                parameter = (ADGParameter) j.next();
                _printStreamFunc.println(_prefix + "#define param_" + parameter.getName() + " " + parameter.getValue());
            }
            _printStreamFunc.println("");

            _writeChannelTypes();
            _printStreamFunc.println("");

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

                CDProcess process = (CDProcess) i.next();
                _printStream = _openFile(process.getName(), "h");
                MProcessor mp = _mapping.getMProcessor(process);
                if (mp == null) {
                  // Process not mapped to a resource, apparently platform file was empty
                  // throw new EspamException("ERROR - Process not mapped onto resource; make sure you specify a non-empty platform.");

                  _scMicroBlazeProcess(process);
                
                } else {

                  Resource r = mp.getResource();

                  //      _printStream = _openFile(process.getName(), "h");
                  if (r instanceof MicroBlaze) {
                    _scMicroBlazeProcess(process);
                  }
                  else if (r instanceof CompaanHWNode) {
                    _scHWNProcess(process);
                    System.err.println("WARNING - CompaanHWNode is not yet supported!");
                  }
                  else {
                    throw new EspamException("ERROR - Unsupported processor type '" + r.toString() + "'");
                  }
                }
            }

            _printStreamFunc.println("");
            _writeOperations();
            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");

        }
        catch( Exception e ) {
            System.out.println(" In SystemC PN Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     *  Create a SystemC PN process for this MicroBlaze process.
     *
     * @param  x Description of the Parameter
     */
    public void _scMicroBlazeProcess( CDProcess x ) {
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("// Timed SystemC model of process " + x.getName() + " implemented on MicroBlaze");
        _writeIncludes( x );

        _writeClassDecl( x );
        _printStream.println("");
        _writeConstructor( x );
        _printStream.println("");

        // Write main
        _writeMainProcBegin( x );
        // Print the Parse tree
        ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        ScTimedMBStatementVisitor mbvisitor = new ScTimedMBStatementVisitor(_printStream, x);
        mbvisitor.setPrefix( _prefix );
        mbvisitor.setOffset( _offset );
        parserNode.accept(mbvisitor);
        _writeMainProcEnd( x );

        _printStream.println("");
        _writeComputPeriod( x );

        _prefixDec();
        _printStream.println("");
        _printStream.println("#endif");

        _writeFunctionsInAuxFile( x );
    }


    /**
     *  Create a SystemC PN process for HWN process.
     *
     * @param  x Description of the Parameter
     */
    public void _scHWNProcess( CDProcess x ) {
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("// Timed SystemC model of process " + x.getName() + " implemented as LAURA hardware node");
        _writeIncludes( x );

        _writeClassDecl( x );
        _printStream.println("");
        _writeConstructor( x );
        _printStream.println("");

        // Write main
        _writeMainProcBegin( x );
        ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        ScTimedHWNStatementVisitor hwnvisitor = new ScTimedHWNStatementVisitor(_printStream, x.getName());
	    //
        hwnvisitor.setPrefix( _prefix );
        hwnvisitor.setOffset( _offset );
        parserNode.accept(hwnvisitor);

        _writeMainProcEnd( x );

        _printStream.println("");
        _writeComputPeriod( x );
        _writeshift_pipeline( x );//zwdbd added

        _prefixDec();
        _printStream.println("");
        _printStream.println("#endif");
    }

    /**
     *  Description of the Method
     *
     * @param  fileName Description of the Parameter
     * @param  extension Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openFile( String fileName, String extension )
            throws FileNotFoundException {

        PrintStream printStream = null;
        UserInterface ui = UserInterface.getInstance();
        String fullFileName = "";

        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename. (REFACTOR)
        if( ui.getOutputFileName() == "" ) {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getFileName() + "_systemc/" + fileName + "." + extension;
        } else {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getOutputFileName() + "/" + fileName + "." + extension;
        }

        System.out.println(" -- OPEN FILE: " + fullFileName);

        if( fileName.equals("") ) {
            printStream = new PrintStream( System.out );
        } else {
            OutputStream file = null;

            file = new FileOutputStream( fullFileName );
            printStream = new PrintStream( file );
        }

        return printStream;
    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeConstructor( CDProcess x ) {
      _printStream.println(_prefix + "// Constructor ");

      String paramString = "";
      Iterator i = _pn.getAdg().getParameterList().iterator();
      while( i.hasNext() ) {
        ADGParameter parameter = (ADGParameter) i.next();
        paramString += ", int value_" + parameter.getName();
      }        

      _printStream.println(_prefix + x.getName() + "::" + x.getName() + "(sc_module_name mn, sc_trace_file *tf" + paramString + ") {");
      _prefixInc();
      _printStream.println(_prefix + "SC_THREAD(main_proc);");
      _printStream.println(_prefix + "sensitive << clk.pos();");
      _printStream.println(_prefix + "dont_initialize();");
      _printStream.println("");
      _printStream.println(_prefix + "sc_trace(tf, rd, \"" + x.getName() + ".RD\");");
      _printStream.println(_prefix + "sc_trace(tf, ex, \"" + x.getName() + ".UX\");");
      _printStream.println(_prefix + "sc_trace(tf, wr, \"" + x.getName() + ".WR\");");
      _printStream.println("");
      _printStream.println(_prefix + "rd.initialize(sc_logic_0);");
      _printStream.println(_prefix + "ex.initialize(sc_logic_0);");
      _printStream.println(_prefix + "wr.initialize(sc_logic_0);");
      _printStream.println(_prefix + "finish.initialize(sc_logic_0);");
      _printStream.println("");

      if (_pn.getAdg().getParameterList().size() > 0) _printStream.println(_prefix + "// Initialize parameters");
      Iterator I = _pn.getAdg().getParameterList().iterator();
      while( I.hasNext() ) {
        String p = ((ADGParameter) I.next()).getName();
        _printStream.println(_prefix + p + " = value_" + p + ";");
      }
      
      // add pipeline initialization
      MProcessor mp = _mapping.getMProcessor(x);
      if (mp != null) {
        Resource r = mp.getResource();
        if (r instanceof CompaanHWNode) {
          _printStream.println(_prefix + "for (int i = latRead + lat_"
        		  + ((ADGNode)(x.getAdgNodeList().get(0))).getFunction().getName() 
        		  + " + latWrite - 1; i >= 0; i--)");
          _prefixInc();
          _printStream.println(_prefix + "pipeline[i] = false;");
          _prefixDec();
        }
      }
      
      _printStream.println(_prefix + "}");
      _printStream.println("");

      // Write Destructor
      _printStream.println(_prefix + x.getName() + "::~" + x.getName() + "() {");
      _prefixInc();
      _printStream.println(_prefix + "cout << finishTime.str();");
      _prefixDec();
      _printStream.println(_prefix + "}");
      _printStream.println("");
      _prefixDec();
    }



    /**
     * @param  x Description of the Parameter
     */
    private void _writeIncludes( CDProcess x ) {

        _printStream.println("#ifndef " + x.getName() + "_H");
        _printStream.println("#define " + x.getName() + "_H");
        _printStream.println("");
        _printStream.println("#include \"math.h\"");
        _printStream.println("");
        _printStream.println("#include \"aux_func.h\"");
        _printStream.println("#include <iostream>");
        _printStream.println("");

        _printStream.println("");
    }


    /**
     * Traverses tree p and writes a function latency variable for every AssignStatement.
     */
//     private void _writeFunctionLatencies(ParserNode p) {
//       if (p instanceof AssignStatement) {
//         AssignStatement s = (AssignStatement) p;
//         if (_functionNames.contains(s.getFunctionName()) == false) {
//           // We insert a _ on purpose, to avoid conflicts with user-defined function names like "Read"
//           _printStream.println(_prefix + "const int lat_" + s.getFunctionName() + " = 10;     // latency of " + s.getFunctionName());
//           _functionNames.add(s.getFunctionName());
//         }
//       }
//       Iterator i = p.getChildren();
//       while (i.hasNext()) {
//         _writeFunctionLatencies((ParserNode) i.next());
//       }
//     }


    /**
     *  Writes the first part of main, up to where the AST should be inserted.
     *
     * @param  x Description of the Parameter
     */
    private void _writeMainProcBegin( CDProcess x) {
        _printStream.println(_prefix + "void " + x.getName() + "::main_proc() {");
        _prefixInc();

        _functionNames = new Vector<String>();
//        _writeFunctionArguments(x);
        _writeLocalVariables(x);

        _printStream.println("");
        _printStream.println(_prefix + "// Initial 1-cycle delay to ensure FIFO is ready");
        _printStream.println(_prefix + "waitcycles(1);");
        _printStream.println("");
        
        // we want to execute the network for 10 times to make sure that it reaches "steady-state" (Teddy)
	if ( _scTimedPeriod ){
	  _printStream.println(_prefix + "for( int t = ceil1(1); t <= ceil1(10); t += 1 ) {");
//	} else {
//	  _printStream.println(_prefix + "// for( int t = ceil1(1); t <= 10; t += 1 ) {");
	}       
    }


    /**
     *  Writes the last part of main that comes after the AST dump.
     *
     * @param  x Description of the Parameter
     */
    private void _writeMainProcEnd( CDProcess x) {
        _printStream.println("");
        	
	// compute the period of processes (networks)
        if ( _scTimedPeriod ){
	  _printStream.println(_prefix + "iter_finish_time.push_back(sc_time_stamp().to_default_time_units());");
	  _printStream.println(_prefix + "compute_period(sc_time_stamp().to_default_time_units());");
	  _printStream.println(_prefix + "}");
//        } else {
//	  _printStream.println(_prefix + "// iter_finish_time.push_back(sc_time_stamp().to_default_time_units());");
//	  _printStream.println(_prefix + "// compute_period(sc_time_stamp().to_default_time_units());");
//          _printStream.println(_prefix + "// }");
        }
        
        _printStream.println(_prefix + "finish.write(sc_logic_1);");
        _printStream.println(_prefix + "cout << \"" + x.getName() + " finished at \" << sc_time_stamp() << endl;");
                
        _printStream.println("");
        _printStream.println(_prefix + "return;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
    }


    /**
     *  Description of the Method
     */
/*
    private void _writeChannelTypes() {

        CDChannel channel;
        String type;

        Iterator i = _pn.getChannelList().iterator();
        while( i.hasNext() ) {
           channel = (CDChannel) i.next();

           // FIXME::TYPE:  We should use the stored datatype "type". However, since the current fsl_fifo inherits from
           // sc_fifo, this requires that the operator<< is defined for "type". We cannot assume that this operator is
           // actually defined for any user data types, so for now we always use the int data type.
           //type = ((ADGVariable) ((ADGEdge)channel.getAdgEdgeList().get(0)).getFromPort().getBindVariables().get(0)).getDataType();

// FIXED: see the writeFSL and readFSL primitives in the aux_func.h file
           type = "int";

           if( !type.equals("") ) {
               String s = "typedef " + type + " t" + channel.getName()+";";
               _printStreamFunc.println( s );
           } else {
               String s = "typedef char t" + channel.getName()+";";
               _printStreamFunc.println( s );
           }
        }
    }
/*

    /**
     *  Description of the Method
     */
    private void _writeChannelTypes() {

        CDChannel channel;
        String type;

        Iterator i = _pn.getChannelList().iterator();
        while( i.hasNext() ) {
           channel = (CDChannel) i.next();
           type = ((ADGVariable) ((ADGEdge)channel.getAdgEdgeList().get(0)).getFromPort().getBindVariables().get(0)).getDataType();

           if( !type.equals("") ) {
               String s = "typedef " + type + " t" + channel.getName()+";";
               _printStreamFunc.println( s );
           } else {
               String s = "typedef char t" + channel.getName()+";";
               _printStreamFunc.println( s );
           }
        }
    }

    /**
     *  Description of the Method
     */
    private void _writeOperations() {
        _printStreamFunc.println("inline");
        _printStreamFunc.println("double min( double a, double b ){");
        _printStreamFunc.println("  if ( a>=b )  {");
        _printStreamFunc.println("    return b;");
        _printStreamFunc.println("  } else {");
        _printStreamFunc.println("    return a;");
        _printStreamFunc.println("  }");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline");
        _printStreamFunc.println("double max( double a, double b ){");
        _printStreamFunc.println("  if ( a>=b )  {");
        _printStreamFunc.println("    return a;");
        _printStreamFunc.println("  } else {");
        _printStreamFunc.println("    return b;");
        _printStreamFunc.println("  }");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline");
        _printStreamFunc.println("int ddiv( double a, double b ){\n");
        _printStreamFunc.println("    //return (int)(a/b);");
        _printStreamFunc.println("    return ( (int) (((a)<0) ? ((a)-(b)+1)/(b) : (a)/(b)) ); ");
        _printStreamFunc.println("    //return ( (int) (((a)<0)^((b)<0) ? ((a) < 0 ? ((a)-(b)+1)/(b) : ((a)-(b)-1)/(b)) : (a)/(b)) ); ");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline");
        _printStreamFunc.println("int mod( double a, double b ){\n");
        _printStreamFunc.println("    return (int)fmod(a, b);");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline");
        _printStreamFunc.println("int ceil1( double a ){\n");
        _printStreamFunc.println("    return (int) ceil(a);");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline");
        _printStreamFunc.println("int floor1( double a ){\n");
        _printStreamFunc.println("    return (int) floor(a);");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("#define writeFSL( wr, fifo, value, len ) \\");
        _printStreamFunc.println("     do {\\");
        _printStreamFunc.println("        unsigned int i;\\");
        _printStreamFunc.println("        for( i = 0; i < len; i++ ) { \\");
        _printStreamFunc.println("            wr.write(sc_logic_X);\\");
        _printStreamFunc.println("            fifo->write( ((int *) &value)[i] );\\");
        _printStreamFunc.println("            wr.write(sc_logic_1);\\");
        _printStreamFunc.println("            waitcycles(latWrite);\\");
        _printStreamFunc.println("            wr.write(sc_logic_0);\\");
        _printStreamFunc.println("        }\\");
        _printStreamFunc.println("     } while(0)");
        _printStreamFunc.println("");
     
        _printStreamFunc.println("#define readFSL( rd, fifo, value, len ) \\");
        _printStreamFunc.println("    do {\\");
        _printStreamFunc.println("       unsigned int i;\\");
        _printStreamFunc.println("       for( i = 0; i < len; i++ ) { \\");
        _printStreamFunc.println("           rd.write(sc_logic_X);\\");
        _printStreamFunc.println("           fifo->read( ((int *) &value)[i] );\\");
        _printStreamFunc.println("           rd.write(sc_logic_1);\\");
        _printStreamFunc.println("           waitcycles(latRead);\\");
        _printStreamFunc.println("           rd.write(sc_logic_0);\\");
        _printStreamFunc.println("       }\\");
        _printStreamFunc.println("    } while(0)");    
        _printStreamFunc.println("");

        _printStreamFunc.println("#define waitcycles(v) \\");
        _printStreamFunc.println("  wait((v), SC_NS);");
        _printStreamFunc.println("");
    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeClassDecl( CDProcess x ) {
        _printStream.println("SC_MODULE(" + x.getName() + ") {");
        _prefixInc();
        _printStream.println(_prefix + "std::vector<double> iter_finish_time;");
        _printStream.println(_prefix + "public:");
        _prefixInc();
        _printStream.println(_prefix + "sc_in<bool> clk;");

        //-----------------------
        // declare the read gates
        //-----------------------
        Iterator n = x.getInGates().iterator();
        if( n.hasNext() ) {
            _printStream.println("");
            _printStream.println(_prefix + "// Input Gates and controllers");
        }
        while( n.hasNext() ) {
            CDInGate gate = (CDInGate) n.next();
            LinearizationType comModel =
                    ((CDChannel)gate.getChannel()).getCommunicationModel();
            String s = gate.getName();
            String t = gate.getChannel().getName();

//            _printStream.println(_prefix + "sc_fifo_in<t" + t + "> " + s + ";");
            _printStream.println(_prefix + "sc_fifo_in<int> " + s + ";");

            if( comModel == LinearizationType.fifo ||
                comModel == LinearizationType.BroadcastInOrder ||
                comModel == LinearizationType.sticky_fifo ||
                comModel == LinearizationType.shift_register ) {
            } else if( comModel == LinearizationType.GenericOutOfOrder ) {
                System.out.println("WARNING: Out of order channels are not supported yet!");
            }
        }

        //------------------------
        // declare the write gates
        //------------------------
        n = x.getOutGates().iterator();
        if( n.hasNext() ) {
            _printStream.println("");
            _printStream.println(_prefix + "// Output Gates");
        }
        while( n.hasNext() ) {
            CDOutGate gate = (CDOutGate) n.next();
            String s = gate.getName();
            String t = gate.getChannel().getName();

//            _printStream.println(_prefix + "sc_fifo_out<t" + t + "> " + s + ";");
            _printStream.println(_prefix + "sc_fifo_out<int> " + s + ";");
        }

        //------------------------------
        // declare the public parameters
        //------------------------------
        Iterator j = _pn.getAdg().getParameterList().iterator();
        if( j.hasNext() ) {
            _printStream.println("");
            _printStream.println(_prefix + "// Parameters");
        }
        while (j.hasNext()) {
            ADGParameter p = (ADGParameter) j.next();
            _printStream.println(_prefix + "int " + p.getName() + ";");
        }

        //----------------------------
        // declare pipeline for HWNode
        //----------------------------
        MProcessor mp = _mapping.getMProcessor(x);
        if (mp != null) {
          Resource r = mp.getResource();
          if (r instanceof CompaanHWNode) {
                _printStream.println("");
        	_printStream.println(_prefix + "bool pipeline[latRead + lat_" 
        			+ ((ADGNode)(x.getAdgNodeList().get(0))).getFunction().getName() + " + latWrite];");
          }
        }

        //-----------------------
        // Declare common signals
        //-----------------------
        _printStream.println("");
//        _printStream.println(_prefix + "// Common signals");
//        _printStream.println(_prefix + "sc_in<bool> clk;");
//        _printStream.println("");

        _printStream.println(_prefix + "// Signals for inspection");
        _printStream.println(_prefix + "sc_out<sc_logic> rd;");
        _printStream.println(_prefix + "sc_out<sc_logic> ex;");
        _printStream.println(_prefix + "sc_out<sc_logic> wr;");
        _printStream.println(_prefix + "sc_out<sc_logic> finish;");
        _printStream.println("");

        _printStream.println(_prefix + "SC_HAS_PROCESS(" + x.getName() + ");");

        String paramString = "";
        Iterator i = _pn.getAdg().getParameterList().iterator();
        while( i.hasNext() ) {
          ADGParameter parameter = (ADGParameter) i.next();
          paramString += ", int value_" + parameter.getName();
        }        

        _printStream.println(_prefix + x.getName() + "(sc_module_name mn, sc_trace_file *tf" + paramString + ");");
        _printStream.println("");

        _printStream.println(_prefix + "~" + x.getName() + "();");
        _printStream.println("");

        _printStream.println(_prefix + "void main_proc();");

        _printStream.println("");
        _prefixDec();
        _printStream.println(_prefix + "private:");
        _prefixInc();
        _printStream.println(_prefix + "void compute_period(const double& finish_time);");
        _printStream.println(_prefix + "std::ostringstream finishTime;");
        //----------------------------
        // declare pipeline for HWNode
        //----------------------------
        mp = _mapping.getMProcessor(x);
        if (mp != null) {
          Resource r = mp.getResource();
          if (r instanceof CompaanHWNode) {
            _printStream.println(_prefix + "void shift_pipeline();");
          }
        }
        _prefixDec();
        _prefixDec();
        _printStream.println(_prefix + "};");
        _printStream.println("");
    }


    /**
     * @param  x Description of the Parameter
     */
    private void _writeLocalVariables( CDProcess x ) {

	String varName = "";
	String dimension = "";
	String t = "";
	HashMap  tmp = new HashMap();
	Vector inArguments = new Vector();
	Vector outArguments = new Vector();
	Vector miscVariables = new Vector();
	Vector tempVector = new Vector();

        Iterator n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {
            ADGNode node = (ADGNode) n.next();
	    ADGFunction function = (ADGFunction) node.getFunction();

	    String suffix = "";
	    if( _bMultiApp ) {
		suffix = "_" + node.getName();
	    }

	    //-------------------------
	    // Scan the ports of a node
	    //-------------------------
	    Iterator j1 = node.getPortList().iterator();
            while( j1.hasNext() ) {
		ADGPort port = (ADGPort) j1.next();

		Iterator j2 = port.getBindVariables().iterator();
		while( j2.hasNext() ) {
                	ADGVariable bindVar = (ADGVariable) j2.next();
			dimension = "";
                	varName = bindVar.getName();

                        //-------------------------------------------------
			// Find the gate corresponding to this funcArgument
                        //-------------------------------------------------
			Iterator g = x.getGateList().iterator();
	        	while ( g.hasNext() ) {
			    CDGate  gate = (CDGate) g.next();

			    Iterator p = gate.getAdgPortList().iterator();
			    while( p.hasNext() ) {
				ADGPort tmpPort = (ADGPort) p.next();

				if( tmpPort.getName().equals( port.getName() ) ) {
				    t = "t" + gate.getChannel().getName();
				}
			    }
			}

                        //-----------------------------------------------------------
			// Find the dimensions in case the local variable is an array
                        //-----------------------------------------------------------
			Iterator j3 = bindVar.getIndexList().iterator();
			while( j3.hasNext() ) {
			   Expression exp = (Expression) j3.next();
                           //----------------------------------------
			   // Do some expression computations here!!!
                           //---------------------------------------- 
			   String arrSize = "";
			   dimension += "[" + arrSize + "]";
			}	
			varName += dimension;
	
                        //-------------------------------------------------------------------
			// Avoid duplicating declarations 
			// (unique names for the hash function are needed in case of merging)
                        //-------------------------------------------------------------------
			if ( !tmp.containsKey(varName+node.getName()) ) {
			   tmp.put(varName+node.getName(), "");
			   String decString = _prefix + t + " " + varName;
			   
                           //------------------------------------------------------------------------------------  
			   // sort the variables into input arguments, output arguments, and additional variables
                           //------------------------------------------------------------------------------------
			   int counter = 0;
			   Iterator j4 = function.getInArgumentList().iterator();
			   while( j4.hasNext() ) {
				ADGVariable arg = (ADGVariable) j4.next();
				String funcArgument = arg.getName();
				if( funcArgument.equals( varName ) ) {
					inArguments.add( decString  + suffix + ";" );
					counter++;
				}
			   }
			   if( counter==0 ) {
				j4 = function.getOutArgumentList().iterator();
				while( j4.hasNext() ) {
					ADGVariable arg = (ADGVariable) j4.next();
					String funcArgument = arg.getName();
					if( funcArgument.equals( varName ) ) {
						outArguments.add( decString + suffix + ";" );
						counter++;
					}
				}
			   }
			   if( counter==0 ) { 
                                boolean bInVar = false;
				// Avoid putting suffix to the control "dc.." variables
				if( varName.contains("dc") ) {
				    if ( !tmp.containsKey(varName) ) {
					tmp.put(varName, "");
				        miscVariables.add( decString + ";" );
				    }

				} else {
				    Iterator jj = node.getInVarList().iterator();
				    while( jj.hasNext() ) {
					ADGInVar invar = (ADGInVar) jj.next();
					String	invarName = invar.getRealName();
                                        if( invarName.equals( varName ) ) {
					    bInVar = true;
					}
				    }

				    if( bInVar ) {
					miscVariables.add( decString + suffix + ";" );
                                    } else { // it is an 'enable' variable
                                        if ( !tmp.containsKey(varName) ) {
					   tmp.put(varName, "");
					   miscVariables.add( decString + ";" );
                                        }
				    }
				}
			   }
			}

                        //-----------------------------------------------
                        // add the static control statements (int e0;...)
                        //-----------------------------------------------
			Vector staticCtrl = ((Polytope)port.getDomain().getLinearBound().get(0)).getIndexVector().getStaticCtrlVector();
			Iterator j = staticCtrl.iterator();
			while( j.hasNext() ) {
				ControlExpression cExp = (ControlExpression) j.next();
				String expName = cExp.getName();

				if ( !tmp.containsKey(expName) ) {
				      tmp.put(expName, "");

				      String decString = _prefix + "int " + expName + ";"; 
				      miscVariables.add( decString );
				}
			}
		}
            } // while 'ports'

	    //------------------------------
	    // Scan the invar list of a node
	    //------------------------------
	    j1 = node.getInVarList().iterator();
            while( j1.hasNext() ) {
		ADGInVar invar = (ADGInVar) j1.next();

		ADGVariable bindVar = invar.getBindVariable();

 			dimension = "";
                	varName = bindVar.getName();
			t = bindVar.getDataType();

                        //-----------------------------------------------------------
			// Find the dimensions in case the local variable is an array
                        //-----------------------------------------------------------
			Iterator j3 = bindVar.getIndexList().iterator();
			while( j3.hasNext() ) {
			   Expression exp = (Expression) j3.next();
                           //----------------------------------------  
			   // Do some expression computations here!!!
                           //----------------------------------------
			   String arrSize = "";
			   dimension += "[" + arrSize + "]";
			}	
			varName += dimension;
		
                        //-------------------------------------------------------------------
			// Avoid duplicating declarations
			// (unique names for the hash function are needed in case of merging)
                        //-------------------------------------------------------------------
			if ( !tmp.containsKey(varName+node.getName()) ) {
			   tmp.put(varName+node.getName(), "");
			   String decString = _prefix + t + " " + varName;
			   
                           //------------------------------------------------------------------------------------ 
			   // sort the variables into input arguments, output arguments, and additional variables
                           //------------------------------------------------------------------------------------
			   int counter = 0;
			   Iterator j4 = function.getInArgumentList().iterator();
			   while( j4.hasNext() ) {
				ADGVariable arg = (ADGVariable) j4.next();
				String funcArgument = arg.getName();
				if( funcArgument.equals( varName ) ) {
					inArguments.add( decString + suffix + ";" );
					counter++;
				}
			   }
			   if( counter==0 ) {
				j4 = function.getOutArgumentList().iterator();
				while( j4.hasNext() ) {
					ADGVariable arg = (ADGVariable) j4.next();
					String funcArgument = arg.getName();
					if( funcArgument.equals( varName ) ) {
						outArguments.add( decString + suffix + ";" );
						counter++;
					}
				}
			   }
			   if( counter==0 ) { 
				// Avoid putting suffix to the control "dc.." variables
				if( varName.contains("dc") ) {
				    if ( !tmp.containsKey(varName) ) {
					tmp.put(varName, "");
				        miscVariables.add( decString + ";" );
                                    }
				} else {
				    miscVariables.add( decString + suffix + ";" );
				}
			   }
			}
            } // while 'invars'

	    //-----------------------------------------------------------------
	    // Add an input function argument which is not bound to any port. 
            // This happens in case loop iterators are propagated to functions.
	    //-----------------------------------------------------------------
	    Iterator f = function.getInArgumentList().iterator();
            while( f.hasNext() ) {
                ADGVariable arg = (ADGVariable) f.next();

		dimension = "";
               	varName = arg.getName();
		String dataType = arg.getDataType();

		if ( !tmp.containsKey(varName+node.getName()) ) {
                    String funcArgDeclaration = _prefix + dataType + " " + arg.getName() + ";";
    		    inArguments.add( funcArgDeclaration );
                }
	    }

	    //----------------------------------------------------------------
	    // Add an output function argument which is not bound to any port.
            // This happens in case of a sink node. 
	    //----------------------------------------------------------------
	    f = function.getOutArgumentList().iterator();
            while( f.hasNext() ) {
                ADGVariable arg = (ADGVariable) f.next();

		dimension = "";
               	varName = arg.getName();
		String dataType = arg.getDataType();

		if ( !tmp.containsKey(varName+node.getName()) ) {
                    String funcArgDeclaration = _prefix + dataType + " " + arg.getName() + ";";
    		    outArguments.add( funcArgDeclaration );
                }
	    }
	} // while 'nodes'

        //-------------------------------
        // print the sorted declarations
        //-------------------------------
	if( inArguments.size()>0 ) {
		_printStream.println("");
		_printStream.println(_prefix + "// Function's Input Arguments ");
		n = inArguments.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
	}
	if( outArguments.size()>0 ) {
		_printStream.println("");
		_printStream.println(_prefix + "// Function's Output Arguments ");
		n = outArguments.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
	}
	if( miscVariables.size()>0 ) {
		_printStream.println("");
		_printStream.println(_prefix + "// Additional Local Variables ");
		n = miscVariables.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
	}
    }


    /**
     * @param  x Description of the Parameter
     */
    private void _writeFunctionsInAuxFile( CDProcess x ) {

	String funcName = "";
        String csl = "";
	String t = "";
        String returnValue = "";
        boolean rhsArg = false;

        //------------------------------- 
        // Write func wrapper in aux file
        //-------------------------------
        Iterator n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {
            ADGNode node = (ADGNode) n.next();
            ADGFunction function1 = (ADGFunction) node.getFunction();

            csl = "";
            funcName = function1.getName();
	    if ( !funcName.equals("") ) {

	        if(!_relation2.containsKey(funcName) ) {
	            _printStreamFunc.println("inline");
                    csl += "void  _" + funcName + "( ";

                    Iterator j2 = function1.getInArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
                        String funcArgument = arg.getName() + node.getName();
	                String dataType = arg.getDataType();

	                t = "char";
		        if (dataType != null) {
		          if (!dataType.equals("") ) {
		             t = dataType;
		          }
		        }

		        Iterator g = x.getGateList().iterator();
	                while ( g.hasNext() ) {
		           CDGate  gate = (CDGate) g.next();

		           Iterator p = gate.getAdgPortList().iterator();
		           while( p.hasNext() ) {
       	                       ADGPort port = (ADGPort) p.next();

   		               Iterator bvi = port.getBindVariables().iterator();
		               while ( bvi.hasNext() ) {
		                   ADGVariable bv = (ADGVariable) bvi.next();
                                   String tmp = bv.getName() + port.getNode().getName();
		                   if( funcArgument.equals( tmp ) ) {
	                               t = "t" + gate.getChannel().getName();
	                           }
		               }
                          }
		       }
		       csl +=  t + " &" + arg.getName() + ", ";
                    }

                    returnValue = "";
                    j2 = function1.getOutArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
                        String funcArgument = arg.getName() + node.getName();
	                String dataType = arg.getDataType();

			if( arg.getPassType().equals("return_value") ) {
                           returnValue = arg.getName() + " = ";
                        } else if( arg.getPassType().equals("reference") ) {
                           rhsArg = true;                            
                        }

	                t = "char";
		        if (dataType != null) {
		          if (!dataType.equals("") ) {
		             t = dataType;
		          }
		        }

		        Iterator g = x.getGateList().iterator();
	                while ( g.hasNext() ) {
		           CDGate  gate = (CDGate) g.next();

		           Iterator p = gate.getAdgPortList().iterator();
		           while( p.hasNext() ) {
       	                      ADGPort port = (ADGPort) p.next();
		              String tmp = ((ADGVariable) port.getBindVariables().get(0)).getName() + port.getNode().getName();
		              if( funcArgument.equals( tmp ) ) {
		                 t = "t" + gate.getChannel().getName();
	                      }
                          }
		        }
                        csl += t + " &" + arg.getName() + ", ";
                   }
                   _printStreamFunc.println(csl.substring(0, (csl.length() - 2)) + " ) {");

		   //-------- print the initial function call in the wrapper -----------------------
                    csl = returnValue + funcName + "( ";

                    j2 = function1.getInArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
                        if( arg.getPassType().equals("reference") ) {
 		              csl += "&" + arg.getName() + ", ";
                        } else {
 		              csl += arg.getName() + ", ";
                        }
                    }

                    j2 = function1.getOutArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
                        if( arg.getPassType().equals("reference") ) {
			      csl += "&" + arg.getName() + ", ";
                        }
                    }

                    if( function1.getInArgumentList().size()==0 && rhsArg==false ) {
                        _printStreamFunc.println("    " + csl.substring(0, (csl.length() - 1)) + ");");
                    } else {
                        _printStreamFunc.println("    " + csl.substring(0, (csl.length() - 2)) + " );");
                    }
		    //-------- END print of the initial function call in the wrapper ---------------

                    _printStreamFunc.println("}");
		    _printStreamFunc.println("");
                }
                _relation2.put(funcName, "");
	    }
        }
        _printStream.println("");
    }








    private void _writeComputPeriod(CDProcess x) {
      _printStream.println(_prefix + "// use this function only for the sink process");
      _printStream.println(_prefix + "void " + x.getName() + "::compute_period(const double& finish_time) {");
      _prefixInc();
      _printStream.println(_prefix + "if (iter_finish_time.size() == 1) return;");
      _printStream.println(""); 
      _printStream.println(_prefix + "// to record the finish time of the last iteration  ");
      
      _printStream.println(_prefix + "double last_finish_time = *(iter_finish_time.end()-2);");
      _printStream.println(_prefix + "double period = finish_time - last_finish_time;");
      _printStream.println(_prefix + "cout << \"period of the PPN is: \" << period << endl;");
      
      _prefixDec();
      _printStream.println(_prefix + "}");
      _printStream.println(""); 
    }
       
    private void _writeshift_pipeline(CDProcess x) {
        _printStream.println(_prefix + "// use this function only for the sink process");
        _printStream.println(_prefix + "void " + x.getName() + "::shift_pipeline() {");
        _prefixInc();
        _printStream.println(_prefix + "for (int i = latRead + lat_"
        		+ ((ADGNode)(x.getAdgNodeList().get(0))).getFunction().getName() + " + latWrite - 1; i > 0; i--)");
        _prefixInc();
        _printStream.println(_prefix + "pipeline[i] = pipeline[i - 1];");
        _prefixDec();
        _printStream.println(_prefix + "pipeline[0] = false;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println(""); 
      }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    private CDProcessNetwork _pn = null;

    private Mapping _mapping = null;
    private boolean _scTimedPeriod = false;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

    private Vector<String> _functionNames = null;  // The function names used in this CDProcess

    private HashMap _relation2 = new HashMap();

    private UserInterface _ui = null;

    private boolean _bMultiApp = false;
}
