
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// PLBPort

/**
 * This class is a PLB port of a resource component.
 *
 * @author Hristo Nikolov
 * @version  $Id: PLBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class PLBPort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a PLBPort with a name.
     *
     */
    public PLBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this PLBPort
     *
     * @return  a new instance of the PLBPort.
     */
    public Object clone() {
        PLBPort newObj = (PLBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the PLB port.
     *
     * @return  a description of the PLB port.
     */
    public String toString() {
        return "PLB Port: " + getName();
    }
}
