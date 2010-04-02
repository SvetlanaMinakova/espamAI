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

import java.lang.Math;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.DLMBPort;
import espam.datamodel.platform.ports.DPLBPort;
import espam.datamodel.platform.ports.ILMBPort;
import espam.datamodel.platform.ports.IPLBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.controllers.MemoryController;

/**
 *  This class refines the processor memories:
 *
 *      The needed amount of memory is spread between several controllers in order to achieve better memory utilization
 *      The smallest piece of memory is 2K block. All the block sizes are power of 2.
 *      So, if we need 37K, it will be spread to 3 memories (with controllers):  37K = 32K + 4K + 2K
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class RefineMemories {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static RefineMemories getInstance() {
		return _instance;
	}

	/**
	 *  This class inserts additional memories and controllers in order to instantiate
	 *  the needed amoount of memory, exmpl: 37K = 32K + 4K + 2K (3 Memory blocks)
	 *
	 * @param  platform Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void refine( Platform platform ) {

		// -----------------------------------------------------------------------------------------------------------
		// Memories and memory controllers are added to this vector
		// This vector is needed because we can not add elements to the resource list until we use the iterator of it
		// -----------------------------------------------------------------------------------------------------------
		_newResources = new Vector();

		// ------------------------------------------------------------------------------------------------------
		// The initial memories and controllers are added to this vector in order to be removed from the platform
		// ------------------------------------------------------------------------------------------------------
		_oldResources = new Vector();

		// -----------------
		// Memory refinement
		// -----------------
		_refineMemories( platform );

		// ---------------------------------------------
		// Add all new created resources to the platform
		// ---------------------------------------------
		platform.getResourceList().addAll( _newResources );
		
		// -------------------------------------------
		// Remove the initial memories and controllers
		// -------------------------------------------
		platform.getResourceList().removeAll( _oldResources );
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/****************************************************************************************************************
	*  Refinement of the memory components in a platform.
        *       inserts additional memories and controllers in order to instantiate
	*       the needed amoount of memory, exmpl: 37K = 32K + 4K + 2K (3 Memory blocks)
	*       the needed amoount of memory, exmpl: 37K = 32K + 8K      (2 Memory blocks)
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
        private void _refineMemories( Platform platform ) {

		Iterator r = platform.getResourceList().iterator();
		while( r.hasNext() ) {

		    Resource resource = (Resource) r.next();
		    if( resource instanceof Processor ) {

                        _processorName = resource.getName();
		        int DM = 0;
			int PM = 1;
		        // -------------------------------------------------------------------------------------
		        // Get the memory component to be refined. This memory will be substituted with new ones
			// -------------------------------------------------------------------------------------
		        Memory dMemory = _getMemory( platform, resource, DM );
		        Memory pMemory = _getMemory( platform, resource, PM );

			if( dMemory.getName().equals( pMemory.getName() ) ) {
			// ----------------------------------------
			// One physical memory for program and data
			// ----------------------------------------

			   // -----------------------------------------------------
			   // Split the data memory into blocks of sizes power of 2
			   // -----------------------------------------------------
		           Vector memories = _splitMemory( dMemory );

			   // ---------------------------------------
			   // Add new memories and memory controllers
			   // ---------------------------------------
			   Iterator mm = memories.iterator();
			   while( mm.hasNext() ) {

			      Memory mem = (Memory) mm.next();
			      // -----------------------------------------
			      // Add this memory component to the platform
			      // -----------------------------------------
			      _newResources.add( mem );

			      // Add a data memory controller between the memory and the processor
			      _addMemory( platform, resource, mem, DM );
			      // Add program memory controller between the same memory and processor
			      _addMemory( platform, resource, mem, PM );
		 	   }

			} else {
			// -----------------------------------------------
			// Separate physical memories for program and data
			// -----------------------------------------------

			   // -----------------------------------------------------
			   // Split the data memory into blocks of sizes power of 2
			   // -----------------------------------------------------
		           Vector memories = _splitMemory( dMemory );

			   // ---------------------------------------
			   // Add new memories and memory controllers
			   // ---------------------------------------
			   Iterator mm = memories.iterator();
			   while( mm.hasNext() ) {

			      Memory mem = (Memory) mm.next();
			      // -----------------------------------------
			      // Add this memory component to the platform
			      // -----------------------------------------
			      _newResources.add( mem );

			      _addMemory( platform, resource, mem, DM );
		 	   }

			   // --------------------------------------------------------
			   // Split the program memory into blocks of sizes power of 2
			   // --------------------------------------------------------
		           memories = _splitMemory( pMemory );

			   // ---------------------------------------
			   // Add new memories and memory controllers
			   // ---------------------------------------
			   mm = memories.iterator();
			   while( mm.hasNext() ) {

			      Memory mem = (Memory) mm.next();
			      // -----------------------------------------
			      // Add this memory component to the platform
			      // -----------------------------------------
			      _newResources.add( mem );

			      _addMemory( platform, resource, mem, PM );
		 	   }
			}
		    }
		}
	}

       /****************************************************************************************************************
	*  Find Program and Data memories connected to a processor through memory controllers.
	*  This procedure removes the (initial) processor memory and the corresponding controller from the platform
	*  because new (refined) memories will substitute the initial memory
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private Memory _getMemory( Platform platform, Resource processor, int type ) {

		// -----------------------------
		// Get the needed processor port
		// -----------------------------
		Port processorPort = null;

		Iterator p = processor.getPortList().iterator();
		while( p.hasNext() ) {

		    Port port = (Port) p.next();
		    // Get the Data memory side
		    if( type == 0 ) {
			if( port instanceof DPLBPort || port instanceof DLMBPort ) {

			    processorPort = port;
			    break;
			}
		    // Or get the Program memory side
		    } else {
			if( port instanceof IPLBPort || port instanceof ILMBPort ) {

			    processorPort = port;
			    break;
			}
		    }
		}
		
		Link link = processorPort.getLink();

		// -----------------------------
		// Finding the memory controller
		// -----------------------------
		MemoryController memCtrl = null;

		p = link.getPortList().iterator();
		while( p.hasNext() ) {

			Port port = (Port) p.next();
			if( port.getResource() instanceof MemoryController ) {

			    memCtrl = (MemoryController) port.getResource();

			    // ------------------------------------------------------------------
			    // remove the controller from the processor bus and from the platform
			    // ------------------------------------------------------------------
			    link.getPortList().remove( port );
			    _oldResources.add( memCtrl );
			    break;
			}
		}

		// ----------------------------
		// Finding the memory component
		// ----------------------------
		Memory memory = null;

		p = memCtrl.getPortList().iterator();
		while( p.hasNext() ) {

			Port port = (Port) p.next();
			Link pLink = port.getLink();
			Iterator pp = pLink.getPortList().iterator();
			while( pp.hasNext() ) {

			    Port pPort = (Port) pp.next();
			    if( pPort.getResource() instanceof Memory ) {
				memory = (Memory) pPort.getResource();

				// -------------------------------------------------------
				// Remove the memory and the link to the memory controller
				// -------------------------------------------------------
				_oldResources.add( memory );
				platform.getLinkList().remove( pLink );

			        break;
			    }
			}
		}

		return memory;
	}


       /****************************************************************************************************************
	*  Split a memory into new memories of sizes power of 2 (The smallest block is 2Kx8 -> 1 BRAM)
	*  Howeevr, VirtexII memory controllers does not support less than 8K memories, so we split
	*  the memories of blocks that are power of 8K
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private Vector _splitMemory( Memory memory ) {

		int memorySize = memory.getSize();
		int cntMem = 1;
		Vector newMemories = new Vector();
		Memory mem;

		while( memorySize > 0 ) {

		    for( int i=1; ; i++) {

		       int memBlock = (int) (Math.pow(2.0, (double) i)) * 8192; //2048 := 0x800

		       if( memBlock >= memorySize ) {

			   if( memBlock - memorySize < 8192 ) {
			      memBlock = (int) (Math.pow(2.0, (double) i)) * 8192;
			      //memBlock = i * 8192;
			   } else {
			      memBlock = (int) (Math.pow(2.0, (double) i-1)) * 8192;
			      //memBlock = (i-1) * 8192;
			   }
			   mem = new BRAM( "BRAM" + cntMem++ + "_" + _processorName );

		           mem.setSize( memBlock ); // size x 8 Bits
		           mem.setDataWidth( 32 );

		           memorySize = memorySize - memBlock;
		           break;
		       }
		    }

		    newMemories.add( mem );
		}

		return newMemories;
	}

       /****************************************************************************************************************
	*  Adds a memory component and memory controller to a Processor component
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _addMemory( Platform platform, Resource processor, Memory memory, int type ) {

		memory.setLevelUpResource( platform );
		
		// -----------------------------
		// Get the needed processor port
		// -----------------------------
		Port processorPort = null;

		Iterator p = processor.getPortList().iterator();
		while( p.hasNext() ) {

		    Port port = (Port) p.next();
		    // Get the Data memory side

		    if( type == 0 ) {
//*
			if( (processor instanceof MicroBlaze && port instanceof DLMBPort) ||
			    (processor instanceof PowerPC    && port instanceof DPLBPort) ) {

			    processorPort = port;
			    break;
			}
		    // Or get the Program memory side
		    } else {
			if( processor instanceof MicroBlaze && port instanceof ILMBPort ||
			    processor instanceof PowerPC    && port instanceof IPLBPort ) {

			    processorPort = port;
			    break;
			}
		    }
/*/
			if( port instanceof DPLBPort || port instanceof DLMBPort ) {
			    processorPort = port;
			    break;
			}
		    // Or get the Program memory side
		    } else {
			if( port instanceof IPLBPort || port instanceof ILMBPort ) {
			    processorPort = port;
			    break;
			}
		    }
