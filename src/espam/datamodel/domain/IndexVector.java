
package espam.datamodel.domain;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

import espam.datamodel.graph.adg.ADGParameter;

//////////////////////////////////////////////////////////////////////////
//// IndexVector

/**
 * This class describes the meaning of the columns of a sign matrix
 *
 * @author Hristo Nikolov
 * @version  $Id: IndexVector.java,v 1.2 2011/10/05 15:03:46 nikolov Exp $
 */

public class IndexVector implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a IndexVector with a name.
     *
     */
    public IndexVector() {
        _iterationVector = new Vector();   // vector of strings
        _staticCtrlVector = new Vector();  // vector of ControlExpressions
        _dynamicCtrlVector = new Vector(); // vector of strings
        _parameterVector = new Vector();   // vector of Parameters 
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(ADGraphVisitor x) {
        x.visitComponent(this);
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof IndexVector))
            return false;
        IndexVector o = (IndexVector) obj;
        return _iterationVector.equals(o._iterationVector)
            && _staticCtrlVector.equals(o._staticCtrlVector)
            && _dynamicCtrlVector.equals(o._dynamicCtrlVector)
            && _parameterVector.equals(o._parameterVector);
    }
    
    /**
     *  Clone this IndexVector
     *
     * @return  a new instance of the IndexPort.
     */
    public Object clone() {
        try {
            IndexVector newObj = (IndexVector) super.clone();
            newObj.setIterationVector( (Vector) _iterationVector.clone() );
            newObj.setStaticCtrlVector( (Vector) _staticCtrlVector.clone() );
            newObj.setDynamicCtrlVector( (Vector) _dynamicCtrlVector.clone() );
            newObj.setParameterVector( (Vector) _parameterVector.clone() );      
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the vector of indexes of this IterationVector.
     *
     * @return  the indexVector
     */
    public Vector<String> getIterationVector() {
        return _iterationVector;
    }
    
    /**
     *  Set the vector of indexes of this IterationVector.
     *
     * @param  indexvector The new indexVector
     */
    public void setIterationVector(Vector<String> indexVector) {
        _iterationVector = indexVector;
    }
    
    /**
     *  Get the static control vector of this IndexVector.
     *
     * @return  the static control vector
     */
    public Vector<ControlExpression> getStaticCtrlVector() {
        return _staticCtrlVector;
    }
    
    /**
     *  Get the names of the static control expressions of this IndexVector.
     *
     * @return  the names of the static control expressions
     */
    public Vector<String> getStaticCtrlVectorNames() {
        
        Vector<String> names = new Vector<String>();
        ControlExpression curCtrl;
        
        Iterator i;  
        i = _staticCtrlVector.iterator();
        
        while( i.hasNext() ) {
            curCtrl = (ControlExpression) i.next();
            names.add(curCtrl.getName());
        }
        
        return names;
    }
    
    /**
     *  Set the static control vector of this IndexVector.
     *
     * @param  static control vector The new static control vector
     */
    public void setStaticCtrlVector(Vector<ControlExpression> staticCtrlVector) {
        _staticCtrlVector = staticCtrlVector;
    }
    
    /**
     *  Get the dynamic control vector of this IndexVector.
     *
     * @return  the dynamic control vector
     */
    public Vector getDynamicCtrlVector() {
        return _dynamicCtrlVector;
    }
    
    /**
     *  Set the dynamic control vector of this IndexVector.
     *
     * @param  dynamic control vector The new dynamic control vector
     */
    public void setDynamicCtrlVector(Vector dynamicCtrlVector) {
        _dynamicCtrlVector = dynamicCtrlVector;
    }
    
    /**
     *  Get the parameters vector of this IndexVector.
     *
     * @return  the parameterVector
     */
    public Vector<ADGParameter> getParameterVector() {
        return _parameterVector;
    }
    
    /**
     *  Get the names of the parameters of this IndexVector.
     *
     * @return  the names of the parameters
     */
    public Vector getParameterVectorNames() {
        Vector names = new Vector();
        ADGParameter curPar;
        
        Iterator i;
        i = _parameterVector.iterator();
        
        while( i.hasNext() ) {
            curPar = (ADGParameter) i.next();
            names.add(curPar.getName());
        }
        
        return names;
    }
    
    /**
     *  Get the names of the vectors contents in this IndexVector.
     *
     * @return  the names of the parameters
     */
    public Vector getVectorsNames() {
        
        Vector concVectNames = new Vector();
        concVectNames.addAll( getIterationVector() );
        concVectNames.addAll( getStaticCtrlVectorNames() );
        concVectNames.addAll( getDynamicCtrlVector() );
        concVectNames.addAll( getParameterVectorNames() );
        
        return concVectNames;
    }
    
    /**
     *  Set the parameres vector of this IndexVector.
     *
     * @param  parameterVector The new parameterVector
     */
    public void setParameterVector(Vector<ADGParameter> parameterVector) {
        _parameterVector = parameterVector;
    }
    
    /**
     *  Return a description of the .
     *
     * @return  a description of the .
     */
    public String toString() {
        return "Index Vector: " + _iterationVector + ", " +
            _staticCtrlVector + ", " +
            _dynamicCtrlVector + ", " +
            _parameterVector;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Vector of iterators.
     */
    private Vector<String> _iterationVector = null;
    
    /**
     *  The static control expression is a vector of ControlExpressions
     *  A control expression contains a name and an expression. 
     *  In "d1 = div(i,2)" 'd1' is the name and 'div(i,2)' is the expression
     */
    private Vector<ControlExpression> _staticCtrlVector = null;
    
    /**
     *  Vector of dynamic control expressions (names only).
     */
    private Vector<String> _dynamicCtrlVector = null;
    
    /**
     *  Vector of parameters (the class contains name, lower bound, upper bound and
     *  default value).
     */
    private Vector<ADGParameter> _parameterVector = null;
}
