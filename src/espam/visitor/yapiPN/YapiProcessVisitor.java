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

package espam.visitor.yapiPN;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import espam.datamodel.graph.adg.ADGVariable;
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

import espam.datamodel.parsetree.ParserNode;

import espam.main.UserInterface;
import espam.datamodel.LinearizationType;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// Yapi Process Visitor

/**
 *  This class ...
 *
 * @author  Hristo Nikolov,Todor Stefanov
 * @version  $Id: YapiProcessVisitor.java,v 1.2 2008/08/08 10:47:02 stefanov Exp $
 */

public class YapiProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the YapiProcessVisitor object
     */
    public YapiProcessVisitor() {
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
            _printStreamFunc.println("");

            _writeChannelTypes();
            _printStreamFunc.println("");

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

                CDProcess process = (CDProcess) i.next();

                _printStream = _openFile(process.getName(), "h");
                yapiProcess( process );
            }

            _printStreamFunc.println("");
            _writeOperations();
            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");

        }
        catch( Exception e ) {
            System.out.println(" In Yapi PN Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Create a Yapi PN process for this process.
     *
     * @param  x Description of the Parameter
     */
    public void yapiProcess( CDProcess x ) {

        _writeIncludes( x );
        _printStream.println("class " + x.getName() + " : public Process {");
        _prefixInc();
        _printStream.println(_prefix + "private:");

        _writeParameterAndGates( x );
        _writeFunctionArguments( x );
        _writeConstructor( x );
        _writeMain( x );

        _prefixDec();
        _printStream.println("};");
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
                ui.getFileName() + "/" + fileName + "." + extension;
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
       _printStream.println(_prefix + "public:");
       _prefixInc();
       _printStream.println("");
       _printStream.println(_prefix + "// Constructor ");
       _printStream.print(_prefix + x.getName() + "(Id n");

       String csl = "";
       CDGate gate;

       Iterator j = x.getGateList().iterator();
       while( j.hasNext() ) {

          gate = (CDGate) j.next();
          String t = "t" + gate.getChannel().getName();
	  if( gate instanceof CDInGate ) {
             csl += ",\n" + _prefix + "In<" + t + ">& " + gate.getName() + "_instance";
	  } else {
             csl += ",\n" + _prefix + "Out<" + t + ">& " + gate.getName() + "_instance";
	  }
       }

       Iterator I = _pn.getAdg().getParameterList().iterator();
       while( I.hasNext() ) {
          String p = ((ADGParameter) I.next()).getName();
          csl += ",\n" + _prefix + "int parm_" + p;
       }
       _printStream.print(csl);
       _printStream.println(") :");

       _writeConstructorBody(x);
       _prefixDec();
       _printStream.println("");
    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeConstructorBody( CDProcess x ) {

        CDGate gate;
        String csl = "";
        csl += _prefix + "Process(n)";

        // instatiate the read ports
        Iterator j = x.getGateList().iterator();
        while( j.hasNext() ) {
            gate = (CDGate) j.next();

	    if( gate.getChannel() != null ) {
               csl += ",\n" + _prefix + gate.getName() +
	              "(id(\"" + gate.getName() +
	              "\"), "  + gate.getName() + "_instance )";
            }
        }

        Iterator I = _pn.getAdg().getParameterList().iterator();
        while( I.hasNext() ) {
            String p = ((ADGParameter) I.next()).getName();
            csl += ",\n" + _prefix + p + "(parm_" + p + ")";
        }

        _printStream.print(csl);
        _printStream.println("");
        _printStream.println(_prefix + "{");

        _prefixInc();

        j = x.getGateList().iterator();
        while( j.hasNext() ) {
            gate = (CDGate) j.next();

  	    LinearizationType comModel =
		    ((CDChannel)gate.getChannel()).getCommunicationModel();
	    if (comModel != LinearizationType.fifo &&
		comModel != LinearizationType.BroadcastInOrder &&
		comModel != LinearizationType.sticky_fifo &&
		comModel != LinearizationType.shift_register) {
               System.out.println("ERROR: Out of order channels are not supported yet!");
	       System.exit(0);
	    }
        }

        _prefixDec();
        _printStream.println(_prefix + "}; ");
        _printStream.println("");

        // print the type() method;
        _printStream.println(_prefix + "const char* type() const {return \"" + x.getName() + "\";};");
    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeFunctionArguments( CDProcess x ) {

	String funcName = "";
        String csl = "";
	String t = "";

	_prefixInc();
        // declare the input arguments of the function
        _printStream.println(_prefix + "// Input Arguments ");

        Iterator n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {
            ADGNode node = (ADGNode) n.next();
            ADGFunction function = (ADGFunction) node.getFunction();

	    Iterator j1 = function.getInArgumentList().iterator();
            while( j1.hasNext() ) {
                ADGVariable arg = (ADGVariable) j1.next();
                String funcArgument = arg.getName() + node.getName();
		String dataType = arg.getDataType();

	        t = "char";
		if (dataType != null) {
		   if (!dataType.equals("")) {
		       t = dataType;
		   }
		}

		// Find the gate corresponding to this funcArgumnet
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

                _printStream.println(_prefix + t + " " + funcArgument + ";");
            }
        }

        _printStream.println("");

        // declare the output arguments of the function
        _printStream.println(_prefix + "// Output Arguments ");

	n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {
            ADGNode node = (ADGNode) n.next();
            ADGFunction function1 = (ADGFunction) node.getFunction();


            Iterator j2 = function1.getOutArgumentList().iterator();
            while( j2.hasNext() ) {
                ADGVariable arg = (ADGVariable) j2.next();
                String funcArgument = arg.getName() + node.getName();
		String dataType = arg.getDataType();

	        t = "char";
		if (dataType != null) {
		   if (!dataType.equals("")) {
		       t = dataType;
		   }
		}

		// Find the gate corresponding to this funcArgumnet
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

                _printStream.println(_prefix + t +" " + funcArgument + ";");
            }
        }

	_prefixDec();

        //write func wrapper in aux file
        n = x.getAdgNodeList().iterator();
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

		       csl +=  t + " " + arg.getName() + ", ";
                    }

                    j2 = function1.getOutArgumentList().iterator();
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
		              String tmp = ((ADGVariable) port.getBindVariables().get(0)).getName() + port.getNode().getName();
		              if( funcArgument.equals( tmp ) ) {
		                 t = "t" + gate.getChannel().getName();
	                      }
                          }
		        }

                        csl += t + " &" + arg.getName() + ", ";
                   }
                   _printStreamFunc.println(csl.substring(0, (csl.length() - 2)) + " ) {");

		   //-------- print the initial function call in the wrapper ------------------------
                    csl = funcName + "( ";

                    j2 = function1.getInArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
 		        csl += "&" + arg.getName() + ", ";
			//csl += arg.getName() + ", ";
                    }

                    j2 = function1.getOutArgumentList().iterator();
                    while( j2.hasNext() ) {
                        ADGVariable arg = (ADGVariable) j2.next();
			csl += "&" + arg.getName() + ", ";
                   }

                   _printStreamFunc.println("    " + csl.substring(0, (csl.length() - 2)) + " );");
		   //-------- END print of the initial function call in the wrapper ------------------------

                   _printStreamFunc.println("}");
		   _printStreamFunc.println("");
                }
                _relation2.put(funcName, "");
	    }
        }
        _printStream.println("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * @param  x Description of the Parameter
     */
    private void _writeIncludes( CDProcess x ) {

        _printStream.println("#ifndef " + x.getName() + "_H");
        _printStream.println("#define " + x.getName() + "_H");
        _printStream.println("");
        _printStream.println("#include \"math.h\"");
        _printStream.println("#include \"yapi.h\"");
        _printStream.println("");
        _printStream.println("#include \"aux_func.h\"");
        _printStream.println("#include <iostream>");
        _printStream.println("");

        Iterator n = x.getGateList().iterator();
        while( n.hasNext() ) {
            CDGate gate = (CDGate) n.next();
  	    LinearizationType comModel =
		    ((CDChannel)gate.getChannel()).getCommunicationModel();

	    if (comModel != LinearizationType.fifo &&
		comModel != LinearizationType.BroadcastInOrder &&
		comModel != LinearizationType.sticky_fifo &&
		comModel != LinearizationType.shift_register ) {
               System.out.println("ERROR: Out of order channels are not supported yet!");
	       System.exit(0);
	    }
        }
        _printStream.println("");
    }

    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _writeMain( CDProcess x) {

        _prefixInc();
        _printStream.println(_prefix + "void main() {");
        _printStream.println("");

        // Print the Parse tree
        YapiStatementVisitor ypvisitor = new YapiStatementVisitor(_printStream, x.getName());
        ypvisitor.setPrefix( _prefix );
        ypvisitor.setOffset( _offset );

	ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        parserNode.accept(ypvisitor);

        _printStream.println("");
        _printStream.println(_prefix + "} // main");
	_prefixDec();
    }

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

    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeParameterAndGates( CDProcess x ) {
        _prefixInc();
        _printStream.println("");
        _printStream.println(_prefix + "// Input Gates and controllers");


        // declare the read gates
        Iterator n = x.getInGates().iterator();
        while( n.hasNext() ) {
            CDInGate gate = (CDInGate) n.next();
  	    LinearizationType comModel =
		    ((CDChannel)gate.getChannel()).getCommunicationModel();
            String s = gate.getName();
            String t = gate.getChannel().getName();

	    _printStream.println(_prefix + "InPort<t" + t + "> " + s + ";");

	    if (comModel != LinearizationType.fifo &&
		comModel != LinearizationType.BroadcastInOrder &&
		comModel != LinearizationType.sticky_fifo &&
		comModel != LinearizationType.shift_register) {
               System.out.println("ERROR: Out of order channels are not supported yet!");
	       System.exit(0);
	    }
        }

        _printStream.println("");

       _printStream.println(_prefix + "// Output Gates");
        // declare the write gates
        n = x.getOutGates().iterator();
        while( n.hasNext() ) {
            CDOutGate gate = (CDOutGate) n.next();
            String s = gate.getName();
            String t = gate.getChannel().getName();

	    _printStream.println(_prefix + "OutPort<t" + t + "> " + s + ";");
        }

	_printStream.println("");

        // declare the public parameters
        _printStream.println(_prefix + "// Parameters");

        Iterator j = _pn.getAdg().getParameterList().iterator();
        while (j.hasNext()) {
            ADGParameter p = (ADGParameter) j.next();
            _printStream.println(_prefix + "int " + p.getName() + ";");
        }

	_printStream.println("");
        _prefixDec();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    private HashMap _relation2 = new HashMap();

    private CDProcessNetwork _pn = null;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

}
