
package espam.datamodel.platform.ports;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// CompaanOutPort

/**
 * This class is an output port of a compaan generated node.
 *
 * @author Hristo Nikolov
 * @version  $Id: CompaanOutPort.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class CompaanOutPort extends FifoWritePort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CompaanOutPort with a name.
     *
     */
    public CompaanOutPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this CompaanOutPort
     *
     * @return  a new instance of the CompaanOutPort.
     */
    public Object clone() {
        CompaanOutPort newObj = (CompaanOutPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the CompaanOutPort.
     *
     * @return  a description of the CompaanOutPort.
     */
    public String toString() {
        return "Compaan Output Port: " + getName();
    }
}
