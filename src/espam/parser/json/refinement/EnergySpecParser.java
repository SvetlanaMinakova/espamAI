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


    public void parseEnergySpec(String path) {

        try {

            String json = FileWorker.read(path);

            /**
             * deserialize node

            JsonObject object = JSONParser.getInstance().getGson(json);
            int id = object.get("id").getAsInt();
            String name = object.get("name").getAsString();
            String op = object.get("function").getAsString();

            CSDFNode node = new CSDFNode(name, id);
            node.setOperation(op);         */

        }
        catch (Exception e){
            System.err.println("Energy specification parsing error: "+e.getMessage());
        }
    }




}
