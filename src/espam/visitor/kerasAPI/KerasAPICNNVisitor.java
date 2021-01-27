package espam.visitor.kerasAPI;

import espam.datamodel.graph.cnn.BoundaryMode;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.arithmetic.Add;
import espam.datamodel.graph.cnn.neurons.arithmetic.Arithmetic;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.ArithmeticOpType;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.cnn.neurons.neurontypes.PoolingType;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CNNGraphVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

public class KerasAPICNNVisitor extends CNNGraphVisitor {
    /**
     * Constructor
     * @param  printStream work I/O stream of the visitor
     */
    public KerasAPICNNVisitor(PrintStream printStream) {
        _printStream = printStream;
    }

    /** Visit a DNN model and generate compact description of a DNN model
     * @param dnn dnn to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(Network dnn, String dir) {
        try {
            PrintStream printStream = JSONFileWorker.openFile(dir, dnn.getName() + "_keras_api", "py");
            KerasAPICNNVisitor visitor = new KerasAPICNNVisitor(printStream);
            dnn.accept(visitor);
            System.out.println("Keras API file generated: " + dir + "/" + dnn.getName() + "_keras_api.py");
            printStream.close();
        } catch (Exception e) {
            System.err.println("Keras API file fault. " + e.getMessage());
        }
    }

    /**
     * Visit DNN
     * @param  x A Visitor Object.
     */
    public void visitComponent(Network x) {
        x.sortLayersInTraverseOrder();

        /** open dnn definition */
        _printStream.println("from tensorflow import keras");
        _printStream.println();
        _printStream.println("def " + x.getName() + "():");

        /**Visit all layers*/
        Layer layer;
        prefixInc();
        prefixInc();
        Iterator i = x.getLayers().iterator();
        while (i.hasNext()) {
            layer = (Layer) i.next();
            visitComponent(layer);
        }

        /**Close network description*/
        _printStream.println();
        _printStream.println(_prefix + "model = keras.Model(inputs=[" + x.getInputLayer().getName() + "], outputs = " + x.getOutputLayer().getName() + ")");
        _printStream.println(_prefix + "return model");
        _prefixDec();
        _prefixDec();
        _printStream.println();

    }

    @Override
    public void visitComponent(Layer x) {
        String layer_prefix = "keras.layers.";
        if (_isIOLayer(x)) {
            if (_isInputLayer(x))
                _processInputLayer(x, layer_prefix);
            if (_isOutputLayer(x))
                _processOutpLayer(x);
        }

        else {
            _printStream.print(_prefix + x.getName() + " = " + layer_prefix);
            visitComponent(x.getNeuron());
            _writeInputs(x);
            _printStream.println();
        }
    }

    @Override
    public void visitComponent(NonLinear x) {
        if (x.getName().equals(NonLinearType.BN.toString())) {
            _printStream.print("BatchNormalization( ");
            if (x.getParameters().containsKey("momentum"))
                _printStream.print("momentum=" + x.getParameters().get("momentum") + ", ");
            if (x.getParameters().containsKey("epsilon"))
                _printStream.print("epsilon=" + x.getParameters().get("epsilon") + ", ");
            _printStream.print("name=\"" + x.getParent().getName() + "\")");
        }
    }

    /**
     * added = Add()([x1, x2])
     * @param  x A Visitor Object.
     */
    public void visitComponent(Arithmetic x) {
        if(x.getName().equals(ArithmeticOpType.ADD.toString()))
            _printStream.print("Add()");
    }

    /**
     * @param  x A Visitor Object.
     */
    @Override
    public void visitComponent(DenseBlock x) {
        _printStream.print("Dense(" + x.getNeuronsNum());
        if (x.hasNonLin())
            _printStream.print(", activation=\"" + _kerasActivation(x.getNonlin()) + "\"");
        _printStream.print(", name=\"" + x.getParent().getName() + "\"");
        _printStream.print(")");
    }

    @Override
    public void visitComponent(CNNNeuron x) {
        if (x instanceof Convolution)
            visitComponent((Convolution) x);
        if (x instanceof Pooling)
            visitComponent((Pooling)x);
    }

