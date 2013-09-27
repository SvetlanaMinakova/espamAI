
package espam.datamodel.platform.peripherals;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Uart

/**
 * This class is a Uart component in a platform.
 *
 * @author Wei Zhong
 * @version  $Id: Uart.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Uart extends Peripheral {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Uart with a name.
     *
     */
    public Uart(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Uart
     *
     * @return  a new instance of the Uart.
     */
    public Object clone() {
        Uart newObj = (Uart) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the Uart.
     *
     * @return  a description of the Uart.
     */
    public String toString() {
        return "Uart: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    
}
