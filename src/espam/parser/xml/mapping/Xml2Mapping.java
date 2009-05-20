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

package espam.parser.xml.mapping;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.MProcess;

import org.xml.sax.Attributes;

//////////////////////////////////////////////////////////////////////////
//// Xml2Mapping

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: Xml2Mapping.java,v 1.3 2009/05/20 13:51:51 stefanov Exp $
 */

public class Xml2Mapping {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Return the singleton instance of this class;
	 *
	 * @return  the instance.
	 */
	public final static Xml2Mapping getInstance() {
		return _instance;
	}

	/**
	 *  Process the start of a mapping tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a platform object.
	 */
	public Object processMapping(Attributes attributes) {
		//System.out.println(" -- Mapping -- ");
		String name = (String) attributes.getValue("name");
		Mapping mapping = new Mapping(name);

		return mapping;
	}

	/**
	 * Process the end of a mapping tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processMapping(Stack stack) {
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
		String schedule = (String) attributes.getValue("scheduleType");
		
		MProcessor processor = new MProcessor(name);
		
		if ( schedule == null || schedule.equals("static") ) {
			processor.setScheduleType(0);
		} else if ( schedule.equals("dynamic-xilkernel")) {
			processor.setScheduleType(1);
		} else {
			throw new Error("Unknown Schedule Type: " + schedule);
		}

		

		return processor;
	}

	/**
	 * Process the end of a processor tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processProcessor(Stack stack) {
		MProcessor processor = (MProcessor) stack.pop();
		Mapping mapping = (Mapping) stack.peek();

                mapping.getProcessorList().add(processor);
	}


	/**
	 *  Process the start of a process tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a port object.
	 */
	public Object processProcess(Attributes attributes) {
		//System.out.println(" -- Process -- ");
		String name = (String) attributes.getValue("name");

		MProcess process = new MProcess(name);

		return process;
	}

	/**
	 * Process the end of a process tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processProcess(Stack stack) {
		MProcess process = (MProcess) stack.pop();
		MProcessor processor = (MProcessor) stack.peek();

                processor.getProcessList().add(process);
	}


	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/**
	*  Constructor that is private because only a single version has to
	*  exist.
	*/
	private Xml2Mapping() {
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance
	 * */
	private final static Xml2Mapping _instance = new Xml2Mapping();
}

