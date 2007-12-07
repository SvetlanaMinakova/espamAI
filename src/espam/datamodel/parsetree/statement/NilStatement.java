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

//////////////////////////////////////////////////////////////////////////
//// NilStatement

/**
 *
 * @author Todor Stefanov
 * @version  $Id: NilStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */

public class NilStatement extends Statement {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor that creates a NilStatement with a given name a list of
     *  linear expressions.
     *
     */
    public NilStatement() {
        super("NilStatement");
    }


    /**
     *  Accept a StatementVisitor
     *
     * @return  Description of the Return Value
     * @see  dgparser.visitor.StatementVisitor
     */
    //public void accept( StatementVisitor x ) throws DgParserException {
    //   x.visitStatement( this );
    //}


    /**
     *  Clone this NilStatement.
     *
     * @return  a new instance of the NilStatement.
     */
     public Object clone() {

            NilStatement ns = (NilStatement) super.clone();
	    return (ns);
     }

    /**
     *  Compare this variable statement object with another object. A
     *  variable is equal to this Object when it is a variable statement
     *  with the same name. This function is used to find common variable
     *  names.
     *
     * @param  x the reference object with which to compare.
     * @return  True if this object is the same as the obj argument; false
     *      otherwise.
     */
    public boolean equals(Object x) {
        if (x instanceof NilStatement) {
            return true;
        }
        return false;
    }


    /**
     *  Set Column number
     *
     * @param  number The new columnNumber value
     */
    public void setColumnNumber(int number) {
        _lineColumn = number;
    }


    /**
     *  Set line number
     *
     * @param  number The new lineNumber value
     */
    public void setLineNumber(int number) {
        _lineNumber = number;
    }


    /**
     *  Return a String representation of the Nil statement. The
     *  representation is of the following form <br>
     *  <i> variableName ( linearExp1, linearExp2, ... , linearExpN ) </i>
     *  <br>
     *  . To get the proper formating of the linear expression, this method
     *  uses the toString method of the linear expressions.
     *
     * @return  Description of the Return Value
     */
    public String toString() {
        return "nil";
    }


    /**
     *  The column number where this assign statement is done
     */
    private int _lineColumn;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The line number where this assign statement is done
     */
    private int _lineNumber;

}
