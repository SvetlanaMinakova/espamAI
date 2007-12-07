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
//// ADGFunction

/**
 * This class describes a function in an ADG.
 * The ADGFunction is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See function Fn in Definition 2.2.2 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: ADGFunction.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class ADGFunction implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an ADG Function
     *
     */
    public ADGFunction(String name) {
    	_name = name;
	_inArgumentList = new Vector();
	_outArgumentList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(ADGraphVisitor x) {
         x.visitComponent(this);
    }

    /**
     *  Clone this ADG function
     *
     * @return  a new instance of the ADG function.
     */
    public Object clone() {
        try {
            ADGFunction newObj = (ADGFunction) super.clone();
	    newObj.setName(_name);
            newObj.setInArgumentList( (Vector) _inArgumentList.clone() );
            newObj.setOutArgumentList( (Vector) _outArgumentList.clone() );
            return( newObj );
        }
	catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the name of the ADG function.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of the ADG function.
     *
     * @param  name The new name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the input arguments list of the ADG function.
     *
     * @return  the input arguments list
     */
    public Vector getInArgumentList() {
        return _inArgumentList;
    }

    /**
     *  Set the input argument list of the ADG function.
     *
     * @param  inArgumentList The new input arguments list
     */
    public void setInArgumentList(Vector inArgumentList) {
        _inArgumentList = inArgumentList;
    }

    /**
     *  Get the output arguments list of the ADG function.
     *
     * @return  the output arguments list
     */
    public Vector getOutArgumentList() {
        return _outArgumentList;
    }

    /**
     *  Set the output argument list of the ADG function.
     *
     * @param  outArgumentList The new output arguments list
     */
    public void setOutArgumentList(Vector outArgumentList) {
        _outArgumentList = outArgumentList;
    }

    /**
     *  Return a description of the ADG function.
     *
     * @return  a description of the ADG function.
     */
    public String toString() {
        return "ADGFunction: " + _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  the name of the ADG function.
     */
    private String _name = null;

    /**
     *  the list of input arguments of the ADG function.
     */
    private Vector _inArgumentList = null;

    /**
     *  the list of output arguments of the ADG function.
     */
    private Vector _outArgumentList = null;
}
