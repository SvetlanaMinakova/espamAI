package espam.datamodel.graph.cnn.neurons;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.datasctructures.Tensor;

import java.util.HashMap;
import java.util.Vector;

/** interface shows, that Neuron processes multiple inputs*/
public interface MultipleInputsProcessor {
    /**
     * Set data formats from multiple inputs
     * @param neuronOwner layer, owns the neuron
     * @throws Exception if an error occurs
     */
    void setDataFromMultipleInputs(Layer neuronOwner) throws Exception;

    /**
     * Get multiple neuron inputs
     * @return multiple neuron inputs
     */
    Vector<Tensor> getInputs();

    /**
     * Get multiple neuron input owners
     * @return multiple neuron input owners
     */
    Vector<Layer> getInputOwners();

    /**
     * Add new input
     */
    void addInput(Layer inputOwner);

    /**
     * Update neuron parameters after input connection was removed
     */
    void removeInput(Layer neuronOwner,Layer inputOwner);

    /**
     * Consistency checkout: checks, if an input is acceptable for the node
     * @param inputDataFormat input data format
     * @return true, if input node is acceptable and false otherwise
     */
    boolean isAcceptableInput(Tensor inputDataFormat);
}
