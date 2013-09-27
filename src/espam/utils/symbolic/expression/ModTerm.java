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
// // ModTerm

/**
 * This class represents a Mod term. A Mod Term is equal to a Term except that
 * is describes the div function instead variable or number. The format of a div
 * Term is:
 * <p>
 * 
 * <pre>
 * 
 *  ModTerm = sign * numerator/denominator * Mod( linear expression, divider )
 * 
 * </pre>
 * 
 * <p>
 * 
 * The calculation to find data dependencies are based on the use of integer
 * polytopes. An integer polytop is consider to be dense, which means that all
 * integer points enclosed by the polytop are valid members of the polytop. On
 * these dense polytopes, addition, subtraction, and multiplication are <i>
 * closed </i> operations. This means that the result of the operations is again
 * a dense polytop containing only integer points. Division, however, is not
 * included because this operation will result in fractional numbers. To be able
 * to use division with integer numbers we define the <i>integer division </i> .
 * Let <i>a </i> and <i>b </i> be positive integer numbers, integer division is
 * defined as
 * <p>
 * 
 * <pre>
 *   a = b * q+r, where a,b,q and r are integer values
 * 
 * </pre>
 * 
 * <p>
 * 
 * The integer division is thus written as a multiplication and addition and
 * therefore again a closed operation. Now, <i>b </i> is called the <i> divider
 * </i> and <i>r </i> is called the <i>remainder </i> of the division. The mod
 * function calculates the remainder <i>r </i> of the division.
 * 
 * @author Bart Kienhuis, Todor Stefanov
 * @version $Id: ModTerm.java,v 1.1 2007/12/07 22:06:50 stefanov Exp $
 */

public class ModTerm extends LinTerm {
    
    /**
     * Default empty constructor.
     */
    public ModTerm() {
        super();
    }
    
    // FIXME: Does not seem to be general enough. How about
    // -1/2*mod(1+x,3)?
    /**
     * Constructor to create a ModTerm with a specific linear expression and
     * divider. Create also a unique name that is used in determining the
     * data-dependencies. This unique name will introduce an additional index
     * variable.
     * 
     * @param expression
     *            The expression of the div term.
     * @param divider
     *            The divider of the div term.
     */
    public ModTerm(Expression expression, int divider) {
        super();
        _expression = expression;
        _divider = divider;
        _sign = 1;
        
        // Get a unique identifier
        String name = "mod" + _number++;
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
    
    /**
     * Clone the ModTerm.
     * 
     * @return a new instance of the ModTerm.
     */
    public Object clone() {
        ModTerm newobj = new ModTerm();
        newobj.setSign(_sign);
        newobj.setDivider(_divider);
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
        // System.out.println("Mod Statement: expression: " +
        // _expression.toString());
        int value = _expression.evaluate(point, indices);
        // System.out.println("Value: " + value);
        int val = (getNumerator() / getDenominator()) * (value % _divider);
        // System.out.println("Overall value: " + val);
        return val;
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
     * Get the divider.
     * 
     * @return the divider.
     */
    public int getDivider() {
        return _divider;
    }
    
    /**
     * Get the linear expression of the ModTerm.
     * 
     * @return the linear expression.
     */
    public Expression getExpression() {
        return _expression;
    }
    
    /**
     * Check whether this Term equals a given Term. A ModTerm is considered
     * equal when it is of the same type and the unqiue names given to the
     * ModTerms are equal .
     * 
     * @param term
     *            the reference object with which to compare.
     * @return True if this object is the same as the obj argument; otherwise
     *         false.
     */
    public boolean isEqual(LinTerm term) {
        if (term instanceof ModTerm) {
            if (_name.equals(((LinTerm) term).getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Set the linear expression of the ModTerm.
     * 
     * @param expression
     *            The new expression value
     */
    public void setExpression(Expression expression) {
        this._expression = expression;
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
        // System.out.println(" Substitute DIV " + index + " with " +
        // expression);
        Expression lexp = _expression.substituteExpression(index, expression);
        // System.out.println(" RESULT DIV " + lexp);
        ModTerm d = new ModTerm(lexp, getDivider());
        d.setNumerator(getNumerator());
        d.setSign(getSign());
        d.setDenominator(getDenominator());
        d.setName(getName());
        Expression s = new Expression();
        s.add(d);
        return s;
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
     * Return a description of the ModTerm.
     * 
     * @return a description of the ModTerm.
     */
    public String toString() {
        String ln = "";
        if (getDenominator() == 1) {
            if (getNumerator() == 1) {
                ln += "mod(" + _expression.toString() + "," + _divider + ")";
            } else {
                ln += getNumerator() + "*mod(" + _expression.toString() + ","
                    + _divider + ")";
            }
        } else {
            ln += getNumerator() + "/" + getDenominator() + "*mod("
                + _expression.toString() + "," + _divider + ")";
        }
        return ln;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private methods ////
    
    /**
     * Set the divider of the ModTerm.
     * 
     * @param divider
     *            The new divider value
     */
    private void setDivider(int divider) {
        this._divider = divider;
    }
    
    /**
     * The divider of the Mod Term.
     */
    private int _divider;
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    
    /**
     * The linear expression of the Mod Term.
     */
    private Expression _expression;
    
    /**
     * Static variable, implementating a singleton, to get a unique number for
     * each ModTerm.
     */
    private static int _number = 1;
    
}
