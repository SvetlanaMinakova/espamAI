
package espam.datamodel.platform.peripherals;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Page;

import espam.visitor.PlatformVisitor;

import java.util.Vector;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Controller

/**
 * This class is the basic peripheral component in a platform.
 *
 * @author Wei Zhong
 * @version  $Id: Peripheral.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class Peripheral extends Resource {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Peripheral with a name.
     *
     */
    public Peripheral(String name) {
        super(name);
        _pageList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Peripheral
     *
     * @return  a new instance of the Peripheral.
     */
    public Object clone() {
        Peripheral newObj = (Peripheral) super.clone();
        newObj.setPageList( (Vector) _pageList.clone() );
        return( newObj );
    }
    
    /**
     *  Get the page list of this Peripheral.
     *
     * @return  the page list of Peripheral
     */
    public Vector getPageList() {
        return _pageList;
    }
    
    /**
     *  Set the page list of this Peripheral.
     *
     * @param  pageList The new list.
     */
    public void setPageList(Vector pageList) {
        _pageList = pageList;
    }
    
    /**
     *  Get the base address of this Peripheral.
     *
     * @return  the base address
     */
    public int getBaseAddress() {
        int Addr = 0;
        
        Iterator i = _pageList.iterator();
        Addr = ((Page) i.next()).getBaseAddress();
        while( i.hasNext() ) {
            int baseAddr  = ((Page) i.next()).getBaseAddress();
            if( baseAddr < Addr ) {
                Addr = baseAddr;
            }
        }
        
        return Addr;
    }
    
    /**
     *  Get the size (in Bytes) of the space occupied by the peripheral.
     *
     * @return  the size
     */
    public int getSize() {
        return _size;
    }
    
    /**
     *  Set the size (in Bytes) of the space occupied by the peripheral.
     *
     * @param  size The size.
     */
    public void setSize(int size) {
        _size = size;
    }
    
    
    /**
     *  Return a description of the Peripheral.
     *
     * @return  a description of the Peripheral.
     */
    public String toString() {
        return "Peripheral: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List containing memory map pages.
     */
    private Vector _pageList = null;
    
    /**
     *  the size (in Bytes) of the space occupied by the peripheral in a processor's memory map  
     */
    private int _size = 0;
    
}
