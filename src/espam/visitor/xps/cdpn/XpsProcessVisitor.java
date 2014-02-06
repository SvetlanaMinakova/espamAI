
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
            // Zedboard does not need MB_interface
            if(!_sdk.getTargetBoard().equals("ZedBoard")){
				_printStreamFunc.println("#include <mb_interface.h>");
			}
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
            // But ZedBoard doesn't need host_if
            if (_sdk_enabled && !_sdk.getTargetBoard().equals("ZedBoard"))
                _sdk.handleHostIF();
            
            Iterator i = x.getProcessList().iterator();
            while( i.hasNext() ) {
                
                CDProcess process = (CDProcess) i.next();
                
                MProcessor mProcessor = _mapping.getMProcessor(process);  
                
                Resource resource = mProcessor.getResource();
                if (resource instanceof Processor) {
                    
                    if(_sdk.getTargetBoard().equals("ZedBoard")){
						
						new File(_codeDir + File.separatorChar + process.getName() + File.separatorChar +"src").mkdir();
						_printStream = _openFile(process.getName()+ File.separatorChar +"src",process.getName() , "cpp");
					}else{
						_printStream = _openFile(process.getName(), process.getName(), "cpp");
					}
                    
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

               // copy all 'static' files (that need no processing) from src/espam/libSDK/SDK/<targetboard>/
               // please notice that the folder names in src/espam/libSDK/SDK/ should have the same name as the targetboard
               
                ArrayList<String> staticFiles = new ArrayList<String>();
                
                String statisFilePath = _sdk.getPathSDK() + File.separatorChar + "SDK" + File.separatorChar;
                
                
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
                    new File(_codeDir + File.separatorChar + "Drivers").mkdirs();
                    PrintStream _printStreamStatic = _openFile("Drivers" + File.separatorChar + fileName, extension);
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
        
        _printStreamFunc.println(_getFileContent(_sdk.getPathSDK() + File.separatorChar + "SDK" + File.separatorChar + "all" + File.separatorChar + "_fifiReadWriteApi"));
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
    
}

