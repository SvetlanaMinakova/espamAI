package espam.datamodel.graph.cnn.neurons.arithmetic;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.neurontypes.ArithmeticOpType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;

/**
 * Class of Arithmetic Neuron:
 * Arithmetic neuron accepts 1+ inputs, coming from previous layers and performs
 * such operations as Add or Multiply
 * For applying bias or any other constant to single input you can also use NonLinear AddConst/MulConst
 */
public class Arithmetic extends Neuron implements MultipleInputsProcessor, ConnectionDependent {
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
     * Create new Arithmetic element with a name
     * a stride and a kernelSize for the Arithmetic element = 1
     * an input sample dimension is 1 (vector)
     */
    public Arithmetic (ArithmeticOpType name) {
        super(name.toString());
        setNeuronType(NeuronType.ARITHMETIC);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

      /**
      * Compares Arithmetic neuron with another object
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

       Arithmetic Arithmetic = (Arithmetic)obj;
         return this.getName().equals(Arithmetic.getName())
              && getInputsNumber()==Arithmetic.getInputsNumber()
              && Tensor.isSame(getInputDataFormat(),Arithmetic.getInputDataFormat())
              && Tensor.isSame(getOutputDataFormat(),Arithmetic.getOutputDataFormat());
       }

     /**
     * Set new data formats for neuron
     * @param inputDataFormats input data formats
     */
    public void setInputDataFormat(Vector<Tensor> inputDataFormats) {
       try {

           Tensor mergedInput;
           if (inputDataFormats.size() == 1)
               mergedInput = inputDataFormats.firstElement();
           else
               mergedInput = Tensor.mergeToSequence(inputDataFormats);
           setInputDataFormat(mergedInput);

           setOutputDataFormat(inputDataFormats.elementAt(0));
           setSampleDim(inputDataFormats.elementAt(0).getDimensionality());
       }
       catch (Exception e) {
           System.err.println(e.getMessage());
       }
    }


     /**
     * Clone this Arithmetic Neuron
     * @return a new reference on the Arithmetic Neuron
     */
    public Arithmetic clone() {
        Arithmetic newObj = (Arithmetic) super.clone();
        newObj._ownerNeuronsNumber = this._ownerNeuronsNumber;
        newObj._inputOwners = this._inputOwners;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     * @param a original neuron to be copied
     */
    public Arithmetic (Arithmetic a) {
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
     * Automatically calculates the output format of a neuron
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {

        return inputDataFormat;
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
            throw new Exception("Arithmetic data formats calculation: no inputs found");


        Tensor common = _inputOwners.firstElement().getOutputFormat();
        setInputDataFormat(common);
        setOutputDataFormat(common);

        neuronOwner.setInputFormat(common);
        neuronOwner.setOutputFormat(common);

        setSampleDim(common.getDimensionality());

    }

    /**
     * Checks, if an input is acceptable for the node
     * Arithmetic Nodes accept inputs, that have the same shape with the output
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
     * Arithmetic new input from another layer
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
     * Arithmetic neuron always fires once for each input pair
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

    /** recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */
    public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{

        if(input != null ){

            neuronOwner.setNeuronsNum(input.getNeuronsNum());
                return;
            }

        System.err.println("Parameters update fail: arithmetic layer " + neuronOwner.getName());
        throw new Exception("Arithmetic layer "+neuronOwner.getName()+" parameters update fail:");
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    @Override
    public void initOperator(int inputChannels, int outputChannels) {
        if (getBiasName() != null)
        {
            if(getBiasName() != "bias") {
                _operator.initStringParams();
                _operator.getStringParams().put("bias_ref",getBiasName());
            }

            Integer size = outputChannels;
            Tensor bias = new Tensor(size);
            _operator.getTensorParams().put("bias",bias);
        }

        //tensor refs
        Vector<Layer> inputs = getInputOwners();
        Integer inpId = 0;
        Integer minInputs = 2;
        for(Layer input: inputs){
            _operator.addTensorRef("input" + inpId,input.getName());
            inpId++;
        }
        if(inpId<minInputs)
        for(int i=inpId;i<minInputs;i++){
            _operator.addTensorRef("input" + inpId,"null");
        }

        //min inputs number = 2. If second input is lacking, it is replaced by null-ref


        /**
         *  Vector<CSDFPort> inputs = _getConcatSortedInputPorts(node);
        Integer inpId = 0;
        String muref;
        String muname;
        MemoryUnit mu;
       for(CSDFPort input: inputs){
           muname = "input" + inpId;
           mu = input.getAssignedMemory();
           if(mu!=null)     muref = "&"+ mu.getName() + "[0]";
           else muref = "nullptr";

           _printStream.println(_prefix + "tensor_params[\""+ muname +"\"] = " + muref + ";");
           inpId++;

           }

           if(inpId==1)
               _printStream.println(_prefix + "tensor_params[\"input1\"] = nullptr;");

         */

    }

        /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
       Long timeComplexity = 1l;

       if(!(getInputDataFormat()==null))
            timeComplexity = (long) getInputDataFormat().getElementsNumber();

        _operator.setTimeComplexity(timeComplexity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** references to input layers*/
    private transient Vector<Layer> _inputOwners = new Vector<>();

    /**
     *  number of Arithmetic processing elements in layer-owner
     *  used for calculation of output format
     */
    private int _ownerNeuronsNumber = 1;
}
