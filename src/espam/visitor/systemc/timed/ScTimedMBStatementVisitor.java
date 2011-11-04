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

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.main.UserInterface;

import espam.visitor.StatementVisitor;
import espam.visitor.expression.CExpressionVisitor;

import espam.utils.symbolic.expression.Expression;

//////////////////////////////////////////////////////////////////////////
//// ScTimedMBStatementVisitor

/**
 * This class generates the main procedure body for MicroBlaze processes. It is based on
 * the YAPI visitor.
 *
 * @author  Hristo Nikolov, Todor Stefanov, Sven van Haastregt
 * @version  $Id: ScTimedMBStatementVisitor.java,v 1.11 2002/06/24 15:48:36 sjain
 *      Exp $
 */

public class ScTimedMBStatementVisitor extends StatementVisitor {

    /**
     *  Constructor for the ScTimedMBStatementVisitor object
     *
     * @param  printStream Description of the Parameter
     * @param  name Description of the Parameter
     */
    public ScTimedMBStatementVisitor(PrintStream printStream, CDProcess process) {
        super();
        _printStream = printStream;
        _cExpVisitor = new CExpressionVisitor();
        _process = process;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Print a root statement in the correct format for C++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(RootStatement x) {
        //_prefixInc();
        _visitChildren(x);
        //_prefixDec();
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
     *  Print an ipd statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(OpdStatement x) {
        _printStream.println("");
/*
        _printStream.println(_prefix + "wr.write(true);");
        _printStream.print(_prefix + x.getGateName() + "->write( " +
                x.getArgumentName() );

	Iterator i = x.getIndexList().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}
	_printStream.println(" );");
        _printStream.println(_prefix + "waitcycles(latWrite);");
        _printStream.println(_prefix + "wr.write(false);");
/*/
    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

        _printStream.print(_prefix + "writeFSL( wr, " + gateName );

	Iterator i = x.getIndexList().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}
        _printStream.println(", " + x.getArgumentName() + ", " + s + " );");
//*/
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
             _printStream.println(_prefix + "// Execute");
             _printStream.println(_prefix + "ex.write(true);");
             _printStream.println(_prefix + "waitcycles(lat_" + x.getFunctionName() + ");");
             _printStream.println(_prefix + "ex.write(false);");

             _printStream.print(_prefix + "_" + x.getFunctionName() + "(");

            Iterator i = rhsStatement.getChildren();
            while( i.hasNext() ) {
                 VariableStatement var = (VariableStatement) i.next();
                 if( i.hasNext() ) {
                     _printStream.print(var.getVariableName() + ", ");
                 } else {
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
                   _printStream.print(var.getVariableName() + ", ");
               } else {
                   _printStream.print(var.getVariableName());
               }
           }
           _printStream.print(") ;");
           _printStream.println("");
           _printStream.println("");

        } else {

             _printStream.println("");
             _printStream.println(_prefix + "// Execute");
             _printStream.println(_prefix + "ex.write(true);");
             _printStream.println(_prefix + "waitcycles(lat_CopyPropagate);");
             _printStream.println(_prefix + "ex.write(false);");

             VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
             VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
             _printStream.print(_prefix + outArg.getVariableName() + " = "  +
                                         inArg.getVariableName() + ";");
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
     *  Print the Fifo Memory Statement in the correct format for c++
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(FifoMemoryStatement x) {
	_printStream.println("");
/*
        // for every binding vazriable, we need a read from a fifo
	Iterator i = x.getArgumentList().iterator();
	while( i.hasNext() ) {
		ADGVariable bindVar = (ADGVariable) i.next();

               _printStream.println(_prefix + "rd.write(true);");
	       _printStream.print(_prefix + x.getGateName() + "->read( " +
                	bindVar.getName() );

		Iterator j = bindVar.getIndexList().iterator();
		while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		}
		_printStream.println(" );");
                _printStream.println(_prefix + "waitcycles(latRead);");
                _printStream.println(_prefix + "rd.write(false);");
                _printStream.println("");
	}
/*/
    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();
    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

        // for every binding vazriable, we need a read from a fifo
	Iterator i = x.getArgumentList().iterator();
	while( i.hasNext() ) {
		ADGVariable bindVar = (ADGVariable) i.next();

               _printStream.print(_prefix + "readFSL( rd, " + gateName + ", " + bindVar.getName() );

		Iterator j = bindVar.getIndexList().iterator();
		while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		}
		_printStream.println(", " + s + " );");
	}
//*/        
        _prefixInc();
        _visitChildren(x);
        _prefixDec();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ///

    /**
     * @param  s Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     * @exception  PandaException Description of the Exception
     */
    private PrintStream _openMakefileFile(String s)
            throws FileNotFoundException, EspamException {

        PrintStream printStream;
        UserInterface ui = UserInterface.getInstance();

        String directory = null;
        //---------------------------------------------------
        // Create the directory indicated by the '-o' option. 
        // Otherwise select the orignal filename.
        //---------------------------------------------------
        if( ui.getOutputFileName() == "" ) {
            directory = ui.getBasePath() + "/" + ui.getFileName() + "_systemc/";
        } else {
            directory = ui.getBasePath() + "/" + ui.getOutputFileName();
        }
        File dir = new File(directory);

        if( !dir.exists() ) {
            if( !dir.mkdirs() ) {
                throw new EspamException("could not create " +
                        "directory '" + dir.getPath() + "'.");
            }
        }

        String fullFileName = dir + "/" + s + ".h";

        System.out.println(" -- OPEN FILE: " + fullFileName);

        OutputStream file = null;

        file = new FileOutputStream(fullFileName);
        printStream = new PrintStream(file);
        return printStream;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _cExpVisitor = null;

    private CDProcess _process = null;
}
