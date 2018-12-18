package espam.datamodel.graph.cnn;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.connections.*;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.CustomConnectionGenerator;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.arithmetic.Add;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CNNGraphVisitor;

import java.util.Iterator;
import java.util.Vector;

/**
 * Class of Neural Network (NN)
 * TODO make reference to some original paper about NNs
 */

public class Network implements Cloneable, ReferenceResolvable {
     ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Empty Constructor to create new NN (required for parsers)
     */
    public Network() {
        setName("network");
        _layers = new Vector<Layer>();
        _connections=new Vector<Connection>();
    }

    /**
     * Constructor to create new NN with specified name
     * @param name name of the new NN
     */
    public Network(String name) {
        setName(name);
        _layers = new Vector<Layer>();
        _connections=new Vector<Connection>();
    }

    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     *  Clone this Network
     * @return  a new reference on the Network
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Network newObj = (Network) super.clone();
            newObj.setName(_name);
            newObj.setLayers((Vector<Layer>)_layers.clone());
            newObj.setConnections((Vector<Connection>)_connections.clone());
            newObj.setInputLayer((Layer)_inputLayer.clone());
            newObj.setOutputLayer((Layer)_outputLayer.clone());
            newObj.setInputLayerId(_inputLayerId);
            newObj.setOutputLayerId(_outputLayerId);
            newObj.setDataType(_dataType);
            newObj.setWeightsType(_weightsType);
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     * Create a deep copy of this Network
     * @param network network to be copied
     */
     public Network(Network network) {
        setName(network._name);

        /** copy layers*/
        _layers = new Vector<>();
        for(Layer layer: network._layers)
            _layers.add(new Layer(layer));

        /** copy connections and resolve references on the layers of new network*/
         _connections = new Vector<>();
         for(Connection connection: network._connections) {
             if(connection instanceof Custom)
                _connections.add(Custom.copyConnection((Custom)connection));
             else
                _connections.add(Connection.copyConnection(connection));

             resolveConnectionReference(_connections.lastElement());
         }

         /** set the rest of the parameters*/
         setInputLayerId(network._inputLayerId);
         setOutputLayerId(network._outputLayerId);
         resolveInputLayerReference();
         resolveOutputLayerReference();

         _dataType = network._dataType;
         _weightsType = network._weightsType;
    }

    /**
    * Compares Network with another object
    * @param obj Object to compare this Network with
    * @return true if Network is equal to the object and false otherwise
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

       Network network = (Network)obj;
         return _name.equals(network._name)
              &&_inputLayerId == network._inputLayerId
              &&_outputLayerId == network._outputLayerId
              &&_inputLayer.equals(network._inputLayer)
              &&_outputLayer.equals(network._outputLayer)
              && isLayersListEqual(network._layers)
              && isConnectionListEqual(network._connections);
       }

     ///////////////////////////////////////////////////////////////////
    ////                  data flow calculation                    ////

    /**
     * Clear data formats of CNN
     */
    public void clearDataFormats() {
        for(Layer layer:_layers)
            layer.clearDataFormats();
    }

    /**
     * Update dnn data formats, using existing dnn input layer
     * @return true, if data formats were successfully updated and
     * false otherwise
     */
    public boolean updateDataFormats(){
        if(_inputLayer==null)
            return false;
        Tensor inputDataFormat = _inputLayer.getOutputFormat();
        if(Tensor.isNullOrEmpty(inputDataFormat))
            return false;
        setDataFormats(inputDataFormat);
        return true;

    }
    /**
     * Set new data formats to CNN
     * @param inputDataFormat input data format for CNNs input layer
     */
     public void setDataFormats(Tensor inputDataFormat){
         if(Tensor.isNullOrEmpty(inputDataFormat)){
            System.err.println("Invalid input data format "+ inputDataFormat);
         return;
         }

        initializeInputLayer(inputDataFormat);
        sortConnectionsInTraverseOrderFromTop();

        for(Connection connection: _connections){
            updateDataFormats(connection);
        }
    }

    /**
     * TODO refactoring for input dependent layers
     * Initialize input layer of the network by specified data format
     * @param inputDataFormat input data format
     */
    private void initializeInputLayer (Tensor inputDataFormat){

        Tensor input = inputDataFormat;
        Neuron inputLayerNeuron = _inputLayer.getNeuron();
        if(inputLayerNeuron instanceof ConnectionDependent) {
            input = input.getSubTensor(inputLayerNeuron.getSampleDim());
        }

        inputLayerNeuron.setInputDataFormat(input);
        inputLayerNeuron.setOutputDataFormat(inputLayerNeuron.calculateOutputDataFormat(input));
        _inputLayer.updateDataFormatsFromTop(input,input);
    }

    /**
     * Update data formats (from top to bottom) for one DNN connection
     * @param connection DNN connection
     */
    public void updateDataFormats(Connection connection){
        Layer src = connection.getSrc();
        Layer dest = connection.getDest();
        Tensor destNeuronInputFormat = src.getNeuron().getOutputDataFormat();
        Tensor destLayerInputFormat = src.getOutputFormat();

        dest.updateDataFormatsFromTop(destNeuronInputFormat, destLayerInputFormat);
    }

    /**
     * TODO check
     * Get inputs number of the connection destination layer
     * @param connection connections
     * @return inputs number of the connection destination layer
     */
    private int getDstInputsNum(Connection connection){
        return connection.getSrc().getNeuronsNum();
    }

