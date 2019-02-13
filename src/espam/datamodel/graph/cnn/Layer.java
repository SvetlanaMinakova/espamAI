package espam.datamodel.graph.cnn;
import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CNNGraphVisitor;

import java.io.Console;
import java.util.Vector;

/**
 * represents one Layer of CNN
 */
public class Layer implements Cloneable, ReferenceResolvable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Empty layer constructor: required for valid parsing
     */
    public Layer() {}

    /**
     * Constructor to create new layer with a name
     * @param name layer's name
     */
    public Layer(String name) {
        setName(name);
    }

    /**
     * Bulids a layer of SDFNods by copying sample Node nodesNum times
     * @param sampleNeuron typical element of a layer
     * @param neuronsNum number of neurons in a layer
     * @return list of SDFNodes, represents a layer
     */
    public Layer(String name, Neuron sampleNeuron, int neuronsNum) {
        setName(name);
        setNeuron(sampleNeuron);
        setNeuronsNum(neuronsNum);
    }

      /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     * Clone this Layer
     * @return  a new reference on the Layer
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Layer newObj = (Layer) super.clone();
            newObj.setName(_name);
            newObj.setNeuron((Neuron)_neuron.clone());
            newObj.setNeuronsNum(_neuronsNum);
            newObj.setOutputFormat((Tensor)_outputDataFormat.clone());
            newObj.setInputFormat((Tensor)_inputDataFormat.clone());
            newObj.setId(_id);
            newObj.setPads(_pads.clone());
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     * Create a deep copy of the Layer
     * @param layer Layer to be copied
     */
    public Layer (Layer layer) {
        setName(layer._name);
        setNeuron(Neuron.copyNeuron(layer._neuron));
        setNeuronsNum(layer._neuronsNum);
        setOutputFormat(new Tensor(layer._outputDataFormat));
        setInputFormat(new Tensor(layer._inputDataFormat));
        setId(layer._id);

        if(_pads!=null) {
            _pads = new int[layer._pads.length];
            for (int i = 0; i < _pads.length; i++) {
                _pads[i] = layer._pads[i];
            }
        }
    }

    /**
     * Return string description of the Layer
     * @return string description of the Layer
     */
    @Override
    public String toString() {
        return getName() + "\nneurons num: "+getNeuronsNum();
    }

    /**
    * Compares Layer  with another object
    * @param obj Object to compare this Layer with
    * @return true if Layer is equal to the object and false otherwise
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

        Layer layer = (Layer)obj;
         return _id==layer._id
              &&_name.equals(layer._name)
              &&_neuronsNum == layer._neuronsNum
              &&_neuron.equals(layer._neuron)
              && Tensor.isSame(_inputDataFormat,layer._inputDataFormat)
              && Tensor.isSame(_outputDataFormat,layer._outputDataFormat);
       }

    /**
     * Clear data formats of the layer
     * Sets all data formats as empty tensors
     */
    public void clearDataFormats() {
        setInputFormat(new Tensor());
        setOutputFormat(new Tensor());
        _neuron.clearDataFormats();
    }

    /**
     * Update data formats of the layer
     * @param neuronInputDataFormat input data format of the typical layer's neuron
     * @param layerInputDataFormat input data format of the layer
     */
    public void updateDataFormatsFromTop(Tensor neuronInputDataFormat,Tensor layerInputDataFormat) {
        /** process generic neuron as a subnetwork*/
        if(_neuron instanceof GenericNeuron){
            Network subNetwork = ((GenericNeuron) _neuron).getInternalStructure();
            subNetwork.setDataFormats(layerInputDataFormat);
            _neuron.setInputDataFormat(subNetwork.getInputLayer().getInputFormat());
            _neuron.setOutputDataFormat(subNetwork.getOutputLayer().getOutputFormat());
            setInputFormat(subNetwork.getInputLayer().getInputFormat());
            setOutputFormat(subNetwork.getOutputLayer().getOutputFormat());
            return;
        }

        _neuron.setInputDataFormat(neuronInputDataFormat);

        Tensor neuronInputDataFormatWithPads = Tensor.addPads(neuronInputDataFormat,_pads);
        //_neuron.setInputDataFormat(neuronInputDataFormatWithPads);
       // Tensor layerInputDataFormatWithPads = Tensor.addPads(layerInputDataFormat,_pads);

        _neuron.setOutputDataFormat(_neuron.calculateOutputDataFormat(neuronInputDataFormatWithPads));

        setInputFormat(layerInputDataFormat);

        setOutputFormat(calculateOutputFormat());

    }


    /**TODO CHECK
     * Automatically calculates the min input height of the layer
     * for dense block
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */

    public int calculateMinInputDataHeight( int minOutputHeight) {
        return _neuron.calculateMinInputDataHeight(minOutputHeight);
    }

     /**
     * Updates min input data height, if possible
     * @param minOutputDataHeight
     */
    public void updateMinDataHeight(int minOutputDataHeight){

        if(_neuron.setMinDataHeight(minOutputDataHeight)) {
          setInputHeight(_neuron.getInputHeight());
          setOutputHeight(minOutputDataHeight);
       }
    }

      /**
     * Set neuron output height
     * @return true, if neuron output height was updated and false otherwise
     */
    public void setOutputHeight(int newHeight){
        _outputDataFormat = new Tensor(_outputDataFormat);
            _outputDataFormat.setDimSize(1, newHeight);
    }

          /**
     * Set neuron input height
     * @return true, if neuron input height was updated and false otherwise
     */
    public void setInputHeight(int newHeight){
        _inputDataFormat = new Tensor(_inputDataFormat);
        _inputDataFormat.setDimSize(1, newHeight);
    }


    /**
     * Get layer input height. layer input height should be the same with the neuron's input height
     * @return layer input height
     */
    public int getInputHeight(){
        return _neuron.getInputHeight();
    }

    /**
     * Get layer output height. layer output height should be the same with the neuron's output height
     * @return layer output height
     */
    public int getOutputHeight(){
        return _neuron.getOutputHeight();
    }

     /**
     * calculates output as a sequence of neurons outputs
     */
    public Tensor calculateOutputFormat() {
        Tensor outputFormat = new Tensor(_neuron.getOutputDataFormat());
        if(_neuronsNum>1)
            outputFormat.addDimension(_neuronsNum);
        /**TODO CHECKOUT*/
        return outputFormat;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   consistency checkout                   ////

    /**
     * Checks if data formats are consistent. Data formats are
     * not consistent if there are any null or '-' output values and
     * consistent otherwise.
     * @return true, if layer data format consistent and false otherwise
     */
    public boolean isDataFormatsConsistent(){

        if (Tensor.isNullOrEmpty(_inputDataFormat) ||
                Tensor.isNullOrEmpty(_outputDataFormat)) {
             //   System.out.println("Layer data formats consistency fault: " +
               //         getName() + " null data formats ");
                return true;
            }

        if(Tensor.containsZeroOrNegativeDims(_inputDataFormat) ||
                    Tensor.containsZeroOrNegativeDims(_outputDataFormat)) {
                System.out.println("Layer data formats consistency fault: " +
                        getName() + " zero or negative data formats ");
                return false;
            }

        if(_inputDataFormat.getDimensionality()<_neuron.getSampleDim()){
            System.out.println("input dimension mismatch ");
            return false;
        }

            return true;

        }
    ///////////////////////////////////////////////////////////////////
    ////            data formats details extraction               ////

    /**
     * Get feature maps number, which is equal to neurons number
     * @return feature maps number, which is equal to neurons number
     */
    public int getFeatureMaps(){
        return getNeuronsNum();
    }

    /**
     * TODO REFACTORING ON DENSEBLOCK INPUT CHANNELS NUMBER
     * TODO denseBlock input dimensionality might be>denseBlock output dimensionality,
     * TODO this is fair only for denseBlock
     * Get input channels number
     * @return input channels number*/

    public int getInputChannels(){
        if(Tensor.isNullOrEmpty(_inputDataFormat)||Tensor.isNullOrEmpty(_neuron.getInputDataFormat()))
            return 0;

        if(_neuron instanceof DenseBlock) {
            int ch = _inputDataFormat.getElementsNumber() / _neuron.getInputDataFormat().getElementsNumber();
            return Math.max(ch, 1);
        }

        //if(_neuron instanceof MultipleInputsProcessor)
          //  return 1;

        if(_inputDataFormat.getDimensionality()<=_neuron.getSampleDim())
            return 1;

        return _inputDataFormat.getDimSize(_neuron.getSampleDim());
    }

     /**
     * Get output channels number
     * @return input channels number
     */
    public int getOutputChannels(){ return getNeuronsNum(); }

    /**
     * Get input data width
     * @return input data width
     */
    public int getInpW(){ return getInpDim(0); }

    /**
     * Get input data height
     * @return input data height
     */
    public int getInpH(){ return getInpDim(1); }

    /**
     * Get output data width
     * @return input data width
     */
    public int getOutpW(){ return getOutpDim(0); }

    /**
     * Get input data width
     * @return input data width
     */
    public int getOutpH(){ return getOutpDim(1); }

    /**
     * Return value of input data dimension (without channels)
     * @param dimId dimension Id
     * @return value of input data dimension (without channels)
     * if it is presented or 0 otherwise
     */
    public int getInpDim(int dimId){
        return _neuron.getInputDataFormat().getDimSize(dimId);
    }

    /**
     * Return value of input data dimension (without channels)
     * @param dimId dimension Id
     * @return value of input data dimension (without channels)
     * if it is presented or 0 otherwise
     */
    public int getOutpDim(int dimId){
        return _neuron.getOutputDataFormat().getDimSize(dimId);
    }

    ///////////////////////////////////////////////////////////////////
    //// simple getters and setters for all private fields methods ////
    //// required for serialization/deserialization                ////
    public String getName() { return _name; }

    public void setName(String name) { this._name = name; }

    public Neuron getNeuron() { return _neuron; }

    public void setNeuron( Neuron neuron) { this._neuron = neuron; }

    public Tensor getInputFormat() { return _inputDataFormat; }

    /** get input data format, refined with pads*/
    public Tensor getInputFormatWithPads() {
        if(_pads==null || _inputDataFormat==null)
            return _inputDataFormat;
        if(_inputDataFormat.getDimensionality()<2)
            return _inputDataFormat;
        Tensor inputDataFormatWithPads = new Tensor(_inputDataFormat);
        inputDataFormatWithPads.setDimSize(0,inputDataFormatWithPads.getDimSize(0)+_pads[0]+_pads[2]);
        inputDataFormatWithPads.setDimSize(1,inputDataFormatWithPads.getDimSize(1)+_pads[1]+_pads[3]);
        return inputDataFormatWithPads;

    }

    public void setInputFormat(Tensor inputFormat) { this._inputDataFormat = inputFormat; }

    public Tensor getOutputFormat() {
    if(_neuron instanceof GenericNeuron)
        return ((GenericNeuron) _neuron).getInternalStructure().getOutputLayer().getOutputFormat();
        return _outputDataFormat;
    }

    public void setOutputFormat(Tensor outputFormat) { this._outputDataFormat = outputFormat; }

    public int getNeuronsNum() { return _neuronsNum; }

    public void setNeuronsNum(int neuronsNum) { this._neuronsNum = neuronsNum; }

    public int getId() { return _id; }

    public void setId(int id) { this._id = id; }

    public Vector<Connection> getInputConnections() {
        return _inputConnections;
    }

    public void setInputConnections(Vector<Connection> inputConnections) {
        this._inputConnections = inputConnections;
    }

    public Vector<Connection> getOutputConnections() {
        return _outputConnections;
    }

    public void setOutputConnections(Vector<Connection> outputConnections) {
        this._outputConnections = outputConnections;
    }

    /**
     * Get layer pads (dummy lines, extending input layer format)
     * @return layer pads (dummy lines, extending input layer format)
     */
    public int[] getPads() { return _pads; }

    /**
     * Set pads (dummy lines, extending input layer format)
     * @param x0 top left pad
     * @param x1 top right pad
     * @param y0 bottom left pad
     * @param y1 bottom right pad
     */
    public void setPads( int x0, int x1, int y0, int y1){
        if(_pads==null)
            _pads = new int[4];

        _pads[0] = x0;
        _pads[1] = x1;
        _pads[2] = y0;
        _pads[3] = y1;
    }


    ///////////////////////////////////////////////////////////////////
    ////           ONNX-data formats compatibility               ////

    /**
     * Generate auto pads in case of 'inconvenient' data formats,
    * where input data shapes are indivisible on min input data shape
    * Pads are values, added to the beginning and ending along each axis.
    * in format [x1_begin, x2_begin...x1_end, x2_end,...],
    * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
    * the number of pixels added at the end of axis `i`.
    * Pads should contain values >=0
     * NOTE: Data formats should be set up before auto pads generation
     */
    public void setAutoPads(){
        if(_neuron instanceof CNNNeuron){
            CNNNeuron cnnNeuron = (CNNNeuron)_neuron;
            if( cnnNeuron.autoPadsNeeded()){
                int[] pads = cnnNeuron.generateAutoPads();
                _pads = pads;
            }
        }
    }

    /**
     * Set layer pads (dummy lines, extending input layer format)
     * @param pads layer pads (dummy lines, extending input layer format)
     */
    public void setPads(int[] pads) { this._pads = pads; }

    /**
     * Resolve references inside the layer after the deserialization
     */
    public void resolveReferences() {
        if(_neuron instanceof ReferenceResolvable)
            ((ReferenceResolvable)_neuron).resolveReferences();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**Unique layer identifier*/
    @SerializedName("id")private int _id;

    /**name of the layer*/
    @SerializedName("name")private String _name;

    /**number of neurons in the layer*/
    @SerializedName("neuronsNum")private int _neuronsNum;

    /**typical neuron of the layer*/
    @SerializedName("neuron")private Neuron _neuron;

    /**layer input data format*/
    @SerializedName("inputDataFormat")private Tensor _inputDataFormat;

    /**layer output data format*/
    @SerializedName("outputDataFormat")private Tensor _outputDataFormat;

    /**Vector of references to input connections of the layer*/
    private transient Vector<Connection> _inputConnections = new Vector<Connection>();

    /**Vector of references to output connections of the layer*/
    private transient Vector<Connection> _outputConnections = new Vector<Connection>();

    /**
    * Pads are values, added to the beginning and ending along each axis.
    * in format [x1_begin, x2_begin...x1_end, x2_end,...],
    * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
    * the number of pixels added at the end of axis `i`.
    * Pads should contain values >=0
    */
    @SerializedName("pads")private int[] _pads = null;
}
