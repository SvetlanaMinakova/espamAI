
package espam.datamodel.platform.communication;

import espam.datamodel.platform.Resource;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// LMBBus

/**
 * This class describes a Local Memory Bus (LMB) of a MicroBlaze processor.
 *
 * @author Todor Stefanov
 * @version  $Id: LMBBus.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class LMBBus extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a LMB bus with a name.
     *
     */
    public LMBBus(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this LMB bus
     *
     * @return  a new instance of the LMB bus.
     */
    public Object clone() {
        LMBBus newObj = (LMBBus) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the LMB bus.
     *
     * @return  a description of the LMB bus.
     */
    public String toString() {
        return "LMB bus: " + getName();
    }
}
