
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// DLMBPort

/**
 * This class is a LMB port connecting a resource component to
 * a data bus.
 *
 * @author Todor Stefanov
 * @version  $Id: DLMBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class DLMBPort extends LMBPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a DLMBPort with a name.
     *
     */
    public DLMBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this DLMBPort
     *
     * @return  a new instance of the DLMBPort.
     */
    public Object clone() {
        DLMBPort newObj = (DLMBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the DLMB port.
     *
     * @return  a description of the DLMB port.
     */
    public String toString() {
        return "DLMB Port: " + getName();
    }
}
