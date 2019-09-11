package espam.operations.transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;

import java.util.Vector;

public class CSDFIOMemoryGenerator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call .h visitor for layer-based model
     * TODO: refactoring: move all layer-specific code into Layer Operator generation
     * @param x   Layer to be visited
     */
    public Vector<MemoryUnit> generateIOMemory(Layer x, String IOdataType) {
        _memory = new Vector<>();
        _IOmemType = IOdataType;
        try {

            /** TODO: check!*/
          //  _addMU("output",x.getOutputFormat());

            if(x.getNeuron() instanceof MultipleInputsProcessor) {
                x.sortInputsInConcatOrder();
                _processMultipleInputs((MultipleInputsProcessor) x.getNeuron());
                _addMU("output",x.getOutputFormat());
                return _memory;
            }
            else  {
                if (x.getNeuron()instanceof Data) {
                    visitDataLayer(x);
                    return _memory;
                }
                if(x.getNeuron() instanceof CNNNeuron){
                    visitCNNLayer(x);
                    return _memory;
                }

                if(x.getNeuron() instanceof GenericNeuron){
                    visitGenericLayer(x);
                    return _memory;
                }
            }

            /** default*/
            _addMU("output",x.getOutputFormat());
            _addLinearMU("input",x.getInputFormat());


        } catch (Exception e) {
            System.err.println("I/Omemory generation error for. " + e.getMessage());
        }
        return _memory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private methods                         ///

    /**
     * Process inputs from multiple sources
     * @param x neuron, processes inputs from multiple sources
     */
    private void _processMultipleInputs(MultipleInputsProcessor x){

        Vector<Layer> inputs = x.getInputOwners();
        if(inputs==null) return;
        if(inputs.size()==0) return;

        for(Layer input: inputs) {
          //  System.out.println(input.getName());
            _memory.add(new MemoryUnit(input.getName(), input.getOutputFormat(), _IOmemType));
        }

    }

     /**
     * Visit a Data component.
     * @param x A Visitor Object.
     */
    public void visitDataLayer(Layer x) {
      String dataParamName = "input";

      if (x.getNeuron().getName().equals(DataType.INPUT.toString()))
           dataParamName = "data";

        _addMU(dataParamName,x.getInputFormat());
        _addMU("output",x.getOutputFormat());
    }

        /**
     * Visit a Data component.
     * @param x A Visitor Object.
     */
    public void visitGenericLayer(Layer x) {
      GenericNeuron gn = (GenericNeuron) x.getNeuron();
      Tensor inputDataFormat = gn.getInternalStructure().getInputLayer().getInputFormat();
      Tensor outputDataFormat = gn.getInternalStructure().getOutputLayer().getOutputFormat();

        _addMU("input",inputDataFormat);
        _addMU("output",outputDataFormat);
    }


    /**
     * Visit Layer, contains CNN neuron
     * @param x Layer, contains CNN neuron
     */
    private void visitCNNLayer(Layer x){
        CNNNeuron layerNeuron = (CNNNeuron)x.getNeuron();
        Tensor input = new Tensor(x.getInputFormat());

         /**create min acceptable for overlapping*/
         int minH;
         if(input!=null) {
                 if (input.getDimensionality() > 1) {
                     minH = Math.max(input.getDimSize(1), layerNeuron.getKernelH());
                     input.setDimSize(1, minH);
                 }
                 _addMU("input",input);
             }
         _addMU("output",x.getOutputFormat());
    }


    /**
     * Add memory unit with given name and shape
     * @param name memory unit name
     * @param shape shape of the memory unit
     */
    private void _addMU(String name, Tensor shape){
        if(shape==null)
            return;
        _memory.add(new MemoryUnit(name, shape,_IOmemType));
    }


    /**
     * Add linearized memory unit (all input tokens are
     * stored in a vector)
     * @param name memory unit name
     * @param shape shape of the memory unit
     */
    private void _addLinearMU(String name, Tensor shape){
        if(shape==null)
            return;

        shape = new Tensor(shape.getElementsNumber());
        _memory.add(new MemoryUnit(name, shape,_IOmemType));
    }

    /**
     * Set input/output memory type
     * @param IOmemType input/output memory type
     */
    public void setIOmemType(String IOmemType) {
        if(!(IOmemType==null))
        this._IOmemType = IOmemType;
    }

    /** get generated memory description*/
    public Vector<MemoryUnit> getMemory() {
        return _memory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ///
    /** memory description*/
    private Vector<MemoryUnit> _memory;

    /** input/output memory type*/
    private String _IOmemType = "float";
}
