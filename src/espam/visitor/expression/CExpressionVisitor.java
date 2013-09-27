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

package espam.visitor.expression;

import java.util.Iterator;

import espam.utils.symbolic.expression.CeilTerm;
import espam.utils.symbolic.expression.DivTerm;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.FloorTerm;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.expression.ModTerm;
import espam.visitor.ExpressionVisitor;

/**
 * This class describes a visitor to format a linear expression specifically for
 * the 'C' langauge.
 * 
 * @author Bart Kienhuis
 * @version $Id: CExpressionVisitor.java,v 1.1 2007/12/07 22:07:26 stefanov Exp $
 */

public class CExpressionVisitor implements ExpressionVisitor {
    
    /**
     */
    public CExpressionVisitor() {
    }
    
    /**
     * Visit the Ceil Term Object and format.
     * 
     * @param x
     *            the Ceil Term to format.
     * @return string representation of the ceil term.
     */
    public String visit(CeilTerm x) {
        return x.toString();
    }
    
    /**
     * Visit the Div Term Object and format.
     * 
     * @param x
     *            the Div Term to format.
     * @return string representation of the div term.
     */
    /*
     * public String visit(DivTerm x) { String ln = ""; if (x.getDenominator() ==
     * 1) { if (x.getNumerator() == 1) { ln += "(" + visit(x.getExpression()) +
     * ")" + "/" + x.getDivider(); } else { ln += x.getNumerator() + "*(" +
     * visit(x.getExpression()) + ")" + "/" + x.getDivider(); } } else { ln +=
     * x.getNumerator() + "/" + x.getDenominator() + "(" +
     * visit(x.getExpression()) + ")" + "/" + x.getDivider(); } return ln; }
     */
    public String visit(DivTerm x) {
        String ln = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                ln += "ddiv(" + visit(x.getExpression()) + "," + x.getDivider()
                    + ")";
            } else {
                ln += x.getNumerator() + "*ddiv(" + visit(x.getExpression())
                    + "," + x.getDivider() + ")";
            }
        } else {
            // FIXME ---- BUG
            ln += x.getNumerator() + "/" + x.getDenominator() + "ddiv("
                + visit(x.getExpression()) + "," + x.getDivider() + ")";
        }
        return ln;
    }
    
    /**
     * Visit the Expression Object and format.
     * 
     * @param x
     *            the Expression to format.
     * @return string representation of the expression.
     */
    public String visit(Expression x) {
        String var = "";
        if (x.isEmpty()) {
            return "0";
        }
        
        // A Linear Expression can have a denominator
        if (x.getDenominator() != 1) {
            var += "(double)(";
        }
        
        Iterator i = x.iterator();
        int position = 0;
        while (i.hasNext()) {
            LinTerm term = (LinTerm) i.next();
            if (position > 0) {
                if (term.isPositive()) {
                    // Add a '+' sign
                    var += " + ";
                } else {
                    // Use the '-' sign
                    var += "";
                }
            }
            var += term.accept(this);
            position++;
        }
        
        if (x.getDenominator() != 1) {
            var += ")/" + x.getDenominator();
        }
        
        return var;
    }
    
    /**
     * Visit the Floor Term Object and format.
     * 
     * @param x
     *            the Floor Term to format.
     * @return string representation of the floor term.
     */
    public String visit(FloorTerm x) {
        return "floor1(" + x.getExpression() + ")";
        // x.toString();
    }
    
    /**
     * Visit the Linear Term Object and format.
     * 
     * @param x
     *            the Linear Term to format.
     * @return string representation of the linear term
     */
    public String visit(LinTerm x) {
        return x.toString();
    }
    
    /**
     * Visit the Maximum Term Object and format.
     * 
     * @param x
     *            the Maximum Term to format.
     * @return string representation of the maximum term.
     */
    public String visit(MaximumTerm x) {
        return x.toString();
    }
    
    /**
     * Visit the Minimum Term Object and format.
     * 
     * @param x
     *            the Minimum Term to format.
     * @return string representation of the minimum term.
     */
    public String visit(MinimumTerm x) {
        return x.toString();
    }
    
    /**
     * Visit the Mod Term Object and format.
     * 
     * @param x
     *            the Mod Term to format.
     * @return string representation of the mod term.
     */
    public String visit(ModTerm x) {
        String ln = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                ln += "(" + visit(x.getExpression()) + ")" + "%"
                    + x.getDivider();
            } else {
                ln += x.getNumerator() + "*(" + visit(x.getExpression()) + ")"
                    + "%" + x.getDivider();
            }
        } else {
            ln += x.getNumerator() + "/" + x.getDenominator() + "("
                + visit(x.getExpression()) + ")" + "%" + x.getDivider();
        }
        return ln;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    
}
