package espam.datamodel.graph.sbrs.control;

import espam.datamodel.graph.sbrs.control.ExecutionSequence;

import java.util.Vector;

/**
 * This class describes transition (switching) from the old SBRS MoC scenario
 * to the new SBRS MoC scenario, performed under the SBRS-TP transition protocol
 */
public class Transition {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public Transition(ExecutionSequence oldScenario, ExecutionSequence newScenario, Vector<Integer> minDelays){
        _oldScenario = oldScenario;
        _newScenario = newScenario;
        _minDelays = minDelays;
    }

    /////////////////////////////////////////////////////////////////////
    ////                     getters and setters                    ////

    public ExecutionSequence getOldScenario() { return _oldScenario; }

    public ExecutionSequence getNewScenario() { return _newScenario; }

    public Vector<Integer> getMinDelays() { return _minDelays; }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    private ExecutionSequence _oldScenario;
    private ExecutionSequence _newScenario;
    private Vector<Integer> _minDelays;
}
