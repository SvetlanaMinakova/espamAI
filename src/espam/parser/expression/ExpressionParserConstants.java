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

public interface ExpressionParserConstants {

    int EOF = 0;
    int ELSE = 1;
    int END = 2;
    int FOR = 3;
    int IF = 4;
    int DIV = 5;
    int MOD = 6;
    int FLOOR = 7;
    int CEIL = 8;
    int EQUAL = 9;
    int MIN = 10;
    int MAX = 11;
    int IPD = 12;
    int OPD = 13;
    int DEFFUNCTION = 14;
    int DEFPARAMETER = 15;
    int EXP = 16;
    int MUL = 17;
    int FRACTION = 18;
    int ADD = 19;
    int SUB = 20;
    int CONCAT = 21;
    int EQ = 22;
    int NEQ = 23;
    int GE = 24;
    int LE = 25;
    int GT = 26;
    int LO = 27;
    int LEFTBRACKET = 28;
    int RIGHTBRACKET = 29;
    int SEMICOLON = 30;
    int COLON = 31;
    int MATLABBEGIN = 32;
    int MATLAB_COMMENT = 40;
    int INTEGER_LITERAL = 42;
    int DECIMAL_LITERAL = 43;
    int HEX_LITERAL = 44;
    int OCTAL_LITERAL = 45;
    int IDENTIFIER = 46;
    int LETTER = 47;
    int DIGIT = 48;

    int DEFAULT = 0;
    int IN_MATLAB_COMMENT = 1;

    String[] tokenImage = {
            "<EOF>",
            "\"else\"",
            "\"end\"",
            "\"for\"",
            "\"if\"",
            "\"div\"",
            "\"mod\"",
            "\"floor\"",
            "\"ceil\"",
            "\"equal\"",
            "\"min\"",
            "\"max\"",
            "\"ipd\"",
            "\"opd\"",
            "\"%function\"",
            "\"%parameter\"",
            "\"**\"",
            "\"*\"",
            "\"/\"",
            "\"+\"",
            "\"-\"",
            "\"&\"",
            "\"=\"",
            "\"!=\"",
            "\">=\"",
            "\"<=\"",
            "\">\"",
            "\"<\"",
            "\"[\"",
            "\"]\"",
            "\";\"",
            "\":\"",
            "\"%matlab\"",
            "\" \"",
            "\"\\t\"",
            "\"\\n\"",
            "\"\\r\"",
            "<token of kind 37>",
            "<token of kind 38>",
            "<token of kind 39>",
            "\"%end\"",
            "<token of kind 41>",
            "<INTEGER_LITERAL>",
            "<DECIMAL_LITERAL>",
            "<HEX_LITERAL>",
            "<OCTAL_LITERAL>",
            "<IDENTIFIER>",
            "<LETTER>",
            "<DIGIT>",
            "\"(\"",
            "\",\"",
            "\")\"",
            };

}
