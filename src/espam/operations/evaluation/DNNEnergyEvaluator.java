package espam.operations.evaluation;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.processors.GPU;
import espam.datamodel.platform.processors.HWCE;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.pn.Process;
import espam.operations.scheduler.dnnScheduler.layerFiring;

import java.util.HashMap;
import java.util.Vector;

public class DNNEnergyEvaluator {

    /**
     * Get energy evaluation for DNN
     * @param dnn Deep Neural Network
     * @return refined energy evaluation for CSDF graph or null
     */
    public static void evaluateEnergy(Network dnn, CSDFEvalResult evalResult, PlatformEval platformEval, Vector<Vector<layerFiring>> schedule){

        Vector<Double> energyPerStep;
        Vector<Double> procEnergyPerStep;
        Vector<MProcessor> procPerStep;
        Integer curProcId;
        Double energy, curEnergy, layerEnergy;
        Double peakEnergy = 0.0;
        Double workloadShare;
        try{

        /** for every execution step*/
        for(Vector<layerFiring> execStep: schedule) {
            energyPerStep = new Vector<>();
            procPerStep = new Vector<>();

            /**evaluate execution step. All processors on different execution steps
             * work in sequence. All processors within one execution step work in parallel.*/
            for(layerFiring lf: execStep){
                procEnergyPerStep = new Vector<>();//processor energy per execution step
                for(MProcessor proc: lf.getProcessors()){ //query all processors, working at this exec. step
                    if (!procPerStep.contains(proc)) {
                        procEnergyPerStep.add(0.0);
                        procPerStep.add(proc);
                    }

                    curProcId = procPerStep.indexOf(proc);
                    energy = procEnergyPerStep.elementAt(curProcId);

                    workloadShare = lf.getWorkloadPercentage(proc);
                    curEnergy = _evaluateEnergyWatt(lf.getLayer(),proc, platformEval, workloadShare);

                    //update layer energy
                    layerEnergy = lf.getLayer().get_energyEval();
                    if(curEnergy>layerEnergy)
                        lf.getLayer().set_energyEval(layerEnergy);

                    if(energy<curEnergy)
                        procEnergyPerStep.setElementAt(curEnergy,curProcId);// processor max energy per this step

                }

                //energy per step = sum of max energy of all processors, executed in parallel on this step
                Double sumEnergy = 0.0;
                for (Double procEnergy: procEnergyPerStep) {
                    sumEnergy += procEnergy;
                }

                energyPerStep.add(sumEnergy);

            }


            for(Double stepEnergy: energyPerStep) {
                if(stepEnergy>peakEnergy)
                    peakEnergy = stepEnergy;
            }
        }

        evalResult.setEnergy(peakEnergy);
        dnn.set_energyEval(peakEnergy);

    }
        catch (Exception e){
        System.err.println("DNN-CSDF energy evaluation error: " + e.getMessage());
    }
    }

    /**Evaluate DNN layerenergy in Watt*/
    private static Double _evaluateEnergyWatt(Layer layer, MProcessor processor, PlatformEval platformEval, Double workloadShare){
        Double energy;

        String procName = processor.getName();
        CoreTypeEval coreTypeEval = platformEval.getCoreTypeEval(procName);

        Operator op = layer.getNeuron().getOperator();
        if(op==null)
            layer.initOperator();

        energy = coreTypeEval.getOperatorEnergyEvalWatt(op);
        return energy;
    }

    /**
     * Get energy evaluation for DNN
     * @param dnn Deep Neural Network
     * @return refined energy evaluation for CSDF graph or null
     */
    public static void evaluateEnergyJoules(Network dnn, CSDFEvalResult evalResult, PlatformEval platformEval, Mapping mapping){
        try{

            Double totalEnergy = 0.0;
            Double layerEnergy;
            MProcessor proc;
            Double sToMs = 1000.0;

            for(Layer layer: dnn.getLayers()){
                proc = mapping.findProcessorForTask(layer.getName());
                layerEnergy =  _evaluateEnergyJoules(layer,proc,platformEval,sToMs);
                layer.set_energyEval(layerEnergy);
                totalEnergy+=layerEnergy;
            }


            evalResult.setEnergy(totalEnergy);
        }
        catch (Exception e){
            System.err.print("Energy computation error: " + e.getMessage());
        }
    }



