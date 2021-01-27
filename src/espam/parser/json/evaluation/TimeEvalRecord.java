package espam.parser.json.evaluation;

import espam.datamodel.graph.cnn.Layer;

public class TimeEvalRecord implements Comparable<TimeEvalRecord> {
    ///////////////////////////////////////////////////////////////////////
    ////                         public methods                       ////

    public TimeEvalRecord(Layer layer){
        this.layer = layer;
    }

    public TimeEvalRecord(Layer layer, String record){
        this.layer = layer;
        this.record = record;
    }

    @Override
    public String toString() {
        return "{" +
                "layer=" + layer.getName() +
                ", record=" + record +
                ", time=" + timeEval +
                '}';
    }

    /** compare time records
     * @param record other record
     * @return comparsion result
     */
    public int compareTo(TimeEvalRecord record){
        if(this.timeEval==record.timeEval)
            return 0;
        if(this.timeEval>record.timeEval)
            return 1;
        return -1;
    }


    ///////////////////////////////////////////////////////////////////////
    ////                         public variables                     ////
    public Layer layer;
    public String record = "";
    public Double timeEval = 0.0;
    public boolean recordSet = false;
}
