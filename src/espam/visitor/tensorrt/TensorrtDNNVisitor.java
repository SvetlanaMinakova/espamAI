package espam.visitor.tensorrt;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.simple.Data;

import espam.main.cnnUI.DNNInitRepresentation;
import espam.operations.transformations.cnn_model_transformations.DNNBlockGrouper;
import espam.operations.transformations.cnn_model_transformations.DNNPartition;
import espam.operations.transformations.cnn_model_transformations.DNNPartitioner;
import espam.utils.fileworker.FileWorker;
import espam.visitor.GraphVisitor;
import espam.visitor.tensorrt.cpp.CPPDNNVisitorARMCL;
import espam.visitor.tensorrt.cpp.CPPDNNVisitorTensorrt;
import espam.visitor.tensorrt.h.HDNNVisitorARMCL;
import espam.visitor.tensorrt.h.HDNNVisitorTensorrt;

import java.io.PrintStream;

import java.util.Vector;

public class TensorrtDNNVisitor extends GraphVisitor {
    /**
     * Call SDFG Visitor
     * @param dir directory for tensorrt executable code
     * @param dnn deep neural network
     * @param partitioning DNN partitioning
     */
     public void callVisitor(Network dnn, String dir, Vector<DNNPartition> partitioning,
                             Vector<TRTCodegenFlag> flags, DNNInitRepresentation dnnRepresentation) {
         _flags = flags;
         if (!dnn.checkConsistency()) {
             System.err.println("Tensorrt/ARM-CL code generation error " + dnn.getName() + " is inconsistent.");
             return;
         }

         switch (dnnRepresentation){
             case BLOCKBASED: {
                 _createDNNBlockBased(dnn, dir);
                 break;
             }
             default: {
                 _createDNNLayerBased(dnn, dir, partitioning);
                 break;
             }
         }
     }

    /**
     * Create DNN code for layer-based DNN representation
     * @param dnn dnn
     * @param dir directory to create code in
     * @param partitioning partitioning for partitioned CPU-GPU code
     */
     private void _createDNNLayerBased(Network dnn, String dir, Vector<DNNPartition> partitioning){
         if(_isGPUEval() || _isGPUWhole())
             _createGPUDNNWhole(dnn, dir);

         if(_isGPUPerLayer())
             _createGPUDNNPerLayer(dnn, dir);

         if(_isCPUEval() || _isCPUPerLayer())
             _createCPUDNNPerLayer(dnn, dir);

         if(_isCPUWhole())
             _createCPUDNNWhole(dnn, dir);

         if (partitioning!=null)
             _createParitionedDNN(dnn, dir, partitioning);
     }


    /**
     * Create DNN code for layer-based DNN representation
     * @param dnn dnn
     * @param dir directory to create code in
     */
    private void _createDNNBlockBased(Network dnn, String dir){
        if(_isGPUEval() || _isCPUEval()) {
            DNNBlockGrouper dbg = new DNNBlockGrouper();
            Vector<DNNPartition> evalPartitioning = dbg.findDNNConcatBlockGroups(dnn);
            partition(dnn, evalPartitioning);

            if(_isGPUEval()){
                Vector<Boolean> useGPU = _getMOCGPUUsageGPU(evalPartitioning.size());
                String templatesDir  = dir + "GPUBB/";
                Vector<Integer> cpuIdsForPartitions = _getMoCCPUIds(evalPartitioning.size());
                callVisitorForPartitions(dnn, useGPU,  templatesDir, cpuIdsForPartitions);
            }

            if(_isCPUEval()){
                Vector<Boolean> useGPU = _getMOCGPUUsageCPU(evalPartitioning.size());
                String templatesDir  = dir + "CPUBB/";
                Vector<Integer> cpuIdsForPartitions = _getMoCCPUIds(evalPartitioning.size());
                callVisitorForPartitions(dnn, useGPU,  templatesDir, cpuIdsForPartitions);
            }
        }
    }

