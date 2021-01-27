package espam.visitor.json;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.operations.evaluation.MeasurementUnitsConverter;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Compact per-layer power/perf/memory evaluation of a DNN
 */
public class CNNEvaluationCompactJsonVisitor extends CNNGraphVisitor{
    /** Visit a DNN model and generate compact description of a DNN model
     * @param dnn dnn to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir) {
        try {
                PrintStream printStream = JSONFileWorker.openFile(dir, dnn.getName() + "_eval", "json");
                CNNEvaluationCompactJsonVisitor visitor = new CNNEvaluationCompactJsonVisitor(printStream);
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
    public CNNEvaluationCompactJsonVisitor(PrintStream printStream) {
        _printStream = printStream;
    }


    /**
     * Visit DNN
     * @param  x A Visitor Object.
     */
    public void visitComponent(Network x) {
        x.sortLayersInTraverseOrder();
        /**Open network description*/
        _printStream.println("{");
        _prefixInc();
        _printStream.println(_prefix + "\"name\": \"" + x.getName() + "\",");
        _printStream.println(_prefix + "\"layers_num\": " + x.countLayers() + ",");
        _printStream.println(_prefix + "\"connections_num\": " + x.countConnections() + ",");
        _printStream.println(_prefix + "\"time_ms\": " + x.get_timeEval() + ",");
        _printStream.println(_prefix + "\"memory_MB\": " + x.get_memEval() + ",");
        _printStream.println(_prefix + "\"energy_J\": " + x.get_energyEval() + ",");
        _prefixInc();

        /**Print layers eval*/

        /**Print all layers descriptions*/
        _printLayersDesc(x);
        /**Print layers times*/
        _printLayersTimeEval(x);
        /**Print layer memories*/
        _printLayersMemEval(x);
        /**print layer energies*/
        _printLayersEnergyEval(x);


        /**Visit all connections*/
        Vector<Connection> connections = x.getConnections();
        Connection con;
        int commaBorder = connections.size()-3;
        _printStream.println(_prefix + "\"connections\": [");

        prefixInc();
        Iterator i = x.getConnections().iterator();
        while (i.hasNext()) {
            con = (Connection) i.next();
            //print only hidden connections
            if(con.getSrc().getInputConnections().size()>0 && con.getDest().getOutputConnections().size()>0) {
                con.accept(this);
                if (commaBorder > 0) {
                    _printStream.println(",");
                    commaBorder--;
                }
            }
        }
        _prefixDec();
        /**Close connections description*/
        _printStream.println();
        _printStream.println(_prefix + "]");

        /**Close network description*/
        _printStream.println("}");
    }


    /** print layers descriptions
     * @param dnn Deep neural network
     */
    private void _printLayersDesc(Network dnn){
        //_printStream.println("total nodes: " + (dnn.countLayers() - 2));
        Integer nodesToEval = (dnn.countLayers() - 2);//All layers except of input and output layer
        Integer evalId = 0;
        //params
        int datasize = MeasurementUnitsConverter.typeSizeBytes(dnn.getDataType());
        int paramsize = MeasurementUnitsConverter.typeSizeBytes(dnn.getWeightsType());

        _printStream.println(_prefix + "\"layers_desc\":[");
        _prefixInc();
        for(Layer l: dnn.getLayers()) {
            if(!l.equals(dnn.getInputLayer()) && !l.equals(dnn.getOutputLayer())) {
                _printLayerDesc(l, datasize, paramsize);
                if(evalId < nodesToEval - 1 )
                    _printStream.println(", ");
                evalId++;
            }
        }
        prefixDec();
        /**Close layers description*/
        _printStream.println();
        _printStream.println(_prefix + "],");
    }

