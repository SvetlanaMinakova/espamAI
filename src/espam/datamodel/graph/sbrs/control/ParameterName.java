package espam.datamodel.graph.sbrs.control;

/**
 * Allowed names for run-time adaptive parameters:
 * OP: CNN operator/neuron (conv/maxpool)...
 * HYP: layer hyperparameters: kernel size, stride,...
 * PAR: learnable parameters: weights, biases, etc.
 * I/O : input and output connections
 */
public enum ParameterName {
    OP, HYP, PAR, I, O
}
