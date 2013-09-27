
package espam.datamodel.pn.cdpn;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.pn.Gate;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// CDGate

/**
 * This class describes a gate in a CompaanDyn Process Network
 * The CDGate is defined in [1] "Converting Weakly Dynamic Programs to
 * Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.3 and 2.4.4 on page 39 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDGate.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class CDGate extends Gate {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CDGate with a name.
     *
     */
    public CDGate(String name) {
        super(name);
        _adgPortList = new Vector();
        _keyFuncList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CDPNVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this CDGate
     *
     * @return  a new instance of the CDGate.
     */
    public Object clone() {
        CDGate newObj = (CDGate) super.clone();
        newObj.setAdgPortList( (Vector) _adgPortList.clone() );
        newObj.setKeyFuncList( (Vector) _keyFuncList.clone() );
        return (newObj);
    }
    
    /**
     *  Get the ADG port list of this CDGate.
     *
     * @return  the ADG port list
     */
    public Vector getAdgPortList() {
        return _adgPortList;
    }
    
    /**
     *  Set the ADG port list of this CDGate.
     *
     * @param  adgPortList The new adg port list
     */
    public void setAdgPortList(Vector adgPortList) {
        _adgPortList = adgPortList;
    }
    
    /**
     *  Get the key function list of this CDGate.
     *
     * @return  the key function list
     */
    public Vector getKeyFuncList() {
        return _keyFuncList;
    }
    
    /**
     *  Set the key func list of this CDGate.
     *
     * @param  keyFuncList The new key func list
     */
    public void setKeyFuncList(Vector keyFuncList) {
        _keyFuncList = keyFuncList;
    }
    
    /**
     *  Return a description of the CDGate.
     *
     * @return  a description of the CDGate.
     */
    public String toString() {
        return "CDGate: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * List of ADG ports associated with the gate. See "Iig"
     * and "Oog" on page 51 in [1] - Definition 2.4.3 and 2.4.4
     *
     */
    private Vector _adgPortList = null;
    
    /**
     * List of key generation functions for every ADG port of this gate.
     * See "IKig" and "OKog" on page 51 in [1] - 
     * Definition 2.4.3 and 2.4.4
     *
     */
    private Vector _keyFuncList = null;
}
