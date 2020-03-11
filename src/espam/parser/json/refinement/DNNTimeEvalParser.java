package espam.parser.json.refinement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;
import java.util.Vector;

public class DNNTimeEvalParser {
    /**
     * print setup times from configuration in .json format
     * @param path path to the timing specification
     */
    public static HashMap<String,Vector<Double>> parseTimingEval(String path){
        try {
            String strJSON =  FileWorker.read(path);
            JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            HashMap<String,Vector<Double>> evals = new HashMap<>();
            for (HashMap.Entry<String,JsonElement> kv: opList.entrySet()){
                String key = kv.getKey();
                String value = kv.getValue().toString();
                /**TODO: refactoring*/
                if(key.toLowerCase().contains("cpu") || key.toLowerCase().contains("gpu")) {
                    Vector<Double> val = new Vector<>();
                    val  = (Vector<Double>)JSONParser.getInstance().fromJson(value,val.getClass());
                    evals.put(key, val);
                }
            }

            return evals;
        }
        catch(Exception e){
            System.err.println(path + " evaluations parsing error: " + e.getMessage());
            return null;
        }

    }


}
