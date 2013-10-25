
package espam.visitor.xps.cdpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.*;

import espam.main.UserInterface;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML605;
import espam.datamodel.platform.communication.AXICrossbar;
import espam.datamodel.platform.controllers.AXI_CM_CTRL;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.CM_CTRL;
import espam.datamodel.platform.memories.CM_AXI;
import espam.datamodel.platform.memories.Memory;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.pn.cdpn.CDProcess;
import espam.visitor.xps.Copier;

//////////////////////////////////////////////////////////////////////////
//// XpsSDKVisitor

/**
*  Visitor to generate Xilinx SDK project files for each MicroBlaze;
*  we also generate makefile to enable compliation of software without
*  using SDK.
*
* @author  Mohamed Bamakhrama, Teddy Zhai, Andrea Ciani 
* @version  $Id: XpsSDKVisitor.java,v 1.18 2012/06/05 15:56:30 tzhai Exp $
*/

public class XpsSDKVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    public XpsSDKVisitor(Mapping mapping, String sdk_dir, String project_name) {
                
        UserInterface _ui = UserInterface.getInstance();

        _mapping = mapping;
        _getPlatformCharecteristics( mapping.getPlatform() );
                
        _libsdk_dir = _ui.getSDKLibPath();
        _sdk_dir = sdk_dir;
        _project_name = project_name;
            
        
        try {
            // Copy the functional code to SDK to allow make symbolic links later
            String funcCodePath = _ui.getFuncCodePath();
            if (funcCodePath == "") {
                    // Look for the functional code in "func_code" at the same level where the XPS project directory is
                    funcCodePath = _sdk_dir + File.separatorChar + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + "func_code";
            }

            File funcCode = new File(funcCodePath);
            String target = _sdk_dir + File.separatorChar + funcCode.getName();
            _funcCodeDirName = funcCode.getName();
        
            Copier copy = new Copier (funcCodePath, target, 1, true);
            copy.copy();        
        } catch (Exception e) {
                System.err.println ("Error copying the functional code directory");
                e.printStackTrace();
        } 
    }
    
    /**
    * generation of SDK project for host IF
    */
    public void handleHostIF() {
        String processorName = "host_if_mb";
        String processName = "host_if";
                
        String bsp_dirname = "BSP_" + processName;
        String xcp_dirname = processName;
        
        String bsp_folder = _sdk_dir + File.separatorChar + bsp_dirname; 
        String xcp_folder = _sdk_dir + File.separatorChar + xcp_dirname; 
        
        boolean dir_if = new File(bsp_folder).mkdir();

        dir_if = new File(xcp_folder).mkdir();
        
        //copySystemMss(bsp_folder, process);
        makeFile(bsp_folder);
        Libgen(processorName, bsp_folder);
        makeProject(bsp_folder, bsp_dirname);
        makeCProject(bsp_folder);
        makeSdkProject(bsp_folder, processorName);
                        
        try {
            makeXCPCProject(xcp_folder, processorName, processName, 3);
            makeXCPProject(xcp_folder, processName);
        } catch (Exception e) {
            System.out.println ("Error making XCP Project/CProject");
            e.printStackTrace();
        }
        
        // copy mss file from libSDK and rename it according to the platform in use
        String originMss;   
        if (_axiPlatform)
            originMss = "_AXI";
        else// PLB
            originMss = "_PLB"; 
        //private String _libsdk_dir;
        originMss = _libsdk_dir + File.separatorChar + "SDK" + File.separatorChar + "BSP_host_if" + File.separatorChar +
                "system"+ originMss+".mss"; // Path with updated filename
        String destination = bsp_folder + File.separatorChar +"system.mss"; // Destination path 
        Copier copy = new Copier (originMss,destination,2);    
        try {
            copy.copy();
        } catch (Exception e) {
            System.err.println ("Error copying the functional code directory");
            e.printStackTrace();
        }
        /* Replacing right DDR type according to the board in use (v5 v6) */
        String replace = "DDR3";
        if (_targetBoard == "XUPV5-LX110T")
            replace = "DDR2";
        
        try {
            FileReader fr = new FileReader(originMss);
            BufferedReader reader = new BufferedReader(fr);
            FileWriter file = new FileWriter (destination);
            PrintWriter printer = new PrintWriter(file);
            String st = "";
            while ((st = reader.readLine()) != null) {
                Pattern p = Pattern.compile("##DDR_TYPE##");
                Matcher m = p.matcher(st);
                st=(m.replaceAll(replace));
                printer.println(st);                        
            }
            printer.close();
        } catch (Exception e) {
            System.out.println ("Error making host interface mss file");
            e.printStackTrace();
        }

        String dirDebug = xcp_folder + File.separatorChar + "Debug";
        boolean IsDirDebug = new File(dirDebug).mkdir();
        if (!IsDirDebug)
            System.err.println ("ERROR creating " + dirDebug + " folder");
        
        try{
            String elfName = dirDebug + File.separatorChar + processName + ".elf";
            FileWriter elfFile = new FileWriter(elfName);
            elfFile.close();
        } catch (IOException exp) {
                    System.err.println("Error creating dummy elf file");
                    System.err.println("Error:" + exp);
        }

        if (_axiPlatform)   
            copyfile(_libsdk_dir + File.separatorChar + "SDK" + File.separatorChar + "host_if" + File.separatorChar + "main_AXI.cpp", xcp_folder + File.separatorChar + "main_AXI.cpp");
        else
            copyfile(_libsdk_dir + File.separatorChar + "SDK" + File.separatorChar + "host_if" + File.separatorChar + "main_PLB.cpp", xcp_folder + File.separatorChar + "main_PLB.cpp");

        makeObject(xcp_folder);
        makeSources(xcp_folder);
        makeSubdirHost(xcp_folder, bsp_dirname, processorName);
        makeFileXCPHost(xcp_folder);
    }


    public void visitProcessor(CDProcess process) {
        String bsp_dirname, xcp_dirname;
        String bsp_folder, xcp_folder;
        boolean dir_if;
        
        String processName = process.getName();
        MProcessor mProcessor = _mapping.getMProcessor(process); 
        String processorName = mProcessor.getName();
        int schedulerType = mProcessor.getScheduleType();
        Resource resource = mProcessor.getResource();
            
        bsp_dirname = "BSP_" + processName;
        xcp_dirname = processName;
        
        bsp_folder = _sdk_dir + File.separatorChar + bsp_dirname; 
        xcp_folder = _sdk_dir + File.separatorChar + xcp_dirname; 
        
        dir_if = new File(bsp_folder).mkdir();
        if (!dir_if)
            System.err.println ("ERROR creating " + bsp_dirname + " folder");

        dir_if = new File(xcp_folder).mkdir();
        

        // Generate BSP files
        copySystemMss(bsp_folder, process);
        makeFile(bsp_folder);
        Libgen(processorName, bsp_folder);
        makeProject(bsp_folder, bsp_dirname);
        makeCProject(bsp_folder);
        makeSdkProject(bsp_folder, processorName);
                        
        // Generate XCP files
        try {
            makeXCPCProject(xcp_folder, processorName, processName, schedulerType);
            makeXCPProject(xcp_folder, processName);
        } catch (Exception e) {
            System.err.println ("Error making XCP Project/CProject");
            e.printStackTrace();
        }

        String sourceDir = _sdk_dir + File.separatorChar + _funcCodeDirName;
        String destinationDir = xcp_folder;
        
        String[] link_cmd;
        File dir = new File(sourceDir);
        File destDir = new File(destinationDir);


        // create a dummy .elf as placeholder, otherwise, opening xps project gives error;
        // we assume that they are in the directory "Debug"
        String dirDebug = xcp_folder + File.separatorChar + "Debug";
        boolean IsDirDebug = new File(dirDebug).mkdir();
        if (!IsDirDebug)
            System.err.println ("ERROR creating " + dirDebug + " folder");
        
        try{
            String elfName = dirDebug + File.separatorChar + processName + ".elf";
            FileWriter elfFile = new FileWriter(elfName);
            elfFile.close();
        } catch (IOException exp) {
            System.err.println("Error creating dummy elf file");
            System.err.println("Error:" + exp);
        }
                
        
        // make symbolic links to implemenation
        try {
            
            File[] files = dir.listFiles(new FilenameFilter() { 
                            public boolean accept(File dir, String filename) { return filename.endsWith(".c"); } } );
            for (int r  = 0; r < files.length; r++) {
                link_cmd = new String[4];
                link_cmd[0] = "/bin/ln";
                link_cmd[1] = "-s";
                link_cmd[2] = files[r].getCanonicalPath();
                link_cmd[3] = destDir.getCanonicalPath() + File.separatorChar + files[r].getName();
                exe(link_cmd);
            }
    
            files = dir.listFiles(new FilenameFilter() { 
                            public boolean accept(File dir, String filename) { return filename.endsWith(".cpp"); } } );
            for (int r  = 0; r < files.length; r++) {
                link_cmd = new String[4];
                link_cmd[0] = "/bin/ln";
                link_cmd[1] = "-s";
                link_cmd[2] = files[r].getCanonicalPath();
                link_cmd[3] = destDir.getCanonicalPath() + File.separatorChar + files[r].getName();
                exe(link_cmd);
            }
    
            files = dir.listFiles(new FilenameFilter() { 
                            public boolean accept(File dir, String filename) { return filename.endsWith(".h"); } } );
            for (int r  = 0; r < files.length; r++) {
                link_cmd = new String[4];
                link_cmd[0] = "/bin/ln";
                link_cmd[1] = "-s";
                link_cmd[2] = files[r].getCanonicalPath();
                link_cmd[3] = destDir.getCanonicalPath() + File.separatorChar + files[r].getName();
                exe(link_cmd);
            }
                
        } catch (Exception e) {
            System.err.println("Error in making the symbolic links");
            e.printStackTrace();
        }
        
        if (Options.USE_FULLY_AUTOMATED_SDK == true) {
            makeObject(xcp_folder);
            makeSources(xcp_folder);
            makeSubdir(xcp_folder, xcp_dirname, bsp_dirname, processorName);
            makeFileXCP(xcp_folder, processorName, processName);
            makeLscript(xcp_folder, processorName, resource);
        }
    }


    private void copySystemMss (String folder, CDProcess process){
            
        Platform platform = _mapping.getPlatform();
        String systemFileName = folder + File.separatorChar + "system.mss";
        
        try {
            FileWriter systemFile = new FileWriter(systemFileName);
            PrintWriter out = new PrintWriter(systemFile);

            MProcessor mProcessor = _mapping.getMProcessor(process); 
            int schedulerType = mProcessor.getScheduleType();
            
            out.println("\n PARAMETER VERSION = 2.2.0\n");

            if (_axiPlatform) { // AXI
                // Print the OS type
                if (schedulerType == 2) { // FreeRTOS
                    out.println("BEGIN OS\n" + 
                                " PARAMETER OS_NAME = freertos\n" + 
                                " PARAMETER OS_VER = 2.00.a\n" + 
                                " PARAMETER PROC_INSTANCE = " + mProcessor.getName() + "\n" + 
                                " PARAMETER STDIN = " + "host_if_mb_RS232_Uart" + "\n" + 
                                " PARAMETER STDOUT = " + "host_if_mb_RS232_Uart" + "\n" + 
                                " PARAMETER SYSTMR_INTERVAL = 1000\n" + // 1000 Hz
                                " PARAMETER MAX_PRIORITIES = 8\n" +
                                " PARAMETER MAX_TASK_NAME_LEN = 16\n" +
                                " PARAMETER TOTAL_HEAP_SIZE = 16384\n" +
                                " PARAMETER MINIMAL_STACK_SIZE = 500\n" +
                                "END\n");
                } else if (schedulerType == 1) { // Xilkernel
                    out.println("BEGIN OS\n" +
                                " PARAMETER OS_NAME = xilkernel\n" +
                                " PARAMETER OS_VER = 5.00.a\n" +
                                " PARAMETER PROC_INSTANCE = " + mProcessor.getName() + "\n" +
                                " PARAMETER STDIN = host_if_mb_RS232_Uart\n" +
                                " PARAMETER STDOUT = host_if_mb_RS232_Uart\n" +
                                " PARAMETER SYSTMR_SPEC = true\n" +
                                " PARAMETER SYSTMR_DEV = " + mProcessor.getName() + "_timer\n" +
                                " PARAMETER SYSINTC_SPEC = " + mProcessor.getName() + "_intc\n" +
                                " PARAMETER ENHANCED_FEATURES = true\n" +
                                " PARAMETER CONFIG_YIELD = true\n" +
                                "END\n");
                
                } else { // Standalone
                    out.println("BEGIN OS\n" +
                                " PARAMETER OS_NAME = standalone\n" +
                                " PARAMETER OS_VER = 3.01.a\n" +
                                " PARAMETER PROC_INSTANCE = " + mProcessor.getName() + "\n" +
                                " PARAMETER STDIN = host_if_mb_RS232_Uart\n" +
                                " PARAMETER STDOUT = host_if_mb_RS232_Uart\n" +
                                "END\n");
                }
                
                
                out.println("BEGIN PROCESSOR\n" +
                            " PARAMETER DRIVER_NAME = cpu\n" +
                            " PARAMETER DRIVER_VER = 1.13.a\n" +
                            " PARAMETER HW_INSTANCE = " + mProcessor.getName() + "\n" +
                            "END\n");
                
                // Iterate over all the resources
                Vector<Resource> resources = (Vector<Resource>)platform.getResourceList();
                Iterator i = resources.iterator();
                while (i.hasNext()) {
                    Resource r = (Resource) i.next();
                    
                    if (r instanceof AXI_CM_CTRL) {
                        out.println("BEGIN DRIVER\n" +
                        " PARAMETER DRIVER_NAME = bram\n" +
                        " PARAMETER DRIVER_VER = 3.00.a\n" +
                        " PARAMETER HW_INSTANCE = " + r.getName() + "\n" +
                        "END\n");
                    }
                    
                    // The following is a "hack" to determine if the controllers belong to the current processor
                    if ( r instanceof MemoryController && r.getName().endsWith(mProcessor.getName()) ) {
                        out.println("BEGIN DRIVER\n" +
                                    " PARAMETER DRIVER_NAME = bram\n" +
                                    " PARAMETER DRIVER_VER = 3.00.a\n" +
                                    " PARAMETER HW_INSTANCE = " + r.getName() + "\n" +
                                    "END\n");
                    }
                    
                    if (r instanceof CM_CTRL && r.getName().endsWith(mProcessor.getName())) {
                        out.println("BEGIN DRIVER\n" +
                                    " PARAMETER DRIVER_NAME = bram\n" +
                                    " PARAMETER DRIVER_VER = 3.00.a\n" +
                                    " PARAMETER HW_INSTANCE = " + r.getName() + "\n" +
                                    "END\n");
                    }
                    
                }
                
                if (_commInterface.equals("Combo")) {
                    out.println("BEGIN DRIVER\n" +
                                " PARAMETER DRIVER_NAME = mpmc\n" +
                                " PARAMETER DRIVER_VER = 4.01.a\n" +
                                " PARAMETER HW_INSTANCE = DDR3_SDRAM\n" +
                                "END\n");
                    out.println("BEGIN DRIVER\n" +
                                " PARAMETER DRIVER_NAME = emaclite\n" +
                                " PARAMETER DRIVER_VER = 3.01.a\n" +
                                " PARAMETER HW_INSTANCE = Ethernet_Lite\n" +
                                "END\n\n" +
                                "BEGIN DRIVER\n" +
                                " PARAMETER DRIVER_NAME = sysace\n" +
                                " PARAMETER DRIVER_VER = 2.00.a\n" +
                                " PARAMETER HW_INSTANCE = SysACE_CompactFlash\n" +
                                "END\n");
                } else {
                // print the stuff not in Platform
                    out.println("BEGIN DRIVER\n" +
                                " PARAMETER DRIVER_NAME = v6_ddrx\n" +
                                " PARAMETER DRIVER_VER = 2.00.a\n" +
                                " PARAMETER HW_INSTANCE = DDR3_SDRAM\n" +
                                "END\n");
                }   
                            
                // TODO: Find a way to print fin_ctrl
                // For the time being, skip it here and do it in SDK
                /*
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = generic\n" +
                            " PARAMETER DRIVER_VER = 1.00.a\n" +
                            " PARAMETER HW_INSTANCE = fin_ctrl_P1\n" +
                            "END\n");
                */
                
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = intc\n" +
                            " PARAMETER DRIVER_VER = 2.02.a\n" +
                            " PARAMETER HW_INSTANCE = " + mProcessor.getName() + "_intc\n" +
                            "END\n\n" +
                            "BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = tmrctr\n" +
                            " PARAMETER DRIVER_VER = 2.03.a\n" +
                            " PARAMETER HW_INSTANCE = " + mProcessor.getName() + "_timer\n" +
                            "END\n");
                
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = uartlite\n" +
                            " PARAMETER DRIVER_VER = 2.00.a\n" +
                            " PARAMETER HW_INSTANCE = host_if_mb_RS232_Uart\n" +
                            "END\n");

            } 
            else { // PLB. 
                out.println("BEGIN OS\n" +
                            " PARAMETER OS_NAME = standalone\n" +
                            " PARAMETER OS_VER = 3.01.a\n" +
                            " PARAMETER PROC_INSTANCE = " + mProcessor.getName() + "\n" +
                            "END\n");

                out.println("BEGIN PROCESSOR\n" +
                            " PARAMETER DRIVER_NAME = cpu\n" +
                            " PARAMETER DRIVER_VER = 1.13.a\n" +
                            " PARAMETER HW_INSTANCE = " + mProcessor.getName() + "\n" +
                            "END\n");
                            
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = bram\n" +
                            " PARAMETER DRIVER_VER = 3.00.a\n" +
                            " PARAMETER HW_INSTANCE = DCTRL_BRAM1_" + mProcessor.getName() + "\n" +
                            "END\n");
                
                int ddr_type = 3;
                if (_targetBoard == "XUPV5-LX110T"){
                    ddr_type = 2;  
                } 
                
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = mpmc\n" +
                            " PARAMETER DRIVER_VER = 4.01.a\n" +
                            " PARAMETER HW_INSTANCE = DDR" + ddr_type + "_SDRAM\n" +
                            "END\n");

                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = bram\n" +
                            " PARAMETER DRIVER_VER = 3.00.a\n" +
                            " PARAMETER HW_INSTANCE = PCTRL_BRAM1_" + mProcessor.getName() + "\n" +
                            "END\n");
                
                // TODO: Find a way to print fin_ctrl
                // For the time being, skip it here and do it in SDK
                /*   
                out.println("BEGIN DRIVER\n" +
                                        " PARAMETER DRIVER_NAME = generic\n" +            } // end AXI/PLB
                                        " PARAMETER DRIVER_VER = 1.00.a\n" +            
                                        " PARAMETER HW_INSTANCE = clock_cycle_counter_" + r.getName() + "\n" + //TODO: Not Right!             
                                        "END\n");            out.close();
                out.println("BEGIN DRIVER\n" +            systemFile.close();
                                        " PARAMETER DRIVER_NAME = generic\n" +            
                                        " PARAMETER DRIVER_VER = 1.00.a\n" +        } catch (IOException exp) {
                                        " PARAMETER HW_INSTANCE = fin_ctrl_" + r.getName() + "\n" + //TODO: Not Right		    System.err.println("Error creating file system.mss");
                                        "END\n"); 		    System.err.println("Error:" + exp);
                */        
            } // end AXI/PLB
            
            out.close();
            systemFile.close();
            
        } catch (IOException exp) {
            System.err.println("Error creating file system.mss");
            System.err.println("Error:" + exp);
        }
    }
    

    private void Libgen(String processorName, String dirname){
        PrintWriter out;
        FileWriter libGenFile;
        try {
            libGenFile = new FileWriter(dirname + File.separatorChar + "libgen.options");

            out = new PrintWriter(libGenFile);
            out.println("PROCESSOR=" + processorName);
            out.println("HWSPEC=.." + File.separatorChar + _project_name + "_hw_platform" + File.separatorChar + "system.xml");
            out.close();
        } 
    
        catch (IOException exp){
            System.err.println("Error creating file libgen.option");
            System.err.println("Error:" + exp);
        }
    
    } 

    public void makeProject (String destFolder, String dirname){
        try {
            FileWriter file = new FileWriter (destFolder + File.separatorChar + ".project");
            PrintWriter printer = new PrintWriter(file);
            printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            printer.println("<projectDescription>");
            printer.println("<name>" + dirname + "</name>");
            printer.println("<comment></comment>");
            printer.println("<projects>");
            printer.println("<project>" + _project_name + "_hw_platform</project>");
            printer.println("</projects>");
            printer.close();
            copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "BSP_Template_Project", destFolder + File.separatorChar + ".project");
        
        } catch (IOException e) {
            System.err.println("ERROR making project file");
            e.printStackTrace();
        }
        
    }

    public void makeCProject (String destFolder){
        copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "BSP_Template_CProject", destFolder + File.separatorChar + ".cproject");
    }

    public void makeSdkProject (String destFolder, String processorName){
        PrintWriter out;
        FileWriter libGenFile;
        try {
            libGenFile = new FileWriter(destFolder + File.separatorChar + ".sdkproject");
    
            out = new PrintWriter(libGenFile);
            out.println("THIRPARTY=false");
                out.println("PROCESSOR=" + processorName);
            out.println("MSS_FILE=system.mss");
            out.close();
        } 
    
        catch (IOException exp){
            System.err.println("Error creating file .sdkproject");
            System.err.println("Error:" + exp);
        }
    }


    public void makeFile(String destFolder) {
        copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "BSP_Template_MakeFile", destFolder + File.separatorChar+ "Makefile");
    }


    private void copyfile(String srcFile, String dstFile){
        try {
            File f1 = new File(srcFile);
            File f2 = new File(dstFile);
            InputStream in = new FileInputStream(f1);
        
            //For Append the file.
            OutputStream out = new FileOutputStream(f2,true);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        catch(Exception ex){
            System.err.println("Error in copying the SDK project files");
            ex.printStackTrace();
        }
    }

    private int exe(String[] cmd) 
    {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader( new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
//    	    	System.out.println(line);
            }
            br.close();       	    
            
            br = new BufferedReader( new InputStreamReader(process.getErrorStream()));
            while ((line = br.readLine()) != null) {
//    	    	System.out.println(line);
            }
            br.close();       	    
            
            process.waitFor();
            int exitValue = process.exitValue();
//    	    System.out.println("Exit value: " + exitValue);
            return exitValue;    	    
            
        } catch (Exception e) {
            System.err.println("Error in executing the following command: " + cmd);
            e.printStackTrace(System.err);
        }
        return -1;
    }

    public void makeXCPProject(String destFolder, String projectName) throws Exception{
        
        File f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Template_FProject");
        FileWriter file = new FileWriter (destFolder + File.separatorChar +".project");
        PrintWriter printer = new PrintWriter(file);
        if (!f.exists() && f.length() < 0) {
            System.out.println("The specified file does not exist");
        } else {
            FileReader fr = new FileReader(f);
            BufferedReader reader = new BufferedReader(fr);
            String st = "";
            String replace;
            
            while ((st = reader.readLine()) != null) {
                replace = projectName;	
                Pattern p = Pattern.compile("##PROCESSOR_NAME##");
                Matcher m = p.matcher(st);
                st = (m.replaceAll(replace));
                
                replace = "BSP_" + projectName;
                p = Pattern.compile("##CPP_BSP_FOLDER##");
                m = p.matcher(st);
                printer.println(m.replaceAll(replace));
            }
            printer.close();
        }
    }

    public void makeXCPCProject(String destFolder, String processorName, String projectName, int type) throws Exception {
        File f;
        if (_axiPlatform) { // AXI
            if (type == 0)  // Standalone
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_AXI_Standalone_Template_CProject");
            else if (type == 1) // Xilkernel
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_AXI_Xilkernel_Template_CProject");
            else if (type == 2) // FreeRTOS
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_AXI_FreeRTOS_Template_CProject");
            else if (type == 3) // Host IF
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_AXI_HostIF_Template_CProject");
            else
                throw new Exception("Invalid CProject file type specified");
                
        } else { // PLB
            // TODO: for PLB platform we currently only support standalone 
            if (type == 0)  // Standalone
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_PLB_Standalone_Template_CProject");
            else if (type == 3) // Host IF
                f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_PLB_HostIF_Template_CProject");
            else
                throw new Exception("Invalid CProject file type specified");
        }
        
        FileWriter file = new FileWriter (destFolder + File.separatorChar + ".cproject");
        PrintWriter printer = new PrintWriter(file);
        if (!f.exists() && f.length() < 0) {
            System.out.println("The specified file does not exist");
        } else {
            FileReader fr = new FileReader(f);
            BufferedReader reader = new BufferedReader(fr);
            String st = "";
            String replace;
            
            while ((st = reader.readLine()) != null) {
                replace = processorName;
                Pattern p = Pattern.compile("##PROCESSOR_NAME##");
                Matcher m = p.matcher(st);
                st=(m.replaceAll(replace));

                replace = "BSP_" + projectName;
                p = Pattern.compile("##CPP_BSP_FOLDER##");
                m = p.matcher(st);
                st=m.replaceAll(replace);

                replace = _project_name;
                p = Pattern.compile("##HW_PROJECT##");
                m = p.matcher(st);
                st=m.replaceAll(replace);

                replace = _sdk_dir;
                p = Pattern.compile("##FOLDER_PROJECT##");
                m = p.matcher(st);
                st=m.replaceAll(replace);
                
                replace = _funcCodeDirName;
                p = Pattern.compile("##FUNC_CODE##");
                m = p.matcher(st);
                st=m.replaceAll(replace);
                
                printer.println(st);
                                            
            }
            printer.close();
        }
    } 
    
    public void makeObject (String destFolder){
        copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "objects.mk", destFolder + File.separatorChar + "Debug" + File.separatorChar + "objects.mk");
    }
    
    public void makeSources (String destFolder){
        copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "sources.mk", destFolder + File.separatorChar + "Debug" + File.separatorChar + "sources.mk");
    }

    public void makeSubdir(String destFolder, String dirnameXCP, String dirnameBSP, String processorName){
        try {
            FileWriter file = new FileWriter (destFolder + File.separatorChar + "Debug" + File.separatorChar + "subdir.mk");
            PrintWriter printer = new PrintWriter(file);

            String dirPath = _sdk_dir + File.separatorChar + _funcCodeDirName;
            File dir = new File(dirPath);
            String[] filenames = dir.list(new FilenameFilter() { 
                public boolean accept(File dir, String filename) { return filename.endsWith(".cpp"); } } );

            printer.println("# Add inputs and outputs from these tool invocations to the build variables");
            printer.println("CPP_SRCS += \\");
            printer.println("../" + dirnameXCP + ".cpp \\");
            for (int i = 0; i < filenames.length - 1; i++) {
                printer.println("../" + filenames[i] + " \\");
            }
            printer.println("../" + filenames[filenames.length - 1]);
            printer.println("\n");
            printer.println("LD_SRCS += \\");
            printer.println("../lscript.ld");
            printer.println("\n");
            printer.println("OBJS += \\");
            printer.println("./" + dirnameXCP + ".o \\");
            for (int i = 0; i < filenames.length - 1; i++) {
                printer.println("./" + filenames[i].replace(".cpp", ".o") + " \\");
            }
            printer.println("./" + filenames[filenames.length - 1].replace(".cpp", ".o"));
            printer.println("\n");
            printer.println("CPP_DEPS += \\");
            printer.println("./" + dirnameXCP + ".d \\");
            for (int i = 0; i < filenames.length - 1; i++) {
                printer.println("./" + filenames[i].replace(".cpp", ".d") + " \\");
            }
            printer.println("./" + filenames[filenames.length - 1].replace(".cpp", ".d"));
            printer.println("\n");
            printer.println("\n");
            printer.println("# Each subdirectory must supply rules for building sources it contributes");
            printer.println("%.o: ../%.cpp");
            printer.println("\t@echo Building file: $<");
            printer.println("\t@echo Invoking: MicroBlaze g++ compiler");
            printer.println("\tmb-g++ -Wall -O2 -ISDK/" + _funcCodeDirName + " -c -fmessage-length=0 -D __XMK__ -I../../" + dirnameBSP + "/" + processorName + "/include -mlittle-endian -mxl-barrel-shift -mxl-pattern-compare -mno-xl-soft-div -mcpu=v8.20.a -mno-xl-soft-mul -mhard-float -MMD -MP -MF\"$(@:%.o=%.d)\" -MT\"$(@:%.o=%.d)\" -o\"$@\" \"$<\"");
            printer.println("\t@echo Finished building: $<");
            printer.println("\t@echo ' '");
            printer.close();
        } catch (IOException e) {
        System.err.println("Error making subdir file");
            e.printStackTrace();
        }
    }

    public void makeSubdirHost(String destFolder, String dirnameBSP, String processorName){
        try {
            FileWriter file = new FileWriter (destFolder + File.separatorChar + "Debug" + File.separatorChar + "subdir.mk");
            PrintWriter printer = new PrintWriter(file);

            printer.println("# Add inputs and outputs from these tool invocations to the build variables");
            printer.println("CPP_SRCS += \\");
            if (_axiPlatform)
                printer.println("../main_AXI.cpp");
            else
                printer.println("../main_PLB.cpp");
            printer.println("\n");
            printer.println("OBJS += \\");
            if (_axiPlatform)
                printer.println("./main_AXI.o");
            else
                printer.println("./main_PLB.o");
            printer.println("\n");
            printer.println("CPP_DEPS += \\");
            if (_axiPlatform)
                printer.println("./main_AXI.d");
            else
                printer.println("./main_PLB.d");
            printer.println("\n");
            printer.println("\n");
            printer.println("# Each subdirectory must supply rules for building sources it contributes");
            printer.println("%.o: ../%.cpp");
            printer.println("\t@echo Building file: $<");
            printer.println("\t@echo Invoking: MicroBlaze g++ compiler");
            printer.println("\tmb-g++ -Wall -O0 -g3 -c -fmessage-length=0 -I../../" + dirnameBSP + "/" + processorName + "/include -mlittle-endian -mxl-pattern-compare -mcpu=v8.20.a -mno-xl-soft-mul -MMD -MP -MF\"$(@:%.o=%.d)\" -MT\"$(@:%.o=%.d)\" -o\"$@\" \"$<\"");
            printer.println("\t@echo Finished building: $<");
            printer.println("\t@echo ' '");
            printer.close();
        } catch (IOException e) {
        System.err.println("Error making subdir file");
            e.printStackTrace();
        }
    }

    /**
    * generation of makefile to compile the software
    * @note: we assume optimization flag "-O2" and without debug flag
    * 
    */

    public void makeFileXCP(String destFolder, String processorName, String processName){
    try{
        File f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Template_Makefile");
        FileWriter file = new FileWriter (destFolder + File.separatorChar + "Debug" + File.separatorChar +"makefile");
        PrintWriter printer = new PrintWriter(file);
        FileReader fr = new FileReader(f);
        BufferedReader reader = new BufferedReader(fr);
        String st = "";
        String replace;
            
        while ((st = reader.readLine()) != null) {
            replace = processorName;	
            Pattern p = Pattern.compile("##PROCESSOR_NAME##");
            Matcher m = p.matcher(st);
            st = m.replaceAll(replace);
                    
            replace = processName;
            p = Pattern.compile("##PROCESS_NAME##");
            m = p.matcher(st);
            st=m.replaceAll(replace);

            replace = "BSP_" + processName;
            p = Pattern.compile("##CPP_BSP_FOLDER##");
            m = p.matcher(st);
            st=m.replaceAll(replace);

            replace = _project_name;
            p = Pattern.compile("##HW_PROJECT##");
            m = p.matcher(st);
            st=m.replaceAll(replace);

            printer.println(st);
            }
            printer.close();
        } catch (IOException e) {
        System.err.println("Error making makefile");
            e.printStackTrace();
        }
    }

    public void makeFileXCPHost (String destFolder){
        copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Host_IF_Makefile", destFolder + File.separatorChar + "Debug" + File.separatorChar +"makefile");
    }

    public void makeLscript(String destFolder, String processorName, Resource resource){
        try {
            Integer s = _mapping.getProcessorList().size();
            Integer localMem = 0;
            FileWriter file = new FileWriter (destFolder + File.separatorChar + "lscript.ld");
            PrintWriter printer = new PrintWriter(file);

            localMem = ((Processor) resource).getProgMemSize();
            localMem += ((Processor) resource).getDataMemSize();
            
            printer.println("_STACK_SIZE = DEFINED(_STACK_SIZE) ? _STACK_SIZE : 0x400;");
            printer.println("_HEAP_SIZE = DEFINED(_HEAP_SIZE) ? _HEAP_SIZE : 0x400;\n");
            printer.println("/* Define Memories in the system */\n");
            printer.println("MEMORY");
            printer.println("{");
            printer.println("   PCTRL_BRAM1_" + processorName + "_DCTRL_BRAM1_" + processorName + " : ORIGIN = 0x00000050, LENGTH = 0x" + _digitToStringHex(localMem - 80, 8));
            if (_axiPlatform) {
                printer.println("   LMB_CTRL_CM_" + processorName + " : ORIGIN = 0xE0000000, LENGTH = 0x00010000");
                printer.println("   DDR3_SDRAM_S_AXI_BASEADDR : ORIGIN = 0xA0000000, LENGTH = 0x10000000");
                for (int i = 1; i <= s; i++) {
                    printer.println("   AXI_CTRL_CM_mb_" + i + "_S_AXI_BASEADDR : ORIGIN = 0x800" + i + "0000, LENGTH = 0x00010000");
                }
            }
            else {
                printer.println("   DDR3_SDRAM_MPMC_BASEADDR : ORIGIN = 0xA0000000, LENGTH = 0x10000000");
            }
            printer.println("}");

            File f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "Linker_Script_Template");
            FileReader fr = new FileReader(f);
            BufferedReader reader = new BufferedReader(fr);
            String st = "";
            String replace;
            
            while ((st = reader.readLine()) != null) {
                replace = processorName;	
                Pattern p = Pattern.compile("##PROCESSOR_NAME##");
                Matcher m = p.matcher(st);
                st = m.replaceAll(replace);
                printer.println(st);
            }
            printer.close();
        } catch (IOException e) {
            System.err.println("ERROR making linker script file");
            e.printStackTrace();
        }
    }
    
    /**
    *  Get the target FPGA board and interconnection type (AXI/PLB)
    *  @param platform
    *  
    */
    private void _getPlatformCharecteristics( Platform x ) {
        Iterator j = x.getResourceList().iterator();
        while (j.hasNext()) {
            Resource resource = (Resource)j.next();
            if( resource instanceof XUPV5LX110T ) {
            _targetBoard = "XUPV5-LX110T";
            _commInterface = ((XUPV5LX110T) resource).getCommInterface();
            } else if( resource instanceof ML605 ) {
            _targetBoard = "ML605";
            _commInterface = ((ML605) resource).getCommInterface();
            } else if( resource instanceof AXICrossbar ) {
            _axiPlatform = true;
            }
        }

        if (_targetBoard != "ML605" && _targetBoard != "XUPV5-LX110T"){
            System.err.println("Error: unsupported target board in using SDK visitor");
        }

    }

    /**
    *  convert to hexical string
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    // The ESPAM project name
    private String _project_name;
    
    // The full path to the SDK dir 
    private String _sdk_dir;
    
    // The path to libSDK which contains the BSPTemplate
    private String _libsdk_dir;
    
    // The directory containing the functional code in SDK
    private String _funcCodeDirName;

    // The mapping   
    private Mapping _mapping;
    
    // the board in use
    private String _targetBoard = "";

    // the communication interface
    private String _commInterface = "";
    
    // A flag to indicate whether the platform is AXI-based or not
    private boolean _axiPlatform = false;

}
