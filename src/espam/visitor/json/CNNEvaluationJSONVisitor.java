package espam.visitor.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Create evaluation graph of CNN
 */
public class CNNEvaluationJSONVisitor extends CNNGraphVisitor {

    /**
     * Call DNN evaluation json visitor
     * @param dnn dnn to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir) {
        try {
            PrintStream printStream = JSONFileWorker.openFile(dir, dnn.getName() + "_eval", "json");
            CNNEvaluationJSONVisitor visitor = new CNNEvaluationJSONVisitor(printStream);
            dnn.accept(visitor);
            System.out.println("JSON file generated: " + dir + "/" + dnn.getName() + "_eval.json");
            printStream.close();
        } catch (Exception e) {
            System.err.println("JSON DNN evaluation visitor fault. " + e.getMessage());
        }
    }

        /**
         * Constructor for the CSDFGraphShortJSONVisitor object
         * @param  printStream work I/O stream of the visitor
         */
    public CNNEvaluationJSONVisitor(PrintStream printStream) {
            _printStream = printStream;
            /**create standard parser*/
            GsonBuilder builder = new GsonBuilder()
                    .setPrettyPrinting();
            _gson = builder.create();
    }

    /**
     * Visit DNN
     * @param  x A Visitor Object.
     */
    public void visitComponent(Network x) {
        /**Open network description*/
        _printStream.println("{");
        _prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getName() + "\",");
        _printStream.println(_prefix + "\"layers_num\": " + x.countLayers() + ",");
        _printStream.println(_prefix + "\"connections_num\": " + x.countConnections() + ",");
        _printStream.println(_prefix + "\"time\": " + x.get_timeEval() + ",");
        _printStream.println(_prefix + "\"memory\": " + x.get_memEval() + ",");
        _printStream.println(_prefix + "\"energy\": " + x.get_energyEval() + ",");

        _prefixInc();

        /** add layers*/

        Iterator i;
        /**Visit all layers*/
        Vector<Layer> layers = x.getLayers();
        Layer layer;
        int commaBorder = layers.size()-1;
        _printStream.println(_prefix + "\"layers\": [");
        prefixInc();

        i = layers.iterator();
        while (i.hasNext()) {
            layer = (Layer) i.next();
            layer.accept(this);
            if(commaBorder>0) {
                _printStream.println(",");
                commaBorder--;
            }
        }

        _prefixDec();
        /**Close layers description*/
        _printStream.println();
        _printStream.println(_prefix + "],");

        /**Visit all connections*/
        Vector<Connection> connections = x.getConnections();
        Connection con;
        commaBorder = connections.size()-1;
        _printStream.println(_prefix + "\"connections\": [");

        prefixInc();
        i = x.getConnections().iterator();
        while (i.hasNext()) {
            con = (Connection) i.next();
            con.accept(this);
            if(commaBorder>0) {
                _printStream.println(",");
                commaBorder--;
            }
        }
        _prefixDec();
        /**Close connections description*/
        _printStream.println();
        _printStream.println(_prefix + "]");

        /**Close network description*/
        _printStream.println("}");
    }

    /** visit CNN layer*/
    @Override
    public void visitComponent(Layer x) {
        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getName() + "\",");
        _printStream.println(_prefix + "\"time\": " + x.get_timeEval() + ",");
        _printStream.println(_prefix + "\"memory\": " + x.get_memEval() + ",");
        _printStream.println(_prefix + "\"energy\": " + x.get_energyEval());
        _prefixDec();
        _printStream.print(_prefix + "}");
    }

    /**visit CNN connection*/
    @Override
    public void visitComponent(Connection x) {
        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getSrcName() + "_to_" + x.getDestName() + "\",");
        _printStream.println(_prefix + "\"time\": " + x.get_timeEval() + ",");
        _printStream.println(_prefix + "\"memory\": " + x.get_memEval() + ",");
        _printStream.println(_prefix + "\"energy\": " + x.get_energyEval());
        _prefixDec();
        _printStream.print(_prefix + "}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///
    /**Standard JSON-parser, implements parsing of non-nested types*/
    private static Gson _gson;

}
