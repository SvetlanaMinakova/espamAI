
package espam.datamodel.platform.communication;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// ReadFifoBus

/**
 * This class describes a ReadFifo bus.
 *
 * @author Todor Stefanov
 * @version  $Id: ReadFifoBus.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class ReadFifoBus extends TransparentBus {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ReadFifo bus with a name.
     *
     */
    public ReadFifoBus(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this ReadFifo bus
     *
     * @return  a new instance of the ReadFifo bus.
     */
    public Object clone() {
        ReadFifoBus newObj = (ReadFifoBus) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the ReadFifo bus.
     *
     * @return  a description of the ReadFifo bus.
     */
    public String toString() {
        return "ReadFifo bus: " + getName();
    }
}
