package espam.parser.json.cnn;

import com.google.gson.*;
import espam.datamodel.graph.cnn.connections.*;

import java.lang.reflect.Type;

public class ConnectionConverter implements JsonDeserializer<Connection> {

public Connection deserialize(JsonElement json, Type type,
                              JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

         GsonBuilder builder = new GsonBuilder()
         .registerTypeAdapter(Custom.class,new CustomConnectionConverter());
         Gson gson = builder.create();

            ConnectionType cType = gson.fromJson( object.get("type"), ConnectionType.class);
            switch (cType){
                case ONETOONE: return gson.fromJson(json,OneToOne.class);
                case ALLTOALL: return gson.fromJson(json, AllToAll.class);
                case ALLTOONE: return gson.fromJson(json,AllToOne.class);
                case ONETOALL: return gson.fromJson(json,OneToAll.class);
                case CUSTOM: return gson.fromJson(json, Custom.class);
                default: throw new JsonParseException("connection parsing error: unknown type of connection!");
            }
    }
}
