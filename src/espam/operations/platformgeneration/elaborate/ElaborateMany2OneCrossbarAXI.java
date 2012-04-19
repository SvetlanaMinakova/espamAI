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

import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.AXIPort;
import espam.datamodel.platform.ports.DLMBPort;
import espam.datamodel.platform.ports.ILMBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.communication.AXICrossbar;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.CM_AXI;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.CM_CTRL;
import espam.datamodel.platform.controllers.AXI_CM_CTRL;
import espam.datamodel.platform.controllers.AXI2AXI_CTRL;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MFifo;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGPort;

import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.pn.cdpn.CDInGate;

/**
 *  This class elaborates a platform in 'many-to-one' manner with a AXI crossbar communication component:
 *
 *	- The platform specification contains the processing components, a communication network component, and a host interface.
 *	  There are no HW components, links are specified.
 *
 *	- A mapping is specified - several processes can be mapped onto 1 processor.
 *
 *  The elaborated platform contains crossbar, processors, processor memories, controllers, and AXI crossbar.
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class ElaborateMany2OneCrossbarAXI {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static ElaborateMany2OneCrossbarAXI getInstance() {
		return _instance;
	}

       /****************************************************************************************************************
	*  Elaborate a platform with many-to-one mapping with crossbar communication network component
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	public void elaborate( Platform platform, Mapping mapping ) {

		// -----------------------------------------------------------------------------------------------------------
		// All new components of the elaborated platform are first added to this vector.
		// This vector is needed because we can not add elements to the resource list until we use the iterator of it
		// -----------------------------------------------------------------------------------------------------------
		_newResources = new Vector();

		// ----------------------------------------------------------------------------------------------------------
		// BRAMs are attached to the MicroBlazes: one memory component is shared for program and data memory.
		// The crossbar ports are set to be of type FifoReadPort
		// Communication Memories (with controllers) are inserted between each processor and the crossbar
		// ----------------------------------------------------------------------------------------------------------
		_elaborateProcessors( platform, mapping );
		
		// ---------------------------------------------
		// Add all new created resources to the platform
		// ---------------------------------------------
		platform.getResourceList().addAll( _newResources );
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////



	/****************************************************************************************************************
	*  Elaborate a Processor component.
	*	BRAMs are attached to the MicroBlazes: one memory component is shared for program and data memory.
	*	Virtual Buffers are inserted between each processor data bus and the crossbar
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _elaborateProcessors( Platform platform, Mapping mapping ) {

	    Iterator i = platform.getResourceList().iterator();
	    while( i.hasNext() ) {

		Resource resource = (Resource) i.next();

		if( resource instanceof Processor ) {
		
		    // --------------------------------------------------------------------------------------------
		    // When a mapping is specified, each processor has only one port which has no type
		    // The port is removed and a new port is set to be of the correct processor port type
		    // --------------------------------------------------------------------------------------------
                    Port port = (Port) resource.getPortList().get(0);
		    Link link = port.getLink();

		    Port port1 = (Port)link.getPortList().get(0);
		    Port port2 = (Port)link.getPortList().get(1);

		    AXICrossbar crossbar = _getAXICrossbar( platform );

        	    // -------------------------------------------------------------
		    // Break the connection. New connection is made in _addMemory()
		    // -------------------------------------------------------------
		    if( port1.getResource() instanceof Processor ) {
			crossbar.getPortList().remove( port2 );                       
			link.getPortList().remove( port2 );
		    } else {
			crossbar.getPortList().remove( port1 );
			link.getPortList().remove( port1 );
		    }
                    //--------------------------------------------
		    // Remove the original processor port as well
                    //--------------------------------------------
		    resource.getPortList().remove( port );
		    link.getPortList().remove( port );
		    
		    // -----------------------------------------------------------------------------
		    // Make the initial link a data bus link (New Program bus link is created later)
		    // -----------------------------------------------------------------------------
		    link.setName( "DBUS_" + resource.getName() ); // Make this link a data bus link
		    Port processorDPort = new DLMBPort( "DLMB" );
		    processorDPort.setLink( link );
	            processorDPort.setResource( resource );
		    resource.getPortList().add( processorDPort );
                    link.getPortList().add( processorDPort );

         	    // -----------------------------------------------------------------------------------------------------
		    // Each port of the crossbar must be of type FifoReadPort (Normally, not specified in the platform file)
		    // -----------------------------------------------------------------------------------------------------
		    FifoReadPort crossbarPort = new FifoReadPort( "CM_side" );
		    // ----------------------------------------------------
		    // Set the new crossbar port and add it to the crossbar
		    // ----------------------------------------------------
		    crossbarPort.setResource( crossbar );
		    crossbar.getPortList().add( crossbarPort );

                    Link cbLink = new Link("cbLink");
                    cbLink.getPortList().add( crossbarPort );
                    crossbarPort.setLink( cbLink );
                    platform.getLinkList().add( cbLink );

		    // ----------------------------------------------------
		    // Add a communication memory (with controllers)
		    // ----------------------------------------------------
                    CM_AXI cmAxi = new CM_AXI( "CM_"+ resource.getName() );
		    cmAxi.setDataWidth( 32 );
		    cmAxi.setLevelUpResource( platform );
                    _addMemory( platform, processorDPort, crossbarPort, cmAxi );
                    _initVirtualBuffer( cmAxi, resource, mapping );

                    //-----------------------------------------------------------------------------------------------
		    // We need to add an AXI bus to the MicroBlaze processor + AXI2AXI ctrl conneted to the crossbar
                    //-----------------------------------------------------------------------------------------------
		    AXIPort processorAXIPort = new AXIPort( "DAXI" );	            
		    processorAXIPort.setResource( resource );
		    resource.getPortList().add( processorAXIPort );

		    FifoReadPort crossbarSide = new FifoReadPort( "CB_AXI" );
                    crossbarSide.setResource( crossbar );
		    crossbar.getPortList().add( crossbarSide );

                    Link link4 = new Link("MB_AXI");
                    link4.getPortList().add( processorAXIPort );
                    processorAXIPort.setLink( link4 );
                    platform.getLinkList().add( link4 );

		    AXI2AXI_CTRL axi2axi = new AXI2AXI_CTRL("AXI_" + resource.getName() );
		    _addController( platform, processorAXIPort, crossbarSide, axi2axi );

		    // ---------------------------------------------------------
		    // The instruction processor port is created here
		    // ---------------------------------------------------------
	            ILMBPort processorIPort = new ILMBPort( "ILMB" );
		    // --------------------------------------------
		    // Set the Program memory side of the processor
		    // --------------------------------------------
		    processorIPort.setResource( resource );
		    resource.getPortList().add( processorIPort );

                    Link pLink = processorIPort.getLink();
		    pLink.setName( "PBUS_" + resource.getName() );
		    pLink.getPortList().add( processorIPort );
		    platform.getLinkList().add( pLink );

		    // -------------------------------------------------------------------------------------------------
		    // Create a Memory component of the processor (for both program and data) and add it to the platform
		    // -------------------------------------------------------------------------------------------------
		    Memory memory = new Memory( "MEM_" + resource.getName() );
		    memory.setSize( ((Processor) resource).getDataMemSize() + ((Processor) resource).getProgMemSize() );
		    memory.setDataWidth( 32 );
		    memory.setLevelUpResource( platform );
 		    _addMemory( platform, processorDPort, processorIPort, memory );
		}
	    }
	}



        /****************************************************************************************************************
	*  Adds a memory component and 2 memory controllers to a Processor component
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _addMemory( Platform platform, Port procDataPort, Port procProgPort, Memory memory ) {

		Link linkData = procDataPort.getLink();
		Link linkProg = procProgPort.getLink();
		Port   memDataPort;
		Port   memProgPort;

		//if( procDataPort.getResource() instanceof MicroBlaze ) {
		    memDataPort = new LMBPort( "DM" );
		    memProgPort = new LMBPort( "PM" );
		//} else {}

		// -----------------------------------------------------------------------------------
		// Don't set the link because we add a controller between the processor and the memory
		// -----------------------------------------------------------------------------------
		//memPort.setLink( link );
		memDataPort.setResource( memory );
		memProgPort.setResource( memory );

		memory.getPortList().add( memDataPort );
		memory.getPortList().add( memProgPort );

		// -----------------------------------------
		// Add this memory component to the platform
		// -----------------------------------------
		_newResources.add( memory );

		// -----------------------
		// Add memory controllers
		// -----------------------
                if( memory instanceof CM_AXI ) { 
		    // ------------------------------------------------------------------------
		    // Add a Communication Memory Controller connected to the MicroBlaze
		    // ------------------------------------------------------------------------
                    CM_CTRL vbCtrl = new CM_CTRL( "LMB_CTRL_" + memory.getName() );
                    _addController( platform, procDataPort, memDataPort, vbCtrl );
                    //------------------------------------------------------------
                    // Add an AXI controller connected to the communication memory 
                    //------------------------------------------------------------
                    AXI_CM_CTRL axiCtrl = new AXI_CM_CTRL( "AXI_CTRL_" + memory.getName() );
                    _addController( platform, procProgPort, memProgPort, axiCtrl );
                } else if( memory instanceof Memory ) {
		      MemoryController memDataCtrl = new MemoryController("DCTRL_" + memory.getName());
		      _addController( platform, procDataPort, memDataPort, memDataCtrl );
		      MemoryController memProgCtrl = new MemoryController("PCTRL_" + memory.getName());
		      _addController( platform, procProgPort, memProgPort, memProgCtrl );
                }
	}

       /****************************************************************************************************************
	*  Add Fifos to a virtual Buffer Component according to the pn and the specified mapping
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _initVirtualBuffer( CM_AXI virtBuffer, Resource processor, Mapping mapping ) {

		CDProcessNetwork cdpn = mapping.getCDPN();
		
		Iterator i = mapping.getProcessorList().iterator();
		while( i.hasNext() ) {

		    MProcessor mProcessor = (MProcessor) i.next();
		    if( mProcessor.getName().equals(processor.getName()) ) {

		        MProcess process = (MProcess) mProcessor.getProcessList().get(0);
			ADGNode node = process.getNode();
			CDProcess cdPrcs = cdpn.getProcess( node );

			Iterator g = cdPrcs.getOutGates().iterator();
			while( g.hasNext() ) {

			    CDOutGate gate = (CDOutGate) g.next();
			    Fifo fifo = new Fifo( gate.getChannel().getName() );
			    fifo.setDataWidth( 32 ); 
			    fifo.setLevelUpResource( virtBuffer );

			    // No fifo ports are set !!!!!!!!!!!!

			    virtBuffer.getFifoList().add( fifo );
		        
			    // ----------------------------------------------------------------------------------------
		            // Create and add a MFifo to the mapping. MFifo points to a Fifo and CDChannel mapped on it
		            // ----------------------------------------------------------------------------------------
			    CDChannel channel = (CDChannel) gate.getChannel();
		            _addMFifo( fifo, channel, mapping );
			}
		    }
		}
	}

       /****************************************************************************************************************
	*  Insert a resource between 2 ports (Used to add Memory, MultiFifo or read crossbar controllers to a processor)
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _addController( Platform platform, Port processorPort, Port memoryPort, Controller controller ) {

         	// -----------------------------------------
		// The link contains only the processor port
		// -----------------------------------------
		Link link = processorPort.getLink();
		
		controller.setLevelUpResource( platform );

		Port processorSide;
		Port memorySide;

	        if( processorPort instanceof LMBPort ) {
		    processorSide = new LMBPort("IO_1");
		    memorySide = new LMBPort("IO_2");
		} if( processorPort instanceof AXIPort ) {
		    processorSide = new AXIPort("IO_1");
		    memorySide = new AXIPort("IO_2");
		} else {  // Fifo (crossbar read controller) to be added
		    processorSide = new FifoWritePort("IO_1"); // crossbarSide
		    memorySide = new FifoReadPort("IO_2");     // Ip read side
		}

		processorSide.setResource( controller );
		memorySide.setResource( controller );

		// --------------------------------------------------------------------------
		// Connect the processor port with the controller port (use the current link)
		// --------------------------------------------------------------------------
		processorSide.setLink( link );
		link.getPortList().add( processorSide );

		// --------------------------------------------------------------------
		// Connect the controller port with the memory port (create a new link)
		// --------------------------------------------------------------------
		Link cbLink = new Link( "BUS_" + controller.getName() );
		cbLink.getPortList().add( memoryPort );
		cbLink.getPortList().add( memorySide );

		memoryPort.setLink( cbLink );
		memorySide.setLink( cbLink );

		// -----------------------------------
		// Add the new ports to the controller
		// -----------------------------------
		controller.getPortList().add( processorSide );
		controller.getPortList().add( memorySide );

		// ------------------------------------------------------
		// Initialize the controller if it is a memory controller
		// ------------------------------------------------------
		if( controller instanceof MemoryController ) {
  		   Memory memory = (Memory) memoryPort.getResource();
		   Page page = new Page();
		   page.setBaseAddress( 0 );
		   page.setSize( memory.getSize() );
		   controller.getPageList().add( page );
		}

		// Add the controller to the platform
		_newResources.add( controller );

		// Add the new created link to the platform
		platform.getLinkList().add( cbLink );

		//System.out.println( ((Link)processorSide.getLink()).getPortList() );
	}

       /****************************************************************************************************************
	*  Finding the crossbar component of the platform
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private AXICrossbar _getAXICrossbar( Platform platform ) {

		Iterator r = platform.getResourceList().iterator();
		while( r.hasNext() ) {
			Resource resource = (Resource) r.next();
			if( resource instanceof AXICrossbar ) {
			    return (AXICrossbar) resource;
			}
		}
		return null;
	}

       /****************************************************************************************************************
	*  Adds a MFifo to the mapping specification. MFifo points to a Fifo and CDChannel mapped on it
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _addMFifo( Fifo fifo, CDChannel channel, Mapping mapping ) {

		/* Check if the mapping has a corresponding MFifo */
		Vector<MFifo> mfifos = mapping.getFifoList();
		Iterator<MFifo> it = mfifos.iterator();
		boolean found = false;
		while (it.hasNext()) {
			MFifo mfifo = it.next();
			if (mfifo.getName().equals(((ADGEdge)channel.getAdgEdgeList().get(0)).getName())) {
				mfifo.setChannel( channel );
				mfifo.setFifo( fifo );
				found = true;
				break;
			}
		}
		
		if (!found) {
			MFifo mFifo = new MFifo( fifo.getName() );
			mFifo.setChannel( channel );
			mFifo.setFifo( fifo );
			mapping.getFifoList().add( mFifo );
		}
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static ElaborateMany2OneCrossbarAXI _instance = new ElaborateMany2OneCrossbarAXI();

	// Contains the new component (memories, fifos, ...) to be added to the platform
	private Vector _newResources = null;
}


