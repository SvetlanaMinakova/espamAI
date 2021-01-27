package espam.visitor.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import espam.datamodel.graph.Edge;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.*;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.parser.json.csdf.*;
import espam.utils.fileworker.JSONFileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

public class CSDFGraphShortJSONVisitor extends CSDFGraphVisitor {

    /**
     * Call SDF json visitor
     * @param graph SDF graph to be visited
     * @param printStream output .json file printstream
     */
    public static void callVisitor(CSDFGraph graph, PrintStream printStream){

           try {
            CSDFGraphShortJSONVisitor CSDFGraphShortJSONVisitor = new CSDFGraphShortJSONVisitor(printStream);
            graph.accept(CSDFGraphShortJSONVisitor);
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
            PrintStream printStream = JSONFileWorker.openFile(dir, graph.getName() + "_short", "json");
            CSDFGraphShortJSONVisitor CSDFGraphShortJSONVisitor = new CSDFGraphShortJSONVisitor(printStream);
            graph.accept(CSDFGraphShortJSONVisitor);
            System.out.println("JSON file generated: " + dir + graph.getName() + "_short.json");
            printStream.close();
        } catch (Exception e) {
            System.err.println("JSON SDFG visitor fault. " + e.getMessage());
        }
    }

     /**
     * Constructor for the CSDFGraphShortJSONVisitor object
     * @param  printStream work I/O stream of the visitor
     */
    public CSDFGraphShortJSONVisitor(PrintStream printStream) {
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
        //_wcet = CSDFTimingRefiner.getInstance().getExecTimes(x);
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
        //_printStream.println(_prefix + "\"function\": \"" + x.getFunction()+"\",");
        if(x.getGroup()!=null)
            _printStream.println(_prefix + "\"group\": \"" + x.getGroup()+"\",");
        //_printStream.println(_prefix + "\"length\": " +x.getLength()+ ",");
        _printStream.println(_prefix + "\"phases\": " +x.getLength()+ ",");
        _printStream.println(_prefix + "\"port_number\": "+ x.countPorts()+",");

        // String jsonOperator = _gson.toJson(x.getOperator(), Operator.class);
        //String jsonOperatorIntParams =_gson.toJson(x.getOperator().getIntParams());
        TreeMap<String, Integer> intParams = x.getOperator().getIntParams();
        Vector<String> keysToPrint = new Vector<>();
        keysToPrint.add("input_dim0");
        keysToPrint.add("input_dim1");
        keysToPrint.add("input_dim2");
        keysToPrint.add("output_dim0");
        keysToPrint.add("output_dim1");
        keysToPrint.add("output_dim2");
        keysToPrint.add("k_h");
        keysToPrint.add("k_w");
        keysToPrint.add("stride");


        //  System.out.println(jsonOperator);
         _printStream.println(_prefix + "\"operator\": {");
         _prefixInc();
        _printStream.println(_prefix + "  \"name\": "+ "\"" + x.getOperator().getName() + "\",");

         for(String keyToPrint: keysToPrint) {
             if(intParams.containsKey(keyToPrint)){
                 _printStream.println(_prefix + "  \"" + keyToPrint + "\": "+ intParams.get(keyToPrint) + ",");
             }
         }

        _printStream.println(_prefix + "  \"time_complexity\": " + x.getOperator().getTimeComplexity() + ",");
        _printStream.println(_prefix + "  \"memory_complexity\": " + x.getOperator().getMemoryComplexity() );

        _prefixDec();
        _printStream.println(_prefix + "},");

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
        _printStream.println(_prefix + "\"src\": " + "\"" + x.getSrc().getNode().getName() + "\"" +",");
        _printStream.println(_prefix + "\"src_rates\": " + x.getSrc().getRates() +",");
        int srcPhases = 0;
        for (IndexPair inRate: x.getSrc().getRates())
            srcPhases+= inRate.getSecond();
        _printStream.println(_prefix + "\"src_phases\": " + srcPhases +",");
        _printStream.println(_prefix + "\"dst\": " + "\"" + x.getDst().getNode().getName()  + "\"" + ",");
        _printStream.println(_prefix + "\"dst_rates\": " + x.getDst().getRates()  +",");
        int dstPhases = 0;
        for (IndexPair outRate: x.getDst().getRates())
            dstPhases+= outRate.getSecond();
        Integer minBufSize = getMinBufSize(x);
        _printStream.println(_prefix + "\"min_buf_size\": " + minBufSize  +",");
        _printStream.println(_prefix + "\"buf_size\": " + (minBufSize)  +",");

        _printStream.println(_prefix + "\"dst_phases\": " + dstPhases);
        if(x.getDst().isOverlapHandler()){
            _printStream.println(",");
            Integer initTokes = _getOverlapInitTokens(x.getDst().getRates());
            _printStream.println(_prefix + "\"initial_tokens\": " + initTokes);
        }

         /**Close edge description */
         prefixDec();
        _printStream.println(_prefix + "}");

    }

    /** get minimum buffer size*/
    private Integer getMinBufSize(CSDFEdge edge) {
        CSDFPort src = edge.getSrc();
        CSDFPort dst = edge.getDst();
        Integer minBufSize = 0;
        for (IndexPair rate : src.getRates()) {
            if (rate.getFirst() > minBufSize) {
                minBufSize = rate.getFirst();
            }
        }

        for (IndexPair rate : dst.getRates()) {
            if (rate.getFirst() > minBufSize) {
                minBufSize = rate.getFirst();
            }
        }

        return minBufSize;
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
        //_printStream.println("");
        _printStream.println(_prefix + "{");
        _prefixInc();
      //  _printStream.println(_prefix + "\"id\": " + x.getId()+",");
        _printStream.println(_prefix + "\"name\": \"" + x.getName()+"\",");

        Vector<IndexPair> rateDesc = x.getRates();
        int sumPhases = 0;
        for(IndexPair ip: rateDesc)
            sumPhases +=ip.getSecond();

        //JsonArray rate = CSDFSupportResolver.serializeIndexPairsAsArray(x.getRates());
        int commaBorder = x.getRates().size()-1;
        StringBuilder json_rate = new StringBuilder("[");
        for(IndexPair rate: x.getRates()){
            json_rate.append("[" + rate.getFirst() +", "+ rate.getSecond() + "]");
            if(commaBorder>0) {
                json_rate.append(", ");
                commaBorder--;
            }
        }
        json_rate.append("]");


        _printStream.println(_prefix + "\"rate\": " + json_rate +",");

        if(x.isOverlapHandler())
            _printStream.println(_prefix + "\"self_loop\": 1, ");


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
}
