package espam.operations.refinement;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.Neuron;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.arithmetic.Add;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;
import espam.datamodel.graph.cnn.neurons.simple.Dropout;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;

import java.util.Vector;

/**
 * Refines generated CSDF graph with DNN-specific features,
 * with memory units and constant values
 */
public class CSDFGMemoryRefiner extends CNNGraphVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call .h visitor for neuron-based model
     *
     * @param x Neuron to be visited
     */
    public Vector<MemoryUnit> callVisitor(Neuron x) {
        _memory = new Vector<>();
        try {
             x.accept(this);
             return _memory;
        } catch (Exception e) {
            System.err.println(" memory refinement generation error for neuron . " + e.getMessage());
        }
        return _memory;
    }

    /**
     * Call .h visitor for layer-based model
     *
     * @param x   Layer to be visited
     */
    public Vector<MemoryUnit> callVisitor(Layer x) {
        _memory = new Vector<>();
        try {
            x.accept(this);
        } catch (Exception e) {
            System.err.println("memory refinement generation error for. " + e.getMessage());
        }
        return _memory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    main elements visit                    ///

    /**
     * Visit a Network component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Network x) {
    }

    /**
     * TODO REFACTORING on dynamic class cast
     * Visit a Layer component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Layer x) {

        Neuron n = x.getNeuron();

         if (n instanceof Add) {
            visitAddLayer(x);
            return;
        }

        if (n instanceof Data) {
            visitDataLayer(x);
            return;
        }

        if (n instanceof DenseBlock) {
            visitDenseLayer(x);
            return;
        }

        if (n instanceof Dropout) {
            visitDropoutLayer(x);
            return;
        }

        if (n instanceof CNNNeuron) {
            _visitCNNLayer(x);
            return;
        }

        if (n instanceof Concat) {
            visitConcatLayer(x);
            return;
        }


        if (n instanceof GenericNeuron) {
            visitGenericLayer(x);
            return;
        }

        if (n instanceof LRN) {
            visitLRNLayer(x);
            return;
        }

        if (n instanceof NonLinear) {
            visitNonLinearLayer(x);
            return;
        }

        if (n instanceof Reshape) {
            visitReshapeLayer(x);
            return;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                      neurons visit                    ///

    /**
     * Visit a Neuron component.
     * TODO REFACTORING on dynamic class cast
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Neuron x) {
        if (x instanceof Add) {
            visitComponent((Add) x);
            return;
        }

        if (x instanceof Data) {
            visitComponent((Data) x);
            return;
        }

        if (x instanceof DenseBlock) {
            visitComponent((DenseBlock) x);
            return;
        }

        if (x instanceof Dropout) {
            visitComponent((Dropout) x);
            return;
        }

        if (x instanceof CNNNeuron) {
            visitComponent((CNNNeuron) x);
            return;
        }

        if (x instanceof Concat) {
            visitComponent((Concat) x);
            return;
        }


        if (x instanceof GenericNeuron) {
            visitComponent((GenericNeuron) x);
            return;
        }

        if (x instanceof LRN) {
            visitComponent((LRN) x);
            return;
        }

        if (x instanceof NonLinear) {
            visitComponent((NonLinear) x);
            return;
        }

        if (x instanceof Reshape) {
            visitComponent((Reshape) x);
            return;
        }
    }


    /**
     * Visit an Add component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Add x) {
        _processMultipleInputs(x);
        _addMU("output",x.getOutputDataFormat());
    }

    /**
     * Visit an Add component.
     *
     * @param x A Visitor Object.
     */
    public void visitAddLayer(Layer x) {
      visitComponent ((Add)x.getNeuron());
    }

    /**
     * Visit a Concat component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Concat x) {
        _processMultipleInputsNB(x);
        _addMU("output",x.getOutputDataFormat());
    }

    /**
     * Visit an Add component.
     *
     * @param x A Visitor Object.
     */
    public void visitConcatLayer(Layer x) {
        _processMultipleInputs((Concat)x.getNeuron());
        _addMU("output",x.getOutputFormat());
    }

    /**
     * Process inputs from multiple sources
     * @param x neuron, processes inputs from multiple sources
     */
    private void _processMultipleInputs(MultipleInputsProcessor x){
        Vector<Layer> inputs = x.getInputOwners();
        if(inputs==null) return;
        if(inputs.size()==0) return;

        for(Layer input: inputs)
            _memory.add(new MemoryUnit(input.getName(), input.getOutputFormat(),_IOmemType));
    }

     /**
     * TODO refactoring
     * Process inputs from multiple sources on neuron-based level
     * @param x neuron, processes inputs from multiple sources
     */
    private void _processMultipleInputsNB(MultipleInputsProcessor x){
        Vector<Layer> inputs = x.getInputOwners();
        if(inputs==null) return;
        if(inputs.size()==0) return;

        int portID = 0;
        if(inputs.size()==1){
            Layer singleInput = inputs.firstElement();
            int inpChannels = singleInput.getOutputChannels();
            for(int i=0; i<inpChannels;i++) {
                String inpName = "IP" + portID;
                _memory.add(new MemoryUnit(inpName, singleInput.getNeuron().getOutputDataFormat(), _IOmemType));
                portID++;
            }
            return;

        }

        for(Layer input: inputs) {
            int inpChannels = input.getOutputChannels();
            for(int i=0; i<inpChannels;i++) {
                String inpName = "IP" + portID;
                _memory.add(new MemoryUnit(inpName, input.getNeuron().getOutputDataFormat(), _IOmemType));
                portID++;
            }
        }
    }

    /**
     * Visit a Data component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Data x) {
        String dataParamName = "input";

        if (x.getName().equals(DataType.INPUT.toString()))
           dataParamName = "data";

        _addMU(dataParamName,x.getInputDataFormat());
        _addMU("output",x.getOutputDataFormat());
    }

       /**
     * Visit a Data component.
     *
     * @param x A Visitor Object.
     */
    public void visitDataLayer(Layer x) {
        visitComponent((Data)x.getNeuron());
    }

    /**
     * Visit a Data component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(DenseBlock x) {
       _addLinearMU("input",x.getInputDataFormat());
       _addLinearMU("output",x.getOutputDataFormat());

    }

        /**
     * Visit a layer, contains nonLinear neuron
     * @param x layer, contains nonLinear neuron
     */
    public void visitDenseLayer(Layer x){
        _addLinearMU("input",x.getInputFormat());
        _addLinearMU("output",x.getOutputFormat());
    }

    /**
     * Visit a Dropout component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Dropout x) {
     _addMU("input",x.getInputDataFormat());
     _addMU("output",x.getOutputDataFormat());
    }

       /**
     * Visit a Reshape component.
     *
     * @param x A Visitor Object.
     */
    public void visitDropoutLayer(Layer x) {
     _addMU("input",x.getInputFormat());
     _addMU("output",x.getOutputFormat());
    }

    /**
     * Visit a Reshape component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Reshape x) {
        _processMultipleInputsNB(x);
        _addMU("output",x.getOutputDataFormat());
    }

    /**
     * Visit a Reshape component.
     *
     * @param x A Visitor Object.
     */
    public void visitReshapeLayer(Layer x) {
        _processMultipleInputs((Reshape)x.getNeuron());
        _addMU("output",x.getOutputFormat());
    }


    /**
     * Visit a CNNNeuron component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(CNNNeuron x) {
        if(x instanceof Convolution)
            visitComponent((Convolution)x);
        if(x instanceof Pooling)
            visitComponent((Pooling) x);
    }

    /**
     * Visit a Convolution component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Convolution x) {
        _visitCNNNeuron(x);
    }

    /**
     * Visit a Pooling component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(Pooling x) {
        _visitCNNNeuron(x);
    }

    /**
     * Visit CNN-neuron
     * @param x CNN-neuron to be visited
     */
    private void _visitCNNNeuron( CNNNeuron x){
        Tensor input = new Tensor(x.getInputDataFormat());
         /**create min input, acceptable for overlapping*/
         if(input!=null) {
             int minH;
             if (input.getDimensionality() > 1) {
                 minH = Math.max(input.getDimSize(1), x.getKernelH());
                 input.setDimSize(1, minH);
             }
             _addMU("input",input);
         }

         _addUnitParam("k_h","" + x.getKernelH(),"int");
         _addUnitParam("k_w","" + x.getKernelW(),"int");
         _addUnitParam("stride","" + x.getStride(),"int");

         _addMU("output",x.getOutputDataFormat());
    }

    /**
     * Visit Layer, contains CNN neuron
     * @param x Layer, contains CNN neuron
     */
    private void _visitCNNLayer(Layer x){
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

         _addUnitParam("k_h","" + layerNeuron.getKernelH(),"int");
         _addUnitParam("k_w","" + layerNeuron.getKernelW(),"int");
         _addUnitParam("stride","" + layerNeuron.getStride(),"int");

         _addMU("output",x.getOutputFormat());
    }

    /**
     * Visit an LRN component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(LRN x) {
        _addLinearMU("input",x.getInputDataFormat());
        _addLinearMU("output",x.getOutputDataFormat());

        int size = x.getSize();
        _addUnitParam("_size","" + size,"int");
    }

    /**
     * TODO check if I/O MUs are not doubled
     * @param x Layer to be visited
     */
    public void visitLRNLayer(Layer x){
        _addLinearMU("input",x.getInputFormat());
        _addLinearMU("output",x.getOutputFormat());

        int size = ((LRN)x.getNeuron()).getSize();
        _addUnitParam("_size","" + size,"int");

    }


    /**
     * Visit a NonLinear component.
     *
     * @param x A Visitor Object.
     */
    public void visitComponent(NonLinear x) {
        _addLinearMU("input",x.getInputDataFormat());
        _addLinearMU("output",x.getOutputDataFormat());
    }

    /**
     * Visit a layer, contains nonLinear neuron
     * @param x layer, contains nonLinear neuron
     */
    public void visitNonLinearLayer(Layer x){
        _addLinearMU("input",x.getInputFormat());
        _addLinearMU("output",x.getOutputFormat());
    }


    /**
     * Add parameter, describes unit value
     * @param name name of the parameter
     * @param valueDesc value.toString()
     * @param typeDesc type of the value
     */
    private void _addUnitParam(String name, String valueDesc, String typeDesc){
        MemoryUnit mu = new MemoryUnit(name,valueDesc,typeDesc);
        _memory.add(mu);
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
     * Visit a GenericNeuron component.
     * TODO implementation
     * @param x A Visitor Object.
     */
    public void visitComponent(GenericNeuron x) {

    }


    /**
     * Visit a Layer contains GenericNeuron .
     * TODO implementation
     * @param x A Visitor Object.
     */
    public void visitGenericLayer(Layer x) {

    }

     /**
     * TODO remove after refactoring is finished
     * create I/0 memory templates description
     * @param layer layer
     * @return Vector of memory templates, required for SDFNode

    public Vector<MemoryUnit> createMemoryDesc(Layer layer){
        Neuron layerNeuron = layer.getNeuron();
        Vector<MemoryUnit> memory = new Vector<>();
        Tensor outShape = layer.getOutputFormat();

         if(outShape!=null) {
             /** linearize output
             if(layerNeuron instanceof NonLinear){
                 outShape = new Tensor(outShape.getElementsNumber());
             }
             /** add common output
             memory.add(new MemoryUnit("output", outShape,_IOmemType));
         }

         Tensor shape = new Tensor(layer.getInputFormat());

         /**create min acceptable for overlapping
         int minH;
         if(layerNeuron instanceof CNNNeuron){
             if(shape==null)
             return memory;
             if(shape.getDimensionality()>1) {
                 minH = Math.max(shape.getDimSize(1), ((CNNNeuron) layerNeuron).getKernelH());
                 shape.setDimSize(1, minH);
             }
          memory.add(new MemoryUnit("input", shape,_IOmemType));
          /** add k_size and stride
          MemoryUnit kh = new MemoryUnit("k_h","" + ((CNNNeuron) layerNeuron).getKernelH(),"int");
          /** add k_size and stride
          MemoryUnit kw = new MemoryUnit("k_w","" + ((CNNNeuron) layerNeuron).getKernelW(),"int");
          MemoryUnit stride = new MemoryUnit("stride","" + ((CNNNeuron) layerNeuron).getStride(),"int");
          memory.add(kh);
          memory.add(kw);
          memory.add(stride);
          return memory;
         }

         /** linearize input
         if(layerNeuron instanceof NonLinear ||layerNeuron instanceof DenseBlock){
             if(shape==null)
                return memory;
             shape = new Tensor(shape.getElementsNumber());
             memory.add(new MemoryUnit("input", shape,_IOmemType));
               if(layerNeuron instanceof LRN){
                 MemoryUnit lrnsize = new MemoryUnit("_size","" + ((LRN)layerNeuron).getSize(),"int");
                 memory.add(lrnsize);
             }

             return memory;
         }

         if(layerNeuron instanceof MultipleInputsProcessor){
             Vector<Layer> inputs = ((MultipleInputsProcessor)layer.getNeuron()).getInputOwners();

            if(inputs==null) return memory;
            if(inputs.size()==0) return memory;

            for(Layer input: inputs){
                memory.add(new MemoryUnit(input.getName(), input.getNeuron().getOutputDataFormat(),_IOmemType));
            }
            return memory;
         }

         /** add simple input
         memory.add(new MemoryUnit("input",shape,_IOmemType));
         return memory;
    }
          */



    /**
     * TODO remove after refactoring is finished
     * create I/0 memory templates description
     * @param neuron neuron
     * @return Vector of memory templates, required for SDFNode

    public Vector<MemoryUnit> createMemoryDesc(Neuron neuron){
         Vector<MemoryUnit> memory = new Vector<>();
         Tensor outShape = neuron.getOutputDataFormat();
         if(outShape!=null) {
             /** linearize output
             if(neuron instanceof NonLinear){
                 outShape = new Tensor(outShape.getElementsNumber());
             }
             /** add common output
             memory.add(new MemoryUnit("output", outShape));
         }

         Tensor shape = new Tensor(neuron.getInputDataFormat());
         /**create min acceptable for overlapping
         int minH;
         if(neuron instanceof CNNNeuron){
             if(shape==null)
             return memory;
             if(shape.getDimensionality()>1) {
                 minH = Math.max(shape.getDimSize(1), ((CNNNeuron) neuron).getKernelSize());
                 shape.setDimSize(1, minH);
             }
          memory.add(new MemoryUnit("input", shape));
          /** add k_size and stride
          MemoryUnit ksize = new MemoryUnit("k_size","" + ((CNNNeuron) neuron).getKernelSize(),"int");
          MemoryUnit stride = new MemoryUnit("stride","" + ((CNNNeuron) neuron).getStride(),"int");
          memory.add(ksize);
          memory.add(stride);
          return memory;
         }

         /** linearize input
         if(neuron instanceof NonLinear ||neuron instanceof DenseBlock){
             if(shape==null)
                return memory;
             shape = new Tensor(shape.getElementsNumber());
             memory.add(new MemoryUnit("input", shape));
             if(neuron instanceof LRN){
                 MemoryUnit lrnsize = new MemoryUnit("_size","" + ((LRN)neuron).getSize(),"int");
                 memory.add(lrnsize);
             }

             return memory;
         }

         if(neuron instanceof MultipleInputsProcessor){
            Vector<Layer> inputs = ((MultipleInputsProcessor)neuron).getInputOwners();

            if(inputs==null) return memory;
            if(inputs.size()==0) return memory;

            for(Layer input: inputs){
                memory.add(new MemoryUnit(input.getName(), input.getNeuron().getOutputDataFormat()));
            }
            return memory;
         }

         /** add simple input
         memory.add(new MemoryUnit("input",shape));
         return memory;
    }
    */

    ////////////////////////////////////////////////////////
    //////      Weights processing                /////////
    /**
     * Create weights description for neuron
     * @param neuron neuron
     * @param channels number of channels
     * @param weightsType type of the weights
     * @return weights description
     */
    public MemoryUnit createWeightsDescription(Neuron neuron, int channels, String weightsType){
        if(neuron instanceof Convolution)
            return createCNNWeightsDescription((Convolution)neuron,channels,weightsType);
        if(neuron instanceof DenseBlock)
            return createDenseWeightsDescription((DenseBlock)neuron,channels,weightsType);

        return null;
    }

    /**
     * Create layer weights description for a layer
     * shape of [neurons, {neuron_weights}],
     * where {neuron_weights} is a tensor of weights for one neuron
     * @param layer layer to create weights for
     * @param weightsType type of the weights
     * @return weights description
     */
    public MemoryUnit createWeightsDescription(Layer layer, String weightsType){
        MemoryUnit neuronWeights = createWeightsDescription(layer.getNeuron(),layer.getInputChannels(),weightsType);

        if(neuronWeights==null)
            return null;

        int neurons = layer.getNeuronsNum();
          if(neurons>1) {
              Tensor weightsShape = neuronWeights.getShape();
              weightsShape.insertDimension(neurons,0);
          }
        return neuronWeights;
    }

    /**
     * Create description of internal buffer, keeps weights
     * for CNNNeuron weights shape:
     * [input channels, k_size, k_size]
     * @return weights description
     */
    private MemoryUnit createCNNWeightsDescription(CNNNeuron neuron, int inputChannels, String weightsType){

        int kernelW = neuron.getKernelW();
        int kernelH = neuron.getKernelH();

        Tensor weights = new Tensor();

        if(inputChannels>1)
            weights.addDimension(inputChannels);
            weights.addDimension(kernelH);
            weights.addDimension(kernelW);

         MemoryUnit weightsMU = new MemoryUnit("weights",weights,weightsType);
         return weightsMU;
    }

    /**
     * Create description of internal buffer, keeps weights for Dense neuron
     * weights shape: [linearized input, one block neurons number]
     * @param neuron Dense neuron
     * @param inputChannels number of input channels
     * @param weightsType type of the weights
     * @return weights description
     */

    public MemoryUnit createDenseWeightsDescription(DenseBlock neuron, int inputChannels, String weightsType){

        Tensor input = neuron.getInputDataFormat();
        if(Tensor.isNullOrEmpty(input))
            return null;

        Tensor weights = new Tensor();

        int blockNeuronsNum = neuron.getNeuronsNum();
        int lin_input = input.getElementsNumber();

        if(inputChannels>1)
            lin_input *= inputChannels;
        weights.addDimension(blockNeuronsNum);
        weights.addDimension(lin_input);

        MemoryUnit weightsMU = new MemoryUnit("weights",weights,weightsType);
        return weightsMU;
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

    /**
     * Get memory unit by its name
     * @param name name of the memory unit
     * @return MemoryUnit with specified name or null
     */
    private MemoryUnit _getMemoryUnit(String name){
        for(MemoryUnit mu: _memory){
            if(mu.getName().equals(name))
                return mu;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ///
    /** memory description*/
    private Vector<MemoryUnit> _memory;

    /** input/output memory type*/
    private String _IOmemType = "float";

    /** weights memory type*/
    private String _weightsMEMType = "float";
}