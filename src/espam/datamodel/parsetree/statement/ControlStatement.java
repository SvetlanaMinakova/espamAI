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

import espam.utils.symbolic.expression.Expression;
import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// ControlStatement

/**
 *  The control statement has the following form:
 *  <ol>
 *    <li> <pre>
 *name = div(nominator, denominator);
 *</pre> or
 *    <li> <pre>name = nominator (if denominator == 1)</pre>
 *  </ol>
 *
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: ControlStatement.java,v 1.5 2002/09/30 14:03:02 kienhuis
 *      Exp $
 */

public class ControlStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Empty Control Statement Constructor.
     */
    public ControlStatement() {
        super("ControlStatement");
    }

    /**
     *  Construct a control statement using a linear expression as nominator
     *  and int as denominator.
     *
     * @param  name The name of the control variable
     * @param  nominator The linear expression for the nominator of the
     *      control statement
     * @param  denominator The in value for the denominator of the control
     *      statement
     */
    public ControlStatement(String name, Expression nominator,
            int denominator) {
        super("ControlStatement");
        _name = name;
        _nominator = nominator;
        _denominator = denominator;
    }

    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }

    /**
     *  Clone this ControlStatement.
     *
     * @return  a new instance of the ControlStatement.
     */
    public Object clone() {

        ControlStatement cs = (ControlStatement) super.clone();
        cs.setName( _name );
        cs.setNominator( (Expression) _nominator.clone() );
        cs.setDenominator( _denominator);

        return (cs);
    }


    /**
     *  Set the denominator
     *
     * @param  denominator the denominator
     */
    public void setDenominator(int denominator) {
        _denominator = denominator;
    }

    /**
     *  Get the denominator
     *
     * @return  the denominator
     */
    public int getDenominator() {
        return _denominator;
    }

    /**
     *  Set the name
     *
     * @param  name the name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the name
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the nominator
     *
     * @param  nominator the nominator
     */
    public void setNominator(Expression nominator) {
        _nominator = nominator;
    }

    /**
     *  Get the nominator
     *
     * @return  the nominator
     */
    public Expression getNominator() {
        return _nominator;
    }

    /**
     *  Give the string representation of the if statement.
     *
     * @return  a string representing the if statement.
     */
    public String toString() {
        String ln = " ControlStatement: " + _nominator.toString() + "/"
                + _denominator;
        return ln;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     */
    private String _name;

    /**
     */
    private Expression _nominator;

    /**
     */
    private int _denominator;
}