    /**
     * Create DNN code for time/energy profiling on GPU
     * @param dnn DNN
     * @param dir directory for CPU code
     */
    private void _createGPUDNNWhole(Network dnn, String dir){
        String templatesDir  = dir + "GPU/whole";
        /** if templates directory already exists,
         *  remove it and all templates inside it*/
        FileWorker.recursiveDelete(templatesDir + "/");
        _callGPUEvalVisitor(dnn, templatesDir);
    }

    /**
     * Create DNN code for time/energy profiling on GPU
     * @param dnn DNN
     * @param dir directory for CPU code
     */
    private void _createGPUDNNPerLayer(Network dnn, String dir){
        String templatesDir  = dir + "GPU/perlayers";
        /** if templates directory already exists,
         *  remove it and all templates inside it*/
        FileWorker.recursiveDelete(templatesDir + "/");
        _callPerLayerEvalVisitorPartitioned(dnn, templatesDir, true);
    }

    /**
     * Create DNN code for time/energy profiling on CPU
     * @param dnn DNN
     * @param dir directory for CPU code
     */
     private void _createCPUDNNWhole(Network dnn, String dir){
         String templatesDir  = dir + "CPU/whole";
         /** if templates directory already exists,
          *  remove it and all templates inside it*/
         FileWorker.recursiveDelete(templatesDir + "/");
         _callCPUEvalVisitor(dnn, templatesDir);
     }

    /**
     * Create DNN code for time/energy profiling on CPU
     * @param dnn DNN
     * @param dir directory for CPU code
     */
    private void _createCPUDNNPerLayer(Network dnn, String dir){
        String templatesDir  = dir + "CPU/perlayers";
        /** if templates directory already exists,
         *  remove it and all templates inside it*/
        FileWorker.recursiveDelete(templatesDir + "/");
        _callPerLayerEvalVisitorPartitioned(dnn, templatesDir, false);
    }

    /**
     * Create partitioned DNN
     * @param dnn dnn
     * @param dir directory for ARM-CL/TRT code
     * @param partitioning DNN partitioning + mapping
     */
     private void _createParitionedDNN(Network dnn, String dir, Vector<DNNPartition> partitioning){
         //create partitined DNN
         if(partitioning!=null) {
             String templatesDir  = dir + "combined/";
             /** if templates directory already exists,
              *  remove it and all templates inside it*/
             FileWorker.recursiveDelete(templatesDir);
             Vector<Boolean> useGPU = getGPUUsage(partitioning);
             partition(dnn, partitioning);
             Vector<Integer> cpuIdsForPartitions = _getCPUIds(partitioning);
             callVisitorForPartitions(dnn, useGPU,  templatesDir, cpuIdsForPartitions);
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

         Vector<Integer> mocCPUIds = _getMoCCPUIds(classesInExecutionOrder.size());
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder, mocCPUIds);

         _hGPUVisitor.callDNNVisitor(dnn, templatesDir);
         _cppGPUVisitor.callDNNVisitor(dnn, templatesDir, true);

         System.out.println("Tensorrt (GPU) application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Tensorrt (GPU) application generation error: " + e.getMessage());

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
         Vector<Integer> mocCPUIds = _getMoCCPUIds(classesInExecutionOrder.size());
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder, mocCPUIds);

         _hCPUVisitor.callDNNVisitor(dnn, templatesDir);
         _cppCPUVisitor.callDNNVisitor(dnn, templatesDir);

