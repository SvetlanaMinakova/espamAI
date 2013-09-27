
package espam.datamodel.parsetree.statement;

import espam.datamodel.graph.adg.ADGInPort;

//////////////////////////////////////////////////////////////////////////
//// MemoryStatement

/**
 *  This class represents a Memory Statement.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: MemoryStatement.java,v 1.3 2002/10/08 14:23:13 kienhuis
 *      Exp $
 */

public class MemoryStatement extends Statement {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Empty Memory Statement Constructor.
     */
    public MemoryStatement(String str) {
        super(str);
        _port = new ADGInPort("");  
    }
    
    /**
     *  Clone this MemoryStatement
     *
     * @return  a new instance of the MemoryStatement.
     */
    public Object clone() {
        
        MemoryStatement ms = (MemoryStatement) super.clone();
        ms.setPort( (ADGInPort) _port.clone() );
        return (ms);
    }
    
    /**
     *  Gets the port attribute of the MemoryStatement object
     *
     * @return  The port value
     */
    public ADGInPort getPort() {
        return _port;
    }
    
    /**
     *  Sets the port attribute of the MemoryStatement object
     *
     */
    public void setPort(ADGInPort port) {
        _port = port;
    }
    
    /**
     *  Give the string representation of the if statement.
     *
     * @return  a string representing the if statement.
     */
    public String toString() {
        String ln = " MemoryStatement: ";
        return ln;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     */
    private ADGInPort _port = null;
}
