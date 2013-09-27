
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.graph.NPort;
import espam.datamodel.domain.LBS;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGPort

/**
 * This class describes a port in an Approximated Dependence Graph (ADG)
 * The ADGPort is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.3 and 2.2.4 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGPort.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGPort extends NPort {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADGPort with a name.
     *
     */
    public ADGPort(String name) {
        super(name);
        _ioVariable = new ADGVariable("");
        _bindVariables = new Vector();
        _domain = new LBS();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ADGPort
     *
     * @return  a new instance of the ADGPort.
     */
    public Object clone() {
        ADGPort newObj = (ADGPort) super.clone();
        newObj.setIOVariable( (ADGVariable) _ioVariable.clone() );
        newObj.setBindVariables( (Vector) _bindVariables.clone() );
        newObj.setDomain( (LBS) _domain.clone() );
        return( newObj );
    }
    
    /**
     *  Get the IO variable of an ADGPort.
     *
     * @return  the IO variable
     */
    public ADGVariable getIOVariable() {
        return _ioVariable;
    }
    
    /**
     *  Set the IO variable of an ADGPort.
     *
     * @param  ioVariable The new IO variable
     */
    public void setIOVariable(ADGVariable ioVariable) {
        _ioVariable = ioVariable;
    }
    
    /**
     *  Get the binding variables of an ADGPort.
     *
     * @return  the binding variable
     */
    public Vector<ADGVariable> getBindVariables() {
        return _bindVariables;
    }
    
    /**
     *  Set the binding variables of an ADGPort.
     *
     * @param  bindVariable The new binding variable
     */
    public void setBindVariables(Vector<ADGVariable> bindVariables) {
        _bindVariables = bindVariables;
    }
    
    /**
     *  Get the domain of an ADGPort.
     *
     * @return  the domain
     */
    public LBS getDomain() {
        return _domain;
    }
    
    /**
     *  Set the domain of an ADGPort.
     *
     * @param  domain The new domain
     */
    public void setDomain(LBS domain) {
        _domain = domain;
    }
    
    /**
     *  Return a description of the ADGPort.
     *
     * @return  a description of the ADGPort.
     */
    public String toString() {
        return "ADGPort: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * Variable that is propagated through the port. See variables "Vp"
     * and "Vq" on page 39 in [1] - Definition 2.2.3 and 2.2.4
     *
     */
    private ADGVariable _ioVariable = null;
    
    /**
     * Variables that binds the port. See variables "Ap" and "Aq"
     * on page 39 in [1] - Definition 2.2.3 and 2.2.4
     *
     */
    private Vector<ADGVariable> _bindVariables = null;
    
    /**
     * The domain of the port. See domains "IPDp" and "OPDq"
     * on page 39 in [1] - Definition 2.2.3 and 2.2.4
     *
     */
    private LBS _domain = null;
}
