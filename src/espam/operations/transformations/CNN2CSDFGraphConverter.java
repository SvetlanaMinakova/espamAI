package espam.operations.transformations;

import espam.datamodel.graph.Edge;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.connections.Custom;
import espam.datamodel.graph.cnn.connections.OneToOne;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.csdf.*;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.platform.memories.Memory;
import espam.main.cnnUI.DNNInitRepresentation;
import espam.operations.refinement.CSDFGMemoryRefiner;
import espam.operations.refinement.CSDFTimingRefiner;

import java.util.HashMap;
import java.util.Vector;

/**
 * Class converts a CNN model to SDF model
 */
public class CNN2CSDFGraphConverter {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////               Layer-based approach                       ////
    /**
     * Builds the whole CSDFGraph from the Network
     * @param network source Network
     * @return CSDFGraph from the Network
     * TODO remove after testing
     */
    public CSDFGraph buildGraphLayerBased(Network network, boolean minimizeDF, boolean keepheighDep) {
        /** sort layer in traverse order*/
        network.sortLayersInTraverseOrder();

        /**Copy network and calculate min DF for it (not to spoil the original DNN model) */
        _minimizedDFDNN = new Network(network);
            if(minimizeDF)
                _minimizedDFDNN.minimizeDataFlow(keepheighDep);
        _CSDFG = new CSDFGraph(network.getName(), SDFGraphType.csdf);
        _CSDFG.setTokenDesc(network.getDataType());
        _refiner.setIOmemType(network.getDataType());
        /** Build layers */

        HashMap<Layer,CSDFNode> layersMapping = new HashMap<>();
        int nextNodeId = 0;
        for(Layer layer:network.getLayers()) {
            CSDFNode newSDFLayer = buildLayerLB(layer,nextNodeId);
            layersMapping.put(layer,newSDFLayer);
            _CSDFG.addNodes(newSDFLayer);
            nextNodeId = _CSDFG.getNextNodeId();
        }
        /**
         * build edges
         */
        int nextEdgeId = _CSDFG.getNextEdgeId();
         for(Connection con:network.getConnections()) {
            CSDFNode l1 = layersMapping.get(con.getSrc());
            CSDFNode l2 = layersMapping.get(con.getDest());
            Vector<CSDFEdge> newCSDFEdges = buildEdgesLB(l1,l2,con,nextEdgeId);
            _CSDFG.addEdges(newCSDFEdges);
            nextEdgeId=_CSDFG.getNextEdgeId();
        }

        _CSDFG.alignRatesLength();
        return _CSDFG;
    }


     /**
     * Builds the whole CSDFGraph from the Network
     * @param network source Network
     * @return CSDFGraph from the Network
     */
    public CSDFGraph buildGraphLayerBased(Network network) {
        /** sort layers in traverse order*/
        network.sortLayersInTraverseOrder();

        /**Copy network and calculate min DF for it (not to spoil the original DNN model) */
        _minimizedDFDNN = new Network(network);
        _minimizedDFDNN.minimizeDataFlow(false);
        _CSDFG = new CSDFGraph(network.getName(), SDFGraphType.csdf);
        _CSDFG.setTokenDesc(network.getDataType());
        _refiner.setIOmemType(network.getDataType());
        /** Build layers */

        HashMap<Layer,CSDFNode> layersMapping = new HashMap<>();
        int nextNodeId = 0;
        for(Layer layer:network.getLayers()) {
            CSDFNode newSDFLayer = buildLayerLB(layer,nextNodeId);
            layersMapping.put(layer,newSDFLayer);
            _CSDFG.addNodes(newSDFLayer);
            nextNodeId = _CSDFG.getNextNodeId();
        }
        /**
         * build edges
         */
        int nextEdgeId = _CSDFG.getNextEdgeId();
         for(Connection con:network.getConnections()) {
            CSDFNode l1 = layersMapping.get(con.getSrc());
            CSDFNode l2 = layersMapping.get(con.getDest());
            Vector<CSDFEdge> newCSDFEdges = buildEdgesLB(l1,l2,con,nextEdgeId);
            _CSDFG.addEdges(newCSDFEdges);
            nextEdgeId=_CSDFG.getNextEdgeId();
        }

        _CSDFG.alignRatesLength();
        return _CSDFG;
    }

    /**
     * Build representation of the CNN layer in SDF-like format:
     * @param layer layer description
     * @return vector of the nodes, corresponding neurons in a layer
     */
    public  CSDFNode buildLayerLB(Layer layer, int nextNodeId)
    {
        Layer minDFLayer = _minimizedDFDNN.getLayer(layer.getId());
        Neuron minDFNeuron = minDFLayer.getNeuron();

        CSDFNode node = new CSDFNode(layer.getName(),nextNodeId);

        /** add memory units*/
        Vector<MemoryUnit> iomemory = _refiner.callVisitor(minDFLayer);
        node.setMemoryUnits(iomemory);
        MemoryUnit weights = _refiner.createWeightsDescription(layer,_minimizedDFDNN.getWeightsType());
        if(weights!=null)
            node.addMemoryUnit(weights);

        /** specify operation */
        String operation = minDFNeuron.getFunctionCallDescription(layer.getInputChannels());
        node.setOperation(operation);
        int dstOperationRepetitions = minDFNeuron.getOperationsNumber(layer.getNeuronsNum());
        node.setOperationRepetitionsNumber(dstOperationRepetitions);
        node.setKernelsNum(layer.getNeuronsNum());

        return node;
    }

     /**
     * Build representation of connection between CNN layers as list of SDF Edges
     * @param startNode connection's input layer in SDF-like format
     * @param endNode connection's output layer in SDF-like format
     * @param connection connection
     * @return representation of connection between CNN layers as a list of edges
     */

