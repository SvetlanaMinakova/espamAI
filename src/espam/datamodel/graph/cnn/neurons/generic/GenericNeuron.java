package espam.datamodel.graph.cnn.neurons.generic;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CNNGraphVisitor;

/**
 * This class is Generic (Complex) Neuron. Generic neuron contains
 * a network inside it and allows to describe complex nested Convolutional Neural Networks (CNNs)
 * @author Svetlana Minakova
 */
public class GenericNeuron extends Neuron implements ReferenceResolvable{
     ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Constructor for new GenericNeuron element with a name
     * @param name name of the GenericNeuron
     */
    public GenericNeuron (String name) {
        setNeuronType(NeuronType.GENERIC);
        setName(name);
    }

    /**
     * Create new GenericNeuron element with a name and internal structure
     * @param name name of the GenericNeuron
     * @param internalStructure internal structure of the GenericNeuron (a neural network)
     */
    public GenericNeuron (String name, Network internalStructure) {
        setName(name);
        setInternalStructure(internalStructure);
        setSampleDim(internalStructure.getInputLayer().getNeuron().getSampleDim());
        setNeuronType(NeuronType.GENERIC);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     * Clone this Generic Neuron
     * @return a new reference on the Generic Neuron
     */
    public GenericNeuron clone() {
        GenericNeuron newObj = (GenericNeuron) super.clone();
        setInternalStructure((Network)_internalStructure.clone());
        return (newObj);
    }

    /**
     * Create a deep copy of the GenericNeuron
     * @param n GenericNeuron to be copied
     */
    public GenericNeuron (GenericNeuron n) {
        super(n);
        setInternalStructure(new Network(n._internalStructure));
    }


    /**
     * Return String description of the GenericNeuron
     * @return  String description of the GenericNeuron
     */
    @Override
    public String toString() {
       StringBuilder strNeuron = new StringBuilder();
       strNeuron.append("\n parameters: [ ");
       strNeuron.append("type: "+ NeuronType.GENERIC + " ]");
       return strNeuron.toString();
    }

    /**
    * Compares Generic neuron with another object
    * @param obj Object to compare this Neuron with
    * @return true if Neuron is equal to the object and false otherwise
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

       GenericNeuron generic = (GenericNeuron)obj;
         return getName().equals(generic.getName())
              &&_internalStructure.equals(generic._internalStructure)
              && Tensor.isSame(getInputDataFormat(),generic.getInputDataFormat())
              && Tensor.isSame(getOutputDataFormat(),generic.getOutputDataFormat());
       }

        /**
     * Clear neuron's data formats
     */
    @Override
    public void clearDataFormats() {
        _internalStructure.clearDataFormats();
    }

     /**
     * Set new data formats for neuron
     * @param inputDataFormat input data format
     */
    @Override
     public void setDataFormats(Tensor inputDataFormat) {
       _internalStructure.setDataFormats(inputDataFormat);
        setInputDataFormat(inputDataFormat);
        setOutputDataFormat(_internalStructure.getOutputLayer().getOutputFormat());
    }

    /**
     * Automatically calculates the output data format of a neuron
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        super.setInputDataFormat(inputDataFormat);
        _internalStructure.setDataFormats(inputDataFormat);
        Tensor outputFormat = _internalStructure.getOutputLayer().getOutputFormat();
        return outputFormat;
    }

     /**
      * TODO CHECKOUT
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight){
        _internalStructure.minimizeDataFlow(true);
        return _internalStructure.getInputLayer().getInputHeight();
    }


     /**
     * Updates min input data height, if possible
     * @param minOutputDataHeight min output data height
     * @return true, if data height was updated and false otherwise
     */
     @Override
    public boolean setMinDataHeight(int minOutputDataHeight){
        int minInputDataHeight= calculateMinInputDataHeight(minOutputDataHeight);
        boolean inputHeightSet = setInputHeight(minInputDataHeight);
        boolean outputHeightSet = setOutputHeight(minOutputDataHeight);

        return inputHeightSet && outputHeightSet;
    }

      /**
     * Set neuron input height
     * @return true, if neuron input height was updated and false otherwise
     */
    @Override
    public boolean setInputHeight(int newHeight){
        Tensor subNetworkInputDataFormat = _internalStructure.getInputLayer().getInputFormat();
        if(Tensor.isHaveHeight(subNetworkInputDataFormat)) {
            Tensor.setHeight(subNetworkInputDataFormat, newHeight);
            super.setInputDataFormat(subNetworkInputDataFormat);
            return true;
        }
        return false;
    }


      /**
     * Set neuron output height
     * @return true, if neuron output height was updated and false otherwise
     */
    @Override
    public boolean setOutputHeight(int newHeight){
        Tensor subNetworkOutputDataFormat = _internalStructure.getOutputLayer().getOutputFormat();
        if(Tensor.isHaveHeight(subNetworkOutputDataFormat)) {
            Tensor.setHeight(subNetworkOutputDataFormat, newHeight);
            super.setOutputDataFormat(subNetworkOutputDataFormat);
            return true;
        }
        return false;
    }

    /**
     * Get neuron input height
     * @return neuron input dataFormat height
     */
    @Override
    public int getInputHeight(){
        return _internalStructure.getInputLayer().getInputHeight();
    }

    /**
     * Get neuron output height
     * @return neuron output dataFormat height
     */
    @Override
    public int getOutputHeight(){
        return _internalStructure.getOutputLayer().getOutputHeight();
    }

    /**
     * Get an internal structure of the generic neuron (a neural network)
     * @return an internal structure of the generic neuron (a neural network)
     */
    public Network getInternalStructure() {
        return _internalStructure;
    }

    /**
     * Set internal structure of the generic neuron (a neural network)
     * @param internalStructure internal structure of the generic neuron (a neural network)
     */
    public void setInternalStructure(Network internalStructure) {
        this._internalStructure = internalStructure;
    }

    /**
     * resolve references inside the generic neuron after the deserialization
     */
    public void resolveReferences() {
        _internalStructure.resolveReferences();
    }

        /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getName());
     //   Integer operationTokensNum = getOperationTokensNumber(channels);
     //   desc.append("(" + operationTokensNum + ")");

        return desc.toString();
    }

    /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     * TODO finish implementation!
     */
    public int getOperationsNumber(int channels){
    return 1;
    }

    //////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    @SerializedName("internalStructure")private Network _internalStructure;
}

