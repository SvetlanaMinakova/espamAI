
package espam.parser.xml.pn;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// XmlErrorHandler

/**
 *  This class
 *
 * @author  Hristo Nikolov
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
        if (publicId != null && publicId.equals("-//Compaan Design//DTD KPN 1//EN")) {
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
        "<!ELEMENT model (parameter*,entity*,link*)>"
        + "<!ATTLIST model name CDATA #REQUIRED>"
        
        + "<!ELEMENT parameter EMPTY>"
        + "<!ATTLIST parameter name CDATA #REQUIRED lb CDATA #IMPLIED ub CDATA #IMPLIED value CDATA #IMPLIED>"
        
        + "<!ELEMENT entity (port*,property*,ipdstatement*,assignstatement*,opdstatement*)*>"
        + "<!ATTLIST entity name CDATA #REQUIRED type CDATA #IMPLIED>"
        
        + "<!ELEMENT port (property*)>"
        + "<!ATTLIST port name CDATA #REQUIRED type CDATA #IMPLIED>"
        
        + "<!ELEMENT ipdstatement (property*,domain,index*,linearization)>"
        + "<!ATTLIST ipdstatement arg CDATA #REQUIRED port CDATA #IMPLIED>"
        
        + "<!ELEMENT opdstatement (property*,domain,index*,filter*)>"
        + "<!ATTLIST opdstatement arg CDATA #REQUIRED port CDATA #IMPLIED>"
        
        + "<!ELEMENT assignstatement (property*,var*,domain,index*)>"
        + "<!ATTLIST assignstatement name CDATA #REQUIRED>"
        
        + "<!ELEMENT filter EMPTY>"
        + "<!ATTLIST filter exp CDATA #REQUIRED type CDATA #IMPLIED>"
        
        + "<!ELEMENT linearization (property*)>"
        + "<!ATTLIST linearization type CDATA #REQUIRED>"
        
        + "<!ELEMENT link (property*)>"
        + "<!ATTLIST link name CDATA #REQUIRED to CDATA #IMPLIED from CDATA #IMPLIED>"
        
        + "<!ELEMENT var EMPTY>"
        + "<!ATTLIST var name CDATA #REQUIRED type CDATA #REQUIRED>"
        
        + "<!ELEMENT property EMPTY>"
        + "<!ATTLIST property name CDATA #REQUIRED value CDATA #IMPLIED type CDATA #IMPLIED>"
        
        + "<!ELEMENT domain ((constraint)+,(context),(mapping))*>"
        + "<!ATTLIST domain index CDATA #REQUIRED control CDATA #REQUIRED parameter CDATA #REQUIRED>"
        
        + "<!ELEMENT constraint EMPTY>"
        + "<!ATTLIST constraint matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT mapping EMPTY>"
        + "<!ATTLIST mapping matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT context EMPTY>"
        + "<!ATTLIST context matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT index EMPTY>"
        + "<!ATTLIST index key CDATA #REQUIRED exp CDATA #REQUIRED>";
}
