
package espam.parser.xml.adg;

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
        "<!ELEMENT adg (parameter*,node*,edge*)>"
        + "<!ATTLIST adg name CDATA #REQUIRED levelUpNode CDATA #IMPLIED>"
        
        + "<!ELEMENT parameter EMPTY>"
        + "<!ATTLIST parameter name CDATA #REQUIRED lb CDATA #IMPLIED ub CDATA #IMPLIED value CDATA #IMPLIED>"
        
        + "<!ELEMENT node (inport*,outport*,function,domain)*>"
        + "<!ATTLIST node name CDATA #REQUIRED  levelUpNode CDATA #IMPLIED>"
        
        + "<!ELEMENT inport (invariable*, bindvariable*,domain)>"
        + "<!ATTLIST inport name CDATA #REQUIRED node CDATA #IMPLIED edge CDATA #IMPLIED>"
        
        + "<!ELEMENT outport (outvariable*, bindvariable*,domain)>"
        + "<!ATTLIST outport name CDATA #REQUIRED node CDATA #IMPLIED edge CDATA #IMPLIED>"
        
        + "<!ELEMENT function (inargument*,outargument*)>"
        + "<!ATTLIST function name CDATA #REQUIRED>"
        
        + "<!ELEMENT edge (linearization,mapping)>"
        + "<!ATTLIST edge name CDATA #REQUIRED fromPort CDATA #IMPLIED fromNode CDATA #IMPLIED toPort CDATA #IMPLIED toNode CDATA #IMPLIED size CDATA #IMPLIED>"
        
        + "<!ELEMENT domain (linearbound*,filterset*)>"
        + "<!ATTLIST domain type CDATA #REQUIRED>"
        
        + "<!ELEMENT linearbound (constraint+,context*,control*)>"
        + "<!ATTLIST linearbound index CDATA #REQUIRED staticControl CDATA #REQUIRED dynamicControl CDATA #REQUIRED parameter CDATA #REQUIRED>"
        
        + "<!ELEMENT filterset (constraint+)>"
        + "<!ATTLIST filterset index CDATA #REQUIRED staticControl CDATA #REQUIRED dynamicControl CDATA #REQUIRED parameter CDATA #REQUIRED>"
        
        + "<!ELEMENT invariable EMPTY>"
        + "<!ATTLIST invariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"
        
        + "<!ELEMENT outvariable EMPTY>"
        + "<!ATTLIST outvariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"
        
        + "<!ELEMENT bindvariable EMPTY>"
        + "<!ATTLIST bindvariable name CDATA #REQUIRED dataType CDATA #IMPLIED>"
        
        + "<!ELEMENT inargument EMPTY>"
        + "<!ATTLIST inargument name CDATA #REQUIRED dataType CDATA #IMPLIED>"
        
        + "<!ELEMENT outargument EMPTY>"
        + "<!ATTLIST outargument name CDATA #REQUIRED dataType CDATA #IMPLIED>"
        
        + "<!ELEMENT constraint EMPTY>"
        + "<!ATTLIST constraint matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT context EMPTY>"
        + "<!ATTLIST context matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT control EMPTY>"
        + "<!ATTLIST control name CDATA #REQUIRED exp CDATA #REQUIRED>"
        
        + "<!ELEMENT mapping EMPTY>"
        + "<!ATTLIST mapping matrix CDATA #REQUIRED>"
        
        + "<!ELEMENT linearization EMPTY>"
        + "<!ATTLIST linearization type CDATA #REQUIRED>";
}
