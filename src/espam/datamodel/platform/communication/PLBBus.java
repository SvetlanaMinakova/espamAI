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

import espam.datamodel.platform.Resource;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// PLBBus

/**
 * This class describes a Processor Local Bus (PLB) of a PowerPC processor.
 *
 * @author Todor Stefanov
 * @version  $Id: PLBBus.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class PLBBus extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a PLB bus with a name.
     *
     */
    public PLBBus(String name) {
        super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this PLB bus
     *
     * @return  a new instance of the PLB bus.
     */
    public Object clone() {
            PLBBus newObj = (PLBBus) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the PLB bus.
     *
     * @return  a description of the PLB bus.
     */
    public String toString() {
        return "PLB bus: " + getName();
    }
}
