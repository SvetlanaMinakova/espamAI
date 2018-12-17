package espam.datamodel.graph.cnn.neurons.simple;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.neurons.neurontypes.NonLinearType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

/**
 * Clas describes a NonLinear block, implements Dropout
 * Dropout is a regularization technique, which throws out part of the connections
 * between neurons of connected layers of DNN model
 * TODO reference
 * TODO parameters of dropout unit are strongly dependent on its realization
 * TODO and fact, if dropout is applied during the inference as well or not
 */

public class Dropout extends NonLinear {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create new default Dropout element
     */
    public Dropout () {
       super(NonLinearType.DROPOUT);
    }

    /**
     * Create new Dropout element with specified ratio
     */
    public Dropout (double ratio) {
       super(NonLinearType.DROPOUT);
       setRatio(ratio);
    }

    /**
     * TODO Data Height is not recalculated for this node for now
     * Automatically calculates the min input height of the neuron
     * h dimension is changed according to an inverse formula of output DataFormat height calculation
     * if this inverse formula exists and return unchanged otherwise
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */

    public int calculateMinInputDataHeight( int minOutputHeight) { return getInputHeight(); }

    /**
     * TODO Data Height is not recalculated for this node for now
     * @param minOutputDataHeight min output data height
     * @return false
     */
    public boolean setMinDataHeight(int minOutputDataHeight){
        return false;
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }


     /**
     * Clone this Dropout Neuron
     * @return a new reference on the Dropout Neuron
     */
    public Dropout clone() {
        Dropout newObj = (Dropout) super.clone();
        newObj._ratio = this._ratio;
        return (newObj);
    }

      /**
     * Create a deep copy of this neuron
     * @param d original neuron to be copied
     */
    public Dropout(Dropout d) {
        super(d);
        _ratio = d._ratio;
    }

     /**
   * Compares Dropout neuron with another object
   * @param obj Object to compare this Neuron with
   * @return true if Neuron is equal to the object and false otherwise
   */
    @Override
    public boolean equals(Object obj) {

        boolean isSuperParamsEqual = super.equals(obj);
        if (isSuperParamsEqual)
            return _ratio == ((Dropout) obj)._ratio;

        return false;
    }

    /**
     * Return string description of neuron's specific parameters
     * @return string description of neuron's specific parameters
     */
    @Override
    public String getStrParameters() {
    StringBuilder strParams = new StringBuilder();
     strParams.append("\n parameters: [\n");
     strParams.append("  ratio: "+_ratio +" ]");
     return strParams.toString();
    }

    /**
     * Get dropout ratio
     * @return dropout ratio
     */
    public double getRatio() { return _ratio; }

    /**
     * Set dropout ratio
     * @param ratio dropout ratio
     */
    public void setRatio(double ratio) { this._ratio = ratio; }

     /**
     * Calculate number of function calls inside of the neuron
     * TODO: classical dropout runs once per input sample, but for
     * TODO the neuron-based model it can be improved
     * @return number of function calls inside of the node
     */
     @Override
    public int getFuncCallsNum(int scale){
         return 1;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /**
     *  dropout probability
     * */
     @SerializedName("ratio")private double _ratio;



}
