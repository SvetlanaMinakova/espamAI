package espam.datamodel.graph.sbrs;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.sbrs.control.ControlNode;
import espam.datamodel.graph.sbrs.control.ExecutionSequence;
import espam.datamodel.graph.sbrs.control.ExecutionStep;
import espam.datamodel.graph.sbrs.supergraph.SBRSConnection;
import espam.datamodel.graph.sbrs.supergraph.SBRSLayer;
import espam.datamodel.graph.sbrs.supergraph.Supergraph;

import java.util.Vector;

public class SBRSMoC {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public SBRSMoC(String name, Supergraph supergraph, ControlNode controlNode){
        this.name = name;
        this.supergraph = supergraph;
        this.controlNode = controlNode;
    }

    /**
     * Check buffers consistency for given scenario
     * @param scenario  scenario, represented as a DNN
     * @param scenarioId representation of scenario in the SBRS MoC execution sequence
     * @return true, if buffers are consistent and false otherwise
     */
    public boolean checkBuffersConsistency(Network scenario, Integer scenarioId, boolean printInconsistent){
        if (scenarioId<0 || scenarioId>supergraph.getExecutionSequences().size()){
            System.err.println("Buffers consistency chek error: wrong scenarioId " + scenarioId);
            return false;
        }
        ExecutionSequence eSeq = supergraph.getExecutionSequences().elementAt(scenarioId);
        if (eSeq.countExecutionSteps()!=scenario.countLayers()){
            System.err.println("Buffers consistency checkout error: scenario execution sequence has "
                    + eSeq.countExecutionSteps() + " steps, while " + scenario.countLayers() + " expected.");
            return false;
        }

        Boolean consistent = true;
        scenario.sortLayersInTraverseOrder();
        Vector<Layer> scenarioLayers = scenario.getLayers();

        for(Integer stepId = 0; stepId < eSeq.countExecutionSteps(); stepId++){
            Layer scenarioLayer = scenarioLayers.elementAt(stepId);
            ExecutionStep executionStep = eSeq.getExecutionStep(stepId);
            boolean stepConsistent = _checkBuffersConsistencyForExecStep(scenarioLayer, executionStep, printInconsistent);
            if(!stepConsistent)
                consistent = false;
        }

        return consistent;
    }

    /////////////////////////////////////////////////////////////////////
    ////                          private methods                   ////
    /**
     * Check buffer consistency of the SBRS MoC execution step, executing scenario layer
     * @param scenarioLayer scenario layer
     * @param execStep SBRS MoC execution step
     * @param printInconsistent print details, if buffers are inconsistent at thos step
     * @return true, if buffers are consistent at this step and false otherwise
     */
    private boolean _checkBuffersConsistencyForExecStep(Layer scenarioLayer,
                                                        ExecutionStep execStep,
                                                        boolean printInconsistent){
        Integer totalChannelsRequired = 0, totalChannelsAvailable = 0;
        Integer totalTokensToStore = 0, totalTokensStorageSize = 0;


        //check DNN requirements
        for(Connection c: scenarioLayer.getOutputConnections()){
            totalChannelsRequired++;
            totalTokensToStore = totalTokensToStore + c.getDest().getOutputFormat().getElementsNumber();
        }

        //check what happens during the execution step
        Vector<SBRSConnection> sbrsDistinctConnectionsAtExecStep = new Vector<>();
        SBRSLayer sbrsLayerSRC = execStep.getSbrsLayer();

        for(Connection c: scenarioLayer.getOutputConnections()){
            SBRSLayer sbrsLayerDst = supergraph.findReuseLayer(c.getDest());
            SBRSConnection sbrsC = supergraph.findFullyEquivalentConnection(sbrsLayerSRC, sbrsLayerDst);
            totalTokensStorageSize =  totalTokensStorageSize + sbrsC.getBufferSize().intValue();
            if (!sbrsDistinctConnectionsAtExecStep.contains(sbrsC))
                sbrsDistinctConnectionsAtExecStep.add(sbrsC);
        }

        totalChannelsAvailable  = sbrsDistinctConnectionsAtExecStep.size();

        boolean consistent =  totalChannelsRequired == totalChannelsAvailable &&
                totalTokensToStore <= totalTokensStorageSize;

        if(printInconsistent){
            if (!consistent){
                System.out.println("Inconsistent output buffers for scenario Layer " + scenarioLayer.getName());
                System.out.println("Parallel channels required: " + totalChannelsRequired + ", available: " + totalChannelsAvailable);
                System.out.println("Storage (tokens) required: " + totalTokensToStore + ", available: " + totalChannelsAvailable);
            }
        }

        return consistent;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         getters and setters                ////

    /////////////////////////////////////////////////////////////////////
    ////                         public variables                   ////
    public String name;
    public Supergraph supergraph;
    public ControlNode controlNode;
}
