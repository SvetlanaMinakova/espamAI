
package espam.datamodel.platform.processors;

import java.util.Vector;

import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// PowerPC Processor

/**
 * This class describes PowerPC processor.
 *
 * @author Todor Stefanov
 * @version  $Id: PowerPC.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class PowerPC extends Processor {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a PowerPC processor with a name.
     *
     */
    public PowerPC(String name) {
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
     *  Clone this PowerPC processor
     *
     * @return  a new instance of the PowerPC processor.
     */
    public Object clone() {
        PowerPC newObj = (PowerPC) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the PowerPC processor.
     *
     * @return  a description of the PowerPC processor.
     */
    public String toString() {
        return "PowerPC processor: " + getName();
    }
}
