package espam.datamodel.graph.cnn;
import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CNNGraphVisitor;

import java.io.Console;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 * represents one Layer of CNN
 */
public class Layer implements Cloneable, ReferenceResolvable, Comparable<Layer> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Empty layer constructor: required for valid parsing
     */
    public Layer() {}

    /**
     * Constructor to create new layer with a name
     * @param name layer's name
     */
    public Layer(String name) {
        setName(name);
    }

    /**
     * Bulids a layer of SDFNods by copying sample Node nodesNum times
     * @param sampleNeuron typical element of a layer
     * @param neuronsNum number of neurons in a layer
     * @return list of SDFNodes, represents a layer
     */
    public Layer(String name, Neuron sampleNeuron, int neuronsNum) {
        setName(name);
        setNeuron(sampleNeuron);
        _neuron.setParent(this);
        setNeuronsNum(neuronsNum);
    }

      /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     * Clone this Layer
     * @return  a new reference on the Layer
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Layer newObj = (Layer) super.clone();
            newObj.setName(_name);
            newObj.setNeuron((Neuron)_neuron.clone());
            newObj.setNeuronsNum(_neuronsNum);
            newObj.setOutputFormat((Tensor)_outputDataFormat.clone());
            newObj.setInputFormat((Tensor)_inputDataFormat.clone());
            newObj.setId(_id);
            newObj.setstartNeuronId(_startNeuronId);
            newObj.setPads(_pads.clone());
            newObj.setAutopads(_autopads);
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    /**
     * Create a deep copy of the Layer
     * @param layer Layer to be copied
     */
    public Layer (Layer layer) {
        setName(layer._name);
        setNeuron(Neuron.copyNeuron(layer._neuron));
        _neuron.setParent(this);
        setNeuronsNum(layer._neuronsNum);
        setOutputFormat(new Tensor(layer._outputDataFormat));
        setInputFormat(new Tensor(layer._inputDataFormat));
        setId(layer._id);
        setstartNeuronId(layer._startNeuronId);

        if(_autopads) {
            setAutopads(true);
        }
        else {
            if (_pads != null) {
                _pads = new int[layer._pads.length];
                for (int i = 0; i < _pads.length; i++) {
                    _pads[i] = layer._pads[i];
                }
            }
        }
    }

    /**
     * Return string description of the Layer
     * @return string description of the Layer
     */
    @Override
    public String toString() {
        return getName() + "\nneurons num: "+getNeuronsNum();
    }

    /**
    * Compares Layer  with another object
    * @param obj Object to compare this Layer with
    * @return true if Layer is equal to the object and false otherwise
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

        Layer layer = (Layer)obj;
         return _id==layer._id
              &&_startNeuronId == layer._startNeuronId
              &&_name.equals(layer._name)
              &&_neuronsNum == layer._neuronsNum
              &&_neuron.equals(layer._neuron)
              && Tensor.isSame(_inputDataFormat,layer._inputDataFormat)
              && Tensor.isSame(_outputDataFormat,layer._outputDataFormat);
       }

    @Override
    public int compareTo(Layer l) {
        return this.getstartNeuronId().compareTo(l.getstartNeuronId());
    }



    /**
     * Clear data formats of the layer
     * Sets all data formats as empty tensors
     */
    public void clearDataFormats() {
        setInputFormat(new Tensor());
        setOutputFormat(new Tensor());
        _neuron.clearDataFormats();
    }

    /**
     * Update data formats of the layer
     * @param neuronInputDataFormat input data format of the typical layer's neuron
     * @param layerInputDataFormat input data format of the layer
     */
    public void updateDataFormatsFromTop(Tensor neuronInputDataFormat,Tensor layerInputDataFormat) {

        // System.out.println(getName() + " out: ");
        /** process generic neuron as a subnetwork*/
        if(_neuron instanceof GenericNeuron){
            Network subNetwork = ((GenericNeuron) _neuron).getInternalStructure();

            subNetwork.setDataFormats(layerInputDataFormat);
            subNetwork.getInputLayer().setInputFormat(layerInputDataFormat);
            subNetwork.getInputLayer().setOutputFormat(subNetwork.getInputLayer().calculateOutputFormat());
            subNetwork.getOutputLayer().setOutputFormat(subNetwork.getOutputLayer().calculateOutputFormat());

            _neuron.setInputDataFormat(layerInputDataFormat);
            _neuron.setOutputDataFormat(subNetwork.getOutputLayer().getOutputFormat());


            /**_neuron.setInputDataFormat(layerInputDataFormat);
            _neuron.setOutputDataFormat(subNetwork.getOutputLayer().getOutputFormat());
            setInputFormat(subNetwork.getInputLayer().getInputFormat());
            setOutputFormat(subNetwork.getOutputLayer().getOutputFormat());*/
            return;
        }
        _neuron.setInputDataFormat(neuronInputDataFormat);

        Tensor neuronInputDataFormatWithPads = Tensor.addPads(neuronInputDataFormat,_pads);
        //_neuron.setInputDataFormat(neuronInputDataFormatWithPads);
       // Tensor layerInputDataFormatWithPads = Tensor.addPads(layerInputDataFormat,_pads);

        _neuron.setOutputDataFormat(_neuron.calculateOutputDataFormat(neuronInputDataFormatWithPads));

        if(!(_neuron instanceof MultipleInputsProcessor))
            setInputFormat(layerInputDataFormat);

        setOutputFormat(calculateOutputFormat());

        //System.out.println(getName() + " output format: "+_outputDataFormat);

    }


    /**TODO CHECK
     * Automatically calculates the min input height of the layer
     * for dense block
     * @param minOutputHeight min height on the neuron output
     * @return new minimal  a neuron
     */

    public int calculateMinInputDataHeight( int minOutputHeight) {
        return _neuron.calculateMinInputDataHeight(minOutputHeight);
    }

     /**
     * Updates min input data height, if possible
     * @param minOutputDataHeight
     */
    public void updateMinDataHeight(int minOutputDataHeight){

        if(_neuron.setMinDataHeight(minOutputDataHeight)) {
          setInputHeight(_neuron.getInputHeight());
          setOutputHeight(_neuron.getOutputH());
       }
    }

      /**
     * Set neuron output height
     * @return true, if neuron output height was updated and false otherwise
     */
    public void setOutputHeight(int newHeight){
        _outputDataFormat = new Tensor(_outputDataFormat);
            _outputDataFormat.setDimSize(1, newHeight);
    }

          /**
     * Set neuron input height
     * @return true, if neuron input height was updated and false otherwise
     */
    public void setInputHeight(int newHeight){
        _inputDataFormat = new Tensor(_inputDataFormat);
        _inputDataFormat.setDimSize(1, newHeight);
    }


    /**
     * Get layer input height. layer input height should be the same with the neuron's
     * input height
     * @return layer input height
     */
    public int getInputHeight(){
        return _neuron.getInputHeight();
    }

    /**
     * Get layer inputwidth. layer input width should be the same with
     * the neuron's input width
     * @return layer input width
     */
    public int getInputWidth(){
        return _neuron.getInputWidth();
    }

    /**
     * Get layer output height. layer output height should be the same with the neuron's output height
     * @return layer output height
     */
    public int getOutputHeight(){
        return _neuron.getOutputHeight();
    }

     /**
     * calculates output as a sequence of neurons outputs
     */
    public Tensor calculateOutputFormat() {
        Tensor outputFormat = new Tensor(_neuron.getOutputDataFormat());
        /** TODO: check!*/
        // if(_neuronsNum>1 && (!(getInputConnections().firstElement().getSrc()._neuron instanceof Concat)))
        if(_neuronsNum>1)
            outputFormat.addDimension(_neuronsNum);
        return outputFormat;
    }

    /**
     * TODO: REFACTORING
     * Sort inputs for concatenation layer
     */
    public void sortInputsInConcatOrder(){
        if(!(_neuron instanceof Concat))
            return;
        ((Concat)_neuron).sortInputsInConcatOrder();

        Vector<Connection> sortedInputConnections = new Vector<>();
        boolean sortedSuccessfully = true;

        for(Layer sortedInput: ((Concat)_neuron).getInputOwners()){
            Connection inpCon = getInputConnection(sortedInput.getId());
            if(inpCon!=null)
                sortedInputConnections.add(inpCon);
            else {
                System.err.println("Connections sort error: connection " +
                        sortedInput.getName() + "-->" + getName() + " not found!");
                sortedSuccessfully = false;
            }
        }

        if(sortedSuccessfully)
            _inputConnections = sortedInputConnections;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   Operator                               ////
    /**
     * Init operator: Description of DNN layer functionality
     * Should be performed after all DNN model parameters are established
     * and DNN data formats are calculated
     */
    public void initOperator() {

        if (_neuron.getOperator() == null) {
            Operator op = new Operator(_neuron._name);
            _neuron.setOperator(op);
        }

        TreeMap<String, Integer> intparams = _neuron._operator.getIntParams();
        TreeMap<String, Tensor> tensorParams = _neuron._operator.getTensorParams();
        int inputChannels = getInputChannels();
        /** TODO: refactoring*/
    //    if(_neuron instanceof DenseBlock)


        _neuron.initOperator(getInputChannels(), getOutputChannels());
        sortInputsInConcatOrder();

        //_neuron._addInputToOpPar(_inputDataFormat);
        //_neuron._addOutputToOpPar(_outputDataFormat);

       if (!(_neuron instanceof GenericNeuron)) {
            intparams.put("neuron_start_id", getstartNeuronId());
            intparams.put("neurons", getOutputChannels());
            _setOperatorPads();
            if (!tensorParams.containsKey("bias"))
                tensorParams.put("bias", new Tensor());
            _neuron._operator.setName(getFunctionCallDescription(getInputChannels()));
            _initDarknetParams();

            if(_neuron.getNonlin()!=null)
                _neuron.getOperator().addStringParam("nonlin", _neuron.getNonlin());
           }

        /** auto-set partitions number =1*/
        if(intparams.get("partitions")==null)
            intparams.put("partitions",1);

       _setGPUUseTemplate();
       _addIODimsAndLen();

       _neuron.setOperatorTimeComplexity(getInputChannels(), getOutputChannels());
      // System.out.println(getName()+" operator init!");
    }

    private void _addIODimsAndLen(){
     TreeMap<String,Integer> intParams = _neuron._operator.getIntParams();
     Neuron neuron = _neuron;

     Integer inputSampleDim = _neuron.getSampleDim();
     Integer outputSampleDim = _neuron.getSampleDim();
     Tensor inputDataFormat = _inputDataFormat;
     Tensor outputDataFormat = _outputDataFormat;

     /** process generic*/
      if(_neuron instanceof  GenericNeuron){
         Network internalStructure = ((GenericNeuron) _neuron).getInternalStructure();
         neuron = internalStructure.getInputLayer().getNeuron();
         inputDataFormat = internalStructure.getInputLayer()._inputDataFormat;
         outputDataFormat = internalStructure.getOutputLayer()._outputDataFormat;
         inputSampleDim = neuron.getSampleDim();
         outputSampleDim = internalStructure.getOutputLayer()._neuron.getSampleDim();
     }

     /** setup input*/
     if(neuron instanceof MultipleInputsProcessor) {
         MultipleInputsProcessor muln = (MultipleInputsProcessor)neuron;
         for(Layer input: muln.getInputOwners()){
             _addIODimAndLenAsIntParams(input._outputDataFormat,input.getName());
         }
     }

     else {
         _addIODimAndLenAsIntParams(inputDataFormat,"input");
         intParams.put("channels", getInputChannels());
         if(inputSampleDim>1) {
             intParams.put("h", getInputHeight());
             intParams.put("w", getInputWidth());
         }
     }

     /** setup output*/
     _addIODimAndLenAsIntParams(outputDataFormat,"output");
     if(outputSampleDim>1) {
             intParams.put("out_h", getOutpH());
             intParams.put("out_w", getOutpW());
         }
    }

    /**
     * Add I/O dims as tensor par
     * @param tensor tensor
     * @param name tensor name
     */
    private void _addIODimAndLenAsIntParams(Tensor tensor,String name){
        if(Tensor.isNullOrEmpty(tensor))
            return;

        TreeMap<String,Integer> intParams = _neuron._operator.getIntParams();

        for(int i=0; i<tensor.getDimensionality(); i++)
            intParams.put(name + "_dim"+i, tensor.getDimSize(i));

        intParams.put(name +"_len",tensor.getElementsNumber());
    }

    //DARKNET special params
    /** TODO might be changed!*/
    private void _initDarknetParams(){
        TreeMap<String,Integer> intParam = _neuron._operator.getIntParams();
         intParam.put("groups", 1);
         intParam.put("batch", 1);
        // intParam.put("batchnormalize", 0);
        // intParam.put("xnor",0);

    }

    /** Process operator pads*/
    private void _setOperatorPads(){
        if(!(_neuron instanceof CNNNeuron))
            return;

        CNNNeuron cnnNeuron = (CNNNeuron)_neuron;
        TreeMap<String,Integer> intParam = _neuron._operator.getIntParams();
        TreeMap<String,Tensor>  tensorParams = _neuron._operator.getTensorParams();
        int [] pads;
        if(_pads!=null)
            pads = _pads;
        else pads = new int[4];

        if(cnnNeuron instanceof Convolution && cnnNeuron.getBoundaryMode().equals(BoundaryMode.SAME))
             pads = _generateSameAutoPads(cnnNeuron, _pads);

         if(_NullOrZeroPads(pads))
             intParam.put("pads",0);

         else{
                 intParam.put("pads",1);
                 intParam.put("pad_w0", pads[0]);
                 intParam.put("pad_h0", pads[1]);
                 intParam.put("pad_w1", pads[2]);
                 intParam.put("pad_h1", pads[3]);

                 /**int envW = getInpW() + pads[0] + pads[2];
                 int enwH = getInpH() + pads[1] + pads[3];
                 int envC = getInputChannels();
                 Tensor envidedInput = new Tensor(envW,enwH,envC);
                 tensorParams.put("envided",envidedInput);*/
             }
    }

    /**
     * In real applications the SAME boundary mode is
     * imitated as VALID boundary mode + special pads
     * @param x CNN neuron
     * @param defaultPads pads, that layer already have
     * @return autopads, imitating the SAME boundary mode
     */
    private int [] _generateSameAutoPads(CNNNeuron x, int[] defaultPads){
        int x_autopad = (((x.getInputWidth() * (x.getStride()-1) + x.getKernelW()))/x.getStride() - 1)/2;
        int y_autopad = (((x.getInputHeight() * (x.getStride()-1) + x.getKernelH()))/x.getStride() - 1)/2;

        if(defaultPads==null)
        { int pads[] = {x_autopad,y_autopad,x_autopad,y_autopad};
            return pads;
        }

        {
            defaultPads[0] += x_autopad;
            defaultPads[1] += y_autopad;
            defaultPads[2] += x_autopad;
            defaultPads[3] += y_autopad;
            return defaultPads;
        }

    }

       /**
     * if all values in pad are equal to zero
     */
    private boolean _NullOrZeroPads(int pads[]){
        if(pads == null)
            return true;

        for(int i=0; i<4;i++){
            if(pads[i]!=0)
                return false;
        }
        return true;
    }

    private void _setGPUUseTemplate(){
       // if(_neuron instanceof Convolution)
            _neuron._operator.getIntParams().put("gpu",-1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   consistency checkout                   ////

    /**
     * Checks if data formats are tent. Data formats are
     * not consistent if there are any null or '-' output values and
     * consistent otherwise.
     * @return true, if layer data format consistent and false otherwise
     */
    public boolean isDataFormatsConsistent(){

        if (Tensor.isNullOrEmpty(_inputDataFormat) ||
                Tensor.isNullOrEmpty(_outputDataFormat)) {
             //   System.out.println("Layer data formats consistency fault: " +
               //         getName() + " null data formats ");
                return true;
            }

        if(Tensor.containsZeroOrNegativeDims(_inputDataFormat) ||
                    Tensor.containsZeroOrNegativeDims(_outputDataFormat)) {
                System.out.println("Layer data formats consistency fault: " +
                        getName() + " zero or negative data formats ");
                return false;
            }

        if(_inputDataFormat.getDimensionality()<_neuron.getSampleDim()){
            System.out.println("input dimension mismatch ");
            return false;
        }

            return true;

        }
    ///////////////////////////////////////////////////////////////////
    ////            data formats details extraction               ////
    /**
     * TODO REFACTORING ON DENSEBLOCK INPUT CHANNELS NUMBER
     * TODO denseBlock input dimensionality might be>denseBlock output dimensionality,
     * TODO this is fair only for denseBlock
     * Get input channels number
     * @return input channels number*/

    public int getInputChannels(){
        Vector<Connection> inputConnections = getInputConnections();

        if(inputConnections.size()>0){
            Layer inpSrc = inputConnections.firstElement().getSrc();
            if(inpSrc._neuron instanceof Concat)
                return inpSrc.getOutputChannels();
        }

        if(_neuron instanceof  GenericNeuron)
            return ((GenericNeuron) _neuron).getInternalStructure().getInputLayer().getInputChannels();

         if(Tensor.isNullOrEmpty(_inputDataFormat)||Tensor.isNullOrEmpty(_neuron.getInputDataFormat()))
         if(Tensor.isNullOrEmpty(_inputDataFormat))
            return 0;

        /** TODO: check!*/
        if(_neuron instanceof DenseBlock) {
            int ch = _inputDataFormat.getElementsNumber() / _neuron.getInputDataFormat().getElementsNumber();
            return Math.max(ch, 1);
        }

        if(_inputDataFormat.getDimensionality()<3)
            return 1;

        return _inputDataFormat.getDimSize(2);
    }

     /**
     * Get output channels number
     * @return input channels number
     */
     public int getOutputChannels(){
         Tensor outputDataFormat = _outputDataFormat;

         if(_neuron instanceof GenericNeuron)
             outputDataFormat = ((GenericNeuron) _neuron).getInternalStructure().getOutputLayer()._outputDataFormat;

         if(outputDataFormat==null){
             System.err.println(_name + " output channels derivation error: output data format is not set!");
             return 1;
         }

        if(outputDataFormat.getDimensionality()<3)
            return 1;


         Integer outputH = getOutpH();
         Integer outputW = getOutpW();

        /**  if(_neuron instanceof GenericNeuron){
              outputH = ((GenericNeuron) _neuron).getInternalStructure().getOutputLayer().getOutpH();
              outputW = ((GenericNeuron) _neuron).getInternalStructure().getOutputLayer().getOutpW();
          }*/


       //System.out.println(_name + " output channels: "+ (outputDataFormat.getElementsNumber()/(outputH*outputW)) );

        return outputDataFormat.getElementsNumber()/(outputH*outputW);


       /** if(_neuron instanceof Concat)
            return _getConcatOutChannels((Concat)_neuron);


        if(_neuron instanceof ConnectionDependent) {
            Vector<Connection> inputConnections = getInputConnections();

            if (inputConnections.size() > 0) {
                Layer inpSrc = inputConnections.firstElement().getSrc();
                if (inpSrc._neuron instanceof Concat)
                    return inpSrc.getOutputChannels();
            }
        }

        return _neuronsNum;*/
    }

    /**
     * Get input data width
     * @return input data width
     */
    public int getInpW(){ return getInpDim(0); }

    /**
     * Get input data height
     * @return input data height
     */
    public int getInpH(){ return getInpDim(1); }

    /**
     * Get output data width
     * @return input data width
     */
    public int getOutpW(){ return getOutpDim(0); }

    /**
     * Get input data width
     * @return input data width
     */
    public int getOutpH(){ return getOutpDim(1); }

    /**
     * Return value of input data dimension (without channels)
     * @param dimId dimension Id
     * @return value of input data dimension (without channels)
     * if it is presented or 0 otherwise
     */
    public int getInpDim(int dimId){
        if(_neuron.getInputDataFormat().getDimensionality() > dimId)
        return _neuron.getInputDataFormat().getDimSize(dimId);
        else return 1;
    }

    /**
     * Return value of input data dimension (without channels)
     * @param dimId dimension Id
     * @return value of input data dimension (without channels)
     * if it is presented or 0 otherwise
     */
    public int getOutpDim(int dimId){
        if(_neuron.getOutputDataFormat().getDimensionality()> dimId)
        return _neuron.getOutputDataFormat().getDimSize(dimId);
        else return 1;
    }

    ///////////////////////////////////////////////////////////////////
    //// simple getters and setters for all private fields methods ////
    //// required for serialization/deserialization                ////
    public String getName() { return _name; }

    public void setName(String name) { this._name = name; }

    public Neuron getNeuron() { return _neuron; }

    public void setNeuron( Neuron neuron) { this._neuron = neuron; }

    public Tensor getInputFormat() { return _inputDataFormat; }

    /** get input data format, refined with pads*/
    public Tensor getInputFormatWithPads() {
        if(_pads==null || _inputDataFormat==null)
            return _inputDataFormat;
        if(_inputDataFormat.getDimensionality()<2)
            return _inputDataFormat;
        Tensor inputDataFormatWithPads = new Tensor(_inputDataFormat);
        inputDataFormatWithPads.setDimSize(0,inputDataFormatWithPads.getDimSize(0)+_pads[0]+_pads[2]);
        inputDataFormatWithPads.setDimSize(1,inputDataFormatWithPads.getDimSize(1)+_pads[1]+_pads[3]);
        return inputDataFormatWithPads;

    }

    public void setInputFormat(Tensor inputFormat) { this._inputDataFormat = inputFormat; }

    public Tensor getOutputFormat() {
    if(_neuron instanceof GenericNeuron)
        return ((GenericNeuron) _neuron).getInternalStructure().getOutputLayer().getOutputFormat();
        return _outputDataFormat;
    }

    public void setOutputFormat(Tensor outputFormat) { this._outputDataFormat = outputFormat; }


    /**
     * Get output channels for concat node
     * @param cn
     * @return
     */
    private Integer _getConcatOutChannels(Concat cn){

        int neurs = 0;
        int inpNeurs = 0;

            for (Layer inputOwner: cn.getInputOwners()) {
                if(inputOwner.getNeuron() instanceof Concat)
                    inpNeurs = _getConcatOutChannels((Concat)inputOwner.getNeuron());
                else
                    inpNeurs = inputOwner.getNeuronsNum();

                neurs += inpNeurs;
            }
            neurs = Math.max(neurs,1);

        //  System.out.println("Concat out channels: "+neurs);
          return neurs;
    }

    public int getNeuronsNum() { return _neuronsNum; }

    public void setNeuronsNum(int neuronsNum) { this._neuronsNum = neuronsNum; }

    public Integer getId() { return _id; }

    public void setId(int id) { this._id = id; }

    public Integer getstartNeuronId() { return _startNeuronId; }

    public void setstartNeuronId(Integer startNeuronId) { this._startNeuronId = startNeuronId; }

    public Vector<Connection> getInputConnections() {
        return _inputConnections;
    }

    /**
     * Get input connection
     * @param srcId input connection source id
     * @return input connection with id == srcId
     */
    public Connection getInputConnection(int srcId) {
        for(Connection con: _inputConnections) {
            if(con.getSrcId()==srcId)
                return con;
        }
        return null;
    }

    public void setInputConnections(Vector<Connection> inputConnections) {
        this._inputConnections = inputConnections;
    }

    public Vector<Connection> getOutputConnections() {
        return _outputConnections;
    }

    public void setOutputConnections(Vector<Connection> outputConnections) {
        this._outputConnections = outputConnections;
    }

    public void setAutopads(boolean autopads) {
        this._autopads = autopads;
    }

    public boolean isAutopads() {
        return _autopads;
    }

    /**
     * Get layer pads (dummy lines, extending input layer format)
     * @return layer pads (dummy lines, extending input layer format)
     */
    public int[] getPads() { return _pads; }

    /**
     * Check if pads can be ignored
     * @return true if pads can be ignored and false otherwise
     */
    public boolean isNullorEmptyPads(){
        if(_pads == null)
            return true;

        return _pads[0]==0 && _pads[1]==0 && _pads[2]==0 && _pads[3]==0;

    }

    /**
     * Set pads (dummy lines, extending input layer format)
     * @param x0 top left pad
     * @param x1 top right pad
     * @param y0 bottom left pad
     * @param y1 bottom right pad
     */
    public void setPads( int x0, int x1, int y0, int y1){
        if(_pads==null)
            _pads = new int[4];

        _pads[0] = x0;
        _pads[1] = x1;
        _pads[2] = y0;
        _pads[3] = y1;
    }


    ///////////////////////////////////////////////////////////////////
    ////           ONNX-data formats compatibility               ////

    /**
     * Generate auto pads in case of 'inconvenient' data formats,
    * where input data shapes are indivisible on min input data shape
    * Pads are values, added to the beginning and ending along each axis.
    * in format [x1_begin, x2_begin...x1_end, x2_end,...],
    * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
    * the number of pixels added at the end of axis `i`.
    * Pads should contain values >=0
     * NOTE: Data formats should be set up before auto pads generation
     */
    public void setAutoPads(){
        if(_neuron instanceof CNNNeuron){
            CNNNeuron cnnNeuron = (CNNNeuron)_neuron;
            if( cnnNeuron.autoPadsNeeded()){
                int[] pads = cnnNeuron.generateAutoPads();
                _pads = pads;
            }
        }
    }

    /**
     * Set layer pads (dummy lines, extending input layer format)
     * @param pads layer pads (dummy lines, extending input layer format)
     */
    public void setPads(int[] pads) { this._pads = pads; }

    /**
     * Resolve references inside the layer after the deserialization
     */
    public void resolveReferences() {
        if(_neuron instanceof ReferenceResolvable)
            ((ReferenceResolvable)_neuron).resolveReferences();
    }

         /**
     * Get function call description. If no execution code is
      * performed inside of the node, empty description is returned
     * By default, function call description is a name of a neuron
     * @return function call description */
     /** TODO: refactoring*/
    public String getFunctionCallDescription(int channels){
        if(_neuron instanceof Convolution){
            String neuronOpDesc = _neuron.getFunctionCallDescription(channels);
            String layerOpDesc = neuronOpDesc.replace(")","_" + _neuronsNum + ")");
            return layerOpDesc;
        }

        if(_neuron instanceof LRN){
            String neuronOpDesc = _neuron.getFunctionCallDescription(channels);
            String layerOpDesc = neuronOpDesc.replace(")","_" +
                    getInputFormat().getElementsNumber()+ ")");
            return layerOpDesc;
        }

        if(_neuron instanceof NonLinear){
            StringBuilder desc = new StringBuilder(_neuron.getName());
            desc.append("(");
            desc.append(getInputFormat().getElementsNumber());
            desc.append(")");
        return desc.toString();
        }



        else return _neuron.getFunctionCallDescription(channels);
    }

    /**************************************************
     **** POWER/PERFORMANCE/MEMORY evaluation
     *************************************************/

    public void set_memEval(double _memEval) { this._memEval = _memEval; }

    public void set_timeEval(double _timeEval) { this._timeEval = _timeEval; }

    public void set_energyEval(double _energyEval) { this._energyEval = _energyEval; }

    public double get_timeEval() {
        return _timeEval;
    }

    public double get_memEval() { return _memEval; }

    public double get_energyEval() { return _energyEval; }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**Unique layer identifier*/
    @SerializedName("id")private int _id;

    /**Layer neruon identifier in hight-level block (see block-based model)*/
    @SerializedName("startNeuronId")private Integer _startNeuronId = 0;

    /**name of the layer*/
    @SerializedName("name")private String _name;

    /**number of neurons in the layer*/
    @SerializedName("neuronsNum")private int _neuronsNum;

    /**typical neuron of the layer*/
    @SerializedName("neuron")private Neuron _neuron;

    /**layer input data format*/
    @SerializedName("inputDataFormat")private Tensor _inputDataFormat;

    /**layer output data format*/
    @SerializedName("outputDataFormat")private Tensor _outputDataFormat;

    /**Vector of references to input connections of the layer*/
    private transient Vector<Connection> _inputConnections = new Vector<Connection>();

    /**Vector of references to output connections of the layer*/
    private transient Vector<Connection> _outputConnections = new Vector<Connection>();

    /**
    * Pads are values, added to the beginning and ending along each axis.
    * in format [x1_begin, x2_begin...x1_end, x2_end,...],
    * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
    * the number of pixels added at the end of axis `i`.
    * Pads should contain values >=0
    */
    @SerializedName("pads")private int[] _pads = null;

    /** if autopads mode (VALID/FULL/SAME) is used.If autopads flag is false,
     *  the explicit pads definition is expected*/
    @SerializedName("autopads")private boolean _autopads = true;

    /** memory evaluation*/
    @SerializedName("mem_eval")private double _memEval = 0.0;

    /** time evaluation*/
    @SerializedName("time_eval")private double _timeEval = 0.0;

    /** energy evaluation*/
    @SerializedName("energy_eval")private double _energyEval = 0.0;

}
