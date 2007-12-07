/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.datamodel.mapping;

import espam.datamodel.platform.Resource;

import java.util.Vector;
import java.util.Iterator;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.platform.memories.Fifo;


//////////////////////////////////////////////////////////////////////////
//// MProcessor

/**
 * This class contains mapping information that shows which channels are
 * mapped onto a fifo.
 *
 * @author Todor Stefanov
 * @version  $Id: MFifo.java,v 1.1 2007/12/07 22:09:10 stefanov Exp $
 */

public class MFifo implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a MFifo with a name
     */
    public MFifo( String name ) {
        _name = name;
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception If an error occurs.
     */
    //public void accept(PlatformVisitor x) {
    //      x.visitComponent(this);
    //}

    /**
     *  Clone this MProcessor
     *
     * @return  a new instance of the MProcessor.
     */
    public Object clone() {
        try {
            MFifo newObj = (MFifo) super.clone();
	    newObj.setName(_name);
            newObj.setChannel( (CDChannel) _channel.clone() );
            newObj.setFifo( (Fifo) _fifo.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }


    /**
     *  Get the name of this MFifo.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }

    /**
     *  Set the name of this MFifo.
     *
     * @param  name The new name value
     */
    public void setName( String name ) {
        _name = name;
    }

    /**
     *  Get the channel of a MFifo.
     *
     * @return  the channel
     */
    public CDChannel getChannel() {
        return _channel;
    }

    /**
     *  Set the channel of a MFifo.
     *
     * @param  channel The new channel
     */
    public void setChannel( CDChannel channel ) {
        _channel = channel;
    }

    /**
     *  Get the fifo of MFifo.
     *
     * @return  the fifo
     */
    public Fifo getFifo() {
        return _fifo;
    }

    /**
     *  Set the fifo of a MFifo.
     *
     * @param  fifo The new fifo
     */
    public void setFifo( Fifo fifo ) {
        _fifo = fifo;
    }


    /**
     *  Return a description of the MProcessor.
     *
     * @return  a description of the MProcessor.
     */
    public String toString() {
        return "MFifo: " + _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Name of a MFIfo.
     */
    private String _name = null;

    /**
     *  CDPN channel which is mapped on a fifo
     */
    private CDChannel _channel = null;

    /**
     *  Platform fifo a CDPN channel is mapped on
     */
    private Fifo _fifo = null;
}
