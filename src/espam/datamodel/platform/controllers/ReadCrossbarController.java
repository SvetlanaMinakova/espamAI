
package espam.datamodel.platform.controllers;

import espam.visitor.PlatformVisitor;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// ReadCrossbarController

/**
 * This class is a controller component that is used to connect a read port
 * of a CompaanHWNode to a port of a crossbar switch.
 *
 * @author Todor Stefanov
 * @version  $Id: ReadCrossbarController.java,v 1.1 2007/12/07 22:09:04 stefanov Exp $
 */

public class ReadCrossbarController extends Controller {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a ReadCrossbarController with a name.
     *
     */
    public ReadCrossbarController(String name) {
        super(name);
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this ReadCrossbarController
     *
     * @return  a new instance of the ReadCrosbarController.
     */
    public Object clone() {
        ReadCrossbarController newObj = (ReadCrossbarController) super.clone();
        newObj.setFifoAddress( _fifoAddress );
        return( newObj );
    }
    
    /**
     *  Get the fifo address of this ReadCrossbarController.
     *
     * @return  the fifo address of ReadCrossbarController
     */
    public int getFifoAddress() {
        return _fifoAddress;
    }
    
    /**
     *  Set the fifo address of this ReadCrossbarController.
     *
     * @param  fifoAddress The new fifo address.
     */
    public void setFifoAddress(int fifoAddress) {
        _fifoAddress = fifoAddress;
    }
    
    /**
     *  Return a description of the ReadCrossbarController.
     *
     * @return  a description of the ReadCrossbarController.
     */
    public String toString() {
        return "ReadCrossbarController: " + getName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The global address of a fifo which is read by this controller
     *  via a crossbar switch port.
     */
    private int _fifoAddress = 0;
    
}
