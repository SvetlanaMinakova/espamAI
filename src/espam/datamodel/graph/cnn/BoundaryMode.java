package espam.datamodel.graph.cnn;

/**
 * Mode of method for processing the bounds of an input tensor
 */
public enum BoundaryMode {
    /**
     * Size of the output tensor is smaller then the size of the input tensor
     */
    VALID,
    /**
     * Size of the output tensor is the same as the size of the input tensor
     */
    SAME,
    /**
     * Size of the output tensor is greater then the size of the input tensor
     */
    FULL,

    /** explicit pads are expected*/

    NOTSET;

    @Override
    public String toString() {
        return super.toString();
    }

}
