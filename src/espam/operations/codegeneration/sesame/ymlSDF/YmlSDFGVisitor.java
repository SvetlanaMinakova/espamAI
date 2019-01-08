package espam.operations.codegeneration.sesame.ymlSDF;

import espam.datamodel.graph.csdf.*;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.main.UserInterface;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;


/**
 *  This class implements creation of Yml file for CSDFGraph class
 *
 * @author Svetlana Minakova
 */
public class YmlSDFGVisitor extends CSDFGraphVisitor{

    /**
     *  Constructor for the YmlSDFGVisitor object
     *
     */
    public YmlSDFGVisitor() {
        _prefix = "   ";
        _cntr = 0;
        _uniqueOperationsNames.clear();
    }

     ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

     /**
     * Call YML SDFG visitor
     * @param sdfg SDFG to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public void callVisitor(CSDFGraph sdfg, String dir){
           try {
               _printStream = FileWorker.openFile(dir,sdfg.getName()+"_app","yml");
               sdfg.accept(this);
               _writeMakeFile(dir);
                // System.out.println("YML file created: " + dir + sdfg.getName() + ".yml");
            }
            catch(Exception e) {
             System.err.println("YML file creation error . " + e.getMessage());
            }
    }

    /** Visit SDF Graph:
     * (1) print common header
     * (2) print .so description. Usually, all the libraries/packages, required for
     *   normal nodes processing are packed in one common .so with the name of
     *   the project
     * (3) visit all graph nodes
     * @param  x SDF Graph
     */
    public void visitComponent( CSDFGraph x ) {
       // _pn = x;
        _sdfg = x;
        _printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _printStream.println("<network xmlns=\"http://sesamesim.sourceforge.net/YML\" name=\"application\" class=\"KPN\">");
        _printStream.println("");
        _printStream.println(_prefix + "<property name=\"library\" value=\"lib" + x.getName() + ".so\"/>");
        _printStream.println("");

        CSDFNode node;
        CSDFEdge edge;

        /** Visit all nodes */
        Iterator i = x.getNodeList().iterator();
        while( i.hasNext() ) {
            node = (CSDFNode) i.next();
            node.accept(this);
        }

        /** Visit all edges*/
        i = x.getEdgeList().iterator();
        while( i.hasNext() ) {
            edge = (CSDFEdge) i.next();
            edge.accept(this);
        }

        _prefixDec();

        _printStream.println("</network>");

    //    YmlProcessVisitor pt = new YmlProcessVisitor();
    //    x.accept( pt );

    //    _writeMakeFile();
    //    _writeSourceFile();
    }

