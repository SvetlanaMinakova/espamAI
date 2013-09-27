
package espam.datamodel.platform.communication;

import espam.datamodel.platform.Resource;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// TransparentBus

/**
 * This class describes a generic transparent bus.
 *
 * @author Todor Stefanov
 * @version  $Id: TransparentBus.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class TransparentBus extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Transparent bus with a name.
     *
     */
    public TransparentBus(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this Transparent bus
     *
     * @return  a new instance of the Transparent bus.
     */
    public Object clone() {
        TransparentBus newObj = (TransparentBus) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the Transparent bus.
     *
     * @return  a description of the Transparent bus.
     */
    public String toString() {
        return "Transparent bus: " + getName();
    }
}
