package espam.visitor.onnx;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.utils.fileworker.FileWorker;

import java.io.PrintStream;
import java.util.Vector;

/**
 * ONNX data formats helper transform internal
 * data formats to ONNX data formats and proposes weights
 * shapes
 */
public class ONNDataFormatsHelper {

    /**
     * Return singleton instance of data formats helper
     * @return singleton instance of data formats helper
     */
    public static ONNDataFormatsHelper getInstance(){
        return _formatsHelper;
    }

    /**
     * Print ONNX data formats and weights to console
     * @param dnn deep neural network
     */
    public void printONNXDataFormatsAndWeights(Network dnn, String dir,String filename, String extension){
       try{
        PrintStream printStream = FileWorker.openFile(dir,filename,extension);

        dnn.sortLayersInTraverseOrder();
        String prefix = "   ";


        for(Layer layer:dnn.getLayers()){
            printStream.println(layer.getName());

            Vector<Tensor> inputs = getONNXInputFormats(layer);

            printStream.println(prefix + "input(s)");
            for(Tensor input: inputs)
                printStream.println(prefix + prefix + input + ", ");


            Tensor output = getONNXOutputFormat(layer);

            printStream.println(prefix + "output");
            printStream.println(prefix + prefix + output);

            Tensor weights = generateWeights(layer);
            if(!Tensor.isNullOrEmpty(weights)){
                printStream.println(prefix + "weights");
                printStream.println(prefix + prefix + weights);
            }
        }

            System.out.println("Data formats file: " + dir + filename + "." + extension + " generated");

        }
        catch (Exception e){
            System.err.println("ONNX data formats generation error: " +
                    e.getMessage());
        }

    }


    /**
     * Print ONNX data formats and weights to console
     * @param dnn deep neural network
     */
    public void printONNXDataFormatsAndWeights(Network dnn){
        dnn.sortLayersInTraverseOrder();
        String prefix = "   ";

        try {


        for(Layer layer:dnn.getLayers()){
            System.out.println(layer.getName());

            Vector<Tensor> inputs = getONNXInputFormats(layer);

            System.out.println(prefix + "input(s)");
            for(Tensor input: inputs)
                System.out.println(prefix + prefix + input + ", ");


            Tensor output = getONNXOutputFormat(layer);

            System.out.println(prefix + "output");
            System.out.println(prefix + prefix + output);

            Tensor weights = generateWeights(layer);
            if(!Tensor.isNullOrEmpty(weights)){
                System.out.println(prefix + "weights");
                System.out.println(prefix + prefix + weights);
            }
        }

        }
        catch (Exception e){
            System.err.println("ONNX data formats generation error: " +
                    e.getMessage());
        }


    }

    /**
     * Generate weights for layer
     * @param layer layer to be processed
     * @return weights description or null
     */
    public Tensor generateWeights(Layer layer){
        Neuron layerNeuron = layer.getNeuron();
        if(layerNeuron instanceof Convolution)
            return _generateONNXConvWeightsShape(layer);
        if(layerNeuron instanceof DenseBlock)
            return _generateONNXDenseWeightsShape(layer);
            return null;
    }

    /**
     * Get ONNX input data format(s) of the layer
     * @param layer layer in internal representation
     */
    public Vector<Tensor> getONNXInputFormats(Layer layer) throws Exception{
        Vector<Tensor> inputDataFormats = new Vector<>();

        Neuron layerNeuron = layer.getNeuron();

        if(layerNeuron instanceof MultipleInputsProcessor){
            for(Tensor input: ((MultipleInputsProcessor) layerNeuron).getInputs()) {
               Tensor onnxFormat = _generateONNXDataFormat(input);
               inputDataFormats.add(onnxFormat);
            }
        }

        else {
            Tensor singleInput = layer.getInputFormat();
            Tensor onnxFormat = _generateONNXDataFormat(singleInput);
            inputDataFormats.add(onnxFormat);
        }

        return inputDataFormats;
    }


     /**
     * Get ONNX output data format of the layer
     * @param layer
     */
    public Tensor getONNXOutputFormat(Layer layer) throws Exception{
        Tensor onnxFormat = _generateONNXDataFormat(layer.getOutputFormat());
        return onnxFormat;
    }

