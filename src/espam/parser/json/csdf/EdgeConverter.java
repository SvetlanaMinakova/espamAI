package espam.parser.json.csdf;
import com.google.gson.*;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.csdf.*;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import java.lang.reflect.Type;


public class EdgeConverter  implements JsonSerializer<Edge>, JsonDeserializer<Edge> {

    public JsonElement serialize(Edge edge, Type type,
                                 JsonSerializationContext context) throws JsonParseException{
        Gson gson = new Gson();
        JsonObject edgeObject = new JsonObject();
        if(!(edge instanceof CSDFEdge))
            throw new JsonParseException("unknown edge type");

        edgeObject.addProperty("id",((CSDFEdge)edge).getId());
        edgeObject.addProperty("name", edge.getName());

        String strSrc = gson.toJson(((CSDFEdge) edge).getSrcId());
        JsonArray src = gson.fromJson(strSrc,JsonArray.class);
        edgeObject.add("src",src);

        String strDst = gson.toJson(((CSDFEdge) edge).getDstId());
        JsonArray dst = gson.fromJson(strDst,JsonArray.class);
        edgeObject.add("dst",dst);

        return edgeObject;
    }

    public CSDFEdge deserialize(JsonElement json, Type type,
                               JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        JsonObject object = json.getAsJsonObject();
        int edgeId = object.get("id").getAsInt();
        String edgeName = object.get("name").getAsString();
        CSDFEdge edge = new CSDFEdge(edgeName,edgeId);

        /**
         * src and dst edge descriptions have complex structure and serialized as JsonArrays
         */
        JsonArray srcPortDescription = object.get("src").getAsJsonArray();
        int[] srcId = new int[2];
        srcId[0]= gson.fromJson(srcPortDescription.get(0), Integer.TYPE);
        srcId[1]= gson.fromJson(srcPortDescription.get(1), Integer.TYPE);

        JsonArray dstPortDescription = object.get("dst").getAsJsonArray();
        int[] dstId = new int[2];
        dstId[0]= gson.fromJson(dstPortDescription.get(0), Integer.TYPE);
        dstId[1]= gson.fromJson(dstPortDescription.get(1), Integer.TYPE);

        edge.setSrcId(srcId);
        edge.setDstId(dstId);

       return edge;
    }
}
