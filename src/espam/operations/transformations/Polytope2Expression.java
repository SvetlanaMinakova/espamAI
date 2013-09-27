
package espam.operations.transformations;

import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import espam.datamodel.domain.Polytope;

import espam.utils.polylib.PolyLib;
import espam.utils.polylib.Polyhedron;
import espam.utils.symbolic.expression.*;
import espam.utils.symbolic.matrix.SignedMatrix;
import espam.utils.util.Convert;


//////////////////////////////////////////////////////////////////////////
//// Polytope2Expression

/**
 *  This class gets the vector of expressions of a polytope
 *
 * @author  Ying Tao
 * @version  $Id: Polytope2Expression.java,v 1.1 2006/06/19 16:26:12
 *
 */

public class Polytope2Expression{
    
    
    public static Vector<Expression> getExpression(Polytope polytope) {
        
        Vector <Expression> vectorExpr = new Vector <Expression>();
        
//      Get the contraints from the domain
        SignedMatrix A = polytope.getConstraints();
        if ( A != null) {
            
            Vector paramVec = new Vector();
//           paramVec.addAll( polytope.getIndexVector().getIterationVector() );
//           paramVec.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
//           paramVec.addAll( polytope.getIndexVector().getParameterVectorNames() );
            paramVec = polytope.getIndexVector().getVectorsNames();
            
            //Vector v = MatrixLib.toLinearExpression(A, paramVec);
            List<Expression> v = Convert.toLinearExpression(A, paramVec);
            
            // v is a vector of linear expressions containing constraint
            Iterator j = v.iterator();
            
            
            while (j.hasNext()) {
                Expression exp = (Expression) j.next();
                
                if(exp.isAlwaysTrue()){
                    //if the condition A>=0 is always true, not needed to add to the control vector
                }
                else{
                    vectorExpr.add(exp);
                }
                
                
            }
            
        }
        return vectorExpr;
    }
    
    
}
