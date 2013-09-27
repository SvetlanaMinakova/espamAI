
package espam.parser.xml.adg;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import espam.datamodel.graph.adg.ADGraph;
import espam.main.UserInterface;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// XmlADGParser

/**
 *  This class
 *
 * @author  Todor Stefanov
 * @version  $Id: XmlADGParser.java,v 1.1 2007/12/07 22:07:09 stefanov Exp $
 */

public class XmlADGParser implements ContentHandler {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     */
    public XmlADGParser() {
        super();
        
        _stack = new Stack();
        _xml2ADG = (Xml2ADG) Xml2ADG.getInstance();
        
        initializeParser();
    }
    
    /**
     * Initialize the XML parser
     */
    public void initializeParser() {
        try {
            
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            SAXParser sp = spf.newSAXParser();
            _parser = sp.getXMLReader();
            _parser.setEntityResolver(new ResolveEntityHandler());
            
        } catch( SAXParseException err ) {
            System.out.println(
                               "** Parsing error"
                                   + ", line "
                                   + err.getLineNumber()
                                   + ", uri "
                                   + err.getSystemId());
            System.out.println("   " + err.getMessage());
        } catch( SAXException e ) {
            e.printStackTrace();
            
        } catch( Throwable t ) {
            t.printStackTrace();
        }
    }
    
    /**
     * @param  stream Description of the Parameter
     * @return  Description of the Return Value
     * @exception  PandaException MyException If such and such occurs
     */
    public ADGraph doParse(InputStream stream) throws Exception {
        ADGraph adg = null;
        
        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());
        
        try {
            
            _parser.parse(new InputSource(stream));
            adg = (ADGraph) _stack.pop();
        } catch( SAXParseException err ) {
            System.out.println(
                               "** Parsing error"
                                   + ", line "
                                   + err.getLineNumber()
                                   + ", uri "
                                   + err.getSystemId());
            System.out.println("   " + err.getMessage());
            err.printStackTrace();
            
        } catch( SAXException e ) {
            Exception x = e;
            if (e.getException() != null) {
                x = e.getException();
            }
            x.printStackTrace();
        } catch( Throwable t ) {
            t.printStackTrace();
        }
        
