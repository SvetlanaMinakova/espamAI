package espam.operations.refinement;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.csdf.CSDFSupportResolver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Refine CSDF model with timing parameters
 * NOTE: reading/writing/execution times should be aligned to one length!
 */
    /////////////////////////////////////////////////////////////////////
    ////                   public methods                           ////
    public class CSDFTimingRefiner{

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
        if(x.getOperation() == null)
            return _readOpRate(x);

        if(x.getOperation().toLowerCase().equals("read") ||x.getOperation().toLowerCase().equals("write"))
            return _readOpRate(x);

        if(outPorts.size()==0)
            return null;


        /**TODO arbitrary CSDF node can have several outputs! For now only
         * TODO single out port is taken into account */
        Vector<IndexPair> outRates = outPorts.firstElement().getRates();
        Vector<Integer> unrolledOutRates = CSDFSupportResolver.indexPairsToVec(outRates);

        Integer opTime = getOpTime(x.getOperation());
        opTime *= (Integer)x.getOperationRepetitionsNumber();
        /** TODO for this estimation a program inside of the node
         * TODO is considered to be sequental!*/
        opTime*= (Integer)x.getKernelsNum();

        for(Integer rate: unrolledOutRates){
            if (rate>0)
                wcet.add(opTime);
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
     * Get time, required for an operation
     * First, the operation is looked up in a basicOperationsTiming table
     * If the whole operation is not found in a table, but contains
     * basic description and parameters for extrapolation, the exec time
     * is extrapolated
     * If the teh whole operation is not found and the extrapolation is not possuble,
     * an exception invoked
     * @param operation operation description
     * @return
     */
    public Integer getOpTime(String operation){
        /**TODO REPLACE BY 0 WHEN R/W VALUES WILL BE ADDED*/
        if(operation==null)
            return 1;
        if(operation=="null")
            return 1;
        Integer time = _basicOperationsTiming.get(operation.toLowerCase());

        if(time == null)
            try { time = _extrapolateParametrizedOperationTime(operation); }
            catch (Exception e){
            System.out.println(operation + " unknown execution time. Default time = 1 is set for " + operation);
            time = 1;
            }
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
    public void initBasicOperationsDefault(){
        _basicOperationsTiming = new HashMap<>();
        /** read /write ops*/
        _basicOperationsTiming.put("read",1);
        _basicOperationsTiming.put("write",1);
        _basicOperationsTiming.put("input",1);
        _basicOperationsTiming.put("output",1);

        /** some specific ops*/
        _basicOperationsTiming.put("addconst",1);
        _basicOperationsTiming.put("avgpool",2);
        _basicOperationsTiming.put("conv",3);
        _basicOperationsTiming.put("concat",1);
        _basicOperationsTiming.put("gemm",2);
        _basicOperationsTiming.put("lrn",3);
        _basicOperationsTiming.put("maxpool",2);
        _basicOperationsTiming.put("matmul",2);
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
     * TODO REFACTORING
     * Extrapolate time for parametrized basic operation,
     * @param operation parametrized operation description
     * @throws Exception if an error occurs
     */
    private Integer _extrapolateParametrizedOperationTime(String operation) throws Exception{
        int paramStart;
        if(operation.contains("("))
            paramStart = operation.indexOf("(");
       else
           paramStart = operation.length();
        String basicOpName = operation.toLowerCase().substring(0,paramStart);
        if(basicOpName.contains("matmul")||basicOpName.contains("gemm")
                ||basicOpName.contains(("dense"))){
            return _extrapolateDenseOpTime(operation,paramStart);
        }

        Integer basicOpTime = _basicOperationsTiming.get(basicOpName);
        if(basicOpTime==null)
            throw new Exception("unknown operation: " + basicOpName);
        if(basicOpName.contains("conv")||basicOpName.contains("pool"))
            return _extrapolateCNNOperationTime(basicOpTime,operation);

       if(basicOpName.contains("lrn") || basicOpName.contains("relu")||
                basicOpName.contains("sigm")||basicOpName.contains("softmax")||
                basicOpName.contains("thn")) {
            return _extrapolateNonLinOpTime(_basicOperationsTiming.get(basicOpName),operation);

        }


        throw new Exception("Exec_time extrapolation error, unknown operation: " + operation);
    }

    /**
     * Extrapolate operation time for a single parametrized operation
     * Alg complexity ~ O(k_w * k_h), where
     * following complexity factors are used:
     *  - tensor_elements = total number of input elements for CNN operation
     *  - k_w and k_h - CNN operation kernel width and height
     * @param cnnOpDesc parametrized operation description
     * @return extrapolated operation time
     */
    private Integer _extrapolateCNNOperationTime(Integer execTime, String cnnOpDesc) throws Exception{
        Integer complexityFactor = 1;
        Vector<Integer> intParams = _getIntParams(cnnOpDesc);
        for(Integer intParam: intParams)
            complexityFactor*= intParam;

        return execTime * (Integer)complexityFactor;
    }

    /**
     * Extrapolate Dense operation execution time
     * TODO refine formulas
     * Dense operation is composed of:
     * - MatMul or Gemm [required]
     * - NonLinearity [optional]
     * algorithm complexity for MatMul ~ O(n3)
     * @param denseOpDesc whole operation description
     * @return Extrapolated Dense operation execution time
     */
    private Integer _extrapolateDenseOpTime(String denseOpDesc, int bracketId) throws Exception{

        Integer matMulTime;
        Integer nonLinTime = 0;
        if(denseOpDesc.toLowerCase().contains("gemm"))
           matMulTime = _basicOperationsTiming.get("gemm");
        else matMulTime = _basicOperationsTiming.get("matmul");

        /** if the nonlinear part is not null, add it to matmul time*/
        if(denseOpDesc.contains("_")){
            int nonlinStart = denseOpDesc.indexOf("_");
            String nonlinDesc =denseOpDesc.toLowerCase().substring(nonlinStart+1,bracketId);
                nonLinTime = _basicOperationsTiming.get(nonlinDesc);
        }

        Vector<Tensor> tensorParams = _getTensorParams(denseOpDesc);
        int elemsNum =0;
        for(Tensor tensorParam:tensorParams){
            elemsNum+=tensorParam.getElementsNumber();
        }

        matMulTime*=elemsNum;
        nonLinTime*=elemsNum;

        return matMulTime + nonLinTime;
    }

    /**
     * Alg complexity ~ O(n). If Nonlinear node is not parametrized,
     * it processes one token per time.
     * @param exec_time one non-linear operation execution time
     * @param nonlinOpDesc one non-linear operation description
     * @return extrapolated nonlinear operation time
     * @throws Exception
     */
    private Integer _extrapolateNonLinOpTime(Integer exec_time, String nonlinOpDesc) throws  Exception{
        Integer totalTokens = 0;
        Vector<Integer> intParams = _getIntParams(nonlinOpDesc);
        if(intParams.size()==0)
            totalTokens=1;
        for(Integer intParam: intParams)
            totalTokens+=intParam;

        return exec_time * totalTokens;
    }

     /**
     * Get list of integer parameters from operation description
     * @param operationDesc operation description
     * @return list of integer parameters extracted from operation description
     */
    private Vector<Tensor>_getTensorParams(String operationDesc) throws Exception{
        Vector<Tensor> params = new Vector<>();
        Integer inBracketId = operationDesc.indexOf("(");
        Integer outBracketId = operationDesc.indexOf(")");
        if(inBracketId==null||outBracketId ==null)
            throw new Exception("parameters parsing error: no brackets found");

        String paramsSubstring = operationDesc.substring(inBracketId+1,outBracketId-1);
        String[] tensorSTRParams = paramsSubstring.split(",");

        for(String strParam:tensorSTRParams) {
            Tensor tensor = _parseTensor(strParam);
            params.add(tensor);
        }

        return params;
    }

    /**
     * Parse tensor-parameter description
     * @param description tensor-parameter description
     * @return Tensor, deserialized from the string description
     */
    private Tensor _parseTensor(String description){
        Tensor result = new Tensor();
        String[] tensorParts = description.split("_");
        for(String tensorPart: tensorParts){
            int dim = Integer.parseInt(tensorPart);
            result.addDimension(dim);
        }
        return result;

    }

    /**
     * Get list of integer parameters from operation description
     * @param operationDesc operation description
     * @return list of integer parameters extracted from operation description
     */
    private Vector<Integer>_getIntParams(String operationDesc) throws Exception{
        Vector<Integer> params = new Vector<>();
        if(!operationDesc.contains("("))
            return params;

        Integer inBracketId = operationDesc.indexOf("(");
        Integer outBracketId = operationDesc.indexOf(")");

        String paramsSubstring = operationDesc.substring(inBracketId + 1,outBracketId);
        String[] strParams = paramsSubstring.split("_");
        for(String strParam:strParams)
            params.add(Integer.parseInt(strParam));

        return params;
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
            _basicOperationsTiming.put(newOp.getKey(),newOp.getValue());
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
