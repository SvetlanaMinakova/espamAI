package espam.operations.evaluation;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;

import java.util.HashMap;
import java.util.TreeMap;

/***
 * Evaluate DNN memory
 */
public class DNNMemoryEvaluator {

    /**
     * Get refined memory evaluator instance
     * @return refined memory evaluator instance
     */
    public static DNNMemoryEvaluator getInstance() {
        return _memEvaluator;
    }

    /**
     * Provide ESPAMAI memory evaluation
     * @param dnn Deep neural network
     * @param evalResult variable to write memory evaluation in
     */
    public void evalMemory(Network dnn, CSDFEvalResult evalResult, DNN_MAPPING_TYPE mappingType, boolean toMB){
        try{

            int tokenSize = typeSize(dnn.getDataType());
            _toMB = toMB;

            long internalMemory = evalInternalMemory(dnn);
            long bufMemory = evalBufferSizesMemory(dnn,mappingType);

            double eval = (internalMemory + bufMemory) * tokenSize;
            if(toMB)
                eval = eval/1000000;
            dnn.set_memEval(eval);

            evalResult.setMemory(eval);
        }
        catch (Exception e){
            System.err.println("DNN-CSDF memory evaluation error: " + e.getMessage() );
        }
    }

    /**
     * Evaluate buffer sizes of the DNN
     *
     * @param dnn dnn graph to be evaluated
     * @return internal memory of the DNN graph evaluation
     * @throws Exception if an error occurs
     */
    public Long evalBufferSizesMemory(Network dnn, DNN_MAPPING_TYPE mappingType) {
        long result = 0;
        for (Connection c : dnn.getConnections()) {
                result += evaluateBufferSize(c,mappingType);
            }
        return result;
    }


    /**
     * Evaluate buffer size for DNN connection
     *
     * @param con connection
     * @return evaluated buffer size
     */
    private Long evaluateBufferSize(Connection con, DNN_MAPPING_TYPE mappingType) {
        try {
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

            double memEval = (double) total;
            if (_toMB)
                memEval = memEval / 1000000.0;
            con.set_memEval(memEval);

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
    public Long evalInternalMemory(Network dnn) {
        long result = 0;
        for (Layer l : dnn.getLayers()) {
            try {
                result += evalInternalMemory(l);
            } catch (Exception e) {
                System.err.println(l.getName() + " DNN-CSDF internal memory evaluation error");
                return 0l;
            }
        }
        return result;
    }

    /**
     * Evaluate internal memory of the CNN layer
     *
     * @param layer CNN layer to be evaluated
     * @return internal memory of the CNN layer evaluation
     * @throws Exception if an error occurs
     */
    public long evalInternalMemory(Layer layer) throws Exception {
        Operator op = layer.getNeuron().getOperator();
        if (op == null) {
            layer.initOperator();
        }

        long result = evaluateOperator(op);
        double memEval = (double)result;
        if(_toMB)
            memEval = memEval/1000000.0;
        layer.set_memEval(memEval);
        return result;
    }


    /**
     * Evaluate DNN/CSDF operator
     *
     * @param op DNN/CSDF operator
     * @return DNN/CSDF operator memory evaluation
     */
    public long evaluateOperator(Operator op) {
        long result = 0l;
        if (op != null) {
            if (op.hasTensorParams()) {
                for (Tensor t : op.getTensorParams().values()) {
                    if (!Tensor.isNullOrEmpty(t))
                        result += t.getElementsNumber();
                }
            }
        }

        return result;
    }

    /**
     * Get memory size of data types in bytes
     *
     * @param valueDesc data type description
     * @return memory size of data types in bytes
     * @throws Exception if an error occurs
     */
    public int typeSize(String valueDesc) {
        if (valueDesc.contains("8"))
            return 1;
        if (valueDesc.contains("16"))
            return 2;
        if (valueDesc.contains("32"))
            return 4;
        if (valueDesc.contains("64"))
            return 8;
        if (valueDesc.contains("128"))
            return 16;
        /** standard shortcuts*/
        /** TODO check*/
        if (valueDesc.equals("bool"))
            return 1;
        if (valueDesc.equals("int"))
            return 4;
        if (valueDesc.equals("float"))
            return 4;
        if (valueDesc.contains("string"))
            return 4;

        System.err.println("mem refinement error: unknown data type " + valueDesc);
        return 0;
    }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static DNNMemoryEvaluator _memEvaluator = new DNNMemoryEvaluator();
    private boolean _toMB;
}

