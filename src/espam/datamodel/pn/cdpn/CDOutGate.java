/*******************************************************************\
  * 
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
//// CDOutGate

/**
 * This class describes an output gate in a CompaanDyn Process Network
 * The CDOutGate is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.4 on page 51 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDOutGate.java,v 1.2 2011/10/20 12:08:44 mohamed Exp $
 */

public class CDOutGate extends CDGate {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CDOutGate with a name.
     *
     */
    public CDOutGate(String name) {
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
        CDOutGate newObj = (CDOutGate) super.clone();
        return( newObj );
    }
    
//  /**
//      *  get CDProcess of this gate
//      *
//      * @return  the CDProcess
//      */
//     public CDProcess getCDProcess() {
//   CDProcess cdproc = null;
//
//
//         assert (cdproc != null);
//         return cdproc;
//     }
    
    /**
     *  Return a description of the CDOutGate.
     *
     * @return  a description of the CDOutGate.
     */
    public String toString() {
        return "CDOutGate: " + getName();
    }
}