    /**Evaluate DNN layer execution energy in Joules*/
    private static Double _evaluateEnergyJoules(Layer layer, MProcessor processor, PlatformEval platformEval, Double scale){
        Double energy;

        String procName = processor.getName();
        CoreTypeEval coreTypeEval = platformEval.getCoreTypeEval(procName);

        Operator op = layer.getNeuron().getOperator();
        if(op==null)
            layer.initOperator();
        Double time = layer.get_timeEval();

        energy = coreTypeEval.getOperatorEnergyEvalJoules(op,time,scale);
        return energy;
    }

    /**
     * Get processor number evaluation for DNN
     * @return refined processor number evaluation for DNN or null
     */
    public static void evaluateProcNum(CSDFEvalResult result, Vector<Vector<layerFiring>> schedule){
        try{
            Vector<MProcessor> distinctProc = new Vector<>();
            Integer procNum = 0;

            /** for every execution step*/
            for(Vector<layerFiring> execStep: schedule) {
                /**Find all procesors, participating in computations*/
                for(layerFiring lf: execStep) {
                    for (MProcessor proc : lf.getProcessors()) { //query all processors, working at this exec. step
                        if (!distinctProc.contains(proc)) {
                            distinctProc.add(proc);
                            procNum++;
                            //System.out.println("proc: "+ proc.getName());
                            if(!proc.isCPU()) {// In pipeline mode any accelerator needs a separate CPU (host) to launch kernels
                                MProcessor host = _getHost(proc, distinctProc);
                                if (!distinctProc.contains(host)) {
                                    distinctProc.add(host);
                                    procNum++;
                                }
                                System.out.println("accelerator: "+ proc.getName());
                            }
                        }
                    }
                  }
                }
            result.setProcessors(procNum);
        }
        catch (Exception e){
            System.err.print("Processors number computation error: " + e.getMessage());
        }
    }


    /** Get host CPU core, assigned to launch kernels on an accelerator
     * TODO: extract the host from json/assign during the mapping
     * */
    private static MProcessor _getHost(MProcessor  accelerator, Vector<MProcessor> distinctProc){
        for(MProcessor proc: distinctProc){
            if(proc.isCPU())
                return proc;
        }
        return accelerator;
    }

    /**
     * Get processor number evaluation for DNN
     * @return refined processor number evaluation for DNN or null

    public static void evaluateProcNum(CSDFEvalResult result, Mapping mapping){
        try{

            Integer processors = 0;

            MProcessor proc;
            for (Object procObj: mapping.getProcessorList()) {
                proc = (MProcessor) procObj;
                Vector processes = proc.getProcessList();
                if (processes.size() > 0) {
                    processors++;
                    if (proc.getResource() instanceof GPU || proc.getResource() instanceof HWCE)
                        processors++; //+= host CPU, launching kernels on GPU/FPGA
                }

            }
            result.setProcessors(processors);

        }
        catch (Exception e){
            System.err.print("Processors number computation error: " + e.getMessage());
        }
    }     */

    /**
     * Get energy evaluation for DNN
     * @return refined energy evaluation for CSDF graph or null
     */
    public static void evaluateEnergy(CSDFEvalResult result, PlatformEval platformEval, Mapping mapping){
        try{

            Integer processors = 0;
            Double energy = 0.0;
            Double procEnergy;
            MProcessor proc;
            String procName;
            CoreTypeEval procEval;
            for (Object procObj: mapping.getProcessorList()) {
                proc = (MProcessor)procObj;
                Vector processes = proc.getProcessList();
                if (processes.size() > 0) {
                    processors++;
                    if(proc.getResource() instanceof GPU || proc.getResource() instanceof HWCE)
                        processors++;

                    procName = proc.getResource().getName();
                    procEval = platformEval.getCoreTypeEval(procName);

                    if (procEval== null) {
                        System.err.println("Energy evaluation error: energy for processor " + procName + " is not found in platform specification");
                    } else {
                        procEnergy = procEval.getMaxPower();
                        energy += procEnergy;
                    }
                }

        }

            result.setEnergy(energy);
            result.setProcessors(processors);
        }
        catch (Exception e){
            System.err.print("Energy computation error: " + e.getMessage());
        }
    }
}
