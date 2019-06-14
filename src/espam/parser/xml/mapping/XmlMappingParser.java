
package espam.parser.xml.mapping;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import espam.datamodel.mapping.Mapping;
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
//// XmlMappingParser

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: XmlMappingParser.java,v 1.2 2012/04/19 21:54:19 mohamed Exp $
 */

public class XmlMappingParser implements ContentHandler {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     * The constructor
     */
    public XmlMappingParser() {
        super();
        
        _stack = new Stack();
        _xml2Mapping = (Xml2Mapping) Xml2Mapping.getInstance();
        
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
     * Do the parsing of an XML stream describing a mapping
     *
     * @param  stream the input XML strem
     * @return  the mapping
     * @exception  EspamException MyException If such and such occurs
     */
    public Mapping doParse(InputStream stream) throws EspamException {
        Mapping mapping = null;
        
        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());
        
        try {
            _parser.parse(new InputSource(stream));
            mapping = (Mapping) _stack.pop();
        } catch( SAXParseException err ) {
            System.out.println(
                               "** Parsing error"
                                   + ", line "
                                   + err.getLineNumber()
                                   + ", uri "
                                   + err.getSystemId());
            System.out.println("   " + err.getMessage());
        } catch( SAXException e ) {
            Exception x = e;
            if( e.getException() != null ) {
                x = e.getException();
            }
            x.printStackTrace();
        } catch( Throwable t ) {
            t.printStackTrace();
        }
        
        // Return the mapping
        return mapping;
    }
    
    /**
     * Do the parsing of an XML file describing a mapping
     *
     * @param  url The input XML file
     * @return  the mapping
     * @exception  EspamException MyException If such and such occurs
     */
    public Mapping doParse(String url) throws EspamException {
        
        Mapping mapping = null;
        
        System.out.println(" - Read Mapping from XML file");
        
        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());
        
        try {
            // Get only the file name from the URL.
            String docString = _getFileName(url);
            String uri = _makeAbsoluteURL(url);
            
            _ui.printlnVerbose(" -- processing XML file: " + uri);
            _ui.printVerbose(" -- read XML file: ");
            
            _parser.parse(new InputSource(uri));
            
            mapping = (Mapping) _stack.pop();
            
            // All done
            _ui.printlnVerbose(" [DONE] ");
            
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
        
        System.out.println(" - Mapping Model from XML [Constructed]");
        System.out.println();
        
        // Return the mapping
        return mapping;
    }

        /**
     * Do the parsing of an XML file describing a mapping
     *
     * @param  url The input XML file
     * @return  the mapping
     * @exception  EspamException MyException If such and such occurs
     */
    public Mapping doParse(String url, boolean verbose) throws EspamException {

        Mapping mapping = null;

        if(verbose)
            System.out.println(" - Read Mapping from XML file");

        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());

        try {
            // Get only the file name from the URL.
            String docString = _getFileName(url,verbose);
            String uri = _makeAbsoluteURL(url);

            if(verbose) {
                _ui.printlnVerbose(" -- processing XML file: " + uri);
                _ui.printVerbose(" -- read XML file: ");
            }

            _parser.parse(new InputSource(uri));

            mapping = (Mapping) _stack.pop();


            // All done
            if(verbose)
                _ui.printlnVerbose(" [DONE] ");

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

        if(verbose) {
            System.out.println(" - Mapping Model from XML [Constructed]");
            System.out.println();
        }

        // Return the mapping
        return mapping;
    }
    
    
    /**
     * @param  text Description of the Parameter
     * @return  Description of the Return Value
     * @exception  PandaException MyException If such and such occurs
     */
    public Mapping parse(String text) throws Exception {
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
     * Action to be done while parsing a start element of an XML
     *
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
        
        
        if( elementName.equals("mapping") ) {
            val = _xml2Mapping.processMapping(attributes);
        } else if( elementName.equals("processor") ) {
            val = _xml2Mapping.processProcessor(attributes);
        } else if( elementName.equals("process") ) {
            val = _xml2Mapping.processProcess(attributes);
        } else if( elementName.equals("fifo") ) {
            val = _xml2Mapping.processFifo(attributes);   
        } else if( elementName.equals("doc") ) {
            _currentCharData = new StringBuffer();
        } else {
            System.out.println(
                               " -- Warning, ESPAM doesn't "
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
     *  Action to be done while parsing an end element of an XML
     *
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
        
        if( elementName.equals("mapping") ) {
            _xml2Mapping.processMapping(_stack);
        } else if( elementName.equals("processor") ) {
            _xml2Mapping.processProcessor(_stack);
        } else if( elementName.equals("process") ) {
            _xml2Mapping.processProcess(_stack);
        } else if( elementName.equals("fifo") ) {
            _xml2Mapping.processFifo(_stack);
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
        if( _currentCharData != null ) {
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
        
        //_ui.setPlatformFileName(fileName);
        
        return fileName;
    }

    /**
     *  Return the orignal filename without any file extension and
     *  regardless of the wether a file of http reference is used.
     *
     * @param  absoluteFileName the absolute filename.
     * @return  the filename.
     */
    private String _getFileName(String absoluteFileName, boolean verbose) {

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
        if(verbose)
            System.out.println(" -- full filename: " + fullFileName);

        // Strip ".xml" if needed
        st = new StringTokenizer(fullFileName, ".");
        String fileName = st.nextToken();

        if(verbose)
            System.out.println(" -- filename: " + fileName);

        //_ui.setPlatformFileName(fileName);

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
     *  Stack containing the generated Objects so far.
     */
    private Stack _stack;
    
    /**
     *  The actions to be taken while parsing every XML element.
     */
    private Xml2Mapping _xml2Mapping;
    
    /**
     *  The current character data for the doc tag.
     */
    private StringBuffer _currentCharData;
    
    /**
     *  Instance of the ESPAM user interface.
     */
    private UserInterface _ui = UserInterface.getInstance();
}

