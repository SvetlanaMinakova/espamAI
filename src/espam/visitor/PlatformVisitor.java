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

package espam.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.Link;
import espam.datamodel.platform.processors.Processor;
import espam.datamodel.platform.processors.PowerPC;
import espam.datamodel.platform.processors.MicroBlaze;
import espam.datamodel.platform.processors.MemoryMap;
import espam.datamodel.platform.processors.Page;
import espam.datamodel.platform.communication.Crossbar;
import espam.datamodel.platform.communication.PLBBus;
import espam.datamodel.platform.communication.LMBBus;
import espam.datamodel.platform.communication.TransparentBus;
import espam.datamodel.platform.communication.ReadFifoBus;
import espam.datamodel.platform.communication.WriteFifoBus;
import espam.datamodel.platform.memories.Memory;
import espam.datamodel.platform.memories.Fifo;
import espam.datamodel.platform.memories.MultiFifo;
import espam.datamodel.platform.memories.BRAM;
import espam.datamodel.platform.memories.ZBT;
import espam.datamodel.platform.ports.PLBPort;
import espam.datamodel.platform.ports.LMBPort;
import espam.datamodel.platform.ports.FifoReadPort;
import espam.datamodel.platform.ports.FifoWritePort;
import espam.datamodel.platform.ports.CompaanInPort;
import espam.datamodel.platform.ports.CompaanOutPort;
import espam.datamodel.platform.hwnodecompaan.CompaanHWNode;
import espam.datamodel.platform.hwnodecompaan.ReadUnit;
import espam.datamodel.platform.hwnodecompaan.WriteUnit;
import espam.datamodel.platform.hwnodecompaan.ExecuteUnit;
import espam.datamodel.platform.controllers.Controller;
import espam.datamodel.platform.controllers.MemoryController;
import espam.datamodel.platform.controllers.FifosController;
import espam.datamodel.platform.controllers.MultiFifoController;
import espam.datamodel.platform.controllers.ReadCrossbarController;
import espam.datamodel.platform.peripherals.Peripheral;
import espam.datamodel.platform.peripherals.ZBTMemoryController;
import espam.datamodel.platform.peripherals.Uart;
import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Platform Visitor

/**
 *  This class is an abstract class for a visitor that is used to generate a
 *  Platform description.
 *
 * @author  Hristo Nikolov, Todor Stefanov
 * @version  $Id: PlatformVisitor.java,v 1.2 2010/04/02 12:21:25 nikolov Exp $
 */

