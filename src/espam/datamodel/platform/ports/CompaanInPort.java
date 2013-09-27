
package espam.datamodel.platform.ports;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// CompaanInPort

/**
 * This class is an input port of a compaan generated node.
 *
 * @author Hristo Nikolov
 * @version  $Id: CompaanInPort.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class CompaanInPort extends FifoReadPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CompaanInPort with a name.
     *
     */
    public CompaanInPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this CompaanInPort
     *
     * @return  a new instance of the CompaanInPort.
     */
    public Object clone() {
        CompaanInPort newObj = (CompaanInPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the CompaanOutPort.
     *
     * @return  a description of the CompaanOutPort.
     */
    public String toString() {
        return "Compaan Input Port: " + getName();
    }
}
