package espam.datamodel.graph.cnn.neurons.neurontypes;

/**
 * All types of neurons used in modeling
 */
public enum NeuronType {
    NONE, CONV, POOL, NONLINEAR, DATA, GENERIC, ADD, CONCAT, DENSEBLOCK, RESHAPE, LRN, UPSAMPLE;

    /**
     * Reconstructs NeuronType from typedDescription, if possible
     * @param typeDescription string description of neuron's type
     * @return corresponding NeuronType or NeuronType.NONE
     */
    public static NeuronType fromString(String typeDescription){
        try{
            return valueOf(typeDescription);
        }
        catch(Exception e){
            return NONE;
        }
    }

    /**
     * Checks if neurons parameters dependent on connections in the network
     * @param neuronType neuronType to be inspected
     * @return true, if neurons parameters dependent on connections in the network
     * and false otherwise
     */
    public static boolean isConnectionDependent (NeuronType neuronType) {
        switch (neuronType) {
            case POOL:
                return true;
            case NONLINEAR:
                return true;
            case CONCAT:
                return true;
            case RESHAPE:
                return true;
            case ADD:
                return true;
            case LRN:
                return true;
                default:return false;
        }
    }
}