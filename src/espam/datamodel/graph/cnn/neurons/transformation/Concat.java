package espam.datamodel.graph.cnn.neurons.transformation;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.neurontypes.NeuronType;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;

public class Concat extends Neuron implements MultipleInputsProcessor {
     /**
     * Create new Concat element with a name
     * a stride and a kernelSize for the Concat element = 1
     * an input sample dimension is 1 (vector)
     */
    public Concat () {
        super();
        setName(NeuronType.CONCAT.toString());
        setNeuronType(NeuronType.CONCAT);
    }

      /**
     * Get number of operations, could be implemented on the neurons current
     * input data. If the input data format is null or empty,
     * 0 operations is returned. By default, operation is implemented
     * once over every input value of every input channel
     * @param channels number of input channels
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(int channels){
    return getInputsNumber();
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

     /**
      * Compares Concat neuron with another object
      * @param obj Object to compare this Neuron with
      * @return true if Neuron is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

       Concat concat = (Concat)obj;
         return getInputsNumber() == concat.getInputsNumber()
              && Tensor.isSame(getInputDataFormat(),concat.getInputDataFormat())
              && Tensor.isSame(getOutputDataFormat(),concat.getOutputDataFormat());
       }

     /**
     * Clone this Neuron
     * @return a new reference on the Neuron
     */
    public Concat clone() {
        Concat newObj = (Concat) super.clone();
        newObj._inputOwners = this._inputOwners;
        return newObj;
    }

    /**
     * Create a deep copy of this neuron
     * @param c original neuron to be copied
     */
    public Concat (Concat c) {
        super(c);

        _inputOwners = new Vector<>();
        for(Layer inputOwner: c._inputOwners)
            _inputOwners.add(inputOwner);
    }

    /**
     * Return the string description of the neuron specific parameters
     * @return the string description of the neuron specific parameters
     */
    @Override
    public String getStrParameters() {
     StringBuilder strParams = new StringBuilder();
     strParams.append("\n [\n");
     strParams.append("inputs number: "+getInputsNumber() + "]");
     return strParams.toString();
    }

    /**
     * Set new data formats for neuron
     * @param inputDataFormats input data formats
     */
    public void setInputDataFormat(Vector<Tensor> inputDataFormats) {
       try {

           Tensor mergedInput = Tensor.mergeToSequence(inputDataFormats);
           setInputDataFormat(mergedInput);
           setOutputDataFormat(mergedInput);
           setSampleDim(inputDataFormats.elementAt(0).getDimensionality());
       }
       catch (Exception e) {
           System.err.println(e.getMessage());
       }
    }

    /**
     * Automatically calculates the output format of a neuron
     * concatenation operation merges input by the last dimension
     * @param inputDataFormat input data format description - tensor w x h x ...
     * @return output data format of a neuron
     */
    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {
        try {
            if(_inputOwners == null)
                return null;

            /** update inputs for dynamic inputs*/
            Vector<Tensor> _inputs = new Vector<>();

            for(Layer inputOwner: _inputOwners) {
                if(inputOwner!=null) {
                    if(inputOwner.getOutputFormat().getDimensionality()==inputOwner.getNeuron().getOutputDataFormat().getDimensionality() &&_inputOwners.size()>1) {
                        Tensor inp = new Tensor(inputOwner.getOutputFormat());
                        inp.addDimension(1);
                        _inputs.add(inp);
                    }
                    else _inputs.add(inputOwner.getOutputFormat());
                }
            }

            Tensor mergedInput = Tensor.mergeToSequence(_inputs);
            return mergedInput;
        }
        catch (Exception e){
            System.err.println("Concat data formats calculation error for data formats ");
            for(Layer inputOwner: _inputOwners){
                 System.err.print(inputOwner.getOutputFormat() + " , ");
            }
            System.err.println("");
            return inputDataFormat;
        }
    }


