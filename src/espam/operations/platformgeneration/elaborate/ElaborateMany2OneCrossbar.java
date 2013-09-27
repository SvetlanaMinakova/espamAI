
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
import espam.datamodel.platform.ports.OPBPort;
import espam.datamodel.platform.ports.DLMBPort;
import espam.datamodel.platform.ports.DPLBPort;
import espam.datamodel.platform.ports.ILMBPort;
import espam.datamodel.platform.ports.IPLBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.MultiFifoController;
import espam.datamodel.platform.controllers.FifosController;
import espam.datamodel.platform.controllers.ReadCrossbarController;

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
 *  This class elaborates a platform in 'many-to-one' manner with a crossbar communication component:
 *
 * - The platform specification contains the processing components and a communication network component.
 *   There is 0 or 1 HW component, links are specified.
 *
 * - A mapping is specified - several processes can be mapped onto 1 processor. If several HW IPs are mapped onto
 *   the HW component, in the elaborated platform a HW component will be generated per IP core.
 *
 *  The elaborated platform contains crossbar, processors, processor memories, hardwared components and fifos.
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class ElaborateMany2OneCrossbar {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static ElaborateMany2OneCrossbar getInstance() {
        return _instance;
    }
    
    /****************************************************************************************************************
      *  elaborate a platform with many-to-one mapping with crossbar communication network component
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    public void elaborate( Platform platform, Mapping mapping ) {
        
        // -----------------------------------------------------------------------------------------------------------
        // HW Nodes, memories, fifos, virtual buffers are added to this vector.
        // This vector is needed because we can not add elements to the resource list until we use the iterator of it
        // -----------------------------------------------------------------------------------------------------------
        _newResources = new Vector();
        
        // -----------------------------------------------------------------------------
        // Add HW Nodes and links accoridng to the process network topology and mapping.
        // FIFOs are inserted between each in-out ports couple of Compaan hw nodes,
        // and add ports to the crossbar if needed.
        // -----------------------------------------------------------------------------
        _elaborateHWNodes( platform, mapping );
        
        // ----------------------------------------------------------------------------------------------------------
        // Current strategy: ZBT memory is not attached to any processor,
        // BRAMs are attached to the MicroBlazes and PPC: one memory component per program and data memory.
        // The crossbar ports are set to be of type FifoReadPort
        // Virtual Buffers are inserted between each processor and the crossbar
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
      *  Elaborates the HW IP cores
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _elaborateHWNodes( Platform platform, Mapping mapping ) {
        
        // Contains the correspondence between CDGates and HW Ports
        Hashtable hashTable = new Hashtable();
        
        // Contains the HW nodes that are already added to the platform
        Hashtable hashNodes = new Hashtable();
        
        // Used in names creation
        int cntHWN = 1;
        int cntInPorts = 1;
        int cntOutPorts = 1;
        int cntCS = 1;
        
        CDProcessNetwork cdpn = mapping.getCDPN();
        
        // ------------------------------------------------------
        // Create the map between process gates and HW node ports
        // ------------------------------------------------------
        Iterator i = mapping.getProcessorList().iterator();
        while( i.hasNext() ) {
            
            MProcessor mProcessor = (MProcessor) i.next();
            
            if( mProcessor.getResource() instanceof CompaanHWNode ) {
                
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
                    // Make a connection between the input gate and HW node input port
                    hashTable.put( gate, inPort );
                }
                
                Iterator outG = cdPrcs.getOutGates().iterator();
                while( outG.hasNext() ) {
                    
                    CDOutGate gate = (CDOutGate) outG.next();
                    
                    CompaanOutPort outPort = new CompaanOutPort( "Out_" + cntOutPorts++ );
                    // Make a connection between the output gate and HW node output port
                    hashTable.put( gate, outPort );
                }
            }
        }
        
        //Remove the hwNode from the initial platform and mapping (new HWNodes to be added)
        _removeHWNode( platform, mapping );
        
        // ---------------------------------------------------------------------------------------------------
        // Create hardware nodes and links between the resources according to the process network and mapping.
        // Add the created hardware nodes and links to the platform
        // ---------------------------------------------------------------------------------------------------
        Iterator c = cdpn.getChannelList().iterator();
        while( c.hasNext() ) {
            
            ADGNode adgNode;
            CompaanHWNode hwNode;
            
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
            if( inPort != null ) {
                
                adgNode = (ADGNode) ((ADGPort) toGate.getAdgPortList().get(0)).getNode();
                hwNode = (CompaanHWNode) hashNodes.get( adgNode );
                
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
            
            if( outPort != null ) {
                
                adgNode = (ADGNode) ((ADGPort) fromGate.getAdgPortList().get(0)).getNode();
                hwNode = (CompaanHWNode) hashNodes.get( adgNode );
                
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
            
            // -----------------------------
            // Connection between 2 HW nodes
            // -----------------------------
            if( inPort != null && outPort != null) {
                
                Link link = new Link("BUS_" + outPort.getResource().getName() + "_" + outPort.getName());
                outPort.setLink( link );
                inPort.setLink( link );
                
                // --------------------------------------------------
                // Set link and add it to the platform. Insert a fifo
                // --------------------------------------------------
                link.getPortList().add( outPort );
                platform.getLinkList().add( link );
                
                Fifo fifo = new Fifo( "FIFO_" + outPort.getResource().getName() + "_" + outPort.getName() );
                _addFifo( platform, outPort, inPort, fifo );
                
                // ----------------------------------------------------------------------------------------
                // Create and add a MFifo to the mapping. MFifo points to a Fifo and CDChannel mapped on it
                // ----------------------------------------------------------------------------------------
                _addMFifo( fifo, channel, mapping );
            }
            
            
            // --------------------------------------------------
            // Connection between HW node (out) and crossbar (in)
            // --------------------------------------------------
            if( inPort == null && outPort != null) {
                
                Link link = new Link("BUS_" + outPort.getResource().getName() + "_" + outPort.getName());
                outPort.setLink( link );
                
                // -------------------------------------------------------------
                // the corresponding port belongs to a node mapped to a processor
                // The connection is through the crossbar
                // -------------------------------------------------------------
                Crossbar crossbar = getCrossbar( platform );
                
                inPort = new FifoReadPort("xCS_IO_" + cntCS++);
                inPort.setLink( link );
                inPort.setResource( crossbar );
                
                crossbar.getPortList().add( inPort );
                
                // --------------------------------------------------
                // Set link and add it to the platform. Insert a fifo
                // --------------------------------------------------
                link.getPortList().add( outPort );
                platform.getLinkList().add( link );
                
                Fifo fifo = new Fifo( "FIFO_" + outPort.getResource().getName() + "_" + outPort.getName() );
                _addFifo( platform, outPort, inPort, fifo );
                
                // ----------------------------------------------------------------------------------------
                // Create and add a MFifo to the mapping. MFifo points to a Fifo and CDChannel mapped on it
                // ----------------------------------------------------------------------------------------
                _addMFifo( fifo, channel, mapping );
            }
            
            // -----------------------------------------------------------------------------------
            // Till now all the connections between Compaan Hardware Nodes are done
            // Compaan output ports to crossbar as well
            // Remain only the links between the crossbar ports and compaan input ports to be done
            // -----------------------------------------------------------------------------------
            // Connection between HW node (in) and crossbar (out). No fifos are added
            // -----------------------------------------------------------------------------------
            if( inPort != null && outPort == null) {
                
                Link link = new Link("BUS_" + inPort.getResource().getName() + "_" + inPort.getName());
                inPort.setLink( link );
                
                Crossbar crossbar = getCrossbar( platform );
                
                outPort = new FifoReadPort("xCS_IO_" + cntCS++);
                outPort.setLink( link );
                outPort.setResource( crossbar );
                
                crossbar.getPortList().add( outPort );
                
                // -----------------------------------------------
                //  Initialize the link and add it to the platform
                // -----------------------------------------------
                //link.getPortList().add(  inPort );
                link.getPortList().add( outPort );
                
                platform.getLinkList().add( link );
                
                // ----------------------------
                // Add crossbar read controller
                // ----------------------------
                ReadCrossbarController cbCtrl = new ReadCrossbarController("CTRL_" + outPort.getName());
                _addController( platform, outPort, inPort, cbCtrl );
            }
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
        //fifo.setSize( 512 ); // ???
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
      *  Elaborate a Processor component.
      *   Current strategy: ZBT memory is not attached to any of the processors,
      * BRAMs are attached to the MicroBlazes and PPC: one memory component per instruction and data bus
      * Virtual Buffers are inserted between each processor data bus and the crossbar
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _elaborateProcessors( Platform platform, Mapping mapping ) {
        
        Iterator i = platform.getResourceList().iterator();
        while( i.hasNext() ) {
            
            Resource resource = (Resource) i.next();
            
            if( resource instanceof Processor ) {
                
                // --------------------------------------------------------------------------------------------
                // When a mapping is specified, each processor has one port which may not have type
                // The port is set to be of the correct processor port type
                // --------------------------------------------------------------------------------------------
                //Port port = (Port) resource.getPortList().get(0);
                Port port = _getProcessorPort( resource );
                Link link = port.getLink();
                
                // -----------------------------------------------------------------------------
                // Make the initial link a data bus link (New Program bus link is created later)
                // -----------------------------------------------------------------------------
                link.setName( "DBUS_" + resource.getName() ); // Make it a data bus link
                
                Port port1 = (Port)link.getPortList().get(0);
                Port port2 = (Port)link.getPortList().get(1);
                
                Crossbar crossbar = getCrossbar( platform );
                
                // -----------------------------------------------------------------------------------------------------
                // Each port of the crossbar must be of type FifoReadPort (Normally, not specified in the platform file)
                // -----------------------------------------------------------------------------------------------------
                FifoReadPort crossbarPort = new FifoReadPort( port.getName() );
                if( port1.getResource() instanceof Processor ) {
                    
                    crossbarPort = new FifoReadPort( port2.getName() );
                    
                    // -------------------------------------------------------------------
                    // Break the connection. New connection is made in _addVirtualBuffer()
                    // -------------------------------------------------------------------
                    crossbar.getPortList().remove( port2 );
                    link.getPortList().remove( port2 );
                    
                } else {
                    
                    crossbarPort = new FifoReadPort( port1.getName() );
                    
                    // -------------------------------------------------------------------
                    // Break the connection. New connection is made in _addVirtualBuffer()
                    // -------------------------------------------------------------------
                    crossbar.getPortList().remove( port1 );
                    link.getPortList().remove( port1 );
                }
                
                // ----------------------------------------------------
                // Set the new crossbar port and add it to the crossbar
                // ----------------------------------------------------
                crossbarPort.setLink( link );
                crossbarPort.setResource( crossbar );
                
                crossbar.getPortList().add( crossbarPort );
                
                // ---------------------------------------------------------
                // The instruction and data processor ports are created here
                // ---------------------------------------------------------
                Port processorIPort;
                Port processorDPort;
                
                if( resource instanceof MicroBlaze ) {
                    processorIPort = new ILMBPort( "ILMB" );
                    processorDPort = new DLMBPort( "DLMB" );
                } else {
                    processorIPort = new IPLBPort( "IPLB" );
                    processorDPort = new DPLBPort( "DPLB" );
                }
                
                processorDPort.setLink( link );
                
                processorDPort.setResource( resource );
                resource.getPortList().add( processorDPort );
                
                // Remove the original processor port
                resource.getPortList().remove( port );
                
                //----------------------------------------------------------------------------------------
                // We have to substitute the port in the original link with the new processor port as well
                // ---------------------------------------------------------------------------------------
                link.getPortList().remove( port );
                link.getPortList().add( processorDPort );
                
                // --------------------------------------------------------------------------
                // Create a Data Memory component of the processor and add it to the platform
                // --------------------------------------------------------------------------
                //Memory memory = new Memory( "DM_" + resource.getName() );
                //memory.setSize( ((Processor) resource).getDataMemSize() );
                //memory.setDataWidth( 32 );
                //memory.setLevelUpResource( platform );
                //_addMemory( platform, processorDPort, memory );
                
                // --------------------------------------------
                // Set the Program memory side of the processor
                // --------------------------------------------
                processorIPort.setResource( resource );
                resource.getPortList().add( processorIPort );
                
                Link pLink = processorIPort.getLink();
                pLink.setName( "PBUS_" + resource.getName() );
                pLink.getPortList().add( processorIPort );
                
                platform.getLinkList().add( pLink );
                
                // -----------------------------------------------------------------------------
                // Create a Program Memory component of the processor and add it to the platform
                // -----------------------------------------------------------------------------
                //memory = new Memory( "PM_" + resource.getName() );
                //memory.setSize( ((Processor) resource).getProgMemSize() );
                //memory.setDataWidth( 32 );
                //memory.setLevelUpResource( platform );
                //_addMemory( platform, processorIPort, memory );
                
                // -------------------------------------------------------------------------------------------------
                // Create a Memory component of the processor (for both program and data) and add it to the platform
                // -------------------------------------------------------------------------------------------------
                Memory memory = new Memory( "M_" + resource.getName() );
                memory.setSize( ((Processor) resource).getDataMemSize() + ((Processor) resource).getProgMemSize() );
                memory.setDataWidth( 32 );
                memory.setLevelUpResource( platform );
                _addMemory2( platform, processorDPort, processorIPort, memory );
                
                // ------------------------------------------------------------------------
                // Add and initialize a virtual buffer between a processor and the crossbar
                // ------------------------------------------------------------------------
                _addVirtualBuffer( platform, processorDPort, crossbarPort, mapping );
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
      *  Adds a Virtual Buffer component between a processor and crossbar
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _addVirtualBuffer( Platform platform, Port processorPort, Port crossbarPort, Mapping mapping ) {
        
        // -----------------------------------------
        // The link contains only the processor port
        // -----------------------------------------
        Link link = processorPort.getLink();
        
        // ----------------------------------------------------------------------------------------------
        // Add a virtual buffer between a processor and the crossbar
        // ----------------------------------------------------------------------------------------------
        MultiFifo virtBuffer = new MultiFifo( "VB_" + processorPort.getResource().getName() );
        
        virtBuffer.setDataWidth( 32 );
        virtBuffer.setLevelUpResource( platform );
        
        // ---------------------------------------------------------------------------------------------------
        // Instantiate the proper processor port type of the virtual buffer to be connected with the processor
        // ---------------------------------------------------------------------------------------------------
        Port processorSide = new Port("");
        if( processorPort.getResource() instanceof MicroBlaze ) {
            processorSide = new LMBPort( "IO_1" );
        } else if( processorPort.getResource() instanceof PowerPC ) {
            processorSide = new PLBPort( "IO_1" );
        }
        
        // -------------------------------------------------------------------------
        // Connect the processor port with the virtual buffer (use the current link)
        // -------------------------------------------------------------------------
        processorSide.setResource( virtBuffer );
        
        // -----------------------------------------------------------------------------------
        // Don't set the link because we add a controller between the processor and the memory
        // -----------------------------------------------------------------------------------
        //processorSide.setLink( link );
        //link.getPortList().add( processorSide );
        
        // ---------------------------------------------------------------------
        // Connect the crossbar port with the virtual buffer (create a new link)
        // ---------------------------------------------------------------------
        FifoReadPort crossbarSide = new FifoReadPort( "IO_2" );
        crossbarSide.setResource( virtBuffer );
        
        Link cbLink = new Link( "BUS_" + virtBuffer.getName() );
        cbLink.getPortList().add( crossbarPort );
        cbLink.getPortList().add( crossbarSide );
        
        crossbarPort.setLink( cbLink );
        crossbarSide.setLink( cbLink );
        
        // ---------------------------------------
        // Add the new ports to the virtual buffer
        // ---------------------------------------
        virtBuffer.getPortList().add( processorSide );
        virtBuffer.getPortList().add( crossbarSide  );
        
        // ------------------------------------------------------------------
        // Initialize the virtual buffer; Add as many fifo channels as needed
        // ------------------------------------------------------------------
        _initVirtualBuffer( virtBuffer, processorPort.getResource(), mapping );
        
        // -----------------------------
        // Add virtual buffer controller
        // -----------------------------
        MultiFifoController vbCtrl = new MultiFifoController("CTRL_" + virtBuffer.getName());
        _addController( platform, processorPort, processorSide, vbCtrl );
        
        // Add the memory component to the platform
        _newResources.add( virtBuffer );
        
        // Add the new created link to the platform
        platform.getLinkList().add( cbLink );
    }
    
    /****************************************************************************************************************
      *  Add Fifos to a virtual Buffer Component according to the pn and the specified mapping
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _initVirtualBuffer( MultiFifo virtBuffer, Resource processor, Mapping mapping ) {
        
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
                    // fifo.setSize( 512 );
                    fifo.setDataWidth( 32 ); // ???
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
                Link resLink = ((Port) res.getPortList().get(0)).getLink();
                platform.getLinkList().remove( resLink );
                
                // -------------------------------------------------------------------------------
                // Remove the port from the corresponding resource (Must be Crossbar component)
                // Instead of checking if the component is a crossbar, simply remove the port from
                // both of the components connected to the link
                // -------------------------------------------------------------------------------
                res = ((Port) resLink.getPortList().get(0)).getResource();
                Port p = (Port) resLink.getPortList().get(0);
                res.getPortList().remove( p );
                
                res = ((Port) resLink.getPortList().get(1)).getResource();
                p = (Port) resLink.getPortList().get(1);
                res.getPortList().remove( p );
                
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
      *  Finding the crossbar component of the platform
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private Crossbar getCrossbar( Platform platform ) {
        
        Iterator r = platform.getResourceList().iterator();
        while( r.hasNext() ) {
            Resource resource = (Resource) r.next();
            if( resource instanceof Crossbar ) {
                return (Crossbar) resource;
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
            MFifo mFifo = new MFifo( fifo.getName());
            mFifo.setChannel( channel );
            mFifo.setFifo( fifo );
            mapping.getFifoList().add( mFifo );
        }
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
    private void _addController( Platform platform, Port processorPort, Port memoryPort, Controller controller ) {
        
        // -----------------------------------------
        // The link contains only the processor port
        // -----------------------------------------
        Link link = processorPort.getLink();
        
        controller.setLevelUpResource( platform );
        
        Port processorSide;
        Port memorySide;
        
//  if( resource instanceof PowerPC && processorPort instanceof PLBPort ) {
        
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
      *  Find the port to be connected to the crossbar. In platform specification it can be of type "LMBPort"
      *  or the general "Port". 
      *  OPBPort and PLBPort cannot be connected to a crossbar, so we check not to return OPBPort or PLBPort
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private Port _getProcessorPort( Resource resource ) {
        
        Iterator i = resource.getPortList().iterator();
        while( i.hasNext() ) {
            
            Port port = (Port) i.next();
            if( !(port instanceof OPBPort) && !(port instanceof PLBPort) ) {
                return port;
            }
        }
        return null; // Should never happen
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static ElaborateMany2OneCrossbar _instance = new ElaborateMany2OneCrossbar();
    
    // Contains the new component (memories, fifos, ...) to be added to the platform
    private Vector _newResources = null;
}


