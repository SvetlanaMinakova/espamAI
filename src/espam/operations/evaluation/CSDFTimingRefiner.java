package espam.operations.evaluation;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.parser.json.csdf.CSDFSupportResolver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Compute time, required for CSDF graph execution
 * The time, required for CSDF graph execution, is computed by DARTS
 * and then scaled by to the abstract timeUnit size
 */
    /////////////////////////////////////////////////////////////////////
    ////                   public methods                           ////
    public class CSDFTimingRefiner{

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

      /** Timing refiner is a singleton so its constructor is protected*/
    protected CSDFTimingRefiner(){
        initBasicOperationsDefault();
    }

    public static CSDFTimingRefiner getInstance(){
        return _refiner;
    }

    /**TODO Refactoring
     * Get execution times for CSFG graph nodes
     * TODO for nown exec time is calculated as operation_execution_time
     * TODO add reading/writing times?? Is it considered that all input
     * TODO data is coming in parallel
     * TODO from different ports?
     * @param  x A Visitor Object.
     */

    public HashMap<CSDFNode,Vector<Integer>> getExecTimes(CSDFGraph x) {
        HashMap<CSDFNode,Vector<Integer>>execTimes = new HashMap<>();

        Iterator i;
        /**Visit all nodes*/
        Vector<CSDFNode> nodes = x.getNodeList();
        CSDFNode node;
        i = nodes.iterator();
        while (i.hasNext()) {
            node = (CSDFNode) i.next();
            try {
               Vector<Integer> execTime = _getExecTime(node);
               execTimes.put(node,execTime);
            }
            catch (Exception e){
            System.err.println("Exec time refinement error for node " +
                   node.getUniqueName() + " . " + e.getMessage());
                   execTimes.put(node,getDefaultExecTime(node.getLength()));
            }
        }
        return execTimes;
    }

    /**
     * Get default execution time
     * @return default execution time
     */
    public Integer getDefaultExecTime(){
        return 1;
    }

     /**
     * Get default execution time
     * @return default execution time
     */
    public Vector<Integer> getDefaultExecTime(int len){
        Vector<Integer> wcet = new Vector<>();
          for(int i=0; i<len;i++)
                wcet.add(getDefaultExecTime());

        return wcet;
    }

    /** TODO refactoring
     *  TODO all port rates should be aligned??
     * Refine worst-case execution time.
     * Number of operation repetitions number = firings number =
     * number of non-zero writing operations (it is considered that
     * node writes only after it executed an operation)
     * @return worst-case execution time for operation
     */
    private Vector<Integer> _getExecTime(CSDFNode x) throws Exception{
        Vector<Integer> wcet = new Vector<>();
        Vector<CSDFPort> outPorts = x.getNonOverlapHandlingOutPorts();
        if(x.getFunction() == null)
            return _readOpRate(x);

        if(x.getFunction().toLowerCase().equals("read") ||x.getFunction().toLowerCase().equals("write"))
            return _readOpRate(x);

        if(outPorts.size()==0)
            return null;


        /**TODO arbitrary CSDF node can have several outputs! For now only
         * TODO single out port is taken into account */
        Vector<IndexPair> outRates = outPorts.firstElement().getRates();
        Vector<Integer> unrolledOutRates = CSDFSupportResolver.indexPairsToVec(outRates);

        Integer opTime = getOpTime(x.getOperator());
        opTime *= (Integer)x.getOperationRepetitionsNumber();


        //opTime*= (Integer)x.getKernelsNum();

       // if(x.getName().contains("node_Conv4_split"))
         //   System.out.println("kernels_conv_4_split "+x.getKernelsNum());

        for(Integer rate: unrolledOutRates){
            if (rate>0)
                wcet.add(opTime);
            else
                wcet.add(0);
        }
        return wcet;
    }

    /**TODO REFACTORING: ADD R/W TO EACH NODE??*/
    /**
     * Get rates for a node which performs only reading operation
     * @param x a node which performs only reading operation
     * @return rates for a node which performs only reading operation
     */
    private Vector<Integer> _readOpRate(CSDFNode x){
        Vector<Integer> wcet = new Vector<>();
        Integer readTime = _basicOperationsTiming.get("read");
        if(readTime==null)
            return null;

        for(int i=0; i< x.getLength();i++)
                wcet.add(readTime);
        return wcet;
    }


    /**
     * Get time, required for an operator
     * First, the operator time is looked up in a basicOperationsTiming table
     * If the whole operation is not found in a table, but Operator has
     * _basic description and time complexity, the time is computed as
     * time for _basic operator * time complexity.
     * @param op Operator
     * @return
     * */

    public Integer getOpTime(Operator op){

        String operation = op.getName().toLowerCase();
        String basicOperation = op.getBasic().toLowerCase();
        Integer defaultBasicTime = 1;
        Integer minTimeComplexity = 1;

        if(operation==null)
            return 1;
        if(operation=="null")
            return 1;
        Integer time = _basicOperationsTiming.get(operation);
        if(time!=null)
            return time;

        time = _basicOperationsTiming.get(basicOperation);
        if(time==null)
            time = defaultBasicTime;

        Integer timeComplexity = op.getTimeComplexity();
        if(timeComplexity == null)
            timeComplexity = minTimeComplexity;
        timeComplexity = Math.max(timeComplexity,minTimeComplexity);

        time *= timeComplexity;

        return time;
    }

    /**
     * Get list of the supported operators
     * @return list of the supported operators
     */
    public Vector<String> getSupportedOperatorsList(){
        Vector<String> supportedOperatorsList = new Vector<>();
        for(Map.Entry<String,Integer> op: _basicOperationsTiming.entrySet()){
            supportedOperatorsList.add(op.getKey());
        }

        return supportedOperatorsList;
    }

    /**
     * Initialize basic operations list by dummy default values
     */
    public void initRWOperationsDefault() {
        _basicOperationsTiming = new HashMap<>();
        /** read /write ops*/
        _basicOperationsTiming.put("read", 1);
        _basicOperationsTiming.put("write", 1);
        _basicOperationsTiming.put("input", 1);
        _basicOperationsTiming.put("output", 1);
    }

    /**
     * Initialize basic operations list by dummy default values
     */
    public void initBasicOperationsDefault(){
        _basicOperationsTiming = new HashMap<>();
        /** read /write ops*/
        _basicOperationsTiming.put("read",1);
        _basicOperationsTiming.put("write",1);
        _basicOperationsTiming.put("input",1);
        _basicOperationsTiming.put("output",1);

        /** some specific ops*/
        _basicOperationsTiming.put("addconst",1);
        _basicOperationsTiming.put("avgpool",1);
        _basicOperationsTiming.put("conv",1);
        _basicOperationsTiming.put("concat",1);
        _basicOperationsTiming.put("gemm",1);
        _basicOperationsTiming.put("lrn",1);
        _basicOperationsTiming.put("maxpool",1);
        _basicOperationsTiming.put("matmul",1);
        _basicOperationsTiming.put("relu",1);
        _basicOperationsTiming.put("reshape",1);
        _basicOperationsTiming.put("sigm",1);
        _basicOperationsTiming.put("softmax",1);
        _basicOperationsTiming.put("thn",1);
        _basicOperationsTiming.put("none",0);
        _basicOperationsTiming.put("dropout",1);
        /**TODO refactoring*/
        _basicOperationsTiming.put(null,1);
    }

    /**
     * Set timing of basic supported operations
     * @return timing of basic supported operations
     */
    public HashMap<String, Integer> getBasicOperationsTiming() {
        return _basicOperationsTiming;
    }
      /**
     * Set timing of basic supported operations
     * @param basicOperationsTiming timing of basic supported operations
     */
    public void setBasicOperationsTiming(HashMap<String, Integer> basicOperationsTiming) {
        this._basicOperationsTiming = basicOperationsTiming;
    }

     /**
      * TODO check overwriting!
     * Update timing of basic supported operations:
     *     - Overwrite operators, mentioned in both old and new specifications.
     *     - Add operators, mentioned only in new specification
     * @param newOperationsTiming new list with timing of basic supported operations
     */
    public void updateBasicOperationsTiming(HashMap<String, Integer> newOperationsTiming) {
        for(Map.Entry<String,Integer> newOp: newOperationsTiming.entrySet())
            _basicOperationsTiming.put(newOp.getKey().toLowerCase(),newOp.getValue());
    }

    /**
     * Print current basic operations timing
     */
    public void printBasicOperationsTiming(){
        for(HashMap.Entry<String,Integer> op: _basicOperationsTiming.entrySet()){
            System.out.println(op.getKey() + " : " + op.getValue());
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /** timing of basic supported operations*/
    @SerializedName("operators")private HashMap<String,Integer> _basicOperationsTiming = new HashMap<>();

    /** refiner singletone*/
    private transient static CSDFTimingRefiner _refiner = new CSDFTimingRefiner();
}
