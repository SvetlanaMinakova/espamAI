
package espam.datamodel.graph;

import java.util.Vector;

import espam.visitor.GraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// NPort

/**
 * This class is the basic port of a node.
 *
 *
 *
 * @author Todor Stefanov
 * @version  $Id: NPort.java,v 1.1 2007/12/07 22:09:09 stefanov Exp $
 */

public class NPort implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a NPort with a name.
     *
     */
    public NPort(String name) {
        _name = name;
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(GraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this NPort
     *
     * @return  a new instance of the NPort.
     */
    public Object clone() {
        try {
            NPort newObj = (NPort) super.clone();
            newObj.setName(_name);
            newObj.setNode( (Node) _node.clone() );
            newObj.setEdge( (Edge) _edge.clone() );
            return( newObj );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this port.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this port.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the node of this port.
     *
     * @return  the node
     */
    public Node getNode() {
        return _node;
    }
    
    /**
     *  Set the node of this port.
     *
     * @param  node The new node
     */
    public void setNode(Node node) {
        _node = node;
    }
    
    /**
     *  Get the edge of this port.
     *
     * @return  the edge
     */
    public Edge getEdge() {
        return _edge;
    }
    
    /**
     *  Set the edge of this port.
     *
     * @param  edge The new edge
     */
    public void setEdge(Edge edge) {
        _edge = edge;
    }
    
    /**
     *  Return a description of the port.
     *
     * @return  a description of the port.
     */
    public String toString() {
        return "NPort: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the NPort.
     */
    private String _name = null;
    
    /**
     *  The Node which the NPort belongs to.
     */
    private Node _node = null;
    
    /**
     *  The Edge which the NPort connects to.
     */
    private Edge _edge = null;
}
