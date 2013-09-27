
package espam.datamodel.pn;

import java.util.Vector;

import espam.visitor.PNVisitor;

//////////////////////////////////////////////////////////////////////////
//// Gate

/**
 * This class is the basic gate of a process.
 *
 *
 * @author Todor Stefanov
 * @version  $Id: Gate.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class Gate implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Gate with a name.
     *
     */
    public Gate(String name) {
        _name = name;
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(PNVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Gate
     *
     * @return  a new instance of the Gate.
     */
    public Object clone() {
        try {
            Gate newObj = (Gate) super.clone();
            newObj.setName(_name);
            newObj.setProcess( (Process) _process.clone() );
            newObj.setChannel( (Channel) _channel.clone() );
            return (newObj);
        }
        catch (CloneNotSupportedException e) {
            System.out.println("Error Clone not Supported");
        }
        return(null);
    }
    
    /**
     *  Get the name of this gate.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this gate.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the process of this gate.
     *
     * @return  the process
     */
    public Process getProcess() {
        return _process;
    }
    
    /**
     *  Set the proces of this gate.
     *
     * @param  process The new process
     */
    public void setProcess(Process process) {
        _process = process;
    }
    
    /**
     *  Get the channel of this gate.
     *
     * @return  the channel
     */
    public Channel getChannel() {
        return _channel;
    }
    
    /**
     *  Set the channel of this gate.
     *
     * @param  channel The new channel
     */
    public void setChannel(Channel channel) {
        _channel = channel;
    }
    
    /**
     *  Return a description of the gate.
     *
     * @return  a description of the gate.
     */
    public String toString() {
        return "Gate: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the Gate.
     */
    private String _name = null;
    
    /**
     *  The Process which the Gate belongs to.
     */
    private Process _process = null;
    
    /**
     *  The Channel which the Gate connects to.
     */
    private Channel _channel = null;
}
