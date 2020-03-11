package espam.operations.evaluation;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.operators.Operator;

import java.util.Vector;

public class CoreTypeEval {

    public Double getOperatorTimeEval(Operator op){
        Double timeEval = 0.0;
        Integer timeComplexity = op.getTimeComplexity();
        String opName = op.getName();
        /** I/O nodes time*/
        if(opName.toLowerCase().equals("read")|| opName.toLowerCase().equals("write"))
            return _computeIOTime(timeComplexity);

        OpTimeEval opTimeEval= findOpEvalRecord(opName);
        if(opTimeEval==null) {

            //System.out.println("no time evaluation  found for " + opName + ", time evaluation skipped!");
            return timeEval;
        }
        /**TODO: apply dynamic scale*/
        else {
            timeEval = _evaluateTime(opTimeEval, timeComplexity);
            //System.out.println(op.getName()+" time_eval = "+ opTimeEval._performance.doubleValue() + "/" + opTimeEval._performanceScale.doubleValue() + "*" + timeComplexity.doubleValue());
        }
        return timeEval;
    }

    private Double _computeIOTime(Integer timeComplexity){
        return 0.0;
    }

    /**
     * Evaluate operator time
     * @param plaEval evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    private double _evaluateTime(OpTimeEval plaEval, Integer opComplexity){
        Double evaluated = 0.0;
        //if(plaEval._performanceVec==null || plaEval._complexitiesVec==null)
            evaluated = _evaluateTimeAverage(plaEval, opComplexity);
        //else
           // evaluated = _evaluateTimeAverage(plaEval, opComplexity);
          //  evaluated = _evaluateTimeVectorized(plaEval, opComplexity);

        return evaluated;
    }

    /**
     * Evaluate operator energy in Joules
     * @param op Operator evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    public  double getOperatorEnergyEvalJoules(Operator op, Double timeEval, double scale){
        Double evaluated = 0.0;
        OpTimeEval opEvalRecord= findOpEvalRecord(op.getName());
        if(opEvalRecord==null) {
            //System.out.println("no time evaluation  found for " + opName + ", time evaluation skipped!");
            return 0.0;
        }
        evaluated = opEvalRecord._energy * timeEval /scale;
        return evaluated;
    }

    /**
     * Evaluate operator time
     * @param op Operator evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    public  double getOperatorEnergyEvalWatt(Operator op){
        Double evaluated = 0.0;
        OpTimeEval opEvalRecord= findOpEvalRecord(op.getName());
        if(opEvalRecord==null) {
            //System.out.println("no time evaluation  found for " + opName + ", time evaluation skipped!");
            return 0.0;
        }
        evaluated = opEvalRecord._energy;
        return evaluated;
    }

    /**
     * Evaluate operator time
     * @param plaEval evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    private double _evaluateTimeAverage(OpTimeEval plaEval, Integer timeComplexity){
        Double evaluated = plaEval._performance.doubleValue() / _performanceScale.doubleValue() * timeComplexity.doubleValue();
        return evaluated;
    }

    /**
     * Evaluate operator time
     * @param plaEval evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    private double _evaluateTimeVectorized(OpTimeEval plaEval, Integer timeComplexity){
        Integer closestComplexityId = _findClosestComplexityId(plaEval,timeComplexity);

        Long closestComplexity = plaEval._complexitiesVec.elementAt(closestComplexityId);
        Long closestTime = plaEval._performanceVec.elementAt(closestComplexityId);

        Double evaluated = closestTime.doubleValue() * (timeComplexity.doubleValue()/closestComplexity.doubleValue())  / _performanceScale.doubleValue();
        return evaluated;
    }

    /**
     * Find Id of evaluation with closest complexity
     * @param plaEval platform evaluations
     * @param timeComplexity operator time complexity
     * @return d of evaluation with closest complexity
     */
    private Integer _findClosestComplexityId(OpTimeEval plaEval, Integer timeComplexity){
        Long tcl = timeComplexity.longValue();
        Integer curId = 0;
        /** find closest complexity*/
        for (Long complexity: plaEval._complexitiesVec){
            if(tcl<complexity) {
                return curId;
            }
            curId++;

        }
        System.out.println("default complexity returned");
        /**set closest complexity to max complexity by default*/
        return plaEval._complexitiesVec.size() - 1;
    }


    /**
     * Find operator time evaluation
     * @param opName operator name. e.g. CONV(5_5_25)
     * @return operator time evaluation
     */
    public OpTimeEval findOpEvalRecord(String opName){
        String baseName = getBaseName(opName);
        for(OpTimeEval ote: _opTimeEvals){
            if(ote.get_name().equals(baseName))
                return ote;
        }
        return null;
    }

    private String getBaseName(String opname){
        String opnameLowerCase = opname.toLowerCase();
        if(opnameLowerCase.contains("conv")) {
            if(opnameLowerCase.contains("conv(1_1"))
                return "CONV1_1";
            if(opnameLowerCase.contains("conv(3_3"))
                return "CONV3_3";
            if(opnameLowerCase.contains("conv(5_5"))
                return "CONV5_5";
            if(opnameLowerCase.contains("conv(7_7"))
                return "CONV7_7";

            return "CONV";
        }

        if(opnameLowerCase.contains("matmul")|| opnameLowerCase.contains("gemm"))
            return "FC";
        if(opnameLowerCase.contains("maxpool")|| opnameLowerCase.contains("avgpool"))
            return "POOL";

        if(opnameLowerCase.contains("lrn("))
            return "LRN";

        if(opnameLowerCase.contains("concat("))
            return "CONCAT";

        if(opnameLowerCase.contains("bn("))
            return "BN";

        if(opnameLowerCase.contains("softmax"))
            return "SOFTMAX";

        return null;

    }

    public String get_type() {
        return _type;
    }

    public String get_subtype() {
        return _subtype;
    }

    public Double getMaxPower(){
        return _maxPower;
    }

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
    @SerializedName("max_power")private Double _maxPower=0.0;

    /** max power */
    @SerializedName("local_memory_size")private Double _local_memory_size=0.0;

    /**Performance derivation characteristic*/
    @SerializedName("supported_operator")private Vector<OpTimeEval> _opTimeEvals = new Vector<>();

    /** performance scale to double value*/
    @SerializedName("performance_scale") Long _performanceScale = 1l;
}
