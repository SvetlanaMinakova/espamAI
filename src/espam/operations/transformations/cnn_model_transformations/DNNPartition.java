package espam.operations.transformations.cnn_model_transformations;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

/** DNN partition*/
public class DNNPartition {

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
    @SerializedName("procId")private int _procId = 0;
    @SerializedName("procName")private String _procName = "CPU0";
}