public class PlatformVisitor implements Visitor {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Visit a Platform component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Platform x) {
	}

	/**
	 *  Visit a Resource component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Resource x) {
	}

	/**
	 *  Visit a Port component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Port x) {
	}

	/**
	 *  Visit a Link component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Link x) {
	}

	/**
	 *  Visit a Processor component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Processor x) {
	}

	/**
	 *  Visit a PowerPC component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(PowerPC x) {
	}

	/**
	 *  Visit a MicroBlaze component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MicroBlaze x) {
	}

	/**
	 *  Visit a MemoryMap component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MemoryMap x) {
	}

	/**
	 *  Visit a Page component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Page x) {
	}

	/**
	 *  Visit a Crossbar component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Crossbar x) {
	}

	/**
	 *  Visit a PLBBus component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(PLBBus x) {
	}

	/**
	 *  Visit a LMBBus component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(LMBBus x) {
	}

	/**
	 *  Visit a TransparentBus component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(TransparentBus x) {
	}

	/**
	 *  Visit a ReadFifoBus component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ReadFifoBus x) {
	}

	/**
	 *  Visit a WriteFifoBus component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(WriteFifoBus x) {
	}

	/**
	 *  Visit a Memory component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Memory x) {
	}

	/**
	 *  Visit a ResouFifo component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Fifo x) {
	}

	/**
	 *  Visit a MultiFifo component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MultiFifo x) {
	}

	/**
	 *  Visit a BRAM component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(BRAM x) {
	}

	/**
	 *  Visit a ZBT component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ZBT x) {
	}

	/**
	 *  Visit a PLBPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(PLBPort x) {
	}

	/**
	 *  Visit a LMBPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(LMBPort x) {
	}

	/**
	 *  Visit a FifoReadPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(FifoReadPort x) {
	}

	/**
	 *  Visit a FifoWritePort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(FifoWritePort x) {
	}

	/**
	 *  Visit a CompaanInPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CompaanInPort x) {
	}

	/**
	 *  Visit a CompaanOutPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CompaanOutPort x) {
	}

	/**
	 *  Visit a CompaanHWNode component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CompaanHWNode x) {
	}

	/**
	 *  Visit a ReadUnit component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ReadUnit x) {
	}

	/**
	 *  Visit a WriteUnit component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(WriteUnit x) {
	}

	/**
	 *  Visit a ExecuteUnit component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ExecuteUnit x) {
	}

	/**
	 *  Visit a Controller component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Controller x) {
	}

	/**
	 *  Visit a MemoryController component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MemoryController x) {
	}

	/**
	 *  Visit a FifosController component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(FifosController x) {
	}
	
	/**
	 *  Visit a MultiFifoController component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MultiFifoController x) {
	}

	/**
	 *  Visit a ReadCrossbarController component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ReadCrossbarController x) {
	}
	
	/**
	 *  Visit a Peripheral component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Peripheral x) {
	}
	
	/**
	 *  Visit a ZBTMemoryController component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ZBTMemoryController x) {
	}
	
	/**
	 *  Visit a Uart component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Uart x) {
	}

// Visit the host interface component (board-specific)
	/**
	 *  Visit an ADM-XRC-II interface component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADMXRCII x) {
	}

	/**
	 *  Visit an ADM-XPL interface component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADMXPL x) {
	}

	/**
	 *  Visit a XUPV5LX110T interface component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(XUPV5LX110T x) {
	}

	/**
	 *  Visit a ML505 interface component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ML505 x) {
	}



/******************************************************************************/
	/**
	 *  Decrement the indentation.
	 */
	protected void _prefixDec() {
		if (_prefix.length() >= _offset.length()) {
			_prefix = _prefix.substring(_offset.length());
		}
	}

	/**
	 *  Decrement the indentation with non standard offset.
	 *
	 * @param  o Description of the Parameter
	 */
	protected void _prefixDec(int o) {
		_prefix = _prefix.substring(o);
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
	 *  Increment the indentation with non standard offset.
	 *
	 * @param  o Description of the Parameter
	 */
	protected void _prefixInc(int o) {
		for (int i = 0; i < o; i++) {
			_prefix += " ";
		}
	}

	/**
	 *  Create a file with name <i>filename</i> .
	 *
	 * @param  filename the name of the file to create.
	 * @return  Description of the Return Value
	 * @exception  FileNotFoundException Description of the Exception
	 * @exception  PandaException Description of the Exception
	 */
/*	protected static PrintStream _createFile(String filename)
		throws FileNotFoundException, EspamException {

		PrintStream printStream = null;
		UserInterface ui = UserInterface.getInstance();

		String directory = null;
		// Create the directory indicated by the '-o' option. Otherwise
		// select the orignal filename.
		if (ui.getOutputFileName() == "") {
			directory = ui.getBasePath() + "/" + ui.getFileName();
		} else {
			directory = ui.getBasePath() + "/" + ui.getOutputFileName();
		}
		File dir = new File(directory);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new EspamException(
					"could not create " + "directory '" + dir.getPath() + "'.");
			}
		}
		String fullFileName = dir + "/" + filename;

		OutputStream file = null;
		file = new FileOutputStream(fullFileName);
		printStream = new PrintStream(file);
		return printStream;
	}
*/

	///////////////////////////////////////////////////////////////////
	////                         protected variables                ///

	/**
	 *  Value for the added offset when indenting.
	 */
	protected static String _offset = "  ";

	/**
	 *  Prefix for indenting nested statement.
	 */
	protected String _prefix = "";

	/**
	 *  Stream where the print output is send to.
	 */
	protected PrintStream _printStream = null;
}
