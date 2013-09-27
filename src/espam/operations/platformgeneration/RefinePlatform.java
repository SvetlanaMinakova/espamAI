
package espam.operations.platformgeneration;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.platform.Platform;

import espam.datamodel.mapping.Mapping;

import espam.operations.platformgeneration.elaborate.RefineMemories;
import espam.operations.platformgeneration.elaborate.RefineCommunicationMB;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class ...
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class RefinePlatform {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static RefinePlatform getInstance() {
        return _instance;
    }
    
    /**
     *  This class generates a refined platform
     *
     * @param  platform Description of the Parameter
     * @exception  EspamException MyException If such and such occurs
     */
    public void refinePlatform( Platform platform, Mapping mapping ) throws EspamException {
        
        System.out.println(" -- Refinement platform ... ");
        
        try {
            
            // --------------------------------------------------------------------------
            // Insert additional memories and controllers in order to instantiate
            // the needed amoount of memory, exmpl: 37K = 32K + 4K + 2K (3 Memory blocks)
            // --------------------------------------------------------------------------
            
            RefineMemories.getInstance().refine( platform );
            
            // ----------------------------------------------------------
            // Refine the communication in MicroBlaze processors.
            // Utilizes the Fast Simplex Links (FSL) where it is possible
            // ----------------------------------------------------------
            
            RefineCommunicationMB.getInstance().refine( platform, mapping );
            
            
            System.out.println(" -- Refinement [Done]");
            
        } catch( Exception e ) {
            e.printStackTrace();
            System.out.println("\nRefinementPlatform Exception: " + e.getMessage());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static RefinePlatform _instance = new RefinePlatform();
}