    /** print layer description*/
    private void _printLayerDesc(Layer l, int datasize, int paramsize) {
        String op;
        int k_size, stride, neurons;
        Tensor idata, odata, weights;
        op = l.getNeuron().getName().toLowerCase();

        k_size = 1;
        stride = 1;

        neurons = l.getNeuronsNum();

        if(l.getNeuron() instanceof CNNNeuron) {
            k_size = ((CNNNeuron) l.getNeuron()).getKernelSize();
            stride = ((CNNNeuron) l.getNeuron()).getStride();
        }

        if(l.getNeuron() instanceof DenseBlock)
            neurons = ((DenseBlock) l.getNeuron()).getNeuronsNum();


        idata = l.getInputFormat();
        odata = l.getOutputFormat();
        weights = new Tensor();

        if(l.getNeuron().getOperator().hasTensorParams()) {
            if(l.getNeuron().getOperator().getTensorParams().containsKey("weights"))
                weights = l.getNeuron().getOperator().getTensorParams().get("weights");
            else weights = new Tensor();
        }

        _printStream.print(_prefix + "{\"id\": "+ l.getId() + ", \"op\": \"" + op + "\", \"k_size\": " + k_size + ", \"stride\": " + stride + ", \"neurons\": " + neurons +
                            ", \"i_data\": "+idata.toString() + ", \"o_data\": "+ odata.toString());

        Long macs_num = l.getNeuron().getOperator().getTimeComplexity();
        Integer i_data_size = idata.getElementsNumber() * datasize;
        Long w_size = l.getNeuron().getOperator().getMemoryComplexity() * paramsize;
        Integer o_size = odata.getElementsNumber() * datasize;

        if(!Tensor.isNullOrEmpty(weights))
            _printStream.print(", \"weights\": " + weights.toString());

        _printStream.println();
        _printStream.print(_prefix + "      , \"ops_num\": "+ macs_num + ", \"weight_size_bytes\": " + w_size + ", \"i_size_bytes\": "
                + i_data_size + ", \"o_size_bytes\": " + o_size);

        _printStream.print(_prefix + ", \"time_ms\": " + l.get_timeEval());

        _printStream.print("}");
    }


    /** print time evaluation per DNN layer*/
    private void _printLayersTimeEval(Network dnn){
        Integer nodesToEval = (dnn.countLayers() - 2);//All layers except of input and output layer
        Integer evalId = 0;
        String strEval;

        _printStream.print(_prefix + "\"time_per_layer_ms\":[");
        _prefixInc();
        for(Layer l: dnn.getLayers()) {
            if(!l.equals(dnn.getInputLayer()) && !l.equals(dnn.getOutputLayer())) {
                 strEval = String.format("%.2f", l.get_timeEval());
                _printStream.print(strEval);
                if(evalId < nodesToEval - 1 )
                    _printStream.print(", ");
                     evalId++;
            }
        }
        prefixDec();
        /**Close layers description*/
        _printStream.println("],");
        _printStream.println();

    }

    /** print total memory evaluation per DNN layer*/
    private void _printLayersMemEval(Network dnn){
        Integer nodesToEval = (dnn.countLayers() - 2);//All layers except of input and output layer
        Integer evalId = 0;
        String strEval;
        _printStream.print(_prefix + "\"mem_per_layer_MB\":[");
        _prefixInc();
        for(Layer l: dnn.getLayers()) {
            if(!l.equals(dnn.getInputLayer()) && !l.equals(dnn.getOutputLayer())) {
                strEval = String.format("%.2f", l.get_memEval());
                _printStream.print(strEval);
                if(evalId < nodesToEval - 1 )
                    _printStream.print(", ");
                evalId++;
            }
        }
        prefixDec();
        /**Close layers description*/
        _printStream.println("],");
        _printStream.println();


    }

    /** perint energy evaluation per DNN layer*/
    private void _printLayersEnergyEval(Network dnn){
        Integer nodesToEval = (dnn.countLayers() - 2);//All layers except of input and output layer
        Integer evalId = 0;
        String strEval;
        //params
        _printStream.print(_prefix + "\"energy_per_layer_J\":[");
        for(Layer l: dnn.getLayers()) {
            if(!l.equals(dnn.getInputLayer()) && !l.equals(dnn.getOutputLayer())) {
                strEval = String.format("%.4f", l.get_energyEvalJoules());
                _printStream.print(strEval);
                if(evalId < nodesToEval - 1 )
                    _printStream.print(", ");
                evalId++;
            }
        }
        prefixDec();
        /**Close layers description*/
        _printStream.println("],");
        _printStream.println();

    }

    /**visit CNN connection*/
    @Override
    public void visitComponent(Connection x) {
            _printStream.print(_prefix + "{\"src\": " + x.getSrcId() + ", \"dst\": "+x.getDestId() + "}");
    }
}
