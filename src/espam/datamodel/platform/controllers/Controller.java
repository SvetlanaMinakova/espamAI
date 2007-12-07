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

package espam.datamodel.platform.controllers;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Page;

import espam.visitor.PlatformVisitor;

import java.util.Vector;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Controller

/**
 * This class is the basic controller component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: Controller.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class Controller extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Controller with a name.
     *
     */
    public Controller(String name) {
        super(name);
        _pageList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this Controller
     *
     * @return  a new instance of the Controller.
     */
    public Object clone() {
            Controller newObj = (Controller) super.clone();
            newObj.setPageList( (Vector) _pageList.clone() );
            return( newObj );
    }

    /**
     *  Get the page list of this Controller.
     *
     * @return  the page list of Controller
     */
    public Vector getPageList() {
        return _pageList;
    }

    /**
     *  Set the page list of this Controller.
     *
     * @param  pageList The new list.
     */
    public void setPageList(Vector pageList) {
        _pageList = pageList;
    }

    /**
     *  Get the base address of this controller.
     *
     * @return  the base address
     */
    public int getBaseAddress() {
        int Addr = 0;

	Iterator i = _pageList.iterator();
	Addr = ((Page) i.next()).getBaseAddress();
	while( i.hasNext() ) {
           int baseAddr  = ((Page) i.next()).getBaseAddress();
	   if( baseAddr < Addr ) {
	      Addr = baseAddr;
	   }
	}

        return Addr;
    }

    /**
     *  Get the size (Bytes) of the memory space
     *  which this controller covers.
     *
     * @return  the size
     */
    public int getSize() {
        int minBaseAddr = 0;
        int maxBaseAddr = 0;
        int maxBaseAddrPageSize = 0;

	Iterator i = _pageList.iterator();
	Page page = (Page) i.next();
	minBaseAddr = page.getBaseAddress();
	maxBaseAddr = page.getBaseAddress();
        maxBaseAddrPageSize = page.getSize();

	while( i.hasNext() ) {
           page = (Page) i.next();
           int baseAddr  = page.getBaseAddress();

	   if( baseAddr < minBaseAddr ) {
	      minBaseAddr = baseAddr;
	   }

	   if( baseAddr > maxBaseAddr ) {
	      maxBaseAddr = baseAddr;
              maxBaseAddrPageSize = page.getSize();
	   }

	}

        return (maxBaseAddr - minBaseAddr + maxBaseAddrPageSize);
    }


    /**
     *  Return a description of the Controller.
     *
     * @return  a description of the Controller.
     */
    public String toString() {
        return "Controller: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List containing memory map pages.
     */
    private Vector _pageList = null;

}
