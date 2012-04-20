/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2011 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.xps.cdpn;

import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.parsetree.statement.AssignStatement;
import espam.datamodel.parsetree.statement.SimpleAssignStatement;
import espam.datamodel.parsetree.statement.ControlStatement;
import espam.datamodel.parsetree.statement.ElseStatement;
import espam.datamodel.parsetree.statement.FifoMemoryStatement;
import espam.datamodel.parsetree.statement.ForStatement;
import espam.datamodel.parsetree.statement.IfStatement;
import espam.datamodel.parsetree.statement.OpdStatement;
import espam.datamodel.parsetree.statement.RootStatement;
import espam.datamodel.parsetree.statement.Statement;
import espam.datamodel.parsetree.statement.VariableStatement;
import espam.datamodel.parsetree.statement.LhsStatement;
import espam.datamodel.parsetree.statement.RhsStatement;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MFifo;

import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.CM_AXI;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.FifoReadPort;

import espam.visitor.StatementVisitor;

import espam.visitor.expression.CExpressionVisitor;
import espam.utils.symbolic.expression.Expression;

//////////////////////////////////////////////////////////////////////////
//// XpsStatementVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Todor Stefanov, Hristo Nikolov, Joris Huizer
 * @version  $Id: XpsStatementVisitor.java,v 1.11 2012/04/20 23:00:41 mohamed Exp $
 *      
 */

public class XpsStatementVisitor extends StatementVisitor {

    /**
     *  Constructor for the XpsStatementVisitor object
     *
     * @param  printStream Description of the Parameter
     * @param  name Description of the Parameter
     */
    public XpsStatementVisitor(PrintStream printStream, CDProcess process, Mapping mapping) {
        super();
        _mapping = mapping;
        _printStream = printStream;
        _process = process;
        _cExpVisitor = new CExpressionVisitor();
        _scheduleType = _mapping.getMProcessor( _process ).getScheduleType();
        _isDynamicSchedule = (_mapping.getMProcessor( _process ).getScheduleType() >= 1);
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Print a root statement in the correct format for C++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(RootStatement x) {
        _visitChildren(x);
    }

    /**
     *  Print a for statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(ForStatement x) {

        Expression ub = x.getUpperBound();
        Expression lb = x.getLowerBound();

        _printStream.println(_prefix + "for( int "
              + x.getIterator() + " =  ceil1(" +
              lb.accept( _cExpVisitor ) + "); " + x.getIterator() + " <= " +
              " floor1(" + ub.accept( _cExpVisitor ) + "); " + x.getIterator() +
              " += " + x.getStepSize() + " ) {");
                
        _prefixInc();
        _visitChildren(x);
        if (!_printDelay && _scheduleType == 2) {
            _printStream.println(_prefix + "vTaskDelayUntil( &xLastWakeTime, xFrequency );");
            _printDelay = true;
        }
        _prefixDec();

        _printStream.println(_prefix + "} // for " + x.getIterator());
    }

   /**
     *  Print an if statement in the correct format for c++
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(IfStatement x) {

    String str = "";
	int sign = x.getSign();
	Expression expression = x.getCondition();

	switch( sign ) {
	   case 0  : str = " == ";
	             break;
	   case 1  : str = " >= ";
	             break;
	   default : str = " <= ";
	             break;
        }

        _printStream.println(_prefix + "if( " +
                    expression.accept( _cExpVisitor ) + str + "0 ) {");

	_prefixInc();
        _visitChildren(x);
        _prefixDec();
        _printStream.println(_prefix + "}");
    }

    /**
     *  Print an else statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(ElseStatement x) {
        _printStream.print(_prefix);
        _printStream.println("else {");
        _prefixInc();
        _visitChildren(x);
        _prefixDec();
        _printStream.println(_prefix + "}");
    }

    /**
     *  Print an opd statement (Write primitives) in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(OpdStatement x) {
	
    	CDGate cdGate = (CDGate)_process.getGate(x.getGateName());
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

        Iterator i;
        String funName = "";
    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";
    	String eName = x.getNodeName() + "_" + x.getGateName() + "_" + t;
    	
    	MFifo mFifo = _mapping.getMFifo(cdChannel);
    	Fifo fifo = mFifo.getFifo();
    	
    	if( fifo.getLevelUpResource() instanceof MultiFifo ) {    	    	
	    if( _isDynamicSchedule ) {
		funName = "writeDynMF(";
	    } else {
		funName = "writeMF(";
  	    }

    	} else if( fifo.getLevelUpResource() instanceof CM_AXI ) {
    	    if (_scheduleType == 2) {
    	        funName = "writeSWF2(";
    	    } else {
        	    funName = "writeSWF(";
    	    }
              

        } else { // fifo is not MultiFifo
    		
            i = fifo.getPortList().iterator();
            Port wPort = null;
            while (i.hasNext()) {
                Port port = (Port) i.next();
                if ( port instanceof FifoWritePort ) {
                   wPort = port;
                }
            }   
            Link link = wPort.getLink();
        	
            i = link.getPortList().iterator();
            while (i.hasNext()) {
                Port port = (Port) i.next();
                if ( !(port.getResource() instanceof Fifo) ) {
                   wPort = port;
                }
            }
        	
            if( wPort.getResource() instanceof Processor ) {
            	funName = "writeFSL("; 
            }
            else {
		if( _isDynamicSchedule ) {
		    funName = "writeDyn(";
	        } else { // static scheduling
		    funName = "write(";	
		}
	    }
        }
           
//-------------------------------------------------------------------------------------
// We can implement the self-channels, in case of static schedule, as local variables.
// This feature is currently not used because, in order to be complete, 
// we need to remove also the HW implementation of these self-channels.
//-------------------------------------------------------------------------------------
/*  
       if( cdChannel.isSelfChannel() && !_isDynamicSchedule ) {
           if( cdChannel.getMaxSize()==1 ) {
               _printStream.print( _prefix + "var_" + cdChannel.getName() + " = " + x.getArgumentName() );      
           } else {
               String wr =  "wr_" + cdChannel.getName();
               _printStream.print( _prefix + "var_" + cdChannel.getName() + "[" + wr + "!=" + (cdChannel.getMaxSize()-1) + "?(++" + wr + "):(" + wr + "=0)] = " + x.getArgumentName() );      
           }
           i = x.getIndexList().iterator();
   	   while( i.hasNext() ) {
 	      Expression expression = (Expression) i.next();
	      _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
           }
           _printStream.println( "; // self-channel with size " + cdChannel.getMaxSize() );

        } else {
*/
           _printStream.print(_prefix + funName + eName + ", " + "&" + x.getArgumentName() );
           i = x.getIndexList().iterator();
   	   while( i.hasNext() ) {
	      Expression expression = (Expression) i.next();
	      _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
           }
           _printStream.print(", " + s);
	   if( fifo.getLevelUpResource() instanceof CM_AXI ) {
              _printStream.print(", size" + t);
              if (_scheduleType == 2) // FreeRTOS
                _printStream.print(", xLastWakeTime, xFrequency");
           } 
           _printStream.println(");");

    }

