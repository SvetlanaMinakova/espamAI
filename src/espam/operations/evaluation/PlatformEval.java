package espam.operations.evaluation;

import com.google.gson.annotations.SerializedName;

import java.util.Vector;

public class PlatformEval {

    public CoreTypeEval getCoreTypeEval(String procname){
        for(procDescription p : _procDescriptions){
            if(p.get_name().equals(procname)) {
                CoreTypeEval c = _getCoreTypeEval(p.get_type(), p.get_subtype());
                if(c!=null)
                    return c;
            }

        }
        System.out.println("Time eval error: time evalution for processor: " + procname + " not found");
        return null;
    }

    private CoreTypeEval _getCoreTypeEval(String proctype, String procSubtype){
        for(CoreTypeEval c: _coreTypesTimeEvals){
            if(c.get_type().equals(proctype) && c.get_subtype().equals(procSubtype))
                return c;

        }
        return null;
    }

    @SerializedName("name") private String name;
    @SerializedName("version") private String version;
    @SerializedName("peak_performance") private String peak_performance;
    @SerializedName("global_memory_size") private String global_memory_size;
    @SerializedName("kernel_memory_size") private String kernel_memory_size;

   /** evaluation of operators execution time for each core type*/
    @SerializedName("core_types") private  Vector<CoreTypeEval> _coreTypesTimeEvals = new Vector<>();

    /** processors list descriptions*/
    @SerializedName("cores") private  Vector<procDescription> _procDescriptions = new Vector<>();
}
