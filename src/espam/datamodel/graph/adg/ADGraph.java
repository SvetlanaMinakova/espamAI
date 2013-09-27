
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.graph.Graph;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGraph

/**
 * This class describes an Approximated Dependence Graph (ADG).
 * The ADGraph is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.2.1 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGraph.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGraph extends Graph {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADGraph with a name and
     *  empty parameter list
     */
    public ADGraph(String name) {
        super(name);
        _parameterList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ADGraph
     *
     * @return  a new instance of the ADGraph.
     */
    public Object clone() {
        ADGraph newObj = (ADGraph) super.clone();
        newObj.setParameterList( (Vector) _parameterList.clone() );
        return( newObj );
    }
    
    /**
     *  Get the parameter list of an ADGrpah.
     *
     * @return  the parameter list
     */
    public Vector getParameterList() {
        return _parameterList;
    }
    
    /**
     *  Set the parameter list of an ADGraph.
     *
     * @param  parameterList The new list
     */
    public void setParameterList(Vector parameterList) {
        _parameterList = parameterList;
    }
    
    /**
     *  Return a description of the ADGraph.
     *
     * @return  a description of the ADGraph.
     */
    public String toString() {
        return "ADGraph: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List of the parameters related to the ADGraph.
     */
    private Vector _parameterList = null;
}
