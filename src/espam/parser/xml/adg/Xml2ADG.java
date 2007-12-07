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

package espam.parser.xml.adg;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.lang.Integer;

import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGParameter;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGPort;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGraph;

import espam.datamodel.domain.LBS;
import espam.datamodel.domain.ControlExpression;
import espam.datamodel.domain.Polytope;
import espam.datamodel.domain.FilterSet;
import espam.datamodel.EspamException;
import espam.datamodel.LinearizationType;

import espam.main.UserInterface;

import org.xml.sax.Attributes;

import espam.parser.matrix.JMatrixParser;
import espam.parser.expression.ExpressionParser;

import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.ModTerm;
import espam.utils.symbolic.expression.DivTerm;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;


//////////////////////////////////////////////////////////////////////////
//// Xml2ADG

/**
 *  This class
 *
 * @author  Todor Stefanov
 * @version  $Id: Xml2ADG.java,v 1.1 2007/12/07 22:07:10 stefanov Exp $
 */

public class Xml2ADG {

	///////////////////////////////////////////////////////////////////
	////                         public methods                   ////

	/**
	 *  Return the singleton instance of this class;
	 *
	 * @return  the instance.
	 */
	public final static Xml2ADG getInstance() {
		return _instance;
	}

	/**
	 *  Process the start of a model tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an ADG object.
	 */
	public Object processADG(Attributes attributes) {
		//System.out.println(" -- ADG -- ");
		String name = (String) attributes.getValue("name");

		ADGraph adg = new ADGraph( name );

		return adg;
	}

