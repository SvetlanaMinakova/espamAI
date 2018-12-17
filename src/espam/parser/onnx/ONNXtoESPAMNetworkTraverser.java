package espam.parser.onnx;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.DataContainer;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import onnx.ONNX;

import java.util.*;
/**
 * Class that implements traverse of Network graph, obtained from ONNX mapping
 */
public class  ONNXtoESPAMNetworkTraverser extends NetworkTraverser {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public  ONNXtoESPAMNetworkTraverser(Network network, ONNX2CNNConverter converter) {
        _converter = converter;
         initialize(network);
    }

    /**
     * get layers traverse order
     */
    public Vector<Integer> getLayersTraverseOrder() {
        _layersTraverseOrder = new Vector<Integer>();

        if (_network == null) {
            System.err.print("Connections traverse could not be performed for a null network");
            return _layersTraverseOrder;
        }
        return getLayersTraverseOrder(_network.getInputLayerId());
    }

     /**
     * get layers traverse order
     * @param startLayerId start node for DNN graph traverse
     * @return layers traverse order
     */
     @Override
    public Vector<Integer> getLayersTraverseOrder(int startLayerId) {
        _layersTraverseOrder = new Vector<Integer>();
        int startLayerTraverseId = getTraverseLayerId(startLayerId);
        modifiedBFS(startLayerTraverseId);
        /** TODO check!*/
       // appendDataLayersDistinctTotraverseOrder();
        mapLayersTraverseOrderOntoRealIds();
        return _layersTraverseOrder;
    }

     /**
     * Set up connections between layers of espam.cnn.Network
     * according to current ONNX model
     * TODO: implement connections setup
     * TODO: For now this is last, interface-like function of the Traverser!
     * TODO it should be performed after Data Extraction and traverseOrder search!
     */
    public void setConnections(Vector<Integer> layersTraverseOrder){
         _network.setConnections(new Vector<>());
        for(Integer layerId: layersTraverseOrder){
            addOutputConnections(layerId);
        }
    }

