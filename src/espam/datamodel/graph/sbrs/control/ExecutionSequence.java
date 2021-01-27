package espam.datamodel.graph.sbrs.control;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.sbrs.supergraph.SBRSLayer;

import java.util.Vector;

public class ExecutionSequence {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**Empty Constructor to create new SBRS MoC */
    public ExecutionSequence(String name) {
        _name = name;
        _executionSteps = new Vector<>();
    }

    public void addNextExecutionStep(SBRSLayer sbrsLayer, Layer scenarioLayer){
        ExecutionStep step = new ExecutionStep(sbrsLayer, scenarioLayer, scenarioLayer.get_timeEval());
        _executionSteps.add(step);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       getters and setters                ////

    public Vector<ExecutionStep> getExecutionSteps() {
        return _executionSteps;
    }

    public Integer countExecutionSteps() {
        return _executionSteps.size();
    }

    public ExecutionStep getExecutionStep(Integer stepId){
        return _executionSteps.elementAt(stepId);
    }

    public String getName() { return _name; }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /**Name of the scenario*/
    @SerializedName("name")private String _name;

    /** sequence of execution steps, representing scenario*/
    @SerializedName("executionSteps")private Vector<ExecutionStep> _executionSteps;
}
