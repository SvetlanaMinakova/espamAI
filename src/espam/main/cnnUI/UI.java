package espam.main.cnnUI;

import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalError;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.processors.GPU;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.main.Config;
import espam.operations.transformations.CNN2CSDFGraphConverter;
import espam.operations.transformations.csdf_model_transformations.CSDFTransformer;
import espam.parser.json.platform.NeurAghePlatformParser;
import espam.parser.json.refinement.EnergySpecParser;
import espam.parser.json.refinement.TimingSpecParser;
import espam.parser.xml.mapping.XmlMappingParser;
import espam.parser.xml.platform.XmlPlatformParser;
import espam.visitor.dot.cnn.CNNDotVisitor;
import espam.visitor.dot.sdfg.SDFGDotVisitor;
import espam.visitor.json.refinement.EnergyRefinerVisitor;
import espam.visitor.json.refinement.TimingRefinerVisitor;
import espam.visitor.pthread.PthreadSDFGVisitor;
import espam.visitor.sesame.SesameSDFGVisitor;
import espam.visitor.xml.csdf.MappingXMLVisitor;
import onnx.ONNX;
import espam.interfaces.python.Espam2DARTS;
import espam.main.ExtensionFilter;
import espam.operations.refinement.CSDFGEnergyRefiner;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.operations.refinement.RefinedCSDFEvaluator;
import espam.operations.transformations.CNN2CSDFGraphConverter;
import espam.operations.transformations.cnn_model_transformations.CNNTransformer;
import espam.parser.json.JSONParser;
import espam.parser.onnx.InferenceDNNOptimizer;
import espam.parser.onnx.ONNX2CNNConverter;
import espam.utils.fileworker.FileWorker;
import espam.utils.fileworker.ONNXFileWorker;
import espam.visitor.json.CNNJSONVisitor;
import espam.visitor.json.CSDFGraphJSONVisitor;
import espam.visitor.xml.csdf.CSDFGraphXMLVisitor;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
     *         - dot       : DNN graph in .dot format and corresponding generateDoted image in .dot format
     *         - sesame    : code templates for Sesame
     *         - json-csdf : CSDF graph in .json format for DARTS
     *         - xml-csdf  : CSDF graph in .xml fomat for SDF3
     *         - dot-csdf  : CSDF graph in .dot format and corresponding generateDoted image in .dot format
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
                System.err.println(absPath + " model error!");
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

        if(_isDNNTransformationRequired(dnn.countLayers())){
            CNNTransformer transformer = new CNNTransformer(dnn);
            transformer.splitToBlocks(_blocks,_splitSafeCounter,_splitChildrenNum,_verbose);
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
            //if (_multipleModels)
                //_runCommandsMultipleInputs();
            //else
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
     * run commands for single input model
     * */
    private void _runCommandsSingleInput() throws Exception {
        Network network = null;
        CSDFGraph csdfg = null;

        if (_inDnn) {
            network = _readDNN();

            if(_isDNNTransformationRequired(network.countLayers()))
               _transformDNN(network);

            csdfg = _convertDNN2SDFG(network);
        }

        if(_inCSDF) csdfg = _readCSDFG();

        if(_sesame || _pthread)
            _edInterface.setRepetitionVector(csdfg);

        if(_consistencyCheckout)
            _checkConsistency(network,csdfg);

        _refineTiming(csdfg);

       if(_isCSDFGTransformationRequired())
           _transformCSDFG(csdfg);

        if(_platformFile!=null)
            parsePlatform();

        _processMapping(csdfg);

        if (_sesame)
            _generateSesame(csdfg);

        if(_pthread)
            _generatePthread(network, csdfg);

        if(_eval) {
            CSDFEvalResult result = _evaluate(csdfg);
            printResult(result);
        }

        if(_inDnn)
            _generateDNNOutputFiles(network);

        if(_generate_csdfg)
            _generateCSDFOutputFiles(csdfg);

        if(_wcetTemplateGen) {
            _generateWCETTemplate(network,csdfg);
        }

        if(_verbose)
             System.out.println("EspamAI finished");
    }

    /**
     * Read Deep neural network
     * @return deep neural network
     * @throws Exception if an error occurs
     */
    private Network _readDNN() throws Exception{
        _curPhase = "DNN model reading ";
        if (_verbose)
            System.out.println(_curPhase + "...");

        Network network = null;
            if (_srcPath.endsWith("onnx"))
                network = _readONNXDNNModel(_srcPath);

            if (_srcPath.endsWith("json"))
                network = _readJSONDNNModel(_srcPath);

            if (network == null)
                throw new Exception(_srcPath + " DNN model reading error");

            InferenceDNNOptimizer.getInstance().optimize(network,_optimizeForInference);

            network.setDataFormats(network.getInputLayer().getOutputFormat());

        if (_verbose)
             System.out.println("[done]");

        return network;
    }


    /**
    * Read CSDF graph from an input file
    * @return CSDF graph
    * @throws Exception if an error occurs
    */
    private CSDFGraph _readCSDFG() throws Exception{
        _curPhase = "CSDF model reading ";
        if (_verbose)
            System.out.println(_curPhase + "...");

        CSDFGraph csdfg = null;
        if(_srcPath.endsWith("json"))
        csdfg = _readSDFGJSONModel(_srcPath);
        if(csdfg == null) throw new Exception(_srcPath + " CSDF model reading error");

        if (_verbose)
             System.out.println("[done]");

        return csdfg;

    }

             /**
          * Check input model consistency
          * @param network DNN
          * @param csdfg CSDFG
          * @throws Exception if an error occurs
          */
    private void _checkConsistency(Network network, CSDFGraph csdfg) throws Exception{
        _curPhase = "Model consistency checkout ";
        if (_verbose)
            System.out.println(_curPhase + "...");

                boolean consistency;
                if(_inDnn)
                    consistency = network.checkConsistency();
                else
                    consistency = _edInterface.checkConsistency(csdfg);
                System.out.println("input model consistency: " + consistency);
        if (_verbose)
            System.out.println("[done]");
    }

    /**
     * Perform transformations over DNN
     * @param network  DNN
     */
    private void _transformDNN(Network network){
        _curPhase = "CNN model transformation ";
        if (_verbose)
            System.out.println(_curPhase + "...");
        CNNTransformer transformer = new CNNTransformer(network);
        transformer.splitToBlocks(_blocks,_splitSafeCounter,_splitChildrenNum,_verbose);
        if (_verbose)
            System.out.println("[done]");
    }

        /**
     * Perform transformations over CSDFG
     * @param csdfg  CSDFG
     */
    private void _transformCSDFG(CSDFGraph csdfg){
        _curPhase = "CSDF Model transformation: ";
        if (_verbose)
            System.out.println(_curPhase);

        if(_incapsulateConcat) {
            _curPhase = "  - Incapsulation of concatenation nodes";
            if (_verbose)
                System.out.println(_curPhase + "...");
            CSDFTransformer transformer = new CSDFTransformer(csdfg);
            transformer.incapsulateConcatNodes();
        }
        if (_verbose)
            System.out.println("[done]");
    }

    /**
     * Process mapping
     * @param csdfg CSDF graph
     */
    private void _processMapping(CSDFGraph csdfg){
        if(_mappingFile!=null) {
            _curPhase = "Mapping file parsing";
            if (_verbose)
                System.out.println(_curPhase + "...");
            parseMapping();
            if (_verbose)
                System.out.println("[done]");
        }
        else {
            if(_platformFile!=null) {
                _curPhase = "Auto mapping generation";
                if (_verbose)
                    System.out.println(_curPhase + "...");
                generateAutoMapping(_platform, csdfg);
                if (_verbose)
                    System.out.println("[done]");
            }
        }
        if(_generateMapping){
            _curPhase = "Mapping file generation";
            if (_verbose)
                System.out.println(_curPhase + "...");
            MappingXMLVisitor.callVisitor(_mapping,_dstPath + csdfg.getName());
        }
    }

    /**
     * Generate Sesame code templates
     * @param csdfg CSDF graph
     */
    private void _generateSesame(CSDFGraph csdfg){
    _curPhase = "Sesame code generation";
    if (_verbose)
        System.out.println(_curPhase + "...");

    if(_inCSDF)
        SesameSDFGVisitor.callVisitor(csdfg,_dstPath + csdfg.getName()+"/sesame/",false);
    if(_inDnn)
        SesameSDFGVisitor.callVisitor(csdfg,_dstPath + csdfg.getName()+"/sesame/",true);
    }

    /**
     * Generate Pthreade executable code
     * @param csdfg CSDF graph
     */
    private void _generatePthread(Network network, CSDFGraph csdfg){
        _curPhase = "pthread code generation";
        if (_verbose)
            System.out.println(_curPhase + "...");

        if(_mapping!=null)
            PthreadSDFGVisitor.setMapping(_mapping);

        if(_inCSDF)
            PthreadSDFGVisitor.callVisitor(csdfg,_dstPath + csdfg.getName()+"/pthread/",false);
        if(_inDnn)
            PthreadSDFGVisitor.callVisitor(network,csdfg,_dstPath + csdfg.getName()+"/pthread/");
    }

    /**
     * Generate DNN output files, if required
     * Admissible DNN output models:
     *  - json   : DNN graph as .json File
     *  - dot    : DNN graph in .dot format and corresponding generateDoted image in .dot format
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
            if (_dot)
               _generateDotDNN(dnn);
            /** generate energy template once*/
            if(_energyTemplateGen) {
                _generateEnergyTemplate();
                _energyTemplateGen = false;
            }
            _dstPath = rootDst;
    }

    /**
     * Generate CSDFG output files, if required
     * Admissible CSDFG output models:
     *  - json   : CSDF graph as .json File for DARTS
     *  - dot    : CSDF graph in .dot format and corresponding generateDoted image in .dot format
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
        if(_csdfg_dot)
            _generateDotSDFG(csdfg);
        if(_energyTemplateGen)
            _generateEnergyTemplate();

        _dstPath = rootDst;
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
     * Generate dot image of DNN model
     * @throws Exception if an error occurs
     */
    private void _generateDotDNN(Network network) throws Exception{
        _curPhase = network.getName() + " DNN dot file generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
        CNNDotVisitor.callVisitor(network,_dstPath+"/dot/");
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
     * Generate dot image of DNN model
     * @throws Exception if an error occurs
     */
    private void _generateDotSDFG(CSDFGraph sdfg) throws Exception{
        _curPhase = sdfg.getName() + " SDFG image generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
        SDFGDotVisitor.callVisitor(sdfg,_dstPath + "/sdfg/dot/");
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

            CSDFEvalResult result = _edInterface.evaluateCSDFGraph(sdfg);

            if(result instanceof CSDFEvalError) {
                throw new Exception(((CSDFEvalError) result).getErrorMessage());
            }
             _curPhase = "Model evaluation: Memory refinement ";
            if(_verbose)
                System.out.println(_curPhase + "...");
            /**refine memory evaluation*/
            RefinedCSDFEvaluator.getInstance().refineMemoryEval(sdfg,result);

            /**refine time evaluation*/
            if(_execTimeScale!=1.0)
                RefinedCSDFEvaluator.getInstance().refineTimingEval(result,_execTimeScale);

            /** refine energy evaluation*/
            Double refinedEnergy = _getRefinedEnergy(sdfg);
            result.setEnergy(refinedEnergy);


            if(_verbose)
                System.out.println("Evaluation finished");

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
            Network espamNetwork;
            if(_extractONNXWeights)
                espamNetwork = ONNX2CNNConverter.convertModel(onnxModel,_srcPath, _dstPath + onnxModel.getGraph().getName(),_verbose);
            else
                espamNetwork = ONNX2CNNConverter.convertModel(onnxModel);
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
        _curPhase = "CNN-to-CSDF Model conversion";
            if (_verbose)
                System.out.println(_curPhase + "...");
        CSDFGraph sdfg;
        //if(_dnnInitRepresentation.equals(DNNInitRepresentation.NEURONBASED))
          //  sdfg = _cnn2CSDFGraphConverter.buildGraph(network);
        //else
            sdfg = _cnn2CSDFGraphConverter.buildGraphLayerBased(network);

        if (_verbose)
             System.out.println("[done]");
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

    /** Generate WCET template */
    private void _generateWCETTemplate(CSDFGraph csdfg){
        TimingRefinerVisitor.printTimeSpec(csdfg,_dstPath + csdfg.getName() + "/",csdfg.getName() + "_wcet_spec");
    }

    /** Generate WCET template, taking into account parametrized DNN operators (supported) */
    private void _generateWCETTemplate(Network dnn){
        TimingRefinerVisitor.printTimeSpec(dnn,_dstPath + dnn.getName() + "/",dnn.getName() + "_wcet_spec");
    }

    /** Generate WCET template, taking into account both CSDFG operators and
     * parametrized DNN operators (supported) */
    private void _generateWCETTemplate(Network dnn,CSDFGraph csdfg){
        if(dnn==null && csdfg==null)
            return;
        if(dnn==null){
            _generateWCETTemplate(csdfg);
            return;
        }
        if(csdfg==null){
            _generateWCETTemplate(dnn);
            return;
        }
        TimingRefinerVisitor.printTimeSpec(dnn,csdfg,_dstPath + dnn.getName() + "/",dnn.getName() + "_wcet_spec");
    }

    /** Generate energy parameters template */
    private void _generateEnergyTemplate(){
        EnergyRefinerVisitor.printDefaultSpec(_dstPath);
    }


    /** Parse platform file */
    public void parsePlatform() {
        _curPhase = "Platform file reading ";
            if (_verbose)
                System.out.println(_curPhase + "...");
        try {
                if(_platformType.equals(Platformtype.ESPAM)) {
                    _parserPlatform.initializeParser();
                    _platform = _parserPlatform.doParse(_platformFile, false);
                }

                if(_platformType.equals(Platformtype.NEURAGHE))
                    parseNeuraghePlatform();

        } catch (Exception e) {
            System.out.println("platform file parsing error " + e.getMessage());
        }

        if (_verbose)
             System.out.println("[done]");
    }

    /**
     * Parse NeurAghe platform specification
     */
    private void parseNeuraghePlatform(){
        setNEURAgheExecTimesSpec(_platformFile);
        double maxEnergy = NeurAghePlatformParser.getWCEnergy(_platformFile);
        CSDFGEnergyRefiner.getInstance().setMaxprocEnergy(maxEnergy);
        _platform = NeurAghePlatformParser.parsePlatform(_platformFile);
    }

    /** Parse mapping file */
    public void parseMapping() {
        try {
            _parserMapping.initializeParser();
            _mapping = _parserMapping.doParse(_mappingFile, false);
        } catch (Exception e) {
            System.out.println("mapping file parsing error " + e.getMessage());
        }
    }


         /**
          * Generate mapping automatically
          * @param platform platform
          * @param csdfg csdf graph
          */
    public void generateAutoMapping(Platform platform, CSDFGraph csdfg){
        try {
            Vector<Processor> cpuList = _getCPUList(platform);
            Vector<Processor> gpuList = _getGPUList(platform);
            Vector<MProcessor> mCPUList = new Vector<>();
            Vector<MProcessor> mGPUList = new Vector<>();
            
            if(cpuList.size()<1){
                System.err.println("mapping generation error: no CPU found!");
            }
            Mapping automapping = new Mapping(csdfg.getName() + "_to_" + platform.getName());
            Vector<MProcessor> processors = new Vector<>();
            
            //init cpu list
            for (Processor proc: cpuList){
                MProcessor cpu = new MProcessor(proc.getName());
                cpu.setResource(proc);
                cpu.setProcessList(new Vector());
                processors.add(cpu);
                mCPUList.add(cpu);
            }

            //init gpu list
            for (Processor proc: gpuList){
                MProcessor gpu = new MProcessor(proc.getName());
                gpu.setResource(proc);
                gpu.setProcessList(new Vector());
                processors.add(gpu);
                mGPUList.add(gpu);
            }

            automapping.setProcessorList(processors);
            _assignNodesToTemplateMapping(csdfg,mCPUList,mGPUList);
            _mapping = automapping;

           /** mapping.setName("mapping1");
            Vector<MProcessor> gpuList = _getGPUList(mapping);
            _assignNodesToTemplateMappingSpread(csdfg,cpuList,gpuList);
            _mapping = mapping;*/
        }
         catch (Exception e){System.out.println("mapping file parsing error "+e.getMessage());
        }
    }

       /**
     * Set dummy mapping for cpu-gpu platform. Map every convolutional and matrix
     * multiplication core on GPU. Map all other kernels on CPU
     * @param csdfg CSDF graph to be mapped
     */
    protected void _assignNodesToTemplateMapping(CSDFGraph csdfg,Vector<MProcessor> cpuList,Vector<MProcessor> gpuList){

        int cpuNum = cpuList.size();
        int gpuNum = gpuList.size();

        Vector<CSDFNode> gpuNodes = new Vector<>();
        boolean useGPU = false;
        if(gpuNum>0) {
            gpuNodes = csdfg.getNodesList("conv");
            useGPU = true;
        }

        int curCPUId = 0;
        MProcessor curCPU = cpuList.firstElement();

        int curGPUId = 0;
        Vector processes;

        for (Object nodeObj: csdfg.getNodeList()) {
            CSDFNode node = (CSDFNode) nodeObj;
            MProcess mp = new MProcess(node.getName());

            /** assign process to CPU*/
            processes = curCPU.getProcessList();
            processes.add(mp);

            /**select next CPU core for mapping */
            curCPUId++;
            if (curCPUId == cpuNum)
                curCPUId = 0;
            curCPU = cpuList.elementAt(curCPUId);

            /** add GPU call property*/
            if (useGPU && gpuNodes.contains(node)) {
                MemoryUnit gpu = node.getMemoryUnit("gpu");
                gpu.setUnitParamDesc("" + curGPUId);
                /**select next GPU core for mapping */
                curGPUId++;
                if (curGPUId == gpuNum)
                    curGPUId = 0;
            }
        }
    }

    /**
     * Get list of cpu cores in the mapping
     * @param platform platform
     * @return list of cpu cores in the mapping
     */
    protected Vector<Processor> _getCPUList(Platform platform){
        Vector<Processor> cpuList = new Vector<>();
            for (Object resObj: platform.getResourceList()) {
                if(resObj instanceof Processor) {
                    if(!(resObj instanceof GPU)){
                        Processor cpu = (Processor)resObj;
                        cpuList.add(cpu);
                    }
                }
            }
        return cpuList;
    }

        /**
     * Get list of cpu cores in the mapping
     * @param platform platform
     * @return list of cpu cores in the mapping
     */
    protected Vector<Processor> _getGPUList(Platform platform){
        Vector<Processor> gpuList = new Vector<>();
            for (Object resObj: platform.getResourceList()) {
                    if(resObj instanceof GPU){
                        Processor gpu = (Processor)resObj;
                        gpuList.add(gpu);
                }
            }
        return gpuList;
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

    /** get dnn-dot output flag*/
    public boolean isdot() { return _dot; }

    /** set dnn-dot output flag*/
    public void setdot(boolean dot) { this._dot = dot; }

    /**check csdfg-json output flag*/
    public boolean isCsdfgJson() {
        return _csdfg_json;
    }

    /**set csdfg-json output flag*/
    public void setCsdfgJson(boolean csdfgJson) {
        this._csdfg_json = csdfgJson;
    }

    /**check csdfg-dot output flag*/
    public boolean isCsdfgdot() {
        return _csdfg_dot;
    }

    /**set csdfg-dot output flag*/
    public void setCsdfgdot(boolean csdfgdot) {
        this._csdfg_dot = csdfgdot;
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
     * Is dnn transformation is required
     * @param dnnLayersCount number of the layers in initial dnn model
     * @return true, if transformation is required and false otherwise
     */
    private  boolean _isDNNTransformationRequired(int dnnLayersCount){
        if(_blocks==null)
            return false;
        if(dnnLayersCount>=_blocks)
            return false;
        return true;
    }

        /**
     * Is CSDFG transformation is required
     * @return true, if transformation is required and false otherwise
     */
    private  boolean _isCSDFGTransformationRequired(){
        return _incapsulateConcat;
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

    public boolean isMultipleModels() {
        return _multipleModels;
    }
    */

    /**
     * Set flag, showing if multiple models should be processed
     * @param multipleModels flag, showing if multiple models should be processed

    public void setMultipleModels(boolean multipleModels) {
        this._multipleModels = multipleModels;
    }*/

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
     * Set CSDF model(s) energy specification
     * @param energySpec path to CSDF model(s) energy specification
     */
    public void setEnergySpec(String energySpec) {
       HashMap<String,Double>operators = EnergySpecParser.parseEnergySpec(energySpec);

       CSDFGEnergyRefiner.getInstance().setAlpha(operators.get("alpha"));
       CSDFGEnergyRefiner.getInstance().setBeta(operators.get("beta"));
       CSDFGEnergyRefiner.getInstance().setB(operators.get("b"));
    }

    /**
     * Set consistency input model consistency checkout flag
     * @param consistencyCheckout
     */
    public void setConsistencyCheckout(boolean consistencyCheckout) {
        this._consistencyCheckout = consistencyCheckout;
    }

    public void setExternalDartsInterface(boolean externalDartsInterface){
        _edInterface.setExternalDartsInterface(externalDartsInterface);
        _edInterface.updatePaths();

    }

         /**
     * TODO extend operators list or replace it??
     * Set CSDF model operators execution time specification
     * @param execTimesSpec path to CSDF model operators execution time specification
     * */
    public void setExecTimesSpec(String execTimesSpec) {
       HashMap<String,Integer> newSpec = TimingSpecParser.parseTimingSpecTemplate(execTimesSpec);
       /** add basic operators*/
       CSDFTimingRefiner.getInstance().initBasicOperationsDefault();
       /** replace basic operators and extend basic operators*/
       CSDFTimingRefiner.getInstance().updateBasicOperationsTiming(newSpec);
    }


    /**
     * Set CSDF model operators execution time specification,
     * extracted from NeurAghe platform specification
     * @param platform path to NeurAghe platform specification
     * */
    public void setNEURAgheExecTimesSpec(String platform) {
       HashMap<String,Integer> newSpecInGops = NeurAghePlatformParser.parseTimingSpecTemplate(platform);
       HashMap<String,Double> newSpecInDoubleSec = _fromGOPSPerSecToSeconds(newSpecInGops);
        /** set time scale, that allow to represent times as Ints (Int times are required by DaedalusRT)*/
        _execTimeScale = _calculateTimeScale(newSpecInDoubleSec.values());
       HashMap<String,Integer> newSpec = _fromDoubleToInt(newSpecInDoubleSec,_execTimeScale);

       /** add only R/W basic operators*/
       CSDFTimingRefiner.getInstance().initRWOperationsDefault();

       /** extend basic operators by parsed specification*/
       CSDFTimingRefiner.getInstance().updateBasicOperationsTiming(newSpec);


    }

    /**change op times measurement units from GOPS (10^9 Ops)/sec to sec*/
    private HashMap<String,Double> _fromGOPSPerSecToSeconds(HashMap<String, Integer> timesInGops){
       HashMap<String,Double> timesInSec = new HashMap<>();
       //1 Gop = 10^9 Ops
       double GOP = 1000000000.0;
        for(Map.Entry<String,Integer> timeInGops: timesInGops.entrySet())
            timesInSec.put(timeInGops.getKey(),1.0/((double)timeInGops.getValue() * GOP));

        return timesInSec;
    }

    /**
     * calcualete time scale for NEURAghe platform time refinement
     * @param times all supported operators times
     * @return scale, that allow to represent all operator times as integers (as required by daedalusRT)
     */
    private double _calculateTimeScale(Collection<Double> times){
        double scale = 1.0;
        for(double time: times){
            if(scale>time)
                scale = time;
        }

        return scale;
    }

       /**change op times measurement units from GOPS (10^9 Ops)/sec to sec*/
    private HashMap<String,Integer> _fromDoubleToInt(HashMap<String, Double> doubleTimes, double scale){
       HashMap<String,Integer> intTimes= new HashMap<>();

        for(Map.Entry<String,Double> doubleTime: doubleTimes.entrySet())
            intTimes.put(doubleTime.getKey(),(int)(doubleTime.getValue()/scale));

        return intTimes;
    }

    /**
    * Set energy template generation flag
    * @param energyTemplateGen energy template generation flag
    */
    public void setEnergyTemplateGen(boolean energyTemplateGen) {
        this._energyTemplateGen = energyTemplateGen;
    }

    /**
    * Set WCET(worst-case execution times) template generation flag
    * @param wcetTemplateGen WCET(worst-case execution times) template generation flag
    */
    public void setWcetTemplateGen(boolean wcetTemplateGen) {
        this._wcetTemplateGen = wcetTemplateGen;
    }

    /**
     * Set Pthread code generation flag
     * @param Pthread Pthread code generation flag
     */
    public void setPthread(boolean Pthread) {
             this._pthread = Pthread;
    }

    /**
     * set optimize for inference
     * @param optimize optimize for inference flag
     */
    public void setOptimizeForInference(Integer optimize){
        _optimizeForInference = optimize;
    }

    /**
     * Set data tiling flag
     * Input data of every node is processed at once, if data tiling flag is false
     * Input data of every node is processed by lines, if data tiling flag is true
     * @param dataTiling  data tiling flag
     */

    public void setDataTiling(boolean dataTiling){
        _cnn2CSDFGraphConverter.setDataTiling(dataTiling);
    }

     /**
     * Set the number of cores
     * Specify the number of cores for pThread application, if the mapping is not
     * specified explicitly
     * @param cores number of cores
     */

    public void setCores(Integer cores){
        if(cores>0)
            PthreadSDFGVisitor.setMaxCores(cores);
        else
            System.err.println("Incorrect number of cores: "+ cores);
    }

    /**
     * Set generated pthread code in silent mode
     * in silent mode no debug information is given on output
     * @param silent silent mode for pthread generator
     */
    public void setPthreadSilent(boolean silent){
        PthreadSDFGVisitor.setSilent(silent);
    }

    /**
     * Set flag,  if the internal library (dnnFunc) generation for CPU is needed
     * @param dNNFuncCPU  flag, if the internal library (dnnFunc) for CPU generation is needed
     */
    public void setPthreadGenerateDNNFuncCPU(boolean dNNFuncCPU) {
        PthreadSDFGVisitor.setGenerateDNNFuncCPU(dNNFuncCPU);
    }

       /**
     * Set flag,  if the internal library (dnnFunc) generation for GPU is needed
     * @param dNNFuncGPU  flag, if the internal library (dnnFunc) for GPU generation is needed
     */
    public void setPthreadGenerateDNNFuncGPU(boolean dNNFuncGPU) {
        PthreadSDFGVisitor.setGenerateDNNFuncGPU(dNNFuncGPU);
    }

    /**
     * Set flag, if the pthread application uses neuraghe functions
     * @param dNNFuncNA flag, if the pthread application uses neuraghe functions
     */
    public void setPthreadGenerateDNNFuncNA(boolean dNNFuncNA) {
        PthreadSDFGVisitor.setGenerateFuncNA(dNNFuncNA);
    }

    /**
     * Set number of inputs to be processed by pthread application.
     */
    public void setPthreadGenerateBatch(Integer batch) {
        PthreadSDFGVisitor.setBatch(batch);
    }

         /** scale factor for FIFO buffers.
          * Meaning: max number of inputs to be stored between 2 nodes. Should be >=1.
          * Default value = 10
          * @param fifoScale scale factor for FIFO buffers
          */
    public void setFIFOScale(Integer fifoScale){
        PthreadSDFGVisitor.setFifoScale(fifoScale);
        SesameSDFGVisitor._ymlVisitor._fifoScale = fifoScale;
    }

    /** extract weights from onnx model*/
    public void setExtractONNXWeights(boolean extractONNXWeights){
        _extractONNXWeights = extractONNXWeights;
    }


      /** set mapping template file*/
   public void setPlatformFile(String platformFile){
       _platformFile = platformFile;
   }

   /**
     * Set path to .xml mapping file
     * @param mappingFile path to .xml mapping file
     */
    public void setMappingFile(String mappingFile) {
        this._mappingFile = mappingFile;
    }

    /**
     * Set flag, if mapping should be generated
     * @param generateMapping if mapping should be generated
     */
    public void setGenerateMapping(boolean generateMapping) {
             this._generateMapping = generateMapping;
    }

    /**
    * Set platform specification type
    * @param platformType platform specification type
    */
    public void setPlatformType(Platformtype platformType) {
        this._platformType = platformType;
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
        return _eval || _generate || _consistencyCheckout ;
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
        _dot = false;
        _csdfg_json = false;
        _csdfg_dot = false;
        _csdfg_xml = false;
        _sesame = false;
        _srcDir = "./";
        _dnnInitRepresentation = DNNInitRepresentation.LAYERBASED;
        _inDnn = true;
        _inCSDF = false;
        _generate_csdfg = false;
        _wcetTemplateGen = false;
        _energyTemplateGen = false;
        _consistencyCheckout = false;
        _optimizeForInference = 2;
        _extractONNXWeights = false;

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
    private String _dstPath = Config.getInstance().getOutputDir();

    /** dnn-json output flag*/
    private boolean _json;

    /** dnn-dot output flag*/
    private boolean _dot;

    /** csdfg-json output flag*/
    private boolean _csdfg_json;

    /** csdfg-dot output flag*/
    private boolean _csdfg_dot;

    /** csdfg-xml output flag*/
    private boolean _csdfg_xml;

    /** sesame-generation flag*/
    private boolean _sesame;

    /** Pthread-code generation flag*/
    private boolean _pthread;

    /** flag, shows, if csdf graph generation is required*/
    private boolean _generate_csdfg;

    /** source models folder*/
    private String _srcDir = "./";

    /** CNN-2-SDFG converter*/
    private CNN2CSDFGraphConverter _cnn2CSDFGraphConverter = new CNN2CSDFGraphConverter();

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
   // private boolean _multipleModels = false;

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

    /** Generate current WCET template*/
    private boolean _wcetTemplateGen = false;

    /** Generate current energy parameters template*/
    private boolean _energyTemplateGen = false;

    /** input model consistency checkout*/
    private boolean _consistencyCheckout = false;

    /** execution time scale - required for NEURAghe exec_times */
    private double _execTimeScale = 1.0;

    /** level of optimization*/
    private int _optimizeForInference = 2;

    /** extract onnx files*/
    private boolean _extractONNXWeights = false;

    /**If concatenation nodes shpuld be incapsulated*/
    private boolean _incapsulateConcat = true;

    /**maping file*/
    private String _platformFile = null;

    /**maping file*/
    private String _mappingFile = null;

    /** if mapping file should be generated*/
    private boolean _generateMapping = false;

   /**mapping file parser*/
    public  static XmlMappingParser _parserMapping = new XmlMappingParser();

    /**platform file parser*/
    public static XmlPlatformParser _parserPlatform = new XmlPlatformParser();

    /** mapping*/
    Mapping _mapping;

    /** platform*/
    Platform _platform;
    /** type of the platform specification*/
    Platformtype _platformType = Platformtype.ESPAM;
}
