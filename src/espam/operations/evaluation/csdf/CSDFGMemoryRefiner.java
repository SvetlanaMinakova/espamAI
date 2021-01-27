package espam.operations.evaluation.csdf;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFEdge;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.operations.evaluation.MeasurementUnitsConverter;

import java.util.Vector;

/**
 * Compute memory, required for CSDF graph execution
 * The memory of the CSDF graph is computed as buffer sizes + internal memory,
 * where buffer sizes are computed by DARTS and internal memory is computed
 * from static CSDF node Operator parameters
 */
public class CSDFGMemoryRefiner {
///////////////////////////////////////////////////////////////
    ////                      public methods                  ////

    /**
     * Get refined memory evaluator instance
     * @return refined memory evaluator instance
     */
    public static CSDFGMemoryRefiner getInstance() {
        return _refinedMemEvaluator;
    }

    /**
     * Refine memory evaluation, provided by DARTS
     * @param graph CSDF graph
     * @param dartsEval DARTS evaluation of CSDF graph
     */
    public void evaluateMemory(CSDFGraph graph, CSDFEvalResult dartsEval, boolean toMB){
        try{
            double internalMemory = evalInternalMemory(graph);
            double bufferSizes = evalBufferSizes(graph);
            double memory = internalMemory + bufferSizes;

            System.out.println("buffer sizes: ");
            System.out.println(bufferSizes/1000000.0);

            System.out.println("params: ");
            System.out.println(internalMemory/1000000.0);

            if(toMB)
                memory = memory/1000000.0;
            dartsEval.setMemory(memory);
        }
        catch (Exception e){
            System.err.println("DNN-CSDF memory evaluation refinement error");
        }
    }


    /**
     * Refine memory evaluation, provided by DARTS
     * @param graph CSDF graph
     * @param dartsEval DARTS evaluation of CSDF graph
     */
    public void refineMemoryEval(CSDFGraph graph, CSDFEvalResult dartsEval){
        try{
           // double refinedMem = refineTotalBufferSizes(dartsEval.getMemory(),graph.getTokenDesc());
           double refinedMem = evalInternalMemory(graph) + refineTotalBufferSizes(dartsEval.getMemory(),graph.getTokenDesc());

           dartsEval.setMemory(refinedMem);
        }
        catch (Exception e){
            System.err.println("DNN-CSDF memory evaluation refinement error");
        }
    }
    ///////////////////////////////////////////////////////////////
    ////                      private methods                  ////


    /**
     * Evaluate buffer sizes of the DNN
     * @param dnn dnn graph to be evaluated
     * @return internal memory of the DNN graph evaluation
     * @throws Exception if an error occurs
     */
    public Long evalBufferSizesMemory(Network dnn){
        long result = 0;
         for(Connection c: dnn.getConnections()){
             try {
                 result += evaluateBufferSize(c);
             }
             catch (Exception e){
                 System.err.println(c.getSrcName() +" DNN-CSDF buffer evaluation error");
                 return 0l;
             }
        }
      return result;
    }


    /**
     * Evaluate buffer size for DNN connection
     * @param con connection
     * @return evaluated buffer size
     */
    private Long evaluateBufferSize(Connection con){
        long osize = con.getSrc().getOutputFormat().getElementsNumber();
        long isize = con.getDest().getInputFormat().getElementsNumber();

        long total = osize;
        if(osize!=isize)
            total+=osize;
        return total;
    }

    /**
     * Evaluate internal memory of the CSDF graph
     * @param dnn dnn graph to be evaluated
     * @return internal memory of the CSDF graph evaluation
     * @throws Exception if an error occurs
     */
    public Long evalInternalMemory(Network dnn){
        long result = 0;
         for(Layer l: dnn.getLayers()){
             try {
                 result += evalInternalMemory(l);
             }
             catch (Exception e){
                 System.err.println(l.getName() +" DNN-CSDF internal memory evaluation error");
                 return 0l;
             }
        }
      return result;
    }

     /**
     * Evaluate internal memory of the CNN layer
     * @param layer CNN layer to be evaluated
     * @return internal memory of the CNN layer evaluation
     * @throws Exception if an error occurs
     */
    public long evalInternalMemory(Layer layer) throws Exception{
        Operator op = layer.getNeuron().getOperator();
        if(op==null){
            layer.initOperator();
        }
        long result = evaluateOperator(op);
        return result;
    }




    /**
     * Evaluate internal memory of the CSDF graph
     * @param graph graph to be evaluated
     * @return internal memory of the CSDF graph evaluation
     * @throws Exception if an error occurs
     */
    public Double evalInternalMemory(CSDFGraph graph){
        Long lresult = 0l;
         for(Object node:graph.getNodeList()){
             try {
                 lresult += evalInternalMemory((CSDFNode) node);
             }
             catch (Exception e){
                 System.err.println(((CSDFNode) node).getName() +" CSDF node internal memory evaluation error, node skipped");
             }
        }
         String tokenDesc = graph.getTokenDesc();
        int typeSize = MeasurementUnitsConverter.typeSizeBytes(tokenDesc);
        double result = lresult.doubleValue() * typeSize;
        return result;
    }

