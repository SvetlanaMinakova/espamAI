package espam.operations.codegeneration.erqian;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.main.cnnUI.DNNInitRepresentation;
import espam.operations.codegeneration.erqian.cpp.CPPSDFGVisitorErqian;
import espam.operations.codegeneration.erqian.h.HSDFGVisitorErqian;
import espam.utils.fileworker.FileWorker;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

public class ErqianSDFGVisitor {
        ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

     /**
     * Call CPP SDFG Visitor
     * @param y  CSDFGraph
     * @param dir directory for sesame templates
     * @param dnn corresponding deep neural network
     * @param mode  DNN transformation mode : layer-based, neuron-based or block-based
     */
     public static void callVisitor(Network dnn, DNNInitRepresentation mode, CSDFGraph y, String dir) {
         _setDummySchedule(dnn,mode);
         callVisitor(y,dir,true);
     }

        /**
     * Call CPP SDFG Visitor
     * @param y  CSDFGraph
     * @param dir directory for sesame templates
     *
     */
     public static void callVisitor(Vector<String> schedule, CSDFGraph y, String dir, boolean CNNRefined) {
         _cppVisitor.setSchedule(schedule);
         callVisitor(y,dir,CNNRefined);
     }


    /**
     * Call CPP SDFG Visitor
     * @param y  CSDFGraph
     * @param dir directory for sesame templates
     */
     public static void callVisitor(CSDFGraph y, String dir, boolean CNNRefined){
     String templatesDir = dir + "app/";
     try {
         /** if templates directory already exists,
          *  remove it and all templates inside it*/
         FileWorker.recursiveDelete(templatesDir);

         /** generate main class : app entry point*/
         _cppVisitor.generateMainClassTemplate(templatesDir,y);
         _hvisitor.generateMainClassTemplate(templatesDir,y);
         _cppVisitor.generateBaseClassTemplate(templatesDir);
         _hvisitor.generateBaseClassTemplate(templatesDir);

         _hvisitor.setCNNRefined(CNNRefined);
         /** generate .cpp and .h files for each SDF graph node */
         Iterator i = y.getNodeList().iterator();
         CSDFNode node;
         while (i.hasNext()) {
             node = (CSDFNode) i.next();
             _hvisitor.callVisitor(node, templatesDir);
             _cppVisitor.callVisitor(node, templatesDir);
         }
         _writeMakeFile(dir,y.getName());
         System.out.println("espamAI-erqian application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-erqian application generation error: " + e.getMessage());

        }
     }

     /**
      * write application makefile
     */
    private static void _writeMakeFile(String dir, String appName) {
        try {
            PrintStream mf = FileWorker.openFile(dir + "app/","Makefile",null);
            mf.println("#File is generated automatically by ESPAM");
            mf.println("");
            mf.println("appname := " + appName);
            mf.println("");
            mf.println("CXX := clang++");
            mf.println("CXXFLAGS := -std=c++11");
            mf.println("");
            mf.println("srcfiles := $(shell find . -maxdepth 1 -name \"*.cpp\")");
            mf.println("objects  := $(patsubst %.cpp, %.o, $(srcfiles))");
            mf.println("");
            mf.println("all: $(appname)");
            mf.println("");
            mf.println("$(appname): $(objects)");
            mf.println("\t$(CXX) $(CXXFLAGS) $(LDFLAGS) -o $(appname) $(objects) $(LDLIBS)");
            mf.println("");
            mf.println("depend: .depend");
            mf.println("");
            mf.println(".depend: $(srcfiles)");
            mf.println("\trm -f ./.depend");
            mf.println("\t$(CXX) $(CXXFLAGS) -MM $^>>./.depend;");
            mf.println("");
            mf.println("clean:");
            mf.println("\trm -f $(objects)");
            mf.println("");
            mf.println("dist-clean: clean");
            mf.println("\trm -f *~ .depend");
            mf.println("");
            mf.println("include .depend");
            mf.println("");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the default makefile");
            System.out.println("please supply your own makefile");
        }
    }

    /**
     * TODO block-based support!
     * Set dummy schedule for dnn running
     * @param dnn deep neural network
     * @param mode DNN transformation mode : layer-based, neuron-based or block-based
     */
     protected static void _setDummySchedule(Network dnn, DNNInitRepresentation mode) {
         if(mode.equals(DNNInitRepresentation.NEURONBASED))
             _setDummyScheduleNB(dnn);
         else
             _setDummyScheduleLB(dnn);

     }

    /**
    * Set dummy schedule for dnn running in Layer-based -mode
     * @param dnn deep neural network
     */
     protected static void _setDummyScheduleLB(Network dnn){
         Vector<String> schedule =  new Vector<>();
         dnn.sortLayersInTraverseOrder();
         for (Layer layer : dnn.getLayers()) {
             schedule.add(layer.getName());
         }
         _cppVisitor.setSchedule(schedule);
     }

     /**
      * TODO parallel neurons running??
     * Set dummy schedule for dnn running in Neuron-based -mode
     * @param dnn deep neural network
     */
     protected static void _setDummyScheduleNB(Network dnn){
          Vector<String> schedule =  new Vector<>();
          String neuronName;
         dnn.sortLayersInTraverseOrder();
         for (Layer layer : dnn.getLayers()) {
             neuronName = layer.getNeuron().getName();
             for(int i=0; i<layer.getNeuronsNum();i++) {
                 schedule.add(layer.getName() + "_" + neuronName + "_" + i);
             }
         }
         _cppVisitor.setSchedule(schedule);
     }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /** header-files visitor*/
    public static HSDFGVisitorErqian _hvisitor = new HSDFGVisitorErqian();

    /**C++ code-files visitor*/
    public static CPPSDFGVisitorErqian _cppVisitor = new CPPSDFGVisitorErqian();

}
