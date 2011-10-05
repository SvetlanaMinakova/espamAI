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

package espam.parser.xml.sadg;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// XmlErrorHandler

/**
 *  This class
 *
 * @author  Todor Stefanov
 * @version  $Id: ResolveEntityHandler.java,v 1.9 2006/04/10 16:19:29
 *      stefanov Exp $
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
		if (publicId != null && publicId.equals("-//LIACS//DTD ESPAM 1//EN")) {
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
		  "<!ELEMENT sadg (adg*,ast*)>"

                /* ----------Begin ADG---------- */
		+ "<!ELEMENT adg (parameter*,node*,edge*)>"
		+ "<!ATTLIST adg name CDATA #REQUIRED levelUpNode CDATA #IMPLIED>"

		+ "<!ELEMENT parameter EMPTY>"
		+ "<!ATTLIST parameter name CDATA #REQUIRED lb CDATA #IMPLIED ub CDATA #IMPLIED value CDATA #IMPLIED>"

		+ "<!ELEMENT node (inport*,invar*,outport*,file*,expression*,function,domain)*>"
		+ "<!ATTLIST node name CDATA #REQUIRED  levelUpNode CDATA #IMPLIED>"

		+ "<!ELEMENT inport (invariable*, bindvariable*,domain)>"
		+ "<!ATTLIST inport name CDATA #REQUIRED node CDATA #IMPLIED edge CDATA #IMPLIED>"

		+ "<!ELEMENT outport (outvariable*, bindvariable*,domain)>"
		+ "<!ATTLIST outport name CDATA #REQUIRED node CDATA #IMPLIED edge CDATA #IMPLIED>"

		+ "<!ELEMENT function (inargument*,outargument*,ctrlvar*,domain)>"
		+ "<!ATTLIST function name CDATA #REQUIRED>"

		+ "<!ELEMENT edge (linearization,mapping)>"
		+ "<!ATTLIST edge name CDATA #REQUIRED fromPort CDATA #IMPLIED fromNode CDATA #IMPLIED toPort CDATA #IMPLIED toNode CDATA #IMPLIED size CDATA #IMPLIED>"

		+ "<!ELEMENT domain (linearbound*,filterset*)>"
		+ "<!ATTLIST domain type CDATA #REQUIRED>"

		+ "<!ELEMENT linearbound (constraint+,context*,control*)>"
		+ "<!ATTLIST linearbound index CDATA #REQUIRED staticControl CDATA #REQUIRED dynamicControl CDATA #REQUIRED parameter CDATA #REQUIRED>"

		+ "<!ELEMENT filterset (constraint+)>"
		+ "<!ATTLIST filterset index CDATA #REQUIRED staticControl CDATA #REQUIRED dynamicControl CDATA #REQUIRED parameter CDATA #REQUIRED>"

		+ "<!ELEMENT invar (bindvariable,domain)>"
		+ "<!ATTLIST invar name CDATA #REQUIRED node CDATA #IMPLIED realName CDATA #IMPLIED>"

		+ "<!ELEMENT invariable EMPTY>"
		+ "<!ATTLIST invariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"

		+ "<!ELEMENT outvariable EMPTY>"
		+ "<!ATTLIST outvariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"

		+ "<!ELEMENT ctrlvar EMPTY>"
		+ "<!ATTLIST ctrlvar name CDATA #REQUIRED iterator CDATA #REQUIRED>"

		+ "<!ELEMENT bindvariable EMPTY>"
		+ "<!ATTLIST bindvariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"

		+ "<!ELEMENT inargument EMPTY>"
		+ "<!ATTLIST inargument name CDATA #REQUIRED dataType CDATA #IMPLIED>"

		+ "<!ELEMENT outargument EMPTY>"
		+ "<!ATTLIST outargument name CDATA #REQUIRED dataType CDATA #IMPLIED>"

		+ "<!ELEMENT file EMPTY>"
		+ "<!ATTLIST file name CDATA #REQUIRED>"

		+ "<!ELEMENT constraint EMPTY>"
		+ "<!ATTLIST constraint matrix CDATA #REQUIRED>"

		+ "<!ELEMENT context EMPTY>"
		+ "<!ATTLIST context matrix CDATA #REQUIRED>"

		+ "<!ELEMENT control EMPTY>"
		+ "<!ATTLIST control name CDATA #REQUIRED exp CDATA #REQUIRED>"

		+ "<!ELEMENT expression EMPTY>"
		+ "<!ATTLIST expression name CDATA #REQUIRED value CDATA #REQUIRED>"

		+ "<!ELEMENT mapping EMPTY>"
		+ "<!ATTLIST mapping matrix CDATA #REQUIRED>"

		+ "<!ELEMENT linearization EMPTY>"
		+ "<!ATTLIST linearization type CDATA #REQUIRED>"
                 /* ----------End ADG---------- */

                /* ----------Begin AST---------- */
		+ "<!ELEMENT ast (stmt*,port*,for*,var*)*>"

		+ "<!ELEMENT for (for*,if*,stmt*,port*,var*)*>"
		+ "<!ATTLIST for iterator CDATA #REQUIRED  LB CDATA #IMPLIED UB CDATA #IMPLIED stride CDATA #IMPLIED>"

		+ "<!ELEMENT if (for*, if*,stmt*,port*,var*)*>"
		+ "<!ATTLIST if LHS CDATA #REQUIRED RHS CDATA #IMPLIED sign CDATA #IMPLIED>"

		+ "<!ELEMENT stmt EMPTY>"
		+ "<!ATTLIST stmt node CDATA #REQUIRED>"

		+ "<!ELEMENT var EMPTY>"
		+ "<!ATTLIST var name CDATA #REQUIRED>"

		+ "<!ELEMENT port EMPTY>"
		+ "<!ATTLIST port name CDATA #REQUIRED>";
                 /* ----------End ADT---------- */

}
