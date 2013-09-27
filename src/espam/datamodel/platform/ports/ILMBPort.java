
package espam.datamodel.platform.ports;

import java.util.Vector;
import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// ILMBPort

/**
 * This class is a LMB port connecting a resource component to 
 * a program memory.
 *
 * @author Todor Stefanov
 * @version  $Id: ILMBPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class ILMBPort extends LMBPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ILMBPort with a name.
     *
     */
    public ILMBPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this ILMBPort
     *
     * @return  a new instance of the ILMBPort.
     */
    public Object clone() {
        ILMBPort newObj = (ILMBPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the ILMB port.
     *
     * @return  a description of the ILMB port.
     */
    public String toString() {
        return "ILMB Port: " + getName();
    }
}
