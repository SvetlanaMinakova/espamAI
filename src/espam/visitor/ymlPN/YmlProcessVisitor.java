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
import espam.datamodel.graph.adg.ADGraph;
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
import espam.datamodel.parsetree.ParserNode;

import espam.main.UserInterface;
import espam.datamodel.LinearizationType;

import espam.visitor.CDPNVisitor;
//import espam.visitor.yapiPN.YapiStatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// Yml Process Visitor

/**
 *  This class ...
 *
 * @author  Hristo Nikolov, Todor Stefanov
 * @version  $Id: YmlProcessVisitor.java,v 1.2 2008/08/08 10:47:02 stefanov Exp $
 */

public class YmlProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the YmlProcessVisitor object
     */
    public YmlProcessVisitor() {
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {
        // Generate the individual processes
        try {

            _pn = x;
            _printStreamFunc = _openFile("aux_func", "h");
	    _printStreamFunc.println("// File automatically generated by ESPAM");
	    _printStreamFunc.println("");
            _printStreamFunc.println("#ifndef aux_func_H");
            _printStreamFunc.println("#define aux_func_H");
            _printStreamFunc.println("");
            _printStreamFunc.println("#include <math.h>");
	    _printStreamFunc.println("#include \"" + x.getName() + "_func.h\"");
            _printStreamFunc.println("");

            _writeParameters( x );
            _printStreamFunc.println("");

            _printStreamFunc.println("");
            _writeOperations();
            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

               CDProcess process = (CDProcess) i.next();
               String name = ((ADGNode) process.getAdgNodeList().get(0)).getName();


               _printStream = _openFile(name, "h");
               _writeHeaderFile( process );
               _printStream = _openFile(name, "cpp");
               _writeCppFile( process );
            }
        }
        catch( Exception e ) {
            System.out.println(" In Yml PN Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     *  Create a header file for this process.
     *
     * @param  x Description of the Parameter
     */
    private void _writeHeaderFile( CDProcess x ) {

        String name = ((ADGNode)x.getAdgNodeList().get(0)).getName();
       _printStream.println("// File automatically generated by ESPAM");
       _printStream.println("");
       _printStream.println("#ifndef " + name + "_H");
       _printStream.println("#define " + name + "_H");
       _printStream.println("");
       _printStream.println("#include \""+ name + "_Base.h\"");
       _printStream.println("");
       _printStream.println("class " + name + " : public " + name + "_Base {");
       _printStream.println("public:");
       _prefixInc();
       _printStream.println(_prefix + name + "(Id n, " + name + "_Ports *ports);");
       _printStream.println(_prefix + "virtual ~" + name + "();");
       _printStream.println("");

       _writeFunctions( x, true );

       _printStream.println("");
       _printStream.println(_prefix + "void main();");
       _prefixDec();
       _printStream.println("};");
       _printStream.println("#endif // " + name + "_H");
    }

    /**
     *  Create a cpp file for this process.
     *
     * @param  x Description of the Parameter
     */
    private void _writeCppFile( CDProcess x ) {

        String name = ((ADGNode)x.getAdgNodeList().get(0)).getName();
       _printStream.println("// File automatically generated by ESPAM");
       _printStream.println("");
       _printStream.println("#include \""+ name + ".h\"");
       _printStream.println("");
       _printStream.println(name + "::" + name + "(Id n, " + name + "_Ports *ports) : " + name + "_Base(n, ports) {}");
       _printStream.println(name + "::~" + name + "() {}");
       _printStream.println("");

       _writeFunctions( x, false );
       _printStream.println("");

       _writeMain( x );
    }

    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _writeParameters( CDProcessNetwork x ) {

        ADGraph adg = x.getAdg();
	Vector parList = adg.getParameterList();

	_printStreamFunc.println("");

	Iterator i = parList.iterator();
	while( i.hasNext() ) {

	   ADGParameter par = (ADGParameter) i.next();
	   _printStreamFunc.println("#define " + par.getName() + " " + par.getValue() );
	}
    }


    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _writeMain( CDProcess x) {

        String name = ((ADGNode)x.getAdgNodeList().get(0)).getName();

	_printStream.println("void " + name + "::main() {");
        _printStream.println("");
        _prefixInc();

        _writeIOArguments( x );

	// Print the Parse tree
        YmlStatementVisitor ymlVisitor = new YmlStatementVisitor(_printStream, name);
        ymlVisitor.setPrefix( _prefix );
        ymlVisitor.setOffset( _offset );

	ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        parserNode.accept(ymlVisitor);

        _printStream.println("} // main");
	_prefixDec();
    }



    /**
     * @param  x Description of the Parameter
     */
    private void _writeIOArguments( CDProcess x ) {

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

		// Find the gate corresponding to this funcArgument
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
			       String dType  = bv.getDataType();
		                  if (dType != null) {
		                     if (!dType.equals("")) {
		                        t = dType;
		                     }
		                  }
			       break;
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
			   String dType  = ((ADGVariable) port.getBindVariables().get(0)).getDataType();
		           if (dType != null) {
		              if (!dType.equals("")) {
		                  t = dType;
		              }
		           }
			   break;
	               }
                    }
		}

                _printStream.println(_prefix + t +" " + funcArgument + ";");
            }
        }

	_prefixDec();
	_printStream.println("");
    }

    private void _writeFunctions( CDProcess x, boolean header ) {

        String name = ((ADGNode)x.getAdgNodeList().get(0)).getName();
	String funcName = "";
        String csl = "";
	String t = "";
	_relation2 = new HashMap();

        //write func in aux file
        Iterator n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {

	    ADGNode node = (ADGNode) n.next();
            ADGFunction function1 = (ADGFunction) node.getFunction();

            funcName = function1.getName();

	    if(!_relation2.containsKey(funcName) ) {

	        if( header ) {  // print the functions in the header file
                   csl = "void _" + funcName + "( ";
	        } else { // print the functions in the cpp file
		   csl = "void " + name + "::_" + funcName + "( ";
	        }

                Iterator j2 = function1.getInArgumentList().iterator();
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
			           String dType  = bv.getDataType();
		                   if (dType != null) {
		                      if (!dType.equals("")) {
		                         t = dType;
		                      }
		                   }
			           break;
	                       }
		          }

                      }
		   }

		   csl += t + " " + arg.getName() + ", ";
                }

                j2 = function1.getOutArgumentList().iterator();
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

		    Iterator g = x.getGateList().iterator();
	            while ( g.hasNext() ) {
		       CDGate  gate = (CDGate) g.next();

		       Iterator p = gate.getAdgPortList().iterator();
		       while( p.hasNext() ) {
       	                  ADGPort port = (ADGPort) p.next();
		          String tmp = ((ADGVariable) port.getBindVariables().get(0)).getName() + port.getNode().getName();
		          if( funcArgument.equals( tmp ) ) {
			      String dType  = ((ADGVariable) port.getBindVariables().get(0)).getDataType();
		              if (dType != null) {
		                 if (!dType.equals("")) {
		                    t = dType;
		                 }
		              }
			      break;
	                  }
                       }
		    }

                    csl += t + " &" + arg.getName() + ", ";
                }

		if( header ) {  // print the functions in the header file
                   _printStream.println(csl.substring(0, (csl.length() - 2)) + " );");
	        } else { // print the functions in the cpp file
		   _printStream.println(csl.substring(0, (csl.length() - 2)) + " ) {");
		   _prefixInc();

		   //-------- print the initial function call ------------------------
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

                   _printStream.println("    " + csl.substring(0, (csl.length() - 2)) + " );");
		   //-------- END print of the initial function call ------------------------

   		   _printStream.println("}");
		   _prefixDec();
		   _printStream.println("");
		}
             }
            _relation2.put(funcName, "");
        }
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
	   type = ((ADGEdge)channel.getAdgEdgeList().get(0)).getFromPort().getIOVariable().getDataType();

	   if( !type.equals("") ) {
	       String s = "typedef " + type + " t" + channel.getName()+";";
               _printStreamFunc.println( s );
           } else {
               String s = "typedef char t" + channel.getName() + ";";
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
                ui.getFileName() + "/app/" + fileName + "." + extension;
        } else {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getOutputFileName() + "/app/" + fileName + "." + extension;
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    private Map _relation2 = null;

    private CDProcessNetwork _pn = null;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

}
