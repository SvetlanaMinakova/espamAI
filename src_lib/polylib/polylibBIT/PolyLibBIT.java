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

package espam.utils.polylib.polylibBIT;

import espam.utils.polylib.Enumeration;
import espam.utils.polylib.ParamPolyhedron;
import espam.utils.polylib.Polyhedron;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

// ////////////////////////////////////////////////////////////////////////
// // PolyLibBIT

/**
 * This class implements the BIT implemenation of Polylib. On a UNIX system, the
 * polylib implemenation uses a 64 bit representation. On a Windows platform a
 * 32 bit representaion is used.
 * 
 * @author Bart Kienhuis
 * @version $Id: PolyLibBIT.java,v 1.1 2007/12/07 22:09:25 stefanov Exp $
 */

public class PolyLibBIT {
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    /**
     * Map the java function Constraints2Polyhedron to its native function in
     * polylib.
     * 
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public static native Polyhedron Constraints2Polyhedron(JMatrix m);
    
    /**
     * Map the java function ConstraintsSimplify to its native function in
     * polylib.
     * 
     * @param domain
     *            Description of the Parameter
     * @param context
     *            Description of the Parameter
     * @return A matrix.
     */
    public static native JMatrix ConstraintsSimplify(JMatrix domain,
                                                     JMatrix context);
    
    /**
     * Map the java function DomainConvex to its native function in polylib.
     * 
     * @param d
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainConvex(Polyhedron d);
    
    /**
     * Map the java function DomainCopy to its native function in polylib.
     * 
     * @param d
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainCopy(Polyhedron d);
    
    /**
     * Map the java function DomainDifference to its native function in polylib.
     * 
     * @param d1
     *            A polyhedron.
     * @param d2
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainDifference(Polyhedron d1,
                                                     Polyhedron d2);
    
    /**
     * Map the java function DomainImage to its native function in polylib.
     * 
     * @param d
     *            A polyhedron.
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainImage(Polyhedron d, JMatrix m);
    
    /**
     * Map the java function DomainIntersection to its native function in
     * polylib.
     * 
     * @param d1
     *            A polyhedron.
     * @param d2
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainIntersection(Polyhedron d1,
                                                       Polyhedron d2);
    
    /**
     * Map the java function DomainPreimage to its native function in polylib.
     * 
     * @param d
     *            A polyhedron.
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainPreimage(Polyhedron d, JMatrix m);
    
    /**
     * Map the java function DomainSimplify to its native function in polylib.
     * 
     * @param d1
     *            A polyhedron.
     * @param d2
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainSimplify(Polyhedron d1, Polyhedron d2);
    
    /**
     * Map the java function DomainUnion to its native function in polylib.
     * 
     * @param d1
     *            A polyhedron.
     * @param d2
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron DomainUnion(Polyhedron d1, Polyhedron d2);
    
    /**
     * Map the java function EmptyPolyhedron to its native function in polylib.
     * 
     * @param Dimension
     *            Description of the Parameter
     * @return An empty polyhedron.
     */
    public static native Polyhedron EmptyPolyhedron(int Dimension);
    
    /**
     * Test whether the solution found by PIP can be trusted. In case PIP
     * observes that large numbers are created, it will flag the result false to
     * indicate that the result can not be trusted. A recalculation is need by
     * the GMP version of PIP.
     * 
     * @return true if solution is trusted by PIP; otherwise false.
     */
    public static native boolean isTrustedSolution();
    
    /**
     * Map the java function JMatrix2JMatrix to its native function in polylib.
     * 
     * @param aJMatrix
     *            A matrix.
     * @return A matrix.
     */
    public static native JMatrix JMatrix2JMatrix(JMatrix aJMatrix);
    
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
    public static native Enumeration LexSmallerEnumerate(SignedMatrix P,
                                                         SignedMatrix D, int dim, SignedMatrix C);
    
    /**
     * Map the java function Polyhedron2Constraints to its native function in
     * polylib.
     * 
     * @param d
     *            A polyhedron.
     * @return A matrix.
     */
    public static native JMatrix Polyhedron2Constraints(Polyhedron d);
    
    /**
     * Map the java function Polyhedron2ParamDomain to its native function in
     * polylib.
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
    public static native ParamPolyhedron Polyhedron2ParamDomain(Polyhedron d1,
                                                                Polyhedron d2);
    
    /**
     * Map the java function Polyhedron2ParamVertices to its native function in
     * polylib.
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
    public static native ParamPolyhedron Polyhedron2ParamVertices(
                                                                  Polyhedron d1, Polyhedron d2);
    
    /**
     * Map the java function Polyhedron2Polyhedron to its native function in
     * polylib.
     * 
     * @param aJPol
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron Polyhedron2Polyhedron(Polyhedron aJPol);
    
    /**
     * Map the java function PolyhedronEnumerate to its native function in
     * polylib.
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
    public static native Enumeration PolyhedronEnumerate(Polyhedron d1,
                                                         Polyhedron d2);
    
    /**
     * Map the java function PolyhedronScan to its native function in polylib.
     * 
     * @param d
     *            A polyhedron.
     * @param c
     *            A polyhedron.
     * @return A polyhedron.
     */
    public static native Polyhedron PolyhedronScan(Polyhedron d, Polyhedron c);
    
    /**
     * Map the java function Rays2Polyhedron to its native function in polylib.
     * 
     * @param m
     *            A matrix.
     * @return A polyhedron.
     */
    public static native Polyhedron Rays2Polyhedron(JMatrix m);
    
    /**
     * Map the java function UniversePolyhedron to its native function in
     * polylib.
     * 
     * @param Dimension
     *            Description of the Parameter
     * @return A universal polyhedron.
     */
    public static native Polyhedron UniversePolyhedron(int Dimension);
    
    // /////////////////////////////////////////////////////////////////
    // // private methods ///
    
    static {
        if (System.getProperty("os.name").equals("Windows XP")) {
            System.out.println("XP hack");
            System.loadLibrary("hypertrm");
        }
        System.loadLibrary("pandapolylib");
    }
}
