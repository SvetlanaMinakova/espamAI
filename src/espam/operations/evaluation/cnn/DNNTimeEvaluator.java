package espam.operations.evaluation.cnn;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.cnn.benchmarkbased.DNNComponentsTimeEvaluatorBenchmark;
import espam.operations.evaluation.cnn.roofline.DNNComponentsTimeEvaluatorRoofline;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.scheduler.dnnScheduler.layerFiring;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Class evaluates DNN execution time with respect to specific schedule
 * of the DNN layers
 */

public class DNNTimeEvaluator {

    public static DNNTimeEvaluator getInstance() {
        return _evaluator;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Evaluate DNN memory and write the result into an evaluation result variable
     * @param dnn dnn
     * @param evalResul evaluation result variable, storing platform-aware eval
     * @param platformDescription platform evaluation: description of platform and supported operators
     * @param schedule dnn schedule: order of layers execution
     * @param mapping mapping of dnn onto platform
     * @param evaluatorAlg algorithm/model to evaluate time
     */
    public void evaluateTime(Network dnn, CSDFEvalResult evalResul, PlatformDescription platformDescription,
                             Vector<Vector<layerFiring>> schedule, Mapping mapping, EvaluatorAlg evaluatorAlg) {
            Double time = evaluateTime(dnn, platformDescription, schedule, mapping, evaluatorAlg);
            evalResul.setPerformance(time);
    }

    /**
     * Evaluate DNN memory
     * @param dnn dnn
     * @param platformDescription platform evaluation: description of platform and supported operators
     * @param schedule dnn schedule: order of layers execution
     * @param mapping mapping of dnn onto platform
     * @param evaluatorAlg algorithm/model to evaluate time
     */
    public Double evaluateTime(Network dnn, PlatformDescription platformDescription,
                               Vector<Vector<layerFiring>> schedule, Mapping mapping,
                               EvaluatorAlg evaluatorAlg){
        try{
            _annotateDNN(dnn, platformDescription, mapping,  evaluatorAlg);
            _schedule = schedule;
            Double totalExecTime = _evaluateTotalExecTime();
            dnn.set_timeEval(totalExecTime);
            return totalExecTime;
        }
        catch (Exception e){
            System.err.println("DNN time evaluation error: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Annotate dnn layers and connections with time evaluation
     * @param dnn dnn
     * @param mapping dnn mapping
     * @param evaluatorAlg algorithm/model to evaluate time
     */
     private void _annotateDNN(Network dnn, PlatformDescription platformDescription, Mapping mapping, EvaluatorAlg evaluatorAlg){
         DNNComponentsTimeEvaluator evaluator;
         switch (evaluatorAlg){
             case ROOFLINE:{
                 evaluator = new DNNComponentsTimeEvaluatorRoofline(dnn, platformDescription, mapping);
                 break;
             }
             default:{
                 evaluator = new DNNComponentsTimeEvaluatorBenchmark(dnn, platformDescription, mapping);
                 break;
             }
         }
         evaluator.annotate();
     }


    /**
     * Evaluate total DNN execution time
     * @return total DNN execution time
     */
     private Double _evaluateTotalExecTime(){
         Vector<Double>timesPerStep = new Vector<>();
         Double stepTime;
         /**find execution time for every sequential computational step in schedule*/
         for(Vector<layerFiring> execStep: _schedule) {
             stepTime = _getTimePerStep(execStep);// i-th element = i-th sequential computation step
             timesPerStep.add(stepTime);
         }

         /**total DNN time = sum of times per step, where all steps are executed sequentially*/
         Double totalExecTime = 0.0;
         for(Double time: timesPerStep)
             totalExecTime += time;

         return totalExecTime;

     }

    /**evaluate time, required for an execution step. All layers within one execution step are executed
     *  in parallel, if mapped on different processors. Time, required for an execution step is determined by
     *  the bottleneck processor*/
    private Double _getTimePerStep(Vector<layerFiring> execStep) {
        HashMap<MProcessor, Double> procTimePerAllLayersFiring = _getProcTimePerAllLayersFiring(execStep);

        //find bottleneck processor
        Double maxTimePerStep = 0.0;
        for (Double procTime: procTimePerAllLayersFiring.values()) {
            if (procTime > maxTimePerStep)
                maxTimePerStep = procTime;
        }

        return maxTimePerStep;
    }


    /**evaluate time, required all processors for firing of all layers, mapped on these processors
     * within one execution step
     * @param execStep execution step
     * @return time, required all processors for firing of all layers, mapped on these processors
     * within one execution step
     */
    private HashMap<MProcessor, Double> _getProcTimePerAllLayersFiring(Vector<layerFiring> execStep){
        HashMap<MProcessor, Double> totalProcExecTime = new HashMap<>(); //time, required by all processors to execute all layers within a computational step
        HashMap<MProcessor, Double> procExecTimePerLayer;//time, required by all processors to execute one layer within a computational step
        Double oldTime, newTime;
        for(layerFiring lf: execStep){
            procExecTimePerLayer = _getProcTimePerLayerFiring(lf);
            for(Map.Entry<MProcessor,Double> procTime: procExecTimePerLayer.entrySet()) {
                if (!totalProcExecTime.containsKey(procTime.getKey())) {
                    totalProcExecTime.put(procTime.getKey(), procTime.getValue());//add new record
                }

                else {//replace record
                    oldTime = totalProcExecTime.get(procTime.getKey());
                    newTime =  oldTime + procTime.getValue();
                    totalProcExecTime.replace(procTime.getKey(), oldTime, newTime);
                }
            }
        }

        return totalProcExecTime;
    }


    /**evaluate time, required multiple processors for firing of one layer within one execution step*/
    private HashMap<MProcessor,Double> _getProcTimePerLayerFiring(layerFiring lf) {
        HashMap<MProcessor, Double> procExecTime = new HashMap<>();
        Double procTime, procTimeUpd, layerTime;
        Double workloadShare;

        Layer curLayer = lf.getLayer(); //layer, executed at current layer firing
        for(MProcessor proc: lf.getProcessors()){ //query all processors, executing current layer firing
            if (!procExecTime.keySet().contains(proc)) {
                procExecTime.put(proc,0.0);
            }

            procTime = procExecTime.get(proc);
            workloadShare = lf.getWorkloadPercentage(proc);
            layerTime = _getLayerTime(curLayer, workloadShare);

            // update processor execution time
            procTimeUpd = procTime + layerTime;
            procExecTime.replace(proc, procTime, procTimeUpd);
        }

        return procExecTime;
    }

    /**
     * Get time, required to execute workloadShare % of DNN layer
     * @param layer DNN layer
     * @param workloadShare workload share
     * @return  time, required to execute workloadShare % of DNN layer
     */
    private Double _getLayerTime(Layer layer, Double workloadShare){
        Double layerTime =  layer.get_timeEval()  * workloadShare;
        return  layerTime;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /** evaluator singletone*/
    private static DNNTimeEvaluator _evaluator = new DNNTimeEvaluator();
    private Vector<Vector<layerFiring>> _schedule;
}