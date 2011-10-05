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

package espam.operations.platformparameterset.memoryparameters;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MFifo;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.Fifo;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGOutPort;

/**
 *  This class sets the size of each FIFO component in a platform.
 *
 *
 * @author  Todor Stefanov
 * @version  $Id: SetFifoSizes.java,v 1.2 2011/10/05 15:03:46 nikolov Exp $
 *
 */
public class SetFifoSizes {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static SetFifoSizes getInstance() {
		return _instance;
	}

	/**
	 *  This ...
	 *
	 * @param  mapping Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void setFifoSizes( Mapping mapping ) {

		Iterator mf = mapping.getFifoList().iterator();
		while( mf.hasNext() ) {

		      MFifo mFifo = (MFifo) mf.next();
		      Fifo fifo = mFifo.getFifo();
                      CDChannel ch = mFifo.getChannel();
		      int fifoSize = ((ADGEdge) ch.getAdgEdgeList().get(0)).getSize();

		      if (fifoSize < 1) {
		          fifo.setSize( 1 );
		      } else {
                          fifo.setSize( fifoSize );
		      }


 		}
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static SetFifoSizes _instance = new SetFifoSizes();

}


