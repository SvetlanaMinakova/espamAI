package espam.parser.json.csdf;
import com.google.gson.*;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.Node;
import espam.datamodel.graph.csdf.*;
import java.lang.reflect.Type;
import java.util.Vector;

public class GraphConverter  implements JsonSerializer<CSDFGraph>, JsonDeserializer<CSDFGraph> {

    public JsonElement serialize(CSDFGraph graph, Type type,
                                 JsonSerializationContext context) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder()
                /**
                 * Apply SDFModel adaptors
                 */
                .registerTypeAdapter(NPort.class, new PortConverter())
                .registerTypeAdapter(CSDFNode.class, new NodeConverter())
                .registerTypeAdapter(CSDFEdge.class, new EdgeConverter());
        Gson gson = builder.create();

        JsonObject graphObject = new JsonObject();
        graphObject.addProperty("type", graph.getType().toString());

        graphObject.addProperty("name", graph.getName());
        /**register total nodes and edges number*/
        graphObject.addProperty("node_number", graph.getNodeList().size());
        graphObject.addProperty("edge_number", graph.getEdgeList().size());

        /**
         * add nodes
         */
        JsonArray nodeList = new JsonArray();
        for (Object node : graph.getNodeList()) {
            String strNode = gson.toJson(node,CSDFNode.class);
            JsonObject objNode = gson.fromJson(strNode,JsonObject.class);
            nodeList.add(objNode);
        }
        graphObject.add("nodes", nodeList);

        /**
         * add edges
         */
        JsonArray edgeList = new JsonArray();
        for (Edge edge : graph.getEdgeList()) {
            String strEdge = gson.toJson(edge,CSDFEdge.class);
            JsonObject objEdge = gson.fromJson(strEdge,JsonObject.class);
            edgeList.add(objEdge);
        }
        graphObject.add("edges", edgeList);

        return graphObject;

    }

    public CSDFGraph deserialize(JsonElement json, Type type,
                                JsonDeserializationContext context) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder()
                /**
                 * Apply SDFModel adaptors
                 */
                .registerTypeAdapter(NPort.class, new PortConverter())
                .registerTypeAdapter(CSDFNode.class, new NodeConverter())
                .registerTypeAdapter(Edge.class, new EdgeConverter())
                .registerTypeAdapter(CSDFEdge.class, new EdgeConverter());
        Gson gson = builder.create();

        JsonObject object = json.getAsJsonObject();

        String graphName = object.get("name").getAsString();
        CSDFGraph graph = new CSDFGraph(graphName);
        String graphType = object.get("type").getAsString();
        graph.setType(SDFGraphType.fromString(graphType));

        /**
        * add nodes
        */
        JsonArray nodeList = object.get("nodes").getAsJsonArray();
        Vector<Node> nodes = new Vector<Node>();
        for(JsonElement jnode:nodeList ) {
            nodes.add(gson.fromJson(jnode,CSDFNode.class));
        }
        graph.setNodeList(nodes);
        /**
        * add edges
        */
        JsonArray edgeList = object.get("edges").getAsJsonArray();
        Vector<Edge> edges = new Vector<Edge>();
        for(JsonElement jedge:edgeList){
            edges.add(gson.fromJson(jedge,CSDFEdge.class));
        }
        graph.setEdgeList(edges);

        return graph;
    }
}