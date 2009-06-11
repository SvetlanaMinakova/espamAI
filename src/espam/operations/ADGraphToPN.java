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
import java.util.HashMap;

import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGraph;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDProcessNetwork;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.MProcess;

import espam.operations.scheduler.Scheduler;

/**
 * This class ...
 *
 * @author Hristo Nikolov, Todor Stefanov, Joris Huizer
 * @version $Id: ADGraphToPN.java,v 1.2 2009/06/11 13:11:35 stefanov Exp $
 */

public class ADGraphToPN {

	///////////////////////////////////////////////////////////////////
	//// public methods ////

	/**
	 * Return the singleton instance of this class;
	 *
	 * @return the instance.
	 */
	public final static ADGraphToPN getInstance() {
		return _instance;
	}

	/**
	 * Transform the ADG Model into PN model according to the Mapping
	 *
	 * @param adg
	 *            Description of the Parameter
	 * @param mapping
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception Error
	 *                MyException If such and such occurs
	 */
	public CDProcessNetwork adgraphToPN( ADGraph adg, Mapping mapping ) throws Error {

		System.out.println(" -- Convert ADG into CDPN ...");

		CDProcessNetwork procNet = new CDProcessNetwork( adg.getName() );
		procNet.setAdg( adg );

		Vector adgEdgeList = adg.getEdgeList();

		_setProcessPN( adg, mapping, procNet );

		_setChannelPN( adgEdgeList, procNet );

		_setSchedule( mapping, procNet );

		System.out.println(" -- Conversion [Done]");

		return( procNet );
	}


	/**
	 * Create the processes of the PN
	 *
	 * @param adg
	 *            Description of the Parameter
	 * @param mapping
	 *            Description of the Parameter
	 * @param proNet
	 *            Description of the Parameter
	 */
	private void _setProcessPN( ADGraph adg, Mapping mapping, CDProcessNetwork procNet ) {

		Integer intgr = new Integer(1);
		int procCounter = 1;

	        if( mapping.getProcessorList().size() == 0 ) {

		   // one-to-one correspondence ( ADGNode <-> CDProcess )
                   Iterator i = adg.getNodeList().iterator();
                   while( i.hasNext() ) {
			ADGNode adgNode = (ADGNode) i.next();

  			CDProcess cdProcess = new CDProcess( "P_" + procCounter++ );
                        cdProcess.setLevelUpProcess( procNet );
                        cdProcess.getAdgNodeList().add( adgNode );

			procNet.getProcessList().add( cdProcess );

			// each HashMap contains No. of process elements. Each element is a counter of In/Out gates
			_prcssInGateCounters.put(cdProcess.getName(), intgr);
			_prcssOutGateCounters.put(cdProcess.getName(), intgr);
                   }

		} else {

		   // many-to-one correspondence ( many ADGNode <-> CDProcess )
                   Iterator i = mapping.getProcessorList().iterator();
                   while( i.hasNext() ) {
                       MProcessor mprocessor = (MProcessor) i.next();

                       CDProcess cdProcess = new CDProcess( "P_" + procCounter++ );
                       cdProcess.setLevelUpProcess( procNet );

		       Iterator j = mprocessor.getProcessList().iterator();
		       while( j.hasNext() ) {
		           MProcess mprocess = (MProcess) j.next();
                           ADGNode adgNode = (ADGNode) adg.getNode( mprocess.getName() );
                           cdProcess.getAdgNodeList().add( adgNode );
		       }

                       procNet.getProcessList().add( cdProcess );

                       // each HashMap contains No. of process elements. Each element is a counter of In/Out gates
                       _prcssInGateCounters.put(cdProcess.getName(), intgr);
                       _prcssOutGateCounters.put(cdProcess.getName(), intgr);
                   }


		}

	}


