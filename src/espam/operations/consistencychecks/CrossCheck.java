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

import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.Mapping;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;

import espam.operations.ConsistencyCheck;

import espam.datamodel.EspamException;

/**
 *  This class cross-checks the platform, the process network,
 *  and the mapping specifications for consistency.
 *
 * @author  Todor Stefanov
 * @version  $Id: CrossCheck.java,v 1.15 2002/10/08 14:23:14 stefanov Exp
 *      $
 */
public class CrossCheck {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

       /**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static CrossCheck getInstance() {
		return _instance;
	}

	/**
	 *  CrossCheck if the platform, network, and mapping specs are
	 *  consistent together
	 *
	 * @param  platform Description of the Parameter
	 * @param  adg Description of the Parameter
	 * @param  mapping Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void crossCheck(Platform platform, ADGraph adg, Mapping mapping) throws EspamException {

		System.out.println(" -- Cross-Checking platform, network, and mapping ... ");
                ConsistencyCheck cc =  ConsistencyCheck.getInstance();

		try {
                    if( cc.getEmptyPlatformFlag() == false ) {
                        // Cross-check processor names
			_checkProcessorNames( mapping, platform );
                    }

                    if( cc.getEmptyNetworkFlag() == false ) {
                        // Cross-check process names
			_checkProcessNames( mapping, adg );
                    }

		    System.out.println(" -- Check [Done]");

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nCrossChack Exception: " + e.getMessage());
		}
	}


	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/**
	 *
	 *  Check if a processor in the mapping spec has
         *  a corresponding processor/IPcore in the platform spec.
         *  The check is done by name comparison.
	 *
	 * @param  mapping Description of the Parameter
         * @param  platform Description of the Parameter
	 */
	private void _checkProcessorNames( Mapping mapping, Platform platform ) {

           Iterator i = mapping.getProcessorList().iterator();
           while( i.hasNext() ) {
              MProcessor mProcessor = (MProcessor) i.next();
              boolean isMatch = false;

              Iterator j = platform.getResourceList().iterator();
              while( j.hasNext() ) {
                 Resource resource = (Resource) j.next();
                 if( mProcessor.getName().equals(resource.getName()) ) {
		     mProcessor.setResource( resource );
                    isMatch = true;
		 }
              }

	      if (isMatch == false) {
	         System.err.println("[Espam]ERROR: Processor \"" + mProcessor.getName() + "\" in the mapping spec" +
		                    " does not have corresponding resourece in the platform spec." );
	         System.err.println();
	         System.err.println( " -- Cross-check failed." );
	         System.err.println();
	         System.exit(0);
	      }

           }

	}

	/**
	 *
	 * Check if a process in the mapping spec has
	 * a corresponding process in the network spec as well as
	 * if the number of processes in the mapping spec equals the
	 * number of processes in the network spec.
         * The check is done by name comparison.
	 *
	 * @param  mapping Description of the Parameter
         * @param  network Description of the Parameter
	 */
	private void _checkProcessNames( Mapping mapping, ADGraph adg ) {

           int numProcesses = 0;

           Iterator i = mapping.getProcessorList().iterator();
           while( i.hasNext() ) {
              MProcessor mProcessor = (MProcessor) i.next();
              numProcesses = numProcesses + mProcessor.getProcessList().size();

              Iterator j = mProcessor.getProcessList().iterator();
              while( j.hasNext() ) {
                 MProcess mProcess = (MProcess) j.next();
                 boolean isMatch = false;

                 Iterator k = adg.getNodeList().iterator();
                 while( k.hasNext() ) {
                    ADGNode node = (ADGNode) k.next();
                    if( mProcess.getName().equals(node.getName()) ) {
		       mProcess.setNode( node ); 
                       isMatch = true;
                    }
                 }

		 if (isMatch == false) {
                    System.err.println("[Espam]ERROR: Process \"" + mProcess.getName() + "\" in the mapping spec" +
                                       " does not have corresponding process in the network spec." );
                    System.err.println();
                    System.err.println( " -- Cross-check failed." );
                    System.err.println();
                    System.exit(0);
		 }

              }

           }

	   if( numProcesses != adg.getNodeList().size() ) {
              System.err.println("[Espam]ERROR: The number of processes in the mapping spec" +
                                 " is not equal to the number of processes in the network spec." );
              System.out.println();
              System.err.println( " -- Cross-check failed." );
              System.err.println();
              System.exit(0);
	   }

	}


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static CrossCheck _instance = new CrossCheck();

}

