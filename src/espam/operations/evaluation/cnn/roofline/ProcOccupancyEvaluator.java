package espam.operations.evaluation.cnn.roofline;

import espam.datamodel.graph.cnn.operators.Operator;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;

/**
 * This class tries to evaluate GPU occupancy from the ProcTypeDescription GPU description
 * and DNN operator description
 */
public class ProcOccupancyEvaluator {
    /////////////////////////////////////////////////////////////////////////
    //////////////     public methods                           ////////////

    /**
     * Evaluate occupancy of the processor by a CNN operator
     * @param procTypeDescription description of the GPU processor
     * @param op operator to execute on the processor
     * @return occupancy of the processor by the operator
     */
    public static Double evalProcessorOccupancy(ProcTypeDescription procTypeDescription, Operator op){
        Double occupancy = 1.0;
        if (procTypeDescription.get_type().toLowerCase().equals("gpu"))
            occupancy = _gpuOccupancyEvaluator.evaluateGPUoccupancy(procTypeDescription, op);
        return occupancy;
    }

    /////////////////////////////////////////////////////////////////////////
    ////////////// private variables                            ////////////
    private static GPUoccupancyEvaluator _gpuOccupancyEvaluator = new GPUoccupancyEvaluator();
}
