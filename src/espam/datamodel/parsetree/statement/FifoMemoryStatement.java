
package espam.datamodel.parsetree.statement;

import java.util.Vector;

import espam.datamodel.graph.adg.ADGVariable;
import espam.visitor.StatementVisitor;

//////////////////////////////////////////////////////////////////////////
//// FifoMemoryStatement

/**
 *  This class represents a Fifo Memory Statement.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: FifoMemoryStatement.java,v 1.1 2007/12/07 22:09:12 stefanov Exp $
 */

public class FifoMemoryStatement extends MemoryStatement {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    public FifoMemoryStatement() {
        super("FifoMemoryStatement");
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
     *  Clone this FifoMemoryStatement
     *
     * @return  a new instance of the FifoMemoryStatement.
     */
    public Object clone() {
        
        FifoMemoryStatement fms = (FifoMemoryStatement) super.clone();
        fms.setProcessName( _processName );
        fms.setGateName( _gateName );
        fms.setNodeName( _nodeName );
        fms.setArgumentList( (Vector<ADGVariable>) _argumentList.clone() );
        return (fms);
    }
    
    
    /**
     *  Get the argument name
     *
     * @return  the name.
     */
    public Vector<ADGVariable> getArgumentList() {
        return _argumentList;
    }
    
    /**
     *  Set the argument name
     *
     */
    public void setArgumentList(Vector<ADGVariable> argumentList) {
        _argumentList = argumentList;
    }
    
    /**
     *  Get the process name
     *
     * @return  the name.
     */
    public String getProcessName() {
        return _processName;
    }
    
    /**
     *  Set the process name
     *
     */
    public void setProcessName(String processName) {
        _processName = processName;
    }
    
    /**
     *  Get the gate name
     *
     * @return  the name.
     */
    public String getGateName() {
        return _gateName;
    }
    
    /**
     *  Set the gate name
     *
     */
    public void setGateName(String gateName) {
        _gateName = gateName;
    }
    
    /**
     *  Get the node name
     *
     * @return  the name.
     */
    public String getNodeName() {
        return _nodeName;
    }
    
    /**
     *  Set the node name
     *
     */
    public void setNodeName(String nodeName) {
        _nodeName = nodeName;
    }
    
    /**
     *  Give the string representation of the FifoMemoryStatement.
     *
     * @return  a string representing the FifoMemoryStatement.
     */
    public String toString() {
        String ln = "FIFO: <"
            + _processName
            + _gateName + ", "
            + _argumentList
            + _nodeName
            + ">";
        return ln;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The name of the process.
     */
    private String _processName;
    
    /**
     *  The name of the gate.
     */
    private String _gateName;
    /**
     *  The name of the node.
     */
    private String _nodeName;
    
    /**
     *  The argument list.
     */
    private Vector<ADGVariable> _argumentList = null;
}
