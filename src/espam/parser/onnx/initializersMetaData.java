package espam.parser.onnx;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class initializersMetaData {

    public HashMap<String, String> get_conv_weights() { return conv_weights; }

    public HashMap<String, Vector<String>> getBNpar() { return BNpar; }

    public void setBNpar(HashMap<String, Vector<String>> BNpar) { this.BNpar = BNpar; }

    public void set_conv_weights(HashMap<String, String> weights) { this.conv_weights = weights; }

    public HashMap<String, String> get_dense_weights() { return dense_weights; }

    public void set_dense_weights(HashMap<String, String> weights) { this.dense_weights = weights; }

    public HashMap<String, String> get_conv_weights_nodes() { return conv_weights_nodes; }
    public HashMap<String, String> get_dense_weights_nodes() { return dense_weights_nodes; }

    public void set_conv_weights_nodes(HashMap<String, String> weightsNodes) {
        this.conv_weights_nodes = weightsNodes;
    }
    public void set_dense_weights_nodes(HashMap<String, String> weightsNodes) { this.dense_weights_nodes = weightsNodes; }

    public HashMap<String, String> getBiases() { return biases; }

    public void setBiases(HashMap<String, String> biases) { this.biases = biases; }

    public HashMap<String, String> getBiasesNodes() {
        return biases_nodes;
    }

    public void setBiasesNodes(HashMap<String, String> biasesNodes) {
        this.biases_nodes = biasesNodes;
    }

    public void setConv_partition(Integer conv_partition) { this.conv_partition = conv_partition; }

    public Integer getDense_partition() { return dense_partition; }

    public void setDense_partition(Integer dense_partition) { this.dense_partition = dense_partition; }

    public Integer getConv_partition() { return conv_partition; }

    public HashMap<String, Integer> getDense_neurons() { return dense_neurons; }

    public void setDense_neurons(HashMap<String, Integer> dense_neurons) { this.dense_neurons = dense_neurons; }

    private HashMap<String,String> conv_weights;
    private HashMap<String,String> dense_weights;
    private HashMap<String,String> biases;

    private HashMap<String,String> conv_weights_nodes;
    private HashMap<String,String> dense_weights_nodes;
    private HashMap<String,String> biases_nodes;

    private HashMap<String,Integer> dense_neurons;

    private Integer dense_partition = 100;
    private Integer conv_partition = 1;

    private HashMap<String,Vector<String>> BNpar;
}
