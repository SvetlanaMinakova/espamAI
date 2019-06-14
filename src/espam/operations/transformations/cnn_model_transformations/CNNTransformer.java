package espam.operations.transformations.cnn_model_transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.connections.Custom;
import espam.datamodel.graph.cnn.connections.OneToOne;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.interfaces.python.Espam2DARTS;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.operations.transformations.CNN2CSDFGraphConverter;

import java.util.HashMap;
import java.util.Vector;

/**TODO finish implementation
 * Class implements transformations over CNN model
 */
public class CNNTransformer {

    /**
     * empty constructor is not allowed for the transformer
     */
    private CNNTransformer(){}

    /**
     * Create new CNNTransformer
     * @param network network with elements to be transformed
     * TODO use network copy?
     */
    public CNNTransformer(Network network){
        _network = network;
        _chains = new Vector<>();
    }

     ///////////////////////////////////////////////////////////////////
    ////                    split transformation                   ////


     /**
     * Print the 'path' of the dependent layers to be copied as a parallel data flows
     * @param layer layer to be split up
     */
    public void printSplitWithDependentTrack(Layer layer, int childrenNum){
        if(!isSplittable(layer,childrenNum)) {
           // System.out.println("Layer could not be split: "+layer);
            return;
        }
        boolean isLastToSplit = false;

        Vector<Layer> dependentOutputs = new Vector<>();
        Vector<Connection> outputConnections = _network.getLayerOutputConnections(layer);


        for(Connection outp: outputConnections){
            if(outp instanceof OneToOne)
                dependentOutputs.add(outp.getDest());
        }

        if(dependentOutputs.size()==0) {
            isLastToSplit = true;
          //  System.out.println(track);
            return;
        }
        else {
            for(Layer dependent: dependentOutputs){
              //  track.append("-->");
              //  track.append(dependent.getName());
              System.out.println();
              System.out.print(layer.getName());
              System.out.print("-->");
              System.out.print(dependent.getName());
              System.out.println();
              printSplitWithDependentTrack(dependent, childrenNum);
            }
        }
    }

    /**
     * Split dependent chain
     * @param chain dependent chain
     * @param childSrc copied path begin
     * @param chainEnds copied path output(s): zero ore more CONCAT nodes
     * @throws Exception
     */
    public void splitDependentChain(Vector<Layer>chain, Layer childSrc, Vector<Layer> chainEnds)throws Exception{
        if(chain.size()<2)
            return;

        /** link chain to child sub-layer*/
        Layer prevLayer = childSrc;
        Layer layerCopy;

        for(int i=1; i<chain.size();i++){
            Layer layer= chain.elementAt(i);

            Neuron neuronCopy = Neuron.copyNeuron(layer.getNeuron());
            _network.addLayer(layer.getName() + "_s_" + _splitId,neuronCopy,layer.getNeuronsNum(),layer.getPads());
            layerCopy = _network.getLastLayer();
            /** split input connection for dependent layer copy*/
                _network.addConnection(prevLayer,layerCopy);
            prevLayer = layerCopy;
        }

        for(Layer chainEnd: chainEnds)
            _network.addConnection(prevLayer.getName(),chainEnd.getName());

        _splitId++;

    }

    /**
     * Checkout, if layer is free of real dependent chains.
     * If a chain list is empty, or each chain is fake (contains only
     * one layer), there are no real chains and layer is free of real chains
     * @param chains chains descriptions
     * @return true, if there are no chains, dependent on splitting layer and false otherwise
     */
    private boolean _isNotChained(Vector<Vector<Layer>> chains){
        for(Vector<Layer>chain: chains){
            if(chain.size()>1)
                return false;
        }
        return true;
    }

     /**
     * Split up a layer of the neural network into 2 layers
     * @param layer layer to be split up
     */
    public boolean splitChains(Layer layer, int childrenNum) throws Exception{
        if(!isSplittable(layer,childrenNum)) {
          //  System.out.println("Layer could not be split: "+layer);
            return false;
        }

        /** process chains*/
        Vector<Vector<Layer>> chains = new Vector<>();
        _buildDependentTrack(layer,chains);

        /** process single layer with no real chains*/
        if(_isNotChained(chains)){
            split(layer,childrenNum);
            return true;
        }

       /** child layers= original layer with changed neurons number + [n-1] copies*/
        int copiesNum = childrenNum-1;

        /** create original layer's neuron copy */
        Neuron [] neuronCopies = new Neuron[copiesNum];
        for(int i=0 ;i<copiesNum;i++) {
            Neuron neuronCopy = Neuron.copyNeuron(layer.getNeuron());
            neuronCopies[i] = neuronCopy;
        }

        Tensor lNeuronsNum = new Tensor();

        /**TODO refactoring*/
        /** process dense block internal neurons number*/
        if(layer.getNeuron() instanceof DenseBlock){
            _splitDenseBlock(layer,childrenNum,copiesNum,neuronCopies,lNeuronsNum);
        }
        else {
            lNeuronsNum = splitNeuronsNum(layer, childrenNum);
            layer.setNeuronsNum(lNeuronsNum.getDimSize(0));
        }

        Layer [] layerCopies = new Layer[copiesNum];

        /** add copied layers*/
        int neuronsStart = layer.getNeuronsNum();
        for(int i=0 ;i<copiesNum;i++){
            _network.addLayer(layer.getName()+"_split_"+_splitId+"_"+i,neuronCopies[i],lNeuronsNum.getDimSize(i+1),layer.getPads());
            Layer added = _network.getLastLayer();
            added.setstartNeuronId(neuronsStart);
            layerCopies[i] = added;
            neuronsStart += added.getNeuronsNum();
        }

        splitInputConnections(layer,layerCopies);
        _splitId++;
        _processDependentChains(chains,layerCopies);
        _network.updateConnectionDependentParameters();
        return true;
    }

