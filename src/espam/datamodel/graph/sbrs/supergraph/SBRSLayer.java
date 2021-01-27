package espam.datamodel.graph.sbrs.supergraph;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.graph.sbrs.control.Parameter;
import espam.datamodel.graph.sbrs.control.ParameterName;
import espam.datamodel.graph.sbrs.control.ParameterValue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * SBRS Layer models a CNN layer, fully or partially reused among scenarios
 * of a CNN-based application.
 * NOTE: adaptive operator and adaptive hyperparameters are not fully supported!
 */
public class SBRSLayer {

    ///////////////////////////////////////////////////////////////////
    ////                         public functions                 ////

    /**Constructor to create new SBRS Layer from a CNN layer*/
    public SBRSLayer(Layer parent, Vector<ParameterName> adaptiveSBRSMOC) {
        _parent = parent;
        _adaptiveParameters = new HashMap<>();
        _adaptive = adaptiveSBRSMOC;
    }

    /*********************************************************************
     *************** REUSE AND ADAPTATION           **********************/

    /**
     * Reuse this SBRS MoC layer to represent functionality of
     * a new scenario layer. Represent sbrs layer op, hyp, and par
     * as adaptive parameters, if required
     * @param scenarioLayer new scenario layer to represent
     */
    public void reuseForLayer(Layer scenarioLayer){
        //System.out.println("REUSE sbrs moc layer " + _parent.getName()+ " for scenarioLayer " + scenarioLayer.getName());

        _reuseForLayerIO(scenarioLayer);
        _reuseForLayerOp(scenarioLayer);
        _reuseForLayerHyp(scenarioLayer);
        _reuseForLayerPar(scenarioLayer);
    }

    private void _reuseForLayerIO(Layer scenarioLayer){
        // parametrize input data tensor
        if (!_equalInputDataFormat(scenarioLayer)){
            if(!isIParametrized()){
                _parametrizeI();
                Tensor value = _parent.getInputFormat();
                _addInputDataparameterValue(value);
            }
            Tensor value = scenarioLayer.getInputFormat();
            _addInputDataparameterValue(value);
        }

        // parametrize output data format
        if(!_equalOutputDataFormat(scenarioLayer)){
            if(!isOParametrized()){
                _parametrizeO();
                Tensor value = _parent.getOutputFormat();
                _addOutputDataparameterValue(value);
            }
            Tensor value = scenarioLayer.getOutputFormat();
            _addOutputDataparameterValue(value);
        }
    }

    private void _reuseForLayerOp(Layer scenarioLayer){
        if (!_equalOp(scenarioLayer)){
            if(!isOpParametrized())
                _parametrizeOp();
        }
    }

    private void _parametrizeOp(){
        _adaptiveParameters.put(ParameterName.OP, new Parameter(ParameterName.OP));
    }

    private void _parametrizeI(){
        _adaptiveParameters.put(ParameterName.I, new Parameter(ParameterName.I));
    }

    private void _parametrizeO(){
        _adaptiveParameters.put(ParameterName.O, new Parameter(ParameterName.O));
    }

    private void _reuseForLayerHyp(Layer scenarioLayer){
        if (!_equalHyp(scenarioLayer)) {
            if(!isHypParametrized())
                _parametrizeHYP();
        }
    }

    private void _parametrizeHYP(){
        _adaptiveParameters.put(ParameterName.HYP, new Parameter(ParameterName.HYP));
    }

    private void _reuseForLayerPar(Layer scenarioLayer){
        if (!_equalPar(scenarioLayer)) {
            if(!isParParametrized())
                _parametrizePar();

            TreeMap<String, Tensor> value = scenarioLayer.getNeuron().getOperator().getTensorParams();
            _addTrainableparameterValue(value);
            //System.out.println("PARAM of " + scenarioLayer.getName() + " ADDED TO sbrs layer " + _parent.getName());
        }
    }

    private void _parametrizePar(){
        //System.out.println(_parent.getName() + " PARAM PARAMETRIZED");
        Parameter trainablePar = new Parameter(ParameterName.PAR);
        _adaptiveParameters.put(ParameterName.PAR, trainablePar);
        TreeMap<String, Tensor> value = _parent.getNeuron().getOperator().getTensorParams();
        _addTrainableparameterValue(value);
    }

    private void _addTrainableparameterValue(TreeMap<String, Tensor> trainableParValue){
        ParameterValue value = new ParameterValue(trainableParValue);
        _adaptiveParameters.get(ParameterName.PAR).addParameterValue(value);
    }

    private void _addInputDataparameterValue(Tensor dataTensor){
        ParameterValue value = new ParameterValue(dataTensor);
        _adaptiveParameters.get(ParameterName.I).addParameterValueDistinct(value);
    }

    private void _addOutputDataparameterValue(Tensor dataTensor) {
        ParameterValue value = new ParameterValue(dataTensor);
        _adaptiveParameters.get(ParameterName.O).addParameterValueDistinct(value);
    }