    /**
     * Evaluate internal memory of the CSDF graph
     * @param graph graph to be evaluated
     * @return internal memory of the CSDF graph evaluation
     * @throws Exception if an error occurs
     */
    public Double evalBufferSizes(CSDFGraph graph){
        double result;
        Long lresult = 0l;
        CSDFEdge csdfEdge;
        for(Edge edge: graph.getEdgeList()){
            try {
                csdfEdge = (CSDFEdge)edge;
                lresult += evalBufferSize(csdfEdge);
            }
            catch (Exception e){
                System.err.println(edge.getName() +" CSDF edge buffer size evaluation error, buffer evaluation skipped");
            }
        }
        String tokenDesc = graph.getTokenDesc();
        int typeSize = MeasurementUnitsConverter.typeSizeBytes(tokenDesc);
        result = lresult.doubleValue() * typeSize;

        return result;
    }

    /**
     * Evaluate buffer size for CSDFG edge
     * @param edge CSDFG edge
     * @return buffer size for CSDFG edge
     */
    public Long evalBufferSize(CSDFEdge edge){

        Integer maxRate = 0;
        Vector<IndexPair> outRates = edge.getSrc().getRates();
        Vector<IndexPair> inRates = edge.getDst().getRates();

        for(IndexPair outRate: outRates){
            if(outRate.getFirst()>maxRate)
                maxRate = outRate.getFirst();
        }

        for(IndexPair inrate: inRates){
            if(inrate.getFirst()>maxRate)
                maxRate = inrate.getFirst();
        }

        //System.out.println(edge.getName() + " BUF: " + maxRate.doubleValue() * 4.0/1000000.0);
        return maxRate.longValue();
    }

    /**
     * Evaluate internal memory of the CSDF node
     * @param node SDF graph node to be evaluated
     * @return internal memory of the CSDF node evaluation
     * @throws Exception if an error occurs
     */
    public Long evalInternalMemory(CSDFNode node) throws Exception{
        long result = 0l;
       // for(MemoryUnit mu: node.getMemoryUnits()){
         //   result+=evalMemoryUnit(mu);
       // }

        Operator op = node.getOperator();
        result+= evaluateOperator(op);

       // System.out.println(node.getName()+" mem: " + (result/1000000.0));

        return result;
    }



    /**
     * Evaluate DNN/CSDF operator
     * @param op DNN/CSDF operator
     * @return DNN/CSDF operator memory evaluation
     */
    public long evaluateOperator(Operator op){
        /** long result = 0l;
        if(op!=null){
            if(op.hasIntegerParams())
                result+=op.getIntParams().size() * typeSize("int");
            if(op.hasTensorParams()){
                for (Tensor t: op.getTensorParams().values()) {
                    if (!Tensor.isNullOrEmpty(t))
                        result += t.getElementsNumber() * typeSize(_tensorType);
                }
            }
            if(op.hasFloatParams())
                result+=op.getStringParams().size() * typeSize("float");

            if(op.hasStringParams())
                result+=op.getStringParams().size() * typeSize("string");
        }*/

        long result = 0l;
        Integer keyId;
        if (op != null) {
            if (op.hasTensorParams()) {
                for (Tensor t : op.getTensorParams().values()) {
                    if (!Tensor.isNullOrEmpty(t)) {
                        result += t.getElementsNumber();

                        //System.out.println("to_eval: "+ t.toString());
                    }
                }
            }
        }

        return result;
    }


    /**
     * Evaluate memory unit size
     * @param mu memory unit
     * @return memory unit internal memory evaluation
     * @throws Exception if an error occurs
     */
    private int evalMemoryUnit(MemoryUnit mu) throws Exception{
        int typeSize = MeasurementUnitsConverter.typeSizeBytes(mu.getTypeDesc());
        int memUnitSize;
        if(mu.isUnitParam())
            memUnitSize = 1;
        else {
            memUnitSize = mu.getShape().getElementsNumber();
            if(mu.getShape()==null)
                System.err.println("mu: "+mu.getName() + " has null shape!");
        }

        return memUnitSize * typeSize;
    }

   /**
     * Refine memory value, required for CSDF model buffers
     * @param tokens total buffer sizes in tokens
     * @param tokenDesc description of the type, equal to one token
     * @return memory value, required for CSDF model buffers in bytes
     * @throws Exception if an error occurs*/

    private double refineTotalBufferSizes(double tokens, String tokenDesc) throws Exception{
         int typeSize = MeasurementUnitsConverter.typeSizeBytes(tokenDesc);
         return tokens * typeSize;
    }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static CSDFGMemoryRefiner _refinedMemEvaluator = new CSDFGMemoryRefiner();
    /** TODO: make a parameter!*/
    private String _tensorType = "float";
}