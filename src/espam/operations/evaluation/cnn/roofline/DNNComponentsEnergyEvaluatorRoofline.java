package espam.operations.evaluation.cnn.roofline;

import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.cnn.DNNComponentsEnergyEvaluator;
import espam.operations.evaluation.cnn.benchmarkbased.BenchmarkHelper;
import espam.operations.evaluation.platformDescription.OpBenchmark;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;

/***
 * Class, which annotates every layer and connection in the DNN
 * with platform-aware energy evaluation, using energy benchmarks, performed on
 * the platform
 */
public class DNNComponentsEnergyEvaluatorRoofline extends DNNComponentsEnergyEvaluator {

    /**
     * Create new dnn components energy evaluator
     * @param dnn dnn
     * @param platformDescription description of platform, on which dnn is executed
     * @param mapping mapping of dnn onto platform
     */
    public DNNComponentsEnergyEvaluatorRoofline(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        super(dnn, platformDescription, mapping);
    }

    /**
     * Evaluate operator energy in watt
     * @param op DNN operator
     * @return evaluated energy in watt
     */
    public double getOperatorEnergyEvalWatt(ProcTypeDescription procTypeDescription, Operator op){
        Double procMaxWatt = procTypeDescription.getMaxPower();
        Double occupancy = ProcOccupancyEvaluator.evalProcessorOccupancy(procTypeDescription, op);
        Double evaluated = procMaxWatt * occupancy;
        return evaluated;
    }
}
