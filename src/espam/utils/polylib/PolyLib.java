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

//import espam.utils.main.UserInterface;
//import espam.utils.polylib.barvinok.Barvinok;
import espam.utils.polylib.polylibBIT.PolylibBITImplementation;
//import espam.utils.polylib.polylibGMP.PolylibGMPImplementation;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

// ////////////////////////////////////////////////////////////////////////
// // PolyLib

/**
 * The class <code>PolyLib</code> contains function calls to the native
 * library PolyLib [ <A HREF="http://icps.u-strasbg.fr/PolyLib/">
 * http://icps.u-strasbg.fr/PolyLib/ </A> ]. The used version of the library
 * contains the computation of Ehrhart polynomials of parameterized polytopes [
 * <A HREF="http://icps.u-strasbg.fr/Ehrhart/">
 * http://icps.u-strasbg.fr/Ehrhart/ </A> ]. All descriptions of the functions
 * that are contained in the PolyLib are from [ <A
 * HREF="http://www.irisa.fr/EXTERNE/bibli/pi/pi785.html">1 </A> ] and [ <A
 * HREF="http://icps.u-strasbg.fr/~loechner/polylib/polylib-doc.ps.gz">2 </A>].
 * <p>
 * 
 * [1] <A HREF="http://www.irisa.fr/EXTERNE/bibli/pi/pi785.html">A Library for
 * doing Polyhedral Operations </A> , Doran K. Wilde, IRISA Research Report
 * #785.
 * <p>
 * 
 * [2] <A HREF="http://icps.u-strasbg.fr/~loechner/polylib/polylib-doc.ps.gz">
 * PolyLib: A Library for Manupulating Parameterized Polyherda </A> , Vincent
 * Loechner.
 * <p>
 * 
 * The first character of the function names implemented in the PolyLib are
 * capital. If two methods appears in the library having the same name except
 * that one function has a small first character in its name while the other has
 * a captital first character then the function with the small first charater
 * must be called from within Java programs. See for example the two functions
 * DomainSimplify and domainSimplify. The former is native to the interface and
 * is the actual interface to the PolyLib. The latter calls the former but
 * provides in addition error checking, it is a shell around the native
 * interface. The interface and the error checking can not be combined in the
 * same method because a native interface can not contain any code; it is a
 * declaration rather than an implementation.
 * 
 * @author Edwin Rypkema, Bart Kienhuis
 * @version $Id: PolyLib.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public class PolyLib {

	/**
	 * Constructor that is private because only a single version has to exist.
	 */
	private PolyLib() {
		_polylib = new PolylibBITImplementation();
	}

	/**
	 * returns the largest polyhedron which satisfies all of the constraints in
	 * matrix m. Described in section 4.4 of [1].
	 * 
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron Constraints2Polyhedron(SignedMatrix m) {
		return _polylib.Constraints2Polyhedron(m);
	}

	/**
	 * is the functional composition of Constraints2Polyhedron and
	 * DomainSimplify implemented in native C. This method avoids the continious
	 * conversion between C and Java.
	 * 
	 * @param domain
	 *            Description of the Parameter
	 * @param context
	 *            Description of the Parameter
	 * @return A matrix.
	 */
	public SignedMatrix ConstraintsSimplify(SignedMatrix domain,
			SignedMatrix context) {
		return new SignedMatrix(_polylib.ConstraintsSimplify((JMatrix) domain,
				(JMatrix) context));
	}

	/**
	 * returns the minimum polyhedron which encloses domain d. Described in
	 * section 4.9 of [1].
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainConvex(Polyhedron d) {
		return _polylib.DomainConvex(d);
	}

	/**
	 * returns a copy of domain d. Described in section 3.5 of [1].
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainCopy(Polyhedron d) {
		return _polylib.DomainCopy(d);
	}

	/**
	 * returns the domain difference, d1 less d2. The dimensions of domains d1
	 * and d2 must be the same. Described in section 4.7 of [1].
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainDifference(Polyhedron d1, Polyhedron d2) {
		return _polylib.DomainDifference(d1, d2);
	}

	/**
	 * calls DomainImage and performs additional error checking.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron domainImage(Polyhedron d, JMatrix m) {
		try {
			if (m.nbColumns() != d.Dimension + 1) {
				throw new PolylibException(
						"Number of columns of the matrix must be equal "
								+ "to the dimension of the polyhedron plus one");
			}
		} catch (PolylibException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return _polylib.DomainImage(d, m);
	}

	/**
	 * returns the image of domain d under affine transformation matrix m. The
	 * number of rows of matrix m must be equal to the dimension of the
	 * polyhedron plus one. Described in section 4.10 of [1].
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainImage(Polyhedron d, JMatrix m) {
		return _polylib.DomainImage(d, m);
	}

	/**
	 * returns the domain intersection of domains d1 and d2. The dimensions of
	 * domains d1 and d2 must be the same. Described in section 4.5 of [1].
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron domainIntersection(Polyhedron d1, Polyhedron d2) {
		try {
			if (d1.Dimension != d2.Dimension) {
				throw new PolylibException(
						"Dimensions of the Polyhedral domains must be the same.");
			}
		} catch (PolylibException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return _polylib.DomainIntersection(d1, d2);
	}

	/**
	 * returns the domain intersection of domains d1 and d2. The dimensions of
	 * domains d1 and d2 must be the same. Described in section 4.5 of [1].
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainIntersection(Polyhedron d1, Polyhedron d2) {
		return _polylib.DomainIntersection(d1, d2);
	}

	/**
	 * returns the preimage of domain d under affine transformation matrix m.
	 * The number of columns of matrix m must be equal to the dimension of the
	 * polyhedron plus one. Described in section 4.11 of [1].
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainPreimage(Polyhedron d, JMatrix m) {
		return _polylib.DomainPreimage(d, m);
	}

	/**
	 * calls DomainSimplify and performs additional error checking.
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 * @exception PolylibException
	 *                If the two polyhedra do not have the same dimensions.
	 */
	public Polyhedron domainSimplify(Polyhedron d1, Polyhedron d2)
			throws PolylibException {
		if (d1.Dimension != d2.Dimension) {
			// System.out.println("D1: " + d1.toString() );
			// System.out.println("D2: " + d2.toString() );
			throw new PolylibException("Dimension of the two polyhedra "
					+ "must be the same. Dimension Polyhedron d1: "
					+ d1.Dimension + " Dimension Polyhedron d2: "
					+ d2.Dimension);
		}
		return _polylib.DomainSimplify(d1, d2);
	}

	/**
	 * returns the domain equal to domain d1 simplified in the context of d2,
	 * i.e. all constraints in d1 that are not redundant with the constraints of
	 * d2. The dimensions of domains d1 and d2 must be the same. Described in
	 * section 4.8 of [1].
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainSimplify(Polyhedron d1, Polyhedron d2) {
		return _polylib.DomainSimplify(d1, d2);
	}

	/**
	 * returns the domain union of domains d1 and d2. The dimensions of domains
	 * d1 and d2 must be the same. Described in section 4.6 of [1].
	 * 
	 * @param d1
	 *            A polyhedron.
	 * @param d2
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron DomainUnion(Polyhedron d1, Polyhedron d2) {
		return _polylib.DomainUnion(d1, d2);
	}

	/**
	 * return the empty polyhedron of dimension n. Described in section 3.6.2 of
	 * [1].
	 * 
	 * @param dimension
	 *            Description of the Parameter
	 * @return An empty polyhedron.
	 */
	public Polyhedron EmptyPolyhedron(int dimension) {
		return _polylib.EmptyPolyhedron(dimension);
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
		return _polylib.LexSmallerEnumerate(P, D, dim, C);
	}

	/**
	 * returns the set of constrains representing the polyhedron. This method is
	 * not documented in [1].
	 * 
	 * @param d
	 *            A polyhedron.
	 * @return A matrix.
	 */
	public SignedMatrix Polyhedron2Constraints(Polyhedron d) {
		return new SignedMatrix(_polylib.Polyhedron2Constraints(d));
	}

	// public native void DomainFree( Polyhedron d );

	/**
	 * returns a parameterized polyhedron having a set of parameterized domains
	 * as its contents. Described in section 2 of [2].
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
		return _polylib.Polyhedron2ParamDomain(d1, d2);
	}

	/**
	 * returns a parameterized polyhedron having a set of parameterized vertices
	 * as its contents. Described in section 2 of [2].
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
		return _polylib.Polyhedron2ParamVertices(d1, d2);
	}


	// ------------------------------------------------------------------------
	// PFI part II (Polylib Function Interface for Ehrhart stuff)
	// ------------------------------------------------------------------------
	/**
	 * computes a set of validity domains and Ehrhart polynomials. Described in
	 * section 3 of [2].
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
		return _polylib.PolyhedronEnumerate(d1, d2);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	// ************************************
	// * PFI (Polylib Function Interface) *
	// ************************************

	/**
	 * Scan a polyhedron in the context of an other polyhedron.
	 * 
	 * @param d
	 *            A polyhedron.
	 * @param c
	 *            A polyhedron.
	 * @return A polyhedron.
	 */
	public Polyhedron PolyhedronScan(Polyhedron d, Polyhedron c) {
		return _polylib.PolyhedronScan(d, c);
	}

	/**
	 * returns the smallest polyhedron which includes all of the vertices, rays,
	 * and lines in matrix m. Described in section 4.4 of [1].
	 * 
	 * @param m
	 *            A matrix.
	 * @return A polyhedron.
	 */
	public Polyhedron Rays2Polyhedron(SignedMatrix m) {
		return _polylib.Rays2Polyhedron((JMatrix) m);
	}

	/**
	 * return the universal polyhedron of dimension n. Described in section
	 * 3.6.3 of [1].
	 * 
	 * @param dimension
	 *            Description of the Parameter
	 * @return A universal polyhedron.
	 */
	public Polyhedron UniversePolyhedron(int dimension) {
		return _polylib.UniversePolyhedron(dimension);
	}

	private PolylibInterface _bitPolylib;

	// /////////////////////////////////////////////////////////////////
	// // private methods ///

	/**
	 * Return the singleton instance of this class;
	 * 
	 * @return the instance.
	 */
	public static PolyLib getInstance() {
/*		if (UserInterface.getInstance().getGMPFlag()) {
			if (_polylib instanceof PolylibBITImplementation) {
				_polylib = new PolylibGMPImplementation();
			}
		} else {
			if (_polylib instanceof PolylibGMPImplementation) {
				_polylib = new PolylibBITImplementation();
			}
		}
*/
		return _instance;
	}

	/**
	 * Create a unique instance of this class to implement a singleton
	 */
	public static PolyLib _instance = new PolyLib();

	// /////////////////////////////////////////////////////////////////
	// // private variables ///

	private static PolylibInterface _polylib;

}
