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

package espam.visitor.xml.adg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.ADGraphVisitor;

import espam.datamodel.graph.Node;
import espam.datamodel.graph.NPort;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGPort;
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
import espam.datamodel.LinearizationType;

import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.expression.Expression;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// ADGraphXmlVisitor

/**
 *  This class is a visitor that is used to generate
 *  Approximated Dependence Graph description in Xml format.
 *
 * @author  Todor Stefanov
 * @version  $Id: ADGraphXmlVisitor.java,v 1.1 2007/12/07 22:07:35 stefanov Exp $
 */

public class ADGraphXmlVisitor extends ADGraphVisitor {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

       /**
        * Constructor for the ADGraphXmlVisitor object
        *
        * @param printStream
        *            the output Xml print stream
        */
        public ADGraphXmlVisitor(PrintStream printStream) {
           _printStream = printStream;
        }

	/**
	 *  Visit a ADGraph component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGraph x) {

           Node levelUpNode = x.getLevelUpNode();
	   String name;
	   if( levelUpNode != null ) {
               name = levelUpNode.getName();
	   } else {
	       name = "null";
	   }

	   _printStream.println("<?xml version=\"1.0\" standalone=\"no\"?>");
           _printStream.println("<!DOCTYPE adg PUBLIC \"-//LIACS//DTD ESPAM 1//EN\" \"http://www.liacs.nl/~cserc/dtd/espam_1.dtd\">\n");

	   _printStream.println("<adg name=\""        + x.getName() + "\" " +
                                      "levelUpNode=\"" + name        + "\""  +
				      ">");
	   _printStream.println("");

	   //visit the list of parameters of this ADGraph
	   prefixInc();
	   Vector parameterList = (Vector) x.getParameterList();
	   if( parameterList != null ) {
              Iterator i = parameterList.iterator();
	      while( i.hasNext() ) {
	         ADGParameter parameter = (ADGParameter) i.next();
                 parameter.accept(this);
	      }
	   }
	   prefixDec();
           _printStream.println("");

	   //visit the list of ports of this ADGraph
	   prefixInc();
	   Vector portList = (Vector) x.getPortList();
	   if( portList != null ) {
              Iterator i = portList.iterator();
	      while( i.hasNext() ) {
	         NPort port = (NPort) i.next();
                 port.accept(this);
	      }
	   }
	   prefixDec();
           _printStream.println("");

	   //visit the list of nodes of this ADGraph
	   prefixInc();
	   Vector nodeList = (Vector) x.getNodeList();
	   if( nodeList != null ) {
              Iterator i = nodeList.iterator();
	      while( i.hasNext() ) {
	         ADGNode node = (ADGNode) i.next();
                 node.accept(this);
                 _printStream.println("");
	      }
	   }
	   prefixDec();

	   //visit the list of edges of this ADGraph
	   prefixInc();
	   Vector edgeList = (Vector) x.getEdgeList();
	   if( edgeList != null ) {
              Iterator i = edgeList.iterator();
	      while( i.hasNext() ) {
	         ADGEdge edge = (ADGEdge) i.next();
                 edge.accept(this);
                 _printStream.println("");
	      }
	   }
	   prefixDec();

           _printStream.println("</adg>");
	}

	/**
	 *  Visit a ADGNode component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGNode x) {

           ADGraph levelUpNode = (ADGraph) x.getLevelUpNode();
	   String name;
	   if( levelUpNode != null ) {
               name = levelUpNode.getName();
	   } else {
	       name = "null";
	   }

	   _printStream.println(_prefix + "<node name=\"" + x.getName() + "\" " +
	                                  "levelUpNode=\""+ name + "\""  + ">");

	   //visit the list of ports of this ADGNode
	   prefixInc();
	   Vector portList = (Vector) x.getPortList();
	   if( portList != null ) {
              Iterator i = portList.iterator();
	      while( i.hasNext() ) {
	         ADGPort port = (ADGPort) i.next();
		 port.accept(this);
	      }
	   }
	   prefixDec();
           _printStream.println("");

	   //visit the function of this ADGNode
	   prefixInc();
	   ADGFunction function = x.getFunction();
	   if( function != null ) {
              function.accept(this);
	   }
	   prefixDec();
           _printStream.println("");

	   //visit the domain of this ADGNode
	   prefixInc();
	   LBS domain = x.getDomain();
	   if( domain != null ) {
              domain.accept(this);
	   }
	   prefixDec();

           _printStream.println(_prefix + "</node>");
	}

	/**
	 *  Visit a ADGInPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGInPort x) {

           ADGNode node = (ADGNode) x.getNode();
	   String nameNode;
	   if( node != null ) {
               nameNode = node.getName();
	   } else {
	       nameNode = "null";
	   }

           ADGEdge edge = (ADGEdge) x.getEdge();
	   String nameEdge;
	   if( edge != null ) {
               nameEdge = edge.getName();
	   } else {
	       nameEdge = "null";
	   }

	   _printStream.println(_prefix + "<inport name=\"" + x.getName() + "\" " +
	                                          "node=\"" + nameNode    + "\" " +
					          "edge=\"" + nameEdge    + "\" " +
					          ">");
	   //visit the IO variable of this ADGPort
	   prefixInc();
	   ADGVariable ioVariable = x.getIOVariable();
	   if( ioVariable != null ) {
	      _printStream.print(_prefix + "<invariable ");
	      ioVariable.accept(this);
	      _printStream.println(" />");
	   }
	   prefixDec();
           //_printStream.println("");

	   //visit the binding variables of this ADGPort
	   prefixInc();
	   Iterator i = x.getBindVariables().iterator();
	   while ( i.hasNext() ) {
	        ADGVariable bindVariable = (ADGVariable) i.next();
	      _printStream.print(_prefix + "<bindvariable ");
              bindVariable.accept(this);
	      _printStream.println(" />");
	   }
	   prefixDec();
           //_printStream.println("");

	   //visit the domain of this ADGPort
	   prefixInc();
	   LBS domain = x.getDomain();
	   if( domain != null ) {
              domain.accept(this);
	   }
	   prefixDec();
           _printStream.println(_prefix + "</inport>");
	}

	/**
	 *  Visit a ADGOutPort component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGOutPort x) {

           ADGNode node = (ADGNode) x.getNode();
	   String nameNode;
	   if( node != null ) {
               nameNode = node.getName();
	   } else {
	       nameNode = "null";
	   }

           ADGEdge edge = (ADGEdge) x.getEdge();
	   String nameEdge;
	   if( edge != null ) {
               nameEdge = edge.getName();
	   } else {
	       nameEdge = "null";
	   }

	   _printStream.println(_prefix + "<outport name=\"" + x.getName() + "\" " +
	                                           "node=\"" + nameNode    + "\" " +
					           "edge=\"" + nameEdge    + "\" " +
					           ">");
	   //visit the IO variable of this ADGPort
	   prefixInc();
	   ADGVariable ioVariable = x.getIOVariable();
	   if( ioVariable != null ) {
	      _printStream.print(_prefix + "<outvariable ");
              ioVariable.accept(this);
	      _printStream.println(" />");
	   }
	   prefixDec();

	   //visit the binding variables of this ADGPort
	   prefixInc();
	   Iterator i = x.getBindVariables().iterator();
	   while ( i.hasNext() ) {
	        ADGVariable bindVariable = (ADGVariable) i.next();
	      _printStream.print(_prefix + "<bindvariable ");
              bindVariable.accept(this);
	      _printStream.println(" />");
	   }
	   prefixDec();

	   //visit the domain of this ADGPort
	   prefixInc();
	   LBS domain = x.getDomain();
	   if( domain != null ) {
              domain.accept(this);
	   }
	   prefixDec();
           _printStream.println(_prefix + "</outport>");
	}

	/**
	 *  Visit an ADGEdge component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGEdge x) {

           ADGOutPort fromPort = x.getFromPort();
	   String nameFromPort;
	   String nameFromNode;
	   if( fromPort != null ) {
               nameFromPort = fromPort.getName();
	       nameFromNode = fromPort.getNode().getName();

	   } else {
	       nameFromPort = "null";
               nameFromNode = "null";
	   }

           ADGInPort toPort = x.getToPort();
	   String nameToPort;
	   String nameToNode;
	   if( toPort != null ) {
               nameToPort = toPort.getName();
	       nameToNode = toPort.getNode().getName();
	   } else {
	       nameToPort = "null";
	       nameToNode = "null";
	   }

	   _printStream.println(_prefix + "<edge name=\""     + x.getName()  + "\" " +
	   				        "fromPort=\"" + nameFromPort + "\" " +
	   				        "fromNode=\"" + nameFromNode + "\" " +
				                "toPort=\""   + nameToPort   + "\" " +
				                "toNode=\""   + nameToNode   + "\" " +
				                "size=\""   + x.getSize()   + "\" " +
				                ">");
	   //visit the linearization model of this ADGEdge
	   prefixInc();
	   LinearizationType linModel = x.getLinModel();
           _printStream.println(_prefix + "<linearization type=\"" + linModel + "\" " + "/>");
	   prefixDec();

	   //visit the mapping of this ADGEdge
	   prefixInc();
	   JMatrix mapping = x.getMapping();
	   if( mapping != null ) {
	       _printStream.println(_prefix + "<mapping matrix=\"" +
	                             mapping.toXMLString("                  " + _prefix) +
				     "\" " + "/>");
	   }
	   prefixDec();
           _printStream.println(_prefix + "</edge>");
	}

	/**
	 *  Visit a ADGParameter component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGParameter x) {
	   _printStream.println(_prefix + "<parameter name=\""  + x.getName()       + "\" " +
                                                     "lb=\""    + x.getLowerBound() + "\" " +
                                                     "ub=\""    + x.getUpperBound() + "\" " +
                                                     "value=\"" + x.getValue()      + "\" " +
                                                     "/>");
	}

	/**
	 *  Visit a ADGVariable component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGVariable x) {

           Vector indexList = x.getIndexList();
	   String index = "";
	   if( indexList != null ) {
              Iterator i = indexList.iterator();
	      if( i.hasNext() ) {
	         index = "(";
                 while( i.hasNext() ) {
	            Expression exp = (Expression) i.next();
		    index = index + exp.toString();
		    if( i.hasNext() ) {
                       index = index + ",";
		    }
	         }
	         index = index + ")";
	      }
	   }

	   _printStream.print("name=\""     + x.getName() + index + "\" " +
                                "dataType=\"" + x.getDataType()     + "\"");
	}

	/**
	 *  Visit an ADGFunction component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ADGFunction x) {

	   _printStream.println(_prefix + "<function name=\"" + x.getName() + "\" >");

	   //visit the list of input arguments of this function
	   prefixInc();
	   Vector inArgumentList = (Vector) x.getInArgumentList();
	   if( inArgumentList != null ) {
              Iterator i = inArgumentList.iterator();
	      while( i.hasNext() ) {
	         ADGVariable var = (ADGVariable) i.next();
	         _printStream.print(_prefix + "<inargument ");
                 var.accept(this);
                 _printStream.println(" />");
	      }
	   }
	   prefixDec();

	   //visit the list of output arguments of this function
	   prefixInc();
	   Vector outArgumentList = (Vector) x.getOutArgumentList();
	   if( outArgumentList != null ) {
              Iterator i = outArgumentList.iterator();
	      while( i.hasNext() ) {
	         ADGVariable var = (ADGVariable) i.next();
	         _printStream.print(_prefix + "<outargument ");
                 var.accept(this);
                 _printStream.println(" />");
	      }
	   }
	   prefixDec();
           _printStream.println(_prefix + "</function>");
	}

	/**
	 *  Visit a LBS component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(LBS x) {

	   _printStream.println(_prefix + "<domain type=\"LBS\" >");

	   //visit the linear bound of this LBS
	   prefixInc();
	   
	   Iterator i = x.getLinearBound().iterator();
	   while( i.hasNext() ) {
	   	Polytope polytope = (Polytope) i.next();
                polytope.accept(this);
	   }
	   prefixDec();

	   //visit the filter of this LBS
	   prefixInc();
	   FilterSet filter = (FilterSet) x.getFilterSet();
	   if( filter != null ) {
                 filter.accept(this);
	   }
	   prefixDec();
           _printStream.println(_prefix + "</domain>");
	}

	/**
	 *  Visit a Polytope component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Polytope x) {

	   _printStream.print(_prefix + "<linearbound ");
           IndexVector iv = x.getIndexVector();
	   iv.accept(this);
          _printStream.println("\" >");

          prefixInc();
          _printStream.println(_prefix
                + "<constraint matrix=\""
                + x.getConstraints().toXMLString("                     " + _prefix)
                + "\" />");

          _printStream.println(_prefix
	        + "<context matrix=\""
                + x.getContext().toXMLString("                  " + _prefix)
		+ "\" />");

           Iterator i = x.getIndexVector().getStaticCtrlVector().iterator();
           while( i.hasNext() ) {
              ControlExpression index = (ControlExpression) i.next();
	      index.accept(this);
           }
           prefixDec();
           _printStream.println(_prefix + "</linearbound>");
	}


	/**
	 *  Visit a FilterSet component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(FilterSet x) {

	   _printStream.print(_prefix + "<filterset ");
           IndexVector iv = x.getIndexVector();
	   iv.accept(this);
          _printStream.println("\" >");

          prefixInc();
          _printStream.println(_prefix
                + "<constraint matrix=\""
                + x.getConstraints().toXMLString("                  " + _prefix)
                + "\" />");

           //Iterator i = x.getIndexVector().getStaticCtrlVector().iterator();
           //while (i.hasNext()) {
           //   ControlExpression index = (ControlExpression) i.next();
	   //   index.accept(this);
           //}
           prefixDec();
           _printStream.println(_prefix + "</filterset>");

	}

	/**
	 *  Visit a IndexVector component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(IndexVector x) {

           Iterator i;
           _printStream.print("index=\"");
           i = x.getIterationVector().iterator();
           while( i.hasNext() ) {
               _printStream.print( (String) i.next());
               if( i.hasNext() ) {
                  _printStream.print(", ");
               }
           }
           _printStream.print("\" ");
           _printStream.print("staticControl=\"");
           i = x.getStaticCtrlVector().iterator();
           while( i.hasNext() ) {
               _printStream.print( ((ControlExpression) i.next()).getName() );
               if( i.hasNext() ) {
                  _printStream.print(", ");
               }
           }
           _printStream.print("\" ");
           _printStream.print("dynamicControl=\"");
           i = x.getDynamicCtrlVector().iterator();
           while( i.hasNext() ) {
               _printStream.print( (String) i.next() );
               if( i.hasNext() ) {
                  _printStream.print(", ");
               }
           }
           _printStream.print("\" ");
           _printStream.print("parameter=\"");
           i = x.getParameterVector().iterator();
           while( i.hasNext() ) {
              _printStream.print( ((ADGParameter) i.next()).getName() );
              if( i.hasNext() ) {
                  _printStream.print(", ");
              }
           }
	}

	/**
	 *  Visit a Control Expression component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(ControlExpression x) {

             _printStream.print(_prefix + "<control name=\"" + x.getName() + "\" ");
             _printStream.println("exp=\"" + x.getExpression().toString() + "\"/>");
	}
}
