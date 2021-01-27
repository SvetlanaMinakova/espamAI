package espam.operations.evaluation.platformDescription;

import com.google.gson.annotations.SerializedName;

/** communication channel description for power/performance evaluation*/
public class channelDescription {

    /** create new channels description*/
    public channelDescription(Integer srcId, Integer dstId, double bandwidth){



    }

    /** time, required to transfer one data element from one proc. to another*/
    private double _timePerDataElement = 0;

    /** connection bandwidth MB/s*/
    @SerializedName("bandwidth") private double _bandwidth;

    /** id of the source processor (CPU/GPU/FPGA, etc.)*/
    @SerializedName("src") private Integer _src;

    /** id of the destination processor (CPU/GPU/FPGA, etc.)*/
    @SerializedName("dst") private Integer _dst;

}
