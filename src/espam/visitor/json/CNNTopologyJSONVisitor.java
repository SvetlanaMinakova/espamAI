package espam.visitor.json;


import com.google.gson.Gson;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.NetworkTopology;
import espam.utils.fileworker.FileWorker;

import java.io.PrintStream;

public class CNNTopologyJSONVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call CNN json visitor
     * @param dnn dnn with Topology to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir){

           try {
            NetworkTopology dnnTopology = new NetworkTopology(dnn);
            PrintStream printStream = FileWorker.openFile(dir, dnnTopology.getName() + "Topology", "json");
            String json = _gson.toJson(dnnTopology);
            printStream.print(json);
            System.out.println("JSON file generated: " + dir +"/" + dnnTopology.getName() + "Topology" + ".json");
            }
            catch(Exception e) {
             System.err.println("JSON dnn Topology visitor call error. " + e.getMessage());
            }
    }


    /**
     * Call CNN json visitor
     * @param dnnTopology dnn Topology to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(NetworkTopology dnnTopology, String dir){

           try {
            PrintStream printStream = FileWorker.openFile(dir, dnnTopology.getName(), "json");
            String json = _gson.toJson(dnnTopology);
            printStream.print(json);
            System.out.println("JSON file generated: " + dir + dnnTopology.getName() + ".json");
            }
            catch(Exception e) {
             System.err.println("JSON dnn visitor call error. " + e.getMessage());
            }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///
   /**Standard JSON-parser, implements parsing of non-nested types*/
    private static Gson _gson = new Gson();

}
