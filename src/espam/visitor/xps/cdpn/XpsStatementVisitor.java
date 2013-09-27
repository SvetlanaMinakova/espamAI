/*******************************************************************\
  * 
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
import espam.datamodel.graph.adg.ADGInVar;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGNode;
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

import espam.main.UserInterface;

//////////////////////////////////////////////////////////////////////////
//// XpsStatementVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Todor Stefanov, Hristo Nikolov, Joris Huizer
 * @version  $Id: XpsStatementVisitor.java,v 1.16 2012/06/05 12:30:17 tzhai Exp $
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
        
        _ui = UserInterface.getInstance();
        if(_ui.getADGFileNames().size() > 1) {
            _bMultiApp = true;
        }        
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
            //_printStream.println(_prefix + "vTaskDelayUntil( &xLastWakeTime, xFrequency );");
            _printStream.println(_prefix + "delayCheckDeadline( &xLastReleaseTime, xPeriod );");
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
        
        String suffix = "";
        if( _bMultiApp  && !(x.getArgumentName().contains("dc")) ) {
            suffix = "_" + x.getNodeName();
        }
        
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
                funName = "writeSWF_Dyn2(";
            } else if (_scheduleType == 1) {
                funName = "writeSWF_Dyn1(";
            } 
            else {
                funName = "writeSWF(";
            }
        } else { // fifo is neither MultiFifo, nor AXI
            
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
            } else {
                if( _isDynamicSchedule ) {
                    funName = "writeDyn(";
                } else { // static scheduling
                    funName = "write("; 
                }
            }
        } // end MultiFifo, CM_AXI, ...
        
        if (_scheduleType == 0 && cdChannel.isSelfChannel() == true && Options.USE_LOCAL_VAR_FIFO == true){
            //-------------------------------------------------------------------------------------
            // We can implement the self-channels, in case of static schedule, as local variables.
            // This feature is currently not used because, in order to be complete, 
            // we need to remove also the HW implementation of these self-channels.
            //-------------------------------------------------------------------------------------
            if( cdChannel.getMaxSize()==1 ) {
                _printStream.print( _prefix + "var_" + cdChannel.getName() + " = " + x.getArgumentName() + suffix );      
            } else {
                String wr =  "wr_" + cdChannel.getName();
                _printStream.print( _prefix + "var_" + cdChannel.getName() + "[" + wr + "!=" + (cdChannel.getMaxSize()-1) + "?(++" + wr + "):(" + wr + "=0)] = " + x.getArgumentName() + suffix );      
            }
            i = x.getIndexList().iterator();
            while( i.hasNext() ) {
                Expression expression = (Expression) i.next();
                _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
            }
            _printStream.println( "; // self-channel with size " + cdChannel.getMaxSize() );
        } else { // FIFO is just implemented as it is (without using local variable)
            _printStream.print(_prefix + funName + eName + ", " + "&" + x.getArgumentName() + suffix );
            i = x.getIndexList().iterator();
            while( i.hasNext() ) {
                Expression expression = (Expression) i.next();
                _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
            }
            _printStream.print(", " + s);
            if( fifo.getLevelUpResource() instanceof CM_AXI ) {
                _printStream.print(", size" + t);
                if (_scheduleType == 2) // FreeRTOS
                    _printStream.print(", xLastReleaseTime, xPeriod");
            } 
            _printStream.println(");");
        }  
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
        
        String suffix = "";
        if( _bMultiApp ) {
            suffix = "_" + x.getNodeName();
        }
        
        if ( !x.getFunctionName().equals("") ) {
            
            _printStream.println("");
            _printStream.print(_prefix + "_" + x.getFunctionName() + "(");
            
            Iterator i = rhsStatement.getChildren();
            while( i.hasNext() ) {
                VariableStatement var = (VariableStatement) i.next();
                if( i.hasNext() ) {
                    _printStream.print(var.getVariableName() + suffix + ", ");
                } else {
                    _printStream.print(var.getVariableName() + suffix );
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
                    _printStream.print(var.getVariableName() + suffix + ", ");
                } else {
                    _printStream.print(var.getVariableName() + suffix );
                }
            }
            _printStream.print(");");
            _printStream.println("");
            
        } else {
            
            VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
            VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);
            
            _printStream.println("");
            _printStream.print(_prefix + outArg.getVariableName() + suffix + " = "  +
                               inArg.getVariableName() + suffix + ";"           );
            _printStream.println("");
        }
    }
    
    
    /**
     *  Print an assign statement in the correct format for c++.
     *
     * @param  x The simple statement that needs to be rendered.
     */
    public void visitStatement(SimpleAssignStatement x) {
        
        String suffix = "";
        if( _bMultiApp ) {
            suffix = "_" + x.getNodeName();
        }
        // Avoid adding suffix to the control "dc" variables
        boolean flag = true;
        if( x.getLHSVarName().contains("dc") ) flag = false;
        
        if( flag ) {
            _printStream.print(_prefix + x.getLHSVarName() + suffix);
        } else {
            _printStream.print(_prefix + x.getLHSVarName());
        }
        
        Iterator i = x.getIndexListLHS().iterator();
        while( i.hasNext() ) {
            Expression expression = (Expression) i.next();
            _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
        }
        
        if( flag ) {
            _printStream.print(" = " + x.getRHSVarName() + suffix);
        } else {
            _printStream.print(" = " + x.getRHSVarName());
        }
        
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
            _printStream.println(_prefix 
                                     + x.getName() + " = "
                                     + expression.accept(_cExpVisitor) + ";");
            
        } else {
            _printStream.println(_prefix
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
        
        String suffix = "";
        if( _bMultiApp ) {
            suffix = "_" + x.getNodeName();
        }
        
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
                funName = "readSWF_Dyn2(";
            } else if( _scheduleType == 1) {
                funName = "readSWF_Dyn1(";
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
            } else if( rPort.getResource() instanceof Crossbar ) {
                // Why is this here???-----------------------------------------------------------------------------------
                if( _isDynamicSchedule ) { 
                    funName = "readDynMF(";
                } else {
                    funName = "readMF(";
                }
                //-------------------------------------------------------------------------------------------------------
            } else { // p2p FIFO port
                // if( _mapping.getMProcessor( _process ).getScheduleType() == 1 ) { // dynamic scheduling
                if( _scheduleType != 0 ) { 
                    funName = "readDyn(";
                } else { // static scheduling
                    funName = "read(";
                }
            }
        }
        
        
        if (_scheduleType == 0 && cdChannel.isSelfChannel() == true && Options.USE_LOCAL_VAR_FIFO == true){
            //-------------------------------------------------------------------------------------
            // We can implement the self-channels, in case of static schedule, as local variables.
            // This feature is currently not used because, in order to be complete, 
            // we need to remove also the HW implementation of these self-channels.
            //-------------------------------------------------------------------------------------
            if( cdChannel.getMaxSize()==1 ) {
                
                ADGVariable bindVar = x.getArgumentList().get(0);
                _printStream.print(_prefix + bindVar.getName() + suffix ); 
                
                Iterator j = bindVar.getIndexList().iterator();
                while( j.hasNext() ) {
                    Expression expression = (Expression) j.next();
                    _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
                }
                _printStream.println(" = var_" + cdChannel.getName() + "; // self-channel with size 1" );
                
            } else { // channel size not equal to 1
                String rd =  "rd_" + cdChannel.getName();
                // for every binding variable, we need a read from a fifo
                i = x.getArgumentList().iterator();
                while( i.hasNext() ) {
                    
                    ADGVariable bindVar = (ADGVariable) i.next();
                    _printStream.print(_prefix + bindVar.getName() + suffix ); 
                    
                    Iterator j = bindVar.getIndexList().iterator();
                    while( j.hasNext() ) {
                        Expression expression = (Expression) j.next();
                        _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
                    }
                    _printStream.println(" = var_" + cdChannel.getName() + "[" + rd + "!=" + (cdChannel.getMaxSize()-1) + "?(++" + rd + "):(" + rd + "=0)]; // self-channel with size " + cdChannel.getMaxSize() );
                }
            }
        } else { // FIFO is just implemented as it is (without using local variable)
            ADGInPort port = (ADGInPort)x.getPort();  
            
            // for every binding variable, we need a read from a fifo
            i = x.getArgumentList().iterator();
            while( i.hasNext() ) {
                ADGVariable bindVar = (ADGVariable) i.next();
                
                if( bindVar.getName().contains("dc") || _isEnableVar(port, bindVar.getName()) ) {
                    suffix = "";
                }
                
                _printStream.print(_prefix + funName + eName + ", " + "&" + bindVar.getName() + suffix );
                
                Iterator j = bindVar.getIndexList().iterator();
                while( j.hasNext() ) {
                    Expression expression = (Expression) j.next();
                    _printStream.print("[" + expression.accept(_cExpVisitor) + "]");
                }
                
                // _printStream.println(", " + s + ");");
                _printStream.print(", " + s);
                if( fifo.getLevelUpResource() instanceof CM_AXI ) {
                    // _printStream.println(", " + s + ", size" + t + ");");
                    _printStream.print(", size" + t);
                    if (_scheduleType == 2) // FreeRTOS
                        _printStream.print(", xLastReleasTime, xPeriod");
                }
                _printStream.println(");");
                //--------------------------------------- 
            }
        }
        
        
        _prefixInc();
        _visitChildren(x);
        _prefixDec();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ///
    
    /**
     * Check whether a port binding variable binds to an invar or function argument.
     * If not, then it is a variable used as 'enable' in case of dynamic PPNs
     */
    private boolean _isEnableVar(ADGInPort port, String name) {
        
        ADGNode node = (ADGNode)port.getNode();
        Iterator i = node.getInVarList().iterator();
        while( i.hasNext() ) {
            ADGInVar invar = (ADGInVar)i.next();
            if( name.equals(invar.getRealName())) {
                return false;
            }
        }
        i = node.getFunction().getInArgumentList().iterator();
        while( i.hasNext() ) {
            ADGVariable arg = (ADGVariable) i.next();
            String funcArgument = arg.getName();
            if( funcArgument.equals( name ) ) {
                return false;
            }
        }
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _cExpVisitor = null;
    
    private Mapping _mapping = null;
    
    private CDProcess _process = null;
    
    private int _scheduleType; // Save the schedule type specified in the mapping file
    
    private boolean _isDynamicSchedule; // Obsolete: now _scheduleType should be used to distinguish static, dynamic Xil-kernel, dynamic FreeRTOS
    
    private boolean _printDelay = false; // This is a hack to print vTaskDelayUntil when FreeRTOS is used
    
    private UserInterface _ui = null;
    
    private boolean _bMultiApp = false;        
}
