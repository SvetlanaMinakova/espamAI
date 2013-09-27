
package espam.datamodel.platform.communication;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Crossbar

/**
 * This class describes a Crossbar communication component.
 *
 * @author Todor Stefanov
 * @version  $Id: Crossbar.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Crossbar extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Crossbar component with a name.
     *
     */
    public Crossbar(String name) {
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
     *  Clone this Crossbar
     *
     * @return  a new instance of the Crossbar.
     */
    public Object clone() {
        Crossbar newObj = (Crossbar) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the Crossbar.
     *
     * @return  a description of the Crossbar.
     */
    public String toString() {
        return "Crossbar Switch: " + getName();
    }
}
