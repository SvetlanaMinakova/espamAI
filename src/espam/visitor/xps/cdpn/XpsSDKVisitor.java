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
 *  Visitor to generate Xilinx SDK project files
 *
 * @author  Mohamed Bamakhrama
 * @note Based on the BSP class written by Andrea Ciani and Teddy Zhai
 * @version  $Id: XpsSDKVisitor.java,v 1.7 2012/05/28 12:00:01 tzhai Exp $
 */

public class XpsSDKVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    public XpsSDKVisitor(Mapping mapping, String sdk_dir, String project_name) {
		
	UserInterface _ui = UserInterface.getInstance();

        _mapping = mapping;
        _libsdk_dir = _ui.getSDKLibPath();
        _sdk_dir = sdk_dir;
        _project_name = project_name;
        
        _axiPlatform = false;
        Iterator i = _mapping.getPlatform().getResourceList().iterator();
	    while( i.hasNext() ) {
    		Resource resource = (Resource) i.next();
        	if( resource instanceof AXICrossbar ) {
                _axiPlatform = true;
                break;
            }
        }
        
        try {
            // Copy the functional code to SDK
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
	
        handleHostIF();	
    }
    
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
        //makeFile(bsp_folder);
        Libgen(processorName, bsp_folder);
        makeProject(bsp_folder, bsp_dirname);
        //makeCProject(bsp_folder);
        //makeSdkProject(bsp_folder, processorName);
			
        try {
		        makeXCPCProject(xcp_folder, processorName, processName, 3);
		        makeXCPProject(xcp_folder, processName);
        } catch (Exception e) {
		        System.out.println ("Error making XCP Project/CProject");
		        e.printStackTrace();
        }    
    }

    public void visitProcessor(CDProcess process) {
	    String bsp_dirname, xcp_dirname;
	    String bsp_folder, xcp_folder;
	    boolean dir_if;
	    
	    String processName = process.getName();
	    MProcessor mProcessor = _mapping.getMProcessor(process); 
	    String processorName = mProcessor.getName();
	    int schedulerType = mProcessor.getScheduleType();
	    
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
                
                // print the stuff not in Platform
                out.println("BEGIN DRIVER\n" +
                            " PARAMETER DRIVER_NAME = v6_ddrx\n" +
                            " PARAMETER DRIVER_VER = 2.00.a\n" +
                            " PARAMETER HW_INSTANCE = DDR3_SDRAM\n" +
                            "END\n");
                            
                            
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
                            " PARAMETER PROC_INSTANCE = mb_1\n" +
                            "END\n");
                
                // TODO: for PROCESSOR
                
                // TODO: for 

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
		    // TODO: path is hard-coded for now
		    out.println("REPOSITORIES=-lp /home/mohamed/tools/FreeRTOS/FreeRTOSV7.1.0/Demo/MicroBlaze_Spartan-6_EthernetLite/KernelAwareBSPRepository");
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
        if (type == 0)  // Standalone
            f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Standalone_Template_CProject");
        else if (type == 1) // Xilkernel
            f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Xilkernel_Template_CProject");
        else if (type == 2) // FreeRTOS
            f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_FreeRTOS_Template_CProject");
        else if (type == 3) // Host IF
            f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_HostIF_Template_CProject");
        else
            throw new Exception("Invalid CProject file type specified");
            
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
      
    // A flag to indicate whether the platform is AXI-based or not
    private boolean _axiPlatform;

}
