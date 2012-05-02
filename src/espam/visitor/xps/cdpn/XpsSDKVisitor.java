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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.regex.*;

import espam.main.UserInterface;


//////////////////////////////////////////////////////////////////////////
//// XpsSDKVisitor

/**
 *  Visitor to generate Xilinx SDK project files
 *
 * @author  Mohamed Bamakhrama
 * @note Based on the BSP class written by Andrea Ciani and Teddy Zhai
 * @version  $Id: XpsSDKVisitor.java,v 1.3 2012/05/02 20:03:20 mohamed Exp $
 */

public class XpsSDKVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    public XpsSDKVisitor(String sdk_dir, String project_name) {
		
	    UserInterface _ui = UserInterface.getInstance();

        _libsdk_dir = _ui.getSDKLibPath();
        _sdk_dir = sdk_dir;
        _project_name = project_name;
    }

    public void visitProcessor(String processorName) {
	    String bsp_dirname, xcp_dirname;
	    String bsp_folder, xcp_folder;
	    boolean dir_if;
	    
        bsp_dirname = "empty_cpp_bsp_" + processorName;
        xcp_dirname = processorName;
        
        bsp_folder = _sdk_dir + File.separatorChar + bsp_dirname; 
        xcp_folder = _sdk_dir + File.separatorChar + xcp_dirname; 
        
        dir_if = new File(bsp_folder).mkdir();
        if (!dir_if)
            System.out.println ("ERROR creating " + bsp_dirname + " folder");

        dir_if = new File(xcp_folder).mkdir();
	
        // Generate BSP files
        copySystemMss(bsp_folder, processorName);
        makeFile(bsp_folder);
        Libgen(processorName, bsp_folder);
        makeProject(bsp_folder, bsp_dirname);
        makeCProject(bsp_folder);
        makeSdkProject(bsp_folder, processorName);
			
        // Generate XCP files
        try {
		        makeXCPCProject(xcp_folder, processorName);
		        makeXCPProject(xcp_folder, processorName);
        } catch (Exception e) {
		        System.out.println ("Error making XCP Project/CProject");
		        e.printStackTrace();
        }	
    }

    private void copySystemMss (String folder, String processorName){
	    /*
	     * Copy the System.mms into the correct folder.
	     * BE CAREFULL: This method needs the PATHSAVE directory to take the "mss"s files!
	     */
	     
	     // Commented out by Mohamed
	     // Teddy and Andrea are still figuring out this one
	     
	    /*
	        String systemFileName;
	
	        systemFileName = PATHSAVE + "system.mss";
	        XpsSDKVisitor.copyfile(systemFileName, folder + File.separatorChar + "system.mss");
	    */
    }

    private void Libgen(String processorName, String dirname){
	    PrintWriter out;
	    FileWriter libGenFile;
	    try {
		    libGenFile = new FileWriter(dirname + File.separatorChar + "libgen.options");
	
		    out = new PrintWriter(libGenFile);
		    out.println("PROCESSOR=" + processorName);
		    out.println("REPOSITORIES=");
		    out.println("HWSPEC=.." + File.separatorChar + _project_name + "_hw_platform" + File.separatorChar + "system.xml");
		    out.close();
	    } 
	
	    catch (IOException exp){
		    System.out.println("Error creating file libgen.option");
		    System.out.println("Error:" + exp);
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
		    System.out.println("ERROR making project file");
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
		    System.out.println("Error creating file .sdkproject");
		    System.out.println("Error:" + exp);
	    }
    }


    public void makeFile(String destFolder) {
	    copyfile(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "BSP_Template_MakeFile", destFolder + File.separatorChar+ "Makefile");
    }


    private void copyfile(String srFile, String dtFile){
	    try {
		    File f1 = new File(srFile);
		    File f2 = new File(dtFile);
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
	    catch(FileNotFoundException ex){
		    System.out.println(ex.getMessage() + " in the specified directory.");
		    System.exit(0);
	    }
	    catch(IOException e){
		    System.out.println(e.getMessage());			
	    }
    }


    public void makeXCPProject(String destFolder, String processorName) throws Exception{
	
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
            		replace = processorName;	
            		
                    Pattern p = Pattern.compile("##PROCESSOR_NAME##");
                    Matcher m = p.matcher(st);
                    st = (m.replaceAll(replace));
                    replace = "empty_cpp_bsp_" + processorName;
                    p = Pattern.compile("##CPP_BSP_FOLDER##");
                    m = p.matcher(st);
                    printer.println(m.replaceAll(replace));
            }
            printer.close();
        }
    }

    public void makeXCPCProject(String destFolder, String processorName) throws Exception {
        File f = new File(_libsdk_dir + File.separatorChar + "BSPTemplate" + File.separatorChar + "XCP_Template_CProject");
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

                        replace = "empty_cpp_bsp_" + processorName;
                        p = Pattern.compile("##CPP_BSP_FOLDER##");
                        m = p.matcher(st);
                        st=m.replaceAll(replace);

                        replace = _sdk_dir;
                        p = Pattern.compile("##FOLDER_PROJECT##");
                        m = p.matcher(st);
                        printer.println(m.replaceAll(replace));
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

}
