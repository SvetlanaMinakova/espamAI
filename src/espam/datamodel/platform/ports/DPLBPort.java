
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// DPLBPort

/**
 * This class is a PLB port connecting a resource component to a data
 * PLB bus.
 *
 * @author Todor Stefanov
 * @version  $Id: DPLBPort.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class DPLBPort extends PLBPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a DPLBPort with a name.
     *
     */
    public DPLBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this DPLBPort
     *
     * @return  a new instance of the DPLBPort.
     */
    public Object clone() {
        DPLBPort newObj = (DPLBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the DPLB port.
     *
     * @return  a description of the DPLB port.
     */
    public String toString() {
        return "DPLB Port: " + getName();
    }
}
