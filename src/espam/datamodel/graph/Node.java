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
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Node

/**
 * This class is the basic node of a generic graph.
 * The node has a name and contains one list: a list that
 * contains the ports of the node.
 *
 * @author Todor Stefanov
 * @version  $Id: Node.java,v 1.1 2007/12/07 22:09:09 stefanov Exp $
 */

public class Node implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Node with a name and an empty
     *  portList.
     */
    public Node(String name) {
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
     *  Clone this Node
     *
     * @return  a new instance of the Node.
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Node newObj = (Node) super.clone();
	    newObj.setName(_name);
            newObj.setPortList( (Vector<NPort>) _portList.clone() );
	    newObj.setLevelUpNode( (Node) _levelUpNode.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the name of this node.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this node.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the list of ports of this node.
     *
     * @return  the list of ports
     */
    public Vector<NPort> getPortList() {
        return _portList;
    }

    /**
     *  Set the list of ports of this node.
     *
     * @param  portList The new list.
     */
    public void setPortList(Vector<NPort> portList) {
        _portList = portList;
    }

    /**
     *  Get the hierarchical parent of this node.
     *
     * @return  the parent of this node.
     */
    public Node getLevelUpNode() {
        return _levelUpNode;
    }

    /**
     *  Set the hierarchical parent of this node.
     *
     * @param  levelUpNode The new parent.
     */
    public void setLevelUpNode(Node levelUpNode) {
        _levelUpNode = levelUpNode;
    }

    /**
     *  Return a description of the node.
     *
     * @return  a description of the node.
     */
    public String toString() {
        return "Node: " + _name;
    }

    /**
     *  Return a port which has a specific name. Return null if port cannot
     *  be found.
     *
     * @param  name the name of the port to search for.
     * @return  the port with the specific name.
     */
     public NPort getPort(String name) {
          Iterator<NPort> i;
          i = _portList.iterator();
          while( i.hasNext() ) {
              NPort port = i.next();
              if( port.getName().equals(name) ) {
                 return port;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the Node.
     */
    private String _name = null;

    /**
     *  List of the ports of the Node.
     */
    private Vector<NPort> _portList = null;

    /**
     *  The parent node of this node in a hierarchical graph
     */
    private Node _levelUpNode = null;
}
