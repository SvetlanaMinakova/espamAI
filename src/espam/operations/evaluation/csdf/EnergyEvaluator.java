package espam.operations.evaluation.csdf;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import java.util.*;


/**
 * Evaluates energy for a CSDF graph
 * TODO Mapping is not taken into account. worst-case evaluatons used
 *
 * based on:
 * Di Liu, Jelena Spasic, Gang Chen, and Todor Stefanov,
 * "Energy-Efficient Mapping of Real-Time Streaming Applications on Cluster Heterogeneous MPSoCs",
 * In Proc. "13th Int. IEEE Symposium on Embedded Systems for Real-Time Multimedia (ESTIMedia'15)",
 * pp. 1-10, Amsterdam, The Netherlands, Oct. 8-9, 2015.
 *
 * inputs:
 * - processors utilizations (from DARTS or any analogue) [required]
 * - mapping of SDFG nodes on platform processors (from Sesame or any analogue) [required]
 * - energy parameters alpha, beta etc, estimated for platform [required]
 * */

public class EnergyEvaluator {

    /////////////////////////////////////////////////////////////////////
    ////                   public methods                           ////

    /**
     * Get instance of the singleton energy refiner
     * @return instance of the singleton energy refiner
     */
    public static EnergyEvaluator getInstance() {
        return _energyRefiner;
    }

    /**
     * TODO: finish implementation
     * Get energy value for csdf graph
     * @param utilization processors utilization for csdf graph actors
     * @return energy evaluation for CSDF graph
     * @throws Exception if an error occurs
     */
    public Double getEnergy(CSDFGraph csdfg, HashMap<Integer, Double> utilization, double staticEnergy)throws Exception {
        if(_procEnergy != null || _mapping !=null)
            return getDynamicEnergyMapped(csdfg,utilization);


        Double dynamicEnergy = getEnergy(utilization);
        return dynamicEnergy + staticEnergy;
    }

    /**
     * Get refined energy value for csdf graph
     * @param utilization utilization vector of csdf graph actors
     * @return refined energy evaluation for CSDF graph
     * @throws Exception if an error occurs
     */
    public Double getEnergy(HashMap<Integer, Double> utilization)throws Exception {
          return getDynamicEnergy(utilization.values());
    }


       /**
     * Get refined energy value for csdf graph
     * @param procUtilization processors utilization
     * @return refined energy
     * @throws Exception if an error occurs
     */
    private Double getDynamicEnergy(Collection<Double> procUtilization)throws Exception {
        double dynProcUtil = 0;
        int utilid = 0;
        for(double procUtil: procUtilization) {
            /** TODO: Why some of the utilization values<0?!*/
            // dynProcUtil += Math.max(procUtil, 0);
            dynProcUtil+=procUtil;

            //if(procUtil<0)
              //  System.out.println(" proc util [" +utilid + "] = " + procUtil + " < 0!");
            //else
              //   System.out.println(" proc util [" +utilid + "] = " + procUtil + " > 0");
                 utilid++;

        }
        /** TODO: Why some of the utilization values<0?!*/
        return Math.abs(dynProcUtil * _maxprocEnergy);
    }

            /**
     * Get dynemic energy value for csdf graph
     * @param procUtilization processors utilization
     * @return CSDF graph dynamic energy
     * @throws Exception if an error occurs
     */
    private Double getDynamicEnergyMapped(CSDFGraph csdfg, HashMap<Integer,Double> procUtilization)throws Exception {
        double dynProcUtil = 0;
        int nodeId = 0;
        String nodeName;

        for(Map.Entry<Integer,Double> procUtil: procUtilization.entrySet()) {
            nodeId = procUtil.getKey();
            nodeName = csdfg.getNode(nodeId).getName();
            dynProcUtil+=procUtil.getValue() * _getEnergyMapped(nodeName);
        }
        /** TODO: Why some of the utilization values<0?!*/
        return Math.abs(dynProcUtil);
    }

    /**
     * Get node energy with respect to the processor
     * @param processName name of the process 9CSDF node)
     * @return node energy with respect to the processor
     */
    private Double _getEnergyMapped(String processName){
        String proc = _getProcName(processName);
        if(proc!=null && _procEnergy.containsKey(proc))
            return _procEnergy.get(proc);

        return 0.0;
    }