//*/
		}

		Link link = processorPort.getLink();

		Port memPort;

		if( processor instanceof PowerPC ) {
		    memPort = new PLBPort( "IO_1" );
		} else { //if( processor instanceof MicroBlaze ) {
		    memPort = new LMBPort( "IO_1" );
		}

		// -----------------------------------------------------------------------------------
		// Don't set the link because we add a controller between the processor and the memory
		// -----------------------------------------------------------------------------------
		//memPort.setLink( link );
		memPort.setResource( memory );

		memory.getPortList().add( memPort );

		// -----------------------------------------
		// Add this memory component to the platform
		// -----------------------------------------
		//_newResources.add( memory );

		// ---------------------------------------------------------------------------
		// Create a memory controller and initialize it with the memory size at base=0
		// ---------------------------------------------------------------------------
		MemoryController memCtrl;
		if( type == 0 ) {  // Data Memory Controller
		    memCtrl = new MemoryController("DCTRL_" + memory.getName());
		} else {
		    memCtrl = new MemoryController("PCTRL_" + memory.getName());
		}

		Page page = new Page();
		page.setBaseAddress( 0 );
		page.setSize( memory.getSize() );
		memCtrl.getPageList().add( page );

		// ---------------------
		// Add memory controller
		// ---------------------
		_addController( platform, processorPort, memPort, memCtrl );
	}


       /****************************************************************************************************************
	*  Insert a resource between 2 ports (Used to add Memory, MultiFifo or read crossbar controllers to a processor)
	*
	* @param  platform Description of the Parameter
	****************************************************************************************************************/
	private void _addController( Platform platform, Port processorPort, Port memoryPort, Resource resource ) {

         	// -----------------------------------------
		// The link contains only the processor port
		// -----------------------------------------
		Link link = processorPort.getLink();

		resource.setLevelUpResource( platform );

		Port processorSide;
		Port memorySide;

		if( processorPort instanceof PLBPort ) {
		    processorSide = new PLBPort("IO_1");
		    memorySide = new PLBPort("IO_2");
		} else if( processorPort instanceof LMBPort ) {
		    processorSide = new LMBPort("IO_1");
		    memorySide = new LMBPort("IO_2");
		} else {  // Fifo (crossbar read controller) to be added
		    processorSide = new FifoWritePort("IO_1"); // crossbarSide
		    memorySide = new FifoReadPort("IO_2");     // Ip read side
		}

		processorSide.setResource( resource );
		memorySide.setResource( resource );

		// --------------------------------------------------------------------------
		// Connect the processor port with the controller port (use the current link)
		// --------------------------------------------------------------------------
		processorSide.setLink( link );

		link.getPortList().add( processorSide );

		// --------------------------------------------------------------------
		// Connect the controller port with the memory port (create a new link)
		// --------------------------------------------------------------------
		Link cbLink = new Link( "BUS_" + resource.getName() );
		cbLink.getPortList().add( memoryPort );
		cbLink.getPortList().add( memorySide );

		memoryPort.setLink( cbLink );
		memorySide.setLink( cbLink );

		// -----------------------------------
		// Add the new ports to the controller
		// -----------------------------------
		resource.getPortList().add( processorSide );
		resource.getPortList().add( memorySide );

		// Add the controller to the platform
		_newResources.add( resource );

		// Add the new created link to the platform
		platform.getLinkList().add( cbLink );
	}
	
	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static RefineMemories _instance = new RefineMemories();

	// Contains the components (memories and memory controllers) to be removed from the platform
	private Vector _oldResources = null;

	// Contains the components (memories and memory controllers) to be added to the platform
	private Vector _newResources = null;
	
	private String _processorName = "";
}



