package espam.operations.evaluation.sbrs;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.sbrs.SBRSMoC;
import espam.datamodel.graph.sbrs.control.ControlNode;
import espam.datamodel.graph.sbrs.control.ExecutionSequence;
import espam.datamodel.graph.sbrs.control.ExecutionStep;
import espam.datamodel.graph.sbrs.control.Transition;

import java.util.Vector;

/**
 * This class evaluates transition delay of the SBRS MoC: MoC for run-time adaptive
 * CNN-based applications, executed at the edge.
 * NOTE: before using the SBRS MoC delay evaluator make sure, that:
 *  - all scenarios in the SBRS MoC are annotated with time
 *  - all transitions are present in the SBRS MoC
 */
public class SBRSTransitionDelayEvaluator {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////
    /**
     * Get the evaluator instance
     * @return the evaluator instance
     */
    public static SBRSTransitionDelayEvaluator getInstance() {
        return _delayEvaluator;
    }


    /////////////////////////////////////////////////////////////////////
    ////                            SBRS-TP                         ////

    /** evaluate minimum delay of the new scenario in time units
     * @param sbrsMoC SBRS MoC
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @return minimum possible delay of the new scenario in time units
     */
    public Double minDelayInTimeUnits(SBRSMoC sbrsMoC, String oldScenarioName, String newScenarioName){
        Integer stepSSR = 0;
        Double delay = delayInTimeUnits(sbrsMoC, oldScenarioName, newScenarioName, stepSSR);
        return delay;
    }

    /** evaluate minimum transition delay in time units
     * @param sbrsMoC SBRS MoC
     *
     * @return minimum possible delay of the new scenario in time units
     */
    public Double minDelayInTimeUnits(SBRSMoC sbrsMoC, Transition transition){
        Integer stepSSR = 0;
        Double delay = delayInTimeUnits(sbrsMoC, transition, stepSSR);
        return delay;
    }

    /**
     * @param sbrsMoC SBRS MoC
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @param stepSSR step in the old scenario, at which the
     * scenarios switch request (SSR) has arrived
     * @return transition delay in time units
     */
    public Double delayInTimeUnits(SBRSMoC sbrsMoC, String oldScenarioName, String newScenarioName, Integer stepSSR){
        Transition transition = sbrsMoC.controlNode.findTransition(oldScenarioName, newScenarioName);
        if (transition == null) {
            System.err.println("Transition delay evaluation error: transition " +
                    oldScenarioName + " --> " + newScenarioName + " not found");
            return 0.0;
        }
        Double transitionDelay = delayInTimeUnits(sbrsMoC, transition, stepSSR);
        return transitionDelay;
    }


    /** evaluate transition delay in time units
     * @param sbrsMoC SBRS MoC
     * @param transition transition
     * @param stepSSR step in the old scenario, at which the
     * scenarios switch request (SSR) has arrived
     * @return minimum possible delay of the new scenario in time units
     */
    public Double delayInTimeUnits(SBRSMoC sbrsMoC, Transition transition, Integer stepSSR){
        Double delay = 0.0;
        Vector<Vector<ExecutionStep>> switchingSchedule = sbrsMoC.controlNode.schedule(transition, stepSSR);
        Double switchingStepExecTime, scenarioStepExecTime;
        Integer newScenarioFirstOutputStep = _newScenarioFirstOutputStep(sbrsMoC, transition, switchingSchedule, stepSSR);

        Vector<ExecutionStep> switchingStep;
        for (int i =0; i<= newScenarioFirstOutputStep; i++){
            switchingStep = switchingSchedule.elementAt(i);
            switchingStepExecTime = 0.0;
            for (ExecutionStep scenarioStep: switchingStep){
                scenarioStepExecTime = scenarioStep.getDuration();
                switchingStepExecTime = Math.max(scenarioStepExecTime, scenarioStepExecTime);
            }
            delay = delay + switchingStepExecTime;
        }
        return delay;
    }


    /**
     * Find an Id of execution step in the scenarios transition (switching)
     * schedule, at which step the first output of the new scenario was produced
     * @param sbrsMoC SBRS MoC
     * @param transition transition (switching) description
     * @param switchingSchedule schedule of scenarios transition
     * @param stepSSR step in the old scenario, at which the
     * scenarios switch request (SSR) has arrived
     * @return Id of execution step in the scenarios transition (switching)
     * schedule, at which step the first output of the new scenario was produced
     */
    private Integer _newScenarioFirstOutputStep(SBRSMoC sbrsMoC, Transition transition,
                                                         Vector<Vector<ExecutionStep>> switchingSchedule, Integer stepSSR){
        Integer switchingLen = switchingSchedule.size();
        Integer overlappingSteps = sbrsMoC.controlNode.findOverlappingStepsNum(transition, stepSSR);
        Integer oldScenarioLen = transition.getOldScenario().countExecutionSteps();
        Integer newScenarioLen = transition.getNewScenario().countExecutionSteps();
        Integer stepId;

        // old scenario ended before the new scenario produced its first output
        if (overlappingSteps <= newScenarioLen)
            stepId = switchingLen - 1;
        else
            stepId = oldScenarioLen + newScenarioLen - overlappingSteps - 1;

        return stepId;
    }

