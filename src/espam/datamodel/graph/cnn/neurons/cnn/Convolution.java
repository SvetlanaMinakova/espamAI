package espam.datamodel.graph.cnn.neurons.cnn;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.BoundaryMode;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.visitor.CNNGraphVisitor;

/**
 * Convolution operation
 * Describes one CNN Convolutional operation
 * One Convolution gets as in input one local area
 * and returns as an output one value
 */
public class Convolution extends CNNNeuron {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Convolution() {
        super();
        setNeuronType(NeuronType.CONV);
        setName(NeuronType.CONV.toString());
    }

    /**
     * Constructor to create a Convolution with a kernel size and boundaryMode and default stride = 1
     * Default boundary mode: SAME
     * Dimension of one input sample: 2
     * @param kernelSize kernel size of a Convolution
     * @param boundaryMode boundary processing mode
     */
    public Convolution(int kernelSize, BoundaryMode boundaryMode) {
        super(NeuronType.CONV.toString(), kernelSize, 1);
        setNeuronType(NeuronType.CONV);
        setBoundaryMode(boundaryMode);
    }

    /**
     * Constructor to create a Convolution with a kernel size, boundaryMode and  stride
     */
    public Convolution(int kernelSize, BoundaryMode boundaryMode, int stride) {
        super(NeuronType.CONV.toString(), kernelSize, stride);
        setNeuronType(NeuronType.CONV);
        setBoundaryMode(boundaryMode);
    }

    /**
     * Constructor to create a Convolution with a kernel size, boundaryMode and  stride
     */
    public Convolution(int kernelSize, BoundaryMode boundaryMode, int stride, boolean transpose) {
        super(NeuronType.CONV.toString(), kernelSize, stride);
        setNeuronType(NeuronType.CONV);
        setBoundaryMode(boundaryMode);
        setTranspose(transpose);
    }

     /**
     * Accept a Visitor
     *
     * @param x A Visitor Object.
     * @throws EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) {x.visitComponent(this);}

    /**
     * Clone this Convolution Neuron
     * @return a new reference on the Convolution Neuron
     */
    public Convolution clone() {
        Convolution newObj = (Convolution) super.clone();
        return newObj;
    }



    /**
     * Copy this Convolution Neuron
     * @return a deep copy of this Convolution Neuron
     */
    public Convolution (Convolution c) {
        super(c);
    }

        /**
     * Get number of input tokens for each operation, perfomed in a neuron.
     * If the input data format is null or empty,0 tokens is returned.
     * For neurons, always taking on input a single value, null-description is returned.
     * For neurons, which performs shape transformation, null-description is returned.
     * @return number of input tokens for each operation, perfomed in a neuron
     */
    @Override
    public int getOperationTokensNumber(int channels){
        return super.getOperationTokensNumber(channels);
    }

}

