package espam.operations.refinement;

import espam.datamodel.graph.Graph;
import espam.datamodel.graph.csdf.CSDFNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 * Refines generated CSDF graph with energy model
 * TODO Mapping is not taken into account. worst-case evaluatons used
 *
 * based on:
 *  * Di Liu, Jelena Spasic, Gang Chen, and Todor Stefanov,
 * "Energy-Efficient Mapping of Real-Time Streaming Applications on Cluster Heterogeneous MPSoCs",
 * In Proc. "13th Int. IEEE Symposium on Embedded Systems for Real-Time Multimedia (ESTIMedia'15)",
 * pp. 1-10, Amsterdam, The Netherlands, Oct. 8-9, 2015.
 *
 * inputs:
 * - processors utilizations (from DARTS or any analogue) [required]
 * - mapping of SDFG nodes on platform processors (from Sesame or any analogue) [required]
 * - energy parameters alpha, beta etc, estimated for platform [required]
 * */

public class CSDFGEnergyRefiner {

    /////////////////////////////////////////////////////////////////////
    ////                   public methods                           ////

    /**
     * Get instance of the singleton energy refiner
     * @return instance of the singleton energy refiner
     */
    public static CSDFGEnergyRefiner getInstance() {
        return _energyRefiner;
    }

    /**
     * Get refined energy value for csdf graph
     * @param csdfg CSDF graph
     * @param utilization utilization vector of csdf graph actors
     * @return refined energy evaluation for CSDF graph
     * @throws Exception if an error occurs
     */
    public Double getRefinedEnergy(Graph csdfg, HashMap<Integer, Double> utilization)throws Exception {
            Double summaryEnergy = 0.0;
            CSDFNode node;
            String proc;
            Double util;

            Iterator i = csdfg.getNodeList().iterator();
            while (i.hasNext()) {
                node = (CSDFNode) i.next();
                util = utilization.get(node.getId());

                /** TODO: common static energy for all the system is not taken into account */
                summaryEnergy += _getDynamicEnergy(node.getName(),util);
            }
            return summaryEnergy;
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
                    nodeName + " utilizations is not defined");

        if(_alpha==null)
            throw new Exception("dynamic energy calculation error: actor alpha-parameter is not defined");

        return utilization * _alpha;
    }

    /**
     * Initialize basic operations list by values,
     * estimated for ODROID XU-3 (see paper)
     */
    private void _initDefault(){
        _alpha = 3.03 /1000000000.0;
        _b = 2.621;
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

    /////////////////////////////////////////////////////////////////////
    ////                      private methods                       ////
    /**Singleton instance of energy refiner*/
    private CSDFGEnergyRefiner(){
        _initDefault();
    }

    /////////////////////////////////////////////////////////////////////
    ////                      private variables                     ////
    /** alpha - parameter*/
    private Double _alpha;

    /** b - parameter*/
    private Double _b;

    /** beta - parameter*/
    private Double _beta;

    /**Singleton instance of energy refiner*/
    private static CSDFGEnergyRefiner _energyRefiner = new CSDFGEnergyRefiner();
}