        public void parametrizeIO(){
        //_adaptive.add(ParameterName.I);
        //_adaptive.add(ParameterName.O);
    }

    /*********************************************************************
    *************** COMPARISON AND CHECKS           **********************/

    /**
     * Compares SBRSLayer  with another object
     * @param obj Object to compare this SBRSLayer with
     * @return true if SBRSLayer is equal to the object and false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

        SBRSLayer layer = (SBRSLayer)obj;
        return layer.getParent().equals(getParent()) &&
                layer.isOpParametrized() == isOpParametrized() &&
                layer.isHypParametrized() == isHypParametrized() &&
                layer.isParParametrized() == isParParametrized() &&
                layer.isIOParametrized() == isIOParametrized();
    }

    /**
     * Compares this SBRS MoC layer with a given scenario layer
     * to decide, whether the layer or the SBRS MoC can be
     * used to represent the scenario layer
     * @param scenarioLayer layer of the scenario
     * @return true, if layer or the SBRS MoC can be used to
     * represent the scenario layer
     */
    public  boolean reusableFor(Layer scenarioLayer){
        if (_reusableOp(scenarioLayer)) {
            if (_reusableHyp(scenarioLayer)){
                if (_reusablePar(scenarioLayer)) {
                    return true;
                }
                else return false;
            }
            else return false;
        }
        else return false;
    }

    private boolean _reusableOp(Layer scenarioLayer){
        boolean reusable = _adaptive.contains(ParameterName.OP) || _equalOp(scenarioLayer);
        return reusable;
    }

    private boolean _equalInputDataFormat(Layer scenarioLayer){
            boolean imatch = _parent.getInputFormat().equals(scenarioLayer.getInputFormat());
            return imatch;
    }

    private boolean _equalOutputDataFormat(Layer scenarioLayer){
        boolean omatch = _parent.getOutputFormat().equals(scenarioLayer.getOutputFormat());
        return omatch;
    }

    private boolean _equalOp(Layer scenarioLayer){
        boolean opsMatch = _parent.getNeuron().getName() == scenarioLayer.getNeuron().getName();
        return opsMatch;
    }


    private boolean _reusableHyp(Layer scenarioLayer){
        boolean reusable = _adaptive.contains(ParameterName.HYP) || _equalHyp(scenarioLayer);
        return reusable;
    }

    boolean _equalHyp(Layer scenarioLayer){
        boolean hypsMatch = true;
        if (_parent.getNeuron() instanceof CNNNeuron){
            CNNNeuron sbrsCNNNeuron = (CNNNeuron) _parent.getNeuron();
            CNNNeuron scenarioCNNNeuron = (CNNNeuron) scenarioLayer.getNeuron();
            boolean kSizeMatch = sbrsCNNNeuron.getKernelH() == scenarioCNNNeuron.getKernelH();
            boolean strideMatch = sbrsCNNNeuron.getStride() == scenarioCNNNeuron.getStride();
            boolean padsMatch = _equalPads(scenarioLayer);
            hypsMatch = kSizeMatch && strideMatch && padsMatch;
        }
        return hypsMatch;
    }


    private boolean _equalPads(Layer scenarioLayer){
        int [] sbrsPads = _parent.getPads();
        int [] scenarioPads = scenarioLayer.getPads();

        // both pads are null
        if (sbrsPads==null && scenarioPads == null)
            return true;

        // one of the pads is null and another is not null
        if (sbrsPads == null || scenarioPads == null)
            return false;

        //both pads are not null
        for (int i=0; i<4; i++){
            if (sbrsPads[i]!=scenarioPads[i])
                return false;
        }
        return true;
    }

    private boolean _reusablePar(Layer scenarioLayer){
        boolean reusable = _adaptive.contains(ParameterName.PAR) || _equalPar(scenarioLayer);
        return reusable;
    }

    /**TODO: introduce reusable parameters for GA*/
    private boolean _equalPar(Layer scenarioLayer){
        Operator sbrsOp = _parent.getNeuron().getOperator();
        Operator scenarioOp = scenarioLayer.getNeuron().getOperator();

        //process null-parameters
        if(sbrsOp==null || scenarioOp==null){
            if (sbrsOp!=null || scenarioOp!=null)
                return false;
            return true;
        }

        //process empty parameters
        if (!sbrsOp.hasTensorParams() || !scenarioOp.hasTensorParams()){
            if (sbrsOp.hasTensorParams() || scenarioOp.hasTensorParams())
                return false;
            return true;
        }

        //process non-null and non-empty parameters
        TreeMap<String, Tensor> sbrsTensorParam = sbrsOp.getTensorParams();
        TreeMap<String, Tensor> scenarioTensorParam = scenarioOp.getTensorParams();

        if (sbrsTensorParam.size() != scenarioTensorParam.size())
            return false;

        for (Map.Entry<String, Tensor> sbrsParam : sbrsTensorParam.entrySet()){
            if (scenarioTensorParam.keySet().contains(sbrsParam.getKey())){
                Tensor sbrsParamValue = sbrsParam.getValue();
                Tensor scenarioParamValue = scenarioTensorParam.get(sbrsParam.getKey());
                if (!sbrsParamValue.equals(scenarioParamValue))
                    return false;
            }
            else return false;
        }

        return true;
    }

