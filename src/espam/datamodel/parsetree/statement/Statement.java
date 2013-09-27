
package espam.datamodel.parsetree.statement;

import java.util.Iterator;

import espam.datamodel.parsetree.ParserNodeImp;

//////////////////////////////////////////////////////////////////////////
//// StatementImp

/**
 * This class implements the Statement interface and provides the basic
 * functionally used by all possible statements as they appear in a Nested Loop
 * Program.
 * 
 * @author Todor Stefanov, Hristo Nikolov
 * @version $Id: Statement.java,v 1.1 2007/12/07 22:09:13 stefanov Exp $
 */

public abstract class Statement extends ParserNodeImp {
    
    ///////////////////////////////////////////////////////////////////
    //// public methods ////
    
    /**
     * Constructor to create a Statement node with a name and an empty ArrayList
     */
    public Statement(String name) {
        _type = name;
    }
    
    /**
     *  Clone this Statement
     *
     * @return  a new instance of the Statement.
     */
    public Object clone() {
        
        Statement s = (Statement) super.clone();
        s.setType( _type );
        return (s);
    }
    
    /**
     * Determine whether this Statement and another Object are equal. The
     * default behavior is to say that both are not equal.
     *
     * @param x
     *            Object with which to compare.
     * @return False indicating that both are not equal.
     */
    public boolean equals(Object x) {
        return false;
    }
    
    /**
     * Get the name of the Statement.
     *
     * @return The name given to the statement.
     */
    public String getType() {
        return _type;
    }
    
    /**
     * Set the name of the statement.
     *
     * @param name
     *            the name that is given to the statement.
     */
    public void setType(String type) {
        _type = type;
    }
    
    ///////////////////////////////////////////////////////////////////
    //// private variables ////
    
    /** The type represeting the statement */
    private String _type = null;
}
