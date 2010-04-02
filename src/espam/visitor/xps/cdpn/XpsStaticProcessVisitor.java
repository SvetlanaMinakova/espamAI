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

package espam.visitor.xps.cdpn;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;

import espam.datamodel.parsetree.ParserNode;

import espam.main.UserInterface;

import espam.visitor.CDPNVisitor;

import espam.datamodel.LinearizationType;

//////////////////////////////////////////////////////////////////////////
//// XpsStaticProcessVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Hristo Nikolov,Todor Stefanov, Joris Huizer
 * @version  $Id: XpsStaticProcessVisitor.java,v 1.3 2010/04/02 12:21:25 nikolov Exp $
 */

public class XpsStaticProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the XpsProcessVisitor object
     */
    public XpsStaticProcessVisitor( Mapping mapping, PrintStream printStream, PrintStream printStreamFunc, Map relation ) {
    	_mapping = mapping;
	_printStream = printStream;
	_printStreamFunc = printStreamFunc;
	_relation2 = relation;
    	_ui = UserInterface.getInstance();
	_targetBoard = _getBoard( mapping.getPlatform() );
    }

    /**
     *  Create a Xps PN process with static scheduling for this process.
     *
     * @param  x process to generate code for
     */
    public void visitComponent( CDProcess x ) {
        _writeIncludes( x );
        _printStream.println("int main (){");
	_prefixInc();

	if( _targetBoard.equals("XUPV5-LX110T") || _targetBoard.equals("ML505") ) {
		_printStream.println("");
		_printStream.println(_prefix + _prefix + "while( *FIN_SIGNAL == 0 ) {};");
		_printStream.println("");
	}

	if( _ui.getDebuggerFlag() ) {
		_printStream.println("");
		_printStream.println(_prefix + "int clk_num;");
		_printStream.println(_prefix + "*clk_cntr = 0;");
		_printStream.println("");
	}

	_writeFunctionArguments(x);

	_writeMain( x );
        
        _prefixDec();
        _printStream.println("");
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
	                           t = "t" + gate.getChannel().getName();
	                       }
		           }

                      }
		   }

		   csl += t + " &" + arg.getName() + ", ";
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
                    //csl += arg.getName() + ", ";
                }

                _printStreamFunc.println("    " + csl.substring(0, (csl.length() - 2)) + " );");
		//-------- END print of the initial function call in the wrapper ------------------------

                _printStreamFunc.println("}");
		_printStreamFunc.println("");
            }
            _relation2.put(funcName, "");
        }
        _printStream.println("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * @param  x Description of the Parameter
     */
    private void _writeIncludes( CDProcess x ) {
    	_printStream.println("#include \"xparameters.h\"");
        _printStream.println("#include \"stdio.h\"");
        _printStream.println("#include \"stdlib.h\"");
        _printStream.println("#include \"aux_func.h\"");
        _printStream.println("#include \"MemoryMap.h\"");
        _printStream.println("");
 
        Iterator n = x.getGateList().iterator();
        while( n.hasNext() ) {
            CDGate gate = (CDGate) n.next();
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
        _printStream.println("");
    }

    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _writeMain( CDProcess x) {
    	_prefixInc();
        // Print the Parse tree
        XpsStatementVisitor xpsvisitor = new XpsStatementVisitor(_printStream, x, _mapping);
        xpsvisitor.setPrefix( _prefix );
        xpsvisitor.setOffset( _offset );

	ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        parserNode.accept(xpsvisitor);

        _printStream.println("");

        if( _ui.getDebuggerFlag() ) {
        	_printStream.println(_prefix + "clk_num = *clk_cntr;");
        }

        _printStream.println(_prefix + "*FIN_SIGNAL = (volatile long)0x00000001;");
        _prefixDec();
        _printStream.println(_prefix + "} // main");
    }

    /**
     *  Get the target FPGA board
     *  @param platform
     */
    private String _getBoard( Platform x ) {
	
	String board = "";
        Iterator j = x.getResourceList().iterator();
    	while (j.hasNext()) {
            Resource resource = (Resource)j.next();
            if( resource instanceof ADMXRCII ) {
               board = "ADM-XRC-II";
            } else if( resource instanceof ADMXPL ) {
               board = "ADM-XPL";
            } else if( resource instanceof XUPV5LX110T ) {
               board = "XUPV5-LX110T";
            } else if( resource instanceof ML505 ) {
               board = "ML505";
            }
        }  

	return board;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     * Repository directory for the source codes
     */
    private static String _codeDir = "";

    /**
     *  The UserInterface
     */
    private UserInterface _ui = null;

    private Mapping _mapping = null;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

    private Map _relation2 = null;

    private String _targetBoard = "";

}
