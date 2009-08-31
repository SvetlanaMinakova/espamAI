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

package espam.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Vector;

import java.net.URL;

import espam.datamodel.platform.Platform;
import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.parsetree.ParserNode;

import espam.operations.ConsistencyCheck;
import espam.operations.SynthesizeCDPN;
import espam.operations.SynthesizePlatform;
import espam.operations.scheduler.Scheduler;

import espam.parser.xml.platform.XmlPlatformParser;
import espam.parser.xml.pn.XmlPNParser;
import espam.parser.xml.adg.XmlADGParser;
import espam.parser.xml.sadg.XmlSADGParser;
import espam.parser.xml.mapping.XmlMappingParser;
import espam.parser.matlab.scheduler.Parser;

import espam.visitor.dot.platform.PlatformDotVisitor;
import espam.visitor.xml.adg.ADGraphXmlVisitor;
import espam.visitor.xml.cdpn.CDPNXmlVisitor;
import espam.visitor.yapiPN.YapiNetworkVisitor;
import espam.visitor.yapiPN.YapiStatementVisitor;
import espam.visitor.ymlPN.YmlNetworkVisitor;
import espam.visitor.xps.cdpn.XpsNetworkVisitor;
import espam.visitor.xps.cdpn.XpsStatementVisitor;
import espam.visitor.xps.cdpn.XmpVisitor;
import espam.visitor.xps.mapping.XpsMemoryMapVisitor;
import espam.visitor.xps.platform.MhsVisitor;
import espam.visitor.xps.platform.MssVisitor;
import espam.visitor.xps.platform.CompaanHWNodeVisitor;
import espam.visitor.xps.platform.FifoCtrlVisitor;
import espam.visitor.xps.platform.CrossbarVisitor;
//import espam.visitor.hdpc.HdpcNetworkVisitor;

import espam.datamodel.EspamException;

/////////////////////////////////////////////////////////////////////////////////
//// Main

/**
 * The Embedded System-level Platform synthesis and Application Mapping tool.
 *
 * This class represents the main controlling part of ESPAM. In this
 * class, the command line options are processed, the input and output
 * files are set, and the complete E-SPAM compilation cycle is
 * done. Also, this class is the final responder to Exception occurring
 * within ESPAM.
 *
 * @author Todor Stefanov
 * @version $Id: Main.java,v 1.5 2009/08/31 16:42:17 nikolov Exp $
 */

public class Main {

	//////////////////////////////////////////////////////////////////
	//// public methods ///

	/**
	 * The main method of this class
	 *
	 * @param args The arguments to provide to ESPAM.
	 */
	public static void main(String[] args) {

		// the user interface
		_ui = UserInterface.getInstance();

		System.out.println("********************************************************************************");
		System.out.println("* ESPAM: Embedded System-level Platform synthesis and Application Mapping Tool *");
		System.out.println("********************************************************************************");
		System.out.println(_ui.getCopyright());

		//        if ( !VerifyLicense.getInstance().isLicensed()) {
		//               System.exit(1);
		//        }

		// process the Command Line options
		try {

			Options io = new Options(args);

		} catch( NumberFormatException e ) {
			System.out.println("Error in Command line options: "
					+ " the numerial format for an argument is incorrect. "
					+ " Message: "
					+ e.getMessage());
			System.exit(-1);
		} catch( IllegalArgumentException e ) {
			System.out.println("Error in Command line option: "
					+ e.getMessage());
			System.exit(-1);
		} catch( Exception e ) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}

