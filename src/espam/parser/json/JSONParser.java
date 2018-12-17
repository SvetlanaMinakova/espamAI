package espam.parser.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.Node;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.connections.Custom;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.csdf.*;
import espam.parser.json.cnn.*;
import espam.parser.json.csdf.EdgeConverter;
import espam.parser.json.csdf.GraphConverter;
import espam.parser.json.csdf.NodeConverter;
import espam.parser.json.csdf.PortConverter;

import java.lang.reflect.Type;

public class JSONParser {

    /** get singleton JSON-parser instance*/
    public static JSONParser getInstance(){
        return _parser;
    }

    /**
     * Get standard parser implementation
     * @return standard parser implementation
     */
    public Gson getGson(){
        return _gson;
    }

    private JSONParser()  {
        /** create custom adaptor for any type of port*/
        PortConverter SDFPortConverter = new PortConverter();

        /**create parser*/
        GsonBuilder builder = new GsonBuilder()
        /**
         * register all custom adaptors
         */
        /**CNNModel adaptors*/
        .registerTypeAdapter(Neuron.class, new NeuronConverter())
        .registerTypeAdapter(Connection.class,new ConnectionConverter())
        .registerTypeAdapter(Custom.class,new CustomConnectionConverter())
        .registerTypeAdapter(GenericNeuron.class,new GenericNeuronConverter())

        /**SDFModel adaptors*/
        .registerTypeAdapter(NPort.class, SDFPortConverter)
        .registerTypeAdapter(CSDFPort.class, SDFPortConverter)
        .registerTypeAdapter(Node.class,new NodeConverter())
        .registerTypeAdapter(CSDFNode.class,new NodeConverter())
        .registerTypeAdapter(CSDFEdge.class,new EdgeConverter())
        .registerTypeAdapter(CSDFGraph.class,new GraphConverter())

        /** Set printing format*/
        .setPrettyPrinting();
        _gson = builder.create();
    }

    public String toJson(Object obj){
        return _gson.toJson(obj,obj.getClass());
    }

    /**
     * Get JSON parser
     * @return
     */
    public Gson getParser() {
        return _gson;
    }

    /**
     * Deserializes objects from json. if necessary, restores reference dependencies.
     * @param json json object representation
     * @param objType type of Java object
     * @return deserialized java object with restored references
     */
    public Object fromJson(String json,Type objType) {
            Object obj = _gson.fromJson(json, objType);
            if (obj instanceof ReferenceResolvable)
                ((ReferenceResolvable) obj).resolveReferences();
            return obj;
    }

    /**
     * JSON-parser
     */
    private Gson _gson;
    /** singleton parser instance*/
    private static JSONParser _parser = new JSONParser();
}
