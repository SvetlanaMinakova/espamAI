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

//////////////////////////////////////////////////////////////////////////
//// Platform

/**
 * This class is a basic platform.
 *
 * @author Todor Stefanov
 * @version  $Id: Platform.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class Platform extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Platform with a name,
     *  empty resource list and empty link list
     */
    public Platform(String name) {
    	super(name);
        _resourceList = new Vector();
        _linkList = new Vector();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
          x.visitComponent(this);
    }

    /**
     *  Clone this Platform
     *
     * @return  a new instance of the Platform.
     */
    public Object clone() {
        Platform newObj = (Platform) super.clone();
        newObj.setResourceList( (Vector) _resourceList.clone() );
        newObj.setLinkList( (Vector) _linkList.clone() );
        return( newObj );
    }

    /**
     *  Get the resource list of a Platform.
     *
     * @return  the resource list
     */
    public Vector getResourceList() {
        return _resourceList;
    }

    /**
     *  Set the resource list of a Platform.
     *
     * @param  resourceList The new list
     */
    public void setResourceList( Vector resourceList) {
        _resourceList = resourceList;
    }

    /**
     *  Get the link list of a Platform
     *
     * @return  the link list
     */
    public Vector getLinkList() {
        return _linkList;
    }

    /**
     *  Set the link list of a Platform
     *
     * @param  linkList The new list
     */
    public void setLinkList(Vector linkList) {
        _linkList = linkList;
    }

    /**
     *  Return a description of the Platform.
     *
     * @return  a description of the Platform.
     */
    public String toString() {
        return "Platform: " + getName();
    }

    /**
     *  Return a resource which has a specific name. Return null if
     *  resource cannot be found.
     *
     * @param  name the name of the resource to search for.
     * @return  the resource with the specific name.
     */
     public Resource getResource(String name) {
          Iterator i;
          i = _resourceList.iterator();
          while (i.hasNext()) {
              Resource resource = (Resource) i.next();
              if (resource.getName().equals(name)) {
                 return resource;
              }
          }
          return null;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  List of the resources of a Platform.
     */
    private Vector _resourceList = null;

    /**
     *  List of the links of a Platform.
     */
    private Vector _linkList = null;
}