                // the main compilation cycle
		try {
			PrintStream printStream;


			// Load the XML file containing the platform specification
			// and parse it  using an XML Parser
			XmlPlatformParser parserPlatform = new XmlPlatformParser();
			_platform = parserPlatform.doParse( _ui.getPlatformFileName() );


			if (_ui.getNetworkFileName() != null ) {
			      // Load the XML file containing the process network specification
			      // and parse it using an XML Parser
			      XmlPNParser parserPN = new XmlPNParser();
			      _adg = parserPN.doParse( _ui.getNetworkFileName() );
			} else {
                              // Load the XML file containing the process network specification
			      // and parse it using an XML Parser

			      //XmlADGParser parserADG = new XmlADGParser();
			      //_adg = parserADG.doParse( _ui.getADGFileName() );

			      XmlSADGParser parserSADG = new XmlSADGParser();
			      _sadg = parserSADG.doParse( _ui.getADGFileName() );
			      _adg = (ADGraph) _sadg.get(0);

			}

			// Load the XML file containing the mapping specification
			// and parse it using an XML Parser
			XmlMappingParser parserMapping = new XmlMappingParser();
			_mapping = parserMapping.doParse( _ui.getMappingFileName() );


			if (_ui.getSchedulerFileName() != null ) {
          			/* Load the scheduler file containing the SCHEDULE
	         		* model using a Matlab Parser.
		        	*/
			       URL url = null;
			       InputStream inputFileStream = null;
			       try {
			            // Open the input and output file
			            url = new URL("file", null, _ui.getSchedulerFileName());
			            inputFileStream = url.openStream();
			       } catch (IOException e) {
			            url = new URL(_ui.getSchedulerFileName());
			            inputFileStream = url.openStream();
			       }

			       _scheduler = Parser.getParseTree((InputStream) inputFileStream);
			       Scheduler.getInstance().setScheduleTree( _scheduler, "fromMatlab" );
                        } else {
			       _scheduler = (ParserNode) _sadg.get(1);
			       Scheduler.getInstance().setScheduleTree( _scheduler, "fromSADG" );
			}


			// Check for consistency the platform, process network, and mapping specs
			ConsistencyCheck.getInstance().consistencyCheck( _platform, _adg, _mapping );

			// Synthesize process network from input specifications
			_cdpn = SynthesizeCDPN.getInstance().synthesizeCDPN( _adg, _mapping );

			// Synthesize platform from input specifications;
			// Generates the mapping in case of empty input mapping specification
			SynthesizePlatform.getInstance().synthesizePlatform( _platform, _mapping );

			if( _ui.getYapiFlag() ) {
				System.out.println(" - Generating CDPN in Yapi format");
				printStream = _openFile(_cdpn.getName() + "_KPN", "h");
				YapiNetworkVisitor pnVisitor = new YapiNetworkVisitor(printStream);
				_cdpn.accept(pnVisitor);
				System.out.println(" - Generation [Finished]");
                        } else if( _ui.getYmlFlag() ) {
				System.out.println(" - Generating CDPN in Yml format");
				String directory = "";
				String dirName = "app";
				if (_ui.getOutputFileName() == "") {
				    directory = _ui.getBasePath() + "/" + _ui.getFileName() + "/" + dirName;
				} else {
				    directory = _ui.getBasePath() + "/" + _ui.getOutputFileName() + "/" + dirName;
				}
				File dir = new File( directory );
				dir.mkdirs();
				printStream = _openFile(dirName + "/" + _cdpn.getName() + "_app", "yml");
				YmlNetworkVisitor ymlVisitor = new YmlNetworkVisitor(printStream);
				_cdpn.accept(ymlVisitor);
				System.out.println(" - Generation [Finished]");
			} else if( _ui.getXpsFlag() ) {
				  System.out.println(" - Generating System in Xps format");

				  //Always this Visitor should be called first!!!
				  XpsNetworkVisitor xpsVisitor = new XpsNetworkVisitor(_mapping);
				  _cdpn.accept(xpsVisitor);

				  XmpVisitor xmpVisitor = new XmpVisitor(_mapping);
				  _cdpn.accept(xmpVisitor);

				  XpsMemoryMapVisitor memoryMapVisitor = new XpsMemoryMapVisitor();
				  _mapping.accept(memoryMapVisitor);

				  MhsVisitor mhsVisitor = new MhsVisitor(_mapping);
				  _platform.accept(mhsVisitor);

				  MssVisitor mssVisitor = new MssVisitor(_mapping);
				  _platform.accept(mssVisitor);

				  CompaanHWNodeVisitor hwNodeVisitor = new CompaanHWNodeVisitor(_mapping);
				  _platform.accept(hwNodeVisitor);

				  FifoCtrlVisitor fifoCtrlVisitor = new FifoCtrlVisitor();
				  _platform.accept(fifoCtrlVisitor);

				  CrossbarVisitor crossbarVisitor = new CrossbarVisitor();
				  _platform.accept(crossbarVisitor);

				  System.out.println(" - Generation [Finished]");
			
			} else if( _ui.getHdpcFlag() ) {
				//System.out.println(" - Generating System in HDPC format");
				//HdpcNetworkVisitor pnVisitor = new HdpcNetworkVisitor( _cdpn );
				//_cdpn.accept(pnVisitor);
				//System.out.println(" - Generation [Finished]");
			} else {

			}

			if( _ui.getDebugFlag() ) {

			    System.out.println(" - Generating ADG in XML format");
	                    printStream = _openFile(_adg.getName() + "_ESPAM", "adg");
			    ADGraphXmlVisitor xmlVisitor = new ADGraphXmlVisitor( printStream );
			    _adg.accept(xmlVisitor);
			    System.out.println(" - Generation [Finished]\n");

			    System.out.println(" - Generating Platform in Dotty format");
                            printStream = _openFile("dummy", "dot");
		            PlatformDotVisitor dotVisitor = new PlatformDotVisitor( printStream );
		            _platform.accept(dotVisitor);
			    System.out.println(" - Generation [Finished]\n");

	                    System.out.println(" - Generating CDPN in XML format");
	                    printStream = _openFile(_cdpn.getName() + "_ESPAM", "kpn");
			    CDPNXmlVisitor pnXmlVisitor = new CDPNXmlVisitor( printStream );
			    _cdpn.accept( pnXmlVisitor );
			    System.out.println(" - Generation [Finished]\n");

                            System.out.println(" - Generating Scheduler in C/C++ format");
			    printStream = _openFile(_cdpn.getName() + "_ESPAM", "sch");
			    YapiStatementVisitor schVisitor = new YapiStatementVisitor(printStream, _cdpn.getName());
			    _scheduler.accept(schVisitor);
			    System.out.println(" - Generation [Finished]\n");

			}

		} catch( EspamException e ) {
			System.out.println(" ESPAM Message: " + e.getMessage());
			e.printStackTrace(System.out);
		} catch( NumberFormatException e ) {
			System.out.println(" ERROR Occured in ESPAM: " + e.getMessage());
			e.printStackTrace(System.out);
		} catch( Exception e ) {
			System.out.println(" ESPAM caught an exception\n " + e.getMessage());
			e.printStackTrace(System.out);
		}
	}

	///////////////////////////////////////////////////////////////////
	//// private methods ///
    /**
     * @param  fileName Description of the Parameter
     * @param  extension Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException MyException If such and such occurs
     * @exception  PandaException MyException If such and such occurs
     */
    private static PrintStream _openFile(String fileName, String extension)
        throws FileNotFoundException, EspamException {

        PrintStream printStream = null;
	String fullFileName = "";

        if( extension.equals("dot") ) {

	    fullFileName = _ui.getPlatformFileName() + "." + extension;

        } else if(extension.equals("adg") ) {

	    fullFileName = fileName + "." + extension;

	} else if( extension.equals("kpn") ) {

	    fullFileName = fileName + "." + extension;

//            fullFileName = ui.getOutputFileName() + "." + extension;
//        } else if (extension.equals("kpn")) {
//            fullFileName = ui.getFileName() + "." + extension;
//        } else if (extension.equals("txt")) {
//            fullFileName = ui.getOutputFileName() + "." + extension;
	} else if( extension.equals("sch") ) {

	    fullFileName = fileName + "." + extension;

        } else {
            String directory = null;
            // Create the directory if it does not exist. Create the
            // directory indicated by the '-o' option. Otherwise
            // select the orignal filename.
            if (_ui.getOutputFileName() == "") {
                directory = _ui.getBasePath() + "/" + _ui.getFileName();
            } else {
                directory = _ui.getBasePath() + "/" + _ui.getOutputFileName();
            }

            File dir = new File(directory);
            if( !dir.exists() ) {
                if( !dir.mkdirs() ) {
                    throw new EspamException(
                        "could not create "
                            + "directory '"
                            + dir.getPath()
                            + "'.");
                }
            }
            fullFileName = dir + "/" + fileName + "." + extension;

        }
        System.out.println(" -- OPEN FILE: " + fullFileName);

        try {
            if( fileName.equals("") ) {
                printStream = new PrintStream(System.out);
            } else {
                OutputStream file = null;
                file = new FileOutputStream(fullFileName);
                printStream = new PrintStream(file);
            }
        } catch( SecurityException e ) {
            System.out.println(" Security Issue: " + e.getMessage());
        }
        return printStream;
    }

	///////////////////////////////////////////////////////////////////
	//// private variables ///

	private static Platform _platform = null;

	private static ADGraph _adg = null;

	private static Vector _sadg = null;

        private static CDProcessNetwork _cdpn = null;

        private static Mapping _mapping = null;

        private static ParserNode _scheduler = null;

	private static UserInterface _ui = null;
}

