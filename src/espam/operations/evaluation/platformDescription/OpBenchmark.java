package espam.operations.evaluation.platformDescription;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

public class OpBenchmark {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public Double getPerformance() { return _performance; }

    public double getEnergy() { return _energy; }

    public String get_name() {
        return _name;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /** name*/
    @SerializedName("name")private String _name = "";

    /** Basic operator performance*/
    @SerializedName("performance")Double _performance = 0.0;

    /** max operator energy evaluation [Watt]*/
    @SerializedName("energy") double _energy = 0.0;
}
