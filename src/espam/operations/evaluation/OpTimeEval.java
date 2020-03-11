package espam.operations.evaluation;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

public class OpTimeEval {

    public String get_name() {
        return _name;
    }

    /** name*/
    @SerializedName("name")private String _name = "";

    /** Basic operator performance*/
    @SerializedName("performance")Long _performance = 0l;

    /**Performance derivation characteristic*/
    @SerializedName("performance_deriv")private Long _performanceDeriv = 0l;

    /**vector of evaluated performances*/
    @SerializedName("performance_vec") Vector<Long>  _performanceVec;

    /**vector of evaluated complexities*/
    @SerializedName("complexities_vec") Vector<Long>  _complexitiesVec;

    /** max operator energy evaluation [Watt]*/
    @SerializedName("energy") double _energy = 0.0;
}
