
package espam.operations.transformations;

import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import espam.datamodel.domain.Polytope;

import espam.utils.polylib.PolyLib;
import espam.utils.polylib.Polyhedron;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.matrix.SignedMatrix;
import espam.utils.util.Convert;

//////////////////////////////////////////////////////////////////////////
//// Polytope2IndexBoundVector

/**
 *  This class gets the vector of expressions of lower and upper bounds of indexes of a polytope
 *
 * @author  Ying Tao, Sven van Haastregt
 * @version  $Id: Polytope2IndexBoundVector.java,v 1.3 2011/10/05 15:03:46 nikolov Exp $
 *
 */

public class Polytope2IndexBoundVector {
    
    
    public static Vector<Expression> getExpression(Polytope polytope) {
        
        Vector upperBound = new Vector();
        Vector lowerBound = new Vector();
        String index = "";
        Vector <Expression> vectorBounds = new Vector <Expression>();
        
        if ( polytope.getIndexVector().getIterationVector().size() != 0 ) {
            
            // Get the contraints from the domain
            SignedMatrix A = polytope.getConstraints();
            
            Vector paramVec = new Vector();
//           paramVec.addAll( polytope.getIndexVector().getIterationVector() );
//           paramVec.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
//           paramVec.addAll( polytope.getIndexVector().getParameterVectorNames() );
            paramVec = polytope.getIndexVector().getVectorsNames();
            
            int nPars = polytope.getIndexVector().getParameterVector().size();
//            try {
            // Sort the constraints by level. level 1 is the most out loop index
            Polyhedron D = PolyLib.getInstance().Constraints2Polyhedron(A);
            Polyhedron U = PolyLib.getInstance().UniversePolyhedron(nPars);
            Polyhedron S = PolyLib.getInstance().PolyhedronScan(D, U);
            /*
             *  Let dim(i) be the dimension of the the index
             *  vector. Construct ForStatements for every level 1, 2, ...,
             *  dim(i). For the constraints at level dim(i)+1, ...,
             *  dim(i) + dim(c) if statements are to be constructed.
             */
            Iterator i = S.domains();
            SignedMatrix M;
            int sign;
            boolean isNonEmpty;
            
            for (int level = 1; level <= polytope.getIndexVector().getIterationVector().size(); level++) {
                
                index = paramVec.get(level - 1).toString();
                Polyhedron pp = (Polyhedron)i.next();
                
                M = PolyLib.getInstance().Polyhedron2Constraints(pp);
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
                
                lowerBound = _simplifyEquivBound(level-1, lowerBound, vectorBounds, paramVec);
                upperBound = _simplifyEquivBound(level-1, upperBound, vectorBounds, paramVec);
                
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
                } else {
                    finalUpperBound = (Expression) upperBound.get(0);
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
                
                finalLowerBound.simplify();
                finalUpperBound.simplify();
                
                vectorBounds.add(finalLowerBound);
                vectorBounds.add(finalUpperBound);
                
                
                upperBound.clear();
                lowerBound.clear();
                
            }
            
//            } catch (Exception e) {
//                e.printStackTrace(System.out);
//                throw new CodeGenerationException(" Polytope2ForStatement " +
//                        "Exception: " + e.getMessage());
//            }
        }
        return vectorBounds;
    }
    
    
    
    /**
     * This function tries to repair/simplify bounds arising from equalities in a polyhedron. getExpression produces
     * unnecessarily complicated bounds when 2 or more equalities that depend on outer iterators are present.
     * E.g., the matrix        0 -1  0  1 -6
     *                         0 -1  1  0  0
     *                         1  1  0  0 -1
     *                         1 -1  0  0  5
     *                        
     * Results in bounds:      c0 := [1]          --  [5]
     *                         c1 := [c0]         --  [c0]
     *                         c2 := [c0+6, c1+6] --  [c0+6, c1+6]
     *                        
     * This function           c0 := [1]          --  [5]
     * simplifies them into:   c1 := [c0]         --  [c0]
     *                         c2 := [c0+6]       --  [c0+6]
     */
    private static Vector<Expression> _simplifyEquivBound(int level, Vector<Expression> expr, Vector<Expression> outerBounds, Vector paramVec) {
        if (expr.size() <= 1) {
            // Nothing to simplify
            return expr;
        }
        
        for (int i = 0; i < expr.size(); i++) {
            // Try to substitute inner iterators by more outer iterators.
            for (int j = level-1; j >= 0; j--) {
                String index = (String)paramVec.get(j);
                Expression lb = outerBounds.get(2*j);
                Expression ub = outerBounds.get(2*j+1);
                if (lb.equals(ub)) {
                    // Only substitute if candidate substitute's LB == UB
                    Expression newExpr = expr.get(i).substituteExpression(index, lb);
                    newExpr.simplify();
                    expr.set(i, newExpr);
                }
            }
        }
        
        // Now prune all expressions that have become equal
        for (int i = 0; i < expr.size(); i++) {
            for (int j = i+1; j < expr.size(); j++) {
                //System.out.println(expr.get(i) + " ?= " + expr.get(j) + "  : " + expr.get(i).equals(expr.get(j)));
                if (expr.get(i).equals(expr.get(j))) {
                    System.out.println("[Polytope2IndexBoundVector] Removing redundant expression: " + expr.get(j).toString());
                    expr.remove(j);
                    j--;
                }
            }
        }
        return expr;
    }
    
}