    /**
     * Update data formats (from top to bottom) for one DNN connection with
     * specified layer input format
     * @param connection DNN connection
     * @param layerInputFormat layer input format
     */
    public void updateDataFormats(Connection connection, Tensor layerInputFormat){
        Layer src = connection.getSrc();
        Layer dest = connection.getDest();

        Tensor destLayerInputFormat = layerInputFormat;
        Tensor destNeuronInputFormat = new Tensor(layerInputFormat);
        /** TODO check if Neurons Number - responsible dimension is always last */
        if(src.getNeuronsNum()>1) {
          //  destNeuronInputFormat.setChannelsNum(src.getNeuronsNum());
            destNeuronInputFormat.removeDimension();
        }
        dest.updateDataFormatsFromTop(destNeuronInputFormat, destLayerInputFormat);
    }

     /**
     * Update layer data formats (from top to bottom) from single data container
     * @param layer DNN Layer
     * @param layerInputFormat DNN Layer's new input format
     */
    public void updateDataFormatsFromSingleDataNode(Layer layer, Tensor layerInputFormat){
        layer.updateDataFormatsFromTop(layerInputFormat, layerInputFormat);
    }

    /**
     * Shrinks current data formats to minimal
     * @param keepHeightDependency if all the output layers should be taken into account
     * while DataFlow calculation if performed
     */
    public void minimizeDataFlow(boolean keepHeightDependency){
        int defaultMinOutputDataHeight = 1;
        setMinDataHeights(defaultMinOutputDataHeight, keepHeightDependency);
    }

