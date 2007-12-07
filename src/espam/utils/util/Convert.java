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

package espam.utils.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import espam.operations.codegeneration.CodeGenerationException;
import espam.utils.polylib.PolylibException;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

// ////////////////////////////////////////////////////////////////////////
// // Convert

/**
 * This class represents.
 * 
 * @author Bart Kienhuis
 * @version $Id: Convert.java,v 1.1 2007/12/07 22:06:45 stefanov Exp $
 */

public class Convert {

	/**
	 * Convert an Expression in a SignedMatrix.
	 * 
	 * @param expression
	 *            the Expression to convert
	 * @param dpVec
	 *            the Vector in which to expression the Expression.
	 * @param type
	 *            type of the expression (==, <=, >=)
	 * @return the SignedMatrix for the Expression
	 * @exception CodeGenerationException
	 *                MyException If such and such occurs
	 * @see JMatrix
	 * @see Expression
	 */
	public static SignedMatrix expression2SignedMatrix(Expression expression,
			Vector dpVec, int type) throws CodeGenerationException {

		expression.simplify();

		// 2 = 1 for type + 1 for constant
		SignedMatrix convertMatrix = new SignedMatrix(1, dpVec.size() + 2);
		convertMatrix.setElement(0, 0, type);
		Iterator i = expression.iterator();
		while (i.hasNext()) {
			LinTerm term = (LinTerm) i.next();
			if (term.isNumber()) {
				if (term.getDenominator() == 1) {
					convertMatrix.setElement(0, dpVec.size() + 1, term
							.getNumerator());
				} else {
					throw new CodeGenerationException("Cannot convert"
							+ "linear expression " + expression.toString()
							+ "to a SignedMatrix instance. ");
				}
			} else {
				String variable = term.getName();
				if (dpVec.contains(variable)) {
					int index = dpVec.indexOf(variable);
					int value = term.getNumerator();
					if (term.getDenominator() == 1) {
						convertMatrix.setElement(0, index + 1, value);
					} else {
						throw new CodeGenerationException("Cannot convert"
								+ "linear expression " + expression.toString()
								+ "to a JMatrix instance. ");
					}
				} else {
					throw new CodeGenerationException("the space in which you "
							+ " want to represent the polyhedron"
							+ " contains not all variable of the affine "
							+ " expression! You want to present the "
							+ " polyhedron in " + dpVec.toString()
							+ ", but the expression is "
							+ expression.toString());
				}
			}
		}
		return convertMatrix;
	}

	/**
	 * Returns a vector of String object representing this JMatrix's value in
	 * terms of linear expressions that are either an equality or inequality. In
	 * case the matrix is empty, return only an empty list.
	 * 
	 * @param matrix
	 *            Description of the Parameter
	 * @param vector
	 *            a vector with the column names.
	 * @return a vector of Strings.
	 */
	public static List<Expression> toLinearExpression(SignedMatrix matrix,
			Vector vector) {
		Vector linearExpressions = new Vector();
		try {
			if (!matrix.isEmpty()) {
				if (vector.size() + 2 != matrix.getNbColumns()) {
					throw new PolylibException("Vector size and Matrix size "
							+ "do not fit. The vector " + vector.toString()
							+ " contains " + vector.size() + " elements, "
							+ "while " + (matrix.getNbColumns() - 2)
							+ " elements are expected");
				}
				for (int row = 0; row < matrix.getNbRows(); row++) {
					Expression linExpression = new Expression();
					long value;
					for (int column = 1; column < matrix.getNbColumns() - 1; column++) {
						value = matrix.getElement(row, column);
						if (value != 0) {
							String term = vector.get(column - 1).toString();
	
							linExpression.addVariable((int) value, 1, term);
						}
					}
					value = matrix.getElement(row, matrix.getNbColumns() - 1);
					if (value != 0) {
						linExpression.addVariable((int) value, 1, "");
					}
					linExpression.setEqualityType((int) matrix.getElement(row,
							0));
					linearExpressions.add(linExpression);
				}
			}
		} catch (PolylibException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return linearExpressions;
	}

}
