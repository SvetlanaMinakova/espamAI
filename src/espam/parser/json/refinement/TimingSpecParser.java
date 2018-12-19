package espam.parser.json.refinement;

import com.google.gson.JsonObject;
import espam.operations.refinement.CSDFTimingRefiner;
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
    public static HashMap<String,Integer>  parseTimingSpecTemplate(String path){
        try {
            String json =  FileWorker.read(path);
            HashMap<String,Integer> operators;
            operators = (HashMap<String,Integer>)JSONParser.getInstance().fromJson(json,CSDFTimingRefiner.getInstance().getBasicOperationsTiming().getClass());
            return operators;
        }
        catch(Exception e){
            System.err.println("wcet parsing error: " + e.getMessage());
            return null;
        }

    }


}
