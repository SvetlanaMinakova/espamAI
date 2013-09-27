/*******************************************************************\
  * 
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

import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

// ////////////////////////////////////////////////////////////////////////
// // PolylibInterface

/**
 * @author Bart Kienhuis
 * @version $Id: PolylibInterface.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public interface PolylibInterface {
    
    /**
     * returns the largest polyhedron which satisfies all of the constraints in
     * matrix m. Described in section 4.4 of [1].
     * 
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public Polyhedron Constraints2Polyhedron(JMatrix m);
    
    /**
     * is the functional composition of Constraints2Polyhedron and
     * DomainSimplify implemented in C. This method avoids the continious
     * conversion between C and Java.
     * 
     * @param domain
     *            Description of the Parameter
     * @param context
     *            Description of the Parameter
     * @return A matrix.
     */
    public JMatrix ConstraintsSimplify(JMatrix domain, JMatrix context);
    
    /**
     * returns the minimum polyhedron which encloses domain d. Described in
     * section 4.9 of [1].
     * 
     * @param d
     *            A polyhedron.
     * @return A polyhedron.
     */
    public Polyhedron DomainConvex(Polyhedron d);
    
    /**
     * returns a copy of domain d. Described in section 3.5 of [1].
     * 
     * @param d
     *            A polyhedron.
     * @return A polyhedron.
     */
    public Polyhedron DomainCopy(Polyhedron d);
    
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
    public Polyhedron DomainDifference(Polyhedron d1, Polyhedron d2);
    
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
    public Polyhedron DomainImage(Polyhedron d, JMatrix m);
    
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
    public Polyhedron DomainIntersection(Polyhedron d1, Polyhedron d2);
    
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
    public Polyhedron DomainPreimage(Polyhedron d, JMatrix m);
    
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
    public Polyhedron DomainSimplify(Polyhedron d1, Polyhedron d2);
    
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
    public Polyhedron DomainUnion(Polyhedron d1, Polyhedron d2);
    
    /**
     * return the empty polyhedron of dimension n. Described in section 3.6.2 of
     * [1].
     * 
     * @param dimension
     *            Description of the Parameter
     * @return An empty polyhedron.
     */
    public Polyhedron EmptyPolyhedron(int dimension);
    
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
                                           int dim, SignedMatrix C);
    
    /**
     * returns the set of constrains representing the polyhedron. This method is
     * not documented in [1].
     * 
     * @param d
     *            A polyhedron.
     * @return A matrix.
     */
    public JMatrix Polyhedron2Constraints(Polyhedron d);
    
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
    public ParamPolyhedron Polyhedron2ParamDomain(Polyhedron d1, Polyhedron d2);
    
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
    public ParamPolyhedron Polyhedron2ParamVertices(Polyhedron d1, Polyhedron d2);
    
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
    public Enumeration PolyhedronEnumerate(Polyhedron d1, Polyhedron d2);
    
    /**
     * Scan a polyhedron in the context of an other polyhedron.
     * 
     * @param d
     *            A polyhedron.
     * @param c
     *            A polyhedron.
     * @return A polyhedron.
     */
    public Polyhedron PolyhedronScan(Polyhedron d, Polyhedron c);
    
    /**
     * returns the smallest polyhedron which includes all of the vertices, rays,
     * and lines in matrix m. Described in section 4.4 of [1].
     * 
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public Polyhedron Rays2Polyhedron(JMatrix m);
    
    /**
     * return the universal polyhedron of dimension n. Described in section
     * 3.6.3 of [1].
     * 
     * @param dimension
     *            Description of the Parameter
     * @return A universal polyhedron.
     */
    public Polyhedron UniversePolyhedron(int dimension);
    
}
