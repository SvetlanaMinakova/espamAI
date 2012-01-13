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

package espam.visitor.hdpc;

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

import espam.datamodel.parsetree.ParserNode;

import espam.datamodel.domain.Polytope;
import espam.datamodel.domain.ControlExpression;

import espam.main.UserInterface;
import espam.datamodel.LinearizationType;

import espam.utils.symbolic.expression.Expression;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// Hdpc Process Visitor

/**
 *  This class ...
 *
 * @author  Hristo Nikolov,Todor Stefanov
 * @version  $Id: HdpcProcessVisitor.java,v 1.4 2012/01/13 17:30:04 nikolov Exp $
 */

public class HdpcProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the HdpcProcessVisitor object
     */
    public HdpcProcessVisitor() {
    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {
        // Generate the aux_func.h and the files for the individual processes
        try {

            _pn = x;
            _printStreamFunc = _openFile("aux_func", "h");
	    _printStreamFunc.println("// File automatically generated by ESPAM");
            _printStreamFunc.println("#ifndef " + "aux_func_H");
            _printStreamFunc.println("#define " + "aux_func_H");
            _printStreamFunc.println("");
            _printStreamFunc.println("#include <math.h>");
            _printStreamFunc.println("#include \"" + x.getName() + "_func.h\"");
            _printStreamFunc.println("");

	// define cpu core numbers
/*	    _printStreamFunc.println("// CORES=0 means that the OS decides to which core to map a thread at runtime");
	    _printStreamFunc.println("#define CORES 0x0");
	    _printStreamFunc.println("#define CORE1 0x1");
	    _printStreamFunc.println("#define CORE2 0x2");
	    _printStreamFunc.println("#define CORE3 0x4");
	    _printStreamFunc.println("#define CORE4 0x8");
	    _printStreamFunc.println("#define CORE5 0x10");
	    _printStreamFunc.println("#define CORE6 0x20");
	    _printStreamFunc.println("#define CORE7 0x40");
	    _printStreamFunc.println("#define CORE8 0x80");
*/
	    _printStreamFunc.println("// CPU_CORES<>() means that the OS decides to which core to map a thread at runtime");
	    _printStreamFunc.println("#define CORES hdpc::CPU_CORES<>()");
	    _printStreamFunc.println("#define CORE1 hdpc::CPU_CORES<1>()");
	    _printStreamFunc.println("#define CORE2 hdpc::CPU_CORES<2>()");
	    _printStreamFunc.println("#define CORE3 hdpc::CPU_CORES<3>()");
	    _printStreamFunc.println("#define CORE4 hdpc::CPU_CORES<4>()");
	    _printStreamFunc.println("#define CORE5 hdpc::CPU_CORES<5>()");
	    _printStreamFunc.println("#define CORE6 hdpc::CPU_CORES<6>()");
	    _printStreamFunc.println("#define CORE7 hdpc::CPU_CORES<7>()");
	    _printStreamFunc.println("#define CORE8 hdpc::CPU_CORES<8>()");
	    _printStreamFunc.println("");

	// some defines
	    _printStreamFunc.println("// Platform defines");
	    _printStreamFunc.println("#define CPU hdpc::platform::CPU");
	    _printStreamFunc.println("#define DISK hdpc::platform::DISK");
	    _printStreamFunc.println("#define CUDA hdpc::platform::CUDA");
	    _printStreamFunc.println("");

	    _printStreamFunc.println("// Channel locking defines");
	    _printStreamFunc.println("#define LOCK_FREE hdpc::channel::lock::LOCK_FREE");
	    _printStreamFunc.println("#define SYNC_FREE hdpc::channel::lock::SYNC_FREE");
	    _printStreamFunc.println("#define SPIN hdpc::channel::lock::SPIN");
	    _printStreamFunc.println("#define SEMAPHORE hdpc::channel::lock::SEMAPHORE");
	    _printStreamFunc.println("#define SPIN_ACQUIRE hdpc::channel::lock::SPIN_ACQUIRE");
	    _printStreamFunc.println("");

            _writeChannelTypes();
            _printStreamFunc.println("");
	    _writeParameter(x);

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

                CDProcess process = (CDProcess) i.next();

                _printStream = _openFile(process.getName(), "hpp");
		_printStream.println("// File automatically generated by ESPAM");
                hdpcProcess( process );
            }

//          write operations
//            _printStreamFunc.println("static inline double min( double a, double b ) {if ( a>=b ) return b; else return a;}");
//            _printStreamFunc.println("static inline double max( double a, double b ) {if ( a>=b ) return a; else return b;}");
            _printStreamFunc.println("static inline int ddiv( double a, double b ) {" + 
				     "return ( (int) (((a)<0) ? ((a)-(b)+1)/(b) : (a)/(b)) );}");
	    _printStreamFunc.println("static inline int mod( double a, double b ) {return (int)fmod(a, b);}");
	    _printStreamFunc.println("static inline int floor1(int i) {return i;}");
            _printStreamFunc.println("static inline int ceil1(int i) {return i;}");

            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");

        }
        catch( Exception e ) {
            System.out.println(" In Hdpc PN Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Declare the public parameters
     * @param  x Description of the Parameter
     */
    private void _writeParameter(CDProcessNetwork x) {
	    _printStreamFunc.println("// Parameters");
        Iterator j = _pn.getAdg().getParameterList().iterator();
        while (j.hasNext()) {
            ADGParameter p = (ADGParameter) j.next();
	        _printStreamFunc.println(_prefix + "#define " + p.getName() + 
				     " " + p.getValue());
        }
        _printStreamFunc.println("");
    }


    /**
     *  Create a Hdpc PN process for this process.
     *
     * @param  x Description of the Parameter
     */
    public void hdpcProcess( CDProcess x ) {

        _writeIncludes( x );
 
//        _printStream.println("void " + x.getLevelUpProcess().getName() + x.getName() + "(Process &proc) {");

 	_printStream.println("");
 	_printStream.println("class p_" + x.getName() + "_function: public " + x.getName() + "_t::EXEC {");
        _prefixInc(2);
	_printStream.println(_prefix + "public:");
        _prefixInc(2);
	_printStream.println(_prefix + "void operator()(" + x.getName() + "_t& proc) {");

//        _writeFunctionArguments( x );
	_writeLocalVariables( x );

        // Print the Parse tree
        HdpcStatementVisitor hsvisitor = new HdpcStatementVisitor(_printStream, x);
        hsvisitor.setPrefix( _prefix );
//        hsvisitor.setOffset( _offset );

	ParserNode parserNode = (ParserNode) x.getSchedule().get(0);
        parserNode.accept(hsvisitor);

        _printStream.println(_prefix + "}");
        _printStream.println("};");
        _printStream.println("");
        _prefixDec(4);
	_writeFunctionsInAuxFile( x );
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

	_prefixInc();

        Iterator n = x.getAdgNodeList().iterator();
        while( n.hasNext() ) {
            ADGNode node = (ADGNode) n.next();
	    ADGFunction function = (ADGFunction) node.getFunction();

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

			// Find the gate corresponding to this funcArgument
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

			// Find the dimensions in case the local variable is an array
			Iterator j3 = bindVar.getIndexList().iterator();
			while( j3.hasNext() ) {
			   Expression exp = (Expression) j3.next();
			// Do some expression computations here
			   String arrSize = "";
			   dimension += "[" + arrSize + "]";
			}	
			varName += dimension;
	
			// Avoid duplicating declarations 
			// (unique names for the hash function are needed in case of merging)
			if ( !tmp.containsKey(varName+node.getName()) ) {
			   tmp.put(varName+node.getName(), "");
			   String decString = _prefix + t + " " + varName;
			   
			   // sort the variables into input arguments, output arguments, and additional variables
			   int counter = 0;
			   Iterator j4 = function.getInArgumentList().iterator();
			   while( j4.hasNext() ) {
				ADGVariable arg = (ADGVariable) j4.next();
				String funcArgument = arg.getName();
				if( funcArgument.equals( varName ) ) {
//					inArguments.add( decString  + node.getName() + ";" );
					inArguments.add( decString  + ";" );
					counter++;
				}
			   }
			   if( counter==0 ) {
				j4 = function.getOutArgumentList().iterator();
				while( j4.hasNext() ) {
					ADGVariable arg = (ADGVariable) j4.next();
					String funcArgument = arg.getName();
					if( funcArgument.equals( varName ) ) {
//						outArguments.add( decString  + node.getName() + ";" );
						outArguments.add( decString + ";" );
						counter++;
					}
	
				}
			   }
			   if( counter==0 ) { 
				miscVariables.add( decString + ";" );
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

// should we use 		if ( !tmp.containsKey(expName+node.getName()) ) {   ???
				if ( !tmp.containsKey(expName) ) {
				      tmp.put(expName, "");

				      String decString = _prefix + "int " + expName + ";"; 
				      miscVariables.add( decString );
				}

			}


		}
            }

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

			// Find the dimensions in case the local variable is an array
			Iterator j3 = bindVar.getIndexList().iterator();
			while( j3.hasNext() ) {
			   Expression exp = (Expression) j3.next();
			// Do some expression computations here
			   String arrSize = "";
			   dimension += "[" + arrSize + "]";
			}	
			varName += dimension;
		
			// Avoid duplicating declarations
			// (unique names for the hash function are needed in case of merging)
			if ( !tmp.containsKey(varName+node.getName()) ) {
			   tmp.put(varName+node.getName(), "");
			   String decString = _prefix + t + " " + varName;
			   
			   // sort the variables into input arguments, output arguments, and additional variables
			   int counter = 0;
			   Iterator j4 = function.getInArgumentList().iterator();
			   while( j4.hasNext() ) {
				ADGVariable arg = (ADGVariable) j4.next();
				String funcArgument = arg.getName();
				if( funcArgument.equals( varName ) ) {
//					inArguments.add( decString  + node.getName() + ";" );
					inArguments.add( decString + ";" );
					counter++;
				}
			   }
			   if( counter==0 ) {
				j4 = function.getOutArgumentList().iterator();
				while( j4.hasNext() ) {
					ADGVariable arg = (ADGVariable) j4.next();
					String funcArgument = arg.getName();
					if( funcArgument.equals( varName ) ) {
//						outArguments.add( decString  + node.getName() + ";" );
						outArguments.add( decString + ";" );
						counter++;
					}
	
				}
			   }
			   if( counter==0 ) { 
				miscVariables.add( decString + ";" );
			   }

			}
            }

	    //---------------------------------------------------------------------------------------------------------------------------
	    // Add an input function argument which is not bound to any port (happens in case loop iterators are propagated to functions)
	    //---------------------------------------------------------------------------------------------------------------------------
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

	    //------------------------------------------------------------------------------------------------
	    // Add an output function argument which is not bound to any port (happens in case of a sink node)
	    //------------------------------------------------------------------------------------------------
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

	} // while nodes

        //-------------------------------
        // print the sorted declarations
        //-------------------------------
	if( inArguments.size()>0 ) {
		_printStream.println(_prefix + "// Function's Input Arguments ");
		n = inArguments.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
		_printStream.println("");
	}
	if( outArguments.size()>0 ) {
		_printStream.println(_prefix + "// Function's Output Arguments ");
		n = outArguments.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
		_printStream.println("");
	}
	if( miscVariables.size()>0 ) {
		_printStream.println(_prefix + "// Additional Local Variables ");
		n = miscVariables.iterator();
		while( n.hasNext() ) {
			String decl = (String) n.next();
			_printStream.println( decl );
		}
		_printStream.println("");
	}
	_prefixDec();
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

        //write func wrapper in aux file
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

		   //-------- print the initial function call in the wrapper ------------------------
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
		   //-------- END print of the initial function call in the wrapper ------------------------

                   _printStreamFunc.println("}");
		   _printStreamFunc.println("");
                }
                _relation2.put(funcName, "");
	    }
        }
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
	_printStream.println("");
        _printStream.println(_prefix + "// Local Variables ");

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

		       csl +=  t + " &" + arg.getName() + ", ";
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

