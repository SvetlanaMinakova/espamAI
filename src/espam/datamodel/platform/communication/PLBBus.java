
package espam.datamodel.platform.communication;

import espam.datamodel.platform.Resource;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// PLBBus

/**
 * This class describes a Processor Local Bus (PLB) of a PowerPC processor.
 *
 * @author Todor Stefanov
 * @version  $Id: PLBBus.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class PLBBus extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a PLB bus with a name.
     *
     */
    public PLBBus(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this PLB bus
     *
     * @return  a new instance of the PLB bus.
     */
    public Object clone() {
        PLBBus newObj = (PLBBus) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the PLB bus.
     *
     * @return  a description of the PLB bus.
     */
    public String toString() {
        return "PLB bus: " + getName();
    }
}
