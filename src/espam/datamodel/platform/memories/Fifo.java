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

package espam.datamodel.platform.memories;

import java.util.Vector;

import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Fifo

/**
 * This class is a basic FIFO component of a platform.
 *
 * @author Hristo Nikolov
 * @version  $Id: Fifo.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class Fifo extends Memory {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a FIFO with a name, size=0, specified 
     *  as a number of available locations, and data width=0
     */
    public Fifo(String name) {
    	super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this FIFO
     *
     * @return  a new instance of a FIFO.
     */
    public Object clone() {
        Fifo newObj = (Fifo) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the FIFO.
     *
     * @return  a description of the FIFO.
     */
    public String toString() {
        return "FIFO: " + getName();
    }
}
