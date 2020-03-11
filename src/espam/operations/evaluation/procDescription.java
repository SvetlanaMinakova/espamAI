package espam.operations.evaluation;

import com.google.gson.annotations.SerializedName;

public class procDescription {

    public String get_name() {
        return _name;
    }

    public String get_type() {
        return _type;
    }

    public String get_subtype() {
        return _subtype;
    }

    /**name */
    @SerializedName("name")private String _name = "";

    /** id */
    @SerializedName("id")private String _id = "";

    /** core type name */
    @SerializedName("type")private String _type = "";

    /** core type name */
    @SerializedName("subtype")private String _subtype = "";

}
