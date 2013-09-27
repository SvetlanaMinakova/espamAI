
package espam.parser.xml.platform;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

//////////////////////////////////////////////////////////////////////////
//// XmlErrorHandler

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: XmlErrorHandler.java,v 1.3 2001/11/26 21:04:15 kienhuis
 *      Exp $
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
    public void error(SAXParseException e)
        throws SAXParseException {
        System.out.println("Error found: " + e.getMessage());
        throw e;
    }
    
    /**
     *  Dump warnings too
     *
     * @param  err Description of the Parameter
     * @exception  SAXParseException MyException If such and such occurs
     */
    public void warning(SAXParseException err)
        throws SAXParseException {
        System.out.println("** Warning"
                               + ", line " + err.getLineNumber()
                               + ", uri " + err.getSystemId());
        System.out.println("   " + err.getMessage());
    }
}
