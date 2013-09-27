
package espam.visitor.dot.cdpn;

import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDChannel;

import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.LinearizationType;

import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
//// CDPN Dotty Visitor

/**
 *  This class is a class for a visitor that is used to generate
 *  ".dot" output in order to visualize a CDPN using the DOTTY tool.
 *
 * @author  Hristo Nikolov
 * @version  $Id: CDPNDotVisitor.java,v 1.2 2012/01/16 16:36:53 nikolov Exp $
 */

public class CDPNDotVisitor extends CDPNVisitor {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     *  Constructor for the CDPNDotVisitor object
     *
     * @param  printStream Description of the Parameter
     */
    public CDPNDotVisitor(PrintStream printStream) {
        _printStream = printStream;
    }
    
    /**
     *  Print a .dot file in the correct format for DOTTY.
     *
     * @param  x The platform that needs to be rendered.
     */
    public void visitComponent(CDProcessNetwork x) {
        
        _prefixInc();
        _printStream.println( "digraph " + x.getName() + " {" );
        _printStream.println("");
        _printStream.println( _prefix + "label = \"" + x.getName() + "\";" );
        _printStream.println( _prefix + "fontname=Helvetica;" );
        _printStream.println( _prefix + "fontcolor=black;" );
        _printStream.println( _prefix + "ratio = auto;" );
        _printStream.println( _prefix + "rankdir = TB;" );
        _printStream.println( _prefix + "nodesep = 0.2;" );
        _printStream.println( _prefix + "center = true;" );
        _printStream.println( _prefix + "node [fontsize=12, height=0.05, width=0.05, style=filled, shape=ellipse, color=orange]" );
        _printStream.println( _prefix + "edge [fontsize=10, decorate=false, width=6, minlen=2]");
        _printStream.println("");
        
        // Visit all CDPN processes
        Iterator i = x.getProcessList().iterator();
        while( i.hasNext() ) {
            CDProcess process = (CDProcess) i.next();
            process.accept(this);
        }
        _printStream.println("");
        
        // Visit all CDPN channels
        i = x.getChannelList().iterator();
        while( i.hasNext() ) {
            CDChannel channel = (CDChannel) i.next();
            channel.accept(this);
        }
        
        _prefixDec();
        _printStream.println("");
        _printStream.println("}");
    }
    
//------------------------------------------------------------------------------------------------
// CDPN Processes
//------------------------------------------------------------------------------------------------
    /**
     *  Print a line for the CDPN processes in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(CDProcess x) {
        
        _printStream.println(
                             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + getADGFunctions(x) + " ];");
    }
    
//------------------------------------------------------------------------------------------------
// CDPN Channels
//------------------------------------------------------------------------------------------------
    /**
     *  Print a line for the CDPN channels in the correct format for DOTTY.
     *
     * @param  x The channel that needs to be rendered.
     */
    public void visitComponent(CDChannel x) {
        
        String color = "dimgray";
        String type = "";
        
        LinearizationType comModel = x.getCommunicationModel(); 
        
        if( comModel == LinearizationType.fifo ) {                       /** In-order without Multiplicity. */
//            color="black";
            type="(fifo)";
        } else if( comModel == LinearizationType.GenericOutOfOrder ) {   /** Out-of-order without Multiplicity. */
            color="firebrick";
            type="(OO)";
        } else if( comModel == LinearizationType.sticky_fifo ) {         /** In-order with Multiplicity (special case defined by Sven Verdoolaege).*/
            color="seagreen";
            type="(sf)";   
        } else if( comModel == LinearizationType.shift_register ) {      /** In-order (special case defined by Sven Verdoolaege).*/
            color="royalblue";
            type="(sr)";
        } else if( comModel == LinearizationType.BroadcastInOrder ) {    /** In-order with Multiplicity (general case defined by Alex Turjan).*/
            color="green";
            type="(IOM)";
        } else if( comModel == LinearizationType.BroadcastOutOfOrder ) { /** Out-of-order with Multiplicity.*/
            color="red";
            type="(OOM)";
        } 
        
        _printStream.println( _prefix + "\"" + x.getFromGate().getProcess().getName() + "\" -> " + "\"" + x.getToGate().getProcess().getName() + 
                             "\" [ label=\"" + x.getName() + "," + getADGEdges(x) + "\\n " + type + ": " + x.getMaxSize() + "\"" + ", color=" + color + " ];");
    }
    
//------------------------------------------------------------------
    
    /**
     *  Get the ADG functions which are executed by a CDPN process
     *
     * @param  x The CDPN process
     */
    public String getADGFunctions( CDProcess x ) {
        
        String strFunc = "";
        
        Iterator i = x.getAdgNodeList().iterator();
        while( i.hasNext() ) {
            ADGNode node = (ADGNode) i.next();
            String funcName = node.getFunction().getName();
            if( funcName != "" ) {
                strFunc += "\\n" + node.getFunction().getName() + "()"; 
            }
        } 
        strFunc += "\"";
        return strFunc;
    }
    
//--------------------------------------------------------------------
    
    /**
     *  Get the ADG edges assigned to a CDPN channel
     *
     * @param  x The CDPN channel
     */    
    public String getADGEdges( CDChannel x ) {
        
        String strEdges = "";
        
        Iterator i = x.getAdgEdgeList().iterator();
        while( i.hasNext() ) {
            ADGEdge edge = (ADGEdge) i.next();
            strEdges += "\\n" + edge.getName() + ", ";
        }
        return strEdges;
    }
    
}
