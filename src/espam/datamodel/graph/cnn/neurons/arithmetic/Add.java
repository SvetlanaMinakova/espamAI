package espam.datamodel.graph.cnn.neurons.arithmetic;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.neurontypes.ArithmeticOpType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;

/**
 * Class of add transformation Neuron:
 * Summarize the results of several input layers
 * IMPORTANT: Min number of inputs, coming from previous layers = 2+
 * For adding biases or any other constants to single input use NonLinear AddConstant
 *
 *
 * TODO FINISH IMPLEMENTATION
 */
public class Add extends Neuron implements MultipleInputsProcessor {
    /////////////////////////////////////////////////////////////////
    ////                         public methods                 ////

      /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(int channels){
      return Math.max(getInputsNumber() - 1,0);
    }
        /**
     * Create new Add element with a name
     * a stride and a kernelSize for the Add element = 1
     * an input sample dimension is 1 (vector)
     */
    public Add () {
        super(ArithmeticOpType.ADD.toString());
        setNeuronType(NeuronType.ADD);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

      /**
      * Compares Add neuron with another object
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

       Add add = (Add)obj;
         return this.getName().equals(add.getName())
              && getInputsNumber()==add.getInputsNumber()
              && Tensor.isSame(getInputDataFormat(),add.getInputDataFormat())
              && Tensor.isSame(getOutputDataFormat(),add.getOutputDataFormat());
       }

     /**
     * Set new data formats for neuron
     * @param inputDataFormats input data formats
     */
    public void setInputDataFormat(Vector<Tensor> inputDataFormats) {
       try {
           Tensor mergedInput = Tensor.mergeToSequence(inputDataFormats);
           setInputDataFormat(mergedInput);
           setOutputDataFormat(inputDataFormats.elementAt(0));
           setSampleDim(inputDataFormats.elementAt(0).getDimensionality());
       }
       catch (Exception e) {
           System.err.println(e.getMessage());
       }
    }


     /**
     * Clone this Add Neuron
     * @return a new reference on the Add Neuron
     */
    public Add clone() {
        Add newObj = (Add) super.clone();
        newObj._ownerNeuronsNumber = this._ownerNeuronsNumber;
        newObj._inputOwners = this._inputOwners;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     * @param a original neuron to be copied
     */
    public Add (Add a) {
        super(a);
        _ownerNeuronsNumber = a._ownerNeuronsNumber;

        _inputOwners = new Vector<>();
        for(Layer inputOwner: a._inputOwners)
            _inputOwners.add(inputOwner);
    }

    /**
     * Return the string description of the neuron specific parameters
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
     StringBuilder strParams = new StringBuilder();
     strParams.append("\n parameters: [\n");
     strParams.append("inputs number: "+ getInputsNumber() + "]");
     return strParams.toString();
    }

    /**
     * Automatically caclulates the output format of a neuron
     * number of outputs of one Add neuron = number of its inputs
     * ic case of vector, the point is returned
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        if(Tensor.isNullOrEmpty(inputDataFormat))
            return inputDataFormat;

        Tensor result = new Tensor(inputDataFormat);
        /** TODO is axis set to 1 or removed/or unchanged after adding??*/
      //  result.setDimSize(result.getDimensionality()-1,1);
        if(_inputOwners.size()==1 && _ownerNeuronsNumber>1)
            result.removeDimension();

        return result;
    }



     /////////////////////////////////////////////////////////////////////
    ////                         protected methods                    ////

    /**
     * Get inputs number of multiple inputs processor
     * @return inputs number of multiple inputs processor
     */
    public int getInputsNumber() {
        if(_inputOwners==null)
            return 0;

        return _inputOwners.size();
    }

    /**
     * TODO Data Height is not recalculated for this node for now
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight){
        return getOutputHeight();
    }

    /**
     * TODO Data Height is not recalculated for this node for now, no data flow minimization
     * TODO is performed
     * @param minOutputDataHeight min output data height
     * @return false
     */
    public boolean setMinDataHeight(int minOutputDataHeight){
        return false;
    }


    /////////////////////////////////////////////////////////////////////
    ////                  multiple inputs resolving                 ////
    /**TODO FINISH IMPLEMENTATION
     * Set data formats from multiple inputs
     * @param neuronOwner layer, owns the neuron
     * @throws Exception if an error occurs
     */
    public void setDataFromMultipleInputs(Layer neuronOwner) throws Exception{
        if(_inputOwners==null)
            throw new Exception("Add data formats calculation: no inputs found");


        Tensor commonInput = _inputOwners.firstElement().getOutputFormat();

        setInputDataFormat(commonInput);
        setOutputDataFormat(calculateOutputDataFormat(getInputDataFormat()));

        neuronOwner.setInputFormat(commonInput);
        neuronOwner.setOutputFormat(neuronOwner.calculateOutputFormat());

        setInputDataFormat(commonInput);
        setOutputDataFormat(commonInput);

        neuronOwner.setInputFormat(commonInput);
        neuronOwner.setOutputFormat(commonInput);
    }

    /**
     * Checks, if an input is acceptable for the node
     * Add Nodes accept inputs, that have the same shape with the output
     * or could be extended
     * @param inputDataFormat input data format
     * @return true, if input node is acceptable and false otherwise
     */
    public boolean isAcceptableInput(Tensor inputDataFormat){
        if(inputDataFormat.equals(getInputDataFormat()))
            return true;

        for(Layer inputOwner: _inputOwners) {
            if (inputOwner.getOutputFormat().equals(inputDataFormat))
                return true;
        }

        return false;
    }


    /////////////////////////////////////////////////////////////////////
    ////                  getters and setters                       ////
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
        if(!_inputOwners.contains(inputOwner))
        _inputOwners.add(inputOwner);
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


    ///////////////////////////////////////////////////////////////////
    ////      SDFG transformation-related  public methods         ////
     /**
     * Calculate number of function calls inside of the neuron
     * Add neuron always fires once for each input pair
     * @return number of function calls inside of the node
     */
     @Override
    public int getFuncCallsNum(int scale){
         if(_inputOwners==null)
             return 0;

         return (_inputOwners.size()-1)/scale;
    }

    /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getName());
        Integer operationTokensNum = getOperationTokensNumber(channels);
        desc.append("(" + operationTokensNum + ")");
        return desc.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** references to input layers*/
    private transient Vector<Layer> _inputOwners = new Vector<>();

    /**
     *  number of Add processing elements in layer-owner
     *  used for calculation of output format
     */
    private int _ownerNeuronsNumber = 1;
}
