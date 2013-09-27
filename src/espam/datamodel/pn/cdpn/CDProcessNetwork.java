
package espam.datamodel.pn.cdpn;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.pn.ProcessNetwork;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;

import espam.datamodel.pn.cdpn.CDProcess;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// CDProcessNetwork

/**
 * This class describes a CompaanDyn Process Network.
 * The CDProcessNetwork is defined in [1] "Converting Weakly Dynamic
 * Programs to Equivalent Process Network Specifications", Ph.D. thesis by
 * Todor Stefanov, Leiden University 2004, ISBN 90-9018629-8.
 *
 * See Definition 2.4.1 on page 50 in [1].
 *
 * @author Todor Stefanov
 * @version  $Id: CDProcessNetwork.java,v 1.1 2007/12/07 22:09:08 stefanov Exp $
 */

public class CDProcessNetwork extends ProcessNetwork {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create an CDProcessNetwork with a name
     *
     */
    public CDProcessNetwork(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CDPNVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this CDProcessNetwork
     *
     * @return  a new instance of the CDProcessNetwork.
     */
    public Object clone() {
        CDProcessNetwork newObj = (CDProcessNetwork) super.clone();
        newObj.setAdg( (ADGraph) _adg.clone() );
        return (newObj);
    }
    
    /**
     *  Get the ADG corresponding to this CDProcessNetwork.
     *
     * @return  the ADG
     */
    public ADGraph getAdg() {
        return _adg;
    }
    
    /**
     *  Set the ADG corresponding to this CDProcessNetwork.
     *
     * @param  adg The new ADG
     */
    public void setAdg(ADGraph adg) {
        _adg = adg;
    }
    
    /**
     *  Return a process which has a specific node. Return null if
     *  process cannot be found.
     *
     * @param  node the node in the process to search for.
     * @return  the process with the specific node.
     */
    public CDProcess getProcess( ADGNode node ) {
        Iterator i,j;
        String nodeName = node.getName();
        
        i = getProcessList().iterator();
        while( i.hasNext() ) {
            CDProcess process = (CDProcess) i.next();
            
            j = process.getAdgNodeList().iterator();
            while( j.hasNext() ) {
                if( ((ADGNode) j.next()).getName().equals(nodeName) ) {
                    return process;
                }
            }
        }
        return null;
    }
    
    /**
     *  Return a description of the CDProcessNetwork.
     *
     * @return  a description of the CDProcessNetwork.
     */
    public String toString() {
        return "CDProcessNetwork: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * Approximated Dependence Graph which is used to generate this
     * CompaanDyn Process Network.
     */
    private ADGraph _adg = null;
}
