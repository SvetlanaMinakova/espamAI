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

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.pn.cdpn.CDProcessNetwork;

import espam.operations.ADGraphToPN;
import espam.operations.CDPNToParseTrees;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

/**
 *  This class creates the topology and the behavior of
 *  Compaan Dynamic Process Netowrk from Approximated Dependence Graph
 *
 *
 * @author  Todor Stefanov
 * @version  $Id: SynthesizePN.java,v 1.15 2002/10/08 14:23:14 stefanov Exp
 *      $
 */
public class SynthesizeCDPN {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static SynthesizeCDPN getInstance() {
		return _instance;
	}

	/**
	 *  This class synthesizes the Compaan Dynamic Process Network
	 *
	 * @param  adg Description of the Parameter
	 * @param  mapping Description of the Parameter
	 * @param  schedule Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public CDProcessNetwork synthesizeCDPN(ADGraph adg, Mapping mapping) throws EspamException {

		System.out.println(" - Synthesize Process Network ");
                CDProcessNetwork cdpnModel = null;

		try {
			// Convert ADG model to Compaan Dynamic Process Network model
			cdpnModel = ADGraphToPN.getInstance().adgraphToPN(adg, mapping);

			// Add parse trees to the CDPN model
			CDPNToParseTrees.getInstance().cdpnToParseTrees( cdpnModel );
			
			// Associate the CDPN model with the Mapping
			mapping.setCDPN( cdpnModel );

			System.out.println(" - Synthesis [Done]");
			System.out.println();

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nSynthesize PN Exception: " + e.getMessage());
		}

		return( cdpnModel );

	}


	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static SynthesizeCDPN _instance = new SynthesizeCDPN();
}

