package espam.visitor.tensorrt;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.simple.Data;

import espam.operations.transformations.cnn_model_transformations.DNNPartition;
import espam.operations.transformations.cnn_model_transformations.DNNPartitioner;
import espam.utils.fileworker.FileWorker;
import espam.visitor.GraphVisitor;
import espam.visitor.tensorrt.cpp.CPPSDFGVisitorARMCL;
import espam.visitor.tensorrt.cpp.CPPSDFGVisitorTensorrt;
import espam.visitor.tensorrt.h.HSDFGVisitorARMCL;
import espam.visitor.tensorrt.h.HSDFGVisitorTensorrt;

import java.io.PrintStream;

import java.util.Vector;

public class TensorrtSDFGVisitor extends GraphVisitor {
    /**
     * Call SDFG Visitor
     * @param dir directory for tensorrt executable code
     * @param dnn deep neural network
     * @param partitioning DNN partitioning
     */
     public void callVisitor(Network dnn, String dir, Vector<DNNPartition> partitioning, boolean gpuEvalNet, boolean cpuEvalNet) {
         String templatesDir;

         //create partitined DNN
         if(partitioning!=null) {
         templatesDir  = dir + "app/";
         /** if templates directory already exists,
          *  remove it and all templates inside it*/
         FileWorker.recursiveDelete(templatesDir);
            Vector<Boolean> useGPU = partition(dnn, partitioning);
             callVisitorForPartitions(dnn, useGPU,  dir);
         }

         if(gpuEvalNet) {
            templatesDir  = dir + "GPU/";
            /** if templates directory already exists,
            *  remove it and all templates inside it*/
            FileWorker.recursiveDelete(templatesDir);
             _callGPUEvalVisitor(dnn, templatesDir);
         }

         if(cpuEvalNet) {
            templatesDir  = dir + "CPU/";
            /** if templates directory already exists,
            *  remove it and all templates inside it*/
            FileWorker.recursiveDelete(templatesDir);
            _callCPUEvalVisitor(dnn, templatesDir + "/whole");
            _callCPUEvalVisitorPartitioned(dnn, templatesDir+ "/perlayers");
         }
     }


    /**
     * Call CPP SDFG GPU eval Visitor
     * @param dnn DNN
     * @param templatesDir directory for code templates
     */
     protected void _callGPUEvalVisitor(Network dnn, String templatesDir){
     try {

        Vector<String> gpuPartitionClassNames = new Vector<>();
        Vector<String> cpuPartitionClassNames = new Vector<>();
        Vector<String> classesInExecutionOrder = new Vector<>();

        classesInExecutionOrder.add(dnn.getName());
        gpuPartitionClassNames.add(dnn.getName());

         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, false, true);
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder);

         _hGPUVisitor.callDNNVisitor(dnn, templatesDir);
         _cppGPUVisitor.callDNNVisitor(dnn, templatesDir);

