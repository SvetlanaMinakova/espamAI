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
 * @version  $Id: ADGraphVisitor.java,v 1.1 2007/12/07 22:07:24 stefanov Exp $
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
