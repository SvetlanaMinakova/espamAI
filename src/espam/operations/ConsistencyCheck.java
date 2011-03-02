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

package espam.operations;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.platform.Platform;
import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.mapping.Mapping;

import espam.operations.consistencychecks.CheckPlatform;
import espam.operations.consistencychecks.CheckMapping;
import espam.operations.consistencychecks.CrossCheck;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class checks the consistency of a platform specification,
 *  a process network specification, and a mapping specification. Also,
 *  a cross-check is done among all specifications.
 *
 * @author  Todor Stefanov
 * @version  $Id: ConsistencyCheck.java,v 1.15 2002/10/08 14:23:14 stefanov Exp
 *      $
 */
public class ConsistencyCheck {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static ConsistencyCheck getInstance() {
		return _instance;
	}

	/**
	 *  This class generates an elaborated platform
	 *
	 * @param  platform Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void consistencyCheck(Platform platform, ADGraph adg, Mapping mapping) throws EspamException {

		System.out.println(" - Check platform, adg, and mapping for consistency");

		try {
		        mapping.setPlatform( platform );
			mapping.setADG( adg );
			mapping.setCDPN( new CDProcessNetwork("") );


			if( adg.getNodeList().size() == 0 && adg.getEdgeList().size() == 0 ) {
				System.out.println(" WARNING> Empty process network spec file. Process network will not be synthesized.");
				_emptyNetwork = true;
			}

			if( platform.getResourceList().size() == 0 && platform.getLinkList().size() == 0 ) {
				System.out.println(" WARNING> Empty platform spec file. Platform will not be synthesized.");
				_emptyPlatform = true;
			}

			if( mapping.getProcessorList().size() == 0 ) {
				System.out.println(" WARNING> Empty mapping spec file.");
                                _emptyMapping = true;
			}

			if( (_emptyNetwork == true) && (_emptyPlatform == true) && (_emptyMapping = true) ) {
				System.out.println(" ERROR> No system specification given.\n");
				System.out.println(" - Consistency check failed.\n");
				System.exit(0);
			}


                        if( _emptyNetwork == false ) {
		                System.out.println(" -- Checking network ... ");
				// Currently NO check is done here !!!
                                System.out.println(" -- Check [Done]");
			}

                        if( _emptyPlatform == false ) {
				CheckPlatform.getInstance().checkPlatform( platform );
			}

			if( _emptyMapping == false ) {
				CheckMapping.getInstance().checkMapping( mapping );
				CrossCheck.getInstance().crossCheck( platform, adg, mapping );
			}

			System.out.println(" - Consistency Check [Done]");
			System.out.println();

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nConsistencyCheck Exception: " + e.getMessage());
		}
	}


       /**
        * Get the status of the emptyPlatform flag
        *
        * @return The emptyPlatform value
        */
        public final boolean getEmptyPlatformFlag() {
           return _emptyPlatform;
        }

        /**
         * Sets the emptyPlatform flag
         */
        public final void setEmptyPlatformFlag() {
           _emptyPlatform = true;
        }


       /**
        * Get the status of the emptyNetwork flag
        *
        * @return The emptyNetwork value
        */
        public final boolean getEmptyNetworkFlag() {
           return _emptyNetwork;
        }

        /**
         * Sets the emptyNetwork flag
         */
        public final void setEmptyNetworkFlag() {
           _emptyNetwork = true;
        }

       /**
        * Get the status of the emptyMapping flag
        *
        * @return The emptyMapping value
        */
        public final boolean getEmptyMappingFlag() {
           return _emptyMapping;
        }

        /**
         * Sets the emptyMapping flag
         */
        public final void setEmptyMappingFlag() {
           _emptyMapping = true;
        }

       /**
        * Get the status of the mapProcessesOne2One flag
        *
        * @return The mapProcessesOne2One value
        */
        public final boolean getMapProcessesOne2OneFlag() {
           return _mapProcessesOne2One;
        }

        /**
         * Sets the mapProcessesOne2One flag
         */
        public final void setMapProcessesOne2OneFlag() {
           _mapProcessesOne2One = true;
        }


       /**
        * Get the status of the mapChannelsOne2One flag
        *
        * @return The mapChannelsOne2One value
        */
        public final boolean getMapChannelsOne2OneFlag() {
           return _mapChannelsOne2One;
        }

        /**
         * Sets the mapChannelsOne2One flag
         */
        public final void setMapChannelsOne2OneFlag() {
           _mapChannelsOne2One = true;
        }


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static ConsistencyCheck _instance = new ConsistencyCheck();

	/**
	 * Flag indicating whether the platform spec is empty.
	 * By default: NOT empty
	 */
	private boolean _emptyPlatform = false;

	/**
	 * Flag indicating whether the network spec is empty.
	 * By default: NOT empty
	 */
	private boolean _emptyNetwork = false;

	/**
	 * Flag indicating whether the mapping spec is empty.
	 * By default: NOT empty
	 */
	private boolean _emptyMapping = false;

	/**
	 * Flag indicating whether the processes in the network spec are mapped
	 * "One-to-One" onto the computational resources of the platform spec.
	 * By default: NOT "one-to-one" mapping
	 */
	private boolean _mapProcessesOne2One = false;

	/**
	 * Flag indicating whether the channels in the network spec are mapped
	 * "One-to-One" onto the communication resources of the platform spec.
	 * By default: NOT "one-to-one" mapping
	 */
	private boolean _mapChannelsOne2One = false;

}

