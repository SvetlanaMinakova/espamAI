package espam.interfaces.python;

import espam.main.Config;

import java.io.File;
import java.io.IOException;

/**
 * ONNX Weights extractor calls onnx_weight_extractor.py
 * and waits for respond
 */
public class ONNXWeightsExtractor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
         /**
     * Create new EspamToDarts interface instance
     */
    public ONNXWeightsExtractor() {
        _appAbsPath = Config.getInstance().getAppPath();

       //System.out.println(_appAbsPath);
        _scriptAbsPath = _appAbsPath + File.separator +
                _interfaceDirRelPath.replace("./","") +
                File.separator + _scriptName;
        _tempDirRelPath = System.getProperty("java.io.tmpdir");
    }


    /**
     * Extract weights from an onnx model and save them in weightsDir
     * as set of numpy arrays
     * @param onnxModelPath path to onnx model
     * @param metaDataPath path to metadata .json script
     * @param weightsDir directory to save weights
     * @param printDetails if the details of python script should be printed
     * @return true, if weights were successfully extracted and false otherwise
     */
    public boolean extractWeights(String onnxModelPath, String metaDataPath, String weightsDir, boolean printDetails){
        if(printDetails)
            System.out.println("Weights extraction...");
        String scriptResult;
        try {

            /** create external command and provide it with arguments*/
            String[] cmd = new String[5];

            /** check version of installed python: python -V */
            cmd[0] = Config.getInstance().getPythonCall();
            cmd[1] = _scriptAbsPath;
            cmd[2] = onnxModelPath;
            cmd[3] = metaDataPath;
            cmd[4] = weightsDir;
            //cmd[5] = "False";
            //if(printDetails)
            //    cmd[5] = "True";

            /** call external python script*/
            Process p = Runtime.getRuntime().exec(cmd);

            /** retrieve output from python script*/
            pythonListener pyl = new pythonListener("weights_extractor", p.getInputStream());
            pyl.start();
            do {
                try {
                    pyl.join(1000);
                }//check python listener every second, while it works
                catch (InterruptedException ex) {
                }
            }
            while (pyl.isAlive());

            scriptResult = pyl.returnResult();
        }
        catch (IOException e){ scriptResult = "Python script call call failed: IOStream error."; }
        catch (Exception e)  { scriptResult = "Unknown error."; }

        return _checkScriptResult(scriptResult);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                ///


    /**
     * Checks result of python script run
     * @param pythonScriptResult result of the onnx_weight_extractor.py script call
     * @return true, if onnx_weight_extractor.py successfully extracted weights and false otherwise
     */
    private boolean _checkScriptResult(String pythonScriptResult){

        if(pythonScriptResult==null){
             System.err.print("Evaluation error: null response");
             return false;
        }

        if(pythonScriptResult.contains("done")){
            return true;
        }

        if(pythonScriptResult.contains("Error")) {
            System.err.print("onnx_weight_extractor python interface call failed: " + pythonScriptResult);
            return false;
        }
        if(pythonScriptResult=="") {
              System.err.print("onnx_weight_extractor python interface call failed: no response");
            return false;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ///

    /** name of the callable python script*/
    private String _scriptName = "onnx_weights_extractor.py";

    /** abs path to the onnx_weight_extractor.py*/
    private String _scriptAbsPath = "";

    /** java temporary  files directory*/
    private String _tempDirRelPath;

    private String _appAbsPath = "";

    /** relative path to current folder*/
    private String _interfaceDirRelPath =  "./src/espam/interfaces/python";

}
