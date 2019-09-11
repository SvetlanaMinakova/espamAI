package espam.datamodel.graph.cnn.neurons.normalization;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.CustomConnectionGenerator;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.TreeMap;

public class LRN extends Neuron implements ConnectionDependent, CustomConnectionGenerator {
     /////////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create new default LRN element
     */
    public LRN () {
       super(NeuronType.LRN.toString());
       setNeuronType(NeuronType.LRN);
    }

    /**
     * Create new LRN element with specified ratio
     */
    public LRN (int size) {
       super(NeuronType.LRN.toString());
       setNeuronType(NeuronType.LRN);
       _size = size;
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }


     /**
     * Clone this LRN Neuron
     * @return a new reference on the LRN Neuron
     */
    public LRN clone() {
        LRN newObj = (LRN) super.clone();
        newObj._size = this._size;
        newObj._alpha = this._alpha;
        newObj._beta = this._beta;
        newObj._bias = this._bias;
        return (newObj);
    }

    /**
     * Create a deep copy of this LRN Neuron
     * @param lrn LRN to be copied
     */
    public LRN(LRN lrn) {
        super(lrn);
        _size = lrn._size;
        _alpha = lrn._alpha;
        _beta = lrn._beta;
        _bias = lrn._bias;
    }

    /**TODO FINISH IMPLEMENTATION: MOCK*/
    /**
     * TODO Data Height is not recalculated for this node for now
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * if this inverse formula exists and return unchanged otherwise
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */

   // public int calculateMinInputDataHeight( int minOutputHeight) { return getInputHeight(); }


     /**
   * Compares LRN neuron with another object
   * @param obj Object to compare this Neuron with
   * @return true if Neuron is equal to the object and false otherwise
   */
    @Override
    public boolean equals(Object obj) {

        boolean isSuperParamsEqual = super.equals(obj);
        if (isSuperParamsEqual) {
            LRN lrn = ((LRN) obj);

            return   lrn._size == this._size &&
                     lrn._alpha == this._alpha &&
                     lrn._beta == this._beta &&
                     lrn._bias == this._bias;
        }

        return false;
    }

    /**
     * Return string description of neuron's specific parameters
     * @return string description of neuron's specific parameters
     */
    @Override
    public String getStrParameters() {
    StringBuilder strParams = new StringBuilder();
     strParams.append("\n parameters: [\n");
     strParams.append("  size: "+_size +" ]");
     return strParams.toString();
    }

    /**
     * Get LRN custom connection matrix. LRN custom connection matrix is
     * a square matrix with sparse connections, where _size number of
     * neighbor input neurons are connected to one output(LRN) neuron.
     * Thus, max distance from the "central" input neuron of each
     * output neuron if floor(size/2), where size is an LRN parameter
     * @return LRN custom connection matrix
     */
    public boolean[][] generateCustomConnectionMatrix(int srcNeuronsNum, int dstNeuronsNum){
        boolean[][] customConnectionMatrix = new boolean[srcNeuronsNum][dstNeuronsNum];
        int idDistance;
        int maxDistance = _size/2;

        for(int inpId = 0; inpId < srcNeuronsNum; inpId++) {
            for(int outpId = 0; outpId  < dstNeuronsNum; outpId ++) {
                idDistance = inpId - outpId;
                if(Math.abs(idDistance) <= maxDistance)
                    customConnectionMatrix[inpId][outpId] = true;
                else
                    customConnectionMatrix[inpId][outpId] = false;
        }
        }
        return customConnectionMatrix;
    }

    ///////////////////////////////////////////////////////////////////
    ////              data formats calculation and set up         ////

    /**
     * Automatically calculates the min input height of the neuron
     * FOR LRN 1 is returned by default, because for the normalization
     * across the channels, height = 1 value is enough
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight){
        return 1;
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
     * Get number of channels to be normalized
     * @return  number of channels to be normalized
     */
    public int getSize() {
        return this._size;
    }

    /**
     * Set number of channels to be normalized
     * @param size number of channels to be normalized
     */
    public void setSize(int size) {
        this._size = size;
    }

     /**
     * Get alpha-parameter of LRN
     * @return alpha-parameter of LRN
     */
    public float getAlpha() { return _alpha; }

      /**
     * Set alpha-parameter of LRN
     * @param alpha  alpha-parameter of LRN
     */
    public void setAlpha(float alpha) {
        this._alpha = alpha;
    }

      /**
     * Get beta-parameter of LRN
     * @return beta-parameter of LRN
     */
    public float getBeta() {
        return _beta;
    }

       /**
     * Set beta-parameter of LRN
     * @param beta beta-parameter of LRN
     */
    public void setBeta(float beta) {
        this._beta = beta;
    }

       /**
     * Get bias of LRN
     * @return bias of LRN
     */
    public float getBias() {
        return _bias;
    }

       /**
     * Set bias of LRN
     * @param bias bias of LRN
     */
    public void setBias(float bias) {
        this._bias = bias;
    }

        /** recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */

    /////////////////////////////////////////////////////////////////////
    ////                    neurons number resolve                  ////

    /**
     * despite of simple NonLinear node, LRN always have 1 neuron, which implements
     * normalization over input channels
     * TODO for now normalization is implemented over all input channels, but
     * TODO in general number of normalizations --> number of output neurons can vary
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     * @throws Exception if an error occurs
     */
        public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{
        int inputNN = input.getNeuronsNum();
        neuronOwner.setNeuronsNum(inputNN);
    }


    ///////////////////////////////////////////////////////////////////
    ////      SDFG transformation-related  public methods         ////

    /**
     * Get number of operations, could be implemented on the neurons with current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. For LRN node, number of operations over channel = _size parameter
     * Number of operations over all channels = _size * channels number
     * @return number of operations, could be implemented on the input data
     */
    @Override
    public int getOperationsNumber(int channels){
    if(Tensor.isNullOrEmpty(getInputDataFormat()))
        return 0;

    return _size * channels;
    }

    /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getName());
        desc.append("(");
        desc.append(_size);
        desc.append(")");

        return desc.toString();
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    @Override
    public void initOperator(int inputChannels, int outputChannels) {
        TreeMap<String,Integer> intParams = _operator.getIntParams();
        TreeMap<String,Tensor> tensorParams = _operator.getTensorParams();

        intParams.put("size",_size);

        _addFloatParamAsScaledIntParam(_alpha,"alpha");
        _addFloatParamAsScaledIntParam(_beta,"beta");
        _addFloatParamAsScaledIntParam(_bias,"kappa");

        /** TODO: check!*/
        int squaredSize = getInputDataFormat().getElementsNumber() * inputChannels;
        int normsSize = squaredSize;

        tensorParams.put("squared",new Tensor(squaredSize));
        tensorParams.put("norms",new Tensor(normsSize));
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        int timeComplexity = 1;
        if(!(getInputDataFormat()==null)){
            timeComplexity = getSize() * getInputHeight() * getInputWidth();
        }

        _operator.setTimeComplexity(timeComplexity);
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /**
     *  Number of channels to be normalized
     * */
     @SerializedName("size")private int _size = 1;

     /**
     * Alpha-parameter of LRN
     * */
     @SerializedName("alpha")private float _alpha =  0.0001f;

     /**
     * Beta-parameter of LRN
     * */
     @SerializedName("beta")private float _beta =  0.75f;

     /**
     * Bias of LRN
     * */
     @SerializedName("_bias")private float _bias =  1.0f;
}
