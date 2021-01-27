package espam.datamodel.graph.cnn.neurons.simple;

import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;

/**
 * Neuron of unknown type, equivalent of NULL for Neuron objects
 */
public class NoneTypeNeuron extends Neuron {
     /**
     * Create new non-type neuron
     */
    public NoneTypeNeuron () {
        super(NeuronType.NONE.toString());
        setNeuronType(NeuronType.NONE);
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
      return 0;
    }

      /**
     * Create new non-type neuron
     */
    public NoneTypeNeuron (String name) {
        super(name);
        setNeuronType(NeuronType.NONE);
    }

        /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description*/
    public String getFunctionCallDescription(int channels){
        StringBuilder desc = new StringBuilder(getName());
        return desc.toString();
    }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    @Override
    public void initOperator(int inputChannels, int outputChannels) {  }

    /**
     * Init operator: Description of DNN neuron functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    protected void setOperatorTimeComplexity(int inputChannels, int outputChannels){
        Long timeComplexity = 1l;
        _operator.setTimeComplexity(timeComplexity);
    }
}