    /**
     *  Print an Assignment statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
      public void visitStatement(AssignStatement x) {
        Statement statement = null;
       	LhsStatement lhsStatement = (LhsStatement) x.getChild(0);
       	RhsStatement rhsStatement = (RhsStatement) x.getChild(1);

	if ( !x.getFunctionName().equals("") ) {

	     _printStream.println("");
             _printStream.print(_prefix + "_" + x.getFunctionName() + "(");

            Iterator i = rhsStatement.getChildren();
            while( i.hasNext() ) {
                 VariableStatement var = (VariableStatement) i.next();
                 if( i.hasNext() ) {
                     _printStream.print(var.getVariableName() + ", ");
                 } else {
                     _printStream.print(var.getVariableName() );
                 }
            }

            // The sequence continues.
           if( lhsStatement.getNumChildren() > 0 && rhsStatement.getNumChildren() > 0) {
                _printStream.print(", ");
           }

           i = lhsStatement.getChildren();
           while( i.hasNext() ) {
               VariableStatement var = (VariableStatement) i.next();
               if( i.hasNext() ) {
                   _printStream.print(var.getVariableName() + ", ");
               } else {
                   _printStream.print(var.getVariableName() );
               }
           }
           _printStream.print(");");
           _printStream.println("");

	} else {

	     VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
	     VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
             _printStream.print(_prefix + outArg.getVariableName() + " = "  +
	                                 inArg.getVariableName() + ";"           );
             _printStream.println("");
	}
    }


    /**
     *  Print an assign statement in the correct format for c++.
     *
     * @param  x The simple statement that needs to be rendered.
     */
    public void visitStatement(SimpleAssignStatement x) {

        _printStream.print(_prefix + x.getLHSVarName() );

	Iterator i = x.getIndexListLHS().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}

        _printStream.print(" = " + x.getRHSVarName() );

