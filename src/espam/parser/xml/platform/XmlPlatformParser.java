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

package espam.parser.xml.platform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import espam.datamodel.platform.Platform;
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
//// XmlPlatformParser

/**
 *  This class ...
 *
 * @author  Todor Stefanov
 * @version  $Id: XmlPlatformParser.java,v 1.2 2010/04/02 12:21:25 nikolov Exp $
 */

public class XmlPlatformParser implements ContentHandler {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 * The constructor
	 */
	public XmlPlatformParser() {
		super();

		_stack = new Stack();
		_xml2Platform = (Xml2Platform) Xml2Platform.getInstance();

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
	 * Do the parsing of an XML stream describing a platform
	 *
	 * @param  stream the input XML strem
	 * @return  the platform
	 * @exception  EspamException MyException If such and such occurs
	 */
	public Platform doParse(InputStream stream) throws EspamException {
		Platform platform = null;

		_parser.setContentHandler(this);
		_parser.setErrorHandler(new XmlErrorHandler());

		try {
			_parser.parse(new InputSource(stream));
			platform = (Platform) _stack.pop();
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

		// Return the platform
		return platform;
	}

	/**
	 * Do the parsing of an XML file describing a platform
	 *
	 * @param  url The input XML file
	 * @return  the platform
	 * @exception  EspamException MyException If such and such occurs
	 */
	public Platform doParse(String url) throws EspamException {

		Platform platform = null;

		System.out.println(" - Read Platform from XML file");

		_parser.setContentHandler(this);
		_parser.setErrorHandler(new XmlErrorHandler());

		try {
			// Get only the file name from the URL.
			String docString = _getFileName(url);
			String uri = _makeAbsoluteURL(url);

			_ui.printlnVerbose(" -- processing XML file: " + uri);
			_ui.printVerbose(" -- read XML file: ");

			_parser.parse(new InputSource(uri));

			platform = (Platform) _stack.pop();

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

		System.out.println(" - Platform Model from XML [Constructed]");
		System.out.println();

		// Return the platform
		return platform;
	}


	/**
	 * @param  text Description of the Parameter
	 * @return  Description of the Return Value
	 * @exception  PandaException MyException If such and such occurs
	 */
	public Platform parse(String text) throws Exception {
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


		if( elementName.equals("platform") ) {
			val = _xml2Platform.processPlatform(attributes);
		} else if( elementName.equals("subplatform") ) {
			val = _xml2Platform.processSubplatform(attributes);
		} else if( elementName.equals("processor") ) {
			val = _xml2Platform.processProcessor(attributes);
		} else if( elementName.equals("peripheral") ) {
			val = _xml2Platform.processPeripheral(attributes);
		} else if( elementName.equals("network") ) {
			val = _xml2Platform.processNetwork(attributes);
		} else if( elementName.equals("memory") ) {
			val = _xml2Platform.processMemory(attributes);
		} else if( elementName.equals("host_interface") ) {
			val = _xml2Platform.processHostInterface(attributes);
		} else if( elementName.equals("link") ) {
			val = _xml2Platform.processLink(attributes);
		} else if( elementName.equals("port") ) {
			val = _xml2Platform.processPort(attributes);
		} else if( elementName.equals("vfifo") ) {
			val = _xml2Platform.processVfifo(attributes);
		} else if( elementName.equals("resource") ) {
			val = _xml2Platform.processResource(attributes);
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

		if( elementName.equals("platform") ) {
			_xml2Platform.processPlatform(_stack);
		} else if( elementName.equals("subplatform") ) {
			_xml2Platform.processSubplatform(_stack);
		} else if( elementName.equals("processor") ) {
			_xml2Platform.processProcessor(_stack);
		} else if( elementName.equals("peripheral") ) {
			_xml2Platform.processPeripheral(_stack);
		} else if( elementName.equals("network") ) {
			_xml2Platform.processNetwork(_stack);
		} else if( elementName.equals("memory") ) {
			_xml2Platform.processMemory(_stack);
		} else if( elementName.equals("host_interface") ) {
			_xml2Platform.processHostInterface(_stack);
		} else if( elementName.equals("link") ) {
			_xml2Platform.processLink(_stack);
		} else if( elementName.equals("port") ) {
			_xml2Platform.processPort(_stack);
		} else if( elementName.equals("vfifo") ) {
			_xml2Platform.processVfifo(_stack);
		} else if( elementName.equals("resource") ) {
			_xml2Platform.processResource(_stack);
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
	private Xml2Platform _xml2Platform;

	/**
	 *  The current character data for the doc tag.
	 */
	private StringBuffer _currentCharData;

	/**
	 *  Instance of the ESPAM user interface.
	 */
	private UserInterface _ui = UserInterface.getInstance();
}
