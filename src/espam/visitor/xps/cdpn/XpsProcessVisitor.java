
package espam.visitor.xps.cdpn;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

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
 * @version  $Id: XpsProcessVisitor.java,v 1.19 2012/05/30 10:34:22 tzhai Exp $
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
        
        _sdk_enabled = _ui.getSDKFlag();
        
        String sdk_project_name;
        String code_dir;
        
        // without using SDK backend, we generate SW code in "code", 
        // otherwise, the SW code is located in "SDK"
        if (_sdk_enabled)
            code_dir = "SDK";
        else
            code_dir = "code";
        
        if (_ui.getOutputFileName() == "") {
            _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getFileName() + File.separatorChar + code_dir;
            sdk_project_name = _ui.getFileName();
        } else {
            _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getOutputFileName() + File.separatorChar + code_dir;
            sdk_project_name = _ui.getOutputFileName();
        }
        
        
        if (_sdk_enabled) {
            _sdk = new XpsSDKVisitor(_mapping, _codeDir, sdk_project_name);
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
            _printStreamFunc.println("#include <mb_interface.h>");
            if (!_ui.getSDKFlag()) {
                _printStreamFunc.println("#include \"func_code" + File.separatorChar + x.getName() + "_func.h\"");
            } else {
                // In case of SDK, include all 
                String funcCodePath = _ui.getFuncCodePath();
                File funcCodeDir = new File(funcCodePath);
                
                Iterator adg_it = _ui.getADGFileNames().iterator();
                while (adg_it.hasNext()) {
                    String adg_filename = (String)adg_it.next();
                    String[] adg_name = adg_filename.split(".kpn");
                    _printStreamFunc.println("#include \"./" + funcCodeDir.getName() + File.separatorChar
                                                 + adg_name[0] + "_func.h\"");
                }
            }
            _printStreamFunc.println("");
            
            _writeChannelTypes();
            _printStreamFunc.println("");
            _writeParameter(x);
            
            // For SDK backend, mProcessor does not contain hostIF, therefore, we handle hostIF explicitly
            if (_sdk_enabled)
                _sdk.handleHostIF();
            
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
                    else if ( mProcessor.getScheduleType() == 2 ) {
                        XpsDynamicFreeRTOSProcessVisitor pt = new XpsDynamicFreeRTOSProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
                        process.accept(pt);
                    }
                    else {
                        XpsStaticProcessVisitor pt = new XpsStaticProcessVisitor( _mapping, _printStream, _printStreamFunc, _relation2 );
                        process.accept(pt);
                    }
                    
                    // generate SW code and SDK projects for procoessors (other than hostIF)
                    if (_sdk_enabled)
                        _sdk.visitProcessor(process);
                    
                }
            }
            
            _printStreamFunc.println("");
            _writeOperations();
            _printStreamFunc.println("");
            _printStreamFunc.println("#endif");
            _printStreamFunc.close();
            
            //creating platform.h
            if (_sdk_enabled) { // platform.h is needed only when using FreeRTOS
                String targetBoard = _sdk.getTargetBoard();

               // copy all 'static' files (that need no processing) from src/espam/libXPS/SDK/<targetboard>/
               // please notice that the folder names in src/espam/libXPS/SDK/ should have the same name as the targetboard
               
                ArrayList<String> staticFiles = new ArrayList<String>();
                
                String statisFilePath = _sdk.getPathSDK() + "SDK" + File.separatorChar;
                
                if(targetBoard.equals("ML605")){
                    statisFilePath += targetBoard + File.separatorChar; // Adding targetboard in path to include board specific files
                    staticFiles.add("platform.h");
                }else if(targetBoard.equals("ZedBoard")){
                    statisFilePath += targetBoard + File.separatorChar; // Adding targetboard in path to include board specific files
                    staticFiles.add("network.h");
                    staticFiles.add("platform.c");
                    staticFiles.add("platform.h");
                    staticFiles.add("xtft.c");
                    staticFiles.add("xtft.h");
                    staticFiles.add("xtft_charcode.h");
                }
                
                
                for(int j=0;j<staticFiles.size();j++){
                    int splitPoint = staticFiles.get(j).lastIndexOf(".");
                    String fileName = staticFiles.get(j).substring(0,splitPoint);
                    String extension = staticFiles.get(j).substring(splitPoint+1,staticFiles.get(j).length());//+1 to get rit of the "."
                        
                    // Printing content of static file to new file
                    PrintStream _printStreamStatic = _openFile(fileName, extension);
                    String content = _getFileContent(statisFilePath + staticFiles.get(j));
                    _printStreamStatic.println(content);
                    _printStreamStatic.close();  
                }
                
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
    
   private static String _getFileContent(String argFileName){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(argFileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            return sb.toString();

        } catch (FileNotFoundException ex) {
             System.out.println("Could not find file while looking up it's content" + argFileName);
        } catch (IOException ex) {
             System.out.println("Could not open file while looking up it's content");
        } finally {
           
        }
        return "";
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
        
        _printStreamFunc.println(_getFileContent(_sdk.getPathSDK() + "SDK" + File.separatorChar + "all" + File.separatorChar + "_fifiReadWriteApi"));
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

    // The path to libSDK which contains the BSPTemplate
     
    private UserInterface _ui = null;
    
    private Mapping _mapping = null;
    
    private CDProcessNetwork _pn = null;
    
    private PrintStream _printStream = null;
    
    private PrintStream _printStreamFunc = null;
        
    private boolean _sdk_enabled = false;
    
    private XpsSDKVisitor _sdk = null;
    
    private Map _relation2 = new HashMap();
    
    /**
     *  Read/Write fifo api
     */
     
    /* 
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
        " volatile long *fifo = (long *)f;\n" +
        " register long fifoSize = fifo[0];\n" +
        " register long fifo_2 = fifo[2];\n" +
        "\n" +
        " while( (fifo_2^fifo[5]) == 0x80000000) { }; // full\n" +
        "\n" +
        " void *ptr = (void *)(fifo + 6 + (fifo_2 & 0x7FFFFFFF));\n" +
        "\n" +
        " fifo_2 += len;      // wr index + token size in dwords (32 bits)\n" +
        "\n" +
        " if( (fifo_2 & 0x7FFFFFFF) == fifoSize ) { \n" +
        "  fifo_2 = fifo_2 & 0x80000000;\n" +
        "  fifo_2 = fifo_2 ^ 0x80000000; // toggle the flag\n" +
        " }\n" +
        "\n" +
        " fifo[2] = fifo_2;\n" +
        "\n" +
        " return ptr;\n" +
        "}\n" +
        "\n\ninline void release_write_ptr(int f) {" +
        "\n volatile long *fifo = (volatile long *)f;\n" +
        " fifo[3] = fifo[2];\n" +
        "}\n" +
        "\n\ninline volatile void *acquire_read_ptr(int f, int len) {" +
        "\n volatile long *fifo = (long *)f;" +
        "\n register long fifoSize = fifo[0];" +
        "\n register long fifo_4 = fifo[4];" +
        "\n" +
        "\n while( fifo[3] == fifo_4 ) { }; // empty" +
        "\n" +
        "\n void *ptr = (void *)(fifo + 6 + (fifo_4 & 0x7FFFFFFF));" +
        "\n" +
        "\n fifo_4 += len;      // rd index + token size in dwords (32 bits)" +
        "\n" +
        "\n if( (fifo_4 & 0x7FFFFFFF) == fifoSize ) {" +
        "\n  fifo_4 = fifo_4 & 0x80000000;" +
        "\n  fifo_4 = fifo_4 ^ 0x80000000; // toggle the flag" +
        "\n }" +
        "\n fifo[4] = fifo_4;" +
        "\n" +
        "\n return ptr;" +
        "\n}" +
        "\n\ninline void release_read_ptr(int f) {" +
        "\n volatile int *fifo = (volatile int *)f;" +
        "\n fifo[5] = fifo[4];" +
        "\n}" +
        "\n\n// Read and Write primitives for Xilkernel \n" +
        "#define readSWF_Dyn1(pos, value, len, fifo_size) \\\n" +
        "do {\\\n" +
        "   volatile int *fifo = (int *)pos;\\\n" +
        "   int r_cnt = fifo[1];\\\n" +
        "   int w_cnt = fifo[0];\\\n" +
        "   while ( w_cnt == r_cnt ) { yield(); w_cnt = fifo[0]; }\\\n" +
        "   for (int i = 0; i < len; i++) {\\\n" +
        "    ((volatile int *) value)[i] = fifo[(r_cnt & 0x7FFFFFFF) + 2 + i];\\\n" +
        "   }\\\n" +
        "   r_cnt += len;\\\n" +
        "   if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
        "    r_cnt &= 0x80000000;\\\n" +
        "    r_cnt ^= 0x80000000;\\\n" +
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
        "    fifo[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((volatile int *) value)[i];\\\n" +
        "   }\\\n" +
        "   w_cnt += len;\\\n" +
        "   if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
        "    w_cnt &= 0x80000000;\\\n" +
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
        "   while ( w_cnt == r_cnt ) { taskDISABLE_INTERRUPTS(); xil_printf(\"PANIC! Buffer underflow\\n\"); for(;;); }\\\n" +
        "   for (int i = 0; i < len; i++) {\\\n" +
        "    ((volatile int *) value)[i] = fifo[(r_cnt & 0x7FFFFFFF) + 2 + i];\\\n" +
        "   }\\\n" +
        "   r_cnt += len;\\\n" +
        "   if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
        "    r_cnt &= 0x80000000;\\\n" +
        "    r_cnt ^= 0x80000000;\\\n" +
        "   }\\\n" +
        "   fifo[1] = r_cnt;\\\n" +
        "} while(0)\n\n" +
        
        "#define writeSWF_Dyn2(pos, value, len, fifo_size, S, T) \\\n" +
        "do {\\\n" +
        "   volatile int *fifo = (int *)pos;\\\n" +
        "   int w_cnt = fifo[0];\\\n" +
        "   int r_cnt = fifo[1];\\\n" +
        "   while ( r_cnt == (w_cnt ^ 0x80000000) ) { taskDISABLE_INTERRUPTS(); xil_printf(\"PANIC! Buffer overflow\\n\"); for(;;); }\\\n" +
        "   for (int i = 0; i < len; i++) {\\\n" +
        "    fifo[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((volatile int *) value)[i];\\\n" +
        "   }\\\n" +
        "   w_cnt += len;\\\n" +
        "   if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {\\\n" +
        "    w_cnt &= 0x80000000;\\\n" +
        "       w_cnt ^= 0x80000000;\\\n" +
        "   }\\\n" +
        "   fifo[0] = w_cnt;\\\n" +
        "} while(0)\n\n";
    */
    /*
    private String _platformFileML605  = "" + 
        "#ifndef PLATFORM_H_\n" +
        "#define PLATFORM_H_\n\n" +
        "#include <FreeRTOS.h>\n" +
        "#include <timers.h>\n" +
        "#include <xtmrctr.h>\n" +
        "#include <stdio.h>\n\n" +
        "#define mainDONT_BLOCK   ( portTickType ) 0\n" +
        "#define TIMER_DEVICE_ID  XPAR_TMRCTR_0_DEVICE_ID\n" +
        "#define TIMER_FREQ_HZ    XPAR_TMRCTR_0_CLOCK_FREQ_HZ\n" +
        "#define TIMER_INTR_ID    XPAR_INTC_0_TMRCTR_0_VEC_ID\n\n" +
        "#if defined __cplusplus\n" +
        "extern \"C\" {\n" +
        "#endif\n\n" +
        "extern void vPortTickISR( void *pvUnused );\n" +
        "static XTmrCtr xTimer0Instance;\n\n" +
        "void vApplicationMallocFailedHook( void )\n" +
        "{\n" +
        "\ttaskDISABLE_INTERRUPTS();\n" +
        "\txil_printf(\"PANIC: malloc failed! Disabling interrupts...\\n\");\n" +
        "\tfor( ;; );\n" +
        "}\n" +
        "void vApplicationStackOverflowHook( xTaskHandle *pxTask, signed char *pcTaskName )\n" +
        "{\n" +
        "\t( void ) pcTaskName;\n" +
        "\t( void ) pxTask;\n" +
        "\ttaskDISABLE_INTERRUPTS();\n" +
        "\txil_printf(\"PANIC: Stack Overflow detected! Disabling interrupts...\\n\");\n" +
        "\tfor( ;; );\n" +
        "}\n" +
        "void vApplicationIdleHook( void ) { } \n" +
        "void vApplicationTickHook( void ) { } \n" +
        "void vSoftwareTimerCallback( xTimerHandle xTimer ) { } \n\n" +
        "void vApplicationSetupTimerInterrupt( void )\n" +
        "{\n" +
        "\tportBASE_TYPE xStatus;\n" +
        "\tconst unsigned char ucTimerCounterNumber = ( unsigned char ) 0U;\n" +
        "\tconst unsigned long ulCounterValue = ( ( TIMER_FREQ_HZ / configTICK_RATE_HZ ) - 1UL );\n" +
        "\txStatus = XTmrCtr_Initialize( &xTimer0Instance, TIMER_DEVICE_ID );\n" +
        "\tif( xStatus == XST_SUCCESS ) {\n" +
        "\t\txStatus = xPortInstallInterruptHandler( TIMER_INTR_ID, vPortTickISR, NULL );\n" +
        "\t}\n" +
        "\tif( xStatus == pdPASS ) { \n" +
        "\t\tvPortEnableInterrupt( TIMER_INTR_ID );\n" +
        "\t\tXTmrCtr_SetHandler( &xTimer0Instance, (XTmrCtr_Handler)vPortTickISR, NULL );\n" +
        "\t\tXTmrCtr_SetResetValue( &xTimer0Instance, ucTimerCounterNumber, ulCounterValue );\n" +
        "\t\tXTmrCtr_SetOptions( &xTimer0Instance, ucTimerCounterNumber, ( XTC_INT_MODE_OPTION | XTC_AUTO_RELOAD_OPTION | XTC_DOWN_COUNT_OPTION ) );\n" +
        "\t\tXTmrCtr_Start( &xTimer0Instance, ucTimerCounterNumber );\n" +
        "\t}\n" +
        "\tconfigASSERT( ( xStatus == pdPASS ) );\n" +
        "}\n" +
        "void vApplicationClearTimerInterrupt( void )\n" +
        "{\n" +
        "\tunsigned long ulCSR;\n" +
        "\tulCSR = XTmrCtr_GetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0 );\n" +
        "\tXTmrCtr_SetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0, ulCSR );\n" +
        "}\n" +
        "void init_platform() { } \n" +
        "#define delayCheckDeadline(xLastReleaseTime, xPeriod) do {\\\n" +
        "                                                          portTickType ticks;\\\n" +
        "                                                          ticks = xTaskGetTickCount();\\\n" +
        "                                                          vTaskDelayUntil( xLastReleaseTime, xPeriod );\\\n" +
        "                                                          if (ticks > *xLastReleaseTime) {\\\n" +
        "                                                              taskDISABLE_INTERRUPTS();\\\n" +
        "                                                              xil_printf(\"PANIC! Deadline miss\\n\");\\\n" +
        "                                                              for (;;);\\\n" +
        "                                                          }\\\n" +
        "                                                      }while(0);\n" +
        "#if defined __cplusplus\n" +
        "}\n" +
        "#endif\n" +
        "#endif\n";
    */
}

