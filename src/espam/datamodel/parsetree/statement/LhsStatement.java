
package espam.datamodel.parsetree.statement;

import espam.visitor.StatementVisitor;

/**
 *  This class represents a place holder for the Left-hand side (LHS)
 *  variables of a function call. This Object is used to structure the
 *  function call such that is can be processed easier by Visitors.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: LhsStatement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 * @see  AssignStatement
 */

//////////////////////////////////////////////////////////////////////////
//// LhsStatement

public class LhsStatement extends Statement {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Empty Constructor.
     */
    public LhsStatement() {
        super("LhsStatement");
    }
    
    /**
     *  Accept a StatementVisitor
     *
     * @param  x A Visitor Object.
     */
    public void accept(StatementVisitor x) {
        x.visitStatement(this);
    }
    
    /**
     *  Clone this LhsStatement.
     *
     * @return  a new instance of the LhsStatement.
     */
    public Object clone() {
        
        LhsStatement lhs = (LhsStatement) super.clone();
        return (lhs);
    }
    
    
    /**
     *  Give the string representation of the lhs statement.
     *
     * @return  a string representing the lhs statement.
     */
    public String toString() {
        String ln = "LhsStatement";
        return ln;
    }
}
