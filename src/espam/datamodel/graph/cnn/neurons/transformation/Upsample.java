package espam.datamodel.graph.cnn.neurons.transformation;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

public class Upsample extends Neuron implements ConnectionDependent{

    /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     *
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(int channels) {
        return 1;
    }

    /**
     * Create new Add element with a name
     * a stride and a kernelSize for the Add element = 1
     * an input sample dimension is 1 (vector)
     */
    public Upsample() {
        super(NeuronType.UPSAMPLE.toString());
        setNeuronType(NeuronType.UPSAMPLE);
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
     * Compares Add neuron with another object
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

        Upsample ups = (Upsample) obj;
        return _isScalesSame(ups._scales);
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
            setOutputDataFormat(inputDataFormats.elementAt(0));
            setSampleDim(inputDataFormats.elementAt(0).getDimensionality());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    /**
     * Clone this Add Neuron
     *
     * @return a new reference on the Add Neuron
     */
    public Upsample clone() {
        Upsample newObj = (Upsample) super.clone();
        newObj._scales = this._scales;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     *
     * @param u original neuron to be copied
     */
    public Upsample(Upsample u) {
        super(u);
        u._scales = new Vector<>();
        for (Integer scale: _scales)
            u._scales.add(scale);
    }

    /**
     * Return the string description of the neuron specific parameters
     *
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
        StringBuilder strParams = new StringBuilder();
        strParams.append("\n parameters: [\n");
        strParams.append("scales: " + _scales + "]");
        return strParams.toString();
    }

    /**
     * Automatically caclulates the output format of a neuron
     * number of outputs of one Add neuron = number of its inputs
     * ic case of vector, the point is returned
     *
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        if (Tensor.isNullOrEmpty(inputDataFormat))
            return inputDataFormat;

        if (_scales.size()<2) {
            System.err.println("Wrong scales for upsample node: scales has "+_scales.size() +
                    " dimensions, 2,3 or 4 dimensions expected");
            return inputDataFormat;
        }

        Tensor output = new Tensor(inputDataFormat);
        setOutputDataFormat(output);
        setOutputHeight(getOutputH()*_getHScale());
        setOutputWidth(getOutputWidth()*_getWScale());

        return getOutputDataFormat();
    }


    /**
     * Compute min input data height
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight(int minOutputHeight) {
        return 1;
    }

    /**
     * If data height can be minimized
     * @param minOutputDataHeight min output data height
     * @return false
     */
    public boolean setMinDataHeight(int minOutputDataHeight) {
        return true;
    }



    /////////////////////////////////////////////////////////////////////
    ////             private and protected methods                  ////

    /**
     * Compare node scales with some other scales
     * @param scales some other scales
     * @return true, if node scales are the same as other scales and false otherwise
     */
    private boolean _isScalesSame(Vector<Integer> scales){
        if(_scales.size()!=scales.size())
            return false;

        for(int i=0; i<_scales.size(); i++) {
            if(_scales.elementAt(i)!=scales.elementAt(i))
                return false;
        }

        return true;
    }

    /////////////////////////////////////////////////////////////////////
    ////                  getters and setters                       ////

    /**
     * Get data scales
     * @return data scales
     */
    public Vector<Integer> getScales() {
        return _scales;
    }

    /**
     * Set data scales
     * @param _scales data scales
     */
    public void setScales(Vector<Integer> _scales) {
        this._scales = _scales;
    }

    ///////////////////////////////////////////////////////////////////
    ////      SDFG transformation-related  public methods         ////

    /**
     * Calculate number of function calls inside of the neuron
     * Add neuron always fires once for each input pair
     *
     * @return number of function calls inside of the node
     */
    @Override
    public int getFuncCallsNum(int scale) {
        return 1;
    }

    /**
     * Get function call description. If no execution code is
     * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     *
     * @return function call description
     */
    public String getFunctionCallDescription(int channels) {
        StringBuilder desc = new StringBuilder(getName());
        if(_scales.size()==0)
            return desc.toString();

        desc.append("(");
        desc.append(getOutputHeight() * getOutputWidth());
        desc.append("_");
        for(int i=0; i<_scales.size()-1; i++) {
            desc.append(_scales.elementAt(i));
            desc.append("_");
        }
        desc.append(_scales.lastElement());
        desc.append(")");
        return desc.toString();
    }

    /**
     * TODO: check!
     * recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */
    public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{
        Integer ownerNeuronsNum = input.getNeuronsNum();
        if(_scales.size()>2){
            Integer channelScale = _scales.elementAt(_scales.size()-3);
                if(channelScale>0)
                    ownerNeuronsNum*=channelScale;
        }

        if(input != null ){
            neuronOwner.setNeuronsNum(ownerNeuronsNum);
                return;
            }

        System.err.println("Parameters update fail: upsample layer " + neuronOwner.getName()+" should not have multiple inputs");
        throw new Exception("Upsample layer "+neuronOwner.getName()+" parameters update fail:");
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    @Override
    public void initOperator(int inputChannels, int outputChannels) {
    TreeMap<String,Integer> intParams = _operator.getIntParams();
        int scaleId = 0;
        for (Integer scale: _scales) {
            intParams.put("scale_"+scaleId, scale);
            scaleId++;
        }
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        int timeComplexity = 1;
        if(!(getOutputDataFormat()==null)){
            timeComplexity = getOutputH() * getOutputWidth() * outputChannels;
        }

        _operator.setTimeComplexity(timeComplexity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** get height scale*/
    private Integer _getHScale(){
        return _scales.elementAt(_scales.size()-2);
    }

    /** get width scale*/
     private Integer _getWScale(){
        return _scales.elementAt(_scales.size()-1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** dimensions extension [N,C,H,W]*/
    private Vector<Integer> _scales = new Vector<>();
}
