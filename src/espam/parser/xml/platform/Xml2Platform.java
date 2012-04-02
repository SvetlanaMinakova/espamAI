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

package espam.parser.xml.platform;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.MemoryMap;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.communication.AXICrossbar;
import espam.datamodel.platform.communication.PLBBus;
import espam.datamodel.platform.communication.LMBBus;
import espam.datamodel.platform.communication.TransparentBus;
import espam.datamodel.platform.communication.ReadFifoBus;
import espam.datamodel.platform.communication.WriteFifoBus;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.ports.AXIPort;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.OPBPort;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.hwnodecompaan.ReadUnit;
import espam.datamodel.platform.hwnodecompaan.WriteUnit;
import espam.datamodel.platform.hwnodecompaan.ExecuteUnit;
import espam.datamodel.platform.peripherals.ZBTMemoryController;
import espam.datamodel.platform.peripherals.Uart;
import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;
import espam.datamodel.platform.host_interfaces.ML605;
import espam.main.UserInterface;

import org.xml.sax.Attributes;

//////////////////////////////////////////////////////////////////////////
//// Xml2Platform

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: Xml2Platform.java,v 1.5 2012/04/02 16:25:40 nikolov Exp $
 */

public class Xml2Platform {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Return the singleton instance of this class;
	 *
	 * @return  the instance.
	 */
	public final static Xml2Platform getInstance() {
		return _instance;
	}

	/**
	 *  Process the start of a platform tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a platform object.
	 */
	public Object processPlatform(Attributes attributes) {
		//System.out.println(" -- Platform -- ");
		String name = (String) attributes.getValue("name");
		Platform platform = new Platform(name);

		return platform;
	}

	/**
	 * Process the end of a platform tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processPlatform(Stack stack) {
	}

	/**
	 *  Process the start of a subplatform tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a subplatform object.
	 */
	public Object processSubplatform(Attributes attributes) {

		return null;
	}

	/**
	 * Process the end of a subplatform tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processSubplatform(Stack stack) {
	}

	/**
	 *  Process the start of a processor tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a processor object.
	 */
	public Object processProcessor(Attributes attributes) {
		//System.out.println(" -- Processor -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");
		String dataMemorySize = (String) attributes.getValue("data_memory");
		String programMemorySize = (String) attributes.getValue("program_memory");

		if( type.equals("PPC") ) {
			Processor processor = new PowerPC(name);
			processor.setDataMemSize(Integer.valueOf(dataMemorySize).intValue());
			processor.setProgMemSize(Integer.valueOf(programMemorySize).intValue());
			return processor;

		} else if( type.equals("MB") ) {
			Processor processor = new MicroBlaze(name);
			processor.setDataMemSize(Integer.valueOf(dataMemorySize).intValue());
			processor.setProgMemSize(Integer.valueOf(programMemorySize).intValue());
			return processor;

		} else if( type.equals("CompaanHWNode") ) {
			CompaanHWNode processor = new CompaanHWNode(name);
			return processor;
		} else {
			throw new Error("Unknown Processor Type: " + type);
		}
	}

	/**
	 * Process the end of a processor tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processProcessor(Stack stack) {
		Resource processor = (Resource) stack.pop();
		Platform platform = (Platform) stack.peek();

		processor.setLevelUpResource(platform);
                platform.getResourceList().add(processor);
	}
	
	/**
	 *  Process the start of a periperal tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a peripheral object.
	 */
	public Object processPeripheral(Attributes attributes) {
		//System.out.println(" -- Peripheral -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");
		String size = (String) attributes.getValue("size");

		if( type.equals("ZBTCTRL") ) {
			ZBTMemoryController zbtMemoryController = new ZBTMemoryController(name);
			zbtMemoryController.setSize( Integer.valueOf(size).intValue() );
			return zbtMemoryController;
		} else if( type.equals("UART") ) {
			Uart uart = new Uart(name);
			uart.setSize( Integer.valueOf(size).intValue() );
			return uart;
		} else {
			throw new Error("Unknown Peripheral Type: " + type);
		}
	}

