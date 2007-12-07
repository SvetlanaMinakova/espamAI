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

/**
 * ASTsignOperator
 *
 * @author Bart Kienhuis
 * @version $Id: ASTsign.java,v 1.1 2007/12/07 22:07:00 stefanov Exp $
 */

public class ASTsign extends SimpleNode {

    /**
     * Constructor for the ASTsign object
     *
     * @param id Description of the Parameter
     */
    public ASTsign(int id) {
        super(id);
    }


    /**
     * Constructor for the ASTsign object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTsign(ExpressionParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the value attribute of the ASTsign object
     *
     * @return The value value
     */
    public int getValue() {
        return _value;
    }


    /**
     * Sets the value attribute of the ASTsign object
     *
     * @param value The new value value
     */
    public void setValue(int value) {
        _value = value;
    }


    private int _value;

}
