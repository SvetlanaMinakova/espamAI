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
import java.util.Vector;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGInVar;
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
import espam.datamodel.mapping.MProcess;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;
import espam.datamodel.platform.host_interfaces.ML605;

import espam.datamodel.parsetree.ParserNode;

import espam.datamodel.domain.Polytope;
import espam.datamodel.domain.ControlExpression;

import espam.utils.symbolic.expression.Expression;

import espam.main.UserInterface;

import espam.visitor.CDPNVisitor;

import espam.datamodel.LinearizationType;

//////////////////////////////////////////////////////////////////////////
//// XpsDynamicFreeRTOSProcessVisitor

/**
 *  Visitor to generate code scheduled using FreeRTOS 
 *
 * @author  Mohamed Bamakhrama
 * @version  $Id: XpsDynamicFreeRTOSProcessVisitor.java,v 1.3 2012/04/27 11:36:47 mohamed Exp $
 */

public class XpsDynamicFreeRTOSProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the XpsProcessVisitor object
     */
    public XpsDynamicFreeRTOSProcessVisitor( Mapping mapping, PrintStream printStream, PrintStream printStreamFunc, Map relation ) {
	_mapping = mapping;
	_printStream = printStream;
	_printStreamFunc = printStreamFunc;
	_relation2 = relation;
	_ui = UserInterface.getInstance();
	_targetBoard = _getBoard( mapping.getPlatform() );
    if(_ui.getADGFileNames().size() > 1) {
        _bMultiApp = true;
    }	
    }

    /**
     *  Create a Xps PN process with static scheduling for this process.
     *
     * @param  x process to generate code for
     */
    public void visitComponent( CDProcess x ) {
        _writeIncludes( x );

	int pos = 0;
	Iterator i = x.getSchedule().iterator();
	while ( i.hasNext() ) {
		String threadName = "thread" + (pos + 1);
		_printStream.println(_prefix + "void " + threadName + "(void *arg) {");
		_prefixInc();
		

        _writeLocalVariables(x, pos++);
		_writeThread( x, (ParserNode)i.next() );
		
		_prefixDec();
        _printStream.println(_prefix + "} // for (;;) ");

		_prefixDec();
		_printStream.println(_prefix + "} // " + threadName);

		_prefixDec();
		_printStream.println("");
	}
	
	_writeThreadMain( x );
	_writeMain();
       
        _writeFunctionsInAuxFile( x );
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * @param  x Description of the Parameter
     */
    private void _writeLocalVariables( CDProcess x, int index ) {

	    String varName = "";
	    String dimension = "";
	    String t = "";
	    HashMap  tmp = new HashMap();
	    Vector inArguments = new Vector();
	    Vector outArguments = new Vector();
	    Vector miscVariables = new Vector();
	    Vector selfEdgeVariables = new Vector();
	    Vector tempVector = new Vector();
	    Iterator n;

	    _prefixInc();

    	ADGNode node = (ADGNode) x.getAdgNodeList().get(index);
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
                            } else { // it is an 'enamble' variable
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
            // This happens in case loop iterators are propagated to functions,
            // or in case of initialization, e.g.., in_0ND_0 = 0
        //-----------------------------------------------------------------
        Iterator f = function.getInArgumentList().iterator();
            while( f.hasNext() ) {
                ADGVariable arg = (ADGVariable) f.next();

	    dimension = "";
       	varName = arg.getName();
	    String dataType = arg.getDataType();

	    if ( !tmp.containsKey(varName+node.getName()) ) {

	        String funcArgDeclaration;
	        // Avoid putting suffix to the loop iterators and parameters
	        if( varName.contains("in") ) {
        		funcArgDeclaration = _prefix + dataType + " " + arg.getName() + suffix + ";";
	        } else {
        		funcArgDeclaration = _prefix + dataType + " " + arg.getName() + ";";
	        }
    //                     String funcArgDeclaration = _prefix + dataType + " " + arg.getName() + ";";
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
                    String funcArgDeclaration = _prefix + dataType + " " + arg.getName() + suffix + ";";
		        outArguments.add( funcArgDeclaration );
                }
        }

        //-----------------------------------------------------------
        // Find the self-channels
        // print local variables, to be used instead of FIFO channels
        //-----------------------------------------------------------
        Iterator g = x.getInGates().iterator();
        while( g.hasNext() ) {
            CDGate gate = (CDGate) g.next();
            CDChannel ch = (CDChannel) gate.getChannel();
            String selfEdgeDeclaration="";

            if( ch.isSelfChannel() ) {
                if( ch.getMaxSize()==1 ) {
                     selfEdgeDeclaration = _prefix + "t" + ch.getName() + " var_" + ch.getName() + ";";
                } else {
                     selfEdgeDeclaration = _prefix + "t" + ch.getName() + " var_" + ch.getName() + "[" + ch.getMaxSize() + "];";
                     // we need to declare also the read and write counters used as array indexes.
                     selfEdgeDeclaration += "\n" + _prefix + "int rd_" + ch.getName() + "=-1;";
                     selfEdgeDeclaration += "\n" + _prefix + "int wr_" + ch.getName() + "=-1;";
                }
    		selfEdgeVariables.add( selfEdgeDeclaration );                 
            } 
        }

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
	
	 MProcess mp = getMProcess(node);
	
	_printStream.println(_prefix + "portTickType xLastWakeTime;");
	_printStream.println(_prefix + "const portTickType xFrequency = " + mp.get_period()  + ";");
    _printStream.println(_prefix + "xLastWakeTime = xTaskGetTickCount();\n");
    
    _printStream.println(_prefix + "for (;;) { ");
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




    /**
     * @param  x Description of the Parameter
     */
    private void _writeIncludes( CDProcess x ) {
	_printStream.println("#include <FreeRTOS.h>");
	_printStream.println("#include <task.h>");
	_printStream.println("#include \"platform.h\"");
	_printStream.println("#include \"MemoryMap.h\"");
	_printStream.println("#include \"aux_func.h\"");
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
     *  write the body of the thread
     *
     * @param  x Process under which the thread runs
     * @param  thread node for which to generate the thread code
     */
    private void _writeThread( CDProcess x, ParserNode threadNode ) {
	_prefixInc();
	// Print the Parse tree
	XpsStatementVisitor xpsvisitor = new XpsStatementVisitor(_printStream, x, _mapping);
	xpsvisitor.setPrefix( _prefix );
	xpsvisitor.setOffset( _offset );
	
	threadNode.accept(xpsvisitor);
    }


    /**
     *  Write the thread_main function
     *
     * @param  x Process under which thread_main is generated
     */
    private void _writeThreadMain( CDProcess x ) {
	_printStream.println("void thread_main(void *arg) {");
	_prefixInc();
	
    // Initilaize the WR and RD counters of the SW FIFOs
    _printStream.println(_prefix + "// Initialize the WR and RD counters of the FIFOs to which the processor writes to");
    Iterator g = x.getOutGates().iterator();
    while( g.hasNext() ) {
        CDGate gate = (CDGate) g.next();
        CDChannel cdChannel = (CDChannel) gate.getChannel();

        ADGEdge edge = (ADGEdge)cdChannel.getAdgEdgeList().get(0);
        ADGPort port = edge.getFromPort();

        String eName = port.getNode().getName() + "_" + gate.getName() + "_" + cdChannel.getName();
        _printStream.println(_prefix + "*(" + eName + ") = *(" + eName + " + 1) = 0;");
    }	
	
		
	if( _targetBoard.equals("XUPV5-LX110T") || _targetBoard.equals("ML505") ) {
		_printStream.println("");
		_printStream.println(_prefix + _prefix + "while( *FIN_SIGNAL == 0 ) {};");
		_printStream.println("");
	}

	if( _ui.getDebuggerFlag() ) {
		_printStream.println(_prefix + "int clk_num;");
		_printStream.println(_prefix + "*clk_cntr = 0;");

	}

	_printStream.println("");

	
	for (int pos = 0; pos < x.getAdgNodeList().size(); pos++) {
	    ADGNode adgNode = (ADGNode)(x.getAdgNodeList().elementAt(pos));
	    MProcess mp = getMProcess(adgNode);
		_printStream.println(_prefix + "xTaskCreate( thread" + (pos + 1) + ", ( signed char * ) \"thread" + (pos + 1) + "\", configMINIMAL_STACK_SIZE, NULL, tskIDLE_PRIORITY + " + (mp.get_priority()) + ", NULL);");
	}
	
	_printStream.println("");
	_printStream.println(_prefix + "vTaskStartScheduler();");
	_printStream.println("");
	_printStream.println(_prefix + "for (;;);");
	
	if( _ui.getDebuggerFlag() ) {
		_printStream.println(_prefix + "clk_num = *clk_cntr;");
	}

	_printStream.println(_prefix + "*FIN_SIGNAL = (volatile long)0x00000001;");
	
	_prefixDec();
	_printStream.println(_prefix + "} // thread_main");
	
	_printStream.println("");	
    }
    
    private MProcess getMProcess(ADGNode x)
    {

        Iterator ii, jj;
        ii = _mapping.getProcessorList().iterator();
        while (ii.hasNext()) {
            MProcessor mProcessor = (MProcessor) ii.next();
            jj = mProcessor.getProcessList().iterator();
            while (jj.hasNext()) {
            	MProcess mProcess = (MProcess) jj.next();
            	String madgNodeName = mProcess.getNode().getName();
            	if (madgNodeName.equals(x.getName())) {
            		return mProcess;
            	}
            }
        }
        return null;
    }
    

    /**
     *  Write the main function
     *
     */
    private void _writeMain() {
	_printStream.println("int main() {");
	_prefixInc();
	_printStream.println(_prefix + "thread_main(NULL);");
	_printStream.println(_prefix + "return 0;");
	_prefixDec();
	_printStream.println(_prefix + "} // main");
	_printStream.println("");
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
            } else if( resource instanceof ML605 ) {
               board = "ML605";
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

    private boolean _bMultiApp = false;    
}
