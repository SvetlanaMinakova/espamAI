package espam.operations.transformations.cnn_model_transformations;

import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.Network;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.cnn.neurons.neurontypes.DataType;

import java.util.Iterator;
import java.util.Vector;

/**
 * Groups CNN layers into blocks
 * **/
public class DNNBlockGrouper {


    /** find CNN blocks of parallel branches, that are connected by a concat node
     * @param dnn dnn to search in
     * @return CNN blocks of parallel branches, that are connected by a concat node
     */
    public Vector<DNNPartition> findDNNConcatBlockGroups(Network dnn){
        Vector<DNNPartition> concatGroups = new Vector<>();
        dnn.updateDataFormats();
        dnn.sortLayersInTraverseOrder();
        Vector<Layer> layersInTraverseOrder = dnn.getLayers();
        if (layersInTraverseOrder.size() == 0)
            return concatGroups;

        String groupStart = null;
        String groupEnd = null;

        DNNPartition partition = new DNNPartition();
        Iterator<Layer> i;
        i = layersInTraverseOrder.iterator();
        Layer l = i.next();


        while( i.hasNext() ) {

            if (_isGroupStart(l)) {
                if (groupStart == null) {
                    groupStart = l.getName();
                }
                else groupEnd = l.getName();
            }

            if (_isGroupEnd(l))
                groupEnd = l.getName();

            if (groupStart!=null && groupEnd!=null){
                if (!partition.getLayers().contains(l.getName()))
                    partition.getLayers().add(l.getName());

                concatGroups.add(partition);
                partition = new DNNPartition();
                groupStart = null;
                groupEnd = null;

            }
            partition.getLayers().add(l.getName());
            l = i.next();
        }

        /** add last partition*/
        partition.getLayers().add(dnn.getOutputLayer().getName());
        concatGroups.add(partition);

        return concatGroups;
    }


    /**
     * Check if layer is a start of parallel branches group. Layer starts parallel
     * branches group if
     * - 1) it is an input layer
     * - 2) it has multiple outputs (output connections)
     * @param layer layer to be checked
     * @return true if layer is a start of parallel branches group and false otherwise
     */
    private boolean _isGroupStart(Layer layer){
        if(_isInputLayer(layer))
            return true;

        if (layer.getOutputConnections().size()>1)
            return true;
        return false;
    }


    /**
     * Check if layer is an end of parallel branches group. Layer ends parallel
     * branches group if it has multiple inputs (input connections) or is output data layer
     * @param layer layer to be checked
     * @return true if layer is an end of parallel branches group and false otherwise
     */
    private boolean _isGroupEnd(Layer layer){
        if (_isOutputLayer(layer))
            return true;
        if (layer.getInputConnections().size()>1)
            return true;
      //  if(_isInputLayer(layer) && layer.getOutputConnections().size()>1)
        //    return true;
        return false;
    }

    private boolean _isInputLayer(Layer layer){
        if (layer.getNeuron().getName().toString() == DataType.INPUT.toString())
            return true;
        return false;
    }

    private boolean _isOutputLayer(Layer layer){
        if (layer.getNeuron().getName().toString() == DataType.OUTPUT.toString())
            return true;
        return false;
    }


}
