package espam.datamodel.graph.cnn.neurons.simple;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.DataContainer;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Class describes input and output nodes of CNN
 * Data neurons do not perform any calculations, they are used only for
 * transfer data in/out of CNN model
 */
public class Data extends Neuron implements DataContainer, ConnectionDependent{

      /**
     * Create new Data element with a name and empty data format
     * @param dataType type of Data node: input or output
     * default parameters:
     * a stride and a kernelSize for the Data element = 1
     * an input sample dimension is 1 (vector)
     */
    public Data (DataType dataType) {
        super();
        setName(dataType.toString());
        setNeuronType(NeuronType.DATA);
        setInputDataFormat(new Tensor());
        setOutputDataFormat(new Tensor());
    }

    /**
     * Create new Data element with certain data Format
     * @param dataType type of Data node: input or output
     * @param dataFormat format of data
     * default parameters:
     * a stride and a kernelSize for the Data element = 1
     * an input sample dimension is 1 (vector)
     */
    public Data (DataType dataType,Tensor dataFormat) {
        super();
        setName(dataType.toString());
        setNeuronType(NeuronType.DATA);
        setInputDataFormat(dataFormat);
        setOutputDataFormat(dataFormat);
    }

     /**
     * Clone this data Neuron
     * @return a new reference on the Convolution Neuron
     */
    public Data clone() {
        Data newObj = (Data) super.clone();
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     * @param d original neuron to be copied
     */
    public Data(Data d){ super(d); }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

           /**
     * Return the string description of the neuron specific parameters
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
     StringBuilder strParams = new StringBuilder();
     strParams.append("\n [\n");
     strParams.append("data format: "+getOutputDataFormat() + "]");
     return strParams.toString();
    }


      /**
      * Compares Data node with another object
      * @param obj Object to compare this Data node with
      * @return true if Data node is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

        Data data = (Data)obj;

        if(!this.getName().equals(data.getName()))
            return false;

        if(getName().equals(DataType.INPUT.toString()))
            return Tensor.isSame(getOutputDataFormat(),data.getOutputDataFormat());

        if(getName().equals(DataType.OUTPUT.toString()))
            return Tensor.isSame(getInputDataFormat(),data.getInputDataFormat());

            return false;
       }

    ///////////////////////////////////////////////////////////////////
    ////              data formats calculation and set up         ////
    /**
     * Automatically caculates the output format of a neuron
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
       return inputDataFormat;
    }

     /**TODO CHECK
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight) {
        if(getSampleDim()>1)
            return getOutputDataFormat().getDimSize(1);
        else return minOutputHeight;
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


    /** recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */
    public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{
        if(input != null ){
            int neuronsNum;

            /** TODO: check!*/
            if(input.getNeuron() instanceof Concat) {
                Concat cn = (Concat)input.getNeuron();
                neuronsNum = _extractNeuronsFromConcat(cn);
                neuronOwner.setNeuronsNum(neuronsNum);
               // System.out.println(" From multiple inp proc: " + input.getName() + " inherited neurons: " + neuronOwner.getNeuronsNum());
            }
            else
                neuronOwner.setNeuronsNum(input.getNeuronsNum());


            return;
            }
            /** neuron owner */
        System.err.println("Parameters update fail: NonLinear layer " + neuronOwner.getName()+" should not have multiple inputs");
        throw new Exception("NonLinear layer "+neuronOwner.getName()+" parameters update fail:");
    }

    private Integer _extractNeuronsFromConcat(Concat cn){
        int neurs = 0;
        int inpNeurs = 0;

            for (Layer inputOwner: cn.getInputOwners()) {
                if(inputOwner.getNeuron() instanceof Concat)
                    inpNeurs = _extractNeuronsFromConcat((Concat)inputOwner.getNeuron());
                else
                    inpNeurs = inputOwner.getNeuronsNum();

                neurs += inpNeurs;
            }
            neurs = Math.max(neurs,1);

            return neurs;
    }

    ///////////////////////////////////////////////////////////////////
    ////      SDFG transformation-related  public methods         ////
    /**
     * Get function call name
     * @return function call name
     */
    @Override
    public String getFunctionCallName() {
       if(getName().equals(DataType.INPUT.toString()))
           return "READ";
       if(getName().equals(DataType.OUTPUT.toString()))
           return "WRITE";
       return getName();
    }

     /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description
     */
    @Override
    public String getFunctionCallDescription(int channels){
        return getFunctionCallName();
    }


        /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value.
     * @return number of operations, could be implemented on the input data
     */
    @Override
    public int getOperationsNumber(int channels){
        return 0;
    }

        /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    @Override
    public void initOperator(int inputChannels, int outputChannels) { }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        long timeComplexity = 1;

        if(_name.equals(DataType.INPUT)) {
            if(!(getOutputDataFormat()==null))
                timeComplexity = getOutputDataFormat().getElementsNumber();
        }
        else if(!(getInputDataFormat()==null)) {
            timeComplexity = getInputDataFormat().getElementsNumber();
        }

        _operator.setTimeComplexity(timeComplexity);
    }

    /**
     * Add to operator parameters inputs from multiple sources
     * @param layerInputFormat layer input data format
     */
    @Override
    public void _addInputToOpPar(Tensor layerInputFormat){
        String dataParamName = "input";

        if (getName().equals(DataType.INPUT.toString()))
           dataParamName = "data";

        TreeMap<String,Tensor> tensorParams = _operator.getTensorParams();
        tensorParams.put(dataParamName, layerInputFormat);
    }

}