    /////////////////////////////////////////////////////////////////////
    ////                            Naive approach                  ////

    /** evaluate minimum delay of the new scenario in time units
     * @param sbrsMoC SBRS MoC
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @return minimum possible delay of the new scenario in time units
     */
    public Double minDelayInTimeUnitsNaive(SBRSMoC sbrsMoC, String oldScenarioName, String newScenarioName){
        Integer stepSSR = 0;
        Double delay = delayInTimeUnitsNaive(sbrsMoC, oldScenarioName, newScenarioName, stepSSR);
        return delay;
    }

    /** evaluate delay of the new scenario in time units
     * @param sbrsMoC SBRS MoC
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @param stepSSR step in the old scenario, at which the
     * scenarios switch request (SSR) has arrived
     * @return minimum possible delay of the new scenario in time units
     */
    public Double delayInTimeUnitsNaive(SBRSMoC sbrsMoC, String oldScenarioName, String newScenarioName, Integer stepSSR) {
        Transition transition = sbrsMoC.controlNode.findTransition(oldScenarioName, newScenarioName);
        if (transition == null) {
            System.err.println("Transition delay evaluation error: transition " +
                    oldScenarioName + " --> " + newScenarioName + " not found");
            return 0.0;
        }
        Double transitionDelay = delayInTimeUnitsNaive(transition, stepSSR);
        return transitionDelay;
    }

    /** evaluate minimum delay of the new scenario in time units (Naive approach)
     * @param transition transition
     * @return minimum possible delay of the new scenario in time units
     */
    public Double minDelayInTimeUnitsNaive(Transition transition){
        Integer stepSSR = 0;
        Double delay = delayInTimeUnitsNaive(transition, stepSSR);
        return delay;
    }

    /** evaluate delay of the new scenario in time units
     * @param transition transition
     * @param stepSSR step in the old scenario, at which the
     * scenarios switch request (SSR) has arrived
     * @return minimum possible delay of the new scenario in time units
     */
    public Double delayInTimeUnitsNaive(Transition transition, Integer stepSSR){
        ExecutionSequence oldScenario = transition.getOldScenario();
        ExecutionSequence newScenario = transition.getNewScenario();

        Double delay = 0.0;
        Double stepTime;
        ExecutionStep step;
        //traverse remaining steps of the old scenario
        for (int i = stepSSR; i< oldScenario.countExecutionSteps(); i++){
            step = oldScenario.getExecutionSteps().elementAt(i);
            stepTime = step.getDuration();
            delay = delay + stepTime;
        }

        //traverse all steps of the new scenario
        for (int i = 0; i< newScenario.countExecutionSteps(); i++){
            step = newScenario.getExecutionSteps().elementAt(i);
            stepTime = step.getDuration();
            delay = delay + stepTime;
        }
        return delay;
    }


    /** evaluate minimum delay of the new scenario in execution steps
     * @param c SBRS MoC control node
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @return minimum possible delay of the new scenario in execution steps
     */
    public Integer minDelayInSteps(ControlNode c, String oldScenarioName, String newScenarioName){
        Integer stepSSR = 0;
        Integer delay = c.findOverlappingStepsNum(oldScenarioName, newScenarioName, stepSSR);
        return delay;
    }

    /** evaluate delay of the new scenario in execution steps
     * @param c SBRS MoC control node
     * @param oldScenarioName old scenario name
     * @param newScenarioName new scenario name
     * @param stepSSR step, at which the scenario switch request (SSR) has arrived
     * @return delay of the new scenario in execution steps
     */
    public Integer delayInSteps(ControlNode c, String oldScenarioName, String newScenarioName, int stepSSR){
        Integer delay = c.findOverlappingStepsNum(oldScenarioName, newScenarioName, stepSSR);
        return delay;
    }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /**Singleton realization of delay evaluator*/
    private static SBRSTransitionDelayEvaluator _delayEvaluator = new SBRSTransitionDelayEvaluator();
}
