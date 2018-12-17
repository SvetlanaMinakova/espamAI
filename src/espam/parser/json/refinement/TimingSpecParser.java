package espam.parser.json.refinement;

import com.google.gson.JsonObject;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

/**
 * JSON parser of CSDF model timing specificatiobn
 */
public class TimingSpecParser {
    /**
     * print setup times from configuration in .json format
     * @param path path to the timing specification
     */
    public static void parseTimingSpecTemplate(String path){
        try {
            String json =  FileWorker.read(path);
            CSDFTimingRefiner parsed =(CSDFTimingRefiner)JSONParser.getInstance().fromJson(json,CSDFTimingRefiner.class);
            CSDFTimingRefiner.getInstance().setBasicOperationsTiming(parsed.getBasicOperationsTiming());
            CSDFTimingRefiner.getInstance().setTimeScale(parsed.getTimeScale());
        }
        catch(Exception e){
            System.err.println("wcet parsing error: " + e.getMessage());
        }

    }


}
