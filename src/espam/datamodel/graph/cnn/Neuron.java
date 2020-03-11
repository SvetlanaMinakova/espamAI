package espam.datamodel.graph.cnn;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.arithmetic.Arithmetic;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.simple.*;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.cnn.neurons.transformation.Transpose;
import espam.datamodel.graph.cnn.neurons.transformation.Upsample;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Describes one Neuron of the Neural Network
 */
public abstract class Neuron implements Cloneable{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Public constructor for an empty neuron is required for .json serializers
     */
    public Neuron() {
       _operator = new Operator("op");
    }

    /**
     * Private constructor of neuron, used by builder
     * @param name name of the neuron
     */
    public Neuron(String name) {
        setName(name);
        _operator = new Operator(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) {
        x.visitComponent(this);
    }

    /**
     *  Clone this Neuron
     * @return  a new reference on instance of the Neuron
     */
   @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
        Neuron newObj = (Neuron)super.clone();
        newObj.setInputDataFormat((Tensor)_inputDataFormat.clone());
        newObj.setOutputDataFormat((Tensor)_outputDataFormat.clone());
        newObj.setNeuronType(neuronType);
        newObj.setSampleDim(_sampleDim);
        newObj.setBiasName(_biasName);
        newObj.setNonlin(_nonlin);
        newObj.setOperator((Operator)_operator.clone());
        return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     * Copy neuron of a certain instance
     * @param neuron neuron to be copied
     * @return copy of the neuron
     */
    public static Neuron copyNeuron(Neuron neuron){
        if (neuron instanceof Arithmetic)
            return new Arithmetic ((Arithmetic) neuron);

        if (neuron instanceof Data)
            return new Data((Data)neuron);

        if (neuron instanceof Convolution)
            return new Convolution((Convolution)neuron);

        if (neuron instanceof Pooling)
            return new Pooling((Pooling)neuron);

        if (neuron instanceof CNNNeuron) {
            if(neuron.getNeuronType().equals(NeuronType.CONV))
                return new Convolution((Convolution)neuron);
            if(neuron.getNeuronType().equals(NeuronType.POOL))
                return new Pooling((Pooling) neuron);
        }

        if (neuron instanceof DenseBlock)
            return new DenseBlock((DenseBlock) neuron);

        if (neuron instanceof Dropout)
            return new Dropout((Dropout)neuron);

        if (neuron instanceof Concat)
            return new Concat((Concat) neuron);

        if (neuron instanceof GenericNeuron)
            return new GenericNeuron((GenericNeuron) neuron);

        if (neuron instanceof LRN)
            return new LRN((LRN) neuron);

        if (neuron instanceof NonLinear)
            return new NonLinear((NonLinear) neuron);

        if (neuron instanceof Reshape)
            return new Reshape((Reshape) neuron);

        if (neuron instanceof Upsample)
            return new Upsample((Upsample)neuron);

        if(neuron instanceof Transpose)
            return new Transpose((Transpose)neuron);

        return new NoneTypeNeuron(neuron.getName());
    }

    /**
     * Return description of a neuron
     * @return description of a neuron
     */
    @Override
    public String toString() {
       StringBuilder strNeuron = new StringBuilder();
       strNeuron.append(getName());
       strNeuron.append("\n type: "+getNeuronType().toString());
       strNeuron.append(getStrParameters());
       return strNeuron.toString();
    }

    /**
     * Return string description of neuron's specific parameters
     * @return string description of neuron's specific parameters
     */
    public String getStrParameters() {
        return "";
    }

    /**
      * Compares Neuron with another object
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

       Neuron neuron = (Neuron)obj;
         return this._name.equals(neuron._name)
              &&   this.neuronType.equals(neuron.neuronType)
              && Tensor.isSame(_inputDataFormat,neuron._inputDataFormat)
              && Tensor.isSame(_outputDataFormat,neuron._outputDataFormat);
       }

    ///////////////////////////////////////////////////////////////////
    ////              data formats calculation and set up         ////

       /**
     * Set neuron's data formats
     */
    public void setDataFormats(Tensor inputDataFormat, Tensor outputDataFormat){
        setInputDataFormat(inputDataFormat);
        setOutputDataFormat(outputDataFormat);
    }

    /**
     * Clear neuron's data formats
     */
    public void clearDataFormats() {
        setInputDataFormat(new Tensor());
        setOutputDataFormat(new Tensor());
    }

       /**
     * Calculates output format of a neuron, using the information
     * about the input data format and specific neuron's features
     * @param inputDataFormat input data format of the neuron
     * @return output data format of the neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        return getOutputDataFormat();
    }

     /**
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * if this inverse formula exists and return unchanged otherwise
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */

    public int calculateMinInputDataHeight( int minOutputHeight) { return minOutputHeight; }

    /**
     * Updates min input data height, if possible
     * TODO fow now multiple inputs processors work like shippers, so their data flow in not minimized
     * @param minOutputDataHeight min output data height
     * @return true, if data height was updated and false otherwise
     */
    public boolean setMinDataHeight(int minOutputDataHeight){
        if(this instanceof MultipleInputsProcessor)
            return false;

        int minInputDataHeight= calculateMinInputDataHeight(minOutputDataHeight);
        boolean inputHeightSet = setInputHeight(minInputDataHeight);
        boolean outputHeightSet = setOutputHeight(minOutputDataHeight);

        return inputHeightSet && outputHeightSet;
    }

    /**
     * Get neuron input height
     * @return neuron input dataFormat height
     */
    public int getInputHeight(){
        if(Tensor.isNullOrEmpty(_inputDataFormat))
            return 0;

        if(_inputDataFormat.getDimensionality()<2)
            return 1;

        return _inputDataFormat.getDimSize(1);
    }

        /**
     * Get neuron input height
     * @return neuron input dataFormat height
     */
    public int getInputWidth(){
        if(Tensor.isNullOrEmpty(_inputDataFormat))
            return 0;

        return _inputDataFormat.getDimSize(0);
    }

      /**
     * Set neuron input height
     * @return true, if neuron input height was updated and false otherwise
     */
    public boolean setInputHeight(int newHeight){
        if(Tensor.isHaveHeight(_inputDataFormat)) {
            _inputDataFormat = new Tensor(_inputDataFormat);
            _inputDataFormat.setDimSize(1, newHeight);
            return true;
        }
        return false;
    }

    /**
     * Get neuron output height
     * @return neuron output dataFormat height
     */
      public int getOutputHeight(){
        if(Tensor.isNullOrEmpty(_outputDataFormat))
            return 0;

        if(_outputDataFormat.getDimensionality()<2)
            return 1;

        return _outputDataFormat.getDimSize(1);
    }

    /**
     * Get neuron output height
     * @return neuron output dataFormat height
     */
      public int getOutputWidth(){
        if(Tensor.isNullOrEmpty(_outputDataFormat))
            return 0;

        return _outputDataFormat.getDimSize(0);
    }

   /**
     * Set neuron output height
     * @return true, if neuron output height was updated and false otherwise
     */
    public boolean setOutputHeight(int newHeight){
        if(Tensor.isHaveHeight(_outputDataFormat)){
            _outputDataFormat = new Tensor(_outputDataFormat);
            _outputDataFormat.setDimSize(1, newHeight);
            return true;
        }
        return false;
    }

      /**
     * Set neuron output height
     * @return true, if neuron output height was updated and false otherwise
     */
    public boolean setOutputWidth(int newWidth){
        if(!Tensor.isNullOrEmpty(_outputDataFormat)){
            _outputDataFormat = new Tensor(_outputDataFormat);
            _outputDataFormat.setDimSize(0, newWidth);
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////           Operator                                       ////
     /**
     * Add to operator parameters inputs from multiple sources
     * @param layerInputFormat layer input data format
     */
    public void _addInputToOpPar(Tensor layerInputFormat){
        TreeMap<String,Tensor> tensorParams = _operator.getTensorParams();
         if (this instanceof MultipleInputsProcessor) _addMultipleInputsToOpPar();
         else tensorParams.put("input", layerInputFormat);
    }

    /**
     * Add to operator parameters inputs from multiple sources
     * @param layerOutputFormat layer output data format
     */
    public void _addOutputToOpPar(Tensor layerOutputFormat){
        TreeMap<String,Tensor> tensorParams = _operator.getTensorParams();
        tensorParams.put("output", layerOutputFormat);
    }

     /** Add to operator parameters inputs from multiple sources*/
    private void _addMultipleInputsToOpPar(){
        TreeMap<String,Tensor> tensorParams = _operator.getTensorParams();
        Vector<Layer> inputs = ((MultipleInputsProcessor)this).getInputOwners();
        if(inputs==null) return;
        if(inputs.size()==0) return;

        for(Layer input: inputs)
           tensorParams.put(input.getName(), input.getOutputFormat());
    }

    ///////////////////////////////////////////////////////////////////
    ////            CSDFG-model-related parameters                ////

     /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description
      */

    public abstract String getFunctionCallDescription(int channels);

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    public abstract void initOperator(int inputChannels, int outputChannels);

     /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected abstract void setOperatorTimeComplexity(int inputChannels, int outputChannels);


    /** TODO: refactoring*/
    /** Store an operator float parameter as two integer parameters*/
    protected  void _addFloatParamAsScaledIntParam(Float par, String name){
        TreeMap<String,Integer> intParams = _operator.getIntParams();

        Integer scale;
        Float floatScale;
        Float scaled;
        Integer parInt;

        if(par!=null) {
            scale = _getScale(par);
            floatScale = Float.parseFloat(scale.toString());
            scaled = par * floatScale;
            parInt = scaled.intValue();
            intParams.put(name, parInt);
            intParams.put(name + "_scale", scale);

           // System.out.println(x.getName() + " constval scaled: " + constvalInt + " with scale: " + scale);
        }
    }

    /**
     * Scale float/double parameters to int parameters
     * @param x float/double parameter
     * @return number of integers after comma
     */
    private int _getScale(Float x){
        String dstring = x.toString();
        int decimalLen = dstring.length()-(dstring.indexOf(".")+1);
        return (int)Math.pow(10,decimalLen);
    }


     /**
     * TODO minH scale!
     * Calculate number of function calls inside of the neuron
     * with current data formats
     * As every neuron produces 1 value on output, function
     * calls number for every simple neuron = its output height
     * @return number of function calls inside of the neuron
     */
    public int getFuncCallsNum(int minInputH){
      int funcCallsNum = getOutputH();
        if(minInputH>getMinInputH()){
            int scale = minInputH/getMinInputH();
            int datatail = 0;
            if(funcCallsNum%scale!=0)
                datatail=1;
            funcCallsNum=funcCallsNum/scale + datatail;
        }
        return funcCallsNum;
    }

     /**
     * Get number of input tokens for each operation, perfomed in a neuron.
     * If the input data format is null or empty,0 tokens is returned.
     * For neurons, always taking on input a single value, null-description is returned.
     * For neurons, which performs shape transformation, null-description is returned.
     * @return number of input tokens for each operation, perfomed in a neuron
     */
    public int getOperationTokensNumber(int channels){
        if(Tensor.isNullOrEmpty(_inputDataFormat))
            return 0;
        return _inputDataFormat.getElementsNumber()*channels;
    }

    ///////////////////////////////////////////////////////////////////
    ////                Getters and setters                       ////

    /**
     * Get name of this neuron
     * @return name of this neuron
     */
    public String getName() {
        return _name;
    }

    /**
     * Set name of this neuron
     * @param  name name of this neuron
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Get input sample dimension
     * @return input sample dimension
     */
    public int getSampleDim() {
        return _sampleDim;
    }

    /**
     * Set input sample dimension
     * @param sampleDim  input sample dimension
     */
    public void setSampleDim(int sampleDim) {
        this._sampleDim = sampleDim;
    }

    /**
     * Get type of the neuron
     * @return type of the neuron
     */
    public NeuronType getNeuronType() {
        return neuronType;
    }

    /**
     * Set type of the neuron
     * @param neuronType neuron's type
     */
    public void setNeuronType(NeuronType neuronType) {
        this.neuronType = neuronType;
    }

    /**
     * Get input data format of the neuron
     * @return  input data format of the neuron
     */
    public Tensor getInputDataFormat() {
        return _inputDataFormat;
    }

    /**
     * Set input data format of the neuron
     * @param inputDataFormat  input data format of the neuron
     */
    public void setInputDataFormat(Tensor inputDataFormat) {
        this._inputDataFormat = inputDataFormat;
    }

     /**
     * Get output data format of the neuron
     * @return  output data format of the neuron
     */
    public Tensor getOutputDataFormat() {
        return _outputDataFormat;
    }

    /**
     * Set input data format of the neuron
     * @param outputDataFormat  input data format of the neuron
     */
    public void setOutputDataFormat(Tensor outputDataFormat) {
        this._outputDataFormat = outputDataFormat;
    }

    /**
     * Set new data formats for neuron
     * @param inputDataFormat input data format
     */
    public void setDataFormats(Tensor inputDataFormat) {
        setInputDataFormat(inputDataFormat);
        setOutputDataFormat(calculateOutputDataFormat(inputDataFormat));
    }

    ///////////////////////////////////////////////////////////////////
    ////      SDFG transformation-related  public methods         ////

    /**
     * Get function call name
     * By default, function call name is a name of a neuron
     * @return function call name
     */
    public String getFunctionCallName() { return _name; }

    /**
     * TODO: REFACTORING
     * returns minimal height of input. By default min height = 1
     * @return minimal height of input. By default min height = 1
     */
    public int getMinInputH() {
        return Math.max(1,_inpH);
    }

    /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     */
    public abstract int getOperationsNumber(int channels);

    /**
     * Get neuron output data height
     * @return output data height
     */
    public int getOutputH(){
        if(Tensor.isNullOrEmpty(_outputDataFormat))
            return 0;

        if(_outputDataFormat.getDimensionality()<2)
            return 1;

        return _outputDataFormat.getDimSize(1);
    }

    /**
     * Get input height, acceptable by the neuron
     * @return input height, acceptable by the neuron
     */
    public int getInpH() { return _inpH; }

    /**
     * Set input height, acceptable by the neuron
     * @param inpH input height, acceptable by the neuron
     */
    public void setInpH(int inpH) { this._inpH = inpH; }


     /////////////////////////////////////////////////////////////////////
    ////           additional parameters processing                    ////

    /**
     * Create additional parameters HashMap
     */
    public void initParams(){
        _parameters = new HashMap<>();
    }

    /**
     * Add new additional parameter
     * @param name parameter name
     * @param valueDescription string description (toString() ) of
     * the parameter value
     */
    public void setParameter(String name, String valueDescription){
        if(_parameters==null)
              _parameters = new HashMap<>();
        _parameters.put(name,valueDescription);
    }

    /**
     * Get additional integer parameter
     * @param name name of the parameter
     * @return parameter with given name or null
     */
    public Integer getIntParameter(String name){
       try{
          String strParam = _parameters.get(name);
          Integer intParam = Integer.parseInt(strParam);
          return intParam;
       }
       catch (Exception e) {
           return null;
       }
    }

       /**
     * Get specific double parameter
     * @param name name of the parameter
     * @return parameter with given name or null
     */
    public Double getDoubleParameter(String name){
       try{
          String strParam = _parameters.get(name);
          Double dParam = Double.parseDouble(strParam);
          return dParam;
       }
       catch (Exception e) {
           return null;
       }
    }

    /**
     * Get specific float parameter
     * @param name name of the parameter
     * @retur parameter with given name or null
     */
    public Float getFloatParameter(String name){
       try{
          String strParam = _parameters.get(name);
          Float fParam = Float.parseFloat(strParam);
          return fParam;
       }
       catch (Exception e) {
           return null;
       }
    }

      /**
     * Get specific integer parameter
     * @param name name of the parameter
     * @return specific integer parameter with given name or null
     */
    public String getStringParameter(String name){
       try{
          String strParam = _parameters.get(name);
          return strParam;
       }
       catch (Exception e) {
           return null;
       }
    }

     /**
     * Get specific integer parameter
     * @param name name of the parameter
     * @return specific integer parameter with given name or null
     */
    public Boolean getBooleanParameter(String name){
       try{
          String strParam = _parameters.get(name);
          Boolean bParam = Boolean.parseBoolean(strParam);
          return bParam;
       }
       catch (Exception e) {
           return null;
       }
    }


    /**
     * Get specific neuron parameters, that are not mentioned as Neuron fields
     * @return list of specific neuron parameters, that are not mentioned as Neuron fields
     */
    public HashMap<String, String> getParameters() {
        return _parameters;
    }


    /**
     * Set specific neuron parameters, that are not mentioned as Neuron fields
     * @param Parameters list of specific
     * neuron parameters, that are not mentioned as Neuron fields
     */
    public void setParameters(HashMap<String, String> Parameters) {
        this._parameters = Parameters;
    }

    /////////////////////////////////////////////////////////////////////
    ////     connection-dependent nodes incapsualtion               ////

    /**
     * Set bias as a reference to external node/file
     * @param bias bias as a reference to external node/file
     */
    public void setBiasName(String bias) { this._biasName = bias; }

    /**
     * Get bias as a reference to external node/file
     * @return  bias as a reference to external node/file
     */

    public String getBiasName() { return _biasName; }

    /**
     * Get built-in nonlinearity
     * @return built-in nonlinearity
     */
    public String getNonlin() { return _nonlin; }

    /**
     * Set built-in nonlinearity
     * @param nonlin  built-in nonlinearity
     */
    public void setNonlin(String nonlin) { this._nonlin = nonlin; }

    ///////////////////////////////////////////////////////////////
    ///         Operator                                      ////

    /**
     * Set operaror
     * @param operator operator
     */
    public void setOperator(Operator operator) {
        this._operator = operator;
    }

    /**
     * Get operator
     * @return operator
     */
    public Operator getOperator() {
        return _operator;
    }

    /**
     * Get parent layer
     * @return parent layer
     */
    public Layer getParent() {
        return _parent;
    }

    /**
     * Set parent layer
     * @return parent layer
     */
    public void setParent(Layer parent) {
        _parent = parent;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         protected methods                    ////
     /**
     * Create a deep copy of this neuron
     * As Neuron is an abstract class, its copyConstructor is
     * implemented as a fabric method copyNeuron
     * @param n original neuron to be copied
     */
    protected Neuron (Neuron n) {
        setName(n._name);
        setNeuronType(n.neuronType);
        setSampleDim(n._sampleDim);
        setBiasName(n._biasName);
        setInputDataFormat(n.getInputDataFormat());
        setOutputDataFormat(n.getOutputDataFormat());
        setNonlin(n.getNonlin());
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    /**Name of the Neuron*/
    @SerializedName("name") protected String _name;

    /**
     * One input sample dimension of a neuron:
     * e.g. =1 for input vector, =2 for 2D input image,
     * =3 for RGB input image (2D image + channels dim) etc.
     * */
    private transient int _sampleDim = 1;

    /**
     * TODO is min H a transient height parameter?
     * Manually set input data height, acceptable by the neuron -
     * minDF scalability management
     */
    private transient int _inpH = 1;

    /**
     * Specific neuron parameters, that are not mentioned as Neuron fields
     */
    private transient HashMap<String, String> _parameters = null;

    /**Type of the neuron*/
    @SerializedName("type")private NeuronType neuronType = NeuronType.NONE;

    /**Neuron's input data format*/
    @SerializedName("inputDataFormat")private Tensor _inputDataFormat = null;

    /**Neuron's output data format*/
    @SerializedName("outputDataFormat")private Tensor _outputDataFormat = null;

    /**Neuron's input data format*/
    @SerializedName("bias")private String _biasName = null;

    /**Neuron's input data format*/
    @SerializedName("nonlin")private String _nonlin = null;

    private transient Layer _parent = null;

    protected transient Operator _operator = null;
}
