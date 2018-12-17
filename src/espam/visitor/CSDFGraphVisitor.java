package espam.visitor;

import espam.datamodel.graph.csdf.CSDFEdge;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;

/**
 *  This class is an abstract class for a visitor that is used to generate
 *  Static Dataflow Graph description.
 *
 *  @author minakovas
 */
public class CSDFGraphVisitor extends GraphVisitor {
     ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Visit a csdfGraph component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFGraph x) {
    }

    /**
     *  Visit a csdfNode component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFNode x) {
    }

    /**
     *  Visit a csdfPort component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFPort x) {
    }

    /**
     *  Visit an csdfEdge component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFEdge x) {
    }

    /**
     *  Visit an IndexPair component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(IndexPair x) {
    }

       /**
     *  Increment the indentation.
     */
    protected void _prefixInc() {
        _prefix += _offset;
    }

    /**
     *  Decrement the indentation.
     */
    protected void _prefixDec() {
        if (_prefix.length() >= _offset.length()) {
            _prefix = _prefix.substring(_offset.length());
        }
    }
}
