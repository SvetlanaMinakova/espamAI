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

/**
 *  This class represents a place holder for the Right-hand side (RHS)
 *  variables of a function call. This Object is used to structure the
 *  function call such that is can be processed easier by Visitors.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: RhsStatement.java,v 1.1 2007/12/07 22:09:12 stefanov Exp $
 * @see  AssignStatement
 * @see  panda.visitor.StatementVisitor
 */

//////////////////////////////////////////////////////////////////////////
//// RhsStatement

public class RhsStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Empty Constructor.
     */
    public RhsStatement() {
        super("RhsStatement");
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
     *  Clone this RhsStatement.
     *
     * @return  a new instance of the RhsStatement.
     */
     public Object clone() {

            RhsStatement rhs = (RhsStatement) super.clone();
	    return (rhs);

     }

    /**
     *  Give the string representation of the rhs statement.
     *
     * @return  a string representing the rhs statement.
     */
    public String toString() {
        String ln = "RhsStatement";
        return ln;
    }
}
