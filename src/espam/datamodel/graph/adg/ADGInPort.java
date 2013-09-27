
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGInPort

/**
 * This class describes an input port in an Approximated Dependence Graph
 * The ADGInPort is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.3 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGInPort.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGInPort extends ADGPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADGInPort with a name.
     *
     */
    public ADGInPort(String name) {
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
     *  Clone this ADGInPort
     *
     * @return  a new instance of the ADGInPort.
     */
    public Object clone() {
        ADGInPort newObj = (ADGInPort) super.clone();
        return( newObj );
    }
    
    /**
     *  Return a description of the ADGInPort.
     *
     * @return  a description of the ADGInPort.
     */
    public String toString() {
        return "ADGInPort: " + getName();
    }
}
