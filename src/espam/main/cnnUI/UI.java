package espam.main.cnnUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalError;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.DNNMapping.DNN_MAPPING_TYPE;
import espam.datamodel.mapping.DNNMapping.MappingGenerator;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.platform.Platform;
import espam.interfaces.python.Espam2DARTS;
import espam.main.Config;
import espam.operations.evaluation.cnn.*;
import espam.operations.evaluation.csdf.CSDFGMemoryRefiner;
import espam.operations.evaluation.csdf.CSDFTimingRefiner;
import espam.operations.evaluation.csdf.EnergyEvaluator;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.scheduler.dnnScheduler.dnnScheduler;
import espam.operations.scheduler.dnnScheduler.layerFiring;
import espam.operations.transformations.CNN2CSDFGraphConverter;
import espam.operations.transformations.cnn_model_transformations.CNNTransformer;
import espam.operations.transformations.cnn_model_transformations.DNNPartition;
import espam.operations.transformations.csdf_model_transformations.CSDFTransformer;
import espam.parser.json.JSONParser;
import espam.parser.json.platform.NeurAghePlatformParser;
import espam.parser.json.refinement.EnergySpecParser;
import espam.parser.json.refinement.TimingSpecParser;
import espam.parser.onnx.InferenceDNNOptimizer;
import espam.parser.onnx.ONNX2CNNConverter;
import espam.parser.xml.mapping.XmlMappingParser;
import espam.parser.xml.platform.XmlPlatformParser;
import espam.utils.fileworker.FileWorker;
import espam.utils.fileworker.ONNXFileWorker;
import espam.visitor.dot.cnn.CNNDotVisitor;
import espam.visitor.dot.sdfg.SDFGDotVisitor;
import espam.visitor.json.*;
import espam.visitor.json.refinement.EnergyRefinerVisitor;
import espam.visitor.json.refinement.TimingRefinerVisitor;
import espam.visitor.pthread.PthreadSDFGVisitor;
import espam.visitor.sesame.SesameSDFGVisitor;
import espam.visitor.tensorrt.TRTCodegenFlag;
import espam.visitor.tensorrt.TensorrtDNNVisitor;
import espam.visitor.txt.CNNEvaluationTxtVisitor;
import espam.visitor.xml.csdf.CSDFGraphXMLVisitor;
import espam.visitor.xml.csdf.MappingXMLVisitor;
import onnx.ONNX;

