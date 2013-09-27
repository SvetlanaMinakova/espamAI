
package espam.datamodel.platform.host_interfaces;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// XUPV5LX110T

/**
 * This class describes a XUPV5LX110T communication component.
 *
 * @author Hristo Nikolov
 * @version  $Id: XUPV5LX110T.java,v 1.2 2012/02/27 11:22:50 nikolov Exp $
 */

public class XUPV5LX110T extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a XUPV5LX110T component with a name.
     *
     */
    public XUPV5LX110T(String name) {
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
     *  Clone this XUPV5LX110T
     *
     * @return  a new instance of the XUPV5LX110T.
     */
    public Object clone() {
        XUPV5LX110T newObj = (XUPV5LX110T) super.clone();
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
     *  Return a description of the XUPV5LX110T.
     *
     * @return  a description of the XUPV5LX110T.
     */
    public String toString() {
        return "XUPV5LX110T host interface component: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private String _commInterface = "";
}
