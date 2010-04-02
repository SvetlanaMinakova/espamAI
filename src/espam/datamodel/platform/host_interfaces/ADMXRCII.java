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

package espam.datamodel.platform.host_interfaces;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADMXRCII

/**
 * This class describes a ADMXRCII communication component.
 *
 * @author Hristo Nikolov
 * @version  $Id: ADMXRCII.java,v 1.1 2010/04/02 12:21:24 nikolov Exp $
 */

public class ADMXRCII extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ADMXRCII component with a name.
     *
     */
    public ADMXRCII(String name) {
        super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
         x.visitComponent(this);
    }


    /**
     *  Clone this ADMXRCII
     *
     * @return  a new instance of the ADMXRCII.
     */
    public Object clone() {
            ADMXRCII newObj = (ADMXRCII) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the ADMXRCII.
     *
     * @return  a description of the ADMXRCII.
     */
    public String toString() {
        return "ADMXRCII host interface component: " + getName();
    }
}
