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

package espam.visitor;
import java.io.PrintStream;
import java.util.Iterator;

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
import espam.datamodel.parsetree.statement.RhsStatement;
import espam.datamodel.parsetree.statement.LhsStatement;

/**
 *  This abstract class
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: StatementVisitor.java,v 1.1 2007/12/07 22:07:24 stefanov Exp $
 */

//////////////////////////////////////////////////////////////////////////
//// Visitor

public class StatementVisitor implements Visitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     *  set the offset to use from now on.
     *
     * @param  o The new offset value
     */
    public void setOffset(String o) {
        _offset = o;
    }

    /**
     *  Set the prefix to s spefic lenght.
     *
     * @param  prefix The new prefix value
     */
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    /**
     *  Visit the Root Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(RootStatement x) {};

    /**
     *  Visit the For Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(ForStatement x) {};

    /**
     *  Visit the If Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(IfStatement x) {};

    /**
     *  Visit the Else Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(ElseStatement x) {};

    /**
     *  Visit the Assign Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(AssignStatement x) {};

    /**
     *  Visit the OPD Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(OpdStatement x) {};

    /**
     *  Visit the Variable Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(VariableStatement x) {};

    /**
     *  Visit the Lhs Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(LhsStatement x) {};

    /**
     *  Visit the Rhs Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(RhsStatement x) {};

    /**
     *  Visit the Control Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(ControlStatement x) {};

    /**
     *  Visit the Fifo Memory Statement.
     *
     * @param  x A Visitor Object.
     */
    public void visitStatement(FifoMemoryStatement x) {};

    /**
     *  Prefix for indenting nested statement.
     */
    protected String _prefix = "";


    /**
     *  Decrement the indentation.
     */
    protected void _prefixDec() {
        _prefix = _prefix.substring(_offset.length());
    }

    /**
     *  Decrement the indentation by n times
     *
     * @param  n
     */
    protected void _prefixDec(int n) {
        for( int i = 1; i <= n; i++ ) {
            _prefix = _prefix.substring(_offset.length());
        }
    }

    /**
     *  increment the indentation.
     */
    protected void _prefixInc() {
        _prefix += _offset;
    }

    /**
     *  Increment the indentation by n times
     *
     * @param  n
     */
    protected void _prefixInc(int n) {
        for (int i = 1; i <= n; i++) {
            _prefix += _offset;
        }
    }

    /**
     *  Walk the parse down, visiting the childeren one by one.
     *
     * @param  x The statement of which to visit the children.
     */
    protected void _visitChildren(Statement x) {
        Iterator i = x.getChildren();
        while( i.hasNext() ) {
            Statement s = (Statement) i.next();
            s.accept(this);
        }
    }

    /**
     *  Visited all children of a given parse node with the current visitor
     *  object. This method is a help funtion to traverse a Parsetree.
     *
     * @param  x Description of the Parameter
     */
    protected void _walkUpTheTree(Statement x) {
        ParserNode node = x.getParent();
        if( node != null ) {
            node.accept(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                ///

    /**
     *  Value for the added offset when indenting.
     */
    protected static String _offset = "  ";

    /**
     *  Stream where the print output is send to.
     */
    protected PrintStream _printStream = null;
}
