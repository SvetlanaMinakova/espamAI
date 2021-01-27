package espam.visitor.json.refinement;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.evaluation.csdf.CSDFTimingRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Visitor of the timing refiner
 */
public class TimingRefinerVisitor {

    /** Visit timing refiner */
    public static void visitTimingRefiner(String dir, String filename){
        try {
            String json = JSONParser.getInstance().toJson(CSDFTimingRefiner.getInstance().getBasicOperationsTiming());
            FileWorker.write(dir,filename,"json",json);
            System.out.println(dir+filename + ".json file generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }
    }


      /**
     * Print default timing specification
     * @param dir output directory
     * @param filename output file name
     * @param graph CSDF graph
     */
    public static void printTimeSpec(Network dnn, CSDFGraph graph, String dir, String filename){
        try {
            HashMap<String,Long> timeSpec = _getTimingSpecTemplate(graph);
            HashMap<String,Long> dnnTimeSpec = _getTimingSpec(dnn);

            for(HashMap.Entry<String,Long> dnnOp: dnnTimeSpec.entrySet()){
                if(!timeSpec.containsKey(dnnOp.getKey())){
                    timeSpec.put(dnnOp.getKey(),dnnOp.getValue());
                }
            }

            String json = JSONParser.getInstance().toJson(timeSpec);
            FileWorker.write(dir,filename,"json",json);
            System.out.println(dir + filename + ".json file generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }

    }

    /**
     * Print default timing specification
     * @param dir output directory
     * @param filename output file name
     */
    public static void printTimeSpec(Network dnn, String dir, String filename){
        try {
            HashMap<String,Long> defaultTimeSpec = _getTimingSpec(dnn);

            String json = JSONParser.getInstance().toJson(defaultTimeSpec);
            FileWorker.write(dir,filename,"json",json);
            System.out.println(dir + "/" + filename + ".json file generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }

    }

      /**
     * Print default timing specification
     * @param dir output directory
     * @param filename output file name
     * @param graph CSDF graph
     */
    public static void printTimeSpec(CSDFGraph graph, String dir, String filename){
        try {
            HashMap<String,Long> timeSpec = _getTimingSpecTemplate(graph);
            String json = JSONParser.getInstance().toJson(timeSpec);
            FileWorker.write(dir,filename,"json",json);
            System.out.println(dir + "/" + filename + ".json file generated");
        }
        catch (Exception e){
             System.err.println(dir + filename +
                     ".json WCET specification generation error "+ e.getMessage());

        }

    }


    /**
     * Print wcet for DNN
     * @param dnn DNN
     * @return wcet specification for DNN
     */
    private static HashMap<String,Long> _getTimingSpec(Network dnn){
        try {
            Vector<Operator> opsDistinct = dnn.getOperatorsDistinct();
            HashMap<String,Long> opList = new HashMap<>();

            for(Operator op: opsDistinct){
                Long opTime = CSDFTimingRefiner.getInstance().getOpTime(op);
                opList.put(op.getName(),opTime);
            }
            return opList;
        }
        catch(Exception e){
            System.err.println(" wcets template generation error: " + e.getMessage());
            return null;
        }
    }


    /**
     * Print wcet specification for CSDF graph
     * @param graph CSDF graph
     * @return wcet specification for CSDF graph
     */
    private static HashMap<String,Long> _getTimingSpecTemplate(CSDFGraph graph){
        try {
            Vector<Operator> opsDistinct = graph.getOpListDistinct();
            HashMap<String,Long> opList = new HashMap<>();

            for(Operator op: opsDistinct){
                Long opTime = CSDFTimingRefiner.getInstance().getOpTime(op);
                opList.put(op.getName(),opTime);
            }
            return opList;
        }
        catch(Exception e){
            System.err.println(" wcets template generation error: " + e.getMessage());
            return null;
        }

    }

    /**
     * Get distinct list of operators
     * @param graph CSDF graph
     * @return distinct list of graph operators
     */
    private static HashMap<String,Integer> getOperatorsDistinct(CSDFGraph graph){
               try {
            HashMap<String, Integer> opnamesDistinct = new HashMap<>();
            Integer defaultTime = 1;
            Iterator i;
            i = graph.getNodeList().iterator();
            while (i.hasNext()) {
                CSDFNode node = (CSDFNode) i.next();
                String op = node.getFunction();
                if (!opnamesDistinct.containsKey(op.toLowerCase())) {
                    opnamesDistinct.put(op, defaultTime);
                }
            }
            return opnamesDistinct;
        }
        catch(Exception e){
            System.err.println(" wcets operator list generation error: " + e.getMessage());
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
