package espam.utils.fileworker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class ONNXFileWorker {

     /**
     * read ONNX model from source file through the bytes buffer
     * @param srcFile path to source file
     * @return ONNX model if it was successfully read and null otherwise
     */
    public static onnx.ONNX.ModelProto readModelThroughBuff (String srcFile) {
       try {
           //File inFile = new File(srcFile);

           System.out.println("Model reading start...");
           long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            byte[] array = Files.readAllBytes(Paths.get(srcFile));
            elapsedTime = (new Date()).getTime() - startTime;
            System.out.println("Model byte array red in : " +elapsedTime);

            startTime = System.currentTimeMillis();
           onnx.ONNX.ModelProto model = onnx.ONNX.ModelProto.parseFrom(array);
           elapsedTime = (new Date()).getTime() - startTime;

           System.out.print("Model byte array parsed in: "+elapsedTime);
           return model;
          }
          catch (Exception e) {
              System.err.println("Model reading exception. " + e.getMessage());
              return null;
          }

  }


    /**
     * read ONNX model from source file
     * @param srcFile path to source file
     * @return ONNX model if it was successfully read and null otherwise
     */
    public static onnx.ONNX.ModelProto readModel (String srcFile) {
       try {
              File inFile = new File(srcFile);
              FileInputStream inStream = new FileInputStream(inFile);

              onnx.ONNX.ModelProto model = onnx.ONNX.ModelProto.parseFrom(inStream);
              inStream.close();
              return model;
          }
          catch (Exception e) {
              System.err.println("Model reading exception. " + e.getMessage());
              return null;
          }

  }

      /**
     * read ONNX graph from source file
     * @param srcFile path to source file
     * @return ONNX model if it was successfully read and null otherwise
     */
    public static onnx.ONNX.GraphProto readGraph (String srcFile) {
       try {
              File inFile = new File(srcFile);
              FileInputStream inStream = new FileInputStream(inFile);

              onnx.ONNX.GraphProto graph = onnx.ONNX.GraphProto.parseFrom(inStream);
              inStream.close();
              return graph;
          }
          catch (Exception e) {
              System.err.println("Graph reading exception. " + e.getMessage());
              return null;
          }

  }

  /**
  * write ONNX model to destination file
  * @param dstFile path to destination file
  * @param model ONNX model to be written
  */
  public static void writeModel(onnx.ONNX.ModelProto model, String dstFile) {
    try
        {
        File outFile = new File(dstFile);
        FileOutputStream outStream = new FileOutputStream(outFile);

        model.writeTo(outStream);
        outStream.close();
        }
    catch (Exception e) {
        System.err.println("Model writing error. "+ e.getMessage());
    }

  }

  private static void readWithBufferedStream(String filepath) {
        long start= System.currentTimeMillis();
        try (FileInputStream myFile = new FileInputStream(filepath)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(myFile);

               try {
          //  ONNX.GraphProto graph = ONNX.GraphProto.parseFrom(buffer);
            onnx.ONNX.ModelProto modelProto = onnx.ONNX.ModelProto.parseFrom(bufferedInputStream);
            onnx.ONNX.GraphProto graph = modelProto.getGraph();
            System.out.println("Parsing finished");

            for(onnx.ONNX.NodeProto nodeProto: graph.getNodeList())
                System.out.println("node: "+nodeProto.getName());
        }

        catch (Exception exc){
            System.err.println("Graph parsing exception: "+ exc.getMessage());
        }

            boolean eof = false;
            while (!eof) {
                int inByteValue = bufferedInputStream.read();
                if (inByteValue == -1) eof = true;
            }


        } catch (IOException e) {
            System.out.println("Could not read the stream...");
            e.printStackTrace();
        }

        System.out.println("time passed with buffered:" + (System.currentTimeMillis()-start));

    }

}
