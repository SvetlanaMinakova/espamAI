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

package espam.operations.consistencychecks;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.OPBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.peripherals.ZBTMemoryController;
import espam.datamodel.platform.peripherals.Uart;
import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;

import espam.operations.ConsistencyCheck;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class ...
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class CheckPlatform {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static CheckPlatform getInstance() {
		return _instance;
	}

	/**
	 *  Check if the input (xml) spec. of the platform is correct
	 *
	 * @param  platform Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void checkPlatform( Platform platform ) throws EspamException {

		System.out.println(" -- Checking platform ... ");

		try {
			// ---------------------------------------------------------------------------------------------
			// Combines general checks and checks the possible type of the mapping:  'many-to-one',
			// 'one-to-one' with 2 PowerPC processors, 'one-to-one' with MicroBlaze processors,
			// 'one-to-one' with compaan hardware nodes (with IP cores)
			// ---------------------------------------------------------------------------------------------
			_generalCheck( platform );

			// ---------------------------------------------------------------------------------------------
			// Check for unique names of each resource
			// ---------------------------------------------------------------------------------------------
			_checkResourceNames( platform );

			// ---------------------------------------------------------------------------------------------
			// Check for the correct type of each port of each resource
			// ---------------------------------------------------------------------------------------------
			_checkPortTypes( platform );

			// ---------------------------------------------------------------------------------------------
			// Check for a correct link specification
			// ---------------------------------------------------------------------------------------------
		        _checkLinks( platform );

			// ---------------------------------------------------------------------------------------------
			// Target FPGA board specific check
			// ---------------------------------------------------------------------------------------------
		        _checkBoard( platform );

			if( _error > 0 ) {
				String er = " ERRORS";
				if( _error == 1 ) {
					er = " ERROR";
				}
				System.err.println( " -- Platform specification check failed. " + _error + er + " found. \n ");
				System.exit(0);
			}

			System.out.println(" -- Check [Done]");

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nElaboratePlatform Exception: " + e.getMessage());
		}
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/**
	 *  Pre-check of the platform and check the mapping type.
	 *
	 * @param  platform Description of the Parameter
	 */
	private void _generalCheck( Platform platform ) {
	
		boolean isMappingEmpty = ConsistencyCheck.getInstance().getEmptyMappingFlag();
		boolean isNetworkEmpty = ConsistencyCheck.getInstance().getEmptyNetworkFlag();

		
		// ------------------------
		// The general check begins
		// ------------------------
		Iterator i = platform.getResourceList().iterator();
		while( i.hasNext() ) {

		    // ------------------------------------------
		    // Check for an empty resource list
		    // ------------------------------------------
		    if( platform.getResourceList().size() == 0 ) {

			System.err.println( "[Espam]ERROR: No processing resources found. \n ");
			_error++;

		    } else {
		    // ------------------------------------------------------------------
		    // Currently allowed resources are: MB, PPC, Crossbar, CompaanHWNode,
		    // ZBTMemoryController
		    // ------------------------------------------------------------------
			Resource resource = (Resource) i.next();
			if( !(resource instanceof MicroBlaze) && !(resource instanceof PowerPC) &&
			    !(resource instanceof Crossbar) && !(resource instanceof CompaanHWNode) &&
			    !(resource instanceof ZBTMemoryController) && !(resource instanceof Uart) && 
			    !(resource instanceof ADMXRCII) && !(resource instanceof ADMXPL) &&
			    !(resource instanceof XUPV5LX110T) && !(resource instanceof ML505) ) {
			    System.err.println("[Espam]ERROR: Resource " + resource + " cannot be used in the describtion of the platform. \n ");
			    _error++;
			}

			if( resource instanceof MicroBlaze ) {
			    _mb++; // count the number of MicroBlaze processors in the platform

			} else if( resource instanceof PowerPC ) {
			    _ppc++; // count the number of PowerPC processors in the platform

			} else if( resource instanceof CompaanHWNode ) {
			    _hwn++; // count the number of Compaan hardware nodes in the platform

			} else if( resource instanceof Crossbar ) {
			    _cb++; // count the number of Crossbars in the platform
			
			} else if( resource instanceof ZBTMemoryController ) {
			    _zmc++; // count the number of ZBTMemoryController in the platform

			} else if( resource instanceof Uart ) {
			    _uart++; // count the number of Uart in the platform

			} else if( resource instanceof ADMXRCII || resource instanceof ADMXPL ||
			           resource instanceof XUPV5LX110T || resource instanceof ML505 ) {

			    _hostInterface++; // count the number of host interfaces
			}
		    }
		}
		
        	// ------------------------------------------------------------------------------------------------
		// If no crossbar component is found in the platform specification, 'one-to-one' mapping of channels is assumed
		// ------------------------------------------------------------------------------------------------
		if( _cb == 0 ) {
		    ConsistencyCheck.getInstance().setMapChannelsOne2OneFlag();
		}

		// --------------------------------------
		// Check for 0 or 1 Compaan hardware node
		// --------------------------------------
		if( _hwn > 1 ) {
			System.err.println("[Espam]ERROR: " + _hwn + " Compaan hardware nodes found.");
			System.err.println("=====> The platform may contain 0 or 1 Compaan hardware nodes \n ");
		        _error++;
		}

		// -----------------------------------
		// Check for 0 or 1 crossbar component
		// -----------------------------------
		if( _cb > 1 ) {
			System.err.println("[Espam]ERROR: " + _cb + " Crossbar components found.");
			System.err.println("=====> The platform may contain 0 or 1 crossbar components \n ");
		        _error++;
		}

		// ---------------------------------------------------
		// Error if there is a crossbar but there are no links
		// ---------------------------------------------------
		if( _cb > 0 ) {
		    if( platform.getLinkList().size() == 0 ) {
			System.err.println( "[Espam]ERROR: No connections found.");
			System.err.println( "=====> If communication network component is used, links must be specified.\n ");
			_error++;
		    }
		// ---------------------------------------------------
		// Error if there is no a crossbar but there are links
		// ---------------------------------------------------
		//} else {
		//    if( platform.getLinkList().size() > 0 ) {
		//	System.out.println( "ERROR> Connections specified.");
		//	System.out.println( "=====> If communication network component is not used, links must not be specified.\n ");
		//	_error++;
		//    }
		}
		
        	// ---------------------------------------------------
		// Error if there is a ZBTMemoryController but there are no links
		// ---------------------------------------------------
		if( _zmc > 0 ) {
		    if( platform.getLinkList().size() == 0 ) {
			System.err.println( "[Espam]ERROR: No connections found.");
			System.err.println( "=====> If ZBTCTRL peripheral component is used, links must be specified.\n ");
			_error++;
		    }
		}
		    
        	// ---------------------------------------------------
		// Error if there is a Uart but there are no links
		// ---------------------------------------------------
		if( _uart > 0 ) {
			if( platform.getLinkList().size() == 0 ) {
			System.err.println( "[Espam]ERROR: No connections found.");
			System.err.println( "=====> If UART peripheral component is used, links must be specified.\n ");
			_error++;
			}
		}
		
		// -----------------------------------
		// Check for 0 or 1 interfaces
		// -----------------------------------
		if( _hostInterface > 1 ) {
			System.err.println("[Espam]ERROR: " + _hostInterface + " host interfaces found.");
			System.err.println("=====> The platform may contain 0 or 1 host interface \n ");
		        _error++;
		}

		// --------------------------------------------------------------------------------------------------
		// If platform is supposed to be generated without an application mapped on it, this check is skipped
		// --------------------------------------------------------------------------------------------------
		if( isMappingEmpty == true && isNetworkEmpty == false ) {

		    // ---------------------------------------------------------------------
		    // If the mapping is empty, 'one-to-one' mapping of processes is assumed
		    // ---------------------------------------------------------------------
		    ConsistencyCheck.getInstance().setMapProcessesOne2OneFlag();

		    // ---------------------------------------------------------------
		    // If the mapping is empty, there must not be a crossbar specified
		    // ---------------------------------------------------------------
		    if( _cb > 0 ) {
			System.err.println( "[Espam]ERROR: The mapping is empty." );
			System.err.println( "=====> Communication network component found. Mapping is required.\n ");
			_error++;
		    }
		    // ---------------------------------------------------------------
		    // If the mapping is empty, there must not be links specified
		    // ---------------------------------------------------------------
		    if( platform.getLinkList().size() > 0 ) {
			System.err.println( "[Espam]ERROR: Connections specified.");
			System.err.println( "=====> Mapping is empty. Links must not be specified.\n ");
			_error++;
		    }
		    // -----------------------------------------------
		    // Only one processing component must be specified
		    // -----------------------------------------------
		    if( !(_mb  == 1 && _ppc == 0 && _hwn == 0) &&
			!(_ppc == 1 && _mb  == 0 && _hwn == 0) &&
			!(_hwn == 1 && _ppc == 0 && _mb  == 0) ) {

			System.err.println( "[Espam]ERROR: More than one processors are found." );
			System.err.println( "=====> One-to-one mapping is assumed and only one processing component must be specified. \n ");
			_error++;
   		    }
		}

		// ---------------------------------------------------------------------------------------
		// If no links are specified, the processing component must be specified without any ports
		// ---------------------------------------------------------------------------------------
		if( platform.getLinkList().size() == 0 ) {

		    Iterator j = platform.getResourceList().iterator();
		    while( j.hasNext() ) {

		        Resource processor = (Resource) j.next();

		        if( processor.getPortList().size() > 0 ) {
			    System.err.println( "[Espam]ERROR: Processor ports found." );
			    System.err.println( "=====> No links are specified and no ports of the processing components must be specified.\n ");
			    _error++;
			}
		    }
		}

		if( _error > 0 ) {
			String er = " ERRORS";
			if( _error == 1 ) {
				er = " ERROR";
			}
			System.err.println( " -- Platform specification check failed. " + _error + er + " found. \n ");
			System.exit(0);
		}
	}

	/**
	 *  Check the resource and link names
	 *
	 * @param  platform Description of the Parameter
	 */
	private void _checkResourceNames( Platform platform ) {

		// ---------------------------------------------------------------------------------------------
		// Check for unique names of each resource
		// ---------------------------------------------------------------------------------------------
		Iterator i = platform.getResourceList().iterator();
		while( i.hasNext() ) {

		        int equalNames = 0;
			Resource curResource = (Resource) i.next();
			Iterator j = platform.getResourceList().iterator();
			while( j.hasNext() ) {
				Resource tempResource = (Resource) j.next();
				if( curResource.getName().equals(tempResource.getName()) ) {
					equalNames++;
				}
			}
			j = platform.getLinkList().iterator();
			while( j.hasNext() ) {
				Link tempLink = (Link) j.next();
				if( tempLink.getName().equals(curResource.getName()) ) {
					equalNames++;
				}
			}
			if( equalNames > 1 ) {
				System.err.println("[Espam]ERROR: " + curResource + ". Redefinition of name \"" + curResource.getName() + "\". \n ");
				System.err.println( " -- Platform specification check failed. \n ");
				System.exit(0);
			}
		}

		// Additional check is needed between the link names
		i = platform.getLinkList().iterator();
		while( i.hasNext() ) {

		        int equalNames = 0;
			Link curLink = (Link) i.next();
			Iterator j = platform.getLinkList().iterator();
			while( j.hasNext() ) {
				Link tempLink = (Link) j.next();
				if( tempLink.getName().equals(curLink.getName()) ) {
					equalNames++;
				}
			}
			if( equalNames > 1 ) {
				System.err.println("[Espam]ERROR: " + curLink + ". Redefinition of name \"" + curLink.getName() + "\". \n ");
				System.err.println( " -- Platform specification check failed. \n ");
				System.exit(0);
			}
		}
	}

	/**
	 *  Check the port types
	 *
	 * @param  platform Description of the Parameter
	 */
	private void _checkPortTypes( Platform platform ) {

		Iterator i = platform.getResourceList().iterator();
		while( i.hasNext() ) {

			Resource resource = (Resource) i.next();
			//---------------------------------------------------------------------------------------------
			// Check for the correct types of each port of each resource
			//---------------------------------------------------------------------------------------------
			if( resource instanceof MicroBlaze ) {

			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				//if( !(port instanceof LMBPort) ) {
				if( port instanceof FifoReadPort  || port instanceof FifoWritePort ||
				    port instanceof CompaanInPort || port instanceof CompaanOutPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type LMBPort, PLBPort, or OPBPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if( resource instanceof PowerPC ) {

			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				//if( !(port instanceof PLBPort) ) {
				if( port instanceof LMBPort || port instanceof FifoReadPort || port instanceof FifoWritePort ||
				    port instanceof CompaanInPort || port instanceof CompaanOutPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type PLBPort or OPBPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if( resource instanceof CompaanHWNode ) {

			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				//if( !(port instanceof CompaanInPort) && !(port instanceof CompaanOutPort)) {
				if( port instanceof LMBPort || port instanceof PLBPort || port instanceof OPBPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type CompaanInPort or CompaanOutPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if( resource instanceof Crossbar ) {

			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				if( (port instanceof LMBPort) || port instanceof PLBPort || port instanceof FifoWritePort ||
				     port instanceof CompaanInPort || port instanceof CompaanOutPort || port instanceof OPBPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type FifoReadPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if ( resource instanceof ZBTMemoryController ) {

			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				if( (port instanceof LMBPort) || port instanceof FifoWritePort ||
				     port instanceof CompaanInPort || port instanceof CompaanOutPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type PLBPort or OPBPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if ( resource instanceof Uart ) {
				
				Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				if( (port instanceof LMBPort) || port instanceof PLBPort || port instanceof FifoWritePort ||
				     port instanceof CompaanInPort || port instanceof CompaanOutPort ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type OPBPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if ( resource instanceof XUPV5LX110T ) {
				
			    Iterator j = resource.getPortList().iterator();
			    while( j.hasNext() ) {
       				Port port = (Port) j.next();
				if( !(port instanceof PLBPort) ) {
					System.err.println("[Espam]ERROR: Resource " + resource + " must have ports of type PLBPort.");
					System.err.println("=====> Found " + port + " \n ");
					_error++;
				}
			    }
			} else if ( resource instanceof ADMXRCII || resource instanceof ADMXPL || resource instanceof ML505 ) {
			    System.out.println("[Espam] WARNING: Checking port types of " + resource + " is skipped.");
			}
		}
	}

	/**
	 *  Check the links
	 *
	 * @param  platform Description of the Parameter
	 */
	private void _checkLinks( Platform platform ) {

		// -------------------------------------------------------------------------------------------
		// Check for missing links (in resorce ports) or multiple links pointing to a resource port
		// -------------------------------------------------------------------------------------------
		Iterator i = platform.getResourceList().iterator();
		while( i.hasNext() ) {

			Resource resource = (Resource) i.next();
			Iterator j = resource.getPortList().iterator();
			while( j.hasNext() ) {

			    Port port = (Port) j.next();
			    Link link = port.getLink();
			    if( link.getName().equals("") ) {
			       System.err.println( "[Espam]ERROR: No set link found in component " + resource + ", " + port + "\n ");
			       _error++;
			    }

			    // ------------------------------------------------------------------------------------
			    // Check if more than 1 link contain this resource port (ERROR)
			    // ------------------------------------------------------------------------------------
			    Vector portLinks = new Vector();
			    Iterator l = platform.getLinkList().iterator();
			    while( l.hasNext() ) {

			        Link curLink = (Link) l.next();
			        Iterator p = curLink.getPortList().iterator();
			        while( p.hasNext() ) {

				    Port curPort = (Port) p.next();
			            if( port.getResource().getName().equals(curPort.getResource().getName()) ) {
				        if( port.getName().equals(curPort.getName()) ) {

				            portLinks.add( curLink );
			                }
				    }
				}
			    }

			    if( portLinks.size() > 1 ) {
				System.err.println("[Espam]ERROR: Multiple links to a resource port.");
				System.err.println("=====> " + portLinks  + " point to " +
						port + " of " + resource + " \n ");
				_error++;
			    }
			}
		}

		// ---------------------------------------------------------------------------------------------------
		// Currently allowed links are: processor-crossbar or hardwareNode-crossbar or ZBTMemoryController
		// ---------------------------------------------------------------------------------------------------
		i = platform.getLinkList().iterator();
		while( i.hasNext() ) {

			Link channel = (Link) i.next();
			int proc=0;
			int commun=0;
			int hw=0;
			Vector procList = new Vector(); // Debug information: which processors are directly connected

			Iterator j = channel.getPortList().iterator();
			while( j.hasNext() ) {
			   Port port = (Port) j.next();
			   if( port.getResource() instanceof Processor ) {
			   	proc++;
				procList.add( port.getResource() );
			   } else if( port.getResource() instanceof CompaanHWNode ) {
				hw++;
			   } else if( port.getResource() instanceof Crossbar ) {
			        commun++;
			   }
			}

			if( proc > 1 || commun > 1 || hw > 1 ) {
				System.err.println( "[Espam]ERROR: Wrong type of connection in " + channel );
				if( proc > 1 ) {
					System.err.println( "=====> " + proc + " processor ports connected point-to-point. " + procList );
				}
				if( commun > 1 ) {
					System.err.println( "=====> " + commun + " crossbar ports connected point-to-point" );
				}
				if( hw > 1 ) {
					System.err.println( "=====> " + "Direct connection between compaan hardware node ports is not supported yet" );
				}

				System.out.println();
				_error++;
			}

			// Check for empty or links poitning to one port only
			if( channel.getPortList().size() < 2 ) {
				System.err.println( "[Espam]ERROR: Link " + channel.getName() + " is not a connection. " );
				System.err.println( "=====> The link points to " + channel.getPortList().size() + " port." );
				System.err.println();
				_error++;
			}
		}
	}
	
	/**
	 *  Target FPGA board specific check
	 *
	 * @param  platform Description of the Parameter
	 */
	private void _checkBoard( Platform platform ) {

		Iterator i = platform.getResourceList().iterator();
		while( i.hasNext() ) {

			Resource resource = (Resource) i.next();

			if( resource instanceof ADMXRCII ) {

			    if( _uart > 1 ) {
				System.err.println("[Espam] ERROR: The target board ADM-XRC-II supports only one UART.");
				System.err.println("=====> Found " + _uart + " UART components in the platform specification. \n ");
				_error++;
			    }
			    if( _zmc > 6 ) {
				System.err.println("[Espam] ERROR: The target board ADM-XRC-II has 6 static RAM off-chip memory banks.");
				System.err.println("=====> Found " + _zmc + " ZBT controllers in the platform specification. \n ");
				_error++;
			    }
			} else if( resource instanceof ADMXPL  ) {
			    if( _ppc > 2 ) {
				System.err.println("[Espam] ERROR: The target board ADM-XPL has only 2 PowerPC processors.");
				System.err.println("=====> Found " + _ppc + " PowerPC processors in the platform specification. \n ");
				_error++;
			    }
			} else if( resource instanceof XUPV5LX110T ) {
			    if( _zmc > 6 ) {
				System.err.println("[Espam] ERROR: The target board XUPV5-LX110T has one static RAM off-chip memory");
				System.err.println("=====> Found " + _zmc + " ZBT controllers in the platform specification. \n ");
				_error++;
			    }
// check the number of processors requiring off-chip memory (host interface port size > 0)
// The MPMC controller has 8 ports, one is reserved for interface with the host.
			} else if( resource instanceof ML505 ) {
			    if( _zmc > 6 ) {
				System.err.println("[Espam] ERROR: The target board XUPV5-LX110T has one static RAM off-chip memory");
				System.err.println("=====> Found " + _zmc + " ZBT controllers in the platform specification. \n ");
				_error++;
			    }
			}
		}
	}


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static CheckPlatform _instance = new CheckPlatform();

	private static int _error = 0;

	private static int _ppc=0; // The number of PowerPC processors in the platform
	private static int _mb=0;  // The number of Microblaze processors in the platform
	private static int _cb=0;  // The number of Crossbars in the platform
	private static int _hwn=0; // The number of Compaan hardware nodes in the platform (must be 0 or 1)
	private static int _zmc=0;  // The number of ZBTMemoryController in the platform
	private static int _uart=0;  // The number of Uart in the platform
	private static int _hostInterface=0;  // The number of host interfaces

}

