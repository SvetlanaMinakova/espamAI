package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.MeasurementUnitsConverter;
import espam.operations.evaluation.cnn.benchmarkbased.BenchmarkHelper;
import espam.operations.evaluation.platformDescription.OpBenchmark;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;
import espam.operations.evaluation.platformDescription.PlatformDescription;

/***
 * Class annotates every layer in the DNN
 * with platform-aware energy evaluation
 */
public abstract class DNNComponentsEnergyEvaluator {

    /**
     * Create new dnn components energy evaluator
     * @param dnn dnn
     * @param platformDescription description of platform, on which dnn is executed
     * @param mapping mapping of dnn onto platform
     */
    public DNNComponentsEnergyEvaluator(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        _dnn = dnn;
        _platformDescription = platformDescription;
        _mapping = mapping;
    }

    /** annotate every node and connection in DNN with amount of
     * energy consumed per DNN  input sample
     */
    public void annotate(){
        _annotateDNNLayersWithEnergy();
        _annotateDNNConnectionsWithEnergy();
    }

    /////////////////////////////////////////////////////////////////////
    ////                      layers evaluation                     ////

    /** annotate DNN layers with power (Watts)/energy(Joules) evalution*/
    protected void _annotateDNNLayersWithEnergy(){
        MProcessor proc;
        for(Layer layer: _dnn.getLayers()){
            proc = _mapping.tryFindProcessorForTask(layer.getName());
            _annotateDNNLayerWithEnergy(layer, proc);
        }
    }

    /**
     * annotate DNN layers with power (Watts)/energy(Joules) evalution
     * @param layer layer
     * @param proc processor, on which layer is executed
     */
    protected void _annotateDNNLayerWithEnergy(Layer layer, MProcessor proc){
        try{
            Double energyWatt, energyJoules;
            energyWatt = _evaluateLayerEnergyWatt(layer, proc);
            layer.set_energyEval(energyWatt);
            energyJoules = _toJoules(energyWatt,layer.get_timeEval());
            layer.set_energyEvalJoules(energyJoules);
        }
        catch (Exception e){
            System.err.println("DNN-CSDF energy annotation error for layer " + layer.getName() + ": "+ e.getMessage());
        }
    }

    /**Evaluate DNN layer energy in Watt*/
    protected Double _evaluateLayerEnergyWatt(Layer layer, MProcessor processor){
        Double energy;

        String procName = processor.getName();
        ProcTypeDescription procTypeDescription = _platformDescription.getProcTypeDescription(procName);

        Operator op = layer.getNeuron().getOperator();
        if(op==null) layer.initOperator();

        energy = getOperatorEnergyEvalWatt(procTypeDescription, op);
        return energy;
    }


    /**
     * Evaluate operator energy in watt
     * @param op Operator evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    public abstract double getOperatorEnergyEvalWatt(ProcTypeDescription procTypeDescription, Operator op);

    /////////////////////////////////////////////////////////////////////
    ////                   connections evaluation                   ////

    /** annotate DNN connections with power (Watts)/energy(Joules) evalution*/
    protected void _annotateDNNConnectionsWithEnergy(){
        for(Layer layer: _dnn.getLayers()) {
            for (Connection con : layer.getOutputConnections()) {
                _annotateDNNConnectionWithEnergy(con);
            }
        }
    }

    /**
     * annotate DNN layers with power (Watts)/energy(Joules) evalution
     * @param con connection
     */
    protected void _annotateDNNConnectionWithEnergy(Connection con){
        MProcessor src, dst;
        Double energyWatt, energyJoules;
        src = _mapping.tryFindProcessorForTask(con.getSrc().getName());
        dst = _mapping.tryFindProcessorForTask(con.getDest().getName());

        if (src==null || dst==null){
            //System.err.println("connection " + con.getSrcName() + " --> " + con.getDestName() + " eval error: src or dst processor not found");
            return;
        }

        try{
            energyWatt = 0.0;//_evaluateEnergyWatt(layer, proc);
            con.set_energyEval(energyWatt);
            energyJoules = _toJoules(energyWatt,con.get_timeEval());
            con.set_energyEvalJoules(energyJoules);
        }
        catch (Exception e){
            System.err.println("DNN-CSDF energy annotation error for connection" + con.getSrcName() + " --> " + con.getDestName() + ": " + e.getMessage());
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////                         protected methods                  ////


    /** Computes energy in Joules
     * @param energyWatt layer/connection energy evaluation in Watts
     * @param timeMS layer/connection execution time in milliseconds (10^(-3) seconds)
     * @return energy in Joules
     */
    protected Double _toJoules(Double energyWatt, Double timeMS){
        Long secondToMS = MeasurementUnitsConverter.secToMs();
        Double joules = (energyWatt * timeMS)/secondToMS.doubleValue();
        return joules;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         protected variables                   ////
    /**protected variables*/
    protected Network _dnn;
    protected PlatformDescription _platformDescription;
    protected Mapping _mapping;
}
