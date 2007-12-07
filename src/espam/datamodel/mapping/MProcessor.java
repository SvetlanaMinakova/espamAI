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

import espam.datamodel.platform.Resource;

import java.util.Vector;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// MProcessor

/**
 * This class contains mapping information that shows which processes are
 * mapped onto a processor.
 *
 * @author Todor Stefanov
 * @version  $Id: MProcessor.java,v 1.1 2007/12/07 22:09:11 stefanov Exp $
 */

public class MProcessor implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a MProcessor with a name
     *  and an empty process list
     */
    public MProcessor(String name) {
        _name = name;
        _processList = new Vector();
	_resource = new Resource("");
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception If an error occurs.
     */
    //public void accept(PlatformVisitor x) {
    //      x.visitComponent(this);
    //}

    /**
     *  Clone this MProcessor
     *
     * @return  a new instance of the MProcessor.
     */
    public Object clone() {
        try {
            MProcessor newObj = (MProcessor) super.clone();
	    newObj.setName(_name);
            newObj.setProcessList( (Vector) _processList.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }


    /**
     *  Get the name of this MProcessor.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this MProcessor.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the process list of MProcessor.
     *
     * @return  the process list
     */
    public Vector getProcessList() {
        return _processList;
    }

    /**
     *  Set the process list of MProcessor.
     *
     * @param  processList The new list
     */
    public void setProcessList( Vector processList) {
        _processList = processList;
    }

    /**
     *  Get the resource of MProcessor.
     *
     * @return  the resource
     */
    public Resource getResource() {
        return _resource;
    }

    /**
     *  Set the resource of MProcessor.
     *
     * @param  resource The new resource
     */
    public void setResource( Resource resource) {
        _resource = resource;
    }


    /**
     *  Return a description of the MProcessor.
     *
     * @return  a description of the MProcessor.
     */
    public String toString() {
        return "MProcessor: " + _name;
    }

    /**
     *  Return a process which has a specific name. Return null if
     *  process cannot be found.
     *
     * @param  name the name of the process to search for.
     * @return  the process with the specific name.
     */
     public MProcess getProcess(String name) {
          Iterator i;
          i = _processList.iterator();
          while (i.hasNext()) {
              MProcess process = (MProcess) i.next();
              if (process.getName().equals(name)) {
                 return process;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of a MProcessor.
     */
    private String _name = null;

    /**
     *  List of the processes of MProcessor.
     */
    private Vector _processList = null;

    /**
     *  Resource associated with MProcessor.
     */
    private Resource _resource = null;


}
