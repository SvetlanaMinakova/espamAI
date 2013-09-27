
package espam.operations.consistencychecks;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.Mapping;

import espam.datamodel.EspamException;

/**
 *  This class checks the mapping specification for consistency.
 *
 * @author  Todor Stefanov
 * @version  $Id: CheckMapping.java,v 1.15 2002/10/08 14:23:14 stefanov Exp
 *      $
 */
public class CheckMapping {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Return the singleton instance of this class;
     *
     * @return  the instance.
     */
    public final static CheckMapping getInstance() {
        return _instance;
    }
    
    /**
     *  Check if the mapping spec is correct
     *
     * @param  platform Description of the Parameter
     * @exception  EspamException MyException If such and such occurs
     */
    public void checkMapping(Mapping mapping) throws EspamException {
        
        System.out.println(" -- Checking mapping ... ");
        
        try {
            
            // Check for unique name of each processor
            _checkProcessorNames( mapping );
            
            
            // Check for unique name of each process
            _checkProcessNames( mapping );
            
            
            System.out.println(" -- Check [Done]");
            
        } catch( Exception e ) {
            e.printStackTrace();
            System.out.println("\nCheckMapping Exception: " + e.getMessage());
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /**
     *  Check the processor names for uniqueness
     *
     * @param  mapping Description of the Parameter
     */
    private void _checkProcessorNames( Mapping mapping ) {
        
        Iterator i = mapping.getProcessorList().iterator();
        while( i.hasNext() ) {
            MProcessor curProcessor = (MProcessor) i.next();
            boolean isFirst = true;
            
            Iterator j = mapping.getProcessorList().iterator();
            while( j.hasNext() ) {
                MProcessor tempProcessor = (MProcessor) j.next();
                if( curProcessor.getName().equals(tempProcessor.getName()) ) {
                    if (isFirst == true) {
                        isFirst = false;
                    } else {
                        System.err.println("[Espam]ERROR: Processor \"" + curProcessor.getName() + "\" is specified more than once." );
                        System.err.println();
                        System.err.println( " -- Mapping specification check failed." );
                        System.err.println();
                        System.exit(0);
                    }
                }
            }
        }
        
    }
    
    /**
     *  Check the process names for uniqueness
     *
     * @param  mapping Description of the Parameter
     */
    private void _checkProcessNames( Mapping mapping ) {
        
        Iterator i = mapping.getProcessorList().iterator();
        while( i.hasNext() ) {
            MProcessor p = (MProcessor) i.next();
            Iterator j = p.getProcessList().iterator();
            while( j.hasNext() ) {
                MProcess curProcess = (MProcess) j.next();
                boolean isFirst = true;
                
                Iterator ii = mapping.getProcessorList().iterator();
                while( ii.hasNext() ) {
                    MProcessor pp = (MProcessor) ii.next();
                    Iterator jj = pp.getProcessList().iterator();
                    while( jj.hasNext() ) {
                        MProcess tempProcess = (MProcess) jj.next();
                        if( curProcess.getName().equals(tempProcess.getName()) ) {
                            if(isFirst == true) {
                                isFirst = false;
                            } else {
                                if( p.getName().equals(pp.getName()) ) {
                                    System.err.println("[Espam]ERROR: Process \"" + curProcess.getName() + "\" is mapped more than once " +
                                                       "onto Processor: \"" + p.getName() + "\"." );
                                } else {
                                    System.err.println("[Espam]ERROR: Process \"" + curProcess.getName() + "\" is mapped onto " +
                                                       "many Processors: \"" + p.getName() + "\" and \"" + pp.getName() + "\"." );
                                }
                                System.err.println();
                                System.err.println( " -- Mapping specification check failed." );
                                System.err.println();
                                System.exit(0);
                            }
                        }
                    }
                }
                
            }
        }
        
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static CheckMapping _instance = new CheckMapping();
    
}

