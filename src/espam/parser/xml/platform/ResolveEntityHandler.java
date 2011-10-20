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

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// ResolveEntityHandler

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: ResolveEntityHandler.java,v 1.9 2002/09/11 16:19:29
 *      kienhuis Exp $
 */

public class ResolveEntityHandler implements EntityResolver {

	///////////////////////////////////////////////////////////////////
	////                         public members                    ////

	/**
	 *  Description of the Method
	 *
	 * @param  publicId Description of the Parameter
	 * @param  systemId Description of the Parameter
	 * @return  Description of the Return Value
	 */

	public InputSource resolveEntity(String publicId, String systemId) {
		if( publicId != null && publicId.equals("-//LIACS//DTD ESPAM 1//EN") ) {
			return new InputSource(new StringReader(ESPAM_DTD_1));
		} else {
			return null;
		}
	}

	///////////////////////////////////////////////////////////////////
	////                         public variables                  ////

	/**
	 *  Description of the Field
	*/

	public static String ESPAM_DTD_1 =
	                  "<!ELEMENT platform (subplatform*,processor*,peripheral*,network*,memory*,host_interface*,link*)>"
			+ "<!ATTLIST platform name CDATA #REQUIRED>"

			+ "<!ELEMENT subplatform EMPTY>"
			+ "<!ATTLIST subplatform name CDATA #REQUIRED file CDATA #IMPLIED>"

			+ "<!ELEMENT processor (port*)>"
			+ "<!ATTLIST processor name CDATA #REQUIRED type CDATA #IMPLIED data_memory CDATA #IMPLIED program_memory CDATA #IMPLIED>"
            
			+ "<!ELEMENT peripheral (port*)>"
			+ "<!ATTLIST peripheral name CDATA #REQUIRED type CDATA #IMPLIED size CDATA #IMPLIED>"
			
			+ "<!ELEMENT network (port*)>"
			+ "<!ATTLIST network name CDATA #REQUIRED type CDATA #IMPLIED>"

			+ "<!ELEMENT memory (port*,vfifo*)>"
			+ "<!ATTLIST memory name CDATA #REQUIRED type CDATA #IMPLIED datawidth CDATA #IMPLIED size CDATA #IMPLIED>"

			+ "<!ELEMENT host_interface (port*)>"
			+ "<!ATTLIST host_interface name CDATA #REQUIRED type (ADM-XRC-II | ADM-XPL | XUPV5-LX110T | ML505 | ML605 | empty) \"empty\">"

			+ "<!ELEMENT link (resource*)>"
			+ "<!ATTLIST link name CDATA #REQUIRED>"

			+ "<!ELEMENT port EMPTY>"
			//+ "<!ATTLIST port name CDATA #REQUIRED type CDATA #IMPLIED>"
			+ "<!ATTLIST port name CDATA #REQUIRED type (PLBPort | OPBPort | LMBPort | FifoReadPort | FifoWritePort | CompaanInPort | CompaanOutPort | empty) \"empty\" size CDATA #IMPLIED>"

			+ "<!ELEMENT vfifo EMPTY>"
			+ "<!ATTLIST vfifo name CDATA #REQUIRED size CDATA #IMPLIED>"

			+ "<!ELEMENT resource EMPTY>"
			+ "<!ATTLIST resource name CDATA #REQUIRED port CDATA #IMPLIED>";

}
