package espam.parser.onnx;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.cnn.neurons.simple.Dropout;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;

import java.util.Vector;

/**
 * Optimizes DNN for inference phase
 */
public class InferenceDNNOptimizer {

/**
 * Get singletone instance of DNN inference optimizer
 * @return singletone instance of DNN inference optimizer
 */
public static InferenceDNNOptimizer getInstance(){
    return _inferenceDNNOptimizer;
}

/**
 * Remove dropout nodes, that are not used during the inference phase
 * @param dnn deep neural network
 */
    public void removeDropouts(Network dnn){
    Vector<Layer> dropouts = new Vector<>();
    for(Layer layer: dnn.getLayers()){
        if(layer.getNeuron().getName().equals(NonLinearType.DROPOUT.toString()))
            dropouts.add(layer);
    }

    for(Layer dropout: dropouts)
        _removeDropout(dnn, dropout);
}

    /**
     * Remove one dropout layer from the dnn
     * TODO: to be removed, dropout should have a single input connection.
     * @param dnn dnn with a Dropout layer
     * @param dropout dropout layer to be removed
     */
    private void _removeDropout(Network dnn, Layer dropout){
        Vector<Connection> dropoutInputs = dnn.getLayerInputConnections(dropout);
        if(dropoutInputs.size()!=1)
            return;
        Connection singleInput = dropoutInputs.firstElement();
        Vector<Connection> dropoutOutputs = dnn.getLayerOutputConnections(dropout);
        /**connect single input and dropout outputs directly*/
        Layer newSrc = singleInput.getSrc();
        for(Connection outp: dropoutOutputs){
            outp.changeSrc(newSrc);
        }
        dnn.removeLayer(dropout);
}




/** DNN optimizer is a singletone, so constructor is private*/
protected InferenceDNNOptimizer(){ }
    private static InferenceDNNOptimizer _inferenceDNNOptimizer = new InferenceDNNOptimizer();

}
