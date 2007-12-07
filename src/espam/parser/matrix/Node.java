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

package espam.parser.matrix;

/*
 * All AST nodes must implement this interface.  It provides basic
 * machinery for constructing the parent and child relationships
 * between nodes.
 *
 * @author Bart Kienhuis
 * @version $Id: Node.java,v 1.1 2007/12/07 22:07:01 stefanov Exp $
 */
public interface Node {

    /**
     * This method is called after the node has been made the current node.
     * It indicates that child nodes can now be added to it.
     */
    public void jjtOpen();


    /**
     * This method is called after all the child nodes have been added.
     */
    public void jjtClose();


    /**
     * This pair of methods are used to inform the node of its parent.
     *
     * @param n Description of the Parameter
     */
    public void jjtSetParent(Node n);


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Node jjtGetParent();


    /**
     * This method tells the node to add its argument to the node's list of
     * children.
     *
     * @param n Description of the Parameter
     * @param i Description of the Parameter
     */
    public void jjtAddChild(Node n, int i);


    /**
     * This method returns a child node. The children are numbered from
     * zero, left to right.
     *
     * @param i Description of the Parameter
     * @return Description of the Return Value
     */
    public Node jjtGetChild(int i);


    /**
     * Return the number of children the node has.
     *
     * @return Description of the Return Value
     */
    public int jjtGetNumChildren();
}
