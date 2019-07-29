package espam.parser.json.cnn;

import com.google.gson.*;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.simple.*;
import espam.datamodel.graph.cnn.neurons.arithmetic.Add;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.cnn.neurons.transformation.Upsample;

import java.lang.reflect.Type;

public class NeuronConverter implements JsonDeserializer<Neuron> {

public Neuron deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

          GsonBuilder builder = new GsonBuilder()
         .registerTypeAdapter(GenericNeuron.class,new GenericNeuronConverter());
          Gson gson = builder.create();

            NeuronType nType = gson.fromJson( object.get("type"), NeuronType.class);
           /**Available types:
            * NONE,CONV, POOL, NONLINEAR, DATA, GENERIC,ADD,MATMUL,CONCAT,DENSEBLOCK,ARITHMETIC_OP
            * */
            switch (nType){
                case CONV: return gson.fromJson(json,Convolution.class);
                case POOL: return gson.fromJson(json, Pooling.class);
                case NONLINEAR: return gson.fromJson(json, NonLinear.class);
                case DATA: return gson.fromJson(json,Data.class);
                case GENERIC: return gson.fromJson(json,GenericNeuron.class);
                case CONCAT: return gson.fromJson(json,Concat.class);
                case DENSEBLOCK: return gson.fromJson(json,DenseBlock.class);
                case ADD: return gson.fromJson(json,Add.class);
                case LRN: return gson.fromJson(json,LRN.class);
                case RESHAPE: return gson.fromJson(json, Reshape.class);
                case UPSAMPLE: return gson.fromJson(json, Upsample.class);
                default: throw new JsonParseException("neuron parsing error: unknown type of neuron!");
            }
    }
}