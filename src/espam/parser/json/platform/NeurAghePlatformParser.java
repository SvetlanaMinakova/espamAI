package espam.parser.json.platform;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.ARM;
import espam.datamodel.platform.processors.GPU;
import espam.datamodel.platform.processors.HWCE;
import espam.datamodel.platform.processors.Processor;
import espam.operations.evaluation.PlatformEval;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/** ALOHA project NeurAghe Platform parser*/
public class NeurAghePlatformParser {


  /**Print list of platform cores with supported operators
  * @param path path to NEURAGHE platform specification (in .json format)
  * @return list of platform cores and operators supported by them
  * @param path
   */
    public static void printSupportedOperators(String path){
        HashMap<String,Vector<String>> supportedOps= getSupportedOperators(path);
        for(Map.Entry<String, Vector<String>> op: supportedOps.entrySet()){
            System.out.println("Core: " + op.getKey());
            System.out.print("Ops: ");
            for(String opName: op.getValue())
                System.out.print(opName+ ", ");
               System.out.println();
           }

    }

/**
  * Get list of operators, supported by the platform cores
  * @param path path to NEURAGHE platform specification (in .json format)
  * @return list of platform cores and operators supported by them
   */
public static HashMap<String,Vector<String>> getSupportedOperators(String path){
        HashMap<String,Vector<String>> coresAndOps= new HashMap<>();
     try {
            String strJSON =  FileWorker.read(path);

            JsonObject pla = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);

            /**get cores*/
            JsonArray cores = pla.get("cores").getAsJsonArray();
            JsonArray supportedOps;
            String opName;
            for(JsonElement core: cores) {
                /**for each core generate processor*/
                String coreName = ((JsonObject) core).get("name").getAsString();
                Vector<String> ops = new Vector<>();
                //System.out.println("core: " + coreName);
                supportedOps = ((JsonObject) core).get("supported_operator").getAsJsonArray();
                if(supportedOps!=null) {
                    for (JsonElement supportedOp: supportedOps) {
                        opName = ((JsonObject) supportedOp).get("name").getAsString();
                        ops.add(opName);
                        //System.out.println(opName);
                    }
                }

                coresAndOps.put(coreName,ops);
            }

            //platform.setResourceList(resources);
            return coresAndOps;
        }
        catch(Exception e){
            System.err.println("NeurAghe Platform parsing error: " + e.getMessage());
            return null;
        }

}


/**
  * Print list of compound nodes, supported by the platform
  * @param path path to NEURAGHE platform specification (in .json format)
   */
public static void printCompounds(String path){
    Vector<Vector<String>> compounds = getCompounds(path);
    System.out.print(" compounds: ");
    for(Vector<String> compound: compounds) {
        for(String compoundEl: compound)
            System.out.print(compoundEl + "->");
        System.out.print(", ");
    }
    System.out.println();
}

/**
  * Get list of compound nodes, supported by the platform
  * @param path path to NEURAGHE platform specification (in .json format)
  * @return list of compound nodes, supported by the platform
   */
public static Vector<Vector<String>> getCompounds(String path){
    Vector<Vector<String>> compounds = new Vector<>();
    Vector<String> strcompounds = new Vector<>();
    /** get string compounds*/
    try {
         HashMap<String,Vector<String>> supportedOps = getSupportedOperators(path);
         for(Vector<String> ops: supportedOps.values()){
             for(String op: ops){
                 if(op.contains("+")){
                     if(!(strcompounds.contains(op)))
                         strcompounds.add(op);
                 }
             }
         }

    /** represent string compounts as vectors with compound elements, sorted by size*/
         for(String strCompound: strcompounds) {
             Vector<String> compoundElements = new Vector<>();
             strCompound = strCompound.replace("+","_");
             String [] compoundEls = strCompound.split("_");
             for(String compoundElement: compoundEls)
                 compoundElements.add(compoundElement);

             Integer newCompoundSize = compoundElements.size();
             Integer insertPos = 0;
             for(Vector<String> compond: compounds){
                 if(compond.size()<newCompoundSize)
                     insertPos = compounds.indexOf(compond);
             }

             compounds.add(insertPos, compoundElements);
         }

        }
        catch(Exception e){
            System.err.println("NeurAghe Platform compounds getting error: " + e.getMessage());
        }

        return compounds;
}

