
package espam.operations.codegeneration;

import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import espam.datamodel.domain.Polytope;
import espam.datamodel.parsetree.statement.IfStatement;

import espam.operations.codegeneration.CodeGenerationException;

import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.matrix.SignedMatrix;
import espam.utils.util.Convert;
import espam.utils.polylib.PolyLib;
//import espam.utils.polylib.Polyhedron;
//import espam.utils.util.ProcLib;

//////////////////////////////////////////////////////////////////////////
//// Polytope2IfStatements

/**
 *  This class ...
 *
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Domain2IfStatement.java,v 1.27 2002/01/24 23:11:40 aturjan
 *      Exp $
 */

public class Polytope2IfStatements {
    
    /**
     * @param  polytope Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException
     */
    public static Vector convert(Polytope polytope)
        throws CodeGenerationException {
        
        Vector ifStatements = new Vector();
        
        try {
            
            SignedMatrix A = polytope.getConstraints();
            Vector strVect = new Vector();
            
//     strVect.addAll( polytope.getIndexVector().getIterationVector() );
//     strVect.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
//     strVect.addAll( polytope.getIndexVector().getDynamicCtrlVector() );
//     strVect.addAll( polytope.getIndexVector().getParameterVectorNames() );
            strVect = polytope.getIndexVector().getVectorsNames();
            
            //Vector v = MatrixLib.toLinearExpression(A, strVect);
            List<Expression> v = Convert.toLinearExpression(A, strVect);
            Iterator j = v.iterator();
            while( j.hasNext() ) {
                Expression exp = (Expression) j.next();
                if( !exp.isAlwaysTrue() ) {
                    IfStatement ifStatement = new IfStatement( exp, exp.getEqualityValue() );
                    ifStatements.add( ifStatement );
                }
            }
            
        } catch( Exception e ) {
            e.printStackTrace(System.out);
            throw new CodeGenerationException(
                                              " Polytope2IfStatement: "
                                                  + " "
                                                  + e.getMessage());
        }
        return ifStatements;
    }
    
    /**
     * @param  polytope Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CodeGenerationException
     */
    public static Polytope simplifyPDinND(Polytope pfd, Polytope nd)
        throws CodeGenerationException {
        
        Polytope sp = (Polytope) pfd.clone(); // pfd represents a port domain or a function domain
        SignedMatrix sND = null;
        try {
            
            int iSizePFD = pfd.getIndexVector().getIterationVector().size();
            int cSizePFD = pfd.getIndexVector().getStaticCtrlVectorNames().size();
            int dSizePFD = pfd.getIndexVector().getDynamicCtrlVector().size();
            int pSizePFD = pfd.getIndexVector().getParameterVectorNames().size();
            
            int iSizeND = nd.getIndexVector().getIterationVector().size();
            int cSizeND = nd.getIndexVector().getStaticCtrlVectorNames().size();
            int dSizeND = nd.getIndexVector().getDynamicCtrlVector().size();
            
            if ( nd.getIndexVector().getIterationVector().size() != 0 ) {
                
                sND = (SignedMatrix) nd.getConstraints().clone();
                //make the node domain with the same size as the port domain or the function domain
                sND.insertZeroColumns( (cSizePFD + dSizePFD) - (cSizeND + dSizeND), iSizeND + cSizeND + dSizeND + 1 );
                //add the context constraints to the node domain
                SignedMatrix ndContext = (SignedMatrix) nd.getContext().clone();
                ndContext.insertZeroColumns(iSizeND+cSizePFD+dSizePFD, 1 );
                sND.insertRows(ndContext, -1);
                
            } else {
                
                sND = (SignedMatrix) nd.getContext().clone();
                
            }
            
            //simplify the port domain or the function domain in the context of the node domain
            SignedMatrix sPFD = pfd.getConstraints();
            SignedMatrix A = PolyLib.getInstance().ConstraintsSimplify(sPFD, sND);
            sp.setConstraints( A );
            
        } catch( Exception e ) {
            e.printStackTrace(System.out);
            throw new CodeGenerationException(
                                              " simplifyPNinND: "
                                                  + " "
                                                  + e.getMessage());
        }
        return sp;
    }
    
    
}
