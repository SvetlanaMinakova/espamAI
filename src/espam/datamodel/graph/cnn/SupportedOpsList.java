package espam.datamodel.graph.cnn;
import espam.datamodel.graph.cnn.neurons.neurontypes.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SupportedOpsList {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** get instance of the singletone class*/
    public static SupportedOpsList getInstance(){
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

    public HashMap<Character, Vector<String>> getSupportedOps(){
        return _supportedOps;
    }

    public Vector<String> getSupportedOpsStartWidth(Character letter){
        if(_supportedOps.containsKey(letter))
            return _supportedOps.get(letter);
        return new Vector<>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                ////
    /** singlentone class*/
    protected SupportedOpsList(){
        _supportedOps = new HashMap<>();
        //A
        _addToSupportedList(ArithmeticOpType.ADD.toString());
        _addToSupportedList(PoolingType.AVGPOOL.toString());
        //B
        _addToSupportedList(NonLinearType.BN.toString());
        //C
        _addToSupportedList(NeuronType.CONV.toString());
        _addToSupportedList(NeuronType.CONCAT.toString());
        //D
        _addToSupportedList(NonLinearType.DIVConst.toString());
        _addToSupportedList(NonLinearType.DROPOUT.toString());
        //G
        _addToSupportedList(DenseType.GEMM.toString());
        _addToSupportedList(PoolingType.GLOBALAVGPOOL.toString());
        _addToSupportedList(PoolingType.GLOBALLPPOOL.toString());
        _addToSupportedList(PoolingType.GLOBALMAXPOOL.toString());
        //I
        _addToSupportedList(NonLinearType.ImageScaler.toString());
        //L
        _addToSupportedList(NonLinearType.LeakyReLu.toString());
        _addToSupportedList(NonLinearType.LRN.toString());
        //M
        _addToSupportedList(PoolingType.MAXPOOL.toString());
        _addToSupportedList(DenseType.MATMUL.toString());
        _addToSupportedList(NonLinearType.MULconst.toString());
        //P
        _addToSupportedList(NonLinearType.PAD.toString());
        //R
        _addToSupportedList(NonLinearType.ReLU.toString());
        _addToSupportedList(NeuronType.RESHAPE.toString());
        //S
        _addToSupportedList(NonLinearType.SELU.toString());
        _addToSupportedList(NonLinearType.SIGM.toString());
        _addToSupportedList(NonLinearType.SOFTMAX.toString());
        _addToSupportedList(NonLinearType.SOFTPLUS.toString());
        _addToSupportedList(NonLinearType.SUBConst.toString());
        //T
        _addToSupportedList(NonLinearType.THN.toString());
        _addToSupportedList(NeuronType.TRANSPOSE.toString());
        //U
        _addToSupportedList(NeuronType.UPSAMPLE.toString());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private   methods                ////

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

    ///////////////////////////////////////////////////////////////////
    ////                   protected variables                    ////

    private static SupportedOpsList _supportedOpsList = new SupportedOpsList();
    private HashMap<Character, Vector<String>> _supportedOps;
}
