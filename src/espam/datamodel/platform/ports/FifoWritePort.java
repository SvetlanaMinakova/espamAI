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
//// FifoWritePort

/**
 * This class is a write port of a fifo resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: FifoWritePort.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class FifoWritePort extends Port {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a FifoWritePort with a name.
     *
     */
    public FifoWritePort(String name) {
       super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this FifoWritePort
     *
     * @return  a new instance of the FifoWritePort.
     */
    public Object clone() {
        FifoWritePort newObj = (FifoWritePort) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the FifoWritePort.
     *
     * @return  a description of the FifoWritePort.
     */
    public String toString() {
        return "Write FIFO Port: " + getName();
    }
}
