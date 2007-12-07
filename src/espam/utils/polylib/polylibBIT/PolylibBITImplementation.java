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

package espam.utils.polylib.polylibBIT;

import espam.utils.polylib.Enumeration;
import espam.utils.polylib.ParamPolyhedron;
import espam.utils.polylib.Polyhedron;
import espam.utils.polylib.PolylibInterface;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

// ////////////////////////////////////////////////////////////////////////
// // PolylibBITImplemenation

/**
 * This class realizes the implemenation of the BIT version of Polylib. It
 * delegates the function calls to the bit true version of the polylib library.
 * On a UNIX/Linux system, polylib uses a 64 bit representaion. On a Windows
 * system, polylib uses a 32 bit representation.
 * 
 * @author Bart Kienhuis
 * @version $Id: PolylibBITImplementation.java,v 1.8 2005/10/05 11:14:38
 *          sverdool Exp $
 */

public class PolylibBITImplementation implements PolylibInterface {

	/**
	 * Constructor for the PolylibBITImplementation object
	 */
	public PolylibBITImplementation() {
		//System.out.println("PolylibBITImplementation()");
		_polylib = new PolyLibBIT();
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Delegates Constraints2Polyhedron to the BIT version of the polylib
	 * library.
	 * 
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron Constraints2Polyhedron(JMatrix m) {
		Polyhedron p = PolyLibBIT.Constraints2Polyhedron(m);
		return p;
	}

	/**
	 * Delegates ConstraintsSimplify to the BIT version of the polylib library.
	 * 
	 * @param domain
	 *            Description of the Parameter
	 * @param context
	 *            Description of the Parameter
	 * @return A matrix.
	 */
	public JMatrix ConstraintsSimplify(JMatrix domain, JMatrix context) {
		return PolyLibBIT.ConstraintsSimplify(domain, context);
	}

	/**
	 * Delegates DomainConvex to the BIT version of the polylib library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainConvex(Polyhedron d) {
		return PolyLibBIT.DomainConvex(d);
	}

	/**
	 * Delegates DomainCopy to the BIT version of the polylib library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainCopy(Polyhedron d) {
		return PolyLibBIT.DomainCopy(d);
	}

	/**
	 * Delegates DomainDifference to the BIT version of the polylib library.
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainDifference(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.DomainDifference(d1, d2);
	}

	/**
	 * Delegates DomainImage to the BIT version of the polylib library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainImage(Polyhedron d, JMatrix m) {
		return PolyLibBIT.DomainImage(d, m);
	}

	/**
	 * Delegates DomainIntersection to the BIT version of the polylib library.
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainIntersection(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.DomainIntersection(d1, d2);
	}

	/**
	 * Delegates DomainPreimage to the BIT version of the polylib library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainPreimage(Polyhedron d, JMatrix m) {
		return PolyLibBIT.DomainPreimage(d, m);
	}

	/**
	 * Delegates DomainSimplify to the BIT version of the polylib library.
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainSimplify(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.DomainSimplify(d1, d2);
	}

	/**
	 * Delegates DomainUnion to the BIT version of the polylib library.
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainUnion(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.DomainUnion(d1, d2);
	}

	/**
	 * Delegates EmptyPolyhedron to the BIT version of the polylib library.
	 * 
	 * @param dimension
	 *            Description of the Parameter
	 * @return An empty polyhedron.
	 */
	public Polyhedron EmptyPolyhedron(int dimension) {
		return PolyLibBIT.EmptyPolyhedron(dimension);
	}

	/**
	 * Computes the number of integer points in P lexicographically smaller than
	 * an arbitrary integer point in D
	 * 
	 * @param P
	 *            the (parametric) polyhedron of dimension n+m to be enumerated.
	 * @param D
	 *            the evaluation domain of dimension n'+m.
	 * @param dim
	 *            the number of dimensions taking into account for counting. The
	 *            remaining (n-dim) dimensions are assumed to be control
	 *            variables. Both n and n' need to be at least dim.
	 * @param C
	 *            the context of dimension m.
	 * @return an enumeration that can be evaluated for each point in D
	 */
	public Enumeration LexSmallerEnumerate(SignedMatrix P, SignedMatrix D,
			int dim, SignedMatrix C) {
		return PolyLibBIT.LexSmallerEnumerate(P, D, dim, C);
	}

	/**
	 * Delegates Polyhedron2Constraints to the BIT version of the polylib
	 * library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A matrix.
	 */
	public JMatrix Polyhedron2Constraints(Polyhedron d) {
		return PolyLibBIT.Polyhedron2Constraints(d);
	}

	/**
	 * Delegates Polyhedron2ParamDomain to the BIT version of the polylib
	 * library.
	 * 
	 * @param d1
	 *            the combined polyhedron of dimension n+m where n is the size
	 *            of the index space, and m the number of parameters.
	 * @param d2
	 *            the context polyhedron, of size m: this polyhedron * contains
	 *            the constraints on the parameters. If there are no *
	 *            constraints, d2 must be equal to the universe polyhedron of *
	 *            dimension m.
	 * @return a parameterized polyhedron containing a linked list of the
	 *         parameterized domains of d1.
	 */
	public ParamPolyhedron Polyhedron2ParamDomain(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.Polyhedron2ParamDomain(d1, d2);
	}

	/**
	 * Delegates Polyhedron2ParamVertices to the BIT version of the polylib
	 * library.
	 * 
	 * @param d1
	 *            the combined polyhedron of dimension n+m where n is the size
	 *            of the index space, and m the number of parameters.
	 * @param d2
	 *            the context polyhedron, of size m: this polyhedron contains
	 *            the constraints on the parameters. If there are no
	 *            constraints, d2 must be equal to the universe polyhedron of
	 *            dimension m.
	 * @return a parameterized polyhedron containing a linked list of the
	 *         parameterized vertices of d1.
	 */
	public ParamPolyhedron Polyhedron2ParamVertices(Polyhedron d1, Polyhedron d2) {
		return PolyLibBIT.Polyhedron2ParamVertices(d1, d2);
	}

	/**
	 * Delegates PolyhedronEnumerate to the BIT version of the polylib library.
	 * 
	 * @param d1
	 *            the combined polyhedron of dimension n+m. It must of course be
	 *            a parameterized polytope.
	 * @param d2
	 *            the context polyhedron, of size m. This polyhedron contains
	 *            the constraints on the parameters. If there are no
	 *            supplementary constraints, you have to provide the universe
	 *            polyhedron of dimension m.
	 * @return an enumeration that is composed of the validity domains and
	 *         Ehrhart polynomials.
	 */
	public Enumeration PolyhedronEnumerate(Polyhedron d1, Polyhedron d2) {
		Enumeration e = PolyLibBIT.PolyhedronEnumerate(d1, d2);
		return PolyLibBIT.PolyhedronEnumerate(d1, d2);
	}

	/**
	 * Delegates PolyhedronScan to the BIT version of the polylib library.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param c
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron PolyhedronScan(Polyhedron d, Polyhedron c) {
		return PolyLibBIT.PolyhedronScan(d, c);
	}

	/**
	 * Delegates Rays2Polyhedron to the BIT version of the polylib library.
	 * 
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron Rays2Polyhedron(JMatrix m) {
		return PolyLibBIT.Rays2Polyhedron(m);
	}

	/**
	 * Delegates UniversePolyhedron to the BIT version of the polylib library.
	 * 
	 * @param dimension
	 *            Description of the Parameter
	 * @return A universal polyhedron.
	 */
	public Polyhedron UniversePolyhedron(int dimension) {
		return PolyLibBIT.UniversePolyhedron(dimension);
	}

	private PolyLibBIT _polylib = null;

	// /////////////////////////////////////////////////////////////////
	// // private methods ///

	private static int _case = 0;
}
