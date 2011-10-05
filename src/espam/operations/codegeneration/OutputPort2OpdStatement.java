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

import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.parsetree.statement.MemoryStatement;
import espam.datamodel.parsetree.statement.FifoMemoryStatement;

import espam.operations.codegeneration.CodeGenerationException;
import espam.datamodel.parsetree.statement.OpdStatement;

//////////////////////////////////////////////////////////////////////////
//// OutputPort2IfOpdStatement

/**
 *  This class ...
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Domain2IfStatement.java,v 1.27 2002/01/24 23:11:40 aturjan
 *      Exp $
 */

public class OutputPort2OpdStatement {

    /**
     * @param  port Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException
     */
    public static OpdStatement convert(ADGOutPort port, CDProcess process)
        throws CodeGenerationException {

	OpdStatement opdStatement = null;

        try {
		String gateName = "";

		Vector outGates = process.getOutGates();
		Iterator i = outGates.iterator();
		while( i.hasNext() ) {
			CDOutGate gate = (CDOutGate) i.next();
			Vector portList = gate.getAdgPortList();
			Iterator j = portList.iterator();
			while( j.hasNext() ) {
				ADGOutPort outPort = (ADGOutPort) j.next();
				if( outPort.getName().equals(port.getName()) ) {
					gateName = gate.getName();
				}
			}
		}

		opdStatement = new OpdStatement();
		opdStatement.setProcessName( process.getName() );
		opdStatement.setGateName( gateName );
		opdStatement.setNodeName( port.getNode().getName() );
		opdStatement.setArgumentName( ((ADGVariable) port.getBindVariables().get(0)).getName() );
		opdStatement.setIndexList( ((ADGVariable) port.getBindVariables().get(0)).getIndexList() );

        } catch (Exception e) {
            throw new CodeGenerationException(
                " OutputPort2OpdStatement: "
                    + " Input port: "
                    + port.getName()
                    + " "
                    + e.getMessage());
        }
        return opdStatement;
    }


    public static Vector convert2list(ADGOutPort port, CDProcess process)
        throws CodeGenerationException {

	Vector opdStatementList = new Vector();

        try {
		String gateName = "";

		Vector outGates = process.getOutGates();
		Iterator i = outGates.iterator();
		while( i.hasNext() ) {
			CDOutGate gate = (CDOutGate) i.next();
			Vector portList = gate.getAdgPortList();
			Iterator j = portList.iterator();
			while( j.hasNext() ) {
				ADGOutPort outPort = (ADGOutPort) j.next();
				if( outPort.getName().equals(port.getName()) ) {
					gateName = gate.getName();
				}
			}
		}

		i = port.getBindVariables().iterator();
		while( i.hasNext() ) {
			ADGVariable bindVar = (ADGVariable) i.next();

			OpdStatement opdStatement = new OpdStatement();
			opdStatement.setProcessName( process.getName() );
			opdStatement.setGateName( gateName );
			opdStatement.setNodeName( port.getNode().getName() );
			opdStatement.setArgumentName( bindVar.getName() );
			opdStatement.setIndexList( bindVar.getIndexList() );
			opdStatementList.add( opdStatement );
		}

        } catch (Exception e) {
            throw new CodeGenerationException(
                " OutputPort2OpdStatement: "
                    + " Input port: "
                    + port.getName()
                    + " "
                    + e.getMessage());
        }
        return opdStatementList;
    }


}