    /**
     * Split dense block
     * Dense block is a complex construction, it is splitted up separately
     */
    private void _splitDenseBlock(Layer layer, int childrenNum, int copiesNum, Neuron[] neuronCopies,Tensor lNeuronsNum){
        int totalNeurons = ((DenseBlock) layer.getNeuron()).getNeuronsNum();
            int nn = totalNeurons/childrenNum;
            ((DenseBlock) layer.getNeuron()).setNeuronsNum(nn);
             for(int i=0 ;i<copiesNum;i++) {
                 ((DenseBlock)neuronCopies[i]).setNeuronsNum(nn);
             }

             /** process rest of the neurons in case of totalNeurons is indivisible on childrenNum*/
             int restNeurons = totalNeurons - nn*childrenNum;
             if(restNeurons>0){
                 ((DenseBlock)neuronCopies[copiesNum-1]).setNeuronsNum(nn + restNeurons);
             }

             for(int i=0;i<childrenNum;i++)
                 lNeuronsNum.addDimension(1);
    }

    /**
     * Process end data flow concatenation for chains
     * (chained processing)
     */
    private void _processDependentChains(Vector<Vector<Layer>> chains, Layer[] layerCopies) throws Exception{
                /** provide chains with output streams concat connections*/
        Vector<Vector<Layer>> endConcats = new Vector<>();
        Layer chainLastLayer;
        Layer concatLayer;
        int concatId =0;
        boolean concatGenerated;
        Vector<Layer> chainEnds;

        for(Vector<Layer>chain:chains) {

            endConcats.add(new Vector<>());
            chainEnds = endConcats.lastElement();

            chainLastLayer = chain.lastElement();
            for (Connection outpCon : _network.getLayerOutputConnections(chainLastLayer)) {

                if (outpCon.getDest().getNeuron() instanceof Concat) {
                    concatLayer = outpCon.getDest();
                    chainEnds.add(outpCon.getDest());
                    concatGenerated = false;
                }

                /** create concat layer with inputs = original layer + child layers*/
                else {
                    Concat concat = new Concat();
                    _network.addLayer(chainLastLayer.getName() + "_output_concat_" + concatId, concat, 1);
                    concatLayer = _network.getLastLayer();
                    chainEnds.add(concatLayer);
                    concatGenerated = true;
                    concatId++;
                }
                if (concatGenerated) {
                    _network.addConnection(chainLastLayer, concatLayer);
                    outpCon.changeSrc(concatLayer);
                }
            }
        }

             /** copy paths and link them to chain concats*/
        int chainId = 0;
        for (Vector<Layer> chain : chains) {
            for(Layer layerCopy: layerCopies) {
                splitDependentChain(chain,layerCopy,endConcats.elementAt(chainId));
            }
            chainId++;
        }
    }

    /**
     * Print the 'path' of the dependent layers to be copied as a parallel data flows
     * @param layer layer to be split up
     */
    public void getDependentTrack(Layer layer, int childrenNum){
        if(!isSplittable(layer,childrenNum)) {
          //  System.out.println("Layer could not be split: "+layer);
        }
        Vector<Vector<Layer>> chains = new Vector<>();
        _buildDependentTrack(layer,chains);
        printChains(chains);
    }


     /**
     * Print the 'path' of the dependent layers to be copied as a parallel data flows
     * @param layer layer to be split up
     */
    private void _buildDependentTrack(Layer layer, Vector<Vector<Layer>> chains){
        Vector<Layer> dependentOutputs = new Vector<>();
        Vector<Connection> outputConnections = _network.getLayerOutputConnections(layer);

        for(Connection outp: outputConnections){
            if(outp.getDest().getNeuron() instanceof ConnectionDependent)
                dependentOutputs.add(outp.getDest());
        }

        if(dependentOutputs.size()==0) {
           // for(Connection outp: outputConnections){
           // if(!(outp instanceof OneToOne))
             //   _extendChain(outp.getSrc(),outp.getDest(),chains);
      //  }

        }
        else {
            for(Layer dependent: dependentOutputs){
              _extendChain(layer,dependent,chains);
              _buildDependentTrack(dependent,chains);
            }
        }
    }

