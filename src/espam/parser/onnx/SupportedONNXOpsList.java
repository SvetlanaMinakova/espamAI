package espam.parser.onnx;

import espam.datamodel.graph.cnn.SupportedOpsList;
import espam.datamodel.graph.cnn.neurons.neurontypes.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SupportedONNXOpsList {
    /** get instance of the singletone class*/
    public static SupportedONNXOpsList getInstance(){
        return _supportedOpsList;
    }

    /** Prints all possible names for CNN operators*/
    public void printSupportedOps(){
        for (Map.Entry<Character, Vector<String>> entry: _supportedOps.entrySet()){
            System.out.println(entry.getKey());
            {
                for(String opName: entry.getValue())
                    System.out.println("    " + opName);

            }
        }
    }

    /** singlentone class*/
    protected SupportedONNXOpsList(){
        _supportedOps = new HashMap<>();
        //A
        _addToSupportedList("Add (espam " + ArithmeticOpType.ADD.toString() + ")");
        _addToSupportedList("AveragePool(espam " + PoolingType.AVGPOOL.toString()+ ")");
        //B
        _addToSupportedList("BatchNormalization (espam " +  NonLinearType.BN.toString() + ")");
        //C
        _addToSupportedList("Conv, ConvTranspose (espam " +  NeuronType.CONV.toString() + ")");
        _addToSupportedList("Concat (espam " +  NeuronType.CONCAT.toString() + ")");
        //D
        _addToSupportedList("Div (espam " +  NonLinearType.DIVConst.toString() + ")");
        _addToSupportedList("Dropout (espam " +  NonLinearType.DROPOUT.toString() + ")");
        //F
        _addToSupportedList("Flatten (espam " +  NeuronType.RESHAPE.toString() + ")");
        //G
        _addToSupportedList("Gemm (espam " +  DenseType.GEMM.toString() + ")");
        _addToSupportedList("GlobalAveragePool (espam " +  PoolingType.GLOBALAVGPOOL.toString() + ")");
        _addToSupportedList("GlobalLpPool (espam " +  PoolingType.GLOBALLPPOOL.toString() + ")");
        _addToSupportedList("GlobalMaxPool (espam " +  PoolingType.GLOBALMAXPOOL.toString() + ")");
        //I
        _addToSupportedList("ImageScaler (espam " +  NonLinearType.ImageScaler.toString() + ")");
        //L
        _addToSupportedList("LeakyRelu (espam " +  NonLinearType.LeakyReLu.toString() + ")");
        _addToSupportedList("LRN (espam " +  NonLinearType.LRN.toString() + ")");
        //M
        _addToSupportedList("MaxPool (espam " +  PoolingType.MAXPOOL.toString() + ")");
        _addToSupportedList("MatMul (espam " +  DenseType.MATMUL.toString() + ")");
        _addToSupportedList("Mul (espam " +  NonLinearType.MULconst.toString() + ")");
        //P
        _addToSupportedList("Pad (espam " +  NonLinearType.PAD.toString() + ")");
        //R
        _addToSupportedList("Relu (espam " +  NonLinearType.ReLU.toString() + ")");
        _addToSupportedList("Reshape (espam " +  NeuronType.RESHAPE.toString() + ")");
        //S
        _addToSupportedList("Selu (espam " +  NonLinearType.SELU.toString() + ")");
        _addToSupportedList("Sigmoid (espam " +  NonLinearType.SIGM.toString() + ")");
        _addToSupportedList("Softmax (espam " +  NonLinearType.SOFTMAX.toString() + ")");
        _addToSupportedList("Softplus (espam " +  NonLinearType.SOFTPLUS.toString() + ")");
        _addToSupportedList("Sub (espam " +  NonLinearType.SUBConst.toString() + ")");
        //T
        _addToSupportedList("Tanh (espam " +  NonLinearType.THN.toString() + ")");
        _addToSupportedList("Transpose (espam " +  NeuronType.TRANSPOSE.toString() + ")");
        //U
        _addToSupportedList("Upsample (espam " +  NeuronType.UPSAMPLE.toString() + ")");
        _addToSupportedList("Unsqueeze (espam " +  NeuronType.RESHAPE.toString() + ")");

    }

    private void _addToSupportedList(String opName){
        char firstLetter = opName.charAt(0);
        Vector<String> valList;
        if(_supportedOps.containsKey(firstLetter)) {
            valList = _supportedOps.get(firstLetter);
            valList.add(opName);
        }
        else {
            valList = new Vector<>();
            valList.add(opName);
            _supportedOps.put(firstLetter, valList);
        }
    }


    private static SupportedONNXOpsList _supportedOpsList = new SupportedONNXOpsList();
    private HashMap<Character, Vector<String>> _supportedOps;
}
