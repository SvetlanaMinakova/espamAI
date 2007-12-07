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

//////////////////////////////////////////////////////////////////////////
//// CompaanOutPort

/**
 * This class is an output port of a compaan generated node.
 *
 * @author Hristo Nikolov
 * @version  $Id: CompaanOutPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class CompaanOutPort extends FifoWritePort {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a CompaanOutPort with a name.
     *
     */
    public CompaanOutPort(String name) {
       super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this CompaanOutPort
     *
     * @return  a new instance of the CompaanOutPort.
     */
    public Object clone() {
        CompaanOutPort newObj = (CompaanOutPort) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the CompaanOutPort.
     *
     * @return  a description of the CompaanOutPort.
     */
    public String toString() {
        return "Compaan Output Port: " + getName();
    }
}
