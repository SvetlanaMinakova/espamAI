/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.dot.platform;

import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.FifosController;
import espam.datamodel.platform.controllers.MultiFifoController;
import espam.datamodel.platform.controllers.ReadCrossbarController;
import espam.datamodel.platform.peripherals.ZBTMemoryController;
import espam.datamodel.platform.communication.AXICrossbar;
import espam.datamodel.platform.controllers.CM_CTRL;
import espam.datamodel.platform.controllers.AXI_CM_CTRL;
import espam.datamodel.platform.controllers.AXI2AXI_CTRL;
import espam.datamodel.platform.memories.CM_AXI;
import espam.datamodel.platform.ports.AXIPort;
import espam.datamodel.platform.host_interfaces.ML605;


import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Platform Dotty Visitor

/**
 *  This class is a class for a visitor that is used to generate
 *  ".dot" output in order to visualize a Platform using the DOTTY tool.
 *
 * @author  Hristo Nikolov
 * @version  $Id: PlatformDotVisitor.java,v 1.2 2012/04/02 16:25:40 nikolov Exp $
 */

public class PlatformDotVisitor extends PlatformVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the PlatformDotVisitor object
     *
     * @param  printStream Description of the Parameter
     */
    public PlatformDotVisitor(PrintStream printStream) {
        _printStream = printStream;
    }

    /**
     *  Print a .dot file in the correct format for DOTTY.
     *
     * @param  x The platform that needs to be rendered.
     */
    public void visitComponent(Platform x) {

        _prefixInc();
        _printStream.println( "digraph " + x.getName() + " {" );
        _printStream.println("");
        _printStream.println( _prefix + "ratio = auto;" );
        _printStream.println( _prefix + "rankdir = LR;" );
        _printStream.println( _prefix + "ranksep = 0.3;" );
        _printStream.println( _prefix + "nodesep = 0.2;" );
        _printStream.println( _prefix + "center = true;" );
        _printStream.println("");
        _printStream.println( _prefix + "node [ fontsize=12, height=0.4, width=0.4, style=filled, color=\"0.650 0.200 1.000\" ]" );
        _printStream.println( _prefix + "edge [ fontsize=10, arrowhead=none, style=bold]");
        _printStream.println("");

	Iterator i;

        // Visit all processes
        Resource resource;
        i = x.getResourceList().iterator();
        while( i.hasNext() ) {
            resource = (Resource) i.next();
//              System.out.println( resource );
            resource.accept(this);
        }

        _printStream.println("");

	// Visit all links
        Link channel;
        i = x.getLinkList().iterator();
        while( i.hasNext() ) {
            channel = (Link) i.next();
            channel.accept(this);
        }

        _prefixDec();
        _printStream.println("");
        _printStream.println("}");
    }



    public void visitComponent(Resource x) {
    }

    public void visitComponent(Controller x) {
    }
/*---------------------------------------- Processors --------------------------------------------------------*/
    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(PowerPC x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=tan ];");
// good colors: beige, lightgoldenrod, orange, tan, khaki3, aliceblue, lightskyblue, lightseagreen, mintcream
// burlywood3, lightblue1, linen, papayawhip, azure1,2,3
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(MicroBlaze x) {

	_printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=burlywood3 ];");
    }
/*--------------------------------------- CompaanHWNode -------------------------------------------------------*/
    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(CompaanHWNode x) {

        _printStream.println();
	_printStream.println( _prefix + "subgraph cluster0 {" );
	_prefixInc();
	_printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=lightseagreen ];");
	_printStream.println( _prefix + "label= \"Compaan PN\";" );
	_printStream.println( _prefix + "color=blue;");
	_prefixDec();
	_printStream.println( _prefix + "}" );
	_printStream.println();
    }
