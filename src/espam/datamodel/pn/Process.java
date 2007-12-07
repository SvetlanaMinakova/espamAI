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

package espam.datamodel.pn;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.PNVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Process

/**
 * This class is the basic process of a generic process network.
 * The process has a name and contains one list: a list that
 * contains the gates of the process.
 *
 * @author Todor Stefanov
 * @version  $Id: Process.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class Process implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Process with a name and an empty
     *  gateList.
     */
    public Process(String name) {
        _name = name;
        _gateList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PNVisitor x) {
          x.visitComponent(this);
    }

    /**
     *  Clone this Process
     *
     * @return  a new instance of the Process.
     */
    public Object clone() {
        try {
            Process newObj = (Process) super.clone();
	    newObj.setName( _name );
            newObj.setGateList( (Vector) _gateList.clone() );
	    newObj.setLevelUpProcess( (Process) _levelUpProcess.clone() );
            return (newObj);
        }
        catch (CloneNotSupportedException e) {
            System.out.println("Error Clone not Supported");
        }
        return(null);
    }

    /**
     *  Get the name of this process.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this process.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the list of gates of this process.
     *
     * @return  the list of gates
     */
    public Vector getGateList() {
        return _gateList;
    }

    /**
     *  Set the list of gates of this process.
     *
     * @param  gateList The new list.
     */
    public void setGateList(Vector gateList) {
        _gateList = gateList;
    }

    /**
     *  Get the hierarchical parent of this process.
     *
     * @return  the parent of this process.
     */
    public Process getLevelUpProcess() {
        return _levelUpProcess;
    }

    /**
     *  Set the hierarchical parent of this process.
     *
     * @param  levelUpProcess The new parent.
     */
    public void setLevelUpProcess(Process levelUpProcess) {
        _levelUpProcess = levelUpProcess;
    }

    /**
     *  Return a description of the process.
     *
     * @return  a description of the process.
     */
    public String toString() {
        return "Process: " + _name;
    }

    /**
     *  Return a gate which has a specific name. Return null if gate cannot
     *  be found.
     *
     * @param  name the name of the gate to search for.
     * @return  the gate with the specific name.
     */
     public Gate getGate(String name) {
          Iterator i;
          i = _gateList.iterator();
          while (i.hasNext()) {
              Gate gate = (Gate) i.next();
              if (gate.getName().equals(name)) {
                 return gate;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the Process.
     */
    private String _name = null;

    /**
     *  List of the gates of the Process.
     */
    private Vector _gateList = null;

    /**
     *  The parent process of this Process in a hierarchical
     *  process network.
     */
    private Process _levelUpProcess = null;
}
