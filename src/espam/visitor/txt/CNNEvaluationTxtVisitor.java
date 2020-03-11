package espam.visitor.txt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.utils.fileworker.FileWorker;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Create evaluation graph of CNN
 */
public class CNNEvaluationTxtVisitor extends CNNGraphVisitor {

    /**
     * Call DNN evaluation json visitor
     * @param dnn dnn to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir) {
        try {
            PrintStream printStream = FileWorker.openFile(dir, dnn.getName() + "_eval", "txt");
            CNNEvaluationTxtVisitor visitor = new CNNEvaluationTxtVisitor(printStream);
            dnn.accept(visitor);
            System.out.println("File generated: " + dir + "/" + dnn.getName() + "_eval.txt");
            printStream.close();
        } catch (Exception e) {
            System.err.println("TXT DNN evaluation visitor fault. " + e.getMessage());
        }
    }

    /**
     * Constructor for the CSDFGraphShortJSONVisitor object
     * @param  printStream work I/O stream of the visitor
     */
    public CNNEvaluationTxtVisitor(PrintStream printStream) {
        _printStream = printStream;
    }

    /**
     * Visit DNN
     * @param  x A Visitor Object.
     */
    public void visitComponent(Network x) {
      _printTimes(x);
      _printMemory(x);
      _printEnery(x);
    }

    /** print time per layer and connection*/
    private void _printTimes(Network dnn) {
        _printStream.println(_prefix + "TIME EVALUATION (ms). Total time = " + dnn.get_timeEval() + "ms");
        _printStream.println(_prefix + "layers: ");
        _prefixInc();
        /** add layers*/
        for (Layer layer : dnn.getLayers()){
            _printStream.println(layer.getName()+ ": " + layer.get_timeEval() + " ms");
        }
        _prefixDec();

        /**_printStream.println(_prefix + "connections: ");
        _prefixInc();
        for(Connection con: dnn.getConnections()) {
            _printStream.println(con.getSrcName()+ "_to_" + con.getDestName() + ": " + con.get_timeEval());
        }
        _prefixDec();*/
        _printStream.println();
    }


    /** print time per layer and connection*/
    private void _printMemory(Network dnn) {
        _printStream.println(_prefix + "MEMORY EVALUATION(MB).Total memory = " + dnn.get_memEval() + " MB");
        _printStream.println(_prefix + "layers: ");
        _prefixInc();
        /** add layers*/
        for (Layer layer : dnn.getLayers()){
            _printStream.println(layer.getName()+ ": " + layer.get_memEval());
        }
        _prefixDec();

        _printStream.println(_prefix + "connections: ");
        _prefixInc();
        for(Connection con: dnn.getConnections()) {
            _printStream.println(con.getSrcName()+ "_to_" + con.getDestName() + ": " + con.get_memEval());
        }
        _prefixDec();
        _printStream.println();
    }

    /** print time per layer and connection*/
    private void _printEnery(Network dnn) {
        _printStream.println(_prefix + "ENERGY EVALUATION(Joules). Total energy = " + dnn.get_energyEval() + " J");
        _printStream.println(_prefix + "layers: ");
        _prefixInc();
        /** add layers*/
        for (Layer layer : dnn.getLayers()){
            _printStream.println(layer.getName()+ ": " + layer.get_energyEval());
        }
        _prefixDec();

        /**_printStream.println(_prefix + "connections: ");
        _prefixInc();
        for(Connection con: dnn.getConnections()) {
            _printStream.println(con.getSrcName()+ "_to_" + con.getDestName() + ": " + con.get_memEval());
        }
        _prefixDec();*/
        _printStream.println();
    }



}

