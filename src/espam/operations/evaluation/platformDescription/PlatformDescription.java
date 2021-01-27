package espam.operations.evaluation.platformDescription;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

public class PlatformDescription {

    /////////////////////////////////////////////////////////////////////////
    //////////////  public methods                              ////////////

    public ProcTypeDescription getProcTypeDescription(String procname){
        for(ProcDescription p : _procDescriptions){
            if(p.get_name().equals(procname)) {
                ProcTypeDescription c = _getCoreTypeEval(p.get_type(), p.get_subtype());
                if(c!=null)
                    return c;
            }
        }
        System.err.println("Time eval error: time evalution for processor: " + procname + " not found");
        return null;
    }

    public ProcDescription getProcDescription(String procname){
        for(ProcDescription p : _procDescriptions){
            if(p.get_name().equals(procname)) {
                return p;
            }

        }
        System.out.println("Processor evaluation: " + procname + " not found");
        return null;
    }

    /**get time, required to transfer data in seconds
     * @param srcName name of the source processor
     * @param dstname name of the destination processor
     * @return time, required to transfer one data element src processor --> dst processor
     * @param dataMB amount of data to transfer, in Megabytes
     * @return data to transfer in megabytes
     */
    public double getTransferTime(String srcName, String dstname, Double dataMB){
        double bandwidth = getBandwidth(srcName, dstname);
        if (bandwidth <= 0) return 0;
        return dataMB/bandwidth;
    }


    /**get time, required to transfer weights in seconds
     * @param dstname name of the destination processor
     * @return time, required to transfer one data element src processor --> dst processor
     * @param dataMB amount of data to transfer, in Megabytes
     * @return data to transfer in megabytes
     */
    public double getWeightsTransferTime(String dstname, Double dataMB){
        double bandwidth = getWeightsBandwidth(dstname);
        if (bandwidth <= 0) return 0;
        return dataMB/bandwidth;
    }

    /**
     * get bandwidth between two processors
     * @param srcName name of the source processor
     * @param dstName name of the destination processor
     * @return bandwidth src processor --> dst processor
     */
    public double getBandwidth(String srcName, String dstName){

        ProcDescription source = getProcDescription(srcName);
        ProcDescription destination = getProcDescription(dstName);

        Integer srcId = source.get_id();
        Integer dstId = destination.get_id();

        if (source!=null && destination!=null)
            return getBandwidth(srcId, dstId);
        else {
            System.err.println("bandwidth obtaining error: source or destination processor is not found");
            return 0;
        }
    }

    /**
     * get bandwidth for weights loading
     * @param dstName name of the destination processor
     * @return bandwidth src processor --> dst processor
     */
    public double getWeightsBandwidth(String dstName){
        ProcDescription destination = getProcDescription(dstName);
        Integer dstId = destination.get_id();

        if (destination!=null)
            return _getWeightsBandwidth(dstId);
        else {
            System.err.println("weighht bandwidth obtaining error: destination processor is not found");
            return 0;
        }
    }


    /** get bandwidth between two processors
     * @param srcId id of the source processor
     * @param dstId id of the destination processor
     * @return bandwidth src processor --> dst processor
     */
    public double getBandwidth(Integer srcId, Integer dstId){
        if(!_checkBandwidthMatrix())
            return 0;

        try{
            Double bw = (double)_bandwidthMatrix[dstId][srcId];
            return bw;

        }
        catch (Exception e) {
            System.out.println("bandwidth matrix has no element[" + dstId + "][" + srcId + "]");
            return 0;
        }
    }

    /** get weights load bandwidth
     * @param dstId id of the destination processor
     * @return weights load bandwidth
     */
    private double _getWeightsBandwidth(Integer dstId){
        if(_weightsBandwidthMatrix==null)
            return -1;
        return _weightsBandwidthMatrix[dstId];
    }

    /** get speed of communication with global memory*/
    public double getGlobalMemoryBandwidth(){
        return _global_memory_bandwidth;
    }


    /**Check evaluations*/
    public void checkEvaluations(){
        _checkProcessors();
        _checkBandwidthMatrix();
    }

    /** get number of processors, accordint to the evalution*/
    public Integer getProcessorsNum(){
        int procNum = 0;
        if(_procDescriptions!=null)
            procNum = _procDescriptions.size();
        return procNum;
    }

    /** if layers is executed in several kernels*/
    public boolean isMultikernelLayer() {
        return _multikernel_layer;
    }


    /////////////////////////////////////////////////////////////////////////
    ////////////// private methods                              ////////////

    private ProcTypeDescription _getCoreTypeEval(String proctype, String procSubtype){
        for(ProcTypeDescription c: _coreTypes){
            if(c.get_type().equals(proctype) && c.get_subtype().equals(procSubtype))
                return c;

        }
        return null;
    }

    /** init bandwidth matrix with zeros */
    private void _initDummyBandwidthMatrix(Integer procNum) {
        _bandwidthMatrix = new double[procNum][procNum];
    }

    /** check if platform evaluation contains processors evaluation*/
    private boolean _checkProcessors(){
        int procNum = getProcessorsNum();
        if(procNum == 0 ){
            System.err.println("No processors found in platform evaluation");
            return false;
        }
        return true;
    }

    /** check if bandwidth matrix is OK*/
    private boolean _checkBandwidthMatrix(){
        int procNum = getProcessorsNum();
        if(procNum ==0)
            return false;

        if (_bandwidthMatrix == null)
            _generateMoCBandwidthMatrix(procNum);

        int oy = _bandwidthMatrix.length;
        int ox = 0;
        if(_bandwidthMatrix.length > 0)
            ox = _bandwidthMatrix[0].length;

        return ox== procNum && oy == procNum;
    }

    private void _generateMoCBandwidthMatrix(int processorsNum){
        _bandwidthMatrix = new double[processorsNum][processorsNum];
        for (int j=0; j<processorsNum; j++){
            for (int i=0; i< processorsNum; i++)
                _bandwidthMatrix[j][i] = 0;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////////////// private variables                            ////////////

    @SerializedName("name") private String name;
    @SerializedName("version") private String version;
    @SerializedName("peak_performance") private String peak_performance;
    @SerializedName("global_memory_size") private String global_memory_size;
    @SerializedName("kernel_memory_size") private String kernel_memory_size;

   /** evaluation of operators execution time for each core type*/
    @SerializedName("core_types") private  Vector<ProcTypeDescription> _coreTypes = new Vector<>();

    /** processors list descriptions*/
    @SerializedName("cores") private  Vector<ProcDescription> _procDescriptions = new Vector<>();

    /** matrix of bandwidth between the processors M = n x n, where n = total number of processors,
     * M[i][i] = 0; M[i][j] is bandwidth processor i --> processor j, i and j = 0 -> n-1*/

    @SerializedName("activations_bandwidth") private double[][] _bandwidthMatrix;

    /** matrix of bandwidth between the processors M = n x n, where n = total number of processors,
     * M[i][i] = 0; M[i][j] is bandwidth processor i --> processor j, i and j = 0 -> n-1*/

    @SerializedName("weights_bandwidth") private double[] _weightsBandwidthMatrix;

    /** if layer is executed in several kernels*/
    @SerializedName("multikernel_layer") private boolean _multikernel_layer = false;

    /** bandwidth with global memory (GB/s)*/
    @SerializedName("global_memory_bandwidth") private Double _global_memory_bandwidth = 0.0;
}
