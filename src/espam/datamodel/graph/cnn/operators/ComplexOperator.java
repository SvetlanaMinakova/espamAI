package espam.datamodel.graph.cnn.operators;

import java.util.HashMap;
import java.util.Vector;

public class ComplexOperator extends Operator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public ComplexOperator(String name){
        super(name);
        _subOperators = new Vector<>();
        _internalBuffers = new Vector<>();
    }

    public ComplexOperator(String name, String basic){
        super(name,name);
        _subOperators = new Vector<>();
        _internalBuffers = new Vector<>();
    }
   /**
     *  Clone this Operator
     * @return  a new reference on instance of the Operator
     */
    public Object clone() {
        ComplexOperator newObj = (ComplexOperator) super.clone();
        newObj._subOperators = (Vector<Operator>)(_subOperators).clone();
        newObj._internalBuffers = (Vector<InternalBuffer>)_internalBuffers.clone();
        return newObj;
    }

    public void setSubOperators(Vector<Operator> subOperators) {
        this._subOperators = subOperators;
    }

    public Vector<Operator> getSubOperators() {
        return _subOperators;
    }

    public Vector<InternalBuffer> getInternalBuffers() { return _internalBuffers; }

    public void setInternalBuffers(Vector<InternalBuffer> internalBuffers) {
        this._internalBuffers = internalBuffers;
    }

    ///////////////////////////////////////////////////////////////////
    ////       private variables                                  ////

    /** List of operators in execution order*/
    private Vector<Operator> _subOperators;
    private Vector<InternalBuffer> _internalBuffers;
}
