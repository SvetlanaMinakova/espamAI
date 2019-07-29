package espam.datamodel.graph.cnn.neurons.transformation;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.DataContainer;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;

/**
 * Reshape transformation class
 */
public class Reshape extends Neuron implements MultipleInputsProcessor,DataContainer {
    /**
     * TODO: finish implementation
     * Create new Reshape element with a name
     * a stride and a kernelSize for the Reshape element = 1
     * an input sample dimension is 1 (vector)
     */
    public Reshape() {
        super();
        setName(NeuronType.RESHAPE.toString());
        setNeuronType(NeuronType.RESHAPE);
    }

      /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(int channels){
        return 1;
    }

    /**
     * TODO: finish implementation: compatibility checkout
     * Create new Reshape element with a name
     * a stride and a kernelSize for the Reshape element = 1
     * an input sample dimension is 1 (vector)
     */
    public Reshape(Tensor inputDataFormat, Tensor outputDataFormat) {
        super();
        setName(NeuronType.RESHAPE.toString());
        setNeuronType(NeuronType.RESHAPE);
        setDataFormats(inputDataFormat, outputDataFormat);
    }

    /**
     * Set specified input and output data formats
     *
     * @param inputDataFormat  input data format
     * @param outputDataFormat output data format
     */

    public void setDataFormats(Tensor inputDataFormat, Tensor outputDataFormat) {
        setInputDataFormat(inputDataFormat);
       if(_flatten)
         setOutputDataFormat(_flatData(outputDataFormat));
        else
            setOutputDataFormat(outputDataFormat);
    }

    /**
     * Set data formats as single unified input/output data format
     * @param dataFormat single unified input/output data format
     */
    public void setDataFormats(Tensor dataFormat) {
        if(_flatten)
         setOutputDataFormat(_flatData(dataFormat));
        else
            setOutputDataFormat(dataFormat);
        setInputDataFormat(dataFormat);

    }

    /**
     * Set default input / output data formats
     */
    public void setDataFormats() {
        setInputDataFormat(new Tensor());
        setOutputDataFormat(new Tensor());
    }

