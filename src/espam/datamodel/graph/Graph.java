/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.datamodel.graph;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.GraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// Graph

/**
 * This class is a basic graph.
 *
 * @author Todor Stefanov
 * @version  $Id: Graph.java,v 1.1 2007/12/07 22:09:09 stefanov Exp $
 */

public class Graph extends Node {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Graph with a name,
     *  empty node list and empty edge list
     */
    public Graph(String name) {
    	super(name);
        _nodeList = new Vector();
        _edgeList = new Vector<Edge>();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(GraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this Graph
     *
     * @return  a new instance of the Graph.
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        Graph newObj = (Graph) super.clone();
        newObj.setNodeList( (Vector) _nodeList.clone() );
        newObj.setEdgeList( (Vector<Edge>) _edgeList.clone() );
        return( newObj );
    }

    /**
     *  Get the node list of a grpah.
     *
     * @return  the node list
     */
    public Vector getNodeList() {
        return _nodeList;
    }

    /**
     *  Set the node list of a Graph.
     *
     * @param  nodeList The new list
     */
    public void setNodeList( Vector nodeList) {
        _nodeList = nodeList;
    }

    /**
     *  Get the edge list of a Graph
     *
     * @return  the edge list
     */
    public Vector<Edge> getEdgeList() {
        return _edgeList;
    }

    /**
     *  Set the edge list of a Graph
     *
     * @param  edgeList The new list
     */
    public void setEdgeList(Vector<Edge> edgeList) {
        _edgeList = edgeList;
    }

    /**
     *  Return a description of the Graph.
     *
     * @return  a description of the Graph.
     */
    public String toString() {
        return "Graph: " + getName();
    }

    /**
     *  Return a node which has a specific name. Return null if
     *  node cannot be found.
     *
     * @param  name the name of the node to search for.
     * @return  the node with the specific name.
     */
     public Node getNode(String name) {
          Iterator i;
          i = _nodeList.iterator();
          while( i.hasNext() ) {
              Node node = (Node) i.next();
              if( node.getName().equals(name) ) {
                 return node;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List of the nodes of the Graph.
     */
    private Vector _nodeList = null;

    /**
     *  List of the edges of the Graph.
     */
    private Vector<Edge> _edgeList = null;
}