/*---------------------------------------- Memories ----------------------------------------------------------*/
    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(MultiFifo x) {

    	if( x.getFifoList().size() > 0 ) {
	    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          " \\n" + x.getFifoList().size() + " FIFOs\", shape=box, width=0.7, color=beige ];");
        } else {
	    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
	                         "\", shape=box, width=0.7, color=beige ];");
        }
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(CM_AXI x) {

    	if( x.getFifoList().size() > 0 ) {
	    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          " \\n" + x.getFifoList().size() + " FIFOs\", shape=box, width=0.7, color=beige ];");
        } else {
	    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
	                         "\", shape=box, width=0.7, color=beige ];");
        }
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(Fifo x) {

        Port port1 = (Port)(x.getPortList().get(0));
        Port port2 = (Port)(x.getPortList().get(1));

	Link link1 = port1.getLink();
        Port port11 = (Port)(link1.getPortList().get(0));
        Port port12 = (Port)(link1.getPortList().get(1));

	Link link2 = port2.getLink();
        Port port21 = (Port)(link2.getPortList().get(0));
        Port port22 = (Port)(link2.getPortList().get(1));

	//if( port11.getResource() instanceof CompaanHWNode && port21.getResource() instanceof CompaanHWNode ) {
	if( port11 instanceof CompaanOutPort || port12 instanceof CompaanOutPort ||
	    port21 instanceof CompaanOutPort || port22 instanceof CompaanOutPort ) {

		_printStream.println();
		_printStream.println( _prefix + "subgraph cluster0 {" );
		_prefixInc();

		if( x.getSize() > 0 ) {
		    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
				         " \\n" + x.getSize() + " Bytes\", shape=box, color=beige ];");
		} else {
		    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", shape=box, color=beige ];");
		}
		_prefixDec();
		_printStream.println( _prefix + "}" );
		_printStream.println();
	} else {
		if( x.getSize() > 0 ) {
		    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
		                          " \\n" + x.getSize() + " Bytes\", shape=box, color=beige ];");
		} else {
		    _printStream.println( _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", shape=box, color=beige ];");
		}
	}
     }

     /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(BRAM x) {

	if( x.getSize() > 0 ) {
  	    _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          " \\n" + x.getSize() + " Bytes\", shape=box, regular=true, color=khaki3 ]; }");
	} else {
  	    _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          "\", shape=box, regular=true, color=black ]; }");
	}
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(ZBT x) {

	if( x.getSize() > 0 ) {
	    _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          " \\n" + x.getSize() + " Bytes\", shape=box, regular=true, color=aliceblue ]; }");
	} else {
	    _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          "\", shape=box, regular=true, color=black ]; }");
	}
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(Memory x) {

        if( x instanceof CM_AXI ) { // BUT WHY?
            visitComponent( (CM_AXI) x );
        } else {

	    if( x.getSize() > 0 ) {
	       _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          " \\n" + x.getSize() + " Bytes\", shape=box, regular=true, color=white ]; }");
	    } else {
	       _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
	                          "\", shape=box, regular=true, color=black ]; }");
	   }
        }
    }

/*--------------------------------------- Controllers -------------------------------------------------------*/
    /**
     *  Print a line for a memory controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(MemoryController x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a ReadCrossbar controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(ReadCrossbarController x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a ReadCrossbar controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(AXI_CM_CTRL x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a ReadCrossbar controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(AXI2AXI_CTRL x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a multi-fifo controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(MultiFifoController x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a multi-fifo controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(CM_CTRL x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");

    }

    /**
     *  Print a line for a fifos controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(FifosController x) {

        _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() +
	                                        "\\n #writePorts = " + x.getNumberFifoWritePorts() +
	                                        "\\n #readPorts  = " + x.getNumberFifoReadPorts() +
						"\", color=papayawhip ];");

    }


/*--------------------------------------- Communication -------------------------------------------------------*/
    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(Crossbar x) {

	_printStream.println(
             //_prefix + "{ rank=sink; \"" + x.getName() + "\" [ label=\"" + x.getName() + "\", regular=true, color=lightgoldenrod ]; }");
            _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", regular=true, color=lightgoldenrod ];");
    }

    /**
     *  Print a line for the process in the correct format for DOTTY.
     *
     * @param  x The process that needs to be rendered.
     */
    public void visitComponent(AXICrossbar x) {

	_printStream.println(
            _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", regular=true, color=lightgoldenrod ];");
    }
        
/*--------------------------------------- Peripherals -------------------------------------------------------*/
    /**
     *  Print a line for a ZBT memory controller in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(ZBTMemoryController x) {

       if( x.getSize() > 0 ) {
    	  _printStream.println( _prefix + "{ rank=source; \"" + x.getName() + "\" [ label=\"" + x.getName() +
    	                          " \\n" + x.getSize() + " Bytes\", shape=box, regular=true, color=white ]; }");
       } else {
          _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", color=papayawhip ];");
       }
    }
/*---------------------------------------- Host Interface --------------------------------------------------*/
    /**
     *  Print a line for a ML605 host interface in the correct format for DOTTY.
     *
     * @param  x The controller that needs to be rendered.
     */
    public void visitComponent(ML605 x) {

       _printStream.println(
             _prefix + "\"" + x.getName() + "\" [ label=\"" + x.getName() + "\", shape=box, regular=true, color=linen ];");
    }

/*---------------------------------------- Link --------------------------------------------------------------*/
    /**
     *  Print a line for the channel in the correct format for DOTTY.
     *
     * @param  x The channel that needs to be rendered.
     */
    public void visitComponent(Link x) {

 	String color = "dimgray";

        Iterator i;
        Port port, portNext;
        i = x.getPortList().iterator();

	port = (Port) i.next();

	if( port instanceof LMBPort  || port instanceof PLBPort || port instanceof AXIPort )
	{
	    color="firebrick";
	}
	if( port instanceof AXIPort )
	{
	    color="lightseagreen";
	}
	if( port instanceof FifoWritePort || port instanceof CompaanOutPort )
	{
	    color="yellowgreen";
	}
	if( port instanceof FifoReadPort || port instanceof CompaanInPort ) 
	{
	    color="khaki3";
	}

	while( i.hasNext() ) {

	    portNext = (Port) i.next();
	    _printStream.print( _prefix + "\"" + port.getResource().getName() + "\" -> " + "\"" + portNext.getResource().getName() + "\" [" );
            //_printStream.print( " sametail=\"" + x.getName() + "\"," );
	    _printStream.println( " label=\"" + x.getName() + "\"" + ", color=" + color + " ];");
        }
    }
}
