/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.datamodel.domain;

import java.util.Vector;

import espam.visitor.ADGraphVisitor;

import espam.utils.symbolic.matrix.SignedMatrix;

//////////////////////////////////////////////////////////////////////////
//// Polytope

/**
 * This class is a Polytope class used in ADG
 *
 *
 *
 * @author Hristo Nikolov
 * @version  $Id: Polytope.java,v 1.1 2007/12/07 22:09:02 stefanov Exp $
 */

public class Polytope implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create Polytope.
     *
     */
    public Polytope() {
        _indexVector = new IndexVector();
	_constraints = new SignedMatrix();
	_context = new SignedMatrix();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /** Compute the intersection of this and another Polytope
      * The Polytopes are assumed to have the same IndexVector,
      * with the exception that the static control vectors may be
      * different, provided they have no overlap.
      *
      * @param p The other Polytope
      * @return the intersection
      */
    public Polytope intersection(Polytope p) {
	assert _indexVector.getIterationVector().equals(
		    p._indexVector.getIterationVector());
	assert _indexVector.getDynamicCtrlVector().equals(
		    p._indexVector.getDynamicCtrlVector());
	assert _indexVector.getParameterVector().equals(
		    p._indexVector.getParameterVector());
	/* n0: number of initial static controls in common
	 * n1,n2: total number of static controls in this and p
	 */
	int n0 = 0, n1 = 0, n2 = 0;
	Vector<ControlExpression> cv1 = _indexVector.getStaticCtrlVector();
	Vector<ControlExpression> cv2 = p._indexVector.getStaticCtrlVector();
	IndexVector iv = (IndexVector) _indexVector.clone();
	/* If the static control vectors are not the same,
	 * no element may appear on both list for now.
	 */
	if (!cv1.equals(cv2)) {
	    n1 = cv1.size();
	    n2 = cv2.size();
	    for (n0 = 0; n0 < n1 && n0 < n2; ++n0) {
		if (!cv1.elementAt(n0).equals(cv2.elementAt(n0)))
		    break;
	    }
	    for (int j = n0; j < n2; ++j) {
		for (int i = n0; i < n1; ++i) {
		    assert(!cv1.elementAt(i).equals(cv2.elementAt(j)));
		}
		iv.getStaticCtrlVector().add(cv2.elementAt(j));
	    }
	}
	int dim = _indexVector.getIterationVector().size();
	Polytope i = new Polytope();
	SignedMatrix cons1 = (SignedMatrix) _constraints.clone();
	SignedMatrix cons2 = (SignedMatrix) p._constraints.clone();
	cons1.insertZeroColumns(n2-n0, 1+dim+n1);
	cons2.insertZeroColumns(n1-n0, 1+dim+n0);
	cons1.insertRows(cons2, -1);
	SignedMatrix ctx = (SignedMatrix) _context.clone();
	if (!ctx.equals(p._context)) {
	    ctx.insertRows(p._context, -1);
	}
	i.setConstraints(cons1);
	i.setContext(ctx);
	i.setIndexVector(iv);
	return i;
    }

    /**
     *  Clone this Polytope
     *
     * @return  a new instance of the Polytope.
     */
    public Object clone() {
        try {
            Polytope newObj = (Polytope) super.clone();
		    newObj.setConstraints( (SignedMatrix) _constraints.clone() );
		    newObj.setContext( (SignedMatrix) _context.clone() );
		    newObj.setIndexVector( (IndexVector) _indexVector.clone() );
		    return newObj;
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the constraints of this Polytope.
     *
     * @return  the contraint matrix
     */
    public SignedMatrix getConstraints() {
        return _constraints;
    }

    /**
     *  Set the contstraints of this polytope. 
     *
     * @param  constraints the contraint matrix
     */    public void setConstraints(SignedMatrix constraints) {
        _constraints = constraints;
    }

    /**
     *  Get the context of this polytope.
     *
     * @return  the context matrix
     */
    public SignedMatrix getContext() {
        return _context;
    }

    /**
     *  Set the context of this polytope. 
     *
     * @param  context the context matrix
     */    public void setContext(SignedMatrix context) {
        _context = context;
    }

    /**
     *  Get the index vector of this Polytope.
     *
     * @return  the index vector
     */
    public IndexVector getIndexVector() {
        return _indexVector;
    }

    /**
     *  Set the index vector of this Polytope.
     *
     * @param  IndexVector The new index vector
     */
    public void setIndexVector(IndexVector indexVector) {
        _indexVector = indexVector;
    }

    /**
     *  Return a description of the .
     *
     * @return  a description of the .
     */
    public String toString() {
        return "Polytope";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Constraints of the Polytope.
     */
    private SignedMatrix _constraints = null;

    /**
     *  Context of the Polytope.
     */
    private SignedMatrix _context = null;
    
    /**
     *  Index vector of the Polytope.
     */
    private IndexVector _indexVector = null;
}
