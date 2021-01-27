package espam.datamodel.graph.sbrs.control;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.sbrs.supergraph.Supergraph;
import java.util.Vector;

/**
 * This class implements the control node C of the SBRS MoC.
 * The control node C of the SBRS MoC implements the
 * SBRS-TP transition protocol for efficient
 * switching among SBRS applications during the CNN-based application run-time.
 */
public class ControlNode {
    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public ControlNode(Supergraph supergraph){
        _supergraph = supergraph;
        _transitions = new Vector<>();
        _generateTransitions();
    }

    /**
     * Find total number of steps, overlapping during the scenarios transition
     * @param stepSSR step of the old scenario, during which the SSR has arrived
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @param stepSSR
     * @return number of steps, overlapping during the scenarios transition
     */
    public Integer findOverlappingStepsNum(String oldScenarioName, String newScenarioName, Integer stepSSR){
        Transition t = findTransition(oldScenarioName, newScenarioName);
        if (t == null) {
            System.err.println("Overlapping steps computation error: transition " +
                    oldScenarioName + " --> " + newScenarioName + " not found");
            return 0;
        }
        Integer stepsOverlap = findOverlappingStepsNum(t, stepSSR);
        return stepsOverlap;
    }

    /**
     * Find total number of steps, overlapping during the scenarios transition
     * @param transition transition
     * @param stepSSR step of the old scenario, during which the SSR has arrived
     * @return schedule for a transition
     */
    public Integer findOverlappingStepsNum(Transition transition, Integer stepSSR){
        Vector<Integer> oldScenarioAsLayerIdsList = _supergraph.getExecSequenceAsLayerIdsList(transition.getOldScenario());
        Vector<Integer> newScenarioAsLayerIdsList = _supergraph.getExecSequenceAsLayerIdsList(transition.getNewScenario());
        Vector<Integer> minDelays = transition.getMinDelays();
        Integer stepIdOld = stepSSR, stepIdNew=0, step=0, layerIdOld, layerIdNew, minDelayNew;
        Integer stepsOverlap = 0;

        //at first step only stepSSR of the old scenario is executed, so no overlapping occurs
        step++; stepIdOld++;

        //schedule remaining steps of the old scenario, and try to
        // schedule some steps of the new scenario in parallel
        while (stepIdOld < oldScenarioAsLayerIdsList.size()) {
            stepIdOld++;
            minDelayNew = minDelays.elementAt(stepIdNew);
            if (step >= minDelayNew - stepSSR + 1){
                stepsOverlap++;
                stepIdNew++;
            }
            step++;
        }
        return stepsOverlap;
    }

    /**
     * Build schedule for a transition
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @param stepSSR step of the old scenario, during which the SSR has arrived
     * @return schedule for a transition
     */
    public Vector<Vector<ExecutionStep>> schedule(String oldScenarioName, String newScenarioName, Integer stepSSR){
        Vector<Vector<ExecutionStep>> tSchedule;
        Transition t = findTransition(oldScenarioName, newScenarioName);
        if (t == null) {
            System.err.println("Overlapping steps computation error: transition " +
                    oldScenarioName + " --> " + newScenarioName + " not found");
            tSchedule = new Vector<>();
        }
        else
            tSchedule = schedule(t, stepSSR);
        return tSchedule;
    }


