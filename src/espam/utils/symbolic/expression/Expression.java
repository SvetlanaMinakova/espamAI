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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import espam.visitor.ExpressionVisitor;

/**
 * This class expresses a linear expression as a array of LinTerms and provides
 * the basic functionally needed to operated on a linear expression. The linear
 * expression extends array list for fast and convenient access to the LinTerms
 * that make up the linear expression.
 * <p>
 * 
 * If the array consists of < LinTerm_0, LinTerm_1, LinTerm_2, ... , LinTerm_n >
 * <p>
 * 
 * Then it represents the following Linear Expression:
 * <p>
 * 
 * LinTerm_0 + LinTerm_1 + LinTerm_2 + ... + LinTerm_n
 * <p>
 * 
 * 
 * 
 * @author Bart Kienhuis, Alexandru Turjan
 * @version $Id: Expression.java,v 1.2 2011/06/10 11:41:38 svhaastr Exp $
 */

public class Expression extends ArrayList {

	/**
	 * default empty constructor.
	 */
	public Expression() {
		super();
	}

	/**
	 * Constructor that construct an Linear Expression on the basis of the
	 * supplied array list.
	 * 
	 * @param aLinTermList
	 *            an array list containing LinTerms.
	 */
	public Expression(ArrayList aLinTermList) {
		super(aLinTermList);
	}

	/**
	 * Constructor that start the definition of a expression using a possibly
	 * fractional number. The constructor creates a LinTerm for the number
	 * supplied and adds it to the arraylist.
	 * 
	 * @param numerator
	 *            The numerator of a possible fractional number.
	 * @param denominator
	 *            The denominator of a possible fractional number.
	 */
	public Expression(int numerator, int denominator) {
		super();
		add(new LinTerm(numerator, denominator, ""));
	}

	/**
	 * Format the Expression in a particular style. This style is describe by a
	 * implementation of the ExpressionVisitor.
	 * 
	 * @param visitor
	 *            the Expression visitor.
	 * @return String representing the Expression.
	 * @see panda.visitor.ExpressionVisitor
	 */
	public String accept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Add a LinTerm to the Expression.
	 * 
	 * @param LinTerm
	 *            Description of the Parameter
	 */
	public void add(LinTerm LinTerm) {
		super.add(LinTerm);
	}

	/**
	 * Add a Expression to this Expression.
	 * 
	 * @param expression
	 *            The linear expression.
	 */
	// FIXME: Confusing is that add(Expression) leads to very nasty errors.
	// Perhaps define a add(Expression) that is super classed to super.allAll
	public void addAll(Expression expression) {
		super.addAll(expression);
		simplify();
	}

	/**
	 * Add an Expression to this Expression.
	 * 
	 * @param num
	 *            The numerator of a possible fractional number.
	 * @param den
	 *            The denominator of a possible fractional number.
	 * @param expression
	 *            The feature to be added to the Expression attribute
	 */
	public void addExpression(int num, int den, Expression expression) {

		// System.out.println("Add to expression " + toString()
		// + " the expression " + expression.toString()
		// + " with num: " + num + " and den: " + den);

		Iterator i = expression.iterator();
		while (i.hasNext()) {
			LinTerm term = (LinTerm) i.next();
			int numerator = term.getNumerator() * num;
			term.setNumerator(numerator);
			// will fix the sign
			int denominator = term.getDenominator() * den;
			term.setDenominator(denominator);
			// System.out.println("Add term: " + term.toString());
			add(term);
		}
		// System.out.println("Before simplify: " + toString());
		simplify();
		// System.out.println("After simplify: " + toString());
	}

	/**
	 * Add a "-1" to the Expression
	 */
	public void addMinusOne() {
		add(new LinTerm(-1, 1, ""));
	}