    /**
     * Extend split-with-dependent chains
     * @param src source layer
     * @param dst destination layer
     * @param chains gloabl list of chains
     */
    private void _extendChain(Layer src, Layer dst, Vector<Vector<Layer>> chains){
        for(Vector<Layer> chain:chains){
            if(chain.lastElement().getId()==src.getId()){
            chain.add(dst);
                return;
            }
        }
        chains.add(new Vector<>());
        chains.lastElement().add(src);
        chains.lastElement().add(dst);
    }

    /**
     * Print chains of layers
     * @param chains chains of layers
     */
    public void printChains(Vector<Vector<Layer>>chains){
        int chainNum =0;
        for(Vector<Layer>chain:chains){
            System.out.println("chain: " +chainNum );
            chainNum++;
            for(Layer layer:chain){
                System.out.print(layer.getName() + "-->" );
            }
            System.out.println();
        }
    }


    /**
     * Split up a layer of the neural network into several blocks
     * @param layer layer to be split up
     * @param childrenNum number of child layers
     * @return true, if layer was successfully splitted up and false otherwise
     * @throws Exception
     */
    public boolean split(Layer layer, int childrenNum) throws Exception{
        if(!isSplittable(layer,childrenNum)) {
          //  System.err.println("Layer could not be split: "+layer);
            return false;
        }

        /** child layers= original layer with changed neurons number + [n-1] copies*/
        int copiesNum = childrenNum-1;

        /** create original layer's neuron copy */
        Neuron [] neuronCopies = new Neuron[copiesNum];
        for(int i=0 ;i<copiesNum;i++) {
            Neuron neuronCopy = Neuron.copyNeuron(layer.getNeuron());
            neuronCopies[i] = neuronCopy;
        }

        Tensor lNeuronsNum = new Tensor();

        /**TODO refactoring*/
        /** process dense block internal neurons number*/
        if(layer.getNeuron() instanceof DenseBlock){
            int totalNeurons = ((DenseBlock) layer.getNeuron()).getNeuronsNum();
            int nn = totalNeurons/childrenNum;
            ((DenseBlock) layer.getNeuron()).setNeuronsNum(nn);
             for(int i=0 ;i<copiesNum;i++) {
                 ((DenseBlock)neuronCopies[i]).setNeuronsNum(nn);
             }

             /** process rest of the neurons in case of totalNeurons is indivisible on childrenNum*/
             int restNeurons = totalNeurons - nn*childrenNum;
             if(restNeurons>0){
                 ((DenseBlock)neuronCopies[copiesNum-1]).setNeuronsNum(nn + restNeurons);
             }

             for(int i=0;i<childrenNum;i++)
                 lNeuronsNum.addDimension(1);
        }
        else {
            lNeuronsNum = splitNeuronsNum(layer, childrenNum);
            layer.setNeuronsNum(lNeuronsNum.getDimSize(0));
        }

        Layer [] layerCopies = new Layer[copiesNum];


        /** add copied layers*/
        for(int i=0 ;i<copiesNum;i++){
            _network.addLayer(layer.getName()+"_split_"+_splitId+"_"+i,neuronCopies[i],lNeuronsNum.getDimSize(i+1),layer.getPads());
            layerCopies[i] = _network.getLastLayer();
        }

        splitInputConnections(layer,layerCopies);
        splitOutputConnections(layer,layerCopies);
        _network.updateConnectionDependentParameters();

        _splitId++;
        return true;
    }

    /**
     * Split input connections of the neural network.
     * During splitting, input connections of original layer
     * are copied for each of child layers
     * @param original original layer
     * @param childLayers array of child layers
     */
    private void splitInputConnections(Layer original, Layer [] childLayers){
        for (Connection inpCon : _network.getLayerInputConnections(original)) {
            if(inpCon instanceof Custom){
                splitInputCustomConnection((Custom)inpCon,childLayers);
            }
            else {
                Layer inputSrc = inpCon.getSrc();
                for (Layer child : childLayers) {
                    _network.addConnection(inputSrc, child);
                }
            }
        }
    }

