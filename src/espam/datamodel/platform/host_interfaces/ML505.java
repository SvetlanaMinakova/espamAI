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
//// ML505

/**
 * This class describes a ML505 communication component.
 *
 * @author Hristo Nikolov
 * @version  $Id: ML505.java,v 1.2 2012/02/27 11:22:50 nikolov Exp $
 */

public class ML505 extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ML505 component with a name.
     *
     */
    public ML505(String name) {
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
     *  Clone this ML505
     *
     * @return  a new instance of the ML505.
     */
    public Object clone() {
            ML505 newObj = (ML505) super.clone();
            newObj.setCommInterface(_commInterface);
            return( newObj );
    }

    /**
     *  Get the communication interface.
     *
     * @return  the communication interface
     */
    public String getCommInterface() {
        return _commInterface;
    }

    /**
     *  Set the communication interface.
     *
     * @param  commInterface The new communication interface.
     */
    public void setCommInterface(String commInterface) {
        _commInterface = commInterface;
    }

    /**
     *  Return a description of the ML505.
     *
     * @return  a description of the ML505.
     */
    public String toString() {
        return "ML505 host interface component: " + getName();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _commInterface = "";
}
