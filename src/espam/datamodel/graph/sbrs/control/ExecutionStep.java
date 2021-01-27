package espam.datamodel.graph.sbrs.control;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.sbrs.supergraph.SBRSLayer;

import java.util.HashMap;

public class ExecutionStep {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**Empty Constructor to create new SBRS MoC */
    public ExecutionStep(SBRSLayer sbrsLayer, Layer scenarioLayer, Double duration) {
        _sbrsLayer = sbrsLayer;
        _scenarioLayer = scenarioLayer;
        _duration = duration;
        _parameters = new HashMap<>();
    }

    public String getSBRSLayerName(){
        return _sbrsLayer.getName();
    }

    public SBRSLayer getSbrsLayer() {
        return _sbrsLayer;
    }

    public Double getDuration() { return _duration; }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /** layer, executed at the given step*/
    @SerializedName("sbrsLayer")private SBRSLayer _sbrsLayer;
    /** layer, executed at the given step*/
    @SerializedName("scenarioLayer")private Layer _scenarioLayer;
    /** step duration (in ms)*/
    @SerializedName("duration")private Double _duration;
    /** layer, executed at the given step*/
    @SerializedName("parameters")private HashMap<ParameterName, ParameterValue> _parameters;

}