     public Vector<CSDFEdge> buildEdgesLB(CSDFNode startNode, CSDFNode endNode, Connection connection, int lastEdgeId) {
         Vector<CSDFEdge> edges = new Vector<>();
         String edgeName = connection.getSrcId()+ "to" + connection.getDestId();
         String dstSelfLoopPrefix = connection.getDestId() + "to" + connection.getDestId();

         try{
            Neuron l1SampleNeuronMinimized = _minimizedDFDNN.getLayer(connection.getSrc().getName()).getNeuron();
            Neuron l2SampleNeuron = connection.getDest().getNeuron();
            Neuron l2SampleNeuronMinimized = _minimizedDFDNN.getLayer(connection.getDest().getName()).getNeuron();

            Vector<IndexPair> srcOutputRate = calcOutputRates(connection.getSrc(),l1SampleNeuronMinimized.getOutputDataFormat());
            int channels = connection.getSrc().getNeuronsNum();
            Vector<IndexPair> dstInputRate = calcInputRates(connection.getDest(),l2SampleNeuronMinimized.getInputDataFormat(),channels);
            Vector<IndexPair> selfLoopRate = _getSelfLoopRate(connection.getDest(),connection,dstInputRate);

            /** connection buffers description*/
            MemoryUnit srcMinOutMemory = startNode.getMemoryUnit("output");
            String endNodeMUBufferName = "input";
            if(l2SampleNeuron instanceof MultipleInputsProcessor){
                if (((MultipleInputsProcessor) l2SampleNeuron).getInputOwners().size()>1)
                endNodeMUBufferName = connection.getSrc().getName();
            }

            MemoryUnit dstMinInMemory = endNode.getMemoryUnit(endNodeMUBufferName);

            CSDFEdge edge = _createEdge(edgeName,lastEdgeId,startNode,endNode,srcOutputRate,dstInputRate, srcMinOutMemory,dstMinInMemory);
            edges.add(edge);
            lastEdgeId++;

            /** process self-loops, if any*/
            if (selfLoopRate!=null) {
                edgeName = dstSelfLoopPrefix + "_" + lastEdgeId;
                CSDFEdge selfEdge = _createOverlapProcessingSelfEdge(edgeName,lastEdgeId,endNode,selfLoopRate,edge);
                edges.add(selfEdge);
                lastEdgeId++;
            }

            /**
             * inherit rate for multiple input processors.
             * Consistency checkout of summary I/O rate for multiple input processors
             * should be done in Network model
             */
            if (l2SampleNeuron instanceof MultipleInputsProcessor) {
                edge.getDst().setRates(edge.getSrc().getRates());
            }

            if(l2SampleNeuron instanceof GenericNeuron){
                _refineGenericNode((GenericNeuron)l2SampleNeuron,(GenericNeuron)l2SampleNeuronMinimized,endNode,connection);
            }

            return edges;
         }

         catch (Exception e) { System.err.println(edgeName +
                 " edges build error. " + e.getMessage()); }
            return edges;
     }

    ///////////////////////////////////////////////////////////////////
    ////               Neuron/block-based approach                ////
    /**
     * Builds the whole CSDFGraph from the Network
     * @param network source Network
     * @return CSDFGraph from the Network
     */
    public CSDFGraph buildGraph(Network network) {
       _CSDFG = new CSDFGraph(network.getName(),SDFGraphType.csdf);
       _CSDFG.setTokenDesc(network.getDataType());
       _refiner.setIOmemType(network.getDataType());
        /** sort layer in traverse order*/
        network.sortLayersInTraverseOrder();

        /**Copy network and calculate min DF for it (not to spoil the original DNN model) */
        _minimizedDFDNN = new Network(network);
        _minimizedDFDNN.minimizeDataFlow(false);

        /**
        * build layers
        */
        int nextNodeId = 0;
        HashMap<Layer,Vector<CSDFNode>> layersMapping = new HashMap<Layer,Vector<CSDFNode>>();
        for(Layer layer:network.getLayers()) {
            Vector<CSDFNode> newSDFLayer = buildLayer(layer.getId(),nextNodeId);
            layersMapping.put(layer,newSDFLayer);
            _CSDFG.addNodes(newSDFLayer);
            nextNodeId = _CSDFG.getNextNodeId();
        }
        /**
         * build edges
         */
        int nextEdgeId = 0;
        for(Connection con:network.getConnections()) {
            Vector<CSDFNode>l1 = layersMapping.get(con.getSrc());
            Vector<CSDFNode>l2 = layersMapping.get(con.getDest());
            Vector<CSDFEdge> newCSDFEdges = buildEdges(l1,l2,con,nextEdgeId);
            _CSDFG.addEdges(newCSDFEdges);
            nextEdgeId = _CSDFG.getNextEdgeId();
        }

        _CSDFG.alignRatesLength();
        return _CSDFG;
    }

     /**
     * Build representation of the CNN layer in SDF-like format
     * @param layerId unique layer identifier
     * @return vector of the nodes, corresponding neurons in a layer
     */
    public  Vector<CSDFNode> buildLayer(int layerId, int nextNodeId)
    {
        Vector<CSDFNode> layerSDF = new Vector<CSDFNode>();
        Layer minDFLayer = _minimizedDFDNN.getLayer(layerId);
        Neuron minDFLayerNeuron = minDFLayer.getNeuron();

        /**
         * Memory refinement:
         * Memory units (i/o data formats and weights) are typical for each node of this layer,
         * so they are stored as references on one typical memory unit
         * */
        Vector<MemoryUnit> iomemory = _refiner.callVisitor(minDFLayerNeuron);
        MemoryUnit weights = _refiner.createWeightsDescription(minDFLayerNeuron, minDFLayer.getInputChannels(),_minimizedDFDNN.getWeightsType());

        /** Add neurons */
        int nodeId = nextNodeId;
        for(int i=0;i<minDFLayer.getNeuronsNum();i++) {
           CSDFNode newCSDFNode = new CSDFNode(minDFLayer.getName() + "_" + minDFLayerNeuron.getName() + "_" + i, nodeId);
           newCSDFNode.setGroup(minDFLayer.getName());
           newCSDFNode.setMemoryUnits(iomemory);
           if(weights!=null)
               newCSDFNode.addMemoryUnit(weights);

           layerSDF.add(newCSDFNode);
           nodeId++;
        }

        return layerSDF;
    }