         System.out.println("espamAI-Tensorrt EVAL application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Tensorrt EVAL application generation error: " + e.getMessage());

        }
     }

     /**
     * Call CPP SDFG GPU eval Visitor
     * @param dnn DNN
     * @param templatesDir directory for code templates
     */
     protected void _callCPUEvalVisitor(Network dnn, String templatesDir){
     try {

        Vector<String> gpuPartitionClassNames = new Vector<>();
        Vector<String> cpuPartitionClassNames = new Vector<>();
        Vector<String> classesInExecutionOrder = new Vector<>();

        classesInExecutionOrder.add(dnn.getName());
        cpuPartitionClassNames.add(dnn.getName());

         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, true, false);
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder);

         _hCPUVisitor.callDNNVisitor(dnn, templatesDir);
         _cppCPUVisitor.callDNNVisitor(dnn, templatesDir);

         System.out.println("espamAI-ARMCL EVAL application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-ARMCL EVAL application generation error: " + e.getMessage());

        }
     }


     /**
     * Call CPP SDFG Visitor
     * @param dnn DNN
     * @param templatesDir directory for code templates
     */
     protected void _callCPUEvalVisitorPartitioned(Network dnn, String templatesDir){
         _cpuDebugMode = true;
         try {

        Vector<String> classesInExecutionOrder = new Vector<>();
        Vector<String> gpuPartitionClassNames = new Vector<>();
        Vector<String> cpuPartitionClassNames = new Vector<>();

        Network partition = null;
        Integer partitionId = 0;

        dnn.sortLayersInTraverseOrder();

        for(Layer partitionLayer: dnn.getLayers()) {

            if (partitionLayer.getNeuron() instanceof GenericNeuron) {
                partition = ((GenericNeuron) partitionLayer.getNeuron()).getInternalStructure();
            }

            /**Process single-layer partition
             * By simulate one-layer subnetwork
             */
            else if (!(partitionLayer.getNeuron() instanceof Data)) {

                Network oneLayerSubnet = new Network(partitionLayer.getName());
                oneLayerSubnet.getLayers().add(partitionLayer);
                oneLayerSubnet.setInputLayer(partitionLayer);
                oneLayerSubnet.setOutputLayer(partitionLayer);

                partition = oneLayerSubnet;

            }

            if(partition!=null) {
                /** generate classes (.cpp and .h files) for each SDF graph node */
                //for(Network partition: partitions) {
                classesInExecutionOrder.add(partition.getName());
                _hCPUVisitor.callDNNVisitor(partition, templatesDir);
                _cppCPUVisitor.callDNNVisitor(partition, templatesDir);
                cpuPartitionClassNames.add(partition.getName());


                partitionId++;
            }
            partition = null;
        }

        //TODO: process FIFOS

           /**
          * Generate classes, used by all the node-classes of
          * the application
          */

         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames,true, false);
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder);


         System.out.println("espamAI-Tensorrt CPU eval application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Tensorrt CPU eval application generation error: " + e.getMessage());

        }
        _cpuDebugMode = false;
     }



    /**
     * Set mapping for DNN
     * @param mapping
     */
     public void setMapping(Vector<Vector<String>> mapping){
         this._mapping = mapping;
     }

    /**TODO: REMOVE useGPU generation After testing! USEGPU should be determined from the mapping
     * @param dnn DNN to be partitioned
     * @param partitioning DNN partitioning
     * @return vector of GPU use
     */

     public Vector<Boolean> partition(Network dnn, Vector<DNNPartition> partitioning){

         Vector<Boolean> useGPU = new Vector<>();
         DNNPartitioner partitioner = new DNNPartitioner();

         partitioner.partitionDNN(dnn, partitioning);

         dnn.sortLayersInTraverseOrder();

         useGPU.add(true);//['node_Conv0', -> 'node_Conv16']
         useGPU.add(false);//['node_MaxPool18']
         useGPU.add(false);//'node_Conv19'
         useGPU.add(true);//['node_Conv21', -> 'node_Conv30']
         useGPU.add(false);//'node_Conv32'
         useGPU.add(true);//'node_Conv34', 'node_MaxPool36'
         useGPU.add(false);//'node_Gemm37'
         useGPU.add(false);//['node_Gemm40']
         useGPU.add(false);//['node_Gemm43', 'node_Softmax44']

         return useGPU;

     }

    /**
     * Check if tensorrt used in mapping
     * @param useGPU
     */
    private boolean _isGPUUsedInMapping(Vector<Boolean> useGPU){
        for(Boolean gpu: useGPU){
            if(gpu)
                return true;
        }
        return false;
     }

     /**
     * Check if tensorrt used in mapping
     * @param useGPU
     */
    private boolean _isCPUUsedInMapping(Vector<Boolean> useGPU){
        for(Boolean gpu: useGPU){
            if(!gpu)
                return true;
        }
        return false;
     }

     /**
     * Call CPP SDFG Visitor
     * @param partitionsNetwork Network, contains Generic Nodes with orginal DNN partiitons
     * @param templatesDir directory for code templates
     */
     public void callVisitorForPartitions(Network partitionsNetwork, Vector<Boolean> useGPU, String templatesDir){
     try {

        _trtUSED = _isGPUUsedInMapping(useGPU);
        _armclUSED = _isCPUUsedInMapping(useGPU);

        Vector<String> classesInExecutionOrder = new Vector<>();
        Vector<String> gpuPartitionClassNames = new Vector<>();
        Vector<String> cpuPartitionClassNames = new Vector<>();

        Network partition = null;
        Integer partitionId = 0;


        partitionsNetwork.sortLayersInTraverseOrder();

        for(Layer partitionLayer: partitionsNetwork.getLayers()) {

            if (partitionLayer.getNeuron() instanceof GenericNeuron) {
                partition = ((GenericNeuron) partitionLayer.getNeuron()).getInternalStructure();
            }

            /**Process single-layer partition
             * By simulate one-layer subnetwork
             */
            else if (!(partitionLayer.getNeuron() instanceof Data)) {

                Network oneLayerSubnet = new Network(partitionLayer.getName());
                oneLayerSubnet.getLayers().add(partitionLayer);
                oneLayerSubnet.setInputLayer(partitionLayer);
                oneLayerSubnet.setOutputLayer(partitionLayer);

                partition = oneLayerSubnet;

            }

            if(partition!=null) {
                /** generate classes (.cpp and .h files) for each SDF graph node */
                //for(Network partition: partitions) {
                classesInExecutionOrder.add(partition.getName());

                if (useGPU.get(partitionId)) {
                    _cppGPUVisitor.callDNNVisitor(partition, templatesDir);
                    _hGPUVisitor.callDNNVisitor(partition, templatesDir);
                    gpuPartitionClassNames.add(partition.getName());
                } else {
                    _hCPUVisitor.callDNNVisitor(partition, templatesDir);
                    _cppCPUVisitor.callDNNVisitor(partition, templatesDir);
                    cpuPartitionClassNames.add(partition.getName());
                }

                partitionId++;
            }
            partition = null;
        }

        //TODO: process FIFOS

           /**
          * Generate classes, used by all the node-classes of
          * the application
          */

         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames,_armclUSED, _trtUSED);
         generateMainClassTemplate(partitionsNetwork, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder);


         System.out.println("espamAI-Tensorrt application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Tensorrt application generation error: " + e.getMessage());

        }
     }


    /**
     * Call CPP SDFG Visitor
     * @param dnn DNN
     * @param dir directory for sesame templates
     */
     public void callVisitor(Network dnn, String dir){
     String templatesDir = dir + "app/";
     try {

        Vector<String> gpuPartitionClassNames = new Vector<>();
        Vector<String> cpuPartitionClassNames = new Vector<>();
        Vector<String> classesInExecutionOrder = new Vector<>();

        // MOC DATA
        // _trtUSED = true;
        // _armclUSED= false;
        //gpuPartitionClassNames.add(dnn.getName());

        //_trtUSED = false;
        //_armclUSED = true;

        classesInExecutionOrder.add(dnn.getName());
        if(_trtUSED)
            gpuPartitionClassNames.add(dnn.getName());


        if(_armclUSED)
            cpuPartitionClassNames.add(dnn.getName());



         /** if templates directory already exists,
          *  remove it and all templates inside it*/
         FileWorker.recursiveDelete(templatesDir);
         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, _armclUSED, _trtUSED);
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder);


         //_cppGPUVisitor.callDNNVisitor(dnn, dir);
         //_hGPUVisitor.callDNNVisitor(dnn, dir);

         if(_armclUSED) {
             _hCPUVisitor.callDNNVisitor(dnn, templatesDir);
             _cppCPUVisitor.callDNNVisitor(dnn, templatesDir);
         }

         if(_trtUSED){
             _hGPUVisitor.callDNNVisitor(dnn, templatesDir);
             _cppGPUVisitor.callDNNVisitor(dnn, templatesDir);
         }

         /** generate classes (.cpp and .h files) for each SDF graph node */


           /**
          * Generate classes, used by all the node-classes of
          * the application
          */
         //_generateAppStandardFuncs(templatesDir,y);


         System.out.println("espamAI-Tensorrt application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "espamAI-Tensorrt application generation error: " + e.getMessage());

        }
     }


     /** get List of ARM Lib source classes */
     private Vector<String> _getARMSrcClasses(){
         Vector<String>classesList = new Vector<>();
         classesList.add("Utils");
         classesList.add("GraphUtils");
         classesList.add("CommonGraphOptions");
         return classesList;
     }