     /**TODO CHECK
     * Automatically calculates the min input height of the neuron
     * for dense block
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */
    @Override
    public int calculateMinInputDataHeight( int minOutputHeight) {
        Tensor curInputDataFormat = getInputDataFormat();

        if(Tensor.isNullOrEmpty(curInputDataFormat))
            return minOutputHeight;

        /** if concat merges list of vectors, return min height of vector = 1*/
        if(getInputsNumber()>1 && curInputDataFormat.getDimensionality()<3)
            return 1;

        if(curInputDataFormat.getDimensionality()>2)
            return curInputDataFormat.getDimSize(2);

        /**
         * return min height of previous data layer by default
         */
        return minOutputHeight;
    }

    /**
     * Get number of concat block inputs
     * @return number of concat block inputs
     */
    public int getInputsNumber() {
        if(_inputOwners==null)
            return 0;

        return _inputOwners.size();
    }

    /**
     * Get multiple node input owners
     * @return multiple node input owners
     */
    public Vector<Layer> getInputOwners() { return _inputOwners; }

    /**
     * Get multiple neuron inputs
     * @return multiple neuron inputs
     */
    public Vector<Tensor> getInputs(){
        Vector<Tensor> inputs = new Vector<>();

        if(_inputOwners==null)
            return  inputs;

        for (Layer inputOwner: _inputOwners){
            inputs.add(inputOwner.getOutputFormat());
        }
        return inputs;
    }

    /////////////////////////////////////////////////////////////////////
    ////                  multiple inputs resolving                 ////
    /**TODO REFACTORING ON CNNNEURONS AND THEIR DEPENDENT
     * Set input data format from multiple inputs
     * @param neuronOwner owner of the concat neuron
     * @throws Exception if an error occurs
     */
    public void setDataFromMultipleInputs(Layer neuronOwner) throws Exception{
        if(_inputOwners==null)
            throw new Exception("Concat data formats calculation: no inputs found");

           Vector<Tensor> _inputs = new Vector<>();
            for(Layer inputOwner: _inputOwners){
                if(inputOwner.getOutputFormat().getDimensionality()==inputOwner.getNeuron().getOutputDataFormat().getDimensionality() &&_inputOwners.size()>1) {
                    Tensor inp = new Tensor(inputOwner.getOutputFormat());
                    inp.addDimension(1);
                    _inputs.add(inp);
                }
                else _inputs.add(inputOwner.getOutputFormat());
            }

        Tensor mergedInput = Tensor.mergeToSequence(_inputs);

        setInputDataFormat(mergedInput);
        setOutputDataFormat(calculateOutputDataFormat(getInputDataFormat()));

        neuronOwner.setInputFormat(mergedInput);
        neuronOwner.setOutputFormat(neuronOwner.calculateOutputFormat());
    }


     /**
     * Consistency checkout: checks, if an input is acceptable for the node
     * @param inputDataFormat input data format
     * @return true, if input node is acceptable and false otherwise
     */
    public boolean isAcceptableInput(Tensor inputDataFormat){
        for(Layer inputOwner: _inputOwners) {
            if (inputOwner.getOutputFormat().equals(inputDataFormat))
                return true;
        }

        return false;
    }


    /**
     * Add new input from another layer
     * @param inputOwner layer, owns the input
     */
    public void addInput(Layer inputOwner){
        _inputOwners.add(inputOwner);
    }

     /**
     * Update neuron parameters after input connection was removed
     */
    public void removeInput(Layer neuronOwner,Layer inputOwner) {
        if(getInputsNumber()>0) {
            _inputOwners.remove(inputOwner);
        }
    }


    /**
     * Get number of operations, could be implemented on the neurons current
     * input data.
     * @return number of operations, could be implemented on the input data
     */
    public int getOperationsNumber(){
        return Math.max(getInputsNumber()-1,0);
    }

     /**
     * Calculate number of function calls inside of the neuron
     * Concat node always called once for all (full) inputs
     * @return number of function calls inside of the node
     */
     @Override
    public int getFuncCallsNum(int scale){
         return 1;
    }

    /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description
     */
    @Override
    public String getFunctionCallDescription(int channels){
      return getFunctionCallName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** references to input layers*/
    private transient Vector<Layer> _inputOwners = new Vector<>();
}
