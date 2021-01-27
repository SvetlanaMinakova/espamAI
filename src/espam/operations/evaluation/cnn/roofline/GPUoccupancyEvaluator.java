package espam.operations.evaluation.cnn.roofline;

import espam.datamodel.graph.cnn.operators.Operator;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;

/**
 * This class tries to evaluate GPU occupancy from the ProcTypeDescription GPU description
 */
public class GPUoccupancyEvaluator {

    /**
     * Evaluate gpu occupancy. By definition, GPU occupancy is computed
     * as number of active warps per SM, divided by max. number of active
     * warps per SM, where
     * - number of active warps per SM is determined by the performed operator
     *  and its parallelism
     * -  max. number of warps per SM is determined by the GPU
     *  physical limitations
     * @param procTypeDescription description of the GPU processor
     * @param op operator to execute on the GPU processor
     * @return occupancy of the GPU processor by the operator
     */
    public Double evaluateGPUoccupancy(ProcTypeDescription procTypeDescription, Operator op){
        Long maxWarpsPerSM = getMaxWarpsPerSM(procTypeDescription);
        Double defaultOccupancy = 1.0;
        Double occupancy;
        if (!_isMaxThreadsPerSMSpecified(procTypeDescription))
             occupancy = defaultOccupancy;
        else {
            //return 0.32;
            Long activeWarpsPerSM = _getActiveWarpsPerSM(procTypeDescription, maxWarpsPerSM, op);
            occupancy = activeWarpsPerSM.doubleValue() / maxWarpsPerSM.doubleValue();
            if (occupancy == 0)
                occupancy = defaultOccupancy;
        }
        //System.out.println("op: " + op.getName() + ", occupancy: " + occupancy);
        return occupancy;
    }

    /**
     * Get active warps per SM, taking into account following operator- and GPU-dependent parameters:
     * - threads per block (max and actual)
     * - shared memory per block (max and actual)
     * - registers per thread (max and actual)
     * In case none of the limiting parameters, listed above, are present in the platform
     * description, the maxWarpsPerSM value is returned
     * @param maxWarpsPerSM max warps per SM
     * @return active warps per SM, taking into account operator-dependent parameters
     */
    private Long _getActiveWarpsPerSM(ProcTypeDescription procTypeDescription, Long maxWarpsPerSM,  Operator op){
        Long blockBottleneck = _getActiveWarpsPerSMBlocksBottleneck(procTypeDescription, maxWarpsPerSM, op);
        Long shMemBottleneck = _getActiveWarpsPerSMSHMemBottleneck(procTypeDescription, maxWarpsPerSM, op);
        Long regBottleneck = _getActiveWarpsPerSMRegBottleneck(procTypeDescription, maxWarpsPerSM, op);
        Long activeWarpsPerSM = _getMinValue(blockBottleneck, shMemBottleneck, regBottleneck);
        return activeWarpsPerSM;
    }

    /**
     * Get active warps per SM, taking into account
     * - threads per block (max and actual)
     * In case this bottleneck cannot be evaluated, the maxWarpsPerSM value is returned
     * @param maxWarpsPerSM max warps per SM
     * @return active warps per SM, taking into account threads per block
     */
    private Long _getActiveWarpsPerSMBlocksBottleneck(ProcTypeDescription procTypeDescription, Long maxWarpsPerSM,  Operator op){
        Long warpsPerSMForFullGPUOccupancy = maxWarpsPerSM;

        Long threadsInOp = op.getTimeComplexity();
        Long warpsInOp = threadsInOp/_getDefaultThreadsInWarp();
        Integer SMNum = procTypeDescription.getSM();
        Long warpsInOpPerSM = warpsInOp/SMNum;

        Long activeWarpsPerSM;
       if (warpsInOpPerSM >= warpsPerSMForFullGPUOccupancy)
             activeWarpsPerSM = maxWarpsPerSM;
       else
           activeWarpsPerSM = warpsInOpPerSM;
        return activeWarpsPerSM;
    }

    /**
     * Get active warps per SM, taking into account
     * - shared memory per block (max and actual)
     * In case this bottleneck cannot be evaluated, the maxWarpsPerSM value is returned
     * @param maxWarpsPerSM max warps per SM
     * @return active warps per SM, taking into account shared memory per block
     */
    private Long _getActiveWarpsPerSMSHMemBottleneck(ProcTypeDescription procTypeDescription, Long maxWarpsPerSM,  Operator op) {
       if(!_isDefaultThreadsPerBlockSpecified(procTypeDescription)||
               !_isDefaultSMMemPerBlockSpecified(procTypeDescription) ||
               !_isSHMemPerSMSpecified(procTypeDescription))
           return maxWarpsPerSM;

       Long shMemPerBlock = procTypeDescription.get_default_sh_mem_per_block();
       Long maxSHMemPerSM = procTypeDescription.getSharedMemoryPerSM();
       Long maxBlocksPerSM = maxSHMemPerSM/shMemPerBlock; //sh-mem bound max-blocks
       Long defaultWarpsPerBlock = Math.max(procTypeDescription.get_default_threads_per_block()/_getDefaultThreadsInWarp(), 1l);
       Long activeWarpsPerSM = maxBlocksPerSM * defaultWarpsPerBlock;

       return activeWarpsPerSM;
    }

