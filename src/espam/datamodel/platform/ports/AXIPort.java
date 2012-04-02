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

package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// AXIPort

/**
 * This class is a AXI port of a resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: AXIPort.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXIPort extends Port {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a AXIPort with a name.
     *
     */
    public AXIPort(String name) {
       super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this AXIPort
     *
     * @return  a new instance of the AXIPort.
     */
    public Object clone() {
        AXIPort newObj = (AXIPort) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the AXI port.
     *
     * @return  a description of the AXI port.
     */
    public String toString() {
        return "AXI Port: " + getName();
    }
}
