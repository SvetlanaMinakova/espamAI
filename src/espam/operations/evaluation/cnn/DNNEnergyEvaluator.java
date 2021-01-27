package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.cnn.benchmarkbased.DNNComponentsEnergyEvaluatorBenchmark;
import espam.operations.evaluation.cnn.roofline.DNNComponentsEnergyEvaluatorRoofline;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.scheduler.dnnScheduler.layerFiring;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DNNEnergyEvaluator {

    /**
     * Get DNN memory evaluator instance
     * @return DNN memory evaluator instance
     */
    public static DNNEnergyEvaluator getInstance() {
        return _energyEvaluator;
    }

    /**
     * Evaluate DNN energy
     * @param dnn DNN
     * @param evalResult evaluation result variable
     * @param platformDescription platform evaluation (based on measurements)
     * @param schedule DNN layers execution schedule
     * @param mapping DNN layers and connections mapping
     * @param watt if evaluation needs to be provided in watt (otherwise it will be provided in joules)
     */
    public void evaluateEnergy(Network dnn, CSDFEvalResult evalResult, PlatformDescription platformDescription, Vector<Vector<layerFiring>> schedule,
                               Mapping mapping, boolean watt, EvaluatorAlg evaluatorAlg){
        _dnn = dnn;
        _platformDescription = platformDescription;
        _schedule = schedule;
        _mapping = mapping;
        //check if energy evaluation can be performed with current parameters
        if(!_checkParam())
            return;

        _annotateDNNComponents(evaluatorAlg);

        Double totalEnergy;
        if(watt)
           totalEnergy = _evaluateEnergyWatt();
        else totalEnergy = _evaluateEnergyJoules();

        /**annotate DNN and evalution result with total DNN energy*/
        evalResult.setEnergy(totalEnergy);
        dnn.set_energyEval(totalEnergy);
    }

    /**
     * Annotate dnn components
     * @param evaluatorAlg algorithm to evaluate dnn components
     */
    private void _annotateDNNComponents(EvaluatorAlg evaluatorAlg){
        DNNComponentsEnergyEvaluator energyEvaluator;
        switch (evaluatorAlg) {
            case ROOFLINE: {
                energyEvaluator = new DNNComponentsEnergyEvaluatorRoofline(_dnn, _platformDescription, _mapping);
                break;
            }
            default:{
                energyEvaluator = new DNNComponentsEnergyEvaluatorBenchmark(_dnn, _platformDescription, _mapping);
                break;
            }
        }
        energyEvaluator.annotate();
    }

    /**
     * Evaluate total DNN energy in watt
     * @return total DNN energy in watt or 0.0
     */
    private Double _evaluateEnergyWatt(){
        Vector<Double> energyPerStep = new Vector<>();
        Double stepEnergy;

        /**find execution time for every sequential computational step in schedule*/
        for(Vector<layerFiring> execStep: _schedule) {
            // evaluate energy for i-th element = i-th sequential computation step
            stepEnergy = _getWattPerStep(execStep);
            energyPerStep.add(stepEnergy);
        }

        /**total DNN energy = peak energy among all steps, executed sequentially*/
        Double peakEnergy = 0.0;
        for(Double energy: energyPerStep) {
            if(energy > peakEnergy)
                peakEnergy = energy;
        }

        return peakEnergy;

    }

    /**
     * Get energy evaluation for DNN in Joules
     * @return energy evaluation or 0.0
     */
    private Double _evaluateEnergyJoules(){
        Vector<Double> energyPerStep;
        Vector<Double> procEnergyPerStep;
        Vector<MProcessor> procPerStep;
        Integer curProcId;
        Double energy, layerEnergy;
        Double sumEnergy = 0.0;
        try{

            /** for every execution step*/
            for(Vector<layerFiring> execStep: _schedule) {
                energyPerStep = new Vector<>();
                procPerStep = new Vector<>();
                procEnergyPerStep = new Vector<>();//processor energy per execution step

                /**evaluate execution step. All processors on different execution steps
                 * work in sequence. All processors within one execution step work in parallel.*/
                for(layerFiring lf: execStep){

                    for(MProcessor proc: lf.getProcessors()){ //query all processors, working at this exec. step
                        if (!procPerStep.contains(proc)) {
                            procEnergyPerStep.add(0.0);
                            procPerStep.add(proc);
                        }

                        curProcId = procPerStep.indexOf(proc);

                        energy = procEnergyPerStep.elementAt(curProcId);
                        layerEnergy = lf.getLayer().get_energyEvalJoules();
                        // add layer energy to energy per step
                        energy += layerEnergy;
                        // update energy per step
                        procEnergyPerStep.setElementAt(energy,curProcId);
                    }

                    //energy per step = sum of energy of all processors, executed in parallel on this step
                    Double sumEnergyPerStep = 0.0;
                    for (Double procEnergy: procEnergyPerStep) {
                        sumEnergyPerStep += procEnergy;
                    }

                    energyPerStep.add(sumEnergyPerStep);
                }

                // total energy = sum of energy of all processor at all steps
                for(Double stepEnergy: energyPerStep) {
                    sumEnergy += stepEnergy;
                }
            }

            return sumEnergy;
        }
        catch (Exception e){
            System.err.println("DNN-CSDF energy evaluation error: " + e.getMessage());
            return 0.0;
        }
    }

    /**evaluate energy, required for an execution step. All layers within one execution step are executed
     *  in parallel, if mapped on different processors. Time, required for an execution step is determined by
     *  the bottleneck processor*/
    private Double _getWattPerStep(Vector<layerFiring> execStep) {
        HashMap<MProcessor, Double> procEnergyPerAllLayersFiring = _getProcWattPerAllLayersFiring(execStep);
        //energy = sum of energies for all processors, working in parallel
        Double totalEnergy = 0.0;
        for (Double procEN: procEnergyPerAllLayersFiring.values()) {
            totalEnergy += procEN;
        }

        return totalEnergy;
    }

    /**evaluate time, required all processors for firing of all layers, mapped on these processors
     * within one execution step
     * @param execStep execution step
     * @return time, required all processors for firing of all layers, mapped on these processors
     * within one execution step
     */
    private HashMap<MProcessor, Double> _getProcWattPerAllLayersFiring(Vector<layerFiring> execStep){
        HashMap<MProcessor, Double> totalProcExecTime = new HashMap<>(); //time, required by all processors to execute all layers within a computational step
        HashMap<MProcessor, Double> procExecTimePerLayer;//time, required by all processors to execute one layer within a computational step
        Double oldTime, newTime;
        for(layerFiring lf: execStep){
            procExecTimePerLayer = _getProcWattPerLayerFiring(lf);
            for(Map.Entry<MProcessor,Double> procTime: procExecTimePerLayer.entrySet()) {
                if (!totalProcExecTime.containsKey(procTime.getKey())) {
                    totalProcExecTime.put(procTime.getKey(), procTime.getValue());//add new record
                }

                else {//replace record
                    oldTime = totalProcExecTime.get(procTime.getKey());
                    newTime =  Math.max(oldTime, procTime.getValue()); //replace by max energy
                    totalProcExecTime.replace(procTime.getKey(), oldTime, newTime);
                }
            }
        }

        return totalProcExecTime;
    }

    /**evaluate energy, required by multiple processors for firing of one layer within one execution step*/
    private HashMap<MProcessor,Double> _getProcWattPerLayerFiring(layerFiring lf) {
        HashMap<MProcessor, Double> processorsEnergy = new HashMap<>();

        Double procEnergy, procEnergyUpd, layerEnergy;
        Double layerEnergyTime = 0.0;

        Layer curLayer = lf.getLayer(); //layer, executed at current layer firing
        for(MProcessor proc: lf.getProcessors()){ //query all processors, executing current layer firing
            if (!processorsEnergy.keySet().contains(proc)) {
                processorsEnergy.put(proc,0.0);
            }

            procEnergy = processorsEnergy.get(proc);
            layerEnergy = curLayer.get_energyEval();

            // update processor energy = max of all energy
            procEnergyUpd = Math.max(procEnergy, layerEnergy);
            processorsEnergy.replace(proc, procEnergy, procEnergyUpd);
        }

        return processorsEnergy;
    }

    /** check all parameters, required for energy evaluation*/
    private boolean _checkParam(){
        if(_dnn==null){
            System.err.println("Energy eval error: dnn = null!");
            return false;
        }

        if(_dnn.getLayers()==null){
            System.err.println("Energy eval error: dnn.layers = null!");
            return false;
        }

        if(_dnn.getConnections()==null){
            System.err.println("Energy eval error: dnn.connections = null!");
            return false;
        }

        if(_mapping==null){
            System.err.println("Energy eval error: mapping = null!");
            return false;
        }

        if(_platformDescription ==null){
            System.err.println("Energy eval error: platform eval = null!");
        }

        if(_schedule==null){
            System.err.println("Energy eval error: schedule = null!");
        }

        return true;
    }

    /** singletone*/
    protected DNNEnergyEvaluator() { };
    private static DNNEnergyEvaluator _energyEvaluator = new DNNEnergyEvaluator();

    /**private variables*/
    private Network _dnn;
    private PlatformDescription _platformDescription;
    private Vector<Vector<layerFiring>> _schedule;
    private Mapping _mapping;
}