	/**
	 * Process the end of a peripheral tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processPeripheral(Stack stack) {
		Resource zbtMemoryController = (Resource) stack.pop();
		Platform platform = (Platform) stack.peek();

		zbtMemoryController.setLevelUpResource(platform);
                platform.getResourceList().add(zbtMemoryController);
	}

	/**
	 *  Process the start of a network tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a network object.
	 */
	public Object processNetwork(Attributes attributes) {
		//System.out.println(" -- Network -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");

		Resource network = null;

		if( type.equals("CrossbarSwitch") ) {
			network = new Crossbar(name);
		} else if( type.equals("AXICrossbarSwitch") ) {
			network = new AXICrossbar(name);
		} else {
			throw new Error("Unknown Network Type: " + type);
		}

		return network;
	}

	/**
	 * Process the end of a network tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processNetwork(Stack stack) {
		Resource network = (Resource) stack.pop();
		Platform platform = (Platform) stack.peek();

		network.setLevelUpResource(platform);
                platform.getResourceList().add(network);
	}

	/**
	 *  Process the start of a memory tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a memory object.
	 */
	public Object processMemory(Attributes attributes) {
		//System.out.println(" -- Memory -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");
		String datawidth = (String) attributes.getValue("datawidth");
		String size = (String) attributes.getValue("size");

		Memory memory = null;

		if( type.equals("Fifo") ) {
			memory = new Fifo(name);
		} else if( type.equals("MultiFifo") ) {
			memory = new MultiFifo(name);
		} else if( type.equals("BRAM") ) {
			memory = new BRAM(name);
		} else if( type.equals("ZBT") ) {
			memory = new ZBT(name);
		} else {
			throw new Error("Unknown Memory Type: " + type);
		}

                memory.setSize( Integer.valueOf(size).intValue() );
		memory.setDataWidth( Integer.valueOf(datawidth).intValue() );

		return memory;
	}

	/**
	 * Process the end of a memory tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processMemory(Stack stack) {
		Memory memory = (Memory) stack.pop();
		Platform platform = (Platform) stack.peek();

		memory.setLevelUpResource(platform);
                platform.getResourceList().add(memory);
	}



	/**
	 *  Process the start of a host_interface tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a host interface object.
	 */
	public Object processHostInterface(Attributes attributes) {
		//System.out.println(" -- Board -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");
		String commInterface = (String) attributes.getValue("interface");

		if( type.equals("ADM-XRC-II") ) {
			System.out.println(" -- FPGA Board: ADM-XRC-II");
			ADMXRCII hostInterface = new ADMXRCII(name);
//			zbtMemoryController.setSize( Integer.valueOf(size).intValue() );
                        hostInterface.setCommInterface(commInterface); 
			return hostInterface;
		} else if( type.equals("ADM-XPL") ) {
			System.out.println(" -- FPGA Board: ADM-XPL");
			ADMXPL hostInterface = new ADMXPL(name);
                        hostInterface.setCommInterface(commInterface); 
			return hostInterface;
		} else if( type.equals("XUPV5-LX110T") ) {
			System.out.println(" -- FPGA Board: XUPV5-LX110T");
			if( commInterface.equals("empty") ) {
			    commInterface = "UART"; // The default communication interface
			}
			System.out.println(" -- Communication interface: " + commInterface);
			XUPV5LX110T hostInterface = new XUPV5LX110T(name);
                        hostInterface.setCommInterface(commInterface); 
			return hostInterface;
		} else if( type.equals("ML505") ) {
			System.out.println(" -- FPGA Board: ML505");
			if( commInterface.equals("empty") ) {
			    commInterface = "UART"; // The default communication interface
			}
			System.out.println(" -- Communication interface: " + commInterface);
			ML505 hostInterface = new ML505(name);
                        hostInterface.setCommInterface(commInterface); 
			return hostInterface;
		} else if ( type.equals("ML605") ) {
			System.out.println(" -- FPGA Board: ML605");
			if( commInterface.equals("empty") ) {
			    commInterface = "UART"; // The default communication interface
			}
			System.out.println(" -- Communication interface: " + commInterface);
			ML605 hostInterface = new ML605(name);
                        hostInterface.setCommInterface(commInterface); 
			return hostInterface;
		} else {
			throw new Error("Unknown Board Type: " + type);
		}
	}

	/**
	 * Process the end of a host_interface tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processHostInterface(Stack stack) {
		Resource hostInterface = (Resource) stack.pop();
		Platform platform = (Platform) stack.peek();

		hostInterface.setLevelUpResource(platform);
                platform.getResourceList().add(hostInterface);
	}


	/**
	 *  Process the start of a link tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a link object.
	 */
	public Object processLink(Attributes attributes) {
		//System.out.println(" -- Link -- ");
		String name = (String) attributes.getValue("name");

		Link link = new Link(name);

		return link;
	}

