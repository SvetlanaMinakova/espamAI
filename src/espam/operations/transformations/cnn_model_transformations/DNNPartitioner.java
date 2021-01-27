package espam.operations.transformations.cnn_model_transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;

import java.util.Vector;

public class DNNPartitioner {

    /**
     * Transform DNN to partitioned DNN
     * @param dnn DNN
     * @param partitioning DNN partitioning specification
     */
    public void partitionDNN(Network dnn, Vector<DNNPartition> partitioning){

        dnn.sortLayersInTraverseOrder();
        dnn.updateDataFormats();

        Vector<Vector<Layer>> partitions = _createPartitions(dnn, partitioning);
        CNNTransformer transformer = new CNNTransformer(dnn);
        partitions = _getDuplicateResolvedPartitions(transformer, partitions);

        dnn.sortLayersInTraverseOrder();

        int pId = 0;
        Vector<Layer> prevPartition = null;
        DuplicateLayersResolveCase drc;

        for(Vector<Layer> p: partitions) {
            if (p.size() > 1) {//group chains with >=2 layers
             _groupLayersIntoPartition(transformer, p, pId);
            }
            pId++;
        }

        System.out.println("Partitioning finished successfully");

    }


    private Vector<Vector<Layer>> _getDuplicateResolvedPartitions(CNNTransformer transformer, Vector<Vector<Layer>> partitions){
        Vector<Vector<Layer>>  resolvedPartitons = partitions;
        int pId = 0;
        Vector<Layer> prevPartition = null;
        DuplicateLayersResolveCase drc;

        for(Vector<Layer> p: partitions) {
            drc = _getDuplicateLayersResolveCase(p, prevPartition);
            if (drc.equals(DuplicateLayersResolveCase.REMOVE_FROM_PREV))
                prevPartition.removeElementAt(prevPartition.size()-1);

            if (drc.equals(DuplicateLayersResolveCase.REMOVE_FROM_NEXT))
                p.removeElementAt(0);

            if (drc.equals(DuplicateLayersResolveCase.DUPLICATE)){
                try {
                    Layer duplicateDst = transformer.splitMultiInMultiOut(p.firstElement().getName());
                    p.removeElementAt(0);
                    p.insertElementAt(duplicateDst, 0);
                }
                catch (Exception e){
                    System.err.println("Partitioning creation error. Layer "+prevPartition.lastElement().getName() +
                            " duplication resolve error: " + e.getMessage());
                }

            }

            prevPartition = p;
        }
      return resolvedPartitons;
    }

    /**
     * Get answer on how to resolve case, when a CNN layer is in two partitions simultaneously
     */
    private DuplicateLayersResolveCase _getDuplicateLayersResolveCase(Vector<Layer> partition, Vector<Layer> prevPartition){
        /**no preious layer*/
        if (prevPartition == null)
            return DuplicateLayersResolveCase.DO_NOTHING;

        /**no duplicates found*/
        if(prevPartition.lastElement().getName() != partition.firstElement().getName())
            return DuplicateLayersResolveCase.DO_NOTHING;

        Layer duplicate = prevPartition.lastElement();
        Integer duplicateOutputsNum = duplicate.getOutputConnections().size();
        Integer duplicateInputsNum = duplicate.getInputConnections().size();

        /** single-input duplicate goes to the next (output) partition */
        if (duplicateInputsNum < 2)
            return DuplicateLayersResolveCase.REMOVE_FROM_PREV;

        /**single-output duplicate goes to the prev (input) partition */
        if (duplicateOutputsNum < 2)
            return DuplicateLayersResolveCase.REMOVE_FROM_NEXT;

        /**multi-input and multi-output duplicate should be duplicated in the target DNN*/
        return DuplicateLayersResolveCase.DUPLICATE;
    }

    /**
     * Create dnn partitions (groups of layers) using given dnn partitioning
     * @param dnn dnn
     * @param partitioning dnn partitioning
     * @return partitions (groups of layers) using given dnn partitioning
     */
    private Vector<Vector<Layer>> _createPartitions(Network dnn, Vector<DNNPartition> partitioning){
        Vector<Vector<Layer>> partitions = new Vector<>();
        Vector<Layer> partition;

        for(DNNPartition p: partitioning ) {
            partition = new Vector<>();
            for (Layer l : dnn.getLayers()){

                if (p.getLayers().contains(l.getName())){
                    partition.add(l);
                }
            }
            partitions.add(partition);
        }
        return partitions;
    }

    /**
     * Group DNN layers into partition
     * @param transformer CNN transformer that performs grouping
     */
    private void _groupLayersIntoPartition(CNNTransformer transformer, Vector<Layer> partition, Integer partitionId){
        Network partitionDNN;
        try {
            //System.out.println("Pid "+ pId + " grouped ");
            partitionDNN = transformer.groupLayers(partition).getInternalStructure();
            partitionDNN.updateDataFormats();

            //System.out.println(partitionDNN.getName() + " successfully created");
            //dnn.sortLayersInTraverseOrder();
            //dnn.updateDataFormats();
        } catch (Exception e) {
            System.out.println("Partitioning error on partition " + partitionId + ", reason: " + e.getMessage());
        }

    }


    /**
     * Transform DNN to partitioned DNN
     * @param dnn DNN
     * @param partitionIds Lists of ids to be partitioned
     */
    private void partitionDNNByIds(Network dnn, Vector<Vector<Integer>> partitionIds ){

        Vector<Vector<Layer>> partitions = new Vector<>();
        Vector<Layer> partition;

        for( Vector<Integer> partitionIdsList: partitionIds ){
            partition = new Vector<>();
            for(Integer id: partitionIdsList){
                Layer l = dnn.getLayer(id);
                partition.add(l);
            }
            partitions.add(partition);
        }

        CNNTransformer transformer = new CNNTransformer(dnn);
        int pId = 0;
        for(Vector<Layer> p: partitions) {
            try {
                transformer.groupLayers(p);
                dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());
            }

            catch (Exception e) {
                System.out.println("Partitioning error on partition "+ pId + ", reason: "+e.getMessage());
            }
            pId++;
        }

    }

    public enum DuplicateLayersResolveCase {
        REMOVE_FROM_PREV, REMOVE_FROM_NEXT, DUPLICATE, DO_NOTHING
    }
}
