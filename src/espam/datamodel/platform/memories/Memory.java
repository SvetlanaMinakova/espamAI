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

import espam.datamodel.platform.Resource;

import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Memory

/**
 * This class is the basic memory component of a platform.
 * The component has a size specified in Bytes and data width.
 *
 * @author Hristo Nikolov
 * @version  $Id: Memory.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class Memory extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Memory with a name, size=0 and 
     *  data width=0
     */
    public Memory(String name) {
    	super(name);
        _size = 0;
        _dataWidth = 0;
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
         x.visitComponent(this);
    }
    /**
     *  Clone this Memory
     *
     * @return  a new instance of the Memory.
     */
    public Object clone() {
        Memory newObj = (Memory) super.clone();
        newObj.setSize( _size );
        newObj.setDataWidth( _dataWidth );
        return( newObj );
    }

    /**
     *  Get the size of the memory.
     *
     * @return  the size
     */
    public int getSize() {
        return _size;
    }

    /**
     *  Set the size of the memory.
     *
     * @param  size The new size value
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     *  Get the data width of the memory.
     *
     * @return  the data width
     */
    public int getDataWidth() {
        return _dataWidth;
    }

    /**
     *  Set the data width of the memory.
     *
     * @param  data width The new data width value
     */
    public void setDataWidth(int dataWidth) {
        _dataWidth = dataWidth;
    }

    /**
     *  Return a description of the memory.
     *
     * @return  a description of the memory.
     */
    public String toString() {
        return "Memory: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The size of the memory.
     */
    private int _size = 0;

    /**
     *  The data width of the memory.
     */
    private int _dataWidth = 0;
}
