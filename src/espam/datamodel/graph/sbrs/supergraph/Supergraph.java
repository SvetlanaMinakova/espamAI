package espam.datamodel.graph.sbrs.supergraph;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.sbrs.control.ExecutionSequence;
import espam.datamodel.graph.sbrs.control.ExecutionStep;
import espam.datamodel.graph.sbrs.control.ParameterName;
import espam.datamodel.graph.sbrs.control.ControlNode;

import java.util.HashMap;
import java.util.Vector;

/** This class implements scenarios supergraph for
 * SBRS computational model models a CNN-based application with scenario-based execution
 * at the edge.
 */

public class Supergraph {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**Empty Constructor to create new SBRS MoC */
    public Supergraph() {
        _name = "sbrs";
        _adaptive = new Vector<>();
        _layers = new Vector<SBRSLayer>();
        _connections=new Vector<SBRSConnection>();
        _executionSequences = new Vector<>();
    }

    /**Empty Constructor to create new SBRS MoC */
    public Supergraph(Vector<ParameterName> adaptive) {
        _name = "sbrs";
        _adaptive = adaptive;
        _layers = new Vector<SBRSLayer>();
        _connections=new Vector<SBRSConnection>();
        _executionSequences = new Vector<>();
    }

    /**
     * Add new sbrs conneciton with given parameters
     * @param parent parent connection (in scenario)
     * @param src connection source
     * @param dst connection destination
     * @param duplicateId duplicate Id (see SBRS connection _duplicateId)
     */
    public void addNewConnection(Connection parent, SBRSLayer src, SBRSLayer dst, Integer duplicateId){
        if (src==null || dst==null){
            System.err.print("SBRS moc adding connection " + parent.getSrcName() + " --> " + parent.getDestName() + " error. ");
            if (src==null)
                System.out.print("Null-src. ");
            if (dst==null)
                System.out.print("Null-dst. ");
            System.out.println();

        }
        SBRSConnection newConnection = new SBRSConnection(parent, src, dst, duplicateId);
        _connections.add(newConnection);
    }

    /**
     * Add new SBRS layer with given parent
     * @param parent parent (CNN) layer
     */
    public void addNewLayer(Layer parent){
        SBRSLayer newLayer = new SBRSLayer(parent, _adaptive);
        _layers.add(newLayer);
    }

    /***
     * Find layer to reuse
     * @param scenarioLayer layer of a scenario
     * @return layer of a scenario, which can be reused,
     * or null if such layer does not exist
     */
    public SBRSLayer findReuseLayer(Layer scenarioLayer){
        for (SBRSLayer sbrsLayer: _layers){
            if (sbrsLayer.reusableFor(scenarioLayer))
                return sbrsLayer;
        }
        return null;
    }

    /***
     * Find layer to reuse
     * @param scenarioLayer layer of a scenario
     * @return layer of a scenario, which can be reused,
     * or null if such layer does not exist
     */
    public SBRSLayer findFullyEquivalentLayer(Layer scenarioLayer){
        for (SBRSLayer sbrsLayer: _layers){
            if (sbrsLayer.getParent().equals(scenarioLayer))
                return sbrsLayer;
        }
        return null;
    }

    /**
     * Find connection to reuse
     * @param src source SBRS layer
     * @param dst destination SBRS layer
     * @param duplicateId duplicate Id of the connection (see SBRS connection _duplicateId)
     * @return connection to reuse
     */
    public SBRSConnection findReuseConnection(SBRSLayer src, SBRSLayer dst, Integer duplicateId){
        for (SBRSConnection sbrsConnection: _connections){
            if (sbrsConnection.reusableForConnection(src, dst, duplicateId))
                return sbrsConnection;
        }
        return null;
    }