     /**
     * Split input connections of the neural network.
     * During splitting, output connections of original layer
     * are copied for each of child layers and then linked to a concat layer
     * to provide single input for connection destinations
     * @param original original layer
     * @param childLayers array of child layers
     */
    private void splitOutputConnections(Layer original, Layer [] childLayers){
        if(childLayers.length==0)
            return;

        Vector<Connection> outpCons = _network.getLayerOutputConnections(original);
        if(outpCons.size()==0)
            return;

        /** concat layer merges streams from splitted layer*/
        Layer concatLayer = null;
        boolean concatAdded = false;

        /** insert concat layer between original layer and it's output layers*/
        int concatId=0;
        for(Connection outpCon: outpCons) {
        /** TODO Refactoring*/
        /** if layer have single output connection, leading to concat, do not create concat*/
        if(outpCon.getDest().getNeuron() instanceof Concat) {
            concatLayer = outpCons.lastElement().getDest();
            concatAdded = false;
        }

        /** create concat layer with inputs = original layer + child layers*/
        else {
            Concat concat = new Concat();
            _network.addLayer(original.getName()+"_output_concat_"+concatId,concat,1);
            concatLayer = _network.getLastLayer();
            concatAdded = true;
            concatId++;
        }
            if(concatAdded) {
                _network.addConnection(original, concatLayer);
                outpCon.changeSrc(concatLayer);
            }
            /** create output connections for each of child layers and link them to concat layer*/
            for(Layer child: childLayers){
                _network.addConnection(child,concatLayer);
                _network.getLastConnection().setAutoChannelsNum();
            }
        }
    }

    /**
     * Generated topology optimization: remove redundant concats
     */
    private void removeRedundantConcats(){
        Vector<Layer> concatLayers = new Vector<>();
        for(Layer layer:_network.getLayers()){
            if(layer.getNeuron()instanceof Concat)
                concatLayers.add(layer);
        }


    }

    /**
     * Remove concatenation layer from DNN, if
     * - concat layer have single input
     * - concat have single output, linked to another concat
     * @param concatLayer concatenation layer
     */
    private void _removeConcatIfRedundand(Layer concatLayer){
        Vector<Connection> concatInputs = _network.getLayerInputConnections(concatLayer);
        Vector<Connection> concatOutputs = _network.getLayerInputConnections(concatLayer);

        /**remove concat layer with a single input*/
          if(concatInputs.size()==1){
            /**transfer output connections to single input*/
            Layer inpSrc = concatInputs.lastElement().getSrc();
            for(Connection outp: concatOutputs)
                outp.changeSrc(inpSrc);
            _network.removeLayer(concatLayer);
        }

        /**remove concat layer with single output, linked to another concat*/
        if(concatOutputs.size()==1){
            Layer singleOutput = concatOutputs.lastElement().getDest();
            if(singleOutput.getNeuron() instanceof Concat){
                for (Connection inpC:concatInputs){
                    inpC.changeDest(singleOutput);
                }
                _network.removeLayer(concatLayer);
            }
        }
    }

    /**
     * Get number of channels for splitted output connection
     * @param outpCon splitted output connection
     * @return
     */
    private int getSplittedOutputChanneslNum(Connection outpCon){
        return 1;
    }

    /**
     * Spread neurons num on
     * @param original original layer
     * @param newNeuronsNum number of neurons in a new layer
     * @return number of neurons in a new layer
     */
    private Tensor splitNeuronsNum(Layer original, int ... newNeuronsNum){
        Tensor result = new Tensor();
        int nnSum = 0;
        for(int nnPart: newNeuronsNum) {
            result.addDimension(nnPart);
            nnSum+=nnPart;
        }

        if(nnSum!=original.getNeuronsNum()){
            return new Tensor(original.getNeuronsNum());
        }

        return result;
    }

    /**
     * Divide original layer's neurons number by 2
     * @param original original layer
     * @return index pair with neurons number for 2 child layers,
     * obtained from original layer by splitting
     */
    private Tensor splitNeuronsNum(Layer original, int childrenCount){
        int original_nn = original.getNeuronsNum();
        if(original_nn<childrenCount)
            return new Tensor(original_nn);

        Tensor result = new Tensor();
        /** spread neurons num as uniform as possible*/
        int common_nn = original_nn/childrenCount;
        for (int i=0; i<childrenCount;i++)
            result.addDimension(common_nn);

        /** add the rest of neurons to the first layer*/
        int dif = original_nn - result.getDimSize(0)*childrenCount;
        result.setDimSize(0,result.getDimSize(0)+dif);

        return result;
    }

