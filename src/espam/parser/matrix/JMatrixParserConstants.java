/*******************************************************************\
  * 
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

package espam.parser.matrix;

public interface JMatrixParserConstants {
    
    int EOF = 0;
    int NAME = 5;
    int NATURAL = 6;
    int SIGN = 7;
    int COMMENT = 8;
    
    int DEFAULT = 0;
    
    String[] tokenImage = {
        "<EOF>",
        "\" \"",
        "\"\\t\"",
        "\"\\n\"",
        "\"\\r\"",
        "<NAME>",
        "<NATURAL>",
        "<SIGN>",
        "<COMMENT>",
        "\"[\"",
        "\";\"",
        "\"]\"",
        "\",\"",
    };
    
}