	/**
	 * Set ChannelPN
	 *
	 * @param adgEdgeList
	 *            Description of the Parameter
	 * @param proNet
	 *            Description of the Parameter
	 */
	private void _setChannelPN(Vector adgEdgeList, CDProcessNetwork procNet) {

		Integer tmp;
		String processName;
		int gNmbr;
		int chCounter = 1;
                CDChannel cdChannel = null;
                CDOutGate outGate = null;
                CDInGate inGate = null;

               /*  Create a channel for each edge when the edge name is unique.
		*  For edges with the same name a single channel is created.
		* So, #channels <= #edges
		*/
		Iterator i = adgEdgeList.iterator();
		while( i.hasNext() ) {
			ADGEdge adgEdge = (ADGEdge) i.next();
			String edgeName = adgEdge.getName();

			if ( !_relationEdgeChannel.containsKey( edgeName ) ) {

			     // Create a channel
			     cdChannel = new CDChannel("CH_" + chCounter++);

                             // Create the output gate of the channel
                             outGate = new CDOutGate( "" );
	   		     outGate.setChannel( cdChannel );
			     cdChannel.getGateList().add( outGate );

			     // Create the input gate of the channel
                             inGate = new CDInGate( "" );
			     inGate.setChannel( cdChannel );
			     cdChannel.getGateList().add( inGate );

			     //Add the channle to the process network
		             procNet.getChannelList().add( cdChannel );

			     _relationEdgeChannel.put( edgeName, cdChannel );

			} else {

                             cdChannel = (CDChannel) _relationEdgeChannel.get( edgeName );

			}

			// Add the edge to the channel
			cdChannel.getAdgEdgeList().add( adgEdge );
		}


                /* For each channel, set its communication model,
		 * set the ports of its IN and OUT gates, and
		 * connect the channel to two processes.
		 */
                Iterator j = procNet.getChannelList().iterator();
                while( j.hasNext() ) {
                        cdChannel = (CDChannel) j.next();
			inGate = cdChannel.getToGate();
			outGate = cdChannel.getFromGate();
			Vector edgeList = cdChannel.getAdgEdgeList();

			 // Set the communication model of the channel
			cdChannel.setCommunicationModel( ((ADGEdge) edgeList.get(0)).getLinModel() );

                        // Set the list of ports of the IN/OUT gates
			Iterator ie = edgeList.iterator();
                        while( ie.hasNext()  ) {
                            ADGEdge edge = (ADGEdge) ie.next();
			    inGate.getAdgPortList().add( edge.getToPort() );
			    outGate.getAdgPortList().add( edge.getFromPort() );
			}

			//Add the input gate of this channel to the corresponding process
			ADGNode toNode = (ADGNode) ( (ADGEdge) edgeList.get(0)).getToPort().getNode();
                        CDProcess toProcess = procNet.getProcess( toNode );
			toProcess.getGateList().add( inGate );
			inGate.setProcess( toProcess );

			//Add the output gate of this channel to the corresponding process
			ADGNode fromNode = (ADGNode) ( (ADGEdge) edgeList.get(0)).getFromPort().getNode();
                        CDProcess fromProcess = procNet.getProcess( fromNode );
			fromProcess.getGateList().add( outGate );
			outGate.setProcess( fromProcess );

			//Set a unique name of the input gate ------------------------
			processName = inGate.getProcess().getName();
			tmp = (Integer)_prcssInGateCounters.get( processName );
			gNmbr = tmp.intValue();
			inGate.setName("IG_" + gNmbr++);
			tmp = new Integer( gNmbr );
			_prcssInGateCounters.remove( processName );
			_prcssInGateCounters.put( processName, tmp );
                        //------------------------------------------------------------

			//Set a unique name of the output gate ------------------------
			processName = outGate.getProcess().getName();
			tmp = (Integer)_prcssOutGateCounters.get( processName );
			gNmbr = tmp.intValue();
			outGate.setName("OG_" + gNmbr++);
			tmp = new Integer( gNmbr );
			_prcssOutGateCounters.remove( processName );
			_prcssOutGateCounters.put( processName, tmp );
                        //------------------------------------------------------------

		}


	}

/*
	private void _setChannelPN(Vector adgEdgeList, CDProcessNetwork procNet) {

		Integer tmp;
		String processName;
		int gNmbr;
		int chCounter = 1;

		// one-to-one correspondence ( Edge<->Channel )
		Iterator i = adgEdgeList.iterator();
		while( i.hasNext() ) {
			ADGEdge adgEdge = (ADGEdge) i.next();

			// Create a channel
			CDChannel cdChannel = new CDChannel("CH_" + chCounter++);

                        // Create the output gate of the channel
                        CDOutGate outGate = new CDOutGate( "" );
			outGate.setChannel( cdChannel );
			cdChannel.getGateList().add( outGate );

			// Create the input gate of the channel
                        CDInGate inGate = new CDInGate( "" );
			inGate.setChannel( cdChannel );
			cdChannel.getGateList().add( inGate );

			// Add the edge to the channel
			cdChannel.getAdgEdgeList().add( adgEdge );

			// Set the communication model of the channel
			cdChannel.setCommunicationModel( adgEdge.getLinModel() );

                        // Set the list of ports of the in/out gates
			Iterator ie = cdChannel.getAdgEdgeList().iterator();
                        while( ie.hasNext()  ) {
                            ADGEdge edge = (ADGEdge) ie.next();
			    inGate.getAdgPortList().add( edge.getToPort() );
			    outGate.getAdgPortList().add( edge.getFromPort() );
			}


			//Add the input gate of this channel to the corresponding process
			ADGNode toNode = (ADGNode) ( (ADGEdge) cdChannel.getAdgEdgeList().get(0)).getToPort().getNode();
                        CDProcess toProcess = procNet.getProcess( toNode );
			toProcess.getGateList().add( inGate );
			inGate.setProcess( toProcess );

			//Add the output gate of this channel to the corresponding process
			ADGNode fromNode = (ADGNode) ( (ADGEdge) cdChannel.getAdgEdgeList().get(0)).getFromPort().getNode();
                        CDProcess fromProcess = procNet.getProcess( fromNode );
			fromProcess.getGateList().add( outGate );
			outGate.setProcess( fromProcess );

			//Set a unique name of the input gate ------------------------
			processName = inGate.getProcess().getName();
			tmp = (Integer)_prcssInGateCounters.get( processName );
			gNmbr = tmp.intValue();
			inGate.setName("IG_" + gNmbr++);
			tmp = new Integer( gNmbr );
			_prcssInGateCounters.remove( processName );
			_prcssInGateCounters.put( processName, tmp );
                        //------------------------------------------------------------

			//Set a unique name of the output gate ------------------------
			processName = outGate.getProcess().getName();
			tmp = (Integer)_prcssOutGateCounters.get( processName );
			gNmbr = tmp.intValue();
			outGate.setName("OG_" + gNmbr++);
			tmp = new Integer( gNmbr );
			_prcssOutGateCounters.remove( processName );
			_prcssOutGateCounters.put( processName, tmp );
                        //------------------------------------------------------------

                        //Add the channle to the process network
		        procNet.getChannelList().add( cdChannel );
		}

	}
*/
	/**
	 * Set Schedule for each Process in a Process Network
	 *
	 * @param proNet
	 *            Description of the Parameter
	 */
	private void _setSchedule(Mapping mapping, CDProcessNetwork proNet) {

                // Get a global schedule
		Scheduler sch = Scheduler.getInstance();

		// Derive a local schedule for each process
		Vector processList = proNet.getProcessList();
		Iterator i = processList.iterator();
		while (i.hasNext()) {
			CDProcess process = (CDProcess) i.next();
			Vector nodeList = process.getAdgNodeList();
			
			MProcessor processor = mapping.getMProcessor(process);
			if  ( processor.getScheduleType() == 1 ) {
				Iterator j = nodeList.iterator();
				while( j.hasNext() ) {
					Vector container = new Vector();
					container.add(j.next());
					process.getSchedule().add( sch.doSchedule(container) );
				}
			} else {
				// Add the local schedule to the process
				process.getSchedule().add( sch.doSchedule(nodeList) );
			}
		}
	}


	///////////////////////////////////////////////////////////////////
	//// private variables ////

	/**
	 * Create a unique instance of this class to implement a singleton
	 */
	private final static ADGraphToPN _instance = new ADGraphToPN();

        private HashMap _prcssInGateCounters = new HashMap();

	private HashMap _prcssOutGateCounters = new HashMap();

	private HashMap _relationEdgeChannel = new HashMap();


}

