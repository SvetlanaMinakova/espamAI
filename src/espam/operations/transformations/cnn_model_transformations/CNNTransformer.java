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
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.interfaces.python.Espam2DARTS;
import espam.operations.evaluation.OperatorsComplexityEvaluator;
import espam.operations.transformations.CNN2CSDFGraphConverter;

import java.util.HashMap;
import java.util.Map;
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
    public void splitByPlan(HashMap<String, Vector<Integer>> splitPlan){
        for(Map.Entry<String, Vector<Integer>> split: splitPlan.entrySet()){
            Layer toSplit = _network.getLayer(split.getKey());
            if(toSplit==null){
                System.err.println("Layer split error: layer " + split.getKey() + " not found in DNN");
            }
            else {
                try {
                    splitLayer(toSplit, split.getValue());
                }
                catch (Exception e){
                    System.err.println("Layer " + split.getKey() + " split error: layer " + e.getMessage());
                }
            }
        }

    }


    /**
     * Split up a layer of the neural network into 2 layers
     * @param layer layer to be split up
     */
    public boolean splitLayer(Layer layer, Vector<Integer> childrenNum) throws Exception{
        Integer totalChildrenNum = 0;
        for(Integer cn: childrenNum) {
            totalChildrenNum+=cn;

        }

        if(!isSplittable(layer,totalChildrenNum)) {
            //  System.out.println("Layer could not be split: "+layer);
            return false;
        }

        /** process chains*/
       // Vector<Vector<Layer>> chains = new Vector<>();
       // _buildDependentTrack(layer,chains);

        /** process single layer with no real chains*/

        //split(layer,childrenNum);

        /** child layers= original layer with changed neurons number + [n-1] copies*/
        int copiesNum = childrenNum.size() -1;

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
            _splitDenseBlock(layer, childrenNum, copiesNum, neuronCopies, lNeuronsNum);
        }
        else {
            lNeuronsNum = splitNeuronsNum(childrenNum);
            layer.setNeuronsNum(lNeuronsNum.getDimSize(0));
        }

        Layer [] layerCopies = new Layer[copiesNum];

        /** add copied layers*/
        int neuronsStart = layer.getstartNeuronId() + layer.getNeuronsNum();
        for(int i=0 ;i<copiesNum;i++){
            _network.addLayer(layer.getName()+"_split_" + _splitId + "_"+i, neuronCopies[i], lNeuronsNum.getDimSize(i+1),layer.getPads());
            Layer added = _network.getLastLayer();
            added.setstartNeuronId(neuronsStart);
            layerCopies[i] = added;
            neuronsStart += added.getNeuronsNum();
        }

        /**TODO refactoring*/
        /** process dense block internal neurons number*/
        if(layer.getNeuron() instanceof DenseBlock) {
            int startNeuronId = layer.getstartNeuronId() + ((DenseBlock) layer.getNeuron()).getNeuronsNum();

            for (int i = 0; i < copiesNum; i++) {
                layerCopies[i].setstartNeuronId(startNeuronId);
                startNeuronId = layerCopies[i].getstartNeuronId() + ((DenseBlock) layerCopies[i].getNeuron()).getNeuronsNum();
            }
        }


        _splitId++;
        splitInputConnections(layer,layerCopies);
        splitOutputConnections(layer,layerCopies);
        _network.updateConnectionDependentParameters();
        _network.updateDataFormats();
        return true;
    }


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
     * @throws Exception if an error occurs
     */
    public void splitDependentChain(Vector<Layer>chain, Layer childSrc, Vector<Layer> chainEnds)throws Exception{
        if(chain.size()<2)
            return;

        /** link chain to child sub-layer*/
        Layer prevLayer = childSrc;
        Layer layerCopy;

        for(int i=1; i<chain.size();i++){
            Layer layer = chain.elementAt(i);

            Neuron neuronCopy = Neuron.copyNeuron(layer.getNeuron());
            _network.addLayer(layer.getName() + "_split_" + _splitId,neuronCopy,layer.getNeuronsNum(),layer.getPads());
            layerCopy = _network.getLastLayer();
            /** inherit start neuron id from chain beginning layer*/
            layerCopy.setstartNeuronId(childSrc.getstartNeuronId());
            //System.out.println(layerCopy.getName()+" start neur: " + layerCopy.getstartNeuronId());
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
        int neuronsStart = layer.getstartNeuronId() + layer.getNeuronsNum();
        for(int i=0 ;i<copiesNum;i++){
            _network.addLayer(layer.getName()+"_split_"+_splitId+"_"+i,neuronCopies[i],lNeuronsNum.getDimSize(i+1),layer.getPads());
            Layer added = _network.getLastLayer();
            added.setstartNeuronId(neuronsStart);
            layerCopies[i] = added;
            neuronsStart += added.getNeuronsNum();
        }

        /**TODO refactoring*/
        /** process dense block internal neurons number*/
        if(layer.getNeuron() instanceof DenseBlock) {
            int startNeuronId = layer.getstartNeuronId() + ((DenseBlock) layer.getNeuron()).getNeuronsNum();

            for (int i = 0; i < copiesNum; i++) {
                layerCopies[i].setstartNeuronId(startNeuronId);
                startNeuronId = layerCopies[i].getstartNeuronId() + ((DenseBlock) layerCopies[i].getNeuron()).getNeuronsNum();
            }
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
        /** spread neurons evenly*/
        int totalNeurons = ((DenseBlock) layer.getNeuron()).getNeuronsNum();
        int nn = totalNeurons/childrenNum;
        Vector<Integer> evenNN = new Vector<>();

        for(int i=0 ;i<copiesNum+1;i++) {
            evenNN.add(nn);
        }

        /** process rest of the neurons in case of totalNeurons is indivisible on childrenNum*/
        int restNeurons = totalNeurons - nn*childrenNum;
             if(restNeurons>0){
                 evenNN.setElementAt((nn + restNeurons), evenNN.size()-1);
             }

        _splitDenseBlock(layer,evenNN,copiesNum,neuronCopies,lNeuronsNum);
    }

    /**
     * Split dense block
     * Dense block is a complex construction, it is splitted up separately
     */
    private void _splitDenseBlock(Layer layer, Vector<Integer> childrenNum, int copiesNum, Neuron[] neuronCopies,Tensor lNeuronsNum){
        int totalNeurons = ((DenseBlock) layer.getNeuron()).getNeuronsNum();
        int nn = childrenNum.elementAt(0);
        ((DenseBlock) layer.getNeuron()).setNeuronsNum(nn);
        for(int i=0 ;i<copiesNum;i++) {
            nn = childrenNum.elementAt(i + 1);
            ((DenseBlock)neuronCopies[i]).setNeuronsNum(nn);
        }

        for(int i=0;i<childrenNum.size();i++)
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
            if(outp.getDest().getNeuron() instanceof ConnectionDependent &&
                    !(outp.getDest().getNeuron() instanceof Data))
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

        /**TODO refactoring*/
        /** process dense block internal neurons number*/
        if(layer.getNeuron() instanceof DenseBlock) {
            int startNeuronId = layer.getstartNeuronId() + ((DenseBlock) layer.getNeuron()).getNeuronsNum();

            for (int i = 0; i < copiesNum; i++) {
                layerCopies[i].setstartNeuronId(startNeuronId);
                startNeuronId = layerCopies[i].getstartNeuronId() + ((DenseBlock) layerCopies[i].getNeuron()).getNeuronsNum();
            }
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
     * Divide original layer's neurons number by n
     * @param original original layer
     * @return tensor with neurons number for n child layers,
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
     * @return neurons number for child layers,
     * obtained from original layer by splitting
     */
    private Tensor splitNeuronsNum(Vector<Integer> childrenNeuronNum){

        Tensor result = new Tensor();
        for (Integer cnn: childrenNeuronNum)
            result.addDimension(cnn);

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

        if(layer.getNeuron() instanceof Data)
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

        /** TODO: check*/
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
     * Function for grouping 2 layers
     * @param chain linear-connected chain
     * @throws Exception if an error occurs
     */
     public void groupLayers(Vector<Layer> chain) throws Exception {
        groupLayers(chain,false);

     }

    /**
     *Function for grouping 2 layers
     * @param chain linear-connected chain
     * TODO Refactoring
     * @param fuse if the grouped layer is fusion
     * @throws Exception if an error occurs
     */
    public void groupLayers(Vector<Layer> chain, boolean fuse) throws Exception {

        if (!isGroupable(chain)) {
            System.err.print("Ungroupable layers chain: ");
            for(Layer l: chain)
                System.err.print(l.getName() + "->");

            System.err.println();
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
        generic.setFusedCompound(fuse);
        /** add generic layer to dnn*/
        _network.addLayer(subNetwork.getName(),generic,1);
        Layer genericLayer = _network.getLayer(subNetwork.getName());

        /**transfer connections*/
        Vector<Connection> inputConnections = _network.getLayerInputConnections(start);
        for(Connection inpCon: inputConnections){
            inpCon.changeDest(genericLayer);
            genericLayer.getInputConnections().add(inpCon);
            //_network.addConnection(inpCon.getSrc().getName(),genericLayer.getName());
        }

        Vector<Connection> outputConnections = _network.getLayerOutputConnections(end);
        for(Connection outpCon: outputConnections) {
            outpCon.changeSrc(genericLayer);
            genericLayer.getOutputConnections().add(outpCon);
            //_network.addConnection(genericLayer.getName(),outpCon.getDest().getName());
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
                //  _network.sortConcatsInputs();
                  return;
              }

              boolean continueSplitting = _makeSplitIteration(curIter, childrenNum, networkInputDataFormat, bottleneck, printDetails);
              if (!continueSplitting) {
                 // _network.sortConcatsInputs();
                  return;
              }
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
                if (!continueSplitting) {
                   // _network.sortConcatsInputs();
                    return;
                }
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

        if(bottleneck.getNeuron() instanceof ConnectionDependent)
            bottleneck = _findConIndependentBottleneck(bottleneck);

        if(bottleneck==null)
            throw new Exception("bottleneck layer search error!");

        return bottleneck;
    }

    /** find splittable layer for connection-dependent bottleneck*/
    private Layer _findConIndependentBottleneck(Layer cdepBottleneck){
        Layer curBottleneck;
        if( cdepBottleneck.getInputConnections().size()==0)
            return null;

        curBottleneck = cdepBottleneck.getInputConnections().firstElement().getSrc();
        if(!(curBottleneck.getNeuron() instanceof ConnectionDependent))
            return curBottleneck;

        else return _findConIndependentBottleneck(curBottleneck);
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

     //////////////////////////////////////////////////////////////////
    /////////          AUTO split-merge algorithms              //////

    /**
     * Auto-split complexity bottlenecks
     * @param maxNodes max number of nodes in the result DNN
     * @param verbose print operation details
     */
     public boolean dartsBottleneckAutoSplit(Integer maxNodes, boolean verbose, boolean eval){
         Integer curSplit = 0;
         boolean continueSplit = true;
         _network.setDataFormats(_network.getInputLayer().getOutputFormat());
         _network.initOperators();
         int childrenNum = 2;
         if(_network.countLayers()>maxNodes)
             return false;

         while (_network.countLayers()<maxNodes && continueSplit){
             try {
                     Layer bottleneck = _findBottleneck();
                     if(!isSplittable(bottleneck,childrenNum))
                         return false;
                     /** TODO: can be removed or changed!*/
                     // if(bottleneck.getNeuron() instanceof Convolution)
                        //   return false;

                      splitChains(bottleneck, childrenNum);
                      _network.setDataFormats(_network.getInputLayer().getOutputFormat());
                      _network.initOperators();

                 if (verbose)
                     System.out.println(bottleneck.getName() + " splitted into " + childrenNum + " sub-layers");

                 if(eval) {
                     CSDFGraph csdf = _converter.buildGraphLayerBased(_network);
                     CSDFEvalResult evalResult = _edInterface.evaluateCSDFGraph(csdf);

                 if(verbose)
                         System.out.println("Expected performance: " + evalResult.getPerformance());

        }


             }
             catch (Exception e){
                 continueSplit = false;
                 System.out.println("Bottleneck Auto-Split error: "+e.getMessage());
                 return false;
             }

             curSplit++;
         }
         return true;
     }


    /**
     * Auto-split complexity bottlenecks
     * @param maxSplits max number of split operations
     * @param verbose print operation details
     */
     public boolean complexityBottleneckAutoSplit(Integer maxSplits, boolean verbose, boolean eval){
         Integer curSplit = 0;
         boolean continueSplit = true;
         _network.setDataFormats(_network.getInputLayer().getOutputFormat());
         _network.initOperators();

         while (curSplit<maxSplits && continueSplit){
             try {
                 continueSplit = _splitComplexityBottleneck(verbose, eval);
             }
             catch (Exception e){
                 continueSplit = false;
                 System.out.println("Bottleneck Auto-Split error: "+e.getMessage());
                 return false;
             }

             curSplit++;
         }
         return true;
     }

    /**
     * Split complexity bottleneck layer
     * @return the complexity bottleneck layer
     * @throws Exception if an error occurs
     */
   private boolean _splitComplexityBottleneck(boolean verbose, boolean eval) throws Exception{
        Layer bottleneck = OperatorsComplexityEvaluator.getHeaviestLayer(_network);
        if(bottleneck==null)
            throw new Exception("bottleneck layer search error!");

        if(bottleneck.getNeuron() instanceof ConnectionDependent)
            bottleneck = _findConIndependentBottleneck(bottleneck);

        if(bottleneck==null)
            throw new Exception("bottleneck connection dependent layer search error!");

        Integer bottleneckComplexity = bottleneck.getNeuron().getOperator().getTimeComplexity();
        Long averageComplexity = OperatorsComplexityEvaluator.getAverageComplexity(_network);

        Integer childrenNum = bottleneckComplexity/averageComplexity.intValue();

        int neurons = bottleneck.getOutputChannels();
        if(bottleneck.getNeuron() instanceof DenseBlock)
            neurons = ((DenseBlock) bottleneck.getNeuron()).getNeuronsNum();
        childrenNum = Math.min(childrenNum,neurons);

        if(childrenNum<2)
            return false;

        /** TODO: can be removed or changed!*/
       // if(bottleneck.getNeuron() instanceof Convolution)
         //   return false;

        splitChains(bottleneck, childrenNum);
         _network.setDataFormats(_network.getInputLayer().getOutputFormat());
         _network.initOperators();


        if (verbose)
            System.out.println(bottleneck.getName() + " splitted into " + childrenNum + " sub-layers");

        if(eval) {
            CSDFGraph csdf = _converter.buildGraphLayerBased(_network);
            CSDFEvalResult evalResult = _edInterface.evaluateCSDFGraph(csdf);

            if(verbose)
                System.out.println("Expected performance: " + evalResult.getPerformance());

        }

        return true;

    }




    /**public static void splitBottleneck(){
        Collection<Map.Entry<String,Long>> complexities = getOperatorsComplexityDistinct(dnn);
        if(complexities.size()<1)
            return;
        Long totalComplexity = 0L;
        Long maxComplexity = 0L;
        Long avgComplexity = 0L;
        String mostComplexOp = "None";
         for(Map.Entry<String,Long> complexity:complexities){
             if(complexity.getValue()>maxComplexity){
                 mostComplexOp = complexity.getKey();
                 maxComplexity = complexity.getValue();
             }
             totalComplexity += complexity.getValue();
        }

        Long complexityRel = (maxComplexity * 100L)/totalComplexity;
        avgComplexity = totalComplexity/complexities.size();


        System.out.println("The most complex node is "+ mostComplexOp + ", with complexity: " + maxComplexity
                + ", which is " + complexityRel + "% of total graph complexity, while average complexity is "+
                avgComplexity + "( "+((avgComplexity * 100L)/totalComplexity)+"%)");
    }*/

    ////////////////////////////////////////////////////////////////
    /////////////////  AUTO MERGE /////////////////////////////////

    /*************************************************************/
    /*******************  COMPOUNDS                  ************/

    public void mergeCompounds(Vector<Vector<String>> compoundTemplates, boolean verbose){

        for(Vector<String> compoundTemplate: compoundTemplates) {
            if(verbose)
                System.out.println("compound template: " + compoundTemplate);

            try {  mergeCompound(compoundTemplate,verbose); }
            catch (Exception e) {System.out.println("Compound merging error: "+e.getMessage()); }
        }

    }

    /**
     * Search DNN in order to find compounds, correponding to compound template
     * @param compoundTemplate compound template to match
     * @param verbose if the details should be printed
     * @throws Exception if an error occurs
     */
    public void mergeCompound(Vector<String> compoundTemplate, boolean verbose) throws Exception {

        _network.setDataFormats(_network.getInputLayer().getOutputFormat());
        _network.sortLayersInTraverseOrder();

        /** get all the layers that potentially can start the compound*/
        String compoundStart = compoundTemplate.firstElement();
        Vector<Layer> compoundStartLayers = new Vector<>();
        for(Layer l: _network.getLayers()) {
            if(l.getNeuron().getName().toLowerCase().contains(compoundStart.toLowerCase()))
                compoundStartLayers.add(l);
        }

        Vector<Vector<Layer>> chainsToMerge = new Vector<>();
        /** prepare chains to merge*/
        Vector<Layer> chainToMerge;
        for (Layer layer : compoundStartLayers) {
                chainToMerge = getGraphCompounds(layer, compoundTemplate);

                if (chainToMerge != null) {
                    chainsToMerge.add(chainToMerge);
                }

        }

        /** merge chains*/
        Integer chainId = 0;
        for (Vector<Layer> chain : chainsToMerge) {
            groupLayers(chain,true);
            if (verbose) {
                System.out.println("Compound: " + chainId);
                for (Layer layer : chain)
                    System.out.print(layer.getName() + " -> ");
                System.out.println();
            }

            _network.setDataFormats(_network.getInputLayer().getOutputFormat());
            _network.initOperators();

            if (verbose) {
                System.out.println("Compound " + chainId + " merged");
            }
            chainId++;
        }
    }


     /**
     * If layer is followed by chain of layers to be merged
     * @param layer possible starts of compound
     * @return compound, if layer indeed starts a compond and null otherwise
     */
    protected Vector<Layer> getGraphCompounds(Layer layer, Vector<String> compoundTemplate){
        Vector<Layer> compound = new Vector<>();
        Layer curLayer = layer;

        for(String compoundElement: compoundTemplate)
        {
            if(matchesCompoundElement(curLayer,compoundElement)){
                compound.add(curLayer);
                curLayer = curLayer.getOutputConnections().firstElement().getDest();
            }
            else return null;

        }

        /**System.out.println("Mergeable chain: ");
        for (Layer chainMember: mergeChain)
            System.out.print(chainMember.getName() + " ... ");
        System.out.println();*/

        return compound;
    }

    /**
     * Check if layer matches compound element
     * Layer matches compound element, if it has one output
     * and name of the Layer neuron is equal to compoundNeuron name
     * Otherwise, layer does not match compound element
     * @param layer DNN layer
     * @param compoundNeuron String name of compound element
     * @return true, if layer matches compound element and false otherwise
     */
    protected boolean matchesCompoundElement(Layer layer, String compoundNeuron){
        boolean neuronMatches = layer.getNeuron().getName().toLowerCase().contains(compoundNeuron.toLowerCase());
        boolean outputConncetionsMatch = layer.getOutputConnections().size()==1;
        return neuronMatches && outputConncetionsMatch;
    }




    public void printGroupPlan(Network dnn, Integer groupsExpected){
        Vector<Vector<Layer>> groups = buildGroupPlan(dnn,groupsExpected);

       /** Integer groupId = 0;
        for(Vector<Layer> group: groups){
            System.out.println("GROUP "+ groupId + ", complexity: " + evalGroupComplexity(group));
            for(Layer layer: group){
                System.out.println("  " + layer.getName());
            }
            groupId++;
        }*/

    }


    public Vector<Vector<Layer>> buildGroupPlan(Network dnn, Integer groupsExpected) {
        dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());
        dnn.initOperators();
        dnn.sortLayersInTraverseOrder();

        Double totalDeviation = _getComplexityDeviation(dnn);
        System.out.println("Total complexity deviation (before transformations): " + totalDeviation);

        /**splitToBlocks(60,1000,2,true);

        totalDeviation = _getComplexityDeviation(dnn);
        System.out.println("Total complexity deviation (after split): " + totalDeviation);
        dnn.initOperators();
        dnn.sortLayersInTraverseOrder();*/

        Vector<Vector<Layer>> groups = _initGroupPlan(dnn);




       if(groups.size()>groupsExpected) {
         //   System.out.println("groups.size() = "+groups.size()+" > groups expected = " + groupsExpected);
            while (groups.size() > groupsExpected) {

                Integer leastComplexGroupId = getLeastComplexGroupId(groups);
                Vector<Layer> leastComplexGroup = groups.elementAt(leastComplexGroupId);

             /**  System.out.println("least complex group, complexity: "+evalGroupComplexity(leastComplexGroup));
                for(Layer l :leastComplexGroup)
                    System.out.print(l.getName() + ", ");
                System.out.println();*/
                _consumeGroup(leastComplexGroup,groups);

            }
        }

        totalDeviation = _getComplexityDeviation(groups);
        System.out.println("Total complexity deviation (after merge): " + totalDeviation);

        return groups;
    }

    private void _consumeGroup(Vector<Layer> group, Vector<Vector<Layer>> groups){
        Layer firstLayer = group.firstElement();
        Layer lastLayer = group.lastElement();

        /** consume by prev group*/
        if(firstLayer.getInputConnections().size()!=0) {
            try {
                Vector<Layer> beginGroup = _findGroupContains(groups, firstLayer.getInputConnections().firstElement().getSrc());

                /** if layer to be consumed is multiple inputs processor, all
                 * its inputs are consumed with this layer
                 * */
                for(int i=1; i<firstLayer.getInputConnections().size();i++){
                Vector<Layer> additionalGroup = _findGroupContains(groups, firstLayer.getInputConnections().elementAt(i).getSrc());

                for(Layer l: additionalGroup)
                    beginGroup.add(l);

                    groups.removeElement(additionalGroup);
                }

                for(Layer l: group)
                    beginGroup.add(l);

                groups.removeElement(group);
            }
            catch (Exception e){ System.out.println(e.getMessage());}
        }

        /** cosume by next group*/
        else {
            try {
                Vector<Layer> endGroup = _findGroupContains(groups, lastLayer.getOutputConnections().firstElement().getDest());
                for(Layer l: endGroup)
                    group.add(l);
                groups.removeElement(endGroup);
            }
            catch (Exception e){ System.out.println(e.getMessage());}
        }
    }

    private Vector<Layer> _findGroupContains(Vector<Vector<Layer>> groups, Layer layer) throws Exception {
        for(Vector<Layer> group: groups){
            if(group.contains(layer))
                return group;
        }
        throw new Exception("layer "+ layer.getName()+ "not found in groups plan!" );
    }
    
    
    

    /**
     * Get Id of the most time-complex group
     * @param groups layer groups
     * @return Id of the most time-complex group
     */
    private Integer getMostComplexGroupId (Vector<Vector<Layer>> groups){
        Long maxComplexity = 0L;
        Integer mostComplexGroupId = 0;
        Long curComplexity;
        Integer curGroupId = 0;
        for(Vector<Layer> group: groups){
            curComplexity = evalGroupComplexity(group);
            if(curComplexity>maxComplexity){
                maxComplexity = curComplexity;
                mostComplexGroupId = curGroupId;
            }
            curGroupId++;
        }
        return mostComplexGroupId;
    }


    /**
     * Get Id of the least time-complex group
     * @param groups layer groups
     * @return Id of the least time-complex group
     */
    private Integer getLeastComplexGroupId (Vector<Vector<Layer>> groups){
        if(groups.size()==0)
            return 0;

        Long leastComplexity = evalGroupComplexity(groups.firstElement());
        Integer leastComplexGroupId = 0;
        Long curComplexity;
        Integer curGroupId = 0;
        for(Vector<Layer> group: groups){
            curComplexity = evalGroupComplexity(group);
            if(curComplexity<leastComplexity){
                leastComplexity = curComplexity;
                leastComplexGroupId = curGroupId;
            }
            curGroupId++;
        }
        return leastComplexGroupId;
    }

    /**
     * Evaluate total group execution time complexity
     * @param group group of layers
     * @return group execution time complexity
     */
    public Long evalGroupComplexity(Vector<Layer> group){
        Long groupComplexity = 0L;
        for(Layer l: group){
            groupComplexity+=evalOpComplexity(l.getNeuron());
        }

        return groupComplexity;
    }

    /**
     * Evaluate DNN neuron operator time complexity as a long number
     * @param neuron DNN neuron
     * @return DNN neuron operator time complexity as a long number
     */
    public Long evalOpComplexity(Neuron neuron){
        Operator op = neuron.getOperator();
        Long opComplexity = 0L;

        if(op!=null) {
            opComplexity = Long.parseLong (op.getTimeComplexity().toString());
            if (opComplexity == null) opComplexity = 0L;
            }

        return opComplexity;
    }

    /**
     * Initialize group plan. Initially in group plan every layer belongs to
     * its own group
     * @param dnn DNN
     * @return list of groups, where each group contains one layer of DNN
     */
    private Vector<Vector<Layer>> _initGroupPlan(Network dnn){


        Vector<Vector<Layer>> chainsToMerge = new Vector<>();
        /** prepare chains*/
        for(Layer layer: dnn.getLayers()){

          Vector<Layer> chainToMerge = new Vector<>();
          chainToMerge.add(layer);
          chainsToMerge.add(chainToMerge);
        }
        return chainsToMerge;
    }

    private Double _getComplexityDeviation(Network dnn){
        Vector<Double> complexities = new Vector<>();
        Double avgComplexity = 0.0;
        Double totalDeviation = 0.0;
        Double totalNodes = 0.0;
        for(Layer l: dnn.getLayers()){
            if(!(l.getNeuron() instanceof Data) && !(l.getNeuron() instanceof Concat)) {
                Double complexity = Double.parseDouble(l.getNeuron().getOperator().getTimeComplexity().toString());
                complexities.add(complexity);
                avgComplexity += complexity;
                totalNodes++;
            }
        }
        avgComplexity = avgComplexity/totalNodes;

        for(Double complexity: complexities){
            totalDeviation += Math.abs((complexity-avgComplexity));
        }
        totalDeviation = totalDeviation/totalNodes;
        //to percent?
        totalDeviation = totalDeviation/avgComplexity;


        return totalDeviation;
    }

     private Double _getComplexityDeviation(Vector<Vector<Layer>> groups){
        Vector<Double> complexities = new Vector<>();
        Double avgComplexity = 0.0;
        Double totalDeviation = 0.0;
        Double totalNodes = 0.0;
        for(Vector<Layer> group: groups){
                Double complexity = Double.parseDouble(evalGroupComplexity(group).toString());
                complexities.add(complexity);
                avgComplexity += complexity;
                totalNodes++;
        }
        avgComplexity = avgComplexity/totalNodes;

        for(Double complexity: complexities){
            totalDeviation += Math.abs((complexity-avgComplexity));
        }
        totalDeviation = totalDeviation/totalNodes;
        //to percent?
         totalDeviation = totalDeviation/avgComplexity;


        return totalDeviation;
    }

    /**
     * If the neuron can start mergeable chain
     * @param neuron neuron
     * @return true, if neuron can start mergeable chain and false otherwise

    protected boolean mergeable(Neuron neuron){
        /** TODo: remove this condition if possible
        if (neuron instanceof MultipleInputsProcessor)
            return false;

        if(neuron instanceof Data)
            return false;

        if (neuron instanceof ConnectionDependent)
            return true;

        if(neuron instanceof GenericNeuron)
            return true;

        return false;
    }*/

     /**
     * Independent layer, followed by chain of dependent layers to be merged
     * @param layer DNN layer
     * @return Independent layer, followed by chain of dependent layers to be merged
     */
    protected Vector<Layer> getMergeChain(Layer layer){
        Vector<Layer> mergeChain = new Vector<>();
        Vector<Connection> ocs = layer.getOutputConnections();

        mergeChain.add(layer);

        switch (ocs.size()){
            case 0: {  break;}
            case 1: {
                 //Connection singleOC = ocs.firstElement();

                  Vector<Vector<Layer>> chains = new Vector<>();
                  _buildDependentTrack(layer,chains);
               //   printChains(chains);

                for(Vector<Layer> chain: chains){
                    for(Layer l: chain){
                        if(!mergeChain.contains(l))
                            mergeChain.add(l);
                    }
                }

             //    if(singleOC.getDest().getNeuron() instanceof ConnectionDependent ||
               //          singleOC.getDest().getNeuron() instanceof MultipleInputsProcessor)
                 //    mergeChain.add(singleOC.getDest());
                 break;

            }

            //2+
            default: {
                //for(Connection oc: ocs)
                  //  mergeChain.add(oc.getDest());

                Vector<Vector<Layer>> chains = new Vector<>();
                  _buildDependentTrack(layer,chains);
               //   printChains(chains);

                for(Vector<Layer> chain: chains){
                    for(Layer l: chain){
                        if(!mergeChain.contains(l))
                            mergeChain.add(l);
                    }
                }


                break;

            }


        }
        return mergeChain;

    }

       /**
     * Stack chain to exstising merging chanes of add new chain to chains list
     * @param chainsToMerge list of existing chains to merge
     * @param newChain new chain to merge
     */
    protected void stackOrAddChain(Vector<Vector<Layer>> chainsToMerge, Vector<Layer> newChain){
        boolean stacked = false;
        for(Vector<Layer> existingChain: chainsToMerge){
            if(existingChain.lastElement().equals(newChain.firstElement())){
                stacked = true;
                for(int i=1; i<newChain.size(); i++ )
                    existingChain.add(newChain.elementAt(i));
            }
        }

        if(!stacked)
            chainsToMerge.add(newChain);

    }




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