         System.out.println("Tensorrt/ARM-CL application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Tensorrt/ARM-CL application generation error: " + e.getMessage());

        }
     }


     /**
     * Call CPP SDFG Visitor
     * @param dnn DNN
     * @param templatesDir directory for code templates
     */
     protected void _callPerLayerEvalVisitorPartitioned(Network dnn, String templatesDir, boolean GPU){
         if (GPU) _gpuDebugMode = true;
         else _cpuDebugMode = true;
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
                if(GPU){
                    _hGPUVisitor.callDNNVisitor(partition, templatesDir);
                    _cppGPUVisitor.callDNNVisitor(partition, templatesDir);
                    gpuPartitionClassNames.add(partition.getName());
                }
                else {
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
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames,!(GPU), GPU);
         Vector<Integer> mocCPUIds = _getMoCCPUIds(classesInExecutionOrder.size());
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder, mocCPUIds);


         System.out.println("Tensorrt/ARM-CL application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Tensorrt/ARM-CL application generation error: " + e.getMessage());

        }
        _cpuDebugMode = false;
        _gpuDebugMode = false;
     }


     /**create moc mapping for GPU-eval and CPU eval code.
      * According to moc-mapping every GPU partition is executed on cpu core 1
     * @param partitionsNum number of partitions
     * @return  moc mapping for GPU-eval and CPU eval code
     */
     private Vector<Integer> _getMoCCPUIds(Integer partitionsNum){
         Vector<Integer> mocCPUIds = new Vector<>();
         Integer procId = 1;
         for (int i=0;i< partitionsNum;i++) {
             mocCPUIds.add(procId);
         }
         return mocCPUIds;

     }

     /**
     * extract mapping (cpu core id per partitioning) from the partitioning file
     * @param partitioning CNN partitioning and mapping
     * @return mapping, extracted from the partitioning file
     */
     private Vector<Integer> _getCPUIds(Vector<DNNPartition> partitioning){
         Vector<Integer> mocCPUIds = new Vector<>();
         Integer procId;
         for (DNNPartition p : partitioning) {
             procId = p.get_procId();
             mocCPUIds.add(procId);
         }
         return mocCPUIds;
     }

    /**
     * Set mapping for DNN
     * @param mapping
     */
     public void setMapping(Vector<Vector<String>> mapping){
         this._mapping = mapping;
     }

     /**
     * get info about gpu usage
     * @param partitioning DNN partitioning
     * @return vector of GPU use
     */
     public Vector<Boolean> getGPUUsage(Vector<DNNPartition> partitioning){
         Vector<Boolean> useGPU = new Vector<>();
         for (DNNPartition p : partitioning) {
             try {
                 if (p.get_procName().toLowerCase().contains("gpu")) {
                     useGPU.add(true);
                 } else {
                     useGPU.add(false);
                 }
                 System.out.println("partitioning " + p.get_procName() + " is processed");
             }
             catch (Exception e){ System.out.println("partitioning " + p.get_procName() + " gave an error.");}
         }

         return useGPU;
     }

    /**
     * get MOC info about gpu usage for GPU only net
     * @param partitions number of partitions
     * @return vector of GPU use
     */
    private Vector<Boolean> _getMOCGPUUsageGPU(Integer partitions){
        Vector<Boolean> useGPU = new Vector<>();
        for (int i=0; i< partitions; i++)
                useGPU.add(true);
        return useGPU;
    }

    /**
     * get MOC info about gpu usage for CPU only net
     * @param partitions number of partitions
     * @return vector of GPU use
     */
    private Vector<Boolean> _getMOCGPUUsageCPU(Integer partitions){
        Vector<Boolean> useGPU = new Vector<>();
        for (int i=0; i< partitions; i++)
            useGPU.add(false);
        return useGPU;
    }

    /**Partition dnn
     * @param dnn DNN to be partitioned
     * @param partitioning DNN partitioning
     * @return vector of GPU use
     */
     public void partition(Network dnn, Vector<DNNPartition> partitioning){
         DNNPartitioner partitioner = new DNNPartitioner();
         partitioner.partitionDNN(dnn, partitioning);
         dnn.sortLayersInTraverseOrder();
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
     public void callVisitorForPartitions(Network partitionsNetwork, Vector<Boolean> useGPU, String templatesDir, Vector<Integer> cpuIdsForPartitions){
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
                //System.out.println("Partiton " +partition.getName());
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
            //System.out.println("Partiton " +partition.getName() + " successfully generated");
            partition = null;

        }

        System.out.println("ALL partitions are generated");

        /** Generate files, used by all the node-classes of the application*/
         _writeConfig(templatesDir);
         _writeMakeFile(templatesDir, gpuPartitionClassNames, cpuPartitionClassNames,_armclUSED, _trtUSED);
         generateMainClassTemplate(partitionsNetwork, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder, cpuIdsForPartitions);


         System.out.println("Tensorrt /ARM-CL application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Tensorrt/ARM-CL application generation error: " + e.getMessage());

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
         Vector<Integer> mocCPUIds = _getMoCCPUIds(classesInExecutionOrder.size());
         generateMainClassTemplate(dnn, templatesDir, gpuPartitionClassNames, cpuPartitionClassNames, classesInExecutionOrder, mocCPUIds);


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
         System.err.println(templatesDir + "TensorRT/ARM-CL application generation error: " + e.getMessage());

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
                mf.println("ARM_PATH="+_pathToARMCL);
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
                mf.println("CUDA_PATH=" + _pathToCUDA + "");
                mf.println();
                mf.println("CUDA_INCFLAG = -I\"" + _pathToCUDA + "/include\" -I\"/usr/local/include\" -I\"../include\" -I\"../common\"" +
                        " -I\"" + _pathToCUDA + "/include\" -I\"../../include\"  -D_REENTRANT");
                mf.println();
                mf.println("CUDA_LIB = -L\"\" -L\"" + _pathToCUDA + "/targets/x86_64-linux/lib64\" -L\"/usr/local/lib\" -L\"../lib\"" +
                        " -L\"" + _pathToCUDA + "/lib64\" -L\"" + _pathToCUDA + "/lib64\" -L\"../../lib\"" +
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
                                          Vector<String> cpuPartitionClassNames, Vector<String> enginesInExecutionOrder, Vector<Integer> partitionCPuCores){
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

            _writeMainClassMain(dnn, gpuPartitionClassNames, cpuPartitionClassNames, enginesInExecutionOrder, partitionCPuCores);
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


     protected void _writeMainClassMain(Network dnn, Vector<String> gpuPartitionClassNames, Vector<String> cpuPartitionClassNames, Vector<String> partitionClassesInExecutionOrder, Vector<Integer> cpuPartitionIds) {

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

       _printStream.println(_prefix + "std::cout<<\"***DNN building phase.***\"<<std::endl;");

       _createPartitions(gpuPartitionClassNames, gpuPartitionNames, cpuPartitionClassNames, cpuPartitionNames);
       _createLocalBuffers(gpuPartitionNames, cpuPartitionNames);
       _createEngines(gpuPartitionNames, gpuEngineNames,cpuPartitionNames, cpuEngineNames);
       _createPthreadParameters(cpuPartitionIds);
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
         _printStream.println(_prefix + "std::cout<<\" - partitions creation.\"<<std::endl;");

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
        _printStream.println(_prefix + "std::cout<<\" - local buffers allocation.\"<<std::endl;");

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
        _printStream.println(_prefix + "std::cout<<\" - Engines creation.\"<<std::endl;");

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

        if(_gpuDebugMode) {
            _printStream.println(_prefix + "std::vector<gpu_engine*> gpu_engine_ptrs;");
            _printStream.println();
            pid = 0;
            for(String partitionName: gpuPartitionNames) {
                _printStream.println(_prefix + "gpu_engine_ptrs.push_back(&e" + pid + ");");
                pid++;
            }
            _printStream.println();

        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    protected void _createGlobalBuffers(Vector<Connection> connections, Vector<String> classesInExecOrder){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// GLOBAL BUFFERS //");
        _printStream.println(_prefix + "std::cout<<\" - global buffers allocation.\"<<std::endl;");
       _printStream.println();
       _printStream.println(_prefix+ "int FIFO_SCALE = 2;");
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
               if (!(_cpuDebugMode || _gpuDebugMode)) {
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
    protected void _createPthreadParameters(Vector<Integer> partitionsCPUCores){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "//  PTHREAD thread_infoparams //");
        _printStream.println(_prefix + "std::cout<<\" - Pthread info-params creation.\"<<std::endl;");
       _printStream.println();
       _printStream.println(_prefix+ "int subnets = " + partitionsCPUCores.size() + ";");

       _printStream.print(_prefix+ "int core_ids[subnets] = {");
       for(int i=0; i< partitionsCPUCores.size() - 1; i++)
           _printStream.print(partitionsCPUCores.elementAt(i) + ", ");
       _printStream.print(partitionsCPUCores.lastElement());
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

        if (_gpuDebugMode){
            _gpuDebuginference();
            return;
        }

       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// INFERENCE //");
        _printStream.println(_prefix + "std::cout<<\"*** DNN inference phase.***\"<<std::endl;");
        _printStream.println(_prefix + "std::cout<<\" - Threads creation and execution.\"<<std::endl;");
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


    /** CPU inference in debug/per-layer profile mode*/
    protected void _gpuDebuginference (){
        _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
        _printStream.println(_prefix + "// INFERENCE //");
        _printStream.println();

        _printStream.println();
        _printStream.println(_prefix + "std::cout<<\"GPU eval over \"<<frames<< \" images\"<<std::endl;");
        _printStream.println(_prefix + "std::vector<float> gpu_time;");
        _printStream.println();
        _printStream.println(_prefix + "//run eval");
        _printStream.print(_prefix + "for(int en=0; en<gpu_engine_ptrs.size();en++) {");
        _prefixInc();
        _printStream.println();
        _printStream.println(_prefix + "auto startTime = std::chrono::high_resolution_clock::now();");
        _printStream.println();

        _printStream.println(_prefix + "std::thread my_thread(&gpu_engine::main, gpu_engine_ptrs.at(en), &thread_info[en]);//(GPU)");
        _printStream.println(_prefix + "my_thread.join();");
        _printStream.println(_prefix + "auto endTime = std::chrono::high_resolution_clock::now();");
        _printStream.println(_prefix + "float totalTime = std::chrono::duration<float, std::milli>(endTime - startTime).count();");
        _printStream.println(_prefix + "gpu_time.push_back((totalTime/float(frames)));");
        _prefixDec();
        _printStream.println(_prefix + "}");

        _printStream.println();
        _printStream.println(_prefix + "std::cout<<\"\\\"Per-layer time\\\" : [\";");
        _printStream.println(_prefix + "for(int i=0; i< gpu_time.size() - 1; i++)");
        _prefixInc();
        _printStream.println(_prefix + "std::cout<<gpu_time.at(i)<<\", \";");
        _prefixDec();
        _printStream.println(_prefix + "std::cout<<(gpu_time.at(gpu_time.size() - 1))<<\"], \"<<std::endl;");
        _printStream.println();
    }

    /** CPU inference in debug/per-layer profile mode*/
    protected void _cpuDebuginference (){
       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// INFERENCE //");
       _printStream.println();
        _printStream.println(_prefix + "// set CPU ids here");
       _printStream.println(_prefix + "int large_cpu_id = 1;");
       _printStream.println(_prefix + "int small_cpu_id = 4;");

       _printStream.println();
       _printStream.println(_prefix + "std::cout<<\"CPU eval over \"<<frames<< \" images\"<<std::endl;");
       _printStream.println(_prefix + "std::vector<float> cpu_time;");
       _cpuDebuginference_large();
        _printStream.println(_prefix + "// clear time evals");
        _printStream.println(_prefix + "cpu_time.clear();");
        _printStream.println();
        _cpuDebuginference_small();
    }

    /** CPU inference in debug/per-layer profile mode: large CPU*/
    protected void _cpuDebuginference_large (){
        _printStream.print(_prefix + "// allocate CPU cores: large CPU");
        _printStream.print(_prefix + "for(int i=0; i<num_threads; i++)");
        _prefixInc();
        _printStream.print(_prefix + "thread_info[i].core_id = large_cpu_id;");
        _prefixDec();
        _printStream.println();
        _printStream.println(_prefix + "//run eval");
        _printStream.print(_prefix + "for(int en=0; en<cpu_engine_ptrs.size();en++) {");
        _prefixInc();
        _printStream.println();
        _printStream.println(_prefix + "auto startTime = std::chrono::high_resolution_clock::now();");
        _printStream.println();

        _printStream.println(_prefix + "std::thread my_thread(&cpu_engine::main, cpu_engine_ptrs.at(en), &thread_info[en]);//(CPU)");
        _printStream.println(_prefix + "my_thread.join();");
        _printStream.println(_prefix + "auto endTime = std::chrono::high_resolution_clock::now();");
        _printStream.println(_prefix + "float totalTime = std::chrono::duration<float, std::milli>(endTime - startTime).count();");
        _printStream.println(_prefix + "cpu_time.push_back((totalTime/float(frames)));");
        _prefixDec();
        _printStream.println(_prefix + "}");

        _printStream.println();
        _printStream.println(_prefix + "std::cout<<\"\\\"large_CPU\\\" : [\";");
        _printStream.println(_prefix + "for(int i=0; i< cpu_time.size() - 1; i++)");
        _prefixInc();
        _printStream.println(_prefix + "std::cout<<cpu_time.at(i)<<\", \";");
        _prefixDec();
        _printStream.println(_prefix + "std::cout<<(cpu_time.at(cpu_time.size() - 1))<<\"], \"<<std::endl;");
        _printStream.println();

    }

    /** CPU inference in debug/per-layer profile mode: large CPU*/
    protected void _cpuDebuginference_small (){
        _printStream.print(_prefix + "// allocate CPU cores: small CPU");
        _printStream.print(_prefix + "for(int i=0; i<num_threads; i++)");
        _prefixInc();
        _printStream.print(_prefix + "thread_info[i].core_id = small_cpu_id;");
        _prefixDec();
        _printStream.println();
        _printStream.println(_prefix + "//run eval");
        _printStream.print(_prefix + "for(int en=0; en<cpu_engine_ptrs.size();en++) {");
        _prefixInc();
        _printStream.println();
        _printStream.println(_prefix + "auto startTime = std::chrono::high_resolution_clock::now();");
        _printStream.println();

        _printStream.println(_prefix + "std::thread my_thread(&cpu_engine::main, cpu_engine_ptrs.at(en), &thread_info[en]);//(CPU)");
        _printStream.println(_prefix + "my_thread.join();");
        _printStream.println(_prefix + "auto endTime = std::chrono::high_resolution_clock::now();");
        _printStream.println(_prefix + "float totalTime = std::chrono::duration<float, std::milli>(endTime - startTime).count();");
        _printStream.println(_prefix + "cpu_time.push_back((totalTime/float(frames)));");
        _prefixDec();
        _printStream.println(_prefix + "}");

        _printStream.println();
        _printStream.println(_prefix + "std::cout<<\"\\\"small_CPU\\\" : [\";");
        _printStream.println(_prefix + "for(int i=0; i< cpu_time.size() - 1; i++)");
        _prefixInc();
        _printStream.println(_prefix + "std::cout<<cpu_time.at(i)<<\", \";");
        _prefixDec();
        _printStream.println(_prefix + "std::cout<<(cpu_time.at(cpu_time.size() - 1))<<\"], \"<<std::endl;");
        _printStream.println();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    protected void _cleanMemory(Vector<String> gpuPartitionNames, Vector<String> cpuPartitionNames){

       _printStream.println(_prefix + "/////////////////////////////////////////////////////////////");
       _printStream.println(_prefix + "// CLEAN MEMORY //");
       _printStream.println(_prefix + "std::cout<<\"*** DNN destruction phase ***\"<<std::endl;");
       _printStream.println();
       _printStream.println(_prefix + "//Destroy GPU streams");
       _printStream.println(_prefix + "std::cout<<\" - CUDA streams destruction\"<<std::endl;");
       for(String gpuPartition: gpuPartitionNames)
         _printStream.println(_prefix + "cudaStreamDestroy("+ gpuPartition + "_stream);");
       _printStream.println();


       _printStream.println(_prefix + "//Destroy CPU partitions");
       _printStream.println(_prefix + "std::cout<<\" - CPU partitions destruction\"<<std::endl;");
        for(String cpuPartition: cpuPartitionNames)
             _printStream.println(_prefix + cpuPartition + ".do_teardown();");

       _printStream.println();
       _printStream.println(_prefix + "//delete pthread parameters");
        _printStream.println(_prefix + "std::cout<<\" - Pthread parameters destruction\"<<std::endl;");
       _printStream.println(_prefix + "free(thread_info);");
       _printStream.println();
       _printStream.println(_prefix + "//free buffers");
        _printStream.println(_prefix + "std::cout<<\" - Buffers deallocation\"<<std::endl;");
       _printStream.println(_prefix + "for (int i=0; i<fifos.size(); i++) {");
       _prefixInc();
       _printStream.println(_prefix + "free(fifos[i].fifo);");
       _prefixDec();
       _printStream.println(_prefix + "}");
    }

    ///////////////////////////////////////////////////////////
    ////        generation flags checking                  ///
    private boolean _isGPUEval(){
        return _flags.contains(TRTCodegenFlag.GPUEVAL);
    }

    private boolean _isGPUWhole(){
        return _flags.contains(TRTCodegenFlag.GPUWHOLE);
    }

    private boolean _isGPUPerLayer(){
        return _flags.contains(TRTCodegenFlag.GPUPERLAYER);
    }

    private boolean _isCPUEval(){
        return _flags.contains(TRTCodegenFlag.CPUEVAL);
    }

    private boolean _isCPUWhole(){
        return _flags.contains(TRTCodegenFlag.CPUWHOLE);
    }

    private boolean _isCPUPerLayer(){
        return _flags.contains(TRTCodegenFlag.CPUPERLAYER);
    }




    ///////////////////////////////////////////////////////////////////
    ////     set path to CUDA and ARM-CL libraries on the board    ///

    public void set_pathToCUDA(String _pathToCUDA) {
        this._pathToCUDA = _pathToCUDA;
    }

    public void set_pathToARMCL(String _pathToARMCL) {
        this._pathToARMCL = _pathToARMCL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /** header-files visitors*/
    public static HDNNVisitorTensorrt _hGPUVisitor = new HDNNVisitorTensorrt();
    public static HDNNVisitorARMCL _hCPUVisitor = new HDNNVisitorARMCL();

    /**C++ code-files visitor*/
    public static CPPDNNVisitorTensorrt _cppGPUVisitor = new CPPDNNVisitorTensorrt();
    public static CPPDNNVisitorARMCL _cppCPUVisitor = new CPPDNNVisitorARMCL();

    private Vector<TRTCodegenFlag> _flags = new Vector<>();

    private boolean _armclUSED = false;
    private boolean _trtUSED = true;

    private boolean _blockBased = false;

    private Vector<Vector<String>> _mapping = null;
    private boolean _cpuDebugMode = false;
    private boolean _gpuDebugMode = false;
    private String _pathToCUDA = "/usr/local/cuda-9.0";
    private String _pathToARMCL = "/home/nvidia/arm_cl/ComputeLibrary";

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
