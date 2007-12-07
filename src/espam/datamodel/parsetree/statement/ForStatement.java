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
//// ForStatement

/**
 *  This class represents For Statement as it appears in a Nested Loop
 *  Program. Each For statement contains at least the following elements A
 *  for-statement contains an iterator, a lowerbound, an upperbound and a
 *  stepsize and is written as: <pre>
 *for iterator = lowerbound to upperbound step stepsize
 *</pre> The for-statement is used to repeat a statement or group of
 *  statements a predetermined number of times as specified by the
 *  lowerbound, upperbound and the stepsize. The iterator is first assigned
 *  the value of the lowerbound. Then on the next call to the statement, the
 *  iterator is incremented by the amount of the stepsize until the value is
 *  equal to or exceeds the upperbound. It is allowed to nest
 *  for-statements.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: ForStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */

public class ForStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Construct an For Statement given an iterator name, two linear
     *  expressions that describe respectiviely the upper and lower bound
     *  and the stepsize. When a step is selected other than one, and unique
     *  name is created to describe the step size.
     *
     * @param  id The name of the iterator defined by this for statement.
     * @param  lb Linear expression defining the lowerbound.
     * @param  ub Linear expression defining the upperbound.
     * @param  stepSize Description of the Parameter
     */
    public ForStatement(String id, Expression lb, Expression ub, int stepSize) {
        super("ForStatement");
        _iterator = id;
        _lowerBound = lb;
        _upperBound = ub;
        _stepSize = stepSize;

        if( _lowerBound != null ) {
            _lowerBound.simplify();
        }

        if( _upperBound != null ) {
            _upperBound.simplify();
        }
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
     *  Clone this ForStatement
     *
     * @return  a new instance of the ForStatement.
     */
    public Object clone() {

            ForStatement fs = (ForStatement) super.clone();
            fs.setIterator( _iterator );
            fs.setLowerBound( (Expression) _lowerBound.clone() );
            fs.setUpperBound( (Expression) _upperBound.clone() );
            fs.setStepSize( _stepSize );
            return (fs);
    }

    /**
     *  Set the iterator name.
     *
     * @param  iterator The iterator name.
     */
    public void setIterator(String iterator) {
        _iterator = iterator;
    }

    /**
     *  Get the iterator name.
     *
     * @return  the iterator name.
     */
    public String getIterator() {
        return _iterator;
    }

    /**
     *  Set the lower bound.
     *
     * @param  lowerBound The lower bound.
     */
    public void setLowerBound(Expression lowerBound) {
        _lowerBound = lowerBound;
    }

    /**
     *  Get the lower bound.
     *
     * @return  the lower bound.
     */
    public Expression getLowerBound() {
        return _lowerBound;
    }

    /**
     *  Set the step size.
     *
     * @param  stepSize The step size.
     */
    public void setStepSize(int stepSize) {
        _stepSize = stepSize;
    }

    /**
     *  Get the step size
     *
     * @return  the step size.
     */
    public int getStepSize() {
        return _stepSize;
    }

    /**
     *  Set the upper bound.
     *
     * @param  upperBound The upper bound.
     */
    public void setUpperBound(Expression upperBound) {
        _upperBound = upperBound;
    }

    /**
     *  Get the upper bound.
     *
     * @return  the upper bound.
     */
    public Expression getUpperBound() {
        return _upperBound;
    }

    /**
     *  Give the string representation of the for statement.
     *
     * @return  a string representing the for statement.
     */
    public String toString() {
        String ln = " ForStatement: " + _iterator + " = "
                + _lowerBound.toString() + " : " + _stepSize
                + " : " + _upperBound.toString();
        return ln;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the iterator defined by the For Statement.
     */
    private String _iterator;

    /**
     *  Lowerbound of the iterator.
     */
    private Expression _lowerBound;

    /**
     *  Upperbound of the iterator.
     */
    private Expression _upperBound;

    /**
     *  Step size of the iterator.
     */
    private int _stepSize;
}