import java.io.File;
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
          * @param pathToDnn path to .onnx/.json file with deep neural
          * network to be evaluated
          * @param platformFile target architecture description in
          * ESPAM.xml or neuraghe.json format
          * @return evaluation of the deep neural network in
          * terms of power and performance
          * @throws Exception if an error occurs
          */
         public CSDFEvalResult evaluate(String pathToDnn, String platformFile, EvaluatorAlg evaluatorAlg) {
             _evaluatorAlg = evaluatorAlg;
             CSDFEvalResult result = evaluate(pathToDnn, platformFile);
             return result;
         }

         /**
          * Evaluate layer-based deep neural network in terms
          * of power and performance
          * @param pathToDnn path to .onnx/.json file with deep neural
          * network to be evaluated
          * @param platformFile target architecture description in
          * ESPAM.xml or neuraghe.json format
          * @return evaluation of the deep neural network in
          * terms of power and performance
          * @throws Exception if an error occurs
          */
         public CSDFEvalResult evaluate(String pathToDnn, String platformFile) {
             try {
                 Network dnn = readDNN(pathToDnn, _optimizeForInference, _outputModelName);
                 return evaluate(dnn, platformFile);
             }
             catch(Exception e){
                 return  new CSDFEvalError("Evaluation error: DNN model reading error: " + e.getMessage());
             }
         }

         /**
          * Evaluate layer-based deep neural network in terms
          * of power and performance
          * @param dnn deep neural network to be evaluated
          * @param platformFile target architecture description in *
          * @param evaluatorAlg algorithm, used for evaluation
          * ESPAM.xml or neuraghe.json format
          * @return evaluation of the deep neural network in
          * terms of power and performance
          * @throws Exception if an error occurs
          */
         public CSDFEvalResult evaluate(Network dnn, String platformFile, EvaluatorAlg evaluatorAlg) {
             _evaluatorAlg = evaluatorAlg;
             CSDFEvalResult result = evaluate(dnn, platformFile);
             return result;
         }



    /**
     * Evaluate layer-based deep neural network in terms
     * of power and performance
     * @param dnn deep neural network to be evaluated
     * @param platformFile target architecture description in
     * ESPAM.xml or neuraghe.json format
     * @return evaluation of the deep neural network in
     * terms of power and performance
     * @throws Exception if an error occurs
     */
    public CSDFEvalResult evaluate(Network dnn, String platformFile) {
        if(platformFile==null)
            return  new CSDFEvalError("Evaluation error: empty platform file!");

        _platformFile = platformFile;
        parsePlatform();

        if(!_checkEvalParams(dnn, _platform))
            return  new CSDFEvalError("Evaluation error: incorrect evaluation parameters!");

        dnn.initOperators();
        _processMapping(dnn, null);
        _processScheduling(dnn);

        try {
            CSDFEvalResult result = _evaluate(dnn);
            return result;
        }
        catch (Exception e) {
            return  new CSDFEvalError("Evaluation error: " + e.getMessage());
        }
    }

         /**
          * Evaluate layer-based deep neural network in terms
          * of power and performance
          * @param dnn deep neural network to be evaluated
          * @param platform target architecture description
          * @return evaluation of the deep neural network in
          * terms of power and performance
          * @throws Exception if an error occurs
          */
         public CSDFEvalResult evaluate(Network dnn, Platform platform,
                                        PlatformDescription platformDescription, EvaluatorAlg evaluatorAlg) {
             if(platform==null || platformDescription==null)
                 return  new CSDFEvalError("Evaluation error: empty platform!");

             _platform = platform;
             _platformDescription = platformDescription;
             _evaluatorAlg = evaluatorAlg;

             if(!_checkEvalParams(dnn, _platform))
                 return  new CSDFEvalError("Evaluation error: incorrect evaluation parameters!");

             dnn.initOperators();
             _processMapping(dnn, null);
             _processScheduling(dnn);

             try {
                 CSDFEvalResult result = _evaluate(dnn);
                 return result;
             }
             catch (Exception e) {
                 return  new CSDFEvalError("Evaluation error: " + e.getMessage());
             }
         }

         /**
          * Evaluate layer-based deep neural network in terms
          * of power and performance
          * @param dnn deep neural network to be evaluated
          * @param platformFile target architecture description in
          * ESPAM.xml or neuraghe.json format
          * @return evaluation of the deep neural network in
          * terms of power and performance
          * @throws Exception if an error occurs
          */
         public CSDFEvalResult evaluatePerLayer(Network dnn, String platformFile, boolean printToJson) {
             _evalPerLayer = true;
             if(platformFile==null)
                 return  new CSDFEvalError("Evaluation error: empty platform file!");

             _platformFile = platformFile;
             parsePlatform();

             if(!_checkEvalParams(dnn, _platform))
                 return  new CSDFEvalError("Evaluation error: incorrect evaluation parameters!");

             dnn.initOperators();
             _processMapping(dnn, null);
             _processScheduling(dnn);

             try {
                 CSDFEvalResult result = _evaluate(dnn);
                 if(printToJson)
                     CNNEvaluationCompactJsonVisitor.callVisitor(dnn, _dstPath);

                 return result;
             }
             catch (Exception e) {
                 return  new CSDFEvalError("Evaluation error: " + e.getMessage());
             }
         }


       /**
     * Evaluate CSDF Graph in terms
     * of power and performance
     * @param csdfg CSDF Graph to be evaluated
     * @return evaluation of the  CSDF Graph in
     * terms of power and performance
     */
    public CSDFEvalResult evaluate(CSDFGraph csdfg){
      if (csdfg == null)
          return new CSDFEvalError(" null CSDFG!");
        _refineTiming(csdfg);

       try {
           CSDFEvalResult result = _evaluate(csdfg);
           return result;
           }

           catch (Exception e){ return new CSDFEvalError(e.getMessage()); }
    }

     /**
     * Set platform file type from platform gile extension
      * @return true, if platform file type was determined
      * and false otherwise
      */
    private boolean _setAutoPlatformType(){
        if(_platformFile==null)
            return false;
        if(_platformFile.endsWith("xml")) {
            _platformType = Platformtype.ESPAM;
            return true;
        }
        if(_platformFile.endsWith("json")) {
            _platformType = Platformtype.NEURAGHE;
            return true;
        }
        return false;
    }



    public Network readDNN(String networkPath) {
        try {
            return readDNN(networkPath, 3, null);
        }
        catch (Exception e)
        {
            System.err.println("DNN model reading error: "+e.getMessage());
            return null;
        }
    }

    public Network readDNN(String networkPath, String outName){
     try {
         return readDNN(networkPath, 3, outName);
     }
         catch (Exception e)
        {
            System.err.println("DNN model reading error: "+e.getMessage());
            return null;
        }

    }


     /**
     * Read Deep neural network
     * @return deep neural network
     * @throws Exception if an error occurs
     */
    public Network readDNN(String networkPath, Integer optimizationLevel, String modelName) throws Exception{
        if(optimizationLevel>=0 && optimizationLevel<=3)
            _optimizeForInference = optimizationLevel;
        if(modelName!=null)
            _outputModelName = modelName;
        _srcPath = networkPath;

        _curPhase = "DNN model reading ";
        if (_verbose)
            System.out.println(_curPhase + "...");

        Network network = null;
            if (_srcPath.endsWith("onnx")) {
                network = _readONNXDNNModel(networkPath);
                InferenceDNNOptimizer.getInstance().optimize(network,_optimizeForInference);
                network.setDataFormats(network.getInputLayer().getOutputFormat());
            }

            if (_srcPath.endsWith("json"))
                network = _readJSONDNNModel(_srcPath);

            if (network == null)
                throw new Exception(_srcPath + " DNN model reading error");


            if(_outputModelName==null)
                _outputModelName = network.getName();

        if (_verbose)
             System.out.println("[done]");

        return network;
    }

    /*****************************************************************/
    /****************    Files generation       *********************/
    /**
    * Generate tensorrt/ARM-CL code for a CNN, mapped on a platform
    */
    public void generateTRT (Network network, String platformFile, Vector<TRTCodegenFlag> flags) throws Exception {

             _platformFile = platformFile;
             _trtFlags = flags;

             if(_platformFile!=null)
                 parsePlatform();

             _processMapping(network, null);

             if(_partitioningFile!=null)
                 _processPartitioning();

             _generateTensorrt(network, null);
         }

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

        if(_platformFile!=null)
            parsePlatform();

        if (_inDnn) {
            network = _readDNN();
            network.initOperators();

        if(_isDNNTransformationRequired(network))
            _transformDNN(network);

            if(_FMSizes)
                network.printFMSizes();
            csdfg = _convertDNN2SDFG(network);
        }

        if(_inCSDF) {
            csdfg = _readCSDFG();
            _refineTiming(csdfg);
        }

        if(_sesame || _pthread) {
            _setRepetitionVectorOneRunPerActor(csdfg);
            // _setRepetitionVector(csdfg);
            if(_isCSDFGTransformationRequired())
                _transformCSDFG(csdfg);
        }

        if(_consistencyCheckout)
            _checkConsistency(network,csdfg);


        _processMapping(network, csdfg);


        if(_eval){
            if(_inDnn){
                if(_checkEvalParams(network,_platform)) {
                    _processScheduling(network);
                    CSDFEvalResult result = _evaluate(network);
                    printResult(result);
                }
            }
            else {
                CSDFEvalResult result = _evaluate(csdfg);
                printResult(result);
            }

        }

        if(_eval_csdf) {
            CSDFEvalResult result = _evaluate(csdfg);
            printResult(result);
        }

        if(_partitioningFile!=null)
            _processPartitioning();

        if (_sesame)
            _generateSesame(csdfg);

        if(_pthread)
            _generatePthread(network, csdfg);

        if(_inDnn)
            _generateDNNOutputFiles(network);

        if(_generate_csdfg)
            _generateCSDFOutputFiles(csdfg);

        if(_wcetTemplateGen) {
            _generateWCETTemplate(network,csdfg);
        }

        if(_tensorrt)
            _generateTensorrt(network, csdfg);

        if(_verbose)
             System.out.println("EspamAI finished");
    }

    /** Check parameters for DNN evaluation
      * @param dnn DNN to be evaluated
      * @param platform hardware platform description
      * @return true, if DNN is ready to be evaluated and false otherwise
    */
    private boolean _checkEvalParams(Network dnn, Platform platform){
             if(dnn==null){
                 System.err.println("Evaluation error: NULL DNN!");
                 return false;
             }
             dnn.setDataFormats(dnn.getInputLayer().getOutputFormat());
             boolean DNNIsconsistent = dnn.checkConsistency();
             if(!DNNIsconsistent){
                 System.err.println("Evaluation error: inconsistent DNN!");
                 return  false;
             }

             if(platform==null) {
                 System.err.println("Evaluation error: empty platform!");
                 return false;
             }

             return true;
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
            if (_srcPath.endsWith("onnx")) {
                network = _readONNXDNNModel(_srcPath);
                InferenceDNNOptimizer.getInstance().optimize(network,_optimizeForInference);
                network.setDataFormats(network.getInputLayer().getOutputFormat());
            }

            if (_srcPath.endsWith("json"))
                network = _readJSONDNNModel(_srcPath);

            if (network == null)
                throw new Exception(_srcPath + " DNN model reading error");



            if(_outputModelName==null)
                _outputModelName = network.getName();

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

        if(_outputModelName==null)
            _outputModelName = csdfg.getName();
        else csdfg.setName(_outputModelName);

        if (_verbose)
             System.out.println("[done]");

        return csdfg;

    }

    /**
    * Set CSDFG repetition vector
    * @param csdfg CSDFG
    */
    private void _setRepetitionVector(CSDFGraph csdfg) {
        _curPhase = " Repetition vector setup ";
        if (_verbose)
            System.out.println(_curPhase + "...");

        try {
             boolean consistency = _edInterface.checkConsistency(csdfg);
             if(!consistency) {
                 System.err.println("inconsistent csdf model generated: " + csdfg.getName());
                 return;
             }


            _edInterface.setRepetitionVector(csdfg);
        }
        catch (Exception e){
            System.err.println("Repetition vector setup error: "+e.getMessage());
            return;
        }
        if (_verbose)
            System.out.println("[done]");
    }

    /**Set one run per actor
    * Set CSDFG repetition vector
    * @param csdfg CSDFG
    */
    private void _setRepetitionVectorOneRunPerActor(CSDFGraph csdfg) {
        _curPhase = " Repetition vector setup ";
        if (_verbose)
            System.out.println(_curPhase + "...");


             for (Object node: csdfg.getNodeList()){
                 ((CSDFNode)node).setRepetitions(1);
             }

        if (_verbose)
            System.out.println("[done]");
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
                if(_inDnn) {
                    consistency = network.checkConsistency();
                }
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
        if(_isDNNBBSplitRequired(network.countLayers()))
            _performDNNBBSplit(transformer);

        if(_fuseCompounds)
            _performFuseCompunds(transformer);

        if (_verbose)
            System.out.println("[done]");

    }

    /**
     * Perform split DNN transformations
     * @param transformer
     */
    private void _performDNNBBSplit(CNNTransformer transformer){
        _curPhase = "  SPLIT ";
        if (_verbose)
            System.out.println(_curPhase + "...");
        transformer.splitToBlocks(_blocks, _splitSafeCounter, _splitChildrenNum, _verbose);
        if (_verbose)
            System.out.println("  [done]");
    }

    /**
     * Perform split DNN transformations
     * @param transformer
     */
    private void _performDNNMappingSplit(HashMap<String, Vector<Integer>> plan, CNNTransformer transformer){
        _curPhase = "  SPLIT ";
             if (_verbose)
                 System.out.println(_curPhase + "...");
             transformer.splitByPlan(plan);
             if (_verbose)
                 System.out.println("  [done]");
         }

    private void _performFuseCompunds (CNNTransformer transformer){
        _curPhase = "  FUSE COMPOUNDS ";
        Vector<Vector<String>> compounds = null;
        if (_verbose)
            System.out.println(_curPhase + "...");

        String errMsg = null;
        if(_platformFile==null){
            errMsg = "Empty platform file";
        }

        else {
            /** TODO: support for ESPAM platform , separate file?*/
            if(!(_platformFile.endsWith("json")))
                errMsg="Compounds merge is currently supported only for NEURAGHE platform";
            else compounds = NeurAghePlatformParser.getCompounds(_platformFile);
        }

        if(compounds==null){
            if(errMsg==null)
                errMsg = "NULL compounds list";
        if (_verbose)
            System.out.println("  [NOT done: " + errMsg + " ]");
        }

        else transformer.mergeCompounds(compounds,false);

        if (_verbose)
            System.out.println("  [done]");
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
    private void _processMapping(Network dnn, CSDFGraph csdfg){
        if(_mappingFile!=null) {
            _curPhase = "Mapping file parsing";
            if (_verbose)
                System.out.println(_curPhase + "...");
            parseMapping();
            if (_verbose)
                System.out.println("[done]");
        }
        else {
            if(_platformFile!=null || _platform!=null) {
                _curPhase = "Auto mapping generation";
                if (_verbose)
                    System.out.println(_curPhase + "...");
                if(_inDnn)
                    generateAutoMapping(_platform, dnn);
                else
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

    /**TODO: scheduling file?
    /** Generate dnn schedule
    * @param dnn DNN
     * */
    private void _processScheduling(Network dnn){
        _curPhase = "Auto schedule generation";
        if (_verbose)
            System.out.println(_curPhase + "...");
        _dnnSchedule = dnnScheduler.generateDNNSchedule(dnn, _mapping, _dnnMappingType, _platformDescription);
        if (_verbose)
            System.out.println("[done]");
    }

    /** Prcoess application partitioning file*/
    private void _processPartitioning(){
         _curPhase = "Partitioning file parsing";
            if (_verbose)
                System.out.println(_curPhase + "...");
            try {
                _partitioning = new Vector<DNNPartition>();


                String partitioningJSON = FileWorker.read(_partitioningFile);
                JsonArray dpArr = JSONParser.getInstance().getGson().fromJson(partitioningJSON, JsonArray.class);


                //System.out.println(dpArr);
                for (JsonElement jElem: dpArr){
                    DNNPartition dp = (DNNPartition) JSONParser.getInstance().fromJson(jElem.toString(),DNNPartition.class);
                    _partitioning.add(dp);
                }

            if (_verbose)
                System.out.println("[done]");
             }

            catch (Exception e){
                System.err.println("Partitioning file parsing ERROR: " + e.getMessage());
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
     * Generate Tensorrt executable code
     * @param csdfg CSDF graph
     */
    private void _generateTensorrt(Network network, CSDFGraph csdfg){
        _curPhase = "tensorrt code generation";
        if (_verbose)
            System.out.println(_curPhase + "...");

        if(_inDnn) {
                trtvisitor.callVisitor(network, _dstPath + network.getName() + "/tensorrt/", _partitioning, _trtFlags, _dnnInitRepresentation);
        }

        else {
            System.out.println(_curPhase + "Tensorrt code generation error: DNN input model is required");
        }
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
             if (_jsonShort)
                _generateDNNJSONShort(dnn);
            if (_dot)
               _generateDotDNN(dnn);
            if(_jsonDNNTopology)
                CNNTopologyJSONVisitor.callVisitor(dnn, _dstPath);
            if(_jsonDNNEval)
                CNNEvaluationCompactJsonVisitor.callVisitor(dnn, _dstPath);
            if(_txtDNNEval)
                CNNEvaluationTxtVisitor.callVisitor(dnn, _dstPath);
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
        if(_csdfg_json_short)
             _generateSDFGJSONShort(csdfg);
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
     * Generate JSON description for DNN model
     * @throws Exception if an error occurs
     */
    private void _generateDNNJSONShort(Network network) throws Exception{
        _curPhase = network.getName() + " DNN json model generation";
        if(_verbose)
            System.out.println(_curPhase + "...");
        CNNJSONVisitorShort.callVisitor(network,_dstPath +"/json/");
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
     * Generate JSON description for SDF/CSDF model
     * @throws Exception if an error occurs
     * @param sdfg
     */
    private void _generateSDFGJSONShort(CSDFGraph sdfg) throws Exception{
         _curPhase = sdfg.getName() + " SDFG JSON generation";
            if(_verbose)
                System.out.println(_curPhase + "...");
              CSDFGraphShortJSONVisitor.callVisitor(sdfg,_dstPath + "/sdfg/json/");
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
     * Evaluate DNN
     * @param dnn Neural network
     * @throws Exception if an error occurs
     */
    private CSDFEvalResult _evaluate(Network dnn) throws Exception{
            _curPhase = "Model evaluation: DNN model evaluation ";
            if(_verbose)
                System.out.println(_curPhase + "...");

            CSDFEvalResult result = new CSDFEvalResult();

             _curPhase = "  -  Memory evaluation ";
            if(_verbose)
                System.out.println(_curPhase + "...");
            /**memory evaluation*/
            DNNMemoryEvaluator.getInstance().evalMemory(dnn,result,_dnnMappingType,  true);

            _curPhase = "  -  Time evaluation ";
            if(_verbose)
               System.out.println(_curPhase + "...");

            /**time evaluation*/
             DNNTimeEvaluator.getInstance().evaluateTime(dnn,result, _platformDescription,_dnnSchedule, _mapping, _evaluatorAlg);

            _curPhase = "  -  Energy evaluation ";
            if(_verbose)
                System.out.println(_curPhase + "...");

            /** evaluate energy*/
            DNNEnergyEvaluator.getInstance().evaluateEnergy(dnn, result, _platformDescription, _dnnSchedule, _mapping, _totalEnergyWatt, _evaluatorAlg);
            //DNNEnergyEvaluator.evaluateEnergy(dnn, result, _platformEval, _dnnSchedule);

            _curPhase = "  -  Processors number evaluation ";
            if(_verbose)
                System.out.println(_curPhase + "...");

            /** evaluate processors number*/
            ProcNumEvaluator.getInstance().evaluateProcNum(dnn, result, _platformDescription, _dnnSchedule, _mapping);
            return result;
    }

    /**
     * Evaluate SDF graph
     * @param sdfg SDF graph
     * @throws Exception if an error occurs
     */
    private CSDFEvalResult _evaluate(CSDFGraph sdfg) throws Exception{
            boolean toMB = true;
            _curPhase = "CSDF Model evaluation";
            if(_verbose)
                System.out.println(_curPhase + "...");

            CSDFEvalResult result = new CSDFEvalResult();

             _curPhase = "Model evaluation: Memory evalution ";
            if(_verbose)
                System.out.println(_curPhase + "...");
            /**refine memory evaluation*/
            CSDFGMemoryRefiner.getInstance().evaluateMemory(sdfg,result, toMB);

            /**evaluate time*/
           // if(_execTimeScale!=1.0)
            //    CSDFTimingRefiner.getInstance().refineTimingEval(result,_execTimeScale);

            /** evaluate energy*/
            //Double refinedEnergy = _getEnergy(sdfg);
            //result.setEnergy(refinedEnergy);


            if(_verbose)
                System.out.println("Evaluation finished");

            return result;
    }

         /**
          * Evaluate SDF graph
          * @param sdfg SDF graph
          * @throws Exception if an error occurs
          */
         private CSDFEvalResult _evaluateDARTs(CSDFGraph sdfg) throws Exception{
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
             CSDFGMemoryRefiner.getInstance().refineMemoryEval(sdfg,result);

             /**refine time evaluation*/
             if(_execTimeScale!=1.0)
                 CSDFTimingRefiner.getInstance().refineTimingEval(result,_execTimeScale);

             /** evaluate energy*/
             Double refinedEnergy = _getEnergy(sdfg);
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
    private Double _getEnergy(CSDFGraph graph){
        try{
            HashMap<Integer,Double> nodesUtilization = _edInterface.getUtilizationVector(graph);


            Double energy = EnergyEvaluator.getInstance().getEnergy(nodesUtilization);
            return energy;
        }
        catch (Exception e){
            System.err.print("Energy computation error: " + e.getMessage());
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

            if(_outputModelName==null)
                _outputModelName = onnxModel.getGraph().getName();

            if(_extractONNXWeights)
                espamNetwork = ONNX2CNNConverter.convertModel(onnxModel,_srcPath,
                        _dstPath + _outputModelName, _outputModelName, _verbose);
            else
                espamNetwork = ONNX2CNNConverter.convertModel(onnxModel, _outputModelName);
            return espamNetwork;
    }

    /**
     * Read dnn model
     * @param modelPath path to dnn model
     */
    private Network _readJSONDNNModel(String modelPath) throws Exception{
         String json = FileWorker.read(modelPath);
         Network espamNetwork = (Network) JSONParser.getInstance().fromJson(json,Network.class);
         if(_outputModelName!=null)
             espamNetwork.setName(_outputModelName);
         else _outputModelName = espamNetwork.getName();
         espamNetwork.resolveReferences();
         espamNetwork.initOperators();
         //System.out.println("DNN json model red!");
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
    private void _refineTiming(HashMap<String,Long> operatorsExecTimes){
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
        _setAutoPlatformType();
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
        _platformDescription = NeurAghePlatformParser.parsePlatformEval(_platformFile);
        _platform = NeurAghePlatformParser.parsePlatform(_platformFile);
    }

    /** Parse mapping file */
    public void parseMapping() {
      try {
                 _parserMapping.initializeParser();
                 _mapping = _parserMapping.doParse(_mappingFile, false);
                 _dnnMappingType = DNN_MAPPING_TYPE.SEQUENTIAL; //TODO: custom-pipeline??
                 return;
      } catch (Exception e) {
                 System.out.println("mapping file parsing error " + e.getMessage());
                 return;
      }

    }

    /** Generate mapping automatically
          * @param platform platform
          * @param dnn dnn graph
          */
    public void generateAutoMapping(Platform platform, Network dnn){
        MappingGenerator mg = new MappingGenerator(platform, dnn, _platformDescription, _dnnMappingType);
        _mapping = mg.generateAutoMapping();
    }


   /**TODO: FPGA?
    * Generate mapping automatically
    * @param platform platform
    * @param csdfg csdf graph
    */
    public void generateAutoMapping(Platform platform, CSDFGraph csdfg){
        MappingGenerator mg = new MappingGenerator(platform, csdfg, _platformDescription);
        _mapping = mg.generateAutoMapping();
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
            if (result instanceof CSDFEvalError)
                System.out.println(((CSDFEvalError) result).getErrorMessage());
            else
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

    /** set dnn-json output flag*/
    public void setJsonShort(boolean json) {
        this._jsonShort = json;
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

    public void setCsdfgJsonShort(boolean csdfgJsonShort) {this._csdfg_json_short = csdfgJsonShort; }

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
     * @param dnn initial dnn model
     * @return true, if transformation is required and false otherwise
     */
    private  boolean _isDNNTransformationRequired(Network dnn){
        int dnnLayersCount = dnn.countLayers();
        return  _isDNNBBSplitRequired(dnnLayersCount) || _isDNNMappingSplitRequired(dnn) || _isDNNMergeRequired();
    }

    private boolean _isDNNBBSplitRequired(int dnnLayersCount){
          if(_blocks==null)
          return false;
          if(dnnLayersCount>=_blocks)
          return false;

          return true;
    }


    private boolean _isDNNMappingSplitRequired(Network dnn){
        //HashMap<String,Vector<Integer>> splitPlan = DNNPartitioner.planMappingBasedCPUSplit(_platform,_platformEval,dnn, _dnnMappingType);
        //if(splitPlan.entrySet().size()>0)
          //  return true;
        return false;
    }


         /**
          * TODO: split-on-CPU implementation???
          * @param dnnLayersCount
          * @return
          */
    private boolean _isDNNCPUStreamSplitRequired(int dnnLayersCount){
             if(_blocks==null)
                 return false;
             if(dnnLayersCount>=_blocks)
                 return false;

             return true;
         }

    /**private boolean _isDNNSplitRequired(int dnnLayersCount){
         if(_blocks==null)
            return false;
        if(dnnLayersCount>=_blocks)
            return false;
        return true;
    }*/

    private boolean _isDNNMergeRequired(){
        return _fuseCompounds;
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

       EnergyEvaluator.getInstance().setAlpha(operators.get("alpha"));
       EnergyEvaluator.getInstance().setBeta(operators.get("beta"));
       EnergyEvaluator.getInstance().setB(operators.get("b"));
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
       HashMap<String,Long> newSpec = TimingSpecParser.parseTimingSpecTemplate(execTimesSpec);
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
       HashMap<String,Long> newSpecInGops = NeurAghePlatformParser.parseTimingSpecTemplate(platform);
       HashMap<String,Double> newSpecInDoubleSec = _fromGOPSPerSecToSeconds(newSpecInGops);
        /** set time scale, that allow to represent times as Ints (Int times are required by DaedalusRT)*/
        _execTimeScale = _calculateTimeScale(newSpecInDoubleSec.values());
       HashMap<String,Long> newSpec = _fromDoubleToLong(newSpecInDoubleSec,_execTimeScale);

       /** add only R/W basic operators*/
       CSDFTimingRefiner.getInstance().initRWOperationsDefault();

       /** extend basic operators by parsed specification*/
       CSDFTimingRefiner.getInstance().updateBasicOperationsTiming(newSpec);
    }

    /**change op times measurement units from GOPS (10^9 Ops)/sec to sec*/
    private HashMap<String,Double> _fromGOPSPerSecToSeconds(HashMap<String, Long> timesInGops){
       HashMap<String,Double> timesInSec = new HashMap<>();
       //1 Gop = 10^9 Ops
       double GOP = 1000000000.0;
        for(Map.Entry<String,Long> timeInGops: timesInGops.entrySet())
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

    /**change op times measurement units from GOPS (10^9 Ops)/sec to sec*/
    private HashMap<String,Long> _fromDoubleToLong(HashMap<String, Double> doubleTimes, double scale){
       HashMap<String,Long> intTimes= new HashMap<>();

        for(Map.Entry<String,Double> doubleTime: doubleTimes.entrySet())
            intTimes.put(doubleTime.getKey(),(long)(doubleTime.getValue()/scale));

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
     * Set tensorrt code generation flag
     * @param Tensorrt tensorrt code generation flag
     */
    public void setTensorrt(boolean Tensorrt) {
             this._tensorrt = Tensorrt;
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
     * Set path to CUDA - flag is important for arm-cl/tensorrt code generation
     * @param cudaPath path to CUDA
     */

    public void setCUDAPath(String cudaPath){
        trtvisitor.set_pathToCUDA(cudaPath);
    }

     /**
     * Set path to ARM compute library - flag is important for arm-cl/tensorrt code generation
     * @param armclPath path to ARM compute library
     */

    public void setARMCLPath(String armclPath){
        trtvisitor.set_pathToARMCL(armclPath);
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

    public void _clearTRTFlags(){
        _trtFlags = new Vector<>();
    }

     /**
     * Set tensorRT CPU eval network generation flag
     */
    public void setTRTEvalCPU() {
        _trtFlags.add(TRTCodegenFlag.CPUEVAL);
    }

    /**
     * Set tensorRT GPUEval network generation flag
     */
    public void setTRTEvalGPU() {
        _trtFlags.add(TRTCodegenFlag.GPUEVAL);
    }

         /**
          * Set flag,  if per-layer power/perf/memory evaluation should be performed
          * @param evalPerLayer  flag, if per-layer power/perf/memory evaluation should be performed
          */
         public void evalPerLayer(boolean evalPerLayer) {
             _evalPerLayer = evalPerLayer;
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

     /**
       * Set flag to print FM sizes
       * @param fmSizes flag to print FM sizes
       */
    public void setFMSizes(boolean fmSizes){
        _FMSizes = fmSizes;
    }

   /**
     * Set output model name (different from the input model name
     * @param outputModelName output model name
    */
    public void setOutputModelName( String outputModelName){
        _outputModelName = outputModelName;
    }

    /** set compounds fusion flag
     * @param fuseCompounds compounds fusion flag
     */
    public void setFuseCompounds(boolean fuseCompounds) {
        this._fuseCompounds = fuseCompounds;
    }

    /** set file with DNN partitioning*/
    public void setPartitioningFile(String partitioningFile) {
             this._partitioningFile = partitioningFile;
    }

    /** use roofline model to perform platform-aware dnn evaluation*/
    public void setRoofline(){
        _evaluatorAlg = EvaluatorAlg.ROOFLINE;
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

    /**generate DNN topology in short JSON notation*/
    public void setJSONDNNTopology(boolean json) {
        this._jsonDNNTopology = json;
    }

    /**generate DNN power/performance/memory per-layer evaluation*/
    public void setJSONDNNEval(boolean json) {
             this._jsonDNNEval = json;
    }

    /**generate DNN power/performance/memory per-layer evaluation*/
    public void setTxtDNNEval(boolean json) {
             this._txtDNNEval = json;
    }

         /**
     * Constructor. Private since only a single version may exist.
     */
    private UI() {
        clearFlags();
    }

    /** Set mapping type to pipeline*/
    public void setPipelineMapping(){
        _dnnMappingType = DNN_MAPPING_TYPE.PIPELINE;
    }

    /** set total energy evaluation measurement unit to Watt*/
    public void set_totalEnergyWatt (boolean totalEnergyWatt) { _totalEnergyWatt = totalEnergyWatt; }

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
        _optimizeForInference = 3;
        _extractONNXWeights = false;
        _clearTRTFlags();
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

    /** if per-layer CNN model evaluation should be provided*/
    private boolean _evalPerLayer = false;

        /** if source model should be evaluated*/
    private boolean _eval_csdf = false;

    /** if any destination model should be generated from source model*/
    private boolean _generate = false;

    /** directory of source model*/
    private String _srcPath;

    /** directory of destination model*/
    private String _dstPath = Config.getInstance().getOutputDir();

    /** dnn-json output flag*/
    private boolean _json;

    /** dnn-json output flag*/
    private boolean _jsonShort;

    /** dnn-dot output flag*/
    private boolean _dot;

    /** csdfg-json output flag*/
    private boolean _csdfg_json;

    /** csdfg-json output flag*/
    private boolean _csdfg_json_short;

    /** csdfg-dot output flag*/
    private boolean _csdfg_dot;

    /** csdfg-xml output flag*/
    private boolean _csdfg_xml;

    /** sesame-generation flag*/
    private boolean _sesame;

    /** Pthread-code generation flag*/
    private boolean _pthread;

    /** Tensorrt-code generation flag*/
    private boolean _tensorrt;

    /** flag, shows, if csdf graph generation is required*/
    private boolean _generate_csdfg;

    /** source models folder*/
    private String _srcDir = "./";

    /** CNN-2-SDFG converter*/
    private CNN2CSDFGraphConverter _cnn2CSDFGraphConverter = new CNN2CSDFGraphConverter();

    TensorrtDNNVisitor trtvisitor = new TensorrtDNNVisitor();

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

    /**mapping file*/
    private String _mappingFile = null;

    /**mapping type*/


    /** if mapping file should be generated*/
    private boolean _generateMapping = false;

   /**mapping file parser*/
    public  static XmlMappingParser _parserMapping = new XmlMappingParser();

    /**platform file parser*/
    public static XmlPlatformParser _parserPlatform = new XmlPlatformParser();

    /** CSDF mapping*/
    Mapping _mapping;

    /** DNN partitioning*/
    Vector<DNNPartition> _partitioning;

    /** DNN partitioning file*/
    String _partitioningFile;

    /** platform*/
    Platform _platform;

    /** evaluation of platform characteristics
         (i.e., energy, supported operators per core, operators exec. times, etc.)*/
    PlatformDescription _platformDescription;

    /** type of the platform specification*/
    Platformtype _platformType = Platformtype.ESPAM;

    /** output model name*/
    String _outputModelName = null;

    boolean _FMSizes = false;

    boolean _fuseCompounds = false;

    /** number of streams for CPU execution*/
    /** TODO: implement!*/
    Integer _cpuStreams = 1;

    /** tensorrt generation flags*/
    Vector<TRTCodegenFlag> _trtFlags = new Vector<>();

    /** print dnn topology in simple and short json encoding*/
    boolean _jsonDNNTopology;

    /** print DNN model per-layer evaluation*/
    boolean _jsonDNNEval;

    /** print DNN model per-layer evaluation*/
    boolean _txtDNNEval;

    /** DNN schedule*/
    Vector<Vector<layerFiring>> _dnnSchedule;

    /**The DNN mapping type determines the mapping of DNN nodes onto a target platform
     * For more details see DNN_MAPPING_TYPE definition*/
    DNN_MAPPING_TYPE _dnnMappingType = DNN_MAPPING_TYPE.SEQUENTIAL;

    /** implement CSDF with minimum buffer sizes*/
    boolean _minimizeBUFFERSizes = false;

    /** if DNN energy evaluation has to be provided in Watt*/
    private boolean _totalEnergyWatt = false;

    /** algorithm/model for DNN platform-aware evaluation*/
    private EvaluatorAlg _evaluatorAlg = EvaluatorAlg.BENCHMARK;
}
