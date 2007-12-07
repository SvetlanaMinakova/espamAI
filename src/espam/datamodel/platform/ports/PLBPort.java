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
//// PLBPort

/**
 * This class is a PLB port of a resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: PLBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class PLBPort extends Port {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a PLBPort with a name.
     *
     */
    public PLBPort(String name) {
       super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this PLBPort
     *
     * @return  a new instance of the PLBPort.
     */
    public Object clone() {
        PLBPort newObj = (PLBPort) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the PLB port.
     *
     * @return  a description of the PLB port.
     */
    public String toString() {
        return "PLB Port: " + getName();
    }
}
