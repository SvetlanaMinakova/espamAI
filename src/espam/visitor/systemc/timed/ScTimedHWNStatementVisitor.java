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

package espam.visitor.systemc.timed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import espam.datamodel.EspamException;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.parsetree.ParserNode;
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

import espam.main.UserInterface;

import espam.visitor.StatementVisitor;
import espam.visitor.expression.CExpressionVisitor;

import espam.utils.symbolic.expression.Expression;

//////////////////////////////////////////////////////////////////////////
//// ScTimedHWNStatementVisitor

/**
 * This class generates the main procedure body for HWN processes.
 *
 * @author  Hristo Nikolov, Todor Stefanov, Sven van Haastregt
 * @version  $Id: ScTimedHWNStatementVisitor.java,v 1.2 2011/04/26 08:58:36 svhaastr Exp $
 */

public class ScTimedHWNStatementVisitor extends StatementVisitor {

    /**
     *  Constructor for the ScTimedHWNStatementVisitor object
     *
     * @param  printStream Description of the Parameter
     * @param  name Description of the Parameter
     */
    public ScTimedHWNStatementVisitor(PrintStream printStream, String name) {
        super();
        _printStream = printStream;
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
        	+ x.getIterator() + "read = ceil1("	+ lb.accept(_cExpVisitor) + "), " 
        	+ x.getIterator() + " = ceil1(" + lb.accept(_cExpVisitor) + "); "
        	+ x.getIterator() + " <= floor1(" + ub.accept(_cExpVisitor) + "); ) {");        
        _prefixInc();

        // Write
        _writeOperation( x );
        
        // Execute
        Iterator i = x.getChild(0).getChildren();
        while( i.hasNext() ) {
          Statement s = (Statement) i.next();
          if (s instanceof AssignStatement) {
        	AssignStatement a = (AssignStatement)s;
            visitStatement(a);
            break;
          }
        }
        // Read
        _readOperation( x );
        
        // shift and wait
        _printStream.println(_prefix + "// shift and wait");
        _printStream.println(_prefix + "shift_pipeline();");
        _printStream.println(_prefix + "waitcycles(1);");
        
        _prefixDec();
        
        //_prefixInc();
        //_visitChildren(x);
        //_prefixDec();

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
    public void visitStatement(OpdStatement x) { // todo : write operation
        _printStream.println(_prefix + "wr.write(true);");
        _printStream.println(_prefix + x.getGateName() + "->write( " +
                x.getArgumentName() + x.getNodeName() + ");");
        _printStream.println(_prefix + "waitcycles(latWrite);");
        _printStream.println(_prefix + "wr.write(false);");
        _printStream.println("");
    }