    /**
     * Checks, if the layer could be split up
     * @param layer CNN layer
     * @return if the layer could be split up and false otherwise
     */
    public boolean isSplittable(Layer layer, int childrenNum){
     /** multiple input processors are not splittable*/
        if(layer.getNeuron() instanceof MultipleInputsProcessor)
        return false;

        if(layer.getNeuron() instanceof DenseBlock && ((DenseBlock) layer.getNeuron()).getNeuronsNum()>=childrenNum) {
         if(layer.getNeuron().getName().toLowerCase().contains("softmax"))
             return false;
         return true;
        }

        if(layer.getNeuronsNum()>=childrenNum)
            return true;

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    merge transformation                   ////
    /**
     * Merge two layer of the neural network
     * During this process the first layer, called "consumer"
     * extends its parameters by parameters of second layer, called "extender"
     * @param consumer layer to be extended by "consuming" another layer parameters
     * @param extender layer to be "consumed"
     * DNN after the layers merging
     */
    public void Merge(Layer consumer, Layer extender) throws Exception{
        if(!isMergeable(consumer,extender)) {
            System.err.println("Layers could not be merged. layer1:  "+consumer+" , layer2: "+extender);
            return;
        }
        /**
         * TODO update connection dependent params?
         * */
        consumeNeuronsNum(consumer,extender);

        for(Connection inpCon: _network.getLayerInputConnections(consumer)) {
            /** connections merge: remove "doubled" input connection of extender */
            Connection l2Input = _network.findConnection(inpCon.getSrcId(),extender.getId());
            _network.removeConnection(l2Input);
         }

        /** replace layer1, layer2 output connections with the merged layer connections*/
        for(Connection outpCon: _network.getLayerOutputConnections(consumer)) {
            /** extend output channels num*/
            outpCon.setChannels(consumer.getNeuronsNum());
            /** remove "doubled" input connection of extender */
            Connection l2Output = _network.findConnection(extender.getId(),outpCon.getDestId());
            _network.removeConnection(l2Output);
        }
        /** remove extender from the layers list*/
        _network.removeLayer(extender);
        _network.updateConnectionDependentParameters();

        _mergeId++;
    }


    /**
     * Imitates "consuming" of neurons in terms of neurons number.
     * For connection independent layers, the result of consuming adds the
     * extender neurons number to the consumer neurons number. For connection
     * depended layer, number of neurons stay untouched
     * @param consumer layer to be extended by "consuming" another layer neurons
     * @param extender layer to be "consumed"
     */
    private void consumeNeuronsNum(Layer consumer, Layer extender){
        if(!(consumer.getNeuron() instanceof ConnectionDependent)){
            consumer.setNeuronsNum(consumer.getNeuronsNum()+extender.getNeuronsNum());
        }
    }

    /**
     * Checks if two layers could be merged. Layers could be merged if each of the following rules if true:
     * 1. Neuron model of l1 = neuron model of l2
     * 2. Input connections sources list of l1 = input connections sources list of l2
     * 3. Output  layer (for hidden layers)/value(for output layers)
     *    of l1 = output layer/value of l2
     * @param l1 layer 1
     * @param l2 layer 2
     * @return true, if layers could be merged and false otherwise
     */
    public boolean isMergeable(Layer l1, Layer l2){
        if(l1==null || l2==null)
            return false;

        return l1.getNeuron().equals(l2.getNeuron()) &&
                _network.isInputLayersEqual(l1,l2) &&
                _network.isOutputLayersEqual(l1,l2);
    }



    /**
     * Checks, if the part of the network could be grouped to block
     * Part of the network could be grouped into the block, if
     * 1. Start node have 0 or 1 inputs
     * 2. End node have 0 or 1 outputs
     * 3. There is a connection between start and end node
     * @param start start node
     * @param end end node
     * @return true, if part of the network could be grouped and false otherwise
     */
    public boolean isGroupable(Layer start, Layer end){
        Vector<Connection>startInputConnections = _network.getLayerInputConnections(start);
        if(startInputConnections.size()>1)
            return false;

        Vector<Connection>endOutputConnections = _network.getLayerOutputConnections(end);
        if(endOutputConnections.size()>1)
            return false;

        Connection path = _network.findConnection(start.getId(),end.getId());
        if(path==null)
            return false;

        return true;
    }

        /**
     * Checks, if the part of the network could be grouped to block
     * Part of the network could be grouped into the block, if
     * 1. Start node have 0 or 1 inputs
     * 2. End node have 0 or 1 outputs
     * TODO: check 3. There is a path between start node and end node
     * TODO is nested grouping  allowed?
     * @param layers layers to be grouped
     * @return true, if part of the network could be grouped and false otherwise
     */
    public boolean isGroupable(Vector<Layer> layers){
        if(layers.size()<2)
            return false;

        Vector<Connection>startInputConnections = _network.getLayerInputConnections(layers.firstElement());
        if(startInputConnections.size()>1)
            return false;

        Vector<Connection>endOutputConnections = _network.getLayerOutputConnections(layers.lastElement());
        if(endOutputConnections.size()>1)
            return false;

        return true;
    }

       /**
     * Checks, if the part of the network could be ungrouped from a block
     * Only a grouped('packed' in a GenericNeuron) part of the network can be ungrouped
     * @return true, if part of the network could be ungrouped and false otherwise
     */
    public boolean isUngroupable(Layer layer){
        if(layer.getNeuron() instanceof GenericNeuron)
            return true;

        return false;
    }

    /**
     * Split input custom connection
     * @param cc custom connection to be splitted up
     * @param childOutputs outputs copies, appeared after the layer 'split' operation
     */
    public void splitInputCustomConnection(Custom cc, Layer[] childOutputs){
        boolean[][] originalMatrix = cc.getMatrix();
        //customConnection.length == srcNeuronsNum && customConnection[0].length==destNeuronsNum
        /** j-axis (related to the input) remains the same*/
        int srclen = originalMatrix.length;
        int dstlen = cc.getDest().getNeuronsNum();
        int shift = dstlen;

        /** assign custom matrix 'pieces' to child layers*/
        for(Layer child:childOutputs){
            dstlen = child.getNeuronsNum();
            boolean[][]childCustom = new boolean[srclen][dstlen];
            /** shift along the i-axis(related to dst layer)*/
            for(int j=0;j<srclen;j++){
                for(int i=0;i<dstlen;i++) {
                    boolean val = originalMatrix[j][i+shift];
                    childCustom[j][i] = val;
                }
                }
            _network.addConnection(cc.getSrc(),child,childCustom);
            shift+=dstlen;
        }

        /** 'crop' the original matrix*/
        boolean[][] newCustom = new boolean[srclen][dstlen];
        /** shift along the i-axis(related to dst layer)*/

        for(int j=0;j<srclen;j++){
            for(int i=0;i<dstlen;i++)
                newCustom[j][i] = originalMatrix[j][i];
        }

        cc.setMatrix(newCustom);
        cc.setAutoChannelsNum();
    }

    /**
     * Test function for grouping 2 layers
     * @param layer1 first layer to be grouped
     * @param layer2 second layer to be grouped

    public void groupLayers(Layer layer1, Layer layer2) throws Exception{
        if(!isGroupable(layer1,layer2)) {
            System.err.println("Ungroupable layers: " + layer1.getName() + " " + layer2.getName());
            return;
        }

        Network subNetwork = new Network(layer1.getName()+layer2.getName());

        subNetwork.addLayer(layer1.getName(),Neuron.copyNeuron(layer1.getNeuron()),layer1.getNeuronsNum(),layer1.getPads());
        subNetwork.stackLayer(layer2.getName(),Neuron.copyNeuron(layer2.getNeuron()),layer2.getNeuronsNum(),layer2.getPads());

        subNetwork.setInputLayer(subNetwork.getLayer(layer1.getName()));
        subNetwork.setOutputLayer(subNetwork.getLayer(layer2.getName()));

        GenericNeuron generic = new GenericNeuron(subNetwork.getName(),subNetwork);
        /** add generic layer to dnn
        _network.addLayer(subNetwork.getName(),generic,1);
        Layer genericLayer = _network.getLayer(subNetwork.getName());

        /**transfer connections
        Vector<Connection> inputConnections = _network.getLayerInputConnections(layer1);
        for(Connection inpCon: inputConnections){
            _network.addConnection(inpCon.getSrc().getName(),genericLayer.getName());
        }

        Vector<Connection> outputConnections = _network.getLayerOutputConnections(layer2);
        for(Connection outpCon: outputConnections) {
            _network.addConnection(genericLayer.getName(),outpCon.getDest().getName());
        }

        /** remove original layers from neural network
        _network.removeLayer(layer1);
        _network.removeLayer(layer2);
    }
    */

     /**
     * Test function for grouping 2 layers
     * @param chain linear-connected chain
     */
    public void groupLayers(Layer... chain) throws Exception{
        if(chain==null) {
            System.err.println("Ungroupable layers: empty chain");
            return;
        }
        Vector<Layer> chainVector = new Vector<>();

        for(Layer layer: chain)
            chainVector.add(layer);

        groupLayers(chainVector);

    }

    /**
     * Test function for grouping 2 layers
     * @param chain linear-connected chain
     * TODO Refactoring
     */
    public void groupLayers(Vector<Layer> chain) throws Exception {

        if (!isGroupable(chain)) {
            System.err.println("Ungroupable layers chain");
            return;
        }

        if (chain.size() < 2){
            System.err.println("Ungroupable layers chain: chain len < 2");
            return;
        }

        Layer start = chain.firstElement();
        String startName = start.getName();
        if(start.getNeuron() instanceof GenericNeuron)
            startName = ((GenericNeuron) start.getNeuron()).getInternalStructure().getInputLayer().getName();

        Layer end = chain.lastElement();
        String endName = end.getName();
        if(end.getNeuron() instanceof GenericNeuron){
            endName = ((GenericNeuron) end.getNeuron()).getInternalStructure().getOutputLayer().getName();
        }

        Network subNetwork = new Network(start.getName() + "_to_" + end.getName() + "_chain");
        for(Layer layer: chain) {
            if(layer.getNeuron() instanceof GenericNeuron){
                Network nestedSubNetwork = ((GenericNeuron) layer.getNeuron()).getInternalStructure();
                _mergeSubnetworks(subNetwork,nestedSubNetwork);
            }
            else
                subNetwork.stackLayer(layer.getName(), Neuron.copyNeuron(layer.getNeuron()), layer.getNeuronsNum(), layer.getPads());
        }

        subNetwork.setInputLayer(subNetwork.getLayer(startName));
        subNetwork.setOutputLayer(subNetwork.getLayer(endName));

        GenericNeuron generic = new GenericNeuron(subNetwork.getName(),subNetwork);
        /** add generic layer to dnn*/
        _network.addLayer(subNetwork.getName(),generic,1);
        Layer genericLayer = _network.getLayer(subNetwork.getName());

        /**transfer connections*/
        Vector<Connection> inputConnections = _network.getLayerInputConnections(start);
        for(Connection inpCon: inputConnections){
            _network.addConnection(inpCon.getSrc().getName(),genericLayer.getName());
        }

        Vector<Connection> outputConnections = _network.getLayerOutputConnections(end);
        for(Connection outpCon: outputConnections) {
            _network.addConnection(genericLayer.getName(),outpCon.getDest().getName());
        }

        /** remove original layers from neural network*/
        for(Layer layer: chain) {
            _network.removeLayer(layer);
        }
    }

    /**TODO finish implementation
     * Merge two subnetworks - for generic operator merge
     * 1. Merge input layer of nested network to main network
     * 2. Add (not stack!) all the layers from nested network
     * 3. Transfer connections of nested network to main network
     * @param main main subnetwork
     * @param nested subnetwork to be 'consumed' by main subnetwork
     */
    private void _mergeSubnetworks(Network main,Network nested) throws Exception{
        /**
         * HashMap of <layer id in nested dnn(old), layer id in main dnn(new)>
         */
        HashMap<Integer,Integer> dnnLayerIds = new HashMap<>();

        /** add layers without connections*/
        for(Layer layer:nested.getLayers()){
            dnnLayerIds.put(layer.getId(),main.getNextLayerId());
            main.addLayer(layer.getName(), Neuron.copyNeuron(layer.getNeuron()), layer.getNeuronsNum(), layer.getPads());
        }

        /**transfer connections*/
        for(Connection connection: nested.getConnections()){
            int newSrcId = dnnLayerIds.get(connection.getSrcId());
            int newDstId = dnnLayerIds.get(connection.getDestId());
            Layer src = main.getLayer(newSrcId);
            Layer dst = main.getLayer(newDstId);
            if(src==null || dst==null)
                throw new Exception("merge subnetworks (connection transfer) error: " +
                        connection.getSrcName() + " to " + connection.getDestName());
            main.addConnection(src,dst);
        }

        /** if main network already had layers,
         *  link dependent subnetwork input to main prev output*/
         Layer prevOutput = main.getOutputLayer();
         if(prevOutput!=null) {
             int nestedInputId = dnnLayerIds.get(nested.getInputLayerId());
             Layer nestedInput = main.getLayer(nestedInputId);
             main.addConnection(prevOutput, nestedInput);
         }

        /** re-set output layer*/
        int newOutputId = dnnLayerIds.get(nested.getOutputLayerId());
        Layer output = main.getLayer(newOutputId);
        main.setOutputLayer(output);
    }

     ///////////////////////////////////////////////////////////////////
    ////             iterative splitting                           ////


    /**
     *
     * @param maxBlocks maximum number of blocks
     * @param childrenNum number of child nodes of splitted up node
     * @param printDetails if the information about the splitting procedure
     * should be printed to console
     * @param maxIters safe-counter on splitting
     */
    public void splitToBlocks(int maxBlocks, int maxIters, int childrenNum, boolean printDetails){
      int curIter=0;
      while (curIter<maxIters) {

          Tensor networkInputDataFormat = _network.getInputLayer().getOutputFormat();

          try {
              Layer bottleneck = _findBottleneck();
              int splittedBlocksNumber = _evalBlocksNumber(bottleneck, childrenNum);

              /** layers number + final blocks number - splitted block*/
              int totalBlocksNumber = _network.countLayers() + splittedBlocksNumber - 1;
              if (totalBlocksNumber > maxBlocks) {
                  if (printDetails)
                      System.out.println("Splitting is stopped on iteration: " + curIter +
                              " , blocks number =" + _network.countLayers() + " is reached. Next blocks: " + totalBlocksNumber);
                  return;
              }

              boolean continueSplitting = _makeSplitIteration(curIter, childrenNum, networkInputDataFormat, bottleneck, printDetails);
              if (!continueSplitting)
                  return;
              curIter++;

          } catch (Exception e) {
              System.err.println("iterative splitting error, iteration: " +
                      curIter + " , " + e.getMessage());
              return;
          }
      }
    }

    /**
     * Split with dependent chains, until max blocks number will
     * not be found, or until all blocks are recognized as unsplittable
     * @param maxIters maximum number of split iterations
     */
    public void iterativeSplit(int maxIters, boolean printDetails){
        iterativeSplit(maxIters,2,printDetails);
    }

    /**
     * Split with dependent chains, until max blocks number will
     * not be found, or until all blocks are recognized as unsplittable
     * @param maxIters maximum number of split iterations
     * @param childrenNum number of nodes to be obtained from the splitted node
     * @param printDetails if the information about the splitting procedure should be printed to console
     */
    public void iterativeSplit(int maxIters, int childrenNum, boolean printDetails){
        Tensor networkInputDataFormat = _network.getInputLayer().getOutputFormat();
        for(int curIter=0; curIter<maxIters;curIter++) {
            try {
                Layer bottleneck = _findBottleneck();
                boolean continueSplitting = _makeSplitIteration(curIter, childrenNum, networkInputDataFormat,bottleneck, printDetails);
                if (!continueSplitting)
                    return;
            }
            catch (Exception e){
                 System.err.println("iterative splitting error, iteration: " +
                        curIter + " , " + e.getMessage());
                return;
            }
        }
        if(printDetails){
            System.out.println("Iterative splitting is finished");
        }
    }

    /**
     * Make one split iteration
     * @param childrenNum number of child dlocks, should be obtained from
     * the bottleneck block
     * @param iterId iteration unique Id
     * @param networkInputDataFormat input data format of the network (to re-setup data formats)
     * @param printDetails
     * @return if the information about the splitting procedure should be printed to console
     */
    private boolean _makeSplitIteration(int iterId,int childrenNum, Tensor networkInputDataFormat,Layer bottleneck, boolean printDetails){
            try {
                if (printDetails) {
                    System.out.println("Iteration: " + iterId + ", bottleneck: " + bottleneck.getName()+", blocks: "+_network.countLayers());
                }

                boolean splitted = splitChains(bottleneck, childrenNum);

               if(!splitted){
                   if(printDetails)
                       System.out.println("Bottleneck: " + bottleneck.getName() +
                               " can not be split up. Splitting is finished on iteration " +
                               iterId + ", blocks number: "+_network.countLayers());
                   return false;
                }

                _network.setDataFormats(networkInputDataFormat);

            } catch (Exception e) {
                System.err.println("iterative splitting error, iteration: " +
                        iterId + " , " + e.getMessage());
                return false;
            }
        return true;
    }

     /**
     * Calculate how many blocks will be obtained from the
     * layer, splitted with dependent chains. This number of calculated as (number of)
      * splitted blocks + concat blocks, where
      *    splitted blocks = main layer blocks + dependent chains blocks
      *    concat blocks: special blocks, concatenating splitted blocks outputs
     * @param layer layer to be splitted up
     * @return number of blocks will be get after
     */
    public int _evalBlocksNumber(Layer layer, int childNumber){
        int layerBlocks = 1;
        int concats = 1;
        Vector<Vector<Layer>> chains = new Vector<>();
        _buildDependentTrack(layer,chains);

        for(Vector<Layer>chain:chains){
            layerBlocks+=chain.size();
            if(!(chain.lastElement().getNeuron() instanceof Concat))
                concats++;
        }
        return layerBlocks*childNumber + concats;
    }

    /**
     * Find bottleneck layer
     * @return the bottleneck layer
     * @throws Exception if an error occurs
     */
   private Layer _findBottleneck() throws Exception{
        CSDFGraph csdf = _converter.buildGraphLayerBased(_network);

        //CSDFTimingRefiner.getInstance().visitComponent(csdf);

        String bottleneckActor = _edInterface.getBottleneckActor(csdf);
        Layer bottleneck = _network.getLayer(bottleneckActor);
        if(bottleneck==null)
            throw new Exception("bottleneck layer search error!");
        return bottleneck;
    }

     /**
     * TODO: return back to CSDF-based bottleneck checkout after the
     * TODO paper experiments are performed
     * Find bottleneck layer
     * @return the bottleneck layer
     * @throws Exception if an error occurs

    private Layer _findBottleneck() throws Exception{
        int maxNeurons = 0;
        Layer bottleneck = _network.getInputLayer();
        for (Layer layer:_network.getLayers()){
            //we consider Conv layers always to be bottlenecks
            if (layer.getNeuron().getNeuronType().equals(NeuronType.CONV)){
                if(layer.getNeuronsNum()>maxNeurons) {
                    maxNeurons = layer.getNeuronsNum();
                    bottleneck = layer;
                }

            }

        }

        return bottleneck;
    }*/

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                  ////

    /** Network */
    private Network _network;

    /** DNN-2-CSDF converter*/
    CNN2CSDFGraphConverter _converter = new CNN2CSDFGraphConverter();

    /** espam - to DARTS interface*/
    private Espam2DARTS _edInterface = new Espam2DARTS();

    /** split transformations identifier*/
    private int _splitId = 0;

    /** merge transformations identifier*/
    private int _mergeId = 0;

    Vector<Layer> _chains;
    /** temp list of unsplittable blocks, required for
     *  iterative splitting
     */
    Vector<Layer>_unsplittable;
}
