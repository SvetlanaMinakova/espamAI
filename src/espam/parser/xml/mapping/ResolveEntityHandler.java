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
	          "<!ELEMENT mapping (processor*, fifo*)>"
			+ "<!ATTLIST mapping name CDATA #REQUIRED>"

			+ "<!ELEMENT processor (process*)>"
			+ "<!ATTLIST processor name CDATA #REQUIRED scheduleType CDATA #IMPLIED>"

			+ "<!ELEMENT process EMPTY>"
			+ "<!ATTLIST process name CDATA #REQUIRED execution CDATA #IMPLIED period CDATA #IMPLIED startTime CDATA #IMPLIED priority CDATA #IMPLIED>"

			+ "<!ELEMENT fifo EMPTY>"
			+ "<!ATTLIST fifo name CDATA #REQUIRED size CDATA #REQUIRED>";

}
