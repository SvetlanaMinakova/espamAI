
package espam.datamodel.platform.controllers;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// FifosController

/**
 * This class is a multi-fifo controller component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: MultiFifoController.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class MultiFifoController extends Controller {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a MultiFifoController with a name.
     *
     */
    public MultiFifoController(String name) {
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
        MultiFifoController newObj = (MultiFifoController) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the FifosController.
     *
     * @return  a description of the FifosController.
     */
    public String toString() {
        return "MultiFifoController: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    
}
