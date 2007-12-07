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

package espam.parser.matrix;

/**
 * Description of the Class
 *
 * @author Bart Kienhuis
 * @version $Id: ASTelement.java,v 1.1 2007/12/07 22:07:00 stefanov Exp $
 */
public class ASTelement extends SimpleNode {
    /**
     * Constructor for the ASTelement object
     *
     * @param id Description of the Parameter
     */
    public ASTelement(int id) {
        super(id);
    }


    /**
     * Constructor for the ASTelement object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTelement(JMatrixParser p, int id) {
        super(p, id);
    }

}