    /**
     * Find connection to reuse
     * @param src source SBRS layer
     * @param dst destination SBRS layer
     * @return connection to reuse
     */
    public SBRSConnection findFullyEquivalentConnection(SBRSLayer src, SBRSLayer dst){
        for (SBRSConnection sbrsConnection: _connections){
            if (sbrsConnection.getSrc() == src && sbrsConnection.getDst() == dst)
                return sbrsConnection;
        }
        return null;
    }

    /*****************************************************************/
    /****                 adaptive parameters checks            ****/
    public boolean isOpAdaptive(){
        boolean opParametrized = _adaptive.contains(ParameterName.OP);
        return opParametrized;
    }

    public boolean isHypAdaptive(){
        boolean hypParametrized = _adaptive.contains(ParameterName.HYP);
        return hypParametrized;
    }

    public boolean isParAdaptive(){
        boolean parParametrized = _adaptive.contains(ParameterName.PAR);
        return parParametrized;
    }

    public boolean isIOAdaptive(){
        return _adaptive.contains(ParameterName.I) ||
                _adaptive.contains(ParameterName.O);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Getters and setters                  ////

    /**
     * Get last connection added to the connections collection or null
     * @return last connection added to the connections collection or null
     */
    public SBRSConnection getLastConnection(){
        if (_connections.size() == 0) return null;
        return _connections.lastElement();
    }

    public Vector<SBRSConnection> getConnections() {
        return _connections;
    }

    public Vector<SBRSLayer> getLayers() {
        return _layers;
    }

    public Vector<ExecutionSequence> getExecutionSequences() {
        return _executionSequences;
    }

    /**
     * Get execution sequence for a given scenario
     * @param scenarioName scenario name
     * @return execution sequence for a given scenario or null (if scenario is not found)
     */
    public ExecutionSequence findExecutionSequence(String scenarioName){
        for(ExecutionSequence eSeq: _executionSequences){
            if(eSeq.getName().equals(scenarioName)){
                return eSeq;
            }
        }
    return null;
    }

    /**
     * Return the execution sequence, represented as list of layer ids:
     * every i-th execution step of the sequence is represented as
     * id n of n-th SBRS MoC layer, executed at the i-th step.
     * @param executionSequence execution sequence
     * @return scenario execution sequence, represented as list of layer ids:
     */
    public Vector<Integer> getExecSequenceAsLayerIdsList(ExecutionSequence executionSequence){
        Vector<Integer> execSeqAsLayerIdsList = new Vector<>();
        SBRSLayer layer;
        Integer layerId;
        for (ExecutionStep step: executionSequence.getExecutionSteps()){
            layer = step.getSbrsLayer();
            layerId = _layers.indexOf(layer);
            execSeqAsLayerIdsList.add(layerId);
        }
        return execSeqAsLayerIdsList;
    }

    /**************************************************
     **** Print
     *************************************************/

    public void printDetails(){
        System.out.println("Layers: ");
        for (SBRSLayer sbrslayer : _layers)
            sbrslayer.printDetails();

        System.out.println("Connections: ");
        for (SBRSConnection con: _connections)
            con.printDetails();
    }

    /**
     * Get names of all SBRS MoC scenarios
     * @return names of all SBRS MoC scenarios
     */
    public Vector<String> getScenarioNames(){
        Vector<String> scenarioNames = new Vector<>();
        String scenarioName;
        for (ExecutionSequence scenario: _executionSequences){
            scenarioName = scenario.getName();
            scenarioNames.add(scenarioName);
        }
        return scenarioNames;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /**Name of the Moc*/
    @SerializedName("name")private String _name;

    /** Layers*/
    @SerializedName("layers")private Vector<SBRSLayer> _layers;

    /** Connections between layers of the moc*/
    @SerializedName("connections")private Vector<SBRSConnection> _connections;

    /**List of all adaptive parameters*/
    @SerializedName("adaptive")private Vector<ParameterName> _adaptive;

    /** Execution sequences of SBRS MoC scenarios*/
    private transient Vector<ExecutionSequence> _executionSequences;
    private transient ControlNode controlNode;
}