        // Return the adg
        return adg;
    }
    
    /**
     * @param  url Description of the Parameter
     * @return  Description of the Return Value
     * @exception  PandaException MyException If such and such occurs
     */
    public ADGraph doParse(String url) throws EspamException {
        
        ADGraph adg = null;
        
        System.out.println(" - Read ADG from XML file");
        
        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());
        
        try {
            
            // Get only the file name of the URL.
            String docString = _getFileName(url);
            String uri = _makeAbsoluteURL(url);
            
            _ui.printlnVerbose(" -- processing XML file: " + uri);
            _ui.printVerbose(" -- read XML file: ");
            
            _parser.parse(new InputSource(uri));
            
            // All done,
            _ui.printlnVerbose(" [DONE] ");
            
            adg = (ADGraph) _stack.pop();
            
        } catch( SAXParseException err ) {
            err.printStackTrace();
            System.out.println(
                               "** Parsing error"
                                   + ", line "
                                   + err.getLineNumber()
                                   + ", uri "
                                   + err.getSystemId());
            System.out.println("   " + err.getMessage());
            err.printStackTrace();
        } catch( SAXException e ) {
            e.printStackTrace();
        } catch( Throwable t ) {
            t.printStackTrace();
        }
        
        System.out.println(" - ADG Model from XML [Constructed]");
        System.out.println();
        
        // Return the adg
        return adg;
    }
    
    /**
     * @param  text Description of the Parameter
     * @return  Description of the Return Value
     * @exception  PandaException MyException If such and such occurs
     */
    public ADGraph parse(String text) throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
        return doParse(stream);
    }
    
    /**
     * @exception  SAXException MyException If such and such occurs
     */
    public void startDocument() throws SAXException {
        _stack.clear();
    }
    
    /**
     * @param  elementName Description of the Parameter
     * @param  attributes Description of the Parameter
     * @exception  SAXException MyException If such and such occurs
     */
    public void startElement(
                             String namespaceURI,
                             String localName,
                             String elementName,
                             Attributes attributes)
        throws SAXException {
        
        /*
         System.out.println(" Start URI:     " + namespaceURI);
         System.out.println(" Start Element: " + elementName);
         System.out.println(" Start Local:   " + localName);
         */
        
        Object val = null;
        
        if( elementName.equals("adg") ) {
            val = _xml2ADG.processADG(attributes);
        } else if( elementName.equals("parameter") ) {
            val = _xml2ADG.processParameter(attributes);
        } else if( elementName.equals("node") ) {
            val = _xml2ADG.processNode(attributes);
        } else if( elementName.equals("inport") ) {
            val = _xml2ADG.processInPort(attributes);
        } else if( elementName.equals("outport") ) {
            val = _xml2ADG.processOutPort(attributes);
        } else if( elementName.equals("function") ) {
            val = _xml2ADG.processFunction(attributes);
        } else if( elementName.equals("edge") ) {
            val = _xml2ADG.processEdge(attributes);
        } else if( elementName.equals("domain") ) {
            val = _xml2ADG.processDomain(attributes);
        } else if( elementName.equals("linearbound") ) {
            val = _xml2ADG.processLinearBound(attributes);
        } else if( elementName.equals("filterset") ) {
            val = _xml2ADG.processFilterSet(attributes);
        } else if( elementName.equals("invariable") ) {
            val = _xml2ADG.processInVariable(attributes);
        } else if( elementName.equals("outvariable") ) {
            val = _xml2ADG.processOutVariable(attributes);
        } else if( elementName.equals("bindvariable") ) {
            val = _xml2ADG.processBindVariable(attributes);
        } else if( elementName.equals("inargument") ) {
            val = _xml2ADG.processInArgument(attributes);
        } else if( elementName.equals("outargument") ) {
            val = _xml2ADG.processOutArgument(attributes);
        } else if( elementName.equals("constraint") ) {
            val = _xml2ADG.processConstraint(attributes);
        } else if( elementName.equals("context") ) {
            val = _xml2ADG.processContext(attributes);
        } else if( elementName.equals("control") ) {
            val = _xml2ADG.processControl(attributes);
        } else if( elementName.equals("mapping") ) {
            val = _xml2ADG.processMapping(attributes);
        } else if( elementName.equals("linearization") ) {
            val = _xml2ADG.processLinearization(attributes);
        } else if( elementName.equals("doc") ) {
            _currentCharData = new StringBuffer();
        } else {
            System.out.println(
                               " -- Warning, Espam doesn't "
                                   + "understand tag <"
                                   + elementName
                                   + "> ");
        }
        
        if( val != null ) {
            _stack.push(val);
        }
    }
    
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }
    
    /**
     * @exception  SAXException MyException If such and such occurs
     */
    public void endDocument() throws SAXException {
    }
    
    /**
     * @param  elementName Description of the Parameter
     * @exception  SAXException MyException If such and such occurs
     */
    public void endElement(
                           String namespaceURI,
                           String localName,
                           String elementName)
        throws SAXException {
        
        /*
         System.out.println(" End URI:     " + namespaceURI);
         System.out.println(" End Element: " + elementName);
         System.out.println(" End qName:   " + qualifiedName);
         */
        
        if( elementName.equals("adg") ) {
            _xml2ADG.processADG(_stack);
        } else if( elementName.equals("parameter") ) {
            _xml2ADG.processParameter(_stack);
        } else if( elementName.equals("node") ) {
            _xml2ADG.processNode(_stack);
        } else if( elementName.equals("inport") ) {
            _xml2ADG.processInPort(_stack);
        } else if( elementName.equals("outport") ) {
            _xml2ADG.processOutPort(_stack);
        } else if( elementName.equals("function") ) {
            _xml2ADG.processFunction(_stack);
        } else if( elementName.equals("edge") ) {
            _xml2ADG.processEdge(_stack);
        } else if( elementName.equals("domain") ) {
            _xml2ADG.processDomain(_stack);
        } else if( elementName.equals("linearbound") ) {
            _xml2ADG.processLinearBound(_stack);
        } else if( elementName.equals("filterset") ) {
            _xml2ADG.processFilterSet(_stack);
        } else if( elementName.equals("invariable") ) {
            _xml2ADG.processInVariable(_stack);
        } else if( elementName.equals("outvariable") ) {
            _xml2ADG.processOutVariable(_stack);
        } else if( elementName.equals("bindvariable") ) {
            _xml2ADG.processBindVariable(_stack);
        } else if( elementName.equals("inargument") ) {
            _xml2ADG.processInArgument(_stack);
        } else if( elementName.equals("outargument") ) {
            _xml2ADG.processOutArgument(_stack);
        } else if( elementName.equals("constraint") ) {
            _xml2ADG.processConstraint(_stack);
        } else if( elementName.equals("context") ) {
            _xml2ADG.processContext(_stack);
        } else if( elementName.equals("control") ) {
            _xml2ADG.processControl(_stack);
        } else if( elementName.equals("mapping") ) {
            _xml2ADG.processMapping(_stack);
        } else if( elementName.equals("linearization") ) {
            _xml2ADG.processLinearization(_stack);
        }
        
    }
    
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    
    /**
     * @param  buf Description of the Parameter
     * @param  offset Description of the Parameter
     * @param  len Description of the Parameter
     * @exception  SAXException MyException If such and such occurs
     */
    public void characters(char buf[], int offset, int len)
        throws SAXException {
        // NOTE:  this doesn't escape '&' and '<', but it should
        // do so else the output isn't well formed XML.  to do this
        // right, scan the buffer and write '&amp;' and '&lt' as
        // appropriate.
        
        // If we haven't initialized _currentCharData, then we don't
        // care about character data, so we ignore it.
        if (_currentCharData != null) {
            _currentCharData.append(buf, offset, len);
        }
    }
    
    /**
     * @param  buf Description of the Parameter
     * @param  offset Description of the Parameter
     * @param  len Description of the Parameter
     * @exception  SAXException MyException If such and such occurs
     */
    public void ignorableWhitespace(char buf[], int offset, int len)
        throws SAXException {
        // this whitespace ignorable ... so we ignore it!
        
        // this callback won't be used consistently by all parsers,
        // unless they read the whole DTD.  Validating parsers will
        // use it, and currently most SAX nonvalidating ones will
        // also; but nonvalidating parsers might hardly use it,
        // depending on the DTD structure.
    }
    
    /**
     * @param  target Description of the Parameter
     * @param  data Description of the Parameter
     * @exception  SAXException MyException If such and such occurs
     */
    public void processingInstruction(String target, String data)
        throws SAXException {
        System.out.println(" Processing Instruction ");
    }
    
    /**
     * @param  l The new documentLocator value
     */
    public void setDocumentLocator(Locator l) {
        // we'd record this if we needed to resolve relative URIs
        // in content or attributes, or wanted to give diagnostics.
    }
    
    public void skippedEntity(String name) throws SAXException {
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /**
     *  Return the orignal filename without any file extension and
     *  regardless of the wether a file of http reference is used.
     *
     * @param  absoluteFileName the absolute filename.
     * @return  the filename.
     */
    private String _getFileName(String absoluteFileName) {
        
        String fileSep = System.getProperty("file.separator");
        String file = absoluteFileName.replace(fileSep.charAt(0), '/') + '/';
        if( file.charAt(0) != '/' ) {
            file = "/" + file;
        }
        
        StringTokenizer st = new StringTokenizer(file, "/");
        int count = st.countTokens();
        for( int i = 0; i < count - 1; i++ ) {
            st.nextToken();
        }
        String fullFileName = st.nextToken();
        System.out.println(" -- full filename: " + fullFileName);
        
        // Strip ".xml" if needed
        st = new StringTokenizer(fullFileName, ".");
        String fileName = st.nextToken();
        
        System.out.println(" -- filename: " + fileName);
        
        //UserInterface.getInstance().setAbsoluteURL( baseURL );
        _ui.setFileName(fileName);
        
        return fileName;
    }
    
    /**
     *  Return a absolute URL reference for the given URL.
     *
     * @param  url the url
     * @return  Description of the Return Value
     * @exception  MalformedURLException MyException If such and such occurs
     */
    private String _makeAbsoluteURL(String url) throws MalformedURLException {
        URL baseURL;
        String currentDirectory = System.getProperty("user.dir");
        String fileSep = System.getProperty("file.separator");
        String file = currentDirectory.replace(fileSep.charAt(0), '/') + '/';
        if( file.charAt(0) != '/' ) {
            file = "/" + file;
        }
        baseURL = new URL("file", null, file);
        return new URL(baseURL, url).toString();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  The XML Parser.
     */
    private XMLReader _parser;
    
    /**
     *  Stack containing realized Objects so far.
     */
    private Stack _stack;
    
    /**
     *  Stack containing realized Objects so far.
     */
    private Xml2ADG _xml2ADG;
    
    /**
     *  The current character data for the doc tag.
     */
    private StringBuffer _currentCharData;
    
    /**
     *  Instance of the ESPAM user interface.
     */
    private UserInterface _ui = UserInterface.getInstance();
}

