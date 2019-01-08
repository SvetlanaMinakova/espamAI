package espam.interfaces.python;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalError;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.main.Config;
import espam.main.cnnUI.DNNInitRepresentation;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.operations.transformations.CNN2CSDFGraphConverter;
import espam.parser.json.JSONParser;
import espam.visitor.json.CSDFGraphJSONVisitor;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * TODO REFACTORING: for now Layer-based approach is switched on
 * TODO neuron-based approach could be switched on as well
 * TODO block-based approach is planned
 * Class calls DARTs (DAEDALUS RT) python module through the Java.Runtime environment
 * More details on java Runtime could be found on: https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html
 *
 * DARTS module implements evaluation of SDF model, provided in .json format.
 * Convertion of espam.SDFG.Java model to required .json format
 * is provided by espam.visitor.json CNNJSONVisitor class.
 *
 * Access to DARTS module is invoked by interface script espam_cnn_interface.py,
 * located in DARTS project module.
 *
 * espam_cnn_interface.py Evaluates SDF graph in terms of power, performance
 * and resources usage and should be provided by two positional arguments:
 *
 *      d   path to source SDF models directory
 *      f   source SDF model file name
 */
 ///////////////////////////////////////////////////////////////////////////////////////
/**
 * if evaluation of SDF model performed sucessfully, espam_cnn_interface.py returns
 * to output stream SDF model evaluation result in .json format with following notation:
 *
 *     {
 *      "performance": double ,
 *      "power": double ,
 *      "memory": double,
 *      "processors": int
 *      }
 *
 * Example:
 *    {
 *      "performance": 1.5 ,
 *      "power": 2.0,
 *      "memory": 3.7,
 *      "processors": 3
 *      }
 */
/////////////////////////////////////////////////////////////////////////////////////
/**
 * if an error occurs, espam_cnn_interface.py returns to output stream an error in
 * following notation:
 *
 * Error message
 *
 * Example:
 * Error: SDF model file not found
 *
 */
public class Espam2DARTS {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Create new EspamToDarts interface instance
     */
    public Espam2DARTS(String dartsPath) {
        _dartsAbsPath = dartsPath + "/darts";
        setPaths();
    }

     /**
     * Create new EspamToDarts interface instance
     */
    public Espam2DARTS() {
        _dartsAbsPath = Config.getInstance().getDartsPath() + "/darts";
        setPaths();
    }

    /**
     * Set paths
     */
    public void setPaths(){

       /** URL asbInterfacesPathURL = Espam2DARTS.class.getProtectionDomain().getCodeSource().getLocation();
        String asbInterfacesPath = asbInterfacesPathURL.getPath();

        System.out.println("absClassPath: " + asbInterfacesPath); */

        _dartsInterfaceScriptAbsPath = _appAbsPath + File.separator +
                _interfaceDirRelPath.replace("./","") +
                File.separator + "espam_cnn_interface.py";
        _dartsTempDirRelPath = System.getProperty("java.io.tmpdir");
    }

    /**
     * Evaluates a set of CNNs with given data
     *
     * @param networks  vector of CNNs to be evaluated
     * @param inputData input data Tensor
     * @return results of CNNs evaluation
     */
    public Vector<CSDFEvalResult> evaluateCNNs(Vector<Network> networks, Tensor inputData) {
        Vector<CSDFEvalResult> results = new Vector<CSDFEvalResult>();
        for (Network network : networks) {
            network.setDataFormats(inputData);
            results.add(evaluateCNN(network));
        }

        return results;
    }

    /**
     * Evaluates a set of CNNs in terms of power/performance
     *
     * @param networks vector of CNNs to be evaluated
     * @return results of CNNs evaluation
     */
    public Vector<CSDFEvalResult> evaluateCNNs(Vector<Network> networks) {
        Vector<CSDFEvalResult> results = new Vector<CSDFEvalResult>();
        for (Network network : networks) {
            results.add(evaluateCNN(network));
        }

        return results;
    }

    /**
     * Evaluates a DNN in terms of power/performance/memory
     * @param network DNN to be evaluated
     * @return result of DNN evaluation in terms of power/performance/memory
     */
    public CSDFEvalResult evaluateCNN(Network network) {
        CSDFGraph cnnSDF = _cnn2SDFConverter.buildGraphLayerBased(network);
        return evaluateCSDFGraph(cnnSDF);
    }

    /**
     * Evaluates a DNN in terms of power/performance/memory
     * @param network DNN to be evaluated
     * @param initRepresentation initial DNN representation: layer-based or neuron-based
     * @return result of DNN evaluation in terms of power/performance/memory
     */
    public CSDFEvalResult evaluateCNN(Network network, DNNInitRepresentation initRepresentation) {
        CSDFGraph dnnCsdf;

        if(initRepresentation.equals(DNNInitRepresentation.NEURONBASED))
            dnnCsdf = _cnn2SDFConverter.buildGraph(network);
        else
            dnnCsdf = _cnn2SDFConverter.buildGraphLayerBased(network);
        return evaluateCSDFGraph(dnnCsdf);
    }


