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

import espam.datamodel.graph.Node;
import espam.datamodel.domain.LBS;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGNode

/**
 * This class describes a node in an Approximated Dependence Graph (ADG)
 * The ADGNode is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.2 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGNode.java,v 1.2 2011/05/04 15:24:41 nikolov Exp $
 */

public class ADGNode extends Node {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an ADGNode with a name.
     *
     */
    public ADGNode(String name) {
    	super(name);
        _function = new ADGFunction("");
	_domain = new LBS();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this ADGNode
     *
     * @return  a new instance of the ADGNode.
     */
    public Object clone() {
        ADGNode newObj = (ADGNode) super.clone();
        newObj.setFunction( (ADGFunction) _function.clone() );
        newObj.setDomain( (LBS) _domain.clone() );
        return( newObj );
    }

    /**
     *  Return a new variable name with given prefix that is unique within this node.
     *
     * @param  prefix The prefix of the variable name
     * @return  the new variable name
     */
    public String uniqueVariableName(String prefix) {
	String name;
	for (int i = 0; ; ++i) {
	    name = prefix + i;
	    int j;
	    for (j = 0; j < _varNames.size(); ++j) {
		if (_varNames.elementAt(j).equals(name))
		    break;
	    }
	    if (j == _varNames.size())
		break;
	}
	_varNames.add(name);
	return name;
    }

    /**
     *  Get the function of an ADGNode.
     *
     * @return  the function
     */
    public ADGFunction getFunction() {
        return _function;
    }

    /**
     *  Set the function of an ADGNode.
     *
     * @param  function The new function
     */
    public void setFunction(ADGFunction function) {
        _function = function;
    }

    /**
     *  Get the domain of an ADGNode.
     *
     * @return  the domain
     */
    public LBS getDomain() {
        return _domain;
    }

    /**
     *  Set the domain of an ADGNode.
     *
     * @param  domain The new domain
     */
    public void setDomain(LBS domain) {
        _domain = domain;
    }

    /**
     *  Get the adg name to which an ADGNode belongs.
     *
     * @return  the _adgName
     */
    public String getADGName() {
        return _adgName;
    }

    /**
     *  Set the adg name to which an ADGNode belongs.
     *
     * @param  name The adg name
     */
    public void setADGName(String name) {
        _adgName = name;
    }

    /**
     *  Return a description of the ADGNode.
     *
     * @return  a description of the ADGNode.
     */
    public String toString() {
        return "ADGNode: " + getName();
    }
    
    /**
     *  Get the input ports of this ADGNode.
     *
     * @return  the input ports
     */
    public Vector getInPorts() {
        Vector v = new Vector();

        Iterator i = getPortList().iterator();
	while ( i.hasNext() ) {
	   ADGPort port = (ADGPort) i.next();
	   if( port instanceof ADGInPort ) {
	      v.add( (ADGInPort) port );
	   }
	}
	return v;
    }

    /**
     *  Get the output ports of this ADGNode.
     *
     * @return  the output ports
     */
    public Vector getOutPorts() {
        Vector v = new Vector();

        Iterator i = getPortList().iterator();
	while( i.hasNext() ) {
	   ADGPort port = (ADGPort) i.next();
	   if( port instanceof ADGOutPort ) {
	      v.add( (ADGOutPort) port);
	   }
	}
	return v;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Function associated with the node. See function "Fn"
     * on page 39 in [1] - Definition 2.2.2
     *
     */
    private ADGFunction _function = null;

    /**
     * The domain of the node. See domain "NDn"
     * on page 39 in [1] - Definition 2.2.2
     *
     */
    private LBS _domain = null;

    /**
     * List of (known) variables in this node.
     */
    private Vector<String> _varNames = new Vector<String>();

    /**
     * The adg name to which an ADGNode belongs.
     */

    private String _adgName = null;

}
