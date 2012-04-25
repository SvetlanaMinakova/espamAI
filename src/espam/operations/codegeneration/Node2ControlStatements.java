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
import java.util.HashMap;

import espam.datamodel.parsetree.statement.ControlStatement;
import espam.datamodel.domain.ControlExpression;
import espam.datamodel.domain.Polytope;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGPort;

import espam.main.UserInterface;

//////////////////////////////////////////////////////////////////////////
//// Node2ControlStatements

/**
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Domain2ControlStatement.java,v 1.6 2002/09/30 12:16:39
 *      kienhuis Exp $
 */

public class Node2ControlStatements {

    /**
     * @param  x Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException MyException If such and such
     *      occurs
     */
    public static Vector convert(ADGNode x) {

        boolean bMultiApp = false;
        if(_ui.getADGFileNames().size() > 1) {
            bMultiApp = true;
        }   

	Vector cStatements = new Vector();
	HashMap  tmp = new HashMap();

	Iterator i = x.getPortList().iterator();
	while( i.hasNext() ) {
		ADGPort port = (ADGPort) i.next();
		Vector staticCtrl = ((Polytope)port.getDomain().getLinearBound().get(0)).getIndexVector().getStaticCtrlVector();
		Iterator j = staticCtrl.iterator();
		while( j.hasNext() ) {
			ControlExpression cExp = (ControlExpression) j.next();
                        String expName = cExp.getName();

                        if ( !tmp.containsKey(expName) ) {

			      tmp.put(expName, "");
  			      ControlStatement statement = new ControlStatement( expName, cExp.getExpression(), 1 );
                              cStatements.add( statement );

			}

		}
	}

	Iterator j = x.getExpressionList().iterator();
	while( j.hasNext() ) {
		ControlExpression cExp = (ControlExpression) j.next();
                String expName = cExp.getName();
                if( bMultiApp ) {
                     expName += "_" + x.getName();
                }

                if ( !tmp.containsKey(expName) ) {

		      tmp.put(expName, "");
		      ControlStatement statement = new ControlStatement( expName, cExp.getExpression(), 1 );
                      cStatements.add( statement );
		}
	}

        return cStatements;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///

    private static UserInterface _ui = UserInterface.getInstance();
}

