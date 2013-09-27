
package espam.datamodel.platform;

import java.util.Vector;

import espam.visitor.PlatformVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Link

/**
 * This class is the basic link in a model specifying a platform.
 * The link has a name and a list of ports connected by this link.
 *
 * @author Todor Stefanov
 * @version  $Id: Link.java,v 1.1 2007/12/07 22:09:03 stefanov Exp $
 */

public class Link implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Link with a name and an empty
     *  portList.
     */
    public Link(String name) {
        _name = name;
        _portList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Link
     *
     * @return  a new instance of the Link.
     */
    public Object clone() {
        try {
            Link newObj = (Link) super.clone();
            newObj.setName(_name);
            newObj.setPortList( (Vector) _portList.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this link.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this link.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the list of ports of this link.
     *
     * @return  the list of ports
     */
    public Vector getPortList() {
        return _portList;
    }
    
    /**
     *  Set the list of ports of this link.
     *
     * @param  portList The new list.
     */
    public void setPortList(Vector portList) {
        _portList = portList;
    }
    
    /**
     *  Return a description of the link.
     *
     * @return  a description of the link.
     */
    public String toString() {
        return "Link: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the Link.
     */
    private String _name = null;
    
    /**
     *  List of the ports of a Link.
     */
    private Vector _portList = null;
}