    /**
     * Build schedule for a transition
     * @param transition transition
     * @param stepSSR step of the old scenario, during which the SSR has arrived
     * @return schedule for a transition
     */
    public Vector<Vector<ExecutionStep>> schedule(Transition transition, Integer stepSSR){
        ExecutionSequence oldScenario = transition.getOldScenario();
        ExecutionSequence newScenario = transition.getNewScenario();
        Integer oldScenarioTotalSteps = oldScenario.getExecutionSteps().size();
        Integer newScenarioTotalSteps = newScenario.getExecutionSteps().size();
        
        Vector<Integer> minDelays = transition.getMinDelays();
        Integer stepIdOld = stepSSR, stepIdNew=0, step=0, minDelayNew;
        ExecutionStep stepOld, stepNew;

        Vector<Vector<ExecutionStep>> tpSchedule = new Vector<>();

        //at first step only stepSSR of the old scenario is executed
        Vector<ExecutionStep> tpScheduleStep = new Vector<>();
        stepOld = oldScenario.getExecutionStep(stepIdOld);
        tpScheduleStep.add(stepOld);
        tpSchedule.add(tpScheduleStep);
        step++; stepIdOld++;

        //schedule remaining steps of the old scenario, and try to
        // schedule some steps of the new scenario in parallel
        while (stepIdOld < oldScenarioTotalSteps) {
            tpScheduleStep = new Vector<>();
            stepOld = oldScenario.getExecutionStep(stepIdOld);
            tpScheduleStep.add(stepOld);
            stepIdOld++;
            minDelayNew = minDelays.elementAt(stepIdNew);
            if (step >= minDelayNew - stepSSR + 1){
                stepNew = newScenario.getExecutionStep(stepIdNew);
                tpScheduleStep.add(stepNew);
                stepIdNew = (stepIdNew + 1) % newScenarioTotalSteps;
            }
            tpSchedule.add(tpScheduleStep);
            step++;
        }

        //schedule remaining steps of the new scenario
        while (stepIdNew < newScenarioTotalSteps) {
            tpScheduleStep = new Vector<>();
            stepNew = newScenario.getExecutionStep(stepIdNew);
            tpScheduleStep.add(stepNew);
            stepIdNew++;
            tpSchedule.add(tpScheduleStep);
            step++;
        }

        return tpSchedule;
    }

    /**
     * Find a transition between two given scenarios
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @return a transition between two given scenarios or null (if such transition is not found)
     */
    public Transition findTransition(String oldScenarioName, String newScenarioName){
        for (Transition t: _transitions){
            if(t.getOldScenario().getName().equals(oldScenarioName)){
                if (t.getNewScenario().getName().equals(newScenarioName))
                    return t;
            }
        }
        return null;
    }

    /**************************************************
     **** Print
     *************************************************/

    /**
     * Print all transitions
     */
    public void printTransitions(){
        printTransitions(false);
    }

    /**
     * Print all transitions
     * @param printDelays print delays for every transition
     */
    public void printTransitions(boolean printDelays){
        for (Transition t: _transitions){
            System.out.print(t.getOldScenario().getName() + " --> " + t.getNewScenario().getName());
            if(printDelays)
                System.out.println(", min delays: " + t.getMinDelays());
            else
                System.out.println();
        }
    }

    /**
     * Print schedule for transition between two given scenarios
     * @param oldScenarioName name of the old scenario
     * @param newScenarioName name of the new scenario
     * @param stepSSR step, at which the scenario switch request (SSR) has arrived
     */
    public void printTransitionSchedule(String oldScenarioName, String newScenarioName, Integer stepSSR){
        Transition transition = findTransition(oldScenarioName, newScenarioName);
        if (transition == null){
            System.err.println("Transition print error: transition " + oldScenarioName +
                    " --> " + newScenarioName + " not found in the control node");
            return;
        }
        Vector<Vector<ExecutionStep>> tSchedule = schedule(transition, stepSSR);
        Integer scheduleStepId = 0;
        Integer sbsrLayerId;
        System.out.println("schedule: old scenario layer Id, new scenario layer Id");
        for (Vector<ExecutionStep> scheduleStep: tSchedule){
            System.out.print("step " + scheduleStepId.toString() + ": ");
            for (ExecutionStep step: scheduleStep){
                sbsrLayerId = _supergraph.getLayers().indexOf(step.getSbrsLayer());
                System.out.print(sbsrLayerId + " ");
            }
            System.out.println();
            scheduleStepId++;
        }
    }

    /**
     * Print all execution sequences, performed by the control node
     */
    public void printAllExecutionSequences(){
        for (ExecutionSequence eseq: _supergraph.getExecutionSequences()) {
            System.out.println(eseq.getName() + ": ");
            for (ExecutionStep step: eseq.getExecutionSteps())
                System.out.print(step.getSBRSLayerName() + ", ");
            System.out.println();
        }
    }

