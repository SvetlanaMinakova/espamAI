
package espam.operations.platformparameterset.memoryparameters;

import java.util.Iterator;
import java.util.Vector;
import java.lang.Integer;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.ARM;
import espam.datamodel.platform.processors.MemoryMap;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.IPLBPort;
import espam.datamodel.platform.ports.DPLBPort;
import espam.datamodel.platform.ports.ILMBPort;
import espam.datamodel.platform.ports.DLMBPort;
import espam.datamodel.platform.ports.OPBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.CM_CTRL;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.FifosController;
import espam.datamodel.platform.controllers.MultiFifoController;
import espam.datamodel.platform.peripherals.Peripheral;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.CM_AXI;

/**
 *  This class defines the memory map of each processor component in a platform.
 *
 *
 * @author  Todor Stefanov
 * @version  $Id: SetMemoryMaps.java,v 1.3 2012/04/02 16:25:40 nikolov Exp $
 *
 */
public class SetMemoryMaps {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static SetMemoryMaps getInstance() {
        return _instance;
    }
    
    /**
     *  This ...
     *
     * @param mapping Description of the Parameter
     * @exception  EspamException MyException If such and such occurs
     */
    public void setMemoryMaps( Mapping mapping ) {
        
        Platform platform = mapping.getPlatform();
        
        _generateMemoryMaps( platform );
        
        _initializeMemoryMaps( platform );
        
        // _printDebugInformation( platform );
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /****************************************************************************************************************
      *  Creates a memory map object for each port of a processor in a platform.
      *  If a port is FSL (MicroBlaze), the number of the channel is initialized as well
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _generateMemoryMaps( Platform platform ) {
        
        Iterator res = platform.getResourceList().iterator();
        while( res.hasNext() ) {
            
            Resource resource = (Resource) res.next();
            
            if( resource instanceof Processor ) {
                
                Processor processor = (Processor) resource;
                // Reset the channels' current number. In each MB processor FSL channels are up to 7
                _writeFSL = 0;
                _readFSL  = 0;
                
                Iterator p = processor.getPortList().iterator();
                while( p.hasNext() ) {
                    
                    Port port = (Port) p.next();
                    
                    if( port instanceof IPLBPort ) { // Program memory of a PPC processor
                        
                        MemoryMap programMemMap = new MemoryMap("IPLB Memory Map");
                        programMemMap.setPort( port );
                        programMemMap.setProgramMemorySegment( 0xffffffff ); // the reset vector of the PPC processor
                        processor.getMemoryMapList().add( programMemMap );
                        
                    } else if( port instanceof ILMBPort ) { // Program memory of a MB processor
                        
                        MemoryMap programMemMap = new MemoryMap("ILMB Memory Map");
                        programMemMap.setPort( port );
                        programMemMap.setProgramMemorySegment( 0x0 ); // the reset vector of the MB processor
                        processor.getMemoryMapList().add( programMemMap );
                        
                    } else if( port instanceof DPLBPort || (port instanceof OPBPort && processor instanceof PowerPC)) { // Data memory of a PPC processor
                        
                        MemoryMap dataMemMap = new MemoryMap("DPLB Memory Map");
                        dataMemMap.setPort( port );
                        dataMemMap.setDataMemorySegment(    0xffffffff );
                        //dataMemMap.setDataMemorySegment(    0x40000000 );
                        dataMemMap.setFifosReadSegment(     0x20800000 );
                        dataMemMap.setFifosWriteSegment(    0x20000000 );
                        dataMemMap.setVirtualBufferSegment( 0x10000000 );
                        dataMemMap.setPeripheralsSegment(   0x0 );
                        processor.getMemoryMapList().add( dataMemMap );
                        
                    } else if( processor instanceof MicroBlaze ) {
                        if(port instanceof DLMBPort || port instanceof OPBPort || port instanceof PLBPort) { // Data memory of a MicroBlaze processor
                            
                            MemoryMap dataMemMap = new MemoryMap("DLMB Memory Map");
                            dataMemMap.setPort( port );
                            dataMemMap.setDataMemorySegment(    0x0 );
                            //dataMemMap.setDataMemorySegment(    0x80000000 );
                            dataMemMap.setFifosReadSegment(     0xc0000000 );
                            dataMemMap.setFifosWriteSegment(    0xc0800000 );
                            dataMemMap.setVirtualBufferSegment( 0xe0000000 );
                            dataMemMap.setPeripheralsSegment(   0xf0000000 );
                            processor.getMemoryMapList().add( dataMemMap );
                            
                        } else if( port instanceof FifoReadPort || port instanceof FifoWritePort ) { // FSL channels of a MicroBlaze processor
                            
                            MemoryMap fslMemMap = new MemoryMap("FSL Memory Map");
                            fslMemMap.setPort( port );
                            _setFSLPage( fslMemMap );
                            processor.getMemoryMapList().add( fslMemMap );
                        }
                    } else if( processor instanceof ARM ) {
                        /*
                        MemoryMap programMemMap = new MemoryMap("Program Memory Map");
                        programMemMap.setPort( port );
                        programMemMap.setProgramMemorySegment( 0x0 ); 
                        processor.getMemoryMapList().add( programMemMap ); 
                        */
                        MemoryMap dataMemMap = new MemoryMap("Data Memory Map");
                        dataMemMap.setPort( port );
                        dataMemMap.setDataMemorySegment(    0x0 );
                        dataMemMap.setFifosReadSegment(     0xc0000000 );
                        dataMemMap.setFifosWriteSegment(    0xc0800000 );
                        dataMemMap.setVirtualBufferSegment( 0xe0000000 );
                        dataMemMap.setPeripheralsSegment(   0xf0000000 );
                        processor.getMemoryMapList().add( dataMemMap );
                    }
                }
            }
        }
    }
    
    /****************************************************************************************************************
      *  Assign unique number to an FSL channel.
      *  Assign a fifo to an FSL channel.
      *
      * @param  MemoryMap Description of the Parameter
      ****************************************************************************************************************/
    private void _setFSLPage( MemoryMap memoryMap ) {
        
        // For each FSL Channel generate one page. It contains the number of the channel (0-7 for read and 0-7 for write)
        Page fslPage = new Page();
        
        if( memoryMap.getPort() instanceof FifoReadPort ) {
            
            fslPage.setBaseAddress( _readFSL++ ); // the number of the read channel
            
        } else if( memoryMap.getPort() instanceof FifoWritePort ) {
            
            fslPage.setBaseAddress( _writeFSL++ ); // the number of the write channel
        }
        
        // We need to set the resource - the corresponding fifo
        Link link = memoryMap.getPort().getLink();
        
        Iterator p = link.getPortList().iterator();
        while( p.hasNext() ) {
            
            Port port = (Port) p.next();
            Resource resource = port.getResource();
            
            if( resource instanceof Fifo ) {
                
                if( port instanceof FifoReadPort ) {
                    
                    fslPage.setReadResource( resource );
                    
                } else if( port instanceof FifoWritePort ) {
                    
                    fslPage.setWriteResource( resource );
                }
                
                break;
            }
        }
        
        memoryMap.getPageList().add( fslPage );
    }
    
    /****************************************************************************************************************
      *  Initialize the memory maps: program/data memories, read/write fifos, and peripherals found in a platform
      *
      * @param  platform Description of the Parameter
      ****************************************************************************************************************/
    private void _initializeMemoryMaps( Platform platform ) {
        
        Iterator res = platform.getResourceList().iterator();
        while( res.hasNext() ) {
            
            Resource resource = (Resource) res.next();
            
            if( resource instanceof MemoryController ) {
                
                MemoryController controller = (MemoryController) resource;
                MemoryMap memMap = _getMemoryMap( controller );
                
                Page page = new Page();
                page.setReadResource( controller );
                page.setWriteResource( controller );
                page.setSize( controller.getSize() );
                
                if( memMap.getPort() instanceof DPLBPort || memMap.getPort() instanceof DLMBPort ) {
                    
                    page.setBaseAddress( memMap.getDataMemorySegment() );
                    
                    // -------------------------------------------------------------------------------------
                    // Here we modify the data segment base address of a memory map.
                    // We do this in order to know where the next (if any) block of memory has to start from
                    // -------------------------------------------------------------------------------------
                    memMap.setDataMemorySegment( memMap.getDataMemorySegment() + controller.getSize() ) ;
                    memMap.getPageList().add( page );
                    // ------------------------------------------------------------------------
                    // Each memory controller has ONLY one page created in the refinement stage
                    // Here we update the page information
                    // ------------------------------------------------------------------------
                    controller.getPageList().clear();
                    controller.getPageList().add( page );
                    
                } else if( memMap.getPort() instanceof ILMBPort ) {
                    
                    page.setBaseAddress( memMap.getProgramMemorySegment() );
                    
                    // -------------------------------------------------------------------------------------
                    // Here we modify the program segment base address of a memory map.
                    // We do this in order to know where the next (if any) block of memory has to start from
                    // -------------------------------------------------------------------------------------
                    memMap.setProgramMemorySegment( memMap.getProgramMemorySegment() + controller.getSize() ) ;
                    memMap.getPageList().add( page );
                    // ------------------------------------------------------------------------
                    // Each memory controller has ONLY one page created in the refinement stage
                    // Here we update the page information
                    // ------------------------------------------------------------------------
                    controller.getPageList().clear();
                    controller.getPageList().add( page );
                    
                } else if( memMap.getPort() instanceof IPLBPort ) {
                    
                    page.setBaseAddress( memMap.getProgramMemorySegment() - controller.getSize() );
                    
                    // -------------------------------------------------------------------------------------
                    // Here we modify the program segment base address of a memory map.
                    // We do this in order to know where the next (if any) block of memory has to start from
                    // -------------------------------------------------------------------------------------
                    memMap.setProgramMemorySegment( page.getBaseAddress() ) ;
                    memMap.getPageList().add( page );
                    // ------------------------------------------------------------------------
                    // Each memory controller has ONLY one page created in the refinement stage
                    // Here we update the page information
                    // ------------------------------------------------------------------------
                    controller.getPageList().clear();
                    controller.getPageList().add( page );
                }
                
            } else if( resource instanceof FifosController ) {
                
                FifosController controller = (FifosController) resource;
                MemoryMap memMap  = _getMemoryMap( controller );
                Vector readPorts  = controller.getFifoReadPorts();
                Vector writePorts = controller.getFifoWritePorts();
                
                Iterator rp = readPorts.iterator();
                while( rp.hasNext() ) {
                    
                    Port port = (Port) rp.next();
                    Fifo fifo = _getFifo( port );
                    
                    // For each FIFO a page is created
                    Page page = new Page();
                    page.setReadResource( fifo );
                    page.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                    page.setBaseAddress( memMap.getFifosReadSegment() );
                    // --------------------------------------------------------------------------
                    // Here we modify the read fifo segment base address of a memory map.
                    // We do this in order to know where the next (if any) fifo has to start from
                    // --------------------------------------------------------------------------
                    memMap.setFifosReadSegment( memMap.getFifosReadSegment() + 8 ) ;
                    memMap.getPageList().add( page );
                    // Add this page to the controller as well
                    controller.getPageList().add( page );
                }
                
                Iterator wp = writePorts.iterator();
                while( wp.hasNext() ) {
                    
                    Port port = (Port) wp.next();
                    Fifo fifo = _getFifo( port );
                    
                    // For each FIFO a page is created
                    Page page = new Page();
                    page.setWriteResource( fifo );
                    page.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                    page.setBaseAddress( memMap.getFifosWriteSegment() );
                    // --------------------------------------------------------------------------
                    // Here we modify the write fifo segment base address of a memory map.
                    // We do this in order to know where the next (if any) fifo has to start from
                    // --------------------------------------------------------------------------
                    memMap.setFifosWriteSegment( memMap.getFifosWriteSegment() + 8 ) ;
                    memMap.getPageList().add( page );
                    // Add this page to the controller as well
                    controller.getPageList().add( page );
                }
                
                
            } else if( resource instanceof MultiFifoController ) {
                
                MultiFifoController controller = (MultiFifoController) resource;
                MemoryMap memMap = _getMemoryMap( controller );
                _fifoNumber = 0; // In each virtual buffer the fifos are numbered starting from 0
                
                // Create a page which shows the read address from a virtual buffer
                Page page = new Page();
                page.setReadResource( controller );
                page.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                page.setBaseAddress( memMap.getVirtualBufferSegment() );
                
                // --------------------------------------------------------------------------
                // Here we modify the virtual buffer segment base address of a memory map.
                // We do this in order to know where the next WRITE fifo has to start from
                // --------------------------------------------------------------------------
                memMap.setVirtualBufferSegment( memMap.getVirtualBufferSegment() + 8 ) ;
                memMap.getPageList().add( page );
                // Add this page to the controller as well
                controller.getPageList().add( page );
                
                // -----------------------------------------------------------------------------
                // The fifos in a virtual buffer can be accessed for both read and write.
                // The fifos for read are specified:
                //        - the base address field shows the global read address of the fifo
                //        - size = 0
                // The SAME fifos for write a specified as we do in the case of FifosController
                // -----------------------------------------------------------------------------
                Vector fifoList = _getFifoList( controller );
                Iterator fl = fifoList.iterator();
                while( fl.hasNext() ) {
                    
                    Fifo fifo = (Fifo) fl.next();
                    
                    // set the write part
                    // For each FIFO a page is created
                    Page fifoPage = new Page();
                    fifoPage.setWriteResource( fifo );
                    fifoPage.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                    fifoPage.setBaseAddress( memMap.getVirtualBufferSegment() );
                    
                    // --------------------------------------------------------------------------
                    // Here we modify the virtual buffer segment base address of a memory map.
                    // We do this in order to know where the next WRITE fifo has to start from
                    // --------------------------------------------------------------------------
                    memMap.setVirtualBufferSegment( memMap.getVirtualBufferSegment() + 8 ) ;
                    memMap.getPageList().add( fifoPage );
                    // Add this page to the controller as well
                    controller.getPageList().add( fifoPage );
                    
                    // set the read part
                    // again, a page is created...
                    Page page1 = new Page();
                    page1.setReadResource( fifo );
                    page1.setSize( -1 ); // Because this is a fake page containing the global read address of a fifo
                    
                    // -----------------------------------------------------------------------
                    // The addresses must be unique. The address is 32 bits formed by 2 parts:
                    // < number of a crossbar port >< number of a fifo in a virtual buffer > :
                    // CB port 1, first fifo: 0x_0001_0000
                    // CB port 3, fifth fifo: 0x_0003_0004
                    // -----------------------------------------------------------------------
                    int cbPort = _crossbarPort<<16;
                    page1.setBaseAddress( cbPort + _fifoNumber );
                    memMap.getPageList().add( page1 );
                    
                    _fifoNumber++;
                }
                
                _crossbarPort++;
                
            } else if( resource instanceof CM_CTRL ) {
                
// We use FIFOs implemented in software. At this point we can not compute the start addresses of the FIFOs because we do not know 
// the 'actual' size of the fifos. The actual addresses are set in the memory map genaration in the MHS visitor.
// CM_CTRL is used to write to FIFOs and read from self channels
                
                CM_CTRL controller = (CM_CTRL) resource;
                MemoryMap memMap = _getMemoryMap( controller );
                _fifoNumber = 0; // In each virtual buffer the fifos are numbered starting from 0
                
                // Create a page which shows the read address from a virtual buffer
                Page page = new Page();
                page.setReadResource( controller );
                page.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                page.setBaseAddress( memMap.getVirtualBufferSegment() );
                
                // --------------------------------------------------------------------------
                // Here we DO NOT modify the virtual buffer segment base address of a memory map.
                // We compute the addresses for the Software FIFOs during memory map code generation
                // --------------------------------------------------------------------------
//       memMap.setVirtualBufferSegment( memMap.getVirtualBufferSegment() + 8 ) ;
                memMap.getPageList().add( page );
                // Add this page to the controller as well
                controller.getPageList().add( page );
                
                // -----------------------------------------------------------------------------
                // The fifos in a virtual buffer can be accessed for both read and write.
                // The fifos for read are specified:
                //        - the base address field shows the global read address of the fifo
                //        - size = 0
                // The SAME fifos for write a specified as we do in the case of FifosController
                // -----------------------------------------------------------------------------
                Vector fifoList = _getFifoListCM( controller );
                Iterator fl = fifoList.iterator();
                while( fl.hasNext() ) {
                    
                    Fifo fifo = (Fifo) fl.next();
                    
                    // set the write part
                    // For each FIFO a page is created
                    Page fifoPage = new Page();
                    fifoPage.setWriteResource( fifo );
                    fifoPage.setSize( 8 ); // A fifo occupies 2 memory locations (x32bits)
                    fifoPage.setBaseAddress( memMap.getVirtualBufferSegment() );
                    
                    // --------------------------------------------------------------------------
                    // Here we modify the virtual buffer segment base address of a memory map.
                    // We do this in order to know where the next WRITE fifo has to start from
                    // --------------------------------------------------------------------------
                    memMap.setVirtualBufferSegment( memMap.getVirtualBufferSegment() + 8 ) ;
                    memMap.getPageList().add( fifoPage );
                    // Add this page to the controller as well
                    controller.getPageList().add( fifoPage );
                    
                    // set the read part
                    // again, a page is created...
                    Page page1 = new Page();
                    page1.setReadResource( fifo );
                    page1.setSize( -1 ); // Because this is a fake page containing the global read address of a fifo
                    
                    // -----------------------------------------------------------------------
                    // The addresses must be unique. We use the following approach:
                    // CB port 1, first fifo: 0x_8001_0000 // Other fifos are offset in the memory map generation
                    // CB port 2, first fifo: 0x_8002_0000 // Other fifos are offset in the memory map generation
                    // CB port 3, first fifo: 0x_8003_0000 // Other fifos are offset in the memory map generation
                    // -----------------------------------------------------------------------
                    int cbPort = _crossbarPort<<16;
//   page1.setBaseAddress( cbPort + _fifoNumber );
                    page1.setBaseAddress( 0x80000000 + cbPort );
                    memMap.getPageList().add( page1 );
                    
//   _fifoNumber++;
                }
                
                _crossbarPort++;
                
            }  else if( resource instanceof Peripheral ) {
                
                Peripheral controller = (Peripheral) resource;
                MemoryMap memMap = _getMemoryMap( controller );
                
                // Create a page which describes the peripheral connected to this controller (ZBT Memory)
                Page page = new Page();
                page.setReadResource( controller );
                page.setWriteResource( controller );
                page.setSize( controller.getSize() );
                page.setBaseAddress( memMap.getPeripheralsSegment() );
                
                // --------------------------------------------------------------------------------
                // Here we modify the peripheral segment base address of a memory map.
                // We do this in order to know where the next (if any) peripheral has to start from
                // --------------------------------------------------------------------------------
                memMap.setPeripheralsSegment( memMap.getPeripheralsSegment() + _greaterPowerOfTwo(controller.getSize()) ) ;
                memMap.getPageList().add( page );
                // --------------------------------------------
                // Each peripheral controller has ONLY one page
                // Here we update the page information
                // --------------------------------------------
                controller.getPageList().clear();
                controller.getPageList().add( page );
            }
        }
    }
    
    /****************************************************************************************************************
      *  Find a memory map of a processor connected to a controller (memory, fifo, multififo) or peripheral
      *
      * @param  controller
      ****************************************************************************************************************/
    private MemoryMap _getMemoryMap( Resource controller ) {
        
        Iterator p = controller.getPortList().iterator();
        while( p.hasNext() ) {
            
            Port port = (Port) p.next();
            Link link = port.getLink();
            
            Iterator pp = link.getPortList().iterator();
            while( pp.hasNext() ) {
                
                Port port1 = (Port) pp.next();
                if( port1.getResource() instanceof Processor ) {
                    
                    Processor processor = (Processor) port1.getResource();
                    Iterator mm = processor.getMemoryMapList().iterator();
                    while( mm.hasNext() ) {
                        
                        MemoryMap memMap = (MemoryMap) mm.next();
                        
                        if( port1.getName().equals( memMap.getPort().getName() ) ) {
                            return memMap;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    
    /****************************************************************************************************************
      *  Find a fifo connected to a FifosController port
      *
      * @param  controller
      ****************************************************************************************************************/
    private Fifo _getFifo( Port port ) {
        
        Link link = port.getLink();
        
        Iterator p = link.getPortList().iterator();
        while( p.hasNext() ) {
            
            Port port1 = (Port) p.next();
            if( port1.getResource() instanceof Fifo ) {
                
                return (Fifo) port1.getResource();
            }
        }
        return null;
    }
    
    /****************************************************************************************************************
      *  Find the fifos in a virtual buffer
      *
      * @param  controller
      ****************************************************************************************************************/
    private Vector _getFifoList( MultiFifoController controller ) {
        
        Iterator p = controller.getPortList().iterator();
        while( p.hasNext() ) {
            
            Port port = (Port) p.next();
            Link link = port.getLink();
            
            Iterator pp = link.getPortList().iterator();
            while( pp.hasNext() ) {
                
                Port port1 = (Port) pp.next();
                if( port1.getResource() instanceof MultiFifo ) {
                    
                    MultiFifo vb = (MultiFifo) port1.getResource();
                    return vb.getFifoList();
                }
            }
        }
        return null;
    }
    
    private Vector _getFifoListCM( CM_CTRL controller ) {
        
        Iterator p = controller.getPortList().iterator();
        while( p.hasNext() ) {
            
            Port port = (Port) p.next();
            Link link = port.getLink();
            
            Iterator pp = link.getPortList().iterator();
            while( pp.hasNext() ) {
                
                Port port1 = (Port) pp.next();
                if( port1.getResource() instanceof CM_AXI ) {
                    
                    CM_AXI vb = (CM_AXI) port1.getResource();
                    return vb.getFifoList();
                }
            }
        }
        return null;
    }
    
    /****************************************************************************************************************
      *  Print debug information
      *
      * @param  controller
      ****************************************************************************************************************/
    private void _printDebugInformation( Platform platform ) {
        
        Iterator res = platform.getResourceList().iterator();
        while( res.hasNext() ) {
            
            Resource resource = (Resource) res.next();
            
            if( resource instanceof Controller ) {
                
                Controller ctrl = (Controller) resource;
                System.out.println("Controller:" + ctrl.getName());
                System.out.println("    - BaseAddress = 0x" + Integer.toHexString(ctrl.getBaseAddress()) );
                System.out.println("    - Size        = " + ctrl.getSize() );
                //System.out.println("    - Size        = 0x" + Integer.toHexString(ctrl.getSize()) );
                System.out.println();
            }
            
            if( resource instanceof Processor ) {
                
                Processor processor = (Processor) resource;
                System.out.println("Processor " + processor.getName() );
                //System.out.println( processor.getPortList() );
                //System.out.println( processor.getMemoryMapList() );
                Iterator mm = processor.getMemoryMapList().iterator();
                while( mm.hasNext() ) {
                    
                    MemoryMap memMap = (MemoryMap) mm.next();
                    System.out.println( memMap.getPort() );
                    System.out.println("    - " + memMap.getName());
                    System.out.println( memMap.getPageList() );
                }
            }
        }
    }
    
    /**
     *  convert integer value to nearest power of 2 number greater than this value
     *  @param xInt integer value to be convert
     *  @return  the nearest power of 2 number greater than xInt
     */
    private int _greaterPowerOfTwo(int xInt) {
        int i = 0;
        while ( Math.pow( 2.0, (double)i ) < xInt ) {
            i++;
        }
        return (int)Math.pow( 2.0, (double)i );
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static SetMemoryMaps _instance = new SetMemoryMaps();
    
    private static int _writeFSL = 0;
    private static int _readFSL  = 0;
    
    private static int _crossbarPort = 1;
    private static int _fifoNumber   = 0;
}


