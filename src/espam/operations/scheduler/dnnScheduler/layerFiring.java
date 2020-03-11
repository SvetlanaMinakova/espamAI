package espam.operations.scheduler.dnnScheduler;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.mapping.MProcessor;

import java.util.Vector;

/** Class, describing CNN layer firing on multiple MProcessors*/
public class layerFiring {

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public void addProcessor(MProcessor MProcessor) {
        this.MProcessors.add(MProcessor);
    }

    public void addWorkloadPercentage(Double workloadPercentage) {
        this.workloadPercentages.add(workloadPercentage);
    }

    public void setMProcessors(Vector<MProcessor> MProcessors) {
        this.MProcessors = MProcessors;
    }

    public void setWorkloadPercentages(Vector<Double> workloadPercentages) {
        this.workloadPercentages = workloadPercentages;
    }

    public Layer getLayer() {
        return layer;
    }

    public Vector<MProcessor> getProcessors() {
        return MProcessors;
    }

    public Vector<Double> getWorkloadPercentages() {
        return workloadPercentages;
    }

    public Double getWorkloadPercentage(Integer id) {
        return workloadPercentages.elementAt(id);
    }

    public Double getWorkloadPercentage(MProcessor proc) {
        Integer id = MProcessors.indexOf(proc);
        if(id==-1)
            return 0.0;
        else return getWorkloadPercentage(id);
    }

    /** CNN layer to execute*/
    private Layer layer;

    /** MProcessors to execute layer*/
    private Vector<MProcessor> MProcessors = new Vector<>();

    /** workload percentages*/
    private Vector<Double> workloadPercentages = new Vector<>();

}
