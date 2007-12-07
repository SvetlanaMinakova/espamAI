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
// // LinTerm

/**
 * This class describes a Term, which is the basic element of a Linear
 * Expression. A Term has in general a name, a sign and a fraction number given
 * by its numerator and denominator.
 * <p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * 
 *       Term = sign * name * numerator/denominator.
 * 
 * 
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * <p>
 * 
 * A Term that has a name, represents a variables. If the name is empty, the
 * Term represents a numerical value, possible as a fractional number. When the
 * denominator is equal to 1, the Term describes an integer value.
 * <p>
 * 
 * The Term class serves as the Base class for the Div/Mod/Floor/Ceil/Max/Min
 * Term.
 * 
 * @author Bart Kienhuis
 * @version $Id: LinTerm.java,v 1.1 2007/12/07 22:06:49 stefanov Exp $
 */

public class LinTerm {

	/**
	 * Default empty constructor. Set the default values for sign,
	 * numerator/denominator, and name.
	 */
	public LinTerm() {
		_sign = 1;
		_numerator = 1;
		_denominator = 1;
		_name = "mata";
	}

	/**
	 * Constructor that creates a term element with a particular name, and value
	 * given by the numerator and denominator. The sign of the Term is
	 * determined by the value of the numerator. If this is a positive number,
	 * than the sign of Term is set to 1; otherwise to -1.
	 * 
	 * @param num
	 *            The numerator.
	 * @param den
	 *            The denominator.
	 * @param name
	 *            The variable name.
	 */
	public LinTerm(int num, int den, String name) {
		_numerator = Math.abs(num);
		_denominator = Math.abs(den);
		_name = name;
		if (num >= 0) {
			if (den >= 0) {
				_sign = 1;
			} else {
				_sign = -1;
			}
		} else {
			if (den >= 0) {
				_sign = -1;
			} else {
				_sign = 1;
			}
		}
	}

	/**
	 * Constructor that creates a Term with a particular name. The value is set
	 * to 1/1. It thus represents a positive variable with value of 1.
	 * 
	 * @param name
	 *            Name of the Term.
	 */
	public LinTerm(String name) {
		_numerator = 1;
		_denominator = 1;
		_name = name;
		_sign = 1;
	}

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

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Add a Term to the current Term. Since both Terms can have a fractional
	 * number, the method first determines the greatest common denominator. Both
	 * fractions are multiplied with this value to get an integer representation
	 * that can be added together by adding the numerators of both Terms.
	 * 
	 * @param t
	 *            The Term to add.
	 */
	public void addTerm(LinTerm t) {
		LinTerm term = (LinTerm) t;
		int d = this._gcd(_denominator, term.getDenominator());
		int numerator = getNumerator();
		_denominator *= d;
		numerator *= d;
		numerator += (d * term.getNumerator());

		setNumerator(numerator);

		// Make the added term equal to zero, so it will be removed
		term.setNumerator(0);
	}

	/**
	 * Clone the Term.
	 * 
	 * @return a new instance of the Term.
	 */
	public Object clone() {
		LinTerm newobj = new LinTerm(_sign * _numerator, _denominator, _name);
		return newobj;
	}

	/**
	 * Check whether this Term equals an object. A Term is considered equal to
	 * the object when both describe a Term and the name of the Terms are the
	 * same.
	 * 
	 * @param term
	 *            the reference object with which to compare.
	 * @return True if this object is the same as the obj argument; otherwise
	 *         false.
	 */
	public boolean equals(Object term) {
		if (term instanceof LinTerm) {
			if (getName().equals(((LinTerm) term).getName())) {
				if (getNumerator() == ((LinTerm) term).getNumerator()) {
					if (getDenominator() == ((LinTerm) term).getDenominator()) {
						return true;
					}
				}
			}
		}
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
		if (isNumber()) {
			return (getNumerator() / getDenominator());
		} else {
			int pos = indices.indexOf(_name);
			if (pos == -1) {
				throw new Error("Cannot find value for index " + _name
						+ " in list " + indices.toString());
			}
			int value = ((Integer) point.get(pos)).intValue();
			return (getNumerator() / getDenominator()) * value;
		}
	}