    /**
     * Implements SDF graph power/performance evaluation
     * @param graph SDFGrpah to be evaluated
     * @throws Exception if an error occurs
     */
    public CSDFEvalResult evaluateCSDFGraph(CSDFGraph graph){
        String scriptResult = _runDARTS(graph,"eval");
        return convertEvalResultToJavaClass(scriptResult);
    }

     /**
     * Set repetition vector of CSDF graph
     * @param graph CSDF graph
     * @return repetition vector of CSDF graph
     * @throws Exception if an error occurs
     */
    public void setRepetitionVector(CSDFGraph graph) throws Exception{
        HashMap<Integer,Integer> repVec = getRepetitionVector(graph);
        for(Map.Entry<Integer,Integer> entry: repVec.entrySet()){
            graph.getNode(entry.getKey()).setRepetitions(entry.getValue());
        }
    }

       /**
     * Get repetition vector of CSDF graph
     * @param graph CSDF graph
     * @return repetition vector of CSDF graph
     * @throws Exception if an error occurs
     */
    public HashMap<Integer, Integer> getRepetitionVector(CSDFGraph graph) throws Exception{
        String scriptResult = _runDARTS(graph,"rep_vec");
                if(isError(scriptResult))
            throw new Exception(" repetition vector calculation error. " + scriptResult);

        return parseRepVec(scriptResult,graph.countNodes());
    }

    /**
     * Get utilization vector of the graph
     * @param graph CSDF graph
     * @return utilization vector of the graph actors
     * @throws Exception if an error occurs
     */
    public HashMap<Integer,Double> getUtilizationVector(CSDFGraph graph) throws Exception {
          String scriptResult = _runDARTS(graph,"utilization");
                if(isError(scriptResult))
            throw new Exception(" utilization vector calculation error. " + scriptResult);

        return _parseUtilVec(scriptResult,graph.countNodes());
    }

    /**
     * Get repetition vector of CSDF graph
     * @param graph CSDF graph
     * @return repetition vector of CSDF graph
     * @throws Exception if an error occurs
     */
    public String getBottleneckActor(CSDFGraph graph) throws Exception{
        String scriptResult = _runDARTS(graph,"bottleneck");
        if(isError(scriptResult))
            throw new Exception(" repetition vector calculation error. " + scriptResult);

        return scriptResult;
    }

