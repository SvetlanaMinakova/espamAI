
package espam.datamodel.platform.processors;

import java.util.Vector;

import espam.visitor.PlatformVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// MicroBlaze Processor

/**
 * This class describes MicroBlaze processor.
 *
 * @author Todor Stefanov
 * @version  $Id: MicroBlaze.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class MicroBlaze extends Processor {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a MicroBlaze processor with a name.
     *
     */
    public MicroBlaze(String name) {
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
     *  Clone this MicroBlaze processor
     *
     * @return  a new instance of the MicroBlaze processor.
     */
    public Object clone() {
        MicroBlaze newObj = (MicroBlaze) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the PowerPC processor.
     *
     * @return  a description of the PowerPC processor.
     */
    public String toString() {
        return "MicroBlaze processor: " + getName();
    }
}
