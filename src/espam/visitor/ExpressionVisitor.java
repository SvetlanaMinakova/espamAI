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

package espam.visitor;

import espam.utils.symbolic.expression.CeilTerm;
import espam.utils.symbolic.expression.DivTerm;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.FloorTerm;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.expression.ModTerm;

/**
 * This interface describes a visitor to be used in formatting a linear
 * expression in a specific way.
 * 
 * @author Bart Kienhuis
 * @version $Id: ExpressionVisitor.java,v 1.1 2007/12/07 22:07:23 stefanov Exp $
 */

public interface ExpressionVisitor extends Visitor {
    
    /**
     * Visit the Ceil Term Object and format.
     * 
     * @param x
     *            the Ceil Term to format.
     * @return string representation of the ceil term.
     */
    public String visit(CeilTerm x);
    
    /**
     * Visit the Div Term Object and format.
     * 
     * @param x
     *            the Div Term to format.
     * @return string representation of the div term.
     */
    public String visit(DivTerm x);
    
    /**
     * Visit the Expression Object and format.
     * 
     * @param x
     *            the Expression to format.
     * @return string representation of the expression.
     */
    public String visit(Expression x);
    
    /**
     * Visit the Floor Term Object and format.
     * 
     * @param x
     *            the Floor Term to format.
     * @return string representation of the floor term.
     */
    public String visit(FloorTerm x);
    
    /**
     * Visit the Linear Term Object and format.
     * 
     * @param x
     *            the Linear Term to format.
     * @return string representation of the linear term
     */
    public String visit(LinTerm x);
    
    /**
     * Visit the Maximum Term Object and format.
     * 
     * @param x
     *            the Maximum Term to format.
     * @return string representation of the maximum term.
     */
    public String visit(MaximumTerm x);
    
    /**
     * Visit the Minimum Term Object and format.
     * 
     * @param x
     *            the Minimum Term to format.
     * @return string representation of the minimum term.
     */
    public String visit(MinimumTerm x);
    
    /**
     * Visit the Mod Term Object and format.
     * 
     * @param x
     *            the Mod Term to format.
     * @return string representation of the mod term.
     */
    public String visit(ModTerm x);
    
}
