
package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGParameter

/**
 * This class describes a parameter in ADG.
 *
 * @author Todor Stefanov
 * @version  $Id: ADGParameter.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGParameter implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an ADG Parameter
     *
     */
    public ADGParameter(String name) {
        _name = name;
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ADG parameter
     *
     * @return  a new instance of the ADG parameter.
     */
    public Object clone() {
        try {
            ADGParameter newObj = (ADGParameter) super.clone();
            newObj.setName(_name);
            newObj.setLowerBound( _lowerBound );
            newObj.setUpperBound( _upperBound );
            newObj.setValue( _value );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return(null);
    }
    
    /**
     *  Get the name of the ADG parameter.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of the ADG parameter.
     *
     * @param  name The new name
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the lower bound of the ADG parameter.
     *
     * @return  the lower bound
     */
    public int getLowerBound() {
        return _lowerBound;
    }
    
    /**
     *  Set the lower bound of the ADG parameter.
     *
     * @param  lowerBound The new lower bound
     */
    public void setLowerBound(int lowerBound) {
        _lowerBound = lowerBound;
    }
    
    
    /**
     *  Get the upper bound of the ADG parameter.
     *
     * @return  the upper bound
     */
    public int getUpperBound() {
        return _upperBound;
    }
    
    /**
     *  Set the upper bound of the ADG parameter.
     *
     * @param  upperBound The new upper bound
     */
    public void setUpperBound(int upperBound) {
        _upperBound = upperBound;
    }
    
    /**
     *  Get the value of the ADG parameter.
     *
     * @return  the value
     */
    public int getValue() {
        return _value;
    }
    
    /**
     *  Set the value of the ADG parameter.
     *
     * @param  value The new value
     */
    public void setValue(int value) {
        _value = value;
    }
    
    /**
     *  Return a description of the ADG parameter.
     *
     * @return  a description of the ADG parameter.
     */
    public String toString() {
        return "ADGParameter: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  the name of the ADG parameter.
     */
    private String _name = null;
    
    /**
     *  the lower bound of the ADG parameter.
     */
    private int _lowerBound = 0;
    
    /**
     *  the upper bound of the ADG parameter.
     */
    private int _upperBound = 0;
    
    /**
     *  the default value of the ADG parameter.
     */
    private int _value = 0;
}
