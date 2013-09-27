
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// OPBPort

/**
 * This class is a OPB port of a resource component.
 *
 * @author Wei Zhong
 * @version  $Id: OPBPort.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class OPBPort extends Port {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a OPBPort with a name.
     *
     */
    public OPBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this OPBPort
     *
     * @return  a new instance of the OPBPort.
     */
    public Object clone() {
        OPBPort newObj = (OPBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the OPB port.
     *
     * @return  a description of the OPB port.
     */
    public String toString() {
        return "OPB Port: " + getName();
    }
}
