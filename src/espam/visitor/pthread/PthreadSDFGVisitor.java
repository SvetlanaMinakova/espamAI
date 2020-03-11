package espam.visitor.pthread;


import espam.datamodel.graph.Node;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.mapping.Mapping;
import espam.utils.fileworker.FileWorker;
import espam.visitor.pthread.cpp.CPPSDFGVisitorPthread;
import espam.visitor.pthread.h.HSDFGVisitorPthread;
import espam.visitor.xml.csdf.MappingXMLVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

public class PthreadSDFGVisitor {
     ///////////////////////////////////////////////////////////////////
     ////                         public methods                     ///

     /**
     * Call SDFG Visitor
     * @param y  CSDFGraph
     * @param dir directory for sesame templates
     * @param dnn corresponding deep neural network
     */
     public static void callVisitor(Network dnn, CSDFGraph y, String dir) {
         _setDummySchedule(dnn,y);
         //setLoadWeightsFolder(dir.replace("/pthread/","/weights/"));
         setLoadWeights();
         if(_generateDNNFuncNA) {
             _setDataTypes("DATA", "DATA");
             _setMUDataTypes(y,"DATA");
         }
         else
            _setDataTypes(dnn.getDataType(),dnn.getWeightsType());

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
        // setLoadWeightsFolder(dir.replace("/pthread/","/weights/"));
         setLoadWeights();
         if(_generateDNNFuncNA) {
             _setDataTypes("DATA", "DATA");
             _setMUDataTypes(y,"DATA");
         }
         _setDataTypes(y.getTokenDesc(),y.getTokenDesc());
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

         _hvisitor.setCNNRefined(CNNRefined);

         /** generate classes (.cpp and .h files) for each SDF graph node */
         Iterator i = y.getNodeList().iterator();
         CSDFNode node;
         while (i.hasNext()) {
             node = (CSDFNode) i.next();
             _hvisitor.callVisitor(node, templatesDir);
             _cppVisitor.callVisitor(node, templatesDir);
         }

           /**
          * Generate classes, used by all the node-classes of
          * the application
          */
         _generateAppStandardFuncs(templatesDir,y);


         System.out.println("espamAI-Pthread application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Pthread application generation error: " + e.getMessage());

        }
     }


    /**
     * Generate application-independent classes
     */
    private static void _generateAppStandardFuncs(String dir,CSDFGraph csdfGraph){

         /** generate main class : app entry point*/
         _cppVisitor.generateMainClassTemplate(dir,csdfGraph);
         _hvisitor.generateMainClassTemplate(dir);

         _cppVisitor.generateBaseClassTemplate(dir);
         _hvisitor.generateBaseClassTemplate(dir);

         _hvisitor.generateFuncClassTemplate(dir);
         _cppVisitor.generateFuncClassTemplate(dir,_generateDNNFuncCPU,_generateDNNFuncGPU);

         //FIFO
         _cppVisitor.generateFIFOClassTemplate(dir);
         _hvisitor.generateFIFOClassTemplate(dir);

         //run.cpp
         _cppVisitor.generateRunClassTemplate(dir);

         //types.h
         _hvisitor.generatetypesClassTemplate(dir);

         //data load
        _hvisitor.generateDataLoadClassTemplate(dir);
        _cppVisitor.generateDataLoadClassTemplate(dir);

        //algorithm
        if(!_generateDNNFuncNA){
        _hvisitor.generateBlockAlgorithmClassTemplate(dir);
        _cppVisitor.generateAlgorithmClass(dir, csdfGraph);
        }

         Vector<String> classesList = _getClassesList(csdfGraph);

        boolean makeFileNotGenerated = true;
        /**Internal CNN operators library generation*/

         if(_generateDNNFuncGPU){
              //_writeMakeFile(dir,csdfGraph.getName(),classesList);
             _writeMakeFileCuda(dir,csdfGraph.getName(),classesList);
             makeFileNotGenerated = false;
         }

         if(_generateDNNFuncNA){
            /** may be changed!*/
            Vector<String> naClassesList = _getNaClassesList();
            _writeMakeFileNA(dir,csdfGraph.getName(),classesList,naClassesList);
            makeFileNotGenerated = false;
         }

         if(makeFileNotGenerated)
             _writeMakeFile(dir,csdfGraph.getName(),classesList);

        /** configuration file for libcnpy*/
        _writeConfig(dir,(_generateDNNFuncCPU||_generateDNNFuncGPU||_generateDNNFuncNA));
    }

    /** get NA-specific classes list
     * TODO: may be changed
     * */
     private static Vector<String> _getNaClassesList(){
        Vector naClasses = new Vector<>();
        //heap_4.o soc_drivers.o types2.o paramIO.o neumem.o ConvLayer.o dnnFunc.o
        naClasses.add("heap_4");
        naClasses.add("soc_drivers");
        naClasses.add("types2");
        naClasses.add("paramIO");
        naClasses.add("neumem");
        naClasses.add("ConvLayer");
        naClasses.add("dnnFunc");
        return naClasses;
     }

    /**
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

        if(_generateDNNFuncCPU||_generateDNNFuncGPU)
            classesList.add(_dnnFuncClassName);

        classesList.add(_funcClassName);
        classesList.add(_baseClassName);
        classesList.add(_mainClassName);


        if(_loadWeights)
          classesList.add(_loadWeightsClassName);

        classesList.add("run");
        classesList.add("fifo");
        if(_generateDNNFuncCPU||_generateDNNFuncGPU)
            classesList.add("blocks_algorithm");

        return classesList;
     }

     /**
      * write application makefile
     */
    private static void _writeMakeFile(String dir, String appName, Vector<String> classesList) {
        try {
            PrintStream mf = FileWorker.openFile(dir,"Makefile",null);
            mf.println("CXX=g++");
            mf.println("");
            mf.println("CXXFLAGS= -std=c++11 -pthread ");
            mf.println("");
            if(_loadWeights)
                mf.println("CXXLIB = -L./ -lcnpy -lz");

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
            mf.print("\t${CXX} ${CXXFLAGS} -o $@ ${OBJS}");
            if(_loadWeights)
                mf.print(" ${CXXLIB}");
            mf.println("");
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
      * write application makefile with neuraghe library support
     */
    private static void _writeMakeFileNA(String dir, String appName, Vector<String> classesList, Vector<String> NaClassesList) {
       try {
            PrintStream mf = FileWorker.openFile(dir,"Makefile",null);
            mf.println("CXX=arm-linux-gnueabihf-g++");
            mf.println("CC=arm-linux-gnueabihf-gcc");
            mf.println("AR=arm-linux-gnueabihf-ar");
            mf.println("NVCC=nvcc");
            mf.println("FIXED:=1");
            mf.println("");
            mf.println("");
            mf.println("CXXFLAGS= -std=c++11 -pthread -O3 -fopenmp -march=armv7-a -mtune=cortex-a9 -mcpu=cortex-a9 -mfpu=neon -mfloat-abi=hard -ftree-vectorize -mvectorize-with-neon-quad -fprefetch-loop-arrays");
            mf.println("");
            mf.println("CCFLAGS = -pthread -O3 -fopenmp -march=armv7-a -mtune=cortex-a9 -mcpu=cortex-a9 -mfpu=neon -mfloat-abi=hard -ftree-vectorize -mvectorize-with-neon-quad -fprefetch-loop-arrays");
            mf.println("");
            mf.println("");
            mf.println("ARCH=arm");
            mf.println("ACC_FLAGS += -D _ARM_");
            mf.println("CFLAGS=-Wall -Wno-unused-result -Wno-unknown-pragmas -Wfatal-errors -fPIC -fpermissive -Wno-error");
            mf.println("");
            mf.println("CXXLIB = -L./ -lcnpy -lz");

            mf.print("OBJS = ");
            for(String classname: classesList)
                mf.print(classname+".o ");

            /** may be changed*/
            for(String classname: NaClassesList)
                mf.print(classname + ".o ");

            mf.println("");
            mf.println("");
            mf.println("ifeq (${FIXED},1)");
            mf.println("TYPE_FLAGS = -D_FIXED_");
            mf.println("\tifeq (${FIXED32},1)");
            mf.println("\t\tTYPE_FLAGS += -D_FIXED32_");
            mf.println("\telse ifeq (${FIXED8},1)");
            mf.println("\t\tTYPE_FLAGS += -D_FIXED8_");
            mf.println("\tendif");
            mf.println("endif");

            mf.println("");
            mf.println("#$(info $$TYPE_FLAGS is [${TYPE_FLAGS}])");
            mf.println("");
            mf.println("PRG = run");
            mf.println("");
            mf.println("");
            mf.println("all: ${PRG} load");
            mf.println("");
            mf.println("");
            mf.println("run:  ${OBJS}");
            mf.print("\t${CXX} ${CXXFLAGS} -o $@ ${OBJS} ${CXXLIB}");

            mf.println("");
            mf.println("");

            for(String classname: classesList){
                mf.println(classname+".o: " + classname + ".cpp");
                mf.println("\t${CXX} ${CXXFLAGS} -c -g $?");
                mf.println("");
            }

            for(String classname: NaClassesList){
                if(classname=="types2"||classname=="paramIO"||classname=="ConvLayer") {
                    mf.println(classname + ".o: " + classname + ".cpp");
                    mf.println("\t${CXX} ${CXXFLAGS} ${TYPE_FLAGS} -c -g $?");
                    mf.println("");
                }

                if(classname=="dnnFunc") {
                    mf.println(classname + ".o: " + classname + ".cpp");
                    mf.println("\t${CXX} ${CXXFLAGS} -c -g $?");
                    mf.println("");
                }

                if(classname=="heap_4"||classname=="soc_drivers"||classname=="neumem") {
                    mf.println(classname + ".o: " + classname + ".c");
                    mf.println("\t${CC} ${CCFLAGS} ${TYPE_FLAGS} -c -g $?");
                    mf.println("");
                }

            }

            mf.println("");
            mf.println("");
            mf.println("clean:");
            mf.println("\trm -rf *~ *.o ");
            mf.println("");
            mf.println("");
            mf.println("BUILD_DIR:= ../../*");
            mf.println("#WDIR:= ../../weights_npz");
            mf.println("NEURAGHE_INSTALL_DIR=espam");
            mf.println("NEURAGHE_BOARD_ADDR=agarufi@10.131.3.219");
            mf.println("EXAMPLE = run");
            mf.println("");
            mf.println("load:");
            mf.println("\t$(CP) -r ${BUILD_DIR}  ");
            mf.println("\tssh $(NEURAGHE_BOARD_ADDR) 'mkdir -p $(NEURAGHE_INSTALL_DIR)'");
            mf.println("\trsync -rctacvzP --delete-after ${BUILD_DIR}   $(NEURAGHE_BOARD_ADDR):~/$(NEURAGHE_INSTALL_DIR)");
            mf.println("\txmessage -center - \"load complete.\"");
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create the default makefile");
            System.out.println("please supply your own makefile");
        }
    }

    /**
      * write application makefile with internal library cuda support
     */
    private static void _writeMakeFileCuda(String dir, String appName, Vector<String> classesList) {
       try {
            PrintStream mf = FileWorker.openFile(dir,"Makefile",null);
            mf.println("CXX=g++");
            mf.println("");
            mf.println("CXXFLAGS= -std=c++11 -pthread ");
            mf.println("");
            /** TODO: a confid param?*/
            mf.println("CUDA_INCFLAG = -I/usr/local/cuda-9.0/samples/common/inc -I/usr/local/cuda-9.0/include -I/home/nvidia/CNN_project/library/cuda/include");
            mf.println("");

            if(_loadWeights)
                mf.println("CXXLIB = -L./ -lcnpy -lz -lcudnn -L/usr/local/cuda-9.0/lib64 -L/home/nvidia/CNN_project/library/cuda/lib64");
            else
                mf.println("CXXLIB = -lcudnn -L/usr/local/cuda-9.0/lib64 -L/home/nvidia/CNN_project/library/cuda/lib64");

            mf.println();
            mf.print("OBJS = kernel.o ");
            for(String classname: classesList)
                mf.print(classname+".o ");

            mf.println("");
            mf.println("PRG = run");
            mf.println("");
            mf.println("all: ${PRG}");
            mf.println("");
            mf.println("run:  ${OBJS}");
            mf.println("\tnvcc -o $@ ${OBJS} $(CUDA_INCFLAG) ${CXXLIB}");
            mf.println("");
            mf.println("kernel.o:");
            mf.println("\tnvcc $(CUDA_INCFLAG) $(CXXLIB) -c kernel.cu");
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

    /** Write configuration file*/
    private static void _writeConfig(String dir, boolean dnnFunc){
           try {
            PrintStream cf = FileWorker.openFile(dir,"configure","sh");
            if(dnnFunc)
                cf.println("while getopts \":b:s:f:\" opt; do");
            else
                cf.println("while getopts \":b:s:\" opt; do");

            cf.println("\tcase $opt in");
            cf.println("\t\tb) CNPY_BUILD=\"$OPTARG\"");
            cf.println("\t\t;;");
            cf.println("\t\ts) CNPY_SRC=\"$OPTARG\"");
            cf.println("\t\t;;");

            if(dnnFunc){
            cf.println("\t\tf) DNN_F_SRC=\"$OPTARG\"");
            cf.println("\t\t;;");
            }

            cf.println("\t\t\\?) echo \"Invalid option -$OPTARG\" >&2");
            cf.println("\t\t;;");
            cf.println("\tesac");
            cf.println("done");
            cf.println("");
            cf.println("CONF_STATUS='success'");
            cf.println("");
            cf.println("if test ! -r \"${CNPY_SRC}/cnpy.h\"; then");
            cf.println("\techo error: $CNPY_SRC should contain cnpy.h and cnpy.cpp!");
            cf.println("\tCONF_STATUS='fail'");
            cf.println("else");
            cf.println("\tcp $CNPY_SRC/cnpy.cpp $CNPY_SRC/cnpy.h ./");
            cf.println("fi");
            cf.println("");

            cf.println("if test ! -r \"${CNPY_BUILD}/libcnpy.so\"; then");
            cf.println("\techo error: $CNPY_BUILD should contain libcnpy.so!");
            cf.println("\tCONF_STATUS='fail'");
            cf.println("else");
            cf.println("\tcp $CNPY_BUILD/libcnpy.so ./");
            cf.println("\tLD_LIBRARY_PATH=$LD_LIBRARY_PATH:./");
            cf.println("\texport LD_LIBRARY_PATH");
            cf.println("fi");
            cf.println("");

            if(dnnFunc){
            cf.println("if test ! -r \"${DNN_F_SRC}/dnnFunc.h\"; then");
            cf.println("\techo error: $DNN_F_SRC should contain dnnFunc.h!");
            cf.println("\tCONF_STATUS='fail'");
            cf.println("else");
            cf.println("\tcp $DNN_F_SRC/* ./");
            cf.println("fi");
            cf.println("");
            }

            cf.println("echo configuration status: $CONF_STATUS");
            cf.close();
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create configuration file!");
        }

    }


    /**
    * Set dummy schedule for dnn running in Layer-based -mode
     * @param dnn deep neural network
     */
     protected static void _setDummySchedule(Network dnn, CSDFGraph csdfg){
         Vector<String> schedule =  new Vector<>();
         dnn.sortLayersInTraverseOrder();
         for (Layer layer : dnn.getLayers()) {
             if(csdfg.getNode(layer.getName())!=null)
                schedule.add(layer.getName());
         }
         _cppVisitor.setSchedule(schedule);
     }

    /**
     * Parse mapping file
     */
    public static void setMapping(Mapping mapping){
        _cppVisitor.setMapping(mapping);
    }


    /**
     * Set flag,  if the internal library (dnnFunc) generation for CPU is needed
     * @param generateDNNFuncCPU  flag, if the internal library (dnnFunc) for CPU generation is needed
     */
    public static void setGenerateDNNFuncCPU(boolean generateDNNFuncCPU) {
        _generateDNNFuncCPU = generateDNNFuncCPU;
    }

    /**
     * Set flag,  if the internal library (dnnFunc) generation for CPU is needed
     * @param generateDNNFuncCPU  flag, if the internal library (dnnFunc) for CPU generation is needed
     */
    public static void setGenerateDNNFuncGPU(boolean generateDNNFuncCPU) {
        _generateDNNFuncGPU = generateDNNFuncCPU;
    }

    /** use neuraghe functions*/
    public static void setGenerateFuncNA(boolean generateDNNFuncNA) {
        _generateDNNFuncNA = generateDNNFuncNA;
        _hvisitor.setGenerateFuncNA(generateDNNFuncNA);
        _cppVisitor.setGenerateFuncNA(generateDNNFuncNA);
    }

    /**Set number of inputs to be processed, until the application stops.
     * Default batch = 1;
     * @param batch number of inputs to be processed, until the application stops.
     */
    public static void setBatch(Integer batch){
        if(batch!=null) {
            _hvisitor.setBatch(batch);
            _cppVisitor.setBatch(batch);
        }
    }

    /**
     * Set max number of input samples to be stored between two nodes
     * Default value = 10
     * @param fifoScale scale factor of fifo buffers
     */
    public static void setFifoScale(Integer fifoScale){
        if(fifoScale!=null)
            _cppVisitor.setBatch(fifoScale);
    }

    /**
     * Set maximum cores number
     * @param maxCores maximum cores number
     * TODO should be replaced by mapping specification
     */

    public static void setMaxCores(int maxCores) {
        _cppVisitor.setMaxCores(maxCores);
    }

    /**
     * Set flag, if the debug couts should be printed
     * @param silent silent flag
     */
    public static void setSilent(boolean silent) {
        _cppVisitor.setSilent(silent);
    }

    /**
     * @param datatype I/O MU data type
     */
   private static void _setMUDataTypes(CSDFGraph csdfGraph, String datatype){
       /** generate classes (.cpp and .h files) for each SDF graph node */
         Iterator inode = csdfGraph.getNodeList().iterator();
         CSDFNode node;
         while (inode.hasNext()) {
             node = (CSDFNode) inode.next();
             for (MemoryUnit mu : node.getMemoryUnits()) {
                 if (!mu.isUnitParam()) {
                     mu.setTypeDesc(datatype);
                 }
             }
         }

   }

    /**
     * Set data types of I/O memory and parameters
     * @param iotype I/O data type
     * @param paramtype parameters type
     */
   private static void _setDataTypes(String iotype, String paramtype ){
       _cppVisitor._IODatatype = iotype;
       _hvisitor._IODatatype = iotype;
       _cppVisitor._paramDataType = paramtype;
       _hvisitor._paramDataType = paramtype;
   }

   public static void setMocMode(boolean mocMode){
        _cppVisitor.setMocMode(mocMode);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /**
     * Load weights
     */
    public static void setLoadWeights() {
        _loadWeights = true;
    }

    /** header-files visitor*/
    public static HSDFGVisitorPthread _hvisitor = new HSDFGVisitorPthread();

    /**C++ code-files visitor*/
    public static CPPSDFGVisitorPthread _cppVisitor = new CPPSDFGVisitorPthread();

    /** application main class name*/
    private static String _mainClassName = "appMain";

    /** CSDF graph node base class*/
    private static String _baseClassName = "csdfNode";

    /** CSDF graph node base class*/
    private static String _loadWeightsClassName = "dataLoader";

    /** CSDF graph node functions class*/
    private static String _funcClassName = "appFunc";

    /** DNN node functions class*/
    private static String _dnnFuncClassName = "dnnFunc";

    /** If the internal library (dnnFunc) generation for CPU is needed*/
    private static boolean _generateDNNFuncCPU = false;

    /** If the internal library (dnnFunc) generation for GPU is needed*/
    private static boolean _generateDNNFuncGPU = false;

    /** If the NA library is used*/
    private static boolean _generateDNNFuncNA = false;

    /** if weights should be loaded from external files*/
    private static boolean _loadWeights = false;



}
