
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGOutPort

/**
 * This class describes an output port in an Approximated Dependence Graph
 * The ADGOutPort is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.4 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGOutPort.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGOutPort extends ADGPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADGOutPort with a name.
     *
     */
    public ADGOutPort(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ADGOutPort
     *
     * @return  a new instance of the ADGOutPort.
     */
    public Object clone() {
        ADGOutPort newObj = (ADGOutPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the ADGOutPort.
     *
     * @return  a description of the ADGOutPort.
     */
    public String toString() {
        return "ADGOutPort: " + getName();
    }
}