	/**
	 * Process the end of a model tag in the XML.
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processADG(Stack stack) {
	}

	/**
	*  Process the start of a parameter tag in the XML.
	*
	* @param  attributes The attributes of the tag.
	* @return  a parameter object.
	*/
	public Object processParameter(Attributes attributes) {
		//System.out.println(" -- Parameters -- ");
		String name  = (String) attributes.getValue("name");
		String lb    = (String) attributes.getValue("lb");
		String ub    = (String) attributes.getValue("ub");
		String value = (String) attributes.getValue("value");

		ADGParameter parameter = new ADGParameter( name );

		parameter.setLowerBound( Integer.valueOf( lb ).intValue() );
		parameter.setUpperBound( Integer.valueOf( ub ).intValue() );
		parameter.setValue( Integer.valueOf( value ).intValue() );

		return parameter;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processParameter(Stack stack) {
		ADGParameter parameter = (ADGParameter) stack.pop();
		ADGraph adg = (ADGraph) stack.peek(); // The adg is at the top of the stack

		Vector parList = adg.getParameterList();
		parList.add( parameter );

		// fill-in the parameter vector
		_parameterVector.add( parameter );
	}

	/**
	 *  Process the start of a node tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an entity object.
	 */
	public Object processNode(Attributes attributes) {
		//System.out.println(" -- Node -- ");
		String name = (String) attributes.getValue("name");

		ADGNode adgNode = new ADGNode( name );

		return adgNode;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processNode(Stack stack) {
		ADGNode adgNode = (ADGNode) stack.pop();
		ADGraph adg = (ADGraph) stack.peek(); // The adg is at the top of the stack

		adgNode.setLevelUpNode( adg );

		Vector nodeList = adg.getNodeList();
		nodeList.add( adgNode );
	}

	/**
	 *  Process the start of a inport tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a port object.
	 */
	public Object processInPort(Attributes attributes) {
		//System.out.println(" -- InPort -- ");
		String name = (String) attributes.getValue("name");

		ADGInPort port = new ADGInPort( name );

		return port;
	}

	/**
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processInPort(Stack stack) {
		ADGInPort port = (ADGInPort) stack.pop();
		ADGNode node = (ADGNode) stack.peek();

		port.setNode( node );
		node.getPortList().add( port );
	}

	/**
	 *  Process the start of a outport tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a port object.
	 */
	public Object processOutPort(Attributes attributes) {
		//System.out.println(" -- OutPort -- ");
		String name = (String) attributes.getValue("name");

		ADGOutPort port = new ADGOutPort( name );

		return port;
	}

	/**
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processOutPort(Stack stack) {
		ADGOutPort port = (ADGOutPort) stack.pop();
		ADGNode node = (ADGNode) stack.peek();

		port.setNode( node );
		node.getPortList().add( port );
	}


	/**
	 *  Process the start of a function tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a function object.
	 */
	public Object processFunction(Attributes attributes) {
		//System.out.println(" -- Function -- ");
		String name = (String) attributes.getValue("name");

		ADGFunction function = new ADGFunction( name );

		return function;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processFunction(Stack stack) {
		ADGFunction function = (ADGFunction) stack.pop();
		ADGNode node = (ADGNode) stack.peek();

		node.setFunction( function );
	}


	/**
	 *  Process the start of an edge tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a edge object.
	 */
	public Object processEdge(Attributes attributes) {
		// System.out.println(" -- Edge -- ");
		String name  = (String) attributes.getValue("name");
		String fromPort = (String) attributes.getValue("fromPort");
		String toPort = (String) attributes.getValue("toPort");
		String size = (String) attributes.getValue("size");

		ADGEdge edge = new ADGEdge( name );
		edge.setSize( Integer.valueOf( size ).intValue()  );

		ADGInPort i_port = new ADGInPort( toPort );
		ADGOutPort o_port = new ADGOutPort( fromPort );

		edge.getPortList().add( i_port );
		edge.getPortList().add( o_port );

		return edge;
	}

	/**
	  * @param  stack Description of the Parameter
	  */
	public void processEdge(Stack stack) {
		ADGEdge edge = (ADGEdge) stack.pop();
		ADGraph adg = (ADGraph) stack.peek();

		Vector ports = new Vector();

		// Add this edge to the correspondent ports and add to-from ports to the edge
		Iterator i = adg.getNodeList().iterator();

		while( i.hasNext() ) {
			ADGNode node = (ADGNode) i.next();
			ADGInPort i_port = (ADGInPort) node.getPort( edge.getToPort().getName() );
			ADGOutPort o_port = (ADGOutPort) node.getPort( edge.getFromPort().getName() );

			if( i_port != null ) {
				i_port.setEdge( edge );
				ports.add( i_port );
			}

			if( o_port != null ) {
				o_port.setEdge( edge );
				ports.add( o_port );
			}
		}

		edge.setPortList( ports );
                adg.getEdgeList().add( edge );

	}


	/**
	 *  Process the start of a domain tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a domain object.
	 */
	public Object processDomain(Attributes attributes) {
           	//System.out.println(" -- Domain -- ");
		String type     = (String) attributes.getValue("type");
                LBS domain = null;

                if ( type.equals("LBS") ) {
                    domain = new LBS();
		} else {
                    throw new Error("Unknown Domain Type: " + type);
		}

		return domain;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processDomain(Stack stack) {
		LBS lbs = (LBS) stack.pop();
		Object obj = (Object) stack.peek();

		if( obj instanceof ADGNode ) {

		     ((ADGNode) obj).setDomain( lbs );

		} else if( obj instanceof ADGInPort ) {

		     ((ADGInPort) obj).setDomain( lbs );

		} else if( obj instanceof ADGOutPort ) {

		    ((ADGOutPort) obj).setDomain( lbs );

		}
	}

	/**
	 *  Process the start of a linearbound tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a linearbound object.
	 */
	public Object processLinearBound(Attributes attributes) {
           	//System.out.println(" -- LinearBound -- ");
		String index   = (String) attributes.getValue("index");
		String staticControl   = (String) attributes.getValue("staticControl");
		String dynamicControl   = (String) attributes.getValue("dynamicControl");
		String parameter   = (String) attributes.getValue("parameter");

		Polytope polytope = new Polytope();
		polytope.getIndexVector().setIterationVector(_string2Vector( index ) );
		polytope.getIndexVector().setDynamicCtrlVector(_string2Vector( dynamicControl ) );

		// set the staic control vector
                Vector staticCtrl = _string2Vector( staticControl );
		Iterator k = staticCtrl.iterator();
		while ( k.hasNext() ) {
		     String p = (String) k.next();
		     ControlExpression ce = new ControlExpression( p );
                     polytope.getIndexVector().getStaticCtrlVector().add( ce );
		}

		// set the parameter vector
                Vector parameters = _string2Vector( parameter );
		Iterator i = parameters.iterator();
		while ( i.hasNext() ) {
		     String p = (String) i.next();
		     Iterator j = _parameterVector.iterator();
		     while ( j.hasNext() ) {
		           ADGParameter param = (ADGParameter) j.next();
			   if ( param.getName().equals(p) ) {
                                polytope.getIndexVector().getParameterVector().add( param );
			   }
                     }
		}

		return polytope;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processLinearBound(Stack stack) {
		Polytope polytope = (Polytope) stack.pop();
		LBS domain = (LBS) stack.peek();

		domain.getLinearBound().add( polytope );

	}


	/**
	 *  Process the start of a filterset tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a filterset object.
	 */
	public Object processFilterSet(Attributes attributes) {
           	//System.out.println(" -- FilterSet -- ");
		String index   = (String) attributes.getValue("index");
		String staticControl   = (String) attributes.getValue("staticControl");
		String dynamicControl   = (String) attributes.getValue("dynamicControl");
		String parameter   = (String) attributes.getValue("parameter");

		FilterSet filterset = new FilterSet();
		filterset.getIndexVector().setIterationVector(_string2Vector( index ) );
		filterset.getIndexVector().setDynamicCtrlVector(_string2Vector( dynamicControl ) );

		// set the staic control vector
                Vector staticCtrl = _string2Vector( staticControl );
		Iterator k = staticCtrl.iterator();
		while ( k.hasNext() ) {
		     String p = (String) k.next();
		     ControlExpression ce = new ControlExpression( p );
                     filterset.getIndexVector().getStaticCtrlVector().add( ce );
		}

		// set the parameter vector
                Vector parameters = _string2Vector( parameter );
		Iterator i = parameters.iterator();
		while ( i.hasNext() ) {
		     String p = (String) i.next();
		     Iterator j = _parameterVector.iterator();
		     while ( j.hasNext() ) {
		           ADGParameter param = (ADGParameter) j.next();
			   if ( param.getName().equals(p) ) {
                                filterset.getIndexVector().getParameterVector().add( param );
			   }
                     }
		}

		return filterset;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processFilterSet(Stack stack) {
		FilterSet filterset = (FilterSet) stack.pop();
		LBS domain = (LBS) stack.peek();

		domain.setFilterSet( filterset );

	}

	/**
	 *  Process the start of an invariable tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an invariable object.
	 */
	public Object processInVariable(Attributes attributes) {
		//System.out.println(" -- InVariable -- ");
		String name = (String) attributes.getValue("name");
		String dataType = (String) attributes.getValue("dataType");

		int index1 = name.indexOf('(');
		int index2 = name.lastIndexOf(')');

		ADGVariable variable = new ADGVariable("");
		Vector indexList = variable.getIndexList();

		// Extract the name and the indexes of a variable (if there are indexes)
		if( index1 == -1 ) {
			variable.setName( name ); // the variable has name without indexes
		} else {
                        variable.setName( name.substring(0, index1) ); // extract only the name
			StringTokenizer st = new StringTokenizer( name.substring(index1+1,index2), "," );

			Expression exp = null;	// The indexes are expressions
			while( st.hasMoreTokens() ) {

				String token = st.nextToken();
				try {
					exp = _expParser.getExpression( token ) ;
					indexList.add( exp );

				} catch( Exception e ) {
					throw new Error("Unkown expression: " + token );
				}
			}
		}

		// Set the data type of the variable
		variable.setDataType( dataType );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processInVariable(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGInPort port = (ADGInPort) stack.peek();

                port.setIOVariable( variable );
	}


	/**
	 *  Process the start of an outvariable tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an outvariable object.
	 */
	public Object processOutVariable(Attributes attributes) {
		//System.out.println(" -- OutVariable -- ");
		String name = (String) attributes.getValue("name");
		String dataType = (String) attributes.getValue("dataType");

		int index1 = name.indexOf('(');
		int index2 = name.lastIndexOf(')');

		ADGVariable variable = new ADGVariable("");
		Vector indexList = variable.getIndexList();

		// Extract the name and the indexes of a variable (if there are indexes)
		if( index1 == -1 ) {
			variable.setName( name ); // the variable has name without indexes
		} else {
                        variable.setName( name.substring(0, index1) ); // extract only the name
			StringTokenizer st = new StringTokenizer( name.substring(index1+1,index2), "," );

			Expression exp = null;	// The indexes are expressions
			while( st.hasMoreTokens() ) {

				String token = st.nextToken();
				try {
					exp = _expParser.getExpression( token ) ;
					indexList.add( exp );

				} catch( Exception e ) {
					throw new Error("Unkown expression: " + token);
				}
			}
		}

		// Set the data type of the variable
		variable.setDataType( dataType );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processOutVariable(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGOutPort port = (ADGOutPort) stack.peek();

                port.setIOVariable( variable );
	}


	/**
	 *  Process the start of a bindvariable tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a bindvariable object.
	 */
	public Object processBindVariable(Attributes attributes) {
		//System.out.println(" -- BindVariable -- ");
		String name = (String) attributes.getValue("name");
		String dataType = (String) attributes.getValue("dataType");

		ADGVariable variable = new ADGVariable( name );
		// Set the data type of the variable
		variable.setDataType( dataType );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processBindVariable(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGPort port = (ADGPort) stack.peek();

                port.getBindVariables().add( variable );
	}

	/**
	 *  Process the start of an inargument tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an inargument object.
	 */
	public Object processInArgument(Attributes attributes) {
		//System.out.println(" -- InArgument -- ");
		String name = (String) attributes.getValue("name");
		String dataType = (String) attributes.getValue("dataType");

		ADGVariable variable = new ADGVariable( name );
		// Set the data type of the variable
		variable.setDataType( dataType );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processInArgument(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGFunction function = (ADGFunction) stack.peek();

                function.getInArgumentList().add( variable );
	}

	/**
	 *  Process the start of an outargument tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an outargument object.
	 */
	public Object processOutArgument(Attributes attributes) {
		//System.out.println(" -- OutArgument -- ");
		String name = (String) attributes.getValue("name");
		String dataType = (String) attributes.getValue("dataType");

		ADGVariable variable = new ADGVariable( name );
		// Set the data type of the variable
		variable.setDataType( dataType );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processOutArgument(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGFunction function = (ADGFunction) stack.peek();

                function.getOutArgumentList().add( variable );
	}


	/**
	 *  Process the start of a constraint tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a constraint matrix object.
	 */
	public Object processConstraint(Attributes attributes) {
		//System.out.println(" -- Constraint -- ");
		String matrix = (String) attributes.getValue("matrix");
		SignedMatrix M = null;

		try {
			M = (SignedMatrix) JMatrixParser.getSignedMatrix( matrix );
		} catch( Exception e ) {
			System.out.println(
				"Cannot convert the Matrix "
				+ matrix
				+ " to an instance of JMatrix"
				+ e.getMessage());
		}
		return M;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processConstraint(Stack stack) {
		SignedMatrix matrix = (SignedMatrix) stack.pop();
		Object obj = stack.peek();

		if( obj instanceof Polytope ) {
			((Polytope) obj).setConstraints( matrix );
		} else if ( obj instanceof FilterSet ) {
			((FilterSet) obj).setConstraints( matrix );
		}
	}

	/**
	 *  Process the start of a context tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a context matrix object.
	 */
	public Object processContext(Attributes attributes) {
		//System.out.println(" -- Context -- ");
		String matrix = (String) attributes.getValue("matrix");
		SignedMatrix M = null;

		try {
			M = (SignedMatrix) JMatrixParser.getSignedMatrix( matrix );
		} catch( Exception e ) {
			System.out.println(
				"Cannot convert the Matrix "
				+ matrix
				+ " to an instance of JMatrix"
				+ e.getMessage());
		}
		return M;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processContext(Stack stack) {
		SignedMatrix matrix = (SignedMatrix) stack.pop();
		Polytope polytope = (Polytope) stack.peek();

		polytope.setContext( matrix );
	}


	/**
	 *  Process the start of a control tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a control object.
	 */
	public Object processControl(Attributes attributes) {
		//System.out.println(" -- Control -- ");
		String name = (String) attributes.getValue("name");
		String exp = (String) attributes.getValue("exp");

		ControlExpression ce = new ControlExpression( name );

		try {
			ce.setExpression( _expParser.getExpression( exp ) );
		} catch( Exception e ) {
			throw new Error("Unkown expression: " + exp);
		}

		return ce;
	}


	/**
	* @param stack Description of the Parameter
	*/
	public void processControl(Stack stack) {
		ControlExpression ce = (ControlExpression) stack.pop();
		Polytope  polytope = (Polytope) stack.peek();

		Iterator i = polytope.getIndexVector().getStaticCtrlVector().iterator();
		while (i.hasNext()) {
                     ControlExpression exp = (ControlExpression) i.next();
		     if ( exp.getName().equals( ce.getName() ) ) {
		           exp.setExpression( ce.getExpression() );
		     }

		}

	}


	/**
	 *  Process the start of a mapping tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a mapping matrix object.
	 */
	public Object processMapping(Attributes attributes) {
		//System.out.println(" -- Mapping -- ");
		String matrix = (String) attributes.getValue("matrix");
		JMatrix M = null;
		try {
			M = JMatrixParser.getJMatrix(matrix);
		} catch( Exception e ) {
			System.out.println(
				"Cannot convert the Matrix "
				+ matrix
				+ " to an instance of JMatrix"
				+ e.getMessage());
		}
		return M;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processMapping(Stack stack) {
		JMatrix matrix = (JMatrix) stack.pop();
		ADGEdge edge = (ADGEdge) stack.peek();

		edge.setMapping( matrix );

	}

	/**
	 *  Process the start of a linearization tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a linearization object.
	 */
	public Object processLinearization(Attributes attributes) {
		//System.out.println(" -- Linearization -- ");
		String linModel     = (String) attributes.getValue("type");

		if ( linModel.equals("fifo") || linModel.equals("BroadcastInOrder") ||
		     linModel.equals("GenericOutOfOrder") || linModel.equals("BroadcastOutOfOrder") ||
		     linModel.equals("sticky_fifo") ||
		     linModel.equals("shift_register") ) {

		     LinearizationType lt = LinearizationType.find( linModel );
		     return lt;

		} else {
 		    throw new Error("Unknown Linearization Type: " + linModel);
		}

	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processLinearization(Stack stack) {
		 LinearizationType lt = (LinearizationType) stack.pop();
		ADGEdge edge = (ADGEdge) stack.peek();

                edge.setLinModel( lt );

	}


	///////////////////////////////////////////////////////////////////
	////                         private methods                    ///

	/**
	 *  Convert a string representing a vector into a Java vector.
	 *
	 * @param  vectorString Description of the Parameter
	 * @return  the java vector.
	 */
	private Vector _string2Vector(String vectorString) {
		StringTokenizer st = new StringTokenizer(vectorString, ", ");
		int count = st.countTokens();
		//Vector vector = new Vector(count);
		Vector vector = new Vector();

		for( int i = 0; i < count; i++ ) {
			String token = st.nextToken() ;
			vector.add( token );
		}
		return vector;
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                  ///

	/**
	 *  Create a unique instance
	 * */
	private final static Xml2ADG _instance = new Xml2ADG();

	private ExpressionParser _expParser = new ExpressionParser();

	private Vector _parameterVector = new Vector();
}
