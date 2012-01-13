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

package espam.visitor.hdpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import espam.datamodel.EspamException;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.parsetree.ParserNode;
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

import espam.main.UserInterface;

import espam.visitor.StatementVisitor;
import espam.visitor.expression.CExpressionVisitor;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.utils.symbolic.expression.Expression;

//////////////////////////////////////////////////////////////////////////
//// HdpcStatementVisitor

/**
 *  This class ...
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: HdpcStatementVisitor.java,v 1.3 2012/01/13 15:11:25 nikolov Exp $
 *      
 */

public class HdpcStatementVisitor extends StatementVisitor {

    /**
     *  Constructor for the HdpcStatementVisitor object
     *
     * @param  printStream Description of the Parameter
     * @param  name Description of the Parameter
     */
    public HdpcStatementVisitor(PrintStream printStream, CDProcess process) {
        super();
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
              " floor1(" + ub.accept( _cExpVisitor ) + "); " + x.getIterator() +
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
     *  Print an Opd (write primitives) statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(OpdStatement x) {

    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

        String syncType = "LOCK_FREE";
	// Check whether a channel is a 'self-loop' in order to use lighter communication primitive
	if( cdChannel.getFromGate().getProcess().getName().equals( cdChannel.getToGate().getProcess().getName() ) ) {
		syncType = "SYNC_FREE";
	}

	StringBuffer tmpStr = new StringBuffer(x.getGateName());
	tmpStr.delete(0,3);
	String port = tmpStr.toString();
        int intPort = Integer.parseInt(port);
	intPort--;
        
        _printStream.println("");
        /* in case of self channel, static scheduling, and size equal to 1,
	 * direct assignment from Opd to Ipd is possible without accessing SW FIFO */
        if (cdChannel.isSelfChannel() && cdChannel.getMaxSize() == 1){
	    _printStream.println(_prefix + "// accessing self-loop with size 1: " + cdChannel.getName());
	}
//        _printStream.println(_prefix + "proc.writeToPort<" + syncType + ">( " +
//	        intPort + ", " + x.getArgumentName() + x.getNodeName() + " );");
        _printStream.print(_prefix + "proc.writeToPort<" + syncType + ">( " +
	        intPort + ", " + x.getArgumentName());

	Iterator i = x.getIndexList().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}
	_printStream.println(" );");
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
//                      _printStream.print(var.getVariableName() + x.getNodeName() + ", ");
                     _printStream.print(var.getVariableName() + ", ");
                 } else {
//                      _printStream.print(var.getVariableName() + x.getNodeName());
                     _printStream.print(var.getVariableName());
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
//                    _printStream.print(var.getVariableName() + x.getNodeName() + ", ");
                   _printStream.print(var.getVariableName() + ", ");
               } else {
//                    _printStream.print(var.getVariableName() + x.getNodeName());
                   _printStream.print(var.getVariableName());
               }
           }
           _printStream.println(") ;");
           _printStream.println(_prefix + "proc.inc_execution_cntr();");
	   _printStream.println("");

	} else {

	       VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
	       VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
//              _printStream.print(_prefix + outArg.getVariableName() + x.getNodeName() + " = "  +
// 	                                 inArg.getVariableName() + x.getNodeName() + ";"           );
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
//            _printStream.println(_prefix + "int "
            _printStream.println(_prefix
                    + x.getName() + " = "
                    + expression.accept(_cExpVisitor) + ";");
        } else {
//             _printStream.println(_prefix + "int "
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

	String tmp = ((ADGVariable) x.getArgumentList().get(0)).getName();

    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

        String syncType = "LOCK_FREE";
	// Check whether a channel is a 'self-loop' in order to use lighter communication primitive
	if( cdChannel.getFromGate().getProcess().getName().equals( cdChannel.getToGate().getProcess().getName() ) ) {
		syncType = "SYNC_FREE";
	}

	StringBuffer tmpStr = new StringBuffer(x.getGateName());
	tmpStr.delete(0,3);
	String port = tmpStr.toString();
        int intPort = Integer.parseInt(port);
	intPort--;
	
	_printStream.println("");
	/* in case of self channel, static scheduling, and size equal to 1,
	 * direct assignment from Opd to Ipd is possible without accessing SW FIFO */
	if (cdChannel.isSelfChannel() && cdChannel.getMaxSize() == 1){
	    _printStream.println(_prefix + "// acessing self-loop with size 1: " + cdChannel.getName());
	}

//*
// for every binding vazriable, we need a read from fifo...
	Iterator i = x.getArgumentList().iterator();
	while( i.hasNext() ) {
		ADGVariable bindVar = (ADGVariable) i.next();

	       _printStream.print(_prefix + "proc.readFromPort<" + syncType + ">( " +
		        intPort + ", " +
                	bindVar.getName() );

		Iterator j = bindVar.getIndexList().iterator();
		while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		}
		_printStream.println(" );");
	}

/*/

        _printStream.println(_prefix + "proc.readFromPort<" + syncType + ">( " +
                intPort + ", " + tmp + x.getNodeName() + " );");
		
        Iterator i = x.getArgumentList().iterator();
	if (i.hasNext()) {
            ADGVariable var = (ADGVariable) i.next();
	}
	while (i.hasNext()) {
             ADGVariable var = (ADGVariable) i.next();
            _printStream.println(_prefix + var.getName() + x.getNodeName() +" = " 
	                                            + tmp + x.getNodeName() + ";");
	}
//*/
        _prefixInc();
        _visitChildren(x);
        _prefixDec();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ///



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     *  The Expressions visitor.
     */

    private CDProcess _process = null;

    private CExpressionVisitor _cExpVisitor = null;
}
