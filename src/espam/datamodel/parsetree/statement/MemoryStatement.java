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

package espam.datamodel.parsetree.statement;

import espam.datamodel.graph.adg.ADGInPort;

//////////////////////////////////////////////////////////////////////////
//// MemoryStatement

/**
 *  This class represents a Memory Statement.
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: MemoryStatement.java,v 1.3 2002/10/08 14:23:13 kienhuis
 *      Exp $
 */

public class MemoryStatement extends Statement {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////
	
	/**
	 *  Empty Memory Statement Constructor.
	 */
	public MemoryStatement(String str) {
		super(str);
	        _port = new ADGInPort("");		
	}

        /**
         *  Clone this MemoryStatement
         *
         * @return  a new instance of the MemoryStatement.
         */
         public Object clone() {

               MemoryStatement ms = (MemoryStatement) super.clone();
               ms.setPort( (ADGInPort) _port.clone() );
               return (ms);
         }

	/**
	 *  Gets the port attribute of the MemoryStatement object
	 *
	 * @return  The port value
	 */
	public ADGInPort getPort() {
		return _port;
	}

	/**
	 *  Sets the port attribute of the MemoryStatement object
	 *
	 */
	public void setPort(ADGInPort port) {
		_port = port;
	}

	/**
	 *  Give the string representation of the if statement.
	 *
	 * @return  a string representing the if statement.
	 */
	public String toString() {
		String ln = " MemoryStatement: ";
		return ln;
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 */
	private ADGInPort _port = null;
}
