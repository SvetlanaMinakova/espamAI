
package espam.datamodel.platform.communication;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// AXICrossbar

/**
 * This class describes a AXICrossbar communication component.
 *
 * @author Todor Stefanov
 * @version  $Id: AXICrossbar.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXICrossbar extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a AXICrossbar component with a name.
     *
     */
    public AXICrossbar(String name) {
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
     *  Clone this AXICrossbar
     *
     * @return  a new instance of the AXICrossbar.
     */
    public Object clone() {
        AXICrossbar newObj = (AXICrossbar) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the AXICrossbar.
     *
     * @return  a description of the AXICrossbar.
     */
    public String toString() {
        return "AXI Crossbar Switch: " + getName();
    }
}
