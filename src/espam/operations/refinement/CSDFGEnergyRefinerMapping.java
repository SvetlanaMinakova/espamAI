package espam.operations.refinement;

import espam.datamodel.graph.Graph;
import espam.datamodel.graph.csdf.CSDFNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 *
 * Refines generated CSDF graph with energy model
 *TODO takes mapping into account - delete this class if mapping is not
 * TODO taken into account by a tool
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

public class CSDFGEnergyRefinerMapping {

    /////////////////////////////////////////////////////////////////////
    ////                   public methods                           ////

    /**
     * Get instance of the singleton energy refiner
     * @return instance of the singleton energy refiner
     */
    public static CSDFGEnergyRefinerMapping getInstance() {
        return _energyRefinerMapping;
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
                proc = _getProcessor(node);
                util = utilization.get(node.getId());

                /** TODO: common static energy for all the system is not taken into account */
                summaryEnergy += _getDynamicEnergy(node.getName(),util, proc);
            }
            return summaryEnergy;
    }

    /**
     * Get dynamic energy according to formula:
     *  Ed = Uproc * alpha(proc), where
     *  Uproc - actor utilization
     *  alpha(proc) - alpha-parameter (estimation on real platform)
     *
     * @return system dynamic energy
     */
    private Double _getDynamicEnergy(String nodeName, Double utilization, String proc) throws Exception{
        if(utilization==null)
            throw new Exception("dynamic energy calculation error: actor" +
                    nodeName + " utilizations is not defined");

        Double alpha = _alphas.get(proc);
        if(alpha==null)
            throw new Exception("dynamic energy calculation error: actor alpha-parameter for " +
                    proc + " is not defined");

        return utilization * alpha;
    }

    /**TODO remove mock-up after testing!
     * TODO should be fill in from the mapping or GraphProperties!*/
    private String _getProcessor(CSDFNode node){
        return "PE";
    }

    /**
     * Initialize basic operations list by values,
     * estimated for ODROID XU-3 (see paper)
     * TODO remove after testing
     */
    private void _initDefault(){
        _initAlphaDefault();
        _initBDefault();
        _initBetaDefault();
    }

    /**
     * Init alpha - parameters default
     */
    private void _initAlphaDefault(){
        double peVal = 3.03 /1000000000.0;
        double eeVal = 2.62 /1000000000.0;
        _alphas.put("PE",peVal);
        _alphas.put("EE",eeVal);

    }

      /**
     * Init beta - parameters default
     */
    private void _initBetaDefault(){
        double peVal = 0.155;
        double eeVal = 0.0278;
        _betas.put("PE",peVal);
        _betas.put("EE",eeVal);

    }

    /**
     * Init b - parameters default
     */
    private void _initBDefault(){
        double peVal = 2.621;
        double eeVal = 2.12;
        _bs.put("PE",peVal);
        _bs.put("EE",eeVal);

    }
    /////////////////////////////////////////////////////////////////////
    ////                      private methods                       ////
    /**Singleton instance of energy refiner*/
    private CSDFGEnergyRefinerMapping(){
        _initDefault();
    }

    /////////////////////////////////////////////////////////////////////
    ////                      private variables                     ////

    /** List of processors, used for model*/
    private Vector<String> _processors = new Vector<>();

    /** alpha - parameters for each processor*/
    private HashMap<String, Double> _alphas = new HashMap<>();

    /** b - parameters for each processor*/
    private HashMap<String, Double> _bs = new HashMap<>();

    /** beta - parameters for each processor*/
    private HashMap<String, Double> _betas = new HashMap<>();

    /**Singleton instance of energy refiner*/
    private static CSDFGEnergyRefinerMapping _energyRefinerMapping = new CSDFGEnergyRefinerMapping();
}
