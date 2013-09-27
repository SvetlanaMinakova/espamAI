
/*
 * The Compaan Software Tool
 * Copyright (c) 2002 Leiden University (LIACS)
 * All rights reserved.
 *
 * Permission is hereby granted, to used for research and teaching but
 * NOT for commercial advantage. No permission is provided to freely
 * distribute this software and its documentation for any purpose. The
 * software provided hereunder is on an "as is" basis, and the
 * copyright holder has no obligation to provide maintenance, support,
 * updates, enhancements, or modifications.
 *
 */

package espam.visitor.yapiPN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import panda.datamodel.PandaException;
import panda.datamodel.parsetree.statement.CAMMemoryStatement;
import panda.datamodel.parsetree.statement.ForStatement;
import panda.datamodel.parsetree.statement.RootStatement;
import panda.main.UserInterface;
import panda.symbolic.expression.Expression;
import panda.symbolic.polynomial.PseudoPolynomial;
import panda.visitor.expression.CExpressionVisitor;
import panda.visitor.pseudopolynomial.CPolynomialVisitor;

//////////////////////////////////////////////////////////////////////////
//// PtolemyStatementVisitor

/**
 *  This class implements a Reorder Controller in C++ to reorder the
 *  token stream to the order required by the Consumer process.
 *
 * @author  Sharad Jain, Todor Stefanov, Bart Kienhuis
 * @version  $Id: PtolemyStatementVisitor.java,v 1.19 2002/07/31 14:22:54
 *      zissules Exp $
 */

public class YapiReorderControllerVisitor extends YapiStatementVisitor {
    
