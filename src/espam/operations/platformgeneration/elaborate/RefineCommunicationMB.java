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

package espam.operations.platformgeneration.elaborate;

import java.io.OutputStream;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.pn.cdpn.CDInGate;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.DLMBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.controllers.FifosController;



/**
 *  This class utilizes the Fast Simplex Links (FSL) presented in MicroBlaze processors:
 *
 *      First 8 input and first 8 output fifos are connected to the FSLs, the others are connected to a fifos controller
 *
 * @author  Hristo Nikolov
 * @version  $Id: RefineCommunicationMB.java,v 1.4 2011/10/31 15:04:47 tzhai Exp $
 *
 */
public class RefineCommunicationMB {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static RefineCommunicationMB getInstance() {
		return _instance;
	}

	/**
	 *  This class reconnect the fifos to a MicroBlaze processor in a way that uses FSLs
	 *
	 * @param  platform Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void refine( Platform platform, Mapping mapping ) {

	    // ---------------------------------------------------------------------------------------------
	    // The empty fifos controllers are added to this vector in order to be removed from the platform
	    // ---------------------------------------------------------------------------------------------
	    _oldResources = new Vector();

	    Iterator r = platform.getResourceList().iterator();
	    while( r.hasNext() ) {

		Resource resource = (Resource) r.next();
		if( resource instanceof MicroBlaze && mapping.getProcessor(resource.getName()).getScheduleType() == 0 ) {

		    // -------------------------------------------------------------------------------------
		    // Get the fifos controller connected to a processor.
		    // Parts of the fifos will be connected to the fsl ports of the MicroBlaze processor
		    // -------------------------------------------------------------------------------------
		    FifosController controller = _getFifosController( platform, resource );

		    // -------------------------------------------------------
		    // Reconnect the fifos to a MicroBlaze FSL processor ports
		    // -------------------------------------------------------
		    if( controller != null ) {

			_setFSLs( controller, resource, mapping );
		    }
		}
	    }

	    // ----------------------------------
	    // Remove the empty fifos controllers
	    // ----------------------------------
	    platform.getResourceList().removeAll( _oldResources );
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/****************************************************************************************************************
	*  Find a fifos controller connected to a processor
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private FifosController _getFifosController( Platform platform, Resource processor ) {

		Port processorPort = null;

		Iterator p = processor.getPortList().iterator();
		while( p.hasNext() ) {

			Port port = (Port) p.next();
			if( port instanceof DLMBPort ) {
			    processorPort = port;
			    break;
			}
		}

		Link link = processorPort.getLink();

		// ----------------------------
		// Finding the fifos controller
		// ----------------------------
		FifosController fifosCtrl = null;

		p = link.getPortList().iterator();
		while( p.hasNext() ) {

			Port port = (Port) p.next();
			if( port.getResource() instanceof FifosController ) {

			    fifosCtrl = (FifosController) port.getResource();
			    break;
			}
		}

		return fifosCtrl;
	}

       /****************************************************************************************************************
	*  First 8 input and first 8 output fifos are connected to the FSLs,
	*  the others are connected to a fifos controller
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _setFSLs( FifosController controller, Resource processor, Mapping mapping) {

		// -------------------------------------------------------------------------------------------------------
		// Find the controller port (processor side), Input fifo ports and output fifo ports of a fifos controller
		// -------------------------------------------------------------------------------------------------------
		Vector inFifoPorts  = new Vector();
		Vector outFifoPorts = new Vector();

		Port controllerPort = new Port("");

		Iterator p = controller.getPortList().iterator();
		while( p.hasNext() ) {
		    Port port = (Port) p.next();

		    if( port instanceof LMBPort ) {

			controllerPort = port;

		    } else if( port instanceof FifoWritePort ) {
				outFifoPorts.add( port );

		    }  else if( port instanceof FifoReadPort ) {

				inFifoPorts.add( port );
		    }
		}

		// ------------------------------------------------------------------
		// Connect the first "nr_mb_fsl" output fifos to the fsl processor output ports
		// ------------------------------------------------------------------
		if( outFifoPorts.size() > 0 ) {

		    int cntFSL = _nr_mb_fsl;

		    Iterator op = outFifoPorts.iterator();
		    while( op.hasNext() ) {

			Port fslPort = (FifoWritePort) op.next();


			//////////////////////////////////////////////////////////////////////
			// check, if the port belongs to a self-edge in the CDPN
			// get the CDchannel of the FIFO
			Port port = fslPort.getConnectedPort();
			assert ( port != null );
			CDChannel self_ch = mapping.getCDChannel((Fifo)port.getResource());
			// get in and out gates
			CDOutGate outgate = self_ch.getFromGate();
			CDInGate ingate = self_ch.getToGate();

			// check if two gates belong to the same CDProcess

			//////////////////////////////////////////////////////////////////////


			fslPort.setResource( processor );
			controller.getPortList().remove( fslPort );
			processor.getPortList().add( fslPort );

			cntFSL--;
			if( cntFSL == 0 ) {
			break;
			}
		    }
		}

		// ----------------------------------------------------------------
		// Connect the first "_nr_mb_fsl" input fifos to the fsl processor input ports
		// ----------------------------------------------------------------
		if( inFifoPorts.size() > 0 ) {

		    int cntFSL = _nr_mb_fsl;

		    Iterator ip = inFifoPorts.iterator();
		    while( ip.hasNext() ) {

			Port fslPort = (FifoReadPort) ip.next();

			fslPort.setResource( processor );
			controller.getPortList().remove( fslPort );
			processor.getPortList().add( fslPort );

			cntFSL--;
			if( cntFSL == 0 ) {
			break;
			}
		    }
		}

		// ----------------------------------------------------------
		// If the controller contains no fifos, remove the controller
		// ----------------------------------------------------------
		if( controller.getPortList().size() == 1 ) { // Only the port connected to the processor

		    Link link = controllerPort.getLink();
		    link.getPortList().remove( controllerPort );

		    _oldResources.add( controller );
		}
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static RefineCommunicationMB _instance = new RefineCommunicationMB();

	// Contains the components (fifos controllers) to be removed from the platform
	private Vector _oldResources = null;
	
	// number of FSL ports attached to MB directly (depending on specification of MicroBlaze provided by Xilinx)
	private int _nr_mb_fsl = 16;
}




