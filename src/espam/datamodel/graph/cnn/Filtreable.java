package espam.datamodel.graph.cnn;

import java.util.Collection;
import java.util.Vector;

/**
 * Interface, allows to filter DNN layers
 */
public interface Filtreable {
    void filterSrcLayers(String layerName,Vector<Layer> filtered, Network network);
    void filterDstLayers(String layerName,Vector<Layer> filtered, Network network);
    void filterDataLayers(Layer layer,Vector<Integer> filteredIds);
}
