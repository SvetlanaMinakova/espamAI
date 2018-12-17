package espam.datamodel.graph.csdf.datasctructures;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

/**
 * list of evaluation cases
 */
public class CSDFEvalResultList {

    /**
     * Get list of the evaluation cases
     * @return list of the evaluation cases
     */
    public Vector<CSDFEvalResult> getEvaluationCases() {
        return _evaluationCases;
    }

    /**
     * Set list of the evaluation cases
     * @return list of the evaluation cases
     */
    public void setEvaluationCases(Vector<CSDFEvalResult> evaluationCases) {
        this._evaluationCases = evaluationCases;
    }

    /**evaluation cases*/
    @SerializedName("evaluation_cases") private Vector<CSDFEvalResult> _evaluationCases;
}
