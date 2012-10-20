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
import java.util.Iterator;

import java.net.URL;

import espam.datamodel.implementationdata.ImplementationTable;
import espam.datamodel.platform.Platform;
import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MFifo;
import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.*;

import espam.datamodel.graph.Edge;

import espam.operations.ConsistencyCheck;
import espam.operations.SynthesizeCDPN;
import espam.operations.SynthesizePlatform;
import espam.operations.scheduler.Scheduler;

import espam.parser.xml.implementationdata.XmlImplementationDataParser;
import espam.parser.xml.platform.XmlPlatformParser;
import espam.parser.xml.pn.XmlPNParser;
import espam.parser.xml.adg.XmlADGParser;
import espam.parser.xml.sadg.XmlSADGParser;
import espam.parser.xml.mapping.XmlMappingParser;
import espam.parser.matlab.scheduler.Parser;

import espam.visitor.dot.platform.PlatformDotVisitor;
import espam.visitor.dot.cdpn.CDPNDotVisitor;
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
import espam.visitor.ise.CompaanHWNodeIseVisitor;
import espam.visitor.ise.IseNetworkVisitor;
import espam.visitor.ipxact.platform.IpxactDwarvVisitor;
import espam.visitor.systemc.untimed.ScUntimedNetworkVisitor;
import espam.visitor.systemc.timed.ScTimedNetworkVisitor;
import espam.visitor.hdpc.HdpcNetworkVisitor;

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
 * @version $Id: Main.java,v 1.17 2012/04/23 17:32:55 nikolov Exp $
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

		// Load the XML file containing the implementation data
		ImplementationTable implTable = new ImplementationTable();
		if (_ui.getScTimedFlag()) {
			try {
				XmlImplementationDataParser parserImpldata = new XmlImplementationDataParser();
				if (_ui.getImplDataFileName() != null) {
					implTable = parserImpldata.doParse(_ui.getImplDataFileName());
				}
				else {
					String userHomeDir = System.getProperty("user.home");
					implTable = parserImpldata.doParse(userHomeDir + "/.daedalus/impldata.xml");
				}
			}
			catch (Exception e) {
				System.err.println("Warning: could not load implementation data: " + e.getMessage() + "\n");
			}
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

//			      XmlSADGParser parserSADG = new XmlSADGParser();
//			      _sadg = parserSADG.doParse( _ui.getADGFileName() );
//			      _adg = (ADGraph) _sadg.get(0);

// sadg(0) contains the adg and sadg(1) contains the ast
			      Vector sadg_0 = new Vector();
		      	      Iterator i = _ui.getADGFileNames().iterator();
			      while( i.hasNext() ) {
				  XmlSADGParser parserSADG = new XmlSADGParser();
				  String fileName = (String) i.next();
				  //_sadg = parserSADG.doParse( fileName );
				  Vector sadgTemp = new Vector();
				  sadgTemp = parserSADG.doParse( fileName );
				  sadg_0.add(sadgTemp.get(0));
				  sadg_0.add(sadgTemp.get(1));
			      }

// in case of a single application, _ui file name (.kpn) is used as the name of the folder where the output is generated	
			      if(_ui.getADGFileNames().size() > 1) {
				   _ui.setFileName("xps_project");
 			           _sadg = _merge( sadg_0 );
			      } else {
			      	   _sadg.add(sadg_0.get(0));
			      	   _sadg.add(sadg_0.get(1));
			      }

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

			/* Update the FIFO sizes in the ADGGraph if dynamic scheduling is used */
			Vector<MFifo> mfifos = _mapping.getFifoList();
			Iterator<MFifo> mit = mfifos.iterator();
			while (mit.hasNext()) {
				MFifo mfifo = mit.next();
				Iterator<Edge> it = _adg.getEdgeList().iterator();
				while (it.hasNext()) {
					ADGEdge e = (ADGEdge)it.next();
					if (e.getName().equals(mfifo.getName())) {
						e.setSize(mfifo.getSize());
					}
				}
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

			} else if (_ui.getIseFlag()) {

                                  System.out.println(" - Generating System in Xilinx ISE format");
                                  IseNetworkVisitor iseNetworkVisitor = new IseNetworkVisitor(_mapping);
                                 _platform.accept(iseNetworkVisitor);

                                  // Use a separate visitor to obtain HDL for the HWnodes; this is to make sure we don't get duplicate eval_logic units
                                  // etc.
				  CompaanHWNodeIseVisitor hwNodeVisitor = new CompaanHWNodeIseVisitor(_mapping);
				  _platform.accept(hwNodeVisitor);
				  System.out.println(" - Generation [Finished]");

			} else if (_ui.getIpxactFlag()) {
                                  System.out.println(" - Generating System in IP-XACT format");

                                  IpxactDwarvVisitor ipxactDwarvVisitor = new IpxactDwarvVisitor(_mapping);
                                  _platform.accept(ipxactDwarvVisitor);

				  System.out.println(" - Generation [Finished]");
			} else if (_ui.getScUntimedFlag()) {

                                  System.out.println(" - Generating untimed SystemC model");
                                  printStream = _openFile(_cdpn.getName() + "_KPN", "h");
                                  ScUntimedNetworkVisitor scUntimedVisitor = new ScUntimedNetworkVisitor(printStream);
                                  _cdpn.accept(scUntimedVisitor);
				  System.out.println(" - Generation [Finished]");

			} else if (_ui.getScTimedFlag()) {
                                  System.out.println(" - Generating timed SystemC model");
	  
	                          boolean _scTimedPeriod = _ui.getScTimedPeriodFlag(); 
                                  //printStream = _openFile(_cdpn.getName() + "_KPN", "h");
                                  ScTimedNetworkVisitor scTimedVisitor = new ScTimedNetworkVisitor(_mapping, implTable, _scTimedPeriod);
                                 _cdpn.accept(scTimedVisitor);
				  System.out.println(" - Generation [Finished]");

			} else if( _ui.getHdpcFlag() ) {

	 			 System.out.println(" - Generating System in HDPC format");
				 HdpcNetworkVisitor pnVisitor = new HdpcNetworkVisitor( _cdpn );
				 _cdpn.accept(pnVisitor);
				 System.out.println(" - Generation [Finished]");
			} 

			if( _ui.getDotFlag() ) {

                                  System.out.println("\n - Generating CDPN in dot format");
                                  printStream = _openFile(_cdpn.getName() + "_KPN", "dot");
		                  CDPNDotVisitor dotVisitor = new CDPNDotVisitor( printStream );
		                  _cdpn.accept(dotVisitor);
			          System.out.println(" - Generation [Finished]\n");
			}

			if( _ui.getDebugFlag() ) {

			    System.out.println(" - Generating ADG in XML format");
	                    printStream = _openFile(_adg.getName() + "_ESPAM", "adg");
			    ADGraphXmlVisitor xmlVisitor = new ADGraphXmlVisitor( printStream );
			    _adg.accept(xmlVisitor);
			    System.out.println(" - Generation [Finished]\n");

			    System.out.println(" - Generating Platform in dot format");
                            printStream = _openFile(_cdpn.getName() + "_ESPAM_PLA", "dot");
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

                            System.out.println("\n - Generating CDPN in dot format");
                            printStream = _openFile(_cdpn.getName() + "_ESPAM_KPN", "dot");
                            CDPNDotVisitor dotVisitor1 = new CDPNDotVisitor( printStream );
	                    _cdpn.accept(dotVisitor1);
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
//	    fullFileName = _ui.getPlatformFileName() + "." + extension;
	    fullFileName = fileName + "." + extension;
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


    /**
     * @param  sadg (vector of n ADGs and ASTs) Description of the Parameter
     * @return _sadg (2-element vector, 1 ADG adn 1 AST) Description of the Return Value
     * merge all ADGs into one ADG placed at _sadg(0). 
     * merge all ASTs into one AST placed at _sadg(1). 
     */
    private static Vector _merge( Vector sadg ) {
// sadg is a vector organized as ADG1, AST1, ADG2, AST2, etc.

	Vector sadgNew = new Vector();
	ADGraph adg = new ADGraph( _ui.getFileName() );
	RootStatement ast = new RootStatement();

	Iterator i = sadg.iterator();
	while( i.hasNext() ) {

		ADGraph adgTemp = (ADGraph) i.next();
		String adgName = adgTemp.getName();

// Merge the adg graphs
// Rename and add all parameters, nodes, and edges into one adg graph
		Iterator j = adgTemp.getParameterList().iterator();
		while( j.hasNext() ) {
			ADGParameter adgParameter = (ADGParameter) j.next();
			adgParameter.setName( adgName + "_" + adgParameter.getName() );
			adg.getParameterList().add(adgParameter);	
		}

		j = adgTemp.getNodeList().iterator();
		while( j.hasNext() ) {
			ADGNode adgNode = (ADGNode) j.next();
			adgNode.setName( adgName + "_" + adgNode.getName() );

                        // Make the names of the ports unique as well
                        Iterator p = adgNode.getPortList().iterator(); 
			while( p.hasNext() ) {
                            ADGPort adgPort = (ADGPort)p.next();
                            adgPort.setName( adgName + "_" + adgPort.getName() );


			}
			adg.getNodeList().add(adgNode);	
		}

		j = adgTemp.getEdgeList().iterator();
		while( j.hasNext() ) {
			ADGEdge adgEdge = (ADGEdge) j.next();
			adgEdge.setName( adgName + "_" + adgEdge.getName() );
			adg.getEdgeList().add(adgEdge);	
		}

// Merge the schedules
		ParserNode nodeTemp = (ParserNode) i.next();
		_parseTreeNodesRename( nodeTemp, adgName );
		ast.addChild(nodeTemp);
	}

	sadgNew.add(adg);
	sadgNew.add(ast);
	return( sadgNew );
    }


    /**
     *  Rename the AssignStatements field adgNodeName to the new node name: adgName+"_"+adgNodeName
     *
     */
    private static void _parseTreeNodesRename(ParserNode node, String adgName) {

        if (node instanceof AssignStatement) {

		((AssignStatement) node).setNodeName( adgName + "_" + ((AssignStatement) node).getNodeName() );
//		System.out.println( ((AssignStatement) node).getNodeName() );
        }

        Iterator j = node.getChildren();
        while (j.hasNext()) {
            _parseTreeNodesRename((ParserNode) j.next(), adgName);
        }
    }


	///////////////////////////////////////////////////////////////////
	//// private variables ///

	private static Platform _platform = null;

	private static ADGraph _adg = null;

	private static Vector _sadg = new Vector();

        private static CDProcessNetwork _cdpn = null;

        private static Mapping _mapping = null;

        private static ParserNode _scheduler = null;

	private static UserInterface _ui = null;
}