	/**
	 * Return the name of an additional index. However, only the
	 * Div/Mod/Floor/Ceil terms add additional Indices to the PipProblem.
	 * 
	 * @return a null reference.
	 */
	public String getAdditionalIndex() {
		return null;
	}

	/**
	 * Get the denominator of the Term.
	 * 
	 * @return the denominator.
	 */
	public int getDenominator() {
		return _denominator;
	}

	/**
	 * Get the first derivative of this linear term. This class runs only with
	 * linear terms!!!
	 * 
	 * @return The firstDerivate value
	 */
	public int getFirstDerivate() {
		if (_name.compareTo("mata") == 0 || _name.compareTo("") == 0) {
			return 0;
		}
		if (_denominator != 0) {
			return _sign * (_numerator / _denominator);
		} else {
			return _sign * _numerator;
		}
	}

	/**
	 * Get the name of the Term.
	 * 
	 * @return the Name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Get the numerator of the Term. The value of the sign of the Term is used
	 * to give the correct sign to the value returned.
	 * 
	 * @return the Numerator that is either positive or negative depending on
	 *         the sign of the Term.
	 */
	public int getNumerator() {
		if (_sign == 0) {
			throw new Error(" _sign == 0");
		}
		return _sign * _numerator;
	}

	/**
	 * Get the sign of the Term.
	 * 
	 * @return the sign of the Term.
	 */
	public int getSign() {
		return _sign;
	}

