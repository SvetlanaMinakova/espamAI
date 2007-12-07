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

// ////////////////////////////////////////////////////////////////////////
// // Polyhedron

/**
 * This class represents polyhedra in the Java programming language and is used
 * for the equivalence with the Polyhedron type used in the PolyLib.
 * <p>
 * 
 * A Polyhedron is a linked list of convex Polyhedra. In this way the polyhedron
 * is closed under union and difference operations. Every element of the linked
 * list is a convex polyhedron in the dual representation. That is, it is
 * simultaneously represented as a set of affine constraints and as a set of
 * rays and lines. There are no vertices here because the PolyLib presents the
 * polyhedra as equivalent homogeneous systems (convex cone model). The data
 * structure of the Polyhedron is represented by the following figure:
 * <p>
 * 
 * <center> <img src = "../../../../panda/polylib/javadoc/polyhedron.jpg">
 * </center> <br>
 * <center>This figure is an adapted version of Figure 3.1 in [1]</center>
 * <p>
 * 
 * The variables <code>constraint</code> and <code>ray</code> are matrices
 * that contain the actual system of constraints and rays. For the
 * <code>constraint</code> matrix a row represents either an equality or an
 * inequality. An equality is specified by setting the first element of the row
 * to zero and an inequality is specified by setting the first element of the
 * row to one.
 * <p>
 * 
 * A similar encoding is used for the <code>ray</code> matrix. Here a zero
 * stands for a line (a bidirectional ray) or a ray.
 * <p>
 * 
 * [1] <A HREF="http://www.irisa.fr/EXTERNE/bibli/pi/pi785.html">A Library for
 * doing Polyhedral Operations</A> , Doran K. Wilde, IRISA Research Report
 * #785.
 * <p>
 * 
 * 
 * 
 * @author Edwin Rypkema
 * @version $Id: Polyhedron.java,v 1.1 2007/12/07 22:06:46 stefanov Exp $
 */

public class Polyhedron {

	/**
	 * Construct an instance of Polyhedron.
	 */
	public Polyhedron() {
	}

	/**
	 * Constructor for the Polyhedron object
	 * 
	 * @param Dimension
	 *            Description of the Parameter
	 * @param NbConstraints
	 *            Description of the Parameter
	 * @param NbRays
	 *            Description of the Parameter
	 * @param NbEq
	 *            Description of the Parameter
	 * @param NbBid
	 *            Description of the Parameter
	 */
	public Polyhedron(int Dimension, int NbConstraints, int NbRays, int NbEq,
			int NbBid) {
		this.Dimension = Dimension;
		this.NbConstraints = NbConstraints;
		this.NbRays = NbRays;
		this.NbEq = NbEq;
		this.NbBid = NbBid;
		constraint = new long[NbConstraints][Dimension + 2];
		ray = new long[NbRays][Dimension + 2];
	}

	/**
	 * Constructor for the Polyhedron object
	 * 
	 * @param p
	 *            Description of the Parameter
	 */
	public Polyhedron(Polyhedron p) {
		next = p.next;

		Dimension = p.Dimension;

		NbConstraints = p.NbConstraints;

		NbRays = p.NbRays;

		NbEq = p.NbEq;

		NbBid = p.NbBid;

		constraint = p.constraint;

		ray = p.ray;

		head = p.head;

		tail = p.tail;
	}

	/**
	 * Add an polyhdron.
	 * 
	 * @param domain
	 *            Description of the Parameter
	 */
	public void add(Polyhedron domain) {
		tail.next = domain;
		// first last in list points to last
		tail = domain;
		// first tail in list point to last in list
		domain.head = head;
		// all heads point to head of first in list
	}

	/**
	 * cleans up the polyhedron and return the memory. This method is
	 * tail-recursive. It starts by removing the last Polyhedron in the list.
	 */
	public void clear() {

		// first clean up next polyhedron before cleaning this one.
		if (next != null) {
			next.clear();
			next = null;
		}
		constraint = null;
		ray = null;
	}

	// This part implements the interface java.utile.Iterator:

