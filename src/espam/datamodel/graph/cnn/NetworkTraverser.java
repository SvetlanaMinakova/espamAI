package espam.datamodel.graph.cnn;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;

import java.util.*;
/**
 * Class that implements traverse of Network graph
 */
public class NetworkTraverser {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * initialize traverser
     */
    public void initialize(Network network, boolean reverseConnections) {

        _network = network;

        _layerTraveseIds = new HashMap<>();
        int traverseId = 0;
        for(Layer layer:_network.getLayers()) {
            _layerTraveseIds.put(traverseId,layer.getId());
            traverseId++;
        }

        _n = _layerTraveseIds.size();

        /**initialize linked list*/
        adj = new ArrayList[_n];
        for (int i = 0; i < _n; ++i) {
            adj[i] = new ArrayList();
        }

        /** extract connections*/
        Vector<IndexPair> connectionsDistinct = new Vector<>();

        if(reverseConnections)
            connectionsDistinct = extractReversedConnectionsDistinct(network.getLayers());
        else
            connectionsDistinct = extractConnectionsDistinct(network.getLayers());

       _connectionsGroupedBySrc = IndexPair.groupByFirstIndex(connectionsDistinct);
       _connectionsGroupedByDst = IndexPair.groupBySecondIndex(connectionsDistinct);

       _m = connectionsDistinct.size();

      IndexPair currentConnection;

         for (int i = 0; i < _m;i++) {

         currentConnection = connectionsDistinct.elementAt(i);

         int v = currentConnection.getFirst();
         int w = currentConnection.getSecond();

         /** dircted graph*/
         adj[v].add(w);
        // adj[w].add(v); //for undirected graphs
         }

         used = new boolean[_n];
         Arrays.fill(used, false);
         queue = new LinkedList();
    }

