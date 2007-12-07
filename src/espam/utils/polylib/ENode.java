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

package espam.utils.polylib;

import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // ENode

/**
 * This class implements the ENode used in the PolyLib.
 * 
 * @author Edwin Rypkema
 * @version $Id: ENode.java,v 1.1 2007/12/07 22:06:48 stefanov Exp $
 * @see PolyLib
 */

public class ENode {

	/**
	 * Construct a ENode.
	 * 
	 * @param newSize
	 * @param newPos
	 * @param newType
	 */
	public ENode(int newSize, int newPos, String newType) {
		type = newType;
		size = newSize;
		pos = newPos;
		arr = new EValue[size];
	}

	/**
	 * Description of the Method
	 */
	public void clear() {
		for (int i = 0; i < size; i++) {
			arr[i].clear();
		}
		type = null;
		arr = null;
	}

	/**
	 * Gets the arr attribute of the ENode object
	 * 
	 * @return The arr value
	 */
	public EValue[] getArr() {
		return arr;
	}

	/**
	 * Gets the pos attribute of the ENode object
	 * 
	 * @return The pos value
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * Gets the size attribute of the ENode object
	 * 
	 * @return The size value
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gets the type attribute of the ENode object
	 * 
	 * @return The type value
	 */
	public String getType() {
		return type;
	}

	/**
	 * Check whether this ENode is Pseudo.
	 * 
	 * @return true id pseudo; false otherwise.
	 */
	public boolean isPseudo() {
		boolean isPseudo = false;
		if (type.equals("periodic")) {
			isPseudo = true;
		} else {
			for (int i = size - 1; i >= 0; i--) {
				isPseudo = arr[i].isPseudo();
				if (isPseudo) {
					break;
				}
			}
		}
		return isPseudo;
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Set a ENode to a particular value.
	 * 
	 * @param i
	 * @param newEValue
	 */
	public void setEValue(int i, EValue newEValue) {
		arr[i] = newEValue;
	}

	/**
	 * Returns a String object representing this EValue's value. In getting the
	 * representation, a list of parameter is needed.
	 * 
	 * @param parameter
	 *            list of parameters.
	 * @return a String.
	 */
	public String toOneLineString(Vector parameter) {
		String aString = "";
		if (type.equals("periodic")) {
			for (int i = 0; i < size; i++) {
				aString += arr[i].toOneLineString(parameter);
				if (i != size - 1) {
					aString += ", ";
				}
			}
			aString = "[ " + aString + " ]_" + parameter.elementAt(pos - 1);
		} else {
			// polynomials are scanned with highest power first
			for (int i = size - 1; i >= 0; i--) {
				aString += arr[i].toOneLineString(parameter);
				if (i == 1) {
					aString += " * " + parameter.elementAt(pos - 1) + " + ";
				} else if (i > 1) {
					aString += " * " + parameter.elementAt(pos - 1) + "^" + i
							+ " + ";
				}
			}
			aString = "( " + aString + " )";
		}
		return aString;
	}

	/**
	 * @param parameter
	 * @param coeff
	 * @return Description of the Return Value
	 */
	public String toPolynomial(Vector parameter, String coeff) {
		String newCoeff = "";
		String aString = "";

		try {
			if (type.equals("periodic")) {

				throw new PolylibException("Currently, we don't accept "
						+ "pseudo polynomial here!");
			} else {
				// polynomials are scanned with highest power first
				for (int i = size - 1; i >= 0; i--) {
					if (i == 0) {
						newCoeff = coeff;
					} else {
						if (i == 1) {
							newCoeff = coeff + parameter.elementAt(pos - 1)
									+ " * ";
						} else {
							newCoeff = coeff + parameter.elementAt(pos - 1)
									+ "^" + i + " * ";
						}
					}
					// System.out.println("newCoeff = " + newCoeff);
					aString += arr[i].toPolynomial(parameter, newCoeff);
					if (i != 0) {
						aString += " + ";
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return aString;
	}

	/**
	 * Returns a String object representing this EValue's value. In getting the
	 * representation, a list of parameter is needed.
	 * 
	 * @param parameter
	 *            list of parameters.
	 * @return a String.
	 */
	public String toString(Vector parameter) {
		String aString = "";
		if (type.equals("periodic")) {
			for (int i = 0; i < size; i++) {
				aString += arr[i].toString(parameter);
				if (i != size - 1) {
					aString += ", ";
				}
			}
			aString = "[ " + aString + " ]_" + parameter.elementAt(pos - 1);
			;
		} else {
			// polynomials are scanned with highest power first
			for (int i = size - 1; i >= 0; i--) {
				aString += arr[i].toString(parameter);
				if (i == 1) {
					aString += " * " + parameter.elementAt(pos - 1) + " + ";
				} else if (i > 1) {
					aString += " * " + parameter.elementAt(pos - 1) + "^" + i
							+ " + ";
				}
			}
			aString = "( " + aString + " )";
		}
		return aString;
	}

	/**
	 */
	private EValue arr[];

	/**
	 */
	private int pos;

	/**
	 */
	private int size;

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 */
	private String type = "";

}
