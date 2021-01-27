package espam.operations.transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.sbrs.SBRSMoC;
import espam.datamodel.graph.sbrs.control.ControlNode;
import espam.datamodel.graph.sbrs.supergraph.SBRSConnection;
import espam.datamodel.graph.sbrs.supergraph.SBRSLayer;
import espam.datamodel.graph.sbrs.supergraph.Supergraph;
import espam.datamodel.graph.sbrs.control.ExecutionSequence;
import espam.datamodel.graph.sbrs.control.ParameterName;

import java.util.Vector;

public class SBRSMoCBuilder {

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    public SBRSMoCBuilder(Vector<Network> scenarios, Vector<ParameterName> adaptiveParameters){
        _supergraph = new Supergraph(adaptiveParameters);
        _scenarios = scenarios;
        _setAutoZeroReuseMode(adaptiveParameters);
    }

    public SBRSMoC buildSBRSMoc(){
        _buildScenariosSupergraph();
        ControlNode cn = new ControlNode(_supergraph);
        SBRSMoC sbrsMoC = new SBRSMoC("sbrs", _supergraph, cn);
        return sbrsMoC;
    }

    private Supergraph _buildScenariosSupergraph(){
        for (Network scenario: _scenarios){
            _addScenario(scenario);
            _addExecutionSequence(scenario);
        }
        return _supergraph;
    }

    private void _addScenario(Network scenario){
        scenario.sortLayersInTraverseOrder();
        _addLayers(scenario);
        _addConnections(scenario);
    }

    /*****************************************************************/
    /****                        add layers                      ****/

    private void _addLayers(Network scenario){
        for (Layer layer: scenario.getLayers())
            _addLayer(layer);
    }

    private void _addLayer(Layer scenarioLayer){
        SBRSLayer reuseLayer = null;
        if (!_zeroReuseMode){
            reuseLayer = _supergraph.findReuseLayer(scenarioLayer);
            //System.out.println("Reused layer " + reuseLayer.getName() + "!");
        }

        if (reuseLayer == null)
            _supergraph.addNewLayer(scenarioLayer);
        else
            reuseLayer.reuseForLayer(scenarioLayer);
    }

    /*****************************************************************/
    /****                 add connections                        ****/
    private void _addConnections(Network scenario){
        for (Layer layer: scenario.getLayers()){
            _addLayerOutputConnections(layer);
        }
    }

    /**
     * Add layer output connections to SBRS MoC
     * NOTE: be careful with multiple-output layers, residual connections, etc.!!!
     * @param layer scenario layer
     */
    private void _addLayerOutputConnections(Layer layer){
        Vector<SBRSConnection> reusedConnections = new Vector<>();
        Layer scenarioSrc, scenarioDst;
        SBRSLayer sbrsSrc, sbrsDst;

        //determine scenario and SBRS source layer
        scenarioSrc = layer;
        sbrsSrc = _findSBRSMocLayer(scenarioSrc);

        //determine scenario and SBRS destination layer
        for (Connection scenarioOutputConnection : layer.getOutputConnections()) {
            scenarioDst = scenarioOutputConnection.getDest();
            sbrsDst = _findSBRSMocLayer(scenarioDst);
            _addOrReuseConnection(scenarioOutputConnection, sbrsSrc, sbrsDst, reusedConnections);
        }
    }

    private SBRSLayer _findSBRSMocLayer(Layer scenarioLayer){
        SBRSLayer sbrsMoCLayer;
        if (_zeroReuseMode)
            sbrsMoCLayer = _supergraph.findFullyEquivalentLayer(scenarioLayer);
        else
            sbrsMoCLayer = _supergraph.findReuseLayer(scenarioLayer);
        return sbrsMoCLayer;
    }


    private void _addOrReuseConnection(Connection scenarioOutputConnection,
                                       SBRSLayer sbrsSrc, SBRSLayer sbrsDst,
                                       Vector<SBRSConnection> reusedConnections){
        Integer duplicateId = 0;
        SBRSConnection connectionToReuse = null;

        if(_supergraph.isIOAdaptive() && !_zeroReuseMode)
            connectionToReuse = _supergraph.findReuseConnection(sbrsSrc, sbrsDst, duplicateId);

        // if there is no existing SBRS connection, able to represent the scenario connection,
        // add new connection
        if (connectionToReuse == null) {
            _supergraph.addNewConnection(scenarioOutputConnection, sbrsSrc, sbrsDst, duplicateId);
            reusedConnections.add(_supergraph.getLastConnection());
            return;
        }
        //else { System.out.println("Reused connection " + connectionToReuse.toString() + "!"); }

        // if there is an SBRS connection, able to represent the scenario connection,
        // and this connection is not yet used by current layer, reuse connection
        if (!reusedConnections.contains(connectionToReuse)) {
            connectionToReuse.reuseForConnection(scenarioOutputConnection);
            return;
        }

        // if there is an SBRS connection, able to represent the scenario connection,
        // but this connection is already used by current layer, find or add a connection duplicate
        if (reusedConnections.contains(connectionToReuse)) {
            //find required duplicate id
            for (SBRSConnection reusedCon : reusedConnections) {
                if (reusedCon.equals(connectionToReuse))
                    duplicateId = duplicateId + 1;
            }
            connectionToReuse = _supergraph.findReuseConnection(sbrsSrc, sbrsDst, duplicateId);
            if (connectionToReuse == null) {
                _supergraph.addNewConnection(scenarioOutputConnection, sbrsSrc, sbrsDst, duplicateId);
                reusedConnections.add(_supergraph.getLastConnection());
            }
            else
                connectionToReuse.reuseForConnection(scenarioOutputConnection);
            }
    }

    /*****************************************************************/
    /****                 add execution sequences                ****/

    private void _addExecutionSequence(Network scenario){
        Vector<ExecutionSequence> executionSequences = _supergraph.getExecutionSequences();
        Integer scenarioId = executionSequences.size();
        String scenarioName = "Scenario" + scenarioId.toString();
        ExecutionSequence es = new ExecutionSequence(scenarioName);
        SBRSLayer sbrsLayer;
        for (Layer layerInTraverseOrder: scenario.getLayers()){
            sbrsLayer = _supergraph.findReuseLayer(layerInTraverseOrder);
            es.addNextExecutionStep(sbrsLayer, layerInTraverseOrder);
        }
        executionSequences.add(es);
    }

    /**
     * Set zero-reuse mode flag
     * @param adaptiveParameters lust of adaptive parameters
     */
    private void _setAutoZeroReuseMode(Vector<ParameterName> adaptiveParameters){
        if (adaptiveParameters.size()==0)
            _zeroReuseMode = true;
        else
            _zeroReuseMode = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    private Vector<Network> _scenarios;
    private Supergraph _supergraph;
    /** zero-reuse flag. If zero-reuse flag is true, none of layers and connections
     * in the application scenarios are reused, a new SBRS layer/connection is created
     * for every scenario layer/connection */
    private boolean _zeroReuseMode = false;
}
