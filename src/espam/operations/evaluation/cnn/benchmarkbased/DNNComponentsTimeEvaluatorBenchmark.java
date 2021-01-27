package espam.operations.evaluation.cnn.benchmarkbased;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.cnn.DNNComponentsTimeEvaluator;
import espam.operations.evaluation.platformDescription.OpBenchmark;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;

/**
 * Class annotates every DNN layer with platform-aware time evaluation.
 * It uses per-layer bechmarks, performed on the tagter platform,
 * to perform the evaluation
 */
public class DNNComponentsTimeEvaluatorBenchmark extends DNNComponentsTimeEvaluator {

    /**
     * Create new dnn components time evaluator
     * @param dnn dnn
     * @param platformDescription description of platform, on which dnn is executed
     * @param mapping mapping of dnn onto platform
     */
    public DNNComponentsTimeEvaluatorBenchmark(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        super(dnn, platformDescription, mapping);
    }


    /** get evaluation of layer execution time*/
    protected Double _getLayerExecTime(Layer layer, MProcessor processor){
        ProcTypeDescription procTypeDescription = _platformDescription.getProcTypeDescription(processor.getName());
        Operator op = layer.getNeuron().getOperator();
        if(_isIODataOperator(op.getName()) || _operatorNotEvaluated(procTypeDescription, op.getName()))
            return 0.0;
        Double opTime =  _evaluateOPTimeWithBenchmark(op, procTypeDescription);
        return opTime;
    }

    /**
     * Checks if operator evaluation is present in the platform benchmark
     * @param opName operator name
     * @return true, if operator evaluation is present in the
     * platform benchmark and false otherwise
     */
     protected boolean _operatorNotEvaluated(ProcTypeDescription procTypeDescription, String opName){
         OpBenchmark opBenchmark = BenchmarkHelper.findOpEvalRecord(opName, procTypeDescription.getOpBenchmarks());
         return  opBenchmark ==null;
    }

    /////////////////////////////////////////////////////////////////////////
    ////////////// operator execution time                      ////////////

    /**
     * Evaluate operator execution time with platform benchmark
     * @param op operator
     * @param procTypeDescription processor type description with op. benchmark
     * @return operator execution time, evaluated with platform benchmark
     */
    protected Double _evaluateOPTimeWithBenchmark(Operator op, ProcTypeDescription procTypeDescription){
        OpBenchmark opBenchmark = BenchmarkHelper.findOpEvalRecord(op.getName(), procTypeDescription.getOpBenchmarks());
        Double dOpPerformance = opBenchmark.getPerformance().doubleValue();
        Double dPerformanceScale = procTypeDescription.get_performanceScale().doubleValue();
        Double dOpCompexity = op.getTimeComplexity().doubleValue();
        Double timeEval = dOpPerformance / dPerformanceScale * dOpCompexity;
        return timeEval;
    }
}
