package espam.parser.onnx;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.cnn.neurons.simple.Dropout;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;

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
     * Optimize a DNN for inference
     * @param optLevel optimization level, that determines optimizations
     * @param dnn deep neural network
     * over a DNN :
     * 0 (default) do nothing
     * 1 (low) remove dropout and reshape nodes
     * 2 (medium) remove dropout and reshape nodes and incapsulate biases
     */
    public void optimize(Network dnn, Integer optLevel){
        switch (optLevel){
            case 3:{
                _removeDropouts(dnn);
                _removeReshapes(dnn);
                _incapsulateBiases(dnn);
                _incapsulatePads(dnn);
                _incapsulateNonlinear(dnn,NonLinearType.ReLU);
                _incapsulateNonlinear(dnn,NonLinearType.LeakyReLu);
                return;
            }

            case 2:{
                _removeDropouts(dnn);
                _removeReshapes(dnn);
                _incapsulateBiases(dnn);
                _incapsulatePads(dnn);
                return;
            }

              case 1: {
                _removeDropouts(dnn);
                _removeReshapes(dnn);
                _incapsulatePads(dnn);
               return;
            }

            case 0: {
                return;
            }

            default: {
                _removeDropouts(dnn);
                _removeReshapes(dnn);
                _incapsulateBiases(dnn);
                _incapsulatePads(dnn);
                break;
            }
        }

}


/**
 * Remove dropout nodes, that are not used during the inference phase
 * @param dnn deep neural network
 */
    private void _removeDropouts(Network dnn){
    _removeLayers(dnn,NonLinearType.DROPOUT.toString());
}

