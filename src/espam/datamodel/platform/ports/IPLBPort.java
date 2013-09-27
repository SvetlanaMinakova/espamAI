
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// IPLBPort

/**
 * This class is a PLB port connecting a resource component to a program
 * memory.
 *
 * @author Todor Stefanov
 * @version  $Id: IPLBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class IPLBPort extends PLBPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a IPLBPort with a name.
     *
     */
    public IPLBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this IPLBPort
     *
     * @return  a new instance of the IPLBPort.
     */
    public Object clone() {
        IPLBPort newObj = (IPLBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the IPLB port.
     *
     * @return  a description of the IPLB port.
     */
    public String toString() {
        return "IPLB Port: " + getName();
    }
}
