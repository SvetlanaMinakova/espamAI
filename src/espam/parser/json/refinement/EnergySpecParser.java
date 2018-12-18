package espam.parser.json.refinement;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.refinement.CSDFGEnergyRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

/**
 * Energy specification parser
 */
public class EnergySpecParser {


    public static void parseEnergySpec(String path) {

        try {

            String json = FileWorker.read(path);
            CSDFGEnergyRefiner parsed = (CSDFGEnergyRefiner)JSONParser.getInstance().fromJson(json,CSDFGEnergyRefiner.class);
            CSDFGEnergyRefiner.getInstance().setAlpha(parsed.getAlpha());
            CSDFGEnergyRefiner.getInstance().setBeta(parsed.getBeta());
            CSDFGEnergyRefiner.getInstance().setB(parsed.getB());
        }
        catch (Exception e){
            System.err.println("Energy specification parsing error: "+e.getMessage());
        }
    }




}
