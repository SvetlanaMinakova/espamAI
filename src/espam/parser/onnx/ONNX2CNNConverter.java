package espam.parser.onnx;

import com.google.protobuf.*;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.neurons.arithmetic.Arithmetic;
import espam.datamodel.graph.cnn.neurons.neurontypes.*;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.NoneTypeNeuron;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.cnn.neurons.transformation.Transpose;
import espam.datamodel.graph.cnn.neurons.transformation.Upsample;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.interfaces.python.ONNXWeightsExtractor;
import espam.operations.evaluation.CSDFGMemoryRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;
import onnx.ONNX;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;


/**
 * Implements convertation from ONNX model to CNN model
 */
public class ONNX2CNNConverter {

    /**
     * Default ONNX2CNN converter if private, because of the interface method
     * for conversion is convertModel
     */
    private ONNX2CNNConverter() {}

    ///////////////////////////////////////////////////////////////////
    ////                        convert elements                   ///

         /**
     * Convert ONNX model to espam.Network model
     * @param model ONNX DNN model
     * @return espam.Network model, corresponding to provided ONNX DNN model
     */
    public static Network convertModel(ONNX.ModelProto model, String modelPath, String saveWeightsPath, String outModelName, boolean verbose){
        ONNX2CNNConverter converter = new ONNX2CNNConverter();

        if(saveWeightsPath!=null && modelPath!=null) {
            converter.setWeightsSavePath(saveWeightsPath);
            converter.setSaveWeights(true);
            converter.setModelPath(modelPath);
            converter.setSaveWeightsVerbose(verbose);
        }
        else {System.err.println(" incorrect parameters for weights saving: mode path: "+modelPath +
                ", weights dir: "+saveWeightsPath + " . Weights would not be extracted.");}

            try{
                converter._outModelName = outModelName;
                Network resultNetwork = converter.convertGraph(model);
                ONNXtoESPAMNetworkTraverser traverser = new ONNXtoESPAMNetworkTraverser(resultNetwork,converter);
                Vector<Integer> layersTraverseOrder = traverser.getLayersTraverseOrder();
              //  for(int lId : layersTraverseOrder)
                //    System.out.println("("+lId+")");//+resultNetwork.getLayer(lId).getName());
                traverser.setConnections(layersTraverseOrder);
                traverser.setDataFormats(layersTraverseOrder);
                converter.removePureDataLayers(resultNetwork);
               // if(converter._giveReadableNames)
                 //   resultNetwork.giveLayersReadableNames();
                resultNetwork.giveIOLayersStandardNames();
                return resultNetwork;
            }

             catch (Exception e){
                System.err.println("Model conversion failed "+e.getMessage());
                return null;
            }

    }


     /**
     * Convert ONNX model to espam.Network model
     * @param model ONNX DNN model
     * @return espam.Network model, corresponding to provided ONNX DNN model
     */
    public static Network convertModel(ONNX.ModelProto model, String outModelName)
        {   ONNX2CNNConverter converter = new ONNX2CNNConverter();

            try{
                converter._outModelName= outModelName;
                Network resultNetwork = converter.convertGraph(model);
                ONNXtoESPAMNetworkTraverser traverser = new ONNXtoESPAMNetworkTraverser(resultNetwork,converter);
                Vector<Integer> layersTraverseOrder = traverser.getLayersTraverseOrder();
              //  for(int lId : layersTraverseOrder)
                //    System.out.println("("+lId+")");//+resultNetwork.getLayer(lId).getName());
                traverser.setConnections(layersTraverseOrder);
                traverser.setDataFormats(layersTraverseOrder);
                converter.removePureDataLayers(resultNetwork);
                resultNetwork.giveIOLayersStandardNames();

                return resultNetwork;
            }

             catch (Exception e){
                System.err.println("Model conversion failed "+e.getMessage());
                return null;
            }
        }

        /**
     * Convert ONNX model graph to espam.Network graph and extract
     * from ONNX model intermediate data, required for full model conversion
     * information from it
     * @param model ONNX model
     */
    private Network convertGraph(ONNX.ModelProto model) throws Exception {
        if (model == null)
            throw new Exception("null ONNX model");

        this._onnxModelGraph = model.getGraph();

        if (_onnxModelGraph == null)
            throw new Exception("null ONNX model graph");

        _generateOutModelname();

        Network resultNetwork = new Network(_outModelName);

        initIntermediateConversionParameters();

        processONNXModelInputs(_onnxModelGraph,resultNetwork);
        processONNXModelOutputs(_onnxModelGraph,resultNetwork);

        /**Process ONNX Graph Nodes*/
        for(ONNX.NodeProto node: _onnxModelGraph.getNodeList()){
            if(!_uniqueNamedONNXNodes.containsValue(node)) {
                String nonEmptyName = generateNonEmptyName(node);
                _uniqueNamedONNXNodes.put(node, nonEmptyName);
                try {
                    processONNXNode(node, resultNetwork);
                }
                catch (Exception e){
                    System.err.println(_uniqueNamedONNXNodes.get(node) +" processing fault ");
                    return resultNetwork;
                }
            }
        }

        if(_saveWeights)
            _saveParametersAsNPYArrays(resultNetwork.getName());
        resultNetwork.setWeightsType(_modelWeightsType);
        resultNetwork.setDataType(_modelDataType);

        return resultNetwork;
    }

    /** Generate output DNN model name */
    private void _generateOutModelname (){
        if(_outModelName!=null)
            return;

        String graphName = _onnxModelGraph.getName();
        if(graphName==null) _outModelName = "network1";

        else _outModelName = graphName;
    }

    /**Save parameters as numpy arrays
     * @param modelName name of the model
     */

    private void _saveParametersAsNPYArrays(String modelName) {
      /** add file extension if not mentioned*/
      try {
          String metaFileDir = _weightsSavePath;
          String weightsDir =  _weightsSavePath + File.separator + "weights_npz";
          String metadataFileName = "inits_metadata";
          String metadataFilePath = metaFileDir + File.separator + metadataFileName + ".json";
          _saveParametersAsJSONMetaData(metaFileDir, metadataFileName);
          boolean weightsExtracted =  _weightsExtractor.extractWeights(_modelPath,metadataFilePath,weightsDir,_saveWeightsVerbose);
          if(!weightsExtracted)
              System.err.println("Weights extraction error for: " + modelName + ". Python script error.");
          }

      catch (Exception e){
          System.err.println("Weights extraction error: " + e.getMessage());
      }
  }


   /**
     * Converts ONNX Tensor to espam.Tensor by changing the dimensions order
     * @param onnxTensor ONNX Tensor
     * @return corrsponding espam.Tensor
     */
   public static Tensor convertTensor(ONNX.TensorProto onnxTensor) {
       Tensor resultTensor = new Tensor();
       for(int i=0; i<onnxTensor.getDimsCount();i++)
           resultTensor.addDimension((int) onnxTensor.getDims(i));

       resultTensor = convertDimsOrder(resultTensor);
       return resultTensor;
   }

    /**
     * Converts ONNX TensorShape to espam.Tensor by changing the dimensions order
     * @param onnxTensorShape ONNX TensorShape
     * @return corrsponding espam.Tensor
     */
   public static Tensor convertTensor(ONNX.TensorShapeProto onnxTensorShape)
   { Tensor resultTensor = new Tensor();
        for(int i=0; i<onnxTensorShape.getDimCount();i++)
            resultTensor.addDimension((int) onnxTensorShape.getDim(i).getDimValue());

        resultTensor = convertDimsOrder(resultTensor);
        return resultTensor;
   }

    /**
     * Converts BoundaryMode from ONNX to internal format
     * ONNX have only 3 allowed modes: SAME_UPPER, SAME_LOWER and VALID
     * VALID means no padding and equal to internal VALID,
     * SAME_UPPER and SAME_LOWER are both SAME, but differs on
     * where padding is located (top or bottom of input data)
     * @param boundaryMode string ONNX description of boundary mode
     * @return Boundary mode in internal format
     */
    public static BoundaryMode convertBoundaryMode(String boundaryMode){
    if(boundaryMode.equals("VALID"))//||boundaryMode.equals("SAME_LOWER"))
        return BoundaryMode.VALID;
    //if(boundaryMode.equals("NOTSET"))
      //  return BoundaryMode.VALID;
    else
        return BoundaryMode.SAME;
    }

   /**
     * Searches for ONNX graph node in layersMapping
     * @param nodeProto ONNX graph node
     * @return corresponding espam.cnn layer or null
     */
    public Layer findEspamLayerInMapping(ONNX.NodeProto nodeProto){
        for(Map.Entry entry: _layersMapping.entrySet()) {
            if (entry.getValue().equals(nodeProto))
                    return (Layer) entry.getKey();

            }
        return null;
    }

    /**
     * Searches for ONNX.NodeProto in layersMapping
     * @param layer espam.cnn layer
     * @return corresponding ONNX graph node or null
     */
    public ONNX.NodeProto findONNXNodeInMapping(Layer layer){
        try{return (ONNX.NodeProto) _layersMapping.get(layer);}
        catch (Exception ex){
            System.err.println("No ONNX.NodeProto mapping found for layer "+layer);
            return null;
        }
    }

    /**
     * Searches for ONNX.NodeProto in Operational Nodes list
     * @param layer espam.cnn layer
     * @return corresponding ONNX graph node or null
     */
    public ONNX.NodeProto findONNXOperationalNode(Layer layer){
        try{return (ONNX.NodeProto) _operationalNodes.get(layer.getName());}
        catch (Exception ex){
            System.err.println("No ONNX.NodeProto mapping found for layer "+layer);
            return null;
        }
    }

