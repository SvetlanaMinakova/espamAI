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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.parsetree.statement.AssignStatement;
import espam.datamodel.parsetree.statement.LhsStatement;
import espam.datamodel.parsetree.statement.RhsStatement;
import espam.datamodel.parsetree.statement.VariableStatement;

import espam.datamodel.graph.adg.ADGVariable;

import espam.datamodel.graph.adg.ADGNode;

//////////////////////////////////////////////////////////////////////////
//// Node2AssignStatement

/**
 *  This class ...
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Node2Assignment.java,v 1.24 2002/09/30 13:34:15 kienhuis
 *      Exp $
 */

public class Node2AssignStatement {

    /**
     *  converts a node description into an AssignStatement.
     *
     * @param  node the node to convert.
     * @return  node that describing part of the parse tree.
     * @exception  CodeGenerationException
     */
    public static AssignStatement convert(ADGNode node)
             throws CodeGenerationException {

	AssignStatement assignStatement = new AssignStatement();
	assignStatement.setNodeName( node.getName() );
	assignStatement.setFunctionName( node.getFunction().getName() );

      	LhsStatement lhsStatement = new LhsStatement();
       	RhsStatement rhsStatement = new RhsStatement();

	Vector arg = node.getFunction().getOutArgumentList();
	Iterator i = arg.iterator();
	while( i.hasNext() ) {
	        VariableStatement var = new VariableStatement( ((ADGVariable) i.next()).getName() );
		lhsStatement.addChild( var );
		var.setParent(lhsStatement);
	}

	arg = node.getFunction().getInArgumentList();
	i = arg.iterator();
	while( i.hasNext() ) {
	        VariableStatement var = new VariableStatement( ((ADGVariable) i.next()).getName() );
		rhsStatement.addChild( var );
                var.setParent(rhsStatement);
	}

	assignStatement.addChild( lhsStatement );
	lhsStatement.setParent(assignStatement);

	assignStatement.addChild( rhsStatement );
        rhsStatement.setParent(assignStatement);

	return assignStatement;
    }
}