    /**
     * @param  x A Visitor Object.
     */
    @Override
    public void visitComponent(Convolution x) {
        String autopad = _kerasAutoPad(x.getParent(), x.getBoundaryMode());

        if (autopad == "custom")
            _printStream.print("PAD LAYER REQUIRED");

        _printStream.print("Conv2D(" + x.getParent().getNeuronsNum() + ", " +
                        "kernel_size=(" + x.getKernelW() + ", " + x.getKernelH() + "), " +
                        "strides=(" + x.getStride() + ", " + x.getStride() + "),");

        _printStream.println(_prefix);

        // print standard autopad
        if (autopad!="custom")
            _printStream.print("padding=\"" + autopad + "\", ");

        _printStream.print("name=\"" + x.getParent().getName() + "\"");
        if (x.hasNonLin())
            _printStream.print(", activation=\"" + _kerasActivation(x.getNonlin()) + "\"");
        _printStream.print(")");
    }

    /**
     * @param  x A Visitor Object.
     */
    @Override
    public void visitComponent(Pooling x) {
        String poolingOp = _kerasPooling(x.getName());
        if (poolingOp.contains("Global"))
            visitGlobalPooling(x, poolingOp);
        else
            visitLocalPooling(x, poolingOp);
    }

    public void visitLocalPooling(Pooling x, String poolingOp){
        _printStream.print(poolingOp + "(" +
                "pool_size=(" + x.getKernelW() + ", " + x.getKernelH() + "), " +
                "strides=(" + x.getStride() + ", " + x.getStride() + "), " +
                "name=\"" + x.getParent().getName() + "\"");
        _printStream.print(")");
    }

    public void visitGlobalPooling(Pooling x, String poolingOp){
        _printStream.print(poolingOp + "()");
    }

    private void _writeInputs(Layer layer){
        Vector<Connection> layerInputs = layer.getInputConnections();
        int inputsCount = layerInputs.size();
        if (inputsCount < 1)
            return;
        int inpId =0;
        _printStream.print("(");
        if (inputsCount > 1)
            _printStream.print("[");
        for (Connection inpConnection : layerInputs){
            _printStream.print(inpConnection.getSrcName());
            if(inpId < inputsCount - 1)
                _printStream.print(", ");
            inpId++;
        }
        if (inputsCount > 1)
            _printStream.print("]");
        _printStream.print(")");
    }


    private String _kerasAutoPad(Layer layer, BoundaryMode espamBoundaryMode){
        if (espamBoundaryMode.equals(BoundaryMode.SAME))
            return "same";
        if (_samePadSimulated(layer))
            return "same";
        if (layer.isNullorEmptyPads())
            return "valid";
        return "custom";
    }

    private boolean _samePadSimulated(Layer layer){
        if (layer.getInputFormat().equals(layer.getOutputFormat()))
            return true;
        return false;
    }

    private String _kerasActivation(String espamNonlinear){
        return espamNonlinear.toLowerCase();
    }

    private String _kerasPooling(String espamPooling){
        if (espamPooling == PoolingType.AVGPOOL.toString())
            return "AveragePooling2D";
        if (espamPooling == PoolingType.GLOBALAVGPOOL.toString())
            return  "GlobalAveragePooling2D";

        return "MaxPooling2D";
    }


    ////////////////////////////////////////////////////////
    ///          INPUT/OUTPUT LAYERS PROCESS          /////
    private boolean _isIOLayer(Layer x) {
        return  _isInputLayer(x) ||_isOutputLayer(x);
    }

    private boolean _isInputLayer(Layer x){
        return  x.getNeuron().getName() == DataType.INPUT.toString();
    }

    private boolean _isOutputLayer(Layer x){
        return x.getNeuron().getName() == DataType.OUTPUT.toString();
    }


    private void _processInputLayer(Layer layer, String layerPrefix){
        _printStream.print(_prefix + layer.getName() + " = " + layerPrefix + "Input(shape=[" + layer.getInpW() + ", " + layer.getInpH() + ", " + layer.getInputChannels()
                + "], name=\"" + layer.getName() + "\")");
        _writeInputs(layer);
        _printStream.println();
    }


    private void _processOutpLayer(Layer layer){
        // outp layer points to the last layer in the DNN (i.e., single input of output data)
        Vector<Connection> inputCons = layer.getInputConnections();
        if (inputCons.size() !=1) {
            System.err.println("Keras output layer generation error. Outp layer " + layer.getName() +
                    " has " + inputCons.size() + " inputs, while 1 input is expected");
        }
        else
            _printStream.println(_prefix + layer.getName() + " = " + inputCons.firstElement().getSrcName());
    }

}
