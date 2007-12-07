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

package espam.operations.platformparameterset;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.mapping.Mapping;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

import espam.operations.platformparameterset.memoryparameters.SetFifoSizes;
import espam.operations.platformparameterset.memoryparameters.SetMemoryMaps;

/**
 *  This class calles a number of procedures that set values of parameters
 *  of the resources that comprise a platform, thereby defining a particular
 *  instance of the platform.
 *
 * @author  Todor Stefanov
 * @version  $Id: SetPlatformParameters.java,v 1.15 2002/10/08 14:23:14 stefanov Exp
 *      $
 */
public class SetPlatformParameters {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static SetPlatformParameters getInstance() {
		return _instance;
	}

	/**
	 *  This metod sets platform parameters
	 *
	 * @param  mapping Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void setPlatformParameters(Mapping mapping) throws EspamException {

		System.out.println(" -- Set platform parameters ... ");

		try {

			SetFifoSizes.getInstance().setFifoSizes( mapping );
			SetMemoryMaps.getInstance().setMemoryMaps( mapping );

			System.out.println(" -- Setting [Done]");

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nSetPlatformParameters Exception: " + e.getMessage());
		}
	}


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static SetPlatformParameters _instance = new SetPlatformParameters();
}


