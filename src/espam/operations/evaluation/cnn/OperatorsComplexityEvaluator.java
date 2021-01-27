package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.operators.Operator;

import java.util.*;

/**
 * Evaluates operators complexity
 */
public class OperatorsComplexityEvaluator {
    static <String,Long extends Comparable<? super Long>>
    SortedSet<Map.Entry<String, Long>> entriesSortedByValues(Map<String, Long> map) {
    SortedSet<Map.Entry<String, Long>> sortedEntries = new TreeSet<Map.Entry<String, Long>>(
        new Comparator<Map.Entry<String, Long>>() {
            @Override public int compare(Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        }
    );
    sortedEntries.addAll(map.entrySet());
    return sortedEntries;
}


    /**
     * Get distinct list of DNN operators complexity
     * @param dnn deep neural network
     * @return distinct list of DNN operators complexity
     */
    public static Collection<Map.Entry<String,Long>> getOperatorsComplexityDistinct(Network dnn){
        TreeMap<String,Long> result = new TreeMap<>();
        Operator op;
        //String opName;
        String lname;
        Long opComplexity;
        for(Layer layer: dnn.getLayers()) {
            lname = layer.getName();
            if (!(layer.getNeuron() instanceof Data) ){
                op = layer.getNeuron().getOperator();
                if (op != null) {
                   // opName = op.getName();

                    if (!result.containsKey(lname)) {
                        opComplexity = Long.parseLong (op.getTimeComplexity().toString());
                        if (opComplexity == null)
                            opComplexity = 0L;
                        result.put(lname, opComplexity);
                    }

                }
            }
        }

        return entriesSortedByValues(result);
    }

    public static void printOperatorsComplexityDistinct(Network dnn){
        Collection<Map.Entry<String,Long>> complexities = getOperatorsComplexityDistinct(dnn);
        for(Map.Entry<String,Long> complexity:complexities){
            System.out.println(complexity.getKey() + ":"+complexity.getValue());
        }
    }

    public static Map.Entry<String,Long> getHeaviestNode(Network dnn){
        Collection<Map.Entry<String,Long>> complexities = getOperatorsComplexityDistinct(dnn);
       Object complArr [] = complexities.toArray();
       return (Map.Entry<String,Long>)complArr[complArr.length-1];
    }

   public static void printBottleneck(Network dnn){
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
    }


    public static Layer getHeaviestLayer(Network dnn){
        Collection<Map.Entry<String,Long>> complexities = getOperatorsComplexityDistinct(dnn);
        Layer heaviest = dnn.getInputLayer();

        if(complexities.size()<1)
            return heaviest;
        Long maxComplexity = 0L;

         for(Map.Entry<String,Long> complexity:complexities){
             if(complexity.getValue()>maxComplexity){
                 maxComplexity = complexity.getValue();
                 heaviest = dnn.getLayer(complexity.getKey());
             }
        }

        return  heaviest;
    }

     public static Long getAverageComplexity(Network dnn){
        Collection<Map.Entry<String,Long>> complexities = getOperatorsComplexityDistinct(dnn);
        if(complexities.size()<1)
            return 0L;
        Long totalComplexity = 0L;
         for(Map.Entry<String,Long> complexity:complexities){
             totalComplexity += complexity.getValue();
        }

        Long avgComplexity = avgComplexity = totalComplexity/complexities.size();

        return avgComplexity;
    }





    public static void printHeaviestNode(Network dnn){
        Map.Entry<String,Long> heaviest = getHeaviestNode(dnn);
        System.out.println(heaviest.getKey() + ":"+heaviest.getValue());
    }

}
