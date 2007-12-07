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
//// ElseStatement

/**
 *  This class represents the root of the else part of an IfStatement. It
 *  alsways should be the direct right sibbling of the IfStatement.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version $Id: ElseStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */
public class ElseStatement extends Statement {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	 *  Construct an empty else statement.
	 */
	public ElseStatement() {
		super("ElseStatement");
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
        *  Clone this ElseStatement.
        *
        * @return  a new instance of the ElseStatement.
        */
        public Object clone() {

            ElseStatement es = (ElseStatement) super.clone();
            if( _condition != null ) {
	         es.setCondition( (Expression) _condition.clone() );
            }
	    
            return (es);
        }

	/**
	 *  Set the condition
	 *
	 * @param  condition the condition
	 */
	public void setCondition(Expression condition) {
		_condition = condition;
	}

	/**
	 *  Get the condition
	 *
	 * @return  the condition
	 */
	public Expression getCondition() {
		return _condition;
	}

	/**
	 *  Give the string representation of the else statement.
	 *
	 * @return  a string representing the else statement.
	 */
	public String toString() {
		return "ElseStatement: " + _condition;
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  The epression of the Else Statement
	 */
	private Expression _condition = null;
}
