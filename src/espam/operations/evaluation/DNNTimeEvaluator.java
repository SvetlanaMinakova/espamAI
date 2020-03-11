package espam.operations.evaluation;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.scheduler.dnnScheduler.layerFiring;

import java.util.Vector;

public class DNNTimeEvaluator {

    public static DNNTimeEvaluator getInstance(){
        return _refiner;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Refine memory evaluation, provided by DARTS
     * @param evalResul DARTS evaluation of CSDF graph
     */
    public void evaluateTime(Network dnn, CSDFEvalResult evalResul, PlatformEval platformEval, Vector<Vector<layerFiring>> schedule){
        try{
            _schedule = schedule;
            _platformEval = platformEval;

            Vector<Double>timesPerStep;
            Vector<Double> procTimePerStep;
            Vector<MProcessor> procPerStep;
            Integer curProcId;
            Double time, curTime, layerTime;
            Double totalExecTime = 0.0;
            Double workloadShare;

            /** for every execution step*/
            for(Vector<layerFiring> execStep: schedule) {
                timesPerStep = new Vector<>();
                procPerStep = new Vector<>();

                /**evaluate execution step. All processors on different execution steps
                 * work in sequence. All processors within one execution step work in parallel.*/
                for(layerFiring lf: execStep){
                    procTimePerStep = new Vector<>();//processor time per execution step
                    for(MProcessor proc: lf.getProcessors()){ //query all processors, working at this exec. step
                        if (!procPerStep.contains(proc)) {
                            procTimePerStep.add(0.0);
                            procPerStep.add(proc);
                        }

                        curProcId = procPerStep.indexOf(proc);
                        time = procTimePerStep.elementAt(curProcId);

                        workloadShare = lf.getWorkloadPercentage(proc);
                        curTime = _evaluateTime(lf.getLayer(),proc) * workloadShare;

                        //update layer execution time
                        layerTime = lf.getLayer().get_timeEval();
                        layerTime+=curTime;
                        lf.getLayer().set_timeEval(layerTime);

                        curTime +=time;
                        procTimePerStep.setElementAt(curTime,curProcId);// processor execution time per this step


                        }

                    //find bottleneck processor
                    Double maxTimePerStep = 0.0;
                    for (Double procTime: procTimePerStep) {
                        if (procTime > maxTimePerStep)
                            maxTimePerStep = procTime;
                        }
                    //set time of the step = bottleneck processor execution time
                    timesPerStep.add(maxTimePerStep);
                        lf.getLayer().set_timeEval(maxTimePerStep);


                    }

                for(Double stepTime: timesPerStep)
                    totalExecTime+=stepTime;
                }

            evalResul.setPerformance(totalExecTime);
            dnn.set_timeEval(totalExecTime);

        }
        catch (Exception e){
            System.err.println("DNN-CSDF time evaluation error: " + e.getMessage());
        }
    }


    /**Evaluate DNN layer execution time*/
    private Double _evaluateTime(Layer layer, MProcessor processor){
        Double time;
        String procName = processor.getName();
        CoreTypeEval coreTypeEval = _platformEval.getCoreTypeEval(procName);

        Operator op = layer.getNeuron().getOperator();
        if(op==null)
            layer.initOperator();
        time =  coreTypeEval.getOperatorTimeEval(op);
        if(_verbose)
            System.out.println(layer.getName() + " " + time + " ms");
        return time;
    }


    public void setPlatformEval(PlatformEval _platformEval) {
        this._platformEval = _platformEval;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /** refiner singletone*/
    private static DNNTimeEvaluator _refiner = new DNNTimeEvaluator();

    private PlatformEval _platformEval;
    private Mapping _mapping;
    private DNN_MAPPING_TYPE AutoMappingType;
    private boolean _verbose = false;
    private Vector<Vector<layerFiring>> _schedule;

}
