
package espam.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGCtrlVariable;
import espam.datamodel.graph.adg.ADGInVar;
import espam.datamodel.graph.adg.ADGFunction;

import espam.datamodel.domain.LBS;
import espam.datamodel.domain.FilterSet;
import espam.datamodel.domain.IndexVector;
import espam.datamodel.domain.Polytope;
import espam.datamodel.domain.ControlExpression;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// ADGraph Visitor

/**
 *  This class is an abstract class for a visitor that is used to generate
 *  Approximated Dependence Graph description.
 *
 * @author  Todor Stefanov
 * @version  $Id: ADGraphVisitor.java,v 1.2 2011/10/05 15:03:46 nikolov Exp $
 */

public class ADGraphVisitor extends GraphVisitor {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     *  Visit a ADGraph component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGraph x) {
    }
    
    /**
     *  Visit a ADGNode component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGNode x) {
    }
    
    /**
     *  Visit a ADGInPort component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGInPort x) {
    }
    
    /**
     *  Visit a ADGOutPort component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGOutPort x) {
    }
    
    /**
     *  Visit an ADGEdge component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGEdge x) {
    }
    
    /**
     *  Visit a ADGParameter component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGParameter x) {
    }
    
    /**
     *  Visit a ADGVariable component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGVariable x) {
    }
    
    /**
     *  Visit a ADGCtrlVariable component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGCtrlVariable x) {
    }
    
    /**
     *  Visit an ADGInVar component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGInVar x) {
    }
    
    /**
     *  Visit an ADGFunction component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ADGFunction x) {
    }
    
    /**
     *  Visit a LBS component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(LBS x) {
    }
    
    /**
     *  Visit a FilterSet component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(FilterSet x) {
    }
    
    /**
     *  Visit a IndexVector component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(IndexVector x) {
    }
    
    /**
     *  Visit a Polytope component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Polytope x) {
    }
    
    /**
     *  Visit a Control Expression component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(ControlExpression x) {
    }
}
