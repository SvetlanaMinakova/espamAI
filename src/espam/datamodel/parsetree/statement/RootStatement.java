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

package espam.datamodel.parsetree.statement;

import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// RootStatement

/**
 *  This class represents a Root Statement. A Rootstatement does not appear
 *  in a Matlab programs. It is used to indicate the starting point of a
 *  parse tree.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: RootStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */

public class RootStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Default empty constructor.
     */
    public RootStatement() {
        super("RootStatement");
    }

    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     * @see  panda.visitor.StatementVisitor
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }

    /**
     *  Clone this RootStatement
     *
     * @return  a new instance of the RootStatement.
     */
    public Object clone() {

            RootStatement rs = (RootStatement) super.clone();
            rs.setDescription( _description );
            return (rs);
    }

    /**
     *  Get a description from this RootStatement.
     *
     * @return  a description
     */
    public String getDescription() {
        return _description;
    }

    /**
     *  Set a description to this RootStatement.
     *
     * @param  description a description.
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     *  Give the string representation of the root statement.
     *
     * @return  a string representing the root statement.
     */
    public String toString() {
        String ln = "RootStatement: " + _description;
        return ln;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _description;
}
