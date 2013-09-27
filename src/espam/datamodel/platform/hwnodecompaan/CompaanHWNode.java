
package espam.datamodel.platform.hwnodecompaan;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// CompaanHWNode

/**
 * This class is the basic Node of compaan generated PN.
 *
 * @author Hristo Nikolov
 * @version  $Id: CompaanHWNode.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class CompaanHWNode extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CompaanHWNode with a name, 
     *  empty resource list and empty link list 
     */
    public CompaanHWNode(String name) {
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
     *  Clone this CompaanHWnode
     *
     * @return  a new instance of the CompaanHWnode.
     */
    public Object clone() {
        CompaanHWNode newObj = (CompaanHWNode) super.clone();
        newObj.setResourceList( (Vector) _resourceList.clone() );
        newObj.setLinkList( (Vector) _linkList.clone() );
        return( newObj );
    }
    
    /**
     *  Get the resource list of a CompaanHWnode.
     *
     * @return  the resource list
     */
    public Vector getResourceList() {
        return _resourceList;
    }
    
    /**
     *  Set the resource list of a CompaanHWnode.
     *
     * @param  resourceList The new list 
     */
    public void setResourceList( Vector resourceList) {
        _resourceList = resourceList;
    }
    
    /**
     *  Get the link list of a CompaanHWnode
     *
     * @return  the link list
     */
    public Vector getLinkList() {
        return _linkList;
    }
    
    /**
     *  Set the link list of a CompaanHWnode
     *
     * @param  linkList The new list
     */
    public void setLinkList(Vector linkList) {
        _linkList = linkList;
    }
    
    /**
     *  Return a description of the CompaanHWnode.
     *
     * @return  a description of the CompaanHWnode.
     */
    public String toString() {
        return "Compaan Hardware Node: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List of the resources of a CompaanHWnode.
     */
    private Vector _resourceList = null;
    
    /**
     *  List of the links of a CompaanHWnode.
     */
    private Vector _linkList = null;
}
