/*******************************************************************\
  * 
  The ESPAM Software Tool 
  Copyright (c) 2004-2009 Leiden University (LERC group at LIACS).
  All rights reserved.
  
  The use and distribution terms for this software are covered by the 
  Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
  which can be found in the file LICENSE at the root of this distribution.
  By using this software in any fashion, you are agreeing to be bound by 
  the terms of this license.
  
  You must not remove this notice, or any other, from this software.
  
  \*******************************************************************/

package espam.visitor.ipxact.platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.lang.Math;

import espam.datamodel.EspamException;
import espam.datamodel.mapping.*;
import espam.datamodel.domain.IndexVector;
import espam.datamodel.domain.Polytope;
import espam.datamodel.graph.adg.*;
import espam.datamodel.platform.memories.*;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.*;
import espam.datamodel.platform.processors.*;

import espam.main.UserInterface;

import espam.operations.transformations.*;
import espam.operations.codegeneration.CodeGenerationException;
import espam.operations.codegeneration.Polytope2IfStatements;

import espam.visitor.PlatformVisitor;
import espam.visitor.expression.*;
import espam.visitor.ipxact.IpxactXmlWriter;
import espam.visitor.xps.Copier;

import espam.utils.symbolic.expression.*;



//////////////////////////////////////////////////////////////////////////
//// IP-XACT DWARV Visitor

/**
 * This class is a visitor that is used to generate IP-XACT descriptions for
 * programmable processors (e.g., MicroBlaze or PowerPC) that use a DWARV CCU.
 * This description then has to be sent to DWARV before the final system can
 * be synthesized. Using this visitor requires specifying the --ipxact
 * parameter to ESPAM.
 *
 * This class was developed as part of the SoftSoC project.
 *
 * @author Sven van Haastregt
 * @version $Id: IpxactDwarvVisitor.java,v 1.2 2009/09/30 14:13:09 sven Exp $
 */

public class IpxactDwarvVisitor extends PlatformVisitor {
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    /**
     * Constructor for the IseNetworkVisitor object
     * @param mapping
     *            The mapping of the corresponding platform which contains crucial mapping information.
     */
    public IpxactDwarvVisitor ( Mapping mapping ) throws FileNotFoundException, EspamException {
        
        _ui = UserInterface.getInstance();
        _mapping = mapping;
        _adg = _mapping.getADG();
        
        if (_ui.getOutputFileName() == "") {
            _outputDir = _ui.getBasePath() + "/" + _ui.getFileName();
            _projectName = _ui.getFileName();
        } else {
            _outputDir = _ui.getBasePath() + "/" + _ui.getOutputFileName();
            _projectName = _ui.getOutputFileName();
        }
        
        _outputDir += "/todelft";
        
    }
    
