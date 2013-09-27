
package espam.datamodel.platform.memories;

import java.util.Vector;

import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// BRAM

/**
 * This class is the on-chip (BRAM) memory of a platform.
 *
 * @author Hristo Nikolov
 * @version  $Id: BRAM.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class BRAM extends Memory {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a BRAM with a name, size=0 and 
     *  data width=0. The size of the memory is specified in Bytes.
     */
    public BRAM(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this BRAM
     *
     * @return  a new instance of a BRAM memory.
     */
    public Object clone() {
        BRAM newObj = (BRAM) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the BRAM.
     *
     * @return  a description of the BRAM.
     */
    public String toString() {
        return "BRAM: " + getName();
    }
}
