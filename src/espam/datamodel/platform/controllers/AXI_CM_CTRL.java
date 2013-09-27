
package espam.datamodel.platform.controllers;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// AXI_CM_CTRL

/**
 * This class is a controller component that is used to connect a read port
 * of a CompaanHWNode to a port of a crossbar switch.
 *
 * @author Todor Stefanov
 * @version  $Id: AXI_CM_CTRL.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXI_CM_CTRL extends Controller {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a AXI_CM_CTRL with a name.
     *
     */
    public AXI_CM_CTRL(String name) {
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
     *  Clone this AXI_CM_CTRL
     *
     * @return  a new instance of the ReadCrosbarController.
     */
    public Object clone() {
        AXI_CM_CTRL newObj = (AXI_CM_CTRL) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the AXI_CM_CTRL.
     *
     * @return  a description of the AXI_CM_CTRL.
     */
    public String toString() {
        return "AXI_CM_CTRL: " + getName();
    }
    
}
