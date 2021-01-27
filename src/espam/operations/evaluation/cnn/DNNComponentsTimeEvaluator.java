package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.cnn.benchmarkbased.BenchmarkHelper;
import espam.operations.evaluation.platformDescription.ProcTypeDescription;
import espam.operations.evaluation.platformDescription.OpBenchmark;
import espam.operations.evaluation.platformDescription.PlatformDescription;

/**
 * Abstract class, which annotates every DNN layer with platform-aware time evaluation,
 * performed by an abstract evaluation algorithm.
 * Every instance of this class should implement an evaluation algorithm.
 */
public abstract class DNNComponentsTimeEvaluator {
    /**
     * Create new dnn components time evaluator
     * @param dnn dnn
     * @param platformDescription description of platform, on which dnn is executed
     * @param mapping mapping of dnn onto platform
     */
    public DNNComponentsTimeEvaluator(Network dnn, PlatformDescription platformDescription, Mapping mapping){
        _dnn = dnn;
        _platformDescription = platformDescription;
        _mapping = mapping;
        _dataType = dnn.getDataType();
        _paramType = dnn.getWeightsType();
    }

    /** annotate DNN layers with time evaluation*/
    public void annotate(){
        Double layerTime;
        for (Layer layer : _dnn.getLayers()) {
            try {
                MProcessor processor = _mapping.findProcessorForTask(layer.getName());
                //operator execution time
                layerTime = _getLayerExecTime(layer, processor);
                layer.set_timeEval(layerTime);
            }

            catch (Exception e) {
                System.err.println("DNN-CSDF layer " + layer.getName() + " time evaluation error: " + e.getMessage());
            }
        }
    }

    /** get evaluation of layer execution time*/
    protected abstract Double _getLayerExecTime(Layer layer, MProcessor processor);

    /**
     * Checks if operator belongs to I/O data layer
     * @param opName operator name
     * @return true, if operator belongs to I/O data layer and false otherwise
     */
    protected boolean _isIODataOperator(String opName){
        if(opName.toLowerCase().equals("read")|| opName.toLowerCase().equals("write"))
            return true;
        return false;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         public print methods               ////

    /** print DNN, annotated with time (without mapping)
     * @param dnn DNN
     */
    public void printDNNTimeAnnotation(Network dnn){
        System.out.println("LAYERS: ");
        for(Layer layer: dnn.getLayers()){
            System.out.println(layer.getName()+ ": " + layer.get_timeEval() + " ms " );
        }

        System.out.println("////////////////////////////////////////////////");
        System.out.println("CONNECTIONS: ");

        for (Connection con: dnn.getConnections()){
            System.out.println(con.getSrcName() + " --> " + con.getDestName() +" : " + con.get_timeEval()+ " ms");
        }

    }

    /** print DNN, annotated with time with mapping
     * @param dnn DNN
     */
    public void printDNNTimeAnnotationWithMapping(Network dnn){
        System.out.println("LAYERS: ");
        MProcessor proc;
        for(Layer layer: dnn.getLayers()){
            proc = _mapping.tryFindProcessorForTask(layer.getName());

            System.out.print(layer.getName()+" (");
            if(proc!=null)
                System.out.print(proc.getName());

            System.out.println(") [ op: " + layer.get_timeEval() + " ms ]");
        }
        System.out.println("////////////////////////////////////////////////");
        System.out.println("CONNECTIONS: ");

        MProcessor srcProc, dstProc;
        for (Connection con: dnn.getConnections()){
            srcProc = _mapping.tryFindProcessorForTask(con.getSrcName());
            dstProc = _mapping.tryFindProcessorForTask(con.getDestName());

            System.out.print(con.getSrcName()+" (");
            if(srcProc!=null)
                System.out.print(srcProc.getName());
            System.out.print(") --> " + con.getDestName() + " (");
            if(dstProc!=null)
                System.out.print(dstProc.getName());
            System.out.println(") = "+con.get_timeEval()+ " ms");

        }

    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    protected PlatformDescription _platformDescription;
    protected Mapping _mapping;
    protected Network _dnn;
    protected boolean _verbose = false;

    protected String _dataType = "float";
    protected String _paramType = "float";
    protected boolean _toMB = true;
}