    /**
     *  Constructor for the PtolemyStatementVisitor object
     *
     * @param  printStream Description of the Parameter
     * @param  name Description of the Parameter
     */
    public YapiReorderControllerVisitor(PrintStream printStream,
                                        String name) {
        super(printStream, name);
        setPrefix("   ");
        _visitor = new CExpressionVisitor();
    }
    
    
    /**
     *  Print a for statement in the correct format for Java.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(ForStatement x) {
        String ubString = x.getUpperBound().accept(_visitor);
        String lbString = x.getLowerBound().accept(_visitor);
        
        _printStream.println(_prefix + "if (state == 1) {");
        _prefixInc();
        _printStream.println(_prefix + "this->" + x.getIterator() 
                                 + " = " + lbString + ";");
        _prefixDec();
        _printStream.println(_prefix + "}");
        
        _printStream.println(_prefix + "for (int "
                                 + x.getIterator() + " = ceil1("
                                 + "this->" + x.getIterator() + "); "
                                 + x.getIterator() + " <= "
                                 + "floor1("
                                 + ubString + "); "
                                 + x.getIterator() + " += "
                                 + x.getStepSize() + ") {");
        
        //_printStream.println(_prefix + "} // for " + x.getIterator());
    }
    
    /**
     *  Print the CAM Memory Statement the correct format for c++.
     *
     * @param  x Description of the Parameter
     */
    public void visitStatement(CAMMemoryStatement x) {
        
        // Get the UI.
        UserInterface _ui = UserInterface.getInstance();
        String name = x.getName();
        String t = "t" + x.getPort().getEdge().getName();
        
        _prefix = "";
        
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        _printStream.println("");
        _printStream.println("/*");
        _printStream.println(" * The Compaan Software Tool");
        _printStream.println(" * Copyright (c) 2002 Leiden University (LIACS)");
        _printStream.println(" * All rights reserved.");
        _printStream.println(" *");
        _printStream.println(" * Permission is hereby granted, to used for research and teaching but");
        _printStream.println(" * NOT for commercial advantage. No permission is provided to freely");
        _printStream.println(" * distribute this software and its documentation for any purpose. The");
        _printStream.println(" * software provided hereunder is on an \"as is\" basis, and the");
        _printStream.println(" * copyright holder has no obligation to provide maintenance, support,");
        _printStream.println(" * updates, enhancements, or modifications.");
        _printStream.println(" *");
        _printStream.println(" */");
        _printStream.println("");
        
        
        _printStream.println("/* This reoder controller is for variable");
        _printStream.println("");
        
        _printStream.println(" " 
                                 + x.getPort().getEdge().getDocumentation());
        
        _printStream.println(" ");
        _printStream.println("*/\n\n");
        
        _printStream.println("");
        _printStream.println("#include \"aux_func.h\"");
        _printStream.println("#include <map>");
        _printStream.println("#include \"yapi.h\"");
        _printStream.println("");
        _printStream.println("class " + name + " {\n");
        _printStream.println(" private:");
        _printStream.println("");
        _prefixInc();
        _printStream.println(_prefix + "/*  The function emptyMem */");
        _printStream.println(_prefix + "bool emptyMem (int key) {");
        _prefixInc();
        _printStream.println(_prefix + "iter1 = CAM.find(key) ; ");
        _printStream.println(_prefix + "if (iter1 != CAM.end()) {");
        _prefixInc();
        _printStream.println(_prefix + "return true;");
        _printStream.println(_prefix + "}");
        _prefixDec();
        _printStream.println(_prefix + "else {");
        _prefixInc();
        _printStream.println(_prefix + "return false;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + "/*  The function readFromMem */");
        _printStream.println(_prefix + t +" readFromMem (int key) {");
        _prefixInc();
        _printStream.println(_prefix + "Data *data;");
        
        if (_ui.getRec2Flag() || _ui.getPipRecFlag()) {
            _printStream.println(_prefix + "int multiplicity = 0;");
        } else {
            _printStream.println(_prefix + "int multiplicity;");
        }
        
        _printStream.println(_prefix + t +" token;");
        _printStream.println(_prefix + "data =  CAM[key] ;");
        
        if (!_ui.getRec2Flag() || !_ui.getPipRecFlag()) {
            _printStream.println(_prefix 
                                     + "multiplicity = data->getMultiplicity();");
        }
        
        _printStream.println(_prefix + "token = data->getToken();");
        
        if (_ui.getRec2Flag() || _ui.getPipRecFlag()) {
            _printStream.println(_prefix + "if( true ) {");
        } else {
            _printStream.println(_prefix + "if(multiplicity >= 1 ) {");
        }
        _prefixInc();
        
        _printStream.println(_prefix 
                                 + "data->setMultiplicity( multiplicity -- );");
        _printStream.println(_prefix 
                                 + "CAM.insert( cam::value_type(key, data ));");
        
        _prefixDec();
        if (_ui.getRec2Flag() || _ui.getPipRecFlag()) {
            _printStream.println(_prefix + "}");
        } else {
            _printStream.println(_prefix + "} else {");
            _prefixInc();
            _printStream.println(_prefix + " CAM.erase( key);");
            _prefixDec();
            _printStream.println(_prefix + "}");
        }
        
        
        _printStream.println(_prefix + "return token;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + "/*  The function readFromFifo */");
        _printStream.println(_prefix +  t +" readFromFifo (int key)  {");
        _prefixInc();
        _printStream.println(_prefix + t +" token; ");
        _printStream.println(_prefix + "Data *data; ");
        _printStream.println(_prefix + "do { ");
        _prefixInc();
        _printStream.println(_prefix + "read(*IP, token); ");
        _printStream.println(_prefix + "recoverKeyAndMultiplicity();");
        _printStream.println(_prefix + "if (Key != key) {");
        _prefixInc();
        _printStream.println(_prefix 
                                 + "data = new Data(token, Multiplicity);");
        _printStream.println(_prefix 
                                 + "CAM.insert(cam::value_type(Key, data ));");
        _prefixDec();
        if (_ui.getRec2Flag() || _ui.getPipRecFlag()) {
            _printStream.println(_prefix + "} else if ( true ) {");
        } else {
            _printStream.println(_prefix + "} else if (Multiplicity > 1) {");
        }
        _prefixInc();
        
        _printStream.println(_prefix 
                                 + "data = new Data(token, Multiplicity--); ");
        _printStream.println(_prefix 
                                 + "CAM.insert(cam::value_type(Key, data ));");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _prefixDec();
        _printStream.println(_prefix + " } while ( key != Key );");
        _printStream.println(_prefix + " return token;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printMultiMethod(x);
        _printStream.println("");
        _printReadMethod(x);
        _printStream.println("");
        _printScanMethod(x);
        _printStream.println("");
        _printRecoverMethod(x);
        _printStream.println("");
        _printStream.println(" public:");
        _printStream.println("");
        _printStream.println(_prefix + "/**  The function Controller */");
        _printStream.println(_prefix + name + "(InPort<" + t + "> *ip) {");
        _prefixInc();
        _printStream.println(_prefix + "IP = ip;");
        _printStream.println(_prefix + "state = 1;");
        _initVariables(x);
        _prefixDec();
        _printStream.println(_prefix + "};");
        _printStream.println("");
        
        // Print Content
        _printParameters(x);
        _printStream.println("");
        _printFromMethod(x);
        _printStream.println("");
        
        // Print the inner class
        _printStream.println(_prefix + "//////////////////////////////////");
        _printStream.println(_prefix + "////       inner class         ///");
        _printStream.println("");
        _printStream.println(_prefix + "class Data  {");
        _prefixInc();
        _printStream.println("");
        _printStream.println(_prefix + " public:");
        _printStream.println(_prefix + "Data(" 
                                 + t +" token, int multiplicity) {");
        _prefixInc();
        _printStream.println(_prefix + " _token = token;");
        _printStream.println(_prefix + " _multiplicity = multiplicity;");
        _prefixDec();
        _printStream.println(_prefix + "};");
        _printStream.println("");
        _printStream.println(_prefix 
                                 + "void setMultiplicity(int multiplicity) {");
        _prefixInc();
        _printStream.println(_prefix + " _multiplicity = multiplicity;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + "int getMultiplicity() {");
        _prefixInc();
        _printStream.println(_prefix + " return _multiplicity;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + "void setToken(" + t + " token) {");
        _prefixInc();
        _printStream.println(_prefix + " _token = token;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + t +" getToken(){");
        _prefixInc();
        _printStream.println(_prefix + " return _token;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _printStream.println("");
        _printStream.println(_prefix + "private:");
        _printStream.println("");
        _printStream.println(_prefix + t +" _token;");
        _printStream.println(_prefix + "int _multiplicity;");
        _printStream.println("");
        _prefixDec();
        _printStream.println(_prefix + "};");
        
        // Print Variables
        
        _printStream.println("");
        _printStream.println(_prefix + "private:");
        _printStream.println(_prefix + "//  The reordering memory :");
        _printStream.println("");
        _printStream.println(_prefix + "typedef map< int, Data* , less<int> >  cam;");
        _printStream.println(_prefix + "cam CAM ;");
        _printStream.println(_prefix + "cam::const_iterator iter1 ;");
        _printStream.println("");
        
        Iterator parameters = x.getParameters().iterator();
        
        while (parameters.hasNext()) {
            _printStream.println(_prefix + "//private member ");
            _printStream.println(_prefix 
                                     + "int " + parameters.next().toString() + ";");
            _printStream.println("");
        }
        
        _printStream.println("");
        _printStream.println(_prefix + "//private member ");
        _printStream.println(_prefix + "InPort<" + t + "> *IP;");
        _printStream.println("");
        _printStream.println(_prefix + "//private member ");
        _printStream.println(_prefix + "int Key;");
        _printStream.println("");
        _printStream.println(_prefix + "//private member ");
        _printStream.println(_prefix + "int Multiplicity;");
        _printStream.println("");
        _printStream.println(_prefix + "//private member ");
        _printStream.println(_prefix + "int state;");
        _printStream.println("");
        _printVariables(x);
        _printStream.println("");
        
        // Print Content
        _prefixDec(); 
        _printStream.println("};");
        _printStream.println("");
        _printStream.println("");
        _printStream.println("#endif");
    }
    
    ///////////////////////////new/////////////////////////////////
    
    /**
     */
    Vector listIterators;
    
    /**
     */
    Vector listParameters;
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _initVariables(CAMMemoryStatement x) {
        
        Iterator i = x.getLoopIterators().iterator();
        
        while (i.hasNext()) {
            ForStatement p = (ForStatement) i.next();
            Expression exp = p.getLowerBound();
            
            _printStream.println(_prefix + "//private member ");
            _printStream.println(_prefix + p.getIterator() 
                                     + " = " + exp.toString() + ";");
            _printStream.println("");
        }
        
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     * @param  s Description of the Parameter
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     * @exception  PandaException Description of the Exception
     */
    private PrintStream _openMakefileFile(String s)
        throws FileNotFoundException, PandaException {
        
        PrintStream printStream;
        UserInterface ui = UserInterface.getInstance();
        
        String directory = null;
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename.
        if (ui.getOutputFileName() == "") {
            directory = ui.getBasePath() + "/" + ui.getFileName();
        } else {
            directory = ui.getBasePath() + "/" + ui.getOutputFileName();
        }
        File dir = new File(directory);
        
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new PandaException("could not create " +
                                         "directory '" + dir.getPath() + "'.");
            }
        }
        
        String fullFileName = dir + "/" + s + ".h";
        
        System.out.println(" -- OPEN FILE: " + fullFileName);
        
        OutputStream file = null;
        
        file = new FileOutputStream(fullFileName);
        printStream = new PrintStream(file);
        return printStream;
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printFromMethod(CAMMemoryStatement x) {
        
        String t = "t" + x.getPort().getEdge().getName();
        _printStream.println(_prefix + "/* The function getFrom */");
        _printStream.print(_prefix + t +" getFrom(");
        
        // Parameters
        Iterator inputIndices = x.getDataControlVector().iterator();
        
        while (inputIndices.hasNext()) {
            _printStream.print("int " + inputIndices.next().toString());
            if (inputIndices.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(") { ");
        _prefixInc();
        
        // Body
        _printStream.println(_prefix + "int key;");
        _printStream.print(_prefix + "key = getReadAddress(");
        inputIndices = x.getDataControlVector().iterator();
        while (inputIndices.hasNext()) {
            _printStream.print(inputIndices.next().toString());
            if (inputIndices.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(_prefix + ");");
        _printStream.println(_prefix + "if (emptyMem(key)) {");
        _prefixInc();
        _printStream.println(_prefix + "return readFromMem(key);");
        _prefixDec();
        _printStream.println(_prefix + "} else {");
        _prefixInc();
        _printStream.println(_prefix + "return readFromFifo(key);");
        _prefixDec();
        _printStream.println(_prefix + "}");
        _prefixDec();
        _printStream.println(_prefix + "}");
        
    }
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printMultiMethod(CAMMemoryStatement x) {
        
        _printStream.println(_prefix + "/* The function multi */");
        _printStream.print(_prefix + "int multi0(");
        
        Iterator outputIndices = x.getOutputPortIndex().iterator();
        
        while (outputIndices.hasNext()) {
            _printStream.print("int " + outputIndices.next().toString());
            if (outputIndices.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(") {");
        // _prefixInc();
        // Print the Pseudo Polynomial
        try {
            CPolynomialVisitor visitor
                = new CPolynomialVisitor(x.getOutputPortIndex(), "multi");
            PseudoPolynomial multiplicity = x.getMultiplicity();
            
            multiplicity.accept(visitor);
            
            String mul = visitor.toString();
            //String mul = visitor.visit(multiplicity).toString();
            _printStream.println(_prefix + mul);
        }
        catch (IOException e) {
            throw new Error(e.getMessage());
        }
        
        // _prefixDec();
        _printStream.println(_prefix + "}");
        
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printParameters(CAMMemoryStatement x) {
        Iterator parameters = x.getParameters().iterator();
        
        _printStream.println(_prefix 
                                 + "/* The function setControllerParameters */");
        _printStream.print(_prefix + "void setControllerParameters(");
        while (parameters.hasNext()) {
            _printStream.print("int " + parameters.next().toString());
            if (parameters.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(") {");
        _prefixInc();
        parameters = x.getParameters().iterator();
        while (parameters.hasNext()) {
            String parameterName = (String) parameters.next();
            
            _printStream.println(_prefix + "this->" + parameterName
                                     + " = " + parameterName + ";");
        }
        _prefixDec();
        _printStream.println(_prefix + "}");
        
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printReadMethod(CAMMemoryStatement x) {
        _printStream.println(_prefix + "/* The function Read Address */");
        _printStream.print(_prefix + "int getReadAddress(");
        
        Iterator inputIndices = x.getDataControlVector().iterator();
        
        while (inputIndices.hasNext()) {
            _printStream.print("int " + inputIndices.next().toString());
            if (inputIndices.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(") {");
        // Print the Scan Function
        _prefixInc();
        _printStream.print(_prefix + "return scan" + x.getNodeName() + "( "); 
        Iterator i = x.getMapping().iterator();
        while(i.hasNext()) {
            Expression exp = (Expression)i.next();
            _printStream.print(exp.accept(_visitor));
            if (i.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(");");
        _prefixDec();
        _printStream.println(_prefix + "}");
        
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printRecoverMethod(CAMMemoryStatement x) {
        _printStream.println(_prefix 
                                 + "/* The function recoverKeyAndMultiplicity */");
        _printStream.println(_prefix + "void recoverKeyAndMultiplicity() {");
        _prefixInc();
        _printStream.println(_prefix + "int stop = 0;");
        Iterator i = x.getLoopIterators().iterator();
        while (i.hasNext()) {
            ForStatement p = (ForStatement) i.next();
            p.accept( this );
            _prefixInc();
        }
        
        //_prefixInc();
        
        _printStream.println(_prefix + "if (stop == 1) {");
        _prefixInc();
        i = x.getLoopIterators().iterator();
        while (i.hasNext()) {
            ForStatement p = (ForStatement) i.next();
            _printStream.println(_prefix + "this->" 
                                     + p.getIterator() + " = " + p.getIterator() + ";");
            
        }
        _printStream.println(_prefix + "state = 0;");
        _printStream.println(_prefix + "return;");
        _prefixDec();
        _printStream.println(_prefix + "}");
        
        
        RootStatement root = x.getOutputFilter();
        root.accept( this );
        
        boolean state = false;
        Iterator j = x.getLoopIterators().iterator();
        while (j.hasNext()) {
            _prefixDec();
            ForStatement p = (ForStatement) j.next();
            _printStream.println(_prefix + "} // for ");
            if ( state == false ) {
                _printStream.println(_prefix + "state = 1;");
                state = true;
            }
        }
        
        _prefixDec();
        
        // Finish of this function
        _printStream.println(_prefix + "}");
        
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printScanMethod(CAMMemoryStatement x) {
        _printStream.println(_prefix + "/* The function Scan */");
        _printStream.print(_prefix + "int scan" + x.getNodeName() + "(");
        
        Iterator outputIndices = x.getOutputPortIndex().iterator();
        
        while (outputIndices.hasNext()) {
            _printStream.print("int " + outputIndices.next().toString());
            if (outputIndices.hasNext()) {
                _printStream.print(", ");
            }
        }
        _printStream.println(") {");
        _prefixInc();
        try {
            CPolynomialVisitor visitor
                = new CPolynomialVisitor(x.getOutputPortIndex(), "scan");
            // Print the Pseudo Polynomial
            PseudoPolynomial scanPolynomial = x.getScan();
            scanPolynomial.accept(visitor);
            String scanString = visitor.toString();
            _printStream.println(_prefix + scanString);
        }
        catch (IOException e) {
            throw new Error(e.getMessage());
        }
        _prefixDec();
        _printStream.println(_prefix + "}");
        
    }
    
    
    /**
     *  Description of the Method
     *
     * @param  x Description of the Parameter
     */
    private void _printVariables(CAMMemoryStatement x) {
        Iterator i = x.getLoopIterators().iterator();
        while (i.hasNext()) {
            ForStatement p = (ForStatement) i.next();
            Expression exp = p.getLowerBound();
            _printStream.println(_prefix + "//private member ");
            _printStream.println(_prefix + "double "
                                     + p.getIterator() + ";");
            _printStream.println("");
        }
        
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     *  The Expressions visitor.
     */
    private CExpressionVisitor _visitor = null;
    
    
}

