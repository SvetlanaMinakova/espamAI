
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// FifoWritePort

/**
 * This class is a write port of a fifo resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: FifoWritePort.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class FifoWritePort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a FifoWritePort with a name.
     *
     */
    public FifoWritePort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this FifoWritePort
     *
     * @return  a new instance of the FifoWritePort.
     */
    public Object clone() {
        FifoWritePort newObj = (FifoWritePort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the FifoWritePort.
     *
     * @return  a description of the FifoWritePort.
     */
    public String toString() {
        return "Write FIFO Port: " + getName();
    }
}
