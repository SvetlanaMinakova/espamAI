package espam.datamodel.mapping.DNNMapping;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.mapping.Accelerator;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.processors.GPU;
import espam.datamodel.platform.processors.Processor;
import espam.operations.evaluation.CoreTypeEval;
import espam.operations.evaluation.OpTimeEval;
import espam.operations.evaluation.PlatformEval;

import java.util.*;


/**
 * ESPAMAI DNN-specific mapping generator
 * Gets a platform and simulates mapping, commonly utilized by
 * Deep Learning (DL) frameworks
 */
public class MappingGenerator {

    /** Init mapping generator for DNN*/
    public  MappingGenerator(Platform platform, Network dnn, PlatformEval plaEval, DNN_MAPPING_TYPE mapping_type) {
        _platform = platform;
        _dnn = dnn;
        _platformEval = plaEval;

    }

    /** Init mapping generator for CSDFG*/
    public MappingGenerator(Platform platform, CSDFGraph csdfg, PlatformEval _platformEval) {
       _platform = platform;
       _csdfg = csdfg;
    }

    public Mapping generateAutoMapping(){
        if(_platform == null) {
            System.err.println("Mapping generation error: null-platform!");
            return null;
        }

        if(_dnn!=null){
            Mapping map = generateAutoMappingDNN();
            return map;
        }

        if(_csdfg!=null){
            Mapping map = generateAutoMappingCSDFG();
            return map;
        }

        System.err.println("Mapping generation error: null application (DNN/CSDFG)!");
        return null;

    }

    /** Generate mapping automatically*/
    public Mapping generateAutoMappingDNN(){
        _mapping = new Mapping(_dnn.getName() + "_to_" + _platform.getName());
        try {

            Vector<Processor> cpuList = _platform.getCPUList();
            Vector<Processor> acceleratorList = _platform.getGPUList();
            Vector<Processor> fpgaList = _platform.getFPGAList();
            acceleratorList.addAll(fpgaList);

            Vector<MProcessor> mCPUList = new Vector<>();
            Vector<MProcessor> mACCList = new Vector<>();

            if(cpuList.size()<1){
                System.err.println("mapping generation error: no CPU found!");
            }

            Vector<MProcessor> processors = new Vector<>();

            //init cpu list
            for (Processor proc: cpuList){
                MProcessor cpu = new MProcessor(proc.getName());
                cpu.setResource(proc);
                cpu.setProcessList(new Vector());
                processors.add(cpu);
                mCPUList.add(cpu);
            }

            /** list of CPU processors,
             * allocated for offloading computations on available accelerators*/
            Vector<MProcessor> _cpusAllocatedForAcceleration = new Vector<>();

            //init gpu + fpga list
            for (Processor proc: acceleratorList){
                MProcessor accelerator = new Accelerator(proc.getName());
                accelerator.setResource(proc);
                accelerator.setProcessList(new Vector());
                processors.add(accelerator);
                mACCList.add(accelerator);
            }

            _mapping.setProcessorList(processors);
            _assignNodesToTemplateMapping(_dnn,mCPUList,mACCList);

        }
        catch (Exception e){System.err.println("mapping file generation error "+e.getMessage());
        }
        return _mapping;
    }

    /** Assign host to an accelerator*/
    private void _assignHostToAccelerator(){


    }




    /**TODO: FPGA?
     * Generate mapping automatically for CSDFG
     */
    public Mapping generateAutoMappingCSDFG(){
        Mapping automapping = new Mapping(_csdfg.getName() + "_to_" + _platform.getName());
        try {
            Vector<Processor> cpuList = _platform.getCPUList();
            Vector<Processor> gpuList = _platform.getGPUList();
            Vector<MProcessor> mCPUList = new Vector<>();
            Vector<MProcessor> mGPUList = new Vector<>();


            if(cpuList.size()<1){
                System.err.println("mapping generation error: no CPU found!");
            }
            Vector<MProcessor> processors = new Vector<>();

            //init cpu list
            for (Processor proc: cpuList){
                MProcessor cpu = new MProcessor(proc.getName());
                cpu.setResource(proc);
                cpu.setProcessList(new Vector());
                processors.add(cpu);
                mCPUList.add(cpu);
            }

            //init gpu list
            for (Processor proc: gpuList){
                MProcessor gpu = new MProcessor(proc.getName());
                gpu.setResource(proc);
                gpu.setProcessList(new Vector());
                processors.add(gpu);
                mGPUList.add(gpu);
            }

            automapping.setProcessorList(processors);
            _assignNodesToTemplateMapping(_csdfg,mCPUList,mGPUList);
            _mapping = automapping;
        }
        catch (Exception e){System.err.println("mapping file generation error "+e.getMessage());
        }
        return _mapping;
    }

