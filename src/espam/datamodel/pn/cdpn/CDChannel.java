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

package espam.datamodel.pn.cdpn;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.pn.Channel;
import espam.datamodel.LinearizationType;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// CDChannel

/**
 * This class describes a channel in a CompaanDyn Process Network.
 * The CDChannel is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.5 on page 51 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDChannel.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class CDChannel extends Channel {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a CDChannel with a name.
     *
     */
    public CDChannel(String name) {
    	super(name);
        _adgEdgeList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CDPNVisitor x) {
          x.visitComponent(this);
    }

    /**
     *  Clone this CDChannle
     *
     * @return  a new instance of the CDChannel.
     */
    public Object clone() {
        CDChannel newObj = (CDChannel) super.clone();
        newObj.setAdgEdgeList( (Vector) _adgEdgeList.clone() );
        newObj.setCommunicationModel( _communicationModel );
        return (newObj);
    }

    /**
     *  Get the ADGEdge list of this CDChannel.
     *
     * @return  the ADGEdge list
     */
    public Vector getAdgEdgeList() {
        return _adgEdgeList;
    }

    /**
     *  Set the ADGEdge list of this CDChannel.
     *
     * @param  adgEdgeList The new ADGEdge list
     */
    public void setAdgEdgeList(Vector adgEdgeList) {
        _adgEdgeList = adgEdgeList;
    }

    /**
     *  Get the communication model of this CDChannel.
     *
     * @return  the communication model
     */
    public LinearizationType getCommunicationModel() {
        return _communicationModel;
    }

    /**
     *  Set the communication model of this CDChannel.
     *
     * @param  communicationModel The new communication model
     */
    public void setCommunicationModel(LinearizationType communicationModel) {
        _communicationModel = communicationModel;
    }

    /**
     *  Return a description of the CDChannel.
     *
     * @return  a description of the CDChannel.
     */
    public String toString() {
        return "CDChannel: " + getName();
    }

    /**
     *  Get the output gate related to this CDChannel.
     *
     * @return  the output gate
     */
    public CDOutGate getFromGate() {
        Iterator i = getGateList().iterator();
	while( i.hasNext() ) {
	   CDGate gate = (CDGate) i.next();
	   if( gate instanceof CDOutGate ) {
	      return( CDOutGate ) gate;
	   }
	}
	return null;
    }

    /**
     *  Get the input gate related to this CDChannel.
     *
     * @return  the input gate
     */
    public CDInGate getToGate() {
        Iterator i = getGateList().iterator();
	while( i.hasNext() ) {
	   CDGate gate = (CDGate) i.next();
	   if( gate instanceof CDInGate ) {
	      return( CDInGate ) gate;
	   }
	}
	return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List containing the ADG Edges that belong to this channel.
     *
     *  See "E" in Definition 2.4.5 on page 51 in [1].
     */
    private Vector _adgEdgeList = null;

    /**
     *  The communication model of this channel.
     *
     *  See "CM" in Definition 2.4.5 on page 52 in [1].
     */
    private LinearizationType _communicationModel = null;
}