/**
* GET ESPAM platform specification from NEURAGHE platform specification
* @param path path to NEURAGHE platform specification (in .json format)
* @return ESPAM platform specification
*/
public static Platform parsePlatform(String path){
        try {
            Platform platform = new Platform("espam_platform");
            Vector<Resource> resources = new Vector<>();

           //   System.out.println(strJSON);
           /** JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);*/
          //  HashMap<String,Integer> operatorsExecTimes = new HashMap<>();

            String strJSON =  FileWorker.read(path);

            JsonObject pla = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            /** get platform name*/
            try {
                String name = pla.get("name").getAsString();
                if(name!=null)
                    platform.setName(name);
            }
            catch (Exception e){ }

            /**get cores*/
            JsonArray cores = pla.get("cores").getAsJsonArray();
            for(JsonElement core: cores) {
                /**for each core generate processor*/
                String coreName = ((JsonObject) core).get("name").getAsString();
                //System.out.println("core: " + coreName);
                Processor proc = _generateProcessor(coreName);
                /** TODO: refactoring*/
                if( proc instanceof ARM) {
                    try{
                        String subtype = ((JsonObject) core).get("subtype").getAsString();
                        ((ARM) proc).setSubType(subtype);
                    }
                    catch (Exception e){

                    }
                }

                if(proc!=null) {
                    resources.add(proc);
                }
            }

            platform.setResourceList(resources);
            return platform;
        }
        catch(Exception e){
            System.err.println("NeurAghe Platform parsing error: " + e.getMessage());
            return null;
        }

    }

    /**
     * GET time/memory/energy NEURAGHE platform characteristics
     * @param path path to NEURAGHE platform specification (in .json format)
     * @return time/memory/energy NEURAGHE platform characteristics
     */
    public static PlatformEval parsePlatformEval(String path){
        try {
            String strJSON =  FileWorker.read(path);
            PlatformEval platfromEval = (PlatformEval) JSONParser.getInstance().fromJson(strJSON,PlatformEval.class);
            return platfromEval;
        }
        catch(Exception e){
            System.err.println("NeurAghe Platform parsing error: " + e.getMessage());
            return null;
        }

    }

    /**
     * TODO: REFACTORING
     * Generate espam processor from NEURAGHE name
     * @param name NEURAGHE name
     * @return espam processor
     */
    private static Processor _generateProcessor(String name){
        if(name.contains("ARM"))
            return new ARM(name);

        if(name.toLowerCase().contains("gpu"))
            return new GPU(name);

        if(name.contains("HWCE"))
            return new HWCE("HWCE");

        System.err.println("Unrecognized processor type: " + name);
        return null;

       // return new Processor("cpu_" + cpuId);
    }


    /**
     * extract time specification
     * This version of timing template does not support mapping.
     * TODO: version supports evaluation with mapping
     * @param path path to the timing specification
     */
    public static HashMap<String,Long> parseTimingSpecTemplate(String path){
        try {

           //   System.out.println(strJSON);
           /** JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);*/
            HashMap<String,Long> operatorsExecTimes = new HashMap<>();

            String strJSON =  FileWorker.read(path);

            JsonObject pla = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            /**get processor types*/
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
                        long cur_performance = ((JsonObject)opMode).get("performance").getAsLong();
                        if (operatorsExecTimes.containsKey(supportedOpName)){
                            long old_performance = operatorsExecTimes.get(supportedOpName);
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

    /**
     * extract time specification
     * This version of timing template does not support mapping.
     * TODO: version supports evaluation with mapping
     * @param path path to the timing specification
     */
    public static HashMap<String,Double> getWCEnergy(String path){

            Double energy;
            String name;
            HashMap<String,Double> WCEnergy  = new HashMap<>();

        try {
            String strJSON =  FileWorker.read(path);

            JsonObject pla = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            /**get cores*/
            JsonArray cores = pla.get("cores").getAsJsonArray();
            for(JsonElement core: cores) {
                /**for each core get max power*/
                energy = ((JsonObject)core).get("max_power").getAsDouble();
                name = ((JsonObject)core).get("name").getAsString();
                WCEnergy.put(name,energy);

                }
            return WCEnergy;
        }
        catch(Exception e){
            System.err.println("NeurAghe Platform parsing error: " + e.getMessage());
            return WCEnergy;
        }

    }



}
