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

public interface ExpressionParserTreeConstants {
    public int JJTCOMPLEXEXPRESSION = 0;
    public int JJTSIMPLEEXPRESSION = 1;
    public int JJTSIGN = 2;
    public int JJTLINEAROPERATOR = 3;
    public int JJTTERMOROPERATOR = 4;
    public int JJTSPECIALTERM = 5;
    public int JJTSPECIALOPERATOR = 6;
    public int JJTTERM = 7;
    public int JJTFRACTION = 8;
    public int JJTIDENTIFIER = 9;
    public int JJTINTEGER = 10;

    public String[] jjtNodeName = {
            "complexExpression",
            "simpleExpression",
            "sign",
            "linearOperator",
            "termOrOperator",
            "specialTerm",
            "specialOperator",
            "term",
            "fraction",
            "Identifier",
            "Integer",
            };
}
