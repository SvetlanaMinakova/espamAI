package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;
import espam.operations.evaluation.MeasurementUnitsConverter;

import java.util.Vector;

/***
 * Class annotates every layer and connection in the DNN
 * with datatype-aware memory evaluation
 */
public class DNNComponentsMemoryEvaluator {

    /**
     * Get DNN memory annotator instance
     * @return DNN memory annotator instance
     */
    public static DNNComponentsMemoryEvaluator getInstance() {
        return _evaluator;
    }


    /** Annotate DNN with memory evaluation*/
    public void annotateDNNLayersAndConnectionsWithTimeEval(Network dnn, DNN_MAPPING_TYPE mappingType, Vector<String> nullBufferDestinations, boolean toMB){
        _toMB = toMB;
        _nullBufferDestinations = nullBufferDestinations;
        _annotateInternalMemory(dnn);
        _annotateBufferSizes(dnn, mappingType);
    }


    /**
     * Evaluate buffer sizes of the DNN
     *
     * @param dnn dnn graph to be evaluated
     * @return internal memory of the DNN graph evaluation
     * @throws Exception if an error occurs
     */
    private void _annotateBufferSizes(Network dnn, DNN_MAPPING_TYPE mappingType) {
        double conEval;
        double tokenSize = MeasurementUnitsConverter.getTokenSize(dnn.getDataType(),_toMB);
        for (Connection c : dnn.getConnections()) {
            conEval = _evaluateBufferSize(c,mappingType,tokenSize);
            c.set_memEval(conEval);
        }
    }


    /**
     * Evaluate buffer size for DNN connection
     * @param con connection
     * @return evaluated buffer size
     */
    private Double _evaluateBufferSize(Connection con, DNN_MAPPING_TYPE mappingType, double tokenSize){
        long bufferSizeInTokens = _evaluateBufferSizeInTokens(con,mappingType);
        Double buffersize = (double)bufferSizeInTokens * tokenSize;
        return buffersize;
    }


    /**
     * Evaluate buffer size for DNN connection
     * @param con connection
     * @return evaluated buffer size
     */
    private Long _evaluateBufferSizeInTokens(Connection con, DNN_MAPPING_TYPE mappingType) {
        try {
            if (_nullBufferDestinations.contains(con.getSrc().getNeuron().getName()))
                return 0l;

            long osize = con.getSrc().getOutputFormat().getElementsNumber();
            long isize;
            if(con.getDest().getNeuron() instanceof MultipleInputsProcessor) {
                isize = osize;
            }
            else isize = con.getDest().getInputFormat().getElementsNumber();



            /** If input buffer = output buffer, only one buffer is implemented, i.e.
             *  out_buf = create_buf(); in_buf = ptr(out_buf); and no data copying is done
             *  Otherwise, both buffers are implemented, i.e.
             *  out_buf = create_buf();  in_buf = create_buf(); and data is copied from
             *  input_buf to output_buf
             *
             */

            long total = osize;
            if (osize != isize || mappingType == DNN_MAPPING_TYPE.PIPELINE)
                total += osize;

            return total;
        }

        catch (Exception e) {
            System.err.println(con.getSrcName() + " DNN-CSDF buffer evaluation error, itensor: " + con.getSrc().getOutputFormat() + ", otensor: " + con.getDest().getInputFormat() );
            return 0l;
        }

    }

    /**
     * Evaluate internal memory of the CSDF graph
     *
     * @param dnn dnn graph to be evaluated
     * @return internal memory of the CSDF graph evaluation
     * @throws Exception if an error occurs
     */
    private void _annotateInternalMemory(Network dnn) {
        double layerEval;
        double tokenSize = MeasurementUnitsConverter.getTokenSize(dnn.getWeightsType(), _toMB);
        for (Layer l : dnn.getLayers()) {
            try {
                layerEval = _evalInternalMemory(l, tokenSize);
                l.set_memEval(layerEval);
            } catch (Exception e) {
                System.err.println(l.getName() + " DNN-CSDF internal memory evaluation error");
            }
        }
    }

    /**
     * Evaluate internal memory of the CNN layer
     *
     * @param layer CNN layer to be evaluated
     * @return internal memory of the CNN layer evaluation
     * @throws Exception if an error occurs
     */
    private Double _evalInternalMemory(Layer layer, double tokenSize) {
        Operator op = layer.getNeuron().getOperator();
        if (op == null) {
            layer.initOperator();
        }

        long resultInTokens = evaluateOperatorMemInTokens(op);

        double memEval = resultInTokens * tokenSize;
        return memEval;
    }


    /**
     * Evaluate DNN/CSDF operator
     *
     * @param op DNN/CSDF operator
     * @return DNN/CSDF operator memory evaluation
     */
    public long evaluateOperatorMemInTokens(Operator op) {
        long result = 0l;
        if (op != null) {
            if (op.hasTensorParams()) {
                for (Tensor t : op.getTensorParams().values()) {
                    if (!Tensor.isNullOrEmpty(t)) {
                        // System.out.println(op.getName() + ", tensor: " + t.toString());
                        result += t.getElementsNumber();
                    }
                }
            }
        }
        return result;
    }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static DNNComponentsMemoryEvaluator _evaluator = new DNNComponentsMemoryEvaluator();
    private boolean _toMB;
    private Vector<String> _nullBufferDestinations;
}