/**
 * Remove reshape nodes, that are not relevant for linear communication channels
 * @param dnn deep neural network
 */
    private void _removeReshapes(Network dnn){
    Vector<Layer> toRemove = new Vector<>();
    Neuron neuron;
    for(Layer layer: dnn.getLayers()){
        neuron = layer.getNeuron();
        if(neuron.getNeuronType().equals(NeuronType.RESHAPE)) {
            Reshape rn = (Reshape)neuron;
            if(!rn.isSlice())
            toRemove.add(layer);
        }
    }

    for(Layer layer: toRemove)
        _removeLayer(dnn, layer);


}

    /**
     * Incapsulate biases, defined as AddConst nodes into
     * predcessing nodes, e.g. Conv(node)--> AddConst(node) will be replaced by Conv_bias(node)
     * @param dnn deep neural network
     */
    private void _incapsulateBiases(Network dnn){
        Vector<Layer> toIncapsulate = new Vector<>();
    for(Layer layer: dnn.getLayers()){
        if(layer.getNeuron().getName().equals(NonLinearType.AddConst.toString()))
            toIncapsulate.add(layer);
    }

    for(Layer layer: toIncapsulate)
        _incapsulateBias(dnn, layer);
    }

        /**
     * Incapsulate biases, defined as AddConst nodes into
     * predcessing nodes, e.g. Conv(node)--> AddConst(node) will be replaced by Conv_bias(node)
     * @param dnn deep neural network
     */
    private void _incapsulatePads(Network dnn){
        Vector<Layer> toIncapsulate = new Vector<>();
    for(Layer layer: dnn.getLayers()){
        if(layer.getNeuron().getName().equals(NonLinearType.PAD.toString()))
            toIncapsulate.add(layer);
    }

    for(Layer layer: toIncapsulate)
        _incapsulatePad(dnn, layer);
    }

      /**
     * Incapsulate nonlinear layers into
     * predcessing nodes, e.g. Conv(node)--> ReLu(node) will be replaced by Conv_relu(node)
     * @param dnn deep neural network
     */
    private void _incapsulateNonlinear(Network dnn, NonLinearType type){
        Vector<Layer> toIncapsulate = new Vector<>();
    for(Layer layer: dnn.getLayers()){
        if(layer.getNeuron().getName().equals(type.toString()))
            toIncapsulate.add(layer);
    }

    for(Layer layer: toIncapsulate)
        _incapsulateNonlinear(dnn, layer);
    }

     /**
     * Incapsulate biases, defined as AddConst nodes into
     * predcessing nodes, e.g. Conv(node)--> AddConst(node) will be replaced by Conv_bias(node)
     * TODO: to be incapsulated, a layer should have a single input connection.
     * @param dnn dnn with a layer to be incapsulated
     * @param layer layer to be incapsulated
     */
    private void _incapsulateBias(Network dnn, Layer layer){
        Vector<Connection> lInputs = dnn.getLayerInputConnections(layer);
        if(lInputs.size()!=1)
            return;
        Connection singleInput = lInputs.firstElement();
        singleInput.getSrc().getNeuron().setBiasName(layer.getName());
        Vector<Connection> layerOutputs = dnn.getLayerOutputConnections(layer);
        /**connect single input and outputs directly*/
        Layer newSrc = singleInput.getSrc();
        for(Connection outp: layerOutputs){
            outp.changeSrc(newSrc);
        }
        dnn.removeLayer(layer);
}

   /**
     * Incapsulate biases, defined as AddConst nodes into
     * predcessing nodes, e.g. Conv(node)--> AddConst(node) will be replaced by Conv_bias(node)
     * TODO: to be incapsulated, a layer should have a single input connection.
     * @param dnn dnn with a layer to be incapsulated
     * @param layer layer to be incapsulated
     */
    private void _incapsulatePad(Network dnn, Layer layer){
        Vector<Connection> lInputs = dnn.getLayerInputConnections(layer);
        if(lInputs.size()!=1)
            return;
        Connection singleInput = lInputs.firstElement();
        Vector<Connection> layerOutputs = dnn.getLayerOutputConnections(layer);

        /**connect single input and outputs directly*/
        Layer newSrc = singleInput.getSrc();
        for(Connection outp: layerOutputs){
            outp.changeSrc(newSrc);

            /**transfer pads to output layers*/
            outp.getDest().setPads(layer.getPads());
        }

        dnn.removeLayer(layer);
}

     /**
     * Incapsulate biases, defined as AddConst nodes into
     * predcessing nodes, e.g. Conv(node)--> AddConst(node) will be replaced by Conv_bias(node)
     * TODO: to be incapsulated, a layer should have a single input connection.
     * @param dnn dnn with a layer to be incapsulated
     * @param layer layer to be incapsulated
     */
    private void _incapsulateNonlinear(Network dnn, Layer layer){
        Vector<Connection> lInputs = dnn.getLayerInputConnections(layer);
        if(lInputs.size()!=1)
            return;

        Connection singleInput = lInputs.firstElement();
        Neuron srcNeuron = singleInput.getSrc().getNeuron();
        Neuron nonlinNeuron = layer.getNeuron();

        srcNeuron.setNonlin(nonlinNeuron.getName());

        //srcNeuron.setName(srcNeuron.getName() + "_" + nonlinNeuron.getName());

        Vector<Connection> layerOutputs = dnn.getLayerOutputConnections(layer);
        /**connect single input and dropout outputs directly*/
        Layer newSrc = singleInput.getSrc();
        for(Connection outp: layerOutputs){
            outp.changeSrc(newSrc);
        }
        dnn.removeLayer(layer);
}

    /**
     * Remove nodes of specific type
     * @param dnn DNN with nodes to be removed
     * @param neuronType type of neuron for nodes to be removed
     */
  private void _removeLayers(Network dnn, String neuronType){
   Vector<Layer> toRemove = new Vector<>();
    for(Layer layer: dnn.getLayers()){
        if(layer.getNeuron().getName().equals(neuronType))
            toRemove.add(layer);
    }

    for(Layer layer: toRemove)
        _removeLayer(dnn, layer);
}

    /**
     * Remove a layer from the dnn
     * TODO: to be removed, a layer should have a single input connection.
     * @param dnn dnn with a layer to be removed
     * @param layer layer to be removed
     */
    private void _removeLayer(Network dnn, Layer layer){
        Vector<Connection> lInputs = dnn.getLayerInputConnections(layer);
        if(lInputs.size()!=1)
            return;
        Connection singleInput = lInputs.firstElement();
        Vector<Connection> layerOutputs = dnn.getLayerOutputConnections(layer);
        /**connect single input and dropout outputs directly*/
        Layer newSrc = singleInput.getSrc();
        for(Connection outp: layerOutputs){
            outp.changeSrc(newSrc);
        }
        dnn.removeLayer(layer);
}

/** DNN optimizer is a singletone, so constructor is private*/
protected InferenceDNNOptimizer(){ }
    private static InferenceDNNOptimizer _inferenceDNNOptimizer = new InferenceDNNOptimizer();

}
