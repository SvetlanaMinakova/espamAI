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

package espam.datamodel.mapping;

import espam.datamodel.graph.adg.ADGNode;

import java.util.Vector;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// MProcess

/**
 * This class contains mapping information related to a process.
 *
 * @author Todor Stefanov
 * @version  $Id: MProcess.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class MProcess implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a MProcess with a name
     *
     */
    public MProcess(String name) {
        _name = name;
	_node = new ADGNode("");
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception If an error occurs.
     */
    //public void accept(PlatformVisitor x) {
    //      x.visitComponent(this);
    //}

    /**
     *  Clone this MProcess
     *
     * @return  a new instance of the MProcess.
     */
    public Object clone() {
        try {
            MProcess newObj = (MProcess) super.clone();
	    newObj.setName(_name);
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }


    /**
     *  Get the name of this MProcess.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this MProcess.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the node of this MProcess.
     *
     * @return  the node
     */
    public ADGNode getNode() {
        return _node;
    }

    /**
     *  Set the node of this MProcess.
     *
     * @param  node The new node
     */
    public void setNode(ADGNode node) {
        _node = node;
    }


    /**
     *  Return a description of the MProcess.
     *
     * @return  a description of the MProcess.
     */
    public String toString() {
        return "MProcess: " + _name;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of a MProcess.
     */
    private String _name = null;

    /**
     *  Node associated with MProcess.
     */
    private ADGNode _node = null;


}
