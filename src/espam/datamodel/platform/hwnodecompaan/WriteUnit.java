
package espam.datamodel.platform.hwnodecompaan;

import java.util.Vector;

import espam.datamodel.platform.Resource;

//////////////////////////////////////////////////////////////////////////
//// WriteUnit

/**
 * This class is the write unit of a compaan generated node.
 *
 * @author Hristo Nikolov
 * @version  $Id: WriteUnit.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class WriteUnit extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a WriteUnit and empty control type. 
     */
    public WriteUnit(String name) {
        super("WriteUnit");
        _ctrlType = "";
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this WriteUnit
     *
     * @return  a new instance of the WriteUnit.
     */
    public Object clone() {
        WriteUnit newObj = (WriteUnit) super.clone();
        newObj.setCtrlType( _ctrlType );
        return( newObj );
    }
    
    /**
     *  Get the control type of a write unit.
     *
     * @return  the control type
     */
    public String getCtrlType() {
        return _ctrlType;
    }
    
    /**
     *  Set the control type of a write unit.
     *
     * @param  ctrlType The new control type
     */
    public void setCtrlType( String ctrlType) {
        _ctrlType = ctrlType;
    }
    
    /**
     *  Return a description of a write unit.
     *
     * @return  a description of write unit.
     */
    public String toString() {
        return "Write Unit with " + getCtrlType();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The type of the control of a write unit
     */
    private String _ctrlType = "";
}
