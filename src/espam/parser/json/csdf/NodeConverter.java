package espam.parser.json.csdf;

import com.google.gson.*;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.CSDFEdge;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;

import java.lang.reflect.Type;
import java.util.Vector;


public class NodeConverter  implements JsonSerializer<CSDFNode>, JsonDeserializer<CSDFNode> {

    public JsonElement serialize(CSDFNode node, Type type,
                                 JsonSerializationContext context) throws JsonParseException{
        /**
         * register custom port and edge serializers
         */
        GsonBuilder builder = new GsonBuilder()
         .registerTypeAdapter(NPort.class,new PortConverter())
         .registerTypeAdapter(CSDFEdge.class,new EdgeConverter())
         .setPrettyPrinting();
         Gson gson = builder.create();
        /**
        * serialize node
        */
        JsonObject nodeObject = new JsonObject();
        nodeObject.addProperty("id",node.getId());
        nodeObject.addProperty("name", node.getName());

        nodeObject.addProperty("function", node.getOperation());
        nodeObject.addProperty("group", node.getGroup());
        /**
         * add extra information
         */
        nodeObject.addProperty("length", 1);
        nodeObject.addProperty("port_number", node.getPortList().size());

        /**
         * add ports
         */
        JsonArray ports = new JsonArray();
        for(NPort port:node.getPortList())
        {
            String strPort = gson.toJson(port,NPort.class);
            JsonObject objPort= gson.fromJson(strPort,JsonObject.class);
            ports.add(objPort);
        }
        nodeObject.add("ports",ports);

            return nodeObject;
    }

    public CSDFNode deserialize(JsonElement json, Type type,
                               JsonDeserializationContext context) throws JsonParseException {

        /**
         * register custom port and edge deserializers
          */
        GsonBuilder builder = new GsonBuilder()
         .registerTypeAdapter(NPort.class,new PortConverter())
         .registerTypeAdapter(CSDFEdge.class,new EdgeConverter())
         .setPrettyPrinting();
         Gson gson = builder.create();
        /**
        * deserialize node
        */
        JsonObject object = json.getAsJsonObject();
        int id = object.get("id").getAsInt();
        String name = object.get("name").getAsString();
        String op = object.get("function").getAsString();

        CSDFNode node = new CSDFNode(name,id);
        node.setOperation(op);

//        String group =  object.get("group").getAsString();
  //      node.setGroup(group);

        /**
         * Deserialize wcet, represented as Vector<Integer>,
         * which transformates sdf graph to its more general csdf graph analogue
         * TODO WCETS THROUGH THE REFINER
         *
         */
    //    JsonArray wcet_seq = object.get("wcet").getAsJsonArray();
    //    node.setWcet(CSDFSupportResolver.deserializeAsArray(wcet_seq,gson));

        /**
         * Deserialize ports
         */
        Vector<NPort> nodePortList = new Vector<NPort>();
        JsonArray ports =  object.get("ports").getAsJsonArray();
        for(JsonElement port: ports) {
            CSDFPort deserializedPort =(CSDFPort) gson.fromJson(port,NPort.class);
            /**resolve reference from port to node*/
            deserializedPort.setNode(node);
            nodePortList.add(deserializedPort);
        }
        node.setPortList(nodePortList);

        return node;
    }
}
