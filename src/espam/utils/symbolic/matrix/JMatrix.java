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

import java.util.Vector;

import espam.parser.matrix.JMatrixParser;

import espam.utils.polylib.PolylibException;
import espam.utils.symbolic.expression.Expression;

// ////////////////////////////////////////////////////////////////////////
// // JMatrix

/**
 * This class represents matrices in the Java programming language and is used
 * for the equivalence with the Matrix type used in the PolyLib.
 * <p>
 * 
 * This class can both be used to specify an matrix of constrainst as well an
 * affine mapping. In case <code>JMatrix</code> is used to represent a set of
 * constraints the first column is used to specify if the constraint is an
 * greater-or-equal inequality, specified by a 1, or a equality, specified by a
 * zero.
 * 
 * @author Edwin Rypkema, Alex Turjan, Bart Kienhuis, Kapil Makhija, Ioan
 *         Cimpian
 * @version $Id: JMatrix.java,v 1.1 2007/12/07 22:06:53 stefanov Exp $
 */

public class JMatrix implements Cloneable {

	/**
	 * Construct an empty matrix.
	 */
	public JMatrix() {
		_nbRows = 0;
		_nbColumns = 0;
		_element = new long[0][0];
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
	public JMatrix(int nRows, int nColumns) {
		_nbRows = nRows;
		_nbColumns = nColumns;
		_element = new long[nRows][nColumns];
	}

	/**
	 * constructs a new matrix from long[][]
	 * 
	 * @param els
	 *            elements used to fill the JMatrix
	 */
	public JMatrix(long[][] els) {
		_nbRows = els.length;
		assert (_nbRows > 0);
		_nbColumns = els[0].length;
		_element = new long[_nbRows][];
		for (int i = 0; i < _nbRows; ++i) {
			_element[i] = els[i];
		}
	}

	/**
	 * Constructs a from a string in the Matlab format. The string is to be
	 * represented as:
	 * 
	 * <pre>
	 * [ int, int, int; int, int,
	 * int]
	 * </pre>, where the semi-colon indicates the start of a new row.
	 * 
	 * @param matlabString
	 *            the matlab string
	 * @deprecated explicitly use a JMatrix Parser instead.
	 */
	public JMatrix(String matlabString) {
		try {
			JMatrix M = JMatrixParser.getJMatrix(matlabString);
			_nbRows = M._nbRows;
			_nbColumns = M._nbColumns;
			_element = new long[_nbRows][_nbColumns];
			for (int j = 0; j < _nbRows; j++) {
				for (int i = 0; i < _nbColumns; i++) {
					_element[j][i] = M._element[j][i];
				}
			}
		} catch (Exception e) {
			System.out.println("Cannot convert the Matrix " + matlabString
					+ " to an instance of " + "JMatrix: " + e.getMessage());
		}
	}

	/**
	 * constructs a new matrix with <code>nRows</code> rows and
	 * <code>nColumns</code> columns of the type specified with the string
	 * <code>type</code> The following types are recognized:
	 * <ol>
	 * <li> identity
	 * <li> secondary
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
	public JMatrix(String type, int nRows, int nColumns) {
		_nbRows = nRows;
		_nbColumns = nColumns;
		_element = new long[nRows][nColumns];
		if (type.equals("identity")) {
			int size = Math.min(nRows, nColumns);
			for (int i = 0; i < size; i++) {
				setElement(i, i, 1);
			}
		} else if (type.equals("secondary")) {
			int size = Math.min(nRows, nColumns);
			for (int i = 0; i < size; i++) {
				setElement(i, size - i - 1, 1);
			}
		} else {
			System.out.println("Matrix Type " + type + " is not recognized.");
			System.exit(-1);
		}
	}

	/**
	 * constructs a new matrix which is a clone of matrix <code>M</code>.
	 * 
	 * @param M
	 *            the matrix.
	 */
	protected JMatrix(JMatrix M) {
		setMatrix(M);
	}

	/**
	 * Description of the Method
	 * 
	 * @param A
	 *            Description of the Parameter
	 */
	public void add(JMatrix A) {
		// check whether or not the dimensions are correct.
		try {
			if (nbColumns() != A.nbColumns() || nbRows() != A.nbRows()) {
				throw new PolylibException("Dimensions are not correct.");
			}
			for (int i = 0; i < A.nbRows(); i++) {
				for (int j = 0; j < A.nbColumns(); j++) {
					setElement(i, j, (getElement(i, j) + A.getElement(i, j)));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Clone a matrix.
	 * 
	 * @return a clone of the matrix.
	 */
	public Object clone() {
		JMatrix newobject = (JMatrix) new JMatrix();
		newobject.setMatrix(this);
		return newobject;
	}

	/**
	 * Combine two columns to a new column. This takes place when two columns
	 * refer to the same variable and can therefore be combined. Two column
	 * locations are given that will be added together in the first column.
	 * 
	 * @param x
	 *            first column
	 * @param y
	 *            second column
	 * @return Description of the Return Value
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public JMatrix combineColumns(int x, int y) throws PolylibException {
		JMatrix matrix = (JMatrix) clone();
		for (int i = 0; i < matrix.getNbRows(); i++) {
			long sum = matrix.getElement(i, x) + matrix.getElement(i, y);
			matrix.setElement(i, x, sum);
			matrix.setElement(i, y, 0);
		}
		// JMatrix result = matrix.removeColumn(y);
		return matrix;
	}

	/**
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <tt>true</tt> if this object is the same as the obj argument;
	 *         <tt>false</tt> otherwise.
	 */
	public boolean equals(Object obj) {
		System.out.println(" -- CALLED JMATRIX equals ");
		if (obj instanceof JMatrix) {
			return isEqual(obj);
		}
		return false;
	}

	/**
	 * Inserts an extra column and an extra row at the end of the matrix and set
	 * the last element of the matrix to 1.
	 * 
	 * @return JMatrix with extra row and column and the last element in the
	 *         matrix set to 1
	 * @author gliem
	 */
	/*
	 * Add a zero row and a zero column and then make 1 !!! the last element in
	 * the matrix
	 */
	public JMatrix formatPolyLib() {
		JMatrix temp = (JMatrix) this.clone();
		JMatrix newRow = new JMatrix(1, temp.getNbColumns());
		JMatrix newColumn = new JMatrix(temp.getNbRows() + 1, 1);
		temp.insertRows(newRow, temp.getNbRows());
		temp.insertColumns(newColumn, -1);
		temp.setElement(temp.getNbRows() - 1, temp.getNbColumns() - 1, 1);
		return temp;
	}

	/**
	 * Return a sub-matrix of this matrix composed of the i-th column until (and
	 * including) the j-th columns of this matrix. If <i>j=-1</i> , return the
	 * columns from <i>i</i> untill the last column.
	 * 
	 * @param i
	 *            column start value of the matrix.
	 * @param j
	 *            column end value of the matrix.
	 * @return the submatrix.
	 */
	public JMatrix getCols(int i, int j) {
		if (j == -1) {
			j = nbColumns() - 1;
		}
		JMatrix M = null;
		try {
			if (j < i - 1) {
				throw new PolylibException("Cannot return a negative "
						+ "number of columns");
			}
			M = new JMatrix(nbRows(), j - i + 1);
			for (int r = 0; r < nbRows(); r++) {
				for (int c = 0; c < j - i + 1; c++) {
					M.setElement(r, c, getElement(r, c + i));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return M;
	}

	/**
	 * Get a particular column from the matrix.
	 * 
	 * @param atColumn
	 *            the column location to return
	 * @return a column from the matrix.
	 * @exception PolylibException
	 *                if the given column location is out of range.
	 */
	public long[] getColumn(int atColumn) throws PolylibException {
		if (_nbColumns < atColumn) {
			throw new PolylibException("In get column, the size of "
					+ "the column and the size of the matrix do not "
					+ "match. Size of column is " + atColumn
					+ " while size of Matrix is " + _nbColumns);
		}
		long[] tmpColumn = new long[_nbRows];
		for (int i = 0; i < _nbRows; i++) {
			tmpColumn[i] = _element[i][atColumn];
		}
		return tmpColumn;
	}

	/**
	 * returns the value of the enty with the specified row and col. The enty at
	 * the top left corner has indices row=0 and col=0.
	 * 
	 * @param row
	 *            the row.
	 * @param col
	 *            the column.
	 * @return the value storted at location (row,col).
	 */
	public long getElement(int row, int col) {
		return _element[row][col];
	}

/*	public IndexVector getIndexVector() {
		return _indexVector;
	}
*/
	/**
	 * Gets the nbColumns attribute of the JMatrix object
	 * 
	 * @return The nbColumns value
	 */
	public int getNbColumns() {
		return _nbColumns;
	}

	/**
	 * Gets the nbRows attribute of the JMatrix object
	 * 
	 * @return The nbRows value
	 */
	public int getNbRows() {
		return _nbRows;
	}

	/**
	 * Get a particular Row from the matrix.
	 * 
	 * @param atRow
	 *            the column location to return
	 * @return a row from the matrix.
	 * @exception PolylibException
	 *                if the given row location is out of range.
	 */
	public long[] getRow(int atRow) throws PolylibException {
		if (_nbRows < atRow) {
			throw new PolylibException("In get row, the size of "
					+ "the row and the size of the matrix do not "
					+ "match. Size of column is " + atRow
					+ " while size of Matrix is " + _nbRows);
		}
		long[] tmpRow = new long[_nbColumns];
		for (int i = 0; i < _nbColumns; i++) {
			tmpRow[i] = _element[atRow][i];
		}
		return tmpRow;
	}

	/**
	 * returns a submatrix of the current matrix composed of the i-th row until
	 * (and including) the j-th row of this matrix. If <i>j=-1</i> , return the
	 * rows from <i>i</i> untill the last row.
	 * 
	 * @param i
	 *            row start value of matrix.
	 * @param j
	 *            row end value of matrix.
	 * @return the submatrix.
	 */
	public JMatrix getRows(int i, int j) {
		if (j == -1) {
			j = nbRows() - 1;
		}
		JMatrix M = null;
		try {
			if (j < i - 1) {
				throw new PolylibException("Cannot return a negative "
						+ "number of rows");
			}
			M = new JMatrix(j - i + 1, nbColumns());
			for (int r = 0; r < j - i + 1; r++) {
				for (int c = 0; c < nbColumns(); c++) {
					M.setElement(r, c, getElement(r + i, c));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return M;
	}

	/**
	 * returns a sub-matrix of this matrix composed of the r1-th row until (and
	 * including) the r2-th row and the c1-th column until (and including) the
	 * c2-th columns of this matrix.
	 * 
	 * @param r1
	 *            the begin row value of the matrix.
	 * @param r2
	 *            the end row value of the matrix.
	 * @param c1
	 *            the begin column value of the matrix.
	 * @param c2
	 *            Description of the Parameter
	 * @return the sub matrix.
	 */
	public JMatrix getSubMatrix(int r1, int r2, int c1, int c2) {
		return getRows(r1, r2).getCols(c1, c2);
	}

	/*
	 * Check whether a matrix has one or more empty columns. An empty column
	 * consists of only zeros. Return true if one or more columns are empty.
	 * Otherwise return false.
	 * 
	 * @return true if matrix has one or more empty rows. Otherwise return
	 * false.
	 */
	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean hasEmptyColumns() {
		try {
			for (int i = 0; i < nbColumns(); i++) {
				if (isColumnZero(i)) {
					return true;
				}
			}
		} catch (PolylibException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Check whether a matrix has one or more empty rows. Return true if one of
	 * the rows of the matrix is zero. Otherwise return false.
	 * 
	 * @return true if one or more rows are zero. Otherwise return false.
	 */
	public boolean hasEmptyRows() {
		try {
			for (int i = 0; i < nbRows(); i++) {
				if (isRowZero(i)) {
					return true;
				}
			}
		} catch (PolylibException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * insert at colum at a particular location.
	 * 
	 * @param column
	 *            the column to insert
	 * @param atColumn
	 *            the location where to insert the column.
	 * @exception PolylibException
	 *                if the length of the column is not equal to the number of
	 *                columns of the matrix.
	 */
	public void insertColumn(long[] column, int atColumn)
			throws PolylibException {
		if (column.length != _nbRows) {
			throw new PolylibException("In insert column, the "
					+ "size of the column and the size of the "
					+ "matrix do not match. Length of column is "
					+ column.length + " while number of rows of "
					+ "Matrix is " + _nbRows);
		}
		for (int r = 0; r < _nbRows; r++) {
			_element[r][atColumn] = column[r];
		}
	}

	/**
	 * inserts matrix <code>M</code> as a number of columns in this matrix
	 * whose first columns is <code>atRow</code> . To let the matrix start
	 * with <code>M</code> use <code>atColumns = 0</code>, to let the
	 * matrix end with <code>M</code> use <code>atColumn =
	 *this.getNbColumns()</code>.
	 * As alternative for the latter you can specify the value -1.
	 * 
	 * @param M
	 *            the matrix
	 * @param atColumn
	 *            the column location where to insert the matrix.
	 */
	public void insertColumns(JMatrix M, int atColumn) {
		if (atColumn == -1) {
			atColumn = this.nbColumns();
		}
		int n = M.nbColumns();
		int nColumnsToShift = _nbColumns - atColumn;
		JMatrix oldMatrix = new JMatrix(this);
		_nbRows = oldMatrix._nbRows;
		_nbColumns = oldMatrix._nbColumns + n;
		_element = new long[_nbRows][_nbColumns];
		for (int r = 0; r < _nbRows; r++) {
			// copy first part;
			for (int c = 0; c < atColumn; c++) {
				_element[r][c] = oldMatrix._element[r][c];
			}
			// copy last part;
			for (int c = _nbColumns - nColumnsToShift; c < _nbColumns; c++) {
				_element[r][c] = oldMatrix._element[r][c - n];
			}
			// copy M;
			for (int c = atColumn; c < _nbColumns - nColumnsToShift; c++) {
				_element[r][c] = M._element[r][c - atColumn];
			}
		}
	}

	/*
	 * This function inserts row, at the position atRow
	 * 
	 * @param row Description of the Parameter @param atRow Description of the
	 * Parameter @exception PolylibException MyException If such and such occurs
	 */
	/**
	 * Description of the Method
	 * 
	 * @param row
	 *            Description of the Parameter
	 * @param atRow
	 *            Description of the Parameter
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public void insertRow(int[] row, int atRow) throws PolylibException {
		if (row.length != _nbColumns) {
			throw new PolylibException("In insert row, the "
					+ "size of the row and the size of the "
					+ "matrix do not match. Length of row is " + row.length
					+ " while number of columns of " + "Matrix is "
					+ _nbColumns);
		}
		for (int r = 0; r < _nbColumns; r++) {
			_element[atRow][r] = row[r];
		}
	}

	/*
	 * This function inserts row, at the position atRow
	 * 
	 * @param row Description of the Parameter @param atRow Description of the
	 * Parameter @exception PolylibException MyException If such and such occurs
	 */
	/**
	 * Description of the Method
	 * 
	 * @param row
	 *            Description of the Parameter
	 * @param atRow
	 *            Description of the Parameter
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public void insertRow(long[] row, int atRow) throws PolylibException {
		if (row.length != _nbColumns) {
			throw new PolylibException("In insert row, the "
					+ "size of the row and the size of the "
					+ "matrix do not match. Length of row is " + row.length
					+ " while number of columns of " + "Matrix is "
					+ _nbColumns);
		}
		for (int r = 0; r < _nbColumns; r++) {
			_element[atRow][r] = row[r];
		}
	}

	/**
	 * inserts matrix <code>M</code> as a number of rows in this matrix whose
	 * first row is <code>atRow</code>. To let the matrix start with
	 * <code>M</code> use <code>atRow = 0</code>, to let the matrix end
	 * with <code>M</code> use <code>atRow =
	 *this.getNbRows()</code>. As
	 * alternative for the latter you can specify the value -1. The size of the
	 * Matrix needs to fit the size of the matrix that is inserted.
	 * 
	 * @param M
	 *            the matrix.
	 * @param atRow
	 *            to row location where to insert the given matrix.
	 */
	public void insertRows(JMatrix M, int atRow) {
		/*
		 * throws PolylibException
		 */
		if (atRow == -1) {
			atRow = this.nbRows();
		}
		int n = M.nbRows();
		int nRowsToShift = _nbRows - atRow;
		try {
			if (nRowsToShift < 0) {
				throw new PolylibException(
						"Trying to insert a new matrix at a location in the"
								+ " current matrix that doesn't exist yet. Check"
								+ " the row location were to insert the matrix");
			}
			JMatrix oldMatrix = (JMatrix) this.clone();
			_nbRows = oldMatrix._nbRows + n;
			_nbColumns = oldMatrix._nbColumns;
			_element = new long[_nbRows][_nbColumns];
			for (int c = 0; c < _nbColumns; c++) {
				// copy first part;
				for (int r = 0; r < atRow; r++) {
					_element[r][c] = oldMatrix._element[r][c];
				}
				// copy last part;
				for (int r = _nbRows - nRowsToShift; r < _nbRows; r++) {
					_element[r][c] = oldMatrix._element[r - n][c];
				}
				// copy M;
				for (int r = atRow; r < _nbRows - nRowsToShift; r++) {
					_element[r][c] = M._element[r - atRow][c];
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * inserts <code>n</code> zero columns in this matrix whose first column
	 * is at position <code>atColumn</code> .
	 * <p>
	 * 
	 * Example: Let <code>M</code> be the following 3-by-6 matrix.
	 * <p>
	 * 
	 * <pre>
	 *<tt>
	 *   0  1  2  3  4  5
	 * </tt> <tt>
	 *  ----------------
	 * </tt> <tt>
	 *  x x x x
	 *  x x
	 * </tt> <tt>
	 *  x x x x x x
	 * </tt> <tt>
	 *  x x x x x x
	 * </tt> </pre>
	 * 
	 * The call <code>M.insertZeroColumns(3,2)</code> results in the following
	 * matrix.
	 * <p>
	 * 
	 * <pre>
	 *<tt>
	 *   0  1  2  3  4  5  6  7  8
	 * </tt> <tt>
	 *  -------------------------
	 * </tt> <tt>
	 *  x x 0 0 0 x x x x
	 * </tt> <tt>
	 *  x x 0 0 0 x x x x
	 * </tt> <tt>
	 *  x x 0 0 0 x x x x
	 * </tt> </pre>
	 * 
	 * <p>
	 * 
	 * 
	 * 
	 * @param n
	 *            number of zero columns that needs to be added.
	 * @param atColumn
	 *            location where the zero columns are added.
	 */
	public void insertZeroColumns(int n, int atColumn) {
		int nColumnsToShift = _nbColumns - atColumn;
		JMatrix oldMatrix = new JMatrix(this);
		_nbColumns = oldMatrix._nbColumns + n;
		_nbRows = oldMatrix._nbRows;
		_element = new long[_nbRows][_nbColumns];
		for (int r = 0; r < _nbRows; r++) {
			// copy first part;
			for (int c = 0; c < atColumn; c++) {
				_element[r][c] = oldMatrix._element[r][c];
			}
			// copy last part;
			for (int c = _nbColumns - nColumnsToShift; c < _nbColumns; c++) {
				_element[r][c] = oldMatrix._element[r][c - n];
			}
		}
	}

	/**
	 * inserts <code>n</code> zero rows in this matrix whose first row is at
	 * position <code>atRow</code>.
	 * <p>
	 * 
	 * Example: Let <code>M</code> be the following 6-by-3 matrix.
	 * <p>
	 * 
	 * <pre>
	 *<tt>
	 *   x  x  x
	 * </tt> <tt>
	 *  x x x
	 * </tt> <tt>
	 *  x x x
	 * </tt> <tt>
	 *  x x x
	 * </tt>
	 * <tt>
	 *  x x x
	 * </tt> <tt>
	 *  x x x
	 * </tt> </pre>
	 * 
	 * The call <code>M.insertZeroRowss(3,2)</code> results in the following
	 * matrix.
	 * <p>
	 * 
	 * <pre>
	 *<tt>
	 *   x  x  x
	 * </tt> <tt>
	 *  x x x
	 * </tt> <tt>
	 *  0 0 0
	 * </tt> <tt>
	 *  0 0 0
	 * </tt>
	 * <tt>
	 *  0 0 0
	 * </tt> <tt>
	 *  x x x
	 * </tt> <tt>
	 *  x x x
	 * </tt> <tt>
	 *  x x x
	 * </tt>
	 * <tt>
	 *  x x x
	 * </tt> </pre>
	 * 
	 * <p>
	 * 
	 * 
	 * 
	 * @param n
	 *            the number of zero rows to be added.
	 * @param atRow
	 *            the location of where the zero rows at to be added.
	 */
	public void insertZeroRows(int n, int atRow) {
		int nRowsToShift = _nbRows - atRow;
		JMatrix oldMatrix = new JMatrix(this);
		_nbRows = oldMatrix._nbRows + n;
		_nbColumns = oldMatrix._nbColumns;
		_element = new long[_nbRows][_nbColumns];
		for (int c = 0; c < _nbColumns; c++) {
			// copy first part;
			for (int r = 0; r < atRow; r++) {
				_element[r][c] = oldMatrix._element[r][c];
			}
			// copy last part;
			for (int r = _nbRows - nRowsToShift; r < _nbRows; r++) {
				_element[r][c] = oldMatrix._element[r - n][c];
			}
		}
	}

	/**
	 * Return true is the column contains only zeros.
	 * 
	 * @param column
	 *            the column to check for zeros.
	 * @return true is the column only contains zeros, otherwise false.
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public boolean isColumnZero(int column) throws PolylibException {
		long[] line = getColumn(column);
		for (int x = 0; x < _nbRows; x++) {
			if (line[x] != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check whether the matrix is empty, which means it doesn't contain a
	 * single item. This means that the matrix has no row or column.
	 * 
	 * @return true if matrix is empty, otherwise false.
	 */
	public boolean isEmpty() {
		if (nbRows() == 0) {
			return true;
		}
		if (nbColumns() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether a particular Matrix equals another Object.
	 * 
	 * @param obj
	 *            The object to check
	 * @return true if the object is the same Matrix; otherwise return false.
	 */
	public boolean isEqual(Object obj) {
		if (obj instanceof JMatrix) {
			JMatrix matrix = (JMatrix) obj;
			if (nbRows() == matrix.nbRows()) {
				if (nbColumns() == matrix.nbColumns()) {
					for (int i = 0; i < nbColumns(); i++) {
						for (int j = 0; j < nbRows(); j++) {
							if (getElement(j, i) != matrix.getElement(j, i)) {
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return true if is a matrix of one //FIXME, better name is isIdentity()
	 */
	public boolean isOne() {
		for (int i = 0; i < nbRows(); i++) {
			for (int j = 0; j < nbColumns(); j++) {
				if (i != j && getElement(i, j) != 1) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Inspects the matrix for certain properties like the matrix is a lower
	 * triangular matrix, with the diagonal entries greater than zero.
	 * 
	 * @return The property value
	 */
	/**
	 * Gets the property attribute of the JMatrix object
	 * 
	 * @return The property value
	 */
	public boolean isProperty() {
		for (int i = 0; i < _nbRows; i++) {
			for (int j = i; j < _nbColumns; j++) {
				if (j == i) {
					if (_element[i][j] <= 0) {
						System.out.println("It does not satisfy the property");
						return false;
					}
				} else {
					if (_element[i][j] != 0) {
						System.out
								.println("Here too it does not satisfy the property!!");
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Return true is the row contains only zeros.
	 * 
	 * @param row
	 *            the row to check for zeros.
	 * @return true is the row only contains zeros, otherwise false.
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public boolean isRowZero(int row) throws PolylibException {
		long[] line = getRow(row);
		for (int x = 0; x < _nbColumns; x++) {
			if (line[x] != 0) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Checks whether the matrix is Square or not. If the matrix is square, it
	 * returns true, otherwise false.
	 */
	/**
	 * Gets the square attribute of the JMatrix object
	 * 
	 * @return The square value
	 */
	public boolean isSquare() {
		if (_nbRows == _nbColumns) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the zero attribute of the JMatrix object
	 * 
	 * @return The zero value
	 */
	public boolean isZero() {
		for (int i = 0; i < nbRows(); i++) {
			for (int j = 0; j < nbColumns(); j++) {
				if (this.getElement(i, j) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * multiplies this matrix on the left handside with matrix a supplied A.
	 * 
	 * @param A
	 *            the matrix with which to multiply.
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public void lMul(JMatrix A) throws PolylibException {
		JMatrix Temp = (JMatrix) A.clone();
		Temp.rMul(this);
		// Temp = Temp*this, A = A*this
		setMatrix(Temp);
		// this = A
	}

	/**
	 * returns the number of columns in the matrix.
	 * 
	 * @return Description of the Return Value
	 */
	public int nbColumns() {
		return _nbColumns;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public int nbNonZeroColumns() throws PolylibException {
		int zeroColumns = 0;
		for (int i = 0; i < _nbColumns; i++) {
			if (isColumnZero(i)) {
				zeroColumns++;
			}
		}
		return _nbColumns - zeroColumns;
	}

	/**
	 * returns the number of rows in the matrix.
	 * 
	 * @return Description of the Return Value
	 */
	public int nbRows() {
		return _nbRows;
	}

	/**
	 * make the matrix negative. Warning, all entries becomes negative, so be
	 * carefull when this matrix is a constraint matrix.
	 */
	public void negate() {
		for (int j = 0; j < _nbRows; j++) {
			for (int i = 0; i < _nbColumns; i++) {
				_element[j][i] = -_element[j][i];
			}
		}
	}

	// Used in ProcLib/ Why not based in removeColumn.
	/**
	 * Return this matrix with the columns i, i+1, ..., j removed.
	 * 
	 * @param i
	 *            Description of the Parameter
	 * @param j
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public JMatrix removeCols(int i, int j) {
		JMatrix M = null;
		try {
			if (i > 0) {
				if (j < i - 1) {
					return (JMatrix) clone();
				}
				M = new JMatrix(nbRows(), 0);
				JMatrix M1 = getCols(0, i - 1);
				// FIXME i > 0
				JMatrix M2 = getCols(j + 1, -1);
				M.insertColumns(M1, -1);
				M.insertColumns(M2, -1);
			} else {
				M = new JMatrix(nbRows(), 0);
				JMatrix M2 = getCols(j + 1, -1);
				M.insertColumns(M2, -1);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return M;
	}

	/**
	 * Return a new matrix with a particular column being removed with repect to
	 * the orignal matrix. This class is immutable which means that the orignal
	 * matrix is not effected.
	 * 
	 * @param column
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public JMatrix removeColumn(int column) throws PolylibException {
		if (column < 0 || column >= _nbColumns) {
			throw new PolylibException("Trying to remove a non-exisiting "
					+ "Column from a matrix. Check the column location of "
					+ "the column to remove from the matrix.");
		}
		JMatrix returnMatrix = new JMatrix(_nbRows, _nbColumns - 1);
		// System.out.println(" -- Have return matrix -- ");
		for (int r = 0; r < _nbRows; r++) {
			// copy first part;
			for (int c = 0; c < column; c++) {
				returnMatrix.setElement(r, c, _element[r][c]);
				// System.out.println(" -- copy first part -- ");
			}
			// copy last part;
			for (int c = column + 1; c < _nbColumns; c++) {
				returnMatrix.setElement(r, c - 1, _element[r][c]);
				// System.out.println(" -- copy second part -- " +
				// c + " : " + r);
			}
		}
		// System.out.println(" -- return -- ");
		return returnMatrix;
	}

	public JMatrix removeColumns(int j, int d) {
		JMatrix matrix = (JMatrix) this.clone();
		try {
			for (int i = 1; i <= d; i++) {
				matrix = matrix.removeColumn(j);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return matrix;
	}

	/**
	 * Remove a particular column from the Matrix.
	 * 
	 * @param row
	 *            Description of the Parameter
	 * @return the matrix with the removed column.
	 * @exception PolylibException
	 *                if the column given doesn't exist in the current matrix
	 */
	public JMatrix removeRow(int row) throws PolylibException {
		if (row < 0 || row > _nbRows) {
			throw new PolylibException("Trying to remove non-existing "
					+ "lineas of a matrix");
		}
		JMatrix returnMatrix = new JMatrix(_nbRows - 1, _nbColumns);
		int j = 0;
		for (int i = 0; i < _nbRows; i++) {
			if (i != row) {
				returnMatrix.insertRows(getRows(i, i), j);
				j++;
			}
		}
		return returnMatrix.getRows(0, _nbRows - 2);
	}

	/**
	 * Remove a particular number of Rows from the Matrix.
	 * 
	 * @param i
	 *            Description of the Parameter
	 * @param j
	 *            Description of the Parameter
	 * @return the matrix with the removed column.
	 * @exception PolylibException
	 *                if the column given doesn't exist in the current matrix
	 */
	public JMatrix removeRows(int i, int j) throws PolylibException {
		if (i < 0 || i > _nbRows) {
			throw new PolylibException("Trying to remove non-existing "
					+ "lineas of a matrix");
		}
		if (j < 0 || j > _nbRows) {
			throw new PolylibException("Trying to remove non-existing "
					+ "lineas of a matrix");
		}
		JMatrix M = null;
		try {
			if (i > 0) {
				if (j < i - 1) {
					throw new PolylibException("You must remove at least "
							+ "0 columns.");
				}
				M = new JMatrix(0, nbColumns());
				JMatrix M1 = getRows(0, i - 1);
				JMatrix M2 = getRows(j + 1, -1);
				M.insertRows(M1, -1);
				M.insertRows(M2, -1);
			} else {
				M = new JMatrix(0, nbColumns());
				JMatrix M2 = getRows(j + 1, -1);
				M.insertRows(M2, -1);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return M;
	}

	/**
	 * Return a matrix with all columns that are zero removed.
	 * 
	 * @return the matrix with all columns rows removed.
	 * @throws PolylibException
	 *             if a column is removed that does not exit.
	 */
	public JMatrix removeZeroColumns() throws PolylibException {
		JMatrix returnMatrix = new JMatrix(this);
		int removedColumns = 0;
		for (int i = 0; i < _nbColumns; i++) {
			if (isColumnZero(i)) {
				returnMatrix = returnMatrix.removeColumn(i - removedColumns);
				removedColumns++;
			}
		}
		return returnMatrix;
	}

	/**
	 * Return a matrix with all rows that are zero removed.
	 * 
	 * @return the matrix with all zero rows removed.
	 * @throws PolylibException
	 *             if a row is removed that does not exit.
	 */
	public JMatrix removeZeroRows() throws PolylibException {
		JMatrix returnMatrix = new JMatrix(this);
		int removedRows = 0;
		for (int i = 0; i < _nbRows; i++) {
			if (isRowZero(i)) {
				returnMatrix = returnMatrix.removeRow(i - removedRows);
				removedRows++;
			}
		}
		return returnMatrix;
	}

	/**
	 * resize this matrix to a matrix with <code>rows</code> rows and
	 * <code>cols</code> columns.
	 * 
	 * @param rows
	 * @param cols
	 */
	public void resize(int rows, int cols) {
		JMatrix M = new JMatrix(this);
		_nbRows = rows;
		_nbColumns = cols;
		_element = new long[_nbRows][_nbColumns];
		for (int j = 0; j < _nbRows; j++) {
			for (int i = 0; i < _nbColumns; i++) {
				_element[j][i] = M._element[j][i];
			}
		}
	}

	/**
	 * multiplies this matrix fon the right handside with matrix a supplied A.
	 * 
	 * @param A
	 *            the matrix with which to multiply.
	 * @exception PolylibException
	 *                MyException If such and such occurs
	 */
	public void rMul(JMatrix A) throws PolylibException {
		// check whether or not the dimensions are correct.
		try {
			if (nbColumns() != A.nbRows()) {
				throw new PolylibException("Dimensions are not correct. "
						+ "Excepted dimension is (" + nbRows() + ","
						+ nbColumns() + ") but provided dimension is ("
						+ A.nbRows() + "," + A.nbColumns() + ")");
			}
			JMatrix B = new JMatrix(this);
			int rows = B.nbRows();
			int cols = A.nbColumns();
			setSize(rows, cols);
			long sum;
			for (int j = 0; j < rows; j++) {
				for (int i = 0; i < cols; i++) {
					sum = 0;
					for (int p = 0; p < B.nbColumns(); p++) {
						sum += B.getElement(j, p) * A.getElement(p, i);
					}
					setElement(j, i, sum);
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * overwrites the columns from column i with the matrix M.
	 * 
	 * @param i
	 *            The new cols value
	 * @param M
	 *            The new cols value
	 */
	public void setCols(int i, JMatrix M) {
		try {
			if (i + M.nbColumns() > nbColumns()) {
				throw new PolylibException("Matrix M does not fit in "
						+ "the current matrix");
			}
			if (M.nbRows() != nbRows()) {
				throw new PolylibException("Number of rows in M and "
						+ "the current matrix must be equal");
			}
			for (int c = 0; c < M.nbColumns(); c++) {
				for (int r = 0; r < M.nbRows(); r++) {
					setElement(r, c + i, M.getElement(r, c));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * sets the entry with the specified row and col to value. The enty at the
	 * top left corner has indices row=0 and col=0.
	 * 
	 * @param row
	 *            the row number.
	 * @param col
	 *            the column number.
	 * @param value
	 *            the value that is stored at location (row,col).
	 */
	public void setElement(int row, int col, long value) {
		_element[row][col] = value;
	}

/*	public void setIndexVector(IndexVector indexVector) {
		_indexVector = indexVector;
	}
*/
	// /////////////////////////////////////////////////////////////////

	/**
	 * sets this matrix to M.
	 * 
	 * @param M
	 *            The new matrix value
	 */
	// FIXME: strange function. Suspect that Clone is more appropriate.
	public void setMatrix(JMatrix M) {
		_nbRows = M._nbRows;
		_nbColumns = M._nbColumns;
		_element = new long[_nbRows][_nbColumns];
		for (int j = 0; j < _nbRows; j++) {
			for (int i = 0; i < _nbColumns; i++) {
				_element[j][i] = M._element[j][i];
			}
		}
	}

	// // public methods ///
	/**
	 * set the size of this matrix to <code>nRows</code> rows and
	 * <code>nColumns</code> columns and sets all entries to zero.
	 * 
	 * @param nRows
	 *            number of rows of the matrix.
	 * @param nColumns
	 *            number of columns of the matrix.
	 */
	public void setSize(int nRows, int nColumns) {
		_nbRows = nRows;
		_nbColumns = nColumns;
		_element = new long[nRows][nColumns];
	}

	/**
	 * Compute the sum of the entries of this matrix.
	 * 
	 * @return the sun of the entries.
	 */
	public long sum() {
		long sum = 0;
		for (int j = 0; j < nbRows(); j++) {
			for (int i = 0; i < nbColumns(); i++) {
				sum += getElement(j, i);
			}
		}
		return sum;
	}

	public JMatrix sumCols() {
		JMatrix matrix = new JMatrix(this._nbRows, 1);
		long buffer;
		for (int i = 0; i < this.getNbRows(); i++) {
			buffer = 0;
			for (int j = 0; j < this.getNbColumns(); j++) {
				buffer = buffer + this.getElement(i, j);
			}
			matrix.setElement(i, 0, buffer);
		}
		return matrix;
	}

	/**
	 * Returns a String object representing this JMatrix's value
	 * 
	 * @return a String.
	 */
	public String toFormat() {
		String aString = "";
		for (int row = 0; row < _nbRows; row++) {
			for (int col = 0; col < _nbColumns; col++) {
				if (_element[row][col] >= 0) {
					aString += "  " + _element[row][col];
				} else {
					aString += " " + _element[row][col];
				}
			}
			aString += "\n";
		}
		return aString;
	}

	/**
	 * Returns a vector of String object representing this JMatrix's value in
	 * terms of linear expressions that are either an equality or inequality. In
	 * case the matrix is empty, return only an empty list.
	 * 
	 * @param vector
	 *            a vector with the column names.
	 * @return a vector of Strings.
	 */
	public Vector toLinearExpression(Vector vector) {
		Vector linearExpressions = new Vector();
		try {
			if (!isEmpty()) {
				if (vector.size() + 2 != _nbColumns) {
					System.out.println(" JMATRIX: \n" + toString());
					throw new PolylibException("Vector size and Matrix size "
							+ "do not fit. The vector " + vector.toString()
							+ " contains " + vector.size() + " elements, "
							+ "while " + (_nbColumns - 2)
							+ " elements are expected");
				}
				for (int row = 0; row < _nbRows; row++) {
					Expression linExpression = new Expression();
					long value;
					for (int column = 1; column < _nbColumns - 1; column++) {
						value = _element[row][column];
						if (value != 0) {
							String term = vector.get(column - 1).toString();
							linExpression.addVariable((int) value, 1, term);
						}
					}
					value = _element[row][_nbColumns - 1];
					if (value != 0) {
						linExpression.addVariable((int) value, 1, "");
					}
					linExpression.setEqualityType((int) _element[row][0]);
					linearExpressions.add(linExpression);
				}
			}
		} catch (PolylibException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return linearExpressions;
	}

	/**
	 * returns a <code>String</code> representation of the in Matlab format.
	 * 
	 * @return the matrix as a string.
	 */
	public String toMatlabString() {
		String aString = "";
		for (int row = 0; row < _nbRows; row++) {
			for (int col = 0; col < _nbColumns; col++) {
				aString += _element[row][col];
				if (col < _nbColumns - 1) {
					aString += ", ";
				}
			}
			if (row < _nbRows - 1) {
				aString += "; ";
			}
		}
		return "[" + aString + "]";
	}

	/**
	 * Returns a String object representing this JMatrix's value
	 * 
	 * @return a String.
	 */
	public String toString() {
		String aString = "";
		for (int row = 0; row < _nbRows; row++) {
			for (int col = 0; col < _nbColumns; col++) {
				aString += " " + _element[row][col];
			}
			aString += "\n";
		}
		return aString;
	}

	/**
	 * returns a <code>String</code> representation of the in Matlab format.
	 * 
	 * @param prefix
	 *            Description of the Parameter
	 * @return the matrix as a string.
	 */
	public String toXMLString(String prefix) {
		String aString = "[";
		for (int row = 0; row < _nbRows; row++) {
			for (int col = 0; col < _nbColumns; col++) {
				aString += _element[row][col];
				if (col < _nbColumns - 1) {
					aString += ", ";
				}
			}
			if (row < _nbRows - 1) {
				aString += ";\n" + prefix;
			}
		}
		return aString + "]";
	}

	/**
	 * Return a transposed version of the matrix.
	 * 
	 * @return the transposed matrix.
	 */
	public JMatrix transpose() {
		JMatrix Temp = new JMatrix(nbColumns(), nbRows());
		for (int j = 0; j < nbRows(); j++) {
			for (int i = 0; i < nbColumns(); i++) {
				Temp.setElement(i, j, getElement(j, i));
			}
		}
		return Temp;
	}

	/**
	 * Removes the last column and the last row
	 * 
	 * @return a JMatrix where last column and last row has been removed
	 * @author gliem
	 */
	public JMatrix unformatPolyLib() {
		JMatrix temp = (JMatrix) this.clone();
		try {
			temp = temp.removeColumn(temp.getNbColumns() - 1);
			temp = temp.removeRow(temp.getNbRows() - 1);
		} catch (PolylibException e) {
			System.out.println("Exception " + e);
		}
		return temp;
	}

	// //////////////////////////////////////////////////////
	// // protected variables ////
	/**
	 * the elements of the matrix
	 */
	protected long[][] _element;

	/**
	 * The index vector of this Matrix
	 */
//	protected IndexVector _indexVector = null;

	/**
	 * the number of column of the matrix.
	 */
	protected int _nbColumns;

	/**
	 * the number of rows of the matrix.
	 */
	protected int _nbRows;

}
