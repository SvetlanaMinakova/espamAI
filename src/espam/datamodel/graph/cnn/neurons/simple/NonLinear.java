package espam.datamodel.graph.cnn.neurons.simple;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;
/**
 * Non-linear element such as ReLu, sigm, thn
 * TODO references to NonLinear Elements description
 * One node of first ReLu layer = Node for operating over the whole input data chunk
 * of one feature map
 */
public class NonLinear extends Neuron implements ConnectionDependent {
    
     /**
     * Create new default NonLinear element
     * a stride and a kernelSize for the NonLinear element = 1
     * a default input sample dimension is 1 (vector)
     * By default non-linear element is ReLu element
     */
    public NonLinear () {
        super(NonLinearType.ReLU.toString());
        setNeuronType(NeuronType.NONLINEAR);
    }

    /**
     * Create new NonLinear element with a name and default settings
     * a stride and a kernelSize for the NonLinear element = 1
     * a default input sample dimension is 1 (vector)
     * The choice of the name of a nonlinear element is limited by the
     * range of current Non-linear elements
     */
    public NonLinear (NonLinearType name) {
        super(name.toString());
        setNeuronType(NeuronType.NONLINEAR);
    }

     /**
     * Create a deep copy of this neuron
     * @param n original neuron to be copied
     */
    public NonLinear(NonLinear n) {
        super(n);
    }


    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

      /**
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight){
        return Math.max(minOutputHeight,1);
    }

    /**
     * Automatically caculates the output format of a neuron
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        return new Tensor(inputDataFormat);
    }

    /**
     * Set input data format
     * @param inputDataFormat input data format
     */
    @Override
    public void setInputDataFormat(Tensor inputDataFormat) {
        super.setInputDataFormat(inputDataFormat);
        setSampleDim(inputDataFormat.getDimensionality());
    }
     /**
     * Set output data format
     * @param outputDataFormat output data format
     */
    @Override
    public void setOutputDataFormat(Tensor outputDataFormat) {
        super.setOutputDataFormat(outputDataFormat);
        setSampleDim(outputDataFormat.getDimensionality());
    }

         /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description
     */
    public String getFunctionCallDescription(){
        return getFunctionCallName();
    }

        /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getName());
      //  Integer operationTokensNum = getOperationTokensNumber(channels);
      //  desc.append("(" + operationTokensNum + ")");

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
            /** neuron owner */
        System.err.println("Parameters update fail: NonLinear layer " + neuronOwner.getName()+" should not have multiple inputs");
        throw new Exception("NonLinear layer "+neuronOwner.getName()+" parameters update fail:");
    }

    ///////////////////////////////////////////////////////////////////
    ////                 Getters and setters                       ///

    /**
     * Get number of input tokens for each operation, perfomed in a neuron.
     * If the input data format is null or empty,0 tokens is returned.
     * For neurons, always taking on input a single value, null-description is returned.
     * For neurons, which performs shape transformation, null-description is returned.
     * @return number of input tokens for each operation, perfomed in a neuron
     */
    @Override
    public int getOperationTokensNumber(int channels){
        if(super.getOperationTokensNumber(channels)==0)
            return 0;
        return 1;
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
    Tensor inputDataFormat = getInputDataFormat();
        if(Tensor.isNullOrEmpty(inputDataFormat))
        return 0;
        return inputDataFormat.getElementsNumber() * channels;
    }
}
