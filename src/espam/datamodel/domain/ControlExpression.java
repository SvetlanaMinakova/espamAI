
package espam.datamodel.domain;

import java.util.Vector;

import espam.visitor.ADGraphVisitor;

import espam.utils.symbolic.expression.Expression;

//////////////////////////////////////////////////////////////////////////
//// ControlExpression

/**
 * This class contains a name and an expression.
 * In "d1 = div(i,2)" 'd1' is the name and 'div(i,2)' is the expression
 *
 * @author Hristo Nikolov
 * @version  $Id: ControlExpression.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class ControlExpression implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ControlExpression with a name.
     *
     */
    public ControlExpression(String name) {
        _name = name;
        _expression = new Expression();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof ControlExpression))
            return false;
        ControlExpression o = (ControlExpression) obj;
        return _name.equals(o._name)
            && _expression.equals(o._expression);
    }
    
    /**
     *  Clone this control expression
     *
     * @return  a new instance of the ControlExpression.
     */
    public Object clone() {
        try {
            ControlExpression newObj = (ControlExpression) super.clone();
            newObj.setName(_name);
            newObj.setExpression( (Expression) _expression.clone() );
            return( newObj );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this control expression.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this control expression.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the expression of this control expression.
     *
     * @return  the expression
     */
    public Expression getExpression() {
        return _expression;
    }
    
    /**
     *  Set the expression of this control expression.
     *
     * @param  expression The new expression
     */
    public void setExpression(Expression expression) {
        _expression = expression;
    }
    
    /**
     *  Return a description of the control expression.
     *
     * @return  a description of the control expression.
     */
    public String toString() {
        return "Control Expression: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the control expresion.
     */
    private String _name = null;
    
    
    /**
     *  The expression itself.
     */
    private Expression _expression = null;
}
