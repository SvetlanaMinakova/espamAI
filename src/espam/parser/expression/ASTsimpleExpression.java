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

import java.util.ArrayList;

import espam.utils.symbolic.expression.Expression;

/**
 * @author Bart Kienhuis
 * @version $Id: ASTsimpleExpression.java,v 1.1 2007/12/07 22:07:00 stefanov Exp $
 */

public class ASTsimpleExpression extends SimpleNode {

    /**
     * Constructor for the ASTsimpleExpression object
     *
     * @param id Description of the Parameter
     */
    public ASTsimpleExpression(int id) {
        super(id);
    }


    /**
     * Constructor for the ASTsimpleExpression object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTsimpleExpression(ExpressionParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the linearExp attribute of the ASTsimpleExpression object
     *
     * @return The linearExp value
     */
    public Expression getLinearExp() {
        return _linearExp;
    }


    /**
     * Sets the linearExp attribute of the ASTsimpleExpression object
     *
     * @param aList The new linearExp value
     */
    public void setLinearExp(ArrayList aList) {
        _linearExp = new Expression(aList);
    }


    Expression _linearExp = null;

}
