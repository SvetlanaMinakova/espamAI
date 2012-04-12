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

package espam.operations.codegeneration;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.parsetree.statement.MemoryStatement;
import espam.datamodel.parsetree.statement.FifoMemoryStatement;
import espam.datamodel.LinearizationType;

import espam.main.UserInterface;

import espam.operations.codegeneration.CodeGenerationException;

//////////////////////////////////////////////////////////////////////////
//// InputPort2IfMemoryStatement

/**
 *  This class ...
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Domain2IfStatement.java,v 1.27 2002/01/24 23:11:40 aturjan
 *      Exp $
 */

public class InputPort2MemoryStatement {

    /**
     * @param  port Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException
     */
    public static MemoryStatement convert(ADGInPort port, CDProcess process)
        throws CodeGenerationException {

        FifoMemoryStatement memoryStatement = null;

	try {

		LinearizationType comModel = null;
		String gateName = "";

		Vector inGates = process.getInGates();
		Iterator i = inGates.iterator();
		while( i.hasNext() ) {
			CDInGate gate = (CDInGate) i.next();
			Vector portList = gate.getAdgPortList();
			Iterator j = portList.iterator();
			while( j.hasNext() ) {
				ADGInPort inPort = (ADGInPort) j.next();
				if( inPort.getName().equals(port.getName()) ) {
					comModel = ((CDChannel)gate.getChannel()).getCommunicationModel();
					gateName = gate.getName();

				}
			}
		}

		UserInterface ui = UserInterface.getInstance();

		if( comModel == LinearizationType.fifo ||
		    comModel == LinearizationType.BroadcastInOrder ||
		    comModel == LinearizationType.sticky_fifo ||
		    comModel == LinearizationType.shift_register ||
		    (comModel == LinearizationType.GenericOutOfOrder &&
		    ui.getIseFlag() == true) ) {      // for now, we only support out-of-order in the ISE visitor
			memoryStatement = new FifoMemoryStatement();
			memoryStatement.setProcessName( process.getName() );
			memoryStatement.setGateName( gateName );
			memoryStatement.setNodeName( port.getNode().getName() );
			memoryStatement.setArgumentList( port.getBindVariables() );

		} else {
			System.out.println( "ERROR: unsupported channeltype " + comModel.toString());
			System.exit(0);
		}

        } catch (Exception e) {
            throw new CodeGenerationException(
                " InputPort2MemoryStatement: "
                    + " Input port: "
                    + port.getName()
                    + " "
                    + e.getMessage());
        }
        return memoryStatement;
    }
}
