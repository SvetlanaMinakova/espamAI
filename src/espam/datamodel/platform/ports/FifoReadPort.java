
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// FifoReadPort

/**
 * This class is a read port of a fifo resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: FifoReadPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class FifoReadPort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a FifoReadPort with a name.
     *
     */
    public FifoReadPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this FifoReadPort
     *
     * @return  a new instance of the FifoReadPort.
     */
    public Object clone() {
        FifoReadPort newObj = (FifoReadPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the FifoReadPort.
     *
     * @return  a description of the FifoReadPort.
     */
    public String toString() {
        return "Read FIFO Port: " + getName();
    }
}
