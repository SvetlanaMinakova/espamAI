
package espam.datamodel.parsetree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import espam.visitor.StatementVisitor;

/**
 *  This class is the implementation of the ParserNode Interface. T o
 *  represent the child of a ParserNode, this implementation uses an array
 *  list.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: ParserNodeImp.java,v 1.10 1999/10/20 02:02:51 kienhuis Exp
 *      $
 */

public class ParserNodeImp implements ParserNode, Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor which creates a ParserNode with a name and an empty
     *  ArrayList.
     */
    public ParserNodeImp() {
        _nodeList = new ArrayList();
        _isParsed = false;
    }
    
    /**
     *  Accept a Visitor
     *
     * @param  x A Visitor Object.
     */
    public void accept(StatementVisitor x) { }
    
    
    /**
     *  Clone this ParserNodeImp
     *
     * @return  a new instance of the ParserNodeImp.
     */
    public Object clone() {
        try {
            ParserNodeImp pni = (ParserNodeImp) super.clone();
            pni.setName( _name );
            pni.setParsedFlag( _isParsed );
            return (pni);
        } catch (CloneNotSupportedException e) {
            System.out.println("Error Clone not Supported");
        }
        
        return null;
    }
    
    
    
    /**
     *  Clone the Parse Tree
     *
     * @return  a new instance of the tree represented by this parse node.
     */
    public Object deepClone() {
        
        ParserNodeImp  newObj = (ParserNodeImp) this.clone();
        newObj.setNodeList(new ArrayList(1));
        
        Iterator i = _nodeList.iterator();
        while (i.hasNext()) {
            ParserNodeImp pn = (ParserNodeImp) ((ParserNodeImp) i.next()).deepClone();
            pn.setParent(newObj);
            newObj.addChild(pn);
        }
        
        return (newObj);
    }
    
    
    /**
     *  Remove recursively all child objects.
     */
    public void deepRemove() {
        
        Iterator j = this.getChildren();
        while (j.hasNext()) {
            ((ParserNodeImp) j.next()).deepRemove();
        }
        ((ParserNodeImp) this).setNodeList(null);
    }
    
    /**
     *  Add a child node to the list of child nodes.
     *
     * @param  n A child node to add.
     */
    public void addChild(ParserNode n) {
        _nodeList.add(n);
    }
    
    /**
     *  Add a child node at a particular place in the list of child nodes.
     *
     * @param  p Number describing the position in the list where to add the
     *      child node to.
     * @param  n A child node to add.
     */
    public void addChildAt(int p, ParserNode n) {
        _nodeList.add(p, n);
    }
    
    /**
     *  Get a child from a particular location in the list of children.
     *
     * @param  i A number indicating the location in the list.
     * @return  The child value
     */
    public ParserNode getChild(int i) {
        return (ParserNode) _nodeList.get(i);
    }
    
    /**
     *  Get the iterator on the list of children.
     *
     * @return  Iterator of the list of children.
     */
    public Iterator getChildren() {
        return _nodeList.iterator();
    }
    
    /**
     *  Return the name of the node.
     *
     * @return  the name of the node.
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Get the number of children this Parse node has.
     *
     * @return  the number of children this parse node has.
     */
    public int getNumChildren() {
        return _nodeList.size();
    }
    
    /**
     *  Get the parent node of this parse node.
     *
     * @return  a parse node representing the parent node.
     */
    public ParserNode getParent() {
        return _parent;
    }
    
    /**
     *  Return whether there are any children. Equivalent to
     *  "getNumChildren()>0".
     *
     * @return  a boolean indicating if there are children.
     */
    public boolean hasChildren() {
        return getNumChildren() > 0;
    }
    
    /**
     *  Get the index of a child node in the list of children.
     *
     * @param  i The parse node for which to find the index.
     * @return  A number giving the position of a parse node in the list of
     *      parse nodes.
     */
    public int indexOf(ParserNode i) {
        return _nodeList.indexOf(i);
    }
    
    /**
     *  Remove all children from the parse node.
     */
    public void removeAllChildren() {
        _nodeList.clear();
    }
    
    /**
     *  Remove a parse node from the list of children and return the index
     *  position the parse node had in the list.
     *
     * @param  n Description of the Parameter
     * @return  A number indicating the index at which parse node was
     *      located.
     */
    public int removeChild(ParserNode n) {
        ListIterator l = _nodeList.listIterator();
        int index = 0;
        while( l.hasNext() ) {
            ParserNode pn = (ParserNode) l.next();
            if( pn == n ) {
                l.remove();
                return index;
            }
            index++;
        }
        return index;
    }
    
    /**
     *  Find a parse node from the list of children and return the index
     *  position the parse node has in the list.
     *
     * @param  n Description of the Parameter
     * @return  A number indicating the index at which parse node was
     *      located.
     */
    public int findChild(ParserNode n) {
        ListIterator l = _nodeList.listIterator();
        int index = 0;
        while( l.hasNext() ) {
            ParserNode pn = (ParserNode) l.next();
            if( pn == n ) {
                return index;
            }
            index++;
        }
        return index;
    }
    
    
    /**
     *  set the name of the node.
     *
     * @param  name the name of the node.
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Set the parent node of this parse node.
     *
     * @param  n parse node representing the parent node. parent.
     */
    public void setParent(ParserNode n) {
        _parent = n;
    }
    
    /**
     *  Get the isParsed flag of the node.
     *
     * @return  The status of the isParsed flag.
     */
    public boolean getParsedFlag() {
        return _isParsed;
    }
    
    /**
     *  Set the isParsed flag of the node.
     *
     * @param  isParsed the value of the flag (true of false).
     */
    public void setParsedFlag(boolean isParsed) {
        _isParsed = isParsed;
    }
    
    /**
     *  Return a description of the parse node.
     *
     * @return  a description of the parse node.
     */
    public String toString() {
        return "ParserNode: " + _name;
        
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /**
     *  Set the list of child nodes to this parse node.
     *
     * @param  nodeList The list of parse node.
     */
    private void setNodeList(ArrayList nodeList) {
        _nodeList = nodeList;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Naming of the ParserNode.
     */
    public String _name = null;
    
    /**
     *  Array of ParserNodes which are the Children of this ParserNode.
     */
    public ArrayList _nodeList = null;
    
    /**
     *  the Parent ParserNode of this ParserNode.
     */
    public ParserNode _parent = null;
    
    /**
     *  Flag showing if this node is processed. This flag is used
     *  when schedule tree is derived.
     */
    public boolean _isParsed = false;
    
    
    
}