    /**
     * Get active warps per SM, taking into account
     * - registers per thread (max and actual)
     * In case this bottleneck cannot be evaluated, the maxWarpsPerSM value is returned
     * @param maxWarpsPerSM max warps per SM
     * @return active warps per SM, taking into account  registers per thread
     */
    private Long _getActiveWarpsPerSMRegBottleneck(ProcTypeDescription procTypeDescription, Long maxWarpsPerSM,  Operator op) {
        if(!_isDefaultThreadsPerBlockSpecified(procTypeDescription)||
                !_isDefaultRegsPerThreadSpecified(procTypeDescription))
            return maxWarpsPerSM;

        Long threadsPerBlock = procTypeDescription.get_default_threads_per_block();
        Long regPerThread = procTypeDescription.get_default_registers_per_thread();
        Long regPerBlock = regPerThread * threadsPerBlock;
        Long maxRegPerSM = procTypeDescription.getRegistersPerSM();
        Long maxBlocksPerSM = maxRegPerSM / regPerBlock; //sh-mem bound max-blocks
        Long defaultWarpsPerBlock = Math.max(threadsPerBlock/_getDefaultThreadsInWarp(), 1l);
        Long activeWarpsPerSM = maxBlocksPerSM * defaultWarpsPerBlock;
        return activeWarpsPerSM;
    }


    /**
     * Get max warps per SM. Max. number of active warps per SM is determined by the GPU
     * physical limitations
     * @param procTypeDescription description of the GPU processor
     * @return max warps per sm
     */
    private Long getMaxWarpsPerSM(ProcTypeDescription procTypeDescription){
        Long maxThreadsPerSM = procTypeDescription.getMaxThreadsPerSM();
        Long threadsPerWarp = _getDefaultThreadsInWarp();
        Long maxWarpsPerSM = maxThreadsPerSM / threadsPerWarp;
        return maxWarpsPerSM;
    }

    ///////////////////////////////////////////////////////////////////
    ////          is-specified checkers                           ////

    /**
     * Determine, if GPU description specifies computational limits of the GPU
     * @param procTypeDescription GPU description
     * @return true, if GPU description specifies computational limits of the GPU, and
     * false otherwise
     */
    private boolean _isMaxThreadsPerSMSpecified(ProcTypeDescription procTypeDescription){
        Long maxThreadsPerSM = procTypeDescription.getMaxThreadsPerSM();
        return maxThreadsPerSM > 0;
    }

    /**
     * @return true, if GPU description specifies computational limits of the GPU, and
     * false otherwise
     */
    private boolean _isDefaultThreadsPerBlockSpecified(ProcTypeDescription procTypeDescription){
        Long defaultThreadsPerBlock = procTypeDescription.get_default_threads_per_block();
        return defaultThreadsPerBlock > 0;
    }

    /**
     * Determine, if GPU description specifies computational limits of the GPU
     * @param procTypeDescription GPU description
     * @return true, if GPU description specifies computational limits of the GPU, and
     * false otherwise
     */
    private boolean _isSHMemPerSMSpecified(ProcTypeDescription procTypeDescription){
        Long shMemPerSM = procTypeDescription.getSharedMemoryPerSM();
        return shMemPerSM > 0;
    }

    /**
     * Determine, if GPU description specifies computational limits of the GPU
     * @param procTypeDescription GPU description
     * @return true, if GPU description specifies computational limits of the GPU, and
     * false otherwise
     */
    private boolean _isDefaultRegsPerThreadSpecified(ProcTypeDescription procTypeDescription){
        Long defaultRegsPerBlock = procTypeDescription.get_default_registers_per_thread();
        return defaultRegsPerBlock > 0;
    }

    /**
     * Determine, if GPU description specifies computational limits of the GPU
     * @param procTypeDescription GPU description
     * @return true, if GPU description specifies computational limits of the GPU, and
     * false otherwise
     */
    private boolean _isDefaultSMMemPerBlockSpecified(ProcTypeDescription procTypeDescription){
        Long defaultSMMemPerBlock = procTypeDescription.get_default_sh_mem_per_block();
        return defaultSMMemPerBlock > 0;
    }

    /**
     * Get default number of threads in a warp. According to https://en.wikipedia.org/wiki/Thread_block_(CUDA_programming)
     * the default value for latest processors is 32 threads per warp
     * @return default number of threads in a warp
     */
    private Long _getDefaultThreadsInWarp(){ return 32l; }


    /**
     * Get min value among the elements
     * @param values several Long elements
     * @return min value among the elements
     */
    private Long _getMinValue(Long ... values){
        if (values.length==0) return 0l;
        Long minValue = values[0];
        for(Long value: values)
            minValue = Math.min(value, minValue);
        return minValue;
    }
}
