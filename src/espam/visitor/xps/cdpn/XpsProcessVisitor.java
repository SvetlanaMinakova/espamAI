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
 * @version  $Id: XpsProcessVisitor.java,v 1.14 2012/05/02 16:31:20 mohamed Exp $
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
	    _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getFileName() + File.separatorChar + "SDK";
        } else {
	    _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getOutputFileName() + File.separatorChar + "SDK";
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

            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {

                CDProcess process = (CDProcess) i.next();

                MProcessor mProcessor = _mapping.getMProcessor(process);            

                _printStreamFunc = _openFile(process.getName(), "aux_func", "h");
                _printStreamFunc.println("#ifndef __AUX_FUNC_H__");
                _printStreamFunc.println("#define __AUX_FUNC_H__");
                _printStreamFunc.println("");
                _printStreamFunc.println("#include <math.h>");
                _printStreamFunc.println("#include <mb_interface.h>");
                _printStreamFunc.println("#include \"func_code" + File.separatorChar + x.getName() + "_func.h\"");
                _printStreamFunc.println("");

                _writeChannelTypes();
                _printStreamFunc.println("");
	            _writeParameter(x);


                Resource resource = mProcessor.getResource();
                if (resource instanceof Processor) {

                    _printStream = _openFile(process.getName(), process.getName(), "cpp");

		            if ( mProcessor.getScheduleType() == 1 ) {
            			XpsDynamicXilkernelProcessVisitor pt = new XpsDynamicXilkernelProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
            			process.accept(pt);
		            }
		            else if ( mProcessor.getScheduleType() == 2 ) {
			            XpsDynamicFreeRTOSProcessVisitor pt = new XpsDynamicFreeRTOSProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
			            process.accept(pt);
		            }
		            else {
			            XpsStaticProcessVisitor pt = new XpsStaticProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
			            process.accept(pt);
		            }
                }
                _printStreamFunc.println("");
                _writeOperations();
                _printStreamFunc.println("");
                _printStreamFunc.println("#endif");
                _printStreamFunc.close();
                
                
                _printStreamPlatform = _openFile(process.getName(), "platform", "h");
                _printPlatformFile();
                _printStreamPlatform.close();                
            }

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

        fullFileName = _codeDir + File.separatorChar + fileName + "." + extension;
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

	    fullDirName = _codeDir + File.separatorChar + subDirName;
	    fullFileName = fullDirName + File.separatorChar + fileName + "." + extension;
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
    
    
    private void _printPlatformFile() {
        _printStreamPlatform.println(_platformFile);
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
    
    private PrintStream _printStreamPlatform = null;

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
	"            while (*isEmpty != 2) { };\\\n" +
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
	"    } while(0)\n" +
	"\n" +
	"#define readDynMF(pos, value, n) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isEmpty;\\\n" +
	"        int inPort = (int) pos;\\\n" +
	"        volatile int *dataReg_requestReg = (volatile int *) 0xE0000000;\\\n" +
	"        isEmpty = dataReg_requestReg + 1;\\\n" +
	"        *dataReg_requestReg = 0x80000000|(inPort);\\\n" +
	"        for (i = 0; i < n; i++) {\\\n" +
	"            while (*isEmpty != 2) {\\\n" +
	"                if( *isEmpty == 3 ) {\\\n" +
	"                    *dataReg_requestReg = 0x7FFFFFFF&(inPort);\\\n" +
	"                    yield();\\\n" +
    "                    *dataReg_requestReg = 0x80000000|(inPort);\\\n" +
	"                }\\\n" +
	"            }\\\n" +
	"            ((volatile int *) value)[i] = *dataReg_requestReg;\\\n" +
	"        }\\\n" +
	"        *dataReg_requestReg = 0x7FFFFFFF&(inPort);\\\n" +
	"    } while(0)\n" +
	"\n" +
	"#define writeDynMF(pos, value, n) \\\n" +
	"    do {\\\n" +
	"        int i;\\\n" +
	"        volatile int *isFull;\\\n" +
	"        volatile int *outPort = (volatile int *)pos;\\\n" +
	"        isFull = outPort + 1;\\\n" +
	"        for (i = 0; i < n; i++) {\\\n" +
	"            while (*isFull) { yield(); };\\\n" +
	"            *outPort = ((volatile int *) value)[i];\\\n" +
	"        }\\\n" +
	"    } while(0)\n\n" +
	"\n///////////////////////////////////// Primitives for SW FIFOs \n" +

	"#define readSWF(pos, value, len, fifo_size) \\\n" +
	"    do {\\\n" +
	"       volatile int *fifo = (int *)pos;\\\n" +
	"       int r_cnt = fifo[1];\\\n" +
	"       while (1) {\\\n" +
	"            int w_cnt = fifo[0];\\\n" +
	"            if ( w_cnt != r_cnt ) {\\\n" +
	"                for (int i = 0; i < len; i++) {\\\n" +
	"                     ((volatile int *) value)[i] = fifo[(r_cnt & 0x7FFFFFFF) + 2 + i];\\\n" +
	"                }\\\n" +
	"                r_cnt += len;\\\n" +
	"                if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
	"                     r_cnt &= 0x80000000;\\\n" +
	"                     r_cnt ^= 0x80000000;\\\n" +
	"                }\\\n" +
	"                fifo[1] = r_cnt;\\\n" +
	"                break;\\\n" +
	"            }\\\n" +
	"       }\\\n" +
	"    } while(0)\n\n" +

	"#define writeSWF(pos, value, len, fifo_size) \\\n" +
	"    do {\\\n" +
	"       volatile int *fifo = (int *)pos;\\\n" +
	"       int w_cnt = fifo[0];\\\n" +
	"       while (1) {\\\n" +
	"            int r_cnt = fifo[1];\\\n" +
	"            if ( r_cnt != (w_cnt ^ 0x80000000) ) {\\\n" +
	"                for (int i = 0; i < len; i++) {\\\n" +
	"                     fifo[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((volatile int *) value)[i];\\\n" +
	"                }\\\n" +
	"                w_cnt += len;\\\n" +
	"                if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
	"                     w_cnt &= 0x80000000;\\\n" +
	"                     w_cnt ^= 0x80000000;\\\n" +
	"                }\\\n" +
	"                fifo[0] = w_cnt;\\\n" +
	"                break;\\\n" +
	"            }\\\n" +
	"       }\\\n" +
	"    } while(0)\n\n" +

	"\n////////// currently not used //////// Primitives for SW FIFOs \n" +
	"inline volatile void *acquire_write_ptr(int f, int len) {\n" +
	"	volatile long *fifo = (long *)f;\n" +
	"	register long fifoSize = fifo[0];\n" +
	"	register long fifo_2 = fifo[2];\n" +
	"\n" +
	"	while( (fifo_2^fifo[5]) == 0x80000000) { }; // full\n" +
	"\n" +
	"	void *ptr = (void *)(fifo + 6 + (fifo_2 & 0x7FFFFFFF));\n" +
	"\n" +
	"	fifo_2 += len;      // wr index + token size in dwords (32 bits)\n" +
	"\n" +
	"	if( (fifo_2 & 0x7FFFFFFF) == fifoSize ) { \n" +
	"		fifo_2 = fifo_2 & 0x80000000;\n" +
	"		fifo_2 = fifo_2 ^ 0x80000000; // toggle the flag\n" +
	"	}\n" +
	"\n" +
	"	fifo[2] = fifo_2;\n" +
	"\n" +
	"	return ptr;\n" +
	"}\n" +
	"\n\ninline void release_write_ptr(int f) {" +
	"\n	volatile long *fifo = (volatile long *)f;\n" +
	"	fifo[3] = fifo[2];\n" +
	"}\n" +
	"\n\ninline volatile void *acquire_read_ptr(int f, int len) {" +
	"\n	volatile long *fifo = (long *)f;" +
	"\n	register long fifoSize = fifo[0];" +
	"\n	register long fifo_4 = fifo[4];" +
	"\n" +
	"\n	while( fifo[3] == fifo_4 ) { }; // empty" +
	"\n" +
	"\n	void *ptr = (void *)(fifo + 6 + (fifo_4 & 0x7FFFFFFF));" +
	"\n" +
	"\n	fifo_4 += len;      // rd index + token size in dwords (32 bits)" +
	"\n" +
	"\n	if( (fifo_4 & 0x7FFFFFFF) == fifoSize ) {" +
	"\n		fifo_4 = fifo_4 & 0x80000000;" +
	"\n		fifo_4 = fifo_4 ^ 0x80000000; // toggle the flag" +
	"\n	}" +
	"\n	fifo[4] = fifo_4;" +
	"\n" +
	"\n	return ptr;" +
	"\n}" +
	"\n\ninline void release_read_ptr(int f) {" +
	"\n	volatile int *fifo = (volatile int *)f;" +
	"\n	fifo[5] = fifo[4];" +
	"\n}" +
    "\n\n// Read and Write primitives for Xilkernel \n" +
    "#define readSWF_Dyn1(pos, value, len, fifo_size) \\\n" +
    "do {\\\n" +
    "   volatile int *fifo = (int *)pos;\\\n" +
    "   int r_cnt = fifo[1];\\\n" +
    "   int w_cnt = fifo[0];\\\n" +
    "   while ( w_cnt == r_cnt ) { yield(); w_cnt = fifo[0]; }\\\n" +
    "   for (int i = 0; i < len; i++) {\\\n" +
    "	   ((volatile int *) value)[i] = fifo[(r_cnt & 0x7FFFFFFF) + 2 + i];\\\n" +
    "   }\\\n" +
    "   r_cnt += len;\\\n" +
    "   if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
    "	   r_cnt &= 0x80000000;\\\n" +
    "	   r_cnt ^= 0x80000000;\\\n" +
    "   }\\\n" +
    "   fifo[1] = r_cnt;\\\n" +
    "} while(0)\n\n" +
    
    "#define writeSWF_Dyn1(pos, value, len, fifo_size) \\\n" +
    "do {\\\n" +
    "   volatile int *fifo = (int *)pos;\\\n" +
    "   int w_cnt = fifo[0];\\\n" +
    "   int r_cnt = fifo[1];\\\n" +
    "   while ( r_cnt == (w_cnt ^ 0x80000000) ) { yield(); r_cnt = fifo[1]; }\\\n" +
    "   for (int i = 0; i < len; i++) {\\\n" +
    "	   fifo[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((volatile int *) value)[i];\\\n" +
    "   }\\\n" +
    "   w_cnt += len;\\\n" +
    "   if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
    "	   w_cnt &= 0x80000000;\\\n" +
    "       w_cnt ^= 0x80000000;\\\n" +
    "   }\\\n" +
    "   fifo[0] = w_cnt;\\\n" +
    "} while(0)\n\n" +
    
    "\n\n// Read and Write primitives for FreeRTOS \n" +
    "#define readSWF_Dyn2(pos, value, len, fifo_size, S, T) \\\n" +
    "do {\\\n" +
    "   volatile int *fifo = (int *)pos;\\\n" +
    "   int r_cnt = fifo[1];\\\n" +
    "   int w_cnt = fifo[0];\\\n" +
    "   while ( w_cnt == r_cnt ) { vTaskDelayUntil(&S,T); w_cnt = fifo[0]; }\\\n" +
    "   for (int i = 0; i < len; i++) {\\\n" +
    "	   ((volatile int *) value)[i] = fifo[(r_cnt & 0x7FFFFFFF) + 2 + i];\\\n" +
    "   }\\\n" +
    "   r_cnt += len;\\\n" +
    "   if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
    "	   r_cnt &= 0x80000000;\\\n" +
    "	   r_cnt ^= 0x80000000;\\\n" +
    "   }\\\n" +
    "   fifo[1] = r_cnt;\\\n" +
    "} while(0)\n\n" +

    "#define writeSWF_Dyn2(pos, value, len, fifo_size, S, T) \\\n" +
    "do {\\\n" +
    "   volatile int *fifo = (int *)pos;\\\n" +
    "   int w_cnt = fifo[0];\\\n" +
    "   int r_cnt = fifo[1];\\\n" +
    "   while ( r_cnt == (w_cnt ^ 0x80000000) ) { vTaskDelayUntil(&S,T); r_cnt = fifo[1]; }\\\n" +
    "   for (int i = 0; i < len; i++) {\\\n" +
    "	   fifo[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((volatile int *) value)[i];\\\n" +
    "   }\\\n" +
    "   w_cnt += len;\\\n" +
    "   if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
    "	   w_cnt &= 0x80000000;\\\n" +
    "       w_cnt ^= 0x80000000;\\\n" +
    "   }\\\n" +
    "   fifo[0] = w_cnt;\\\n" +
    "} while(0)\n\n";
    
    
    private String _platformFile  = "" + 
    "#ifndef PLATFORM_H_\n" +
    "#define PLATFORM_H_\n\n" +
    "#include <FreeRTOS.h>\n" +
    "#include <timers.h>\n" +
    "#include <xtmrctr.h>\n\n" +
    "#define mainDONT_BLOCK\t\t\t( portTickType ) 0\n" +
    "#define TIMER_DEVICE_ID\t\t\tXPAR_TMRCTR_0_DEVICE_ID\n" +
    "#define TIMER_FREQ_HZ\t\t\tXPAR_TMRCTR_0_CLOCK_FREQ_HZ\n" +
    "#define TIMER_INTR_ID\t\t\tXPAR_INTC_0_TMRCTR_0_VEC_ID\n\n" +
    "#if defined __cplusplus\n" +
    "extern \"C\" {\n" +
    "#endif\n\n" +
    "extern void vPortTickISR( void *pvUnused );\n" +
    "static XTmrCtr xTimer0Instance;\n\n" +
    "void vApplicationMallocFailedHook( void ) { }\n" +
    "void vApplicationStackOverflowHook( xTaskHandle *pxTask, signed char *pcTaskName ) { } \n" +
    "void vApplicationIdleHook( void ) { } \n" +
    "void vApplicationTickHook( void ) { } \n" +
    "void vSoftwareTimerCallback( xTimerHandle xTimer ) { } \n\n" +
    "void isr(void *args, u8 c)\n" +
    "{\n" +
    "	vPortTickISR(args); \n" +
    "}\n\n" +
    "void vApplicationSetupTimerInterrupt( void )\n" +
    "{\n" +
    "	portBASE_TYPE xStatus;\n" +
    "	const unsigned char ucTimerCounterNumber = ( unsigned char ) 0U;\n" +
    "	const unsigned long ulCounterValue = ( ( TIMER_FREQ_HZ / configTICK_RATE_HZ ) - 1UL );\n" +
    "	xStatus = XTmrCtr_Initialize( &xTimer0Instance, TIMER_DEVICE_ID );\n" +
    "	if( xStatus == XST_SUCCESS )\n" +
	"   	xStatus = xPortInstallInterruptHandler( TIMER_INTR_ID, vPortTickISR, NULL );\n" +
    "	if( xStatus == pdPASS ) { \n" +
    "		vPortEnableInterrupt( TIMER_INTR_ID );\n" +
    "		XTmrCtr_SetHandler( &xTimer0Instance, isr, NULL );\n" +
    "		XTmrCtr_SetResetValue( &xTimer0Instance, ucTimerCounterNumber, ulCounterValue );\n" +
	"		XTmrCtr_SetOptions( &xTimer0Instance, ucTimerCounterNumber, ( XTC_INT_MODE_OPTION | XTC_AUTO_RELOAD_OPTION | XTC_DOWN_COUNT_OPTION ) );\n" +
	"		XTmrCtr_Start( &xTimer0Instance, ucTimerCounterNumber );\n" +
    "	}\n" +
    "	configASSERT( ( xStatus == pdPASS ) );\n" +
    "}\n\n" +
    "void vApplicationClearTimerInterrupt( void )\n" +
    "{\n" +
    "	unsigned long ulCSR;\n" +
    "	ulCSR = XTmrCtr_GetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0 );\n" +
    "	XTmrCtr_SetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0, ulCSR );\n" +
    "}\n\n" +
    "void init_platform() { } \n\n" +
    "#if defined __cplusplus\n" +
    "}\n" +
    "#endif\n" +
    "#endif\n";

}