    /**
     * CSDF graph consistency checkout
     * @param graph CSDF graph to be checked
     * @return true, if CSDF graph is consistent and fals otherwise
     * @throws Exception if an error occurs
     */
    public boolean checkConsistency(CSDFGraph graph) throws Exception{
        String scriptResult = _runDARTS(graph,"consistency");
        if(isError(scriptResult))
            throw new Exception(" consistency checkout error. " + scriptResult);
        if(scriptResult.equals("true"))
            return true;
        return false;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                ///


    /**
     * Run DARTS tool
     * @param graph CSDF graph to be processed
     * @param command processing command:
     * -eval : for graph evaluation
     * -rep_vec : for repetition vector calculation
     * @return result, returned to command line by DARTS
     */
    private String _runDARTS(CSDFGraph graph, String command){
        String scriptResult;
       /** add file extension if not mentioned*/
       try{
            /** create temp file and save model in .json format */
            File tempFile = File.createTempFile(graph.getName(), ".json");
            PrintStream printStream = new PrintStream(tempFile);
            CSDFGraphJSONVisitor.callVisitor(graph,printStream);

            /** create external command and provide it with arguments*/
            String[] cmd = new String[5];

            /** check version of installed python: python -V */
            cmd[0] = Config.getInstance().getPythonCall();
            cmd[1] = _dartsInterfaceScriptAbsPath;
            cmd[2] = command;
            cmd[3] = _dartsTempDirRelPath;
            cmd[4] = tempFile.getName();

            /** call DARTS as external python script*/
            Process p = Runtime.getRuntime().exec(cmd);

            /** retrieve output from python script*/
            pythonListener pyl = new pythonListener(graph.getName(), p.getInputStream());
            pyl.start();
            do {
                try{ pyl.join(1000); }//check python listener every second, while it works
                catch (InterruptedException ex){}
            }
            while (pyl.isAlive());

            scriptResult = pyl.returnResult();

            /**remove temp file after the evaluation*/
            boolean fileRemoveErrorMet = !tempFile.delete();
            if(fileRemoveErrorMet) {
                System.err.println("temp file remove error!");
            }
        }
        catch (IOException e){ scriptResult = "Python script call call failed: IOStream error."; }
        catch (Exception e)  { scriptResult = "Unknown error."; }
        return scriptResult;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      Getters and setters                   ///

   /** public String getAppAbsPath() {
        return _appAbsPath;
    }

    public void setAppAbsPath(String appAbsPath) {
        this._appAbsPath = appAbsPath;
    }*/

    public String getDartsInterfaceScriptAbsPath() {
        return _dartsInterfaceScriptAbsPath;
    }

    public void setDartsInterfaceScriptAbsPath(String dartsInterfaceScriptAbsPath) {
        this._dartsInterfaceScriptAbsPath = dartsInterfaceScriptAbsPath;
    }

    public String getDartsTempDirRelPath() {
        return _dartsTempDirRelPath;
    }

    public void setDartsTempDirRelPath(String dartsTempDirRelPath) {
        this._dartsTempDirRelPath = dartsTempDirRelPath;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ///

    /**
     * Checks if python script returned error
     * @param pythonScriptResult result of python script run
     * @return true, if python script returned error and false otherwise
     */
    private boolean isError(String pythonScriptResult){
        if(pythonScriptResult==null){
             System.err.print("Evaluation error: null response");
        }

        if(pythonScriptResult.contains("Error")) {
            System.err.print("DARTS python interface call failed: "+pythonScriptResult);
            return true;
        }
        if(pythonScriptResult=="") {
              System.err.print("DARTS python interface call failed: no response");
            return true;
        }

        return false;
    }

    /**
     * Converts result of python script run into CSDFEvalResult or CSDFEvalError object
     * @param pythonScriptResult result of the DARTS python script call
     * @return CSDFEvalResult or CSDFEvalError object, corresponding to DARTS python script call
     */
    private  CSDFEvalResult convertEvalResultToJavaClass(String pythonScriptResult){
       CSDFEvalResult result;
       if(isError(pythonScriptResult))
           result = new CSDFEvalError(pythonScriptResult);
       else {
           try { result = (CSDFEvalResult) _jsonParser.fromJson(pythonScriptResult, CSDFEvalResult.class); }
           catch (Exception e) { result = new CSDFEvalError("JSON eval result parsing error " +e.getMessage()); }
       }
     return result;
    }

    /**
     * Parse repetitions vector description in Map<CSDF node id, repetitions>
     * @param strToParse string description of the repetition vector
     * @return repetitions vector description in Map<CSDF node id, repetitions>
     */
    public HashMap<Integer,Integer> parseRepVec(String strToParse, int expectedSize) throws Exception {
          HashMap<Integer,Integer> repetitions = new HashMap<>();
          if(!strToParse.contains("="))
              return repetitions;

          String preprocessed = strToParse.replace("[","");
          int lastCommaId = preprocessed.lastIndexOf(",");
          preprocessed = preprocessed.substring(0,lastCommaId);
          String[] splitted = preprocessed.split(",");

              String[] strIndexPair;
              int first, second;
              for (String part : splitted) {
                  strIndexPair = part.split("=");
                  first = Integer.parseInt(strIndexPair[0].trim());
                  second = Integer.parseInt(strIndexPair[1].trim());
                  repetitions.put(first,second);
              }

          if(repetitions.size()!=expectedSize)

              throw new Exception("Repetition vector size " + repetitions.size() +
                      " differs from expected "+ expectedSize);
              return repetitions;
      }

       /**
     * Parse utilization description in Map<CSDF node id, actor utilization>
     * @param strToParse string description of the utilization vector
     * @return utilization vector description in Map<CSDF node id, repetitions>
     */
    private HashMap<Integer,Double> _parseUtilVec(String strToParse, int expectedSize) throws Exception {
          HashMap<Integer,Double> utilizations = new HashMap<>();
          if(!strToParse.contains("="))
              return utilizations;

          String preprocessed = strToParse.replace("[","");
          int lastCommaId = preprocessed.lastIndexOf(",");
          preprocessed = preprocessed.substring(0,lastCommaId);
          String[] splitted = preprocessed.split(",");

              String[] strIndexPair;
              int first;
              double second;
              for (String part : splitted) {
                  strIndexPair = part.split("=");
                  first = Integer.parseInt(strIndexPair[0].trim());
                  second = Double.parseDouble(strIndexPair[1].trim());
                  utilizations.put(first,second);
              }

          if(utilizations.size()!=expectedSize)

              throw new Exception("Repetition vector size " + utilizations.size() +
                      " differs from expected "+ expectedSize);
              return utilizations;
      }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///

    /**JSON parser, implements conversion of DARTS results into CSDFEvalResult class*/
    private  JSONParser _jsonParser = JSONParser.getInstance();

    /** Converter from CNN to SDF*/
    private CNN2CSDFGraphConverter _cnn2SDFConverter = new CNN2CSDFGraphConverter();

    /** abs path to application directory*/
    private String _appAbsPath = Config.getInstance().getAppPath();

    /** relative path to current folder*/
    private String _interfaceDirRelPath =  "./src/espam/interfaces/python";

    /**absolute path to DARTS directory*/
    private  String _dartsAbsPath;

    /**absolute path to DARTS directory*/
    private String _dartsInterfaceScriptAbsPath;

    /** path to temp directory with files*/
    private String _dartsTempDirRelPath = "./";
}







