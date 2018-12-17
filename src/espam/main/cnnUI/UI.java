package espam.main.cnnUI;

import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalError;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.main.Config;
import espam.parser.json.refinement.TimingSpecParser;
import onnx.ONNX;
import espam.interfaces.python.Espam2DARTS;
import espam.main.ExtensionFilter;
import espam.operations.codegeneration.sesame.SesameSDFGVisitor;
import espam.operations.refinement.CSDFGEnergyRefiner;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.operations.refinement.RefinedCSDFEvaluator;
import espam.operations.transformations.CNN2CSDFGraphConverter;
import espam.operations.transformations.cnn_model_transformations.CNNTransformer;
import espam.parser.json.JSONParser;
import espam.parser.onnx.InferenceDNNOptimizer;
import espam.parser.onnx.ONNX2CNNConverter;
import espam.utils.fileworker.DotFileWorker;
import espam.utils.fileworker.FileWorker;
import espam.utils.fileworker.ONNXFileWorker;
import espam.visitor.json.CNNJSONVisitor;
import espam.visitor.json.CSDFGraphJSONVisitor;
import espam.visitor.xml.csdf.CSDFGraphXMLVisitor;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

/** Class include CNN-espam user interface, both command-line and API*/

/********************************************/
/** Command-line CNN-espam user interface **/

 /** Designed for calling cnnespam from the command line
 *
 * Input: path to DNN model in .onnx or .json format + options (see below)
 *       or path to CSDF model in .json format + options (see below)
 *
 * Actions:
 * -evaluation : Evaluation of DNN model in terms of power/performance
 * -generation : Generation of files intended for CSDF-models
 *   processing tools such as Sesame/DARTS/SDF3
 *
 * Output:
 * - DNN model evaluation results in .json format [optional]
 * - Files, generated from DNN model and intended for CSDF-models
 *   processing tools such as Sesame/DARTS/SDF3
 *
 *
 *
     * the interface commands running include the following steps:
     * (1) Model reading: reading of one or several input models
  * . Admissible input models:
     *     DNN models in .onnx or .json format
     *     CSDF models in .json format
     *
     * (2) Model conversion:
     *     - conversion of input .onnx DNN model to internal Network model
     *        (initiated automatically after onnx model reading)
     *     - conversion of internal Network model to CSDF model:
     *        (initiated automatically, if evaluation flag if set or/and
     *        if one or multiple *-csdf output files generation flags are set)
     *
     * (3) Output files generation [optional]: generates for input model output files,
     *     according to set options
     *         - json      : DNN graph in .json format
     *         - png       : DNN graph in .png format and corresponding rendered image in .png format
     *         - sesame    : code templates for Sesame
     *         - json-csdf : CSDF graph in .json format for DARTS
     *         - xml-csdf  : CSDF graph in .xml fomat for SDF3
     *         - png-csdf  : CSDF graph in .png format and corresponding rendered image in .png format
     *
     * (4) Model evaluation [optional]: evaluates one or several input models
     *     in terms of power/performance, by means of the DARTS/SDF3 tool.
     *     TODO provide interface to SDF3 tool as well
     */


     /***************************************************************/
     /************************  Internal API  **********************/

     /** Internal API is designed for calling CNN-espam from java code
      *  For now only DNN model evaluation is supported by espam-cnn
      *
      *  Input: DNN model in .onnx, .json or espam.Network format
      *         or path to dnn model in .onnx or .json format
      *  Output: DNN model evaluation result as CSDFEvalResult class object
      *
      *  The API include the following steps:
      *  (1) DNN model reading and transformation to espam.Network format
      *     (if DNN model is provided in onnx format or as path)
      *
      *  (2) DNN model blocks splitting (if the model is block-based)
      *
      *  (3) DNN model transformation to CSDF model
      *
      *  (4) DNN model timing and memory refinement
      *
      *  (5) DNN model evaluation by means of DARTS power/performance
      *     evaluation tool and cnn-espam refined-model evaluator
      *
      * */

public class UI {

    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ///

    /**
     * returns the singleton instance of this class.
     * @return The instance value
     */
    public final static UI getInstance() {
       // _instance.clearFlags();
        return _instance;
    }

    ///////////////////////////////////////////////////////////////////
    ////   Internal jar API(calling of cnnespam from java code)    ///

    /*****************************************************************/
    /******************* DNN model evaluation************************/


