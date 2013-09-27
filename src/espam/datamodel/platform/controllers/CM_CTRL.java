
package espam.datamodel.platform.controllers;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// FifosController

/**
 * This class is a communication memory controller component in a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: CM_CTRL.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class CM_CTRL extends Controller {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CM_CTRL with a name.
     *
     */
    public CM_CTRL(String name) {
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
        CM_CTRL newObj = (CM_CTRL) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the FifosController.
     *
     * @return  a description of the FifosController.
     */
    public String toString() {
        return "CM_CTRL: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    
}
