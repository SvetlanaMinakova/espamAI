
package espam.datamodel.graph;

import java.util.Vector;

import espam.visitor.GraphVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Edge

/**
 * This class is the basic edge in a generic graph.
 * The Edge has a name and a list of ports connected by this edge.
 *
 * @author Todor Stefanov
 * @version  $Id: Edge.java,v 1.1 2007/12/07 22:09:09 stefanov Exp $
 */

public class Edge implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an Edge with a name and an empty
     *  portList.
     */
    public Edge(String name) {
        _name = name;
        _portList = new Vector<NPort>();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(GraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Edge
     *
     * @return  a new instance of the Edge.
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Edge newObj = (Edge) super.clone();
            newObj.setName(_name);
            newObj.setPortList( (Vector<NPort>) _portList.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this edge.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this edge.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the list of ports of this edge.
     *
     * @return  the list of ports
     */
    public Vector<NPort> getPortList() {
        return _portList;
    }
    
    /**
     *  Set the list of ports of this edge.
     *
     * @param  portList The new list.
     */
    public void setPortList(Vector<NPort> portList) {
        _portList = portList;
    }
    
    /**
     *  Return a description of the edge.
     *
     * @return  a description of the edge.
     */
    public String toString() {
        return "Edge: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the Edge.
     */
    private String _name = null;
    
    /**
     *  List of the ports of the edge.
     */
    private Vector<NPort> _portList = null;
}
