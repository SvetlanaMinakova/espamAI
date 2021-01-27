package espam.datamodel.graph.cnn.neurons.transformation;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.TreeMap;
import java.util.Vector;

public class Transpose extends Neuron implements ConnectionDependent {

    /**Create new Reshape element with a name*/
    public Transpose() {
        super();
        setName(NeuronType.TRANSPOSE.toString());
        setNeuronType(NeuronType.TRANSPOSE);
    }

    /**
     * Clone this Neuron
     *
     * @return a new reference on the Neuron
     */
    public Transpose clone() {
        Transpose newObj = (Transpose) super.clone();
        newObj._perm= this._perm;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     *
     * @param u original neuron to be copied
     */
    public Transpose(Transpose u) {
        super(u);
        u._perm = new Vector<>();
        for (Integer p: _perm)
            u._perm.add(p);
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

        Transpose tr = (Transpose)obj;
        return _isPermSame(tr._perm);
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
     * Get permutation parameter
     * @return permutation parameter
     */
    public Vector<Integer> getPerm() {
        return _perm;
    }

    /**
     * Set permutation parameter
     * @param perm permutation parameter
     */
    public void setPerm(Vector<Integer> perm) {
        this._perm = perm;
    }

    /**TODO: finish implementation
     * Get function call description. If no execution code is
     * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     *
     * @return function call description
     */
    public String getFunctionCallDescription(int channels) {
        StringBuilder desc = new StringBuilder(getName());

        if(_perm.size()==0)
            return desc.toString();

        desc.append("(");
        for(int i=0; i<_perm.size()-1; i++) {
            desc.append(_perm.elementAt(i));
            desc.append("_");
        }
        desc.append(_perm.lastElement());
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
        int permId = 0;
        for (Integer perm: _perm) {
            intParams.put("perm_" + permId, perm);
            permId++;
        }
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        long timeComplexity = 1;
        if(!(getOutputDataFormat()==null)){
            timeComplexity = getOutputH() * getOutputWidth() * outputChannels;
        }

        _operator.setTimeComplexity(timeComplexity);
    }

    /**TODO: finish implementation
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

       // System.out.println("Transpose " + getParent().getName() + " . Input data: " + inputDataFormat.toString() + ", permutation: " + _perm.toString());

        if(_perm==null) _initPermDefault(inputDataFormat);
        Tensor output = new Tensor();
        for(Integer curPerm: _perm){
            try{
                Integer odim = inputDataFormat.getDimSize(curPerm);
                output.addDimension(odim);
            }
            catch (Exception e){
                System.err.println("Transpose " + getParent().getName() + ": input dim ["+ curPerm + "] not found! Dimensions permutation ignored.");
                //return null;
            }
        }


        setOutputDataFormat(output);

        //setOutputHeight(getOutputH()*_getHScale());
        //setOutputWidth(getOutputWidth()*_getWScale());

        return getOutputDataFormat();
    }

    /**
     * Init permitation array with default valuer.
     * By default Transpose operator reverses input
     * Tensor dimensions, e.g. input(W,H,C) -> output(C,H,W)
     * @param inputDataFormat
     */
    private void _initPermDefault(Tensor inputDataFormat){
        if(Tensor.isNullOrEmpty(inputDataFormat))
            return;

        for(int i=inputDataFormat.getDimensionality()-1;i>=0; i--)
                _perm.add(inputDataFormat.getDimSize(inputDataFormat.getDimSize(i)));
    }


   /////////////////////////////////////////////////////////////////////
    ////             private and protected methods                  ////

    /**
     * Compare node scales with some other perm
     * @param perm some other perm
     * @return true, if node perm is the same as other perm and false otherwise
     */
    private boolean _isPermSame(Vector<Integer> perm){
        if(perm==null && _perm==null)
            return true;
        if(perm==null || _perm==null)
            return false;

        if(_perm.size()!=perm.size())
            return false;

        for(int i=0; i<_perm.size(); i++) {
            if(_perm.elementAt(i)!=perm.elementAt(i))
                return false;
        }

        return true;
    }


    /**
     * TODO: check!
     * recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */
    public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{
        Integer ownerNeuronsNum = input.getNeuronsNum();

        /**if(_scales.size()>2){
            Integer channelScale = _scales.elementAt(_scales.size()-3);
                if(channelScale>0)
                    ownerNeuronsNum*=channelScale;
        }*/

        if(input != null ){
            neuronOwner.setNeuronsNum(ownerNeuronsNum);
                return;
            }

        System.err.println("Parameters update fail: transpose layer " + neuronOwner.getName()+" should not have multiple inputs");
        throw new Exception("Transpose layer "+neuronOwner.getName()+" parameters update fail:");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**A list of integers. By default, transpose layer reverse the
     * input dimensions, otherwise it permutes the axes according to the values given.
     * */
    @SerializedName("perm") private Vector<Integer> _perm = new Vector<>();
}
