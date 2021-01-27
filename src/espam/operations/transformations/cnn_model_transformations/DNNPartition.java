package espam.operations.transformations.cnn_model_transformations;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

/** DNN partition*/
public class DNNPartition {

    public int get_procId() {
        return _procId;
    }

    public String get_procName() {
        return _procName;
    }

    public DNNPartition(){
        _layers = new Vector<>();
    }

    public DNNPartition(Vector<String> layers){
        _layers = layers;
    }

    public DNNPartition(Vector<String> layers, int procId, String procName){
        _layers = layers;
        _procId = procId;
        _procName = _procName;
    }

    /** Get list of layers*/
    public Vector<String> getLayers() {
        return _layers;
    }

    @SerializedName("layers")private Vector<String> _layers;
    @SerializedName("proc_id")private int _procId = 0;
    @SerializedName("proc_name")private String _procName = "CPU0";
}
