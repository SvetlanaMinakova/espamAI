package espam.operations.evaluation.cnn.roofline;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.MeasurementUnitsConverter;
import espam.operations.evaluation.cnn.DNNComponentsTimeEvaluator;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;
public class DNNComponentsTimeEvaluatorRoofline extends DNNComponentsTimeEvaluator {

    /**
     * Class annotates every DNN layer with platform-aware time evaluation
     * The evaluation is performed using roofline model:
     * https://en.wikipedia.org/wiki/Roofline_model
     * */

    /** Create new roofline time evaluator
     * @param dnn dnn to evaluate
     * @param platformDescription description of platform, where dnn is executed
     * @param mapping mapping of DNN onto platform
     */
    public DNNComponentsTimeEvaluatorRoofline(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        super(dnn, platformDescription, mapping);
    }

    /** get evaluation of layer execution time*/
    protected Double _getLayerExecTime(Layer layer, MProcessor processor){
        ProcTypeDescription procTypeDescription = _platformDescription.getProcTypeDescription(processor.getName());
        Operator op = layer.getNeuron().getOperator();
        if(_isIODataOperator(op.getName()) || !_isOpTimeComplexityDefined(op))
            return 0.0;

        Double opTime =  getOperatorTimeEval(procTypeDescription, op);

        Double inputReadTime =_getInputDataReadTime(layer);
        Double outputWriteTime = _getOutputDataWriteTime(layer);
        Double weightsExchangeTime = _getWeightsExchangeTime(layer, processor);
        Double layerExecTime =  _maxValue(opTime, inputReadTime, outputWriteTime, weightsExchangeTime);
        return layerExecTime;
    }

    /**
     * Checks if operational complexity is defined for a CNN operator
     * @param op CNN operator
     * @return true, if operational complexity is evaluated and false otherwise
     */
    private boolean _isOpTimeComplexityDefined(Operator op){
        if(op==null)
            return false;
        if (op.getTimeComplexity()<=1)
            return false;
        return true;
    }


    /////////////////////////////////////////////////////////////////////////
    ////////////// operator execution time                      ////////////

    /**
     * Evaluate a CNN operator execution time, using benchmarking
     * @param procTypeDescription processor type
     * @param op operator
     * @return CNN operator execution time, obtained using benchmarking
     */
    public Double getOperatorTimeEval(ProcTypeDescription procTypeDescription, Operator op){
        Double occupancy = ProcOccupancyEvaluator.evalProcessorOccupancy(procTypeDescription, op);
        if (occupancy==0)
            return 0.0;

        Long opsToPerform = op.getTimeComplexity();
        Double GOPSToPerform = opsToPerform.doubleValue()/1e9;
        Double procMaxPerformanceGOPSperSec = procTypeDescription.getMaxPerformance() * occupancy;
        Double optimeSec = GOPSToPerform/procMaxPerformanceGOPSperSec;
        Double optimeMs = optimeSec *1e3;
        return optimeMs;
    }



    /////////////////////////////////////////////////////////////////////////
    //////////////           data transfer time                 ////////////

    /** get time, taken to exchange data with accelerator
     * @param layer layer
     * @param processor processor, on which layer is executed
     * @return time, taken to exchange data with accelerator
     */
    private Double _getWeightsExchangeTime(Layer layer, MProcessor processor){
        if(!_weightsTransfer || processor.isCPU()) //assuming that weights are always in CPU memory
            return 0.0;

        MProcessor dst = processor;
        long dataSizeInTokens = 0l;
        try { dataSizeInTokens = layer.getNeuron().getOperator().getMemoryComplexity(); }
        catch (Exception e) {System.err.println("Weights transfer time computation error: " +
                "memory complexity cannot be obtained for layer " + layer.getName()); }

        double paramTokenSize = MeasurementUnitsConverter.getTokenSizeInMegaBytes(_dataType);
        Double dataToTransferMB = (double)(dataSizeInTokens) * paramTokenSize;
        Double transferTimeS = _platformDescription.getWeightsTransferTime(dst.getName(), dataToTransferMB);
        double transferTimeMS = transferTimeS * (double)MeasurementUnitsConverter.secToMs();
       // System.out.println(layer.getName() + " " + dataToTransferMB + " MB to transfer = "+ transferTimeS + " s, " + transferTimeMS + " ms");

        return transferTimeMS;
    }

    /**
     * Get time, required by layer to read its input data
     * @param layer layer
     * @return time, required by layer to read its input data
     */
    protected Double _getInputDataReadTime(Layer layer){
        Double time = 0.0;
        for(Connection con: layer.getInputConnections())
            time = time + _getDataTransferTime(con);
        return time;
    }

    /**
     * Get time, required by layer to write its output data
     * @param layer layer
     * @return time, required by layer to write its output data
     */
    protected Double _getOutputDataWriteTime(Layer layer){
        Double time = 0.0;
        for(Connection con: layer.getOutputConnections())
            time = time + _getDataTransferTime(con);
        return time;
    }

    /**Evaluate DNN layer execution time*/
    protected Double _getDataTransferTime(Connection con){
        Tensor exchangeTensor = con.getSrc().getOutputFormat();
        try {
            MProcessor srcProc = _mapping.tryFindProcessorForTask(con.getSrcName());
            MProcessor dstProc = _mapping.tryFindProcessorForTask(con.getDestName());
            Double tokenSize = MeasurementUnitsConverter.getTokenSizeInMegaBytes(_dataType);
            Double time  = 0.0;
            time = _getDataExchangeTime(exchangeTensor,tokenSize, srcProc, dstProc);
            return time;
        }
        catch (Exception e) {
            //System.err.println("DNN-CSDF connection " + con.getSrcName() + " --> " + con.getDestName() + " time evaluation error: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Get time in seconds, required to communicate data between two processors
     * @param data data tensor
     * @param src source processor
     * @param dst destination processor
     * @return time in seconds, required to communicate data between two processors
     */
    private Double _getDataExchangeTime(Tensor data, double tokenSize, MProcessor src, MProcessor dst){
        int dataSizeInTokens = 0;
        if(!Tensor.isNullOrEmpty(data))
            dataSizeInTokens = data.getElementsNumber();

        Double dataToTransferMB = (double)(dataSizeInTokens) * tokenSize;
        double transferTimeS =  _platformDescription.getTransferTime(src.getName(),dst.getName(),dataToTransferMB);
        double transferTimeMS = transferTimeS * (double)MeasurementUnitsConverter.secToMs();

        // System.out.println(data.toString()+ " = " + dataToTransferMB + " MB, bandwidth = " + bandwidthMB + " MB/s, time = " + transferTimeS + " s, " +
        // transferTimeMS + " ms");
        return transferTimeMS;
    }

    /**
     * Find max value among given Double values
     * @param values Double values
     * @return max value among given Double values
     */
    protected Double _maxValue(Double ... values){
        Double maxValue = 0.0;
        for (Double value: values){
            maxValue = Math.max(maxValue, value);
        }
        return maxValue;
    }

    /////////////////////////////////////////////////////////////////////////
    ////////////// private variables                            ////////////
    protected boolean _weightsTransfer = true;
    protected boolean _ioDataTransfer = true;
}
