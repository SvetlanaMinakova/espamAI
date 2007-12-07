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
import espam.datamodel.parsetree.statement.ForStatement;

import espam.utils.polylib.PolyLib;
import espam.utils.polylib.Polyhedron;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.matrix.SignedMatrix;
import espam.utils.util.Convert;

//////////////////////////////////////////////////////////////////////////
//// Node2ForStatements

/**
 *  This class converts a part of a Node domain (Polytope) into 
 *  one or more For Statements.
 *
 * @author  Todor Stefanov
 * @version  $Id: Node2ForStatement.java,v 1.21 2002/06/05 12:23:42
 *      stefanov Exp $
 */

public class Node2ForStatements {

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
        Vector forStatements = new Vector();

	if ( polytope.getIndexVector().getIterationVector().size() != 0 ) {

            // Get the contraints from the domain
           SignedMatrix A = polytope.getConstraints();

           Vector paramVec = new Vector();
	   paramVec.addAll( polytope.getIndexVector().getIterationVector() );
           paramVec.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
           paramVec.addAll( polytope.getIndexVector().getParameterVectorNames() );

           int nPars = polytope.getIndexVector().getParameterVector().size();
//            try {
                // Sort the constraints by level.
                Polyhedron D = PolyLib.getInstance().Constraints2Polyhedron(A);
                Polyhedron U = PolyLib.getInstance().UniversePolyhedron(nPars);
                Polyhedron S = PolyLib.getInstance().PolyhedronScan(D, U);

                /*
                 *  Let dimIter be the dimension of the iteration vector.
                 * Construct ForStatements for every level from 1 to dimIter.
                 */
                int dimIter   = polytope.getIndexVector().getIterationVector().size();

                Iterator i = S.domains();
                SignedMatrix M;
                int sign;
                boolean isNonEmpty;

                for (int level = 1; level <= dimIter; level++) {

                    index = paramVec.get(level - 1).toString();
                    M = PolyLib.getInstance().Polyhedron2Constraints((Polyhedron) i.next());
		    //System.out.println("MMMMMMM: " + M.toString());
                    //Vector v = MatrixLib.toLinearExpression(M, paramVec);
                    List<Expression> v = Convert.toLinearExpression(M, paramVec);

                    // v is a vector of linear expressions containing constraint
                    Iterator j = v.iterator();
                    int ctr = 0;

                    while (j.hasNext()) {

                        Expression exp = (Expression) j.next();
                        int row = ctr++;

                        long coefficient = M.getElement(row, level);
                        if (coefficient < 0) {
                            sign = 1;
                            exp.addVariable(-(int) coefficient, 1, index);

                            //still I have to devide all the terms with the coefficient
                            exp.setDenominator(-(int) coefficient);

                            upperBound.add(exp);

                            if (exp.getEqualityType() == Expression.EQU) {
                               lowerBound.add(exp);
                            }
                        } else if (coefficient > 0) {
                            sign = -1;
                            exp.addVariable(-(int) coefficient, 1, index);
                            exp.negate();
                            exp.setDenominator((int) coefficient);

                            lowerBound.add(exp);
                            if (exp.getEqualityType() == Expression.EQU) {
                               upperBound.add(exp);
                            }
                        } else {
                        }
                    }

                    Expression finalUpperBound;
                    Expression finalLowerBound;
                    Expression one;
                    Expression two;
                    LinTerm term;
                    if (upperBound.size() > 1) {
                        one = (Expression) upperBound.get(0);
                        for (int tc = 1; tc < upperBound.size(); tc++) {
                            two = (Expression) upperBound.get(tc);
                            term = new MinimumTerm(one, two);
                            one = new Expression();
                            one.add(term);
                        }
                        finalUpperBound = one;
                    } else if (upperBound.size() == 1) {
                        finalUpperBound = (Expression) upperBound.get(0);
                    } else {
                        finalUpperBound =new Expression();
                        finalUpperBound.addVariable(1,1, index);
		    }

                    if (lowerBound.size() > 1) {
                       one = (Expression) lowerBound.get(0);
                       for (int tc = 1; tc < lowerBound.size(); tc++) {
                           two = (Expression) lowerBound.get(tc);
                           term = new MaximumTerm(one, two);
                           one = new Expression();
                           one.add(term);
                       }
                       finalLowerBound = one;
                    } else {
                       finalLowerBound = (Expression) lowerBound.get(0);
                    }
                    ForStatement fs = new ForStatement(index, finalLowerBound,
                                                                             finalUpperBound, 1);
                    upperBound.clear();
                    lowerBound.clear();
                    forStatements.add(fs);
                }

//            } catch (Exception e) {
//                e.printStackTrace(System.out);
//                throw new CodeGenerationException(" Polytope2ForStatement " +
//                        "Exception: " + e.getMessage());
//            }
        }
        return forStatements;
    }


}