    /**
     * Build representation of connection between CNN layers as list of SDF Edges
     * @param SDFlayer1 connection's input layer in SDF-like format
     * @param SDFlayer2 connection's output layer in SDF-like format
     * @param connection connection
     * @return representation of connection between CNN layers as a list of edges
     */

     public Vector<CSDFEdge> buildEdges(Vector<CSDFNode> SDFlayer1, Vector<CSDFNode>SDFlayer2, Connection connection, int lastEdgeId) {
           Vector<CSDFEdge> edges = new Vector<CSDFEdge>();
           Vector<CSDFEdge> selfEdges = new Vector<>();
           String edgePrefix = connection.getSrcName() + "to" + connection.getDestName();
           String dstSelfLoopPrefix = connection.getDestName() + "to" + connection.getDestName();
           boolean[][] connectionMatrix = connection.getConnectionMatrix();

           Neuron l1SampleNeuron = connection.getSrc().getNeuron();
           Neuron l1SampleNeuronMinimized = _minimizedDFDNN.getLayer(connection.getSrc().getName()).getNeuron();
           Neuron l2SampleNeuron = connection.getDest().getNeuron();
           Neuron l2SampleNeuronMinimized = _minimizedDFDNN.getLayer(connection.getDest().getName()).getNeuron();

          MemoryUnit srcMinOutMemory = null;
          MemoryUnit dstMinInMemory =null;
           /** port assigned I/O memory description*/

           srcMinOutMemory = SDFlayer1.elementAt(0).getMemoryUnit("output");
           if (l2SampleNeuron instanceof MultipleInputsProcessor){
               if(((MultipleInputsProcessor) l2SampleNeuron).getInputOwners().size()>1)
               dstMinInMemory = SDFlayer2.elementAt(0).getMemoryUnit(connection.getSrc().getName());
           }
           else
               dstMinInMemory = SDFlayer2.elementAt(0).getMemoryUnit("input");

           Vector<IndexPair> srcNeuronOutputRate = calcOutputRates(l1SampleNeuron,l1SampleNeuronMinimized.getOutputDataFormat());
           Vector<IndexPair> dstNeuronInputRate = calcInputRates(l2SampleNeuron,l2SampleNeuronMinimized.getInputDataFormat());

           /** flag, if a neuron have self-loops*/
           boolean dstSelfLoop = false;
           Vector<IndexPair> selfLoopRate = _getSelfLoopRate(l2SampleNeuron,dstNeuronInputRate);

           if(selfLoopRate != null)
                   dstSelfLoop = true;

           try {
               for (int j = 0; j < connectionMatrix.length; j++) {
                   CSDFNode startNode = SDFlayer1.elementAt(j);
                   for (int i = 0; i < connectionMatrix[j].length; i++) {
                       if (connectionMatrix[j][i]) {
                           CSDFNode endNode = SDFlayer2.elementAt(i);
                           String edgeName = edgePrefix + "_" + i + "_" + j + "_";
                           CSDFEdge edge = _createEdge(edgeName, lastEdgeId, startNode, endNode,
                                   srcNeuronOutputRate, dstNeuronInputRate,srcMinOutMemory,dstMinInMemory);

                           edges.add(edge);
                           lastEdgeId++;

                           /** process self-loops, if any information is kept in j node*/
                           if (dstSelfLoop) {
                               edgeName = dstSelfLoopPrefix + "_" + lastEdgeId;
                               CSDFEdge selfEdge =  _createOverlapProcessingSelfEdge(edgeName,lastEdgeId,endNode,selfLoopRate, edge);

                               selfEdges.add(selfEdge);
                               lastEdgeId++;
                           }
                       }
                   }
               }

               /**
                * TODO move to nodes building if possible
                */
               for(int i=0; i<SDFlayer2.size();i++){
                    CSDFNode endNode = SDFlayer2.elementAt(i);
                    int dstInputs = endNode.getNonOverlapHandlingInPorts().size();
                    String operation = l2SampleNeuronMinimized.getFunctionCallDescription(dstInputs);
                    endNode.setOperation(operation);
                    int dstOperationRepetitions = l2SampleNeuronMinimized.getOperationsNumber(dstInputs);
                    endNode.setOperationRepetitionsNumber(dstOperationRepetitions);
               }

               /**
                * Inherit rate for multiple input processors.
                * Consistency checkout of summary I/O rate for multiple input processors
                * should be done in Network model
                * */
               if (l2SampleNeuron instanceof MultipleInputsProcessor) {
                   for (CSDFEdge edge : edges) {
                       edge.getDst().setRates(edge.getSrc().getRates());
                   }
               }

           }

        catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(" err bounds exception: src" + connection.getSrc().getName() + " (" + l1SampleNeuron.getName() +
            ") , nn: " + connection.getSrc().getNeuronsNum() +
            "\n , dst: " + connection.getDest().getName() + ("(") + l2SampleNeuron.getName() +
            "\n , nn: " + connection.getDest().getNeuronsNum() +
            "\n connection type: " + connection.getType().toString()+
            "\n connection matrix len: " + connectionMatrix.length + "\n");
        }

