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

	    strVect.addAll( polytope.getIndexVector().getIterationVector() );
	    strVect.addAll( polytope.getIndexVector().getStaticCtrlVectorNames() );
	    strVect.addAll( polytope.getIndexVector().getParameterVectorNames() );

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
    public static Polytope simplifyPDinND(Polytope pd, Polytope nd)
        throws CodeGenerationException {

	Polytope sp = (Polytope) pd.clone();
        SignedMatrix sND = null;
        try {

	    int iSizePD  = pd.getIndexVector().getIterationVector().size();
            int cSizePD = pd.getIndexVector().getStaticCtrlVectorNames().size();
            int pSizePD = pd.getIndexVector().getParameterVectorNames().size();

	    int iSizeND  = nd.getIndexVector().getIterationVector().size();
	    int cSizeND = nd.getIndexVector().getStaticCtrlVectorNames().size();

            if ( nd.getIndexVector().getIterationVector().size() != 0 ) {

                sND = (SignedMatrix) nd.getConstraints().clone();
	        //make the node domain with the same size as the port domain
	        sND.insertZeroColumns( cSizePD - cSizeND, iSizeND + cSizeND +1 );
	        //add the context constraints to the node domain
	        SignedMatrix ndContext = (SignedMatrix) nd.getContext().clone();
	        ndContext.insertZeroColumns(iSizeND+cSizePD, 1 );
                sND.insertRows(ndContext, -1);

	    } else {

	        sND = (SignedMatrix) nd.getContext().clone();

	    }

           //simplify the port domain in the context of the node domain
	   SignedMatrix sPD = pd.getConstraints();
           SignedMatrix A = PolyLib.getInstance().ConstraintsSimplify(sPD, sND);
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
