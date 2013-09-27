
package espam.datamodel.pn;

import java.util.Vector;

import espam.visitor.PNVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Channel

/**
 * This class is the basic channel in a process network.
 * The channel has a name and a list of gates connected by this channel.
 *
 * @author Todor Stefanov
 * @version  $Id: Channel.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class Channel implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Channel with a name and an empty
     *  gateList.
     */
    public Channel(String name) {
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
     *  Clone this Channel
     *
     * @return  a new instance of the Channel.
     */
    public Object clone() {
        try {
            Channel newObj = (Channel) super.clone();
            newObj.setName( _name );
            newObj.setGateList( (Vector) _gateList.clone() );
            return (newObj);
        }
        catch (CloneNotSupportedException e) {
            System.out.println("Error Clone not Supported");
        }
        return(null);
    }
    
    /**
     *  Get the name of this channel.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this channel.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the list of gates of this channel.
     *
     * @return  the list of gates
     */
    public Vector getGateList() {
        return _gateList;
    }
    
    /**
     *  Set the list of gates of this channel.
     *
     * @param  gateList The new list.
     */
    public void setGateList(Vector gateList) {
        _gateList = gateList;
    }
    
    /**
     *  Return a description of the channel.
     *
     * @return  a description of the channel.
     */
    public String toString() {
        return "Channel: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the Channel.
     */
    private String _name = null;
    
    /**
     *  List of the gates of the channel.
     */
    private Vector _gateList = null;
}
