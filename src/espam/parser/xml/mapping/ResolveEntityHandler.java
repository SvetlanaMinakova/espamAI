
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
