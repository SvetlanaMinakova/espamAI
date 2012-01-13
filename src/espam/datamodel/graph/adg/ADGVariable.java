/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.datamodel.graph.adg;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGVariable

/**
 * This class describes a variable in ADG.
 *
 * @author Todor Stefanov
 * @version  $Id: ADGVariable.java,v 1.2 2012/01/13 15:11:25 nikolov Exp $
 */

public class ADGVariable implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an ADG variable
     *
     */
    public ADGVariable(String name) {
    	_name = name;
	_indexList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this ADG variable
     *
     * @return  a new instance of the ADG variable.
     */
    public Object clone() {
        try {
            ADGVariable newObj = (ADGVariable) super.clone();
	    newObj.setName(_name);
            newObj.setDataType( _dataType );
            newObj.setIndexList( (Vector) _indexList.clone() );
            return (newObj);
        }
	catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the name of the ADG variable.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of the ADG variable.
     *
     * @param  name The new name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the data type of the ADG variable.
     *
     * @return  the data type
     */
    public String getDataType() {
        return _dataType;
    }

    /**
     *  Set the data type of the ADG variable.
     *
     * @param  dataType The new data type
     */
    public void setDataType(String dataType) {
        _dataType = dataType;
    }

    /**
     *  Get the index list of the ADG variable.
     *
     * @return  the index list
     */
    public Vector getIndexList() {
        return _indexList;
    }

    /**
     *  Set the index list of the ADG variable.
     *
     * @param  indexList The new index list
     */
    public void setIndexList(Vector indexList) {
        _indexList = indexList;
    }

    /**
     *  Get the propagation type of the ADG variable (function argument).
     *  
     * @return  _pass: the way a function argument is propagated
     */
    public String getPassType() {
        return _pass;
    }

    /**
     *  Set the propagation type of the ADG variable (function argument).
     *
     * @param  pass the way a function argument is propagated
     */
    public void setPassType(String pass) {
        _pass = pass;
    }

    /**
     *  Return a description of the ADG variable.
     *
     * @return  a description of the ADG variable.
     */
    public String toString() {
        return "ADGVariable: " + _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  the name of the ADG variable.
     */
    private String _name = null;

    /**
     *  The data type of the variable.
     */
    private String _dataType = null;

    /**
     *  List of linear expressions. The number of expressions is
     *  equal to the dimension of the variable. Every expression
     *  represents the indexing of the variable in the coresponding 
     *  dimension. Example: assume variable X(2*j+i,i+3). This variable
     *  is 2 dimensional. So, "_indexList" consists of 2 linear 
     *  expressions: "2*j+i" and "i+3"
     */
    private Vector _indexList = null;

    /**
     * Captures how a funtion argument is propagates: passed by "value", "reference"
     * or it is a left-hand-side "return_value".
     */
    private String _pass = "";
}