    /**
     * Generate ONNX I/O data format from internal data format
     * @param internalDataFormat internal data format
     * @return ONNX I/O data format
     */
    private Tensor _generateONNXDataFormat(Tensor internalDataFormat) throws Exception{
        if(internalDataFormat==null)
            return null;
        if(internalDataFormat.getDimensionality()<3)
            return _generateONNXTwoDimDataFormat(internalDataFormat);
        if(internalDataFormat.getDimensionality()<5)
            return _generateONNXFourDimDataFormat(internalDataFormat);

        throw new Exception("Data formats conversion error: internal data format "+
                internalDataFormat+" ,unprocessable dimensionality");
    }

    /**
     * Generate ONNX-like I/O data format for CNN Layer
     *  and all dependent layers from CNN Layers
     *
     * Internal format: [W x H x C], where
     *      W - image width
     *      H - image height
     *      C - number of input channels
     *
     * ONNX format: [ N x C x H x W], where
     *
     *      N - image batch size (always = 1)
     *      TODO: for now batch size always = 1 for CNNs (img processing)
     *      C - number of input channels
     *      H - image height
     *      W - image width
     *
     * @param internalDataFormat internal data format of convolutional neuron
     * @return ONNX-like data format for CNN Layer
     */
    private Tensor _generateONNXFourDimDataFormat(Tensor internalDataFormat){
       Tensor onnxDataFormat = Tensor.reverse(internalDataFormat);

       /** batch size is omitted and should be set to 1*/
       if(internalDataFormat.getDimensionality()<4)
           onnxDataFormat.insertDimension(1,0);

       return onnxDataFormat;
    }

     /**
     * Generate ONNX-like I/O data format for Dense(GEMM/MATMUL/FC) Layer
     *  and all dependent layers
     *
     * Internal format: [W x H], where
     *      W - image width
     *      H - image height
      *
     *
     * ONNX format: [M, K]
     *
     *      N - image batch size (always = 1)
     *      TODO: for now batch size always = 1 for CNNs (img processing)
     *      C - number of input channels
     *      H - image height
     *      W - image width
     *
     * @param internalDataFormat internal data format of Dense(GEMM/MatMul/FC) neuron
     * @return ONNX-like data format for CNN Layer
     */
    private Tensor _generateONNXTwoDimDataFormat(Tensor internalDataFormat){
       Tensor onnxDataFormat = Tensor.reverse(internalDataFormat);

       /** batch size is omitted and should be set to 1*/
       if(internalDataFormat.getDimensionality()==1)
           onnxDataFormat.insertDimension(1,0);

       return onnxDataFormat;
    }

    /**
     * Generate weights shape for Conv layer shape of
     * [M x C/group x kH x kW], where
     *     M - number of neurons (feature maps),
     *     C - number of input channels,
     *     k_h - kernel height
     *     k_w - kernel width
     * @param convLayer Convolutional layer
     * @return weights shape for Convolutional layer
     */
    private Tensor _generateONNXConvWeightsShape(Layer convLayer){
        Tensor weightsShape = new Tensor();
        Convolution convNeuron = (Convolution) convLayer.getNeuron();

        weightsShape.addDimension(convLayer.getNeuronsNum());
        weightsShape.addDimension(convLayer.getInputChannels());
        weightsShape.addDimension(convNeuron.getKernelH());
        weightsShape.addDimension(convNeuron.getKernelW());

        /**TODO: 3D-convs depth - processing */
       /** Integer depth = convNeuron.getKernelD();
        if(depth!=null)
            weightsShape.addDimension(depth); */

        return weightsShape;
    }

     /**
     * Generate weights shape for Dense layer shape of
     * [M x N], where
     *     M - number of neurons,
     *     N - number of flatten inputs,
     * @param denseLayer Dense layer (GEMM or MatMul)
     * @return weights shape for Convolutional layer
     */
    private Tensor _generateONNXDenseWeightsShape(Layer denseLayer){
        Tensor weightsShape = new Tensor();
        DenseBlock denseNeuron = (DenseBlock) denseLayer.getNeuron();

        int flattenInputs = 0;
        Tensor denseInput = denseLayer.getInputFormat();
        if(!Tensor.isNullOrEmpty(denseInput)){
            flattenInputs = denseInput.getElementsNumber();
        }

        weightsShape.addDimension(denseNeuron.getNeuronsNum());
        weightsShape.addDimension(flattenInputs);

        return weightsShape;
    }


    /** singleton instance of data formats helper*/
 public static ONNDataFormatsHelper _formatsHelper = new ONNDataFormatsHelper();

}
