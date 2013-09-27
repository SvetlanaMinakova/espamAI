
package espam.datamodel.pn.cdpn;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.pn.Process;

import espam.visitor.CDPNVisitor;
//////////////////////////////////////////////////////////////////////////
//// CDProcess

/**
 * This class describes a Process in CompaanDyn Process Network.
 * The CDProcess is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.2 on page 50 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDProcess.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class CDProcess extends Process {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CDProcess with a name.
     *
     */
    public CDProcess(String name) {
        super(name);
        _adgNodeList = new Vector();
        _schedule = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CDPNVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this CDProcess
     *
     * @return  a new instance of the CDProcess.
     */
    public Object clone() {
        CDProcess newObj = (CDProcess) super.clone();
        newObj.setAdgNodeList( (Vector) _adgNodeList.clone() );
        newObj.setSchedule( (Vector) _schedule.clone() );
        return (newObj);
    }
    
    /**
     *  Get the list of ADG Nodes of this CDProcess.
     *
     * @return  the list
     */
    public Vector getAdgNodeList() {
        return _adgNodeList;
    }
    
    /**
     *  Set the list of ADG Nodes of this CDProcess.
     *
     * @param  adgNodeList The new list
     */
    public void setAdgNodeList(Vector adgNodeList) {
        _adgNodeList = adgNodeList;
    }
    
    /**
     *  Get the schedule of this CDProcess.
     *
     * @return  the schedule
     */
    public Vector getSchedule() {
        return _schedule;
    }
    
    /**
     *  Set the schedule of this CDProcess.
     *
     * @param  schedule The new schedule
     */
    public void setSchedule(Vector schedule) {
        _schedule = schedule;
    }
    
    /**
     *  Return a description of the CDProcess.
     *
     * @return  a description of the CDProcess.
     */
    public String toString() {
        return "CDProcess: " + getName();
    }
    
    /**
     *  Get the input gates of this CDProcess.
     *
     * @return  the input gates
     */
    public Vector getInGates() {
        Vector v = new Vector();
        
        Iterator i = getGateList().iterator();
        while ( i.hasNext() ) {
            CDGate gate = (CDGate) i.next();
            if (gate instanceof CDInGate) {
                v.add( (CDInGate) gate );
            }
        }
        return v;
    }
    
    /**
     *  Get the output gates of this CDProcess.
     *
     * @return  the output gates
     */
    public Vector getOutGates() {
        Vector v = new Vector();
        
        Iterator i = getGateList().iterator();
        while ( i.hasNext() ) {
            CDGate gate = (CDGate) i.next();
            if (gate instanceof CDOutGate) {
                v.add( (CDOutGate) gate );
            }
        }
        return v;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * List of ADG nodes associated with the process. See set "NP"
     * on page 50 in [1] - Definition 2.4.2
     *
     */
    private Vector _adgNodeList = null;
    
    /**
     * The schedule of the process. See "ST"
     * on page 51 in [1] - Definition 2.4.2
     *
     */
    private Vector _schedule = null;
}
