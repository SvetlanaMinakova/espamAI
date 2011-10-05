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

import espam.datamodel.graph.adg.ADGVariable;

import espam.visitor.ADGraphVisitor;

//////////////////////////////////////////////////////////////////////////
//// ADGControlVariable

/**
 * This class describes a control variable in ADG. 
 * The control variable is in the form, e.g., ctrl_x[2*i+1][j] = i, where, 
 * the indexing functions '2*i+3' and 'j' are captured in the indexList field of 
 * ADGVariable, and iterator 'i' -> in the iterator field of ADGCtrlVariable.
 *
 * @author Todor Stefanov
 * @version  $Id: ADGCtrlVariable.java,v 1.1 2011/10/05 15:03:46 nikolov Exp $
 */

public class ADGCtrlVariable extends ADGVariable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an ADG control variable
     *
     */
    public ADGCtrlVariable(String name) {
    	super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this ADG control variable
     *
     * @return  a new instance of the ADG control variable.
     */
    public Object clone() {
        ADGCtrlVariable newObj = (ADGCtrlVariable) super.clone();
        newObj.setIterator( _iterator );
        return (newObj);
    }

    /**
     *  Get the iterator of the ADG control variable.
     *
     * @return  the data type
     */
    public String getIterator() {
        return _iterator;
    }

    /**
     *  Set the iterator of the ADG control variable.
     *
     * @param  dataType The new data type
     */
    public void setIterator(String iterator) {
        _iterator = iterator;
    }


    /**
     *  Return a description of the ADG variable.
     *
     * @return  a description of the ADG variable.
     */
    public String toString() {
        return "ADGCtrlVariable: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The data type of the variable.
     */
    private String _iterator = null;

}
