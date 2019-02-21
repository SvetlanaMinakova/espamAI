package espam.operations.refinement;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;

/**
 * Class evaluates CSDF model, refined with memory
 */
public class RefinedCSDFEvaluator {
    ////////////////////////////////////////////////////////////////
    ////    private constructor for singleton method           ////
    private RefinedCSDFEvaluator(){}


    ///////////////////////////////////////////////////////////////
    ////                      public methods                  ////

    /**
     * Get refined memory evaluator instance
     * @return refined memory evaluator instance
     */
    public static RefinedCSDFEvaluator getInstance() {
        return _refinedMemEvaluator;
    }

     /**
     * Refine memory evaluation, provided by DARTS
     * @param dartsEval DARTS evaluation of CSDF graph
     */
    public void refineTimingEval(CSDFEvalResult dartsEval, double execTimeScale){
        try{
            dartsEval.setPerformance(dartsEval.getPerformance() * execTimeScale);
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
     * Evaluate internal memory of the CSDF graph
     * @param graph graph to be evaluated
     * @return internal memory of the CSDF graph evaluation
     * @throws Exception if an error occurs
     */
    public int evalInternalMemory(CSDFGraph graph) throws Exception{
        int result = 0;
         for(Object node:graph.getNodeList()){
            result += evalInternalMemory((CSDFNode)node);
        }
      return result;
    }

    /**
     * Evaluate internal memory of the CSDF node
     * @param node SDF graph node to be evaluated
     * @return internal memory of the CSDF node evaluation
     * @throws Exception if an error occurs
     */
    public int evalInternalMemory(CSDFNode node) throws Exception{
        int result = 0;
        for(MemoryUnit mu: node.getMemoryUnits()){
            result+=evalMemoryUnit(mu);
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
        int typeSize = typeSize(mu.getTypeDesc());
        int memUnitSize;
        if(mu.isUnitParam())
            memUnitSize = 1;
        else
            memUnitSize = mu.getShape().getElementsNumber();

        return memUnitSize * typeSize;
    }

    /**
     * Refine memory value, required for CSDF model buffers
     * @param tokens total buffer sizes in tokens
     * @param tokenDesc description of the type, equal to one token
     * @return memory value, required for CSDF model buffers in bytes
     * @throws Exception if an error occurs
     */
    private double refineTotalBufferSizes(double tokens, String tokenDesc) throws Exception{
         int typeSize = typeSize(tokenDesc);
         return tokens * typeSize;
    }

    /**
     * Get memory size of data types in bytes
     * @param valueDesc data type description
     * @return memory size of data types in bytes
     * @throws Exception if an error occurs
     */
    public int typeSize(String valueDesc) throws Exception{
        if(valueDesc.contains("8"))
            return 1;
        if(valueDesc.contains("16"))
            return 2;
        if(valueDesc.contains("32"))
            return 4;
        if(valueDesc.contains("64"))
            return 8;
        if(valueDesc.contains("128"))
            return 16;
        /** standard shortcuts*/
        /** TODO check*/
        if(valueDesc.equals("boolean"))
            return 1;
        if(valueDesc.equals("int"))
            return 2;
        if(valueDesc.equals("float"))
            return 4;
        throw new Exception("Unknown data type!");
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of memory evaluator*/
    private static RefinedCSDFEvaluator _refinedMemEvaluator = new RefinedCSDFEvaluator();
}