    /**
     * TODO: check nodes acceleration possibility.
     * TODO: gpus separately?
     * Set dummy mapping for cpu-gpu/fpga platform.
     * If at least one GPU or FPGA is available on the platform,
     * all nodes, that can be accelerated, will be accelerated.
     * Layers are executed one-by-one (WCET).
     * @param dnn DNN to be mapped
     */
    protected void _assignNodesToTemplateMapping(Network dnn,Vector<MProcessor> cpuList,
                                                 Vector<MProcessor> gpuList) {
        int cpuNum = cpuList.size();
        int gpuNum = gpuList.size();


        int curCPUId = 0;
        MProcessor curCPU = cpuList.firstElement();
        MProcessor curGPU = gpuList.firstElement();

        int curGPUId = 0;

        Vector processes;

        for (Layer node : dnn.getLayers()) {
            MProcess mp = new MProcess(node.getName());


            /**try to map node on GPU/FPGA*/
            Integer acceleratorId = getNextAcceleratorId(gpuList,node);
            if(acceleratorId!=-1) { //no accelerator found
                /** assign process to GPU*/
                curGPU = gpuList.elementAt(acceleratorId);
                processes = curGPU.getProcessList();
                processes.add(mp);

            }

            else {

                /** assign process to CPU*/
                processes = curCPU.getProcessList();
                processes.add(mp);

                /**select next CPU core for mapping */
                curCPUId = getNextCPUId(cpuNum,curCPUId);
                curCPU = cpuList.elementAt(curCPUId);
            }
        }
    }

    /**Get next accelerator Id*/
    private Integer getNextAcceleratorId( Vector<MProcessor> accelerators, Layer layer){
        if(accelerators.size()==0) //no accelerators found in the platform
            return -1;

        Vector<MProcessor> canRun = new Vector<>();
        for(MProcessor accelerator :accelerators){
            if(_isMappable(layer,accelerator))
                canRun.add(accelerator);
        }

        if(canRun.size()==0)//no accelerator can handle this layer
            return -1;

        if(canRun.size()==1) //single accelerator can handle this layer
            return accelerators.indexOf(canRun.firstElement());

        //if multiple accelerators can handle layer, find least busy processor
        Integer leastBusyProcId = 0;
        Integer procId =0;
        Integer processesMapped;
        Integer leastProcessesMapped = canRun.firstElement().getProcessList().size();
        for(MProcessor proc: canRun){
            processesMapped = proc.getProcessList().size();
            if(processesMapped<leastProcessesMapped){
                leastBusyProcId = procId;
                leastProcessesMapped = processesMapped;
            }
            procId ++;
        }

        return leastBusyProcId;
    }


    /**select next CPU core for mapping */
    private Integer getNextCPUId(Integer cpuNum, Integer curCPUId){
        if(_dnn_mapping_type == DNN_MAPPING_TYPE.SEQUENTIAL)
            return 0;
        else {
            Integer nextCPUId = curCPUId + 1;

            if (nextCPUId == cpuNum)
                nextCPUId = 0;
            return nextCPUId;
        }
    }

    /** check if layer can be mapped on processor
     * @param layer DNN layer
     * @param proc processor
     * @return true, if layer can be mapped on processor and false otherwise
     */
    private boolean _isMappable(Layer layer, MProcessor proc){
        /** no cheks for missing platform evaluation*/
        if(_platformEval==null)
            return true;

        if(layer.getNeuron() instanceof Data || layer.getNeuron() instanceof Concat)
            return true;

        /**chek up*/
        CoreTypeEval coreTypeEval = _platformEval.getCoreTypeEval(proc.getName());
        if(coreTypeEval==null)
            return false;

        OpTimeEval eval = coreTypeEval.findOpEvalRecord(layer.getNeuron().getOperator().getName());
        if(eval==null) {
           // System.out.println("Eval record for "+ layer.getName() + " not found");
            return false;
        }

        return true;
    }

