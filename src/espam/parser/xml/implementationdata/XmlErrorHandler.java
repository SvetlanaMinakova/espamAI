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

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

//////////////////////////////////////////////////////////////////////////
//// XmlErrorHandler

/**
 * XmlErrorHandler for implementationdata XML parser.
 *
 * @author Sven van Haastregt
 */

public class XmlErrorHandler extends DefaultHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Empty constructor
     */
    public XmlErrorHandler() {
        super();
    }

    /**
     *  Treat validation errors as fatal
     *
     * @param  e Description of the Parameter
     * @exception  SAXParseException MyException If such and such occurs
     */
    public void error(SAXParseException e) throws SAXParseException {
        System.out.println("Error found: " + e.getMessage());
        throw e;
    }

    /**
     *  Dump warnings too
     *
     * @param  err Description of the Parameter
     * @exception  SAXParseException MyException If such and such occurs
     */
    public void warning(SAXParseException err) throws SAXParseException {
        System.out.println("** Warning"
                + ", line " + err.getLineNumber()
                + ", uri " + err.getSystemId());
        System.out.println("   " + err.getMessage());
    }
}
