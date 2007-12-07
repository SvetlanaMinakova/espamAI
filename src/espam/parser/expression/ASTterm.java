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

import espam.utils.symbolic.expression.LinTerm;

/**
 * @author Bart Kienhuis
 * @version $Id: ASTterm.java,v 1.1 2007/12/07 22:06:56 stefanov Exp $
 */

public class ASTterm extends SimpleNode {

    /**
     * Constructor for the ASTterm object
     *
     * @param id Description of the Parameter
     */
    public ASTterm(int id) {
        super(id);
    }


    /**
     * Constructor for the ASTterm object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTterm(ExpressionParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the term attribute of the ASTterm object
     *
     * @return The term value
     */
    public LinTerm getTerm() {
        return _term;
    }


    /**
     * Sets the term attribute of the ASTterm object
     *
     * @param aTerm The new term value
     */
    public void setTerm(LinTerm aTerm) {
        _term = aTerm;
    }


    private LinTerm _term;
}
