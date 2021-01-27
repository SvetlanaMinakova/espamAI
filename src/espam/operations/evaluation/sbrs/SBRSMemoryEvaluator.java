package espam.operations.evaluation.sbrs;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.graph.sbrs.supergraph.SBRSConnection;
import espam.datamodel.graph.sbrs.supergraph.SBRSLayer;
import espam.datamodel.graph.sbrs.supergraph.Supergraph;
import espam.datamodel.graph.sbrs.control.Parameter;
import espam.datamodel.graph.sbrs.control.ParameterValue;

import java.util.TreeMap;
import java.util.Vector;

/**
 * This class evaluates memory of the SBRS MoC: MoC for run-time adaptive
 * CNN-based applications, executed at the edge.
 */
public class SBRSMemoryEvaluator {

    /**
     * Get the memory evaluator instance
     * @return the memory evaluator instance
     */
    public static SBRSMemoryEvaluator getInstance() {
        return _memEvaluator;
    }

    /**
     * Provide ESPAMAI memory evaluation
     * @param Supergraph SBRS MoC
     * @param tokenBytes size of token in bytes
     * @param toMB represent value in megabytes
     */
    public Double evalMemory(Supergraph Supergraph, double tokenBytes, boolean toMB, Vector<String> nullBufferDestinations){
        try{
            double internalMemory = _evalInternalMemory(Supergraph) * tokenBytes;
            double bufMemory = evalBufferSizes(Supergraph, nullBufferDestinations) * tokenBytes;
            if (toMB){
                internalMemory = internalMemory/1e6;
                bufMemory = bufMemory/1e6;
            }
            //System.out.println("SBRS MoC weights: " + internalMemory + " MB, buffers: " + bufMemory + " MB");
            double eval = internalMemory + bufMemory;
            return eval;
        }
        catch (Exception e){
            System.err.println("SBRS MoC memory evaluation error: " + e.getMessage() );
            return 0.0;
        }
    }

    /** evaluate buffer sizes*/
    private Long evalBufferSizes(Supergraph Supergraph, Vector<String> nullBufferDestinations){
        _nullBufferDestinations = nullBufferDestinations;
        Long bufferSizes = 0l;
        for(SBRSConnection sbrsConnection: Supergraph.getConnections()){
            bufferSizes = bufferSizes + evalBufferSize(sbrsConnection);
        }
        return bufferSizes;
    }

    /** evaluate buffer sizes*/
    private Long evalBufferSize(SBRSConnection connection){
        if (_nullBufferDestinations.contains(connection.getSrc().getParent().getNeuron().getName()))
            return 0l;
        Long bufferSize = connection.getBufferSize();
        return bufferSize;
    }

    /** evaluate internal (weights) memory in tokens*/
    private Long _evalInternalMemory(Supergraph Supergraph){
        Long internalMemory = 0l;
        Long layerMemory;
        for (SBRSLayer sbrsLayer : Supergraph.getLayers()) {
            layerMemory = _evaluateLayerInternalMemory(sbrsLayer);
            internalMemory +=layerMemory;
        }
        return internalMemory;
    }

    /** evaluate internal memory(weights) for a layer*/
    private Long _evaluateLayerInternalMemory(SBRSLayer sbrsLayer){
        Long internalMemory;

        if(sbrsLayer.isParParametrized())
            internalMemory = _computeInternalMemoryFromAdaptiveParameters(sbrsLayer);
        else
            internalMemory = _computeInternalMemoryFromParentLayer(sbrsLayer);

        return internalMemory;
    }

    private Long _computeInternalMemoryFromAdaptiveParameters(SBRSLayer sbrsLayer){
        Long internalMemory = 0l;
        Parameter adaptivePar = sbrsLayer.getAdaptiveTrainableParameters();
        Vector<ParameterValue> values = adaptivePar.getParameterValues();
        for (ParameterValue pv: values){
            TreeMap<String, Tensor> pvFormat = (TreeMap<String, Tensor>)pv.getValue();
            for(Tensor adaptiveParValue: pvFormat.values()) {
                if (!Tensor.isNullOrEmpty(adaptiveParValue)){
                    internalMemory = internalMemory + adaptiveParValue.getElementsNumber();
                }
            }
        }
        return internalMemory;
    }


    private Long _computeInternalMemoryFromParentLayer(SBRSLayer sbrsLayer){
        Long internalMemory = 0l;
        Operator parentOp = sbrsLayer.getParent().getNeuron().getOperator();

        if (parentOp==null) return 0l;
        if(!parentOp.hasTensorParams()) return 0l;
        for(Tensor parShape: parentOp.getTensorParams().values()) {
            if (!Tensor.isNullOrEmpty(parShape)){
                internalMemory = internalMemory + parShape.getElementsNumber();
            }
        }
        return internalMemory;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static SBRSMemoryEvaluator _memEvaluator = new SBRSMemoryEvaluator();
    private Vector<String> _nullBufferDestinations;
}
