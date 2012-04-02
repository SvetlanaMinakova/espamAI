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

package espam.datamodel.platform.controllers;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// AXI2AXI_CTRL

/**
 * This class is a controller component that is used to connect a read port
 * of a CompaanHWNode to a port of a crossbar switch.
 *
 * @author Todor Stefanov
 * @version  $Id: AXI2AXI_CTRL.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXI2AXI_CTRL extends Controller {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a AXI2AXI_CTRL with a name.
     *
     */
    public AXI2AXI_CTRL(String name) {
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
     *  Clone this AXI2AXI_CTRL
     *
     * @return  a new instance of the ReadCrosbarController.
     */
    public Object clone() {
            AXI2AXI_CTRL newObj = (AXI2AXI_CTRL) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the AXI2AXI_CTRL.
     *
     * @return  a description of the AXI2AXI_CTRL.
     */
    public String toString() {
        return "AXI2AXI_CTRL: " + getName();
    }

}
