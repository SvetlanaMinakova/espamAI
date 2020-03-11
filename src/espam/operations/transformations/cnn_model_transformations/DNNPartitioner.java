package espam.operations.transformations.cnn_model_transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import java.util.Vector;

public class DNNPartitioner {





    /** TODO: REMOVE After testing*/
    public void partitionDNNAlexNet(Network dnn){

         DNNPartitioner partitioner = new DNNPartitioner();
         Vector<Vector<Integer>> partitions = new Vector<>();

         Vector<Integer> p1 = new Vector<>();
         p1.add(dnn.getLayer("node_Conv0").getId());
         p1.add(dnn.getLayer("node_Conv2").getId());
         p1.add(dnn.getLayer("node_MaxPool4").getId());
         p1.add(dnn.getLayer("node_Conv5").getId());
         p1.add(dnn.getLayer("node_Conv7").getId());
         p1.add(dnn.getLayer("node_MaxPool9").getId());
         p1.add(dnn.getLayer("node_Conv10").getId());
         p1.add(dnn.getLayer("node_Conv12").getId());
         p1.add(dnn.getLayer("node_Conv14").getId());
         p1.add(dnn.getLayer("node_Conv16").getId());

         partitions.add(p1);

         //'node_Conv19 - CPU1'

         //['node_Conv21', 'node_Conv23', 'node_Conv25', 'node_MaxPool27', 'node_Conv28', 'node_Conv30']
         Vector<Integer> p2 = new Vector<>();
         p2.add(dnn.getLayer("node_Conv21").getId());
         p2.add(dnn.getLayer("node_Conv23").getId());
         p2.add(dnn.getLayer("node_Conv25").getId());
         p2.add(dnn.getLayer("node_MaxPool27").getId());
         p2.add(dnn.getLayer("node_Conv28").getId());
         p2.add(dnn.getLayer("node_Conv30").getId());

         partitions.add(p2);

         //'node_Conv32 - CPU4'

         //['node_Conv34', 'node_MaxPool36'] - GPU
         Vector<Integer> p3 = new Vector<>();
         p3.add(dnn.getLayer("node_Conv34").getId());
         p3.add(dnn.getLayer("node_MaxPool36").getId());

         partitions.add(p3);
         Vector<Integer> p4 = new Vector<>();
         p4.add(dnn.getLayer("node_Gemm43").getId());
         p4.add(dnn.getLayer("node_Softmax44").getId());
         partitions.add(p4);


        // partitioner.partitionDNN(dnn, partitions);

         dnn.sortLayersInTraverseOrder();
    }


    /** TODO: REMOVE After testing*/
    public void partitionDNNVGG(Network dnn){


          //callVisitorForPartitions(dnn, useGPU, dir);
         // CNNTopologyJSONVisitor.callVisitor(dnn, dir);

         /***
          *
          *
          * CPU0 {
          ['node_Gemm37']
               }
CPU1 {
['node_Conv19']
}
CPU2 {
['node_Gemm43', 'node_Softmax44']
}
CPU3 {
['node_MaxPool18']
['node_Gemm40']
}
CPU4 {
['node_Conv32']
}
GPU {
['node_Conv0', 'node_Conv2', 'node_MaxPool4', 'node_Conv5', 'node_Conv7', 'node_MaxPool9', 'node_Conv10', 'node_Conv12', 'node_Conv14', 'node_Conv16']
['node_Conv21', 'node_Conv23', 'node_Conv25', 'node_MaxPool27', 'node_Conv28', 'node_Conv30']
['node_Conv34', 'node_MaxPool36']
}
          *
          *
          */

         Vector<Boolean> useGPU = new Vector<>();
         DNNPartitioner partitioner = new DNNPartitioner();
         Vector<Vector<Integer>> partitions = new Vector<>();

         Vector<Integer> p1 = new Vector<>();
         p1.add(dnn.getLayer("node_Conv0").getId());
         p1.add(dnn.getLayer("node_Conv2").getId());
         p1.add(dnn.getLayer("node_MaxPool4").getId());
         p1.add(dnn.getLayer("node_Conv5").getId());
         p1.add(dnn.getLayer("node_Conv7").getId());
         p1.add(dnn.getLayer("node_MaxPool9").getId());
         p1.add(dnn.getLayer("node_Conv10").getId());
         p1.add(dnn.getLayer("node_Conv12").getId());
         p1.add(dnn.getLayer("node_Conv14").getId());
         p1.add(dnn.getLayer("node_Conv16").getId());

         partitions.add(p1);

         //'node_Conv19 - CPU1'

         //['node_Conv21', 'node_Conv23', 'node_Conv25', 'node_MaxPool27', 'node_Conv28', 'node_Conv30']
         Vector<Integer> p2 = new Vector<>();
         p2.add(dnn.getLayer("node_Conv21").getId());
         p2.add(dnn.getLayer("node_Conv23").getId());
         p2.add(dnn.getLayer("node_Conv25").getId());
         p2.add(dnn.getLayer("node_MaxPool27").getId());
         p2.add(dnn.getLayer("node_Conv28").getId());
         p2.add(dnn.getLayer("node_Conv30").getId());

         partitions.add(p2);

         //'node_Conv32 - CPU4'

         //['node_Conv34', 'node_MaxPool36'] - GPU
         Vector<Integer> p3 = new Vector<>();
         p3.add(dnn.getLayer("node_Conv34").getId());
         p3.add(dnn.getLayer("node_MaxPool36").getId());

         partitions.add(p3);
         Vector<Integer> p4 = new Vector<>();
         p4.add(dnn.getLayer("node_Gemm43").getId());
         p4.add(dnn.getLayer("node_Softmax44").getId());
         partitions.add(p4);


       //  partitioner.partitionDNN(dnn, partitions);
    }


    /**
     * Transform DNN to partitioned DNN
     * @param dnn DNN
     * @param partitioning DNN partitioning specification
     */
    public void partitionDNN(Network dnn, Vector<DNNPartition> partitioning){

        Vector<Vector<Layer>> partitions = new Vector<>();
        Vector<Layer> partition;

        for(DNNPartition p: partitioning ){
            partition = new Vector<>();
            for(String name: p.getLayers()){
                Layer l = dnn.getLayer(name);
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
}