    /**
     * Set dummy mapping for cpu-gpu platform. Map every convolutional and matrix
     * multiplication core on GPU. Map all other kernels on CPU
     * @param csdfg CSDF graph to be mapped
     */
    protected void _assignNodesToTemplateMapping(CSDFGraph csdfg,Vector<MProcessor> cpuList, Vector<MProcessor> gpuList){

        int cpuNum = cpuList.size();
        int gpuNum = gpuList.size();


        int curCPUId = 0;
        MProcessor curCPU = cpuList.firstElement();

        int curGPUId = 0;
        Vector<CSDFNode> gpuNodes = new Vector<>();
        boolean useGPU = false;
        if(gpuNum>0) {
            gpuNodes = csdfg.getNodesList("conv");
            useGPU = true;
        }

        Vector processes;

        for (Object nodeObj: csdfg.getNodeList()) {
            CSDFNode node = (CSDFNode) nodeObj;
            MProcess mp = new MProcess(node.getName());

            /** assign process to CPU*/
            processes = curCPU.getProcessList();
            processes.add(mp);

            /**select next CPU core for mapping */
            curCPUId++;
            if (curCPUId == cpuNum)
                curCPUId = 0;
            curCPU = cpuList.elementAt(curCPUId);

            /** add GPU call property*/
            /** TODO: refactoring*/
            if (useGPU && gpuNodes.contains(node)) {
                Operator nodeOp = node.getOperator();
                if(nodeOp!=null) {
                    if(nodeOp.hasIntegerParams()) {
                        node.getOperator().getIntParams().remove("gpu");
                        node.getOperator().addIntParam("gpu",curGPUId);
                    }
                }
                /**select next GPU core for mapping */
                curGPUId++;
                if (curGPUId == gpuNum)
                    curGPUId = 0;
            }
        }
    }




/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////           REMOVE IF NOT NEEDED

