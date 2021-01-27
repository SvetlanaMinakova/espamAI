package espam.operations.evaluation.platformDescription;

import com.google.gson.annotations.SerializedName;

public class ProcDescription {

    /////////////////////////////////////////////////////////////////////
    ////                         public methods                     ////

    public String get_name() {
        return _name;
    }

    public String get_type() {
        return _type;
    }

    public String get_subtype() {
        return _subtype;
    }

    public int get_id() { return _id; }

    public String get_host() { return _host; }

    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    /**name */
    @SerializedName("name")private String _name = "";

    /** id */
    @SerializedName("id")private Integer _id;

    /** core type name */
    @SerializedName("type")private String _type = "";

    /** core type name */
    @SerializedName("subtype")private String _subtype = "";

    /** host - for accelerators only: a CPU,
     * responsible for launching kernels on this accelerator*/
    @SerializedName("host")private String _host = "";

}
