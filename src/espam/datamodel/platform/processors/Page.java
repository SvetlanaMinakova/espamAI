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
//// Page

/**
 * This class describes a memory page (segment) in a memory map.
 *
 * @author Todor Stefanov
 * @version  $Id: Page.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Page implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a page.
     *
     */
    public Page() {

       _readResource = new Resource("");
       _writeResource = new Resource("");

    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this page
     *
     * @return  a new instance of the page.
     */
    public Object clone() {
        try {
            Page newObj = (Page) super.clone();
	    newObj.setWriteResource( (Resource) _writeResource.clone() );
	    newObj.setReadResource( (Resource) _readResource.clone() );
            newObj.setBaseAddress( _baseAddr );
            newObj.setSize( _size );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the write resource of this page.
     *
     * @return  the resource
     */
    public Resource getWriteResource() {
        return _writeResource;
    }

    /**
     *  Set the write resource of this page.
     *
     * @param  resource The new resource
     */
    public void setWriteResource(Resource writeResource) {
        _writeResource = writeResource;
    }


    /**
     *  Get the read resource of this page.
     *
     * @return  the resource
     */
    public Resource getReadResource() {
        return _readResource;
    }

    /**
     *  Set the read resource of this page.
     *
     * @param  resource The new resource
     */
    public void setReadResource(Resource readResource) {
        _readResource = readResource;
    }


    /**
     *  Get the base address of this page.
     *
     * @return  the address
     */
    public int getBaseAddress() {
        return _baseAddr;
    }

    /**
     *  Set the base address of this page.
     *
     * @param  baseAddr The new address
     */
    public void setBaseAddress(int baseAddr) {
        _baseAddr = baseAddr;
    }

    /**
     *  Get the size of this page.
     *
     * @return  the size
     */
    public int getSize() {
        return _size;
    }

    /**
     *  Set the size of this page.
     *
     * @param  size The new size
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     *  Return a description of the page.
     *
     * @return  a description of the page.
     */
    public String toString() {
        return "Page for: resource (read) =" + _readResource.getName() +
	                " resource (write) =" + _writeResource.getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  the resource mapped in this page (segment)
     *  and accessed by write.
     */
    private Resource _writeResource = null;

    /**
     *  the resource mapped in this page (segment)
     *  and accessed by read.
     */
    private Resource _readResource = null;


    /**
     *  The starting address of this page.
     */
    private int _baseAddr = 0;

    /**
     *  the size of this page in Bytes
     */
    private int _size = 0;
}
