package espam.visitor.pthread.weights;

import espam.utils.fileworker.FileWorker;
import java.io.PrintStream;
import java.util.Vector;

public class WeightsLoader {


    /**
     * Read weights from the source files directory and print them to destination file
     * @param srcDir source files directory
     * @param nodeName node name
     * @param dstWriteStream stream, writing to destination file
     * @throws Exception if an error occurs */

    public static void insertWeightsFromFolder(String srcDir, String nodeName, PrintStream dstWriteStream, int arrSize) throws Exception
    {
         String nodeWeightsName = nodeName +"_w";
         Vector<String> weightPaths = FileWorker.getAllFilePaths(srcDir,"txt",nodeWeightsName);
         weightPaths.sort(String::compareToIgnoreCase);

         int commaCntr = 0;
         //int commaBorder = weightPaths.size()-1;
         for (String weightsPath : weightPaths) {
           dstWriteStream.print("float " + nodeName + "_w_p" + commaCntr + "["+ arrSize + "] = { " );

           FileWorker.insert(weightsPath, dstWriteStream);
           dstWriteStream.println("};");
           commaCntr++;

           //if (commaCntr < commaBorder)
             //        dstWriteStream.print(",");
         }

        System.out.println(nodeName + "weights loaded ");
    }

    /**
     * Read weights from the source files directory and print them to destination file
     * @param srcDir source files directory
     * @param nodeName node name
     * @param dstWriteStream stream, writing to destination file
     * @throws Exception if an error occurs*/

    public static void insertWeightsFromFolder(String srcDir, String nodeName, PrintStream dstWriteStream) throws Exception
    {
         String nodeWeightsName = nodeName +"_w";
         Vector<String> weightPaths = FileWorker.getAllFilePaths(srcDir,"txt",nodeWeightsName);
         weightPaths.sort(String::compareToIgnoreCase);

         int commaCntr = 0;
         int commaBorder = weightPaths.size()-1;
         for (String weightsPath : weightPaths) {
             FileWorker.insert(weightsPath, dstWriteStream);
             if (commaCntr < commaBorder)
                     dstWriteStream.print(",");
             commaCntr++;
         }

        System.out.println(nodeName + "weights loaded ");
    }

    /**
     * Read weights from the source files directory and print them to destination file
     * @param srcDir source files directory
     * @param nodeName node name
     * @param dstWriteStream stream, writing to destination file
     * @throws Exception if an error occurs

    public static void insertWeightsFromFolder(String srcDir, String nodeName, PrintStream dstWriteStream) throws Exception
    {
         String nodeWeightsName = nodeName +"_w";
         Vector<String> weightPaths = FileWorker.getAllFilePaths(srcDir,"txt",nodeWeightsName);
         weightPaths.sort(String::compareToIgnoreCase);

         int commaCntr = 0;
         int commaBorder = weightPaths.size()-1;
         for (String weightsPath : weightPaths) {
             FileWorker.insert(weightsPath, dstWriteStream);
             if (commaCntr < commaBorder)
                     dstWriteStream.print(",");
             commaCntr++;
         }

        System.out.println(nodeName + "weights loaded ");
    }*/

    /**
     * Read biases from the source files directory and print them to destination file
     * @param srcDir source files directory
     * @param nodeName node name
     * @param dstWriteStream stream, writing to destination file
     * @throws Exception if an error occurs
     */
      public static void insertBiasFromFolder(String srcDir, String nodeName, PrintStream dstWriteStream) throws Exception
        {
            String biasPath = srcDir + "/" + nodeName +"_b.txt";
            FileWorker.insert(biasPath, dstWriteStream);
        }

    public static void insertWeightsFromFolder(String srcDir, String nodeName, int neurons, String dstFile) throws Exception
    {
         String nodeWeightsName = nodeName +"_w";
         Vector<String> weightPaths = new Vector<>();
         for(int i=0; i<neurons; i++)
             weightPaths.add(srcDir + "/" + nodeWeightsName + i +".txt");

             PrintStream dst = FileWorker.openFile(dstFile);
             int commaCntr = 0;
             int commaBorder = neurons-1;
             for (String weightsPath : weightPaths) {
                 FileWorker.insert(weightsPath, dst);
                 if (commaCntr < commaBorder)
                     dst.print(",");
                 commaCntr++;
             }
    }
}