        edges.addAll(selfEdges);
        return edges;
    }


    /**
     * TODO REFACTORING ON MUs!
     * Create new SDF Edge
     * @param name edge name
     * @param id edge id
     * @param startNode source node
     * @param endNode destination node
     * @param srcRate source node output format
     * @param dstRate dest node output format
     * @return new SDF edge
     */
    private CSDFEdge _createEdge(String name, int id, CSDFNode startNode, CSDFNode endNode,
                                Vector<IndexPair> srcRate, Vector<IndexPair> dstRate,
                                MemoryUnit startMUDesc, MemoryUnit endMUDesc) {

        CSDFEdge edge = new CSDFEdge(name, id);
        CSDFPort start = addOutput(startNode, srcRate);
        CSDFPort end = addInput(endNode, dstRate);

        start.setEdge(edge);
        end.setEdge(edge);

        edge.setSrc(start);
        edge.setDst(end);

        if(startMUDesc==null){
            startMUDesc = startNode.getMemoryUnit(startNode.getOutPorts().lastElement().getName());
        }

        if(startMUDesc==null){
            startMUDesc = startNode.getMemoryUnit(endNode.getName());
        }

        if(endMUDesc==null){
            endMUDesc = endNode.getMemoryUnit(endNode.getInPorts().lastElement().getName());
        }

         if(endMUDesc==null){
            endMUDesc = endNode.getMemoryUnit(startNode.getName());
        }

        start.setAssignedMemory(startMUDesc);
        MemoryUnit memCpy = null;
        if (endMUDesc != null) {
            /** if memory is occupied, provide input port with a new memory template*/
            if(endMUDesc.isAssigned()) {
                memCpy = endMUDesc.clone();
                memCpy.setName(memCpy.getName() + "_" + end.getName());
                endNode.addMemoryUnit(memCpy);
            }
            else {
                endMUDesc.setAssigned(true);
                memCpy = endMUDesc;
            }
        }
         end.setAssignedMemory(memCpy);
         return edge;
    }



    /**
     * Create new SDF Edge, handling overlapping input data for
     * generic(block) neuron
     * @param name edge name
     * @param id edge id
     * @param node overlap processing node
     * @param rate self-loop rate
     * @return new SDF edge, handling overlapping input data
     * @return
     */
    private CSDFEdge _createOverlapProcessingSelfEdge(String name, int id, CSDFNode node,
                                                     Vector<IndexPair> rate, CSDFEdge parentEdge){
         CSDFEdge selfEdge = new CSDFEdge(name, id);
         CSDFPort start = addOutput(node, rate);
         start.setOverlapHandler(true);

         Vector<IndexPair> inRate = new Vector<>();
         for(int i=rate.size()-1;i>=0;i--)
             inRate.add(rate.elementAt(i));

         CSDFPort end = addInput(node, inRate);
         end.setOverlapHandler(true);
         start.setEdge(selfEdge);
         end.setEdge(selfEdge);

         selfEdge.setSrc(start);
         selfEdge.setDst(end);

         parentEdge.getDst().setStartTokens(end.getRates());

         end.setOverlapPair(parentEdge.getDst().getName());
         end.setAssignedMemory(parentEdge.getDst().getAssignedMemory());

         start.setOverlapPair(end.getName());
         start.setAssignedMemory(end.getAssignedMemory());

         return selfEdge;
    }

    /**
     * Create new SDF Edge, handling overlapping input data for
     * generic(block) neuron
     * @param name edge name
     * @param id edge id
     * @param node overlap processing node
     * @param rate self-loop rate
     * @param dataShape shape of the data, circulating in the internal self-loop
     * @return new SDF edge, handling overlapping input data
     * @return
     */
    private CSDFEdge _createSelfEdge(String name, int id, CSDFNode node,
                                                     Vector<IndexPair> rate, Tensor dataShape, String dataType, boolean isOverlapHandler){
         CSDFEdge selfEdge = new CSDFEdge(name, id);
         CSDFPort start = addOutput(node, rate);
         start.setOverlapHandler(isOverlapHandler);

         Vector<IndexPair> inRate = new Vector<>();
         for(int i=rate.size()-1;i>=0;i--)
             inRate.add(rate.elementAt(i));

         CSDFPort end = addInput(node, inRate);
         end.setOverlapHandler(isOverlapHandler);
         start.setEdge(selfEdge);
         end.setEdge(selfEdge);

         selfEdge.setSrc(start);
         selfEdge.setDst(end);

         /** TODO check if the name is unique and easily extractable*/
         MemoryUnit mu = new MemoryUnit(start.getName(),dataShape,dataType);
         node.addMemoryUnit(mu);
         node.assignMemoryUnit(name,end);

         if(isOverlapHandler)
            start.setOverlapPair(end.getName());
         start.setAssignedMemory(end.getAssignedMemory());

         return selfEdge;
    }


        /**
     * calculates port rate (in tokens)
     * @param dataFormat  data format coming in to/out of SDF port
     * @return  port rate (in tokens)
     */
    public int calcRate(Tensor dataFormat){
            if(Tensor.isNullOrEmpty(dataFormat))
                return 0;

            return dataFormat.getElementsNumber();
    }

      /**
     * Creates a new SDFInPort and adds it to SDF Node
     * @param node SDF node
     * @param rate port's rate
     * @return created SDF Port
     */
    private CSDFPort addInput(CSDFNode node, Vector<IndexPair> rate) {
        int nextPortId = node.getNextPortId();
        CSDFPort newInPort = new CSDFPort(node.getNextInPortName(),nextPortId,CSDFPortType.in);
        newInPort.setRates(rate);
        node.addPort(newInPort);
        return newInPort;
    }


      /**
     * Creates a new SDFOutPort and adds it to SDF Node
     * @param node SDF node
     * @param rate port rate
     * @return created SDF Port
     */
      private CSDFPort addOutput(CSDFNode node, Vector<IndexPair> rate) {
        int nextPortId = node.getNextPortId();
        CSDFPort newOutPort = new CSDFPort(node.getNextOutPortName(),nextPortId, CSDFPortType.out);
        newOutPort.setRates(rate);
        node.addPort(newOutPort);

        return newOutPort;
    }

    /**
     * Calculate output rates for the layer.
     * Each output rate description is multiplied on number of output rates,
     * where number of output rates = number of neurons
     * @param layer layer to to be processed
     * @return output rates description for the layer
     * TODO check
     */
    private Vector<IndexPair> calcOutputRates(Layer layer, Tensor neuronMinimizedOutput){
        Neuron neuron = layer.getNeuron();
        Vector<IndexPair> neuronRates = calcOutputRates(neuron,neuronMinimizedOutput);
        int neuronsNumber = layer.getNeuronsNum();
        if(neuronsNumber==1)
            return neuronRates;

        for(IndexPair neuronRate: neuronRates){
            neuronRate.setFirst(neuronRate.getFirst() * neuronsNumber);
        }
        return neuronRates;
    }
     /**
     * Calculate output rates for the neuron
     * @param neuron neuron to be processed
     * @return output rates description for the neuron
     * TODO check
     */
    private Vector<IndexPair> calcOutputRates(Neuron neuron, Tensor minimizedOutput){
        Tensor rateSrc = minimizedOutput;


        Vector<IndexPair> rates = new Vector<>();
        if(Tensor.isNullOrEmpty(rateSrc))
              return rates;

        if(neuron instanceof GenericNeuron)
            return calcOutputRates(((GenericNeuron) neuron).getInternalStructure().getOutputLayer().getNeuron(),minimizedOutput);

        if(neuron instanceof Data)
            return calcDataOutputRates((Data)neuron,minimizedOutput);

        if(neuron instanceof Concat || neuron instanceof Reshape)
            rateSrc = neuron.getOutputDataFormat();

        /** process min output from CNN neurons */

        int rate = calcRate(rateSrc);
        int scale = 1;
        if(minimizedOutput.getDimensionality()>1)
            scale = minimizedOutput.getDimSize(1);
        int repetitions = calcRepetitionsNum(neuron,1);
        int scaleTail = 0;
        if(repetitions%scale!=0)
            scaleTail=1;
        repetitions/=scale;
        repetitions+=scaleTail;

        IndexPair rateDesc = new IndexPair(rate,repetitions);
        rates.add(rateDesc);

        /** */
        return rates;
    }

    /**
     * For data node output rates are flexible and calculated according to its internal parameters
     * @param neuron data neuron
     * @return data node output rates
     */
    private Vector<IndexPair> calcDataOutputRates(Data neuron, Tensor minimizedOutput){
        Vector<IndexPair> rates = new Vector<>();
        /** Output neuron fires once and read all available input information*/
        if(neuron.getName().equals(DataType.OUTPUT.toString())) {
            int rate = calcRate(neuron.getOutputDataFormat());
            int repetitions = 1;
            IndexPair rateDesc = new IndexPair(rate,repetitions);
            rates.add(rateDesc);
            return rates;
        }

        /**
         * Input/constant data neurons writing data by lines, number of firing is
         * specified by ratio of real input height to minimal input height
         * */
        int realInputH = Tensor.getHeight(neuron.getOutputDataFormat());
        int minH = Tensor.getHeight(minimizedOutput);
        if(minH==0) {
            System.err.println("Data formats calculation error: data node has null minimum h.");
            return rates;
        }
        int repetitions = realInputH/minH;
        repetitions = Math.max(repetitions,1);
        int rate = calcRate(minimizedOutput);
            IndexPair rateDesc = new IndexPair(rate,repetitions);
            rates.add(rateDesc);
        return rates;
    }

    /**
     * Calculate input rates for the neuron
     * @param layer layer to be processed
     * @return rates description for the layer
     * TODO CHECK: for now it is supposed, that all information, taken by the layer
     * TODO is used by each neuron, so, Input rates for the layer are the same as for its single neuron.
     */
    private Vector<IndexPair> calcInputRates(Layer layer, Tensor neuronMinimizedInput, int inputChannels){
        Neuron neuron = layer.getNeuron();
        Vector<IndexPair> neuronRates = calcInputRates(neuron,neuronMinimizedInput);
     //   int inputChannels = layer.getInputChannels();

        if(inputChannels==1)
            return neuronRates;

        for(IndexPair rate: neuronRates){
            rate.setFirst(rate.getFirst() * inputChannels);
        }
        return neuronRates;
    }

    /**
     * Calculate input rates for the neuron
     * @param neuron neuron to be processed
     * TODO if scale is needed for Input rates?
     * @return rates description for the neuron
     */
    private Vector<IndexPair> calcInputRates(Neuron neuron, Tensor minimizedInput){

        if(neuron instanceof GenericNeuron){
            return calcInputRates(((GenericNeuron) neuron).getInternalStructure().getInputLayer().getNeuron(),minimizedInput);
        }
        /** process overlapping, if any*/
        if(neuron instanceof CNNNeuron) {
            int minH = ((CNNNeuron) neuron).getKernelSize();
            if(minimizedInput.getDimensionality()>1)
                minH = minimizedInput.getDimSize(1);
            return calcInputRatesWithOverlapping((CNNNeuron) neuron,minH);
        }
        if(neuron instanceof Data)
            return calcDataInputRates((Data)neuron);

        Vector<IndexPair> rates = new Vector<>();
        if(Tensor.isNullOrEmpty(minimizedInput))
              return rates;

        /** */
        int rate = calcRate(minimizedInput);
        int minH = 1;
        if(minimizedInput.getDimensionality()>1)
            minH = minimizedInput.getDimSize(1);
        int repetitions = calcRepetitionsNum(neuron,minH);

        IndexPair rateDesc = new IndexPair(rate,repetitions);
        rates.add(rateDesc);

        /** */
        return rates;
    }

    /**
     * For data node output rates are flexible and calculated according to its internal parameters
     * @param neuron data neuron
     * @return data node output rates
     */
    private Vector<IndexPair> calcDataInputRates(Data neuron) {
        Vector<IndexPair> rates = new Vector<>();
        /** input data have no input rates*/
        if (!neuron.getName().equals(DataType.OUTPUT.toString())) {
            return rates;
        }
           /** Output neuron fires once and read all available input information*/
            int rate = calcRate(neuron.getInputDataFormat());
            int repetitions = 1;
            IndexPair rateDesc = new IndexPair(rate, repetitions);
            rates.add(rateDesc);
            return rates;
        }


    /**
     * Calculate rates for the neuron with overlapping (CNN pooling and conv neurons)
     * @param neuron neuron to be processed
     * @return rates description for the neuron
     */
    private Vector<IndexPair> calcInputRatesWithOverlapping(CNNNeuron neuron, int minH){
          Vector<IndexPair> rates = new Vector<>();
          Tensor input = neuron.getInputDataFormat();
          if(Tensor.isNullOrEmpty(input))
              return rates;
          /** first iteration always requires h = k_size*/
          Tensor iterInput = new Tensor(input);
          iterInput.setDimSize(1,minH);
          int rate = calcRate(iterInput);
          IndexPair firstIter = new IndexPair(rate,1);
          rates.add(firstIter);
          IndexPair firstRate = rates.elementAt(0);

          /** all other iterations require h = stride*/
          int minhShift = minH-neuron.getKernelSize();
          int readingH = neuron.getStride();
          if(minhShift>0)
              readingH+=minhShift;
          iterInput.setDimSize(1,readingH);
          rate = calcRate(iterInput);
          /**
           *  Number of operation firings for convolutional and pooling neurons equals to
           *  output height after the full image processing.*/
          int itersNum = calcRepetitionsNum(neuron,minH);

          /** As first iteration is added
           *  with firstIterRate, number of rest iterations = itersNum - 1;
           *
           */
          if(itersNum > 1) {
              if(rate==firstRate.getFirst())
                  firstRate.setSecond(itersNum);
              else {
                  IndexPair restIters = new IndexPair(rate, itersNum - 1);
                  rates.add(restIters);
              }
          }

          _processDataTail(rates,neuron.getInputDataFormat());
          /** remove zero-readings*/
          if(rates.lastElement().getFirst()==0)
              rates.removeElementAt(rates.size()-1);
          return rates;
    }

     /**
     * move dummy data production from input data flow to internal data flow:
     * processes mismatching of required input data size and real produced input data size
     * @param rates input rates, calculated for an input
     * @param realInput real input tensor
     */
    private boolean _processDataTail(Vector<IndexPair> rates, Tensor realInput){
        int sumRate = 0;
        for(IndexPair rate: rates)
            sumRate += rate.getFirst() * rate.getSecond();

        int inpTokens = realInput.getElementsNumber();
        int dif = sumRate - inpTokens;

        if(dif==0)
            return false;

        /** crop last input data chunk*/
        if(dif>0)
            _processPositiveDataTail(rates,dif);
        else
            _processNegativeDataTail(rates,Math.abs(dif));

        return true;
    }

    /**
     * Process data tail in case, when consumer tries to read more data,
     * Then producer can provide
     * @param rates current port rates
     * @param dif sum port rate - input tokens;
     */
    private void _processPositiveDataTail(Vector<IndexPair> rates, int dif){
         IndexPair dataTail;
         int difToProcess = dif;
         /** split last input*/
            IndexPair lastInput;
            int lastinputRate;
            int zeroratesNum = 0;

           while (difToProcess>0) {
             /** decrement last input*/
               lastInput = rates.lastElement();
               lastinputRate = lastInput.getFirst();
               if (dif >= lastInput.getFirst()) {
                   if(lastInput.getSecond()==1)
                       rates.remove(lastInput);
                   else
                       lastInput.setSecond(lastInput.getSecond()-1);
                   zeroratesNum++;
               }

               /** split last input*/
               else {
                   if (lastInput.getSecond() > 1) {
                       /** split up vector*/
                       lastInput.setSecond(lastInput.getSecond() - 1);
                       dataTail = new IndexPair(lastinputRate, 1);
                       rates.add(dataTail);
                   } else
                       dataTail = lastInput;
                   dataTail.setFirst(lastinputRate - dif);
               }
               difToProcess-=lastinputRate;
           }

           if(zeroratesNum>0)
               rates.add(new IndexPair(0,zeroratesNum));
    }

    /**Process data tail in case, when consumer reads more data,
     * Then it needs for the execution
     * @param rates current port rates
     * @param dif sum port rate - input tokens;
     */
    private void _processNegativeDataTail(Vector<IndexPair> rates, int dif){
        int difToRemove = dif;
        int zeroratesNum = 0;
        IndexPair lastRatePair;
        /** decrement last rate and replace it by zero-rate*/
        while (difToRemove>0 && rates.size()>0) {
            lastRatePair = rates.lastElement();
            difToRemove -= lastRatePair.getFirst();

            if (lastRatePair.getSecond() == 1)
                rates.remove(lastRatePair);
            else
                lastRatePair.setSecond(lastRatePair.getSecond() - 1);
            zeroratesNum++;
        }
        if(difToRemove<0)
            _processPositiveDataTail(rates,Math.abs(dif));
        rates.add(new IndexPair(0,zeroratesNum));
    }

    /**
     * Calculate number of node repetitions. For every neuron,
     * transformed into CSDF Node, number of repetitions is
     * determined by number of specific function calls inside of the neuron
     */
    private int calcRepetitionsNum(Neuron neuron, int minOutputHeight){
        if(neuron instanceof GenericNeuron)
            return ((GenericNeuron) neuron).getInternalStructure().getOutputLayer().getNeuron().getFuncCallsNum(minOutputHeight);
        return neuron.getFuncCallsNum(minOutputHeight);
    }

    /**
     * Calculate self-loop rate, if any
     * TODO for custom connection number of channels is always max and =
     * TODO connection src neurons num
     * @param layer DNN model layer
     * @param consVector corresponding node consumption rate
     * @return vector of IndexPairs, describing the self-loop rate or null
     */
    private Vector<IndexPair> _getSelfLoopRate(Layer layer,Connection connection, Vector<IndexPair> consVector){
        Vector<IndexPair> rates = _getSelfLoopRate(layer.getNeuron(),consVector);
        if(rates==null)
            return null;

        int channels_num = connection.getChannels();
        if(connection instanceof Custom){
            channels_num = connection.getSrc().getNeuronsNum();

        }
        for(IndexPair rate: rates){
            rate.setFirst(rate.getFirst()*channels_num);
        }
        return rates;
    }


    /**
     * Calculate self-loop rate, if any
     * For now self-loops are calculated only for convolutional/pooling neurons
     * @param neuron DNN model neuron
     * @param consVector corresponding node consumption rate
     * @return vector of IndexPairs, describing the self-loop rate or null
     */
    private Vector<IndexPair> _getSelfLoopRate(Neuron neuron, Vector<IndexPair> consVector){
        if(neuron instanceof CNNNeuron){
            return _getSelfLoopRate((CNNNeuron) neuron,consVector);
        }
        return null;
    }

    /**
     * Calculate self-loop rate of convolutional/pooling neuron , if any
     * @param neuron DNN model convolutional/pooling neuron
     * @param consVector corresponding node consumption rate
     * @return vector of IndexPairs, describing the self-loop rate or null
     */
    private Vector<IndexPair> _getSelfLoopRate(CNNNeuron neuron, Vector<IndexPair> consVector){
        if(consVector.size()<2)
            return null;

        int overlapH = neuron.getKernelH()-neuron.getStride();
        if(overlapH==0)
            return null;

        Vector<IndexPair> selfLoopRate = new Vector<>();
        Tensor overlapTensor = new Tensor(neuron.getInputDataFormat());
        overlapTensor.setDimSize(1,overlapH);
        int overlapRate = calcRate(overlapTensor);

        /** for all other iterations, overlapping data is sent back*/
        int selfItersNum = 0;

        for(int i = 1; i < consVector.size(); i++)
            selfItersNum += consVector.elementAt(i).getSecond();
        selfLoopRate.add(new IndexPair(overlapRate,selfItersNum));

        /**
         * last iteration of conv/overlapped pooling
         *  self-loops does not process any data
         *  */
        selfLoopRate.add(new IndexPair(0,1));
        return selfLoopRate;
    }

    /** get graph*/
    public CSDFGraph getCSDFG() {
        return _CSDFG;
    }



    ///////////////////////////////////////////////////////////////////
    ////           GENERIC NODES PROCESSING                        ///

     /**
     * Refine generic node by 'transferring' its internal structure
     * TODO: for it ia assumed that CNN layers always
     * TODO have 0 or 1 input connections, as they are mot multiple inputs processors
     * @param genericMinimized generic neuron with minimized data flow
     * @param CSDFNode corresponding sdf node
     **/
    private void _refineGenericNode(GenericNeuron genericFull,GenericNeuron genericMinimized, CSDFNode CSDFNode, Connection blockEntry){
         _addHiddenOverlappingEdges(genericFull,genericMinimized,CSDFNode,blockEntry);
         _addHiddenNonOverlappingEdges(genericFull,genericMinimized,CSDFNode);
    }

    /**
     * Add connections between internal elements
     * @param genericMinimized generic neuron with minimized data flow
     * @param genericNode corresponding sdf node
     */
    private void _addHiddenNonOverlappingEdges(GenericNeuron genericFull,GenericNeuron genericMinimized, CSDFNode genericNode){
        Network subNetwork = genericFull.getInternalStructure();
        Network minimizedSubNetwork = genericMinimized.getInternalStructure();

        Neuron l2SampleNeuronMinimized;
       // Neuron l2SampleNeuron;
        Neuron l1SampleNeuronMinimized;
        Connection minimizedConnection;

        for(Connection connection:subNetwork.getConnections()) {
            minimizedConnection = minimizedSubNetwork.findConnection(connection.getSrcId(),connection.getDestId());

            l1SampleNeuronMinimized = minimizedConnection.getSrc().getNeuron();
            l2SampleNeuronMinimized = minimizedConnection.getDest().getNeuron();

            /** port assigned memory description: calculated as source min output memory*/
            String muName = connection.getSrcName()+"_"+connection.getDestName();
            String dataType = _minimizedDFDNN.getDataType();
            MemoryUnit srcMinOutMemory = new MemoryUnit(muName,connection.getSrc().getOutputFormat(),dataType);
            MemoryUnit dstMinInMemory = new MemoryUnit(muName,connection.getDest().getInputFormat(),dataType);
            int lastEdgeId = _CSDFG.getNextEdgeId();
            String edgeName = genericNode.getName()+"_self_"+lastEdgeId;

            Vector<IndexPair> srcOutputRate = calcOutputRates(connection.getSrc(),l1SampleNeuronMinimized.getOutputDataFormat());

            int channels = connection.getSrc().getNeuronsNum();
            Vector<IndexPair> dstInputRate = calcInputRates(connection.getDest(), l2SampleNeuronMinimized.getInputDataFormat(),channels);

            CSDFEdge edge = _createEdge(edgeName, lastEdgeId, genericNode, genericNode, srcOutputRate, dstInputRate, srcMinOutMemory, dstMinInMemory);


            /**TODO REFACTORING
             * inherit rate for multiple input processors.
             * Consistency checkout of summary I/O rate for multiple input processors
             * should be done in Network model

            if (l2SampleNeuron instanceof MultipleInputsProcessor) {
                edge.getDst().setRates(edge.getSrc().getRates());
            }

            if (l2SampleNeuron instanceof GenericNeuron) {
                _refineGenericNode((GenericNeuron) l2SampleNeuron, (GenericNeuron) l2SampleNeuronMinimized, endNode, connection);
            }
                       */

            /** TODO CHECK*/
            String operation = l2SampleNeuronMinimized.getFunctionCallDescription(connection.getDstInputsNum());
           // g.setOperation(operation);
           // int dstOperationRepetitions = l2SampleNeuronMinimized.getOperationsNumber(connection.getDest().getNeuronsNum());
           // endNode.setOperationRepetitionsNumber(dstOperationRepetitions);

            /** create weights description for nodes with weights*/
          //  MemoryUnit dstWeights = _createDstWeightsDescriptionLB(connection);
          //  if (dstWeights != null)
            //    endNode.addMemoryUnit(dstWeights);
       // }

                      _CSDFG.addEdge(edge);
        }


    /**    for(Connection connection:subNetwork.getConnections()){
            l1SampleNeuronMinimized = connection.getSrc().getNeuron();
            l2SampleNeuronMinimized = connection.getDest().getNeuron();

            /** port assigned memory description
            String muName = connection.getSrcName()+"_"+connection.getDestName();
            String dataType = _minimizedDFDNN.getDataType();
            MemoryUnit srcMinOutMemory = new MemoryUnit(muName,connection.getSrc().getOutputFormat(),dataType);
            MemoryUnit dstMinInMemory = new MemoryUnit(muName,connection.getDest().getInputFormat(),dataType);

            int edgeId = _CSDFG.getNextEdgeId();
            String edgeName = genericNode.getName()+"_self_"+edgeId;

            /** rates
            Vector<IndexPair> srcOutputRate = calcOutputRates(connection.getSrc(),l1SampleNeuronMinimized.getOutputDataFormat());
            /** TODO tune for sparsed connections - number of inputs may be different for nodes inside, so data flow will differ
            Vector<IndexPair> dstInputRate = calcInputRates(connection.getDest(),l2SampleNeuronMinimized.getInputDataFormat(),connection.getSrc().getNeuronsNum());

            CSDFEdge edge = _createEdge(edgeName,edgeId,genericNode,genericNode,srcOutputRate,dstInputRate, srcMinOutMemory,dstMinInMemory);

            _CSDFG.addEdge(edge);
        // CSDFEdge selfEdge = _createSelfEdge(edgeName,edgeId,genericNode, selfLoopRate,dstNeuronMinimized.getInputDataFormat(),_minimizedDFDNN.getDataType(),false);
        }
        */

    }


    /**
     * Add hidden overlapping-processors of the Generic block
     * @param genericMinimized Generic block with minimized data flow
     * @param CSDFNode corresponding sdf node
     * @param blockEntry entry to the Generic block from the main dnn
     */
    private void _addHiddenOverlappingEdges(GenericNeuron genericFull, GenericNeuron genericMinimized, CSDFNode CSDFNode, Connection blockEntry){
       Network subNetwork = genericMinimized.getInternalStructure();
       for(Layer layer:subNetwork.getLayers()){
           if(layer.getNeuron() instanceof CNNNeuron){
               /** If a layer is an input layer of generic block,
                *  it has a 'parent'- connection from outside of the block,
                *  where 'parent'-connection is the one, writing new data to the
                *  overlapping buffer
                */
               if(layer.getId()==subNetwork.getInputLayerId()){
                   _appendInternalSelfEdge(blockEntry,subNetwork.getInputLayer().getNeuron(),CSDFNode,null);
               }

               /** otherwise, layer is linked only to the internal connections
                * TODO search for the corresponding internal connection buffer mem unit??
                * */
               Vector<Connection> inputs = subNetwork.getLayerInputConnections(layer);
               if(inputs.size()==1) {
                   Connection singleInput = inputs.firstElement();
                       _appendInternalSelfEdge(singleInput,singleInput.getDest().getNeuron(),CSDFNode,null);
                }
           }

       }
    }

    /**
     * Chech is csdf edge is a self-edge by
     * comparing node ids in edge src and dst ids
     * @param edge edge to be checked
     * @return true, if edge is a self-edge and false otherwise
     */
    private boolean isSelfEdge(CSDFEdge edge){
        int[] srcId = edge.getSrcId();
        int[] dstId = edge.getDstId();
        if(srcId[0]==dstId[0])
            return true;
        return false;
    }

    private void _appendInternalSelfEdge(Connection connection,Neuron dstNeuronMinimized, CSDFNode genericNode, CSDFEdge parentEdge){
         Vector<IndexPair> dstInputRate = calcInputRates(connection.getDest().getNeuron(),dstNeuronMinimized.getInputDataFormat());
         Vector<IndexPair> selfLoopRate = _getSelfLoopRate(dstNeuronMinimized,dstInputRate);
         if(selfLoopRate == null)
             return;
         int edgeId = _CSDFG.getNextEdgeId();
         CSDFEdge selfEdge;
         String edgeName = genericNode.getName()+"_self_"+edgeId;
         if(parentEdge!=null)
            selfEdge = _createOverlapProcessingSelfEdge(edgeName,edgeId,genericNode,selfLoopRate,parentEdge);
         else
             selfEdge = _createSelfEdge(edgeName,edgeId,genericNode, selfLoopRate,dstNeuronMinimized.getInputDataFormat(),_minimizedDFDNN.getDataType(),true);
         _CSDFG.addEdge(selfEdge);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///

    /** Neural network with minimized dataFlow*/
    private Network _minimizedDFDNN;

    /**  current CSDF graph*/
    private CSDFGraph _CSDFG;

    /** conversion mode: layer-based or neuron-based. By default: layer-based*/
    private DNNInitRepresentation _conversionMode = DNNInitRepresentation.LAYERBASED;

    /** Dnn to CSDF refiner refines CSDF nodes with specific features of DNN model*/
    CSDFGMemoryRefiner _refiner = new CSDFGMemoryRefiner();

    /** generic nodes processor*/
 //   CNN2CCSDFGraphConverter _subgraphProcessor = new CNN2CCSDFGraphConverter();
}
