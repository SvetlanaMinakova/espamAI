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

package espam.parser.matlab.scheduler;

import espam.utils.symbolic.expression.Expression;

/**
 * @author Todor Stefanov
 * @version  $Id: ASTcondition.java,v 1.1 2008/05/23 15:04:15 stefanov Exp $
 */

public class ASTcondition extends SimpleNode {

    /**
     *  Constructor for the ASTcondition object
     *
     * @param  id Description of the Parameter
     */
    public ASTcondition(int id) {
        super(id);
    }


    /**
     *  Constructor for the ASTcondition object
     *
     * @param  p Description of the Parameter
     * @param  id Description of the Parameter
     */
    public ASTcondition(Parser p, int id) {
        super(p, id);
    }


    /**
     *  Gets the lhsLinearExp attribute of the ASTcondition object
     *
     * @return  The lhsLinearExp value
     */
    public Expression getLhsLinearExp() {
        return _lhs;
    }


    /**
     *  Gets the relation attribute of the ASTcondition object
     *
     * @return  The relation value
     */
    public int getRelation() {
        return _relation;
    }


    /**
     *  Gets the rhsLinearExp attribute of the ASTcondition object
     *
     * @return  The rhsLinearExp value
     */
    public Expression getRhsLinearExp() {
        return _rhs;
    }


    /**
     *  Sets the lhsLinearExp attribute of the ASTcondition object
     *
     * @param  lhs The new lhsLinearExp value
     */
    public void setLhsLinearExp(Expression lhs) {
        _lhs = lhs;
    }


    /**
     *  Sets the relation attribute of the ASTcondition object
     *
     * @param  relation The new relation value
     */
    public void setRelation(int relation) {
        _relation = relation;
    }


    /**
     *  Sets the rhsLinearExp attribute of the ASTcondition object
     *
     * @param  rhs The new rhsLinearExp value
     */
    public void setRhsLinearExp(Expression rhs) {
        _rhs = rhs;
    }


    private Expression _lhs;
    private int _relation;
    private Expression _rhs;
}
