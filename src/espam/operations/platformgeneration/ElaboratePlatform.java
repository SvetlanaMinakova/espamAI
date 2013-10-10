
package espam.operations.platformgeneration;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.ARM;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.communication.AXICrossbar;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.Fifo;

import espam.datamodel.mapping.Mapping;

import espam.operations.ConsistencyCheck;
import espam.operations.platformgeneration.elaborate.ElaborateMany2OneCrossbarAXI;
import espam.operations.platformgeneration.elaborate.ElaborateMany2OneCrossbar;
import espam.operations.platformgeneration.elaborate.ElaborateMany2One;
import espam.operations.platformgeneration.elaborate.ElaborateOne2One;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class ...
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class ElaboratePlatform {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static ElaboratePlatform getInstance() {
        return _instance;
    }
    
    /**
     *  This class generates an elaborated platform
     *
     * @param  platform Description of the Parameter
     * @exception  EspamException MyException If such and such occurs
     */
    public void elaboratePlatform(Platform platform, Mapping mapping) throws EspamException {
        
        System.out.println(" -- Elaborate platform ... ");
        
        try {
            if( ConsistencyCheck.getInstance().getMapProcessesOne2OneFlag() == true &&
               ConsistencyCheck.getInstance().getMapChannelsOne2OneFlag()  == true ) {
                // ---------------------------------------------------------------------
                // One-to-one mapping with MicroBlaze processors only or
                // PowerPC processors only or CompaanHWNodes only.
                // Connections point-to-point according to the process network topology.
                // ---------------------------------------------------------------------
                
                ElaborateOne2One.getInstance().elaborate( platform, mapping );
                System.out.println(" -- Elaboration ONE2ONE");
                
            } else if( ConsistencyCheck.getInstance().getMapProcessesOne2OneFlag() == false &&
                      ConsistencyCheck.getInstance().getMapChannelsOne2OneFlag()  == true ) {
                // ---------------------------------------------------------------------
                // Many-to-one mapping with several processing components specified.
                // Connections point-to-point according to the process network topology.
                // ---------------------------------------------------------------------
                
                ElaborateMany2One.getInstance().elaborate( platform, mapping );
                System.out.println(" -- Elaboration MANY2ONE");
                
            } else if( ConsistencyCheck.getInstance().getMapProcessesOne2OneFlag() == false &&
                      ConsistencyCheck.getInstance().getMapChannelsOne2OneFlag()  == false ) {
                // ---------------------------------------------------------------------
                // Many-to-one mapping with several processing components specified.
                // Connections through a communication network component.
                // ---------------------------------------------------------------------
                
                // find the type of the communication network component
                
                if( _getAxiCrossbar( platform ) ) { 
                    ElaborateMany2OneCrossbarAXI.getInstance().elaborate( platform, mapping );
                    System.out.println(" -- Elaboration MANY2ONE AXI Crossbar");
                                     
                } else {
                    ElaborateMany2OneCrossbar.getInstance().elaborate( platform, mapping );
                    System.out.println(" -- Elaboration MANY2ONE Crossbar");
                }
            }
            
            System.out.println(" -- Elaboration [Done]");
            
        } catch( Exception e ) {
            e.printStackTrace();
            System.out.println("\nElaboratePlatform Exception: " + e.getMessage());
        }
    }
    
    
    private boolean _getAxiCrossbar( Platform platform ) {
        
        boolean tmp=false;
        Iterator i = platform.getResourceList().iterator();
        while( i.hasNext() ) {
            
            Resource resource = (Resource) i.next();
            
            if( resource instanceof AXICrossbar ) {
                tmp = true;
            }
        }
        return tmp;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static ElaboratePlatform _instance = new ElaboratePlatform();
}


