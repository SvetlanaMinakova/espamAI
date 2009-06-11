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

package espam.visitor.xps.cdpn;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGEdge;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDGate;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.platform.Resource;
import espam.datamodel.platform.processors.Processor;

import espam.datamodel.parsetree.ParserNode;

import espam.main.UserInterface;

import espam.visitor.CDPNVisitor;

import espam.datamodel.LinearizationType;

//////////////////////////////////////////////////////////////////////////
//// XpsDynamicXilkernelProcessVisitor

/**
 *  This class ...
 *
 * @author  Wei Zhong, Hristo Nikolov,Todor Stefanov, Joris Huizer
 * @version  $Id: XpsDynamicXilkernelProcessVisitor.java,v 1.1 2009/06/11 13:18:14 stefanov Exp $
 */

public class XpsDynamicXilkernelProcessVisitor extends CDPNVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the XpsProcessVisitor object
     */
    public XpsDynamicXilkernelProcessVisitor( Mapping mapping, PrintStream printStream, PrintStream printStreamFunc, Map relation ) {
	_mapping = mapping;
	_printStream = printStream;
	_printStreamFunc = printStreamFunc;
	_relation2 = relation;
	_ui = UserInterface.getInstance();
    }

    /**
     *  Create a Xps PN process with static scheduling for this process.
     *
     * @param  x process to generate code for
     */
    public void visitComponent( CDProcess x ) {
        _writeIncludes( x );
        
	int pos = 0;
	Iterator i = x.getSchedule().iterator();
	while ( i.hasNext() ) {
		String threadName = "thread" + (pos + 1);
		_printStream.println(_prefix + "void *" + threadName + "(void *arg) {");
		_prefixInc();
	
		_writeFunctionArguments(x, pos++);	
		_writeThread( x, (ParserNode)i.next() );
	        _printStream.println("");

		_prefixDec();
		_printStream.println(_prefix + "} // " + threadName);

		_prefixDec();
		_printStream.println("");
	}
	
	_writeThreadMain( x );
	_writeMain();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * @param  x Description of the Parameter
     */
    private void _writeFunctionArguments( CDProcess x, int index ) {
	String funcName = "";
        String csl = "";
	String t = "";

	_prefixInc();
        // declare the input arguments of the function
        _printStream.println(_prefix + "// Input Arguments ");

	ADGNode node = (ADGNode) x.getAdgNodeList().get(index);
	ADGFunction function = (ADGFunction) node.getFunction();

	Iterator j1 = function.getInArgumentList().iterator();
	while( j1.hasNext() ) {
		ADGVariable arg = (ADGVariable) j1.next();
		String funcArgument = arg.getName() + node.getName();
		String dataType = arg.getDataType();
	
		t = "char";
		if (dataType != null) {
			if (!dataType.equals("")) {
				t = dataType;
			}
		}
	
		// Find the gate corresponding to this funcArgumnet
		Iterator g = x.getGateList().iterator();
		while ( g.hasNext() ) {
			CDGate  gate = (CDGate) g.next();
	
			Iterator p = gate.getAdgPortList().iterator();
			while( p.hasNext() ) {
				ADGPort port = (ADGPort) p.next();
		
				Iterator bvi = port.getBindVariables().iterator();
				while ( bvi.hasNext() ) {
					ADGVariable bv = (ADGVariable) bvi.next();
					String tmp = bv.getName() + port.getNode().getName();
					if( funcArgument.equals( tmp ) ) {
						t = "t" + gate.getChannel().getName();
					}
				}	
			}
		}
	
		_printStream.println(_prefix + t + " " + funcArgument + ";");
	}

        _printStream.println("");

        // declare the output arguments of the function
        _printStream.println(_prefix + "// Output Arguments ");

	ADGFunction function1 = (ADGFunction) node.getFunction();

	Iterator j2 = function1.getOutArgumentList().iterator();
	while( j2.hasNext() ) {
		ADGVariable arg = (ADGVariable) j2.next();
		String funcArgument = arg.getName() + node.getName();
		String dataType = arg.getDataType();
	
		t = "char";
		if (dataType != null) {
			if (!dataType.equals("")) {
				t = dataType;
			}
		}
	
		// Find the gate corresponding to this funcArgumnet
		Iterator g = x.getGateList().iterator();
		while ( g.hasNext() ) {
			CDGate  gate = (CDGate) g.next();
	
			Iterator p = gate.getAdgPortList().iterator();
			while( p.hasNext() ) {		
				ADGPort port = (ADGPort) p.next();
				String tmp = ((ADGVariable) port.getBindVariables().get(0)).getName() + port.getNode().getName();
				if( funcArgument.equals( tmp ) ) {
					t = "t" + gate.getChannel().getName();
				}
			}
		}
	
		_printStream.println(_prefix + t +" " + funcArgument + ";");
	}

    	_prefixDec();

        //write func wrapper in aux file

	csl = "";
	funcName = function1.getName();

	if(!_relation2.containsKey(funcName) ) {
		_printStreamFunc.println("inline");
		csl += "void  _" + funcName + "( ";
	
		j2 = function1.getInArgumentList().iterator();
		while( j2.hasNext() ) {
			ADGVariable arg = (ADGVariable) j2.next();
			String funcArgument = arg.getName() + node.getName();
			String dataType = arg.getDataType();
	
			t = "char";
			if (dataType != null) {
				if (!dataType.equals("")) {
					t = dataType;
				}
			}
	
			Iterator g = x.getGateList().iterator();
			while ( g.hasNext() ) {
				CDGate  gate = (CDGate) g.next();
		
				Iterator p = gate.getAdgPortList().iterator();
				while( p.hasNext() ) {
					ADGPort port = (ADGPort) p.next();
		
					Iterator bvi = port.getBindVariables().iterator();
					while ( bvi.hasNext() ) {
						ADGVariable bv = (ADGVariable) bvi.next();
						String tmp = bv.getName() + port.getNode().getName();
						if( funcArgument.equals( tmp ) ) {
							t = "t" + gate.getChannel().getName();
						}
					}	
				}
			}
	
			csl += t + " " + arg.getName() + ", ";
		}
	
		j2 = function1.getOutArgumentList().iterator();
		while( j2.hasNext() ) {
			ADGVariable arg = (ADGVariable) j2.next();
			String funcArgument = arg.getName() + node.getName();
			String dataType = arg.getDataType();
	
			t = "char";
			if (dataType != null) {
				if (!dataType.equals("")) {
					t = dataType;
				}
			}
	
			Iterator g = x.getGateList().iterator();
			while ( g.hasNext() ) {
				CDGate  gate = (CDGate) g.next();
		
				Iterator p = gate.getAdgPortList().iterator();
				while( p.hasNext() ) {
					ADGPort port = (ADGPort) p.next();
					String tmp = ((ADGVariable) port.getBindVariables().get(0)).getName() + port.getNode().getName();
					if( funcArgument.equals( tmp ) ) {
					t = "t" + gate.getChannel().getName();
					}
				}
			}
	
			csl += t + " *" + arg.getName() + ", ";
		}
		_printStreamFunc.println(csl.substring(0, (csl.length() - 2)) + " ) {");
	
		//-------- print the initial function call in the wrapper ------------------------
		csl = funcName + "( ";
	
		j2 = function1.getInArgumentList().iterator();
		while( j2.hasNext() ) {
			ADGVariable arg = (ADGVariable) j2.next();
			csl += "&" + arg.getName() + ", ";
			//csl += arg.getName() + ", ";
		}
	
		j2 = function1.getOutArgumentList().iterator();
		while( j2.hasNext() ) {
			ADGVariable arg = (ADGVariable) j2.next();
			//csl += "&" + arg.getName() + ", ";
			csl += arg.getName() + ", ";
		}
	
		_printStreamFunc.println("    " + csl.substring(0, (csl.length() - 2)) + " );");
		//-------- END print of the initial function call in the wrapper ------------------------
	
		_printStreamFunc.println("}");
		_printStreamFunc.println("");
	}
	_relation2.put(funcName, "");

        _printStream.println("");
    }

    /**
     * @param  x Description of the Parameter
     */
    private void _writeIncludes( CDProcess x ) {
	_printStream.println("#include \"xmk.h\"");
	_printStream.println("#include <os_config.h>");
	_printStream.println("#include <sys/process.h>");
	_printStream.println("#include <pthread.h>");
	_printStream.println("#include \"MemoryMap.h\"");
	_printStream.println("#include \"aux_func.h\"");
	_printStream.println("");
	
	Iterator n = x.getGateList().iterator();
	while( n.hasNext() ) {
		CDGate gate = (CDGate) n.next();
		LinearizationType comModel = 
			((CDChannel)gate.getChannel()).getCommunicationModel();
		if (comModel != LinearizationType.fifo &&
		comModel != LinearizationType.BroadcastInOrder &&
		comModel != LinearizationType.sticky_fifo &&
		comModel != LinearizationType.shift_register) {
			System.out.println("ERROR: Out of order channels are not supported yet!");
		System.exit(0);
		}
	}
	_printStream.println("");
    }

    /**
     *  write the body of the thread
     *
     * @param  x Process under which the thread runs
     * @param  thread node for which to generate the thread code
     */
    private void _writeThread( CDProcess x, ParserNode threadNode ) {
	_prefixInc();
	// Print the Parse tree
	XpsStatementVisitor xpsvisitor = new XpsStatementVisitor(_printStream, x, _mapping);
	xpsvisitor.setPrefix( _prefix );
	xpsvisitor.setOffset( _offset );
	
	threadNode.accept(xpsvisitor);
    }


    /**
     *  Write the thread_main function
     *
     * @param  x Process under which thread_main is generated
     */
    private void _writeThreadMain( CDProcess x ) {
	_printStream.println("extern \"C\"");
	_printStream.println("void *thread_main(void *arg) {");
	_prefixInc();
	_printStream.println(_prefix + "pthread_t threadID[" + x.getSchedule().size() + "];");
	_printStream.println(_prefix + "int ret;");
		
	if( _ui.getDebuggerFlag() ) {
		_printStream.println(_prefix + "int clk_num;");
		_printStream.println(_prefix + "*clk_cntr = 0;");

	}	

	_printStream.println("");
	for (int pos = 0; pos < x.getSchedule().size(); pos++) {
		_printStream.println(_prefix + "ret = pthread_create(&threadID[" + pos + "], NULL, thread" + (pos + 1) + ", NULL);");
	}
	
	_printStream.println("");
	for (int pos = 0; pos < x.getSchedule().size(); pos++) {
		_printStream.println(_prefix + "ret = pthread_join(threadID[" + pos + "], NULL);");
	}
	
	if( _ui.getDebuggerFlag() ) {
		_printStream.println(_prefix + "clk_num = *clk_cntr;");
	}

	_printStream.println(_prefix + "*FIN_SIGNAL = (volatile long)0x00000001;");
	
	_prefixDec();
	_printStream.println(_prefix + "} // thread_main");
	
	_printStream.println("");	
    }

    /**
     *  Write the main function
     *
     */
    private void _writeMain() {
	_printStream.println("int main() {");
	_prefixInc();
	_printStream.println(_prefix + "xilkernel_main();");
	_prefixDec();
	_printStream.println(_prefix + "} // main");
	_printStream.println("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    /**
     * Repository directory for the source codes
     */
    private static String _codeDir = "";

    /**
     *  The UserInterface
     */
    private UserInterface _ui = null;

    private Mapping _mapping = null;

    private PrintStream _printStream = null;

    private PrintStream _printStreamFunc = null;

    private Map _relation2 = null;

}
