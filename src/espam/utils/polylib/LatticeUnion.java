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

import java.util.Iterator;

// /////////////////////////////////////////////////////////////////////////////////
// LatticeUnion

/**
 * This class is used for equivalence with the LatticeUnion used in PolyLib.
 * 
 * @author Hui Li
 * @version $Id: LatticeUnion.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public class LatticeUnion {

	/**
	 * Constructor for the LatticeUnion object
	 */
	public LatticeUnion() {
		Lat = new Lattice();
	}

	/**
	 * Constructor for the LatticeUnion object
	 * 
	 * @param aLat
	 *            Description of the Parameter
	 */
	public LatticeUnion(Lattice aLat) {
		Lat = new Lattice(aLat);
	}

	/**
	 * Constructor for the LatticeUnion object
	 * 
	 * @param aLatUnion
	 *            Description of the Parameter
	 */
	public LatticeUnion(LatticeUnion aLatUnion) {
		Lat = new Lattice(aLatUnion.Lat);
		next = aLatUnion.next;
	}

	/**
	 * Description of the Method
	 * 
	 * @param aLatUnion
	 *            Description of the Parameter
	 */
	public void add(LatticeUnion aLatUnion) {

		tail.next = aLatUnion;
		tail = aLatUnion;
		aLatUnion.head = head;
	}

	/**
	 * Gets the lattice attribute of the LatticeUnion object
	 * 
	 * @return The lattice value
	 */
	public Lattice getLattice() {
		return Lat;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean hasNext() {
		return (next != null);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public Iterator LatUnion() {

		return new Iterator() {

			public boolean hasNext() {
				return (item != null);
			}

			public java.lang.Object next() {
				LatticeUnion temp = item;
				item = item.next;
				return temp;
			}

			public void remove() {
			}

			LatticeUnion item = head;
		};
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public LatticeUnion next() {
		return next;
	}

	// //////////////////////////////////////////////////////////
	// //// public methods //////

	/**
	 * Sets the lattice attribute of the LatticeUnion object
	 * 
	 * @param aLat
	 *            The new lattice value
	 */
	public void setLattice(Lattice aLat) {
		Lat = (Lattice) new Lattice(aLat);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public String toString() {

		String aString = "";
		aString += "LATTICE:" + "\n";
		aString += Lat.toString();

		return aString;
	}

	// //////////////////////////////////////////////////////////
	// //// public variables //////

	/**
	 * Description of the Field
	 */
	public LatticeUnion head = this;

	/**
	 * Description of the Field
	 */
	public Lattice Lat;

	/**
	 * Description of the Field
	 */
	public LatticeUnion next = null;

	/**
	 * Description of the Field
	 */
	public LatticeUnion tail = this;

}
