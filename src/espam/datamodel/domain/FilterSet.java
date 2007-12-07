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
//// FilterSet

/**
 * This class contains a filtering set of the dynamic conditions in ADG
 *
 * @author Hristo Nikolov
 * @version  $Id: FilterSet.java,v 1.1 2007/12/07 22:09:02 stefanov Exp $
 */

public class FilterSet implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a FilterSet.
     *
     */
    public FilterSet() {
        _indexVector = new IndexVector();
	_constraints = new SignedMatrix();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof FilterSet))
	    return false;
	FilterSet o = (FilterSet) obj;
	return _indexVector.equals(o._indexVector)
	    && _constraints.equals(o._constraints);
    }

    /**
     *  Clone this FilterSet
     *
     * @return  a new instance of the FilterSet.
     */
    public Object clone() {
        try {
            FilterSet newObj = (FilterSet) super.clone();
		    newObj.setConstraints( (SignedMatrix) _constraints.clone() );
		    newObj.setIndexVector( (IndexVector) _indexVector.clone() );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return(null);
    }

    /**
     *  Get the constraints of this FilterSet.
     *
     * @return  the contraint matrix
     */
    public SignedMatrix getConstraints() {
        return _constraints;
    }

    /**
     *  Set the contstraints of this FilterSet. 
     *
     * @param  constraints the contraint matrix
     */    public void setConstraints(SignedMatrix constraints) {
        _constraints = constraints;
    }

    /**
     *  Get the index vector of this FilterSet.
     *
     * @return  the index vector
     */
    public IndexVector getIndexVector() {
        return _indexVector;
    }

    /**
     *  Set the index vector of thris FilterSet.
     *
     * @param  indexVector The new indexVector
     */
    public void setIndexVector(IndexVector indexVector) {
        _indexVector = indexVector;
    }

    /**
     *  Return a description 
     *
     * @return  a description 
     */
    public String toString() {
        return "FilterSet";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Constraint matrix of the FilterSet.
     */
    private SignedMatrix _constraints = null;
   
    /**
     *  IndexVector of the FilterSet.
     */
    private IndexVector _indexVector = null;
}
