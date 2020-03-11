package espam.visitor.dot.sdfg;
import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.*;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

public class SDFGDotVisitor extends CSDFGraphVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                   ///
    protected SDFGDotVisitor(){ }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call CSDFGraph json visitor
     * @param sdfg CSDFGraph to be visited
     * @param dir directory for .dot and .png files, corresponding to visited dnn
     */
    public static void callVisitor(CSDFGraph sdfg, String dir){
        try {
            PrintStream printStream = FileWorker.openFile(dir, sdfg.getName(), "dot");
            _sdfgDotVisitor._printStream = printStream;
            _sdfgDotVisitor.visitComponent(sdfg);
            System.out.println("DOT file generated: " + dir + sdfg.getName());
        }
        catch (Exception e){
            System.err.println(sdfg.getName() + " rendering error " + e.getMessage());
        }
     }

      /**
     * Call DNN dot visitor
     * @param printStream dot print stream
     */
    public static void callVisitor(CSDFGraph sdfg, PrintStream printStream){
        try {
            _sdfgDotVisitor._printStream = printStream;
            _sdfgDotVisitor.visitComponent(sdfg);
        }
        catch (Exception e){
            System.err.println("DNN rendering error " + e.getMessage());
        }
     }

    /**
     * SDF Graph
     * Print a line for the SDFG graph in the correct format for DOTTY.
     * @param  x The graph that needs to be rendered.
     */
    public void visitComponent(CSDFGraph x) {
        _prefixInc();

        _printStream.println("digraph " + x.getName() + " {");
        _printStream.println("");
        _printStream.println(_prefix + "ratio = auto;");
        _printStream.println(_prefix + "rankdir = LR;");
        _printStream.println(_prefix + "ranksep = 0.3;");
        _printStream.println(_prefix + "nodesep = 0.2;");
        _printStream.println(_prefix + "center = true;");
        _printStream.println("");
        _printStream.println(_prefix + "node [ fontsize=12, height=0.4, width=0.4, style=filled, color=\"0.650 0.200 1.000\" ]");
        _printStream.println(_prefix + "edge [ fontsize=10, arrowhead=none, style=bold]");
        _printStream.println("");
        Iterator i;

        // Visit all nodes
        CSDFNode node;
        i = x.getNodeList().iterator();
        while (i.hasNext()) {
            node = (CSDFNode) i.next();
            node.accept(this);
        }

        _printStream.println("");

        // Visit all edges
        CSDFEdge edge;
        i = x.getEdgeList().iterator();
        while (i.hasNext()) {
            edge = (CSDFEdge) i.next();
            edge.accept(this);
        }

        _prefixDec();
        _printStream.println("");
        _printStream.println("}");
    }


    /**
     * SDFG Nodes
     * Print a line for the SDFG Nodes in the correct format for DOTTY.
     * @param  x The node that needs to be rendered.
     */
    public void visitComponent(CSDFNode x) {
        String label = x.getFunction();
        if(label==null)
            label=x.getName();

        _printStream.println(_prefix + "\"" + x.getName() + "\" [ label=\"" + label +"\"];");
    }

    /** SDFG Edges
     *  Print a line for the edge in the correct format for DOTTY.
     * @param  x The SDF edge that needs to be rendered.
     */
    public void visitComponent(CSDFEdge x) {

        String color = "dimgray";

        Iterator i;
        NPort port, portNext;
        i = x.getPortList().iterator();

        port = (NPort) i.next();

        if(port instanceof CSDFPort) {
            if(((CSDFPort) port).isOverlapHandler())
                color = "green";
            else
                color = "red";
        }

        while( i.hasNext() ) {
            portNext = (NPort) i.next();
            _printStream.print( _prefix + "\"" + port.getNode().getName() + "\" -> " +
                    "\"" + portNext.getNode().getName()+"\" [" );
            _printStream.println( " label=\"[p: " + x.getSrc().getStrRates()+
                    ",c: " + x.getDst().getStrRates() + "]\"" + ", color=" + color + " ];");
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                  ///

    /**
     *  Increment the indentation.
     */
    protected void _prefixInc() {
        _prefix += _offset;
    }

    /**
     *  Decrement the indentation.
     */
    protected void _prefixDec() {
        if (_prefix.length() >= _offset.length()) {
            _prefix = _prefix.substring(_offset.length());
        }
    }

     ///////////////////////////////////////////////////////////////////
    ////                     private variables                      ///
    private static SDFGDotVisitor _sdfgDotVisitor = new SDFGDotVisitor();
}
