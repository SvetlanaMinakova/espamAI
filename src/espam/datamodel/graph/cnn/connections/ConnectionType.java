package espam.datamodel.graph.cnn.connections;
/**
 * Possible types of connections between two layers of Neural Network
 * @author Svetlana Minakova
 */
public enum ConnectionType {
    /**All neurons of previous layer are connected to all neurons of the next layer
     * (connection matrix consists of 1 only)
     */
    ALLTOALL,
    /**Each neuron of previous layer is connected to one corresponding neuron of the next layer
     *(unit connection matrix) previous and next layer should have the same number of neurons
     *to be connected via this type of connection
     */
    ONETOONE,
    /**one (first) neuron of previous layer is connected to all neurons of the next layer*/
    ONETOALL,
    /**all neurons of previous layer are connected to one (first) neuron of the next layer*/
    ALLTOONE,
    /**custom connection with connection matrix described by hand*/
    CUSTOM
}
