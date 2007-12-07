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

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// CDInGate

/**
 * This class describes an input gate in a CompaanDyn Process Network
 * The CDInGate is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.3 on page 51 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDInGate.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class CDInGate extends CDGate {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a CDInGate with a name.
     *
     */
    public CDInGate(String name) {
    	super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CDPNVisitor x) {
          x.visitComponent(this);
    }

    /**
     *  Clone this CDInGate
     *
     * @return  a new instance of the CDInGate
     */
    public Object clone() {
        CDInGate newObj = (CDInGate) super.clone();
        return (newObj);
    }

    /**
     *  Return a description of the CDInGate.
     *
     * @return  a description of the CDInGate.
     */
    public String toString() {
        return "CDInGate: " + getName();
    }
}
