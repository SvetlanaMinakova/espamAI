package espam.datamodel.graph.cnn.neurons.cnn;/** * abstract class CNN CNNNeuron is an abstract representation for * Convolution and Pooling CNNNeurons, typical for Convolutional neural networks  */import com.google.gson.annotations.SerializedName;import espam.datamodel.EspamException;import espam.datamodel.graph.cnn.BoundaryMode;import espam.datamodel.graph.cnn.Layer;import espam.datamodel.graph.cnn.Neuron;import espam.datamodel.graph.csdf.datasctructures.Tensor;import espam.visitor.CNNGraphVisitor;/** * Describes one CNNNeuron Actor of the Neural Network */public class CNNNeuron extends Neuron {    ///////////////////////////////////////////////////////////////////    ////                         public methods                    ////    /**     * TODO replace by instance creator if reasonable     * Constructor to create empty CNNNeuron with default parameters:     * kernelSize = 1     * stride = 1     * sample dimension = 2     * name: empty     * input format: empty     * output format: empty     */    public CNNNeuron() {        setSampleDim(2);    }    /**     * Constructor to create an CNNNeuron with a name and set of default parameters     * @param name name of a CNNNeuron     * kernelSize = 1     * stride = 1     * sample dimension = 2     * input format: empty     * output format: empty     */    public CNNNeuron(String name) {        super(name);        setSampleDim(2);    }    /**     * Constructor to create an CNNNeuron with a name, a kernelSize and a stride (without internal memory)     * @param name name of a CNNNeuron     * @param kernelH kernelSize = kernel width = kernel height of a CNNNeuron     * @param kernelH kernelSize = kernel width = kernel height of a CNNNeuron     * default parameters:     * sample dimension = 1     * input format: empty     * output format: empty     */    public CNNNeuron(String name, int kernelW, int kernelH, int stride) {        setName(name);        setKernelW(kernelW);        setKernelH(kernelH);        setStride(stride);        setSampleDim(2);    }      /**     * Constructor to create an CNNNeuron with a name, a kernelSize and a stride (without internal memory)     * @param name name of a CNNNeuron     * @param kernelSize kernelSize of a CNNNeuron     * default parameters:     * sample dimension = 1     * input format: empty     * output format: empty     */    public CNNNeuron(String name, int kernelSize, int stride) {        setName(name);        setKernelH(kernelSize);        setKernelW(kernelSize);        setStride(stride);        setSampleDim(2);    }    /** Accept a Visitor     *  @param x A Visitor Object.     *  @exception EspamException If an error occurs.     */    public void accept(CNNGraphVisitor x) {        x.visitComponent(this);    }    /**     * Clone this neuron     * @return a new reference on the neuron     */    public CNNNeuron clone() {        CNNNeuron newObj = (CNNNeuron) super.clone();        newObj.setKernelH(_kernelH);        newObj.setKernelW(_kernelW);        newObj.setStride(_stride);        newObj.setBoundaryMode(_boundaryMode);        newObj.setDim(_dim);        newObj.setExpandH(_expandH);        newObj.setExpandW(_expandW);        return (newObj);    }     /**     * Create a deep copy of this CNNNeuron - creates fully new instance of the CNNNeuron     * @param n original CNNNeuron to be copied     */    public CNNNeuron (CNNNeuron n) {        super(n);        setKernelH(n._kernelH);        setKernelW(n._kernelW);        setStride(n._stride);        setBoundaryMode(n._boundaryMode);        setDim(n._dim);        setExpandH(n._expandH);        setExpandW(n._expandW);    }    /**     * Return string description of neuron's specific parameters     * @return string description of neuron's specific parameters     */    @Override    public String getStrParameters() {    StringBuilder strParams = new StringBuilder();        strParams.append("\n parameters: [\n");        strParams.append("k: " + _kernelW +"x"+_kernelH);        strParams.append(",\n  s: " + _stride);        strParams.append(",\n  boundary_mode: " + _boundaryMode + " ]");     return strParams.toString();    }    /**   * Compares CNN neuron to another object   * @param obj Object to compare this Neuron with   * @return true if Neuron is equal to the object and false otherwise   */    @Override    public boolean equals(Object obj) {        boolean isSuperParamsEqual = super.equals(obj);        if (isSuperParamsEqual) {            CNNNeuron neuron = (CNNNeuron)obj;              return this._kernelH == neuron._kernelH              &&         this._kernelH == neuron._kernelH              && this._stride == neuron._stride              && this._boundaryMode == neuron._boundaryMode;        }        return false;       }    ///////////////////////////////////////////////////////////////////    ////              data formats calculation and set up         ////     /**     * Automatically calculates the output format of a neuron     * @param inputDataFormat input data format description - tensor w x h x ...     * @return output data format of a neuron     */    @Override    public Tensor calculateOutputDataFormat(Tensor inputDataFormat) {        Tensor result = new Tensor();        if(inputDataFormat==null)            return result;        if(inputDataFormat.getDimensionality()<getSampleDim())            return result;        switch (getBoundaryMode()) {            case SAME: {                Tensor croppedInput = inputDataFormat.getSubTensor(getSampleDim());                return croppedInput;                }            /**             * TODO Check the formula for the FULL mode!                 * w and h dimension sizes increases according to a certain formula,                 */            case FULL: {                int w = inputDataFormat.getDimSize(0);                int h = inputDataFormat.getDimSize(1);                int newW = (w-1) * _stride + _kernelW;                int newH = (h-1) * _stride + _kernelH;                newW = Math.max(newW,1);                newH = Math.max(newH,1);                result.addDimension(newW);                result.addDimension(newH);              /** TODO CHECKOUT*/              if(_kernelD!=null && inputDataFormat.getDimensionality()>2)                    result.addDimension(inputDataFormat.getDimSize(2));                return result;            }            /**             * the boundary mode is VALID by default             * For VALID boundary mode, the input tensors with dimensions,             * indivisible on corresponding kernel dim are expanded by pads.             * Pads are values, added to the beginning and ending along each axis.             * in format [x1_begin, x2_begin...x1_end, x2_end,...],             * for current moment only 2D pads are supported. The format is             * [width_begin, height_begin,width_end, height_end]              */             default: {                /**                 * w and h dimension sizes decreases according to a certain formula,                 * other dimensions sizes (e.g. input sequence length) are saved                 */                int w = inputDataFormat.getDimSize(0);                int h = inputDataFormat.getDimSize(1);                int newW = (w - _kernelW)/_stride + 1;                int newH = (h - _kernelH)/_stride + 1;                newW = Math.max(newW,0);                newH = Math.max(newH,0);                if(autoPadsNeeded()){                    int[] pads = generateAutoPads();                    int wPad = pads[0]+pads[2];                    if(wPad>0 && newW>0){                        _expandW = wPad;                         newW++;                    }                    int hPad = pads[1]+pads[3];                    if(hPad>0 && newH>0){                        _expandH = hPad;                         newH++;                    }                }               // newW = Math.max(newW,1);               // newH = Math.max(newH,1);                result.addDimension(newW);                result.addDimension(newH);                /** TODO CHECKOUT*/                if(_kernelD!=null && inputDataFormat.getDimensionality()>2)                    result.addDimension(inputDataFormat.getDimSize(2));              //  result.setBatchSize(inputDataFormat.getBatchSize());                return result;                }        }    }    /**     * Calculate min output height for given min input height     * @param inputHeight given input height     * @return output height, calculated from the given input height     * using neuron parameters     */    private int calculateOutputHeight(int inputHeight) {        if(inputHeight<getMinInputH())            return 0;        if(inputHeight==getMinInputH())            return 1;        switch (getBoundaryMode()) {            case SAME: {               return inputHeight;            }            case FULL: {                int outH = (inputHeight - 1) * _stride + _kernelH;                return outH;            }            /**             * the boundary mode is VALID by default             */            default: {                /**                 * w and h dimension sizes decreases according to a certain formula,                 * other dimensions sizes (e.g. input sequence length) are saved                 * Calculations performed in double-format to save the calculations accuracy                 */                int outH = (inputHeight - _kernelH) / _stride + 1;                if(outH<1)                    return 0;                int hCalc = ((outH - 1) * _stride) + _kernelH;                if (outH > hCalc) {                    outH++;                }                return outH;            }        }    }     /**     * Automatically calculates the min input height of the neuron     * h dimension is changed according to an inverse formula of output DataFormat height calculation     * @param minOutputHeight min height on the neuron output     * @return new minimal  a neuron     */    @Override    public int calculateMinInputDataHeight( int minOutputHeight) {        switch (getBoundaryMode()) {            case SAME:                return Math.max(_kernelH,minOutputHeight);            case FULL:{            int calculated =  (minOutputHeight - _kernelH)/_stride + 1;            return Math.max(calculated,_kernelH);            }            /**the boundary mode is VALID by default*/            default: {             int calculated =  (minOutputHeight - 1) * _stride + _kernelH;             if(_expandH>0)                 calculated--;             return Math.max(calculated,_kernelH);            }        }    }     /**     * Set input data format of neuron     * @param inputDataFormat  input data format of the neuron     */    @Override    public void setInputDataFormat(Tensor inputDataFormat) {    super.setInputDataFormat(inputDataFormat);       // Tensor.updateChannelsNum(inputDataFormat);       // if(!Tensor.isNullOrEmpty(inputDataFormat))         //       setSampleDim(inputDataFormat.getChannelsNum());    }    /**     * Set new data formats for CNNNeuron     * @param inputDataFormat input data format     */    public void setDataFormats(Tensor inputDataFormat) {        setInputDataFormat(inputDataFormat);        setOutputDataFormat(calculateOutputDataFormat(inputDataFormat));    }    /**     * Add pads to input image     * @param neuronOwner layer, owns this neuron     * @param pads pads to be added to layer input     */    public void processPads(Layer neuronOwner, int[] pads){       Tensor extendedInput = Tensor.addPads(neuronOwner.getInputFormat(),pads);       neuronOwner.setInputFormat(extendedInput);    }    /**     * returns minimal height of input. for CNN neurons min height = k_size     * @return minimal height of input. for CNN neurons min height = k_size     */    public int getMinInputH() {        return _kernelH;    }    ///////////////////////////////////////////////////////////////////    ////                Getters and setters                       ////    /**     * Return the height of the kernel of the CNNNeuron     * @return the kernelSize of the CNNNeuron     */    public int getKernelH() { return _kernelH; }     /**     * Set the height of the kernel of the CNNNeuron     * @param kernelH the kernelSize of the CNNNeuron     */    public void setKernelH(int kernelH) {       _kernelH = kernelH;    }    /**     * TODO only for square kernels!     * Return the width of the kernel of the CNNNeuron     * @return the width of the kernel of the CNNNeuron     */    public int getKernelSize() {        return _kernelW;    }    /**     * TODO only for square kernels!     * Return the width of the kernel of the CNNNeuron     * @return the width of the kernel of the CNNNeuron     */    public void setKernelSize(int k_size) {        _kernelH = k_size;        _kernelW = k_size;    }    /**     * Return the width of the kernel of the CNNNeuron     * @return the width of the kernel of the CNNNeuron     */    public int getKernelW() { return _kernelW; }     /**     * Set the width of the kernel of the CNNNeuron     * @param kernelW the width of the kernel of the CNNNeuron     */    public void setKernelW(int kernelW) {       _kernelW = kernelW;    }    /**     * Get kernel depth (for 3D convs)     * @return kernel depth (for 3D convs)     */    public Integer getKernelD() {        return _kernelD;    }    /**     * Set kernel depth (for 3D convs)     * @param kernelD kernel depth (for 3D convs)     */    public void setKernelD(Integer kernelD) {        this._kernelD = kernelD;        if(_kernelD!=null)            _dim = 3;    }    /**     * Return the stride of Convolution     * @return the stride of Convolution     */    public int getStride() { return _stride; }     /**     * Set the stride of Convolution     * @param stride  the stride of Convolution     */    public void setStride(int stride) {        _stride = stride; }     /**     * Return the boundary processing mode of a Convolution Operation     * @return the boundary processing mode of a Convolution Operation     */    public BoundaryMode getBoundaryMode() { return _boundaryMode; }    /**     * Set the boundary processing mode of a Convolution Operation     * @param boundaryMode the boundary processing mode of a Convolution Operation     */    public void setBoundaryMode (BoundaryMode boundaryMode) {        _boundaryMode = boundaryMode;    }         /**     * Get number of dummy zero-filled lines, that should be added to input sample     * @return number of dummy zero-filled lines, that should be added to input sample     */    public int getExpandW() { return _expandW; }    /**     * Set number of dummy zero-filled lines, that should be added to input sample     * @param expandW number of dummy zero-filled lines, that should be added to input sample     */    public void setExpandW(int expandW) {        this._expandW = expandW;    }    /**     * Get number of dummy zero-filled lines, that should be added to input sample     * @return number of dummy zero-filled lines, that should be added to input sample     */    public int getExpandH() {        return _expandH;    }    /**     * Set number of dummy zero-filled lines, that should be added to input sample     * @param expandH  number of dummy zero-filled lines, that should be added to input sample     */    public void setExpandH(int expandH) {        this._expandH = expandH;    }    /**     * Get convolutional operation dimensionality must be equal 1, 2, or 3     * By default = 2. Examples:     * 2D for one 2D input image     * 3D for one RGB input image     * 3D for several 2D outputs, coming from another conv/pooling layer     * @return convolutional operation dimensionality     */    public int getDim() {        return _dim;    }    /**     * Set convolutional operation dimensionality must be equal 1, 2, or 3     * By default = 2. Examples:     * 2D for one 2D input image     * 3D for one RGB input image     * 3D for several 2D outputs, coming from another conv/pooling layer     * @return convolutional operation dimensionality     */    public void setDim(int dim) {        this._dim = dim;    }    ///////////////////////////////////////////////////////////////////    ////      SDFG transformation-related  public methods         ////    /**     * Get number of operations, could be implemented on the neurons current     * input data. If the input data format is null or empty,     * 0 operations is returned. For convolution/pooling neurons     * number of operations is calculated as number of convolutions/     * poolings of 2D images over all channels     * @return number of operations, could be implemented on the input data     */    @Override    public int getOperationsNumber(int channels){     Tensor outputDataFormat = getOutputDataFormat();     if(Tensor.isNullOrEmpty(outputDataFormat))        return 0;    if(outputDataFormat.getDimensionality()<getSampleDim())        return 0;    int opNum = 1;    for(int dim : outputDataFormat.getShape())        opNum *=dim;    return opNum;    }    /**     * Get number of input tokens for each operation, perfomed in a neuron.     * If the input data format is null or empty,0 tokens is returned.     * For neurons, always taking on input a single value, null-description is returned.     * For neurons, which performs shape transformation, null-description is returned.     * @return number of input tokens for each operation, perfomed in a neuron     */    @Override    public int getOperationTokensNumber(int channels){        return _kernelH * _kernelW * channels;    }     /**     * Get function call description. If no execution code is      * performed inside of the node, empty description is returned     * By default, function call description is a name of a neuron     * @return function call description */    public String getFunctionCallDescription(int channels){        StringBuilder desc = new StringBuilder(getName());        desc.append("(");        desc.append(_kernelW);        desc.append("_");        desc.append(_kernelH);        if(channels>1) {            desc.append("_");            desc.append(channels);        }        desc.append(")");        return desc.toString();    }     /**     * TODO scale!     * Calculate number of function calls inside of the neuron     * with current data formats     * As every neuron produces 1 value on output, function     * calls number for every simple neuron = its output height     * @return number of function calls inside of the neuron     * TODO refactoring     */     @Override    public int getFuncCallsNum(int minInputH){        int funcCallsNum = getOutputH();        if(minInputH>getMinInputH()){            int scale = calculateOutputHeight(minInputH);            int datatail = 0;            if(funcCallsNum%scale!=0)                datatail=1;            funcCallsNum=funcCallsNum/scale + datatail;        }        return funcCallsNum;    }    ///////////////////////////////////////////////////////////////////    ////           ONNX-data formats compatibility               ////    /**     * Generate auto pads in case of 'inconvenient' data formats,    * where input data shapes are indivisible on min input data shape    * Pads are values, added to the beginning and ending along each axis.    * in format [x1_begin, x2_begin...x1_end, x2_end,...],    * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,    * the number of pixels added at the end of axis `i`.    * Pads should contain values >=0     * NOTE: Data formats should be set up before auto pads generation     */    public int[] generateAutoPads(){        Tensor inputDataFormat = getInputDataFormat();        int inpW = inputDataFormat.getDimSize(0);        int inpH = inputDataFormat.getDimSize(1);        int outpW = (inpW - _kernelW)/_stride + 1;        int outpH = (inpH - _kernelH)/_stride + 1;        int wCalc = ((outpW-1)*_stride) + _kernelW;        int hCalc = ((outpH-1)*_stride) + _kernelH;        int wDif = Math.abs(inpW-wCalc);        int hDif = Math.abs(inpH-hCalc);        int x0 = wDif/2;        int y0 = hDif/2;        int x1 = wDif - x0;        int y1 = hDif - y0;        int pads[] = {x0,y0,x1,y1};        return pads;    }    /**     * Check, if pads are needed to provide formats 'convenient'     * for CNN neuron     * @return true, if pads are needed and false otherwise     */    public boolean autoPadsNeeded(){        Tensor inputDataFormat = getInputDataFormat();        int inpW = inputDataFormat.getDimSize(0);        int inpH = inputDataFormat.getDimSize(1);        int outpW = (inpW - _kernelW)/_stride + 1;        int outpH = (inpH - _kernelH)/_stride + 1;        int wCalc = ((outpW-1)*_stride) + _kernelW;        int hCalc = ((outpH-1)*_stride) + _kernelH;        return (inpW!=wCalc) || (inpH!=hCalc);    }    ///////////////////////////////////////////////////////////////////////    ////                         private variables                    ////    /**     * for more information see     * TODO reference to AlexNet paper     * convolutional operation dimensionality must be equal 1, 2, or 3     * By default = number of input channels. Examples:     * 2D for one 2D input image     * 3D for one RGB input image     * 3D for several 2D outputs, coming from another conv layer     */    @SerializedName("dim")private int _dim = 2;    /**     * Convolutional kernel width     * for more information see     * TODO reference to Yan LeCun's paper     */    @SerializedName("k_h")private int _kernelH = 1;    /**     * Convolutional kernel height     * for more information see     * TODO reference to Yan LeCun's paper     */    @SerializedName("k_w")private int _kernelW = 1;     /**     * Convolutional kernel depth (3d dimension for 3D convs):     * TODO implementation for 3d convs     * for more information see     * TODO reference to 3D-convs paper/documenantion     * TODO process 3D convolutions     */    @SerializedName("k_d")private Integer _kernelD = null;    /**     * stride is a step with which     * the sliding window passes the input matrix     * for more information see     * TODO reference to Yan LeCun's paper     */    @SerializedName("stride")private int _stride = 1;    /**     * Mode of method for processing the bounds of an input tensor     */    @SerializedName("boundaryMode")private BoundaryMode _boundaryMode = BoundaryMode.VALID;    /**     * Number of dummy zero-filled lines, that should be added to input sample     */    @SerializedName("expandH")private int _expandH = 0;    /**     * Number of dummy zero-filled rows, that should be added to input sample     */    @SerializedName("expandW")private int _expandW = 0;}