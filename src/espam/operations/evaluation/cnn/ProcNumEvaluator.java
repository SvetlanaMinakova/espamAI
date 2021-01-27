package espam.operations.evaluation.cnn;

import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.csdf.datasctructures.CSDFEvalResult;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.operations.evaluation.platformDescription.PlatformDescription;
import espam.operations.evaluation.platformDescription.ProcDescription;
import espam.operations.scheduler.dnnScheduler.layerFiring;

import java.util.Vector;

/**
 * Evaluates number of processors, utilized by the DNN during its execution
 */
public class ProcNumEvaluator {

    /**
     * Get DNN memory evaluator instance
     * @return DNN memory evaluator instance
     */
    public static ProcNumEvaluator getInstance() {
        return _evaluator;
    }

    public void evaluateProcNum(Network dnn, CSDFEvalResult evalResult, PlatformDescription platformDescription,
                                Vector<Vector<layerFiring>> schedule, Mapping mapping){
        _dnn = dnn;
        _platformDescription = platformDescription;
        _schedule = schedule;
        _mapping = mapping;
        Integer procNum = _evaluateProcNum();
        evalResult.setProcessors(procNum);

    }

    /**
     * Get processor number evaluation for DNN
     * @return refined processor number evaluation for DNN or null
     */
    private Integer _evaluateProcNum(){
        try{
            Vector<MProcessor> distinctProc = new Vector<>();

            /** for every execution step*/
            for(Vector<layerFiring> execStep: _schedule) {
                /**Find all procesors, participating in computations*/
                for(layerFiring lf: execStep) {
                    for (MProcessor proc : lf.getProcessors()) { //query all processors, working at this exec. step
                        if (!distinctProc.contains(proc)) {
                            distinctProc.add(proc);
                            //System.out.println("proc: "+ proc.getName());
                            if(!proc.isCPU()) {// Any accelerator needs a separate CPU (host) to launch kernels
                                MProcessor host = _getHost(proc);
                                if (!distinctProc.contains(host)) {
                                    distinctProc.add(host);
                                }
                                //System.out.println("accelerator: "+ proc.getName());
                            }
                            //else
                            //System.out.println(lf.getLayer().getName() + ", neur: " + lf.getLayer().getNeuron().getName() + " is mapped on CPU: "+ proc.getName());
                        }
                    }
                }
            }
            Integer procNum = distinctProc.size();
            return procNum;
        }
        catch (Exception e){
            System.err.print("Processors number computation error: " + e.getMessage());
            return 0;
        }
    }


    /** Get host CPU core, assigned to launch kernels on an accelerator
     * */
    private MProcessor _getHost(MProcessor  accelerator){
        ProcDescription procDesc = _platformDescription.getProcDescription(accelerator.getName());
        String host = procDesc.get_host();
        if(host==null) return accelerator;
        else return _mapping.getProcessor(host);
    }

    /** singletone*/
    protected ProcNumEvaluator() { };
    private static ProcNumEvaluator _evaluator = new ProcNumEvaluator();

    /**private variables*/
    private Network _dnn;
    private PlatformDescription _platformDescription;
    private Vector<Vector<layerFiring>> _schedule;
    private Mapping _mapping;
}
