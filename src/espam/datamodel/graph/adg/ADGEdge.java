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

package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.LinearizationType;
import espam.datamodel.graph.Edge;

import espam.visitor.ADGraphVisitor;

import espam.utils.symbolic.matrix.JMatrix;

//////////////////////////////////////////////////////////////////////////
//// ADGEdge

/**
 * This class describes an edge in an Approximated Dependence Graph (ADG).
 * The ADGEdge is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.5 on page 40 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGEdge.java,v 1.3 2012/01/20 16:46:42 nikolov Exp $
 */

public class ADGEdge extends Edge {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an ADGEdge with a name.
     *
     */
    public ADGEdge(String name) {
    	super(name);
        _mapping = new JMatrix();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this ADGEdge
     *
     * @return  a new instance of the ADGEdge.
     */
    public Object clone() {
        ADGEdge newObj = (ADGEdge) super.clone();
        newObj.setMapping( (JMatrix) _mapping.clone() );
	newObj.setLinModel( _linearization );
        return( newObj );
    }

    /**
     *  Get the mapping matrix of an ADGEdge.
     *
     * @return  the mapping matrix
     */
    public JMatrix getMapping() {
        return _mapping;
    }

    /**
     *  Set the mapping matrix of an ADGEdge.
     *
     * @param  mapping The new mapping matrix
     */
    public void setMapping(JMatrix mapping) {
        _mapping = mapping;
    }

    /**
     *  Get the linearization model of an ADGEdge.
     *
     * @return  the linearization
     */
    public LinearizationType getLinModel() {
        return _linearization;
    }

    /**
     *  Set the size of an ADGEdge.
     *
     * @param  size
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     *  Get the size of an ADGEdge.
     *
     * @return  the size
     */
    public int getSize() {
        return _size;
    }

    /**
     *  Set the linearization model of an ADGEdge.
     *
     * @param  linearization
     */
    public void setLinModel(LinearizationType linearization) {
        _linearization = linearization;
    }

    /**
     *  Get the adg name to which an ADGEdge belongs.
     *
     * @return  the adgName
     */
    public String getADGName() {
        return _adgName;
    }

    /**
     *  Set the adg name to which an ADGEdge belongs.
     *
     * @param  name The adg name
     */
    public void setADGName(String name) {
        _adgName = name;
    }

    /**
     *  Return a description of the ADGEdge.
     *
     * @return  a description of the ADGEdge.
     */
    public String toString() {
        return "ADGEdge: " + getName();
    }

    /**
     *  Get the output port related to this ADGEdge.
     *
     * @return  the output port
     */
    public ADGOutPort getFromPort() {
        Iterator i = getPortList().iterator();
	while ( i.hasNext() ) {
	   ADGPort port = (ADGPort) i.next();
	   if (port instanceof ADGOutPort) {
	      return (ADGOutPort) port;
	   }
	}
	return null;
    }

    /**
     *  Get the input port related to this ADGEdge.
     *
     * @return  the input port
     */
    public ADGInPort getToPort() {
        Iterator i = getPortList().iterator();
	while ( i.hasNext() ) {
	   ADGPort port = (ADGPort) i.next();
	   if (port instanceof ADGInPort) {
	      return (ADGInPort) port;
	   }
	}
	return null;
    }

    /**
     *  Chcck if the edge is a self-edge
     *
     * @return  true if the edge is a self-edge, false otherwise
     */
    public boolean isSelfEdge() {
	if ( this.getFromPort().getNode().getName().equals( this.getToPort().getNode().getName() ) ) {
	    return true;
	} else {
	    return false;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Mapping matrix that defines the relation between the domains of
     *  the input port and the output port related to this edge.
     *  See mapping M in Definition 2.2.5 on page 40 in [1].
     */
    private JMatrix _mapping = null;
    private LinearizationType _linearization = null;
    /**
     *  Size required for the edge; -1 if unknown.
     */
    private int _size = -1;

    /**
     * The adg name to which an ADGEdge belongs.
     */

    private String _adgName = null;

}
