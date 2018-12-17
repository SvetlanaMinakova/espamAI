package espam.parser.json.csdf;

import com.google.gson.*;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;

import java.util.Vector;

/**
 * Class resolves some parsing detailes while processing sdf graph as its more general csdf analogue
 */
public class CSDFSupportResolver {


    /**
     * Serialize rate description for the port.
     * Rate is always represented as Vector<rates>, which corresponds to more general
     * rates description. For sdf ports a special transformation is used to
     * provide Vector<rate> description for a single port rate
     * @param port port to be processed
     * @param portObject .json object, corresponding the port
     */
    public static void serializeRateDescription(CSDFPort port, JsonObject portObject){
        Vector<Integer> fullRatesDesc = indexPairsToVec(port.getRates());
        portObject.add("rate",CSDFSupportResolver.serializeAsArray(fullRatesDesc));
    }

    /**
     * Deserialize rate description for the port.
     * Serialized rate is always provided as Vector<rates>, which corresponds to
     * more general rates description. For sdf ports a special transformation is used to
     * extract single rate description from a Vector<rate> description
     * @param port port to be processed
     * @param portObject .json object, corresponding the port
     */
    public static void deserializeRateDescription(CSDFPort port, JsonObject portObject, Gson gson){
        /** resolve for csdf port*/
        JsonArray rate_seq = portObject.get("rate").getAsJsonArray();
        Vector<Integer> rates = deserializeAsArray(rate_seq,gson);
        Vector<IndexPair> ratesDesc = vecToIndexPairs(rates);
        port.setRates(ratesDesc);
    }

    /**
     * Transformation trick, serializes single value as one-element array, contains this value
     * This kind of transformation is required while sdf model should be represented as
     * more general CSDF model
     * @param field field to be serialized as sequence
     */
    public static JsonArray serializeAsArray(Integer field) {
        JsonArray fieldArray = new JsonArray();
        fieldArray.add(field);
        return fieldArray;
    }

    /**
     * Transformation trick, serializes IndexPair description, contains compressed
     * <Field,Number of field repetitions> as linear Vector<Field>
     * @param fieldsDesc fields description to be serialized as linear sequence
     */
    public static String serializeIndexPairsAsLongString(Vector<IndexPair> fieldsDesc){
        StringBuilder result = new StringBuilder();
        Vector<Integer> fields = indexPairsToVec(fieldsDesc);
        int commaborder = fields.size() - 1;
        int commaSep =0;
        for(Integer field: fields) {
            result.append(field);
            if(commaSep<commaborder)
                result.append(",");
            commaSep++;
        }
        return result.toString();
    }

    /**
     * Transformation trick, serializes IndexPair description, contains compressed
     * <Field,Number of field repetitions> as linear Vector<Field>
     * @param fieldsDesc fields description to be serialized as linear sequence
     */
    public static JsonArray serializeIndexPairsAsArray(Vector<IndexPair> fieldsDesc) {
        JsonArray fieldArray = new JsonArray();
        Vector<Integer> fields = indexPairsToVec(fieldsDesc);
        for(Integer field: fields)
            fieldArray.add(field);
        return fieldArray;
    }

        /**
     * Transformation trick, serializes IndexPair description, contains compressed
     * <Field,Number of field repetitions> as linear Vector<Field>
     * @param fieldDesc field description to be serialized as linear sequence
     */
    public static JsonArray serializeIndexPairAsArray(IndexPair fieldDesc) {
        JsonArray fieldArray = new JsonArray();
        Integer val = fieldDesc.getFirst();
        for(int rep=0; rep<fieldDesc.getSecond(); rep++)
            fieldArray.add(val);
        return fieldArray;
    }
    
    /**
     * Transformation trick, serializes single value as one-element array, contains this value
     * This kind of transformation is required while sdf model should be represented as
     * more general CSDF model
     * @param fields fields to be serialized as sequence
     */
    public static JsonArray serializeAsArray(Vector<Integer> fields) {
        JsonArray fieldArray = new JsonArray();
        for(Integer field: fields)
            fieldArray.add(field);
        return fieldArray;
    }

