package espam.parser.json.evaluation;

import espam.datamodel.graph.cnn.Layer;

import java.util.Vector;

public class TimeEvalBenchmark {

    public void printBenchmark(){
        for (TimeEvalRecord record: records){
            System.out.println(record);
        }
    }

    public void addRecord(TimeEvalRecord record){
        this.records.add(record);
    }

    public TimeEvalRecord findRecord(Layer layer){
        for (TimeEvalRecord r: records){
            if (r.layer.equals(layer))
                return r;
        }
        return null;
    }

    /**
     * Get records that have specified record name
     * @param recordName record name
     * @return set of records, that have specified record name
     */
    public Vector<TimeEvalRecord> getRecordsWithRecordName(String recordName){
        Vector<TimeEvalRecord> recordsWithName = new Vector<>();
        for (TimeEvalRecord record: records){
            if (record.record.equals(recordName))
                recordsWithName.add(record);
        }
        return recordsWithName;
    }

    /**
     * Get records that have specified operator
     * @param opName operator name
     * @return set of records, that have specified operator name
     */
    public Vector<TimeEvalRecord> getRecordsWithOperator(String opName){
        Vector<TimeEvalRecord> recordsWithOp = new Vector<>();
        for (TimeEvalRecord record: records){
            if (record.layer.getNeuron().getName().equals(opName))
                recordsWithOp.add(record);
        }
        return recordsWithOp;
    }



    public Vector<TimeEvalRecord> records = new Vector<>();

}
