package espam.datamodel.graph.cnn.neurons;

import espam.datamodel.graph.cnn.Layer;

import java.util.Vector;

/** shows, that neuron definition is dependent on its input connections*/
public interface ConnectionDependent {

    /** recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     * TODO for now only single input is allowed for DNN neurons number recalculation
     */
    public void recalculateNeuronsNumber(Layer neuronOwner,Layer input) throws Exception;
}
