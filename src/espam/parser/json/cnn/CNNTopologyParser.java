package espam.parser.json.cnn;

import espam.datamodel.graph.cnn.NetworkTopology;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

public class CNNTopologyParser extends JSONParser{
    public NetworkTopology parseNetworkTopology(String path){
        try {
           String json = FileWorker.read(path);
           NetworkTopology topology = getGson().fromJson(json,NetworkTopology.class);
           return topology;
        }

        catch (Exception e){
            System.err.print("CNN topology parsing error: " + e.getMessage());
            return  null;
        }

    }


}
