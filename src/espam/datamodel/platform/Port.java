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

//////////////////////////////////////////////////////////////////////////
//// Port

/**
 * This class is the basic port of a resource component.
 *
 *
 *
 * @author Todor Stefanov
 * @version  $Id: Port.java,v 1.2 2010/04/02 12:21:24 nikolov Exp $
 */

public class Port implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Port with a name.
     *
     */
    public Port(String name) {
        _name = name;
	_link = new Link(""); // default link - used in platform elaboration
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception MatParserException If an error occurs.
     */
    //public void accept(Visitor x) throws EspamException { }

    /**
     *  Clone this Port
     *
     * @return  a new instance of the Port.
     */
    public Object clone() {
        try {
            Port newObj = (Port) super.clone();
	    newObj.setName(_name);
	    newObj.setResource( (Resource) _resource.clone() );
	    newObj.setLink( (Link) _link.clone() );
	    newObj.setMemSize(_memSize);
            return( newObj );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     *  Get the name of this port.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this port.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     *  Get the resource of this port.
     *
     * @return  the resource
     */
    public Resource getResource() {
        return _resource;
    }

    /**
     *  Set the resource of this port.
     *
     * @param  reource The new resource
     */
    public void setResource(Resource resource) {
        _resource = resource;
    }

    /**
     *  Get the link of this port.
     *
     * @return  the link
     */
    public Link getLink() {
        return _link;
    }

    /**
     *  Set the link of this port.
     *
     * @param  link The new link
     */
    public void setLink(Link link) {
        _link = link;
    }

    /**
     *  Get the memory size this port can address.
     *
     * @return  the size
     */
    public int getMemSize() {
        return _memSize;
    }

    /**
     *  Set the memory size this port can address.
     *
     * @param  size The new memory size
     */
    public void setMemSize(int size) {
        _memSize = size;
    }

    /**
     *  Return a description of the port.
     *
     * @return  a description of the port.
     */
    public String toString() {
        return "Port: " + _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of the Port.
     */
    private String _name = null;


    /**
     *  The Resource which the Port belongs to.
     */
    private Resource _resource = null;


    /**
     *  The Link which the Port connects to.
     */
    private Link _link = null;

    /**
     *  The size of the memory this port can address.
     */
    private int _memSize = 0;

}