	i = x.getIndexListRHS().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}

	_printStream.println(";\n");
    }


    /**
     *  Print a Control statement in the correct format for c++.
     *
     * @param  x The control statement that needs to be rendered.
     */
    public void visitStatement(ControlStatement x) {
        Expression expression = x.getNominator();
        if( x.getDenominator() == 1 ) {
            _printStream.println(_prefix + "int "
                    + x.getName() + " = "
                    + expression.accept(_cExpVisitor) + ";");
        } else {
            _printStream.println(_prefix + "int "
                    + x.getName() + " = ("
                    + expression.accept(_cExpVisitor) + ")/" +
                    x.getDenominator() + ";");
        }
        _visitChildren(x);
    }

    /**
     *  Print the Fifo Memory (Read primitives) Statement in the correct format for c++
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(FifoMemoryStatement x) {

    	CDGate cdGate = (CDGate)_process.getGate(x.getGateName());
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

        Iterator i;
        String funName = "";
    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";
    	String eName = x.getNodeName() + "_" + x.getGateName() + "_" + t;

    	MFifo mFifo = _mapping.getMFifo(cdChannel);
    	Fifo fifo = mFifo.getFifo();

    	if( fifo.getLevelUpResource() instanceof MultiFifo ) {
	     if( _isDynamicSchedule ) {
		    funName = "readDynMF(";
	     } else {
		    funName = "readMF(";	

	     }

    	} else if( fifo.getLevelUpResource() instanceof CM_AXI) {
                if (_scheduleType == 2) {
                    funName = "readSWF2(";
                } else {
                    funName = "readSWF(";
                }
        } else { // not MultiFifo

            i = fifo.getPortList().iterator();
            Port rPort = null;
            while (i.hasNext()) {
                Port port = (Port) i.next();
                if ( port instanceof FifoReadPort ) {
                   rPort = port;
                }
            }
            Link link = rPort.getLink();

            i = link.getPortList().iterator();
            while (i.hasNext()) {
                Port port = (Port) i.next();
                if ( !(port.getResource() instanceof Fifo) ) {
                   rPort = port;
                }
            }

            if( rPort.getResource() instanceof Processor ) {
            	funName = "readFSL(";
            }
            else if( rPort.getResource() instanceof Crossbar ) {
// Why is this here???-----------------------------------------------------------------------------------
		if( _isDynamicSchedule ) { 
			funName = "readDynMF(";
		} else {
			funName = "readMF(";
		}
//-------------------------------------------------------------------------------------------------------
            } else { // p2p FIFO port
//		if( _mapping.getMProcessor( _process ).getScheduleType() == 1 ) { // dynamic scheduling
		if( _isDynamicSchedule ) { 
			funName = "readDyn(";
		} else { // static scheduling
			funName = "read(";
		}
            }
	}

//-------------------------------------------------------------------------------------
// We can implement the self-channels, in case of static schedule, as local variables.
// This feature is currently not used because, in order to be complete, 
// we need to remove also the HW implementation of these self-channels.
//-------------------------------------------------------------------------------------
/*
         if( cdChannel.isSelfChannel() && !_isDynamicSchedule ) {
           if( cdChannel.getMaxSize()==1 ) {

              ADGVariable bindVar = x.getArgumentList().get(0);
              _printStream.print(_prefix + bindVar.getName() ); 
     
              Iterator j = bindVar.getIndexList().iterator();
	      while( j.hasNext() ) {
		  Expression expression = (Expression) j.next();
		  _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	      }
              _printStream.println(" = var_" + cdChannel.getName() + "; // self-channel with size 1" );

           } else {
              String rd =  "rd_" + cdChannel.getName();
              // for every binding variable, we need a read from a fifo
	      i = x.getArgumentList().iterator();
	      while( i.hasNext() ) {

		  ADGVariable bindVar = (ADGVariable) i.next();
                  _printStream.print(_prefix + bindVar.getName() ); 

		  Iterator j = bindVar.getIndexList().iterator();
		  while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		  }
                  _printStream.println(" = var_" + cdChannel.getName() + "[" + rd + "!=" + (cdChannel.getMaxSize()-1) + "?(++" + rd + "):(" + rd + "=0)]; // self-channel with size " + cdChannel.getMaxSize() );
	      }
          }
        } else {       
*/
            // for every binding variable, we need a read from a fifo
	    i = x.getArgumentList().iterator();
	    while( i.hasNext() ) {
		ADGVariable bindVar = (ADGVariable) i.next();

	       _printStream.print(_prefix + funName + eName + ", " + "&" + bindVar.getName() );

		Iterator j = bindVar.getIndexList().iterator();
		while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		}

// 		_printStream.println(", " + s + ");");
 	        _printStream.print(", " + s);
                if( fifo.getLevelUpResource() instanceof CM_AXI ) {
// 		       _printStream.println(", " + s + ", size" + t + ");");
		       _printStream.print(", size" + t);
                 if (_scheduleType == 2) // FreeRTOS
                    _printStream.print(", xLastWakeTime, xFrequency");
                }
               _printStream.println(");");
//--------------------------------------- 
	    }
//        }       

        _prefixInc();
        _visitChildren(x);
        _prefixDec();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _cExpVisitor = null;
    
    private Mapping _mapping = null;
    
    private CDProcess _process = null;
    
    private int _scheduleType; // Save the schedule type
    
    private boolean _isDynamicSchedule;
    
    private boolean _printDelay = false; // This is a hack to print vTaskDelayUntil when FreeRTOS is used
        
}