     /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CSDFNode x ) {

        String name = x.getName();
        _printStream.println(_prefix + "<node name=\"" + name + "\" class=\"CPP_Process\""  + ">");
        _prefixInc();
        _printStream.println(_prefix + "<property name=\"class\" value=\"" + _getBaseClassName(x)+ "\"/>");
        _printStream.println(_prefix + "<property name=\"header\" value=\"" + name + ".h\"/>");
        _printStream.println(_prefix + "<property name=\"source\" value=\"" + name + ".cpp\"/>");
        /** TODO any ports descriptions?*/
        _printStream.println("");

        //visit the list of ports of this CDProcess
        String dir = "";
        Vector portList = (Vector) x.getPortList();
        if( portList != null ) {
            Iterator i = portList.iterator();
            while( i.hasNext() ) {
                CSDFPort port = (CSDFPort) i.next();
                if( port.getType().equals(CSDFPortType.in)) {
                    dir = "in";
                } else if(  port.getType().equals(CSDFPortType.out) ) {
                    dir = "out";
                }

                MemoryUnit mu = port.getAssignedMemory();
                String type;
                if(mu==null)
                    type = _stdDataType;
                else
                    type = mu.getTypeDesc();

                String t = "char";
                if (type != null) {
                    if (!type.equals("")) {
                        t = type;
                    }
                }

                _printStream.println(_prefix + "<port name=\"" + port.getName() + "\" dir=\"" + dir + "\">");
                _prefixInc();
                _printStream.println(_prefix + "<property name=\"type\" value=\"" + t + "\"/>");
               // _printStream.println(_prefix + "<property name=\"pos\" value=\"\"/>");
                _prefixDec();
                _printStream.println(_prefix + "</port>");
            }
        }

        _prefixDec();
        _printStream.println(_prefix + "</node>");
        /** get unique operation id*/
         String operation = x.getOperation();
        int operationId;
        if(_uniqueOperationsNames.contains(operation)){
            operationId = _uniqueOperationsNames.indexOf(operation);
        }
        else {
            _cntr++;
            operationId = _cntr;
            _uniqueOperationsNames.add(operation);
        }


        _printStream.println(_prefix + "<property name=\"operation:" + operation + "\" value=\"" + operationId + "\" />" );

        _printStream.println("");
    }

     /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CSDFEdge x ) {

        CSDFPort src = x.getSrc();
        String srcPortName = src.getName();
        String nameSrcNode = src.getNode().getName();

        CSDFPort dst = x.getDst();
        String dstPortName = dst.getName();
        String nameDstNode = dst.getNode().getName();

        _printStream.print(_prefix + "<link innode=\""    + nameSrcNode + "\" " +
                             "inport=\""    + srcPortName    + "\" " +
                             "outnode=\""   + nameDstNode   + "\" " +
                             "outport=\""   + dstPortName      + "\" " + ">");

        _prefixInc();
       // _printStream.println(_prefix + "<property name=\"pos\" value=\"\"/>");
        _prefixDec();
        _printStream.println(" </link>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     */
    private void _writeMakeFile(String dir) {
        try {
            // create the makefile
            //PrintStream mf = _openMakefileFile();

            PrintStream mf = FileWorker.openFile(dir,"Makefile",null);
            mf.println("#@Author: ESPAM");
            mf.println("");
            mf.println("# include ./sources");
            mf.println("");
            mf.println("NAME=" + _sdfg.getName());
            mf.println("");
            mf.println("YML=$(NAME)_app.yml");
            mf.println("APPVIRTUAL_MAP=../$(NAME)_appvirt_map.yml");
            mf.println("");
            mf.println("APP_LIB=lib$(NAME).so");
            mf.println("STUB=$(NAME)_stub");
            mf.println("");
            mf.println("NODESRCS := $(shell PNRunnerYMLTool --sources $(YML))");
            mf.println("CLASSES := $(shell PNRunnerYMLTool --classes $(YML))");
            mf.println("BASES := $(patsubst %,%_Base.h,$(CLASSES))");
            mf.println("SOURCES := $(STUB).cpp $(NODESRCS)");
            mf.println("OBJS := $(patsubst %.cpp,%.o,$(SOURCES)) $(OBJ_FILES)");
            mf.println("");
            mf.println("CXX=g++");
            mf.println("CFLAGS += `sesamesim-config --PNRunner-cflags` -Wall -std=c++11");
            mf.println("LIBS += `sesamesim-config --PNRunner-libs`");
            mf.println("APPFLAGS := $(APPFLAGS) -L . -l $(APP_LIB)");
            mf.println("");
            mf.println("all: build");
            mf.println("");
            mf.println("build: base_classes $(APP_LIB)");
            mf.println("");
            mf.println("base_classes: $(YML)");
            mf.println("\tfor i in $(CLASSES); do\\");
            mf.println("\t  PNRunnerYMLTool --create-base-class $(YML) $$i >$${i}_Base.tmp;\\");
            mf.println("\t  diff $${i}_Base.h $${i}_Base.tmp 2>/dev/null >/dev/null;\\");
            mf.println("\t  if [ $$? -ne 0 ]; then mv $${i}_Base.tmp $${i}_Base.h;\\");
            mf.println("\t  else rm -f $${i}_Base.tmp; fi;\\");
            mf.println("\tdone");
            mf.println("");
            mf.println("$(APP_LIB): $(OBJS)");
            mf.println("\t$(CXX) $(CFLAGS) $(OBJS) $(LIBS) -o $@");
            mf.println("");
            mf.println("$(STUB).cpp: $(YML)");
            mf.println("\tPNRunnerYMLTool --create-stubs $(YML) >$@.tmp");
            mf.println("\tdiff $@ $@.tmp 2>/dev/null >/dev/null;\\");
            mf.println("\tif [ $$? -ne 0 ]; then mv $@.tmp $@;\\");
            mf.println("\telse rm -f $@.tmp; fi");
            mf.println("");
            mf.println("%.o: %.cpp %.h %_Base.h");
            mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
            mf.println("%.o: %.cpp %.h");
            mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
            mf.println("%.o: %.cpp");
            mf.println("\t$(CXX) -c $(CFLAGS) $<");
            mf.println("");
            mf.println("run: build");
            mf.println("\tPNRunner $(APPFLAGS) $(YML)");
            mf.println("");
            mf.println("runmap: build");
            mf.println("\tPNRunner $(APPFLAGS) -m $(APPVIRTUAL_MAP) $(YML)");
            mf.println("");
            mf.println("runtrace: build");
            mf.println("\tPNRunner $(APPFLAGS) -T ../trace -m $(APPVIRTUAL_MAP) $(YML)");
            mf.println("");
            mf.println("clean:");
            mf.println("\trm -rf *.o *~ \\#* *.jpg core* \\");
            mf.println("\t$(APP_LIB) $(STUB).cpp $(BASES)");
            mf.println("");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the default makefile");
            System.out.println("please supply your own makefile");
        }
    }

    /**
     * Write .cpp file, corresponding to SDFG node source code
     */
    private void _writeSourceFile() {
        try {
            // create the makefil
            PrintStream sf = _openSourceFile();

            //System.out.print("Generating config file .........");

            sf.println("#source file for YML Process Networks");
            sf.println("#@Author: ESPAM");
            sf.println(" ");
            sf.println("OBJ_FILES = ");

            //System.out.println("[Done]");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannt create the default source file");
            System.out.println("please supply your own config file");
        }
    }

    /**
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openMakefileFile()
        throws FileNotFoundException {

        PrintStream printStream = null;
        UserInterface ui = UserInterface.getInstance();
        String fullFileName = "";
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename. (REFACTOR)
        if( ui.getOutputFileName() == "" ) {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getFileName() + "/app/Makefile";
        } else {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getOutputFileName() + "/app/Makefile";
        }
        OutputStream file = null;

        System.out.println(" -- OPEN FILE: " + fullFileName);
        file = new FileOutputStream( fullFileName );
        printStream = new PrintStream( file );
        return printStream;
    }

    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     * @exception  FileNotFoundException Description of the Exception
     */
    private static PrintStream _openSourceFile()
        throws FileNotFoundException {

        PrintStream printStream = null;
        UserInterface ui = UserInterface.getInstance();
        String fullFileName = "";
        // Create the directory indicated by the '-o' option. Otherwise
        // select the orignal filename. (REFACTOR)
        if( ui.getOutputFileName() == "" ) {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getFileName() + "/app/sources";
        } else {
            fullFileName =
                ui.getBasePath() + "/" +
                ui.getOutputFileName() + "/app/sources";
        }
        OutputStream file = null;

        System.out.println(" -- OPEN FILE: " + fullFileName);
        file = new FileOutputStream( fullFileName );
        printStream = new PrintStream( file );
        return printStream;
    }

    /**
     * Get CSDFNode base .cpp class. If SDF Node have group, the base class is a group class.
     * Otherwise, base class is node own class
     * @param node CSDFNode
     * @return CSDFNode base .cpp class
     */
    private String _getBaseClassName(CSDFNode node){

    //    String baseClass = node.getGroup();

    //    if(baseClass==null || baseClass=="")
          String  baseClass = node.getName();
        return baseClass;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CSDFGraph _sdfg = null;

    /** number of unique operations*/
    private int _cntr = 0;

    /**unique operations names*/
    Vector<String> _uniqueOperationsNames = new Vector<>();


    /** Standard data type (if no custom memory is assigned to port, the int-type memory will be assigned)*/
    private String _stdDataType = "int";



}
