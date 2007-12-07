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

package espam.datamodel.platform;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.PlatformVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Resource

/**
 * This class is the basic component of a model specifying a platform.
 * The component has a name and contains one list: a list that
 * contains the ports of the component.
 *
 * @author Todor Stefanov
 * @version  $Id: Resource.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class Resource implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Resource with a name and an empty
     *  portList.
     */
    public Resource(String name) {
        _name = name;
        _portList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) { }

    /**
     *  Clone this Resource
     *
     * @return  a new instance of the Resource.
     */
    public Object clone() {
        try {
            Resource newObj = (Resource) super.clone();
	    newObj.setName(_name);
            newObj.setPortList( (Vector) _portList.clone() );
	    newObj.setLevelUpResource( (Resource) _levelUpResource.clone() );
	    //newObj.setLevelUpResource( _levelUpResource );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the name of this resource.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this resource.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the list of ports of this resource.
     *
     * @return  the list of ports
     */
    public Vector getPortList() {
        return _portList;
    }

    /**
     *  Set the list of ports of this resource.
     *
     * @param  portList The new list.
     */
    public void setPortList(Vector portList) {
        _portList = portList;
    }

    /**
     *  Get the hierarchical parent of this resource.
     *
     * @return  the parent of this resource.
     */
    public Resource getLevelUpResource() {
        return _levelUpResource;
    }

    /**
     *  Set the hierarchical parent of this resource.
     *
     * @param  levelUpResource The new parent.
     */
    public void setLevelUpResource(Resource levelUpResource) {
        _levelUpResource = levelUpResource;
    }

    /**
     *  Return a description of the resource.
     *
     * @return  a description of the resource.
     */
    public String toString() {
        return "Resource: " + _name;
    }

    /**
     *  Return a port which has a specific name. Return null if port cannot
     *  be found.
     *
     * @param  name the name of the port to search for.
     * @return  the port with the specific name.
     */
     public Port getPort(String name) {
          Iterator i;
          i = _portList.iterator();
          while( i.hasNext() ) {
              Port port = (Port) i.next();
              if( port.getName().equals(name) ) {
                 return port;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the Rerource.
     */
    private String _name = null;

    /**
     *  List of the ports of a resource.
     */
    private Vector _portList = null;

    /**
     *  The parent resource of this resource in a hierarchical platform
     */
    private Resource _levelUpResource = null;
}
