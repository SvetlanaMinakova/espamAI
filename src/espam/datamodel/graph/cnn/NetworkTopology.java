package espam.datamodel.graph.cnn;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.simple.Data;

import java.util.Vector;

/**
 * Simplified DNN topology graph representation
 */
public class NetworkTopology {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create simplified representation of a DNN topology
     * @param dnn DNN
     */
    public NetworkTopology(Network dnn){
        try {
            _name = dnn.getName();
            dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());
            dnn.initOperators();
            dnn.sortLayersInTraverseOrder();

            Integer taskId = 0;
            long taskComplexity;
            Long totalComplexity = 0L;
            /** fill in task ids, names and complexities*/
            for(Layer l: dnn.getLayers()) {
                if (!(l.getId() == dnn.getInputLayerId() || l.getId() == dnn.getOutputLayerId())) {
                    _taskNames.add(l.getName());
                    _taskIds.add(taskId);
                    taskComplexity = l.getNeuron().getOperator().getTimeComplexity();
                    _taskComplexities.add(taskComplexity);
                    _operators.add(l.getNeuron().getOperator().getName());
                    totalComplexity += taskComplexity;
                    taskId++;
                }
            }

            taskId = 0;
            Integer ioTaskId, weight;
            Float relTaskComplexity;
            Float totalComplexityF = Float.parseFloat(totalComplexity.toString());
            //_adjacencyLists
            /** fill in relationships between tasks*/
            for(Layer l: dnn.getLayers()) {
                if (!(l.getId() == dnn.getInputLayerId() || l.getId() == dnn.getOutputLayerId())) {
                    Vector<Integer> adSubList = new Vector<>();
                    Vector<Integer> adReverseSubList = new Vector<>();

                    /** output connections weighst - equal for all output connections*/
                    weight = l.getOutputFormat().getElementsNumber();
                    _connectionWeights.add(weight);

                    /**total complexity percentage: share of task exec time complexity in total DNN exec time complexity*/
                    relTaskComplexity = Float.parseFloat(_taskComplexities.elementAt(taskId).toString()) * 100F / totalComplexityF ;
                    _taskComplexitiesShare.add(relTaskComplexity);


                    /** output connections*/
                    for (Connection con : l.getOutputConnections()) {
                        ioTaskId = _getTaskId(con.getDestName());
                        if (ioTaskId != -1)
                            adSubList.add(ioTaskId);

                    }
                    _adjacencyLists.add(adSubList);

                    /**input connections*/
                    for (Connection con : l.getInputConnections()) {
                        ioTaskId = _getTaskId(con.getSrcName());
                        if (ioTaskId != -1)
                            adReverseSubList.add(ioTaskId);

                    }
                    _adjacencyListsReverse.add(adReverseSubList);
                    taskId++;
                }
            }

        }
        catch (Exception e){

            System.out.println("Network simplified topology creation error: " + e.getMessage());
        }


    }

    /**public NetworkTopology(Vector<String> operators, Vector<String> tasks, Vector<Integer> task_complexities){
        _taskNames = tasks;
        _operators = operators;
        _taskComplexities = task_complexities;

    }*/

    /**
     * Get task Ids
     * @return task Ids
     */
    public Vector<Integer> getTaskIds() {
        return _taskIds;
    }

    /**
     * Get Operators
     * @return Operators
     */
    public Vector<String> getOperators() {
        return _operators;
    }

    /**
     * Get task complexities share
     * @return task complexities share
     */
    public Vector<Float> getTaskComplexitiesShare() {
        return _taskComplexitiesShare;
    }

    /**
     * Get task complexities
     * @return task complexities
     */
    public Vector<Long> getTaskComplexities() {
        return _taskComplexities;
    }

    /**
     * Get task names
     * @return task names
     */
    public Vector<String> getTaskNames() {
        return _taskNames;
    }

    /**
     * Get CNN topology name
     * @return CNN topology name
     */
    public String getName() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                ////

    /**
     * get task id by task name
     * @param name task name
     * @return task id
     */
    private int _getTaskId(String name){
        int taskId = _taskNames.indexOf(name);
        return taskId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////
    @SerializedName("name")private String _name;

    /**performed base operators:*/
    @SerializedName("operators")private Vector<String> _operators = new Vector<>();

    /**unique task names - layer names in DNN*/
    @SerializedName("task_names")private Vector<String> _taskNames = new Vector<>();

    /** unique ids of the DNN nodes in topology - might not match DNN Layer ids!
     * 1) DNN layer Ids are determined by order of DNN layers insertion into topology,
     *    taskIds are sorted in DNN traverse order
     * - taskIds.lastElement() = DNN layers.count(), which is not necessary for layer ids*/
    @SerializedName("task_ids")private Vector<Integer> _taskIds = new Vector<>();

    /** evaluated task complexities */
    @SerializedName("task_complexities")private Vector<Long> _taskComplexities = new Vector<>();

    /** relative task complexities percentage*/
    @SerializedName("task_complexities_share")private Vector<Float> _taskComplexitiesShare = new Vector<>();


    /** Connections between DNN layers, represented as  adjacencyLists:
     * Contains ouytput connections for every DNN layer*/
    @SerializedName("adjacency_lists") Vector<Vector<Integer>> _adjacencyLists = new Vector<>();


    /** weights of connections between CNN nodes*/
    @SerializedName("output_connection_weighs")private Vector<Integer> _connectionWeights = new Vector<>();

    /** Connections between DNN layers, represented as  adjacencyLists:
     * contain input connections for every DNN layer*/
    @SerializedName("adjacency_lists_reverse") Vector<Vector<Integer>> _adjacencyListsReverse = new Vector<>();

}
