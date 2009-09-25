/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2009 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.ipxact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Stack;




//////////////////////////////////////////////////////////////////////////
//// IP-XACT XML Writer

/**
 * This class provides some methods that simplify writing out IP-XACT.
 *
 * This class was developed as part of the SoftSoC project.
 *
 * @author Sven van Haastregt
 * @version $Id: IpxactXmlWriter.java,v 1.1 2009/09/25 15:23:34 sven Exp $
 */

public class IpxactXmlWriter {

  // /////////////////////////////////////////////////////////////////
  // // public methods ///

  /**
   * Constructor
   */
  public IpxactXmlWriter(String filename) throws FileNotFoundException {
    _xmlPS = _openFile(filename);
    _elementStack = new Stack<String>();
  }

  /**
   * Close file
   */
  public void close() {
    if (!_elementStack.empty()) {
      System.out.println("Warning: IpxactXmlWriter: Element stack was not empty upon close() invocation.");
    }
    _xmlPS.close();
  }


  /**
   * Writes the start of a document
   */
  public void writeStartDocument() {
    _xmlPS.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  }


  /**
   * Writes element and value. Produces one line of XML.
   */
  public void writeElement(String element, String value) {
    writeElement(element, "", value);
  }


  /**
   * Writes element with attributes and value. Produces one line of XML.
   */
  public void writeElement(String element, String attributes, String value) {
    _writeIndent();
    _xmlPS.print("<spirit:" + element);
    if (!attributes.equals("")) {
      _xmlPS.print(" " + attributes);
    }
    _xmlPS.println(">" + value + "</spirit:" + element + ">");

  }


  /**
   * Writes start tag of element. This is typically used for elements containing other elements.
   */
  public void writeStartElement(String element) {
    writeStartElement(element, "");
  }


  /**
   * Writes start tag of element and attributes. This is typically used for elements containing other elements.
   */
  public void writeStartElement(String element, String attributes) {
    _writeIndent();
    _indentInc();
    _xmlPS.print("<spirit:" + element);
    if (!attributes.equals("")) {
      _xmlPS.print(" " + attributes);
    }
    _xmlPS.println(">");
    _elementStack.push(element);
  }


  /**
   * Writes end tag of current element, based on internal state
   */
  public void writeEndElement() {
    _indentDec();
    _writeIndent();
    _xmlPS.println("</spirit:" + _elementStack.pop() + ">");
  }


  /**
   * Starts a top-level component element.
   *
   * @param element According to IP-XACT v1.4 specification, this should be one of busDefinition, abstractionDefinition,
   * component, design, abstractor, generatorChain or designConfiguration.
   */
  public void writeStartTopElement(String element) {
    writeStartElement(element, "xmlns:spirit=\"http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + 
    "xsi:schemaLocation=\"http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4 http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4/index.xsd \"");
  }






  // /////////////////////////////////////////////////////////////////
  // // private methods ///

  /**
   * Open a file to write
   * 
   * @param fileName
   *            the fullpath file name
   */
  private PrintStream _openFile(String fileName) throws FileNotFoundException {
    PrintStream ps = null;

    System.out.println(" -- OPEN FILE: " + fileName);

    if (fileName.equals(""))
      ps = new PrintStream(System.out);
    else
      ps = new PrintStream(new FileOutputStream(fileName));

    return ps;
  }


  /**
   * Writes newline and indent before a new tag is written.
   */
  private void _writeIndent() {
    _xmlPS.print(_indent);
  }

  /**
   * Increases indentation level
   */
  private void _indentInc() {
    _indent += _indentStep;
  }

  /**
   * Decreases indentation level
   */
  private void _indentDec() {
    _indent = _indent.substring(_indentStep.length());
  }
  
  
  
  // /////////////////////////////////////////////////////////////////
  // // private variables ///
  private String _indent = "";

  private String _indentStep = "  ";

  private PrintStream _xmlPS;

  private Stack<String> _elementStack;
  
}
