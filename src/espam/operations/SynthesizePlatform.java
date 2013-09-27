
package espam.operations;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.platform.Platform;

import espam.datamodel.mapping.Mapping;

import espam.operations.platformgeneration.ElaboratePlatform;
import espam.operations.platformgeneration.RefinePlatform;
import espam.operations.platformparameterset.SetPlatformParameters;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class processs the platform specified in xml format and generates new (elaborated) platform
 *
 * @author  Hristo Nikolov
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class SynthesizePlatform {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static SynthesizePlatform getInstance() {
        return _instance;
    }
    
    /**
     *  This class generates an elaborated platform
     *
     * @param  platform Description of the Parameter
     * @exception  EspamException MyException If such and such occurs
     */
    public void synthesizePlatform(Platform platform, Mapping mapping) throws EspamException {
        
        System.out.println(" - Synthesize platform ");
        
        try {
            ElaboratePlatform.getInstance().elaboratePlatform( platform, mapping );
            
            RefinePlatform.getInstance().refinePlatform( platform, mapping );
            
            SetPlatformParameters.getInstance().setPlatformParameters( mapping );
            
            System.out.println(" - Synthesis [Done]");
            System.out.println();
            
        } catch( Exception e ) {
            e.printStackTrace();
            System.out.println("\nElaboratePlatform Exception: " + e.getMessage());
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static SynthesizePlatform _instance = new SynthesizePlatform();
}

