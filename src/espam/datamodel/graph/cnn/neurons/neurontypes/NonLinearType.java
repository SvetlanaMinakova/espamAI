package espam.datamodel.graph.cnn.neurons.neurontypes;
/**
 * Describes the concrete type of the NonLinear Neuron
 */
public enum NonLinearType {
    ReLU, LeakyReLu, SELU, SIGM, SOFTMAX, SOFTPLUS, THN, DROPOUT, LRN, BN,
    ImageScaler, AddConst, NONE, PAD, DIVConst, MULconst, SUBConst, CLIP
}
