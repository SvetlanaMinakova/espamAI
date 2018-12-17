package espam.parser.json.cnn;
import com.google.gson.*;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.connections.Custom;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;

import java.lang.reflect.Type;

public class GenericNeuronConverter implements JsonSerializer<GenericNeuron>, JsonDeserializer<GenericNeuron> {

    public JsonElement serialize(GenericNeuron neuron, Type type,
                                 JsonSerializationContext context) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder()
        .registerTypeAdapter(Neuron.class, new NeuronConverter())
        .registerTypeAdapter(Connection.class,new ConnectionConverter())
        .registerTypeAdapter(Custom.class,new CustomConnectionConverter())
        .setPrettyPrinting();
        Gson gson = builder.create();
        /**
        * call standard serializer
        */
        String result = gson.toJson(neuron,GenericNeuron.class);
        JsonObject neuronObject = gson.fromJson(result,JsonObject.class);
        /**
         * add internal structure description
         */
        Network intStructure = neuron.getInternalStructure();
        if(intStructure!=null) {
            String internalStructure = gson.toJson(neuron.getInternalStructure(), Network.class);
            JsonObject internalStructObj = gson.fromJson(internalStructure, JsonObject.class);
            neuronObject.add("internalStructure", internalStructObj);
        }
        else
              neuronObject.add("internalStructure", JsonNull.INSTANCE);
        return neuronObject;
    }

    public GenericNeuron deserialize(JsonElement json, Type type,
                               JsonDeserializationContext context) throws JsonParseException {
         GsonBuilder builder = new GsonBuilder()
        .registerTypeAdapter(Neuron.class, new NeuronConverter())
        .registerTypeAdapter(Connection.class,new ConnectionConverter())
        .registerTypeAdapter(Custom.class,new CustomConnectionConverter())
        .setPrettyPrinting();
        Gson gson = builder.create();
        JsonObject object = json.getAsJsonObject();
        /**
        * call standard deserializer
        */
        GenericNeuron neuron = gson.fromJson(object,GenericNeuron.class);

        return neuron;
    }
}

