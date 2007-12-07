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

package espam.datamodel.platform.hwnodecompaan;

import java.util.Vector;

import espam.datamodel.platform.Resource;

//////////////////////////////////////////////////////////////////////////
//// ExecuteUnit

/**
 * This class is the execute unit of a compaan generated node.
 *
 * @author Hristo Nikolov
 * @version  $Id: ExecuteUnit.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class ExecuteUnit extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ExecuteUnit and empty control type.
     */
    public ExecuteUnit(String name) {
    	super("ExecuteUnit");
        _ctrlType = "";
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this ExecuteUnit
     *
     * @return  a new instance of the ExecuteUnit.
     */
    public Object clone() {
        ExecuteUnit newObj = (ExecuteUnit) super.clone();
        newObj.setCtrlType( _ctrlType );
        return( newObj );
    }

    /**
     *  Get the control type of a execute unit.
     *
     * @return  the control type
     */
    public String getCtrlType() {
        return _ctrlType;
    }

    /**
     *  Set the control type of a execute unit.
     *
     * @param  ctrlType The new control type
     */
    public void setCtrlType( String ctrlType) {
        _ctrlType = ctrlType;
    }

    /**
     *  Return a description of a execute unit.
     *
     * @return  a description of execute unit.
     */
    public String toString() {
        return "Execute Unit with " + getCtrlType();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The type of the control of a execute unit
     */
    private String _ctrlType = "";
}
