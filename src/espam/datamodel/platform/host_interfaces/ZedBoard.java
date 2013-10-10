package espam.datamodel.platform.host_interfaces;

import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// ZedBoard

/**
 * This class describes a ZedBoard component.
 *
 * @author Mohamed Bamakhrama
 * @version  $Id: ZedBoard.java,v 1.2 2012/02/27 11:22:50 nikolov Exp $
 */

public class ZedBoard extends Resource {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ZedBoard component with a name.
     *
     */
    public ZedBoard(String name) {
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
     *  Clone this ZedBoard
     *
     * @return  a new instance of the ZedBoard.
     */
    public Object clone() {
            ZedBoard newObj = (ZedBoard) super.clone();
            newObj.setCommInterface(_commInterface);
            return( newObj );
    }

    /**
     *  Get the communication interface.
     *
     * @return  the communication interface
     */
    public String getCommInterface() {
        return _commInterface;
    }

    /**
     *  Set the communication interface.
     *
     * @param  commInterface The new communication interface.
     */
    public void setCommInterface(String commInterface) {
        _commInterface = commInterface;
    }

    
    /**
     *  Return a description of the ZedBoard.
     *
     * @return  a description of the ZedBoard.
     */
    public String toString() {
        return "ZedBoard host interface component: " + getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _commInterface = "";
}