     /**
     * find ONNX attribute by name
     * @param node node - owner of the attribute
     * @param name name of the node attribute
     * @return ONNX attribute, found by name or NULL if node was not found
     */
    public static ONNX.AttributeProto getONNXAttribute (ONNX.NodeProto node, String name){
        Iterator<ONNX.AttributeProto> i;
        i = node.getAttributeList().iterator();
        while( i.hasNext() ) {
            ONNX.AttributeProto attr = i.next();
            if( attr.getName().equals(name) ) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Get connection source node
     * @param connectionName name of the connection
     * @return connection source node, if connection was found and null otherwise
     */
    public Layer findConnectionSrc(String connectionName,Network network){
        /** if it is an input connection*/
        if(_ILayersNames.contains(connectionName)||_OLayersNames.contains(connectionName))
            return network.getLayer(connectionName);

        ONNX.NodeProto onnxConnectionSrc = _operationalNodesOutputs.get(connectionName);
        if(onnxConnectionSrc!=null)
            return findEspamLayerInMapping(onnxConnectionSrc);

        return null;
    }

     /**
     * Get connection source node
     * @param connectionName name of the connection
     * @return connection source node, if connection was found and null otherwise
     */
    public Layer findConnectionDst(String connectionName,Network network){
        /** if it is an input connection*/
        if(_ILayersNames.contains(connectionName)||_OLayersNames.contains(connectionName))
            return network.getLayer(connectionName);

        ONNX.NodeProto onnxConnectionDst = _operationalNodesInputs.get(connectionName);
        if(onnxConnectionDst!=null)
            return findEspamLayerInMapping(onnxConnectionDst);

        return null;
    }

     /**
     * Get ONNX.NodeProto inputs for corresponding espam.Layer, using information about mapping
     * @param layer espam Layer to be inspected
     * @return ONNX.NodeProto inputs for corresponding espam.Layer
     */
   public Vector<String> getLayerInputs(Layer layer){
       Vector<String> inputs = new Vector<>();

       /** do not process input layers*/
        if(getILayersNames().contains(layer.getName()))
            return inputs;

        /** TODO process output nodes separately*/
         if(getOLayersNames().contains(layer.getName()))
            return inputs;


        /** search in operational nodes (espam.Network layers) */
        ONNX.NodeProto node = findONNXNodeInMapping(layer);

        if (node != null) {
            for(String input: node.getInputList())
                inputs.add(input);

            return inputs;
        }

        System.err.println("Layer "+layer.getName()+ " not found in the mapping");
        return inputs;

   }


    /**
     * print attributes of ONNX.NodeProto
     * @param node
     */
    public static void printAttributes(ONNX.NodeProto node){
        for(ONNX.AttributeProto attr: node.getAttributeList()){
            System.out.println(attr.getName() + " ,type: "+attr.getType()+", values: ");

            if(attr.hasI())
                System.out.print(attr.getI()+",");

            for(Long val:attr.getIntsList())
                    System.out.print(val+",");

            if(attr.hasF()) {
                System.out.print(attr.getF() + ",");
            }


            for(float val:attr.getFloatsList())
                System.out.print(val+",");


            if(attr.hasS()) {
                System.out.print(attr.getS()+",");
            }

            for(ByteString val:attr.getStringsList())
                    System.out.print(val+",");


            if(attr.hasG()) {
                System.out.print(attr.getG());
            }

            for (ONNX.GraphProto graph: attr.getGraphsList())
                System.out.print(graph);


        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                public getters and setters                  ///

    public Vector<String> getILayersNames() { return _ILayersNames; }

    public Vector<String> getOLayersNames() { return _OLayersNames; }

    public HashMap<String, ONNX.NodeProto> getOperationalNodes() { return _operationalNodes; }

    public HashMap<String, ONNX.NodeProto> getParameterNodes() { return _parameterNodes; }

    public HashMap<String, ONNX.NodeProto> getDataParameterNodes() { return _dataParameterNodes; }

    public HashMap<Layer, GeneratedMessage> getLayersMapping() { return _layersMapping; }

    public HashMap<String, ONNX.NodeProto> getOperationalNodesOutputs() { return _operationalNodesOutputs; }

    public HashMap<String, ONNX.TensorProto> getOnnxModelInitializers() { return _onnxModelInitializers; }

    public HashMap<String, Tensor> getExtractedDataFormats() { return _extractedDataFormats; }

    public Tensor getExtractedDataFormat(String dataSrcName) {
        return _extractedDataFormats.get(dataSrcName);
    }

    /**
     * Get all fixed pads
     * @return all fixed pads of ONNX graph nodes
     */
    public HashMap<String,int[]> getPads() { return _pads; }

    /**
     * Get pads for ONNX node with specified name or default pads
     * Default pads are [0,0,0,0]
     * @param ownerName name of the ONNX node
     * @return  pads for ONNX node with specified name or default pads
     */
    private int[] getPads(String ownerName){
        int [] pads = _pads.get(ownerName);
        if(pads==null) {
            int[] default_pads = {0, 0, 0, 0};
            return default_pads;
        }
        return pads;
    }

     /**
     * Set pads for ONNX node with specified name
     * Default pads are [0,0,0,0]
     * @param ownerName name of the ONNX node
     * @return  pads for ONNX node with specified name or default pads
     */
    private void setPads(String ownerName, int[] pads){
        if(pads!=null)
            _pads.put(ownerName,pads);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                     ///

    /**
     * Initialize all intermediate parameters, required for DNN model conversion
     */
    private void initIntermediateConversionParameters(){
        /**Operational nodes are nodes that will be translated into hidden Layers of internal Network model*/
        _operationalNodes = new HashMap<String, ONNX.NodeProto>();

        /**Parameter nodes like weights and input arguments are used for extracting the additional information*/
        _parameterNodes = new HashMap<String, ONNX.NodeProto>();

        /** Const data-containing parameter nodes */
        _dataParameterNodes = new HashMap<String, ONNX.NodeProto>();

        /** data formats, extracted from ONNX model*/
        _extractedDataFormats = new HashMap<>();

        /**Outputs of nodes in ONNX.Model. Used for connections restore*/
        _parameterNodesOutputs = new HashMap<String, ONNX.NodeProto>();
        _operationalNodesOutputs = new HashMap<String, ONNX.NodeProto>();
        _operationalNodesInputs = new HashMap<>();
        _ILayersNames = new Vector<String>();
        _OLayersNames = new Vector<>();
        _onnxNodesOutputs = new HashMap<String, String>();

        /** Mapping of ONNX nodes onto espam.cnn Layers*/
        _layersMapping = new HashMap<>();

        /** unique named and unified ONNX Nodes*/
        _uniqueNamedONNXNodes = new HashMap<>();
        /** resolving of multiple inputs of ONNX.MatMul and GEMM nodes
         *  while transferring to DenseBlock*/
        _denseInputs = new HashMap<>();

          /** List of all nodes that contain weights*/
        _nodesWithWeights = new Vector<String>();

        /** list of operational nodes, contains data*/
         _dataParameterNodesOutputs = new HashMap<>();

         /** weights initializers: <operational node name,weights initializer name> */
         _convWeightsInits = new HashMap<>();
         _denseWeightsInits = new HashMap<>();

         /** bias initializers: <operational node name, bias initializer name> */
         _biasInits = new HashMap<>();

         /** weights nodes: <operational node name,weights node name> */
         _convWeightsNodeInits = new HashMap<>();
         _denseWeightsNodeInits = new HashMap<>();

         /** bias nodes: <operational node name, bias node name> */
         _biasNodeInits = new HashMap<>();

         /** number of neurons in dense blocks*/
         _denseNeurons = new HashMap<>();

         /** batchNormalization initializers*/
         _bnInits = new HashMap<>();
    }

    /**
     * Process ONNX model inputs
     * ONNX model inputs contains following data parameters:
     *  - DNN input data
     *  - Layers constants inputs
     *  - Links on external constant data sources
     *  This types are proceed separately
     * @param onnxGraph DNN as onnx GraphProto
     * @param resultNetwork DNN as espam Network
     * @throws Exception if an error occurs
     */
    private void processONNXModelInputs(ONNX.GraphProto onnxGraph, Network resultNetwork) throws Exception{
        /**
         * Extract ONNX graph inputs : both model input and model input parameter nodes
         * */
        _onnxGraphInputs = onnxGraph.getInputList();

        for(ONNX.ValueInfoProto dataInput: _onnxGraphInputs) {
            String inputName = generateNonEmptyName(dataInput);
            _onnxNodesOutputs.put(inputName,inputName);
        }

         /**ONNX initializers are links on output sources of model constant data
         * and parameters*/
         processInitializers(onnxGraph);

        /**select single model input. Model input is a graph input,
         * but not an initializer. For now only single model input is allowed
         * */
           Vector<ONNX.ValueInfoProto> modelInputs = new Vector<>();

        for(ONNX.ValueInfoProto onnxGraphInput: _onnxGraphInputs){
            if(!_onnxModelInitializers.containsKey(onnxGraphInput.getName())) {
                modelInputs.add(onnxGraphInput);
            }
        }

        if (modelInputs.size()==1) {
            ONNX.ValueInfoProto singleModelInput = modelInputs.elementAt(0);
            String inputNodeName = generateNonEmptyName(singleModelInput);
            _uniqueNamedONNXNodes.put(singleModelInput,inputNodeName);
            appendInputLayer(singleModelInput, resultNetwork);
            _ILayersNames.add(inputNodeName);
            _extractDataTypeFromInputNode(singleModelInput);
        }

        else {
            System.err.println("Wrong DNN model inputs number: "+modelInputs.size() +" .DNN model should have single input");
            throw new Exception("Input extraction exception");
        }
    }


     /**
     * Process ONNX model outputs
     * ONNX model output should be defined in ONNX.GraphProto outputs
     * For now only one DNN output is allowed
     * @param onnxGraph DNN as onnx GraphProto
     * @param resultNetwork DNN as espam Network
     * @throws Exception if an error occurs
     */
    private void processONNXModelOutputs(ONNX.GraphProto onnxGraph, Network resultNetwork) throws Exception{
        _onnxGraphOutputs = onnxGraph.getOutputList();

        if (_onnxGraphOutputs.size()==1) {
            ONNX.ValueInfoProto singleModelOutput = _onnxGraphOutputs.get(0);
            String outputName = generateNonEmptyName(singleModelOutput);
      //      String outputName = generateSpecialName(singleModelOutput,"_output_");
          //  System.out.println(outputName);
           // _onnxGraphOutputsNames.add(graphOutputname);
            _uniqueNamedONNXNodes.put(singleModelOutput,outputName);

            appendOutputLayer(singleModelOutput, resultNetwork);

            _OLayersNames.add(outputName);

        }
        else {
            System.err.println("Wrong DNN model outputs number: "+_onnxGraphOutputs.size() +
                    " .DNN model should have single output");
            throw new Exception("Output extraction exception");
        }
    }


    /**
     * Extractes and processes initializers of ONNX Model
     * ONNX initializers are links on output sources of model constant data
         * and parameters. They are us
         * add initializer nodes to Param nodes, if there are any
         * In some cases data Param Nodes could be placed into initializers list
         * */
    private void processInitializers(ONNX.GraphProto onnxGraph){
         _onnxModelInitializers = new HashMap<>();

        for(ONNX.TensorProto initializer: onnxGraph.getInitializerList()){
            String initializerName = generateNonEmptyName(initializer);
            _uniqueNamedONNXNodes.put(initializer, initializerName);
           _onnxModelInitializers.put(initializerName,initializer);
           Tensor extractedDataFormat = ONNX2CNNConverter.convertTensor(initializer);
           saveExtractedDataFormat(initializerName,extractedDataFormat);
        }
    }

     /**
     * Extracts espam.cnn Neuron from ONNX.NodeProto and add it to the target DNN
     * @param node source ONNX.NodeProto
     * @param espamDNN target DNN
     * @return espam.cnn Neuron, specified according to the internal model
     */
    private void processONNXNode(ONNX.NodeProto node, Network espamDNN){

        String neuronType = node.getOpType();

        if(neuronType.equals("Add")) {
            appendAddLayer(node,espamDNN);
            processOperationalNode(node);
           _extractParamsMetaData(node,false,true);
            return;
        }

        if(neuronType.equals("AveragePool")) {
            appendPoolingLayer(node,PoolingType.AVGPOOL,espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("BatchNormalization")) {

            appendNonLinearLayer(node, NonLinearType.BN, espamDNN);
            _extractBNParamsMetaData(node);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Conv")) {
             appendConvLayer(node,espamDNN);
             processOperationalNode(node);
              _extractParamsMetaData(node,true,true);
            return;
        }

         if(neuronType.equals("Concat")) {
            appendConcatLayer(node, espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Constant")) {
            processParamNode(node);
            Tensor extractedDataFormat = extractDataAttributeFromConstNode(node);
            saveExtractedDataFormat(node,extractedDataFormat);
            return;
        }

        if(neuronType.equals("Div")) {
            appendNonLinearArithmeticLayer(node, NonLinearType.DIVConst, espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Dropout")) {
            appendNonLinearLayer(node, NonLinearType.DROPOUT, espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Gemm")){
           appendGemmLayer(node,espamDNN);
           processOperationalNode(node);
           processDenseNode(node);
           _extractParamsMetaData(node,true,true);

          return;
        }

        if(neuronType.equals("GlobalAveragePool")) {
             appendPoolingLayer(node,PoolingType.GLOBALAVGPOOL,espamDNN);
             processOperationalNode(node);
             return;
        }


        if(neuronType.equals("GlobalLpPool")) {
            appendPoolingLayer(node,PoolingType.GLOBALLPPOOL,espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("GlobalMaxPool")) {
            appendPoolingLayer(node,PoolingType.GLOBALMAXPOOL,espamDNN);
             processOperationalNode(node);
            return;
        }


        if(neuronType.equals("ImageScaler")) {
            appendNonLinearLayer(node, NonLinearType.ImageScaler, espamDNN);
            processOperationalNode(node);
            return;
        }


         if(neuronType.equals("LeakyRelu")) {
            appendNonLinearLayer(node, NonLinearType.LeakyReLu,espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("LRN")){
           appendLRNLayer(node,espamDNN);
           processOperationalNode(node);
           return;
        }


        if(neuronType.equals("MaxPool")) {
            appendPoolingLayer(node,PoolingType.MAXPOOL,espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("MatMul")){
           appendMatMulLayer(node,espamDNN);
            processOperationalNode(node);
            processDenseNode(node);

           _extractParamsMetaData(node,true,false);
           return;
        }

        if(neuronType.equals("Mul")) {
            appendMulLayer(node,espamDNN);
            processOperationalNode(node);
           _extractParamsMetaData(node,false,true);

            return;
        }

        if(neuronType.equals("Pad")){
             appendNonLinearLayer(node, NonLinearType.PAD, espamDNN);
             processOperationalNode(node);
             int[] pads = extractPads(node);
             Layer addedLayer = espamDNN.getLastLayer();
             addedLayer.setPads(pads);
             return;
        }

        if(neuronType.equals("Relu")) {
           appendNonLinearLayer(node, NonLinearType.ReLU,espamDNN);
           processOperationalNode(node);
           return;
        }

        if(neuronType.equals("Reshape") || neuronType.equals("Flatten")) {
            boolean isFlatten = false;
            if(neuronType.equals("Flatten"))
                isFlatten = true;
            appendReshapeLayer(node,espamDNN,isFlatten);
            processParamNode(node);
            processDataParamNode(node);
            processOperationalNode(node);
            Tensor extractedDataFormat = extractDataAttributeFromReshapeNode(node);
            saveExtractedDataFormat(node,extractedDataFormat);
            /**if(!_setReshapeShape(node))
                System.err.println("reshape parameter not set for reshape node: " + _uniqueNamedONNXNodes.get(node));
            else {
                Layer l = findEspamLayerInMapping(node);
                Reshape r = (Reshape)l.getNeuron();
                System.out.print(l.getName()+ " shape: [");
                for(Integer sh: r.getShape()){
                    System.out.print(sh + " ");
                }

                System.out.println("]");
            }*/
            return;
        }

           if(neuronType.equals("Slice")) {
            appendSliceLayer(node,espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Selu")) {
            appendNonLinearLayer(node, NonLinearType.SELU,espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Sigmoid")) {
            appendNonLinearLayer(node, NonLinearType.SIGM,espamDNN);
             processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Softmax")) {
            appendNonLinearLayer(node, NonLinearType.SOFTMAX,espamDNN);
            processOperationalNode(node);

            //_printstrWeights(node);

            return;
        }

        if(neuronType.equals("Softplus")) {
            appendNonLinearLayer(node, NonLinearType.SOFTPLUS,espamDNN);
              processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Sub")) {
            appendNonLinearArithmeticLayer(node, NonLinearType.SUBConst, espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Tanh")) {
            appendNonLinearLayer(node, NonLinearType.THN,espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Transpose")) {
            appendTransposeLayer(node,espamDNN);
            processOperationalNode(node);
            return;
        }

        if(neuronType.equals("Upsample")) {
            appendUpsampleLayer(node,espamDNN);
            processOperationalNode(node);
            if(!_setUpsampleScale(node))
                System.err.println("scale parameter not found for upsample node: " + _uniqueNamedONNXNodes.get(node));
            return;
        }

        /**
         * If no corresponding espam DNN layer type is found,
         * the NONE-typed layer is appended. By default, non-typed Layer is operational
         */
        System.err.println("DNN-2-CSDF conversion error: Unknown neuron type: " + neuronType);
        appendNoneTypeLayer(node,neuronType,espamDNN);
        processOperationalNode(node);
        return;
    }


    ///////////////////////////////////////////////////////////////////
    ////                 layers adders                             ///

    /**Appends Add layer to target DNN
     * There are 2 types of add layers in espam.Network model
     * if, the Add layer implements adding a constant variable (e.g. bias)
     * to inputs data, it is treated as a NonLinear AddConst element.
     * If the Add layer is used to summarize the result of 2 or more input layers
     * it have completely another behaviour and should be
     * created as Add Layer
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendAddLayer(ONNX.NodeProto node, Network dnn){
        Neuron neuron;

        /** determine how many inputs are coming from another layers*/
        int fromAnotherLayers = 0;
        for(String input: node.getInputList()){
            if(_operationalNodesOutputs.containsKey(input))
                fromAnotherLayers++;
        }
        if(fromAnotherLayers==1)
            neuron = new NonLinear(NonLinearType.AddConst);
        else
            neuron = new Arithmetic(ArithmeticOpType.ADD);

        /** add layer have one neuron by default*/
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);
    }

       /**Appends Add layer to target DNN
     * There are 2 types of add layers in espam.Network model
     * if, the Add layer implements adding a constant variable (e.g. bias)
     * to inputs data, it is treated as a NonLinear AddConst element.
     * If the Add layer is used to summarize the result of 2 or more input layers
     * it have completely another behaviour and should be
     * created as Add Layer
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendMulLayer(ONNX.NodeProto node, Network dnn){
        Neuron neuron;

        /** determine how many inputs are coming from another layers*/
        int fromAnotherLayers = 0;
        for(String input: node.getInputList()){
            if(_operationalNodesOutputs.containsKey(input))
                fromAnotherLayers++;
        }
        if(fromAnotherLayers==1)
            neuron = new NonLinear(NonLinearType.MULconst);
        else
            neuron = new Arithmetic(ArithmeticOpType.MUL);


        /** add layer have one neuron by default*/
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);
    }

     /** appends Concat layer to target DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendConcatLayer(ONNX.NodeProto node, Network dnn){
        Neuron neuron = new Concat();

        /** concat layer have one neuron by default*/
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);
    }

    /**
     *appends Pooling layer to target DNN
     * @param node corresponding ONNX node
     * @param poolingType concrete type of pooling
     * @param dnn target DNN
     */
    private void appendPoolingLayer(ONNX.NodeProto node, PoolingType poolingType, Network dnn) {

       Neuron neuron = createPoolingNeuron(node,poolingType);
        /**initially, pooling layer have 1 neuron. Number of neurons of pooling layer
         * depends on it's inputs number and determined after layers definition
         */
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

       // if(((CNNNeuron) neuron).getBoundaryMode().equals(BoundaryMode.NOTSET)) {
            int[] pads = extractPads(node);
            addedLayer.setPads(pads);
        //    ((CNNNeuron) neuron).setBoundaryMode(BoundaryMode.SAME);

        //}

        _layersMapping.put(addedLayer,node);
    }

     /**
     * appends Conv layer to target DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendConvLayer(ONNX.NodeProto node, Network dnn){

        Neuron neuron = createConvNeuron(node);
        //neuron.setBiasName("bias");
        neuron.setBiasName(null);
        /**
         * Number of neurons of Conv layer is determined from corresponding
         * Layer weights shape
         */
        int neurons_number = neuronsNumberFromWeightsOrDefault(node);

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        //if(((CNNNeuron) neuron).getBoundaryMode().equals(BoundaryMode.NOTSET)) {
            int[] pads = extractPads(node);
            addedLayer.setPads(pads);
        //    ((CNNNeuron) neuron).setBoundaryMode(BoundaryMode.SAME);

        //}

        _layersMapping.put(addedLayer,node);
    }

     /**
     * appends LRN layer to target DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendLRNLayer(ONNX.NodeProto node, Network dnn){

        Neuron neuron = createLRNNeuron(node);
        /**For normalization layers, neurons number are first set
         * to default value and then determined after connections set up
         */
        int neurons_number = _default_neurons_number;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();
        _layersMapping.put(addedLayer,node);
    }

    /**
     * Appends NonLinear layer to the target DNN
     * @param node corresponding ONNX node
     * @param nonLinearType  specified nonLinear type
     * @param dnn target DNN
     */
    private void appendNonLinearLayer(ONNX.NodeProto node,NonLinearType nonLinearType, Network dnn){

        Neuron neuron = new NonLinear(nonLinearType);
        int neurons_number = neuronsNumberFromWeightsOrDefault(node);

        if(nonLinearType.equals(NonLinearType.ImageScaler))
            _appendConstvalAttr(neuron,node,"scale", "float");

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);
    }



     /**
     * Appends Upsample layer to the target DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendUpsampleLayer(ONNX.NodeProto node, Network dnn){

        Upsample neuron = new Upsample();
        /**For dense layers, neurons number are determined by their
         * weights. For activation non-linear layers, following
         * after the cnn nodes, number of neurons is first set
         * to default value and then determined after connections set up
         */
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);

    }

    /**
     * Appends Transpose layer to the target DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendTransposeLayer(ONNX.NodeProto node, Network dnn){
        Transpose neuron = new Transpose();
        Vector<Integer> perm = _getPermParam(node);
        neuron.setPerm(perm);
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);

    }

    /**
     * Transpose permutation parameter
     * @return transpose permutation parameter
     */
    private Vector<Integer> _getPermParam(ONNX.NodeProto node){
        Vector<Integer> perm = new Vector<>();
        ONNX.AttributeProto permAttr = getONNXAttribute(node,"perm");
        try {
            List<Long> permList = permAttr.getIntsList();
            if(permList!=null){
                for(Long lPerm: permList)
                    perm.add(lPerm.intValue());
            }
        }
        catch (Exception e){System.err.println("perm attribute not found for node "+ _uniqueNamedONNXNodes.get(node) +
                "default permutation is set. ");}

        return perm;

    }


         /**
     * Calculates number of neurons, using information about weights shape.
     * If no weights were found for a neuron, the number of neuron is set up
     * to default_neurons_number
     * @param node corresponding ONNX.NodeProto
     * @return number of neurons for neurons with weights
     */
    private boolean _setReshapeShape(ONNX.NodeProto node){
        Layer layer = findEspamLayerInMapping(node);
        Reshape neuron = (Reshape) layer.getNeuron();
        ProtocolStringList nodeInputs = node.getInputList();
        String opNodeName = _uniqueNamedONNXNodes.get(node);

        for(String input: nodeInputs) {

              ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                if (externalInitializer != null) {

                  ByteString rawData = externalInitializer.getRawData();
                  if(rawData!=null) {
                      Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(), (int) externalInitializer.getDims(0), externalInitializer.getDataType().toString());
                      neuron.setShape(shape);
                      return true;
                  }
             }

             /** References to I/O nodes and another operational nodes are
             *  processed separately
             * */

            if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input) ||
                    _operationalNodesOutputs.containsKey(input))) {
                ONNX.NodeProto scaleParamNode = _parameterNodes.get(input);

                /** search weight-node in graph nodes*/
                if (scaleParamNode != null) {

                  ONNX.TensorProto scaleT = getONNXAttribute(scaleParamNode, "value").getT();
                  ByteString rawData = scaleT.getRawData();
                  Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                  neuron.setShape(shape);
                  return true;
                }

                ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                if (dataParamNode != null) {
                    ONNX.TensorProto scaleT = getONNXAttribute(dataParamNode, "value").getT();
                    ByteString rawData = scaleT.getRawData();
                    Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                    neuron.setShape(shape);
                    return true;
                }

            }

            if (_dataParameterNodesOutputs.containsKey(input)) {
                if(_findShapeInDPN(opNodeName, input, layer))
                    return true;
                }
        }
        return false;
    }

     /**
     * Extract weights from data parameter node
     * @return weights, extracted from data parameter node
     */
    private boolean _findShapeInDPN(String opNodeName, String inp, Layer layer){
        Reshape neuron = (Reshape) layer.getNeuron();
        for (ONNX.NodeProto dpn:_dataParameterNodes.values()) {
            if (dpn.getOutputList().contains(inp)) {

                for (String input : dpn.getInputList()) {
                    {
                        if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input))) {
                            ONNX.NodeProto scaleParamNode = _parameterNodes.get(input);
                            /** search weight-node in graph nodes*/
                            if (scaleParamNode != null) {
                                ONNX.TensorProto scaleT = getONNXAttribute (scaleParamNode, "value").getT();
                                ByteString rawData = scaleT.getRawData();
                                Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                                neuron.setShape(shape);
                                return true;
                               // }
                            }

                            ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                            if (dataParamNode != null) {
                                ONNX.TensorProto scaleT = getONNXAttribute (dataParamNode, "value").getT();
                                ByteString rawData = scaleT.getRawData();
                                Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                                neuron.setShape(shape);
                                return true;
                            }
                        }

                        ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                        if (externalInitializer != null) {
                            ByteString rawData = externalInitializer.getRawData();
                            Vector<Integer> shape = _littleEndianToIntArray(rawData.toByteArray(),(int)externalInitializer.getDims(0),externalInitializer.getDataType().toString());
                            neuron.setShape(shape);
                        }
                    }
                }
            }
        }

    return false;
    }


    /**
     * Calculates number of neurons, using information about weights shape.
     * If no weights were found for a neuron, the number of neuron is set up
     * to default_neurons_number
     * @param node corresponding ONNX.NodeProto
     * @return number of neurons for neurons with weights
     */
    private boolean _setUpsampleScale(ONNX.NodeProto node){
        Layer layer = findEspamLayerInMapping(node);
        Upsample neuron = (Upsample) layer.getNeuron();
        ProtocolStringList nodeInputs = node.getInputList();
        String opNodeName = _uniqueNamedONNXNodes.get(node);

        for(String input: nodeInputs) {

              ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                if (externalInitializer != null) {

                  ByteString rawData = externalInitializer.getRawData();
                  Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)externalInitializer.getDims(0),externalInitializer.getDataType().toString());
                  neuron.setScales(scale);
                  return true;
             }

             /** References to I/O nodes and another operational nodes are
             *  processed separately
             * */

            if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input) ||
                    _operationalNodesOutputs.containsKey(input))) {
                ONNX.NodeProto scaleParamNode = _parameterNodes.get(input);

                /** search weight-node in graph nodes*/
                if (scaleParamNode != null) {

                  ONNX.TensorProto scaleT = getONNXAttribute(scaleParamNode, "value").getT();
                  ByteString rawData = scaleT.getRawData();
                  Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                  neuron.setScales(scale);
                  return true;
                }

                ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                if (dataParamNode != null) {
                    ONNX.TensorProto scaleT = getONNXAttribute(dataParamNode, "value").getT();
                    ByteString rawData = scaleT.getRawData();
                    Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                    neuron.setScales(scale);
                    return true;
                }

            }

            if (_dataParameterNodesOutputs.containsKey(input)) {
                if(_findScalesInDPN(opNodeName, input, layer))
                    return true;
                }
        }
        return false;
    }

    /**
     * Convert little indian bytestring to float array
     * @param buffer bytestring buffer
     * @param dims number of array dimensions
     * @return float array, obtained from a little indian byte string
     */
    private Vector<Integer> _littleEndianToIntArray(byte[] buffer, int dims, String tensorDataType){
        String javaType = CSDFGMemoryRefiner.getInstance().javaType(tensorDataType.toLowerCase());
        Vector<Integer> res = new Vector<>();

        if(!(javaType.toLowerCase().contains("int")||javaType.equals("float"))){
            System.err.println("Little indian to array conversion error: unknown array data type");
            return res;
        }

        int typeSize = CSDFGMemoryRefiner.getInstance().typeSize(tensorDataType.toLowerCase());
        int offset = typeSize;
        int startId = 0;

        if(javaType.equals("float")){
            for(int i=0; i<dims; i++){
                float val = _littleEndianToFloat(buffer,startId);
                res.add((int)val);
                startId+=offset;
            }
        }

          if(javaType.toLowerCase().contains("int")){
            for(int i=0; i<dims; i++){
                int val;
              //  if(tensorDataType.contains("64"))
                //    val = _littleEndianToInt64(buffer,startId);
                //else
                    val = _littleEndianToInt(buffer,startId);
                res.add(val);
                startId+=offset;
            }
        }


        return res;
    }


    /**
     * Extract weights from data parameter node
     * @return weights, extracted from data parameter node
     */
    private boolean _findScalesInDPN(String opNodeName, String inp, Layer layer){
        Upsample neuron = (Upsample)layer.getNeuron();
        for (ONNX.NodeProto dpn:_dataParameterNodes.values()) {
            if (dpn.getOutputList().contains(inp)) {

                for (String input : dpn.getInputList()) {
                    {
                        if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input))) {
                            ONNX.NodeProto scaleParamNode = _parameterNodes.get(input);
                            /** search weight-node in graph nodes*/
                            if (scaleParamNode != null) {
                                ONNX.TensorProto scaleT = getONNXAttribute (scaleParamNode, "value").getT();
                                ByteString rawData = scaleT.getRawData();
                                Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                                neuron.setScales(scale);
                                return true;
                               // }
                            }

                            ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                            if (dataParamNode != null) {
                                ONNX.TensorProto scaleT = getONNXAttribute (dataParamNode, "value").getT();
                                ByteString rawData = scaleT.getRawData();
                                Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)scaleT.getDims(0),scaleT.getDataType().toString());
                                neuron.setScales(scale);
                                return true;
                            }
                        }

                        ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                        if (externalInitializer != null) {
                            ByteString rawData = externalInitializer.getRawData();
                            Vector<Integer> scale = _littleEndianToIntArray(rawData.toByteArray(),(int)externalInitializer.getDims(0),externalInitializer.getDataType().toString());
                            neuron.setScales(scale);
                        }
                    }
                }
            }
        }

    return false;
    }


    /**
     * Convert little indian value to float
     * @param buffer bytestring buffer
     * @param startId value start id in bytestring
     * @return float value, obtained from a little indian value
     */
    private float _littleEndianToFloat(byte[] buffer, int startId) {
        Float res =  ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN ).getFloat(startId);
        return res;
        }

            /**
     * Convert little indian value to int
     * @param buffer bytestring buffer
     * @param startId value start id in bytestring
     * @return int value, obtained from a little indian value
     */
    private int _littleEndianToInt(byte[] buffer, int startId) {
        Integer res =  ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN ).getInt(startId);
        return res;
    }

      /**
     * Appends NonLinear layer to the target DNN
     * @param node corresponding ONNX node
     * @param nonLinearType  specified nonLinear type
     * @param dnn target DNN
     */
    private void appendNonLinearArithmeticLayer(ONNX.NodeProto node,NonLinearType nonLinearType, Network dnn){

        Neuron neuron = new NonLinear(nonLinearType);
       // neuron.setParameter("constval","-1");
        int neurons_number = 1;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();


        _layersMapping.put(addedLayer,node);
    }


  /**TODO for now only one Data layer is allowed as DNN input
   * TODO input should have non-empty name!
   * setup single input Layer from Model description
   * @param dnn target DNN
   */
  private void appendInputLayer(ONNX.ValueInfoProto onnxModelInput, Network dnn){
        Data dataNeuron = new Data(DataType.INPUT);
        String layerName = _uniqueNamedONNXNodes.get(onnxModelInput);

        Tensor dataFormat = extractDataAttributeFromIONode(onnxModelInput,false);
        dataFormat = Tensor.omitOneSizedFourthDim(dataFormat);

        Tensor.updateChannelsNum(dataFormat);

        _extractedDataFormats.put(layerName,dataFormat);

      dataNeuron.setDataFormats(dataFormat);
        /**Input layer, representing an input have one neuron = input data*/

        dnn.addLayer(layerName,dataNeuron,1);

        /** As data layer have single data neuron,
        *   the Data nodes parameters are set at once after layer creation */
        Layer dataLayer = dnn.getLayers().lastElement();
        dataLayer.setInputFormat(dataFormat);
        dataLayer.setOutputFormat(dataFormat);

        /** set node as model input*/
        dnn.setInputLayer(dnn.getLayers().lastElement());

        /** save mapping info*/
        _layersMapping.put(dataLayer,onnxModelInput);
  }




   /**
    * * TODO : for now only one Data layer is allowed as DNN output
    * TODO outputshould have non-empty name!
    * setup single output Layer from Model description
    * @param dnn target DNN
    */
    private void appendOutputLayer(ONNX.ValueInfoProto onnxModelOutput, Network dnn){
        Data dataNeuron = new Data(DataType.OUTPUT);
        String layerName = _uniqueNamedONNXNodes.get(onnxModelOutput);

        Tensor dataFormat = extractDataAttributeFromIONode(onnxModelOutput,true);
        _extractedDataFormats.put(layerName, dataFormat);

        dataNeuron.setDataFormats(dataFormat);
        /**Input layer, representing an input have one neuron = input data*/

        dnn.addLayer(_uniqueNamedONNXNodes.get(onnxModelOutput),dataNeuron,1);

        /** As data layer have single data neuron,
        *   the Data nodes parameters are set at once after layer creation */
        Layer dataLayer = dnn.getLayers().lastElement();
        dataLayer.setInputFormat(dataFormat);
        dataLayer.setOutputFormat(dataFormat);

        /** set node as model output*/
        dnn.setOutputLayer(dnn.getLayers().lastElement());

        /** save mapping info*/
        _layersMapping.put(dataLayer,onnxModelOutput);
  }

     /**
      * TODO extract data format
     * Appends DATA layer to the target DNN
     * @param node corresponding ONNX node
     * @param dataType  specified DATA type
     * @param dnn target DNN
     */
    private void appendDataLayer(ONNX.NodeProto node,DataType dataType, Network dnn){
        Data dataNeuron = new Data(dataType);
        /** TODO replace dummy data format by the one extracted from initializers
         * TODO and dataParam Nodes
         * */

        Tensor dataFormat = new Tensor();

        dataNeuron.setDataFormats(dataFormat);
        /**Layer, representing a constant DATA node have one neuron = input data*/

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),dataNeuron,1);

        /** As data layer have single data neuron,
        *   the Data nodes parameters are set at once after layer creation */
        Layer dataLayer = dnn.getLayers().lastElement();
        dataLayer.setInputFormat(dataFormat);
        dataLayer.setOutputFormat(dataFormat);

        /** save mapping info*/
        _layersMapping.put(dataLayer,node);
    }

    /**
     * Append Reshape Layer to the target DNN
     * In case, if the reshape node is pure data container
     * (have no inputs from other layers)
     * it would not be added as an independent node
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendReshapeLayer(ONNX.NodeProto node,Network dnn, boolean isFlatten){
        Reshape neuron = createReshapeNeuron(node);
        neuron.setFlatten(isFlatten);
        /**For reshape layers, number of neurons =1 by default*/
        int neurons_number = 1;
        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();
        _layersMapping.put(addedLayer,node);
    }

    /**
     * Append Slice Layer to the target DNN
     * Slice layer is a special type of Reshape layer
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendSliceLayer(ONNX.NodeProto node,Network dnn){
        Reshape neuron = createReshapeNeuron(node);
        neuron.setSlice(true);
        /**For reshape layers, number of neurons =1 by default*/
        int neurons_number = 1;
        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();
        _layersMapping.put(addedLayer,node);
    }

     /**
     * Append DenseLayer, corresponding to ONNX.Matmul Layer to the target espam DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendMatMulLayer(ONNX.NodeProto node,Network dnn){

        DenseBlock neuron = createMatMulNeuron(node);

        /**For MatMul layers, number of neurons =1 by default*/
        int neurons_number = 1;
        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();
        _layersMapping.put(addedLayer,node);
        _denseNeurons.put(addedLayer.getName(),neuron.getNeuronsNum());
    }

    /**
     * Append DenseLayer, corresponding to ONNX.GEMM Layer to the target espam DNN
     * @param node corresponding ONNX node
     * @param dnn target DNN
     */
    private void appendGemmLayer(ONNX.NodeProto node,Network dnn){

         DenseBlock neuron = createGemmNeuron(node);
         neuron.setBiasName("bias");
        /**For Gemm layers, number of neurons =1 by default*/
        int neurons_number = 1;
        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();
        _layersMapping.put(addedLayer,node);
        _denseNeurons.put(addedLayer.getName(),neuron.getNeuronsNum());
    }

    /**
    * If there is no corresponding internal type,
    * the NONE-type layer type is appended to DNN
     * @param node corresponding ONNX node
     * @param unknownNeuronType description of unrecognized neuron type
     * @param dnn target DNN
     */
    private void appendNoneTypeLayer(ONNX.NodeProto node,String unknownNeuronType, Network dnn){

        Neuron neuron = new NoneTypeNeuron();
        neuron.setName(unknownNeuronType);
        int neurons_number = _default_neurons_number;

        dnn.addLayer(_uniqueNamedONNXNodes.get(node),neuron,neurons_number);
        Layer addedLayer = dnn.getLayers().lastElement();

        _layersMapping.put(addedLayer,node);
    }

     ///////////////////////////////////////////////////////////////////
    ////                  specific neurons creators                ///

    /**
     * Specified builder for Conv neuron
     * @param onnxNode corresponding onnx node
     * @return convolution typed neuron in an internal format
     */
    private Convolution createConvNeuron(ONNX.NodeProto onnxNode){
        Convolution resultNeuron = new Convolution();
        for (ONNX.AttributeProto attr: onnxNode.getAttributeList()){
            setCNNNeuronParam(resultNeuron,attr);
        }
        resultNeuron.setDim(3);
        return resultNeuron;
    }

     /**
     * Specified builder for Pooling neuron
     * @param onnxNode corresponding onnx node
     * @return pooling typed neuron in an internal format
     */
    private Pooling createPoolingNeuron(ONNX.NodeProto onnxNode,PoolingType poolingType){
        Pooling resultNeuron = new Pooling(poolingType);

        for (ONNX.AttributeProto attr: onnxNode.getAttributeList()){
            setCNNNeuronParam(resultNeuron,attr);
        }
        resultNeuron.setBoundaryMode(resultNeuron.getAutoBoundaryMode());
        return resultNeuron;
    }

     /**
     * Specified builder for Reshape neuron
     * @param onnxNode corresponding onnx node
     * @return pooling typed neuron in an internal format
     */
    private Reshape createReshapeNeuron(ONNX.NodeProto onnxNode) {
        Reshape resultNeuron = new Reshape();
        setReshapeNeuronParam(resultNeuron,onnxNode);
        return resultNeuron;
    }




     /**
     * Specified builder for MatMul neuron, ver2
     * @param onnxNode corresponding onnx node
     * @return MatMul typed neuron in an internal format
     */
    private DenseBlock createMatMulNeuron(ONNX.NodeProto onnxNode){
        int neuronsNum = extractDenseBlockNeuronsNum(onnxNode);
        DenseBlock denseBlock = new DenseBlock(neuronsNum);
        denseBlock.setRefinedType(DenseType.MATMUL);
        return denseBlock;
    }

     /**
     * Specified builder for Gemm neuron, ver2
     * @param onnxNode corresponding onnx node
     * @return MatMul typed neuron in an internal format
     */
    private DenseBlock createGemmNeuron(ONNX.NodeProto onnxNode){
        int neuronsNum = extractDenseBlockNeuronsNum(onnxNode);
        DenseBlock denseBlock = new DenseBlock(neuronsNum);
        denseBlock.setRefinedType(DenseType.GEMM);
        denseBlock.initParams();
        for (ONNX.AttributeProto attr: onnxNode.getAttributeList()){
            setGemmNeuronParam(denseBlock,attr);
        }
        return denseBlock;
    }

    /**
     * Find single real dynamic input for espam DenseBlock among
     * multiple inputs for MatMul or GEMM ONNX.NodeProto
     * @param onnxNode MatMul or GEMM ONNX.NodeProto
     * @return  single real dynamic input for espam DenseBlock or null
     */
    private String findDenseBlockSingleInput(ONNX.NodeProto onnxNode){
        Vector<String> comingFromLayers  = new Vector<>();

        for(String input: onnxNode.getInputList()){
            if(_operationalNodesOutputs.containsKey(input))
                comingFromLayers.add(input);
        }

        if(comingFromLayers.size()==0) {
            System.err.println("No inputs found for layer: "+_uniqueNamedONNXNodes.get(onnxNode));
            return null;
        }

        if (comingFromLayers.size()==1)
            return comingFromLayers.lastElement();

        /** find first non-pure data node*/
        for (String input: comingFromLayers) {
            if(!_dataParameterNodes.containsKey(input))
                return input;
        }

        /** return null by default*/
            return null;
    }

    /**
     * Extract neurons number for DenseBlock
     * @param onnxNode corresponding ONNX.NodeProto
     * @return extracted neurons number
     */
    private int extractDenseBlockNeuronsNum(ONNX.NodeProto onnxNode){
        int inputsCount = onnxNode.getInputCount();

        switch(inputsCount){
            case 1: {
                Tensor param = extractMatMulInputParam(onnxNode.getInput(0));
                return extractNeuronsNumFromTensor(param);
            }

            case 2:{
                Tensor firstParam = extractMatMulInputParam(onnxNode.getInput(0));
                Tensor secondParam = extractMatMulInputParam(onnxNode.getInput(1));
                Tensor weightParam = findWeightParam(firstParam,secondParam);
                return extractNeuronsNumFromTensor(weightParam);
            }

            case 3: {
                Tensor firstParam = extractMatMulInputParam(onnxNode.getInput(0));
                Tensor secondParam = extractMatMulInputParam(onnxNode.getInput(1));
                Tensor thirdParam = extractMatMulInputParam(onnxNode.getInput(2));
                Tensor weightParam = findWeightParam(firstParam,secondParam,thirdParam);
                return extractNeuronsNumFromTensor(weightParam);
            }

            default: return _default_neurons_number;
        }
    }

    /** searches for weight param in 2 matrices
     * @return matrix, containing weights info, if it was found and null otherwise
     */
    public Tensor findWeightParam(Tensor tensor1, Tensor tensor2){
        if(Tensor.isNullOrEmpty(tensor1) && Tensor.isNullOrEmpty(tensor2)){
            return null;
        }

        /** only one argument defined : weights coming from first input*/
        if(Tensor.isNullOrEmpty(tensor1))
            return tensor2;


          /** only one argument defined : weights coming from second input*/
        if(Tensor.isNullOrEmpty(tensor2))
             return tensor1;

        /** the weight param should be the seconf input of ONNX.NodeProto,
         * but orded is not guaranteed while parsing ONNX.NodeProto,
         * so additional checkout is added
         */
         if(isMatmulInputsOrderReversed(tensor1,tensor2))
             return tensor1;
         else
             return tensor2;
    }

     /**
      * TODO Finish implementation for "messed up" cases!
      *
      * searches for weight param in 2 matrices
      * @return matrix, containing weights info, if it was found and null otherwise
      */
    public Tensor findWeightParam(Tensor tensor1, Tensor tensor2, Tensor tensor3){

      boolean ist1empty = Tensor.isNullOrEmpty(tensor1);
      boolean ist2empty = Tensor.isNullOrEmpty(tensor2);
      boolean ist3empty = Tensor.isNullOrEmpty(tensor3);


        if(ist1empty && ist2empty && ist3empty)
            return null;

        if(!ist3empty)
            return tensor3;

        return findWeightParam(tensor1,tensor2);
    }


    /**
     * Extract neurons number from weight param
     * @param tensor weight-param tensor
     * @return neurons number, extracted from weight param tensor or
     * default neuron number;
     */
    private int extractNeuronsNumFromTensor(Tensor tensor){
        if(Tensor.isNullOrEmpty(tensor))
            return 1;
        if(tensor.getDimensionality()==1)
            return extractNeuronsNumFromVec(tensor);
        return extractNeuronsNumFromMatrix(tensor);
    }

    /**
     * TODO refactoring
     * extract number of neurons from parameters
     * */
    private int extractNeuronsNumFromVec(Tensor vector){
        return vector.getDimSize(0);
    }

    /** extract neurons number from second argument of MatMul, if possible
     * @return extracted neurons number, of default neurons number == 1
     */
    private int extractNeuronsNumFromMatrix(Tensor matrix){
        /** Tensor representation is <W,H> in espam.Network format */
        return matrix.getDimSize(0);
    }


     /**
      * Determine neurons number while both arguments are vectors (
      * the element-vise vector multiplication is implemented )
      * if vectors have the same size, the result will be the same size of them
      * if vectors have different size, they result data shape is aligned to longest vector
      * @return data shape of 2 vectors multiplication result
     */
    protected int getNNFrom2Vectors(Tensor firstArg, Tensor secondArg){
        if (firstArg.getLastDimSize() == secondArg.getLastDimSize())
             return firstArg.getLastDimSize();

        int alignedSize = Math.max(firstArg.getLastDimSize(), secondArg.getLastDimSize());
        return alignedSize;
    }


    /**Determine neurons number while one argument is matrix and another is vector
     * MatMul result is: V[n x m1] x M[m2 x r] = M[n x r] if m1==m2
     * if m1!=m2, result is undefined and set up to null
     * TODO this is done, because it is hard to determine the order of matrices
     * TODO in parsed ONNX format
     * @return data shape of 2 matrices multiplication result
     */
    protected int getNNFromVecAndMatrix(Tensor vec, Tensor matrix) throws Exception{
        int vecSize = vec.getLastDimSize();

        if (vecSize == matrix.getDimSize(0)) {
            return matrix.getDimSize(1);
        }

        if (vecSize == matrix.getDimSize(1)) {
            return matrix.getDimSize(0);
        }

        System.err.println("DenseBlock neurons number extraction error: bad tensors shape."+
                vec+" , " + matrix);
        throw new Exception("DenseBlock neurons num extraction error");
    }


    /**Determine neurons number while both arguments are matrices
     * MatMul result is: V[n x m1] x M[m2 x r] = M[n x r] if m1==m2
     * if m1!=m2, result is undefined and set up to null
     * TODO this is done, because it is hard to determine the order of matrices
     * TODO in parsed ONNX format
     * @return data shape of 2 matrices multiplication result
     */
    protected int getNNFrom2Matrices(Tensor firstArg, Tensor secondArg) throws Exception{
        if (firstArg.getDimSize(1) == secondArg.getDimSize(0)) {
            return Math.max(firstArg.getDimSize(0),secondArg.getDimSize(1));
        }

        System.err.println("DenseBlock neurons number extraction error: bad matrices shape."+
                firstArg+" , "+secondArg);
        throw new Exception("DenseBlock neurons num extraction error");
    }


    /** constant data parameter can be extracted from 3 sources:
     * - node attributes
     * @return
     */
    private Tensor extractMatMulInputParam(String matmulInput){

      /** search in parameter nodes*/
        if(_parameterNodesOutputs.containsKey(matmulInput)){
            ONNX.NodeProto inputOwner = _parameterNodesOutputs.get(matmulInput);
            String inputOwnerName = _uniqueNamedONNXNodes.get(inputOwner);
            Tensor result = _extractedDataFormats.get(inputOwnerName);
            if (!Tensor.isNullOrEmpty(result))
                return result;
        }
       /** search in initializers*/
       if(_onnxModelInitializers.containsKey(matmulInput)){
            Tensor result = _extractedDataFormats.get(matmulInput);
            if (!Tensor.isNullOrEmpty(result))
                return result;
       }

        /**
         * TODO: REFACTORING!
         */
      if(_dataParameterNodesOutputs.containsKey(matmulInput)) {
          //  System.out.println(matmulInput + " dpn for neurons");
           for(ONNX.NodeProto dpn: _dataParameterNodes.values()) {
               for (String out : dpn.getOutputList()) {
                   if(out.equals(matmulInput)) {
                     //  System.out.println(matmulInput + " dpn for neurons, source: " + _uniqueNamedONNXNodes.get(dpn));
                       for(String dpnInput: dpn.getInputList()) {
                           Tensor fromInitDpn = _extractedDataFormats.get(dpnInput);
                           if(fromInitDpn!=null) {
                               if(fromInitDpn.getDimensionality()>1) {
                                  return  fromInitDpn;
                                   // System.out.println("result = " + fromInitDpn);
                               }
                           }
                       }
                   }
               }
           }

       }

        return null;
    }


    /**
     * Checks if MatMul operation arguments should be switched (order could be messed up)
     * TODO during the chechout, please, note that  ONNX.TensorProto and espam.Tensor have different
     * TODO order of dimensions
     * @param firstArg first input of ONNX.NodeProto
     * @param secondArg second input of ONNX.NodeProto
     * @return true, if first/second input of ONNX.NodeProto == second/first argument of matMul node
     * and false otherwise
     */
    private boolean isMatmulInputsOrderReversed (Tensor firstArg, Tensor secondArg){
         if (firstArg.getDimSize(0) == secondArg.getDimSize(1))
             return false;
         return true;
    }


     /**
     * Specified builder for Reshape neuron
     * @param onnxNode corresponding onnx node
     * @return pooling typed neuron in an internal format
     */
    private LRN createLRNNeuron(ONNX.NodeProto onnxNode) {
        LRN resultNeuron = new LRN();
        for (ONNX.AttributeProto attr: onnxNode.getAttributeList()){
            setLRNNeuronParam(resultNeuron,attr);
        }
        //set default parameters, if none extracted
        if(resultNeuron.getSize()==0)
            resultNeuron.setSize(1);
        if(resultNeuron.getAlpha()==0)
            resultNeuron.setAlpha(0.0001f);
        if(resultNeuron.getBeta()==0)
            resultNeuron.setBeta(0.75f);
        if(resultNeuron.getBias()==0)
            resultNeuron.setBias(1.0f);

        return resultNeuron;
    }

     ///////////////////////////////////////////////////////////////////
    ////                        node processors                     ///

        /**
     * Operational nodes are nodes, that performs some operation
     * like convolution, pooling, MatMul etc.
     * Operatonal node outputs names are used as references on ONNX nodes,
     * and so, they are saved for DNN Connections revealing
     */
    private void processOperationalNode(ONNX.NodeProto node){
        _operationalNodes.put(_uniqueNamedONNXNodes.get(node),node);
        for(String output: node.getOutputList())
            _operationalNodesOutputs.put(output,node);
        for(String input: node.getInputList())
            _operationalNodesInputs.put(input,node);
    }

    /**
     * Parameter nodes are nodes, that contain
     * information about target DNN layers parameters such as
     * weights, biases etc.
     * During param Node processing this information is saved, until
     * it is requested for DNN Layers parameters extraction
     * TODO refactoring on parameter nodes outputs
     */
    public void processParamNode(ONNX.NodeProto node){
        _parameterNodes.put(_uniqueNamedONNXNodes.get(node), node);
                for (String output : node.getOutputList()) {
                    _parameterNodesOutputs.put(output, node);
                }
            }

     /**
     * Data parameter nodes are nodes, that contain
     * information about target DNN layers parameters such as
     * data
     * During param Node processing this information is saved, until
     * it is requested for DNN Layers parameters extraction
     * TODO Merge Param nodes and Data Param Nodes??
     */
    public void processDataParamNode(ONNX.NodeProto node){
        _dataParameterNodes.put(_uniqueNamedONNXNodes.get(node),node);
        for(String outp: node.getOutputList())
            _dataParameterNodesOutputs.put(outp,node);
    }

    /**
     * Process Dense node. For dense node should be implemented resolving of
     * multiple inputs of GEMM/MatMul Node --> single input of DenseBlock
     * @param node corresponding ONNX.NodeProto
     */
    public void processDenseNode(ONNX.NodeProto node){
      String singleInput  = findDenseBlockSingleInput(node);
      if(singleInput==null) {
          System.err.println("No inputs found for DenseBlock "+ _uniqueNamedONNXNodes.get(node));
          return;
      }
      _denseInputs.put(_uniqueNamedONNXNodes.get(node),singleInput);
    }

    /**Saves data Format, extracted from ONNX.NodeProto
     * TODO for now only one Data Tensor associated with each Param Node
     * */
    public void saveExtractedDataFormat(ONNX.NodeProto node, Tensor dataFormat){
        String srcName = _uniqueNamedONNXNodes.get(node);
        saveExtractedDataFormat(srcName,dataFormat);
    }

    /**Saves data Format extracted from some named ONNX.ModelProto element
     * TODO for now only one Data Tensor associated with each Param Node
     * @param srcName name of ONNX.ModelProto element
     * @param dataFormat extracted data Format
     */
    public void saveExtractedDataFormat(String srcName, Tensor dataFormat){
        if(srcName!=null && dataFormat!=null)
            _extractedDataFormats.put(srcName,dataFormat);
    }

    /**
     * Extract and save pads of ONNX.NodeProto, if pads are defined
     * @param node ONNX.NodeProto
     */
    public int[] extractPads(ONNX.NodeProto node){
        ONNX.AttributeProto pads = getONNXAttribute(node,"pads");
        if(pads==null)
            return null;
        int[] padsValues = new int[pads.getIntsList().size()];
        for(int i=0;i<padsValues.length;i++)
            padsValues[i] = (int)pads.getInts(i);

        return padsValues;
    }


    ///////////////////////////////////////////////////////////////////
    ////                    parameter extractors                   ///

     /**
     * Extract metadata about the batchNormalization node parameters:
     * <scale, B, mean, var> (see https://github.com/onnx/onnx/blob/master/docs/Operators.md#BatchNormalization
     * for more details).
     * As ONNX format do not preserve the parameters order, the parameters should
     * contain specialized names prefixes to be found:
     * scale : scale,weights
     * B : B,bias
     * mean : mean
     * var: var
     * Otherwise, parameters would not be found and should be obtained manually
     * (e.g. through the https://lutzroeder.github.io/netron/)
     * @param node node
     */
    private void _extractBNParamsMetaData(ONNX.NodeProto node){
        boolean paramsFound = false;
            paramsFound = _findBNInits(node);

            /**System.out.println(" BN init parameters: ");
            System.out.print("<");
            for (String init: _bnInits.get(_uniqueNamedONNXNodes.get(node)))
                System.out.print(init + " ");
            System.out.println(">");*/
    }


     /**
     * Extract metadata about the node parameters such as weights and biases
     * @param node node
     * @param weights if weights should be found
     * @param bias if bias should be found
     */
    private void _extractParamsMetaData(ONNX.NodeProto node, boolean weights, boolean bias){
        boolean weightsFound = false;
        boolean biasFound = false;
        HashMap<String,String> initsList = _denseWeightsInits;
        HashMap<String,String> nodeInitsList = _denseWeightsNodeInits;
        if(node.getOpType().toLowerCase().contains("conv")){
            initsList = _convWeightsInits;
            nodeInitsList = _convWeightsNodeInits;
        }

        if(weights){
           weightsFound = _findInitWeights(node,initsList);

           if(!weightsFound)
            weightsFound = _findWeightsNode(node, initsList, nodeInitsList);

           if(!weightsFound)
               System.err.println("weights not found for "+_uniqueNamedONNXNodes.get(node));
        }

        if(bias){
            biasFound = _findInitBias(node);
            if(!biasFound)
                biasFound = _findBiasNode(node);
            if(biasFound)
                findEspamLayerInMapping(node).getNeuron().setBiasName("bias");
            //if(node.getOpType().equals("Mul") || node.getOpType().equals("Add"))
              //  System.out.println(_uniqueNamedONNXNodes.get(node) + " bias found: " + biasFound);

            //if(!biasFound)
             //   System.out.println("bias not found for "+_uniqueNamedONNXNodes.get(node));
        }

    }

    /** Save list of extracted parameters as JSON metadata file
     * @param dir directory
     * @param fileName name of the file
     */
   private void _saveParametersAsJSONMetaData(String dir, String fileName){
        initializersMetaData initM = new initializersMetaData();
        initM.set_conv_weights(_convWeightsInits);
        initM.set_dense_weights(_denseWeightsInits);
        initM.setBiases(_biasInits);
        initM.set_conv_weights_nodes(_convWeightsNodeInits);
        initM.set_dense_weights_nodes(_denseWeightsNodeInits);
        initM.setBiasesNodes(_biasNodeInits);
        initM.setDense_neurons(_denseNeurons);
        initM.setBNpar(_bnInits);

        try {
            String jsonInintM = JSONParser.getInstance().toJson(initM);
            FileWorker.write(dir,fileName,"json",jsonInintM);
           // System.out.println(dir + fileName + ".json saved");
        }
        catch (Exception e){
            System.out.println("Initializers metadata saving error: " + e.getMessage());
        }
    }


    /**
     * Find weights initializer
     * @param node onnx node
     */
    private boolean _findInitWeights(ONNX.NodeProto node, HashMap<String,String> initWeightsList){
    ProtocolStringList nodeInputs = node.getInputList();
    Layer layer = findEspamLayerInMapping(node);

        for(String input: nodeInputs) {
            ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
            if (externalInitializer != null) {
               Vector<Integer> dims = _getDimsList(externalInitializer);
                if(_isInitWeights(layer,dims)) {
                    initWeightsList.put(_uniqueNamedONNXNodes.get(node),externalInitializer.getName());
                    // System.out.println(externalInitializer.getName() + " is weights init for " + layer.getName());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates number of neurons, using information about weights shape.
     * If no weights were found for a neuron, the number of neuron is set up
     * to default_neurons_number
     * @param node corresponding ONNX.NodeProto
     * @return number of neurons for neurons with weights
     */
    private boolean _findWeightsNode(ONNX.NodeProto node, HashMap<String,String> initsList, HashMap<String,String> nodeInitsList){
        Layer layer = findEspamLayerInMapping(node);
        ProtocolStringList nodeInputs = node.getInputList();
        String opNodeName = _uniqueNamedONNXNodes.get(node);

        for(String input: nodeInputs) {
            /** References to I/O nodes and another operational nodes are
             *  processed separately
             * */
            if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input) ||
                    _operationalNodesOutputs.containsKey(input))) {
                ONNX.NodeProto weightsParamNode = _parameterNodes.get(input);
                /** search weight-node in graph nodes*/
                if (weightsParamNode != null) {

                    ONNX.TensorProto w = getONNXAttribute(weightsParamNode, "value").getT();
                    Vector<Integer> dims = _getDimsList(w);
                    if(_isInitWeights(layer,dims)) {
                        nodeInitsList.put(opNodeName,weightsParamNode.getName());
                        return true;
                    }
                }

                ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                if (dataParamNode != null) {
                    ONNX.TensorProto w = getONNXAttribute(dataParamNode, "value").getT();
                    Vector<Integer> dims = _getDimsList(w);
                    if(_isInitWeights(layer,dims)) {
                        nodeInitsList.put(opNodeName,dataParamNode.getName());
                        return true;
                    }
                }

            }

            if (_dataParameterNodesOutputs.containsKey(input)) {
                if(_findWeightsInDPN(opNodeName, input, layer,initsList,nodeInitsList))
                    return true;
                }
        }
        return false;
    }

    /**
     * Extract weights from data parameter node
     * @return weights, extracted from data parameter node
     */
    private boolean _findWeightsInDPN(String opNodeName, String inp, Layer layer, HashMap<String,String> initsList, HashMap<String,String> nodeInitsList){
        for (ONNX.NodeProto dpn:_dataParameterNodes.values()) {
            if (dpn.getOutputList().contains(inp)) {

                for (String input : dpn.getInputList()) {
                    {
                        if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input))) {
                            ONNX.NodeProto weightsParamNode = _parameterNodes.get(input);
                            /** search weight-node in graph nodes*/
                            if (weightsParamNode != null) {
                                ONNX.TensorProto w = getONNXAttribute(weightsParamNode, "value").getT();
                                Vector<Integer> dims = _getDimsList(w);
                                if (_isInitWeights(layer, dims)) {
                                    nodeInitsList.put(opNodeName, _uniqueNamedONNXNodes.get(weightsParamNode));
                                    return true;
                                }
                            }

                            ONNX.NodeProto dataParamNode = _dataParameterNodes.get(input);
                            if (dataParamNode != null) {
                                ONNX.TensorProto w = getONNXAttribute(dataParamNode, "value").getT();
                                Vector<Integer> dims = _getDimsList(w);
                                if (_isInitWeights(layer, dims)) {
                                    nodeInitsList.put(opNodeName, _uniqueNamedONNXNodes.get(dataParamNode));
                                    return true;
                                }
                            }
                        }

                        ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                        if (externalInitializer != null) {
                            Vector<Integer> dims = _getDimsList(externalInitializer);
                            if (_isInitWeights(layer, dims)) {
                                initsList.put(opNodeName, externalInitializer.getName());
                                // System.out.println(externalInitializer.getName() + " is weights init for " + layer.getName());
                                return true;
                            }
                        }
                    }
                }
            }
        }

    return false;
    }

      /**
     * Calculates number of neurons, using information about weights shape.
     * If no weights were found for a neuron, the number of neuron is set up
     * to default_neurons_number
     * @param node corresponding ONNX.NodeProto
     * @return number of neurons for neurons with weights
     */
    private boolean _findBiasNode(ONNX.NodeProto node){
        Layer layer = findEspamLayerInMapping(node);
        ProtocolStringList nodeInputs = node.getInputList();
        String opNodeName = _uniqueNamedONNXNodes.get(node);
        boolean addConstLayer = false;
        if(layer.getNeuron() instanceof NonLinear)
            addConstLayer = true;

        for(String input: nodeInputs) {
            /** References to I/O nodes and another operational nodes are
             *  processed separately
             * */
            if (!(_ILayersNames.contains(input) || _OLayersNames.contains(input) ||
                    _operationalNodesOutputs.containsKey(input))) {

                ONNX.NodeProto biasParamNode = _parameterNodes.get(input);
                /** search weight-node in graph nodes*/
                if (biasParamNode != null) {
                    ONNX.TensorProto b = getONNXAttribute(biasParamNode, "value").getT();
                    Vector<Integer> dims = _getDimsList(b);
                    if(dims.size()==1 || addConstLayer) {
                        if (_hasLenEqualToNeuronsNum(layer, dims.get(0))|| addConstLayer) {
                            _biasNodeInits.put(opNodeName, biasParamNode.getName());
                            // System.out.println(externalInitializer.getName() + " is bias init for " + layer.getName());
                            return true;
                        }
                    }
                }

                ONNX.NodeProto biasDataParamNode = _dataParameterNodes.get(input);
                if (biasDataParamNode != null) {
                    ONNX.TensorProto b = getONNXAttribute(biasDataParamNode, "value").getT();
                    Vector<Integer> dims = _getDimsList(b);
                    if(dims.size()==1 || addConstLayer) {
                        if (_hasLenEqualToNeuronsNum(layer, dims.get(0))|| addConstLayer) {
                            _biasNodeInits.put(opNodeName, biasParamNode.getName());
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    /**
     * Find bias initializer
     * @param node onnx node
     */
    private boolean _findBNInits(ONNX.NodeProto node){
    ProtocolStringList nodeInputs = node.getInputList();
    Layer layer = findEspamLayerInMapping(node);
    String nodeName = _uniqueNamedONNXNodes.get(node);
    Vector<String> nodeInits = new Vector<>();
    Vector<String> nodeInitsSorted = new Vector<>();
    boolean initsFound = true;

        for(String input: nodeInputs) {
            ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
            if (externalInitializer != null) {
                /** get dims and represent them as int values*/
                List<Long> dims = externalInitializer.getDimsList();
                /** All BN parameters are linear*/
                if(dims.size()==1) {
                    Integer len = Integer.parseInt(dims.get(0).toString());

                    if (_hasLenEqualToNeuronsNum(layer, len)) {
                        //System.out.println(_uniqueNamedONNXNodes.get(node) +" has bias-len param "+ externalInitializer.getName());
                        nodeInits.add(externalInitializer.getName());
                       // _biasInits.put(_uniqueNamedONNXNodes.get(node),externalInitializer.getName());
                       // System.out.println(externalInitializer.getName() + " is bias init for " + layer.getName());
                        //return true;
                    }
                }
            }
        }

        //find scale
        String scale = "none";
        for(String nodeInit: nodeInits) {
            if(nodeInit.contains("scale")||nodeInit.contains("weight")||nodeInit.contains("gamma"))
                scale = nodeInit;
        }

        nodeInitsSorted.add(scale);
        if(scale.equals("none")) {
            System.err.println("BN node " + nodeName + "scale not found!");
            initsFound = false;
        }
        else nodeInits.remove(scale);

        //find mean
        String mean = "none";
        for(String nodeInit: nodeInits) {
            if(nodeInit.contains("mean"))
               mean = nodeInit;
        }
        nodeInitsSorted.add(mean);

        if(mean.equals("none")) {
            System.err.println("BN node " + nodeName + "mean not found!");
            initsFound = false;
        }
        else nodeInits.remove(mean);

        //find var
        String var = "none";
        for(String nodeInit: nodeInits) {
            if(nodeInit.contains("var"))
               var = nodeInit;
        }

        nodeInitsSorted.add(var);
        if(var == "none") {
            System.err.println("BN node " + nodeName + "var not found!");
            initsFound = false;
        }
        else nodeInits.remove(var);

        //find B
        if(nodeInits.size()==1) {
            nodeInitsSorted.add(nodeInits.elementAt(0));
        }
        else {
            if (nodeInits.size() == 0) {
                System.err.println("BN node " + nodeName + "bias not found!");
                nodeInitsSorted.add("none");
                initsFound = false;
            } else {
                for (String nodeInit : nodeInits) {
                    if (nodeInit.contains("bias") || nodeInit.contains("B")) {
                        nodeInitsSorted.add(nodeInit);
                    }
                    else  nodeInitsSorted.add("none");
                }
            }
        }

        _bnInits.put(nodeName,nodeInitsSorted);
        return initsFound;
    }


    /**
     * Find bias initializer
     * @param node onnx node
     */
    private boolean _findInitBias(ONNX.NodeProto node){
    ProtocolStringList nodeInputs = node.getInputList();
    Layer layer = findEspamLayerInMapping(node);
    boolean addConstLayer = false;
        if(layer.getNeuron() instanceof NonLinear)
            addConstLayer = true;

        for(String input: nodeInputs) {
            ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
            if (externalInitializer != null) {
                /** get dims and represent them as int values*/
                List<Long> dims = externalInitializer.getDimsList();
                /** TODO we assume that all biases are linear*/
                if(dims.size()==1 || addConstLayer) {
                    Integer initSize = Integer.parseInt(dims.get(0).toString());

                    if (_hasLenEqualToNeuronsNum(layer, initSize)|| addConstLayer) {
                        _biasInits.put(_uniqueNamedONNXNodes.get(node),externalInitializer.getName());
                       // System.out.println(externalInitializer.getName() + " is bias init for " + layer.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check, if initializer is bias
     * @param layer a layer
     * @param initSize number of elements in the initializer
     * @return true, if initializer is bias and false otherwise
     */
    private boolean _hasLenEqualToNeuronsNum(Layer layer, int initSize){
        int neurons = layer.getNeuronsNum();

        if(layer.getNeuron().getNeuronType().equals(NeuronType.DENSEBLOCK))
            neurons = ((DenseBlock)layer.getNeuron()).getNeuronsNum();

        if(neurons==initSize)
            return true;

        return false;
    }

    /**
     * get list of the ONNX.TensorProto dimensions as list of integers
     * @param onnxTensor ONNX.TensorProto
     * @return list of the ONNX.TensorProto dimensions as list of integers
     */
    private Vector<Integer> _getDimsList(ONNX.TensorProto onnxTensor){
        Vector<Integer> intDims = new Vector<>();
        /** get dims and represent them as int values*/
        List<Long> dims = onnxTensor.getDimsList();
        for(Long dim: dims)
            intDims.add(Integer.parseInt(dim.toString()));
        return intDims;
    }

    /**
     * Check, if initializer is weights
     * @param layer a layer
     * @param dims initializer dimensions
     * @return true, if initializer is weights and false otherwise
     */
    private boolean _isInitWeights(Layer layer, Vector<Integer> dims){
        if(layer.getNeuron().getNeuronType().equals(NeuronType.CONV))
            return _isInitConvWeights(layer,dims);
        if(layer.getNeuron().getNeuronType().equals(NeuronType.DENSEBLOCK))
            return _isInitDenseWeights(layer,dims);

        return false;
    }



    /**
     * Check, if initializer is Convolutional node weights
     * @param convLayer a convolutional layer
     * @param dims initializer dimensions
     * @return true, if initializer is convolutional neuron weights and false otherwise
     */
    private boolean _isInitConvWeights(Layer convLayer, Vector<Integer> dims){
        try {
            CNNNeuron cnnNeuron = (CNNNeuron) convLayer.getNeuron();

            if (dims.contains(cnnNeuron.getKernelH()) && dims.contains(cnnNeuron.getKernelW()) &&
                    dims.contains(convLayer.getNeuronsNum()) && dims.size()>2)
                return true;
        }
        catch (Exception e){
            System.err.println(convLayer.getName() + " CNN layer weights checkout error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check, if initializer is Convolutional node weights
     * @param denseLayer a dense layer
     * @param dims initializer dimensions
     * @return true, if initializer is convolutional neuron weights and false otherwise
     */
    private boolean _isInitDenseWeights(Layer denseLayer, Vector<Integer> dims){
        try {
            DenseBlock denseNeuron = (DenseBlock) denseLayer.getNeuron();
            if (dims.contains(denseNeuron.getNeuronsNum()) && dims.size()>1)
                return true;
           // System.out.println(denseLayer.getName()+" neurons: "+ denseNeuron.getNeuronsNum());
        }
        catch (Exception e){
            System.err.println(denseLayer.getName() + " dense layer weights checkout error: " + e.getMessage());
        }

        return false;
    }


    /**
     * Calculates number of neurons, using information about weights shape.
     * If no weights were found for a neuron, the number of neuron is set up
     * to default_neurons_number
     * @param node corresponding ONNX.NodeProto
     * @return number of neurons for neurons with weights
     */
    private int neuronsNumberFromWeightsOrDefault(ONNX.NodeProto node){
        ProtocolStringList nodeInputs = node.getInputList();
        for(String input: nodeInputs) {
            /** References to I/O nodes and another operational nodes are
             *  processed separately
             * */
            if (!(_ILayersNames.contains(input) ||_OLayersNames.contains(input) ||
                    _operationalNodesOutputs.containsKey(input) ))
            {
                    ONNX.NodeProto weightsParamNode = _parameterNodes.get(input);
                    /** search weight-node in graph nodes*/
                    if (weightsParamNode != null) {
                        return extractNeuronsNumFromParameterNode(weightsParamNode);
                    }

                    ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);
                    if (externalInitializer != null) {
                        return extractNeuronsNumFromInitializer(externalInitializer);
                    }
                }
        }

         /**
          * If the weights are not found neither in graph nodes nor in
          * initializers, default neurons number is returned
          */
        return _default_neurons_number;
    }

     /**
     * Extract neurons number from ONNX.NodeProto format, using
     * weights parameter in format: [M x C x k1 x...kn], where
     * M = number of neurons/feature maps
     * @param parameterNode ONNX NodeProto node, contains related information
     * @return extracted information or default_value = 1
     */
    private int extractNeuronsNumFromParameterNode(ONNX.NodeProto parameterNode) {
        return (int) getONNXAttribute(parameterNode, "value").getT().getDims(0);
    }

      /**
     * Extract neurons number from ONNX.TensorProto format, using
     * weights parameter in format: [M x C x k1 x...kn], where
     * M = number of neurons/feature maps
     * @param initializer external initializer, contains related information
     * @return extracted information or default_value = 1
     */
    private int extractNeuronsNumFromInitializer(ONNX.TensorProto initializer) {
        return (int)initializer.getDims(0);
    }

    /**
     * Set specific param for convolutional and pooling neurons:
     * int kernel_size, int stride, boundaryMode
     */
   private void setCNNNeuronParam(CNNNeuron neuron, ONNX.AttributeProto attr) {
       String paramName = attr.getName();

       if (paramName.equals("kernel_shape")) {
           int k_size = (int)attr.getInts(0);
           neuron.setKernelH(k_size);
           neuron.setKernelW(k_size);
           return;
       }

       if (paramName.equals("strides")) {
            neuron.setStride((int)attr.getInts(0));
            return;
       }

       if (attr.getName().equals("auto_pad")) {
           neuron.setBoundaryMode(convertBoundaryMode(attr.getS().toStringUtf8()));
           return;
       }

       if (paramName.equals("dilations")) {
           Integer[] dilations = new Integer[2];
           List<Long> dilationsAttr = attr.getIntsList();
           try{
               dilations[0] = dilationsAttr.get(0).intValue();
               dilations[1] = dilationsAttr.get(1).intValue();
               /** default dilations*/
               if(dilations[0]==1 && dilations[1]==1)
                   return;
               neuron.setDilations(dilations);
           }
           catch (Exception e){System.err.println("Dilations setup error: "+e.getMessage());}

          // System.out.println("dilations found: "+attr.getIntsList() +
            //       ", set: [" + dilations[0] +","+ dilations[1]+"]");
            return;
       }

          if (paramName.equals("group")) {
            String strGroup = attr.getI() + "";
            neuron.setGroup(Integer.parseInt(strGroup));
            //System.out.println("neuron_group: "+ neuron.getGroup());
            return;
       }
   }

      /**
     * Set specific param for convolutional and pooling neurons:
     * int kernel_size, int stride, boundaryMode
     */
   private void setLRNNeuronParam(LRN neuron, ONNX.AttributeProto attr) {
       String paramName = attr.getName();

       if (paramName.equals("size")) {
           neuron.setSize((int)attr.getI());
           return;
       }

       if (paramName.equals("alpha")) {
            neuron.setAlpha((int)attr.getF());
            return;
       }

        if (paramName.equals("beta")) {
            neuron.setBeta((int)attr.getF());
            return;
       }

        if (paramName.equals("bias")) {
            neuron.setBias((int)attr.getF());
            return;
       }

   }

       /**
     * Add GEMM - parameters such as:
     * alpha [float] Scalar multiplier of input tensors of GEMM
     * beta [float] Scalar multiplier for GEMM tensor C
     * transA [int] Whether A should be transposed
     * transB [int] Whether B should be transposed
     * @param neuron gemm neuron
     */
    private void setGemmNeuronParam(DenseBlock neuron, ONNX.AttributeProto attr){
            String paramName = attr.getName();

       if (paramName.equals("alpha") || paramName.equals("beta")) {
           Float val = attr.getF();
           neuron.setParameter(paramName,val.toString());
           return;
       }
        if (paramName.equals("transA") || paramName.equals("transB")) {
            Long val = attr.getI();
            neuron.setParameter(paramName,val.toString());
            return;
       }
    }

    /**
     * Extract reshape neuron parameters, if any given
     * @param neuron Reshape neuron
     * @param nodeProto corresponding onnx NodeProto
     * @return Reshape Neuron with extracted parameters
     */
    private void setReshapeNeuronParam (Reshape neuron, ONNX.NodeProto nodeProto) {
        /**
         *  specify output shape if shape parameter is given
         */

         /**1. search in attributes*/
        Tensor onnxOutputShape = extractShapeFromNodeAttributes(nodeProto);
        if (!(Tensor.isNullTensor(onnxOutputShape))) {
                neuron.setDataFormats(onnxOutputShape);
                return;
        }

        /**2. if not found, search in extracted data formats
        for(String input: nodeProto.getInputList()) {
            if (_extractedDataFormats.containsKey(input))
                System.out.println("param input for reshape: "+_extractedDataFormats.get(input));
        }


        /**2. if not found, search in initializers*/
      /**  onnxOutputShape = extractShapeFromInitializers(nodeProto);
        if (!(Tensor.isNullTensor(onnxOutputShape))) {
                neuron.setDataFormats(onnxOutputShape);
                return;
        }*/

        /**3. if not found, set default*/
        //System.out.println("no shape set for reshape node: "+_uniqueNamedONNXNodes.get(nodeProto));
        neuron.setDataFormats();
    }

    /** extract shape attr for Reshape Node from NodeProto attributes
     * @param nodeProto corresponding ONNX.NodeProto
     * @return Reshape Node shape, extracted from node attributes or null
     */
    private Tensor extractShapeFromNodeAttributes(ONNX.NodeProto nodeProto){
        Tensor result = null;

        ONNX.AttributeProto outputShapeAttr = getONNXAttribute(nodeProto, "shape");

        if(outputShapeAttr!=null){
            List<Long> shape = outputShapeAttr.getIntsList();

            result = new Tensor();
            for (Long val : shape) {
                int intVal = Integer.parseInt(val.toString());
                result.addDimension(intVal);
            }
            result = convertDimsOrder(result);
        }

        return result;
    }

    /** extract shape attr for Reshape Node from NodeProto attributes
     * @param nodeProto corresponding ONNX.NodeProto
     * @return Reshape Node shape, extracted from node attributes or null
     */
    private Tensor extractShapeFromInitializers(ONNX.NodeProto nodeProto){
        Tensor result = null;
        ProtocolStringList nodeInputs = nodeProto.getInputList();
        for(String input: nodeInputs) {
            ONNX.TensorProto externalInitializer = _onnxModelInitializers.get(input);



            if (externalInitializer != null) {
                System.out.println("initializer: "+ externalInitializer);
                result = convertTensor(externalInitializer);
                return result;
            }
        }
        return result;
    }


    /**
     * Extracts shape of data, contained in ONNX.NodeProto data container
     * @param nodeProto ONNX.NodeProto
     * @return shape of data, contained in ONNX.NodeProto data container
     */
    private Tensor extractDataAttributeFromConstNode(ONNX.NodeProto nodeProto)
    {
        ONNX.AttributeProto constNodeVal = getONNXAttribute(nodeProto,"value");

        if(constNodeVal!=null){
            if(constNodeVal.hasT()) {
                ONNX.TensorProto onnxTensor = constNodeVal.getT();
                return convertTensor(onnxTensor);
            }
            else System.out.println("node has value, but does not have Tensor");
            System.out.print(nodeProto);

        }
        System.out.println("node does not have value");
        System.out.print(nodeProto);

        return null;
    }

     /**
     * Extracts I/O formats from input/output nodes of DNN model, if possible
     * @param node Input or output of DNN Graph
     * @return extracted Tensor in espam.Tensor format or null
     */
    public Tensor extractDataAttributeFromIONode(ONNX.ValueInfoProto node, boolean omitOneSizedDims) {
        Tensor dataFormat = convertTensor(node.getType().getTensorType().getShape());
        if(omitOneSizedDims)
            dataFormat = Tensor.omitOneSizedDims(dataFormat);
        return dataFormat;
    }

    /**
     * Extract string description of data type
     * @param node ONNX.ValueInfoProto onnx model input node
     * @return string description of data type
     */
    private void _extractDataTypeFromInputNode(ONNX.ValueInfoProto node){
        try {
            String dataType = node.getType().getTensorType().getElemType().toString().toLowerCase();
            //System.out.println(dataType);
            if(dataType.equals("int")||dataType.equals("float")||dataType.equals("double")) {
                _modelDataType = dataType;
                _modelWeightsType = dataType;
            }
            else {
                System.err.println("ONNX model input type extraction error, unknown data type: " + dataType
                        + " .Data type is set to float");
            }

        }
        catch (Exception e){
            System.err.println("ONNX model input type extraction error. Data type is set to float");
        }
    }

    /**
     * Extracts information about Reshape Node input/output format, if possible
     * Reshape node output format could be contained either in "shape" attribute, or
     * in data attribute. If both attributes does not contain related information,
     * It is extracted implicitly from input/output Reshape node connections
     * @param nodeProto Reshape Node
     * @return extracted Tensor in espam.Tensor format or null
     */
    public Tensor extractDataAttributeFromReshapeNode(ONNX.NodeProto nodeProto)
    {
        /** Extract from "shape" attribute */
        ONNX.AttributeProto outputShapeAttr = getONNXAttribute(nodeProto, "shape");

        if(outputShapeAttr!=null){
            List<Long> shape = outputShapeAttr.getIntsList();
            Tensor result = new Tensor();

            for (Long val : shape) {
                int intVal = Integer.parseInt(val.toString());
                result.addDimension(intVal);
            }
            /** convert Tensor from ONNX.Tensor notation to espam.Tensor notation*/
            result = convertDimsOrder(result);
            return result;
        }

        /**extract from "data" attribute
         * TODO implement search in "data" param if reasonable
         * */
        return null;
    }

        /**
     * Append constant-value attribute
     * @param neuron DNN neuron
     * @param node ONNX node
     * @param attrname attribute name
     * @param expectedType expected attribute type (float/int)
     */
    private void _appendConstvalAttr(Neuron neuron, ONNX.NodeProto node, String attrname, String expectedType){
      ONNX.AttributeProto attr =  getONNXAttribute(node,attrname);
      String constval = null;
      if(attr==null)
          return;

      if(expectedType.equals("float")){
          constval = "" + attr.getF();
      }
      if(expectedType.equals("int")){
          constval = "" + attr.getI();
      }

      if(constval!=null) {
          neuron.setParameter("constval", constval);
      }
    }

    ////////////////////////////////////////////////////////////////////
    ////                     other methods                        ///

    /**
     * Some of the nodes (e.g. Reshape Node) could contain only the Data information
     * In this case, the are not implemented as separate layers of the neural network
     * and treated as parameters
     * This nodes should be removed after the DNN "skeleton" creation,
     * when all the information about connections between nodes is already available,
     * but connections are not set up
     * @param network network to be cleaned up
     * @return list of pure-data layers
     */
    private Vector<Layer> getPureDataLayers (Network network){
        Vector<Layer> pureDataLayers = new Vector<>();
        for(Map.Entry<String,ONNX.NodeProto> entry: _operationalNodes.entrySet()){
            if(_dataParameterNodes.containsKey(entry.getKey())){
                /** determine how many inputs are coming from another layers*/
                int fromAnotherLayers = 0;
                ONNX.NodeProto node = entry.getValue();
                for(String input: node.getInputList()){
                    if(_operationalNodesOutputs.containsKey(input))
                    fromAnotherLayers++;
                }
                if(fromAnotherLayers==0) {
                    Layer layer = network.getLayer(entry.getKey());
                    pureDataLayers.add(layer);
                }

            }
        }
        return pureDataLayers;
    }

  /** Remove pure data layers from DNN topology
     * Some of the nodes (e.g. Reshape Node) could contain only the Data information
     * In this case, the are not implemented as separate layers of the neural network
     * and treated as parameters
     * This nodes should be removed after the DNN "skeleton" creation,
     * when all the information about connections between nodes is already available,
     * but connections are not set up
     * @param network network to be cleaned up
     */
 public void removePureDataLayers(Network network) {
     Vector<Layer> pureDataLayers = getPureDataLayers(network);
     for (Layer layer : pureDataLayers)
         network.removeLayer(layer);
 }

    /**
     * Some of the nodes (e.g. Reshape Node) could contain only the Data information
     * In this case, the are not implemented as separate layers of the neural network
     * and treated as parameters
     * This nodes should be removed after the DNN "skeleton" creation,
     * when all the information about connections between nodes is already available,
     * but connections are not set up
     * @param network network to be cleaned up
     * @return list of pure-data layers
     */
    public void removePureDataNodes(Network network){
        Vector<ONNX.NodeProto> puredataNodes = getPureDataNodes();
        for(ONNX.NodeProto pureDataNode: puredataNodes)
            removePureDataNode(pureDataNode,network);
    }

    /**
     * Get list of ONNX.Nodes that contain only information, but were marked as operational nodes
     * @return list of ONNX.Nodes that contain only information, but were marked as operational nodes
     */
    private Vector<ONNX.NodeProto> getPureDataNodes(){
        Vector<ONNX.NodeProto> pureDataNodes = new Vector<>();

        for(Map.Entry<String,ONNX.NodeProto> entry: _operationalNodes.entrySet()){
            if(_dataParameterNodes.containsKey(entry.getKey())){
                /** determine how many inputs are coming from another layers*/
                int fromAnotherLayers = 0;
                ONNX.NodeProto node = entry.getValue();
                for(String input: node.getInputList()){
                    if(_operationalNodesOutputs.containsKey(input))
                    fromAnotherLayers++;
                }
                if(fromAnotherLayers==0) {
                    pureDataNodes.add(node);
                }

            }
        }
        return pureDataNodes;
    }

    /**
     * remove pure data node from operational intermediate info lists and
     * result network "skeleton"
     * @param node pure data node
     * @param network result network
     */
    private void removePureDataNode(ONNX.NodeProto node,Network network){
        String nodeName = _uniqueNamedONNXNodes.get(node);
        _operationalNodes.remove(nodeName);
        Layer correspondingLayer = findEspamLayerInMapping(node);
        _layersMapping.remove(correspondingLayer);
        network.removeLayer(correspondingLayer);

    //    for(String input: node.getInputList())
      //      _operationalNodesInputs.remove(input);

     //   for(String output: node.getOutputList())
       //     _operationalNodesOutputs.remove(output);
    }

    /**
     * Change the Tensor dimensions order from ONNX.NodeProto order
     * to espam.Tensor order
     *
     * ONNX Tensor order:
     * N x C x H x W, where
     * N - batch size
     * C - channels number
     * H - height
     * W - width
     *
     * Internal Tensor order:
     * W x H x <N1...Nn>, where
     * W - input width
     * H - input height (if input have height)
     * N1...Nn-1 - samples number in a sequence
     * Nn - channels number
     * n - format dimensionality
     *
     * example 1: W x H x N1 : N1 inputs W x H,
     * example 2: W x H x N1 x N2 : N2 sequences of N1 inputs W x H
     * example 3: W x N1 : N1 input vectors with length = W
     *
     * @return internal Tensor description of input data
     */
   private static Tensor convertDimsOrder(Tensor onnxFormatTensor)
   {   /**
           Convert tensors form  up to 4 dimensions.
           They are just reversed
      */
       if(onnxFormatTensor.getDimensionality()<5) {
           Tensor resultTensor = Tensor.reverse(onnxFormatTensor);
           return resultTensor;
       }

        /**
           Convert tensors from 0 and up to 4 dimensions.
           They are just reversed
        */
       else{
               Tensor resultTensor = new Tensor();
               /** reverse first 4 dims*/
               for(int i=3;i>=0;i--)
                   resultTensor.addDimension(onnxFormatTensor.getDimSize(i));

               /** add other dims*/
               for(int i = 4;i< onnxFormatTensor.getDimensionality();i++)
                   resultTensor.addDimension(onnxFormatTensor.getDimSize(i));

               return resultTensor;
           }
   }

    /**
     * Return current node name, if it is not empty.
     * otherwise create and return default node name.
     * @param node ONNX.NodeProto
     * @return non-empty node name
     */
    private String generateNonEmptyName(ONNX.NodeProto node){
        String nonEmptyName = node.getName();
        if(isEmptyName(nonEmptyName) || _giveReadableNames) {

            if(node.getOpType().equals("Constant"))
                return getConstantNodeNonEmptyName(node);

            nonEmptyName = "node_" + node.getOpType() + _nextUniqueNodeId;
            _nextUniqueNodeId++;
        }

        return nonEmptyName;
    }

    private String getConstantNodeNonEmptyName(ONNX.NodeProto node){
        ProtocolStringList outputs = node.getOutputList();
        if(outputs.size()==0){
            String name = "node_" + node.getOpType() + _nextUniqueNodeId;
            _nextUniqueNodeId++;
            return name;
        }

        return outputs.get(0);
    }

        /**
     * Return current valueProto name, if it is not empty.
     * otherwise create and return default valueProto name.
     * @param node ONNX.NodeProto
     * @return non-empty node name
     */
    private String generateSpecialName(ONNX.NodeProto node, String name){
        String nonEmptyName = node.getName();
        if(isEmptyName(nonEmptyName)) {
            nonEmptyName = "node_" + node.getOpType() +_nextUniqueNodeId;
            _nextUniqueNodeId++;
        }

        nonEmptyName+=name;
        return nonEmptyName;
    }

     /**
     * Return current valueProto name, if it is not empty.
     * otherwise create and return default valueProto name.
     * @param valueInfo ONNX.ValueInfoProto
     * @return non-empty node name
     */
    private String generateNonEmptyName(ONNX.ValueInfoProto valueInfo){
        String nonEmptyName = valueInfo.getName();
        /** if node, provided by ONNX Model have empty name,
         * create default name
         */
        if(isEmptyName(nonEmptyName)) {
            nonEmptyName = "value_" + _nextUniqueNodeId;
            _nextUniqueNodeId++;
        }

        return nonEmptyName;
    }

    /**
     * Return current valueProto name, if it is not empty.
     * otherwise create and return default valueProto name.
     * @param valueInfo ONNX.ValueInfoProto
     * @return non-empty node name
     */
    private String generateSpecialName(ONNX.ValueInfoProto valueInfo, String name){
        String nonEmptyName = valueInfo.getName();
        /** if node, provided by ONNX Model have empty name,
         * create default name
         */
        if(isEmptyName(nonEmptyName)) {
            nonEmptyName = "value_" + _nextUniqueNodeId;
            _nextUniqueNodeId++;
        }

        nonEmptyName += name;

        return nonEmptyName;
    }

    /**
     * Return current Tensor name, if it is not empty or default-generated node name otherwise
     * @param tensorProto ONNX.TensorProto
     * @return non-empty node name
     */
    private String generateNonEmptyName(ONNX.TensorProto tensorProto){
        String nonEmptyName = tensorProto.getName();
        /** if node, provided by ONNX Model have empty name,
         * create default name
         */
        if(isEmptyName(nonEmptyName)) {
            nonEmptyName = "tensor_" + _nextUniqueNodeId;
            _nextUniqueNodeId++;
        }

        return nonEmptyName;
    }

    /**checks, if the node's name is empty
     * @param name name to be inspected
     * @return true, if the name is empty and false otherwise
     */
    private boolean isEmptyName(String name){
        if(name.equals(""))
            return true;

        return false;
    }

    /**
     * Resolve multiple inputs or DenseBlock
     * @param layerName layer name
     * @return single input of DenseBlock
     */
    public String getDenseBlockSingleInput(String layerName){
        return _denseInputs.get(layerName);
    }

    /**
     * Set save weights flag
     * @param saveWeights save weights flag
     */
    public void setSaveWeights(boolean saveWeights) { this._saveWeights = saveWeights; }

    /**
     * Set the path, where numpy weights should be saved
     * @param weightsSavePath path, where numpy weights should be saved
     */
    public void setWeightsSavePath(String weightsSavePath) {
       this._weightsSavePath = weightsSavePath;
        // this._weightsSavePath = weightsSavePath.replace("./",Config.getInstance().getAppPath()+File.separator);
    }

    /** if the details of weights saving should be printed*/
    public void setSaveWeightsVerbose(boolean saveWeightsVerbose) {
        this._saveWeightsVerbose = saveWeightsVerbose;
    }

    /**
     * Set path to ONNX model
     * @param modelPath  path to ONNX model
     */
    public void setModelPath(String modelPath){
        _modelPath = modelPath;
    }

  ///////////////////////////////////////////////////////////////////
  ////                     private variables                     ///
  /** Helper, used for naming ONNX nodes with empty names*/
  private  int _nextUniqueNodeId = 0;

  /** ONNX Nodes, ValuesProto and Tensors with unique names*/
  private HashMap<GeneratedMessage, String> _uniqueNamedONNXNodes;

  /** Mapping of ONNX nodes onto espam.cnn Layers*/
  private HashMap<Layer,GeneratedMessage> _layersMapping;

  /** Current onnx Model graph*/
  private ONNX.GraphProto _onnxModelGraph;

  /**
  * ONNX model initializers contains list of weights and other data parameters, sent to
  * hidden layers of model
  */
  private HashMap<String,ONNX.TensorProto> _onnxModelInitializers;

  /** list of weights initializers */
  private HashMap<String,String> _convWeightsInits;
  private HashMap<String,String> _denseWeightsInits;

  /** list of bias initializers */
  private HashMap<String,String> _biasInits;

  /** list of batchnormalization initializers */
  private HashMap<String,Vector<String>> _bnInits;

  /** list of weights nodes */
  private HashMap<String,String> _convWeightsNodeInits;
  private HashMap<String,String> _denseWeightsNodeInits;

  /** number of neurons in dense blocks*/
  private HashMap<String,Integer> _denseNeurons;

  /** list of bias initializers */
  private HashMap<String,String> _biasNodeInits;

  /** Data shapes, extracted from ONNX.ModelProto */
  private HashMap<String,Tensor> _extractedDataFormats;

  /** Operational nodes are translated into hidden Layers*/
  private HashMap<String,ONNX.NodeProto> _operationalNodes;

  /** Parameter nodes are used for extracting the additional information*/
  private HashMap<String,ONNX.NodeProto> _parameterNodes;

  /**Nodes, contains additional constant data parameters*/
  private HashMap<String,ONNX.NodeProto> _dataParameterNodes;

  /** Outputs of operational nodes.used for connections revealing */
  private HashMap<String,ONNX.NodeProto> _operationalNodesOutputs;

  /** Inputs of operational nodes.used for connections revealing.*/
  private HashMap<String,ONNX.NodeProto> _operationalNodesInputs;

  /** List of all nodes that contain weights*/
  private Vector<String> _nodesWithWeights;

  /**
   *  List of single DenseBlock inputs, used while resolving
   *  Multiple MatMul/Gemm inputs in ONNX.NodeProto --> single
   *  input in espam Dense Layer. Contains mapping
   *  <Layer/node name, single dynamic input_name>
   * */
  private HashMap<String,String> _denseInputs;

  /**
   * List of INPUT and OUTPUT layers names
   * used for connections revealing.
   */
  private Vector<String> _ILayersNames;
  /**
   * List of INPUT and OUTPUT layers names
   * used for connections revealing.
   */
  private Vector<String> _OLayersNames;

  /**
  * Outputs of operational nodes and input data layers. Used for connections revealing.
  * Contains mapping <Output_name, output_owner(node)_name>
  */
  private HashMap<String,String> _onnxNodesOutputs;

  /**
  * Outputs of parameter nodes. Used for extracting the additional information
  */
  private HashMap<String,ONNX.NodeProto> _parameterNodesOutputs;

  /**
  * Outputs of data parameter nodes. Used for extracting the additional information
  */
  private HashMap<String,ONNX.NodeProto> _dataParameterNodesOutputs;

  /**ONNX graph inputs*/
  private List<ONNX.ValueInfoProto> _onnxGraphInputs;

  /**ONNX graph outputs*/
  private List<ONNX.ValueInfoProto> _onnxGraphOutputs;

  /**
   * Pads
   * Padding for the beginning and ending along each axis, it can take any value >= 0
   * in format [x1_begin, x2_begin...x1_end, x2_end,...],
   * for ONNX Processing Nodes pads are in format
   * [delta_height (top),delta_height(bottom), delta_width(left), delta_width *right]
   * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
   * the number of pixels added at the end of axis `i`.
   * This attribute cannot be used simultaneously with auto_pad attribute.
   * If not present, the padding defaults to 0 along start and end of each axis.
   */
  HashMap<String,int[]> _pads;

  /**
  * Default number of neurons in a layer
  */
  private int _default_neurons_number = 1;

  /** DNN model weights type (float by default)*/
  private String _modelDataType = "float";

  /** DNN model data type (float by default)*/
  private String _modelWeightsType = "float";

  /** onnx weights extractor*/
  private ONNXWeightsExtractor _weightsExtractor = new ONNXWeightsExtractor();

  /** if weights should be extracted from the onnx model*/
  private boolean _saveWeights = false;

  /** if details of weights extraction should be printed*/

  private boolean _saveWeightsVerbose = false;

  /** path to java temporary files directory*/
  private String _tempDirRelPath = System.getProperty("java.io.tmpdir");

  /** path for saving the weights*/
  private String _weightsSavePath = "./";

  /** ONNX model path*/
  private String _modelPath = null;

  /** give layers readable names*/
  /** TODO: make a parameter!*/
  private boolean _giveReadableNames = true;

  /** non-default output model name*/
  private String _outModelName;
}
