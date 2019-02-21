package espam.parser.json.platform;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;
import java.util.Map;

/** ALOHA project NeurAghe Platform parser*/
public class NeurAghePlatformParser {
    /**
     * print setup times from configuration in .json format
     * This version of timing template does not support mapping.
     * TODO: version supports evaluation with mapping
     * @param path path to the timing specification
     */
    public static HashMap<String,Integer> parseTimingSpecTemplate(String path){
        try {

           //   System.out.println(strJSON);
           /** JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);*/
            HashMap<String,Integer> operatorsExecTimes = new HashMap<>();

            String strJSON =  FileWorker.read(path);

            JsonObject pla = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            /**get cores*/
            JsonArray cores = pla.get("cores").getAsJsonArray();
            for(JsonElement core: cores) {
                /**for each core get supported operators*/
                JsonArray supportedOps = ((JsonObject) core).get("supported_operator").getAsJsonArray();
                /** for each supported operator:
                 * - if the operator is not in the operators HashMap, add operator to HashMap.
                 * - if the operator is in list, but exec_time for this core is the worst-case time (WCET),
                 *      replace existing exec_time by current exec_tim
                 */
                for(JsonElement supportedOp: supportedOps) {
                    JsonObject supportedOpObj = (JsonObject) supportedOp;
                    String supportedOpName = supportedOpObj.get("name").getAsString();
                    JsonArray opModes = supportedOpObj.getAsJsonArray("operator_mode");
                    /** opMode contains relevant information for time and memory(?) specs
                     *  opMode contains performance (exec_time) in GOPs --> the smallest performance is the worst.*/
                    for (JsonElement opMode : opModes) {
                        int cur_performance = ((JsonObject)opMode).get("performance").getAsInt();
                        if (operatorsExecTimes.containsKey(supportedOpName)){
                            int old_performance = operatorsExecTimes.get(supportedOpName);
                            if(cur_performance<old_performance) {
                                operatorsExecTimes.replace(supportedOpName, cur_performance, old_performance);
                            }
                        }
                        else
                            operatorsExecTimes.put(supportedOpName,cur_performance);
                    }
                    }
                }
          //  for(Map.Entry<String,Integer> operatorsExecTime: operatorsExecTimes.entrySet())
            //    System.out.println(operatorsExecTime.getKey()+" : "+operatorsExecTime.getValue());

            return operatorsExecTimes;
        }
        catch(Exception e){
            System.err.println("NeurAghe Platform parsing error: " + e.getMessage());
            return null;
        }

    }



}