     /**
     * get layers traverse order
     * @param startLayerId start node for DNN graph traverse
     * @return layers traverse order
     */
    public Vector<Integer> getLayersTraverseOrder(int startLayerId) {
        _layersTraverseOrder = new Vector<Integer>();
        int startLayerTraverseId = getTraverseLayerId(startLayerId);
        modifiedBFS(startLayerTraverseId);
        mapLayersTraverseOrderOntoRealIds();
        return _layersTraverseOrder;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  public debugging methods                ////
     /**
     * Get list of the connections grouped by source layer Id
     * @return list of the connections grouped by source layer Id
     */
    public HashMap<Integer, Vector<Integer>> getConnectionsGroupedBySrc() {
        return _connectionsGroupedBySrc;
    }

     /**
     * Print list of connections of result network, grouped by source layer Id
     */
    public void printGroupedBySrcConnections() {
        printGroupedConnections(_connectionsGroupedBySrc);
    }

    /**
     * Print list of connections of result network, grouped by source layer Id
     */
    public void printGroupedByDstConnections() {
        printGroupedConnections(_connectionsGroupedByDst);
    }

    /**
     * Print list of connections of result network, grouped by layer Id
     */
    public void printGroupedConnections(HashMap<Integer,Vector<Integer>> groupedConnections) {
        for (Map.Entry entry : groupedConnections.entrySet()) {
            Layer src = _network.getLayer((int) entry.getKey());
            System.out.println("");
            System.out.print(src.getName() + "---> [");

            for (Integer dstId : (Vector<Integer>) entry.getValue()) {
                Layer dst = _network.getLayer(dstId);
                System.out.print(dst.getName() + " , ");
            }
            System.out.print(" ]");
            System.out.println("");
        }
    }

    /**
     * Print layers traverse order
     */
    public void printTraverseOrder(){
        for(Integer layerId:_layersTraverseOrder){
            System.out.println(_network.getLayer(layerId).getName());
        }
    }

    /**
     * Print traverse order of specified layers
     * @param layerNames names of the layers
     */
    public void printTraverseOrder(String ... layerNames){
        for(String layerName: layerNames){
            int layerId =_network.getLayer(layerName).getId();
            int traverseOrder = _layersTraverseOrder.indexOf(layerId);
            System.out.println(layerName + " : "+traverseOrder);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                     ////

    /**
     * Modified breadth-first search (BFS). Algorithm is a modification
     * of a classical BFS algorithm: look E. F. Moore (1959),
     * The shortest path through a maze. In Proceedings of the
     * International Symposium on the Theory of Switching,
     * Harvard University Press, pp. 285–292)
     * Modified BFS moves from DNN graph input to DNN graph output,
     * uses principles of classical BFS,but takes into account multiple
     * inputs of DNN nodes.It recursively returns back and searches
     * for a new way, if any of inputs of the current node is not visited.
     * @param startLayerId start layer id
     */
    protected void modifiedBFS(int startLayerId) {
        /** if node is already traversed, pass */
        if (used[startLayerId]) {
            return;
        }

        /**start traverse*/
        queue.add(startLayerId);

        /**mark node as visited*/
        used[startLayerId] = true;

        while (!queue.isEmpty()) {
            /**get node from the queue*/
            startLayerId = queue.poll();

            /** go back, if layer have non-visited inputs*/
            Vector<Integer> nonVisitedInputs = getNonVisitedInputsList(startLayerId);
            if(nonVisitedInputs.size()>0) {
                for(Integer nonVisitedInput: nonVisitedInputs){
                    modifiedBFS(nonVisitedInput);
                }
            }

            /** add layers to sorted traverse order*/
            _layersTraverseOrder.add(startLayerId);

            /**visit all nodes, adjusted with current layer*/
            for (int i = 0; i < adj[startLayerId].size(); ++i) {
                /**if node is already visited, pass */
                int w = adj[startLayerId].get(i);
                if (used[w]) {
                    continue;
                }
                /** add node id to queue*/
                queue.add(w);

                /**mark node as visited*/
                used[w] = true;
            }
        }
    }

     /**
     * Get list of non-visited inputs of the layer node
     * @param layerId layer id
     * @return id of the next non-visited input, if there are any
     * and null otherwise
     */
    protected Vector<Integer> getNonVisitedInputsList(int layerId){
        Vector<Integer> nonVitiedInputs = new Vector<>();

        Vector<Integer> layerInputs = _connectionsGroupedByDst.get(layerId);
        /** layer have no inputs*/
        if(layerInputs==null)
            return nonVitiedInputs;

        for(int inputLayerId: layerInputs){
            if(!_layersTraverseOrder.contains(inputLayerId))
                nonVitiedInputs.add(inputLayerId);
    }
        return nonVitiedInputs;

    }

     /** Extract distinct connections for specified layer
     * @param layers list of espam.cnn Layers
     */
    private Vector<IndexPair> extractConnectionsDistinct(Vector<Layer> layers){
        Vector<IndexPair> connectionsList = new Vector<>();

        for(Layer layer: layers) {
            Vector<Layer> inputs = getInputLayers(layer);
            for (Layer input : inputs)
                appendConnectionDistinct(input, layer, connectionsList);

            Vector<Layer> outputs = getOutputLayers(layer);
            for (Layer output : outputs)
                appendConnectionDistinct(layer, output, connectionsList);
        }

        return connectionsList;
    }

       /** Extract distinct connections for specified layer
     * @param layers list of espam.cnn Layers
     */
    private Vector<IndexPair> extractReversedConnectionsDistinct(Vector<Layer> layers){
       Vector<IndexPair> connectionsList = new Vector<>();

        for(Layer layer: layers) {

            Vector<Layer> inputs = getInputLayers(layer);
            for (Layer input : inputs)
                appendConnectionDistinct(layer, input, connectionsList);

            Vector<Layer> outputs = getOutputLayers(layer);
            for (Layer output : outputs)
                appendConnectionDistinct(output, layer, connectionsList);
        }
        return connectionsList;
    }

       /**
     * append connection to connections list, if connection does not exist in this list
     * @param src connection source Layer
     * @param dst connection destination Layer
     * @param connectionsList existing connections List
     */
    protected void appendConnectionDistinct(Layer src,Layer dst,Vector<IndexPair> connectionsList){
        int srcTraverseId = getTraverseLayerId(src.getId());
        int dstTraverseId = getTraverseLayerId(dst.getId());

        IndexPair newIndexPair = new IndexPair(srcTraverseId,dstTraverseId);
        if(!connectionsList.contains(newIndexPair)){
            connectionsList.add(newIndexPair);
        }
    }

    /**
     * Return list of layer's inputs
     * @param layer layer to inspect
     * @return list of names of layer inputs
     */
    private Vector<Layer> getInputLayers(Layer layer) {
        Vector<Layer> inputLayers = new Vector<>();
        Vector<Connection> inputConnections= _network.getLayerInputConnections(layer);
        for(Connection connection: inputConnections)
            inputLayers.add(connection.getSrc());
        return inputLayers;
    }

    /**
     * Return list of layer's outputs
     * @param layer layer to inspect
     * @return list of names of layer outputs
     */
    private Vector<Layer> getOutputLayers(Layer layer) {
        Vector<Layer> outputLayers = new Vector<>();
        Vector<Connection> outputConnections= _network.getLayerOutputConnections(layer);
        for(Connection connection: outputConnections)
            outputLayers.add(connection.getDest());
        return outputLayers;
    }

    /**
     * Extracts real layer id from layerTraveseIds hashmap
     * @return real layer id extracted from layerTraveseIds hashmap
     */
    protected Integer getRealLayerId(int traverseId){
        Integer layerRealId = _layerTraveseIds.get(traverseId);

        if(layerRealId==null)
            System.err.println("Layer with traverse id = "+traverseId + " not found!");
        return layerRealId;
    }

      /**
     * Extracts traverse layer id from layerTraveseIds hashmap
     * @return traverse id extracted from layerTraveseIds hashmap
     */
    protected Integer getTraverseLayerId(int realId){
        for(Map.Entry<Integer,Integer> entry: _layerTraveseIds.entrySet()){
            if(entry.getValue()==realId)
                return entry.getKey();
        }
            System.err.println("Layer with id = "+realId + " not found in traverse order!");
        return null;
    }

    protected void mapLayersTraverseOrderOntoRealIds(){
        Vector<Integer> mappedTraverseOrder = new Vector<>();
        for(Integer traverseOrderId: _layersTraverseOrder){
            Integer realLayerId = getRealLayerId(traverseOrderId);
            mappedTraverseOrder.add(realLayerId);
        }
        _layersTraverseOrder = mappedTraverseOrder;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                   ////
    /**Network to traverse*/
    protected Network _network;
    /** number of nodes in the Network graph*/
    protected int _n;
    /** map with pairs <Node_id_in_traverse_order,Node_id>*/
    protected HashMap<Integer,Integer> _layerTraveseIds;
    /** number of connections in the Network graph*/
    protected int _m;
    /** adjust list*/
    protected ArrayList<Integer> adj[];
    /** if-visited markers list*/
    protected boolean used[];
    /**helper queue*/
    protected Queue<Integer> queue;
    /**Layers Ids in traverse order*/
    protected Vector<Integer> _layersTraverseOrder;
    /**connections in result DNN, grouped by source Layer Id*/
    protected HashMap<Integer,Vector<Integer>> _connectionsGroupedBySrc;
    /**connections in result DNN, grouped by destination Layer Id*/
    protected HashMap<Integer,Vector<Integer>> _connectionsGroupedByDst;
}
