package espam.datamodel.graph.csdf;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.Graph;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CSDFGraphVisitor;
import java.util.Iterator;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// CSDFGraph

/**
 * This class describes an Synchronous Dataflow Graph (SDFG)
 * SDFG is a restriction of Kahn process networks, nodes produce and consume
 * a fixed number of data items per firing.
 * The full description could be found in  Edward A. Lee and David G. Messerschmitt
 * Proceedings of the IEEE, vol. 75, no. 9, p 1235-1245, September, 1987.
 *
 * @author minakovas
 */


public class CSDFGraph extends Graph implements ReferenceResolvable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Constructor to create an CSDFGraph with a specified name
     * @param name name of an SDF graph
     * */
    public CSDFGraph(String name) {
        super(name);
    }

        /**
     * Constructor to create an CSDFGraph with a specified name
     * @param name name of an SDF graph
     * */
    public CSDFGraph(String name, SDFGraphType type) {
        super(name);
        setType(type);
    }

    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CSDFGraphVisitor x) { x.visitComponent(this); }

    /**
     *  Clone this CSDFGraph
     * @return  a new reference on the CSDFGraph.
     */
    public Object clone() {
        CSDFGraph newObj = (CSDFGraph) super.clone();
        newObj.setType(_type);
        newObj.setTokenDesc(_tokenDesc);
        return (newObj);
    }

     /**
      * Align each graph node rates according to calculated
      * node length property
      */
    public void alignRatesLength(){
        for(Object node:getNodeList()){
            ((CSDFNode)node).alignRatesLength();
        }
    }

    /**
     * Return description of SDF graph
     * @return description of SDF graph
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
      * Compares CSDFGraph with another object
      * @param obj Object to compare this SDFGrpah with
      * @return true if CSDFGraph is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

        CSDFGraph graph = (CSDFGraph)obj;
        return this.getName().equals(graph.getName())
              && isNodeListsEqual(graph)
              && isEdgeListsEqual(graph);
       }

    /**
     * Links two SDF actors. Creates new ports on a start and end nodes
     * with correspondence Nodenames and a new Edge with a default edgeName
     * @param start start CSDFNode
     * @param end end CSDFNode
     * @param prodRate start node production rate
     * @param consRate end node consumption rate
     */

    public void addLink(CSDFNode start, CSDFNode end, int prodRate, int consRate) {
        String edgeName = start.getName()+"_to_"+end.getName();
        addLink(start,end,prodRate,consRate,edgeName);
    }

      /**
     * Links two SDF actors. Creates new ports on a start and end nodes
     * with correspondence Nodenames and a new Edge with a default edgeName
     * @param start start CSDFNode
     * @param end end CSDFNode
     * @param prodRates start node production rates
     * @param consRates end node consumption rates
     */

    public void addLink(CSDFNode start, CSDFNode end, Vector<IndexPair> prodRates, Vector<IndexPair> consRates) {
        String edgeName = start.getName()+"_to_"+end.getName();
        addLink(start,end,prodRates,consRates,edgeName);
    }

    /**
     * Links two SDF actors. Creates new ports on a start and end nodes
     * with correspondence Nodenames and a new Edge with an edgeName
     * @param start start CSDFNode
     * @param end end CSDFNode
     * @param EdgeName name for a new Edge
     * @param prodRate start node production rate
     * @param consRate end node consumption rate
     */
    public void addLink(CSDFNode start, CSDFNode end, int prodRate, int consRate,String EdgeName) {

        CSDFPort startPort = new CSDFPort(start.getNextOutPortName(),start.getNextPortId(), CSDFPortType.out);
        startPort.addRate(prodRate,1);
        start.addPort(startPort);

        CSDFPort endPort = new CSDFPort(end.getNextInPortName(),end.getNextPortId(),CSDFPortType.in);
        endPort.addRate(consRate,1);
        end.addPort(endPort);

        CSDFEdge newEdge = new CSDFEdge(EdgeName,getNextEdgeId());
        addEdge(newEdge,startPort,endPort);
    }

    /**
     * Links two SDF actors. Creates new ports on a start and end nodes
     * with correspondence Nodenames and a new Edge with an edgeName
     * @param start start CSDFNode
     * @param end end CSDFNode
     * @param EdgeName name for a new Edge
     * @param prodRates start node production rates
     * @param consRates end node consumption rates
     */
    public void addLink(CSDFNode start, CSDFNode end, Vector<IndexPair> prodRates, Vector<IndexPair> consRates, String EdgeName) {

        CSDFPort startPort = new CSDFPort(start.getNextOutPortName(),start.getNextPortId(),CSDFPortType.out);
        startPort.setRates(prodRates);
        start.addPort(startPort);

        CSDFPort endPort = new CSDFPort(end.getNextInPortName(),end.getNextPortId(),CSDFPortType.in);
        endPort.setRates(consRates);
        end.addPort(endPort);

        CSDFEdge newEdge = new CSDFEdge(EdgeName,getNextEdgeId());
        addEdge(newEdge,startPort,endPort);
    }

    /**
     * Remove link between two SDF actors. Deletes corresponding ports and Edge
     * @param start start CSDFNode
     * @param end end CSDFNode
     * @param edgeName name of an Edge to be removed
     * @return true if the link was successfully found and removed and false otherwise
     */
    public boolean removeLink(CSDFNode start, CSDFNode end, String edgeName) {
        Vector<CSDFPort> startOutPorts = start.getOutPorts();
        Vector<CSDFPort> startInPorts = start.getInPorts();
        Edge edgeToRemove;

        for(CSDFPort outport: startOutPorts) {
            for(CSDFPort inport: end.getInPorts()) {
                if(outport.getEdge()==inport.getEdge()) {
                    edgeToRemove = outport.getEdge();
                    if(edgeToRemove.getName().equals(edgeName)) {
                        start.removePort(outport);
                        end.removePort(inport);
                        getEdgeList().remove(edgeToRemove);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates new SDF Edge and Links two selected ports of the SDFG Nodes (Actors) with it
     * @param name name of the Edge
     * @param start start SDF Port of the Edge
     * @param end end SDF Port of the Edge
     */
    public void addEdge (String name, CSDFPort start, CSDFPort end) {
        int edgeId = getNextEdgeId();
        CSDFEdge newEdge = new CSDFEdge(name,edgeId);
        addEdge(newEdge,start,end);
    }

    /**
     * Links two selected ports of the SDFG Nodes (Actors) with an existing CSDFEdge
     * @param edge SDF Edge
     * @param start start SDF Port of the Edge
     * @param end end SDF Port of the Edge
     */
    public void addEdge(CSDFEdge edge, CSDFPort start, CSDFPort end)
    {
        edge.setSrc(start);
        edge.setDst(end);
        start.setEdge(edge);
        end.setEdge(edge);
        getEdgeList().add(edge);
    }

    /**
     * Add new node to the graph's nodeList
     * @param newNode new node to be added into the graph
     */
    public void addNodes(CSDFNode newNode) {
        getNodeList().add(newNode);
    }

    /**
     * Add new nodes to the graph's nodeList
     * @param newNodes new nodes list
     */
    public void addNodes(Vector<CSDFNode> newNodes) {
        getNodeList().addAll(newNodes);
    }

    /**
     * Add new edge to the graph's nodeList
     * @param newEdge new edge
     */
    public void addEdge(CSDFEdge newEdge) {getEdgeList().add(newEdge);}

    /**
     * Add new edges to the graph's nodeList
     * @param newEdges new edges list
     */
    public void addEdges(Vector<CSDFEdge> newEdges) {getEdgeList().addAll(newEdges);}

    /**
     * Generates next unique edge id
     * @return next unique edge id
     */
    public int getNextEdgeId() {
        return getEdgeList().size();
    }

     /**
     * Generates next unique node id
     * @return next unique node id
     */
    public int getNextNodeId() {
        return getNodeList().size();
    }
       /**
     *  Return a node which has a specific name. Return null if
     *  node cannot be found.
     *
     * @param id unique identifier of the node to search for.
     * @return  the node with the specific id.
     */
    public CSDFNode getNode(int id) {
        Iterator i;
        i = getNodeList().iterator();
        while( i.hasNext() ) {
            CSDFNode node = (CSDFNode) i.next();
            if(node.getId()==id) {
                return node;
            }
        }
        return null;
    }

    /**
     * Resolves the references inside the graph after the deserealization
     */
    public void resolveReferences() {
        resolveEdgesReferences();
        resolveNodesReferences();
    }

     /**
     * Get number of CSDFGraph nodes
     * @return number of CSDFGraph nodes
     */
    public int countNodes(){
        return getNodeList().size();
    }

      /**
     * Get number of CSDFGraph edges
     * @return number of CSDFGraph edges
     */
    public int countEdges(){
        return getEdgeList().size();
    }

    /**
     * get type of the sdf graph
     * @return type of the sdf graph
     */
    public SDFGraphType getType() {
        return _type;
    }

    /**
     * Set type of the sdf graph
     * @param type type of the sdf graph
     */
    public void setType(SDFGraphType type) {
        this._type = type;
    }


    /**
     * Get one token description
     * @return one token description
     */
    public String getTokenDesc() { return _tokenDesc; }

    /** Set one token description
     * @param tokenDesc one token description
     */
    public void setTokenDesc(String tokenDesc) {
        this._tokenDesc = tokenDesc;
    }


    /**
     * Get list of supported operators
     * @return list of supported operators
     */
    public Vector<String> getOpListDistinct() {
        Vector<String> opListDistinct = new Vector<>();
        Iterator i = getNodeList().iterator();
        while (i.hasNext()) {
            CSDFNode node = (CSDFNode) i.next();
            String op = node.getOperation();
            if (!opListDistinct.contains(op)) {
                opListDistinct.add(op);
            }
        }
        return opListDistinct;
    }

    /////////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

        /**
        * Compares nodeLists of two CSDFGraphs
        * @param graph second CSDFGraph for comparision
        * @return true, if nodeLists are equal and false otherwise
        */
       private boolean isNodeListsEqual(CSDFGraph graph )
       {
           if(getNodeList().size()!=graph.getNodeList().size())
               return false;

        Iterator i = getNodeList().iterator();
        Iterator i2 = graph.getNodeList().iterator();
        while ( i.hasNext() ) {
            CSDFNode node = (CSDFNode) i.next();
            CSDFNode node2 = (CSDFNode) i2.next();
            if(!node.equals(node2)) {
                return false;
            }
        }
        return true;
       }

       /**
        * Compares edgeLists of two CSDFGraphs
        * @param graph second CSDFGraph for comparision
        * @return true, if edgeLists are equal and false otherwise
        */
       private boolean isEdgeListsEqual(CSDFGraph graph )
       {
           if(getEdgeList().size()!=graph.getEdgeList().size())
               return false;

        Iterator i = getEdgeList().iterator();
        Iterator i2 = graph.getEdgeList().iterator();
        while ( i.hasNext() ) {
            CSDFEdge edge = (CSDFEdge) i.next();
            CSDFEdge edge2 = (CSDFEdge) i2.next();
            if(!edge.equals(edge2)) {
                return false;
            }
        }
        return true;
       }



    ///////////////////////////////////////////////////////////////////////
    //// resolve references inside the graph after the deserialization ////


    /**
     * Resolves the references inside the graph's edges after the deserealization
     * @throws EspamException
     */
    private void resolveEdgesReferences() {
        for(Edge edge:getEdgeList()) {
            try {
                resolveEdgeSrcReferences((CSDFEdge) edge);
                resolveEdgeDstReferences((CSDFEdge) edge);
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Resolves the source-node reference
     * inside one graph's edge after the deserealization
     * @throws EspamException if the reference could not be resolved
     */
    private void resolveEdgeSrcReferences(CSDFEdge edge) throws EspamException {
        int srcNodeId = edge.getSrcId()[0];
        CSDFNode srcNode = getNode(srcNodeId);
        if(srcNode==null)
            throw new EspamException("source node "+srcNodeId+ " reference resolving error, node not found");

        int srcPortId = edge.getSrcId()[1];
        CSDFPort srcPort = (CSDFPort) srcNode.getPort(srcPortId);
        if(srcPort==null)
                throw new EspamException("source node "+srcNodeId+ " reference resolving error, port"+srcPortId+" not found");

        edge.setSrc(srcPort);
    }
     /**
     * Resolves the sink-node reference
     * inside one graph's edge after the deserealization
     * @throws EspamException if the reference could not be resolved
     */
    private void resolveEdgeDstReferences(CSDFEdge edge) throws EspamException {
        int dstNodeId = edge.getDstId()[0];
        CSDFNode dstNode = getNode(dstNodeId);
        if(dstNode==null){
            System.err.println("destination node "+dstNodeId+ " reference resolving error, node not found");
            throw new EspamException("SDF Edge parse exception");
        }

        int dstPortId = edge.getDstId()[1];
        CSDFPort dstPort =(CSDFPort)dstNode.getPort(dstPortId);
        if(dstPort==null) {
            System.err.println("destination node " + dstNodeId +
                    " reference resolving error, port" + dstPortId + " not found");
                throw new EspamException("SDF Edge parse exception");
        }

        edge.setDst(dstPort);
    }
    /**
     * Resolves the references inside the graph's nodes after the deserealization
     * @throws EspamException if an error occurs
     */
    private void resolveNodesReferences() {
        for(Object node:getNodeList()) {
            try {  resolveNodeReferences((CSDFNode) node); }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

     /**
     * Resolves the references inside one grEaph's CSDFNode
     * after the deserealization
     * @param node CSDFNode with unresolved references
     * @return CSDFPort port with specified owner(node) and id
     */
    private void resolveNodeReferences(CSDFNode node) throws EspamException {
        for(NPort port: node.getPortList()) {
            resolvePortReferences((CSDFPort)port);
        }
    }

    /**
     * Resolves the CSDFPort reference
     * inside one graph's edge after the deserealization
     * @param port CSDFPort with unresolved references
     * @return CSDFPort port with specified owner(node) and id
     */
    private void resolvePortReferences(CSDFPort port) throws EspamException{
        int nodeId = port.getNodeId();
        CSDFNode node = getNode(nodeId);
        if(node==null)
            throw new EspamException("port "+port.getId()+" reference resolving error, node "+ nodeId + " not found");
        port.setNode(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ///
    /**Type of sdf graph, sdf by default */
    @SerializedName("type")private SDFGraphType _type = SDFGraphType.sdf;
    /**One token value type description */
    @SerializedName("tokenDecs")private String _tokenDesc = "float";
}