      /**
     * Evaluate layer-based deep neural network in terms
     * of power and performance
     * @param dir directory with deep neural networks
     * in .json format to be evaluated
     * @return evaluation of the deep neural network in
     * terms of power and performance
     */
    public Vector<CSDFEvalResult> evaluateJsonDNNs(String dir, boolean neuronBased){
        Vector<String> absPaths = FileWorker.getAllFilePaths(dir,".json");
        Vector<CSDFEvalResult> results = new Vector<>();

        for(String absPath: absPaths) {
            try{

            if(_verbose)
                System.out.println(absPath+ " DNN evaluation...");

             Network dnn = _readJSONDNNModel(absPath);
             dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());

             CSDFEvalResult res = evaluate(dnn);
             results.add(res);

             if(_verbose) {
                 System.out.println(absPath + " eval finished. Result:  ");
                 System.out.println(res);
             }
            }
            catch (Exception e){
                System.err.println(absPath+" model error!");
            }
        }
        return results;
    }

    /**
     * Evaluate layer-based deep neural network in terms
     * of power and performance
     * @param dnn deep neural network to be evaluated
     * @return evaluation of the deep neural network in
     * terms of power and performance
     */
    public CSDFEvalResult evaluate(Network dnn){
        return evaluate(dnn,false);
    }

    /**
     * Evaluate deep neural network in terms of power and performance
     * @param dnn deep neural network to be evaluated
     * @param neuronBased if the start model is neuron based
     * @return evaluation of the deep neural network in
     * terms of power and performance
     */
    public CSDFEvalResult evaluate(Network dnn, boolean neuronBased){
        _eval = true;
        if(neuronBased)
            _dnnInitRepresentation = DNNInitRepresentation.NEURONBASED;
        else
            _dnnInitRepresentation = DNNInitRepresentation.LAYERBASED;
        if (dnn == null)
                return new CSDFEvalError(" null DNN model");
        try {
            CSDFGraph csdfg = _convertDNN2SDFG(dnn);
            _refineTiming(csdfg);

            CSDFEvalResult result = _evaluate(csdfg);
            return result;
        }
        catch (Exception e){
            return new CSDFEvalError(e.getMessage());
        }
    }

    /**
     * Evaluate deep neural network in terms of power and performance
     * @param dnn deep neural network to be evaluated
     * @param maxBlocks desired maximum number of blocks
     * @return evaluation of the deep neural network in
     * terms of power and performance
     */
    public CSDFEvalResult evaluate(Network dnn, Integer maxBlocks){
        return evaluate(dnn,maxBlocks,2);
    }

    /**
     * Evaluate deep neural network in terms of power and performance
     * @param dnn deep neural network to be evaluated
     * @param maxBlocks desired maximum number of blocks
     * @param splitChildrenNum number of child blocks to be generated after
     * the splitting of one dnn layer/block
     * @return evaluation of the deep neural network in
     * terms of power and performance
     */
    public CSDFEvalResult evaluate(Network dnn, Integer maxBlocks, Integer splitChildrenNum){
        _eval = true;
        _dnnInitRepresentation = DNNInitRepresentation.BLOCKBASED;

        if (dnn == null)
            return new CSDFEvalError(" null DNN model");

        _blocks = maxBlocks;
        _splitChildrenNum = splitChildrenNum;

        Network dnnToEval = dnn;

        if(_isTransformationRequired(dnn.countLayers())){
          //  Network dnnCopy = new Network(dnn);
            CNNTransformer transformer = new CNNTransformer(dnn);
            transformer.splitToBlocks(_blocks,_splitSafeCounter,_splitChildrenNum,_verbose);
           // dnnToEval = dnnCopy;
        }

        try {
            CSDFGraph csdfg = _convertDNN2SDFG(dnnToEval);
            _refineTiming(csdfg);

            CSDFEvalResult result = _evaluate(csdfg);
            return result;
        }
        catch (Exception e){
            return new CSDFEvalError(e.getMessage());
        }
    }

    /*****************************************************************/
    /****************    Files generation       *********************/
    /** TODO is there a need in files-generation internal api?     */




    ///////////////////////////////////////////////////////////////////
    ////   External jar UI (calling of cnnespam from the console)  ///

    /**
     * Run interface commands
     * @throws Exception if an error occurs
     */
    public void runCommands() throws Exception{
        try {

            if(_execTimesSpec!=null)
                _trySetExecTimesSpec();

            if(_energySpec!=null)
                _trySetEnergySpec();

            if (_multipleModels)
                _runCommandsMultipleInputs();
            else
                _runCommandsSingleInput();
        }
        catch (Exception e){
            if(_verbose)
                System.out.println("Commands running error!");
            if(_logErr)
                _logError(_curPhase + e.getMessage());
            throw (e);
        }
    }

    /**
     * Set energy specification, if it is possible.
     * Otherwise print an error.
     * TODO FINISH IMPLEMENTATION
     */
    private void _trySetEnergySpec(){
        try{}
        catch (Exception e){
            System.err.println("Energy specification set up error: "+ e.getMessage() +
            "default energy specification will be used");
        }
    }

    /**
     * Set execution times (wcet) specification, if it is possible.
     * Otherwise print an error.
     */
    private void _trySetExecTimesSpec(){
        try{ TimingSpecParser.parseTimingSpecTemplate(_execTimesSpec); }
        catch (Exception e){
            System.err.println("Execution times (wcet)  specification set up error: "+ e.getMessage() +
            "default energy specification will be used");
        }
    }

    /**
     * run commands for single input model
     * */
    private void _runCommandsSingleInput() throws Exception {
        Network network = null;
        CSDFGraph csdfg = null;

        _curPhase = "Model reading ";
        if (_verbose)
            System.out.println(_curPhase + "...");

        if (_inDnn) {
            if (_srcPath.endsWith("onnx"))
                network = _readONNXDNNModel(_srcPath);

            if (_srcPath.endsWith("json"))
                network = _readJSONDNNModel(_srcPath);

            if (network == null)
                throw new Exception(_srcPath + " DNN model reading error");

            network.setDataFormats(network.getInputLayer().getOutputFormat());

            if(_isTransformationRequired(network.countLayers())){
                CNNTransformer transformer = new CNNTransformer(network);
                transformer.splitToBlocks(_blocks,_splitSafeCounter,_splitChildrenNum,_verbose);
            }
        }

        if(_inCSDF){
            if(_srcPath.endsWith("json"))
            csdfg = _readSDFGJSONModel(_srcPath);
            if(csdfg == null)
                throw new Exception(_srcPath + " CSDF model reading error");
        }


        /**generate CSDFG from input DNN model if required*/
        if(_isSDFGenerationRequired()) {
            csdfg = _convertDNN2SDFG(network);
            _refineTiming(csdfg);
        }

        /** generate Sesame template */
        if (_sesame) {
               _edInterface.setRepetitionVector(csdfg);
            if(_inCSDF)
                SesameSDFGVisitor.callVisitor(csdfg,_dstPath + csdfg.getName()+"/",false);
            if(_inDnn)
                 SesameSDFGVisitor.callVisitor(csdfg,_dstPath + csdfg.getName()+"/",true);
        }

        if(_eval) {
            CSDFEvalResult result = _evaluate(csdfg);
            printResult(result);
        }

        if(_inDnn)
            _generateDNNOutputFiles(network);

        if(_generate_csdfg)
            _generateCSDFOutputFiles(csdfg);
    }

    /**
     * Run commands for multiple input models
     * */
    private void _runCommandsMultipleInputs() throws Exception{
         _curPhase = "Model reading ";
        if (_verbose)
            System.out.println(_curPhase + "...");
        Network [] dnns = null;
        CSDFGraph[] csdfgs = null;

        if (_inDnn) {
            dnns = _readAllDNNs(_srcPath);
            if(dnns==null)
                throw new Exception("no dnn models found in "+_srcPath);
             for(int i=0;i< dnns.length;i++){
                 Network dnn = dnns[i];
                 dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());
              if(_isTransformationRequired(dnn.countLayers())) {
                  CNNTransformer transformer = new CNNTransformer(dnn);
                  transformer.splitToBlocks(_blocks, _splitSafeCounter, _splitChildrenNum, _verbose);
              }
            }
        }

        if(_inCSDF){
           csdfgs = _readAllSDFGs(_srcPath);
           if(csdfgs == null)
                throw new Exception(_srcPath + " CSDF model reading error");
        }

        /**generate CSDFG from input DNN model if required*/
        if(_isSDFGenerationRequired()) {
            csdfgs = new CSDFGraph[dnns.length];
            for(int i=0;i< dnns.length;i++){
                csdfgs[i]= _convertDNN2SDFG(dnns[i]);
                _refineTiming(csdfgs[i]);
            }
        }

        /** generate Sesame template */
        if (_sesame) {
            for(CSDFGraph csdfg:csdfgs) {
                _edInterface.setRepetitionVector(csdfg);
                if (_inCSDF)
                    SesameSDFGVisitor.callVisitor(csdfg, _dstPath + csdfg.getName() + "/", false);
                if (_inDnn)
                    SesameSDFGVisitor.callVisitor(csdfg, _dstPath + csdfg.getName() + "/",true);
            }
        }

        if(_eval) {
            if(csdfgs==null)
                throw new Exception("evaluation error: null CSDF graphs list");

            CSDFEvalResult[] results = new CSDFEvalResult[csdfgs.length];
             for(int i=0;i< csdfgs.length;i++) {
                 results[i] = _evaluate(csdfgs[i]);
             }
            printResult(results);
        }


        if(_inDnn && dnns!=null){
            for(Network dnn:dnns){
                _generateDNNOutputFiles(dnn);
            }
        }

        if(_generate_csdfg) {
            if (csdfgs != null) {
                for (CSDFGraph csdfg : csdfgs) {
                    _generateCSDFOutputFiles(csdfg);
                }
            }
        }
    }

    /**
     * Generate DNN output files, if required
     * Admissible DNN output models:
     *  - json   : DNN graph as .json File
     *  - png    : DNN graph in .png format and corresponding rendered image in .png format
     *  - sesame : code templates for Sesame
     * @param dnn input DNN model
     */
    private void _generateDNNOutputFiles(Network dnn) throws Exception{
        if (dnn == null)
                throw new Exception(_srcPath + " DNN model output file generation error: null model");
             /** generate DNN output models*/
             String rootDst = _dstPath;
             _dstPath += dnn.getName();
             if (_json)
                _generateDNNJSON(dnn);
            if (_png)
               _renderDNN(dnn);
            _dstPath = rootDst;
    }

    /**
     * Generate CSDFG output files, if required
     * Admissible CSDFG output models:
     *  - json   : CSDF graph as .json File for DARTS
     *  - png    : CSDF graph in .png format and corresponding rendered image in .png format
     *  - xml    : CSDF graph as .xml File for SDF3
     * @param csdfg input CSDF graph model
     */
    private void _generateCSDFOutputFiles(CSDFGraph csdfg) throws Exception{
        if (csdfg == null)
            throw new Exception(_srcPath + "CSDF graph output file generation error: null graph");

        String rootDst = _dstPath;
        _dstPath += csdfg.getName();
        if(_csdfg_xml)
            _generateSDFGXML(csdfg);
        if(_csdfg_json)
            _generateSDFGJSON(csdfg);
        if(_csdfg_png)
            _renderSDFG(csdfg);
        _dstPath = rootDst;
    }

    /**
     * Update flag which shows if csdf model generation is required
     */
    private boolean _isSDFGenerationRequired(){
        if(_inCSDF)
            return false;
       return _eval||_sesame||_csdfg_json ||_csdfg_png||_csdfg_xml;
    }

    /**
     * Generate JSON description for DNN model
     * @throws Exception if an error occurs
     */
    private void _generateDNNJSON(Network network) throws Exception{
          _curPhase = network.getName() + " DNN json model generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
            CNNJSONVisitor.callVisitor(network,_dstPath +"/json/");
    }

    /**
     * Generate PNG image of DNN model
     * @throws Exception if an error occurs
     */
    private void _renderDNN(Network network) throws Exception{
        _curPhase = network.getName() + " DNN image generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
        DotFileWorker.render(network,_dstPath + "/img/",_imgW);
    }

    /**
     * Generate JSON description for SDF/CSDF model
     * @throws Exception if an error occurs
     * @param sdfg
     */
    private void _generateSDFGJSON(CSDFGraph sdfg) throws Exception{
         _curPhase = sdfg.getName() + " SDFG JSON generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
              CSDFGraphJSONVisitor.callVisitor(sdfg,_dstPath + "/sdfg/json/");
    }

    /**
     * Generate XML description for SDF/CSDF model
     * @throws Exception if an error occurs
     * @param sdfg
     */
    private void _generateSDFGXML(CSDFGraph sdfg) throws Exception{
         _curPhase = sdfg.getName() + " SDFG XML generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
              CSDFGraphXMLVisitor.callVisitor(sdfg,_dstPath + "/sdfg/xml/");
    }

    /**
     * Generate PNG image of DNN model
     * @throws Exception if an error occurs
     */
    private void _renderSDFG(CSDFGraph sdfg) throws Exception{
        _curPhase = sdfg.getName() + " SDFG image generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
        DotFileWorker.render(sdfg,_dstPath + "/sdfg/img/",_imgW);
    }

    /**
     * Read all CSDF graph models in 'json'format in the folder
     * TODO extend by .xml files reading
     * @param folder source folder
     * @return array with espam.CSDFG models, extracted from the source models folder
     * @throws Exception if an error occurs
     */
    private CSDFGraph[] _readAllSDFGs(String folder) throws Exception{
        File dir = new File(folder);
        /** find paths to all onnx models*/
        File [] jsonModelsPaths = dir.listFiles(new ExtensionFilter("json"));
        if(jsonModelsPaths==null)
             throw new Exception("No .json models found in " + folder);
        CSDFGraph[] sdfgs = new CSDFGraph[jsonModelsPaths.length];
        for(int i=0;i<sdfgs.length;i++) {
             sdfgs[i] = _readSDFGJSONModel(jsonModelsPaths[i].getPath());
        }

        return sdfgs;
    }

    /**
     * Read all DNN models in 'json' or 'onnx' format in the folder
     * @param folder folder with DNN models
     * @return array with espam.Network models, extracted from the source models folder
     * @throws Exception if an error occurs
     */
    private Network [] _readAllDNNs(String folder) throws Exception{
        File dir = new File(folder);
        int onnxModelsNum = 0;
        int jsonModelsNum = 0;
        /** find paths to all onnx models*/
        File [] onnxModelPaths = dir.listFiles(new ExtensionFilter("onnx"));
        if(onnxModelPaths!=null)
            onnxModelsNum = onnxModelPaths.length;

        /** find paths to all json models*/
        File [] jsonModelPaths = dir.listFiles(new ExtensionFilter("json"));
        if(jsonModelPaths!=null)
            jsonModelsNum = jsonModelPaths.length;

        if(onnxModelsNum==0 && jsonModelsNum==0)
            throw new Exception("No .json or .onnx models found in " + folder);

        Network [] dnns = new Network[onnxModelsNum + jsonModelsNum];

        /** read all onnx models*/
        if(onnxModelsNum>0) {
            for(int i=0;i<onnxModelsNum;i++)
                dnns[i] = _readONNXDNNModel(onnxModelPaths[i].getPath());
        }

        /** read all json models*/
        if(jsonModelsNum>0) {
            for(int i=0;i<jsonModelsNum;i++)
                dnns[i+onnxModelsNum] = _readJSONDNNModel(jsonModelPaths[i].getPath());
        }

        return dnns;
    }


    /**
     * Evaluate SDF graph
     * @param sdfg SDF graph
     * @throws Exception if an error occurs
     */
    private CSDFEvalResult _evaluate(CSDFGraph sdfg) throws Exception{
            _curPhase = "Model evaluation: DARTS interface call ";
            if(_verbose)
                System.out.println(_curPhase + "...");


            /**TODO REFINEMENT THROUGH THE JSON GENERATION??*/
            /** refine timing evaluation*/
            //CSDFTimingRefiner.getInstance().visitComponent(sdfg);

            CSDFEvalResult result = _edInterface.evaluateCSDFGraph(sdfg);

            if(_verbose)
                System.out.println("Evaluation finished");

            if(result instanceof CSDFEvalError) {
                throw new Exception(((CSDFEvalError) result).getErrorMessage());
            }
             _curPhase = "Model evaluation: Memory refinement ";
            if(_verbose)
                System.out.println(_curPhase + "...");
            /**refine memory evaluation*/
            RefinedCSDFEvaluator.getInstance().refineMemoryEval(sdfg,result);

            /**TODO REFINEMENT THROUGH THE JSON GENERATION??*/

            /** refine energy evaluation*/
          //  Double refinedEnergy = _getRefinedEnergy(sdfg);
          //  result.setEnergy(refinedEnergy);

            return result;
    }

    /**
     * Get refined energy evaluation for csdf graph
     * @param graph CSDF graph
     * @return refined energy evaluation for CSDF graph or null
     */
    private Double _getRefinedEnergy(CSDFGraph graph){
        try{
            HashMap<Integer,Double> nodesUtilization = _edInterface.getUtilizationVector(graph);
            Double refinedEnergy = CSDFGEnergyRefiner.getInstance().getRefinedEnergy(graph,nodesUtilization);
            return refinedEnergy;
        }
        catch (Exception e){
            System.err.print("Energy refinement error: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Read dnn model
     * @param modelPath path to dnn model
     */
    private Network _readONNXDNNModel(String modelPath) throws Exception{
            ONNX.ModelProto onnxModel = ONNXFileWorker.readModel(modelPath);
            _curPhase = "Model conversion: onnx to espam.Network ";
            if(_verbose)
                System.out.println(_curPhase + "...");
            Network espamNetwork = ONNX2CNNConverter.convertModel(onnxModel);
            return espamNetwork;
    }

    /**
     * Read dnn model
     * @param modelPath path to dnn model
     */
    private Network _readJSONDNNModel(String modelPath) throws Exception{
         String json = FileWorker.read(modelPath);
         Network espamNetwork = (Network) JSONParser.getInstance().fromJson(json,Network.class);
         return espamNetwork;
    }

    /**
     * Read sdf graph model
     * @param modelPath path to sdf graph model
     * @return
     * @throws Exception
     */
    private CSDFGraph _readSDFGJSONModel(String modelPath) throws Exception{
        String json = FileWorker.read(modelPath);
        CSDFGraph sdfg = (CSDFGraph)JSONParser.getInstance().fromJson(json,CSDFGraph.class);
        return sdfg;
    }

    /**
     * Convert deep neural network model to csdf graph
     * @return deep neural network model represented as a csdf-graph
     */
    private CSDFGraph _convertDNN2SDFG(Network network){
        _curPhase = "Model conversion: espam.Network to CSDF Model ";
            if(_verbose)
                System.out.println(_curPhase + "...");
        CSDFGraph sdfg;
        if(_dnnInitRepresentation.equals(DNNInitRepresentation.NEURONBASED))
            sdfg = cnn2CSDFGraphConverter.buildGraph(network);
        else
            sdfg = cnn2CSDFGraphConverter.buildGraphLayerBased(network);
        return sdfg;
    }

    /**
     * TODO Refine timing parameters of csdf graph
     * @param graph CSDF graph to be refined
     */
    private void _refineTiming(CSDFGraph graph){
        //CSDFTimingRefiner.getInstance().visitComponent(graph);
    }

     /**
     * TODO Refine timing parameters of csdf graph
     * @param operatorsExecTimes hashmap of format
     * key: Name_of_the_operator, value: execution_time
     * example: "Conv:5: means Conv operation takes 5 time units
     */
    private void _refineTiming(HashMap<String,Integer> operatorsExecTimes){
        CSDFTimingRefiner.getInstance().setBasicOperationsTiming(operatorsExecTimes);
       // CSDFTimingRefiner.getInstance().visitComponent(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  getters and setters                      ///

    /**
     * Get current UI running phase
     * @return current UI running phase
     */
    public String getCurPhase() {
        return _curPhase;
    }

    /**
     * Get path to source model(s)
     * @return path to source model(s)
     */
    public String getSrcPath() { return _srcPath; }

    /**
     * Set path to source model(s)
     * @param srcPath path to source model(s)
     */
    public void setSrcPath(String srcPath) {



        this._srcPath = srcPath;
    }


    /**
     * Set path to destination model(s)
     * @param dstPath path to destination model(s)
     */
    public void setDstPath(String dstPath) {
        this._dstPath = dstPath;
    }

    /**
     * Get path to destination model(s)
     * @return path to destination model(s)
     */
    public String getDstPath() { return _dstPath; }


    /**
     * Print single evaluation result
     * @param result single evaluation result
     */
    private void printResult(CSDFEvalResult result){
        if(result instanceof CSDFEvalError)
                System.out.println(((CSDFEvalError) result).getErrorMessage());
        System.out.println(result.toJSON());
    }

    /**
     * Print single evaluation result
     * @param results multiple evaluation results
     */
    private void printResult(CSDFEvalResult [] results){
        for(CSDFEvalResult result: results){
            if(result instanceof CSDFEvalError)
                     System.out.println(((CSDFEvalError) result).getErrorMessage());
        }

        StringBuilder resBuilder = new StringBuilder("{ \"evaluation_cases\": [");
        if(results!=null) {
            for(int i = 0; i<results.length; i++){
            results[i].setId(i);
                resBuilder.append(results[i].toJSON());

            if(i<results.length-1)
                resBuilder.append(",");
            }
        }
        resBuilder.append("]}");
        System.out.println(resBuilder.toString());
    }


    /**
     * Get number of DNN models for evaluation
     * @return  number of DNN models for evaluation
     */
    public int getModelsToEval() {
        return _modelsToEval;
    }

    /**
     * Set number of DNN models for evaluation
     * @param modelsToEval number of DNN models for evaluation
     */
    public void setModelsToEval(int modelsToEval){
        _modelsToEval = modelsToEval;
    }

    /**
     * Set verbose flag
     * @param flag verbose flag
     */
    public void setVerboseFlag(boolean flag){
        _verbose = flag;
    }

    /**
     * Check, if source model should be evaluated
     * @return true,if source model should be evaluated and false otherwise
     */
    public boolean isEval() {
        return _eval;
    }

    /**
     * Set source model evaluation flag
     * @param eval source model evaluation flag
     */
    public void setEval(boolean eval) {
        this._eval = eval;
    }

    /** check output model generation flag*/
    public boolean isGenerate() {
        return _generate;
    }

    /** set output model generation flag*/
    public void setGenerate(boolean generate) {
        this._generate = generate;
    }

    /** check dnn-json output flag*/
    public boolean isJson() { return _json; }

    /** set dnn-json output flag*/
    public void setJson(boolean json) {
        this._json = json;
    }

    /** get dnn-png output flag*/
    public boolean ispng() { return _png; }

    /** set dnn-png output flag*/
    public void setpng(boolean png) { this._png = png; }

    /**check csdfg-json output flag*/
    public boolean isCsdfgJson() {
        return _csdfg_json;
    }

    /**set csdfg-json output flag*/
    public void setCsdfgJson(boolean csdfgJson) {
        this._csdfg_json = csdfgJson;
    }

    /**check csdfg-png output flag*/
    public boolean isCsdfgpng() {
        return _csdfg_png;
    }

    /**set csdfg-png output flag*/
    public void setCsdfgpng(boolean csdfgpng) {
        this._csdfg_png = csdfgpng;
    }

      /**check csdfg-xml output flag*/
    public boolean isCsdfgXml() {
        return _csdfg_xml;
    }

    /**set csdfg-xml output flag*/
    public void setCsdfgXml(boolean csdfgXml) {
        this._csdfg_xml = csdfgXml;
    }

    /** check sesame-template generation flag*/
    public boolean isSesame() { return _sesame; }

    /** set sesame-template generation flag*/
    public void setSesame(boolean sesame) {
        this._sesame = sesame;
    }

    /**
     * Get max number of blocks for DNN transformation
     * @return max number of blocks for DNN transformation
     */
    public Integer getBlocks() { return _blocks; }

    /**
     * Set max number of blocks for DNN transformation
     * @param blocks  max number of blocks for DNN transformation
     */
    public void setBlocks(Integer blocks) { this._blocks = blocks; }

    /**
     * Set maximum split transformations safe-counter
     * @param safeCounter max split transformations safe-counter
     */
    public void setSplitSafeCounter(int safeCounter){
        _splitSafeCounter = safeCounter;
    }

    /**
     * Set number of children got from one layer after the splitting
     * @param childrenNum number of
     * children got from one layer after the splitting
     */
    public void setSplitChldrenNum(int childrenNum){
        _splitChildrenNum = childrenNum;
    }

    /**
     * Set generated images width
     * @param imgW generated images width
     */
    public void setImgW(int imgW) { this._imgW = imgW; }

    /**
     * Is dnn transformation is required
     * @param dnnLayersCount number of the layers in initial dnn model
     * @return true, if transformation is required and false otherwise
     */
    private  boolean _isTransformationRequired(int dnnLayersCount){
        if(_blocks==null)
            return false;
        if(dnnLayersCount>=_blocks)
            return false;
        return true;
    }

    /**
     * get DNN model init representation flag
     * @return DNN model init representation flag
     */
    public DNNInitRepresentation getDnnInitRepresentation() {
        return _dnnInitRepresentation;
    }

    /**
     * Set DNN model init representation flag
     * @param dnnInitRepresentation
     */
    public void setDnnInitRepresentation(DNNInitRepresentation dnnInitRepresentation) {
        this._dnnInitRepresentation = dnnInitRepresentation;
    }

    /**
     * Check, if multiple models should be processed
     * @return true, if multiple models should be processed and false otherwise
     */
    public boolean isMultipleModels() {
        return _multipleModels;
    }

    /**
     * Set flag, showing if multiple models should be processed
     * @param multipleModels flag, showing if multiple models should be processed
     */
    public void setMultipleModels(boolean multipleModels) {
        this._multipleModels = multipleModels;
    }

    ///////////////////////////////////////////////////////////////////
    ////                input models settings                      ///
    /** only one type of input models: DNN or CSDF could be processed at once!"

    /**
     * Set flag, shows if DNN models are expected as espam-dnn input
     * @param inDnn flag, shows if DNN models are expected as espam-dnn input
     */
    public void setInDnn(boolean inDnn) {
        this._inDnn = inDnn;
    }

    /**
     * Set flag, shows if CSDF models are expected as espam-dnn input
     * @param inCSDF flag, shows if CSDF models are expected as espam-dnn input
     */
    public void setInCSDF(boolean inCSDF){
        this._inCSDF = inCSDF;
    }

    /**
     * Check, if application error logs should be saved
     * @return true, if application error logs should be saved  and false otherwise
     */
    public boolean isLogErr() {
        return _logErr;
    }

    /**
     * Set flag, showing if application error logs should be saved
     * @param logErr flag, showing if application error logs should be saved
     */
    public void setLogErr(boolean logErr) {
        this._logErr = logErr;
    }

    /** set csdf graph generation flag*/
    public void setGenerateCsdfg(boolean generateCSDFG) {
        this._generate_csdfg = generateCSDFG;
    }

    /**
     * Get CSDF model(s) energy specification
     * @return CSDF model(s) energy specification
     */
    public String getEnergySpec() { return _energySpec; }

    /**
     * Set CSDF model(s) energy specification
     * @param energySpec path to CSDF model(s) energy specification
     */
    public void setEnergySpec(String energySpec) {
        this._energySpec = energySpec;
    }

    /**
     * Get CSDF model operators execution time specification
     * @return CSDF model operators execution time specification
     * */
    public String getExecTimesSpec() {
        return _execTimesSpec;
    }

    /**
     * Set CSDF model operators execution time specification
     * @param execTimesSpec path to CSDF model operators execution time specification
     * */
    public void setExecTimesSpec(String execTimesSpec) {
        this._execTimesSpec = execTimesSpec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ///

    /**
     * Write error stack trace to log file
     */
    private void _logError(){
       FileWorker.writeAppend(_errLogPath,"log","txt","\n stack trace: \n");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
               espam.utils.fileworker.FileWorker.writeAppend(_errLogPath,"log","txt",ste.toString());
               System.out.println(ste);
        }
    }

     /**
     * Write error stack trace to log file
     * @param desc error description
     */
    private void _logError(String desc){
       FileWorker.writeAppend(_errLogPath,"log","txt",desc);
       FileWorker.writeAppend(_errLogPath,"log","txt","\n stack trace: \n");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
               FileWorker.writeAppend(_errLogPath,"log","txt",ste.toString());
        }
    }

    /**
     * TODO finish implementation
     * @param phase current phase
     * @return phase description error
     */
    private String _getErrorDescription(String phase){
        return phase;
    }


    /**TODO REFACTORING ON ordinary espam/cnnEspam calls*/
         /**
          * Check if cnnespam commands are used.
          * If cnnespam commands are used, original espam is not called.
          * Cnnespam commands are used if anything if anything should be
          * generated/evaluated with cnnespam
          * @return true, if cnnespam commands are used and false otherwise
          */
    public boolean isUsed(){
        return _eval || _generate;
    }

    /**
     * Constructor. Private since only a single version may exist.
     */
    private UI() {
        clearFlags();
    }

    /**
     * Clear internal user interface flags
     */
    private void clearFlags(){
        _verbose = false;
        _modelsToEval = 1;
        _srcPath = null;
        _dstPath = null;
        _eval = false;
        _generate = false;
        _srcPath = "";
        _dstPath = Config.getInstance().getOutputDir();
        if(!_dstPath.endsWith(File.separator))
            _dstPath += File.separator;
        _json = false;
        _png = false;
        _csdfg_json = false;
        _csdfg_png = false;
        _csdfg_xml = false;
        _sesame = false;
        _srcDir = "./";
        _dnnInitRepresentation = DNNInitRepresentation.LAYERBASED;
        _inDnn = true;
        _inCSDF = false;
        _generate_csdfg = false;
        _execTimesSpec = null;
        _energySpec = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ///
    /**
     * Get a single instance of the UserInterface object.
     */
    private final static UI _instance = new UI();

    /** if model processing details should be printed*/
    private boolean _verbose = false;

    /** if source model should be evaluated*/
    private boolean _eval = false;

    /** if any destination model should be generated from source model*/
    private boolean _generate = false;

    /** directory of source model*/
    private String _srcPath;

    /** directory of destination model*/
    private String _dstPath = Config.getInstance().getOutputDir() + File.separator;

    /** dnn-json output flag*/
    private boolean _json;

    /** dnn-png output flag*/
    private boolean _png;

    /** csdfg-json output flag*/
    private boolean _csdfg_json;

    /** csdfg-png output flag*/
    private boolean _csdfg_png;

    /** csdfg-xml output flag*/
    private boolean _csdfg_xml;

    /** sesame-generation flag*/
    private boolean _sesame;

    /** flag, shows, if csdf graph generation is required*/
    private boolean _generate_csdfg;

    /** source models folder*/
    private String _srcDir = "./";

    /** CNN-2-SDFG converter*/
    private CNN2CSDFGraphConverter cnn2CSDFGraphConverter = new CNN2CSDFGraphConverter();

    /** espam - to DARTS interface*/
    private Espam2DARTS _edInterface = new Espam2DARTS();

    /** Number of models to evaluate */
    private int _modelsToEval = 1;

    /** current UI work phase*/
    private String _curPhase = "";

    /** level of abstraction of init model representation*/
    private DNNInitRepresentation _dnnInitRepresentation = DNNInitRepresentation.LAYERBASED;

    /** Flag, shows that input model type = DNN model, defult = true*/
    private boolean _inDnn = true;

    /** Flag, shows that input model type = CSDF model, default = false*/
    private boolean _inCSDF = false;

     /** Flag, shows, if several input models are processed*/
    private boolean _multipleModels = false;

    /** Flag, shows, if application error logs should be written*/
    private boolean _logErr = true;

    /** path to error log*/
    private String _errLogPath = "./output/err_log/";

    /** number of blocks in the model representation
     * (for block-based mode, requires transformation)*/
    private Integer _blocks = null;

    /**Safe-counter for split transformation*/
    private int _splitSafeCounter = 500;

    /** number of nodes after one node split up*/
    private int _splitChildrenNum = 2;

    /** generated images width*/
    private int _imgW = 7000;

    /** execution times specification*/
    private String _execTimesSpec = null;

    /** energy model specification*/
    private String _energySpec = null;
}
