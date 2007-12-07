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

// ////////////////////////////////////////////////////////////////////////
// // MatlabVisitor

/**
 * This class represents parameterized polyhedra in the Java programming
 * language and is used for the equivalence with the ParamPolyhedron type used
 * in the PolyLib. The class definitions are shown here, but are NOT yet
 * implemented in the interface.
 * 
 * @author Edwin Rypkema
 * @version $Id: ParamPolyhedron.java,v 1.1 2007/12/07 22:06:46 stefanov Exp $
 */

public class ParamPolyhedron {

	/**
	 * Sets the paramDomain attribute of the ParamPolyhedron object
	 * 
	 * @param Din
	 *            The new paramDomain value
	 */
	public void setParamDomain(ParamDomain Din) {
		D = Din;
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Sets the paramVertices attribute of the ParamPolyhedron object
	 * 
	 * @param Vin
	 *            The new paramVertices value
	 */
	public void setParamVertices(ParamVertices Vin) {
		V = Vin;
	}

	/**
	 * linked list of validity domains.
	 */
	public ParamDomain D = null;

	/**
	 * linked list of parameterized vertices.
	 */
	public ParamVertices V = null;

	// /////////////////////////////////////////////////////////////////
	// // public variables ////

	/**
	 * number of parameterized vertices.
	 */
	public int nbV = 0;

}
