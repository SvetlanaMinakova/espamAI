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
//// MemeoryController

/**
 * This class is a generic memory controller component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: MemoryController.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class MemoryController extends Controller {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a MemoryController with a name.
     *
     */
    public MemoryController(String name) {
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
     *  Clone this MemoryController
     *
     * @return  a new instance of the Controller.
     */
    public Object clone() {
            MemoryController newObj = (MemoryController) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the MemoryController.
     *
     * @return  a description of the MemoryController.
     */
    public String toString() {
        return "MemoryController: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}
