package espam.operations.evaluation.cnn.benchmarkbased;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.MeasurementUnitsConverter;
import espam.operations.evaluation.cnn.DNNComponentsEnergyEvaluator;
import espam.operations.evaluation.platformDescription.OpBenchmark;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;

/***
 * Class, which annotates every layer and connection in the DNN
 * with platform-aware energy evaluation, using energy benchmarks, performed on
 * the platform
 */
public class DNNComponentsEnergyEvaluatorBenchmark extends DNNComponentsEnergyEvaluator {

    /**
     * Create new dnn components energy evaluator
     * @param dnn dnn
     * @param platformDescription description of platform, on which dnn is executed
     * @param mapping mapping of dnn onto platform
     */
    public DNNComponentsEnergyEvaluatorBenchmark(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        super(dnn, platformDescription, mapping);
    }

    /**
     * Evaluate operator energy in watt
     * @param op Operator evaluations, measured on real platfrom
     * @return evaluated operator time
     */
    public double getOperatorEnergyEvalWatt(ProcTypeDescription procTypeDescription, Operator op){
        Double evaluated = 0.0;
        OpBenchmark opBenchmarkRecord = BenchmarkHelper.findOpEvalRecord(op.getName(), procTypeDescription.getOpBenchmarks());
        if(opBenchmarkRecord ==null) {
            //System.out.println("no time evaluation  found for " + opName + ", time evaluation skipped!");
            return 0.0;
        }
        evaluated = opBenchmarkRecord.getEnergy();
        return evaluated;
    }
}
