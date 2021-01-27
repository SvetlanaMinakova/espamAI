package espam.visitor.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.connections.Custom;
import espam.datamodel.graph.cnn.neurons.arithmetic.Arithmetic;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.normalization.ImageScaler;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.simple.*;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.cnn.neurons.transformation.Upsample;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.cnn.ConnectionConverter;
import espam.parser.json.cnn.GenericNeuronConverter;
import espam.parser.json.cnn.NeuronConverter;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.PrintStream;
import java.util.*;

public class CNNJSONVisitorShort extends CNNGraphVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call CNN json visitor
     * @param dnn dnn to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir){

        try {
            PrintStream printStream = FileWorker.openFile(dir, dnn.getName(), "json");
            CNNJSONVisitorShort cnnvisitor = new CNNJSONVisitorShort(printStream);
            dnn.accept(cnnvisitor);
            System.out.println("JSON file generated: " + dir + dnn.getName() + ".json");
        }
        catch(Exception e) {
            System.err.println("JSON dnn visitor call error. " + e.getMessage());
        }
    }

    /**
     * Call CNN json visitor
     * @param n neuron to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Neuron n, String dir){

        try {
            PrintStream printStream = FileWorker.openFile(dir, n.getName(), "json");
            CNNJSONVisitorShort cnnvisitor = new CNNJSONVisitorShort(printStream);
            n.accept(cnnvisitor);
            System.out.println("JSON file generated: " + dir + n.getName() + ".json");
        }
        catch(Exception e) {
            System.err.println("JSON dnn visitor call error. " + e.getMessage());
        }
    }

    /**
     * Call CNN json visitor
     * @param l layer to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Layer l, String dir){

        try {
            PrintStream printStream = FileWorker.openFile(dir, l.getName(), "json");
            CNNJSONVisitorShort cnnvisitor = new CNNJSONVisitorShort(printStream);
            l.accept(cnnvisitor);
            System.out.println("JSON file generated: " + dir + l.getName() + ".json");
        }
        catch(Exception e) {
            System.err.println("JSON dnn visitor call error. " + e.getMessage());
        }
    }

    /**
     * Constructor for the CNNJSONVisitorShort object
     * @param  printStream work I/O stream of the visitor
     */
    public CNNJSONVisitorShort(PrintStream printStream) {
        _printStream = printStream;
        /**
         * create parser
         * */
        GsonBuilder builder = new GsonBuilder()
                /**
                 * register all custom adaptors
                 */
                .registerTypeAdapter(Neuron.class, new NeuronConverter())
                .registerTypeAdapter(Connection.class,new ConnectionConverter())
                .registerTypeAdapter(GenericNeuron.class,new GenericNeuronConverter())
                .setPrettyPrinting();
        _gson = builder.create();
    }


    /**
     * CNN Network
     * Print a line for the CNN Layer in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */
    public void visitComponent(Network x) {
        /**Open network description*/
        _printStream.println("{");
        prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");
        _printStream.println(_prefix + "\"inputLayerId\": " + x.getInputLayerId()+",");
        _printStream.println(_prefix + "\"outputLayerId\": " + x.getOutputLayerId()+",");
        prefixInc();

        Iterator i;
        /**Visit all Layers*/
        Vector<Layer> layers = x.getLayers();
        Layer layer;
        int commaBorder = layers.size()-1;
        _printStream.println(_prefix+ "\"layers\": [");
        _prefixInc();

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
        _printStream.println("");
        _prefixDec();
        _printStream.println(_prefix + "],");

        /**Visit all Connections*/
        Vector<Connection> connections = x.getConnections();
        Connection connection;
        commaBorder = connections.size()-1;

        _printStream.println(_prefix + "\"connections\": [");
        _prefixInc();

        i = x.getConnections().iterator();
        while (i.hasNext()) {
            connection = (Connection) i.next();
            connection.accept(this);
            if(commaBorder>0) {
                _printStream.println(",");
                commaBorder--;
            }
        }
        _prefixDec();
        /**Close connections description*/
        _printStream.println("");
        _printStream.println(_prefix + "]");

        /**Close network description*/
        _printStream.println("}");
    }


    /**
     * CNN Layer
     * Print a line for the CNN Layer in the correct format.
     * @param  x Layer to visit
     */
    public void visitComponent(Layer x) {
        /**Open layer description*/
        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"id\": " + x.getId()+",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");
        _printStream.println(_prefix + "\"operator\": \"" + x.getNeuron().getName()+"\",");
        Operator op = x.getNeuron().getOperator();
        visitOperator(op);


        /**Visit data formats*/
        if(!Tensor.isNullOrEmpty(x.getInputFormat())) {
            _printStream.println(_prefix + "\"inputDataFormat\": " + x.getInputFormat().toString() + ",");
        }
        if(!Tensor.isNullOrEmpty(x.getOutputFormat())) {
            _printStream.println(_prefix + "\"outputDataFormat\": "+ x.getOutputFormat().toString() + ",");
        }

        /**Visit pads*/
        int[] pads = x.getPads();
        if(pads!=null){
            _printStream.print(_prefix + "\"pads\": [");
            for(int i=0; i<3; i++){
                _printStream.print(pads[i] + ", ");
            }
            _printStream.println( pads[3] + "]");
        }

        /**Close layer description*/
        _printStream.println( _prefix+ "}");
        prefixDec();

    }

    public void  visitOperator(Operator x){
        if (x == null) return;
        Integer comma_ctr = 0;
        if (x.hasTensorParams()) {
            _printStream.println(_prefix + "\"learnable_parameters\": " );
            _prefixInc();
            _printStream.println(_prefix + "{");
            _prefixInc();
            TreeMap<String, Tensor> tensorParam = x.getTensorParams();
            for (Map.Entry<String, Tensor> entry : tensorParam.entrySet()) {
                if (!Tensor.isNullOrEmpty(entry.getValue())) {
                    _printStream.print(_prefix + "\"" + entry.getKey() + "\": " + entry.getValue().toString());
                    if (comma_ctr < tensorParam.size() - 1){
                        _printStream.println(", ");
                    }
                    else _printStream.println();
                }
                comma_ctr++;
            }
            _prefixDec();
            /**Close tensor param description*/
            _printStream.println( _prefix+ "}, ");
            _prefixDec();
        }
    }

    /**
     * Visit generic neuron
     * @param  x A Visitor Object.
     */
    @Override
    public void visitComponent(GenericNeuron x) {
        /**Open generic neuron description*/
        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getName() + "\",");
        _printStream.println(_prefix + "\"type\": \"" + x.getNeuronType() + "\"");

        /**Visit data formats*/
        if(!Tensor.isNullOrEmpty(x.getInputDataFormat())) {
            _printStream.print(",");
            _printStream.println(_prefix + "\"inputDataFormat\": ");
            x.getInputDataFormat().accept(this);
        }
        if(!Tensor.isNullOrEmpty(x.getOutputDataFormat())) {
            _printStream.print(",");
            _printStream.println(_prefix + "\"outputDataFormat\": ");
            x.getOutputDataFormat().accept(this);
        }
        _printStream.print(",");
        _prefixInc();
        /**Visit internal structure*/
        _printStream.println(_prefix + "\"internalStructure\": ");
        visitComponent(x.getInternalStructure());
        prefixDec();

        /**Close generic neuron description*/
        _printStream.println( _prefix+ "}");
        prefixDec();

    }

    public void visitComponent(Arithmetic x){
        String strComponent = _gson.toJson(x, Arithmetic.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit CNN Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(CNNNeuron x) {
        if(x instanceof Pooling) {
            visitComponent((Pooling) x);
            return;
        }

        if(x instanceof Convolution){
            visitComponent((Convolution) x);
            return;
        }
    }



    /**
     * Visit Pooling Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Pooling x){
        String strComponent = _gson.toJson(x, Pooling.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Convolution Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Convolution x){
        String strComponent = _gson.toJson(x, Pooling.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Concat Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Concat x){
        String strComponent = _gson.toJson(x, Concat.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit LRN Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(LRN x){
        String strComponent = _gson.toJson(x, LRN.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Dropout Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Dropout x){
        String strComponent = _gson.toJson(x, Dropout.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Reshape Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Reshape x){
        String strComponent = _gson.toJson(x, Reshape.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Data Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Data x){
        String strComponent = _gson.toJson(x, Data.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit DenseBlock Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(DenseBlock x){
        String strComponent = _gson.toJson(x, DenseBlock.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit NonLinear Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(NonLinear x){
        String strComponent = _gson.toJson(x, NonLinear.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(ImageScaler x){
        String strComponent = _gson.toJson(x, ImageScaler.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit NonLinear Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Upsample x){
        String strComponent = _gson.toJson(x, Upsample.class);
        _printStream.print(strComponent);
    }

    /**
     * Visit CNN Connection
     * Print a line for the Connection in the correct json format
     * @param x Connection to visit
     */
    public void visitComponent(Connection x) {
        if(x instanceof Custom)
            visitComponent((Custom)x);

        else {
            /** Print standard connection fields */

            _printStream.println(_prefix + "{");
            _prefixInc();
            _printStream.println(_prefix + "\"src\": \"" + x.getSrcName()+"\",");
            _printStream.println(_prefix + "\"dest\": \"" + x.getDestName()+"\"");
            _printStream.println(_prefix + "}");
            _prefixDec();
        }
    }

    /**
     *Visit CNN Custom Connection
     * Print a line for the custom connection in the correct json format
     * @param  x cutsom connection to be visited
     */
    public void visitComponent(Custom x) {
        /** Print standard connection fields */

        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"type\": \"CUSTOM\",");
        _printStream.println(_prefix + "\"src\": \"" + x.getSrcName()+"\",");
        _printStream.println(_prefix + "\"dest\": \"" + x.getDestName()+"\",");
        _printStream.println(_prefix + "\"srcId\": " + x.getSrcId()+",");
        _printStream.println(_prefix + "\"destId\": " + x.getDestId()+",");
        _printStream.println(_prefix + "\"channels\": " + x.getChannels()+",");

        /**Print connection matrix*/
        boolean[][] _matrix = x.getMatrix();
        _printStream.println(_prefix + "\"matrix\": \"");
        _prefixInc();
        String line = "";
        if(_matrix!=null) {
            for (int j = 0; j < _matrix.length; j++) {
                for (int i = 0; i < _matrix[j].length; i++) {
                    if (_matrix[j][i])
                        line += "1";
                    else
                        line += "0";
                }
                _printStream.println(_prefix + line);
                line = "";
            }
        }
        /**Close matrix description*/
        _printStream.println(_prefix + "\"");
        _prefixDec();
        _prefixDec();
        _printStream.println(_prefix + "}");
        _prefixDec();
    }

    /**
     * Visit NonLinear Neuron
     * Print a line for the Neuron in the correct json format.
     * @param  x Neuron to visit
     */
    public void visitComponent(Tensor x){
        String strComponent = _gson.toJson(x, Tensor.class);
        _printStream.print(strComponent);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///
    /**
     * Standard JSON-parser, implements parsing of non-nested types
     */
    private Gson _gson;
}
