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

package espam.utils.symbolic.matrix;

import espam.parser.matrix.JMatrixParser;

import espam.utils.polylib.PolyLib;
import espam.utils.polylib.Polyhedron;
import espam.utils.polylib.PolylibException;

// ////////////////////////////////////////////////////////////////////////
// // SignedMatrix

/**
 * This class represents a signed matrix in the Java programming language. It is
 * equivalence with the Matrix type used in PolyLib.
 * <p>
 * 
 * This class be used to specify a matrix of constrainst. In class <i>
 * SignedMatrix </i> the first column represents the constraint. The first
 * column can have three values, representing three different (in)equalities:
 * <p>
 * 
 * 
 * <ol>
 * <li> 1, this represents an inequality that is greater-or-equal to zero (i.e, >=
 * 0).
 * <li> 0, this represents an equality to zero (i.e, == 0).
 * <li> -1, this represents an inequality that is less-or-equal to zero (i.e, <=
 * 0).
 * </ol>
 * is To sepecify an affine mapping, one should use the JMatrix class.
 * 
 * @author Edwin Rypkema, Alex Turjan, Bart Kienhuis, Kapil Makhija
 * @version $Id: SignedMatrix.java,v 1.1 2007/12/07 22:06:54 stefanov Exp $
 */

public class SignedMatrix extends JMatrix {

	/**
	 * Construct an empty matrix.
	 */
	public SignedMatrix() {
		super();
	}

	/**
	 * constructs a new matrix with <code>nRows</code> rows and
	 * <code>nColumns</code> columns.
	 * 
	 * @param nRows
	 *            number of rows of the matrix.
	 * @param nColumns
	 *            number of columns of the matrix.
	 */
	public SignedMatrix(int nRows, int nColumns) {
		super(nRows, nColumns);
	}

	/**
	 * constructs a new matrix which is a clone of matrix <code>M</code>.
	 * 
	 * @param M
	 *            the matrix.
	 */
	public SignedMatrix(JMatrix M) {
		super(M);
	}

	/**
	 * constructs a new matrix from long[][]
	 * 
	 * @param els
	 *            elements used to fill the SignedMatrix
	 */
	public SignedMatrix(long[][] els) {
		super(els);
	}

	/**
	 * Constructs a from a string in the Matlab format. The string is to be
	 * represented as:
	 * 
	 * <pre>
	 * 
	 * 
	 * 
	 *     [ int, int, int; int, int,
	 *     int]
	 * 
	 * 
	 * 
	 * </pre>, where the semi-colon indicates the start of a new row.
	 * 
	 * @param matlabString
	 *            the matlab string
	 * @deprecated explicitly use a JMatrix Parser instead.
	 */
	public SignedMatrix(String matlabString) {
		try {
			SignedMatrix M = JMatrixParser.getSignedMatrix(matlabString);
			_nbRows = M._nbRows;
			_nbColumns = M._nbColumns;
			_element = new long[_nbRows][_nbColumns];
			for (int j = 0; j < _nbRows; j++) {
				for (int i = 0; i < _nbColumns; i++) {
					_element[j][i] = M._element[j][i];
				}
			}
		} catch (Exception e) {

			System.out
					.println("Cannot convert the Matrix " + matlabString
							+ " to an instance of " + "SignedMatrix: "
							+ e.getMessage());
		}
	}

	/**
	 * constructs a new matrix with <code>nRows</code> rows and
	 * <code>nColumns</code> columns of the type specified with the string
	 * <code>type</code> The following types are recognized:
	 * <ol>
	 * <li> identity
	 * </ol>
	 * 
	 * 
	 * @param type
	 *            the type of matrix to create.
	 * @param nRows
	 *            the number of rows od the matrix.
	 * @param nColumns
	 *            the number of columns of the matrix.
	 */
	public SignedMatrix(String type, int nRows, int nColumns) {
		super(type, nRows, nColumns);
	}

	/**
	 * Clone a matrix.
	 * 
	 * @return a clone of the matrix.
	 */
	public Object clone() {
		SignedMatrix newobject = (SignedMatrix) new SignedMatrix();
		newobject.setMatrix(this);
		return newobject;
	}

	/**
	 * Return a new matrix without the sign column.
	 * 
	 * @return The matrix value
	 */
	public JMatrix getMatrix() {
		JMatrix returnMatrix = null;
		try {
			returnMatrix = removeColumn(0);
		} catch (PolylibException e) {
			e.printStackTrace();
		}
		return returnMatrix;
	}

	/**
	 * @return true if is a matrix of one
	 * @see panda.symbolic.matrix.JMatrix.isOne()
	 */
	public boolean isIdentity() {

		// System.out.println(" CHECK: \\n" + this);
		for (int i = 0; i < nbRows(); i++) {
			for (int j = 0; j < nbColumns(); j++) {
				// System.out.println(" i: " + i + " j:" + j);
				if (j == 0 && getElement(i, j) != 0) {
					// System.out.println(" (1) CHECK: " + getElement(i, j));
					return false;
				}
				if (i == (j - 1) && getElement(i, j) != 1) {
					// System.out.println(" (2) CHECK: " + getElement(i, j));
					return false;
				}
				if (i != (j - 1) && getElement(i, j) != 0) {
					// System.out.println(" (3) CHECK: " + getElement(i, j));
					return false;
				}
			}
		}
		// System.out.println(" ** TRUE ** ");
		return true;
	}

	/**
	 * retuns the polyhedral representation of the matrix. This function makes
	 * only sense when this matrix represens a set of constraints.
	 * 
	 * @return the polyhedral representation of this matrix.
	 */
	public Polyhedron toPolyhedron() {
		return PolyLib.getInstance().Constraints2Polyhedron(this);
	}
}
