package espam.visitor.pthread;

import espam.datamodel.graph.Node;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.main.cnnUI.DNNInitRepresentation;
import espam.utils.fileworker.FileWorker;
import espam.visitor.pthread.cpp.CPPSDFGVisitorPthread;
import espam.visitor.pthread.h.HSDFGVisitorPthread;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

public class PthreadSDFGVisitor {
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
         _hvisitor.generateMainClassTemplate(templatesDir);

         _cppVisitor.generateBaseClassTemplate(templatesDir);
         _hvisitor.generateBaseClassTemplate(templatesDir);

         _hvisitor.generateFuncClassTemplate(templatesDir);
         _cppVisitor.generateFuncClassTemplate(templatesDir);


         _hvisitor.setCNNRefined(CNNRefined);
         /** generate .cpp and .h files for each SDF graph node */
         Iterator i = y.getNodeList().iterator();
         CSDFNode node;
         while (i.hasNext()) {
             node = (CSDFNode) i.next();
             _hvisitor.callVisitor(node, templatesDir);
             _cppVisitor.callVisitor(node, templatesDir);
         }
         Vector<String> classesList = _getClassesList(y);
         _writeMakeFile(dir,y.getName(),classesList);
         System.out.println("espamAI-Pthread application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Pthread application generation error: " + e.getMessage());

        }
     }

    /**
     * TODO REFACTOR FOR NB MODE BASE CLASSES
     * get list of classes names
     * @return list of classes names
     */
    private static Vector<String> _getClassesList(CSDFGraph csdfG){
        Vector<String>classesList = new Vector<>();
        Iterator i = csdfG.getNodeList().iterator();
            while (i.hasNext()) {
                CSDFNode node = (CSDFNode) i.next();
                classesList.add(node.getName());
            }

        classesList.add(_dnnFuncClassName);
        classesList.add(_funcClassName);
        classesList.add(_baseClassName);
        classesList.add(_mainClassName);

        classesList.add("run");
        classesList.add("fifo");
        return classesList;
     }

     /**
      * write application makefile
     */
    private static void _writeMakeFile(String dir, String appName, Vector<String> classesList) {
        try {
            PrintStream mf = FileWorker.openFile(dir + "app/","Makefile",null);
            mf.println("CXX=g++");
            mf.println("");
            mf.println("CXXFLAGS= -std=c++11 -pthread ");
            mf.println("");

            mf.print("OBJS = ");
            for(String classname: classesList)
                mf.print(classname+".o ");

            mf.println("");
            mf.println("");
            mf.println("PRG = run");
            mf.println("");
            mf.println("all: ${PRG}");
            mf.println("");
            mf.println("");
            mf.println("run:  ${OBJS}");
            mf.println("\t${CXX} ${CXXFLAGS} -o $@ ${OBJS}");
            mf.println("");
            for(String classname: classesList){
                mf.println(classname+".o: " + classname + ".cpp");
                mf.println("\t${CXX} ${CXXFLAGS} -c -g $?");
                mf.println("");
            }
            mf.println("");
            mf.println("");
            mf.println("clean:");
            mf.println("\trm -rf *~ *.o ");
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
    public static HSDFGVisitorPthread _hvisitor = new HSDFGVisitorPthread();

    /**C++ code-files visitor*/
    public static CPPSDFGVisitorPthread _cppVisitor = new CPPSDFGVisitorPthread();

        /** application main class name*/
    private static String _mainClassName = "appMain";

    /** CSDF graph node base class*/
    private static String _baseClassName = "csdfNode";


    /** CSDF graph node functions class*/
    private static String _funcClassName = "appFunc";

    /** DNN node functions class*/
    private static String _dnnFuncClassName = "dnnFunc";

}
