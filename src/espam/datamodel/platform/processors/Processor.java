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

package espam.datamodel.platform.processors;

import espam.datamodel.platform.Resource;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Processor

/**
 * This class is the basic processor component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: Processor.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Processor extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Processor with a name.
     *
     */
    public Processor(String name) {
        super(name);
        _memoryMapList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this Processor
     *
     * @return  a new instance of the Processor.
     */
    public Object clone() {
            Processor newObj = (Processor) super.clone();
	    newObj.setProgMemSize(_progMemSize);
	    newObj.setDataMemSize(_dataMemSize);
            newObj.setMemoryMapList( (Vector) _memoryMapList.clone() );
            return( newObj );
    }

    /**
     *  Get the program memory size of this Processor.
     *
     * @return  the size
     */
    public int getProgMemSize() {
        return _progMemSize;
    }

    /**
     *  Set the program memory size of this Processor.
     *
     * @param  progMemSize The new size value
     */
    public void setProgMemSize(int progMemSize) {
        _progMemSize = progMemSize;
    }

    /**
     *  Get the data memory size of this Processor.
     *
     * @return  the size
     */
    public int getDataMemSize() {
        return _dataMemSize;
    }

    /**
     *  Set the data memory size of this Processor.
     *
     * @param  dataMemSize The new size value
     */
    public void setDataMemSize(int dataMemSize) {
        _dataMemSize = dataMemSize;
    }


    /**
     *  Get the memory maps list of this Processor.
     *
     * @return  the memory maps list of Processor
     */
    public Vector getMemoryMapList() {
        return _memoryMapList;
    }

    /**
     *  Set the memory maps list of this Processor.
     *
     * @param  memoryMapList The new list.
     */
    public void setMemoryMapList(Vector memoryMapList) {
        _memoryMapList = memoryMapList;
    }

    /**
     *  Return a description of the Processor.
     *
     * @return  a description of the Processor.
     */
    public String toString() {
        return "Processor: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List containing the memory maps of Processor.
     */
    private Vector _memoryMapList = null;

    /**
     *  Size (in Bytes) of the program memory of Processor.
     */
    private int _progMemSize = 0;

    /**
     *  Size (in Bytes) of the data memory of Processor.
     */
    private int _dataMemSize = 0;
}