//////////////////////////           MAKEFILE                 ////////////////////////

    /**
      * write application makefile with internal library cuda support
     */
    private void _writeMakeFile(String dir, Vector<String> gpuPartitionClassNames, Vector<String> cpuPartitionClassNames, boolean armCL, boolean trt) {
       try {
            PrintStream mf = FileWorker.openFile(dir,"Makefile",null);
            mf.println("CXX=aarch64-linux-gnu-g++");
            mf.println("");
            mf.println("CXXFLAGS= -std=c++11 -Wl,--allow-shlib-undefined");
            mf.println("");

            if(armCL) {
                mf.println("ARM_PATH=/home/nvidia/arm_cl/ComputeLibrary");
                mf.println("");
                mf.println("ARM_INCFLAG = -I${ARM_PATH} -I${ARM_PATH}/include");
                mf.println("");
                mf.println("ARM_SOURCES = ${ARM_PATH}/utils/Utils.cpp ${ARM_PATH}/utils/GraphUtils.cpp ${ARM_PATH}/utils/CommonGraphOptions.cpp");
                mf.println("CXXLIB = -L${ARM_PATH} -larm_compute_graph -larm_compute -larm_compute_core -lpthread");
                mf.println();
            }

            else {
                mf.println("CXXLIB = -lpthread");
                mf.println();
            }

            if (trt) {
                mf.println("CUDA_FLAGS= -Wall -std=c++11 -O2");
                mf.println();
                mf.println("CUDA_PATH=/usr/local/cuda-9.0");
                mf.println();
                mf.println("CUDA_INCFLAG = -I\"/usr/local/cuda-9.0/include\" -I\"/usr/local/include\" -I\"../include\" -I\"../common\"" +
                        " -I\"/usr/local/cuda-9.0/include\" -I\"../../include\"  -D_REENTRANT");
                mf.println();
                mf.println("CUDA_LIB = -L\"\" -L\"/usr/local/cuda-9.0/targets/x86_64-linux/lib64\" -L\"/usr/local/lib\" -L\"../lib\"" +
                        " -L\"/usr/local/cuda-9.0/lib64\" -L\"/usr/local/cuda-9.0/lib64\" -L\"../../lib\"" +
                        "   -L./ -Wl,--start-group -lnvinfer -lnvparsers -lnvinfer_plugin -lcudnn -lcublas -lcudart_static -lnvToolsExt -lcudart -lrt -ldl -lpthread -Wl,--end-group");
                mf.println();
            }


            mf.println();
            mf.print("OBJS = ");

            //CPU part
            if(armCL) {
                for(String classname: cpuPartitionClassNames)
                    mf.print(classname+".o ");

                mf.print("cpu_engine.o ");

                Vector<String> armSrcClasses = _getARMSrcClasses();
                for (String classname : armSrcClasses)
                    mf.print(classname + ".o ");
            }

            //GPU part
            if(trt) {
                mf.print("gpu_engine.o ");
                for(String classname: gpuPartitionClassNames)
                    mf.print(classname+".o ");
                mf.print("gpu_partition.o ");
            }

            mf.print("fifo.o appMain.o");
            mf.println("");
            mf.println("");
            mf.println("PRG = appMain");
            mf.println("");
            mf.println("all: ${PRG}");
            mf.println("");

            // build main classes compiler string
            mf.println("appMain: ${OBJS}");
                     mf.print("\t${CXX} -o appMain ${OBJS}");

	        StringBuilder appMainCompilerDependencies = new StringBuilder("");

            if(armCL) appMainCompilerDependencies.append(" $(ARM_INCFLAG)");
	        if(trt)
	            appMainCompilerDependencies.append(" $(CUDA_INCFLAG)");
	        appMainCompilerDependencies.append(" ${CXXLIB}");

	        if(trt)
	            appMainCompilerDependencies.append(" ${CUDA_LIB}");
	        appMainCompilerDependencies.append(" ${CXXFLAGS}");


            mf.println(appMainCompilerDependencies.toString());
            mf.println("");


            //per - object classes
            //CPU
            if(armCL) {
                Vector<String> armSrcClasses = _getARMSrcClasses();
                for (String classname : armSrcClasses) {
                    mf.println(classname + ".o: ");
                    mf.println("\t${CXX} ${ARM_PATH}/utils/" + classname + ".cpp $(ARM_INCFLAG) ${CXXLIB} ${CXXFLAGS} -c -g $?");
                    mf.println();
                }

                mf.println("cpu_engine.o: ");
                mf.println("\t${CXX} cpu_engine.cpp $(ARM_INCFLAG) ${CXXLIB} ${CXXFLAGS} -c -g $?");
                mf.println();

                for(String classname: cpuPartitionClassNames) {
                    mf.println(classname + ".o: ");
                    mf.println("\t${CXX} " + classname + ".cpp $(ARM_INCFLAG) ${CXXLIB} ${CXXFLAGS} -c -g $?");
                    mf.println();
                }
            }

            //GPU
            if(trt) {

                mf.println("gpu_partition.o: ");
                mf.println("\t${CXX} gpu_partition.cpp $(CUDA_INCFLAG) ${CUDA_LIB} ${CUDA_FLAGS} -c -g $?");
                mf.println();

                mf.println("gpu_engine.o: ");
                mf.println("\t${CXX} gpu_engine.cpp $(CUDA_INCFLAG) ${CUDA_LIB} ${CUDA_FLAGS} -c -g $?");
                mf.println();

                for(String classname: gpuPartitionClassNames) {
                    mf.println(classname + ".o: ");
                    mf.println("\t${CXX} " + classname + ".cpp $(CUDA_INCFLAG) ${CUDA_LIB} ${CUDA_FLAGS} -c -g $?");
                    mf.println();
                }
            }

            mf.println("fifo.o: fifo.cpp");
            mf.println("\t${CXX} ${CXXFLAGS} -c -g $?");
            mf.println("");

            mf.println("appMain.o: appMain.cpp");
            mf.print("\t${CXX} appMain.cpp");
            mf.print(appMainCompilerDependencies.toString());
            mf.println(" -c -g $?");
            mf.println("");

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
    private static void _writeConfig(String dir){
           try {
            PrintStream cf = FileWorker.openFile(dir,"configure","sh");
            cf.println("while getopts \":l:\" opt; do");

            cf.println("\tcase $opt in");
            cf.println("\t\tl) DNN_F_SRC=\"$OPTARG\"");
            cf.println("\t\t;;");
            cf.println("\t\t\\?) echo \"Invalid option -$OPTARG\" >&2");
            cf.println("\t\t;;");
            cf.println("\tesac");
            cf.println("done");
            cf.println("");

            cf.println("CONF_STATUS='success'");
            cf.println("");

            cf.println("if test ! -r \"${DNN_F_SRC}/gpu_engine.h\"; then");
            cf.println("\techo error: $DNN_F_SRC should contain gpu_engine.h!");
            cf.println("\tCONF_STATUS='fail'");
            cf.println("else");
            cf.println("\tcp $DNN_F_SRC/* ./");
            cf.println("fi");
            cf.println("");


            cf.println("echo configuration status: $CONF_STATUS");
            cf.close();
        }
        catch( Exception e ) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cannot create configuration file!");
        }

    }


//////////////////////////            APPMAIN CLASS                 ////////////////////////
      /**
     * generate main class template contains
     * application structure definition and
     * application control logic
     */
    public void generateMainClassTemplate(Network dnn, String dir, Vector<String> gpuPartitionClassNames,
                                          Vector<String> cpuPartitionClassNames, Vector<String> enginesInExecutionOrder){
        try {
            _printStream = FileWorker.openFile(dir, "appMain", "cpp");
            _printStream.println("// File automatically generated by ESPAM");
             boolean armcl = false;
             boolean trt = false;
             if(cpuPartitionClassNames.size()>0)
                 armcl = true;
             if(gpuPartitionClassNames.size()>0)
                trt = true;

            _writeMainClassCPPBeginning(armcl, trt);
            // write all partitions here
            //_addAllClassesHeaders(sdfg);

            for(String cpuPartition: cpuPartitionClassNames)
                _printStream.println("#include \"" + cpuPartition + ".h\"");

            for(String gpuPartition: gpuPartitionClassNames)
                _printStream.println("#include \"" + gpuPartition + ".h\"");

            _printStream.println("using namespace std;");
            _printStream.println();

            _writeMainClassMain(dnn, gpuPartitionClassNames, cpuPartitionClassNames, enginesInExecutionOrder);
            _printStream.close();
        }
        catch (Exception e){
            System.err.println(".cpp file creation error for appMain " + e.getMessage());
        }
    }


    /**
     * Write application main class .cpp beginning
     */
    protected void _writeMainClassCPPBeginning(boolean armcl, boolean trt){
        _printStream.println("#include <iostream>");
        _printStream.println("#include <map>");
        _printStream.println("#include <vector>");
        _printStream.println("#include <thread>");
        _printStream.println("#include <chrono>");

        if(armcl) {
             _printStream.println("#include \"arm_compute/graph.h\"");
             _printStream.println("#include \"cpu_engine.h\"");
        }

        if(trt) {
            _printStream.println("#include \"cuda_runtime_api.h\"");
            _printStream.println("#include \"gpu_partition.h\"");
            _printStream.println("#include \"gpu_engine.h\"");
        }
        _printStream.println();

    }


     protected void _writeMainClassMain(Network dnn, Vector<String> gpuPartitionClassNames, Vector<String> cpuPartitionClassNames, Vector<String> partitionClassesInExecutionOrder ) {

        Integer partitions = partitionClassesInExecutionOrder.size();

        // give names to partitions and engines
        Vector<String> gpuPartitionNames = new Vector<>();
        Vector<String> gpuEngineNames = new Vector<>();

        Vector<String> cpuPartitionNames = new Vector<>();
        Vector<String> cpuEngineNames = new Vector<>();

        Vector<Boolean> useGPU = new Vector<>();
        Vector<String> enginesInExecutionOrder = new Vector<>();

        String partitionName, engineName;
        Integer partitionId = 0;
        boolean added;

        for (String partition: partitionClassesInExecutionOrder){
            added = false;
            partitionName = "p" + partitionId;
            engineName = "e" + partitionId;

            if(gpuPartitionClassNames.contains(partition)) {
                gpuPartitionNames.add(partitionName);
                gpuEngineNames.add(engineName);
                useGPU.add(true);
                added = true;
            }

            if(cpuPartitionClassNames.contains(partition)) {
                cpuPartitionNames.add(partitionName);
                cpuEngineNames.add(engineName);
                useGPU.add(false);
                added = true;
            }

            if(added) {
                enginesInExecutionOrder.add(engineName);
                partitionId++;
            }

        }

       _printStream.println(_prefix + "int main (int argc, char **argv) {");
       _prefixInc();
       if(gpuPartitionClassNames.size() > 0)
           _printStream.println(_prefix + "cudaDeviceReset();");

       _printStream.println(_prefix + "std::cout<<\"Hello, I am the main program!\"<<std::endl;");

       _createPartitions(gpuPartitionClassNames, gpuPartitionNames, cpuPartitionClassNames, cpuPartitionNames);
       _createLocalBuffers(gpuPartitionNames, cpuPartitionNames);
       _createEngines(gpuPartitionNames, gpuEngineNames,cpuPartitionNames, cpuEngineNames);
       _createPthreadParameters(partitions);
       _createGlobalBuffers(dnn.getConnections(), partitionClassesInExecutionOrder);
       _inference (enginesInExecutionOrder, useGPU);
       _cleanMemory(gpuPartitionNames, cpuPartitionNames);
       _printStream.println();
       _printStream.println(_prefix + "return 0;");
       _prefixDec();
       _printStream.println(_prefix + "}");
     }

     /////////////////////////////////////////////
     protected  void _createPartitions(Vector<String> gpuPartitionClassNames, Vector<String> gpuPartitionNames,
                                       Vector<String> cpuPartitionClassNames, Vector<String> cpuPartitionNames) {
       _printStream.println(_prefix + "////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// CREATE PARTITIONS (DNN-DEPENDENT TOPOLOGY INIT/CLEAN) //");

       _printStream.println(_prefix + "//GPU");
       int pid = 0;
       for(String gpuPartition: gpuPartitionClassNames) {
           _printStream.println(_prefix + gpuPartition + " " + gpuPartitionNames.get(pid) + ";");
           pid++;
       }
       _printStream.println();
       pid = 0;
       _printStream.println(_prefix + "//CPU");
       for(String cpuPartition: cpuPartitionClassNames) {
           _printStream.println(_prefix + cpuPartition + " " + cpuPartitionNames.get(pid) + ";");
           pid++;
       }
    }

    /////////////////////////////////////////////////////////////////
    protected void _createLocalBuffers(Vector<String> gpuPartitionNames, Vector<String> cpuPartitionNames){
        _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
        _printStream.println(_prefix + "// CREATE LOCAL BUFFERS //");

        _printStream.println(_prefix + "//GPU");
       for(String partitionName: gpuPartitionNames) {
           _printStream.println(_prefix + "float " + partitionName + "_input[" + partitionName + ".batchSize *  "
                   + partitionName + ".INPUT_H * " + partitionName + ".INPUT_W * " + partitionName + ".INPUT_C] = {0};");

           _printStream.println(_prefix + "float " + partitionName + "_output[" + partitionName + ".batchSize * " + partitionName + ".OUTPUT_SIZE] = {0};");
       }
       _printStream.println();
       _printStream.println(_prefix + "//CPU");

       for(String partitionName: cpuPartitionNames) {
           _printStream.println(_prefix + "float " + partitionName + "_input[" + partitionName + ".batchSize *  "
                   + partitionName + ".INPUT_H * " + partitionName + ".INPUT_W * " + partitionName + ".INPUT_C] = {0};");

           _printStream.println(_prefix + "float " + partitionName + "_output[" + partitionName + ".batchSize * " + partitionName + ".OUTPUT_SIZE] = {0};");
       }
       _printStream.println();

    }

    ////////////////////////////////////////////////////////////
    protected void _createEngines(Vector<String> gpuPartitionNames, Vector<String> gpuEngineNames,
                                  Vector<String> cpuPartitionNames, Vector<String> cpuEngineNames){
       _printStream.println(_prefix + "// CREATE ENGINES (OBJECTS TO RUN DNN PARTITIONS) //");
       _printStream.println(_prefix + "//GPU");
       int pid = 0;
       for(String partitionName: gpuPartitionNames) {
           _printStream.println(_prefix + "cudaStream_t " + partitionName + "_stream; ");
           _printStream.println(_prefix + "CHECK(cudaStreamCreate(&" + partitionName + "_stream));");
           _printStream.println(_prefix + "gpu_engine " + gpuEngineNames.get(pid) + " (&" + partitionName + ", " +
                   partitionName + "_input, " + partitionName + "_output, &" + partitionName + "_stream, \"" + gpuEngineNames.get(pid) + "\");");
           pid++;
       }
       _printStream.println();

       _printStream.println(_prefix + "//CPU");
       pid = 0;
       for(String partitionName: cpuPartitionNames) {
           //cpu_engine e1 (argc, argv, input2, output2, &p1, "engine2");
           _printStream.println(_prefix + "cpu_engine " + cpuEngineNames.get(pid) + " (argc, argv, " +
                   partitionName + "_input, " + partitionName + "_output, &" + partitionName + ", \"" + cpuEngineNames.get(pid) + "\");");
           pid++;
       }
       _printStream.println();


       if(_cpuDebugMode) {
           _printStream.println(_prefix + "std::vector<cpu_engine*> cpu_engine_ptrs;");
           _printStream.println();
            pid = 0;

          for(String partitionName: cpuPartitionNames) {
              _printStream.println(_prefix + "cpu_engine_ptrs.push_back(&e" + pid + ");");
               pid++;
          }
          _printStream.println();

          }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    protected void _createGlobalBuffers(Vector<Connection> connections, Vector<String> classesInExecOrder){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// GLOBAL BUFFERS //");
       _printStream.println();
       _printStream.println(_prefix+ "int FIFO_SCALE = 10;");
       _printStream.println();
       _printStream.println(_prefix + "//fifo channels definition and initialization. Fifo sizes are given in tokens.");
       _printStream.println(_prefix + "std::vector<fifo_buf> fifos = std::vector<fifo_buf>();");
       _printStream.println();

       String fifoName;
       Integer srcEngineId, dstEngineId;
       for(Connection con: connections) {
           srcEngineId = classesInExecOrder.indexOf(con.getSrcName());
           dstEngineId = classesInExecOrder.indexOf(con.getDestName());

           if (srcEngineId != -1 && dstEngineId != -1) {
               fifoName = con.getSrcId() + "_" + con.getDestId();
               _printStream.println(_prefix + "// FIFO " + con.getSrcName() + "-->" + con.getDestName());
               _printStream.println(_prefix + "void *fifo_" + fifoName + " = NULL;");
               _printStream.println(_prefix + "int fifo_" + fifoName + "_min_size = " + con.getSrc().getOutputFormat().getElementsNumber() + ";");
               _printStream.println(_prefix + "int fifo_" + fifoName + "_size = FIFO_SCALE * fifo_" + fifoName + "_min_size;");
               _printStream.println(_prefix + "fifo_" + fifoName + " = calloc(fifo_" + fifoName + "_size +2, sizeof(float));");
               _printStream.println(_prefix + "struct fifo_buf buf_" + fifoName + " = fifo_buf " +
                       "(fifo_" + fifoName + ", fifo_" + fifoName + "_size, \"e" + srcEngineId + "\" , \"e" + dstEngineId + "\", " +
                       "fifo_" + fifoName + "_min_size, fifo_" + fifoName + "_min_size);");
               _printStream.println(_prefix + "fifos.push_back(buf_" + fifoName + ");");
               if (!_cpuDebugMode) {
                   _printStream.println(_prefix + "//add references to src and dst engines thread info");
                   //add ref to src
                   _printStream.println(_prefix + "thread_info[" + srcEngineId + "].add_fifo_buf_ref(&buf_" + fifoName + ");");
                   //add ref to dst
                   _printStream.println(_prefix + "thread_info[" + dstEngineId + "].add_fifo_buf_ref(&buf_" + fifoName + ");");
               }

               _printStream.println();
           }
       }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    protected void _createPthreadParameters(Integer partitions){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "//  PTHREAD thread_infoparams //");
       _printStream.println();
       _printStream.println(_prefix+ "int subnets = " + partitions + ";");

       /** TODO: replace by mapping*/
       _printStream.print(_prefix+ "int core_ids[subnets] = {");
       for(int i=0; i<partitions - 1; i++)
           _printStream.print("1, ");
       _printStream.print("1");
       _printStream.println("};");


       _printStream.println(_prefix+ "//Allocate memory for pthread_create() arguments");
       _printStream.println(_prefix + "const int num_threads = subnets;");
       _printStream.println(_prefix+ "struct thread_info *thread_info = (struct thread_info*)(calloc(num_threads, sizeof(struct thread_info)));");
       _printStream.println();

       _printStream.println(_prefix + "//  allocate CPU cores");
       _printStream.println(_prefix + "for(int i = 0;i<num_threads; i++)");
       _prefixInc();
       _printStream.println(_prefix + "thread_info[i].core_id = core_ids[i];");
       _prefixDec();

       _printStream.println(_prefix + "//add refs to buffers");
       _printStream.println(_prefix + "//subnet1");

       _printStream.println();

    }

    ///////////////////////////////////////////////////////////////
    protected void _inference (Vector<String> enginesInExecutionOrder, Vector<Boolean> useGPU){
       if (_cpuDebugMode){
           _cpuDebuginference();
           return;
       }

       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// INFERENCE //");
       _printStream.println();
       _printStream.println(_prefix + "auto startTime = std::chrono::high_resolution_clock::now();");
       _printStream.println();
       _printStream.println(_prefix + "//Create and run posix threads");

        Integer threadId = 0;
        for(String engine: enginesInExecutionOrder) {

            if(useGPU.get(threadId)) {//GPU
                _printStream.println(_prefix + "std::thread my_thread" + threadId +
                        "(&gpu_engine::main, &" + enginesInExecutionOrder.get(threadId) + ", &thread_info[" + threadId + "]);//(GPU)");
            }

            else {//CPU
                 _printStream.println(_prefix + "std::thread my_thread" + threadId +
                        "(&cpu_engine::main, &" + enginesInExecutionOrder.get(threadId) + ", &thread_info[" + threadId + "]);//(CPU)");
            }
            threadId++;
        }
        _printStream.println();

        _printStream.println(_prefix + "//join posix threads");

        for(int i=0; i< enginesInExecutionOrder.size(); i++)
            _printStream.println(_prefix + "my_thread" + i +".join();");
       _printStream.println();

       _printStream.println(_prefix + "auto endTime = std::chrono::high_resolution_clock::now();");
       _printStream.println(_prefix + "float totalTime = std::chrono::duration<float, std::milli>(endTime - startTime).count();");
       _printStream.println(_prefix + "std::cout<<\"Average over \"<<frames<< \" images = ~ \"<<(totalTime/float(frames))<<\" ms/img\"<<std::endl;");
       _printStream.println();
    }

    /**
     *
     *




  auto startTime = std::chrono::high_resolution_clock::now();
  //std::cout<<"engine"<<en<<std::endl;
  std::thread my_thread(&cpu_engine::main, cpu_engine_ptrs.at(en), &thread_info[en]);//(CPU)
  my_thread.join();
     *
     */

    /** CPU inference in debug/per-layer profile mode*/
    protected void _cpuDebuginference (){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// INFERENCE //");
       _printStream.println();
       _printStream.println(_prefix + "std::cout<<\"CPU eval over \"<<frames<< \" images\"<<std::endl;");
       _printStream.println(_prefix + "std::vector<float> large_cpu_time;");
       _printStream.print(_prefix + "for(int en=0; en<cpu_engine_ptrs.size();en++) {");
       _prefixInc();
       _printStream.println();
       _printStream.println(_prefix + "auto startTime = std::chrono::high_resolution_clock::now();");
       _printStream.println();

       _printStream.println(_prefix + "std::thread my_thread(&cpu_engine::main, cpu_engine_ptrs.at(en), &thread_info[en]);//(CPU)");
       _printStream.println(_prefix + "my_thread.join();");
       _printStream.println(_prefix + "auto endTime = std::chrono::high_resolution_clock::now();");
       _printStream.println(_prefix + "float totalTime = std::chrono::duration<float, std::milli>(endTime - startTime).count();");
       _printStream.println(_prefix + "large_cpu_time.push_back((totalTime/float(frames)));");
       _prefixDec();
       _printStream.println(_prefix + "}");

       _printStream.println();
       _printStream.println(_prefix + "std::cout<<\"\\\"large_CPU\\\" : [\";");
       _printStream.println(_prefix + "for(int i=0; i< large_cpu_time.size() - 1; i++)");
       _prefixInc();
       _printStream.println(_prefix + "std::cout<<large_cpu_time.at(i)<<\", \";");
       _prefixDec();
       _printStream.println(_prefix + "std::cout<<(large_cpu_time.at(large_cpu_time.size() - 1))<<\"], \"<<std::endl;");
       _printStream.println();
       _printStream.println(_prefix + "std::cout<<\"\\\"small_CPU\\\" : [\";");
       _printStream.println(_prefix + "for(int i=0; i< large_cpu_time.size() - 1; i++)");
       _prefixInc();
       _printStream.println(_prefix + "std::cout<<large_cpu_time.at(i) * 1.5f <<\", \";");
       _prefixDec();
       _printStream.println(_prefix + "std::cout<<((large_cpu_time.at(large_cpu_time.size() - 1))* 1.5f)<<\"], \"<<std::endl;");
       _printStream.println();

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    protected void _cleanMemory(Vector<String> gpuPartitionNames, Vector<String> cpuPartitionNames){

       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// CLEAN MEMORY //");
       _printStream.println();
       _printStream.println(_prefix + "//Destroy GPU streams");
       for(String gpuPartition: gpuPartitionNames)
         _printStream.println(_prefix + "cudaStreamDestroy("+ gpuPartition + "_stream);");
       _printStream.println();


       _printStream.println(_prefix + "//Destroy CPU partitions");
        for(String cpuPartition: cpuPartitionNames)
             _printStream.println(_prefix + cpuPartition + ".do_teardown();");

       _printStream.println();
       _printStream.println(_prefix + "//delete pthread parameters");
       _printStream.println(_prefix + "free(thread_info);");
       _printStream.println();
       _printStream.println(_prefix + "//free buffers");
       _printStream.println(_prefix + "for (int i=0; i<fifos.size(); i++) {");
       _prefixInc();
       _printStream.println(_prefix + "free(fifos[i].fifo);");
       _prefixDec();
       _printStream.println(_prefix + "}");
    }



     ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /** header-files visitors*/
    public static HSDFGVisitorTensorrt _hGPUVisitor = new HSDFGVisitorTensorrt();
    public static HSDFGVisitorARMCL _hCPUVisitor = new HSDFGVisitorARMCL();

    /**C++ code-files visitor*/
    public static CPPSDFGVisitorTensorrt _cppGPUVisitor = new CPPSDFGVisitorTensorrt();
    public static CPPSDFGVisitorARMCL _cppCPUVisitor = new CPPSDFGVisitorARMCL();

    private boolean _armclUSED = false;
    private boolean _trtUSED = true;

    private Vector<Vector<String>> _mapping = null;
    private boolean _cpuDebugMode = false;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                  ///
    /**
     *  Increment the indentation.
     */
    protected void _prefixInc() {
        _prefix += _offset;
    }

    /**
     *  Decrement the indentation.
     */
    protected void _prefixDec() {
        if (_prefix.length() >= _offset.length()) {
            _prefix = _prefix.substring(_offset.length());
        }
    }

}
