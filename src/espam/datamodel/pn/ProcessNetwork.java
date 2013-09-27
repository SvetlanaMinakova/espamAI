
package espam.datamodel.pn;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.PNVisitor;

//////////////////////////////////////////////////////////////////////////
//// Process Network

/**
 * This class is a basic process network.
 *
 * @author Todor Stefanov
 * @version  $Id: ProcessNetwork.java,v 1.1 2007/12/07 22:09:07 stefanov Exp $
 */

public class ProcessNetwork extends Process {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ProcessNetwork with a name,
     *  empty process list and empty channel list.
     */
    public ProcessNetwork(String name) {
        super(name);
        _processList = new Vector();
        _channelList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(PNVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ProcessNetwork
     *
     * @return  a new instance of the ProcessNetwork.
     */
    public Object clone() {
        ProcessNetwork newObj = (ProcessNetwork) super.clone();
        newObj.setProcessList( (Vector) _processList.clone() );
        newObj.setChannelList( (Vector) _channelList.clone() );
        return (newObj);
    }
    
    /**
     *  Get the process list of a ProcessNetwork.
     *
     * @return  the process list
     */
    public Vector getProcessList() {
        return _processList;
    }
    
    /**
     *  Set the process list of a ProcessNetwork.
     *
     * @param  processList The new list
     */
    public void setProcessList( Vector processList ) {
        _processList = processList;
    }
    
    /**
     *  Get the channel list of a ProcessNetwork
     *
     * @return  the channel list
     */
    public Vector getChannelList() {
        return _channelList;
    }
    
    /**
     *  Set the channel list of a ProcessNetwork
     *
     * @param  channelList The new list
     */
    public void setChannelList(Vector channelList) {
        _channelList = channelList;
    }
    
    /**
     *  Return a description of the ProcessNetwork.
     *
     * @return  a description of the ProcessNetwork.
     */
    public String toString() {
        return "ProcessNetwork: " + getName();
    }
    
    /**
     *  Return a process which has a specific name. Return null if
     *  process cannot be found.
     *
     * @param  name the name of the process to search for.
     * @return  the process with the specific name.
     */
    public Process getProcess(String name) {
        Iterator i;
        i = _processList.iterator();
        while (i.hasNext()) {
            Process process = (Process) i.next();
            if (process.getName().equals(name)) {
                return process;
            }
        }
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List of the processes in the ProcessNetwork.
     */
    private Vector _processList = null;
    
    /**
     *  List of the channels in the ProcessNetwork.
     */
    private Vector _channelList = null;
}
