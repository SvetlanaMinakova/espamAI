package espam.visitor.xml.csdf;

import espam.datamodel.graph.csdf.*;
import espam.parser.json.csdf.CSDFSupportResolver;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class building the XML scheme in SDF3 format for an SDF/CSDF graph
 */
public class CSDFGraphXMLVisitor extends CSDFGraphVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Constructor for the CSDFGraphXMLVisitor object
     * @param printStream the output Xml print stream
     */
    public CSDFGraphXMLVisitor(PrintStream printStream) {
        _printStream = printStream;
        _propertiesVisitor = new CSDFGraphPropertiesXMLVisitor(printStream);
    }

     /**
     * Constructor for the CSDFGraphXMLVisitor object with dummy mapping specified
     * @param printStream the output Xml print stream
     */
    public CSDFGraphXMLVisitor(PrintStream printStream, boolean dummyMapping) {
        _printStream = printStream;
        _propertiesVisitor = new CSDFGraphPropertiesXMLVisitor(printStream);
        _dummyMapping = dummyMapping;
        _printProperties = dummyMapping;
    }

    /**
     * Constructor for the CSDFGraphXMLVisitor object with dummy
     * node-to-processor mapping flag
     * @param printStream the output Xml print stream
     */
    public CSDFGraphXMLVisitor(PrintStream printStream, HashMap<String,String> nodesToProcessors) {
        _printStream = printStream;
        _propertiesVisitor = new CSDFGraphPropertiesXMLVisitor(printStream);
        _propertiesVisitor.setNodeProcessorsMapping(nodesToProcessors);
        _printProperties = true;
    }

    /**
     * Call graph visitor with node-to-processor mapping specified
     * @param graph SDF Graph to be visited
     * @param dir .xml file output directory
     */
    public static void callVisitor(CSDFGraph graph, String dir){
        try {
            PrintStream printStream = FileWorker.openFile(dir, graph.getName(), "xml");
            CSDFGraphVisitor xmlVisitor = new CSDFGraphXMLVisitor(printStream);
            graph.accept(xmlVisitor);
            System.out.println("XML File generated: " + dir + graph.getName()+".xml ");
        }
        catch (Exception e){
            System.err.println("XML CSDFGraph visitor call error."+ e.getMessage());
        }
    }

    /**
     * Call graph visitor with graph properties printout and
     * dummy mapping of sdf graph nodes on an abstract processor
     * (required for analysis by SDF3)
     * @param graph SDF Graph to be visited
     * @param dir .xml file output directory
     */
    public static void callVisitor(CSDFGraph graph, String dir, boolean dummyMapping){
        try {
            PrintStream printStream = FileWorker.openFile(dir, graph.getName(), "xml");
            CSDFGraphVisitor xmlVisitor = new CSDFGraphXMLVisitor(printStream, dummyMapping);
            graph.accept(xmlVisitor);
            System.out.println("XML File generated: " + dir + graph.getName()+".xml ");
        }
        catch (Exception e){
            System.err.println("XML CSDFGraph visitor call error."+ e.getMessage());
        }
    }

     /**
     * Call graph visitor with graph properties printout and
     * mapping of sdf graph nodes on specified processor
     * (required for analysis by SDF3)
     * @param graph SDF Graph to be visited
     * @param dir .xml file output directory
     * @param nodesToProcessors mapping of sdf graph nodes on specified processor
     */
    public static void callVisitor(CSDFGraph graph, String dir, HashMap<String,String> nodesToProcessors){
        try {
            PrintStream printStream = FileWorker.openFile(dir, graph.getName(), "xml");
            CSDFGraphVisitor xmlVisitor = new CSDFGraphXMLVisitor(printStream, nodesToProcessors);
            graph.accept(xmlVisitor);
            System.out.println("XML File generated: " + dir + graph.getName()+".xml ");
        }
        catch (Exception e){
            System.err.println("XML CSDFGraph visitor call error."+ e.getMessage());
        }
    }

    /**
     *  Visit a CSDFGraph component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFGraph x) {
        SDFGraphType graphType = x.getType();
        /** print header of .xml sdf3 document*/
        printSDF3Header(graphType);
        prefixInc();
        /** print graph*/
        _printStream.println(_prefix + "<applicationGraph name=\"" + x.getName() + "\" >");
        _prefixInc();
        _printStream.println(_prefix + "<" + graphType + " name=\"" + x.getName() + "\" "
                + "type=\"" + x.getName().toUpperCase() + "\" >");
        prefixInc();
        /** Visit the list of nodes of this CSDFGraph*/

        Vector nodeList = x.getNodeList();
        if( nodeList != null ) {
            Iterator i = nodeList.iterator();
            while( i.hasNext() ) {
                CSDFNode node = (CSDFNode) i.next();
                node.accept(this);
            }
        }
        prefixDec();

        /** Visit the list of edges of this CSDFGraph*/
        prefixInc();
        Vector edgeList = x.getEdgeList();
        if( edgeList != null ) {
            Iterator i = edgeList.iterator();
            while( i.hasNext() ) {
                CSDFEdge edge = (CSDFEdge) i.next();
                edge.accept(this);
            }
        }


        /** print CSDF model graph end*/
        prefixDec();
        _printStream.println("");
        _printStream.println(_prefix + "</" + graphType + ">");

        /**
         * Print properties pf the graph, if needed
         */
        if(_printProperties){
            if(_dummyMapping)
                _propertiesVisitor.createOneToOneDummyMapping(x);
            _propertiesVisitor.setPrefix(_prefix);
            _propertiesVisitor.visitComponent(x);
        }

        /** print end of .xml sdf3 document*/
        printSDF3End(graphType);
    }

    /**
     *  Visit a CSDFEdge component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFEdge x) {
      _printStream.println("");
      _printStream.print(_prefix + "<channel name=\"" + x.getName() + "\" ");
      /**
        * Add src port information
        */
      CSDFPort srcPort = x.getSrc();
      if(srcPort!=null) {
        CSDFNode srcActor = (CSDFNode)srcPort.getNode();
            if(srcActor!=null)
                _printStream.print(" srcActor=\"" + srcActor.getUniqueName() + "\" ");
          _printStream.print(" srcPort=\"" + srcPort.getName() + "\" ");
      }

        /**
         * Add dst port information
         */
         CSDFPort dstPort = x.getDst();
        if(dstPort!=null){
            CSDFNode dstActor = (CSDFNode)dstPort.getNode();
            if(dstActor!=null)
                _printStream.print ("dstActor=\"" + dstActor.getUniqueName() + "\" ");
            _printStream.print("dstPort=\"" + dstPort.getName() + "\" ");
        }

       _printStream.print(">");
       _printStream.print("</channel>");
    }

     /**
     *  Visit a CSDFNode component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFNode x) {
        _printStream.println("");
        _printStream.print(_prefix + "<actor name=\"" + x.getUniqueName() + "\" " +
                             "type=\"Node\""  + ">");

        /**
         * Visit all ports of CSDFNode
         */
        prefixInc();
        Vector portList = x.getPortList();
        if( portList != null ) {
            Iterator i = portList.iterator();
            while( i.hasNext() ) {
                CSDFPort port = (CSDFPort) i.next();
                port.accept(this);
            }
        }
        prefixDec();
        _printStream.println("");
        _printStream.print(_prefix + "</actor>");
    }


    /**
     * Assign processor to a node
     * @param x
     */
    public void assignProcessor(CSDFNode x, int procId){
        prefixInc();
        _printStream.println(_prefix + "<processor type = proc_" + procId + ">");

        _printStream.println(_prefix + "</processor>");
        prefixDec();
    }

    /**
     *  Visit a CSDFPort component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFPort x) {
        _printStream.println("");
        String strRates = CSDFSupportResolver.serializeIndexPairsAsLongString(x.getRates());
        _printStream.print(_prefix + "<port name=\"" + x.getName() + "\" " +
                "type=\"" + x.getType() + "\" rate=\"" + strRates + "\" " + "/>");
    }


    /** print header of .xml sdf3 document*/
    public void printSDF3Header(SDFGraphType graphType) {
        _printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _printStream.print("<sdf3 type=\"" + graphType + "\" version=\"1.0\" ");
        _printStream.print(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        _printStream.print(" xsi:noNamespaceSchemaLocation=\"http://www.es.ele.tue.nl/sdf3/xsd/sdf3-sdf.xsd\">");
        _printStream.println("");
    }

    /** print end of .xml sdf3 document*/
    private void printSDF3End(SDFGraphType graphType) {

        _prefixDec();
        _printStream.println(_prefix + "</applicationGraph>");
        _prefixDec();
        _printStream.println(_prefix + "</sdf3>");
    }


    ///////////////////////////////////////////////////////////////////
    ////                   protected variables                     ///
    /**
     * if a graph should be provided by a dummy one-to-one mapping
     * on abstract 'proc_0' processors
     */
    protected boolean _dummyMapping = false;

    /**Flag, signals if the properties should be printed*/
    protected boolean _printProperties = false;

    /** graph properties visitor*/
    protected  CSDFGraphPropertiesXMLVisitor _propertiesVisitor;
}
