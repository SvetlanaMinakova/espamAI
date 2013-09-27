
package espam.datamodel.platform.host_interfaces;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADMXRCII

/**
 * This class describes a ADMXRCII communication component.
 *
 * @author Hristo Nikolov
 * @version  $Id: ADMXRCII.java,v 1.2 2012/02/27 11:22:50 nikolov Exp $
 */

public class ADMXRCII extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ADMXRCII component with a name.
     *
     */
    public ADMXRCII(String name) {
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
     *  Clone this ADMXRCII
     *
     * @return  a new instance of the ADMXRCII.
     */
    public Object clone() {
        ADMXRCII newObj = (ADMXRCII) super.clone();
        newObj.setCommInterface(_commInterface);
        return( newObj );
    }
    
    /**
     *  Get the communication interface.
     *
     * @return  the communication interface
     */
    public String getCommInterface() {
        return _commInterface;
    }
    
    /**
     *  Set the communication interface.
     *
     * @param  commInterface The new communication interface.
     */
    public void setCommInterface(String commInterface) {
        _commInterface = commInterface;
    }
    
    /**
     *  Return a description of the ADMXRCII.
     *
     * @return  a description of the ADMXRCII.
     */
    public String toString() {
        return "ADMXRCII host interface component: " + getName();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private String _commInterface = "";
}
