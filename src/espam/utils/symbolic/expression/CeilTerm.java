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

package espam.utils.symbolic.expression;

import java.util.List;

import espam.visitor.ExpressionVisitor;

// ////////////////////////////////////////////////////////////////////////
// // CeilTerm

/**
 * This class represents a Ceil term. A Ceil Term is equal to a Term except that
 * is describes the Ceil function instead of a variable or number. The format of
 * a Ceil Term is:
 * <p>
 * 
 * <pre>
 * 
 * 
 *   CeilTerm = sign * numerator/denominator * Ceil( linear expression )
 * 
 * 
 * </pre>
 * 
 * <p>
 * 
 * The ceil function rounds a linear expression to the nearest integer towards
 * positive infinity. The result is an integer. The linear expression will
 * typically contain fractional numbers of type numerator/denominator.
 * 
 * @author Bart Kienhuis, Todor Stefanov
 * @version $Id: CeilTerm.java,v 1.1 2007/12/07 22:06:49 stefanov Exp $
 */

public class CeilTerm extends LinTerm {
    
    /**
     * default empty constructor.
     */
    public CeilTerm() {
        super();
    }
    
    // FIXME: Does not seem to be general enough. How about
    // -1/2*ceil(1+x)?
    
    /**
     * Constructor to create a CeilTerm with a specific linear expression. It
     * also creates a unique name that is used in determining the
     * data-dependencies. This unique name will introduce an additional index
     * variable.
     * 
     * @param expression
     *            The expression of the Ceil term.
     */
    public CeilTerm(Expression expression) {
        super();
        _expression = expression;
        
        // Get a unique identifier
        String name = "c" + _number++;
        setName(name);
        
    }
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    
    /**
     * Accept an ExpressionVisitor.
     * 
     * @param x
     *            The expression visitor.
     * @return String representing the Term.
     * @see panda.visitor.ExpressionVisitor
     */
    public String accept(ExpressionVisitor x) {
        return x.visit(this);
    }
    
    // FIXEM: Should constructor not be private?
    /**
     * Clone the CeilTerm.
     * 
     * @return a new instance of the CeilTerm.
     */
    public Object clone() {
        CeilTerm newobj = new CeilTerm();
        newobj.setSign(_sign);
        newobj.setExpression((Expression) _expression.clone());
        newobj.setName(_name);
        newobj.setNumerator(this.getNumerator());
        newobj.setDenominator(this.getDenominator());
        return newobj;
    }
    
    /**
     * Evaluate the expression for a particular point.
     * 
     * @param point
     *            Description of the Parameter
     * @param indices
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public int evaluate(List point, List indices) {
        int value = _expression.evaluate(point, indices);
        return getNumerator() / getDenominator() * value;
    }
    
    /**
     * Return the name of an additional index.
     * 
     * @return a null reference.
     */
    public String getAdditionalIndex() {
        return _name;
    }
    
    /**
     * Get the linear expression of the CeilTerm.
     * 
     * @return the linear expression.
     */
    public Expression getExpression() {
        return _expression;
    }
    
    /**
     * Check whether this Term equals a given Term. A CeilTerm is considered
     * equal when it is of the same type and the unqiue names given to the
     * CeilTerms are equal .
     * 
     * @param term
     *            the reference object with which to compare.
     * @return True if this object is the same as the obj argument; otherwise
     *         false.
     */
    public boolean isEqual(LinTerm term) {
        if (term instanceof CeilTerm) {
            if (_name.equals(((LinTerm) term).getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Substitue a term in the LinearExp with a new Linear expression.
     * 
     * @param index
     *            the index to substitue
     * @param expression
     *            the expression to use for the substitution.
     * @return Description of the Return Value
     */
    public Expression substituteExpression(String index, Expression expression) {
        throw new Error("Substitution by Expression not implemeted in CeilTerm");
    }
    
    /**
     * Substitute a particular index with a new index name.
     * 
     * @param index
     *            the index to substitute
     * @param newIndexName
     *            the substitute name
     */
    public void substituteName(String index, String newIndexName) {
        // Recursively substitute names in the expression.
        _expression.substituteName(index, newIndexName);
        
        // see if it matches the name to substitue
        if (getName().equals(index)) {
            // substitute with new name
            setName(newIndexName);
        }
    }
    
    /**
     * Return a description of the CeilTerm.
     * 
     * @return a description of the CeilTerm.
     */
    
    public String toString() {
        String ln = "";
        if (getDenominator() == 1) {
            if (getNumerator() == 1) {
                ln += "ceil(" + _expression.toString() + ")";
            } else {
                ln += getNumerator() + "*ceil(" + _expression.toString() + ")";
            }
        } else {
            ln += getNumerator() + "/" + getDenominator() + "*ceil("
                + _expression.toString() + ")";
        }
        return ln;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private methods ////
    
    /**
     * Set the linear expression of the CeilTerm.
     * 
     * @param expression
     *            The new expression value
     */
    private void setExpression(Expression expression) {
        this._expression = expression;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    
    /**
     * The linear expression.
     */
    private Expression _expression;
    
    /**
     * Static variable, implementing a singleton, to get a unique number for
     * each CeilTerm.
     */
    private static int _number = 1;
    
}
