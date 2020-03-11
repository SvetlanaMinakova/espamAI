package espam.visitor.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.*;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.operations.evaluation.CSDFTimingRefiner;
import espam.parser.json.csdf.*;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class CSDFGraphJSONVisitor extends CSDFGraphVisitor{

    /**
     * Call SDF json visitor
     * @param graph SDF graph to be visited
     * @param printStream output .json file printstream
     */
    public static void callVisitor(CSDFGraph graph, PrintStream printStream){

           try {
            CSDFGraphJSONVisitor CSDFGraphJSONVisitor = new CSDFGraphJSONVisitor(printStream);
            graph.accept(CSDFGraphJSONVisitor);
         //   System.out.println("JSON file generated: " + graph.getName() + ".json");
            }
            catch(Exception e) {
             System.err.println("JSON SDFG visitor fault. " + e.getMessage());
            }
    }

     /**
     * Call SDF json visitor
     * @param graph SDF graph to be visited
     * @param dir directory for .json file corresponding to visited dnn
     */
    public static void callVisitor(CSDFGraph graph, String dir) {
        try {
            PrintStream printStream = JSONFileWorker.openFile(dir, graph.getName(), "json");
            CSDFGraphJSONVisitor CSDFGraphJSONVisitor = new CSDFGraphJSONVisitor(printStream);
            graph.accept(CSDFGraphJSONVisitor);
            System.out.println("JSON file generated: " + dir + graph.getName() + ".json");
            printStream.close();
        } catch (Exception e) {
            System.err.println("JSON SDFG visitor fault. " + e.getMessage());
        }
    }

     /**
     * Constructor for the CSDFGraphJSONVisitor object
     * @param  printStream work I/O stream of the visitor
     */
    public CSDFGraphJSONVisitor(PrintStream printStream) {
        _printStream = printStream;

         /** create custom adaptor for any type of port*/
        PortConverter CSDFPortConverter = new PortConverter();

        /**create standard parser*/
        GsonBuilder builder = new GsonBuilder()
        /**
         * register all SDFModel adaptors*/
        .registerTypeAdapter(NPort.class, CSDFPortConverter)
        .registerTypeAdapter(CSDFPort.class, CSDFPortConverter)
        .registerTypeAdapter(CSDFNode.class,new NodeConverter())
        .registerTypeAdapter(CSDFEdge.class,new EdgeConverter())
        .registerTypeAdapter(CSDFGraph.class,new GraphConverter())
        .setPrettyPrinting();
        _gson = builder.create();
    }

    /**
     * Visit SDFGraph
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFGraph x) {
        /** align graph parameters  */
        x.alignRatesLength();
        /**wcets*/
        _wcet = CSDFTimingRefiner.getInstance().getExecTimes(x);
        /**Open graph description */
        _printStream.println("{");
        _prefixInc();
        _printStream.println(_prefix + "\"type\": \"" + x.getType() + "\",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName() + "\",");
        _printStream.println(_prefix + "\"node_number\": " + x.countNodes() + ",");
        _printStream.println(_prefix + "\"edge_number\": " + x.countEdges() + ",");
        _prefixInc();

        /** add nodes*/

        Iterator i;
        /**Visit all nodes*/
        Vector<CSDFNode> nodes = x.getNodeList();
        CSDFNode node;
        int commaBorder = nodes.size()-1;
        _printStream.println(_prefix + "\"nodes\": [");
        prefixInc();

        i = nodes.iterator();
        while (i.hasNext()) {
            node = (CSDFNode) i.next();
            node.accept(this);
            if(commaBorder>0) {
                _printStream.println(_prefix + ",");
                commaBorder--;
            }
        }

        _prefixDec();
        /**Close nodes description*/
        _printStream.println("");
        _printStream.println(_prefix + "],");

         /**Visit all edges*/
        Vector<Edge> edges = x.getEdgeList();
        Edge edge;
        commaBorder = edges.size()-1;
       _printStream.println(_prefix + "\"edges\": [");

       prefixInc();
        i = x.getEdgeList().iterator();
        while (i.hasNext()) {
            edge = (Edge) i.next();
            edge.accept(this);
            if(commaBorder>0) {
                _printStream.println(_prefix + ",");
                commaBorder--;
            }
        }
        _prefixDec();
         /**Close connections description*/
        _printStream.println("");
        _printStream.println(_prefix + "]");

        /**Close network description*/
        _printStream.println("}");
    }


    /**
     * Visit CSDFNode
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFNode x){
        /**Open node description */
        _printStream.println(_prefix + "{");
         prefixInc();
        _printStream.println(_prefix + "\"id\": " + x.getId()+",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");
        _printStream.println(_prefix + "\"function\": \"" + x.getFunction()+"\",");
        if(x.getGroup()!=null)
            _printStream.println(_prefix + "\"group\": \"" + x.getGroup()+"\",");
        _printStream.println(_prefix + "\"length\": " +x.getLength()+ ",");
        _printStream.println(_prefix + "\"port_number\": "+x.countPorts()+",");
        String jsonOperator = _gson.toJson(x.getOperator(), Operator.class);
      //  System.out.println(jsonOperator);
        _printStream.println(_prefix + "\"operator\": " + jsonOperator + ",");

         Vector<Long> wcet = _wcet.get(x);
         if(wcet==null)
             wcet = CSDFTimingRefiner.getInstance().getDefaultExecTime(x.getLength());

        //JsonArray wcetArray = CSDFSupportResolver.serializeAsArray(x.getWcet());
         _printStream.println(_prefix + "\"wcet\":" + wcet + ", ");

        /** add ports*/

        Iterator i;
        /**Visit all ports*/
        Vector<NPort> ports = x.getPortList();
        NPort port;
        int commaBorder = ports.size()-1;
        _printStream.println(_prefix + "\"ports\": [");
        prefixInc();

        i = ports.iterator();
        while (i.hasNext()) {
            port = (NPort) i.next();
            port.accept(this);
            if(commaBorder>0) {
                _printStream.print(",");
                commaBorder--;
            }
        }
        _printStream.println("");
        /**Close ports description*/
        prefixDec();
        _printStream.println(_prefix + "]");
        _prefixDec();
        /**Close node description */
        _printStream.println(_prefix + "}");
    }

     /**
     * Visit Edge
     * @param  x A Visitor Object.
     */
    public void visitComponent(Edge x){
        if(x instanceof CSDFEdge)
            visitComponent((CSDFEdge) x);
             else
        System.err.println("SDFG parsing error: unacceptable edge type, "+x.toString());
    }
      /**
     * Visit CSDFEdge
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFEdge x){
        /**Open edge description */
        _printStream.println(_prefix + "{");
        prefixInc();
        _printStream.println(_prefix + "\"id\": " + x.getId()+",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");
        //String strSrc = _gson.toJson(x.getSrcId());
        String strSrc = "[" + x.getSrcId()[0] + "," + x.getSrcId()[1] + "]";
        _printStream.println(_prefix + "\"src\": " + strSrc+",");
        //String strDst = _gson.toJson(x.getDstId());
        String strDst = "[" + x.getDstId()[0] + "," + x.getDstId()[1] + "]";
        _printStream.print(_prefix + "\"dst\": " + strDst);
        if(x.getDst().isOverlapHandler()){
            _printStream.println(",");
            Integer initTokes = _getOverlapInitTokens(x.getDst().getRates());
            _printStream.println(_prefix + "\"initial_tokens\": " + initTokes);
        }

         /**Close edge description */
         prefixDec();
        _printStream.println(_prefix + "}");

    }

    /**
     * Get init tokens for Overlap self-loop (required by DARTs)
     * @param rates rates of self-loop (overlapping processor) channel
     * @return init tokens for Overlap self-loop
     */
    private Integer _getOverlapInitTokens(Vector<IndexPair>rates){
        Integer maxRate = 0;
        for(IndexPair rate: rates){
            if(rate.getFirst()>maxRate)
                maxRate=rate.getFirst();
        }
        return maxRate;
    }

    /**
     * Visit NPort
     * Print a line for the port in the correct json format, if the port is CSDFPort
     * @param  x NPort to visit
     */
    public void visitComponent(NPort x) {
        if(x instanceof CSDFPort)
            visitComponent((CSDFPort) x);
        else
            System.err.println("SDFG parsing error: unacceptable port type, "+x.toString());
    }

     /**
     * Visit NPort
     * Print a line for the port in the correct json format.
     * @param  x NPort to visit
     */
    public void visitComponent(CSDFPort x) {
        /**Open port description */
        _printStream.println("");
        _printStream.println(_prefix + "{");
        _prefixInc();
        _printStream.println(_prefix + "\"id\": " + x.getId()+",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");

        Vector<IndexPair> rateDesc = x.getRates();
        int sumPhases = 0;
        for(IndexPair ip: rateDesc)
            sumPhases +=ip.getSecond();

        JsonArray rate = CSDFSupportResolver.serializeIndexPairsAsArray(x.getRates());
        _printStream.println(_prefix + "\"rate\": " + rate +",");

        if(x.getType().equals(CSDFPortType.in))
            _printStream.println(_prefix + "\"type\": \"in\"");

        if(x.getType().equals(CSDFPortType.out))
            _printStream.println(_prefix + "\"type\": \"out\"");

        _prefixDec();

        /**Close port description */
        _printStream.print(_prefix + "}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///
   /**Standard JSON-parser, implements parsing of non-nested types*/
    private Gson _gson;
   /** worst-case execution times. By default operation wcet = 1*/
    private HashMap<CSDFNode,Vector<Long>> _wcet;
}
