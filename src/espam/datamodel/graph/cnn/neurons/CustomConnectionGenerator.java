package espam.datamodel.graph.cnn.neurons;

/**
 * Interface shows, that Neuron has custom connection
 */
public interface CustomConnectionGenerator {
    boolean[][] generateCustomConnectionMatrix(int srcNeuronsNum, int dstNeuronsNum);
}
