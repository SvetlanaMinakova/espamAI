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

import espam.utils.symbolic.expression.Expression;

/**
 * Description of the Class
 *
 * @author Bart Kienhuis
 * @version $Id: ASTcomplexExpression.java,v 1.2 2010/08/13 14:16:13 sven Exp $
 */
public class ASTcomplexExpression extends SimpleNode {
    /**
     * Constructor for the ASTcomplexExpression object
     *
     * @param id Description of the Parameter
     */
    public ASTcomplexExpression(int id) {
        super(id);
    }


    /**
     * Constructor for the ASTcomplexExpression object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTcomplexExpression(ExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Gets the linearExp attribute of the ASTcomplexExpression object
     *
     * @return The linearExp value
     */
    public Expression getExpression() {
        return _linearExp;
    }


    /**
     * Sets the linearExp attribute of the ASTcomplexExpression object
     *
     * @param aList The new linearExp value
     */
    public void setExpression(Expression exp) {
        _linearExp = exp;
    }


    Expression _linearExp = null;

}