     /**
     * Transformation trick, deserializes one-element array as single value
     * This kind of transformation is required while sdf model should be represented as
     * more general CSDF model
     * @param fieldArray one-element array to be deserialized
     */
    public static int deserializeAsInteger(JsonArray fieldArray, Gson gson) throws JsonParseException {
        if(fieldArray.size()!=1)
            throw new JsonParseException("array can not be deserialized as integer: invalid elements number");

        int signleElement = gson.fromJson(fieldArray.get(0),Integer.TYPE);
        return signleElement;
    }

     /**
     * Deserialize JsonArray as Vector<Integer>, if possible
     * @param fieldArray array to be deserialized
     * @param gson standard gson parser, processes simple .json types
     * @return  JsonArray as Vector<Integer>
     * @throws JsonParseException if deserialization error occur
     */
    public static Vector<Integer> deserializeAsArray(JsonArray fieldArray, Gson gson) throws JsonParseException {
        Vector<Integer> result = new Vector<>();

        for(int i=0; i<fieldArray.size(); i++){
            int element = gson.fromJson(fieldArray.get(i),Integer.TYPE);
            result.add(element);
        }

        return result;
    }
     /**
     * Transformation trick, serializes IndexPair as two-element array, contains IndexPair values
     * This kind of transformation is required while sdf model should be represented as
     * more general CSDF model
     * @param field field to be serialized as sequence
     */
    public static JsonArray serializeAsArray(IndexPair field) {
        JsonArray fieldArray = new JsonArray();
        fieldArray.add(field.getFirst());
        fieldArray.add(field.getSecond());
        return fieldArray;
    }

      /**
     * Transformation trick, deserializes two-element array as IndexPair
     * This kind of transformation is required while sdf model should be represented as
     * more general CSDF model
     * @param fieldArray one-element array to be deserialized
     */
    public static IndexPair deserializeAsIndexPair(JsonArray fieldArray, Gson gson) throws JsonParseException {
        if(fieldArray.size()!=2)
            throw new JsonParseException("array can not be deserialized as integer: invalid elements number");

        int firstElement = gson.fromJson(fieldArray.get(0),Integer.TYPE);
        int secondElement = gson.fromJson(fieldArray.get(1),Integer.TYPE);
        IndexPair deserialized = new IndexPair(firstElement,secondElement);

        return deserialized;
    }

    /**
     * Serialize Vector<IndexPair> port rates notation as Vector<Integer>,
     * where each IndexPair <Integer, Integer>  means <Rate,Number of rate repetitions>
     * @return Vector<Integer> port rates notation
     */
    public static Vector<Integer> indexPairsToVec(Vector<IndexPair> ratesDesc){
        Vector<Integer> rates = new Vector<>();
        int rate;
        for(IndexPair rateDesc: ratesDesc){
            rate = rateDesc.getFirst();
            for(int rep=0; rep<rateDesc.getSecond(); rep++)
                rates.add(rate);
        }
        return rates;
    }

    /**
     * Deserialize vector<Integer> ports description as Vector<IndexPair> port rates notation,
     * where each IndexPair <Integer, Integer>  means <Rate,Number of rate repetitions>
     * @return Vector<Integer> port rates notation
     */
    public static Vector<IndexPair> vecToIndexPairs(Vector<Integer> rates){
        Vector<IndexPair> ratesDesc = new Vector<>();
        if(rates.size()==0)
            return ratesDesc;

        int curRate,prevRate;
        prevRate = rates.elementAt(0);
        int distinctCounter = 0;

        int lastElemId = rates.size()-1;
        for(int i = 0; i<rates.size(); i++){
            curRate = rates.elementAt(i);
            if(curRate==prevRate)
                distinctCounter++;
            else
                {
                IndexPair rateDesc = new IndexPair(prevRate,distinctCounter);
                ratesDesc.add(rateDesc);
                prevRate = curRate;
                distinctCounter = 1;
            }
            if(i==lastElemId && distinctCounter>0){
                IndexPair rateDesc = new IndexPair(prevRate,distinctCounter);
                ratesDesc.add(rateDesc);
            }

        }
        return ratesDesc;
    }


}

