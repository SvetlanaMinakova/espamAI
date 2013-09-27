/*******************************************************************\
  * 
  This file is donated to ESPAM by Compaan Design BV (www.compaandesign.com) 
  Copyright (c) 2000 - 2005 Leiden University (LERC group at LIACS)
  Copyright (c) 2005 - 2007 CompaanDesign BV, The Netherlands
  All rights reserved.
  
  The use and distribution terms for this software are covered by the 
  Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
  which can be found in the file LICENSE at the root of this distribution.
  By using this software in any fashion, you are agreeing to be bound by 
  the terms of this license.
  
  You must not remove this notice, or any other, from this software.
  
  \*******************************************************************/

package espam.parser.matrix;

/**
 * Description of the Class
 *
 * @author Bart Kienhuis
 * @version $Id: JJTJMatrixParserState.java,v 1.1 2007/12/07 22:07:01 stefanov Exp $
 */
class JJTJMatrixParserState {
    
    /**
     * Constructor for the JJTJMatrixParserState object
     */
    JJTJMatrixParserState() {
        nodes = new java.util.Stack();
        marks = new java.util.Stack();
        sp = 0;
        mk = 0;
    }
    
    
    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    void clearNodeScope(Node n) {
        while (sp > mk) {
            popNode();
        }
        mk = ((Integer) marks.pop()).intValue();
    }
    
    
    /*
     * A definite node is constructed from a specified number of
     * children.  That number of nodes are popped from the stack and
     * made the children of the definite node.  Then the definite node
     * is pushed on to the stack.
     */
    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     * @param num Description of the Parameter
     */
    void closeNodeScope(Node n, int num) {
        mk = ((Integer) marks.pop()).intValue();
        while (num-- > 0) {
            Node c = popNode();
            c.jjtSetParent(n);
            n.jjtAddChild(c, num);
        }
        n.jjtClose();
        pushNode(n);
        node_created = true;
    }
    
    
    /*
     * A conditional node is constructed if its condition is true.  All
     * the nodes that have been pushed since the node was opened are
     * made children of the the conditional node, which is then pushed
     * on to the stack.  If the condition is false the node is not
     * constructed and they are left on the stack.
     */
    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     * @param condition Description of the Parameter
     */
    void closeNodeScope(Node n, boolean condition) {
        if (condition) {
            int a = nodeArity();
            mk = ((Integer) marks.pop()).intValue();
            while (a-- > 0) {
                Node c = popNode();
                c.jjtSetParent(n);
                n.jjtAddChild(c, a);
            }
            n.jjtClose();
            pushNode(n);
            node_created = true;
        }
        else {
            mk = ((Integer) marks.pop()).intValue();
            node_created = false;
        }
    }
    
    
    /*
     * Returns the number of children on the stack in the current node
     * scope.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    int nodeArity() {
        return sp - mk;
    }
    
    
    /*
     * Determines whether the current node was actually closed and
     * pushed.  This should only be called in the final user action of a
     * node scope.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    boolean nodeCreated() {
        return node_created;
    }
    
    
    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    void openNodeScope(Node n) {
        marks.push(new Integer(mk));
        mk = sp;
        n.jjtOpen();
    }
    
    
    /*
     * Returns the node currently on the top of the stack.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    Node peekNode() {
        return (Node) nodes.peek();
    }
    
    
    /*
     * Returns the node on the top of the stack, and remove it from the
     * stack.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    Node popNode() {
        if (--sp < mk) {
            mk = ((Integer) marks.pop()).intValue();
        }
        return (Node) nodes.pop();
    }
    
    
    /*
     * Pushes a node on to the stack.
     */
    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    void pushNode(Node n) {
        nodes.push(n);
        ++sp;
    }
    
    
    /*
     * Call this to reinitialize the node stack.  It is called
     * automatically by the parser's ReInit() method.
     */
    /**
     * Description of the Method
     */
    void reset() {
        nodes.removeAllElements();
        marks.removeAllElements();
        sp = 0;
        mk = 0;
    }
    
    
    /*
     * Returns the root node of the AST.  It only makes sense to call
     * this after a successful parse.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    Node rootNode() {
        return (Node) nodes.elementAt(0);
    }
    
    
    private java.util.Stack marks;
    // number of nodes on stack
    private int mk;
    // current mark
    private boolean node_created;
    private java.util.Stack nodes;
    
    private int sp;
}
