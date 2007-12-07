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
//// Crossbar

/**
 * This class describes a Crossbar communication component.
 *
 * @author Todor Stefanov
 * @version  $Id: Crossbar.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Crossbar extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Crossbar component with a name.
     *
     */
    public Crossbar(String name) {
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
     *  Clone this Crossbar
     *
     * @return  a new instance of the Crossbar.
     */
    public Object clone() {
            Crossbar newObj = (Crossbar) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the Crossbar.
     *
     * @return  a description of the Crossbar.
     */
    public String toString() {
        return "Crossbar Switch: " + getName();
    }
}
