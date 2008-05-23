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

import java.util.ArrayList;

import espam.utils.symbolic.expression.Expression;

/**
 * @author Todor Stefanov
 * @version  $Id: ASTcomplexExpression.java,v 1.2 2002/05/28 12:37:06
 *      stefanov Exp $
 */

public class ASTcomplexExpression extends SimpleNode {

    /**
     *  Constructor for the ASTcomplexExpression object
     *
     * @param  id Description of the Parameter
     */
    public ASTcomplexExpression(int id) {
        super(id);
    }


    /**
     *  Constructor for the ASTcomplexExpression object
     *
     * @param  p Description of the Parameter
     * @param  id Description of the Parameter
     */
    public ASTcomplexExpression(Parser p, int id) {
        super(p, id);
    }


    /**
     *  Gets the linearExp attribute of the ASTcomplexExpression object
     *
     * @return  The linearExp value
     */
    public Expression getLinearExp() {
        return _linearExp;
    }


    /**
     *  Sets the linearExp attribute of the ASTcomplexExpression object
     *
     * @param  aList The new linearExp value
     */
    public void setLinearExp(ArrayList aList) {
        _linearExp = new Expression(aList);
        //System.out.println(" -- Complex Expression: " + _linearExp.toString());
    }


    Expression _linearExp = null;

}
