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
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

import espam.utils.symbolic.matrix.SignedMatrix;

//////////////////////////////////////////////////////////////////////////
//// LBS

/**
 * This class is a Linear Bound Set of used in ADG
 *
 * @author Hristo Nikolov
 * @version  $Id: LBS.java,v 1.1 2007/12/07 22:09:02 stefanov Exp $
 */

public class LBS implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a LBS.
     *
     */
    public LBS() {
        _linearBound = new Vector();
        _filterSet = new FilterSet();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /** Intersect this LBS with another LBS
      * Both are currently assumed to have the same filterset
      * and each Polytope is assumed to have the same IndexVector.
      *
      * @param o The other LBS
      */
    public void intersect(LBS o) {
	assert _filterSet.equals(o._filterSet);
	Vector n = new Vector();
        Iterator i = getLinearBound().iterator();
        Iterator j = o.getLinearBound().iterator();
        while (i.hasNext()) {
	    Polytope p1 = (Polytope) i.next();
	    while (j.hasNext()) {
		Polytope p2 = (Polytope) j.next();
		n.add(p1.intersection(p2));
	    }
	}
	setLinearBound(n);
    }

    /**
     *  Clone this LBS
     *
     * @return  a new instance of the IndexPort.
     */
    public Object clone() {
        try {
            LBS newObj = (LBS) super.clone();
		    newObj.setLinearBound( (Vector) _linearBound.clone() );
		    newObj.setFilterSet( (FilterSet) _filterSet.clone() );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the vector of Polytopes of this LBS.
     *
     * @return  the vector of Polytopes
     */
    public Vector<Polytope> getLinearBound() {
        return _linearBound;
    }

    /**
     *  Set the vector of Polytopes of this LBS. 
     *
     * @param  Polytope the new vector of Polytopes
     */    
    public void setLinearBound(Vector<Polytope> polytope) {
        _linearBound = polytope;
    }

    /**
     *  Get the filter set of this LBS.
     *
     * @return  the filterSet
     */
    public FilterSet getFilterSet() {
        return _filterSet;
    }

    /**
     *  Set the filter set of this LBS. 
     *
     * @param  filterSet the new filterSet
     */    public void setFilterSet(FilterSet filterSet) {
        _filterSet = filterSet;
    }

    /**
     *  Get the iteration vector of this LBS.
     *
     *  This iteration vector is the same over all polytopes in linearBound
     *  and the filterSet.
     *
     *	@return the iteration vector
     */
    public Vector<String> getIterationVector() {
	int i;
	Vector<String> iv;

	assert getLinearBound().size() > 0;
	iv =  getLinearBound().get(0).getIndexVector().getIterationVector();
	for (i = 1; i < getLinearBound().size(); ++i) {
	    assert iv.equals(getLinearBound().get(i).
			     getIndexVector().getIterationVector());
	}
	// assert iv.equals(getFilterSet().getIndexVector().getIterationVector());

	return iv;
    }

    /**
     *  Return a description of the .
     *
     * @return  a description of the .
     */
    public String toString() {
        return "LBS";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The vector of Polytopes of the LBS.
     */
    private Vector<Polytope> _linearBound = null;
   
    /**
     *  The filterSet of the LBS.
     */
    private FilterSet _filterSet = null;
}
