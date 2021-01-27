package espam.operations.evaluation.platformDescription;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

public class ProcTypeDescription {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public Long get_performanceScale(){ return _performanceScale; }

    public String get_type() {
        return _type;
    }

    public String get_subtype() {
        return _subtype;
    }

    public Double getMaxPower(){
        return _maxPower;
    }

    public Double getMaxPerformance() { return _maxPerformance; }

    public Vector<OpBenchmark> getOpBenchmarks() {
        return _opBenchmarks;
    }

    public Long getMaxThreadsPerSM() { return _maxThreadsPerSM; }

    public Long getSharedMemoryPerSM() { return _sharedMemoryPerSM; }

    public Long getRegistersPerSM() { return _registersPerSM; }

    public Long get_default_threads_per_block() {
        return _default_threads_per_block;
    }

    public Long get_default_registers_per_thread() {
        return _default_registers_per_thread;
    }

    public Long get_default_sh_mem_per_block() {
        return _default_sh_mem_per_block;
    }

    public Integer getSM() { return _SM; }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /** core type name */
    @SerializedName("name")private String _name = "";

    /** core type name */
    @SerializedName("type")private String _type = "";

    /** core type name */
    @SerializedName("subtype")private String _subtype = "";

    /** core type id */
    @SerializedName("id")private Integer _id =0;

    /** max frequency */
    @SerializedName("max_frequency")private Double _maxFrequency=0.0;

    /** max power */
    @SerializedName("max_power")private Double _maxPower = 0.0;

    /**Max operator performance Giga ops/second*/
    @SerializedName("max_performance") Double _maxPerformance = 1.0;

    /** max power */
    @SerializedName("local_memory_size")private Double _local_memory_size=0.0;

    /**Performance derivation characteristic*/
    @SerializedName("supported_operator")private Vector<OpBenchmark> _opBenchmarks = new Vector<>();

    /** performance scale to double value*/
    @SerializedName("performance_scale") Long _performanceScale = 1l;

    /////////////////////////////////////////////////////////////////////
    ////                         GPU parameters                     ////
    /** total number of streaming multiprocessors (SM)*/
    @SerializedName("SM")private Integer _SM = 1;
    /**shared memory per SM*/
    @SerializedName("sh_mem_per_sm")private Long _sharedMemoryPerSM = 0l;
    /** registers per SM*/
    @SerializedName("reg_per_sm")private Long _registersPerSM = 0l;
    /** max threads per SM*/
    @SerializedName("max_threads_per_sm")private Long _maxThreadsPerSM = 0l;

    /** default kernel-dependent values*/
    @SerializedName("default_threads_per_block") Long _default_threads_per_block=0l;
    @SerializedName("default_sh_mem_per_block") Long _default_sh_mem_per_block = 0l;
    @SerializedName("default_registers_per_thread") Long _default_registers_per_thread = 0l;
}
