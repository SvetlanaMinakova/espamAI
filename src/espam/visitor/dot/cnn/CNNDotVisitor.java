package espam.visitor.dot.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;

public class CNNDotVisitor extends CNNGraphVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ///
    protected CNNDotVisitor(){ }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call DNN dot visitor
     * @param dnn dnn to be visited
     * @param dir directory for .dot and .png files, corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir){
        try {
            PrintStream printStream = FileWorker.openFile(dir, dnn.getName(), "dot");
            _cnnDotVisitor._printStream = printStream;
            _cnnDotVisitor.visitComponent(dnn);
            System.out.println("DOT file generated: " + dir + dnn.getName());
        }
        catch (Exception e){
            System.err.println("Dot file generation error " + e.getMessage());
        }
     }

      /**
     * Call DNN dot visitor
     * @param printStream dot print stream
     */
    public static void callVisitor(Network dnn, PrintStream printStream){
        try {
            _cnnDotVisitor._printStream = printStream;
            _cnnDotVisitor.visitComponent(dnn);
        }
        catch (Exception e){
            System.err.println("Dot file generation error" + e.getMessage());
        }
     }


    /**
     *  Constructor for the CNNDotVisitor object
     * @param  printStream Description of the graph
     */
     public CNNDotVisitor(PrintStream printStream) { _printStream = printStream; }

    /**
     * CNN Network
     * Print a line for the CNN Layer in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */
    public void visitComponent(Network x) {
         _prefixInc();

        _printStream.println("digraph " + x.getName() + " {");
        _printStream.println("");
        _printStream.println(_prefix + "ratio = auto;");
        _printStream.println(_prefix + "rankdir = LR;");
        _printStream.println(_prefix + "ranksep = 0.3;");
        _printStream.println(_prefix + "nodesep = 0.2;");
        _printStream.println(_prefix + "center = true;");
        _printStream.println("");
        _printStream.println(_prefix + "node [ fontsize=12, height=0.4, width=0.5, style=filled, color=\"0.650 0.200 1.000\"]");
        _printStream.println(_prefix + "edge [ fontsize=10, arrowhead=none, style=bold]");
        _printStream.println("");
        Iterator i;

      // Visit all Layers
        Layer layer;
        i = x.getLayers().iterator();
        while (i.hasNext()) {
            layer = (Layer) i.next();
            layer.accept(this);
        }
        _printStream.println("");

        // Visit all connections
        Connection connection;
        i = x.getConnections().iterator();
        while (i.hasNext()) {
            connection = (Connection) i.next();
            connection.accept(this);
        }
        _prefixDec();
        _printStream.println("");
        _printStream.println("}");
    }

     /**
     * CNN Layer
     * Print a line for the CNN Layer in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */


    /**
     *        StringBuilder strNeuron = new StringBuilder();
       strNeuron.append(getName());
       strNeuron.append("\n type: "+getNeuronType().toString());
       strNeuron.append(getStrParameters());
       return strNeuron.toString();
     *
     *
     * @param  x A Visitor Object.
     */

    public void visitComponent(Layer x) {
        String strParams = x.getNeuron().getStrParameters();
        strParams = strParams.replaceAll("\n"," ");

       _printStream.println(_prefix +
               "\"" + x.getName() + x.getId() +"\" [ label=\"" +
               x.getName() + " \\n neurons: " + x.getNeuronsNum() +
               " \\n neuron: " + x.getNeuron().getName()  + "(" +x.getNeuron().getNeuronType() +  ") \\n" +
               strParams + "\\n"+
               " \", shape=box, color=" + getColor(x.getNeuron().getNeuronType()) + ", margin=0.4];");
    }

     /**
     * CNN Neuron
     * Print a line for the CNN Neuron in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */
    public void visitComponent(Neuron x) {
        _printStream.print(_prefix + " \"" +x.getName() + "\" [ label=\"" + x.getName()+"\"]; ");
    }

     /**
     * CNN Connection
     * Print a line for the SDFG Nodes in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */
    public void visitComponent(Connection x) {
        String color = "dimgray";

        _printStream.print( _prefix + "\"" + x.getSrc().getName() +x.getSrc().getId() + "\" -> " +
                "\"" + x.getDest().getName() + x.getDest().getId() + "\" [" );
        _printStream.println( " label=\"" + x.toString() + "\"" + ", color=" + color + "];");
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                  ///

    protected String getColor(NeuronType neuronType)
    {
        switch (neuronType) {
            case DATA: return "lightskyblue";
            case CONCAT: return "gainsboro";
            case ADD: return "gainsboro";
            case NONLINEAR: return "powderblue";
            case CONV: return "lightcoral";
            case POOL: return "orange";
            case DENSEBLOCK: return "palegreen";
            default: return "\"0.650 0.200 1.000\"";
        }
    }

    private static CNNDotVisitor _cnnDotVisitor = new CNNDotVisitor();

}
