package espam.datamodel.mapping.DNNMapping;


import com.google.gson.annotations.SerializedName;

import java.util.Vector;

/** TODO: attach high-throughput mapping procedure? (implemented in Python)*/
public class DNNGAMapping {


    @SerializedName("processor_names")private Vector<Vector<String>> _mapping;

}
