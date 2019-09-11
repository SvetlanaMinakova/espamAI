package espam.parser.json.refinement;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.util.HashMap;

/**
 * Energy specification parser
 */
public class EnergySpecParser {


    public static HashMap<String,Double> parseEnergySpec(String path) {

        HashMap<String,Double> operators = new HashMap<>();
        try {

            String strJSON = FileWorker.read(path);
            JsonObject opList = (JsonObject) JSONParser.getInstance().fromJson(strJSON,JsonObject.class);
            for (HashMap.Entry<String,JsonElement> kv: opList.entrySet()){
                String key = kv.getKey();
                double val = kv.getValue().getAsDouble();
                operators.put(key,val);
            }

        }
        catch (Exception e){
            System.err.println("Energy specification parsing error: "+e.getMessage());
        }

        return operators;
    }




}
