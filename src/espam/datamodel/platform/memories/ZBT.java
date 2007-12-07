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
//// ZBT

/**
 * This class is the external static (ZBT) RAM of a platform.
 *
 * @author Hristo Nikolov
 * @version  $Id: ZBT.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class ZBT extends Memory {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ZBT with a name, size=0, specified 
     *  in Bytes, and data width=0
     */
    public ZBT(String name) {
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
     *  Clone this ZBT
     *
     * @return  a new instance of a ZBT memory.
     */
    public Object clone() {
        ZBT newObj = (ZBT) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the ZBT.
     *
     * @return  a description of the ZBT.
     */
    public String toString() {
        return "ZBT: " + getName();
    }
}
