package espam.parser.json.cnn;


import com.google.gson.*;
import espam.datamodel.graph.cnn.connections.Custom;

import java.lang.reflect.Type;

public class CustomConnectionConverter implements JsonSerializer<Custom>, JsonDeserializer<Custom> {

       public JsonElement serialize(Custom customConnection, Type type,
                                 JsonSerializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        /**
        * call standard serializer
        */
        String result = gson.toJson(customConnection,Custom.class);
        JsonObject customConnectionObject = gson.fromJson(result,JsonObject.class);
        /**
        * replace standard custom matrix serialization with the "pretty" one
        */
        customConnectionObject.remove("_matrix");
        customConnectionObject.addProperty("matrix",getMatrixDescription(customConnection.getMatrix()));
        return customConnectionObject;
    }

    public Custom deserialize(JsonElement json, Type type,
                          JsonDeserializationContext context) throws JsonParseException {
           try {
               JsonObject object = json.getAsJsonObject();
               String matrixDescription = object.get("matrix").getAsString();
               boolean[][] matrix = parseMatrixDescription(matrixDescription);
               int srcId = object.get("srcId").getAsInt();
               int destId = object.get("destId").getAsInt();
               String srcName = object.get("src").getAsString();
               String destName = object.get("dest").getAsString();
               Custom custom = new Custom(srcId, destId,srcName,destName,matrix);
               return custom;
           }
           catch (Exception e) {
               throw new JsonParseException("custom connections matrix deserialization error. "+e.getMessage());

           }
    }

    public static String getMatrixDescription(boolean[][] matrix) {
        StringBuilder matrixDescription = new StringBuilder("");
        for(int j=0;j<matrix.length;j++)
       {    String row = "";
           for(int i=0;i<matrix[j].length;i++)
           {
               if(matrix[j][i])
                   row+="1";
               else
                   row+="0";
           }
           matrixDescription.append(row+"\n");
       }
        return matrixDescription.toString();
    }

    /**
     * Parse custom connection description
     * @param matrixDescription string matrix description
     * @return custom connection boolean matrix
     * @throws JsonParseException if description could not be parsed
     */
     public boolean[][] parseMatrixDescription(String matrixDescription) throws JsonParseException {
         try {
             matrixDescription = matrixDescription.trim();
             String[] cols = matrixDescription.split("\n");
             int h = cols.length;
             int w = cols[0].toCharArray().length;
              boolean[][] matrix = new boolean[h][w];

             for(int j=0; j<h;j++) {
                 String col = cols[j].trim();
                 char[] row = col.toCharArray();
                 for(int i=0;i<w;i++) {
                      if(row[i]=='1')
                          matrix[j][i]=true;
                        if(row[i]=='0')
                          matrix[j][i]=false;
                 }
             }
             return matrix;
         }
         catch (Exception e) {
             throw  new JsonParseException("Matrix parsing exception. "+e.getMessage());
         }
    }

}
