package espam.operations.scheduler.dnnScheduler;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;
import espam.datamodel.mapping.DNNMapping.MappingGenerator;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.processors.Processor;
import espam.operations.evaluation.CoreTypeEval;
import espam.operations.evaluation.PlatformEval;

import java.util.HashMap;
import java.util.Vector;

public class dnnScheduler {

    public static  Vector<Vector<layerFiring>> generateDNNSchedule(Network dnn, Mapping mapping, DNN_MAPPING_TYPE mappingType, PlatformEval plaEval) {
        Vector<Vector<layerFiring>> schedule = new Vector<>();

        if (dnn == null) {
            System.err.println("Auto-schedule generation error: null-DNN!");
            return schedule;
        }

        if (mapping == null) {
            System.err.println("Auto-schedule generation error: null-Mapping!");
            return schedule;
        }

        switch (mappingType){

            case PIPELINE: {
                schedule = _generatePipelineSchedule(dnn, mapping);
                return schedule;
            }

            default:{
                schedule = _generateSequentialSchedule(dnn, mapping, plaEval);
                return schedule;
            }
        }
    }

    /** generate sequential schedule, where every layer is executed sequentially, but can be distributed on several processors*/
    private static Vector<Vector<layerFiring>> _generateSequentialSchedule(Network dnn, Mapping mapping, PlatformEval plaEval){

        Vector<Vector<layerFiring>> schedule = new Vector<>();
        try {
            Vector<MProcessor> cpuList = mapping.getCPUList();
            Vector<MProcessor> toShareWorkload = cpuList; //TODO: check, if operator is supported there??
            boolean shareWorkload = true;
            if(toShareWorkload.size()==0) {
                System.out.println("no cpus to share workload found, every layer is executed on one CPU");
            }

            DNN_MAPPING_TYPE mt = DNN_MAPPING_TYPE.SEQUENTIAL;
            MProcessor proc;


            for(Layer l: dnn.getLayers()) {
                Vector<layerFiring> scheduleStep = new Vector<>();
                proc = mapping.findProcessorForTask(l.getName());
                if(proc.isCPU() && shareWorkload) {
                    Integer totalNeurons = l.getNeuronsNum();
                    if (l.getNeuron() instanceof DenseBlock)
                        totalNeurons = ((DenseBlock) l.getNeuron()).getNeuronsNum();

                    layerFiring lf = _getlayerFiringShared(l, plaEval, proc, toShareWorkload, totalNeurons);
                    scheduleStep.add(lf);
                }
                else {
                    layerFiring lf = _getLayerFiringNonShared(l,proc);
                    scheduleStep.add(lf);
                }

                schedule.add(scheduleStep);
            }

            return schedule;

        }
        catch (Exception e){System.err.println("sequential schedule generation error "+ e.getMessage());
        }

        return schedule;
    }

    /**generate pipeline schedule, where all available processors work in parallel,
     * and all layers are executed as parallel as possible*/
    private static Vector<Vector<layerFiring>> _generatePipelineSchedule(Network dnn, Mapping mapping){

        Vector<Vector<layerFiring>> schedule = new Vector<>();
        Vector<layerFiring> scheduleStep = new Vector<>();
        try {
            MProcessor proc;

            for(Layer l: dnn.getLayers()) {
                proc = mapping.findProcessorForTask(l.getName());

                layerFiring lf = _getLayerFiringNonShared(l,proc);
                scheduleStep.add(lf);
            }

            schedule.add(scheduleStep);
            return schedule;

        }
        catch (Exception e){System.err.println("pipeline schedule generation error "+ e.getMessage());
        }

        return schedule;
    }

    /** execute layer on processor
     * @param layer layer
     * @param processor processor
     * @return firing, describing execution of layer on a processor
     */
    private static layerFiring _getLayerFiringNonShared(Layer layer, MProcessor processor){
        layerFiring firing = new layerFiring();
        firing.setLayer(layer);

        firing.addProcessor(processor);
        firing.addWorkloadPercentage(1.0);
        return firing;

    }

    /** execute layer on multiple processors, with workload, evenly shared between the processors
     * @param layer layer
     * @param originalProcessor a single processor to execute layer, if
     * @return firing, describing execution of layer on a processor
     */
    private static layerFiring _getlayerFiringShared(Layer layer, PlatformEval plaEval, MProcessor originalProcessor, Vector<MProcessor> toShareWorkload, Integer totalNeurons){

        /** workload cannot be shared among less then 2 neurons*/
        if(totalNeurons < 2)
            return _getLayerFiringNonShared(layer, originalProcessor);


        layerFiring firing = new layerFiring();
        firing.setLayer(layer);


        Vector<Integer> neuronShares = new Vector<>();
        Vector<Double> timeEvals = new Vector<>();
        Vector<Double> workloadSharesPreliminaty = new Vector<>();//WCET-based preliminary evaluation
        Double time;
        Double totalTime = 0.0;
        Double share;
        Integer neuronsNumShare;
        Double neuronsNumDShare;
        Integer neuronsAssigned = 0;


        for(MProcessor mproc: toShareWorkload){
            time = _evaluateTime(layer,mproc,plaEval);
            timeEvals.add(time);
            totalTime+=time;
        }

        for(Double timeEval:timeEvals){
            share = timeEval/totalTime;
            workloadSharesPreliminaty.add(share);
        }

        for(Double neuronShare:workloadSharesPreliminaty){
            neuronsNumDShare = neuronShare * (double)totalNeurons;
            neuronsNumShare = neuronsNumDShare.intValue();
            neuronShares.add(neuronsNumShare);
            neuronsAssigned+=neuronsNumShare;
        }

        if(neuronsAssigned!=totalNeurons){
            Integer dif = totalNeurons - neuronsAssigned;
            Integer maxShareId = findMaxShareId(neuronShares);
            Integer neuronsShareWithTail = neuronShares.elementAt(maxShareId) + dif;
            neuronShares.setElementAt(neuronsShareWithTail,maxShareId);
        }

        /** check for null-share*/
        Integer procId = 0;
        MProcessor proc;
        for(Integer neurons:neuronShares){
            share = (double)neurons/(double)totalNeurons;
            if(share>0){
                proc = toShareWorkload.elementAt(procId);
                firing.addProcessor(proc);
                firing.addWorkloadPercentage(share);
            }
            procId++;

        }

        return firing;
    }

    /** find id of share with max neurons assigned*/
    private static Integer findMaxShareId(Vector<Integer> neuronShares){
        Integer maxShareId = 0;
        Integer curId =0;
        Integer maxShare = neuronShares.firstElement();
        for(Integer share : neuronShares){
            if(share>maxShare){
                maxShare = share;
                maxShareId = curId;
            }
            curId++;
        }

        return maxShareId;

    }

    /**Evaluate DNN layer execution time*/
    private static  Double _evaluateTime(Layer layer, MProcessor processor, PlatformEval plaEval){
        Double time = 0.0;
        String procName = processor.getName();
        CoreTypeEval coreTypeEval = plaEval.getCoreTypeEval(procName);

        Operator op = layer.getNeuron().getOperator();
        if(op==null)
            layer.initOperator();
        time =  coreTypeEval.getOperatorTimeEval(op);
        //System.out.println("time of "+layer.getName() + " on "+ procName + " = " + time);
        return time;
    }
}
