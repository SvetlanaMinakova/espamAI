package espam.parser.json.refinement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;

/**
 * JSON parser of CSDF model timing specificatiobn
 */
public class TimingSpecParser {
    /**
     * print setup times from configuration in .json format
     * @param path path to the timing specification
     */
    public static HashMap<String,Long> parseTimingSpecTemplate(String path){
        try {
            String strJSON =  FileWorker.read(path);
            JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            HashMap<String,Long> operators = new HashMap<>();
            for (HashMap.Entry<String,JsonElement> kv: opList.entrySet()){
                String key = kv.getKey();
                long val = kv.getValue().getAsLong();
                operators.put(key,val);
            }

            return operators;
        }
        catch(Exception e){
            System.err.println("wcet parsing error: " + e.getMessage());
            return null;
        }

    }


}