    /**
     * Produces IP-XACT packages for all MicroBlaze processors with hasCCU-flag set.
     *
     * @param x
     *            The platform that needs to be rendered.
     */
    public void visitComponent(Platform x) {
        try {
            Iterator i;
            i = x.getResourceList().iterator();
            File dir = new File(_outputDir);
            dir.mkdirs();
            
            while (i.hasNext()) {
                Resource resource = (Resource) i.next();
                if (resource instanceof MicroBlaze) {
                    MicroBlaze node = (MicroBlaze) resource;
                    //if (node.useCCU = true) { //TODO: don't do this for every processor, only when CCU flag is set in platform description
                    _node = node;
                    _coreName = node.getName();
                    
                    // Create subdirectory for this processor
                    _nodeDir = _outputDir + "/" + _coreName;
                    dir = new File(_nodeDir);
                    dir.mkdirs();
                    
                    // Copy the common LAURA processor components from the XPS library
                    //File f = new File(_ui.getXpsLibPath() + "/pcores/HWnode_template_v1_00_a");
                    //File t = new File(_codeDir + "/" + _commonlauraDir);
                    //Copier.copy(f, t, 1, true);
                    
                    Iterator j = _mapping.getProcessorList().iterator();
                    while(j.hasNext()){
                        MProcessor mp = (MProcessor) j.next();
                        if(mp.getResource() instanceof MicroBlaze){
                            if(mp.getResource().getName().equals(_coreName)== true){
                                MProcess p = (MProcess)mp.getProcessList().get(0);
                                _adgNode = p.getNode();
                                _inArgList = _adgNode.getFunction().getInArgumentList();
                                _adgInPorts = _adgNode.getInPorts();
                                _outArgList = _adgNode.getFunction().getOutArgumentList();
                                _adgOutPorts = _adgNode.getOutPorts();
                            }
                        }
                    }
                    
                    // Now visit the node
                    if ( _adgNode != null) {
                        _ipxactWriter = new IpxactXmlWriter(_nodeDir + "/" + "todwarv.xml");
                        resource.accept(this);
                        _ipxactWriter.close();
                    }
                    //} // if useCCU
                    
                }
            }
            
        } catch (Exception e) {
            System.out.println("In IP-XACT DWARV Visitor: exception occured: "
                                   + e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * Writes an IP-XACT document for the CCU that has to be attached to this MicroBlaze.
     *
     * @param  x The corresponding MicroBlaze processor
     */
    public void visitComponent(MicroBlaze x) {
        _ipxactWriter.writeStartDocument();
        _ipxactWriter.writeStartTopElement("component");
        _writeVLNV();
        _ipxactWriter.writeStartElement("fileSets");
        
        _ipxactWriter.writeStartElement("fileSet");
        _ipxactWriter.writeElement("name", "fs-cSource");
        _ipxactWriter.writeStartElement("file", "spirit:fileId=\"f-cSource\"");
        _ipxactWriter.writeElement("name", _adgNode.getFunction().getName() + ".c");
        _ipxactWriter.writeElement("fileType", "cSource");
        _ipxactWriter.writeEndElement();
        _ipxactWriter.writeStartElement("function");
        _ipxactWriter.writeElement("fileRef", "f-cSource");
        _ipxactWriter.writeElement("returnType", "void");
        
        Iterator j;
        j = _inArgList.iterator();
        while (j.hasNext()) {
            ADGVariable var = (ADGVariable) j.next();
            _ipxactWriter.writeStartElement("argument", "spirit:dataType=\"" + var.getDataType() + "\"");
            _ipxactWriter.writeElement("name", var.getName());
            _ipxactWriter.writeElement("value", "0");
            _ipxactWriter.writeStartElement("vendorExtensions");
            _ipxactWriter.writeStartElement("parameters");
            _ipxactWriter.writeStartElement("parameter");
            _ipxactWriter.writeElement("name", "direction");
            _ipxactWriter.writeElement("value", "in");
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement(); // argument
        }
        j = _outArgList.iterator();
        while (j.hasNext()) {
            ADGVariable var = (ADGVariable) j.next();
            _ipxactWriter.writeStartElement("argument", "spirit:dataType=\"" + var.getDataType() + "\"");
            _ipxactWriter.writeElement("name", var.getName());
            _ipxactWriter.writeElement("value", "0");
            _ipxactWriter.writeStartElement("vendorExtensions");
            _ipxactWriter.writeStartElement("parameters");
            _ipxactWriter.writeStartElement("parameter");
            _ipxactWriter.writeElement("name", "direction");
            _ipxactWriter.writeElement("value", "out");
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement();
            _ipxactWriter.writeEndElement(); // argument
        }
        
        _ipxactWriter.writeEndElement(); // function
        _ipxactWriter.writeEndElement(); // fileSet
        
        _ipxactWriter.writeStartElement("fileSet");
        _ipxactWriter.writeElement("name", "fs-hds1");
        _ipxactWriter.writeStartElement("file", "spirit:fileId=\"f-hds1_h\"");
        _ipxactWriter.writeElement("name", "hds1.h");
        _ipxactWriter.writeElement("fileType", "cSource");
        _ipxactWriter.writeElement("isIncludeFile", "true");
        _ipxactWriter.writeEndElement();
        
        // send_command function
        _ipxactWriter.writeStartElement("function");
        _ipxactWriter.writeElement("entryPoint", "send_command");
        _ipxactWriter.writeElement("fileRef", "f-hds1_h");
        _ipxactWriter.writeElement("returnType", "void");
        _ipxactWriter.writeStartElement("argument", "spirit:dataType=\"int\"");
        _ipxactWriter.writeElement("name", "command");
        _ipxactWriter.writeElement("value", "0");
        _ipxactWriter.writeEndElement();
        _ipxactWriter.writeEndElement();
        
        // send_data function
        _ipxactWriter.writeStartElement("function");
        _ipxactWriter.writeElement("entryPoint", "send_data");
        _ipxactWriter.writeElement("fileRef", "f-hds1_h");
        _ipxactWriter.writeElement("returnType", "void");
        _ipxactWriter.writeStartElement("argument", "spirit:dataType=\"int\"");
        _ipxactWriter.writeElement("name", "data");
        _ipxactWriter.writeElement("value", "0");
        _ipxactWriter.writeEndElement();
        _ipxactWriter.writeEndElement();
        
        // read_obus function
        _ipxactWriter.writeStartElement("function");
        _ipxactWriter.writeElement("entryPoint", "read_obus");
        _ipxactWriter.writeElement("fileRef", "f-hds1_h");
        _ipxactWriter.writeElement("returnType", "int");
        _ipxactWriter.writeEndElement();
        
        _ipxactWriter.writeEndElement(); // fileSet
        
        _ipxactWriter.writeStartElement("fileSet");
        _ipxactWriter.writeElement("name", "fs-toolconfig");
        _ipxactWriter.writeStartElement("file", "spirit:fileId=\"f-toolconfig\"");
        _ipxactWriter.writeElement("name", "null");
        _ipxactWriter.writeElement("fileType", "unknown");
        _ipxactWriter.writeEndElement();
        _ipxactWriter.writeEndElement(); // fileSet
        
        _ipxactWriter.writeEndElement(); // fileSets
        _ipxactWriter.writeEndElement(); // component
    }
    
    
    
    /**
     * Writes an IP-XACT document for the CCU that has to be attached to this PowerPC.
     *
     * @param  x The corresponding PowerPC processor
     */
    public void visitComponent(PowerPC x) {
    }
    
    
    
    
    
    // /////////////////////////////////////////////////////////////////
    // // private methods ///
    
    /**
     * Open a file to write
     * 
     * @param fileName
     *            the fullpath file name
     */
    private PrintStream _openFile(String fileName) throws FileNotFoundException {
        PrintStream ps = null;
        String fn = "";
        
        System.out.println(" -- OPEN FILE: " + fileName);
        
        fn = _outputDir + "/" + fileName;
        if (fileName.equals(""))
            ps = new PrintStream(System.out);
        else
            ps = new PrintStream(new FileOutputStream(fn));
        
        return ps;
    }
    
    /**
     * Writes VLNV
     */
    private void _writeVLNV() {
        _ipxactWriter.writeElement("vendor", "LIACS");
        _ipxactWriter.writeElement("library", "DwarvIntegration");
        _ipxactWriter.writeElement("name", _adgNode.getFunction().getName());
        _ipxactWriter.writeElement("version", "1.0");
    }
    
    
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ///
    private String _coreName;
    
    private UserInterface _ui = null;
    
    private String _outputDir;
    
    private String _nodeDir;
    
    private String _projectName;
    
    private Mapping _mapping;
    
    private MicroBlaze _node;
    
    private ADGraph _adg;
    
    private ADGNode _adgNode; //corresponding ADG node
    
    private Vector _inArgList ;   //in arguments of the ADG function
    
    private Vector _adgInPorts ;      //in ports of the ADG node
    
    private Vector _outArgList ;      //out arguments of the ADG function
    
    private Vector _adgOutPorts ;     //out ports of the ADG node
    
    private PrintStream _xmlPS;
    
    private IpxactXmlWriter _ipxactWriter = null;
    
}
