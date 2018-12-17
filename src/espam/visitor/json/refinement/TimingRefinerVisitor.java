package espam.visitor.json.refinement;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Visitor of the timing refiner
 */
public class TimingRefinerVisitor {

    /** Visit timing refiner */
    public static void visitTimingRefiner( String dir, String filename){
        try {
            PrintStream printStream = FileWorker.openFile(dir,filename,"json");
            printStream.println("{");

            HashMap<String,Integer> operators = _getDefaultTimingSpec();
            int commaBorder = operators.size()-1;
            printStream.println("  \"operators \": {");
            for(HashMap.Entry<String,Integer> op: operators.entrySet()) {
                printStream.print("    \"" + op.getKey() + "\":" + op.getValue());
                if (commaBorder > 0) {
                    printStream.println(",");
                    commaBorder--;
                }
            }
            printStream.println("  },");
            printStream.println("  \"scale\": " + CSDFTimingRefiner.getInstance().getTimeScale());
            printStream.println("}");
            printStream.close();
            System.out.println(dir + filename +".json WCET specification generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }
    }

    /** Visit timing refiner of a specific graph*/
    public static void visitTimingRefiner(CSDFGraph graph, String dir, String filename){
     try {
            PrintStream printStream = FileWorker.openFile(dir,filename,"json");
            printStream.println("{");

            HashMap<String,Integer> operators = _getTimingSpec(graph);
            int commaBorder = operators.size()-1;
            printStream.println("  \"operators \": {");
            for(HashMap.Entry<String,Integer> op: operators.entrySet()) {
                printStream.println("    \"" + op.getKey() + "\":" + op.getValue());
                if (commaBorder > 0) {
                    printStream.print(",");
                    commaBorder--;
                }
            }
            printStream.println("  },");
            printStream.println("  \"scale\": " + CSDFTimingRefiner.getInstance().getTimeScale());
            printStream.println("}");
            printStream.close();
            System.out.println(dir + filename +".json WCET specification generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }
    }


    /**
     * print current exec times configuration in .json format
     */
    private static HashMap<String,Integer> _getDefaultTimingSpec(){
        try {
            HashMap<String,Integer> execTimes = CSDFTimingRefiner.getInstance().getBasicOperationsTiming();
            return execTimes;
        }
        catch(Exception e){
            System.err.println("wcets printout error: " + e.getMessage());
            return null;
        }

    }

     /**
     * Print required set of operations for an arbitrary CSDF graph
     * @param graph CSDF graph
     */
    private static HashMap<String,Integer> _getTimingSpec(CSDFGraph graph) {
        try {
            HashMap<CSDFNode, Vector<Integer>> times = CSDFTimingRefiner.getInstance().getExecTimes(graph);
            HashMap<String, Integer> opnamesDistinct = new HashMap<>();
            Integer defaultTime = 1;
            opnamesDistinct.put("read", defaultTime);
            opnamesDistinct.put("write", defaultTime);
            //Vector<String> graphOperations = graph.getOpListDistinct();

            Iterator i;
            i = graph.getNodeList().iterator();
            while (i.hasNext()) {
                CSDFNode node = (CSDFNode) i.next();
                String op = node.getOperation();
                if (!opnamesDistinct.containsKey(op)) {
                    Vector<Integer> nodetimes = times.get(node);
                    opnamesDistinct.put(op, _findWcet(nodetimes));
                }
            }
            return opnamesDistinct;
        } catch (Exception e) {
            System.err.println(" wcets printout error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Print required set of operations for an arbitrary CSDF graph
     * @param graph CSDF graph
     */
    private static HashMap<String,Integer> _getTimingSpecTemplate(CSDFGraph graph){
        try {
            HashMap<String, Integer> opnamesDistinct = new HashMap<>();
            Integer defaultTime = 1;
            opnamesDistinct.put("read", defaultTime);
            opnamesDistinct.put("write", defaultTime);
            Iterator i;
            i = graph.getNodeList().iterator();
            while (i.hasNext()) {
                CSDFNode node = (CSDFNode) i.next();
                String op = node.getOperation();
                if (!opnamesDistinct.containsKey(op)) {
                    opnamesDistinct.put(op, defaultTime);
                }
            }
            return opnamesDistinct;
        }
        catch(Exception e){
            System.err.println(" wcets printout error: " + e.getMessage());
            return null;
        }

    }

    /**
     * Find worst-case execution time in the list
     * @param times list of the times
     * @return worst-case execution time in the list
     */
    private static Integer _findWcet(Vector<Integer> times){
        Integer maxTime = 0;
        for(Integer time: times){
            if(time>maxTime)
                maxTime = time;
        }
        return maxTime;
    }

}
