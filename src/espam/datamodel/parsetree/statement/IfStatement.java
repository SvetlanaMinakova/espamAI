
package espam.datamodel.parsetree.statement;

import espam.utils.symbolic.expression.Expression;
import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// IfStatement

/**
 *  This class represents an If Statement as it appears in a Nested Loop
 *  Program. The general format of an If Statement is: <p>
 *
 *  <pre> if ( expression relation expression ) == true then </pre> <p>
 *
 *  The relations possible are:
 *  <ol>
 *    <li> <b> a < b</b> , Less then equal (LT)
 *    <li> <b> a <= b</b> , Less Or Equal to (LE)
 *    <li> <b> a > b</b> , Greater Then Equal (GT)
 *    <li> <b> a >= b</b> , Greater or Equal to (GE)
 *  </ol>
 *  All these forms are transformed into the following canonical
 *  representation: <pre> if ( expression >= 0 ) == true then </pre> <p>
 *
 *  The relation equal or not equal to are not allowed. These two relations
 *  require the use of two if-statements to placed the if-statement in the
 *  canonical form. Currently, it is assumed that in a Nested Loop Program,
 *  the equal and not equal are replaced with two if-statements. So the
 *  relations a == b, is replaced by <pre>
 *if a-b>= 0,
 *if b-a>= 0,
 *a and b are equal
 *</pre>
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: IfStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */

public class IfStatement extends Statement {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Construct an canonical if statement using an linear expression.
     *
     * @param  condition The linear expression of the if statement.
     */
    public IfStatement(Expression condition, int sign) {
        super("IfStatement");
        _condition = condition;
        _sign = sign;
    }
    
    /**
     *  Empty constructor
     *
     * @param  condition The linear expression of the if statement.
     */
    public IfStatement() {
        super("IfStatement");
    }
    
    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     * @see  panda.visitor.StatementVisitor
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }
    
    /**
     *  Clone this IfStatement.
     *
     * @return  a new instance of the IfStatement.
     */
    public Object clone() {
        
        IfStatement is = (IfStatement) super.clone();
        is.setCondition( (Expression) _condition.clone() );
        is.setSign( _sign );
        return (is);
    }
    
    /**
     *  Get the condition
     *
     * @return  the condition
     */
    public Expression getCondition() {
        return _condition;
    }
    
    /**
     *  Set the condition
     *
     * @param  condition the condition
     */
    public void setCondition(Expression condition) {
        _condition = condition;
    }
    
    /**
     *  Gets the sign of the IfStatement object
     *
     * @return  The modulo value
     */
    public int getSign() {
        return _sign;
    }
    
    /**
     *  Sets the sign of the IfStatement object
     *
     */
    public void setSign(int sign) {
        _sign = sign;
    }
    
    /**
     *  Give the string representation of the if statement.
     *
     * @return  a string representing the if statement.
     */
    public String toString() {
        String ln = "";
        if( _condition != null ) {
            ln = "IfStatement: " + _condition.toString();
        } else {
            ln = "IfStatement: NULL";
        }
        return ln;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The epression of the If Statement
     */
    private Expression _condition;
    
    /**
     *  The sign of the If Statement:
     *  0 : '=='
     *  1 : '>='
     *        -1 : '<='
     */
    private int _sign;
}
