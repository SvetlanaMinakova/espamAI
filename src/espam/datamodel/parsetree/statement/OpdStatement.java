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
//// OpdStatement
/*
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: OpdStatement.java,v 1.1 2007/12/07 22:09:12 stefanov Exp $
 */

public class OpdStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

   public OpdStatement() {
        super("OpdStatement");
    }

    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }

    /**
     *  Clone this OpdStatement
     *
     * @return  a new instance of the OpdStatement.
     */
    public Object clone() {

            OpdStatement os = (OpdStatement) super.clone();
            os.setProcessName( _processName );
            os.setGateName( _gateName );
            os.setNodeName( _nodeName );
            os.setArgumentName( _argumentName );
            return (os);
    }


    /**
     *  Get the argument name
     *
     * @return  the name.
     */
    public String getArgumentName() {
        return _argumentName;
    }

    /**
     *  Set the argument name
     *
     */
    public void setArgumentName(String argumentName) {
        _argumentName = argumentName;
    }

    /**
     *  Get the process name
     *
     * @return  the name.
     */
    public String getProcessName() {
        return _processName;
    }

    /**
     *  Set the process name
     *
     */
    public void setProcessName(String processName) {
        _processName = processName;
    }

    /**
     *  Get the gate name
     *
     * @return  the name.
     */
    public String getGateName() {
        return _gateName;
    }

    /**
     *  Set the gate name
     *
     */
    public void setGateName(String gateName) {
        _gateName = gateName;
    }

    /**
     *  Get the node name
     *
     * @return  the name.
     */
    public String getNodeName() {
        return _nodeName;
    }

    /**
     *  Set the node name
     *
     */
    public void setNodeName(String nodeName) {
        _nodeName = nodeName;
    }

    /**
     *  Give the string representation of the opd statement.
     *
     * @return  a string representing the opd statement.
     */
    public String toString() {
        String ln = "OPD: <"
                + _processName
                + _gateName + ", "
                + _argumentName
		+ _nodeName
		+ ">";
        return ln;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The name of the process.
     */
    private String _processName;

    /**
     *  The name of the gate.
     */
    private String _gateName;
    /**
     *  The name of the node.
     */
    private String _nodeName;

    /**
     *  The name of the argument.
     */
    private String _argumentName;
}
