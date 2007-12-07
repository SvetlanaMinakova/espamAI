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

import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;

import espam.visitor.PlatformVisitor;

import java.util.Vector;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// FifosController

/**
 * This class is a fifo controller component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: FifosController.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class FifosController extends Controller {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a FifosController with a name.
     *
     */
    public FifosController(String name) {
        super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this FifosController
     *
     * @return  a new instance of the FifosController.
     */
    public Object clone() {
            FifosController newObj = (FifosController) super.clone();
            return( newObj );
    }

    /**
     *  Get the number of FifoRead ports of this controller.
     *
     * @return  the number of FifoRead ports
     */
    public int getNumberFifoReadPorts() {
        int num = 0;

	Iterator i = getPortList().iterator();
	while( i.hasNext() ) {
           Port port = (Port) i.next();
	   if( port instanceof FifoReadPort ) {
	      num++;
	   }
	}

        return num;
    }

    /**
     *  Get the number of FifoWrite ports of this controller.
     *
     * @return  the number of FifoWrite ports
     */
    public int getNumberFifoWritePorts() {
        int num = 0;

	Iterator i = getPortList().iterator();
	while( i.hasNext() ) {
           Port port = (Port) i.next();
	   if( port instanceof FifoWritePort ) {
	      num++;
	   }
	}

        return num;
    }

    /**
     *  Get the FifoRead ports of this controller.
     *
     * @return  the FifoRead ports
     */
    public Vector getFifoReadPorts() {

	Vector readPorts = new Vector();
	Iterator i = getPortList().iterator();
	while( i.hasNext() ) {
           Port port = (Port) i.next();
	   if( port instanceof FifoReadPort ) {
	      readPorts.add( port );
	   }
	}

        return readPorts;
    }

     /**
     *  Get the FifoWrite ports of this controller.
     *
     * @return  the FifoWrite ports
     */
    public Vector getFifoWritePorts() {

	Vector writePorts = new Vector();
	Iterator i = getPortList().iterator();
	while( i.hasNext() ) {
           Port port = (Port) i.next();
	   if( port instanceof FifoWritePort ) {
	      writePorts.add( port );
	   }
	}

        return writePorts;
    }

    /**
     *  Return a description of the FifosController.
     *
     * @return  a description of the FifosController.
     */
    public String toString() {
        return "FifosController: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}
