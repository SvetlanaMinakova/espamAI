package espam.datamodel.graph.cnn.operators;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.csdf.datasctructures.Tensor;

import java.util.TreeMap;

/**
 * This class describes DNN operator
 * TODO: finish implementation and integrate!!!
 */
public class Operator{

     ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Operator(String name){
        _name = name;
        _basic = getName();
        _tensorParams = new TreeMap<>();
        _intParams = new TreeMap<>();
        _stringParams = new TreeMap<>();
    }

    public Operator(String name, String basic){
        _name = name;
        _basic = basic;
        _tensorParams = new TreeMap<>();
        _intParams = new TreeMap<>();
        _stringParams = new TreeMap<>();
    }

    /**
     * TODO extend!
    * Compares Layer  with another object
    * @param obj Object to compare this Layer with
    * @return true if Layer is equal to the object and false otherwise
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

        Operator op = (Operator) obj;
         return _name.equals(op._name) &&_basic == op._basic;
       }
    
     /**
     *  Clone this Operator
     * @return  a new reference on instance of the Operator
     */
   @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Operator newObj = (Operator) super.clone();
            newObj.setName(_name);
            newObj.setIntParams((TreeMap<String, Integer>)_intParams.clone());
            newObj.setFloatParams((TreeMap<String, Float>)_floatParams.clone());
            newObj.setTensorParams((TreeMap<String, Tensor>)_tensorParams.clone());
            newObj._basic = _basic;
            newObj._timeComplexity = _timeComplexity;
            newObj._concat = _concat;
            newObj.setTensorRefs((TreeMap<String, String>)_tensorRefs.clone());
        return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////            getters and setters                           ////

    public void setName(String name) {
        this._name = name;
    }

    public void setFloatParams(TreeMap<String, Float> floatParams) {
        this._floatParams = floatParams;
    }

    public void setIntParams(TreeMap<String, Integer> intParams) {
        this._intParams = intParams;
    }

    public void setTensorParams(TreeMap<String, Tensor> tensorParams) {
        this._tensorParams = tensorParams;
    }

    public void setTensorRefs(TreeMap<String, String> tensorRefs) {
        this._tensorRefs = tensorRefs;
    }

    public void setStringParams(TreeMap<String, String> stringParams) {
        this._stringParams = stringParams;
    }

    public String getName() {
        return _name;
    }

    public TreeMap<String, Float> getFloatParams() {
        return _floatParams;
    }

    public TreeMap<String, Integer> getIntParams() {
        return _intParams;
    }

    public TreeMap<String, Tensor> getTensorParams() { return _tensorParams; }

    public TreeMap<String, String> getTensorRefs() { return _tensorRefs; }

    public TreeMap<String, String> getStringParams() {
        return _stringParams;
    }

    public String getBasic() { return _basic; }

    public void setBasic(String basic) {
        this._basic = basic;
    }

    public Long getTimeComplexity() { return _timeComplexity; }

    /** operator memory complexity*/
    /** TODO: update eval*/
    public Long getMemoryComplexity() {
        Long totalComplexity =0l;
        if(this.hasTensorParams()){
            for(Tensor tensorParam: _tensorParams.values()){
                totalComplexity += tensorParam.getElementsNumber();
            }
        }

        return totalComplexity;
    }

    public void setTimeComplexity(Long timeComplexity) {
        this._timeComplexity = timeComplexity;
    }

    public boolean isConcat() {
        return _concat;
    }

    public void setConcat(boolean concat) {
        this._concat = concat;
    }

    ///////////////////////////////////////////////////////////////////
    ////           null-TreeMap checkers                           ////

    public boolean hasFloatParams() {
        if (_floatParams==null)
            return false;
        if(_floatParams.size()==0)
            return false;
        return true;
    }

      public boolean hasIntegerParams() {
        if (_intParams==null)
            return false;
        if(_intParams.size()==0)
            return false;
        return true;
    }

      public boolean hasTensorParams() {
        if (_tensorParams==null)
            return false;
        if(_tensorParams.size()==0)
            return false;
        return true;
    }

    public boolean hasStringParams() {
        if (_stringParams==null)
            return false;
        if(_stringParams.size()==0)
            return false;
        return true;
    }

    public boolean hasTensorRefs(){
       if(_tensorRefs == null)
           return false;
       if(_tensorRefs.size()==0)
           return false;
       return true;

    }

     ///////////////////////////////////////////////////////////////////
    ////           TreeMap initializers                            ////

    public void initFloatParams() {
        this._floatParams = new TreeMap<>();
    }

    public void initIntParams() {
        this._intParams = new TreeMap<>();
    }

    public void initTensorParams() {
        this._tensorParams = new TreeMap<>();
    }

    public void initStringParams() {
        this._tensorParams = new TreeMap<>();
    }

    public void initTensorRefs(){this._tensorRefs = new TreeMap<>();}

  public void addTensorParam(String key, Tensor shape){
       if(_tensorParams==null)
           initTensorParams();
       Tensor val = new Tensor(shape);
       _tensorParams.put(key,val);
    }

    public void addTensorParam(String key, int ... shape){
       if(_tensorParams==null)
           initTensorParams();
       Tensor val = new Tensor(shape);
       _tensorParams.put(key,val);
    }

    public void addIntParam(String key, Integer val){
       if(_intParams==null)
           initIntParams();
       _intParams.put(key,val);
    }

    public void addStringParam(String key, String val){
       if(_stringParams==null)
           initStringParams();
       _stringParams.put(key,val);
    }

    public void addTensorRef(String key, String val){
       if(_tensorRefs==null)
           initTensorRefs();
       _tensorRefs.put(key,val);
    }


    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    @SerializedName("name")  private String _name;
    @SerializedName("tensorParams")  private TreeMap<String, Tensor> _tensorParams = new TreeMap<>();
    @SerializedName("intParams")  private TreeMap<String, Integer> _intParams = new TreeMap<>();
    @SerializedName("floatParams")  private TreeMap<String, Float> _floatParams = null;
    @SerializedName("stringParams")  private TreeMap<String, String> _stringParams = new TreeMap<>();
    @SerializedName("basic")  private String _basic;
    @SerializedName("timeComplexity")  private Long _timeComplexity = 1l;
    @SerializedName("concat")  private boolean _concat = false;
    /** non-trivial tensor references*/
    @SerializedName("tensorRefs")  private TreeMap<String, String> _tensorRefs = null;

}
