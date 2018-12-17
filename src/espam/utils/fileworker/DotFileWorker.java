package espam.utils.fileworker;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.visitor.dot.cnn.CNNDotVisitor;
import espam.visitor.dot.sdfg.SDFGDotVisitor;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.io.File;
import java.io.PrintStream;

/**
 * Class provides read and write operations for work with models represented as JSON-files
 */
public class DotFileWorker extends FileWorker {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Render DNN model
     * @param dnn cnn model
     * @param directory root directory of CNN model
     */
    public static void render(Network dnn, String directory) {
        render(dnn,directory,1000);
    }

      /**
     * Render CNN model
     * @param dnn cnn model
     * @param directory root directory of CNN model
     */
    public static void render(Network dnn, String directory, int imgW) {
        try{
            /** create temp file and save model in .dot format */
            File tempFile = File.createTempFile(dnn.getName(), ".dot");
            PrintStream printStream = new PrintStream(tempFile);
            CNNDotVisitor.callVisitor(dnn,printStream);
            String dstPath = createPath(directory,dnn.getName(),"png");
            convertToPNG(tempFile,dstPath, imgW);
            tempFile.delete();
            }
            catch(Exception e) {
            System.err.println("DNN rendering error. "+e.getMessage());
            }
    }

    /**
     * Render CSDFGraph
     * @param graph SDFG model
     * @param directory root directory of SDF graph
     */
    public static void render(CSDFGraph graph, String directory){
        render(graph,directory,10000);
    }

     /**
     * Render CSDFGraph
     * @param graph SDFG model
     * @param directory root directory of SDF graph
     */
    public static void render(CSDFGraph graph, String directory, int imgW){
        try{
            /** create temp file and save model in .dot format */
            File tempFile = File.createTempFile(graph.getName(), ".dot");
            PrintStream printStream = new PrintStream(tempFile);
            SDFGDotVisitor.callVisitor(graph,printStream);
            String dstPath = createPath(directory,graph.getName(),"png");
            convertToPNG(tempFile,dstPath,imgW);
            tempFile.delete();
        }
        catch (Exception e) {
            System.err.println("sdfg rendering fault. "+e.getMessage());
        }
    }

    /**
     * Creates .png representation of graph from its .dot representation
     * with specified image size parameters
     * @param imgW image width in pixels
     */
     private static void convertToPNG(File srcFile, String dstPath,int imgW) {
         try {
        MutableGraph g = Parser.read(srcFile);
        Graphviz.fromGraph(g).width(imgW).render(Format.PNG).toFile(new File(dstPath));
    }
        catch (Exception e) {
        System.err.println(e.getMessage());
    }
  }
}