    /**
     * set all data heights to min
     */
    public void setMinDataHeights(int minOutputDataHeight, boolean keepHeightDependency){
        _outputLayer.updateMinDataHeight(minOutputDataHeight);
        Vector<Layer> layersToTraverse = getLayersInTraverseOrderFromBottom();
        if(_inputLayer.getNeuron() instanceof Data)
            layersToTraverse.remove(_inputLayerId);
        for(Layer layer: layersToTraverse) {
                updateUpcomingLayersDataHeights(layer, keepHeightDependency);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                  consistency check out                    ////
    /**
     * Checks if the Network model is consistent by
     * 1) comparing input and output data formats of each connected
     *    pair of layers
     * 2)checking if there are no null or '-' outputs
     * @return true, if network is consistent and false otherwise
     */
    public boolean checkConsistency() {
        for (Connection con : _connections) {

            if (!_isConsistent(con))
                return false;

            if (isDenseToConv(con)){
                return false;
            }
        }

     /**   try{
            setDataFormats(_inputLayer.getOutputFormat());
        }
        catch (Exception e){
            System.out.println("data formats setup inconsistency");
            return false;
        } */

        for(Layer layer: _layers){
            if(!layer.isDataFormatsConsistent()){
                return false;
            }
        }

       return true;
    }

    /**
     * TODO REFACTORING
     * Checks if connections is type of DenseBlock--> Convolution
     * @return true, if connections is type of DenseBlock--> Convolution and false otherwise
     */
    private  boolean isDenseToConv(Connection con){
        Neuron srcNeuron = con.getSrc().getNeuron();
        if(srcNeuron instanceof Data)
            return false;

        Neuron dstNeuron = con.getDest().getNeuron();

        if(srcNeuron.getSampleDim()<2 && dstNeuron instanceof CNNNeuron) {
          //  System.out.println("Dense to conv connection found: "+srcNeuron.getName() +"-->"+dstNeuron.getName());
            return true;
        }

        return false;
    }


    /**
     * Checks if the Network connection is consistent by comparing input and output data formats
     * @param con connection to be checked
     * @return true, if network is consistent and false otherwise
     */
    private boolean _isConsistent(Connection con){
        Tensor srcOutputFormat = con.getSrc().getOutputFormat();
        Tensor destInputFormat = con.getDest().getInputFormat();

        if(!Tensor.isHaveSameElementsNumber(srcOutputFormat,destInputFormat)) {
            System.out.println("Consistency fault: "+con.getSrcName()+" --> "+con.getDestName());
            System.out.println(srcOutputFormat+" --> "+destInputFormat);
            return false;
        }
        return true;
    }

    /**
     * Returns debug information about non-consistent connections
     * @return debug information about non-consistent connections
     */
    public void printNonConsistent() {

        for(Connection con : _connections) {
           if(!isConsistent(con)) {
               System.out.println("Consistency fault: "+con.getSrcName()+" --> "+con.getDestName());

                 /** print non-consistent formats*/
               Tensor srcOutputFormat = con.getSrc().getOutputFormat();
               Tensor destInputFormat = con.getDest().getInputFormat();

               String srcOutputFormatDesc, destInputFormatDesc;

               if (Tensor.isNullOrEmpty(srcOutputFormat))
                   srcOutputFormatDesc = "null or empty ";
               else srcOutputFormatDesc = srcOutputFormat.toString();

               if (Tensor.isNullOrEmpty(destInputFormat))
                   destInputFormatDesc = "null or empty ";
               else destInputFormatDesc= destInputFormat.toString();

               System.out.println(srcOutputFormatDesc+" --> "+srcOutputFormatDesc);
               }
           }
    }

    /**
     * Returns debug information about consistent connections
     * @return debug information about consistent connections
     */
    public void printConsistent() {
        for(Connection con : _connections) {
            Tensor srcOutputFormat = con.getSrc().getOutputFormat();
            Tensor destInputFormat = con.getDest().getInputFormat();
           if(isConsistent(con)) {
               System.out.println("Consistent connection: "+con.getSrcName()+" --> "+con.getDestName());
               System.out.println(srcOutputFormat+" --> "+destInputFormat);
               }
           }
    }

    /**
     * Checks if connection is consistent
     * @param connection connection to be checked
     * @return true, if connection is consistent and false otherwise
     */
    public boolean isConsistent(Connection connection){
        Layer src = connection.getSrc();
        Layer dst = connection.getDest();

        if(src==null || dst==null)
            return false;
        if(Tensor.isNullOrEmpty(src.getOutputFormat()) || Tensor.isNullOrEmpty(dst.getInputFormat()))
            return false;

        Neuron dstNeuron = dst.getNeuron();
        if(dstNeuron instanceof  MultipleInputsProcessor)
            return ((MultipleInputsProcessor) dstNeuron).isAcceptableInput(src.getOutputFormat());

        return Tensor.isHaveSameElementsNumber(src.getOutputFormat(),dst.getInputFormat());
    }

     ///////////////////////////////////////////////////////////////////
    ////                  layers processing                        ////
       /**
     * Attaches input DATA layer to existing neural network
     * @param network neural network
     * @param inputDataFormat input data format
     * @param name name of the input layer
     */
    public static void addInputLayer(Network network, Tensor inputDataFormat, String name){
            Data inputNeuron = new Data(DataType.INPUT,inputDataFormat);
            network.stackLayerTop(name,inputNeuron,1);
    }

    /**
     * Attaches output DATA layer to existing neural network
     * @param network neural network
     * @param name output layer name
     */
    public static void addOutputLayer(Network network, String name){
        Data outputNeuron = new Data(DataType.OUTPUT);
        network.stackLayer(name,outputNeuron,1);
        network.setOutputLayer(network.getLayers().lastElement());
    }

     /**
     * Creates layer and adds it to the network layers list without any connection
     * useful for creation the first layer of the network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     */
    public void addLayer (String name, Neuron neuron,int neuronsNum) {
        Layer layer = new Layer(name,neuron,neuronsNum);
        layer.setId(getNextLayerId());
        addLayer(layer);
    }

      /**
     * Creates layer and adds it to the network layers list without any connection
     * useful for creation the first layer of the network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     */
    public void addLayer (String name, Neuron neuron,int neuronsNum, int[] pads) {
        Layer layer = new Layer(name,neuron,neuronsNum);
        if(pads!=null)
            layer.setPads(pads);
        layer.setId(getNextLayerId());
        addLayer(layer);
    }

    /**
     * Creates layer and links  it to the current last layer of network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     * Type of connection between new layer and current last layer of the network
     * is determined automatically
     */
    public void stackLayer (String name, Neuron neuron,int neuronsNum, int[] pads) {
        stackLayer(name,neuron,neuronsNum);
        _layers.lastElement().setPads(pads);
    }

    /**
     * Creates layer and links  it to the current last layer of network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     * Type of connection between new layer and current last layer of the network
     * is determined automatically
     */
    public void stackLayer (String name, Neuron neuron,int neuronsNum) {

        if(_layers.size()==0){
            addLayer(name,neuron,neuronsNum);
            setInputLayer(_layers.lastElement());
            return;
        }

        Layer prevOutput = _layers.lastElement();
        Layer layer = new Layer(name,neuron,neuronsNum);
        layer.setId(getNextLayerId());
        addLayer(layer);
        ConnectionType connectionType = getAutoConnectionShortcut(prevOutput.getNeuron().getNeuronType(), layer.getNeuron().getNeuronType());

        updateConnectionDependentLayer(layer,prevOutput);

        addConnection(prevOutput,layer,connectionType);
    }

      /**
       * TODO update all next connection dependent layers, until non-connection dependent layer is met
     * Creates layer and links  it to the current last layer of network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     * Type of connection between new layer and current last layer of the network
     * is determined automatically
     */
    public void stackLayerTop (String name, Neuron neuron,int neuronsNum) {

        if(_layers.size()==0){
            Layer layer = new Layer(name,neuron,neuronsNum);
            layer.setId(getNextLayerId());
            addLayer(layer);
            setInputLayer(_layers.lastElement());
            return;
        }

        Layer prevInput = getInputLayer();
        Layer layer = new Layer(name,neuron,neuronsNum);
        layer.setId(getNextLayerId());
        addLayer(layer);
        ConnectionType connectionType = getAutoConnectionShortcut(
                layer.getNeuron().getNeuronType(),prevInput.getNeuron().getNeuronType());
        addConnection(layer,prevInput,connectionType);
        setInputLayer(layer);
    }


    /**
     * Determines connection type automatically, using information
     * about source and destination layers types
     * @param src source layer type
     * @param dst destination layer type
     * @return connection shortcut
     */
    public ConnectionType getAutoConnectionShortcut(NeuronType src, NeuronType dst){

        switch (dst){
            case POOL:
                return ConnectionType.ONETOONE;
            case NONLINEAR:
                return ConnectionType.ONETOONE;
            case DATA:
                return ConnectionType.ALLTOONE;
            case LRN:
                return ConnectionType.CUSTOM;
           // case RESHAPE:
             //   return ConnectionType.ONETOONE;
           // case ADD:
             //   return ConnectionType.ONETOONE;
        }
        if(src==NeuronType.DATA)
            return ConnectionType.ONETOALL;

        /**
        * Default connection type is ALLTOALL
        */
        return ConnectionType.ALLTOALL;
    }


    /**
     * Creates layer and links  it to the current last layer of network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     * @param connectionType type of connection betweeen new layer with the current last layer of the network
     */
    public void stackLayer (String name, Neuron neuron,int neuronsNum, ConnectionType connectionType) {
        Layer prevOutput = _layers.lastElement();
        Layer layer = new Layer(name,neuron,neuronsNum);
        layer.setId(getNextLayerId());
        addLayer(layer);

        updateConnectionDependentLayer(layer,prevOutput);

        addConnection(prevOutput,layer,connectionType);
    }
     /**
     * Creates layer and links  it to the current last layer of network
     * @param name name of the new layer
     * @param neuron typical neuron of the new layer
     * @param neuronsNum number of neurons of the new layer
     * @param connectionMatrix custom connection matrix between new layer with the current last layer of the network
     */
    public void stackLayer (String name, Neuron neuron,int neuronsNum, boolean[][] connectionMatrix) {
        Layer prevOutput = _layers.lastElement();
        Layer layer = new Layer(name,neuron,neuronsNum);
        layer.setId(getNextLayerId());
        addLayer(layer);

        updateConnectionDependentLayer(layer,prevOutput);

        addConnection(prevOutput,layer,connectionMatrix);
    }

      /**
     * Remove layer and all related connections from DNN, if possible
     * @param layer layer to be removed
     */
    public void removeLayer(Layer layer){
        if(!_layers.contains(layer))
            return;
        int layerToRemoveId = layer.getId();
        _layers.remove(layer);
        Vector<Connection> connectionsToRemove = new Vector<>();

        for(Connection con: _connections){
            if(con.getSrcId()==layerToRemoveId || con.getDestId()==layerToRemoveId)
               connectionsToRemove.add(con);
        }
         for (Connection conToRemove: connectionsToRemove){
            _connections.remove(conToRemove);
         }

    }

    /**
     * Update dependent parameters
     * @param layer layer with dependent parameters
     * @param input input layer
     */
    private void updateConnectionDependentLayer(Layer layer, Layer input){
        Neuron curLayerNeuron = layer.getNeuron();
        if(curLayerNeuron instanceof  ConnectionDependent) {

            try { ((ConnectionDependent) curLayerNeuron).recalculateNeuronsNumber(layer, input);
              //  curLayerNeuron.setSampleDim(input.getNeuron().getSampleDim());

            }
            catch (Exception e){
                System.err.println(" Connection dependent layer "+ layer.getName()+" update error.");
            }
        }

       // if(curLayerNeuron instanceof MultipleInputsProcessor){
         //   _processMultipleConnectionsAdding(layer,input);
         //  }
    }

      /**
     * Get layers of the network
     * @return list of layers of the network
     */
    public Vector<Layer> getLayers() { return _layers; }

    /**
     * Set layers of the network
     * @param layers  list of layers of the network
     */
    public void setLayers(Vector<Layer> layers) { this._layers = layers; }

    /**
     * Get input connections of the certain layer
     * @param layer layer
     * @return list of the input connections of the certain layer
     */
    public Vector<Connection> getLayerInputConnections(Layer layer) {

        Vector<Connection> layerInputConnections = new Vector<Connection>();

        for(Connection connection: _connections){
            if(connection.getDestName().equals(layer.getName()))
                layerInputConnections.add(connection);

        }
        return layerInputConnections;
    }

    /**
     * Get number of input links of the certain layer
     * @param layer layer
     * @return number of input links of the certain layer
     */
     public int getLayerInputsNumber(Layer layer){
        int inputsNumber = 0;

        for(Connection connection: _connections) {
            if (connection.getDestName().equals(layer.getName()))
                inputsNumber += connection.getSrc().getNeuronsNum(); }
        return inputsNumber;
        }

    /**
     * Get output connections of the certain layer
     * @param layer layer
     * @return list of the output connections of the certain layer
     */
    public Vector<Connection> getLayerOutputConnections(Layer layer) {

        Vector<Connection> layerOutputConnections = new Vector<Connection>();

        for(Connection connection: _connections){
            if(connection.getSrcName().equals(layer.getName()))
                layerOutputConnections.add(connection);

        }
        return layerOutputConnections;
    }

    /**
     * Get input layer of the network
     * @return input layer of the network
     */
    public Layer getInputLayer() {
        return _inputLayer;
    }

    /**
     * Set input layer of the network
     * @param inputLayer input layer of the network
     */
    public void setInputLayer(Layer inputLayer) {
        this._inputLayer = inputLayer;
        this._inputLayerId= inputLayer.getId();
    }
    /**
     * Get output layer of the network
     * @return output layer of the network
     */
    public Layer getOutputLayer() { return _outputLayer; }

    /**
     * Set output layer of the network
     * @param outputLayer output layer of the network
     */
    public void setOutputLayer(Layer outputLayer) {
        this._outputLayer = outputLayer;
        this._outputLayerId=outputLayer.getId();
    }

     /**
     * Find layer by Name
     * @param name name of the layer
     * @return layer with specified name, if it was found and null otherwise
     */
      public Layer getLayer(String name) {
        Iterator<Layer> i;
        i = _layers.iterator();
        while( i.hasNext() ) {
            Layer layer = i.next();
            if( layer.getName().equals(name) ) {
                return layer;
            }
        }
        return null;
    }
    /**
     * Find layer by Id
     * @param layerId Id of the layer
     * @return layer with specified Id
     * @throws NullPointerException if the layer was not found
     */
    public Layer getLayer(int layerId) throws NullPointerException
    {
        for(Layer layer: _layers){
            if(layer.getId()==layerId)
                return layer;
        }

        System.err.println("Layer"+layerId+" not found");
        throw new NullPointerException();
    }

    /**
     * Get input layer id
     * @return input layer id
     */
    public int getInputLayerId() { return _inputLayerId; }

     /**
     * Set input layer id
     * @param inputLayerId input layer id
     */
    public void setInputLayerId(int inputLayerId) {
        this._inputLayerId = inputLayerId;
    }

    /**
     * Get output layer id
     * @return output layer id
     */
    public int getOutputLayerId() { return _outputLayerId; }

     /**
     * Set output layer id
     * @param outputLayerId output layer id
     */
    public void setOutputLayerId(int outputLayerId) {
        this._outputLayerId = outputLayerId;
    }

    /**
     * Return number of layers of the network
     * @return network layers number
     */
    public int getLayersNum(){ return _layers.size(); }

        /**
     * Get Id for the next layer of network
     * @return Id for the next layer of network
     */
    public int getNextLayerId() {
        return getMaxLayerId()+1;
    }

    /**
     * Get max layer Id in the Network
     * @return max layer Id in the Network
     */
    public int getMaxLayerId(){
        int curLayerId;
        int maxLayerId = -1;
        for(Layer layer:_layers){
            curLayerId = layer.getId();
            if(curLayerId>maxLayerId)
                maxLayerId = curLayerId;
        }
        return maxLayerId;
    }

    /**
     * Get last layer in the layers list
     * @return last layer in the layers list
     */
    public Layer getLastLayer(){
        return _layers.lastElement();
    }

     ///////////////////////////////////////////////////////////////////
    ////                  connections processing                   ////

      /**
     * Add connection with automatically determined Type
     * @param src name of the input layer of connection
     * @param dst name of the output layer of connection
     * @throws Exception if connection could not be set up
     */
     public void addConnection(String src, String dst) throws Exception {
         Layer srcLayer = getLayer(src);
          if(srcLayer == null) {
             System.err.println("Connection set up error, null src layer: "+src);
             throw new Exception("Connection set up error");

         }

         Layer dstLayer = getLayer(dst);
         if(dstLayer == null) {
            System.err.println("Connection set up error, null dst layer: "+dst);
            throw new Exception("Connection set up error");
         }

         updateConnectionDependentLayer(dstLayer,srcLayer);
         addConnection(srcLayer,dstLayer);
     }

    /**
     * Add connection with automatically determined Shortcut type
     * of generated custom matrix
     * @param src input layer of connection
     * @param dst output layer of connection
     */
     public void addConnection(Layer src, Layer dst) {
         ConnectionType autoConnectionType = getAutoConnectionShortcut(src.getNeuron().getNeuronType(),
                 dst.getNeuron().getNeuronType());
            addConnection(src, dst,autoConnectionType);
     }

    /**
     * Add custom connection between layers
     * @param src input layer of connection
     * @param dst output layer of connection
     * @param customConnection custom connection matrix
    */
    public void addConnection(Layer src,Layer dst, boolean[][] customConnection) {
          try {
              Custom newConnection = new Custom(src,dst, customConnection);
              _connections.add(newConnection);
               src.getOutputConnections().add(newConnection);
               dst.getInputConnections().add(newConnection);
          }
           catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Add connection with specified shortcut
     * @param src input layer
     * @param dst output layer
     * @param conType type of the default connection
     */
    public void addConnection(Layer src, Layer dst, ConnectionType conType) {
        try {
            Connection newConnection;
            switch (conType) {
                case ONETOONE:
                    newConnection = new OneToOne(src, dst);
                    break;
                case ONETOALL:
                    newConnection = new OneToAll(src, dst);
                    break;
                case ALLTOONE:
                    newConnection = new AllToOne(src, dst);
                    break;
                case ALLTOALL:
                    newConnection = new AllToAll(src, dst);
                    break;
                case CUSTOM:
                    if(dst.getNeuron() instanceof CustomConnectionGenerator) {
                        boolean[][] connectionMatrix =
                            ((CustomConnectionGenerator) dst.getNeuron()).generateCustomConnectionMatrix(src.getNeuronsNum(),dst.getNeuronsNum());
                            newConnection = new Custom(src,dst,connectionMatrix);
                        break;
                    }
                    else {
                        System.err.println(" Unspecified custom connection matrix " +
                                src.getName() + " --> " + dst.getName() + " Default all-to-all connection added");
                        newConnection = new AllToAll(src, dst);
                        break;
                    }
                default:
                    throw new EspamException("Connection creation error. Unknown connection type.");
            }

            if(dst.getNeuron() instanceof MultipleInputsProcessor){
            _processMultipleConnectionsAdding(dst,src);
           }
            _connections.add(newConnection);
            src.getOutputConnections().add(newConnection);
            dst.getInputConnections().add(newConnection);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Remove connection from DNN, if possible
     * @param connection connection to be removed
     */
    public void removeConnection(Connection connection){
        if(!_connections.contains(connection))
            return;
        Layer connectionDst = connection.getDest();
        Layer connectionSrc = connection.getSrc();
        _connections.remove(connection);
        if(connectionDst.getNeuron() instanceof MultipleInputsProcessor)
            ((MultipleInputsProcessor) connectionDst.getNeuron()).removeInput(connectionDst,connectionSrc);
    }

    /**
     * Get connections of the network
     * @return list of connections between layers of the network
     */
    public Vector<Connection> getConnections() { return _connections; }

    /**
     * Find connection by srcId and dstId. In espam. Network model
     * srcId and dstId identifies connection
     * @param srcId source layer Id
     * @param dstId destination layer Id
     * @return connection with specified source and destination layers ids or null
     */
    public Connection findConnection(int srcId, int dstId){
        Iterator<Connection> i;
        i = _connections.iterator();
        while( i.hasNext() ) {
            Connection connection = i.next();
            if( connection.getSrcId()==srcId && connection.getDestId()==dstId) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Set connections of the network
     * @param connections connections between layers of the network
     */
    public void setConnections(Vector<Connection> connections) { this._connections = connections; }

    /**
     * Get last connection, added to the network
     * @return last connection, added to the network
     */
    public Connection getLastConnection(){
        return this._connections.lastElement();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       other methods                      ////


    /**
     * Process adding of connection to multiple connection processor
     * @param layer multiple inputs processor
     * @param input input layer
     */
    private void _processMultipleConnectionsAdding(Layer layer, Layer input){
        ((MultipleInputsProcessor) layer.getNeuron()).addInput(input);
    }

    /**
     * Update DNN traverser after any transformations over the DNN structure were implemented
     */
    public void updateTraverser(){
        _traverser.initialize(this,false);
    }

    /**
     * Get number of Network layers
     * @return number of Network layers
     */
    public int countConnections(){
        return _connections.size();
    }

    /**
     * Get number of Network layers
     * @return number of Network layers
     */
    public int countLayers(){
        return _layers.size();
    }

    /**
     * Get name of the network
     * @return name of the network
     */
    public String getName() { return _name; }

    /**
     * Set name of the network
     * @return name of the network
     */
    public void setName(String _name) { this._name = _name; }

    /**
     * Get DNN I/O data type
     * @return DNN I/O data type
     */
    public String getDataType() {
        return _dataType;
    }

    /**
     * Set DNN I/O data type
     * @param dataType DNN I/O data type
     */
    public void setDataType(String dataType) {
        this._dataType = dataType;
    }

    /**
     * Get DNN weights type
     * @param weightsType DNN weights type
     */
    public void setWeightsType(String weightsType) {
        this._weightsType = weightsType;
    }

    /**
     * Get DNN weights type
     * @return DNN weights type
     */
    public String getWeightsType() {
        return _weightsType;
    }

    /**
     * Checks if specified layer is a hidden layer of the network
     * @param layer layer to be checked
     * @return if layer is a hidden layer of the network and false otherwise
     */
    public boolean isHiddenLayer(Layer layer){
        Vector<Connection> layerInputs = getLayerInputConnections(layer);
        if(layerInputs.size()==0)
            return false;

        return true;
    }

    /**
     * Checks, if layers input connections have same sources
     * @param l1 layer 1
     * @param l2 layer 2
     * @return true, if layers input connections have same sources and false otherwise
     */
    public boolean isInputLayersEqual(Layer l1, Layer l2){

        Vector<Layer> l1Inputs = new Vector<>();
        for(Connection connection: getLayerInputConnections(l1))
            l1Inputs.add(connection.getSrc());

        Vector<Layer> l2Inputs = new Vector<>();
        for(Connection connection: getLayerInputConnections(l2))
            l2Inputs.add(connection.getSrc());

        return isLayersListEqual(l1Inputs,l2Inputs);
    }

     /**
     * Checks, if layers output connections have same destinations
     * @param l1 layer 1
     * @param l2 layer 2
     * @return true, if layers output connections have same destinations and false otherwise
     */
    public boolean isOutputLayersEqual(Layer l1, Layer l2){

        Vector<Layer> l1Outputs = new Vector<>();
        for(Connection connection: getLayerOutputConnections(l1))
            l1Outputs.add(connection.getDest());

        Vector<Layer> l2Outputs = new Vector<>();
        for(Connection connection: getLayerOutputConnections(l2))
            l2Outputs.add(connection.getDest());

        return isLayersListEqual(l1Outputs,l2Outputs);
    }

      /**
      * Compares two layers lists
      * @param layers1 first list for comparison
      * @param layers2 second list for comparison
      * @return true, if layers list are equal and false otherwise
      */
       private boolean isLayersListEqual(Vector<Layer> layers1, Vector<Layer> layers2)
       {
           if(layers1.size()!=layers2.size())
               return false;

        Iterator i = layers2.iterator();
        while ( i.hasNext() ) {
            Layer layer = (Layer) i.next();
            if(!layers1.contains(layer)) {
                return false;
            }
        }
        return true;
       }

    /**
      * Compares two connection lists
      * @param clist1 first list for comparison
      * @param clist2 second list for comparison
      * @return true, if connections list are equal and false otherwise
      */
       private boolean isConnectionListEqual(Vector<Connection> clist1,Vector<Connection> clist2 )
       {
           if(clist1.size()!=clist2.size())
               return false;

        Iterator i = clist2.iterator();

        while ( i.hasNext() ) {
            Connection connection = (Connection) i.next();
            if(!clist1.contains(connection)) {
                return false;
            }
        }
        return true;
       }

     ////////////////////////////////////////////////////////////////////
    ////                    References resolving                    ////
     /**
     * Resolves the references after the deserialization
     * @throws EspamException if the references can not be resolved
     */
    public void resolveReferences() {
        try {
            resolveInputLayerReference();
            resolveOutputLayerReference();

            for(Layer layer: _layers)
               layer.resolveReferences();

            for (Connection connection : _connections)
                resolveConnectionReference(connection);
        }
        catch (Exception e) {
            System.err.println("reference resolve error: "+e.getMessage());
        }
    }

    /**
     * Resolves the connection reference after the deserialization
     * @throws NullPointerException if the source of sink layers were not found
     */
    private void resolveConnectionReference(Connection connection) {
        try {
            Layer srcLayer = getLayer(connection.getSrcId());

            Layer destLayer = getLayer(connection.getDestId());
            connection.setSrc(srcLayer);
            connection.setDest(destLayer);

            srcLayer.getOutputConnections().add(connection);
            destLayer.getInputConnections().add(connection);

            Neuron destLayerNeuron = destLayer.getNeuron();

            if(destLayerNeuron instanceof MultipleInputsProcessor) {
                ((MultipleInputsProcessor)destLayerNeuron).addInput(srcLayer);
            }

            }
        catch (Exception e) {
            throw  new NullPointerException("connection reference resolving error");
        }
    }

    /**
     * Resolves the input layer reference after the deserialization
     */
    private void resolveInputLayerReference() {
        Layer inputLayer = getLayer(getInputLayerId());
        if(inputLayer==null)
            throw  new NullPointerException("input layer reference resolving error");
        setInputLayer(inputLayer);
    }
     /**
     * Resolves the output layer reference after the deserialization
     */
    private void resolveOutputLayerReference() {
        Layer outputLayer = getLayer(getOutputLayerId());
         if(outputLayer==null)
            throw  new NullPointerException("input layer reference resolving error");
        setOutputLayer(outputLayer);
    }

     ////////////////////////////////////////////////////////////////////
    ////                         private methods                    ////
     /**
     * Add new layer to the network
     * @param layer layer
     */
    private void addLayer(Layer layer) {
        _layers.add(layer);
    }

    /**
     * Compares layers of Network with given layers list
     * @param layers layers list for comparision
     * @return true, if Network's layers list is equal to
     * given layers list and false otherwise
     */
       private boolean isLayersListEqual(Vector<Layer> layers)
       {
           if(_layers.size()!=layers.size())
               return false;

        Iterator i = layers.iterator();
        while ( i.hasNext() ) {
            Layer layer = (Layer) i.next();
            if(!_layers.contains(layer)) {
                return false;
            }
        }
        return true;
       }

     /**
      * Compares connections of Network with given connections list
      * @param connections connections for comparision
      * @return true, if Network's connections list is equal to the given
      * connections list and false otherwise
      */
       private boolean isConnectionListEqual(Vector<Connection> connections)
       {
           if(_connections.size()!=connections.size())
               return false;

        Iterator i = connections.iterator();

        while ( i.hasNext() ) {
            Connection connection = (Connection) i.next();
            if(!_connections.contains(connection)) {
                return false;
            }
        }
        return true;
       }

     /**
     * Set all height of layer's input layers to min value
     * @param layer layer
     * @param keepHeightDependency if upcoming layers min height is dependent on layer
     */
    private void updateUpcomingLayersDataHeights(Layer layer, boolean keepHeightDependency){
         Vector<Connection> curLayerInputs = getLayerInputConnections(layer);
         if(curLayerInputs.size()==0)
            return;

         int upcomingLayersMinHeight = 1;

         for(Connection inputConnection: curLayerInputs){
             Layer upcomingSrc = inputConnection.getSrc();
           //if (keepHeightDependency) {
             //    upcomingLayersMinHeight = findMinInputHeight(upcomingSrc);
                // System.out.println(upcomingSrc.getName()+" min h = " + upcomingLayersMinHeight);
            // }
             upcomingSrc.updateMinDataHeight(upcomingLayersMinHeight);
         }
    }

    /**
     * Find min input height for a layer:
     *  1. Find min possible output height
     *  2. Calculate input height, using certain formulas
     * @param layer layer to be processed
     */
    private int findMinInputHeight(Layer layer){
        Vector<Connection>outpCons = getLayerOutputConnections(layer);
        if(outpCons.size()==0)
            return layer.getInputHeight();
        if(outpCons.size()==1)
            return outpCons.firstElement().getDest().getInputHeight();

        int minAcceptableOutH = outpCons.firstElement().getDest().getInputHeight();
        int curH;
        for(int i=1; i<outpCons.size();i++) {
            curH = outpCons.elementAt(i).getDest().getInputHeight();
            if(curH>minAcceptableOutH)
                minAcceptableOutH = curH;
        }

        int minAcceptableH = layer.getNeuron().calculateMinInputDataHeight(minAcceptableOutH);

        return minAcceptableH;

    }

    ////////////////////////////////////////////////////////////////////
    ////         Connections-dependent layers manipulation         ////
   /**
     * Updates neurons number for layers, inherits some
     * parameters from connections
     * @return number of neurons for target espam.cnn.Layer
     */
    public void updateConnectionDependentParameters() throws Exception {
        sortLayersInTraverseOrder();
        for(Layer layer:_layers){ updateConnectionDependentParameters(layer); }
    }

    /**
     * Updates neurons number for layers, inherits some
     * parameters from connections
     * @param dependent dependent layer
     * @return number of neurons for target espam.cnn.Layer
     */
    private void updateConnectionDependentParameters(Layer dependent) throws Exception {
        NeuronType dependentLayerNeuronType = dependent.getNeuron().getNeuronType();
        if(!NeuronType.isConnectionDependent(dependentLayerNeuronType))
            return;

        Vector<Layer> inputLayers = new Vector<Layer>();
        for(Connection inpCon:dependent.getInputConnections())
            inputLayers.add(inpCon.getSrc());

        switch (dependentLayerNeuronType) {
            case POOL: {
                inheritParamsFromSingleOutput(inputLayers, dependent);
                return;
            }
            case NONLINEAR: {
                inheritParamsFromSingleOutput(inputLayers, dependent);
                return;
            }
            case LRN: {
                inheritParamsFromSingleOutput(inputLayers, dependent);
                return;
            }

            case CONCAT: {
                Concat dependentNeuron = (Concat)(dependent.getNeuron());
                return;
            }
            case RESHAPE: {
                Reshape dependentNeuron = (Reshape)(dependent.getNeuron());
                for(Layer inputLayer: inputLayers)
                   // System.out.println(inputLayer.getName());
                return;
            }

            case ADD: {
               // Add dependentNeuron = (Add)(dependent.getNeuron());
                inheritParamsFromSingleOutput(inputLayers, dependent);
                return;
            }

            default:
                return ;
        }
    }
                /**
    * TODO resolve for multiple inputs for POOLING/NONLINEAR layers if reasoneable
    * for now each dependent layer should have 0 or 1 outputs
    * otherwise the error if thrown
    */
    private void inheritParamsFromSingleOutput(Vector<Layer> inputLayers, Layer dependent) throws Exception
    {
        switch (inputLayers.size()){
            case 0:
                return;
            case 1:{
                Layer singleInput = inputLayers.firstElement();
                dependent.setNeuronsNum(singleInput.getNeuronsNum());
                dependent.getNeuron().setSampleDim(singleInput.getNeuron().getSampleDim());
                return;
            }
            /** other cases */
            default:
                throw new Exception("Parameters update fail: "+ dependent.getNeuron().getNeuronType()+" layer should have a single input");
        }

    }


      /**
     * Sorts layer connections in traverse order from input layer of DNN
     */
    private void sortConnectionsInTraverseOrderFromTop(){
        _traverser.initialize(this,false);
        Vector<Integer> layersTraverseOrder = _traverser.getLayersTraverseOrder(_inputLayerId);
        Vector<Connection> connectionsInTraverseOrder = new Vector<>();

        for (Integer layerId: layersTraverseOrder){
            Layer layer = getLayer(layerId);

            Vector<Connection> layerOutputConnections = getLayerOutputConnections(layer);
            for(Connection connection: layerOutputConnections)
                connectionsInTraverseOrder.add(connection);
        }

        _connections = connectionsInTraverseOrder;
    }

    /**
     * Sorts layer connections in traverse order from output layer of the DNN
     */
    private void sortConnectionsInTraverseOrderFromBottom(){
        _traverser.initialize(this,true);
        Vector<Integer> layersTraverseOrder = _traverser.getLayersTraverseOrder(_outputLayerId);
        Vector<Connection> connectionsInTraverseOrder = new Vector<>();

        for (Integer layerId: layersTraverseOrder){
            Layer layer = getLayer(layerId);

            Vector<Connection> layerInputConnections = getLayerOutputConnections(layer);
            for(Connection connection: layerInputConnections)
                connectionsInTraverseOrder.add(connection);
        }

        _connections = connectionsInTraverseOrder;
    }

    /**
     * Sort list of layers in traverse order
     */
    public void sortLayersInTraverseOrder(){
        _layers = getLayersInTraverseOrderFromTop();
    }

    /**
     * Sorts layer connections in traverse order from output layer of the DNN
     * */
    public Vector<Layer> getLayersInTraverseOrderFromTop() {
        Vector<Layer> sorted = new Vector<Layer>();
        _traverser.initialize(this,false);
        Vector<Integer> layersTraverseOrder = _traverser.getLayersTraverseOrder(_outputLayerId);
        for(int layerId: layersTraverseOrder){
            Layer layer = getLayer(layerId);
            sorted.add(layer);
        }
        return sorted;
    }

     /**
     * Sorts layer connections in traverse order from output layer of the DNN
     * */
    public Vector<Layer> getLayersInTraverseOrderFromBottom() {
        Vector<Layer> sorted = new Vector<Layer>();
        _traverser.initialize(this,true);
        Vector<Integer> layersTraverseOrder = _traverser.getLayersTraverseOrder(_outputLayerId);
        for(int layerId: layersTraverseOrder){
            Layer layer = getLayer(layerId);
            sorted.add(layer);
        }
        return sorted;
    }

    ///////////////////////////////////////////////////////////////////
    ////           ONNX-data formats compatibility               ////

    /**
     * Generate auto pads in case of 'inconvenient' data formats,
     * where input data shapes are indivisible on min input data shape
     * of neuron (for Convolution/Pooling neurons)
     * NOTE: Data formats should be set up before auto pads generation
     */
    public void setAutoPads(){
        for(Layer layer:_layers){
            layer.setAutoPads();
        }
    }

    /**
     * Stack flatten layer to DNN last layer
     * Flatten layer transforms any input data shape to a vector
     */
    public void stackFlattenLayer(){
        Reshape flattenNeuron = new Reshape();
        flattenNeuron.setFlatten(true);
        String name = "flatten_" + getNextLayerId();
        stackLayer(name,flattenNeuron,1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /**Name of the Network*/
    @SerializedName("name")private String _name;

    /**Input layer of the network*/
    private transient Layer _inputLayer;

    /**Output layer of the network*/
    private transient Layer _outputLayer;

    /** DNN traverser*/
    private transient NetworkTraverser _traverser = new NetworkTraverser();

    /** Input layer Id*/
    @SerializedName("inputLayerId")private int _inputLayerId;

    /** Output layer Id*/
    @SerializedName("outputLayerId")private int _outputLayerId;

    /** Layers*/
    @SerializedName("layers")private Vector<Layer> _layers;

    /** Connections between layers of the network*/
    @SerializedName("connections")private Vector<Connection> _connections;

    /** TODO change if not the same for all the layers*/
    /** I/O data type description*/
    @SerializedName("dataType")private String _dataType = "float";

    /** Weights type description*/
    @SerializedName("weightsType")private String _weightsType = "float";

}