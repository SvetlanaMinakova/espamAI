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

package espam.visitor.xps.mapping;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.EspamException;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDGate;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MFifo;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.MemoryMap;
import espam.datamodel.platform.processors.Page;

import espam.main.UserInterface;

import espam.visitor.MappingVisitor;

//////////////////////////////////////////////////////////////////////////
//// XpsMemoryMapVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Hristo Nikolov,Todor Stefanov
 * @version  $Id: XpsMemoryMapVisitor.java,v 1.1 2007/12/07 22:07:34 stefanov Exp $
 */

public class XpsMemoryMapVisitor extends MappingVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the XpsMemoryMapVisitor object
     */
    public XpsMemoryMapVisitor() 
        throws FileNotFoundException,EspamException { 

	    	_ui = UserInterface.getInstance();
	        if (_ui.getOutputFileName() == "") {
		    _codeDir = _ui.getBasePath() + "/" + _ui.getFileName() + "/code";
	        } else {
		    _codeDir = _ui.getBasePath() + "/" + _ui.getOutputFileName() + "/code";
	        }
	        File dir = new File(_codeDir);
	        if( !dir.exists() ) {
	            if( !dir.mkdirs() ) {
	                throw new EspamException(
	                    "could not create "
	                        + "directory '"
	                        + dir.getPath()
	                        + "'.");
	            }
	        }
	        _printStream = _openFile("MemoryMap", "h");
	        _printStream.println("// File automatically generated by ESPAM");
	        _printStream.println("");
	        _printStream.println("");

    }
     
	/**
	 *  Visit a Mapping component.
	 *
	 * @param  x A Visitor Object.
	 */
    public void visitComponent( Mapping x ) {
        // Generate the individual processes
        try {

            _mapping= x;
            
	        _printStream.println("#ifndef __MEMORYMAP_H_");
	    	_printStream.println("#define __MEMORYMAP_H_");
	    	_printStream.println("");

            Iterator i = x.getPlatform().getResourceList().iterator();
            while( i.hasNext() ) {

            	Resource resource = (Resource) i.next();
                
                if (resource instanceof Processor) {

                    Processor processor = (Processor) resource;
                    Iterator j = processor.getMemoryMapList().iterator();
                    
                    while( j.hasNext() ) {
                    	
                    	MemoryMap memoryMap = (MemoryMap) j.next();
                    	Iterator k = memoryMap.getPageList().iterator();
                    	
                    	while( k.hasNext() ) {
                    		
                    		Page page = (Page) k.next();
                    		
                    		if (page.getReadResource() instanceof Fifo) {
                    			
                    			Fifo fifo = (Fifo) page.getReadResource();
                    			CDChannel cdChannel = x.getCDChannel(fifo);
                    			CDGate cdGate = (CDGate)cdChannel.getToGate();
                    			ADGEdge adgEdge = (ADGEdge)cdChannel.getAdgEdgeList().get(0);
                    			ADGPort adgPort = (ADGPort)adgEdge.getToPort();
                    			ADGNode adgNode = (ADGNode)adgPort.getNode();
                    			
                    			String cdChannelName = cdChannel.getName();
                    			String adgNodeName = adgNode.getName();
                    			String cdGateName = cdGate.getName();
                    			
                    			String s = adgNodeName + "_" + cdGateName + "_" + cdChannelName;
                    			String baseAddress;
                    			if (page.getSize() == 0) {
                    				baseAddress = Integer.toString(page.getBaseAddress());
                    			}
                    			else {
                    				baseAddress = "0x" + _digitToStringHex(page.getBaseAddress(), 8);
                    			}
                    			
                    			_printStream.println("//" + processor.getName() + " FIFOs");
                    			_printStream.println("#define " + s + " "
                    					   + baseAddress + " //read from CDChannel" + cdChannelName + " address for " + processor.getName());
                    			_printStream.println("");
                    			
                    		}
                    		else if (page.getReadResource().getName() != "") {
                    			String s = page.getReadResource().getName();
                    			String baseAddress = "0x" + _digitToStringHex(page.getBaseAddress(), 8);
                    			_printStream.println("#define " + s + " "
                 					   + baseAddress + " //read from " + page.getReadResource().getName() + " address for " + processor.getName());
                 			    _printStream.println("");	
                    		}
                    		
                    		if (page.getWriteResource() instanceof Fifo) {
                    		
                    			Fifo fifo = (Fifo) page.getWriteResource();
                    			CDChannel cdChannel = x.getCDChannel(fifo);
                    			CDGate cdGate = (CDGate)cdChannel.getFromGate();
                    			ADGEdge adgEdge = (ADGEdge)cdChannel.getAdgEdgeList().get(0);
                    			ADGPort adgPort = (ADGPort)adgEdge.getFromPort();
                    			ADGNode adgNode = (ADGNode)adgPort.getNode();
                    			
                    			String cdChannelName = cdChannel.getName();
                    			String adgNodeName = adgNode.getName();
                    			String cdGateName = cdGate.getName();
                    			
                    			String s = adgNodeName + "_" + cdGateName + "_" + cdChannelName;
                    			String baseAddress;
                    			if (page.getSize() == 0) {
                    				baseAddress = Integer.toString(page.getBaseAddress());
                    			}
                    			else {
                    				baseAddress = "0x" + _digitToStringHex(page.getBaseAddress(), 8);
                    			}
                    			
                    			_printStream.println("//" + processor.getName() + " FIFOs");
                    			_printStream.println("#define " + s + " "
                    					   + baseAddress + " //write to CDChannel" + cdChannelName + " address for " + processor.getName());
                    			_printStream.println("");
                    			
                    		}
                    		else if (page.getWriteResource().getName() != "") {
                    			String s = page.getWriteResource().getName();
                    			String baseAddress = "0x" + _digitToStringHex(page.getBaseAddress(), 8);
                    			_printStream.println("#define " + s + " "
                 					   + baseAddress + " //write to " + page.getWriteResource().getName() + " address for " + processor.getName());
                 			    _printStream.println("");
                    		}
                    	}
                    }
                	
                }
            }
            
            if( _ui.getDebuggerFlag() ) {
            	
            	_printStream.println("#define clk_cntr (volatile int *)0xf8000000");
     		    
     		}
            _printStream.println("#define ZBT_MEM (volatile int *)0xf0000000");
            _printStream.println("#define FIN_SIGNAL (volatile int *)0xf9000000");
            _printStream.println("");
            _printStream.println("#endif");

        }
        catch( Exception e ) {
            System.out.println(" In Xps MemoryMap Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }

	/**
	 *  Visit a MFifo component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MFifo x) {
	}

	/**
	 *  Visit a MProcess component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MProcess x) {
	}

	/**
	 *  Visit a MProcessor component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MProcessor x) {
	}

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
	
    /**
     *  conver to hexical string
     *  @param xLong long value to be changed
     *  @param format length of the digit format
     */
    private String _digitToStringHex(int xInt, int format) {
        String binStr = Integer.toHexString(xInt);
        int binStrlength = binStr.length();
        if (format < binStrlength) {
            System.out.println(
                "Error!!!!: The value can not be represented as " + format + " digit hex");
        }
        String returnStr = new String();
        for (int i = 0; i < (format - binStrlength); i++) {
            returnStr = returnStr + '0';
        }
        returnStr = returnStr + binStr;
        return returnStr;
    }
    
    /**
     *  Description of the Method
     * @param  subDirName Descripton of the subdirectory
     * @param  fileName Description of the Parameter
     * @param  extension Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openFile( 
					 String fileName, 
					 String extension)
            throws FileNotFoundException {
        PrintStream printStream = null;
        String fullFileName = "";
	    String fullDirName = "";

        System.out.println(" -- OPEN FILE: " + fileName);

	    fullDirName = _codeDir;
	    fullFileName = fullDirName + "/" + fileName + "." + extension;
        if (fileName.equals("")) {
            printStream = new PrintStream(System.out);
        } else {
            OutputStream file = null;
            file = new FileOutputStream(fullFileName);
            printStream = new PrintStream(file);
        }
        return printStream;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     * Repository directory for the source codes
     */
    private static String _codeDir = "";
    
    private Mapping _mapping = null;
    
    /**
     *  The UserInterface
     */
    private UserInterface _ui = null;

    private PrintStream _printStream = null;

}
