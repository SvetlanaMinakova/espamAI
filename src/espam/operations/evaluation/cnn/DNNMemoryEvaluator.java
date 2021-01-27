package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;

import java.util.Vector;

/***
 * Singletone class for DNN memory evaluation
 */
public class DNNMemoryEvaluator {

    /**
     * Get DNN memory evaluator instance
     * @return DNN memory evaluator instance
     */
    public static DNNMemoryEvaluator getInstance() {
        return _evaluator;
    }

    /**
     * Provide ESPAMAI memory evaluation
     * @param dnn Deep neural network
     * @param evalResult variable to write memory evaluation in
     */
    public void evalMemory(Network dnn, CSDFEvalResult evalResult, DNN_MAPPING_TYPE mappingType, boolean toMB){
        Double memEval = evalMemory(dnn, mappingType, toMB, new Vector<>());
        evalResult.setMemory(memEval);
    }

    /**
     * Provide ESPAMAI memory evaluation
     * @param dnn Deep neural network
     */
    public Double evalMemory(Network dnn, DNN_MAPPING_TYPE mappingType, boolean toMB){
        Vector<String> nullBufferDestinations = new Vector<>();
        return evalMemory(dnn, mappingType, toMB, nullBufferDestinations);

    }

    /**
     * Provide ESPAMAI memory evaluation
     * @param dnn Deep neural network
     */
    public Double evalMemory(Network dnn, DNN_MAPPING_TYPE mappingType, boolean toMB, Vector<String> nullBufferDestinations){
        try{
            _toMB = toMB;
            _mappingType = mappingType;
            _nullBufferDestinations = nullBufferDestinations;
            _annotateDnnComponents(dnn);
            double internalMemory = evalInternalMemory(dnn);
            double bufMemory = evalBufferSizes(dnn);
            //System.out.println("DNN "  + dnn.getName() + " weights: " + internalMemory + " MB, buffers: " + bufMemory + " MB");

            double eval = internalMemory + bufMemory;
            dnn.set_memEval(eval);
            return eval;
        }
        catch (Exception e){
            System.err.println("DNN-CSDF memory evaluation error: " + e.getMessage() );
            return 0.0;
        }
    }

    /** evaluate internal (weights) memory*/
    public Double evalInternalMemory(Network dnn){
        if (!_componentsAnnotated)
            _annotateDnnComponents(dnn);
        Double internalMemory = 0.0;
        for (Layer layer : dnn.getLayers())
            internalMemory += layer.get_memEval();
        return internalMemory;
    }

    /** evaluate buffer sizes*/
    public Double evalBufferSizes(Network dnn){
        if (!_componentsAnnotated)
            _annotateDnnComponents(dnn);
        Double bufferSizes = 0.0;
        Vector<Layer> layers = dnn.getLayers();
        for(Layer l: layers){
            for(Connection con: l.getOutputConnections())
                bufferSizes += con.get_memEval();
        }
        return bufferSizes;
    }


    public void resetAnnottaionFlag(){
        _componentsAnnotated = false;
    }

    private void _annotateDnnComponents(Network dnn){
        DNNComponentsMemoryEvaluator.getInstance().annotateDNNLayersAndConnectionsWithTimeEval(dnn, _mappingType, _nullBufferDestinations, _toMB);
        _componentsAnnotated = true;
    }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static DNNMemoryEvaluator _evaluator = new DNNMemoryEvaluator();
    private boolean _componentsAnnotated = false;
    private DNN_MAPPING_TYPE _mappingType = DNN_MAPPING_TYPE.SEQUENTIAL;
    private boolean _toMB = true;
    private Vector<String> _nullBufferDestinations = new Vector<>();

}

