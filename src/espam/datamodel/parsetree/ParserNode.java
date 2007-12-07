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

package espam.datamodel.parsetree;

import java.util.Iterator;

import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// ParserNode

/**
 *  Interface ParserNode defines an interface for objects, to be used to
 *  build up tree-based data structures. In Dgparser, the main data
 *  structure used is a so-called <i>Parse tree</i> . A Parse tree is a
 *  Graph G(E, V) in which the nodes represent statements and the edges
 *  represent scope relationships between statements. A parse tree has by
 *  definition only a single starting point, called the <i>Root</i> of the
 *  tree. <p>
 *
 *  A Nested Loop Program can be represented as a Parse Tee. Parse trees are
 *  used extensively in the area of computer science concerning syntax rules
 *  and language specifications. In this area a language is written in terms
 *  of a <i>grammar</i> . A grammar can be expressed as a tree. Also,
 *  intermediate results found in the data-dependence analysis are also
 *  simply represented by means of a parse tree. <p>
 *
 *  Using Parse trees, it is very easy to determine the scope a statement
 *  has with regards to other statements. Suppose there is Tree containing
 *  two nodes <i>A</> and <i>B</> . Node <i>A</> is found closer to the Root
 *  of the tree than <i>B</> . Now, if a path exists from <i>A</i> to <i>B
 *  </i>, it means that <i>B</I> is within scope of <i>A</i> . Dgparser uses
 *  this feature extensively in the process of determining data-dependence
 *  relations. <p>
 *
 *  In a parse tree, there are particular statements that can only exist
 *  when they are followed by an other statement. These statements are
 *  called <i>non-terminals</i> . An example of an non-terminal is the <i>if
 *  </i> statement. If the boolean condition of the if-statement is true, an
 *  other statement will be executed. So the if-statement will have at least
 *  one child, and is therefore a non-terminal statement. A statement that
 *  has no children, is called a <i>terminal</i> or <i>leaf</i> . A <i>
 *  function call</i> is example of a terminal.
 *
 * @author  Todor Stefanov
 * @version  $Id: ParserNode.java,v 1.1 2007/12/07 22:09:11 stefanov Exp $
 */

public interface ParserNode {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Create a clone of this Object, default shallow cloning is performed
     *
     * @return  Description of the Return Value
     */

     public Object clone();

    /**
     *  Create a recursive clone of this Object and all child objects.
     *
     * @return  Description of the Return Value
     */
    public Object deepClone();


    /**
     *  Remove recursively this Object and all child objects.
     */
    public void deepRemove();


    /**
     *  Accept a Visitor
     *
     * @param  x A Visitor Object.
     * @see  panda.visitor.Visitor
     */
    public void accept(StatementVisitor x);

    /**
     *  Add a child node to the list of child nodes.
     *
     * @param  n A child node to add.
     */
    public void addChild(ParserNode n);

    /**
     *  Add a child node at a particular place in the list of child nodes.
     *
     * @param  p Number describing the position in the list where to add the
     *      child node to.
     * @param  n A child node to add.
     */
    public void addChildAt(int p, ParserNode n);

    /**
     *  Get a child from a particular location in the list of children.
     *
     * @param  i A number indicating the location in the list.
     * @return  The child value
     */
    public ParserNode getChild(int i);

    /**
     *  Get the iterator on the list of children.
     *
     * @return  Iterator of the list of children.
     */
    public Iterator getChildren();

    /**
     *  Return whether there are any children. Equivalent to
     *  "getNumChildren()>0".
     *
     * @return  a boolean indicating if there are children.
     */
    public boolean hasChildren();

    /**
     *  Get the number of children this Parse node has.
     *
     * @return  the number of children this parse node has.
     */
    public int getNumChildren();

    /**
     *  Get the parent node of this parse node.
     *
     * @return  a parse node representing the parent node.
     */
    public ParserNode getParent();

    /**
     *  Get the index of a child node in the list of children.
     *
     * @param  i The parse node for which to find the index.
     * @return  A number giving the position of a parse node in the list of
     *      parse nodes.
     */
    public int indexOf(ParserNode i);

    /**
     *  Remove a parse node from the list of children and return the index
     *  position the parse node had in the list.
     *
     * @param  o The parse node to remove.
     * @return  A number indicating the index at which parse node was
     *      located.
     */
    public int removeChild(ParserNode o);

    /**
     *  Find a parse node from the list of children and return the index
     *  position the parse node has in the list.
     *
     * @param  n Description of the Parameter
     * @return  A number indicating the index at which parse node was
     *      located.
     */
    public int findChild(ParserNode n);

    /**
     *  Remove all children from the parse node.
     */
    public void removeAllChildren();

    /**
     *  Set the parent node of this parse node.
     *
     * @param  n parse node representing the parent node. parent.
     */
    public void setParent(ParserNode n);

    /**
     *  Give the name of the parse node.
     *
     * @return  the name;
     */
    public String toString();

    /**
     *  Give the name of the parse node.
     *
     * @return  the name;
     */
    public String getName();
    
     /**
     *  Get the isParsed flag of the node.
     *
     * @return  The status of the isParsed flag.
     */
    public boolean getParsedFlag();


    /**
     *  Set the isParsed flag of the node.
     *
     * @param  isParsed the value of the flag (true of false).
     */
    public void setParsedFlag(boolean isParsed);
}
