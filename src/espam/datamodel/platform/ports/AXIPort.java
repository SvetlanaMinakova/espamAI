
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// AXIPort

/**
 * This class is a AXI port of a resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: AXIPort.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class AXIPort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a AXIPort with a name.
     *
     */
    public AXIPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this AXIPort
     *
     * @return  a new instance of the AXIPort.
     */
    public Object clone() {
        AXIPort newObj = (AXIPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the AXI port.
     *
     * @return  a description of the AXI port.
     */
    public String toString() {
        return "AXI Port: " + getName();
    }
}
