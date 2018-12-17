package espam.parser.json.pyplot;

import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.io.PrintStream;
import java.util.Vector;

/**
 * Evaluation cases JSON visitor for pyplot
 */
public class EvalCasesJSONVisitor {

    /**
     * Create new EvalCasesJSONVisitor
     */
    public EvalCasesJSONVisitor(){ }

    /**
     * Read all evaluation cases, saved as .json files from a directory
     * @param dir directory
     * @return list of the evaluation cases
     */
    public Vector<CSDFEvalResult> getEvalResults(String dir){
        Vector<String> allJsonFiles = FileWorker.getAllFilePaths(dir,"json");

        Vector<CSDFEvalResult> evalResults = new Vector<>();
        for(String jsonPath: allJsonFiles){
            try{
                CSDFEvalResult eval =_getEvalCase(jsonPath);
                evalResults.add(eval);
            }
            catch (Exception e){
                System.err.println(jsonPath + " evaluation case parsing error " + e.getMessage());
            }
        }


        return evalResults;

    }

    /**
     * Get evaluation case from a path
     * @param path path with evaluation case in .json format
     * @return evaluation case
     */
    private CSDFEvalResult _getEvalCase(String path) throws Exception{
        String jsonEval = FileWorker.read(path);
        CSDFEvalResult evalRes = (CSDFEvalResult) JSONParser.getInstance().fromJson(jsonEval,CSDFEvalResult.class);
        return evalRes;
    }

    /**
     * generate json file for pyplot
     * @param evalResults list of evaluation results
     * @param axis axis for graphic
     * @param dir directory to save a file
     * @param filename name of the file
     * @throws Exception if an error occurs
     */
    public  void generateJSONForPlot(Vector<CSDFEvalResult> evalResults,
                                           Vector<pyplotAxis> axis, String dir, String filename) throws Exception{
        int axisSize = axis.size();
        if(axisSize<1 || axisSize>3)
            throw new Exception("Invalid axis number: " + axisSize + " . Possible axis number: 1, 2 or 3 ");

        PrintStream printStream = FileWorker.openFile(dir,filename,"json");
        /** open plot description*/
        printStream.println("{");
        printStream.println("  \"axis_number\": " + axisSize + ",");
        _printAxis(printStream,axis,"  ");
        printStream.println(",");
        _printValues(printStream,evalResults,axis,"  ");
       /** close plot description*/
        printStream.println("}");
    }


    /**
     * Write names of the axis
     * @param printStream printstream
     */
    private  void _printAxis(PrintStream printStream, Vector<pyplotAxis> axis, String prefix){
       //  _printStream.println(_prefix + "\"type\": \"" + x.getType() + "\",");
        pyplotAxis curAxis;

        printStream.println(prefix + "\"axis\": [");

        int axisSize = axis.size();
        for(int i=0; i<axisSize;i++) {
            curAxis = axis.elementAt(i);
            printStream.print(prefix + "  \"" + curAxis.toString() +
                    pyplotAxis.getMeasurementUnitDesc(curAxis) + "\"");
            if(i<axisSize-1)
                printStream.println(",");
            else
                printStream.println("");
        }
        printStream.println(prefix + "]");
    }


    /**
     * Print axis values
     * @param evalResults list of evaluation results
     * @param axis axis for graphic
     */
    private  void _printValues(PrintStream printStream, Vector<CSDFEvalResult> evalResults,
                                           Vector<pyplotAxis> axis, String prefix){
        pyplotAxis curAxis;
        Double [] curValues;
        String jsonValues;
        printStream.println(prefix + "\"values\": [");
        int axisSize = axis.size();
        for(int i=0; i<axisSize;i++) {
            curAxis = axis.elementAt(i);
            curValues = _getValues(evalResults,curAxis);
            jsonValues = JSONParser.getInstance().toJson(curValues);
            printStream.print(jsonValues);
            if(i<axisSize-1)
                printStream.println(",");
            else
                printStream.println("");
        }
        printStream.println(prefix + "]");
    }

    /**
     * Get values for pyplot axis
     * @param evalResults evaluation results
     * @param axis name of the axis
     */
    private  Double[] _getValues(Vector<CSDFEvalResult> evalResults, pyplotAxis axis){
        Double[] values = new Double[evalResults.size()];
        int i = 0;

        switch (axis){
            case memory:{
                for(CSDFEvalResult evalResult: evalResults){
                    values[i] = evalResult.getMemory();
                    i++;
                }
                return values;
            }

            case performance:{
                for(CSDFEvalResult evalResult: evalResults){
                    values[i] = evalResult.getPerformance();
                    i++;
                }
                return values;
            }

            case energy:{
                for(CSDFEvalResult evalResult: evalResults){
                    values[i] = evalResult.getEnergy();
                    i++;
                }
                return values;
            }

            case processors:{
                for(CSDFEvalResult evalResult: evalResults){
                    values[i] = (double)evalResult.getProcessors();
                    i++;
                }
                return values;
            }

            default: return values;
        }

    }


    /**
     *
     * colors = ['r', 'r', 'r', 'r', 'r']
     * markers = ['o', 'o', 'o', 'o', 'o']
     * names = ['bvlc_alexnet_LB', 'bvlc_alexnet_BB_40', 'bvlc_alexnet_BB_100', 'vgg19_LB', 'vgg19_BB_100']
     * ids = ['0', '1', '2', '3', '4', '5']
     * TODO PRINT
     *
     */

    /**
     *
     *
     *
     */
    private void _printColors(){

    }

}