    /**
     * Return the string description of the neuron specific parameters
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
        StringBuilder strParams = new StringBuilder();
        strParams.append("\n parameters: [\n");
        strParams.append("from: " + getInputDataFormat() + "\n to: " + getOutputDataFormat() + "]\n");
        return strParams.toString();
    }

     /**
     * Clone this Neuron
     * @return a new reference on the Neuron
     */
    public Reshape clone() {
        Reshape newObj = (Reshape) super.clone();
        newObj._inputsNum = this._inputsNum;
        newObj._inputOwners = this._inputOwners;
        newObj._slice = this._slice;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     * @param r original neuron to be copied
     */
    public Reshape (Reshape r) {
        super(r);
        _inputsNum = r._inputsNum;
        _inputOwners = new Vector<>();
        for(Layer inputOwner: r._inputOwners)
            _inputOwners.add(inputOwner);
        _slice = r._slice;
    }

    /**
     * Accept a Visitor
     *
     * @param x A Visitor Object.
     * @throws EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) {
        x.visitComponent(this);
    }

    /**
     * Compares Reshape neuron with another object
     *
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

        espam.datamodel.graph.cnn.neurons.transformation.Reshape Reshape = (espam.datamodel.graph.cnn.neurons.transformation.Reshape) obj;
        return Tensor.isSame(getInputDataFormat(), Reshape.getInputDataFormat())
                && Tensor.isSame(getOutputDataFormat(), Reshape.getOutputDataFormat());
    }

    /**
     * Set new data formats for neuron
     *
     * @param inputDataFormats input data formats
     */
    public void setInputDataFormat(Vector<Tensor> inputDataFormats) {
        try {
            Tensor mergedInput = Tensor.mergeToSequence(inputDataFormats);
            setInputDataFormat(mergedInput);
            setOutputDataFormat(mergedInput);
            setSampleDim(inputDataFormats.elementAt(0).getDimensionality());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * TODO REFACTORING ON FLATTEN
     * @param inputDataFormat input data format of the neuron
     * For reshape neuron, the output format is either manually
     * specified or just copy input format
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {

        Vector<Tensor> _inputs = new Vector<>();

        for (Layer inputOwner: _inputOwners){
            _inputs.add(inputOwner.getOutputFormat());
        }


        if(_flatten) {
            if (_inputOwners.size() > 0)
                return _flatData(_inputOwners.firstElement().getOutputFormat());

            if (_inputs.size() > 0)
                return _flatData(_inputs.firstElement());
            System.err.println("Reshape data formats calculation error: no inputs set");
        }

        Tensor curOutputFormat = getOutputDataFormat();

        return curOutputFormat;
    }

    /**
     * TODO CHECK
     * Automatically calculates the min input height of the neuron
     * for dense block
     *
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight(int minOutputHeight) {

        Tensor curInputDataFormat = getInputDataFormat();

        /** if reshape node input is empty*/
        if (Tensor.isNullOrEmpty(curInputDataFormat)) {
            Tensor curOutputDataFormat = getOutputDataFormat();
            if (Tensor.isNullOrEmpty(curOutputDataFormat))
                return minOutputHeight;
            if (curOutputDataFormat.getDimensionality() < 2)
                return curOutputDataFormat.getDimSize(0);
            return curOutputDataFormat.getDimSize(1);

        }

        /** in case of vector,  return vector length */
        if (curInputDataFormat.getDimensionality() == 1)
            return curInputDataFormat.getDimSize(0);


        /** if height is already set up, return it without any changes*/
        if (curInputDataFormat.getDimensionality() > 1)
            return curInputDataFormat.getDimSize(1);

        /**return min height of previous data layer by default*/
        return minOutputHeight;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         multiple inputs resolving           ////

    /**
     * Set data formats from multiple inputs
     *
     * @param neuronOwner  layer, owns the neuron
     * @throws Exception if an error occurs
     */
    public void setDataFromMultipleInputs(Layer neuronOwner) throws Exception {
        Vector<Tensor> _inputs = new Vector<>();

        for (Layer inputOwner: _inputOwners){
            _inputs.add(inputOwner.getOutputFormat());
        }


        switch (_inputs.size()) {
            /** constant-data reshape node*/
            case 0: {
                neuronOwner.setInputFormat(getInputDataFormat());
                neuronOwner.setOutputFormat(getOutputDataFormat());
                break;
                   // System.err.println("Parameters update failed: Reshape layer " + neuronOwner.getName() + " have no inputs ");
                   // throw new Exception("Parameters update exception.");
            }
            /** in case of Reshape Node have only one input,
             * it is set as input and output data format*/
            case 1: {
                Tensor singleInput = _inputs.firstElement();
                Tensor inputDataFormat = new Tensor(singleInput);
                setInputDataFormat(inputDataFormat);
                neuronOwner.setInputFormat(singleInput);
                neuronOwner.setOutputFormat(inputDataFormat);

                break;
            }
            /** in case of Reshape Node have 2 inputs,
             *  one is set as input format and other set as output format
             *  */

            case 2: {
                Tensor inputDataFormat = _inputs.elementAt(0);
                Tensor outputDataFormat = _inputs.elementAt(1);
                setDataFormats(inputDataFormat, outputDataFormat);
                neuronOwner.setInputFormat(inputDataFormat);
                neuronOwner.setOutputFormat(outputDataFormat);
                break;
            }
        }



    }

    /**
     * Get list of minimal input data formats
     *
     * @return list of minimal input data formats
     */
    public Vector<Tensor> getMinInputDataFormats() {
        Vector<Tensor> minDataFormats = new Vector<>();
        minDataFormats.add(getInputDataFormat());
        return minDataFormats;
    }

    /**
     * Checks, if an input is acceptable for the node
     * Add Nodes accept inputs, that have the same shape with the output
     * or could be extended
     *
     * @param inputDataFormat input data format
     * @return true, if input node is acceptable and false otherwise
     */
    public boolean isAcceptableInput(Tensor inputDataFormat) {
        if (Tensor.isHaveSameElementsNumber(inputDataFormat, getInputDataFormat()))
            return true;
        return false;
    }

    /**
     * Get multiple node inputs
     * @return multiple node inputs
     */
    public Vector<Tensor> getInputs() {
        Vector<Tensor> inputs = new Vector<>();

        if(_inputOwners==null)
            return  inputs;

        for (Layer inputOwner: _inputOwners){
            inputs.add(inputOwner.getOutputFormat());
        }
        return inputs;
    }


    /**
     * Get multiple node input owners
     * @return multiple node input owners
     */
    public Vector<Layer> getInputOwners() { return _inputOwners; }


    /**
     * Add new input from another layer
     * @param inputOwner layer, owns the input
     */
    public void addInput(Layer inputOwner){
        _inputOwners.add(inputOwner);
    }

    /** Insert new input */
    public void insertInput(Layer inputOwner, int position) {
        _inputOwners.insertElementAt(inputOwner,position);
    }

     /**
     * Update neuron parameters after input connection was removed
     * @param neuronOwner, layer, owns this neuron
     * @param inputOwner layer, owns input
     */
    public void removeInput(Layer neuronOwner,Layer inputOwner) {
        if(_inputOwners!=null) {
            _inputOwners.remove(inputOwner);
        }
    }

     /**
     * Get number of operations, could be implemented for reshape node.
     * Reshape node implements single operation.
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(){ return 1; }

     /**
     * Calculate number of function calls inside of the neuron
     * Reshape node always called once per input
     * @return number of function calls inside of the node
     */
     @Override
    public int getFuncCallsNum(int scale){
         return 1;
    }

    /**
     * Check if the neuron is flatten neuron
     * Flatten layer transforms any input data shape to a vector
     * @return true, if the layer is flatten and false otherwise
     */
    public boolean isFlatten() {
        return _flatten;
    }

    /**
     * Set flag, if the neuron is flatten
     * @param flatten flag, if the neuron is flatten
     */
    public void setFlatten(boolean flatten) {
        this._flatten = flatten;
    }

        /**
     * Check if the neuron is slice neuron
     * Flatten layer transforms an input tensor into a sequence of subtensors
     * @return true, if the layer is slice and false otherwise
     */
    public boolean isSlice() {
        return _slice;
    }

    /**
     * Set flag, if the neuron is slice neuron
     * @param slice flag, if the neuron is slice neuron
     */
    public void setSlice(boolean slice) {
        this._slice = slice;
    }

    /**
     * Get function call description. If no execution code is
     * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description
     */
    public String getFunctionCallDescription(int channels){
        return getFunctionCallName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                     ////

    /**
     * Flat data format
     * @param input data format to be flatten
     * @return flatten (turned into vector) data format
     */
    private Tensor _flatData(Tensor input){
        if(input==null)
            return null;
        int totalInp = input.getElementsNumber();
        if(_inputsNum>1)
            totalInp*=_inputsNum;

        Tensor flatten = new Tensor(totalInp);

        return flatten;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    /** number of inputs, coming from previous layer =
     * number of neurons of previous layer or 1*/
    int _inputsNum = 0;

    /** flag, if shape param is set*/
    private transient boolean shapeParamSet = false;

    /**Flag, shows is the Reshape layer is Flatten layer
     * Flatten layer transforms any input data shape to a vector
     * */
    @SerializedName("flatten")private boolean _flatten = false;

        /**Flag, shows is the Reshape layer is Flatten layer
     * Flatten layer transforms any input data shape to a vector
     * */
    @SerializedName("slice")private boolean _slice = false;

    /** references to input layers*/
    private transient Vector<Layer> _inputOwners = new Vector<>();
}
