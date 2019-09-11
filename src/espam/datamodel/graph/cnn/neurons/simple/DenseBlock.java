package espam.datamodel.graph.cnn.neurons.simple;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.DenseType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;

/**
 * Non-linear element such as ReLu, sigm, thn
 * TODO references to DenseBlock Elements description
 * One node of first ReLu layer = Node for operating over the whole input data chunk
 * of one feature map
 */
public class DenseBlock extends Neuron {
 /**
     * Create new DenseBlock element with a name
     * a stride and a kernelSize for the DenseBlock element = 1
     * an input sample dimension is 1 (vector)
     * By default, Dense Block is THN Dense block
     * @param neuronsNum number of the hidden neurons inside the Dense block
     */
    public DenseBlock(int neuronsNum) {
        super(NonLinearType.NONE.toString());
        setNeuronType(NeuronType.DENSEBLOCK);
        setRefinedType(DenseType.DENSEBLOCK);
        setNeuronsNum(neuronsNum);
    }

    /**
     * Create new DenseBlock element with a name
     * a stride and a kernelSize for the DenseBlock element = 1
     * an input sample dimension is 1 (vector)
     * @param name name of the Dense Block.
     * @param neuronsNum number of the hidden neurons inside the Dense block
     */
    public DenseBlock (NonLinearType name, int neuronsNum) {
        super(name.toString());
        setNeuronType(NeuronType.DENSEBLOCK);
        setRefinedType(DenseType.DENSEBLOCK);
        setNeuronsNum(neuronsNum);
    }

    /**
     * Create new DenseBlock element with a name
     * a stride and a kernelSize for the DenseBlock element = 1
     * an input sample dimension is 1 (vector)
     * @param name name of the Dense Block.
     * @param neuronsNum number of the hidden neurons inside the Dense block
     * @param refinedType refined type of operation, implemented inside of the dense block
     */
    public DenseBlock (NonLinearType name, DenseType refinedType, int neuronsNum) {
        super(name.toString());
        setRefinedType(refinedType);
        setNeuronType(NeuronType.DENSEBLOCK);
        setNeuronsNum(neuronsNum);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

      /**
     *  Clone this DenseBlock
     *
     * @return  copy of this DenseBlock
     */
    public Object clone() {
        DenseBlock newObj = (DenseBlock) super.clone();
        newObj.setNeuronsNum(_neuronsNum);
        newObj.setRefinedType(_refinedType);
        return (newObj);
    }

     /**
     * Create a deep copy of this neuron
     * @param db original neuron to be copied
     */
    public DenseBlock (DenseBlock db) {
        super(db);
        setNeuronsNum(db._neuronsNum);
        setRefinedType(db._refinedType);
    }

    /**
     * Return the string description of the neuron specific parameters
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
     StringBuilder strParams = new StringBuilder();
     strParams.append("\n [\n");
     strParams.append("neurons: "+_neuronsNum + "]\n");
     return strParams.toString();
    }

      /**
      * Compares DenseBlock with another object
      * @param obj Object to compare this DenseBlock with
      * @return true if DenseBlock is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

       DenseBlock block = (DenseBlock)obj;
         return this.getName().equals(block.getName())
              && Tensor.isSame(getInputDataFormat(),block.getInputDataFormat())
              && Tensor.isSame(getOutputDataFormat(),block.getOutputDataFormat());
       }


    ///////////////////////////////////////////////////////////////////
    ////              data formats calculation and set up         ////

    /**
     * Automatically calculates the output format of a neuron
     * number of outputs of one DenseBlock neuron = number of neurons inside it
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        return new Tensor(_neuronsNum);
    }

     /**TODO CHECK
     * Automatically calculates the min input height of the neuron
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight) {

        Tensor currentInputDataFormat = getInputDataFormat();
        if(Tensor.isNullOrEmpty(currentInputDataFormat))
            return 1;

        if(currentInputDataFormat.getDimensionality()>1)
            return currentInputDataFormat.getDimSize(1);

        return 1;
    }


    ///////////////////////////////////////////////////////////////////
    ////                       Getters and setters                ////
    /**
     * get number of neurons inside the Dense block
     * @return number of neurons inside the Dense block
     */
    public int getNeuronsNum() {
        return _neuronsNum;
    }

    /**
     * get number of neurons inside the Dense block
     * @param neuronsNum number of neurons inside the Dense block
     */
    public void setNeuronsNum(int neuronsNum) {
        this._neuronsNum = neuronsNum;
    }

    /**
     * Get Neurons refined type
     * @return Neuron's refined type
     */
    public DenseType getRefinedType() {
        return _refinedType;
    }

    /**
     * Set operation refined type
     * @param refinedType refined type of operation inside of Dense block
     */
    public void setRefinedType(DenseType refinedType) {
        _refinedType = refinedType;
    }

        /**
     * Get function call name
     * By default, function call name is a name of a neuron
     * @return function call name
     */
    @Override
    public String getFunctionCallName() {
        if(_refinedType==null)
        return getName();

        return _refinedType.toString();
    }


    ///////////////////////////////////////////////////////////////////
    ////            CSDFG-model-related parameters                ////

    /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. Else, MatMul is considered to be one big block
     * operation over the input.
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(int channels){
        return 1;
    }

      /**
     * Calculate number of function calls inside of the neuron
     * classical DenseBlock runs once per input sample, but if
     * necessary it can be changed in future (e.g. by using partial sums)
     * @return number of function calls inside of the node
     */
     @Override
    public int getFuncCallsNum(int scale){
         return 1;
    }

     /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getRefinedType() + "_" + getName());
        Tensor inputDataFormat = getInputDataFormat();
        if(!Tensor.isNullOrEmpty(inputDataFormat)) {
            int indataTokens = inputDataFormat.getElementsNumber() * channels;
            Tensor flattenInData = new Tensor(1,indataTokens);
            desc.append("(");
            desc.append(Tensor.toOpParam(flattenInData));
            desc.append(",");
            Tensor weightsDesc = new Tensor(indataTokens,_neuronsNum);
            desc.append(Tensor.toOpParam(weightsDesc));
            desc.append(")");
        }
       // Integer operationTokensNum = getOperationTokensNumber(channels);
       // desc.append("(" + operationTokensNum + ")");

        return desc.toString();
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

            Integer size = _neuronsNum;
            Tensor bias = new Tensor(size);
            _operator.getTensorParams().put("bias",bias);
        }

        Tensor input = getInputDataFormat();
        if(!Tensor.isNullOrEmpty(input)) {

            Tensor weights = new Tensor();
            int blockNeuronsNum = _neuronsNum;
            int lin_input = input.getElementsNumber();
            /** TODO: refactoring! does not work with Concat inp!*/
            if(inputChannels>1)
                lin_input*=inputChannels;

            weights.addDimension(blockNeuronsNum);
            weights.addDimension(lin_input);

            _operator.getTensorParams().put("weights",weights);
        }
        else {System.out.println("Dense weights construction error: null or empty input data format!");}

    }

        /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        int timeComplexity = 1;
        if(!(getInputDataFormat()==null)){
            timeComplexity = getInputDataFormat().getElementsNumber() *
                    Math.max(inputChannels,1) * _neuronsNum;
        }

        _operator.setTimeComplexity(timeComplexity);
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////
    /** Number of neurons inside the Dense block*/
    @SerializedName("neuronsNum")private int _neuronsNum;

    /** refined type of Matrix multiplication, implemented inside of the Dense Layer*/
    @SerializedName("refinedType")private DenseType _refinedType;
}
