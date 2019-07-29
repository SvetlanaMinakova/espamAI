package espam.operations.transformations.csdf_model_transformations;

import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.CSDFEdge;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**TODO finish implementation
 * Class implements transformations over CSDF model
 */

public class CSDFTransformer {

     /**
     * empty constructor is not allowed for the transformer
     */
    private CSDFTransformer(){}

    /**
     * Create new CSDFTransformer
     * @param csdfg CSDF graph to be transformed
     */
    public CSDFTransformer(CSDFGraph csdfg){
        _csdfg = csdfg;
    }

    /**
     * Incapsulate concatenation nodes of the generated CSDF graph
     * into their output nodes
     */
    public void incapsulateConcatNodes(){
        Vector<CSDFNode> concatNodesList = _getConcatNodesList();
        for(CSDFNode concat: concatNodesList){
            try{
                _incapsulateConcatNode(concat);
            }
            catch (Exception e){
                System.err.println(concat.getName() + " (id = "+concat.getId()+") incapsulation error!");
            }
        }

    }

    /**
     * Incapsulate concatenation node into it's output node
     * @param concat concatenation node
     */
    private void _incapsulateConcatNode(CSDFNode concat) throws Exception {

        Vector<CSDFEdge> concatOutputEdges = _csdfg.getNodeOutputEdges(concat.getId());
        switch (concatOutputEdges.size()) {
            case 0: break;
            case 1:
                {
                    CSDFEdge concatOutputEdge = concatOutputEdges.firstElement();
                    _incapsulateConcatNodeSingleOutput(concat, concatOutputEdge);
                    break;
                }
            default:
                {
                    _incapsulateConcatNodeMultipleOutputs(concat, concatOutputEdges);
                    break;
                }
        }

    }

        /**
     * Incapsulate concatenation node into it's output node
     * @param concat concatenation node
     */
    private void _incapsulateConcatNodeSingleOutput(CSDFNode concat, CSDFEdge concatOutputEdge) throws Exception {

        // CSDFEdge concatOutputEdge = null;
        //_csdfg.getFirstOutputEdge(concat.getId());

           CSDFNode concatOutputNode = (CSDFNode) concatOutputEdge.getDst().getNode();

           Vector<CSDFPort> concatInPorts = _getConcatSortedInputPorts(concat);

           _csdfg.removeLink(concat, concatOutputNode, concatOutputEdge.getName());

           for (CSDFPort concatInPort : concatInPorts)
                concatInPort.getEdge().changeDst(concatOutputNode);

           // CSDFEdge concatToOutputLink = _csdfg.getFirstOutputEdge(concat.getId());

            concatOutputNode.setConcat(true);

            _csdfg.getNodeList().remove(concat);
    }

    /**
     * Incapsulate concatenation node into it's output node
     * @param concat concatenation node
     */
    private void _incapsulateConcatNodeMultipleOutputs(CSDFNode concat, Vector<CSDFEdge> concatOutputEdges) throws Exception {

    /**  for (CSDFEdge concatOutputEdge: concatOutputEdges){
            CSDFNode concatOutputNode = (CSDFNode) concatOutputEdge.getDst().getNode();
            _csdfg.broadcastToNewDst(concatOutputEdge,concatOutputNode);
            concatOutputNode.setConcat(true);
         //   _csdfg.removeLink(concat, concatOutputNode, concatOutputEdge.getName());
     } */

    //System.out.println(concat.getName()+" inp_edges num: "+_csdfg.getNodeInputEdges(concat.getId()).size());
        Vector<CSDFEdge> concatSortedInputs = _getConcatSortedInputs(concat);

        for (CSDFEdge inputEdge: concatSortedInputs){
        for (CSDFEdge concatOutputEdge: concatOutputEdges) {
            CSDFNode concatOutputNode = (CSDFNode) concatOutputEdge.getDst().getNode();
            _csdfg.removeLink((CSDFNode)inputEdge.getSrc().getNode(),concat,inputEdge.getName());
            _csdfg.broadcastToNewDst(inputEdge,concatOutputNode);
            concatOutputNode.setConcat(true);
        }

    }

    for (CSDFEdge concatOutputEdge: concatOutputEdges) {
         CSDFNode concatOutputNode = (CSDFNode) concatOutputEdge.getDst().getNode();
         concatOutputNode.setConcat(true);
        _csdfg.removeLink(concat, concatOutputNode, concatOutputEdge.getName());
    }
        _csdfg.getNodeList().remove(concat);
    }


    /**
     *
     * @param concatNodeId id of the concat node
     * @return non-concat node, following the concat node
     */
    private CSDFNode _getConcatOutputNode(int concatNodeId) throws Exception{
        CSDFEdge concatOutput = _csdfg.getFirstOutputEdge(concatNodeId);
        CSDFNode output = (CSDFNode)concatOutput.getDst().getNode();
        return output;
    }

    private Vector<CSDFEdge> _getConcatSortedInputs(CSDFNode concat){
        Vector<CSDFEdge> sortedInputs = new Vector<>();
        Vector<CSDFEdge> edges = _csdfg.getNodeInputEdges(concat.getId());
        Vector<CSDFPort> sortedInPorts = _getConcatSortedInputPorts(concat);

        for(CSDFPort sortedInPort: sortedInPorts){
            for (CSDFEdge edge: edges){
                if(edge.getDst().equals(sortedInPort))
                    sortedInputs.add(edge);
            }
        }

        return sortedInputs;
    }

    /**
     * Get inputs sorted in concatenation order
     * @param node CSDF node
     * @return input ports, sorted in concatenation order
     */
    private Vector<CSDFPort> _getConcatSortedInputPorts(CSDFNode node){
        Vector<CSDFPort> sortedInputPorts = new Vector<>();

        HashMap<MemoryUnit,CSDFPort> inputs = new HashMap<>();

        for(CSDFPort inport: node.getNonOverlapHandlingInPorts()){
            MemoryUnit mu = inport.getAssignedMemory();
           // System.out.println(mu.getName());
            if(mu!=null){
                inputs.put(mu,inport);
            }
        }

        for(MemoryUnit mu: node.getMemoryUnits()){
            if(inputs.containsKey(mu))
                sortedInputPorts.add(inputs.get(mu));
        }

        //for(CSDFPort p: sortedInputPorts)
        //  System.out.println(p.getName());

        return sortedInputPorts;
    }

    /**
     * Get list of concatenation nodes
     * @return list of concatenation nodes
     */
    private Vector<CSDFNode> _getConcatNodesList(){
        Vector<CSDFNode> concatNodesList = new Vector<>();
        Iterator i;
        i = _csdfg.getNodeList().iterator();
        while( i.hasNext() ) {
            CSDFNode node = (CSDFNode) i.next();
            if(node.getOperation()!=null) {
                if (node.getOperation().toLowerCase().equals("concat")) {
                    concatNodesList.add(node);
                }
            }
        }
        return concatNodesList;
    }


    ///////////////////////////////////////////////////////////////////
    ////                       private variables                  ////

    /** CSDF graph */
    private CSDFGraph _csdfg;
}
