
package espam.operations.platformgeneration.elaborate;

import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;

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
import espam.datamodel.platform.ports.OPBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.FifosController;

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
 *  This class elaborates a platform in 'many-to-one' manner without a communication network component:
 *
 * - The platform specification contains only the processing components. There is no communication network component,
 *   there is 0 or 1 HW component and there are no links specified.
 *   Links are created according to the PN topology and the mapping.
 *
 * - A mapping is specified - several processes can be mapped onto 1 processor. If several HW IPs are mapped onto
 *   the HW component, in the elaborated platform a HW component will be generated per IP core.
 *
 *  The elaborated platform contains processors, processor memories, hardwared components, fifos, virtual buffers, crossbar component
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class ElaborateMany2One {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static ElaborateMany2One getInstance() {
        return _instance;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /****************************************************************************************************************
      *  elaborate a platform with many-to-one mapping without crossbar communication network component
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    public void elaborate( Platform platform, Mapping mapping ) {
        
        // -----------------------------------------------------------------------------------------------------------
        // HW Nodes, memories, fifos are added to this vector.
        // This vector is needed because we can not add elements to the resource list until we use the iterator of it
        // -----------------------------------------------------------------------------------------------------------
        _newResources = new Vector();
        
        // ---------------------------------------------------------------------------------
        // ZBT memory is attached to the first PPC,
        // BRAMs are attached to the MicroBlazes and PPC: one memory component per processor
        // ---------------------------------------------------------------------------------
        _elaborateProcessors( platform );
        
        // ---------------------------------------------------------------------------------------
        // Create the platform according to the process network topology and the specified mapping
        // ---------------------------------------------------------------------------------------
        _elaboratePlatform( platform, mapping );
        
        // -----------------------------------------------------
        // Add fifos controller to each procesor of the platform
        // -----------------------------------------------------
        _addFifosControllers( platform );
        
        // ---------------------------------------------
        // Add all new created resources to the platform
        // ---------------------------------------------
        platform.getResourceList().addAll( _newResources );
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    
    /****************************************************************************************************************
      *  Elaborate processor components:
      * ZBT memory is attached to the first PPC,
      * BRAMs are attached to the MicroBlazes and PPC: one memory component per processor
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _elaborateProcessors( Platform platform ) {
        
        Iterator i = platform.getResourceList().iterator();
        while( i.hasNext() ) {
            
            Resource processor = (Resource) i.next();
            
            if( processor instanceof Processor ) {
                
                // ---------------------------------------------------------------------------
                // When a mapping is not specified, the processors are specified without ports
                // The instruction and data processor ports are created here
                // ---------------------------------------------------------
                Port processorIPort;
                Port processorDPort;
                
                if( processor instanceof MicroBlaze ) {
                    processorIPort = new ILMBPort( "ILMB" );
                    processorDPort = new DLMBPort( "DLMB" );
                } else {
                    processorIPort = new IPLBPort( "IPLB" );
                    processorDPort = new DPLBPort( "DPLB" );
                }
                
                processor.getPortList().add( processorDPort );
                processor.getPortList().add( processorIPort );
                processorDPort.setResource( processor );
                processorIPort.setResource( processor );
                
                // --------------------------------------------------------------------------------
                // When the port is created a default link is created and added to the port as well
                // However, this default link does not contain the processor port and the platform
                // does not contain this default link
                // --------------------------------------------------------------------------------
                Link link = processorDPort.getLink();
                link.setName( "DBUS_" + processor.getName() );
                link.getPortList().add( processorDPort );
                
                // Add the link to the platform
                platform.getLinkList().add( link );
                
                link = processorIPort.getLink();
                link.setName( "PBUS_" + processor.getName() );
                link.getPortList().add( processorIPort );
                
                // Add the link to the platform
                platform.getLinkList().add( link );
                
                // -------------------------------------------------------------------------------------------------
                // Create a Memory component of the processor (for both program and data) and add it to the platform
                // -------------------------------------------------------------------------------------------------
                Memory memory = new Memory( "M_" + processor.getName() );
                memory.setSize( ((Processor) processor).getDataMemSize() + ((Processor) processor).getProgMemSize() );
                memory.setDataWidth( 32 );
                memory.setLevelUpResource( platform );
                _addMemory2( platform, processorDPort, processorIPort, memory );
                
                // --------------------------------------------------------------------------
                // Create a Data Memory component of the processor and add it to the platform
                // --------------------------------------------------------------------------
                //Memory memory = new Memory( "DM_" + processor.getName() );
                //memory.setSize( ((Processor) processor).getDataMemSize() );
                //memory.setDataWidth( 32 );
                //memory.setLevelUpResource( platform );
                //_addMemory( platform, processorDPort, memory );
                
                // -----------------------------------------------------------------------------
                // Create a Program Memory component of the processor and add it to the platform
                // -----------------------------------------------------------------------------
                //memory = new Memory( "PM_" + processor.getName() );
                //memory.setSize( ((Processor) processor).getProgMemSize() );
                //memory.setDataWidth( 32 );
                //memory.setLevelUpResource( platform );
                //_addMemory( platform, processorIPort, memory );
            }
        }
    }
    
    /****************************************************************************************************************
      *  Adds a memory component and memory controller to a Processor component
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addMemory( Platform platform, Port processorPort, Memory memory ) {
        
        Link link = processorPort.getLink();
        Port   memPort;
        
        if( processorPort.getResource() instanceof PowerPC ) {
            memPort = new PLBPort( "IO_1" );
        } else { //if( resource instanceof MicroBlaze ) {
            memPort = new LMBPort( "IO_1" );
        }
        
        // -----------------------------------------------------------------------------------
        // Don't set the link because we add a controller between the processor and the memory
        // -----------------------------------------------------------------------------------
        //memPort.setLink( link );
        memPort.setResource( memory );
        
        memory.getPortList().add( memPort );
        
        // --------------------------------------------
        // Link the memory port with the processor port
        // --------------------------------------------
        //link.getPortList().add( memPort );
        
        // -----------------------------------------
        // Add this memory component to the platform
        // -----------------------------------------
        _newResources.add( memory );
        
        // ---------------------
        // Add memory controller
        // ---------------------
        MemoryController memCtrl = new MemoryController("CTRL_" + memory.getName());
        _addController( platform, processorPort, memPort, memCtrl );
    }
    
    /****************************************************************************************************************
      *  Adds a memory component and 2 memory controllers to a Processor component
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addMemory2( Platform platform, Port procDataPort, Port procProgPort, Memory memory ) {
        
        Link linkData = procDataPort.getLink();
        Link linkProg = procProgPort.getLink();
        Port   memDataPort;
        Port   memProgPort;
        
        if( procDataPort.getResource() instanceof PowerPC ) {
            memDataPort = new PLBPort( "DM" );
            memProgPort = new PLBPort( "PM" );
        } else { //if( resource instanceof MicroBlaze ) {
            memDataPort = new LMBPort( "DM" );
            memProgPort = new LMBPort( "PM" );
        }
        
        // -----------------------------------------------------------------------------------
        // Don't set the link because we add a controller between the processor and the memory
        // -----------------------------------------------------------------------------------
        //memPort.setLink( link );
        memDataPort.setResource( memory );
        memProgPort.setResource( memory );
        
        memory.getPortList().add( memDataPort );
        memory.getPortList().add( memProgPort );
        
        // --------------------------------------------
        // Link the memory port with the processor port
        // --------------------------------------------
        //link.getPortList().add( memPort );
        
        // -----------------------------------------
        // Add this memory component to the platform
        // -----------------------------------------
        _newResources.add( memory );
        
        // ----------------------
        // Add memory controllers
        // ----------------------
        MemoryController memDataCtrl = new MemoryController("DCTRL_" + memory.getName());
        _addController( platform, procDataPort, memDataPort, memDataCtrl );
        MemoryController memProgCtrl = new MemoryController("PCTRL_" + memory.getName());
        _addController( platform, procProgPort, memProgPort, memProgCtrl );
    }
    
    
    /****************************************************************************************************************
      *  Elaborate Platform:
      * Create the platform according to the process network topology and the specified mapping
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _elaboratePlatform( Platform platform, Mapping mapping ) {
        
        // Contains the correspondence between CDGates and platform components ports
        Hashtable hashTable = new Hashtable();
        
        // Contains the HW nodes that are already added to the platform
        Hashtable hashNodes = new Hashtable();
        
        // Used in names creation
        int cntHWN = 1;
        int cntInPorts = 1;
        int cntOutPorts = 1;
        
        CDProcessNetwork cdpn = mapping.getCDPN();
        
        // ------------------------------------------------------------------
        // Create the map between process gates and platform components ports
        // ------------------------------------------------------------------
        Iterator i = mapping.getProcessorList().iterator();
        while( i.hasNext() ) {
            
            cntInPorts  = 1;
            cntOutPorts = 1;
            
            MProcessor mProcessor = (MProcessor) i.next();
            Resource processor = mProcessor.getResource();
            
            if( processor instanceof Processor ) {
                
                MProcess process = (MProcess) mProcessor.getProcessList().get(0);
                ADGNode node = process.getNode();
                CDProcess cdPrcs = cdpn.getProcess( node );
                
                // -----------------------
                // Add communication ports
                // -----------------------
                Iterator inG = cdPrcs.getInGates().iterator();
                while( inG.hasNext() ) {
                    
                    CDInGate gate = (CDInGate) inG.next();
                    
                    FifoReadPort inPort = new FifoReadPort( "In_" + cntInPorts++ );
                    inPort.setResource( processor );
                    processor.getPortList().add( inPort );
                    // Make a connection between the processor port and the gate
                    hashTable.put( gate, inPort );
                }
                
                Iterator outG = cdPrcs.getOutGates().iterator();
                while( outG.hasNext() ) {
                    
                    CDOutGate gate = (CDOutGate) outG.next();
                    
                    FifoWritePort outPort = new FifoWritePort( "Out_" + cntOutPorts++ );
                    outPort.setResource( processor );
                    processor.getPortList().add( outPort );
                    // Make a connection between the processor port and the gate
                    hashTable.put( gate, outPort );
                }
                
            } else if( processor instanceof CompaanHWNode ) {
                
                MProcess process = (MProcess) mProcessor.getProcessList().get(0);
                ADGNode node = process.getNode();
                CDProcess cdPrcs = cdpn.getProcess( node );
                
                // -----------------------
                // Add communication ports
                // -----------------------
                Iterator inG = cdPrcs.getInGates().iterator();
                while( inG.hasNext() ) {
                    
                    CDInGate gate = (CDInGate) inG.next();
                    
                    CompaanInPort inPort = new CompaanInPort( "In_" + cntInPorts++ );
                    // Make a connection between the processor port and HW Node
                    hashTable.put( gate, inPort );
                }
                
                Iterator outG = cdPrcs.getOutGates().iterator();
                while( outG.hasNext() ) {
                    
                    CDOutGate gate = (CDOutGate) outG.next();
                    
                    CompaanOutPort outPort = new CompaanOutPort( "Out_" + cntOutPorts++ );
                    // Make a connection between the processor port and the gate
                    hashTable.put( gate, outPort );
                }
            }
        }
        
        // Remove the hwNode from the initial platform and mapping (new HWNodes to be added)
        _removeHWNode( platform, mapping );
        
        // ---------------------------------------------------------------------------------------------------
        // Create hardware nodes and links between the resources according to the process network and mapping.
        // Add the created hardware nodes and links to the platform, add MProcessor to the mapping
        // ---------------------------------------------------------------------------------------------------
        Iterator c = cdpn.getChannelList().iterator();
        while( c.hasNext() ) {
            
            ADGNode adgNode;
            
            CDChannel channel = (CDChannel) c.next();
            CDInGate toGate = channel.getToGate();
            CDOutGate fromGate = channel.getFromGate();
            Port inPort;
            Port outPort;
            
            inPort  = (FifoReadPort) hashTable.get( toGate );
            outPort = (FifoWritePort) hashTable.get( fromGate );
            
            // -------------------------------------------------------
            // Add HW nodes if needed and attach I/O ports to HW nodes
            // -------------------------------------------------------
            if( inPort instanceof CompaanInPort ) {
                
                adgNode = (ADGNode) ((ADGPort) toGate.getAdgPortList().get(0)).getNode();
                CompaanHWNode hwNode = (CompaanHWNode) hashNodes.get( adgNode );
                
                if( hwNode == null ) {
                    // Create a new HW Node and put it into the hash table
                    hwNode = new CompaanHWNode( "HWN_" + cntHWN++ );
                    platform.getResourceList().add( hwNode );
                    
                    hashNodes.put( adgNode, hwNode );
                    
                    // Create and add a MProcessor to the mapping. MProcessor points to a Resource and ADGNode mapped on it
                    _addMProcessor( hwNode, adgNode, mapping );
                }
                inPort.setResource( hwNode );
                hwNode.getPortList().add( inPort );
            }
            
            if( outPort instanceof CompaanOutPort ) {
                
                adgNode = (ADGNode) ((ADGPort) fromGate.getAdgPortList().get(0)).getNode();
                CompaanHWNode hwNode = (CompaanHWNode) hashNodes.get( adgNode );
                
                if( hwNode == null ) {
                    // Create a new HW Node and put it into the hash table
                    hwNode = new CompaanHWNode( "HWN_" + cntHWN++ );
                    platform.getResourceList().add( hwNode );
                    
                    hashNodes.put( adgNode, hwNode );
                    
                    // Create and add a MProcessor to the mapping. MProcessor points to a Resource and ADGNode mapped on it
                    _addMProcessor( hwNode, adgNode, mapping );
                }
                outPort.setResource( hwNode );
                hwNode.getPortList().add( outPort );
            }
            
            // ---------------------------------------------------------------------------------------
            // Create and initialize a link, add it to the inPort, outPort and platform. Insert a fifo
            // ---------------------------------------------------------------------------------------
            Link link = new Link("BUS_" + outPort.getResource().getName() + "_" + outPort.getName());
            link.getPortList().add( outPort );
            
            inPort.setLink(  link );
            outPort.setLink( link );
            platform.getLinkList().add( link );
            
            Fifo fifo = new Fifo( "FIFO_" + outPort.getResource().getName() + "_" + outPort.getName() );
            _addFifo( platform, outPort, inPort, fifo );
            
            // ----------------------------------------------------------------------------------------
            // Create and add a MFifo to the mapping. MFifo points to a Fifo and CDChannel mapped on it
            // ----------------------------------------------------------------------------------------
            _addMFifo( fifo, channel, mapping );
        }
    }
    
    /****************************************************************************************************************
      *  Adds a Fifo component between 2 ports
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addFifo( Platform platform, Port writePort, Port readPort, Fifo fifo ) {
        
        // -------------------------------------
        // The link contains only the write port
        // -------------------------------------
        Link link = writePort.getLink();
        
        // ----------------------------------------------------------------------------------------------
        // Add a fifo between the read and the write ports
        // ----------------------------------------------------------------------------------------------
        //Fifo fifo = new Fifo( "FIFO_" + writePort.getResource().getName() + "_" + writePort.getName() );
        //fifo.setSize( 512 );
        fifo.setDataWidth( 32 ); // ???
        fifo.setLevelUpResource( platform );
        
        // -----------------------------------------------------------------
        // Connect the Compaan out port with the fifo (use the current link)
        // -----------------------------------------------------------------
        FifoWritePort writeSide = new FifoWritePort( "IO_1" );
        writeSide.setResource( fifo );
        writeSide.setLink( link );
        
        link.getPortList().add( writeSide );
        
        // -----------------------------------------------------------
        // Connect the crossbar port with the fifo (create a new link)
        // -----------------------------------------------------------
        FifoReadPort readSide = new FifoReadPort( "IO_2" );
        readSide.setResource( fifo );
        
        Link cbLink = new Link( "BUS_" + fifo.getName() );
        cbLink.getPortList().add( readPort );
        cbLink.getPortList().add( readSide );
        
        readPort.setLink( cbLink );
        readSide.setLink( cbLink );
        
        // -----------------------------
        // Add the new ports to the fifo
        // -----------------------------
        fifo.getPortList().add( writeSide   );
        fifo.getPortList().add( readSide );
        
        // Add the fifo component to the platform
        _newResources.add( fifo );
        
        // Add the new created link to the platform
        platform.getLinkList().add( cbLink );
    }
    
    /****************************************************************************************************************
      *  Removes the hardware node from the initial platform specification
      *  Removes the port from the corresponding (Crossbar) component as well
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _removeHWNode( Platform platform, Mapping mapping ) {
        
        Resource   res = null;
        MProcessor prc = null;
        String resName = "";
        
        Iterator r = platform.getResourceList().iterator();
        while( r.hasNext() ) {
            
            res = (Resource) r.next();
            if( res instanceof CompaanHWNode ) {
                
                resName = res.getName();
                platform.getResourceList().remove( res );
                
                // --------------------------------------------
                // The specification contained only one HW node
                // --------------------------------------------
                break;
            }
        }
        
        Iterator m = mapping.getProcessorList().iterator();
        while( m.hasNext() ) {
            
            prc = (MProcessor) m.next();
            if( prc.getName().equals( resName ) ) {
                
                mapping.getProcessorList().remove( prc );
                break;
            }
        }
    }
    
    /****************************************************************************************************************
      *  Adds a MFifo to the mapping specification. MFifo points to a Fifo and CDChannel mapped on it
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addMFifo( Fifo fifo, CDChannel channel, Mapping mapping ) {
        
        MFifo mFifo = new MFifo( fifo.getName() );
        mFifo.setChannel( channel );
        mFifo.setFifo( fifo );
        mapping.getFifoList().add( mFifo );
    }
    
    /****************************************************************************************************************
      *  Adds a MProcessor to the mapping specification.
      *  MProcessor points to a processing component of a platform and an ADGNode mapped on it
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addMProcessor( Resource resource, ADGNode node, Mapping mapping ) {
        
        MProcess mProcess = new MProcess( node.getName() );
        mProcess.setNode( node );
        
        MProcessor mProcessor = new MProcessor( resource.getName() );
        mProcessor.getProcessList().add( mProcess );
        mProcessor.setResource( resource );
        mProcessor.setScheduleType( 0 ); // Default value
        
        mapping.getProcessorList().add( mProcessor );
    }
    
    /****************************************************************************************************************
      *  Insert a resource between 2 ports (Used to add Memory, MultiFifo or read crossbar controllers to a processor)
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addController( Platform platform, Port processorPort, Port memoryPort, Controller resource ) {
        
        // -----------------------------------------
        // The link contains only the processor port
        // -----------------------------------------
        Link link = processorPort.getLink();
        
        resource.setLevelUpResource( platform );
        
        Port processorSide;
        Port memorySide;
        
//  if( (resource instanceof PowerPC && processorPort instanceof PLBPort ) {
        
//  if( processorPort instanceof PLBPort ) {
//      processorSide = new PLBPort("IO_1");
//      memorySide = new PLBPort("IO_2");
//  } else if( processorPort instanceof LMBPort ) {
        if( processorPort instanceof LMBPort ) {
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
        //Link cbLink = new Link( "BUS_" + resource.getName() );
        Link cbLink = new Link( "BUS_" + processorPort.getName() );
        cbLink.getPortList().add( memoryPort );
        cbLink.getPortList().add( memorySide );
        
        memoryPort.setLink( cbLink );
        memorySide.setLink( cbLink );
        
        // -----------------------------------
        // Add the new ports to the controller
        // -----------------------------------
        resource.getPortList().add( processorSide );
        resource.getPortList().add( memorySide );
        
        // -------------------------
        // Initialize the controller
        // -------------------------
        Memory memory = (Memory) memoryPort.getResource();
        Page page = new Page();
        page.setBaseAddress( 0 );
        page.setSize( memory.getSize() );
        resource.getPageList().add( page );
        
        // Add the controller to the platform
        _newResources.add( resource );
        
        // Add the new created link to the platform
        platform.getLinkList().add( cbLink );
    }
    
    /****************************************************************************************************************
      *  Add and connect a Fifos controller to a processor
      *  A Fifo controller has one port (processor port) and several fifo read and/or fifo write ports
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addFifosControllers( Platform platform ) {
        
        Iterator r = platform.getResourceList().iterator();
        while( r.hasNext() ) {
            
            Resource resource = (Resource) r.next();
            
            if( resource instanceof Processor ) {
                
                Vector ctrlPorts = new Vector();
                
                Port processorIPort = new Port("");
                Port processorDPort = new Port("");
                Port controllerPort = new Port("");
                Port opbPort        = new Port("");
                Port plbPort        = new Port(""); // of a MicroBlaze
                
                if( resource instanceof MicroBlaze ) {
                    
                    controllerPort = new LMBPort("CTRL_IO");
                    
                } else if( resource instanceof PowerPC ) {
                    
                    controllerPort = new PLBPort("CTRL_IO");
                }
                
                // --------------------
                // Find processor ports
                // --------------------
                Iterator p = resource.getPortList().iterator();
                while( p.hasNext() ) {
                    Port port = (Port) p.next();
//*
                    if( (resource instanceof PowerPC && port instanceof IPLBPort) || port instanceof ILMBPort ) {
                        
                        processorIPort = port;
                        
                    } else if( (resource instanceof PowerPC && port instanceof DPLBPort) || port instanceof DLMBPort ) {
                        
                        processorDPort = port;
                        
                    } else if( port instanceof FifoWritePort ) {
                        
                        ctrlPorts.add( port );
                        
                    } else if( port instanceof FifoReadPort ) {
                        
                        ctrlPorts.add( port );
                        
                    } else if( port instanceof OPBPort ) {
                        
                        opbPort = port;
                        
                    } else if( port instanceof PLBPort && resource instanceof MicroBlaze ) {
                        
                        plbPort = port;
                    }
                    /*/
                     if( port instanceof IPLBPort || port instanceof ILMBPort ) {
                     
                     processorIPort = port;
                     
                     } else if( port instanceof DPLBPort || port instanceof DLMBPort ) {
                     
                     processorDPort = port;
                     
                     } else if( port instanceof FifoWritePort ) {
                     
                     ctrlPorts.add( port );
                     
                     } else if( port instanceof FifoReadPort ) {
                     
                     ctrlPorts.add( port );
                     
                     } else if( port instanceof OPBPort ) {
                     
                     opbPort = port;
                     }
                     //*/
                }
                
                // -----------------------------------------------------------------
                // Create a controller if there are fifos connected to the processor
                // -----------------------------------------------------------------
                if( ctrlPorts.size() > 0 ) {
                    
                    // Only the processor port remains in the processor
                    resource.getPortList().clear();
                    resource.getPortList().add( processorIPort );
                    resource.getPortList().add( processorDPort );
                    if( !opbPort.getName().equals("") ) {
                        resource.getPortList().add( opbPort );
                    }
                    if( !plbPort.getName().equals("") ) {
                        resource.getPortList().add( plbPort );
                    }
                    
                    // create the fifo controller
                    FifosController ctrl = new FifosController("CTRL_" + resource.getName() + "_FIFOs");
                    ctrl.setLevelUpResource( platform );
                    ctrl.getPortList().add( controllerPort );
                    ctrl.getPortList().addAll( ctrlPorts );
                    
                    // Connect the fifos controller to the processor data bus
                    Link link = processorDPort.getLink();
                    link.getPortList().add( controllerPort );
                    
                    controllerPort.setResource( ctrl );
                    controllerPort.setLink( link );
                    
                    // Re-direct the processor fifo ports (already removed from the processor) to be part of the fifos controller
                    p = ctrl.getPortList().iterator();
                    while( p.hasNext() ) {
                        
                        Port cPort = (Port) p.next();
                        cPort.setResource( ctrl );
                    }
                    
                    // Add the controller to the platform
                    _newResources.add( ctrl );
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static ElaborateMany2One _instance = new ElaborateMany2One();
    
    // Contains the new component (memories, fifos, ...) to be added to the platform
    private Vector _newResources = null;
}