	/**
	 * Process the end of a link tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processLink(Stack stack) {
		Link link = (Link) stack.pop();
		Platform platform = (Platform) stack.peek();

                platform.getLinkList().add(link);
	}

	/**
	 *  Process the start of a port tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a port object.
	 */
	public Object processPort(Attributes attributes) {
		//System.out.println(" -- Port -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");
		String size = (String) attributes.getValue("size");

		Port port = null;

		if( type.equals("PLBPort") ) {
			port = new PLBPort(name);
		} else if( type.equals("AXIPort") ) {
			port = new AXIPort(name);
		} else if( type.equals("LMBPort") ) {
			port = new LMBPort(name);
		} else if( type.equals("OPBPort") ) {
			port = new OPBPort(name);
		} else if( type.equals("FifoReadPort") ) {
			port = new FifoReadPort(name);
		} else if( type.equals("FifoWritePort") ) {
			port = new FifoWritePort(name);
		} else if( type.equals("CompaanInPort") ) {
			port = new CompaanInPort(name);
		} else if( type.equals("CompaanOutPort") ) {
			port = new CompaanOutPort(name);
		} else if( type.equals("empty") ) {
			port = new Port(name);
		} else {
			throw new Error("Unknown Port Type: " + type);
		}

		if( size != null ) port.setMemSize( Integer.valueOf(size).intValue() );
		return port;
	}

	/**
	 * Process the end of a port tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processPort(Stack stack) {
		Port port = (Port) stack.pop();
		Resource resource = (Resource) stack.peek();

		port.setResource(resource);
                resource.getPortList().add(port);
	}

	/**
	 *  Process the start of a vfifo tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a vfifo object.
	 */
	public Object processVfifo(Attributes attributes) {
                //System.out.println(" -- vfifo -- ");
		String name = (String) attributes.getValue("name");
		String size = (String) attributes.getValue("size");

                Fifo fifo = new Fifo(name);
                fifo.setSize( Integer.valueOf(size).intValue() );

		return fifo;
	}

	/**
	 * Process the end of a vfifo tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processVfifo(Stack stack) {
		Fifo fifo = (Fifo) stack.pop();
		MultiFifo multififo = (MultiFifo) stack.peek();

                multififo.getFifoList().add(fifo);
	}

	/**
	 *  Process the start of a resource tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a resource object.
	 */
	public Object processResource(Attributes attributes) {
		//System.out.println(" -- resource -- ");
		String resourceName = (String) attributes.getValue("name");
		String portName = (String) attributes.getValue("port");

		Resource resource = new Resource(resourceName);
		Port port = new Port(portName);
		resource.getPortList().add(port);

		return resource;
	}

	/**
	 * Process the end of a resource tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processResource(Stack stack) {
		Resource r = (Resource) stack.pop();
		Link link = (Link) stack.pop();
		Platform platform = (Platform) stack.peek();

		String resourceName = r.getName();
		Resource resource = platform.getResource( resourceName );
		if( resource == null ) {
		    throw new Error("Link is not possible: " +
			            "Unknown resource: " + resourceName);
		}

		String portName = ((Port) r.getPortList().get(0)).getName();
                Port port = resource.getPort( portName );
		if( port == null ) {
			throw new Error("Link is not possible: " +
			                "Unknown port: " + portName +
					" of resource " + resourceName);
		}

		port.setLink(link);
		link.getPortList().add(port);

		stack.push(link);
	}

	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/**
	*  Constructor that is private because only a single version has to
	*  exist.
	*/
	private Xml2Platform() {
	}
	/**
	 *  Convert a string representing a vector into a Java vector.
	 *
	 * @param  vectorString Description of the Parameter
	 * @return  the java vector.
	 */
	private Vector _string2Vector(String vectorString) {
		StringTokenizer st = new StringTokenizer(vectorString, ", ");
		int count = st.countTokens();
		Vector vector = new Vector(count);
		for( int i = 0; i < count; i++ ) {
			String num = st.nextToken();
			vector.add(num);
		}
		return vector;
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance
	 * */
	private final static Xml2Platform _instance = new Xml2Platform();
}


