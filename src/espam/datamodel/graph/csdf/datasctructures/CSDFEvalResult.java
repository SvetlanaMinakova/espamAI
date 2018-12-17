package espam.datamodel.graph.csdf.datasctructures;

import com.google.gson.annotations.SerializedName;

/**
 * Class describes the result of SDF model evaluation in terms of energy/performance
 * and resource usage
 */
public class CSDFEvalResult {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get performance evaluation
     * @return performance evaluation
     */
    public double getPerformance() {return _performance; }

    /**
     * Set performance evaluation
     * @param performance  performance evaluation
     */
    public void setPerformance(double performance) {
        this._performance = performance;
    }

    /**
     * Get energy evaluation
     * @return energy evaluation
     */
    public double getEnergy() { return _energy; }

    /**
     * Set energy evaluation
     * @param energy energy evaluation
     */
    public void setEnergy(double energy) {
        this._energy = energy;
    }

    /**
     * Get memory evaluation
     * @return memory evaluation
     */
    public double getMemory() {
        return _memory;
    }

    /**
     * Set memory evaluation
     * @param memory memory evaluation
     */
    public void setMemory(double memory) {
        this._memory = memory;
    }

    /**
     * Get processors number evaluation
     * @return processors number evaluation
     */
    public int getProcessors() {
        return _processors;
    }

    /**
     * Set processors number evaluation
     * @return processors number evaluation
     */
    public void setProcessors(int processors) {
        this._processors = processors;
    }

    /**
     * Set SDF eval result unique identifier
     * @return SDF eval result unique identifier
     */
    public int getId() { return _id; }

    /**
     * Set SDF eval result unique identifier
     * @param id SDF eval result unique identifier
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Returns string description of CSDFEvalResult
     * @return string description of CSDFEvalResult
     */
    @Override
    public String toString() {
        return "performance: " +_performance+" ,energy: " + _energy +
                " ,memory: "+_memory+", processors: " + _processors;
    }

    /**
     * Returns JSON string description of CSDFEvalResult
     * @return JSON string description of CSDFEvalResult
     */
    public String toJSON() {
        StringBuilder jsonSB = new StringBuilder("{ ");
        jsonSB.append("\"id\": " + _id + ", ");
        jsonSB.append("\"execution_time\": " + _performance + ", ");
        jsonSB.append("\"energy\": " + _energy + ", ");
        jsonSB.append("\"memory\": " + _memory + ", ");
        jsonSB.append("\"processors\": " + _processors+ " }");
        return jsonSB.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**evaluation result id*/
    @SerializedName("id") private int _id = 0;

    /**performance*/
    @SerializedName("performance") private double _performance = 0;

    /**energy*/
    @SerializedName("energy") private double _energy = 0;

    /**memory footprint evaluation*/
    @SerializedName("memory") private double _memory = 0;

    /**number of processors*/
    @SerializedName("processors") private int _processors = 0;

}
