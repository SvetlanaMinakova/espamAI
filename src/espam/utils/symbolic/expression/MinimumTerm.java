/*******************************************************************\

This file is donated to ESPAM by Compaan Design BV (www.compaandesign.com) 
Copyright (c) 2000 - 2005 Leiden University (LERC group at LIACS)
Copyright (c) 2005 - 2007 CompaanDesign BV, The Netherlands
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.utils.symbolic.expression;

import java.util.List;

import espam.visitor.ExpressionVisitor;

// ////////////////////////////////////////////////////////////////////////
// // MinimumTerm

/**
 * This class represents a Div term. A Div Term is equal to a Term except that
 * is describes the div function instead variable or number. The format of a div
 * Term is:
 * <p>
 * 
 * <pre>
 * 
 * 
 * 
 *    MinimumTerm = sign * numerator/denominator * DIV( linear expression, divider )
 * 
 * 
 * 
 * </pre>
 * 
 * <p>
 * 
 * The calculation to find data dependencies are based on the use of integer
 * polytopes. An integer polytop is consider to be dense, which means that all
 * integer points enclosed by the polytop are valid members of the polytop. On
 * these dense polytopes, addition, subtraction, and multiplication are <i>
 * closed </i> operations. This means that the result of the operations is again
 * a dense polytop containing only integer points. Division, however, is not
 * included because this operation will result in fractional numbers. To be able
 * to use division with integer numbers we define the <i>integer division </i> .
 * Let <i>a </i> and <i>b </i> be positive integer numbers, integer division is
 * defined as
 * <p>
 * 
 * <pre>
 * 
 * 
 *     a = b * q+r, where a,b,q and r are integer values
 * 
 * 
 * 
 * </pre>
 * 
 * <p>
 * 
 * The integer division is thus written as a multiplication and addition and
 * therefore again a closed operation. Now, <i>b </i> is called the <i> divider
 * </i> and <i>r </i> is called the <i>remainder </i> of the division. The div
 * function calculates <i>q </i> .
 * 
 * @author Bart Kienhuis
 * @version $Id: MinimumTerm.java,v 1.1 2007/12/07 22:06:52 stefanov Exp $
 * @see LinTerm
 */

public class MinimumTerm extends LinTerm {

	/**
	 * Default empty constructor.
	 */
	public MinimumTerm() {
		super();
	}

	/**
	 * Constructor that creates a MinimumTerm with a given linear expression and
	 * divider. Create also a unique name that is used in determining the
	 * data-dependencies. This unique name will introduce an additional index
	 * variable.
	 * 
	 * @param expressionOne
	 *            Description of the Parameter
	 * @param expressionTwo
	 *            Description of the Parameter
	 */
	public MinimumTerm(Expression expressionOne, Expression expressionTwo) {
		super();
		_expressionOne = expressionOne;
		_expressionTwo = expressionTwo;

		_expressionOne.simplify();
		_expressionTwo.simplify();

		// Get a unique identifier
		String name = "s" + _number++;
		setName(name);

	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Accept an ExpressionVisitor.
	 * 
	 * @param x
	 *            The expression visitor.
	 * @return String representing the Term.
	 * @see panda.visitor.ExpressionVisitor
	 */
	public String accept(ExpressionVisitor x) {
		return x.visit(this);
	}

	/**
	 * Clone the MinimumTerm.
	 * 
	 * @return a new instance of the MinimumTerm. / FIXME need more....
	 */
	public Object clone() {
		MinimumTerm newobj = new MinimumTerm();
		newobj.setName(_name);
		newobj._expressionOne = (Expression) _expressionOne.clone();
		newobj._expressionTwo = (Expression) _expressionTwo.clone();
		newobj.setNumerator(getNumerator());
		newobj.setDenominator(getDenominator());
		return newobj;
	}

	/**
	 * Evaluate the expression for a particular point.
	 * 
	 * @param point
	 *            Description of the Parameter
	 * @param indices
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public int evaluate(List point, List indices) {
		int value1 = _expressionOne.evaluate(point, indices);
		int value2 = _expressionTwo.evaluate(point, indices);
		if (value1 < value2) {
			return (getNumerator() / getDenominator()) * value1;
		} else {
			return (getNumerator() / getDenominator()) * value2;
		}
	}

	/**
	 * Return the name of an additional index. Only the Div/Mod/Floor/Ceil terms
	 * add additional Indices to the PipProblem. This additional name is the
	 * transformation of non-dense term into a dense term.
	 * 
	 * @return The additionalIndex value
	 */
	public String getAdditionalIndex() {
		return _name;
	}

	/**
	 * Return expressionOne.
	 * 
	 * @return expressionOne.
	 */
	public Expression getExpressionOne() {
		return _expressionOne;
	}

	/**
	 * Return expressionTwo.
	 * 
	 * @return expressionTwo.
	 */
	public Expression getExpressionTwo() {
		return _expressionTwo;
	}

	/**
	 * Substitue a term in the LinearExp with a new Linear expression.
	 * 
	 * @param index
	 *            the index to substitue
	 * @param expression
	 *            the expression to use for the substitution.
	 * @return Description of the Return Value
	 */
	public Expression substituteExpression(String index, Expression expression) {
		throw new Error(
				"Substitution by Expression not implemeted in MinimumTerm");
	}

	/**
	 * Substitute a particular index with a new index name.
	 * 
	 * @param index
	 *            the index to substitute
	 * @param newIndexName
	 *            the substitute name
	 */
	public void substituteName(String index, String newIndexName) {
		// Recursively substitute names in the expression.
		_expressionOne.substituteName(index, newIndexName);
		_expressionTwo.substituteName(index, newIndexName);

		// see if it matches the name to substitue
		if (getName().equals(index)) {
			// substitute with new name
			setName(newIndexName);
		}
	}

	/**
	 * Return a description of the MinimumTerm.
	 * 
	 * @return a description of the MinimumTerm.
	 */
	public String toString() {
		String ln = "";
		ln += "min(" + _expressionOne.toString() + ","
				+ _expressionTwo.toString() + ")";
		return ln;
	}

	// /////////////////////////////////////////////////////////////////

	// // private variables ////

	/**
	 */
	Expression _expressionOne = null;

	/**
	 */
	Expression _expressionTwo = null;

	/**
	 * Static variable, implementing a singleton, to get a unique number for
	 * each MinTerm.
	 */
	private static int _number = 1;

}