    static <String,Long extends Comparable<? super Long>>
    SortedSet<Map.Entry<String, Long>> entriesSortedByValues(Map<String, Long> map) {
    SortedSet<Map.Entry<String, Long>> sortedEntries = new TreeSet<Map.Entry<String, Long>>(
        new Comparator<Map.Entry<String, Long>>() {
            @Override public int compare(Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        }
    );
    sortedEntries.addAll(map.entrySet());
    return sortedEntries;
    }

    public TreeMap<String, Long> getBlockSizes() {
        return _blockSizes;
    }

    public void setBlockSizes(TreeMap<String, Long> _blockSizes) {
        this._blockSizes = _blockSizes;
    }

    /** INSERT BLOCKSIZES ALGORITHM/ALGORITHM CALL here*/
    public void computeBlockSizes(){

        //_blockSizes = new TreeMap<>();


    }

    public void generateMapping(){
        _initNodesWCETS();
        _preSpreadConvNodes();
        _spreadNonConvNodesFirtFitBest();
    }


       public void printSplitPlanAvgNode(){
        _initNodesWCETS();

      //  if(accelerateConvs)
        //    accelerateConvs();

        HashMap<String,Long> splitPlan = new HashMap<>();
        _preSpreadConvNodes();

        Integer cpuNUm = _mCPUList.size();
        Long maxCPUWCET = _getAvgWCET(_csdfg.countNodes());
        Long splitFactor;
        Long avgCPULoag = _getAvgWCET(_mCPUList.size());

        for(Map.Entry<String,Long> wcet: _nodeWCETs.entrySet()){
            if(wcet.getValue()>maxCPUWCET){
                splitFactor = wcet.getValue() / maxCPUWCET;
                if(splitFactor==1L)
                    splitFactor = 2L;

                splitPlan.put(wcet.getKey(),splitFactor);
            }
        }

        //_spreadNonConvNodesFirtFitBestWithSplit(splitPlan,avgCPULoag);
        for(Map.Entry<String,Long> split: splitPlan.entrySet()){
            System.out.println("split " + split.getKey() + " into " + split.getValue() + " subnodes");
        }
    }

    public void printSplitPlanAvgCPU(){
        _initNodesWCETS();

      //  if(accelerateConvs)
        //    accelerateConvs();

        HashMap<String,Long> splitPlan = new HashMap<>();
        _preSpreadConvNodes();

        Integer cpuNUm = _mCPUList.size();
        Long maxCPUWCET = _getAvgWCET(cpuNUm);
        Long splitFactor;
        Long avgCPULoag = _getAvgWCET(_mCPUList.size());

        for(Map.Entry<String,Long> wcet: _nodeWCETs.entrySet()){
            if(wcet.getValue()>maxCPUWCET){
                splitFactor = wcet.getValue() / maxCPUWCET;
                if(splitFactor==1L)
                    splitFactor = 2L;

                splitPlan.put(wcet.getKey(),splitFactor);
            }
        }

        //_spreadNonConvNodesFirtFitBestWithSplit(splitPlan,avgCPULoag);
        for(Map.Entry<String,Long> split: splitPlan.entrySet()){
            System.out.println("split " + split.getKey() + " into " + split.getValue() + " subnodes");
        }
    }



   protected void _spreadNonConvNodesFirtFitBestWithSplit(HashMap<String,Long> splitPlan, Long avgCPULoad){
     /** TODO: REFACTORING!*/
     Vector<CSDFNode> mapped = _convNodes;
     Vector<CSDFNode> toMap = new Vector<>();

     for(Object nodeObj: _csdfg.getNodeList()){
         CSDFNode node = (CSDFNode)(nodeObj);
         if(!_convNodes.contains(node))
            toMap.add(node);
     }

     MProcessor curCPU;

     for (int i=0; i<_csdfg.countNodes()-_convNodes.size();i++) {
            CSDFNode node = _getLargestWCETNode(toMap);
            if(!mapped.contains(node)) {
                curCPU = _getLeastBusyCPU();
                if(nodeFitsLightestCPU(node.getName(),curCPU.getName(),avgCPULoad)) {
                    _assignNodeToCPU(node, curCPU);
                    toMap.removeElement(node);
                }
                else {
                    Long totalPartitions = getTotalPartitions(node);
                    if(totalPartitions>1L)
                        _splitNodeOverCPUs(node,avgCPULoad,totalPartitions);
                    else _assignNodeToCPU(node,curCPU);
                   // System.out.println("node " + node.getName() + " does not fit!");
                    toMap.removeElement(node);

                }
            }
        }
    }


    protected boolean nodeFitsLightestCPU(String nodeName,String cpuName, Long avgCPULoad ){
        Long nodeWCET = _nodeWCETs.get(nodeName);
        Long cpuLoad = _cpuWCETSpread.get(cpuName);
        if(nodeWCET+cpuLoad>avgCPULoad)
            return false;
        return true;
    }


    protected Long getTotalPartitions(CSDFNode node){
        Long totalPartitions = 1L;
        Operator nodeOp = node.getOperator();
        if(nodeOp!=null) {
            if(nodeOp.hasIntegerParams())
            totalPartitions = Long.parseLong( node.getOperator().getIntParams().get("partitions") + "");
        }

        return totalPartitions;
    }

    /**
     * Split node over several CPUs, until it fits
     */
    protected void _splitNodeOverCPUs(CSDFNode node, Long avgCPULoad, Long totalPartitions){
        Vector<String> checkedCPUs = new Vector<>();
        Vector<MProcessor> CPUsToAssign = new Vector<>();
        Long nodeWCET =  _nodeWCETs.get(node.getName());
        Vector<Long> cpusWCETLoad = new Vector<>();
        //Long assignedFreeWCET = 0L;
        Long curCPUFreeWCET;
        Long curCPUWCETToAssign;
        Integer visitedCPUs = 0;
        Integer assignedCPUs = 0;
        Integer maxCPUs = _mCPUList.size();
        Long wcetToAssign = nodeWCET;

        while (wcetToAssign>0 && visitedCPUs<maxCPUs) {
            MProcessor curCPU = _getLeastBusyCPU(checkedCPUs);
            curCPUFreeWCET = avgCPULoad - _cpuWCETSpread.get(curCPU.getName());

            if(curCPUFreeWCET>0) {
                curCPUWCETToAssign = Math.min(curCPUFreeWCET, wcetToAssign);

                if(!CPUsToAssign.contains(curCPU)) {
                    cpusWCETLoad.add(curCPUWCETToAssign);
                    CPUsToAssign.add(curCPU);
                    checkedCPUs.add(curCPU.getName());
                    assignedCPUs++;
                }

                else {
                    Integer cpuId = CPUsToAssign.indexOf(curCPU);
                    Long newCPUWCETToAssign = curCPUWCETToAssign + cpusWCETLoad.elementAt(cpuId);
                    cpusWCETLoad.removeElementAt(cpuId);
                    cpusWCETLoad.insertElementAt(newCPUWCETToAssign,cpuId);
                }

                wcetToAssign -= curCPUWCETToAssign;
            }

            visitedCPUs++;
        }

        if(visitedCPUs==maxCPUs && wcetToAssign>0){
            System.err.println("Automated mapping generations error: no CPUS with free space found during " +
                   node.getName()+ " partitions assignment");

        }


        Vector<Long> partitionSizes = getPartitionSizes(cpusWCETLoad,nodeWCET,totalPartitions);
        Long partitionSize;
        MProcessor cpu;
        Integer partitionId = 0;
        for(int i=0; i< assignedCPUs;i++){
            cpu = CPUsToAssign.elementAt(i);
            partitionSize = partitionSizes.elementAt(i);
            _assignNodePartitionToCPU(node,cpu,partitionId,partitionSize, totalPartitions);
            partitionId++;
        }


    }



    private Vector<Long> getPartitionSizes(Vector<Long> cpusFreeWCET, Long totalWCETToShare,
                                              Long totalPartitions){
        Double share;
        Double dPartitions;
        Vector<Long> pSizes = new Vector<>();
        /** check sum for partitions */
        Long pCheckSum = 0L;

        for(Long cpuWCETAsssigned: cpusFreeWCET){
            share = (double)cpuWCETAsssigned/(double)totalWCETToShare;
            dPartitions = totalPartitions * share;
            pSizes.add(dPartitions.longValue());
            pCheckSum += dPartitions.longValue();
        }

        Long newLastElem;
        if(pCheckSum!=totalPartitions){
            newLastElem = pSizes.elementAt(pSizes.size()-1) + (totalPartitions-pCheckSum);
            pSizes.removeElementAt(pSizes.size()-1);
            pSizes.add(newLastElem);
        }

        return pSizes;
    }


   /** assign node partition to CPU*/
    private void _assignNodePartitionToCPU(CSDFNode node, MProcessor cpu, Integer partitionId, Long neurons, Long totalNeurons){
        MProcess mp = new MProcess(node.getName() + "_" + partitionId);
        Vector  processes = cpu.getProcessList();
        processes.add(mp);
        Long oldWCET = _cpuWCETSpread.get(cpu.getName());
        Long partitionWCET = _nodeWCETs.get(node.getName()) * neurons/totalNeurons;
        Long newWCET = oldWCET + partitionWCET;
        _cpuWCETSpread.replace(cpu.getName(),oldWCET,newWCET);

        System.out.println(node.getName() + " partition_" +
                partitionId + " assigned to " + cpu.getName() + " with share = " + neurons + "/" + totalNeurons + " neurs" );
    }



    public void printCPUWCETSpread(){
        for (Map.Entry<String,Long> wcetSpread: entriesSortedByValues(_cpuWCETSpread)){
            System.out.println("CPU: "+wcetSpread.getKey() + ", wcet: "+wcetSpread.getValue());
            MProcessor mProcessor = _findCPUMProcessorByName(wcetSpread.getKey());
            System.out.print("Nodes: { ");
            for(Object mProcessObj: mProcessor.getProcessList()){
                MProcess proc = (MProcess)(mProcessObj);
                CSDFNode node = (CSDFNode) _csdfg.getNode(proc.getName());
                System.out.print("[ " + proc.getName()+", wcet: " + node.getOperator().getTimeComplexity()+"]; ");
            }

            System.out.println("}");
        }
    }

        public void printCPUWCETSpreadWithSplits(){
        for (Map.Entry<String,Long> wcetSpread: entriesSortedByValues(_cpuWCETSpread)){
            System.out.println("CPU: "+wcetSpread.getKey() + ", wcet: "+wcetSpread.getValue());
            MProcessor mProcessor = _findCPUMProcessorByName(wcetSpread.getKey());
            System.out.print("Nodes: { ");
            for(Object mProcessObj: mProcessor.getProcessList()){
                MProcess proc = (MProcess)(mProcessObj);
                System.out.print(proc.getName()+", ");
            }

            System.out.println("}");
        }
    }

     public void printWCETs(){
        _initNodesWCETS();
        Long avgWCET = 0L;
        for (Map.Entry<String,Long> wcetSpread: entriesSortedByValues(_nodeWCETs)){
            System.out.println(wcetSpread.getKey() + " : " + wcetSpread.getValue());
            avgWCET += wcetSpread.getValue();
        }

        Integer iNodesNum = _csdfg.countNodes();

        Long nodesNum = Long.parseLong(iNodesNum.toString());
        avgWCET = avgWCET/nodesNum;


        System.out.println("AVERAGE WCET: " + avgWCET);

    }



    public void generateEvenNodeSpreadMapping(){
        _initNodesWCETS();
        int cpuNum = _cpuList.size();
        int gpuNum = _gpuList.size();


        int curCPUId = 0;
        MProcessor curCPU = _mCPUList.firstElement();

        int curGPUId = 0;
        Vector<CSDFNode> gpuNodes = new Vector<>();
        boolean useGPU = false;
        if(gpuNum>0) {
            gpuNodes = _convNodes;
            useGPU = true;
        }

        for (CSDFNode node: gpuNodes) {
            /** Accelerate node, using GPU*/
            /** TODO: refactoring*/
            if (useGPU) {
                _setGPUCallPthreadParam(node,curGPUId);
                /**select next GPU core for mapping */
                curGPUId++;
                if (curGPUId == gpuNum)
                    curGPUId = 0;
            }
        }

        for (Object nodeObj: _csdfg.getNodeList()) {
            CSDFNode node = (CSDFNode) nodeObj;
            _assignNodeToCPU(node,curCPU);
            /**select next CPU core for mapping */
            curCPUId++;
            if (curCPUId == cpuNum)
                curCPUId = 0;
            curCPU = _mCPUList.elementAt(curCPUId);
        }
    }







public void accelerateConvs() {
    int gpuNum = _gpuList.size();
    /** no GPUs = no acceleration*/
    if(gpuNum<1) return;

    if(_blockSizes==null)
        return;
    if(_blockSizes.size()!=_convNodes.size())
        return;

    /** Accelerate conv nodes, using GPU*/
    Long blockSize;
    for (CSDFNode node : _convNodes) {
        blockSize = _blockSizes.get(node.getName());
        if(blockSize==null) blockSize = 1L;
        _accelerate(node, blockSize);
    }
}

    /**
     * Pre-spread Convolutional nodes over CPUs and Accelerators (GPUs/FPGAs)
     */
    protected void _preSpreadConvNodes(){
        int cpuNum = _cpuList.size();
        int gpuNum = _gpuList.size();

        /** Nodes to accelerate!*/
        int curCPUId = 0;
        MProcessor curCPU = _mCPUList.firstElement();
        int curGPUId = 0;

        boolean useGPU = false;
        if(gpuNum>0) { useGPU = true; }

        for (CSDFNode node: _convNodes) {
            /** Accelerate node, using GPU*/
            /** TODO: refactoring*/
            if (useGPU) {
                _setGPUCallPthreadParam(node,curGPUId);
                /**select next GPU core for mapping */
                curGPUId++;
                if (curGPUId == gpuNum)
                    curGPUId = 0;
            }

            /**select least busy CPU core for mapping */
            MProcessor cpu = _getLeastBusyCPU();
            _assignNodeToCPU(node,cpu);
        }
    }



    protected void _spreadNonConvNodesFirtFitBest(){
     /** TODO: REFACTORING!*/
     Vector<CSDFNode> mapped = _convNodes;
     Vector<CSDFNode> toMap = new Vector<>();

     for(Object nodeObj: _csdfg.getNodeList()){
         CSDFNode node = (CSDFNode)(nodeObj);
         if(!_convNodes.contains(node))
            toMap.add(node);
     }


     MProcessor curCPU;

     for (int i=0; i<_csdfg.countNodes()-_convNodes.size();i++) {
            CSDFNode node = _getLargestWCETNode(toMap);
            if(!mapped.contains(node)) {
                curCPU = _getLeastBusyCPU();
                _assignNodeToCPU(node, curCPU);
                toMap.removeElement(node);
            }

     }

    }


    /** find CPU with least estimated WCET*/
    protected MProcessor _getLeastBusyCPU(){
        Long leastWCET = _cpuWCETSpread.firstEntry().getValue();

        String leastBusyCPUName = _cpuWCETSpread.firstEntry().getKey();
        for(Map.Entry<String,Long> wcet: _cpuWCETSpread.entrySet()){
            if(wcet.getValue()<leastWCET) {
                leastWCET = wcet.getValue();
                leastBusyCPUName = wcet.getKey();
            }
        }

        MProcessor leastBusyCPU = _findCPUMProcessorByName(leastBusyCPUName);
        return leastBusyCPU;
    }

    /** find CPU with least estimated WCET, which does NOT belong
     * to exceptCPUs list
     * @param exceptCPUs exceptCPUs list
     * @return CPU with least estimated WCET, which does NOT belong
     * to exceptCPUs list
     */
    protected MProcessor _getLeastBusyCPU(Vector<String> exceptCPUs){

        Long leastWCET = 0L;
        String leastBusyCPUName = "";

          for(Map.Entry<String,Long> wcet: _cpuWCETSpread.entrySet()){
                if(!( exceptCPUs.contains(wcet.getKey()))){
                    leastWCET = wcet.getValue();
                    leastBusyCPUName = wcet.getKey();
                }
        }

        for(Map.Entry<String,Long> wcet: _cpuWCETSpread.entrySet()){
            if(wcet.getValue()<leastWCET) {
                if(!( exceptCPUs.contains(wcet.getKey()))){
                    leastWCET = wcet.getValue();
                    leastBusyCPUName = wcet.getKey();
                }
            }
        }
        MProcessor leastBusyCPU = _findCPUMProcessorByName(leastBusyCPUName);
        return leastBusyCPU;
    }

    /** find CPU with least estimated WCET*/
    protected Long _getMaxConvWCET(){
        Long maxWCET = 0L;

        for(Long wcet: _cpuWCETSpread.values()){
            if(wcet > maxWCET) {
                maxWCET = wcet;
            }
        }
        return maxWCET;
    }


    protected Long _getAvgWCET(Integer cpuNum){
        if(cpuNum==0)
            return 0L;

        Long avgWCET = 0L;

        for(Long wcet: _nodeWCETs.values())
            avgWCET+=wcet;

        Long lCPUNum = Long.parseLong(cpuNum+ "");
        avgWCET = avgWCET/lCPUNum;
        return avgWCET;
    }



    /** find CPU with least estimated WCET*/
    protected Long _getMaxCPUWCET(){
        Long maxWCET = 0L;

        for(Long wcet: _cpuWCETSpread.values()){
            if(wcet > maxWCET) {
                maxWCET = wcet;
            }
        }
        return maxWCET;
    }

    protected MProcessor _findCPUMProcessorByName(String name){
        for(MProcessor mProcessor: _mCPUList) {
            if(mProcessor.getName().equals(name))
                return mProcessor;
        }
        return null;
    }


    /** assign node to CPU*/
    private void _assignNodeToCPU(CSDFNode node, MProcessor cpu){
        MProcess mp = new MProcess(node.getName());
        Vector  processes = cpu.getProcessList();
        processes.add(mp);
        Long oldWCET = _cpuWCETSpread.get(cpu.getName());
        Long newWCET = oldWCET + _nodeWCETs.get(node.getName());
        _cpuWCETSpread.replace(cpu.getName(),oldWCET,newWCET);

        System.out.println("Node "+ node.getName()+" assigned to " + cpu.getName());
    }

    /**
     * Set CPU call pthread parameter
     * @param node CSDF node
     * @param GPUId GPU Id
     */
    private void _setGPUCallPthreadParam(CSDFNode node, int GPUId){
        Operator nodeOp = node.getOperator();
         if(nodeOp!=null) {
             if (nodeOp.hasIntegerParams()) {
                 /** change GPU call property for pthread application*/
                 node.getOperator().getIntParams().remove("gpu");
                 node.getOperator().addIntParam("gpu", GPUId);
             }
         }

    }

    private void _accelerate(CSDFNode node, Long accelerationFactor){
        Operator nodeOp = node.getOperator();
         if(nodeOp==null || accelerationFactor<1)
         return;

         Long originalComplexity = Long.parseLong(node.getOperator().getTimeComplexity().toString());
         Long acceleratedCompelxity = originalComplexity/accelerationFactor;
         acceleratedCompelxity = Math.max(acceleratedCompelxity,1);
         node.getOperator().setTimeComplexity(acceleratedCompelxity.intValue());
    }

    ///////////////////
    /// INITIALIZE ///
     /**
      * Initialize mapping generation algorithm
      */
    public boolean initMapping(){
        if(_csdfg==null) {
            System.err.println("mapping generation error: null application (CSDFG) model ");
            return false;
        }

        if (_platform==null) {
            System.err.println("mapping generation error: null platform");
            return false;
        }

        try {

            _mCPUList = new Vector<>();
            _mGPUList = new Vector<>();
            _processors = new Vector<>();
            _cpuWCETSpread  = new TreeMap<>();

            _cpuList = _getCPUList(_platform);
            _gpuList = _getGPUList(_platform);

            if(_cpuList.size()<1) {
                System.err.println("mapping generation error: no CPU found!");
                return false;
            }

            _mapping = new Mapping(_csdfg.getName() + "_to_" + _platform.getName());


            _initCPUList();
            _initGPUList();
            _mapping.setProcessorList(_processors);

            /** TODO: update for other types of nodes if needed*/
            _convNodes = _csdfg.getNodesList("conv");

            return true;
        }
         catch (Exception e){
            System.err.println("mapping generation error "+e.getMessage());
            return false;
        }
    }


    /**
     * Get list of cpu cores in the mapping
     * @param platform platform
     * @return list of cpu cores in the mapping
     */
    protected Vector<Processor> _getCPUList(Platform platform){
        Vector<Processor> cpuList = new Vector<>();
            for (Object resObj: platform.getResourceList()) {
                if(resObj instanceof Processor) {
                    if(!(resObj instanceof GPU)){
                        Processor cpu = (Processor)resObj;
                        cpuList.add(cpu);
                    }
                }
            }
        return cpuList;
    }

        /**
     * Get list of cpu cores in the mapping
     * @param platform platform
     * @return list of cpu cores in the mapping
     */
    protected Vector<Processor> _getGPUList(Platform platform){
        Vector<Processor> gpuList = new Vector<>();
            for (Object resObj: platform.getResourceList()) {
                    if(resObj instanceof GPU){
                        Processor gpu = (Processor)resObj;
                        gpuList.add(gpu);
                }
            }
        return gpuList;
    }

        private void _initCPUList(){
     //init cpu list
        for (Processor proc: _cpuList){
            MProcessor cpu = new MProcessor(proc.getName());
            cpu.setResource(proc);
            cpu.setProcessList(new Vector());
            _processors.add(cpu);
            _mCPUList.add(cpu);
            _cpuWCETSpread.put(cpu.getName(),0L);
        }

    }

    private void _initGPUList(){
     //init gpu list
        for (Processor proc: _gpuList){
            MProcessor gpu = new MProcessor(proc.getName());
            gpu.setResource(proc);
            gpu.setProcessList(new Vector());
            _processors.add(gpu);
            _mGPUList.add(gpu);
        }
    }

    /** initialize node worst-case exec times*/
    private CSDFNode _getLargestWCETNode(Vector<CSDFNode> nodesToMap){

        String nodeName = nodesToMap.firstElement().getName();
        CSDFNode nodeToReturn = nodesToMap.firstElement();
        Long WCET;
        Long largestWCET = _nodeWCETs.get(nodeName);

        for(CSDFNode node: nodesToMap){
            if(node.getOperator()!=null){
                WCET = _nodeWCETs.get(node.getName());

                if(WCET>largestWCET){
                    //System.out.println("[ node " + node.getName() + " : " + WCET + "] > ["
                      //      + nodeToReturn.getName() + " : " + largestWCET + "]");

                    nodeToReturn = node;
                    largestWCET = WCET;
                }
            }

        }
        return nodeToReturn;
    }


    /** initialize node worst-case exec times*/
    private CSDFNode _getSmallestWCETNode(Vector<CSDFNode> nodesToMap){

        String nodeName = nodesToMap.firstElement().getName();
        CSDFNode nodeToReturn = nodesToMap.firstElement();
        Long WCET;
        Long smallestWCET = _nodeWCETs.get(nodeName);

        for(CSDFNode node: nodesToMap){
            if(node.getOperator()!=null){
                WCET = _nodeWCETs.get(node.getName());

                if(WCET<smallestWCET){
                    //System.out.println("[ node " + node.getName() + " : " + WCET + "] > ["
                      //      + nodeToReturn.getName() + " : " + largestWCET + "]");

                    nodeToReturn = node;
                    smallestWCET = WCET;
                }
            }

        }
        return nodeToReturn;
    }


    /** initialize node worst-case exec times*/
    private void _initNodesWCETS(){
        _nodeWCETs = new TreeMap<>();
        String nodeName;
        Long WCET;
        for(Object nodeObj : _csdfg.getNodeList()){
            CSDFNode node = (CSDFNode) nodeObj;
            nodeName = node.getName();
            WCET = 1L;
            if(node.getOperator()!=null){
                WCET = Long.parseLong(node.getOperator().getTimeComplexity().toString());
            }
            _nodeWCETs.put(nodeName,WCET);
        }
    }

    public Mapping getMapping() {
        return _mapping;
    }

    ////////////////////////////////////////////////////////
    //////////// private variables ////////////////////////

    private Network _dnn;
    private CSDFGraph _csdfg;
    private Platform _platform;
    private Vector<Processor> _cpuList;
    private Vector<Processor> _gpuList;
    private Vector<MProcessor> _mCPUList;
    private Vector<MProcessor> _mGPUList;
    Vector<MProcessor> _processors;
    private Mapping _mapping;
    private TreeMap<String, Long> _cpuWCETSpread;
    private TreeMap<String, Long> _nodeWCETs;
    private TreeMap<String,Long> _blockSizes;
    /** TODO: update for other types of nodes if needed*/
    private Vector<CSDFNode> _convNodes;
    private Integer _awgWCEtperCPU = 0;
    private PlatformEval _platformEval;
    private  DNN_MAPPING_TYPE _dnn_mapping_type = DNN_MAPPING_TYPE.SEQUENTIAL;

}
