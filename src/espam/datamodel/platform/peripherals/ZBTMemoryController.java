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

package espam.datamodel.platform.peripherals;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// ZBTMemeoryController

/**
 * This class is a ZBT memory controller component in a platform.
 *
 * @author Wei Zhong
 * @version  $Id: ZBTMemoryController.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class ZBTMemoryController extends Peripheral {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ZBTMemoryController with a name.
     *
     */
    public ZBTMemoryController(String name) {
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
     *  Clone this ZBTMemoryController
     *
     * @return  a new instance of the ZBTMemoryController.
     */
    public Object clone() {
            ZBTMemoryController newObj = (ZBTMemoryController) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the ZBTMemoryController.
     *
     * @return  a description of the ZBTMemoryController.
     */
    public String toString() {
        return "ZBTMemoryController: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}
