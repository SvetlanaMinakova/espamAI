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

package espam.datamodel.platform.communication;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// AXICrossbar

/**
 * This class describes a AXICrossbar communication component.
 *
 * @author Todor Stefanov
 * @version  $Id: AXICrossbar.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXICrossbar extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a AXICrossbar component with a name.
     *
     */
    public AXICrossbar(String name) {
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
     *  Clone this AXICrossbar
     *
     * @return  a new instance of the AXICrossbar.
     */
    public Object clone() {
            AXICrossbar newObj = (AXICrossbar) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the AXICrossbar.
     *
     * @return  a description of the AXICrossbar.
     */
    public String toString() {
        return "AXI Crossbar Switch: " + getName();
    }
}
