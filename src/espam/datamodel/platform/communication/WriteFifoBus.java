
package espam.datamodel.platform.communication;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// WriteFifoBus

/**
 * This class describes a WriteFifo bus.
 *
 * @author Todor Stefanov
 * @version  $Id: WriteFifoBus.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class WriteFifoBus extends TransparentBus {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a WriteFifo bus with a name.
     *
     */
    public WriteFifoBus(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this WriteFifo bus
     *
     * @return  a new instance of the ReadFifo bus.
     */
    public Object clone() {
        WriteFifoBus newObj = (WriteFifoBus) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the WriteFifo bus.
     *
     * @return  a description of the WriteFifo bus.
     */
    public String toString() {
        return "WriteFifo bus: " + getName();
    }
}
