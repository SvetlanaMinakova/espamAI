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
import espam.datamodel.graph.adg.ADGInVar;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGNode;
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
    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();

	String suffix = "";
        if( _bMultiApp && !(x.getArgumentName().contains("dc")) ) {
	    suffix = "_" + x.getNodeName();
        }

    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

        if( cdChannel.isSelfChannel() ) {
// quick hack to determine the sinks (in the monitor)
             _printStream.print(_prefix + "writeFSL( ex, " + gateName );
        } else {
             _printStream.print(_prefix + "writeFSL( wr, " + gateName );
        }

	Iterator i = x.getIndexList().iterator();
	while( i.hasNext() ) {
		Expression expression = (Expression) i.next();
		_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
	}
        _printStream.println(", " + x.getArgumentName() + suffix + ", " + s + " );");
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
             _printStream.println(_prefix + "// Execute");
             _printStream.println(_prefix + "ex.write(sc_logic_1);");
             _printStream.println(_prefix + "waitcycles(lat_" + x.getFunctionName() + ");");
             _printStream.println(_prefix + "ex.write(sc_logic_0);");

             _printStream.print(_prefix + "_" + x.getFunctionName() + "(");

            Iterator i = rhsStatement.getChildren();
            while( i.hasNext() ) {
                 VariableStatement var = (VariableStatement) i.next();
                 if( i.hasNext() ) {
                     _printStream.print(var.getVariableName() + suffix + ", ");
                 } else {
                     _printStream.print(var.getVariableName() + suffix);
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
                   _printStream.print(var.getVariableName() + suffix);
               }
           }
           _printStream.print(") ;");
           _printStream.println("");
           _printStream.println("");

        } else {

             _printStream.println("");
             _printStream.println(_prefix + "// Execute");
             _printStream.println(_prefix + "ex.write(sc_logic_1);");
             _printStream.println(_prefix + "waitcycles(lat_CopyPropagate);");
             _printStream.println(_prefix + "ex.write(sc_logic_0);");

             VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
             VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
             _printStream.print(_prefix + outArg.getVariableName() + suffix + " = "  +
                                         inArg.getVariableName() + suffix + ";");
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
     *  Print the Fifo Memory Statement in the correct format for c++
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(FifoMemoryStatement x) {

	String suffix = "";
        if( _bMultiApp ) {
	    suffix = "_" + x.getNodeName();
        }
        ADGInPort port = (ADGInPort)x.getPort(); 

	_printStream.println("");
    	String gateName = x.getGateName();
    	CDGate cdGate = (CDGate)_process.getGate(gateName);
    	CDChannel cdChannel = (CDChannel)cdGate.getChannel();
    	String t = cdChannel.getName();
    	String s = "(sizeof(t" + t + ")+(sizeof(t" + t + ")%4)+3)/4";

        // for every binding vazriable, we need a read from a fifo
	Iterator i = x.getArgumentList().iterator();
	while( i.hasNext() ) {
		ADGVariable bindVar = (ADGVariable) i.next();

                if( bindVar.getName().contains("dc") || _isEnableVar(port, bindVar.getName()) ) {
                  suffix = "";
                }

                if( cdChannel.isSelfChannel() ) {
// quick hack to determine the sinks (in the monitor)
                    _printStream.print(_prefix + "readFSL( ex, " + gateName + ", " + bindVar.getName() + suffix );
                } else {
                    _printStream.print(_prefix + "readFSL( rd, " + gateName + ", " + bindVar.getName() + suffix );
                }

		Iterator j = bindVar.getIndexList().iterator();
		while( j.hasNext() ) {
			Expression expression = (Expression) j.next();
			_printStream.print("[" + expression.accept(_cExpVisitor) + "]");
		}
		_printStream.println(", " + s + " );");
	}
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

    private CDProcess _process = null;

    private UserInterface _ui = null;

    private boolean _bMultiApp = false;
}
