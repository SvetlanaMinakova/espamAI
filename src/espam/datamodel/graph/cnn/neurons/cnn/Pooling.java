package espam.datamodel.graph.cnn.neurons.cnn;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.BoundaryMode;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.cnn.neurons.neurontypes.PoolingType;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

/**
 * Class of pooling neuron of neural network
 */
public class Pooling extends CNNNeuron implements ConnectionDependent {

    /**Constructor to create new pooling Neuron with default kernel 2x2
     * @param name name of the new Pooling neuron. The choice of the name of a nonlinear element is limited
     * by available types of the Pooling neurons
     */
    public Pooling (PoolingType name) {
        super(name.toString(),2,2);
        setNeuronType(NeuronType.POOL);
        setBoundaryMode(BoundaryMode.VALID);
        setSampleDim(2);
    }

    /**Constructor to create new pooling Neuron with specified kernel size
     * If stride is not specified, stride = kernelSize
     * @param name name of the new Pooling neuron
     * @param kernelSize kernelsize of a pooling neuron
     */
    public Pooling (PoolingType name, int kernelSize) {
        super(name.toString(),kernelSize,kernelSize);
        setNeuronType(NeuronType.POOL);
        setBoundaryMode(BoundaryMode.VALID);
        setSampleDim(2);
    }

    /**Constructor to create new pooling Neuron
     * @param name name of the new Pooling neuron
     * @param stride stride and kernelsize of a pooling neuron (for pooling neuron kernelSize = stride by default)
     */
    public Pooling (PoolingType name, int kernelSize, int stride) {
        super(name.toString(),kernelSize,stride);
        setNeuronType(NeuronType.POOL);
        setBoundaryMode(getAutoBoundaryMode());
        setSampleDim(2);
    }

          /**
     * Constructor to create a Convolution with a kernel size, boundaryMode and  stride
     */
    public Pooling(PoolingType name, int kernelSize, int stride, BoundaryMode boundaryMode) {
        super(name.toString(), kernelSize, stride);
        setNeuronType(NeuronType.POOL);
        setBoundaryMode(boundaryMode);
    }

       /**
     * Constructor to create a Convolution with a kernel size, boundaryMode and  stride
     */
    public Pooling(PoolingType name, int kernelSize, int stride, BoundaryMode boundaryMode, boolean transpose) {
        super(name.toString(), kernelSize, stride);
        setNeuronType(NeuronType.POOL);
        setBoundaryMode(boundaryMode);
        setTranspose(transpose);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     * Clone this Pooling Neuron
     * @return a new reference on the pooling Neuron
     */
    public Pooling clone() {
        Pooling newObj = (Pooling) super.clone();
        return (newObj);
    }

     /**
     * Copy this Convolution Neuron
     * @return a deep copy of this Convolution Neuron
     */
    public Pooling (Pooling c) {
        super(c);
    }

   /**
   * Compares Pooling neuron with another object
   * @param obj Object to compare this Neuron with
   * @return true if Neuron is equal to the object and false otherwise
   */
    @Override
    public boolean equals(Object obj) {

        boolean isSuperParamsEqual = super.equals(obj);
        if (isSuperParamsEqual) {
            Pooling pool = (Pooling) obj;
            return getBoundaryMode()==pool.getBoundaryMode();

        }
        return false;
       }

    /**
     * Automatically determines boundary mode for Pooling neuron.
     * For pooling neuron only FULL or VALID boundary modes are allowed.
     * @return automatically determined boundary mode for Pooling neuron
     */
    public BoundaryMode getAutoBoundaryMode() {
        if(getKernelH()<getStride()&&getKernelW()<getStride())
            return BoundaryMode.FULL;
        return BoundaryMode.VALID;
    }

     /** recalculate Layer neurons number, if it is dependent on input connections
     * @param neuronOwner Layer, contains neuron
     * @param input input of neuronOwner
     */
    public void recalculateNeuronsNumber(Layer neuronOwner, Layer input) throws Exception{

        if(input != null ){

            neuronOwner.setNeuronsNum(input.getNeuronsNum());
         /**   if(input.getNeuron() instanceof Concat){
                System.out.println(neuronOwner.getName()+" takes after Concat");
                System.out.println("cur sampleDim: " + getSampleDim());
            } */

                return;
            }

        System.err.println("Parameters update fail: pooling layer " + neuronOwner.getName()+" should not have multiple inputs");
        throw new Exception("Pooling layer "+neuronOwner.getName()+" parameters update fail:");
    }

    /** TODO REFACTORING. REMOVE HOTFIX*/
    @Override
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        Tensor outputFormat = super.calculateOutputDataFormat(inputDataFormat);
        int inpDims = inputDataFormat.getDimensionality();
        int outpDims = outputFormat.getDimensionality();

        if(outpDims<inpDims){
            int diff = inpDims - outpDims;
            int startId = inpDims - diff;
            for(int i = startId;i<inpDims;i++) {
                outputFormat.addDimension(inputDataFormat.getDimSize(i));
            }
          //  System.out.println("Pooling output envided from "+ outpDims +" to "+ inpDims);
        }
        return outputFormat;
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