    /**
     * Print execution sequence of a given scenario
     * @param scenarioName scenario name
     */
    public void printExecutionSequence(String scenarioName){
        ExecutionSequence eSeq = _supergraph.findExecutionSequence(scenarioName);
        if (eSeq == null){
            System.err.println("Execution sequence print error: scenario " + scenarioName + " not found");
            return;
        }
        Vector<Integer> scenarioAsLayerIdsList = _supergraph.getExecSequenceAsLayerIdsList(eSeq);
        System.out.println(scenarioAsLayerIdsList);
    }

    /**
     * Print execution sequence of a given scenario
     * @param scenarioId scenario id
     */
    public void printExecutionSequenceTimed(Integer scenarioId, boolean printPerStep){
        ExecutionSequence eSeq = _supergraph.getExecutionSequences().get(scenarioId);
        if (eSeq == null){
            System.err.println("Execution sequence print error: scenario " + scenarioId + " not found");
            return;
        }
        Double duration, totalTime =0.0;
        Integer totalSteps = 0;
        Layer execStepLayer;
        for(ExecutionStep step: eSeq.getExecutionSteps()){
            execStepLayer = step.getSbrsLayer().getParent();
            duration = step.getDuration();
            totalTime = totalTime + duration;
            totalSteps++;
            if(printPerStep)
                System.out.println(execStepLayer.getName() + " (" + duration + " ms ) ");
        }
        System.out.println("TOTAL TIME: " + totalTime + ", TOTAL STEPS: " + totalSteps);
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private  methods                   ////

    /**
     * Generate new transition
     * @param oldScenario old scenario, represented as an execution sequence
     * @param newScenario new scenario, represented as an execution sequence
     * @return Transition between the oldScenario and the newScenario,
     * performed under the SBRS-TP transition protocol
     */
    protected Transition _generateTransition(ExecutionSequence oldScenario, ExecutionSequence newScenario){
        Vector<Integer> delay = _computeMinDelay(oldScenario, newScenario);
        Transition t = new Transition(oldScenario, newScenario, delay);
        return t;
    }

    /**
     * Generate transitions between every pair (DNN_new, DNN_old) of scenarios
     * of the CNN-based application with SBRS.
     */
    private void _generateTransitions(){
        Vector<ExecutionSequence> executionSequences = _supergraph.getExecutionSequences();
        Integer scenariosNum = executionSequences.size();
        ExecutionSequence oldScenario, newScenario;
        for (int i=0; i<scenariosNum;i++ ){
            for (int j=0; j<scenariosNum;j++ ){
                if (i!=j){
                    newScenario = executionSequences.elementAt(i);
                    oldScenario = executionSequences.elementAt(j);
                    Transition t = _generateTransition(oldScenario, newScenario);
                    _transitions.add(t);
                }
            }
        }
    }

    /**
     * Compute minimum delay from the first step of the old scenario
     * to every i-th step of the new scenario
     * @param oldScenario old scenario, represented as an execution sequence
     * @param newScenario new scenario, represented as an execution sequence
     * @return minimum delay from the first step of the old scenario
     * to every i-th step of the new scenario
     */
   private Vector<Integer> _computeMinDelay(ExecutionSequence oldScenario, ExecutionSequence newScenario){
        Vector<Integer> oldScenarioAsLayerIdsList = _supergraph.getExecSequenceAsLayerIdsList(oldScenario);
        Vector<Integer> newScenarioAsLayerIdsList = _supergraph.getExecSequenceAsLayerIdsList(newScenario);
        Vector<Integer> minDelay = new Vector<>();

        Integer delay = 0;
        for(Integer i=0; i< newScenarioAsLayerIdsList.size(); i++){
            Integer newScenarioLayerId = newScenarioAsLayerIdsList.elementAt(i);

            for(Integer j=0; j< oldScenarioAsLayerIdsList.size(); j++){
                Integer oldScenarioLayerId = oldScenarioAsLayerIdsList.elementAt(j);
                if(oldScenarioLayerId == newScenarioLayerId){
                    if (j >= delay)
                        delay = j;
                }
            }
            minDelay.add(delay);
            delay = delay + 1;
        }
        return minDelay;
    }

    /////////////////////////////////////////////////////////////////////
    ////                       getters and setters                   ////


    public Vector<Transition> getTransitions() { return _transitions; }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    private Vector<Transition> _transitions;
    private Supergraph _supergraph;
}
