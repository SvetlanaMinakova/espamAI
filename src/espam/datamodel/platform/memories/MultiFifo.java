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
import java.util.Iterator;

import espam.visitor.PlatformVisitor;

import espam.datamodel.platform.memories.Fifo;

//////////////////////////////////////////////////////////////////////////
//// MultiFifo

/**
 * This class is the Virtual Buffer component of a platform.
 * The component has a size, data width and list of fifos.
 *
 * @author Hristo Nikolov
 * @version  $Id: MultiFifo.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class MultiFifo extends Memory {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a MultiFifo with a name, size=0 (the total amount
     *  of available locations) and empty fifo list
     */
    public MultiFifo(String name) {
    	super(name);
        _fifoList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    public void accept(PlatformVisitor x) { 
         x.visitComponent(this);
    }

    /**
     *  Clone this MultiFifo
     *
     * @return  a new instance of a MultiFifo.
     */
    public Object clone() {
        MultiFifo newObj = (MultiFifo) super.clone();
        newObj.setFifoList( (Vector) _fifoList.clone() );
        return( newObj );
    }

    /**
     *  Get the list of fifos of this MultiMifo.
     *
     * @return  the list of fifos
     */
    public Vector getFifoList() {
        return _fifoList;
    }

    /**
     *  Set the list of ports of this MultiFifo.
     *
     * @param  fifoList The new list.
     */
    public void setFifoList(Vector fifoList) {
        _fifoList = fifoList;
    }
    
    /**
     *  Get the memory size of this MultiMifo.
     *  The size is the sum of the fifo sizes of this MultiFifo
     *
     * @return  the MultiFifo size
     */
    public int getSize() {
        int size = 0;
	
	Iterator f = _fifoList.iterator();
	while( f.hasNext() ) {

	    Fifo fifo = (Fifo) f.next();
	    size += fifo.getSize();
	}

        return size;
    }

    /**
     *  Return a description of a MultiFifo.
     *
     * @return  a description of a MultiFifo.
     */
    public String toString() {
        return "MultiFifo: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List of the fifos of a MultiFifo.
     */
    private Vector _fifoList = null;
}