    /**
     *  Print an Assignment statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(AssignStatement x) {// Execute
        Statement statement = null;
        LhsStatement lhsStatement = (LhsStatement) x.getChild(0);
        RhsStatement rhsStatement = (RhsStatement) x.getChild(1);

        if ( !x.getFunctionName().equals("") ) {
             _printStream.println("");
             _printStream.println(_prefix + "// Execute");
             _printStream.println(_prefix + "int execute_i;");
             _printStream.println(_prefix + "for (execute_i = latRead + lat_"
//            		 + ((ADGNode)(x.getAdgNodeList().get(0))).getFunction().getName()
            		 + x.getFunctionName()
            		 + " - 1; execute_i >= latRead; execute_i--)");             
             _prefixInc();
             _printStream.println(_prefix + "if (pipeline[execute_i]) // there is something to execute");
             _prefixInc();
             _printStream.println(_prefix + "break;");
             _prefixDec();
             _prefixDec();
             _printStream.println(_prefix + "ex.write(execute_i >= latRead);");
          
             _printStream.print(_prefix + "//_" + x.getFunctionName() + "(");
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
                   _printStream.print(var.getVariableName() + x.getNodeName() + ", ");
               } else {
                   _printStream.print(var.getVariableName() + x.getNodeName());
               }
           }
           _printStream.print(") ;");
           _printStream.println("");
          _printStream.println("");

        } else {

               VariableStatement inArg = (VariableStatement) rhsStatement.getChild(0);
               VariableStatement outArg = (VariableStatement) lhsStatement.getChild(0);

             _printStream.println("");
             _printStream.print(_prefix + outArg.getVariableName() + x.getNodeName() + " = "  +
                                         inArg.getVariableName() + x.getNodeName() + ";"           );
             _printStream.println("");
             _printStream.println(_prefix + "execute(\"CopyPropagate\");");
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

          String tmp = ((ADGVariable) x.getArgumentList().get(0)).getName();

        _printStream.println(_prefix + "rd.write(true);");
        _printStream.println(_prefix + x.getGateName() + "->read(" + tmp + x.getNodeName() + ");");
                
        Iterator i = x.getArgumentList().iterator();
        if (i.hasNext()) {
            ADGVariable var = (ADGVariable) i.next();
        }
        while (i.hasNext()) {
             ADGVariable var = (ADGVariable) i.next();
            _printStream.println(_prefix + var.getName() + x.getNodeName() +" = " 
                                                    + tmp + x.getNodeName() + ";");
        }

        _printStream.println(_prefix + "waitcycles(latRead);");
        _printStream.println(_prefix + "rd.write(false);");
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
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename.
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
     *  Print the write operation in a for statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    private void _writeOperation(ForStatement x) {
        //printType(x);
    	RootStatement r = (RootStatement) x.getChild(0);
    	ArrayList<Constraint> write = new ArrayList<Constraint>();
        String funcName = _organizeWriteInfo(r, write);
    	
        _printStream.println(_prefix + "// Write");
        if (write.isEmpty()) {
        	_printStream.println(_prefix + "wr.write(false);");
        }
        else {        	
            _printStream.println(_prefix + "if (pipeline[latRead + lat_" + funcName + " + latWrite - 1]) {");
            _prefixInc();
            _printStream.println(_prefix + "bool write_done = false;");
            Iterator<Constraint> ic = write.iterator();
            while (ic.hasNext()) {
            	Constraint c = ic.next();
            	_printStream.println(_prefix + "if (" + c.getConstraintString() + ") {");
            	_prefixInc();
            	_printStream.println(_prefix + "if (");
            	ArrayList<Statement> states = c.getList();
        		_printStream.println(_prefix + "(" 
        		    + ((OpdStatement)(states.get(states.size() - 1))).getGateName() + "->num_free() > 0)");
            	for (int i = states.size() - 2; i >= 0; i--) {
            		OpdStatement opd = (OpdStatement) states.get(i);
            		_printStream.println(_prefix + "&& (" + opd.getGateName() + "->num_free() > 0)");
            	}
            	_printStream.println(_prefix + ") {");
            	_prefixInc();
            	for (int i = states.size() - 1; i >= 0; i--) {
            		OpdStatement opd = (OpdStatement) states.get(i);
            		_printStream.println(_prefix + opd.getGateName() + "->write("
            				+ opd.getArgumentName() + opd.getNodeName() + ");");
            	}
            	_printStream.println(_prefix + "write_done = true;");
            	_prefixDec();
            	_printStream.println(_prefix + "}");
            	_prefixDec();
            	_printStream.println(_prefix + "}");
            }
            _printStream.println(_prefix + "if (write_done) {");
            _prefixInc();
            _printStream.println(_prefix + "wr.write(true);");
            _printStream.println(_prefix + x.getIterator() + " += 1;");
            _prefixDec();
            _printStream.println(_prefix + "}");
            _printStream.println(_prefix + "else { //write blocked");
            _prefixInc();
            _printStream.println(_prefix + "rd.write(false);");
            _printStream.println(_prefix + "ex.write(false);");
            _printStream.println(_prefix + "wr.write(false);");
            _printStream.println(_prefix + "waitcycles(1);");
            _printStream.println(_prefix + "continue;");
            _prefixDec();
            _printStream.println(_prefix + "}");
            _prefixDec();
            _printStream.println(_prefix + "}");
            _printStream.println(_prefix + "else");
            _prefixInc();
            _printStream.println(_prefix + "wr.write(false);");
            _prefixDec();
        }
        
    	_printStream.println();
    }
    
    private String _organizeWriteInfo(RootStatement r, ArrayList<Constraint> write) {
    	String funcName = null;
    	Iterator i = r.getChildren();
    	while (i.hasNext()) {
    		Statement s = (Statement) i.next();
    		if (s instanceof IfStatement) {
    			IfStatement ifs = (IfStatement) s;
    			Iterator iifs = ifs.getChildren();
    			while (iifs.hasNext()) {
    				Statement child = (Statement)iifs.next();
    				if (child instanceof OpdStatement) { // write element
	    				OpdStatement opd = (OpdStatement) child;
	    				Iterator<Constraint> ic = write.iterator();
	    				boolean found = false;
	    				while (ic.hasNext()) {
	    					Constraint c = ic.next();
							if (c.getConstraintString().compareTo(ifs.getCondition().toConstraintString()) == 0) {
								c.getList().add(opd);
	    						found = true;
	    						break;
	    					}
	    				}
	    				if (!found) {
	    					Constraint c = new Constraint();
	    					c.setConstraintString(ifs.getCondition().toConstraintString());
	    					c.getList().add(opd);
	    					write.add(c);
	    				}
    				}
    			}
   	
    		}
    		else if (s instanceof AssignStatement) {
				funcName = ((AssignStatement)(s)).getFunctionName();
			}
    	}
    	return funcName;
    }
    
    /**
     *  Print the read operation in a for statement in the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    private void _readOperation(ForStatement x) {
        //printType(x);
    	RootStatement r = (RootStatement) x.getChild(0);
    	ArrayList<Constraint> read = new ArrayList<Constraint>();
        _organizeReadInfo(r, read);
    	
        _printStream.println(_prefix + "// Read");
        _printStream.println(_prefix + "if (" + x.getIterator() + "read <= floor1("
        	+ x.getUpperBound().accept(_cExpVisitor) + ")) {");
        _prefixInc();
        if (read.isEmpty()) {
        	_printStream.println(_prefix + "rd.write(false);");
        }
        else {
        	
            _prefixInc();
            _printStream.println(_prefix + "bool read_done = false;");
            Iterator<Constraint> ic = read.iterator();
            while (ic.hasNext()) {
            	Constraint c = ic.next();
            	_printStream.println(_prefix + "if (" + c.getConstraintString() + ") {");
            	_prefixInc();
            	_printStream.println(_prefix + "if (");
            	ArrayList<Statement> states = c.getList();
            	_printStream.println(_prefix + "(" 
            		    + ((FifoMemoryStatement)(states.get(states.size() - 1))).getGateName() + "->num_available() > 0)");
            	for (int i = states.size() - 2; i >= 0; i--) {
            		FifoMemoryStatement fifo = (FifoMemoryStatement) states.get(i);
            		_printStream.println(_prefix + "&& (" + fifo.getGateName() + "->num_available() > 0)");
            	}
            	_printStream.println(_prefix + ") {");
            	_prefixInc();
            	for (int i = states.size() - 1; i >= 0; i--) {
            		FifoMemoryStatement fifo = (FifoMemoryStatement) states.get(i);
            		_printStream.println(_prefix + fifo.getGateName() + "->read("
                   	    + ((ADGVariable) fifo.getArgumentList().get(0)).getName()
            			+ fifo.getNodeName() + ");");
            	}
            	_printStream.println(_prefix + "read_done = true;");
            	_prefixDec();
            	_printStream.println(_prefix + "}");
            	_prefixDec();
            	_printStream.println(_prefix + "}");
            }
            
            _printStream.println(_prefix + "pipeline[0] = read_done;");
            _printStream.println(_prefix + "rd.write(read_done);");
            _printStream.println(_prefix + "if (read_done)");
            _prefixInc();
            _printStream.println(_prefix + x.getIterator() + "read += 1;");
            _prefixDec();
        }
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println(_prefix + "else");
        _prefixInc();
        _printStream.println(_prefix + "rd.write(false);");
        _prefixDec();        
    	_printStream.println();
    }
    
    private void _organizeReadInfo(Statement r, ArrayList<Constraint> read) {
    	Iterator i = r.getChildren();
    	while (i.hasNext()) {
    		Statement s = (Statement) i.next();
    		if (s instanceof IfStatement) {
    			IfStatement ifs = (IfStatement) s;
    			Iterator iifs = ifs.getChildren();
    			while (iifs.hasNext()) {
    				Statement child = (Statement) iifs.next();
    				if (child instanceof FifoMemoryStatement) { // read element
        				FifoMemoryStatement fifo = (FifoMemoryStatement) child;
        				Iterator<Constraint> ic = read.iterator();
        				boolean found = false;
        				while (ic.hasNext()) {
        					Constraint c = ic.next();
        					if (c.getConstraintString().compareTo(ifs.getCondition().toConstraintString()) == 0) {
        						c.getList().add(fifo);
        						found = true;
        						break;
        					}
        				}
        				if (!found) {
        					Constraint c = new Constraint();
        					c.setConstraintString(ifs.getCondition().toConstraintString());
        					c.getList().add(fifo);
        					read.add(c);
        				}    					
    				}
    			}
    		}
    	}
    }
    
    /**
     * print tree of Statement type
     * @param x
     */
    private void printType(Statement x) {
    	System.err.println(_prefix + x.getType() + "-" + x.getClass());
    	Iterator i = x.getChildren();
    	_prefixInc();
    	while (i.hasNext()) {
    		printType((Statement) i.next());
    	}
    	_prefixDec();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _cExpVisitor = null;
}

class Constraint {
	public String getConstraintString() {
		return _ConstraintString;
	}
	public void setConstraintString(String s) {
		_ConstraintString = s;
	}
	public ArrayList<Statement> getList() {
		return _list;
	}
	private String _ConstraintString = null;
	ArrayList<Statement> _list = new ArrayList<Statement>();
}

