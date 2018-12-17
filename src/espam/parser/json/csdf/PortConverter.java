package espam.parser.json.csdf;
import com.google.gson.*;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.CSDFPortType;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;

import java.lang.reflect.Type;
import java.util.Vector;


public class PortConverter  implements JsonSerializer<NPort>, JsonDeserializer<NPort> {

    public JsonElement serialize(NPort port, Type type,
                                 JsonSerializationContext context) throws JsonParseException{
        Gson gson = new Gson();
        JsonObject portObject = new JsonObject();

             portObject.addProperty("id", ((CSDFPort) port).getId());
                portObject.addProperty("name", port.getName());

                /** rate is represented as Vector<Integer>,which transformates sdf graph to its more general
                 * csdf graph analogue*/
                portObject.add("rate",CSDFSupportResolver.serializeIndexPairsAsArray(((CSDFPort) port).getRates()));

            if (((CSDFPort) port).getType().equals(CSDFPortType.in)) {
                portObject.addProperty("type", "in");
                return portObject;
            }

            if (((CSDFPort) port).getType().equals(CSDFPortType.out)) {
                portObject.addProperty("type", "out");
                return portObject;
            }
              throw new JsonParseException("Unknown port type");
    }

    public CSDFPort deserialize(JsonElement json, Type type,
                               JsonDeserializationContext context) throws JsonParseException {

        try {
            Gson gson = new Gson();
            JsonObject object = json.getAsJsonObject();
            int portId = object.get("id").getAsInt();

            /** rate in serialized port objects is represented as Vector<Integer>,
            * which transformates sdf graph to its more general csdf graph analogue*/
            JsonArray rate_seq = object.get("rate").getAsJsonArray();
            Vector<Integer> portRates = CSDFSupportResolver.deserializeAsArray(rate_seq,gson);
            Vector<IndexPair> portRatesDesc = CSDFSupportResolver.vecToIndexPairs(portRates);

            /**deserialize the rest of the fields*/
            String portName = object.get("name").getAsString();
            String strPortType = object.get("type").getAsString();
            CSDFPortType portType =  CSDFPortType.fromString(strPortType);
            CSDFPort port = new CSDFPort(portName,portId,portType);
            port.setRates(portRatesDesc);
            return port;
            }
            catch(Exception e){
                throw new JsonParseException("Port parsing exception: " + e.getMessage());
        }
    }
}
