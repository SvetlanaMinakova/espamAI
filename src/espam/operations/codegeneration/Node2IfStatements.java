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
import java.util.List;

import espam.datamodel.domain.Polytope;
import espam.datamodel.parsetree.statement.IfStatement;

import espam.utils.polylib.PolyLib;
import espam.utils.polylib.Polyhedron;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.matrix.SignedMatrix;
import espam.utils.util.Convert;

//////////////////////////////////////////////////////////////////////////
//// Node2IfStatements

/**
 *  This class converts a part of a Node domain (Polytope) into
 *  one or more IF Statements.
 *
 * @author  Todor Stefanov
 * @version  $Id: Node2IfStatement.java,v 1.21 2002/06/05 12:23:42
 *      stefanov Exp $
 */

public class Node2IfStatements {

    /**
     * @param  node Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException MyException If such and such
     *      occurs
     */
    public static Vector convert(Polytope polytope) {

        Vector upperBound = new Vector();
        Vector lowerBound = new Vector();
        String index = "";
        Vector ifStatements = new Vector();

	if ( polytope.getIndexVector().getIterationVector().size() != 0 ) {

            // Get the contraints from the domain
           SignedMatrix A = polytope.getConstraints();

           Vector paramVec = new Vector();
	   paramVec.addAll( polytope.getIndexVector().getIterationVector() );
           paramVec.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
           paramVec.addAll( polytope.getIndexVector().getParameterVectorNames() );


           int nPars = polytope.getIndexVector().getParameterVector().size();

                // Sort the constraints by level.
                Polyhedron D = PolyLib.getInstance().Constraints2Polyhedron(A);
                Polyhedron U = PolyLib.getInstance().UniversePolyhedron(nPars);
                Polyhedron S = PolyLib.getInstance().PolyhedronScan(D, U);

                /*
                 *  Let dimIter be the dimension of the iteration vector and
		 *  dimCtrl be the dimension of the static control vector.
                 * Construct IfStatements for every level
                 *  dimIter+1 to dimIter + dimCtrl
                 */
                int dimIter   = polytope.getIndexVector().getIterationVector().size();
                int dimCtrl   = polytope.getIndexVector().getStaticCtrlVectorNames().size();

                Iterator i = S.domains();
                SignedMatrix M;
                int sign;
                boolean isNonEmpty;

                for (int level = 1; level <= dimIter; level++) {
		    // DO NOTHING
                    Polyhedron p = (Polyhedron) i.next();
		}

                for (int level = dimIter + 1; level <= dimIter + dimCtrl; level++) {

                    index = paramVec.get(level - 1).toString();
                    M = PolyLib.getInstance().Polyhedron2Constraints( (Polyhedron) i.next() );
                    //Vector v = MatrixLib.toLinearExpression(M, paramVec);
                    List<Expression> v = Convert.toLinearExpression(M, paramVec);

                    // v is a vector of linear expressions containing constraint
                    Iterator j = v.iterator();
                    while (j.hasNext()) {
                         Expression exp = (Expression) j.next();
                         if ( !exp.isAlwaysTrue() ) {
                            IfStatement ifStatement = new IfStatement( exp, exp.getEqualityValue() );
                            ifStatements.add( ifStatement );
                         }
                    }

                }

        }
        return ifStatements;
    }


}