    /**
     * Get processor Id for the CSDF node
     * @param processName name of the process
     * @return processor Id for the CSDF node
     */
    private String _getProcName(String processName){
        String nodeName = processName;
        String procName;

        for(Object mp: _mapping.getProcessorList()) {
            Vector processes = ((MProcessor)mp).getProcessList();
            Iterator i;

            i = processes.iterator();
            while (i.hasNext()) {
                MProcess process = (MProcess) i.next();
                if (process.getName().equals(nodeName)) {
                    procName =  ((MProcessor) mp).getName();

                    if(procName!=null)
			            return procName;
                }
            }

            }

            System.err.println(" Energy evaluation error: processor for node " + nodeName + " not found!");
            return null;
    }


    /**
     * Get dynamic energy according to formula:
     *  Ed = U * alpha, where
     *  U - actor utilization
     *  alpha- alpha-parameter (estimation on real platform)
     *
     * @return system dynamic energy
     */
    private Double _getDynamicEnergy(String nodeName, Double utilization) throws Exception{
        if(utilization==null)
            throw new Exception("dynamic energy calculation error: actor" +
                    nodeName + " utilization is not defined");

        if(_alpha==null)
            throw new Exception("dynamic energy calculation error: actor alpha-parameter is not defined");

        return utilization * _alpha;
    }

    /**
     * Initialize basic operations list by values,
     * estimated for ODROID XU-3 (see paper)
     */
    private void _initDefault(){
        _alpha = 3.03 /1000000000.0; //(W/MHz) one-processor dynamic power consumption
        _b = 2.621; //static power consumption of one processor (W)
        _beta = 0.155;
    }

    /**
     * Get alpha-parameter
     * @return alpha-parameter
     */
    public Double getAlpha() { return _alpha;}

    /**
     * Set alpha-parameter
     * @param alpha alpha-parameter
     */
    public void setAlpha(Double alpha) {
        this._alpha = alpha;
    }

    /**
     * Get beta-parameter
     * @return beta-parameter
     */
    public Double getBeta() { return _beta; }

    /**
     * Set beta-parameter
     * @param beta beta-parameter
     */
    public void setBeta(Double beta) {
        this._beta = beta;
    }

      /**
     * Get b-parameter
     * @return  beta-parameter
     */
    public Double getB() { return _b; }

    /**
     * Set b-parameter
     * @param b beta-parameter
     */
    public void setB(Double b) {
        this._b = b;
    }

    /**max dynamic energy consumption per platform processor
     * @return max dynamic energy consumption per platform processor
     */
    public Double getMaxprocEnergy() {
        return _maxprocEnergy;
    }

    /**max dynamic energy consumption per platform processor
     * @param maxprocEnergy  max dynamic energy consumption per platform processor
     */
    public void setMaxprocEnergy(Double maxprocEnergy) {
        this._maxprocEnergy = maxprocEnergy;
    }

    public void setProcEnergy(HashMap<String,Double> _procEnergy) {
        this._procEnergy = _procEnergy;
    }

    public HashMap<String, Double> getProcEnergy() {
        return _procEnergy;
    }

    /**
     * Compute max processor energy automatically
     */
    public void setAutoMaxProcEnergy(){
        if(_procEnergy !=null){
            _maxprocEnergy = 0.0;
            for(Double energy: _procEnergy.values())
                if(energy>_maxprocEnergy)
                    _maxprocEnergy = energy;
        }

        if(_maxprocEnergy ==null || _maxprocEnergy <= 0)
            _maxprocEnergy = 1.0;

    }

    public Mapping getMapping() { return _mapping; }

    public void setMapping(Mapping _mapping) { this._mapping = _mapping; }

    /////////////////////////////////////////////////////////////////////
    ////                      private methods                       ////
    /**Singleton instance of energy refiner*/
    private EnergyEvaluator(){
        _initDefault();
    }

    /////////////////////////////////////////////////////////////////////
    ////                      private variables                     ////
    /** alpha - parameter*/
    @SerializedName("alpha")private Double _alpha;

    /** b - parameter*/
    @SerializedName("b")private Double _b;

    /** beta - parameter*/
    @SerializedName("beta")private Double _beta;

    /** max dynamic energy consumption per platform processor*/
    @SerializedName("maxprocEnergy")private Double _maxprocEnergy = 1.0;

    /** dynamic energy consumption per platform processor*/
    @SerializedName("procEnergy")private HashMap<String,Double> _procEnergy = null;

    /**Singleton instance of energy refiner*/
    private static EnergyEvaluator _energyRefiner = new EnergyEvaluator();

    private transient Mapping _mapping;
}
