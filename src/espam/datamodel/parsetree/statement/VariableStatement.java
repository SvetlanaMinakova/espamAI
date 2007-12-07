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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import espam.utils.symbolic.expression.Expression;

import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// VariableStatement

/**
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: VariableStatement.java,v 1.6 2002/10/02 08:56:49 kienhuis
 *      Exp $
 */

public class VariableStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor that creates a VariableStatement with a given name
     *
     * @param  id Name of the Variable
     */
    public VariableStatement(String id) {
        super("VariableStatement");
        _variableName = id;
    }

    /**
     *  Clone this VariableStatement
     *
     * @return  a new instance of the VariableStatement.
     */
    public Object clone() {

            VariableStatement vs = (VariableStatement) super.clone();
            vs.setVariableName( _variableName );
            return (vs);
    }

    /**
     *  Get the Name the variable defined by this variable statement.
     *
     * @return  the name of the variable statement.
     */
    public String getVariableName() {
        return _variableName;
    }

    /**
     *  Set the name of this variable statement.
     *
     * @param  name The name of this variable statement.
     */
    public void setVariableName(String name) {
        _variableName = name;
    }

    /**
     *  Return a String representation of the Variable statement. T
     *
     * @return  Description of the Return Value
     */
    public String toString() {
        return _variableName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the Variable
     */
    private String _variableName = "";
}