	/**
	 * returns an iterator for the Polyhedron. To iterate over all domains of a
	 * polyhedron <code>P</code> use the following piece of code:
	 * 
	 * <pre>
	 *<tt>
	 * Iterator i = P.domains();
	 * </tt> <tt>
	 *  while (i.hasNext() ) {
	 * </tt> <tt>
	 * System.out.println((Polyhedron) i.next());
	 * <tt>
	 *  }
	 * </tt>
	 * </pre>
	 *
	 * @return An instance of Iterator.
	 */
	public Iterator domains() {
		return new Iterator() {

			public boolean hasNext() {
				return (item != null);
			}

			public java.lang.Object next() {
				Polyhedron Tmp = item;
				item = item.next;
				return Tmp;
			}

			public void remove() {
			}

			Polyhedron item = head;
		};
	}

	/**
	 * @return Description of the Return Value
	 */
	public String enumerateToString() {
		int cnt = 0;
		String result = "";
		Iterator i = domains();
		while (i.hasNext()) {
			result += "Validity Domain(" + cnt++ + "): \n";
			result += (i.next()).toString();
		}
		return result;
	}

	/**
	 * @param row
	 *            The row number.
	 * @param col
	 *            The column number.
	 * @return The constraint value
	 */
	public long getConstraint(int row, int col) {
		return constraint[row][col];
	}

	/**
	 * @param row
	 *            The row number.
	 * @param col
	 *            The column number.
	 * @return The ray value
	 */
	public long getRay(int row, int col) {
		return ray[row][col];
	}

	/**
	 * Check whether this Polyhedron has a next.
	 * 
	 * @return true if a next exists; otherwise false;
	 */
	public boolean hasNext() {
		return (next != null);
	}

	/**
	 * Return the next polyhdron.
	 * 
	 * @return The polyhedron.
	 */
	public Polyhedron next() {
		return next;
	}

	/**
	 * construct a polyhedron which is a clone of polyhedron
	 * 
	 */
	// public Polyhedron( Polyhedron aP) {
	// }
	// /////////////////////////////////////////////////////////////////
	// // public methods ///
	/**
	 * @param row
	 *            The row number.
	 * @param col
	 *            The column number.
	 * @param value
	 *            The value.
	 */
	public void setConstraint(int row, int col, long value) {
		constraint[row][col] = value;
	}

	/**
	 * @param row
	 *            The row number.
	 * @param col
	 *            The column number.
	 * @param value
	 *            The val
	 */
	public void setRay(int row, int col, long value) {
		ray[row][col] = value;
	}

	/**
	 * Return the string representation of this polyhderon.
	 * 
	 * @return A string.
	 */
	public String toString() {
		String aString = "";
		aString += "Dimension:     " + Dimension + "\n";
		aString += "NbConstraints: " + NbConstraints + "\n";
		aString += "NbRays:        " + NbRays + "\n";
		aString += "NbEq:          " + NbEq + "\n";
		aString += "NbBid:         " + NbBid + "\n\n";
		for (int row = 0; row < NbConstraints; row++) {
			for (int col = 0; col < Dimension + 2; col++) {
				aString += constraint[row][col] + "\t";
			}
			aString += "\n";
			;
		}
		aString += "\n";
		for (int row = 0; row < NbRays; row++) {
			for (int col = 0; col < Dimension + 2; col++) {
				aString += ray[row][col] + "\t";
			}
			aString += "\n";
			;
		}
		return aString;
	}

	/**
	 */
	public long[][] constraint;

	/**
	 */
	public int Dimension;

	/**
	 */
	public Polyhedron head = this;

	/**
	 */
	public int NbBid;

	/**
	 */
	public int NbConstraints;

	/**
	 */
	public int NbEq;

	/**
	 */
	public int NbRays;

	// /////////////////////////////////////////////////////////////////
	// // public variables ////

	/**
	 */
	public Polyhedron next = null;

	/**
	 */
	public long[][] ray;

	/**
	 */
	public Polyhedron tail = this;

}