	/**
	 * Check whether this Term equals a given Term. A Term is considered equal
	 * when it is of the same type and name.
	 * 
	 * @param term
	 *            the reference object with which to compare.
	 * @return True if this object is the same as the obj argument; otherwise
	 *         false.
	 */
	public boolean isEqual(LinTerm term) {
		if (term instanceof LinTerm) {
			if (_name.equals(((LinTerm) term).getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this term represents a number.
	 * 
	 * @return true if the term is a number; otherwise return false.
	 */
	public boolean isNumber() {
		if (getName().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * Return whether is Term is positive or not. A Term is positive when the
	 * sign is equal to one.
	 * 
	 * @return True when positive; otherwise false.
	 */
	public boolean isPositive() {
		if (_sign == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return whether the Term is equal to zero. A Term is zero when the
	 * numerator is equal to zero.
	 * 
	 * @return True is Term is zero; otherwise false.
	 */
	public boolean isZero() {
		if (_numerator == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Multiply the Term with a given multiplier. The numerator is multiplied
	 * with the multiplier. The numerator is set to the multiplied value unless
	 * the numerator divided by the denominator results in an integer value. In
	 * that case, the Term is simplified by setting the denominator to 1 and the
	 * numerator to value of numerator divided by the denominator.
	 * 
	 * @param multiplier
	 *            The multiplier with which to multiply the term.
	 */
	public void multiply(int multiplier) {
		int numerator = getNumerator();
		numerator *= multiplier;
		if ((numerator % _denominator) == 0) {
			setNumerator(numerator / _denominator);
			_denominator = 1;
		} else {
			setNumerator(numerator);
		}
		// Take the sign into account
		if (numerator < 0) {
			setSign(-1);
		}
	}

	/**
	 * @param num
	 *            Description of the Parameter
	 * @param den
	 *            Description of the Parameter
	 */
	public void multiply(int num, int den) {
		int numerator = getNumerator();
		int denominator = getDenominator();
		numerator *= num;
		denominator *= den;
		if ((numerator % denominator) == 0) {
			setNumerator(numerator / denominator);
			_denominator = 1;
		} else {
			setNumerator(numerator);
			setDenominator(denominator);
		}
		// Take the sign into account
		// FIXME: whow, this setSign remains dangerous.
		if (numerator < 0) {
			setSign(-1);
		}
	}

	/**
	 * Negate the Term. This is done by changing the sign of the Term.
	 */
	public void negate() {
		_sign *= -1;
	}

	/**
	 * Create a Term which is equal to one.
	 * 
	 * @return a Term which is equal to one.
	 */
	public LinTerm one() {
		return new LinTerm(1, 1, "mata");
	}

	/**
	 * Remove the Term. This means clearing all references.
	 */
	public void remove() {
		_name = "";
	}

	/**
	 * Sets the denominator attribute of the LinTerm object
	 * 
	 * @param denominator
	 *            The new denominator value
	 */
	public void setDenominator(int denominator) {
		_denominator = denominator;
	}

	/**
	 * Set the Name of the Term.
	 * 
	 * @param name
	 *            The name of the Term.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Set the numerator of the Term. Set, based on the value of Term, the sign
	 * value and set the absolute value to the numerator.
	 * 
	 * @param numerator
	 *            The new numerator value
	 */
	public void setNumerator(int numerator) {
		_numerator = Math.abs(numerator);
		if (numerator >= 0) {
			_sign = 1;
		} else {
			_sign = -1;
		}
		;
	}

	/**
	 * Set the sign of the Term.
	 * 
	 * @param sign
	 *            The sign of the Term.
	 */
	public void setSign(int sign) {
		_sign = sign;
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
		if (isNumber()) {
			return null;
		} else if (getName().equals(index)) {
			Expression tmp = (Expression) expression.clone();
			// System.out.println(" FOUND MATCH " + index + " replace for " +
			// tmp);
			return tmp.multiply(getNumerator(), getDenominator());
		}
		return null;
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
		// see if it matches the name to substitue
		if (getName().equals(index)) {
			// substitute with new name
			setName(newIndexName);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public String toConstraintString() {
		return toString();
	}

	/**
	 * Return a description of the Term. Based on the value of the sign and
	 * name, the formatting of the term is adjusted.
	 * 
	 * @return a description of the Term.
	 */
	public String toString() {
		int numerator = getNumerator();
		String start = "";

		// //////////////////////////////////////////////////////////////////////////////
		/*
		 * HMMMM very strange code. Seems a way to force a division to be
		 * rounded correctly. Should have been taken over by the
		 * ExpressionVisitor. if (_name.compareTo("mata") != 0) { start =
		 * "(double)"; }
		 */
		// //////////////////////////////////////////////////////////////////////////////
		if (numerator != 0) {

			if (_name.compareTo("mata") == 0 || _name.compareTo("") == 0) {
				if (_denominator == 1) {
					return "" + numerator;
				} else {
					return start + numerator + "/" + _denominator;
				}
			} else {
				if (_denominator == 1) {
					if (numerator == 1 || numerator == -1) {
						if (_sign == 1) {
							return "" + _name;
						} else {
							return "-" + _name;
						}
					} else {
						return numerator + "*" + _name;
					}
				} else {
					return start + numerator + "/" + _denominator + "*" + _name;
				}
			}
		} else {
			return "0";
		}
	}

	/**
	 * Implement Euclid's method for finding the Greatest Common Divisor (GCD)
	 * of two numbers. If the numbers are negative, * compute the GCD of their
	 * absolute values. "The Art of Computer * Programming, vol. 2" by D.E.
	 * Knuth, Addison-Wesley, 1969 (for the GCD, LCM routines)
	 * 
	 * @param u
	 *            Description of the Parameter
	 * @param v
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private int _gcd(int u, int v) {
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
		return v;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/**
	 * The name of the Term.
	 */
	protected String _name = "mata";

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

	/**
	 * The sign of the Term.
	 */
	protected int _sign;

	/**
	 * The denominator of the fractional value of the Term.
	 */
	private int _denominator;

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 * The numerator of the fractional value of the Term.
	 */
	private int _numerator;

}
