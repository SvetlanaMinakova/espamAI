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

import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.parsetree.statement.AssignStatement;
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
 * @author  Wei Zhong, Todor Stefanov, Hristo Nikolov
 * @version  $Id: XpsStatementVisitor.java,v 1.11 2002/06/24 15:48:36 stefanov
 *      Exp $
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Print a root statement in the correct format for C++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(RootStatement x) {
        _prefixInc();
        _visitChildren(x);
        _prefixDec();
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
              " floor1(" + ub.accept( _cExpVisitor ) + " ); " + x.getIterator() +
              " += " + x.getStepSize() + " ) {");
                
        _prefixInc();
        _visitChildren(x);
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
     *  Print an opd statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(OpdStatement x) {
    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

    	
    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

    	String eName = x.getNodeName() + "_" + 
    	               x.getGateName() + "_" + t;
    	
    	MFifo mFifo = _mapping.getMFifo(cdChannel);
    	Fifo fifo = mFifo.getFifo();
    	
    	if (fifo.getLevelUpResource() instanceof MultiFifo) {
    		String funName = "writeMF(";	

            _printStream.println("");
            _printStream.println(_prefix + funName + eName + 
        			 ", " + "&" + x.getArgumentName() +
        			 x.getNodeName() + ", " + s + ");");

        	_printStream.println("");
    	} else {
    		Iterator i;
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
        	
            String funName;
            if ( wPort.getResource() instanceof Processor ) {
            	funName = "writeFSL("; 
            }
            else funName = "write(";	

            _printStream.println("");
            _printStream.println(_prefix + funName + eName + 
        			 ", " + "&" + x.getArgumentName() +
        			 x.getNodeName() + ", " + s + ");");

        	_printStream.println("");
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

	if ( !x.getFunctionName().equals("") ) {

	     _printStream.println("");
             _printStream.print(_prefix + "_" + x.getFunctionName() + "(");

            Iterator i = rhsStatement.getChildren();
            while( i.hasNext() ) {
                 VariableStatement var = (VariableStatement) i.next();
                 if( i.hasNext() ) {
                     _printStream.print(var.getVariableName() + x.getNodeName() + ", ");
                 } else {
                     _printStream.print(var.getVariableName() + x.getNodeName());
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
                   _printStream.print("&" + var.getVariableName() + x.getNodeName() + ", ");
               } else {
                   _printStream.print("&" + var.getVariableName() + x.getNodeName());
               }
           }
           _printStream.print(") ;");
           _printStream.println("");

	} else {

	       VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
	       VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
             _printStream.print(_prefix + outArg.getVariableName() + x.getNodeName() + " = "  +
	                                 inArg.getVariableName() + x.getNodeName() + ";"           );
             _printStream.println("");

	}

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
     *  Print the Fifo Memory Statement in the correct format for c++
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(FifoMemoryStatement x) {

       	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();


    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

    	String eName = x.getNodeName() + "_" +
    	               x.getGateName() + "_" + t;

    	MFifo mFifo = _mapping.getMFifo(cdChannel);
    	Fifo fifo = mFifo.getFifo();

	String tmp = ((ADGVariable) x.getArgumentList().get(0)).getName();
        Iterator i;

    	if (fifo.getLevelUpResource() instanceof MultiFifo) {
    	     String funName = "readMF(";

            _printStream.println("");

            _printStream.println(_prefix + funName + eName +
        			 ", " + "&" + tmp +
        			 x.getNodeName() + ", " + s + ");");

            i = x.getArgumentList().iterator();
	    if (i.hasNext()) {
                 ADGVariable var = (ADGVariable) i.next();
	    }
	    while (i.hasNext()) {
                  ADGVariable var = (ADGVariable) i.next();
                 _printStream.println(_prefix + var.getName() + x.getNodeName() +" = "
	                                                 + tmp + x.getNodeName() + ";");
	    }

            _printStream.println("");
    	} else {
	
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

            String funName;
            if ( rPort.getResource() instanceof Processor ) {
            	funName = "readFSL(";
            }
            else if ( rPort.getResource() instanceof Crossbar ) {
            	funName = "readMF(";
            } else {
            	funName = "read(";
            }

            _printStream.println("");

            _printStream.println(_prefix + funName + eName +
        			 ", " + "&" + tmp +
        			 x.getNodeName() + ", " + s + ");");

            i = x.getArgumentList().iterator();
	    if (i.hasNext()) {
                 ADGVariable var = (ADGVariable) i.next();
	    }
	    while (i.hasNext()) {
                  ADGVariable var = (ADGVariable) i.next();
                 _printStream.println(_prefix + var.getName() + x.getNodeName() +" = "
	                                                 + tmp + x.getNodeName() + ";");
	    }

            _printStream.println("");
    	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _cExpVisitor = null;
    
    private Mapping _mapping = null;
    
    private CDProcess _process = null;
        
}
