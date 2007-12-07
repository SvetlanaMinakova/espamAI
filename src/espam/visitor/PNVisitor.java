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

import espam.datamodel.pn.ProcessNetwork;
import espam.datamodel.pn.Process;
import espam.datamodel.pn.Gate;
import espam.datamodel.pn.Channel;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Process Network Visitor

/**
 *  This class is an abstract class for a visitor that is used to generate a
 *  Process Network description.
 *
 * @author  Todor Stefanov
 * @version  $Id: PNVisitor.java,v 1.1 2007/12/07 22:07:24 stefanov Exp $
 */

public class PNVisitor implements Visitor {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Visit a ProcessNetwork component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ProcessNetwork x) {
	}

	/**
	 *  Visit a Process component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Process x) {
	}

	/**
	 *  Visit a Gate component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Gate x) {
	}

	/**
	 *  Visit a Channel component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Channel x) {
	}

	///////////////////////////////////////////////////////////////////
	////                         protected methods                  ///

	/**
	 *  Decrement the indentation.
	 */
	protected void _prefixDec() {
		if( _prefix.length() >= _offset.length() ) {
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
