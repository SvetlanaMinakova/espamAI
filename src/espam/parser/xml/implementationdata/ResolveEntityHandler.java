/*******************************************************************\

The ESPAM Software Tool
Copyright (c) 2004-2012 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.parser.xml.implementationdata;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// ResolveEntityHandler

/**
 * EntityResolver for implementationdata XML parser.
 *
 * @author Sven van Haastregt
 */

public class ResolveEntityHandler implements EntityResolver {

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

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
     * DTD of implementationdata XML.
     */
    public static String ESPAM_DTD_1 =
        "<!ELEMENT implementationMetrics (functions)>"

        + "<!ELEMENT functions (function*)>"

        + "<!ELEMENT function (implementation*)>"
        + "<!ATTLIST function functionName CDATA #REQUIRED>"

        + "<!ELEMENT implementation (performance?,resources?,power?)>"
        + "<!ATTLIST implementation componentName CDATA #REQUIRED implementationType CDATA #REQUIRED>"

        + "<!ELEMENT performance (delay?,ii?)>"

        + "<!ELEMENT delay EMPTY>"
        + "<!ATTLIST delay average CDATA #REQUIRED worstcase CDATA #IMPLIED bestcase CDATA #IMPLIED>"

        + "<!ELEMENT ii EMPTY>"
        + "<!ATTLIST ii value CDATA #REQUIRED>"

        + "<!ELEMENT resources (slices?,memory?)>"

        + "<!ELEMENT slices EMPTY>"
        + "<!ATTLIST slices value CDATA #REQUIRED>"

        + "<!ELEMENT memory EMPTY>"
        + "<!ATTLIST memory program CDATA #IMPLIED data CDATA #IMPLIED>"

        + "<!ELEMENT power ANY>";
}