    /** TODO uses connections information
     * Find all inputs of the network
     * TODO uses _network parameter
     * traverser.setupConnections(layersTraverseOrder);
     */
    public void setDataFormats (Vector<Integer> layersTraverseOrder) {

        for(Integer layerId: layersTraverseOrder) {
           boolean isDataFormatSet = setDataFormats(layerId);

           if(!isDataFormatSet)
                System.err.println(_network.getLayer(layerId).getName() + " inputs set up error");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                    ////

    /**
     * initialize traverser
     * @param network DNN to be traversed
     */
    private void initialize(Network network) {
      _network = network;

        _layerTraveseIds = new HashMap<>();
        int traverseId = 0;
        for(Layer layer:_network.getLayers()) {
            _layerTraveseIds.put(traverseId,layer.getId());
            traverseId++;
        }

        _n = _layerTraveseIds.size();

        /**initialize linked list*/
        adj = new ArrayList[_n];
        for (int i = 0; i < _n; ++i) {
            adj[i] = new ArrayList();
        }

        /** extract connections*/
        Vector<IndexPair> connectionsDistinct = new Vector<>();

        for (Layer layer : network.getLayers()) {
            extractConnectionsDistinct(layer,connectionsDistinct);
        }
       _connectionsGroupedBySrc = IndexPair.groupByFirstIndex(connectionsDistinct);
       _connectionsGroupedByDst = IndexPair.groupBySecondIndex(connectionsDistinct);

       _m = connectionsDistinct.size();
       IndexPair currentConnection;

         for (int i = 0; i < _m;i++) {

         currentConnection = connectionsDistinct.elementAt(i);

         int v = currentConnection.getFirst();
         int w = currentConnection.getSecond();

         /** directed graph*/
         adj[v].add(w);
         // adj[w].add(v); //for undirected graphs
         }

         used = new boolean[_n];
         Arrays.fill(used, false);
         queue = new LinkedList();
    }

     /**
     * Data layers could be unreachable by sorting algorithms,
     * but they must be presented in the resulting DNN.
     * In case of missing data nodes, they are added manually
     */
    private void appendDataLayersDistinctTotraverseOrder(){
        Vector<Integer> dataLayersIds = new Vector<>();
        for(Layer layer:_network.getLayers()){
            espamDNNLayersFilter.filterDataLayers(layer,dataLayersIds);
        }
        /** Append additional data layers after input layer.
         * Input layer should have id = 0*/
        for (Integer dataLayerId :dataLayersIds){
            int traverselayerId = getTraverseLayerId(dataLayerId);
            if(!_layersTraverseOrder.contains(traverselayerId)){
                _layersTraverseOrder.insertElementAt(traverselayerId,1);
            }
        }

    }

    /**
     * Add output layer connections to result DNN
     * @param srcId connections-source layer Id
     */
    private void addOutputConnections(int srcId){
        String srcName = "undefined";
        try {
            Layer src = _network.getLayer(srcId);
            srcName = src.getName();

            Vector<Integer> dstIds = _connectionsGroupedBySrc.get(srcId);
            if(dstIds!=null) {

                for (Integer dstId : dstIds) {
                    Layer dst = _network.getLayer(dstId);

                    if(dst.getNeuron() instanceof  ConnectionDependent) {
                       ((ConnectionDependent) dst.getNeuron()).recalculateNeuronsNumber(dst,src);
                    }

                    _network.addConnection(src, dst);
                }
            }
        }
        catch (Exception ex){
            System.err.println("output connections adding error, source layer Id: "+srcId +" , source layer name: "+srcName);
        }
    }

    /** Extract distinct connections for specified layer
     * @param layer espam.cnn Layer
     */
    private void extractConnectionsDistinct(Layer layer,Vector<IndexPair> connectionsList){
        Vector<Layer> inputs = getInputLayers(layer);
        for (Layer input: inputs)
            appendConnectionDistinct(input,layer,connectionsList);

        Vector<Layer> outputs = getOutputLayers(layer);
        for (Layer output: outputs)
            appendConnectionDistinct(layer,output,connectionsList);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                   ////

    /**
     * Return list of layer's inputs
     * I/O DNN layers are not processed,
     * connections to this nodes are extracted from _operationalNodes I/O connection lists
     * @param layer layer to inspect
     * @return list of names of layer inputs
     */
    private Vector<Layer> getInputLayers(Layer layer) {
        Vector<Layer> inputLayers = new Vector<>();

        ONNX.NodeProto node = _converter.findONNXOperationalNode(layer);
        if (node != null) {
            for(String input: node.getInputList())
                espamDNNLayersFilter.filterSrcLayers(input,inputLayers,_network);
            return inputLayers;
        }
        return inputLayers;
    }

    /**
     * Return list of layer's outputs
     * I/O DNN layers are not processed,
     * connections to this nodes are extracted from _operationalNodes I/O connection lists
     * @param layer layer to inspect
     * @return list of names of layer outputs
     */
    private Vector<Layer> getOutputLayers(Layer layer) {
        Vector<Layer> outputLayers = new Vector<>();
        ONNX.NodeProto node = _converter.findONNXOperationalNode(layer);

        if (node != null) {
            for(String input: node.getOutputList())
                espamDNNLayersFilter.filterDstLayers(input,outputLayers,_network);
            return outputLayers;
        }
        return outputLayers;
    }

    /**
     * set data formats of a specified layer
     * @param layerId layer Id
     * @return true if data formats were sucessfully set and false otherwise
     */
    private boolean setDataFormats (int layerId) {
        Layer layer = _network.getLayer(layerId);

       // System.out.println(layer.getName());

        if(layerId==_network.getInputLayerId() || layerId==_network.getOutputLayerId()) {
            boolean result = checkIOLayerDataFormat(layer);
           // System.out.println("    I/O data format: "+layer.getOutputFormat());
            return result;
        }

        Vector<String> layerInputs = _converter.getLayerInputs(layer);

        if (layer.getNeuron() instanceof MultipleInputsProcessor) {
            boolean result = setMultipleInputsDataFormat(layer, layerInputs);
           // System.out.println("    input: "+layer.getInputFormat()+" output: "+layer.getOutputFormat());
            return result;
        }
        else{
            if(layer.getNeuron() instanceof DenseBlock){
                String singleInput = _converter.getDenseBlockSingleInput(layer.getName());
                if(singleInput==null)
                    return false;
                layerInputs = new Vector<>();
                layerInputs.add(singleInput);
            }

            boolean result = setSingleInputDataFormat(layer,layerInputs);
           // System.out.println("    input: "+layer.getInputFormat()+" output: "+layer.getOutputFormat());
            return result;
        }
    }

     /**
     * Input/output DNN layers data formats are set up during node extraction as node parameters.
     * There is no need to reset these data formats, but they should not be null
     * @param IOLayer input or output DNN layer
     * @return true, if input layer's data format was successfully set up
     */
    private boolean checkIOLayerDataFormat(Layer IOLayer){
        Tensor dataFormat = IOLayer.getOutputFormat();
            if(Tensor.isNullTensor(dataFormat))
                return false;
            else
                return true;
    }

    /**
     * Set up input of the layer, if layer have a single input
     * @param layer espam.cnn. Layer
     * @param layerInputs possible inputs of the layer
     * @return true, if input was successfully set up and false otherwise
     */
    private boolean setSingleInputDataFormat(Layer layer, Vector<String> layerInputs){

        /** First try to extract data Format from connections*/
        for(String layerInput: layerInputs) {
            if (isExtractableFromConnections(layer, layerInput)) {
                Tensor dataFormat = tryExtractLayerInputDataFormatFromConnection(layer, layerInput);
               // System.out.println("Layer " + layer.getName() + " input data format = " + dataFormat);
                return trySetUpLayerInputDataFormatFromConnection(layer,layerInput);
            }
        }

        /** If dataFormat was not found, search in data parameters*/
         for(String layerInput: layerInputs) {
             if (isExtractableFromParams(layer, layerInput)) {
                 Tensor dataFormat = tryExtractLayerInputDataFormatFromParamNode(layer, layerInput);
                // System.out.println("Layer " + layer.getName() + " input data format = " + dataFormat);
                 return trySetLayerInputDataFormatFromParamNode(layer,layerInput);
             }
         }
         /** If data format was not found neither in connections, nor in parameters, return false*/
        return false;
    }

        /**
     * Set up input of the layer, if layer have multiple inputs
     * @param layer espam.cnn. Layer
     * @param layerInputs inputs of the layer
     * @return true, if inputs were successfully set up and false otherwise
     */
    private boolean setMultipleInputsDataFormat(Layer layer, Vector<String> layerInputs){

    //    Vector<Tensor> inputDataFormats = new Vector<>();
    //    Vector<Layer> inputOwners = new Vector<>();
        try {

        MultipleInputsProcessor mun = ((MultipleInputsProcessor) layer.getNeuron());
        mun.getInputs().clear();
        for(String layerInput: layerInputs) {

            if(isExtractableFromConnections(layer,layerInput)) {
                    Tensor dataFormat = tryExtractLayerInputDataFormatFromConnection(layer,layerInput);
                        Layer inputOwner = getInputOwner(layerInput);
                         if(dataFormat!=null)
                             mun.addInput(inputOwner);

                }
               /** else {
                    Tensor dataFormat = tryExtractLayerInputDataFormatFromParamNode(layer, layerInput);
                    if(dataFormat!=null)
                        mun.addInput(dataFormat);
                       // inputOwners.add(null);
                }*/
        }

        mun.setDataFromMultipleInputs(layer);
                return true;
        }

        catch (Exception ex){
            System.err.println(layer.getName() + " layer Data Formats setup error. " + ex.getMessage());
            return false;

        }
    }

    /**
     * Checks, if data format could be extracted from layer input connections
     * @param layer layer cnn.espam Layer
     * @param input input name
     * @return true, if data could be extracted and false otherwise
     */
    private boolean isExtractableFromParams(Layer layer, String input){
        /** Search in parameter nodes */
       Tensor dataFormat = _converter.getExtractedDataFormat(input);
        if(dataFormat!=null) {
            return true;
        }
        return false;
    }

    /**
     * Checks, if data format could be extracted from layer input connections
     * @param layer layer cnn.espam Layer
     * @param input input name
     * @return true, if data could be extracted and false otherwise
     */
    private boolean isExtractableFromConnections(Layer layer, String input) {
        Layer src = getInputOwner(input);
        if(src==null){ return false; }

        Connection connection = _network.findConnection(src.getId(),layer.getId());
        if(connection==null){ return false; }

        return true;
    }

    /**
     * Tries to extract layer input dataFormat from parameter node
     * @param layer espam Layer
     * @param input layer's input name
     * @return extracted DataFormat or null
     */
    private Tensor tryExtractLayerInputDataFormatFromParamNode(Layer layer, String input){
        /** Search in parameter nodes */
       Tensor dataFormat = _converter.getExtractedDataFormat(input);
       if(Tensor.isNullTensor(dataFormat)){
           System.out.println("Parameter "+input+" extraction error");
       }
       return dataFormat;
    }


    /**
     * Tries to set layer input dataFormat from parameter node
     * @param layer espam Layer
     * @param input layer's input name
     * @return true, if input format was set up sucessfully and false otherwise
     */
    private boolean trySetLayerInputDataFormatFromParamNode(Layer layer, String input){
        /** Search in parameter nodes */
       Tensor dataFormat = _converter.getExtractedDataFormat(input);
        if(Tensor.isNullTensor(dataFormat)) {
           System.out.println("Parameter "+input+" extraction error");
            return false;
        }

       // dataFormat = Tensor.addPads(dataFormat,layer.getPads());
        _network.updateDataFormatsFromSingleDataNode(layer,dataFormat);
        return true;
    }

    /**
     * Try extract data format from layer input connections
     * @param layer layer cnn.espam Layer
     * @param input input name
     * @return Tensor, if data format was sucessfully extracted and null otherwise
     */
    private Tensor tryExtractLayerInputDataFormatFromConnection(Layer layer, String input) {

        Layer src = getInputOwner(input);
            if(src==null){
                System.err.println("Data formats update error: connection "+input+" source not found");
                return null;
            }

            Connection connection = _network.findConnection(src.getId(),layer.getId());
            if(connection==null){
                System.err.println("Input "+input+" of "+layer.getName() +" layer not found");
                return null;
            }
        return src.getOutputFormat();
    }

    /**
     * Try extract data format from layer input connections
     * @param layer layer cnn.espam Layer
     * @param input input name
     * @return Tensor, if data format was successfully extracted and null otherwise
     */
    private boolean trySetUpLayerInputDataFormatFromConnection(Layer layer, String input) {
        Layer src = getInputOwner(input);
        if(src==null){
            System.err.println("Data formats update error: connection "+input+" source not found");
            return false;
        }

        Connection connection = _network.findConnection(src.getId(),layer.getId());
            if(connection==null){
             System.err.println("Input "+input+" of "+layer.getName() +" layer not found");
                return false;
            }


        Tensor layerInputFormat = connection.getSrc().getOutputFormat();
       // layerInputFormat = Tensor.addPads(layerInputFormat,connection.getDest().getPads());

        _network.updateDataFormats(connection,layerInputFormat);
            return true;
    }

    /**
     * Get input owner
     * @param input input link name
     * @return input owner or null
     */
    private Layer getInputOwner(String input) {

        /** Search in graph Inputs*/
        if (_converter.getILayersNames().contains(input)) {
           return _network.getLayer(input);
        }

        /** Search in operational nodes outputs */
        if (_converter.getOperationalNodesOutputs().keySet().contains(input)) {
            ONNX.NodeProto onnxNode = _converter.getOperationalNodesOutputs().get(input);
            Layer src = _converter.findEspamLayerInMapping(onnxNode);
            return src;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                   ////

    /**Converter with extracted ONNX.Model - related information*/
    ONNX2CNNConverter _converter;

    ///////////////////////////////////////////////////////////////////
    ////                     node-type filters                    ////
    /**
     * Return ONNX DNN nodes, mapped to espam.DNN layers
     * TODO operates over real layer Ids!
     */
    private Filtreable espamDNNLayersFilter = new Filtreable() {
        @Override
        public void filterSrcLayers(String input, Vector<Layer> filtered, Network network) {
                Layer inputOwner = _converter.findConnectionSrc(input,network);
                if (inputOwner != null)
                    filtered.add(inputOwner);
        }

        @Override
        public void filterDstLayers(String output, Vector<Layer> filtered, Network network) {
            Layer outputReceiver = _converter.findConnectionDst(output,network);
                if (outputReceiver != null)
                    filtered.add(outputReceiver);

        }

        @Override
        public void filterDataLayers(Layer layer, Vector<Integer> filteredIds) {
            if(layer.getNeuron() instanceof DataContainer)
                filteredIds.add(layer.getId());
        }
    };
}