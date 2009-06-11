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

package espam.visitor.xps.cdpn;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;

import espam.datamodel.parsetree.ParserNode;

import espam.main.UserInterface;

import espam.visitor.CDPNVisitor;

import espam.datamodel.LinearizationType;

//////////////////////////////////////////////////////////////////////////
//// XpsProcessVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Hristo Nikolov,Todor Stefanov, Joris Huizer
 * @version  $Id: XpsProcessVisitor.java,v 1.4 2009/06/11 13:18:14 stefanov Exp $
 */

public class XpsProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the XpsProcessVisitor object
     */
    public XpsProcessVisitor( Mapping mapping ) {
    	_mapping = mapping;
    	_ui = UserInterface.getInstance();
        if (_ui.getOutputFileName() == "") {
	    _codeDir = _ui.getBasePath() + "/" + _ui.getFileName() + "/code";
        } else {
	    _codeDir = _ui.getBasePath() + "/" + _ui.getOutputFileName() + "/code";
        }
	    File dir = new File(_codeDir);
	    dir.mkdirs();
    }
     
    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {
        // Generate the individual processes
        try {

            _pn = x;
            _printStreamFunc = _openFile("aux_func", "h");
            _printStreamFunc.println("#ifndef __AUX_FUNC_H__");
            _printStreamFunc.println("#define __AUX_FUNC_H__");
            _printStreamFunc.println("");
            _printStreamFunc.println("#include <math.h>");
            _printStreamFunc.println("#include \"mb_interface.h\"");
            _printStreamFunc.println("#include \"./func_code/" + x.getName() + "_func.h\"");	    
            _printStreamFunc.println("");

            _writeChannelTypes();
            _printStreamFunc.println("");
	    _writeParameter(x);

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

                CDProcess process = (CDProcess) i.next();

                MProcessor mProcessor = _mapping.getMProcessor(process);
                Resource resource = mProcessor.getResource();
                if (resource instanceof Processor) {

                    _printStream = _openFile(process.getName(), process.getName(), "cpp");
		    
		    if ( mProcessor.getScheduleType() == 1 ) {
			XpsDynamicXilkernelProcessVisitor pt = new XpsDynamicXilkernelProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
			process.accept(pt);
		    }
		    else {
			XpsStaticProcessVisitor pt = new XpsStaticProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
			process.accept(pt);
		    }
                }
            }

            _printStreamFunc.println("");
            _writeOperations();
            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");

        }
        catch( Exception e ) {
            System.out.println(" In Xps PN Visitor: exception " +
                    "occured: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Description of the Method
     *
     * @param  fileName Description of the Parameter
     * @param  extension Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openFile( String fileName, String extension )
            throws FileNotFoundException {

        PrintStream printStream = null;
        String fullFileName = "";

        System.out.println(" -- OPEN FILE: " + fileName);

        fullFileName = _codeDir + "/" + fileName + "." + extension;
        if (fileName.equals("")) {
            printStream = new PrintStream(System.out);
        } else {
            OutputStream file = null;
            file = new FileOutputStream(fullFileName);
            printStream = new PrintStream(file);
        }
        
        return printStream;
        
    }
    
    /**
     *  Description of the Method
     * @param  subDirName Descripton of the subdirectory
     * @param  fileName Description of the Parameter
     * @param  extension Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openFile(String subDirName, 
					 String fileName, 
					 String extension)
            throws FileNotFoundException {
        PrintStream printStream = null;
        String fullFileName = "";
	    String fullDirName = "";

        System.out.println(" -- OPEN FILE: " + fileName);

	    fullDirName = _codeDir + "/" + subDirName;
	    fullFileName = fullDirName + "/" + fileName + "." + extension;
        if (fileName.equals("")) {
            printStream = new PrintStream(System.out);
        } else {
	        File dir = new File(fullDirName);
	        dir.mkdirs();
            OutputStream file = null;
            file = new FileOutputStream(fullFileName);
            printStream = new PrintStream(file);
        }
        return printStream;
    }

    
    /**
     *  Description of the Method
     */
    private void _writeChannelTypes() {

        CDChannel channel;
	String type;

        Iterator i = _pn.getChannelList().iterator();
        while( i.hasNext() ) {
           channel = (CDChannel) i.next();
	   type = ((ADGVariable) ((ADGEdge)channel.getAdgEdgeList().get(0)).getFromPort().getBindVariables().get(0)).getDataType();

	   if( !type.equals("") ) {
	       String s = "typedef " + type + " t" + channel.getName()+";";
               _printStreamFunc.println( s );
           } else {
               String s = "typedef char t" + channel.getName()+";";
               _printStreamFunc.println( s );
           }
        }
    }


    /**
     *  Declare the public parameters
     * @param  x Description of the Parameter
     */
    private void _writeParameter(CDProcessNetwork x) {
	    _printStreamFunc.println("// Parameters");
        Iterator j = _pn.getAdg().getParameterList().iterator();
        while (j.hasNext()) {
            ADGParameter p = (ADGParameter) j.next();
	        _printStreamFunc.println(_prefix + "#define " + p.getName() + 
				     " " + p.getValue());
        }
        _printStreamFunc.println("");
    }
    
    /**
     *  Description of the Method
     */
    private void _writeOperations() {
        _printStreamFunc.println("#define min(a,b) ((a)<=(b))?(a):(b)");
        _printStreamFunc.println("#define max(a,b) ((a)>=(b))?(a):(b)");
	_printStreamFunc.println("");

        _printStreamFunc.println("inline int ddiv(int a, int b ){");
        _printStreamFunc.println("    //return (int)(a/b);");
        _printStreamFunc.println("    return ( (int) (((a)<0) ? ((a)-(b)+1)/(b) : (a)/(b)) ); ");
        _printStreamFunc.println("    //return ( (int) (((a)<0)^((b)<0) ? ((a) < 0 ? ((a)-(b)+1)/(b) : ((a)-(b)-1)/(b)) : (a)/(b)) ); ");
        _printStreamFunc.println("}\n");

        _printStreamFunc.println("inline int mod(int a, int b){");
        _printStreamFunc.println("    return mod(a, b);\n}\n");

        _printStreamFunc.println("inline int ceil1(int a){");
        _printStreamFunc.println("    return a; /* return (int) ceil(a);*/\n}\n");

        _printStreamFunc.println("inline int floor1(int a){");
        _printStreamFunc.println("    return a; /* return (int) floor(a);*/\n}\n");

        _printStreamFunc.println(_fifoReadWriteApi);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     * Repository directory for the source codes
     */
    private static String _codeDir = "";
    
    /**
     *  The UserInterface
     */
    private UserInterface _ui = null;
    
    private Mapping _mapping = null;

    private CDProcessNetwork _pn = null;

    private PrintStream _printStream = null;
    
    private PrintStream _printStreamFunc = null;

    private Map _relation2 = new HashMap();
    
    /**
     *  Read/Write fifo api
     */
    private String _fifoReadWriteApi = "" +
	"#define readFSL(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        for (i = 0; i < len; i++) \\\n" +
	"            microblaze_bread_datafsl(((volatile int *) value)[i], pos);\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define writeFSL(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        for (i = 0; i < len; i++)  \\\n" +
	"            microblaze_bwrite_datafsl(((volatile int *) value)[i], pos);\\\n"+
	"    } while(0)\n" +
	"\n" +
	"#define read(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isEmpty;\\\n" +
	"        volatile int *inPort = (volatile int *)pos;\\\n" +
 	"        isEmpty = inPort + 1;\\\n" +
	"        for (i = 0; i < len; i++) {\\\n" +
	"            while (*isEmpty) { };\\\n" +
	"            ((volatile int *) value)[i] = *inPort;\\\n" +
	"        }\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define write(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isFull;\\\n" +
	"        volatile int *outPort = (volatile int *)pos;\\\n" +
	"        isFull = outPort + 1;\\\n" +
	"        for (i = 0; i < len; i++) {\\\n" +
	"            while (*isFull) { };\\\n" +
	"            *outPort = ((volatile int *) value)[i];\\\n" +
	"        }\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define readDyn(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isEmpty;\\\n" +
	"        volatile int *inPort = (volatile int *)pos;\\\n" +
 	"        isEmpty = inPort + 1;\\\n" +
	"        for (i = 0; i < len; i++) {\\\n" +
	"            while (*isEmpty) { yield(); };\\\n" +
	"            ((volatile int *) value)[i] = *inPort;\\\n" +
	"        }\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define writeDyn(pos, value, len) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isFull;\\\n" +
	"        volatile int *outPort = (volatile int *)pos;\\\n" +
	"        isFull = outPort + 1;\\\n" +
	"        for (i = 0; i < len; i++) {\\\n" +
	"            while (*isFull) { yield(); };\\\n" +
	"            *outPort = ((volatile int *) value)[i];\\\n" +
	"        }\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define readMF(pos, value, n) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isEmpty;\\\n" +
	"        int inPort = (int) pos;\\\n" +
	"        volatile int *dataReg_requestReg = (volatile int *) 0xE0000000;\\\n" +
 	"        isEmpty = dataReg_requestReg + 1;\\\n" +
 	"        *dataReg_requestReg = 0x80000000|(inPort);\\\n" +
	"        for (i = 0; i < n; i++) {\\\n" +
	"            while (*isEmpty) { };\\\n" +
	"            ((volatile int *) value)[i] = *dataReg_requestReg;\\\n" +
	"        }\\\n" +
	"        *dataReg_requestReg = 0x7FFFFFFF&(inPort);\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define writeMF(pos, value, n) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isFull;\\\n" +
	"        volatile int *outPort = (volatile int *)pos;\\\n" +
	"        isFull = outPort + 1;\\\n" +
	"        for (i = 0; i < n; i++) {\\\n" +
	"            while (*isFull) { };\\\n" +
	"            *outPort = ((volatile int *) value)[i];\\\n" +
	"        }\\\n" +
	"    } while(0)\n";

}
