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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import espam.datamodel.implementationdata.ImplementationTable;
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
//// XmlImplementationDataParser

/**
 * Parses an ImplementationData file.
 *
 * @author Sven van Haastregt
 */

public class XmlImplementationDataParser implements ContentHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * The constructor
     */
    public XmlImplementationDataParser() {
        super();

        _stack = new Stack<Object>();
        _xml2ImplementationTable = (Xml2ImplementationTable) Xml2ImplementationTable.getInstance();

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
     * Do the parsing of an XML stream.
     *
     * @param  stream the input XML stream
     * @return  the ImplementationTable
     */
    public ImplementationTable doParse(InputStream stream) throws EspamException {
        ImplementationTable table = null;

        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());

        try {
            _parser.parse(new InputSource(stream));
            table = (ImplementationTable) _stack.pop();
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

        return table;
    }

    /**
     * Do the parsing of an XML file describing a table
     *
     * @param  url The input XML file
     * @return  the table
     * @exception  EspamException MyException If such and such occurs
     */
    public ImplementationTable doParse(String url) throws EspamException {

        ImplementationTable table = null;

        System.out.println(" - Read ImplementationTable from XML file " + url);

        _parser.setContentHandler(this);
        _parser.setErrorHandler(new XmlErrorHandler());

        try {
            // Get only the file name from the URL.
            String uri = _makeAbsoluteURL(url);

            _ui.printlnVerbose(" -- processing XML file: " + uri);
            _ui.printVerbose(" -- read XML file: ");

            _parser.parse(new InputSource(uri));

            table = (ImplementationTable) _stack.pop();

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
            throw new EspamException(t.getMessage());
        }

        System.out.println(" - ImplementationTable Model from XML [Constructed]");
        System.out.println();

        // Return the table
        return table;
    }


    /**
     * @param  text Description of the Parameter
     * @return  Description of the Return Value
     * @exception  PandaException MyException If such and such occurs
     */
    public ImplementationTable parse(String text) throws Exception {
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
     */
    public void startElement(
            String namespaceURI,
            String localName,
            String elementName,
            Attributes attributes) throws SAXException {

        // These are the metrics and the corresponding attribute names.
        String[] attrsDelay   = new String[]{"DELAY_AVG",     "average",
                                             "DELAY_WORST",   "worstcase",
                                             "DELAY_BEST",    "bestcase"};
        String[] attrsII      = new String[]{"II",            "value"};
        String[] attrsSlices  = new String[]{"SLICES",        "value"};
        String[] attrsMemory  = new String[]{"MEMORY_DATA",   "data",
                                             "MEMORY_CODE",   "program"};

        Object val = null;

        if (elementName.equals("implementationMetrics")) {
            val = _xml2ImplementationTable.processImplementationMetrics(attributes);
        } else if (elementName.equals("functions")) {
            val = _xml2ImplementationTable.processFunctions(attributes);
        } else if (elementName.equals("function")) {
            val = _xml2ImplementationTable.processFunction(attributes);
        } else if (elementName.equals("implementation")) {
            val = _xml2ImplementationTable.processImplementation(attributes);
        } else if (elementName.equals("performance") ||
                   elementName.equals("resources") ||
                   elementName.equals("power")) {
            // Do nothing, these elements are just to group metrics in the XML.
        } else if (elementName.equals("delay")) {
            val = _xml2ImplementationTable.processStartMetric(attributes, attrsDelay);
        } else if (elementName.equals("ii")) {
            val = _xml2ImplementationTable.processStartMetric(attributes, attrsII);
        } else if (elementName.equals("slices")) {
            val = _xml2ImplementationTable.processStartMetric(attributes, attrsSlices);
        } else if (elementName.equals("memory")) {
            val = _xml2ImplementationTable.processStartMetric(attributes, attrsMemory);
        } else if (elementName.equals("doc")) {
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

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    /**
     * @exception  SAXException MyException If such and such occurs
     */
    public void endDocument() throws SAXException {
    }

    /**
     *  Action to be done while parsing an end element of an XML
     */
    public void endElement(
            String namespaceURI,
            String localName,
            String elementName) throws SAXException {

        if (elementName.equals("implementationMetrics")) {
            _xml2ImplementationTable.processImplementationMetrics(_stack);
        } else if (elementName.equals("functions")) {
            _xml2ImplementationTable.processFunctions(_stack);
        } else if (elementName.equals("function")) {
            _xml2ImplementationTable.processFunction(_stack);
        } else if (elementName.equals("implementation")) {
            _xml2ImplementationTable.processImplementation(_stack);
        } else if (elementName.equals("delay") ||
                elementName.equals("ii") ||
                elementName.equals("slices") ||
                elementName.equals("memory")) {
            _xml2ImplementationTable.processEndMetric(_stack);
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
    public void characters(char buf[], int offset, int len) throws SAXException {
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
    public void ignorableWhitespace(char buf[], int offset, int len) throws SAXException {
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
    public void processingInstruction(String target, String data) throws SAXException {
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
    private Stack<Object> _stack;

    /**
     *  The actions to be taken while parsing every XML element.
     */
    private Xml2ImplementationTable _xml2ImplementationTable;

    /**
     *  The current character data for the doc tag.
     */
    private StringBuffer _currentCharData;

    /**
     *  Instance of the ESPAM user interface.
     */
    private UserInterface _ui = UserInterface.getInstance();
}