    public boolean isOpParametrized(){
        boolean opParametrized = _adaptiveParameters.containsKey(ParameterName.OP);
        return opParametrized;
    }

    public boolean isHypParametrized(){
        boolean hypParametrized = _adaptiveParameters.containsKey(ParameterName.HYP);
        return hypParametrized;
    }

    public boolean isParParametrized(){
        boolean parParametrized = _adaptiveParameters.containsKey(ParameterName.PAR);
        return parParametrized;
    }

    public boolean isIOParametrized(){
        return isIParametrized()||
                isOParametrized();
    }

    public boolean isIParametrized(){
        return _adaptiveParameters.containsKey(ParameterName.I);
    }

    public boolean isOParametrized(){
        return _adaptiveParameters.containsKey(ParameterName.O);
    }


    ///////////////////////////////////////////////////////////////////
    ////                    getters and setters                   ////

    public Layer getParent(){ return _parent; }

    public String getName(){ return _parent.getName();}

    public Parameter getAdaptiveTrainableParameters(){
        if(!isParParametrized())
            return null;
        Parameter par = _adaptiveParameters.get(ParameterName.PAR);
        return par;
    }
    /**************************************************
     **** Print
     *************************************************/

    public void printDetails(){
            Layer layer = _parent;
            System.out.print("  " + layer.getName());

            //operator
            if (isOpParametrized()) {
                System.out.print("( op: adaptive");
            } else {
                System.out.print("( op: " + layer.getNeuron().getName());
            }

            // hyper-parameters
            if (isHypParametrized()) {
                System.out.print(", hyp: adaptive");
            } else {
                System.out.print(", hyp: " + "{");
                if (layer.getNeuron() instanceof CNNNeuron) {
                    System.out.print("k: " + ((CNNNeuron) layer.getNeuron()).getKernelW() +
                            ", s: " + ((CNNNeuron) layer.getNeuron()).getStride());
                    if(layer.getPads()!=null){
                        System.out.print(", pads: " + layer.getSTRpads());
                    }
                }
                System.out.print(" }");
            }

            //trainable parameters
            if (isParParametrized()) {
                System.out.print(", par: adaptive )");
            } else {
                System.out.print(", par: " + "{");
                if (layer.getNeuron().getOperator() != null) {
                    if (layer.getNeuron().getOperator().hasTensorParams()) {
                        for (Map.Entry<String, Tensor> tensorParam : layer.getNeuron().getOperator().getTensorParams().entrySet()) {
                            if (!Tensor.isNullOrEmpty(tensorParam.getValue()))
                                System.out.print(tensorParam.getKey() + ": " + tensorParam.getValue() + ", ");
                        }
                    }
                }
                System.out.print("}");
            }

            //i/o data

            // input data
            if (isIParametrized()){ System.out.print(", i_data: adaptive "); }
            else {
                if (layer.getNeuron() instanceof MultipleInputsProcessor) {
                    System.out.print(" [ ");
                    for (Tensor iData : ((MultipleInputsProcessor) layer.getNeuron()).getInputs())
                        System.out.print(iData + ", ");
                    System.out.print(" ]");
                } else {
                    System.out.print(", i_data: " + layer.getInputFormat());
                }
            }

            // output data
            if (isOParametrized()){ System.out.print(", o_data: adaptive)"); }
            else { System.out.print(", o_data: " + layer.getOutputFormat() + ")"); }
            System.out.println();

            //adaptive parameters:
            if (_adaptiveParameters.size()>0) {
                System.out.println("    adaptive: ");
                if (isParParametrized()) {
                    System.out.print("      parameters: ");
                    _printAdaptiveValues(ParameterName.PAR);
                }
                if (isIParametrized()){
                    System.out.print("      input data shape: ");
                    _printAdaptiveValues(ParameterName.I);
                }
                if (isOParametrized()){
                    System.out.print("      output data shape: ");
                    _printAdaptiveValues(ParameterName.O);
                }
            }
        }

        private void _printAdaptiveValues(ParameterName parameterName){
            Parameter par = _adaptiveParameters.get(parameterName);
            par.printValues();
            System.out.println();

        }


        ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /** A CNN layer, used for creation of the SBRS MoC layer*/
    @SerializedName("parent")private Layer _parent;

    /** A list of possible adaptive parameters
     */
    @SerializedName("adaptiveMoC")private Vector<ParameterName> _adaptive;

    /**A list of run-time parameters and their values,
     *  associated with the SBRS Layer*/
    private transient HashMap<ParameterName, Parameter> _adaptiveParameters;
}
