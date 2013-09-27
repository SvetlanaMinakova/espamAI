
package espam.datamodel.parsetree.statement;

import java.util.Vector;

import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// AssignStatement

/**
 *  This class represents an assign statement, e.g., for control variables 
 *  which can appear in a Weakly Dynamic Nested Loop Program. Such an assign
 *  statement is of the form 'ctrl(i,...,k) = iterator.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: AssignStatement.java,v 1.9 2002/09/30 14:03:02 kienhuis
 *      Exp $
 */

public class SimpleAssignStatement extends Statement {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Construct an assign statement give a specific name.
     *
     * @param  
     */
    public SimpleAssignStatement() {
        super("SimpleAssignStatement");
        _indexListLHS = new Vector();
        _indexListRHS = new Vector();
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
     *  Clone this SimpleAssignStatement
     *
     * @return  a new instance of the AssignStatement.
     */
    public Object clone() {
        
        SimpleAssignStatement sas = (SimpleAssignStatement) super.clone();
        sas.setIndexListLHS( (Vector) _indexListLHS.clone() );
        sas.setIndexListRHS( (Vector) _indexListRHS.clone() );
        sas.setLHSVarName( _lhsVarName );
        sas.setRHSVarName( _rhsVarName );
        sas.setNodeName( _nodeName );
        
        return (sas);
        
    }
    
    
    /**
     *  Get the name of the LHS variable
     *
     * @return  the variable name.
     */
    public String getLHSVarName() {
        return _lhsVarName;
    }
    
    /**
     *  Set the LHS variable name.
     *
     * @param  name The name of the LHS variable.
     */
    public void setLHSVarName(String name) {
        _lhsVarName = name;
    }
    
    public String getRHSVarName() {
        return _rhsVarName;
    }
    
    public void setRHSVarName(String name) {
        _rhsVarName = name;
    }
    
    /**
     *  Get the index list of the control variable.
     *
     * @return  the index list
     */
    public Vector getIndexListLHS() {
        return _indexListLHS;
    }
    
    /**
     *  Set the index list of the control variable.
     *
     * @param  indexListLHS The new index list
     */
    public void setIndexListLHS(Vector indexList) {
        _indexListLHS = indexList;
    }
    
    /**
     *  Get the index list of the RHS variable.
     *
     * @return  the index list
     */
    public Vector getIndexListRHS() {
        return _indexListRHS;
    }
    
    /**
     *  Set the index list of the RHS variable.
     *
     * @param  indexListRHS The new index list
     */
    public void setIndexListRHS(Vector indexList) {
        _indexListRHS = indexList;
    }
    
    public String getNodeName() {
        return _nodeName;
    }
    
    public void setNodeName(String name) {
        _nodeName = name;
    }
    
    /**
     *  Give the string representation of the assign statement.
     *
     * @return  a string representing the assign statement.
     */
    public String toString() {
        String ln = "SimpleAssignStatement: " + _lhsVarName;
        return ln;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The name of the function.
     */
    private String _lhsVarName;
    
    /**
     *  The name of the node.
     */
    private String _rhsVarName;
    
    /**
     *  The list of expressions for the index functions of the LHS variable.
     */
    private Vector _indexListLHS = null;
    
    /**
     *  The list of expressions for the index functions of the RHS variable
     */
    private Vector _indexListRHS = null;
    
    /**
     *  The name of the node.
     */
    private String _nodeName;
    
}
