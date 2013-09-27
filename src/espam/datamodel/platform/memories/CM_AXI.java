
package espam.datamodel.platform.memories;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.PlatformVisitor;

import espam.datamodel.platform.memories.Fifo;

//////////////////////////////////////////////////////////////////////////
//// CM_AXI

/**
 * This class is the Virtual Buffer component of a platform.
 * The component has a size, data width and list of fifos.
 *
 * @author Hristo Nikolov
 * @version  $Id: CM_AXI.java,v 1.1 2012/04/02 16:31:04 nikolov Exp $
 */

public class CM_AXI extends Memory {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a CM_AXI with a name, size=0 (the total amount
     *  of available locations) and empty fifo list
     */
    public CM_AXI(String name) {
        super(name);
        _fifoList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    public void accept(PlatformVisitor x) { 
        x.visitComponent(this);
    }
    
    /**
     *  Clone this CM_AXI
     *
     * @return  a new instance of a CM_AXI.
     */
    public Object clone() {
        CM_AXI newObj = (CM_AXI) super.clone();
        newObj.setFifoList( (Vector) _fifoList.clone() );
        return( newObj );
    }
    
    /**
     *  Get the list of fifos of this MultiMifo.
     *
     * @return  the list of fifos
     */
    public Vector getFifoList() {
        return _fifoList;
    }
    
    /**
     *  Set the list of ports of this CM_AXI.
     *
     * @param  fifoList The new list.
     */
    public void setFifoList(Vector fifoList) {
        _fifoList = fifoList;
    }
    
    /**
     *  Get the memory size of this MultiMifo.
     *  The size is the sum of the fifo sizes of this CM_AXI
     *
     * @return  the CM_AXI size
     */
    public int getSize() {
        int size = 0;
        
        Iterator f = _fifoList.iterator();
        while( f.hasNext() ) {
            
            Fifo fifo = (Fifo) f.next();
            size += fifo.getSize();
        }
        
        return size;
    }
    
    /**
     *  Return a description of a CM_AXI.
     *
     * @return  a description of a CM_AXI.
     */
    public String toString() {
        return "CM_AXI: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List of the fifos of a CM_AXI.
     */
    private Vector _fifoList = null;
}