	/**
	 * Add a variable to the Expression.
	 * 
	 * @param num
	 *            The numerator of a possible fractional number.
	 * @param den
	 *            The denominator of a possible fractional number.
	 */
	public void addNumber(int num, int den) {
		add(new LinTerm(num, den, ""));
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Add a "1" to the Expression.
	 */
	public void addOne() {
		add(new LinTerm(1, 1, ""));
	}

	/**
	 * Add a variable to the Expression.
	 * 
	 * @param num
	 *            The numerator of a possible fractional number.
	 * @param den
	 *            The denominator of a possible fractional number.
	 * @param name
	 *            The variable name
	 */
	public void addVariable(int num, int den, String name) {

		// System.out.println("num " + num+"den " + den +"name " + name);
		// System.out.println("EXPRESSION A " + toString());
		add(new LinTerm(num, den, name));
		// System.out.println("EXPRESSION B " + toString());
		// simplify();

		// System.out.println("EXPRESSION C " + toString());
	}

	/**
	 * Add a variable to the Expression.
	 * 
	 * @param name
	 *            The variable name.
	 */
	public void addVariable(String name) {
		add(new LinTerm(1, 1, name));
	}

	/**
	 * Clone the linear expression.
	 * 
	 * @return clone of the linear expression.
	 */
	public Object clone() {
		Expression newobj = new Expression();
		Iterator i = iterator();
		while (i.hasNext()) {
			// System.out.println("obj: " + i.next().getClass().getName());
			newobj.add(((LinTerm) i.next()).clone());
		}
		// Clone the Equality Type
		newobj._setEqualityType(getEqualityType());
		newobj.setDenominator(getDenominator());
		return newobj;
	}

	/**
	 * Check if an expressoin constains a particular symbol.
	 * 
	 * @param symbol
	 *            the search for
	 * @return true is symbol is found; otherwise return false
	 */
	public boolean containsVariable(String symbol) {
		Iterator i = iterator();
		while (i.hasNext()) {
			LinTerm term = (LinTerm) i.next();
			if (term.getName().equals(symbol)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Devide a Linear Expression by a fix value. This means that each term is
	 * divided by the divider.
	 * 
	 * @param divider
	 *            the divider
	 * @deprecated IS THIS USED?
	 */
	public void devideAll(int divider) {
		LinTerm j;
		Iterator i = iterator();
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			j.setDenominator(j.getDenominator() * divider);
		}
	}

	// public boolean isEqual(Object x ) {
	// System.out.print(" -- isEQUAL -- " + toString() + " equal to " +
	// x.toString());
	// return equals(x);
	// }

	/**
	 * Compare if two linear expression are the same. This method is used
	 * extensively by the collection package to find common linear expression in
	 * abstract data-structures like lists and maps.
	 * 
	 * @param x
	 *            The object with which to compare.
	 * @return true if two linear expressions are the same; otherwise false.
	 */
	// Interesting enough this equal comparision was not reflective:
	// k-1 is not equal to k but k is equal to k-1 in the old
	// implementation. But if two expression are canonical, they
	// should have the same size in order to be equal. This is the bug
	// fix
	// FIXME: We should take the sign also in consideration.
	public boolean equals(Object x) {
		if (x instanceof Expression) {
			// System.out.print(
			// " -- is "
			// + toSignedString()
			// + " equal to "
			// + ((Expression) x).toSignedString());
			if (size() == ((Expression) x).size()) {
				if (toEqualityTypeString().equals(
						((Expression) x).toEqualityTypeString())) {
					Iterator i = ((Expression) x).iterator();
					while (i.hasNext()) {
						LinTerm t = (LinTerm) i.next();
						if (!(contains(t))) {
							// System.out.println(" NO");
							return false;
						}
					}
				}
				// System.out.println(" YES");
				return true;
			}
		}
		// System.out.println(" NO");
		return false;
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
		int sum = 0;
		LinTerm j;
		Iterator i = iterator();
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			sum += j.evaluate(point, indices);
		}
		return sum/getDenominator();
	}

	/**
	 * Get the list of additional names. The special operators
	 * mod/div/floor/ceil define additional names. These names are needed in
	 * building a pip case in a pip basket.
	 * 
	 * @return the list with additional names.
	 */
	public ArrayList getAdditionalNames() {
		// System.out.println("\n Check Expression: " + toString());
		ArrayList nameList = null;
		Iterator i = this.iterator();
		while (i.hasNext()) {
			LinTerm y = (LinTerm) i.next();
			String name = y.getAdditionalIndex();
			if (name != null) {
				if (nameList == null) {
					nameList = new ArrayList();
				}
				// System.out.println(" --- ADDING ADDITIONAL NAME: " + name );
				nameList.add(name);
			}
		}
		return nameList;
	}

	/**
	 * @return
	 */
	public int getConstant() {
		// System.out.println("Exp: " + toString());
		simplify();
		Iterator i = iterator();
		while (i.hasNext()) {
			LinTerm term = (LinTerm) i.next();
			if (term.isNumber()) {
				if (term.getDenominator() != 1) {
					throw new Error(" Denominator not 1!" + toString());
				}
				return term.getNumerator();
			}
		}
		// There is no constants available
		return 0;
	}

	/**
	 * FIXME: What is an expression with a denominator. Were is this set?
	 * 
	 * @return The denominator value
	 */
	public int getDenominator() {
		return _denominator;
	}

	/**
	 * Get the kind of (in)equality this linear expression is. A value of 0
	 * represents a equality (== 0). A value of 1 represents a greater or equal
	 * to kind (>= 0). A value of -1 reprents a less or equal kind ( <= 0).
	 * 
	 * @return the type of the equality.
	 */
	public State getEqualityType() {
		return _state;
	}

	/**
	 * Get the kind of (in)equality this linear expression is. A value of 0
	 * represents a equality (== 0). A value of 1 represents a greater or equal
	 * to kind (>= 0). A value of -1 reprents a less or equal kind ( <= 0).
	 * 
	 * @return the type of the equality as an integer.
	 */
	public int getEqualityValue() {
		return _state.getValue();
	}

	/**
	 * Get of this vlinear expression all the nLinTerms that require a
	 * particular index statement. Thus suppose an linear expression uses 'd12'
	 * to reference a previous defined index statement, e.g., d12=div(i+1,2),
	 * then this method returns the name d12.
	 * 
	 * @return A list with all required index statements.
	 */
	public List getIndexRequirements() {
		List list = null;
		Iterator i = iterator();
		LinTerm j;
		String name;
		// System.out.println(" Check linear expression: " + toString() );
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			name = j.getName();
			// FIXME: Requires a more specific check. for example
			// that d is followed by a number.
			if (name.startsWith("d")) {
				if (list == null) {
					list = new ArrayList();
				}
				list.add(j.getName());
				// System.out.println(" Get Index Requirement: Add index name: "
				// + j.getName() );
			}
		}
		return list;
	}

	/**
	 * @param symbol
	 * @return
	 */
	public int getValue(String symbol) {
		Iterator i = iterator();
		while (i.hasNext()) {
			LinTerm term = (LinTerm) i.next();
			if (term.getName().equals(symbol)) {
				if (term.getDenominator() != 1) {
					throw new Error(" Denominator not 1!" + toString());
				}
				return term.getNumerator();
			}
		}
		return 0;
	}

	/**
	 * Get of this linear expression all the variable names. terms that require
	 * a particular index statement. Thus suppose an linear expression uses
	 * 'd12' to reference a previous defined index statement, e.g., d12 =
	 * div(i+1, 2), then this method returns the name d12.
	 * 
	 * @return A list with all required index statements.
	 */
	public List getVariableRequirements() {
		List list = null;
		Iterator i = iterator();
		LinTerm j;
		String name;
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			name = j.getName();
			if (list == null) {
				list = new ArrayList();
			}
			list.add(j.getName());
			if (j instanceof DivTerm) {
				DivTerm p = (DivTerm) j;
				Expression exp = p.getExpression();
				List ll = exp.getVariableRequirements();
				if (ll != null) {
					if (list != null) {
						list.addAll(ll);
					} else {
						list = ll;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Get the HashCode for the Expression. Used by some Set classes in
	 * Collection.
	 * 
	 * @return The hash value for this object.
	 */
	public int hashCode() {
		return toEqualityTypeString().hashCode();
	}

	/**
	 * Check whether this linearExp is always false in the equation A>=0, the
	 * default expression in which we express all relations.
	 * 
	 * @return true if the condition A>=0 is always false, otherwise return
	 *         false.
	 */
	public boolean isAlwaysFalse() {
		simplify();
		if (size() == 0) {
			return true;
			/*
			 * Number is 0, and is always true
			 */
		} else {
			if (size() == 1) {
				/*
				 * it is either a variable or a number
				 */
				LinTerm t = (LinTerm) get(0);
				if (t.getName().equals("")) {
					/*
					 * it is a number
					 */
					if (t.getSign() == -1) {
						return true;
						/*
						 * negative number
						 */
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether this linearExp is always true in the equation A>=0, the
	 * default expression in which we express all relations.
	 * 
	 * @return true if the condition A>=0 is always true, otherwise return
	 *         false.
	 */
	public boolean isAlwaysTrue() {
		simplify();
		if (size() == 0) {
			return true;
			/*
			 * Number is 0, and is always true
			 */
		} else {
			if (size() == 1) {
				/*
				 * it is either a variable or a number
				 */
				LinTerm t = (LinTerm) get(0);
				if (t.getName().equals("")) {
					/*
					 * it is a number
					 */
					if (t.getSign() == 1) {
						return true;
						/*
						 * Positive number
						 */
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check whether the name is of the typical Div index statement format. That
	 * means, it starts with the letter 'd' followed by a number.
	 * 
	 * @param name
	 *            the name to check
	 * @return true if the name is a div index statement name; false otherwise.
	 */
	public boolean isDivIndexName(String name) {
		if (name.startsWith("d")) {
			String remainder = name.substring(1, name.length());
			try {
				Integer.parseInt(remainder);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Check whether this linearExp represents a number. In this case no
	 * LinTerms exist that contains variables names or special operators like
	 * Mod/Div/Floor/Ceil.
	 * 
	 * @return true if the linear expression only contains numbers; otherwise
	 *         return false.
	 */
	public boolean isNumber() {
		simplify();
		if (size() == 0) {
			return true;
			/*
			 * number is 0
			 */
		} else {
			if (size() == 1) {
				LinTerm t = (LinTerm) get(0);
				if (t.getName().equals("")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return The positive value
	 */
	public boolean isPositive() {
		boolean var = true;
		simplify();
		if (this.isNumber() == false) {
			Iterator iterator = iterator();
			while (iterator.hasNext()) {
				LinTerm lt = (LinTerm) iterator.next();
				if (lt.isNumber() == false) {
					return lt.isPositive();
				}
			}
		} else {
			return (getConstant() > 0);
		}
		return var;
	}

	/**
	 * Is true if the linear expression linearExp doesn't contain any special
	 * operators like Mod/Div/Floor/Ceil.
	 * 
	 * @return true if the linear expression doen't contains special operators;
	 *         otherwise return false.
	 */
	public boolean isPureLinear() {
		simplify();
		if (size() == 0) {
			return true;
			/*
			 * number is 0
			 */
		} else {
			Iterator i = iterator();
			while (i.hasNext()) {
				LinTerm t = (LinTerm) i.next();
				if (t instanceof DivTerm || t instanceof ModTerm
						|| t instanceof FloorTerm || t instanceof CeilTerm
						|| t instanceof MaximumTerm || t instanceof MinimumTerm) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check whether this linearExp equals to zero
	 * 
	 * @return true is zero, otherwise false.
	 */
	public boolean isZero() {
		simplify();
		if (size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Return the least common mutliplier between the LinTerms of a linear
	 * expression
	 * 
	 * @return the least common multiplier.
	 */
	public int leastCommonMultipier() {
		Iterator i = iterator();
		LinTerm f = (LinTerm) i.next();
		int gcdNumber = f.getDenominator();
		;
		while (i.hasNext()) {
			LinTerm j = (LinTerm) i.next();
			gcdNumber = _lcm(j.getDenominator(), gcdNumber);
			// System.out.println(" --- GCD running LinTerm: " + gcdNumber + "
			// LinTerm denominator " + j.getDenominator() );
		}
		return gcdNumber;
	}

	/**
	 * Multiply a linear expression with a given multiplier.
	 * 
	 * @param multiplier
	 *            the multiplier.
	 * @return a linear expression.
	 */
	public Expression multiply(int multiplier) {
		Expression lexp = (Expression) this.clone();
		Iterator i = lexp.iterator();
		while (i.hasNext()) {
			LinTerm j = (LinTerm) i.next();
			j.multiply(multiplier);
		}
		return lexp;
	}

	/**
	 * Multiply a linear expression with a given multiplier.
	 * 
	 * @param numerator
	 *            Description of the Parameter
	 * @param denominator
	 *            Description of the Parameter
	 * @return a linear expression.
	 */
	public Expression multiply(int numerator, int denominator) {
		Expression lexp = (Expression) this.clone();
		Iterator i = lexp.iterator();
		while (i.hasNext()) {
			LinTerm j = (LinTerm) i.next();
			j.multiply(numerator, denominator);
		}
		return lexp;
	}

	/**
	 * Negate the current Linear expression by changing all signs of the
	 * LinTerms of the Linear Expression
	 */
	public void negate() {
		LinTerm j;
		Iterator i = iterator();
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			j.negate();
		}
	}

	/**
	 * Return a Linear Expression which is the opposite of the current Linear
	 * Expression. If the current linear expression is
	 * 
	 * <pre>
	 * expression &gt;= 0
	 * </pre>, then the opposite is equal the negated version of expression
	 * minus 1.
	 * 
	 * <pre>
	 * -expression - 1 &gt;= 0
	 * </pre>
	 * 
	 * @return a linear expression.
	 */
	public Expression opposite() {
		Expression exp = (Expression) this.clone();
		exp.negate();
		exp.add(new LinTerm(-1, 1, ""));
		return exp;
	}

	/**
	 * Remove the BigParameter from this Expression. The BigParameter is defined
	 * as
	 * 
	 * <pre>
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *                     i' = BigPar - i
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 * </pre>. So, the back transformation is
	 * 
	 * <pre>
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *                     i = BigPar = i'
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 * </pre>. This means we negate all indices and remove the BigParameter
	 * LinTerm.
	 */
	public void removeBigParameter() {
		this.add(new LinTerm(-1, 1, "BigPar"));
		this.negate();
	}

	/**
	 * FIXME: What is an expression with a denominator. Were is this set? Seems
	 * to be Port2COntrollerStatement and Domain2ForStatement
	 * 
	 * @param s
	 *            The new denominator value
	 */
	public void setDenominator(int s) {
		_denominator = s;
	}

	/**
	 * Set the kind of (in)equality this linear expression is. A value of 0
	 * represents a equality (== 0). A value of 1 represents a greater or equal
	 * to kind (>= 0). A value of -1 reprents a less or equal kind ( <= 0).
	 * 
	 * @param state
	 *            The new equalityType value
	 */
	public void setEqualityType(Expression.State state) {
		_state = state;
	}

	/**
	 * Set the kind of (in)equality this linear expression is. A value of 0
	 * represents a equality (== 0). A value of 1 represents a greater or equal
	 * to kind (>= 0). A value of -1 reprents a less or equal kind ( <= 0).
	 * 
	 * @param value
	 *            the equality type. / FIXME: perhaps a enumeral type?
	 */
	public void setEqualityType(int value) {
		if (value == 0) {
			_state = EQU;
		} else if (value == 1) {
			_state = GEQ;
		} else if (value == -1) {
			_state = LEQ;
		} else {
			System.err.println("Unrecognized type");
		}
	}

	/**
	 * Simplify this Expression.
	 */
	public void simplify() {

		LinTerm iLinTerm;

		LinTerm jLinTerm;
		ListIterator i = listIterator();
		while (i.hasNext()) {
			// get a LinTerm from the list
			iLinTerm = (LinTerm) i.next();

			ListIterator j = listIterator(i.nextIndex());

			// check is a LinTerm with the same name exitst
			while (j.hasNext()) {
				// get a LinTerm from the list
				jLinTerm = (LinTerm) j.next();
				if (iLinTerm.isEqual(jLinTerm)) {
					// LinTerms are equal and can be added
					iLinTerm.addTerm(jLinTerm);
				}
			}
		}

		i = listIterator();
		while (i.hasNext()) {
			// get a LinTerm from the list
			iLinTerm = (LinTerm) i.next();
			// see if the LinTerm is Zero
			if (iLinTerm.isZero()) {
				i.remove();
			}
		}
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
		// System.out.println(" Substitute Expression " + index + " for "
		// + this);

		Expression lexp = new Expression();
		LinTerm iterm;
		// Find the term
		Iterator i = iterator();
		while (i.hasNext()) {
			// get a term from the list
			iterm = (LinTerm) ((LinTerm) i.next()).clone();
			Expression t = iterm.substituteExpression(index, expression);
			// System.out.println("add " + t + " to " + lexp);
			if (t != null) {
				lexp.addAll(t);
			} else {
				lexp.add(iterm);
			}
		}
		// System.out.println(" RESULT SUB: " + lexp);
		return lexp;
	}

	/**
	 * Substitue a variable name in the LinearExp with a new variable name.
	 * 
	 * @param index
	 *            the index to substitue
	 * @param newIndexName
	 *            the index name to use for the substitution.
	 */
	public void substituteName(String index, String newIndexName) {
		// Expression lexp = (Expression) this.clone();
		LinTerm iterm;
		LinTerm jterm;
		LinTerm oppose = null;

		// Find the term
		Iterator i = iterator();
		while (i.hasNext()) {
			// get a term from the list
			iterm = (LinTerm) i.next();
			iterm.substituteName(index, newIndexName);
		}
	}

	/**
	 * Substitue variable names in the LinearExp with a new variable names. For
	 * substituting only a single name, use substritueName.
	 * 
	 * @param indices
	 *            vector with indices to substitue
	 * @param newIndexNames
	 *            vector with the new index names to use for the substitution.
	 * @return expression with names substituted.
	 */
	public Expression substituteNames(Vector indices, Vector newIndexNames) {
		Expression lexp = (Expression) this.clone();
		LinTerm iterm;
		LinTerm jterm;
		LinTerm oppose = null;

		if (indices.size() == newIndexNames.size()) {
			// Find a substitute name
			Iterator i = indices.iterator();
			Iterator j = newIndexNames.iterator();
			while (i.hasNext()) {
				// get a term from the list
				String index = (String) i.next();
				String newIndex = (String) j.next();
				lexp.substituteName(index, newIndex);
			}
		} else {
			throw new Error("Substitute name " + indices + " : "
					+ newIndexNames);
		}
		return lexp;
	}

	/**
	 * Description of the Method
	 * 
	 * @param expressions
	 *            Description of the Parameter
	 * @param indices
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public Expression substitutions(Vector expressions, Vector indices) {
		if (expressions.size() != indices.size()) {
			throw new Error("Number of expression != number of indices"
					+ expressions.toString() + " <-> " + indices.toString());
		}
		Iterator i = indices.iterator();
		int count = 0;
		Expression exp = this;
		while (i.hasNext()) {
			String index = (String) i.next();
			Expression substitution = (Expression) expressions.get(count++);
			exp = exp.substituteExpression(index, substitution);
		}
		return exp;
	}

	/**
	 * Get the linear expression as a constraint expression. This means that the
	 * type of the expression, equality or inequality be added to the return
	 * string value.
	 * 
	 * @return the linear expressions string value including (in)equality sign.
	 */
	public String toConstraintString() {
		String ln = toString();
		ln += _state.getDescription();
		return ln;
	}

	/**
	 * Get the type of the expression in terms of equality string or inequality
	 * string. This can be
	 * <p>
	 * 
	 * 
	 * <ol>
	 * <li> LEQ, Less Then Equal: <=0
	 * <li> GEQ, Greater Them Equal: >= 0
	 * <li> EQU, Equals: == 0
	 * <li> NONE, not relation to zero
	 * <ol>
	 * 
	 * @return the (in)equality string.
	 */
	public String toEqualityTypeString() {
		return _state.getDescription();
	}

	/**
	 * Coverts an expression to a PseudoPolinomail data structure
	 * 
	 * @return Description of the Return Value
	 */
/*	public PseudoPolynomial toPseudoPolynomial() {
		PseudoPolynomial pseudo = new PseudoPolynomial();
		ListIterator i = listIterator();
		while (i.hasNext()) {
			// get a LinTerm from the list
			LinTerm linTerm = (LinTerm) i.next();
			Fraction fraction = new Fraction(linTerm.getNumerator(), linTerm
					.getDenominator());
			Term term = null;
			if (linTerm instanceof DivTerm) {
				String str = "div("
						+ ((DivTerm) linTerm).getExpression().toString();
				str = str + "," + ((DivTerm) linTerm).getDivider() + ")";
				// System.out.println(" HE CONVERT TO DIVIDER TERM: " + str);
				// FIXME: Needs to become a DivTerm that extends CTerm
				term = new DividerTerm((CTerm) new CoefficientTerm(fraction),
						((DivTerm) linTerm).getExpression()
								.toPseudoPolynomial(), ((DivTerm) linTerm)
								.getDivider());
				// System.out.println(" CREATED " + term);
			} else {
				if (linTerm.isNumber()) {
					term = new CoefficientTerm(fraction);
				} else {
					term = new VariableTerm(fraction, new Variable(linTerm
							.getName(), 1));
				}
			}
			// System.out.println(" ADD TERM " + term);
			pseudo.add(term);
			// System.out.println(" PARTIAL RETURN: " + pseudo);
		}
		// System.out.println(" TOTAL RETURN: " + pseudo);
		return pseudo;
	}

	public String toSignedString() {
		String ln = toString();
		ln += _state.getDescription();
		return ln;
	}
*/
	/**
	 * Return the String representation of a linear expression. If the linear
	 * expression does not contain any LinTerms, return a '0'.
	 * 
	 * @return the string representation of a linear expression.
	 */
	public String toString() {
		String ln = "";
		if (this._denominator != 1) {
			ln += "(";
		}
		if (isZero() == false) {
			LinTerm j;
			Iterator i = iterator();
			int pos = 0;
			while (i.hasNext()) {
				j = (LinTerm) i.next();
				if (pos > 0) {
					if (j.isPositive()) {
						ln += "+";
					} else {
						ln += "";
					}
				}
				pos++;
				ln += j.toString();
			}
		} else {
			ln += "0";
		}
		if (this._denominator != 1) {
			ln += ")/" + _denominator;
		}

		return ln;
	}

	/**
	 * Update the Expression using the Map that contains aliases for some
	 * variables. This is done by going through each LinTerm. The method checks
	 * the variables against the alias map. If a match is found, the variable is
	 * replaced.
	 * 
	 * @param map
	 *            Description of the Parameter
	 */
	public void update(Map map) {
		LinTerm iLinTerm;
		LinTerm jLinTerm;
		ListIterator i = listIterator();
		while (i.hasNext()) {
			// get a LinTerm from the list
			iLinTerm = (LinTerm) i.next();

			// check if its name is contained in the map
			if (map.containsKey(iLinTerm.getName())) {
				// System.err.println(" BINGO: map contains the name " +
				// iLinTerm.getName() );

				// get the proper alias
				String alias = (String) map.get(iLinTerm.getName());
				// System.err.println(" BINGO: replace it with name " + alias);
				iLinTerm.setName(alias);
			}
		}
	}

	/**
	 * Implement Euclid's method for finding the Greatest Common Divisor (GCD)
	 * of two numbers. If the numbers are negative, then we compute the GCD of
	 * their absolute values.
	 * 
	 * @param u
	 *            Description of the Parameter
	 * @param v
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private int _gcd(int u, int v) {
		// System.out.print(" -- GCD u: " + u + " v: " + v + " return: ");
		int t;
		if (u < 0) {
			u = -u;
		}
		if (v < 0) {
			v = -v;
		}
		while (u > 0) {
			if (u < v) {
				t = u;
				u = v;
				v = t;
			} else {
				u = u % v;
			}
		}
		// System.out.println( v );
		return v;
	}

	/**
	 * Return the Least Common Multiplier between value <i>a </i> and <i>b </i>.
	 * The calculation performed is
	 * <p>
	 * 
	 * LCM(a,b) = a * b/GCD(a,b)
	 * <p>
	 * 
	 * "The Art of Computer Programming, vol. 2" by D.E. Knuth, Addison-Wesley,
	 * 1969 (for the GCD, LCM routines)
	 * 
	 * @param a
	 *            Description of the Parameter
	 * @param b
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private int _lcm(int a, int b) {
		return (a * b) / _gcd(a, b);
	}

	// /////////////////////////////////////////////////////////////////
	// // public variables ////

	/**
	 * Set the kind of (in)equality of this linear expression.
	 * 
	 * @param x
	 *            the equality type.
	 */
	private void _setEqualityType(Expression.State x) {
		_state = x;
	}

	/**
	 * The state of the execution, which is by default NONE. This indicates that
	 * the expression does not expresses a relationship.
	 */
	public State _state = NONE;

	/**
	 */
	private int _denominator = 1;

	/**
	 * Indicates the type of the Expression, whether it represents an inequalty
	 * ({-1, <=},{1,>=}) or equality (0,==).
	 */
	int _type = 0;

	/**
	 * Instances of this class represent phases of execution, or the state of
	 * the manager.
	 * 
	 * @author Bart Kienhuis
	 * @version $Id: Expression.java,v 1.2 2011/06/10 11:41:38 svhaastr Exp $
	 */
	private static class State {

		// Constructor is private because only Manager instantiates this class.
		/**
		 * Constructor for the State object
		 * 
		 * @param description
		 *            Description of the Parameter
		 * @param value
		 *            Description of the Parameter
		 */
		private State(String description, int value) {
			_description = description;
			_value = value;
		}

		public String toString() {
			return _description;
		}

		/**
		 * Get a description of the state.
		 * 
		 * @return A description of the state.
		 */
		public String getDescription() {
			return _description;
		}

		public int getValue() {
			return _value;
		}

		/**
		 * The description of the relationship.
		 */
		private String _description;

		private int _value = -100;
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 * Compute the similiraty between two linear expression. This method shall
	 * be used to perform commom sub-expression elimination for the hardware
	 * control generation.
	 * 
	 * @param x
	 *            object with which to compare.
	 * @param y
	 *            object with which to compare.
	 * @return the number of similar LinTerm false.
	 */
	public static int getScore(Expression x, Expression y) {
		Iterator i = ((Expression) x).iterator();
		Iterator j = ((Expression) y).iterator();
		int score = 0;

		while (i.hasNext()) {
			LinTerm tx = (LinTerm) i.next();
			while (j.hasNext()) {
				LinTerm ty = (LinTerm) j.next();
				// if (LinTerm.areEquals(tx,ty))
				// score ++;
				// }
			}
		}

		System.out.println("X is " + x.toString());
		System.out.println("Y is " + y.toString());
		System.out.println("their score is " + score);

		return score;
	}

	/**
	 * Compute the similiraty between two linear expression. This method shall
	 * be used to perform commom sub-expression elimination for the hardware
	 * control generation.
	 * 
	 * @param x
	 *            object with which to compare.
	 * @param y
	 *            object with which to compare.
	 * @return the number of similar LinTerm false.
	 */
	public static boolean identical(Expression x, Expression y) {
		String nx = ((Expression) x).toString();
		String ny = ((Expression) y).toString();
		return (nx.equals(ny));
	}

	/**
	 * Indicator that expression is equal to zero. <br>
	 * 
	 * <pre>
	 * expression == 0
	 * </pre>
	 */
	public static State EQU = new State(" == 0", 0);

	// /////////////////////////////////////////////////////////////////
	// // private methods ///

	/**
	 * Indicator that expression is larger or equal to zero. <br>
	 * 
	 * <pre>
	 * expression &gt;= 0
	 * </pre>
	 */
	public static State GEQ = new State(" >= 0", 1);

	// /////////////////////////////////////////////////////////////////
	// // inner class ////

	/**
	 * Indicator that expression is smaller or equal to zero. <br>
	 * 
	 * <pre>
	 * expression &lt;= 0
	 * </pre>
	 */
	public static State LEQ = new State(" <= 0", -1);

	/**
	 * Indicator of only an expression, without a relation.
	 * 
	 * <pre>
	 * expression
	 * </pre>
	 */
	public static State NONE = new State("", -2);
}
