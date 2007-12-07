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

package espam.datamodel.parsetree.statement;

import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// AssignStatement

/**
 *  This class represents and assign statement, also referred to as a </i>
 *  function call</i> , as it appears in a Nested Loop Program. A variable
 *  statement defines a function with a name <i>function</i> that is called.
 *  This function takes variable values from the Right Hand Side (RHS) of
 *  the function call, performs a computation and returns the results of the
 *  computation to the variable of the Left Hand Side (LHS) of the function
 *  call. A function call statement can be written as: <p>
 *
 *  <pre>
 *[value1, value2, ... ] = function( value1, value2, ... );
 *</pre> The assig statement is set-up using three Objects. This Objects
 *  contains reference to the function name. The reference to the RHS and
 *  LHS variables is done in two seperated Objects, respectively RHS
 *  statement and LHS statement. The structure between the tree elements is
 *  maintained by the ParseTree build up from Parse Nodes. This setup for
 *  assign statement is preferable because it make the formating and
 *  processing of function calls easier.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: AssignStatement.java,v 1.9 2002/09/30 14:03:02 kienhuis
 *      Exp $
 */

public class AssignStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Construct an assign statement give a specific function name.
     *
     * @param  functionName The name of the function in the function all.
     */
    public AssignStatement() {
        super("AssignStatement");
    }

    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     * @see  panda.visitor.StatementVisitor
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }

    /**
     *  Clone this AssignStatement
     *
     * @return  a new instance of the AssignStatement.
     */
    public Object clone() {

        AssignStatement as = (AssignStatement) super.clone();
        as.setFunctionName( _functionName );
        as.setNodeName( _nodeName );

        return (as);
	
    }


    /**
     *  Get the function name
     *
     * @return  the function name.
     */
    public String getFunctionName() {
        return _functionName;
    }

    /**
     *  Set the function name.
     *
     * @param  name The name of the function in the function call.
     */
    public void setFunctionName(String name) {
        _functionName = name;
    }

    public String getNodeName() {
        return _nodeName;
    }

    public void setNodeName(String name) {
        _nodeName = name;
    }

    /**
     *  Give the string representation of the assign statement.
     *
     * @return  a string representing the assign statement.
     */
    public String toString() {
        String ln = "AssignStatement: " + _functionName;
        return ln;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The name of the function.
     */
    private String _functionName;

    /**
     *  The name of the node.
     */
    private String _nodeName;
}
