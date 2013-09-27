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

package espam.parser.expression;

/**
 * ASTIdentifier
 *
 * @author Bart Kienhuis
 * @version $Id: ASTIdentifier.java,v 1.1 2007/12/07 22:06:57 stefanov Exp $
 */

public class ASTIdentifier extends SimpleNode {
    
    /**
     * Constructor for the ASTIdentifier object
     *
     * @param id Description of the Parameter
     */
    public ASTIdentifier(int id) {
        super(id);
    }
    
    
    /**
     * Constructor for the ASTIdentifier object
     *
     * @param p Description of the Parameter
     * @param id Description of the Parameter
     */
    public ASTIdentifier(ExpressionParser p, int id) {
        super(p, id);
    }
    
    
    /**
     * Gets the name attribute of the ASTIdentifier object
     *
     * @return The name value
     */
    public String getName() {
        return _name;
    }
    
    
    /**
     * Sets the name attribute of the ASTIdentifier object
     *
     * @param name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    
    private String _name;
    
}
