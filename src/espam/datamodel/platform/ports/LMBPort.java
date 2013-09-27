
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// LMBPort

/**
 * This class is a LMB port of a resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: LMBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class LMBPort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a PLBPort with a name.
     *
     */
    public LMBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this LMBPort
     *
     * @return  a new instance of the LMBPort.
     */
    public Object clone() {
        LMBPort newObj = (LMBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the LMB port.
     *
     * @return  a description of the LMB port.
     */
    public String toString() {
        return "LMB Port: " + getName();
    }
}
