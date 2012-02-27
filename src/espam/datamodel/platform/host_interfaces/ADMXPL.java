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
//// ADMXPL

/**
 * This class describes a ADMXPL communication component.
 *
 * @author Hristo Nikolov
 * @version  $Id: ADMXPL.java,v 1.2 2012/02/27 11:22:50 nikolov Exp $
 */

public class ADMXPL extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ADMXPL component with a name.
     *
     */
    public ADMXPL(String name) {
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
     *  Clone this ADMXPL
     *
     * @return  a new instance of the ADMXPL.
     */
    public Object clone() {
            ADMXPL newObj = (ADMXPL) super.clone();
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
     *  Return a description of the ADMXPL.
     *
     * @return  a description of the ADMXPL.
     */
    public String toString() {
        return "ADMXPL host interface component: " + getName();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _commInterface = "";
}
