
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.domain.LBS;
import espam.datamodel.graph.adg.ADGNode;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGInVar

/**
 * This class describes an 'invar' variable in an Approximated Dependence Graph (ADG)
 *
 * @author Todor Stefanov
 * @version  $Id: ADGInVar.java,v 1.1 2011/10/05 15:03:46 nikolov Exp $
 */

public class ADGInVar implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADGInVar with a name.
     *
     */
    public ADGInVar(String name) {
        _name = name;
        _bindVariable = new ADGVariable("");
        _domain = new LBS();
        _node = new ADGNode("");
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ADGInVar
     *
     * @return  a new instance of the ADGInVar.
     */
    public Object clone() {
        try {
            ADGInVar newObj = (ADGInVar) super.clone();
            newObj.setBindVariable( (ADGVariable) _bindVariable.clone() );
            newObj.setDomain( (LBS) _domain.clone() );
            newObj.setName(_name);
            newObj.setNode(_node);
            newObj.setRealName(_realName);
            return( newObj );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this invar.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this invar.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the binding variable of an ADGInVar.
     *
     * @return  the bindvariable
     */
    public ADGVariable getBindVariable() {
        return _bindVariable;
    }
    
    /**
     *  Set the binding variable of an ADGInVar.
     *
     * @param  bindVariable The new IO variable
     */
    public void setBindVariable(ADGVariable bindVariable) {
        _bindVariable = bindVariable;
    }
    
    /**
     *  Get the domain of an ADGInVar.
     *
     * @return  the domain
     */
    public LBS getDomain() {
        return _domain;
    }
    
    /**
     *  Set the domain of an ADGInVar.
     *
     * @param  domain The new domain
     */
    public void setDomain(LBS domain) {
        _domain = domain;
    }
    
    /**
     *  Get the node.
     *
     * @return  the nodeName
     */
    public ADGNode getNode() {
        return _node;
    }
    
    /**
     *  Set the node.
     *
     * @param  name The new node name value
     */
    public void setNode(ADGNode node) {
        _node = node;
    }
    
    /**
     *  Get the real name.
     *
     * @return  the realName
     */
    public String getRealName() {
        return _realName;
    }
    
    /**
     *  Set the real name.
     *
     * @param  name The new real name value
     */
    public void setRealName(String name) {
        _realName = name;
    }
    
    /**
     *  Return a description of the ADGInVar.
     *
     * @return  a description of the ADGInVar.
     */
    public String toString() {
        return "ADGInVar: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *
     */
    private ADGVariable _bindVariable = null;
    
    /**
     * The domain of the invar.
     *
     */
    private LBS _domain = null;
    
    private String _name = null;
    
    private ADGNode _node = null;
    
    private String _realName = null;
}