//        _printStream.println("#ifndef " + x.getName() + "_H");
//        _printStream.println("#define " + x.getName() + "_H");
//        _printStream.println("");
//
//        _printStream.println("#include \"../hdpc/process.h\"");
//        _printStream.println("#include \"aux_func.h\"");

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
//        _printStream.println("");
    }

    /**
     *  Description of the Method
     */
    private void _writeChannelTypes() {

        CDChannel channel;
	String type;

	_printStreamFunc.println("// Channel Types");

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

       if (ui.getOutputFileName() == "") {
            fullFileName = ui.getBasePath() + "/" + ui.getFileName() + "_hdpc/src" + "/" + fileName + "." + extension;
        } else {
            fullFileName = ui.getBasePath() + "/" + ui.getOutputFileName() + "_hdpc/src" + "/" + fileName + "." + extension;
        }

        System.out.println(" -- OPEN FILE: " + fullFileName);

        try {
            if( fileName.equals("") ) {
                printStream = new PrintStream(System.out);
            } else {
                OutputStream file = null;
                file = new FileOutputStream(fullFileName);
                printStream = new PrintStream(file);
            }
        } catch( SecurityException e ) {
            System.out.println(" Security Issue: " + e.getMessage());
        }

        return printStream;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    private HashMap _relation2 = new HashMap();

    private CDProcessNetwork _pn = null;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

}
