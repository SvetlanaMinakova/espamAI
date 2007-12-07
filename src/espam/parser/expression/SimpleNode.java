/*******************************************************************\

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

package espam.parser.expression;

/**
 * Description of the Class
 *
 * @author Bart Kienhuis
 * @version $Id: SimpleNode.java,v 1.1 2007/12/07 22:06:58 stefanov Exp $
 */
public class SimpleNode implements Node {

    /**
     * Constructor for the SimpleNode object
     *
     * @param i Description of the Parameter
     */
    public SimpleNode(int i) {
        id = i;
    }


    /**
     * Constructor for the SimpleNode object
     *
     * @param p Description of the Parameter
     * @param i Description of the Parameter
     */
    public SimpleNode(ExpressionParser p, int i) {
        this(i);
        parser = p;
    }


    /*
     * Override this method if you want to customize how the node dumps
     * out its children.
     */
    /**
     * Description of the Method
     *
     * @param prefix Description of the Parameter
     */
    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }


    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     * @param i Description of the Parameter
     */
    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        }
        else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }


    /**
     * Description of the Method
     */
    public void jjtClose() {
    }


    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @return Description of the Return Value
     */
    public Node jjtGetChild(int i) {
        return children[i];
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Node jjtGetParent() {
        return parent;
    }


    /**
     * Description of the Method
     */
    public void jjtOpen() {
    }


    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    public void jjtSetParent(Node n) {
        parent = n;
    }


    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped.  If
     * your output uses more than one line you should override
     * toString(String), otherwise overriding toString() is probably all
     * you need to do.
     */
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String toString() {
        return ExpressionParserTreeConstants.jjtNodeName[id];
    }


    /**
     * Description of the Method
     *
     * @param prefix Description of the Parameter
     * @return Description of the Return Value
     */
    public String toString(String prefix) {
        return prefix + toString();
    }


    /**
     * Description of the Field
     */
    protected Node[] children;
    /**
     * Description of the Field
     */
    protected int id;
    /**
     * Description of the Field
     */
    protected Node parent;
    /**
     * Description of the Field
     */
    protected ExpressionParser parser;
}

