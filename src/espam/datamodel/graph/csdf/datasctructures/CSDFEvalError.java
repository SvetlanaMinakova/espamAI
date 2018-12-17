package espam.datamodel.graph.csdf.datasctructures;

public class CSDFEvalError extends CSDFEvalResult{

    /**
     * Create new SDF Evaluation error with specified message
     * @param errorMessage error message
     */
    public CSDFEvalError(String errorMessage){
        _errorMessage = errorMessage;
    }

    /**
     * Get sdf evaluation error message
     * @return  sdf evaluation error message
     */
    public String getErrorMessage() {
        return _errorMessage;
    }

    /**
     * Set sdf evaluation error message
     * @param errorMessage sdf evaluation error message
     */
    public void setErrorMessage(String errorMessage) {
        this._errorMessage = errorMessage;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                  ////
    String _errorMessage;
}
