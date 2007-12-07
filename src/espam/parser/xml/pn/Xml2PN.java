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

package espam.parser.xml.pn;

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
import espam.utils.util.Convert;


//////////////////////////////////////////////////////////////////////////
//// Xml2PN

/**
 *  This class
 *
 * @author  Hristo Nikolov
 * @version  $Id: Xml2PN.java,v 1.1 2007/12/07 22:07:09 stefanov Exp $
 */

public class Xml2PN {

	///////////////////////////////////////////////////////////////////
	////                         public methods                   ////

	/**
	 *  Return the singleton instance of this class;
	 *
	 * @return  the instance.
	 */
	public final static Xml2PN getInstance() {
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
		adg.setParameterList( parList );
		// fill-in the parameter vector
		_parameterVector.add( parameter );
	}

	/**
	 *  Process the start of a entity tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an entity object.
	 */
	public Object processEntity(Attributes attributes) {
		//System.out.println(" -- Entity -- ");
		String name = (String) attributes.getValue("name");
		//String type = (String) attributes.getValue("type");

		ADGNode adgNode = new ADGNode( name );

		return adgNode;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processEntity(Stack stack) {
		ADGNode adgNode = (ADGNode) stack.pop();
		ADGraph adg = (ADGraph) stack.peek(); // The adg is at the top of the stack

		adgNode.setLevelUpNode( adg );

		Vector nodeList = adg.getNodeList();
		nodeList.add( adgNode );
		adg.setNodeList( nodeList );
	}

	/**
	 *  Process the start of a port tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a port object.
	 */
	public Object processPort(Attributes attributes) {
		//System.out.println(" -- Port -- ");
		//String name = (String) attributes.getValue("name");
		//String type = (String) attributes.getValue("type");

		ADGPort port = new ADGPort("dummy"); // will be discarded
		return port;
	}

	/**
	 * Ports are processed in processIPD()
	 *
	 * @param  stack Description of the Parameter
	 */
	public void processPort(Stack stack) {
		ADGPort port = (ADGPort) stack.pop(); // A port is just removed from the stack
	}

	/**
	 *  Process the start of an ipdstatement tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a ipdstatement object.
	 */
	public Object processIPD(Attributes attributes) {
		//System.out.println(" -- IPD Statement -- ");
		String arg = (String) attributes.getValue("arg");
		String port = (String) attributes.getValue("port");

		int index1 = arg.indexOf('(');
		int index2 = arg.lastIndexOf(')') - 1;

		ADGVariable variable = new ADGVariable( arg );
		Vector indexList = variable.getIndexList();

		// Extract the name and the indexes of a variable (if there is indexes)
		if( index1 == -1 ) {
			variable.setName( arg ); // the variable has name without indexes
		} else {
			variable.setName( arg.substring(0, index1-1) ); // extract only the name
			indexList = _string2Vector( arg.substring(index1+1,index2) ); // extract indexes
			variable.setIndexList( indexList );
		}
		//---------------------------------------------------------------------

		ADGInPort readPort = new ADGInPort( port );
		//readPort.setBindVariable( variable );
		readPort.getBindVariables().add( variable );

		return readPort;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processIPD(Stack stack) {
		ADGInPort readPort = (ADGInPort) stack.pop();
		ADGNode adgNode = (ADGNode) stack.peek(); // An adg node is at the top of the stack

		Vector portList = adgNode.getPortList();
		portList.add( readPort );
		adgNode.setPortList( portList );
	}

	/**
	 *  Process the start of an opdstatement tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a opdstatement object.
	 */
	public Object processOPD(Attributes attributes) {
		String arg = (String) attributes.getValue("arg");
		String port = (String) attributes.getValue("port");

		int index1 = arg.indexOf('(');
		int index2 = arg.lastIndexOf(')') - 1;

		ADGVariable variable = new ADGVariable( arg );
		Vector indexList = variable.getIndexList();

		// Extract the name and the indexes of a variable (if there is indexes)
		if( index1 == -1 ) {
			variable.setName( arg ); // the variable has name without indexes
		} else {
			variable.setName( arg.substring(0, index1-1) ); // extract only the name
			indexList = _string2Vector( arg.substring(index1+1,index2) ); // extract indexes
			variable.setIndexList( indexList );
		}
		//---------------------------------------------------------------------

		ADGOutPort writePort = new ADGOutPort( port );
		//writePort.setBindVariable( variable );
		writePort.getBindVariables().add( variable );

		return writePort;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processOPD(Stack stack) {
		// Domain domain = (Domain) stack.pop();
		ADGOutPort writePort = (ADGOutPort) stack.pop();
		ADGNode adgNode = (ADGNode) stack.peek(); // An adg node is at the top of the stack

		Vector portList = adgNode.getPortList();
		portList.add( writePort );
		adgNode.setPortList( portList );
	}

	/**
	 *  Process the start of an assignstatement tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  an assignment object.
	 */
	public Object processAssignstatement(Attributes attributes) {
		//System.out.println(" -- Assign Statement -- ");
		String name = (String) attributes.getValue("name"); // The function name
		ADGFunction function = new ADGFunction( name );

		return function;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processAssignstatement(Stack stack) {
		ADGFunction function = (ADGFunction) stack.pop();
		ADGNode adgNode = (ADGNode) stack.peek(); // An adg node is at the top of the stack

		adgNode.setFunction( function );
	}

	/**
	 *  Process the start of a variable tag in the XML.
	 *  variable is an argument of a function
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a variable object.
	 */
	public Object processVariable(Attributes attributes) {
		//System.out.println(" -- Argument -- ");
		String name = (String) attributes.getValue("name");
		String type = (String) attributes.getValue("type");

		int index1 = name.indexOf('(');
		int index2 = name.lastIndexOf(')') - 1;

		ADGVariable variable = new ADGVariable("");
		Vector indexList = variable.getIndexList();

		// Extract the name and the indexes of a variable (if there are indexes)
		if( index1 == -1 ) {
			variable.setName( name ); // the variable has name without indexes
		} else {
                        variable.setName( name.substring(0, index1-1) ); // extract only the name
			StringTokenizer st = new StringTokenizer( name.substring(index1+1,index2), "," );

			Expression exp = null;	// The indexes are expressions
			while( st.hasMoreTokens() ) {

				String token = st.nextToken();
				try {
					exp = _expParser.getExpression( token ) ;
					indexList.add( exp );

				} catch( Exception e ) {
					throw new Error("Unkown expression: " + exp);
				}
			}

			variable.setIndexList( indexList );
		}
		//---------------------------------------------------------------------

		variable.setDataType( type );

		return variable;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processVariable(Stack stack) {
		ADGVariable variable = (ADGVariable) stack.pop();
		ADGFunction function = (ADGFunction) stack.peek(); // An adg function is at the top of the stack

		if( variable.getName().startsWith("in_") ) {
			Vector inArguments = function.getInArgumentList();
			inArguments.add( variable );
			function.setInArgumentList( inArguments );

		} else {
			Vector outArguments = function.getOutArgumentList();
			outArguments.add( variable );
			function.setOutArgumentList( outArguments );
		}

	}

	/**
	 *  Process the start of a domain tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a domain object.
	 */
	public Object processDomain(Attributes attributes) {
           	//System.out.println(" -- Domain -- ");
		String index     = (String) attributes.getValue("index");
		String control   = (String) attributes.getValue("control");
		String parameter = (String) attributes.getValue("parameter");

		//lbs.getLinearBound().getIndexVector().setIndexVector( _string2Vector( index ));
		//lbs.getLinearBound().getIndexVector().setParameterVector( _parameterVector ); //generated in 'processParameter()'

	//!!!!!!!!   StaticCtrlVector has to be of type ControlExpression    !!!!!!!!!!!!
	// made in processIndex() and processfilter()

		return index;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processDomain(Stack stack) {
		LBS lbs = (LBS) stack.pop();
		Object obj = (Object) stack.peek();

		if( obj instanceof ADGFunction ) {
		   ADGFunction function = (ADGFunction) stack.pop();
		   ADGNode node = (ADGNode) stack.peek(); // we need one level up - the node of the function
		   node.setDomain( lbs );
		   stack.push( function );

		} else if( obj instanceof ADGInPort ) {
		   ADGInPort inPort = (ADGInPort) obj;
		   inPort.setDomain( lbs );

		} else if( obj instanceof ADGOutPort ) {
		   ADGOutPort outPort = (ADGOutPort) obj;
		   outPort.setDomain( lbs );
		}
	}

	/**
	 *  Process the start of a constraint tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a constraint matrix object.
	 */
	public Object processConstraint(Attributes attributes) {
		//System.out.println(" -- constraint -- ");
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

		//lbs.getLinearBound().setConstraints( matrix );

		if( obj instanceof String ) {
			stack.pop();
			Polytope polytope = new Polytope();
			polytope.setConstraints( matrix );
			polytope.getIndexVector().setIterationVector( _string2Vector( (String)obj ) );
			polytope.getIndexVector().setParameterVector( _parameterVector ); //generated in 'processParameter()'
			LBS lbs = new LBS();
			lbs.getLinearBound().add( polytope );
			stack.push(lbs);
		} else {
			LBS lbs = (LBS) obj;
			Polytope polytope = (Polytope)((Polytope) lbs.getLinearBound().get(0)).clone();
			polytope.setConstraints( matrix );
			lbs.getLinearBound().add( polytope );
		}
	}

	/**
	 *  Process the start of a Context tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a context matrix object.
	 */
	public Object processContext(Attributes attributes) {
		//System.out.println(" -- context -- ");
		String matrix = (String) attributes.getValue("matrix");
		SignedMatrix M = null;

		try {
			M = (SignedMatrix) JMatrixParser.getSignedMatrix(matrix);
		} catch( Exception e ) {
			System.out.println(
				"Cannot convert the Matrix "
				+ matrix
				+ " to an instance of "
				+ "a Signed JMatrix: "
				+ e.getMessage());
		}
		return M;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processContext(Stack stack) {
		SignedMatrix matrix = (SignedMatrix) stack.pop();
		LBS lbs = (LBS) stack.peek();

		//lbs.getLinearBound().setContext(matrix);

		Iterator i = lbs.getLinearBound().iterator();
		while( i.hasNext() ) {
			((Polytope) i.next()).setContext(matrix);
		}
	}

	/**
	 *  Process the start of a mapping tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a mapping matrix object.
	 */
	public Object processMapping(Attributes attributes) {
		//System.out.println(" -- context -- ");
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
		LBS lbs = (LBS) stack.pop();
		Object obj = (Object) stack.peek(); // inPort, outPort, or function

		if( obj instanceof ADGInPort ) {
			ADGInPort inPort = (ADGInPort) obj;
			_mappingHashT.put( inPort.getName(), matrix);
		}

		stack.push( lbs );
	}


	/**
	 *  Process the start of a linearization tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a linearization object.
	 */
	public Object processLinearization(Attributes attributes) {
		//System.out.println(" -- Linearization -- ");
		//Object obj = new Object();
		//return obj;
		String linModel     = (String) attributes.getValue("type");
		return linModel;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processLinearization(Stack stack) {
		String linModel = (String) stack.pop();  // linearization Model
		ADGInPort readPort = (ADGInPort) stack.peek(); // An adg read Port is at the top of the stack
		_linModelHashT.put( readPort.getName(), linModel );
	}

	/**
	 *  Process the start of a link tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a link object.
	 */
	public Object processLink(Attributes attributes) {
		// System.out.println(" -- Link -- ");
		String name  = (String) attributes.getValue("name");
		String toName = (String) attributes.getValue("to");
		String fromName = (String) attributes.getValue("from");
		ADGEdge link = new ADGEdge( name );
		ADGInPort i_port = new ADGInPort( toName );
		ADGOutPort o_port = new ADGOutPort( fromName );

		link.getPortList().add( i_port );
		link.getPortList().add( o_port );

		return link;
	}

	/**
	  * @param  stack Description of the Parameter
	  */
	public void processLink(Stack stack) {
		ADGEdge link = (ADGEdge) stack.pop();
		ADGraph adg = (ADGraph) stack.peek();

		Vector edges = adg.getEdgeList();
		Vector ports = new Vector();

		// Add this edge to the correspondent ports and add to-from ports to the edge
		Iterator i = adg.getNodeList().iterator();

		while( i.hasNext() ) {
			ADGNode node = (ADGNode) i.next();
			ADGInPort i_port = (ADGInPort) node.getPort( link.getFromPort().getName() );
			ADGOutPort o_port = (ADGOutPort) node.getPort( link.getToPort().getName() );

			if( i_port != null ) {
				i_port.setEdge( link );
				i_port.setNode( node );
				ports.add( i_port );
			}

			if( o_port != null ) {
				o_port.setEdge( link );
				o_port.setNode( node );
				ports.add( o_port );
			}
		}

		link.setPortList( ports );
		link.setMapping( (JMatrix) _mappingHashT.get(  link.getToPort().getName() ) );
		LinearizationType lt = LinearizationType.find(
			 (String) _linModelHashT.get( link.getToPort().getName() ) );
		assert(lt != null);
		link.setLinModel(lt);
		edges.add( link );
		adg.setEdgeList( edges );
	}

	/**
	 *  Process the start of a property tag in the XML.
	 *
	 * @param  attributes The attributes of the tag.
	 * @return  a proprty object.
	 */
	public Object processProperty(Attributes attributes) {
		//System.out.println(" -- Port -- ");
		String name  = (String) attributes.getValue("name");
		String value = (String) attributes.getValue("value");

		Vector v = new Vector();
		v.add(name);
		v.add(value);

                return v;
	}

	/**
	 * @param  stack Description of the Parameter
	 */
	public void processProperty(Stack stack) {

		Vector vec = (Vector) stack.pop();
                Object obj = (Object) stack.peek();

                if( obj instanceof ADGEdge ) {

		   String value  = (String) vec.get(0);
		   String type   = (String) vec.get(1);

                   ADGVariable variable = new ADGVariable("");
                   Vector indexList = variable.getIndexList();

                   int index1 = value.indexOf('(');
                   int index2 = value.lastIndexOf(')');

                   // Extract the name and the indexes of a variable (if there are indexes)
                   if( index1 == -1 ) {
                      variable.setName( value ); // the variable has name without indexes
                   } else {
                      variable.setName( value.substring(0, index1) ); // extract only the name
                      StringTokenizer st = new StringTokenizer( value.substring(index1+1,index2), "," );

                      Expression exp = null;	// The indexes are expressions
                      while( st.hasMoreTokens() ) {
                         String token = st.nextToken();
                         try {
                             exp = _expParser.getExpression( token ) ;
                             indexList.add( exp );
                         } catch (Exception e) {
                             throw new Error("Unkown expression: " + exp);
                         }
                      }

		      variable.setIndexList( indexList );
                   }

	           variable.setDataType( type );

                   ADGEdge link = (ADGEdge) stack.pop();
                   ADGraph adg  = (ADGraph) stack.peek();

                   Iterator i = adg.getNodeList().iterator();
                   while( i.hasNext() ) {
                      ADGNode node = (ADGNode) i.next();
                      ADGPort i_port = (ADGPort) node.getPort( link.getFromPort().getName() );
                      ADGPort o_port = (ADGPort) node.getPort( link.getToPort().getName() );

                      if( i_port != null ) {
                          ((ADGVariable) i_port.getBindVariables().get(0)).setDataType( variable.getDataType() );
                           i_port.setIOVariable( variable );
                      }

                      if(  o_port != null ) {
                           ADGVariable outVar = (ADGVariable) variable.clone();

                           ((ADGVariable) o_port.getBindVariables().get(0)).setDataType( outVar.getDataType() );

                           int indexSize = outVar.getIndexList().size();

                           Vector index = ((Polytope)o_port.getDomain().getLinearBound().get(0)).getIndexVector().getIterationVector();

                           Vector newIndex = new Vector();
                           Expression exp = null;

                           Iterator j = index.iterator();
                           for(int k=0; k<indexSize; k++) {
                              try {
                                 exp = _expParser.getExpression( (String)j.next() ) ;
                              } catch (Exception e) {
                                 throw new Error("Unkown expression: " + exp);
                              }
                              newIndex.add( exp );
                           }

                           outVar.setIndexList( newIndex );
                           o_port.setIOVariable( outVar );
                      }
                   }

		   stack.push( link );

		}

	}

	/**
	*  Process the start of an index tag in the XML.
	*
	* @param  attributes The attributes of the tag.
	* @return  an argument object.
	*/
	public Object processIndex(Attributes attributes) {
		//System.out.println(" -- Argument -- ");
		// Initialize StaticCtrlVector of a LBS
		String name = (String) attributes.getValue("key");
		String exp  = (String) attributes.getValue("exp");
		ControlExpression expression = new ControlExpression( name );

		try {
			expression.setExpression( _expParser.getExpression( exp ) );
		} catch( Exception e ) {
			throw new Error("Unkown expression: " + exp);
		}

		return expression;
	}

	/**
	* @param _stack
	*/
	public void processIndex(Stack stack) {
		ControlExpression expression = (ControlExpression) stack.pop();
		Object obj = (Object) stack.peek();
		Vector expList = null;

		if( obj instanceof ADGInPort ) {
		   ADGInPort inPort = (ADGInPort) obj;
		   expList = ((Polytope)inPort.getDomain().getLinearBound().get(0)).getIndexVector().getStaticCtrlVector();
		   expList.add( expression );
		   //inPort.getDomain().getLinearBound().getIndexVector().setStaticCtrlVector( expList );

		   Iterator i = inPort.getDomain().getLinearBound().iterator();
		   while( i.hasNext() ) {
		   	((Polytope) i.next()).getIndexVector().setStaticCtrlVector( expList );
		   }

		} else if( obj instanceof ADGOutPort ) {
		   ADGOutPort outPort = (ADGOutPort) obj;
		   expList = ((Polytope)outPort.getDomain().getLinearBound().get(0)).getIndexVector().getStaticCtrlVector();
		   expList.add( expression );
		   //outPort.getDomain().getLinearBound().getIndexVector().setStaticCtrlVector( expList );

		   Iterator i = outPort.getDomain().getLinearBound().iterator();
		   while( i.hasNext() ) {
		   	((Polytope) i.next()).getIndexVector().setStaticCtrlVector( expList );
		   }

  	       } else if( obj instanceof ADGFunction ) {

	           ADGFunction function = (ADGFunction) stack.pop();
		   ADGNode node = (ADGNode) stack.peek(); // we need one level up - the node of the function

		   expList = ((Polytope)node.getDomain().getLinearBound().get(0)).getIndexVector().getStaticCtrlVector();
		   expList.add( expression );
		   //outPort.getDomain().getLinearBound().getIndexVector().setStaticCtrlVector( expList );

		   Iterator i = node.getDomain().getLinearBound().iterator();
		   while( i.hasNext() ) {
		   	((Polytope) i.next()).getIndexVector().setStaticCtrlVector( expList );
		   }

		   stack.push( function );

	       }

	}

	/**
	 *  Process the start of a filter tag in the XML
	 *
	 * @param attributes
	 * @return a filer object
	 */
	public Object processFilter(Attributes attributes) {
		//      System.out.println(" -- Filter -- ");
		String exp  = (String) attributes.getValue("exp");
		String type = (String) attributes.getValue("type");
		ControlExpression expression = new ControlExpression("");

		try {
			expression.setExpression( _expParser.getExpression( exp ) );
		} catch( Exception e ) {
			throw new Error("Unkown expression: " + exp);
		}

		if( type.equals("EQU") ) {
			expression.getExpression().setEqualityType(0);
		} else if (type.equals("GEQ")) {
			expression.getExpression().setEqualityType(1);
		} else if (type.equals("LEQ")) {
			expression.getExpression().setEqualityType(-1);
		} else {
			throw new Error("Unknown Expression Type: " + type);
		}

		return expression;
	}

	/**
	 * @param _stack
	 *  Process an object (ADGInPort, ADGOutPort, without ADGNode!!!) taken from the stack
	 *  in case of a filter token (processFilter(stack)
	 */
	public void processFilter(Stack stack) {

		ControlExpression expression = (ControlExpression) stack.pop();
		ADGPort obj = (ADGPort) stack.peek();

		Iterator j = obj.getDomain().getLinearBound().iterator();
		while( j.hasNext() ) {

		        Polytope linearBound = (Polytope) j.next();

			int index = linearBound.getIndexVector().getIterationVector().size();
			Expression substExp = new Expression();
			// Add expressions to the staticCtrlVector and extend the constraint matrix
			Iterator i = expression.getExpression().iterator();
			while( i.hasNext() ) {
			LinTerm linTerm = (LinTerm) i.next();
			if( linTerm instanceof ModTerm ) {

				ModTerm modTerm = (ModTerm) linTerm;
				String termName = "m" + _modInc++; // A unique name
				ControlExpression exp = new ControlExpression( termName );
				Expression temp = new Expression();
				temp.add (modTerm );
				exp.setExpression( temp );

				// Add expression to the staticCtrlVector
				linearBound.getIndexVector().getStaticCtrlVector().add( 0, exp );

				// Add zero column to the constraint matrix.
				linearBound.getConstraints().insertZeroColumns( 1, index + 1 );

				// Concatination vector conataining the names of components in Index-, StaticCtrl-, DynamicCtrl-, and Parameter vectors:
				Vector concVect = linearBound.getIndexVector().getVectorsNames();

				LinTerm substTerm = new LinTerm( termName );
				substExp.add( substTerm );

				// Add "m >= 0 " to the constraint matrix ----------------------------------
				Expression oneExp   = new Expression();
				oneExp.add( substTerm );

				SignedMatrix rowMat = null;
				try {
					rowMat = Convert.expression2SignedMatrix( oneExp, concVect, 1);
				} catch (Exception e) {
					System.out.println("Error! Convert.expression2SignedMatrix (instance of ADGNode, ModTerm)\n" + e.getMessage());
				}
				linearBound.getConstraints().insertRows( rowMat, -1);
				//---------------------------------------------------------------------------
				// Add "m <= y-1" (modx,y) => -m + (y-1) >= 0 -------------------------------
				Expression oneExp1 = new Expression();
				LinTerm substTerm1 = new LinTerm( -1,1,termName );
				oneExp1.add( substTerm1 );
				LinTerm substTerm2 = new LinTerm( modTerm.getDivider()-1,1,"" );
				oneExp1.add( substTerm2 );

				SignedMatrix rowMat1 = null;
				try {
					rowMat1 = Convert.expression2SignedMatrix( oneExp1, concVect, 1);
				} catch (Exception e) {
					System.out.println("Error! Convert.expression2SignedMatrix (instance of ADGNode, ModTerm)\n" + e.getMessage());
				}
				linearBound.getConstraints().insertRows( rowMat1, -1 );
				//---------------------------------------------------------------------------

			} else if( linTerm instanceof DivTerm ) {
				DivTerm divTerm = (DivTerm) linTerm;
				String termName = "d" + _divInc++;
				ControlExpression exp = new ControlExpression( termName );
				Expression temp = new Expression();
				temp.add (divTerm );
				exp.setExpression( temp );

				LinTerm substTerm = new LinTerm( termName );
				substExp.add( substTerm );

				// Add expression to the staticCtrlVector
				linearBound.getIndexVector().getStaticCtrlVector().add( 0, exp );

				// Add zero column to the constraint matrix.
				linearBound.getConstraints().insertZeroColumns( 1, index + 1 );

			} else {
				substExp.add( linTerm );
			}
			}

			// Concatination vector conataining the names of components in Index-, StaticCtrl-, DynamicCtrl-, and Parameter vectors:
			Vector concVect = linearBound.getIndexVector().getVectorsNames();

			SignedMatrix rowMat = null;
			try {
			rowMat = Convert.expression2SignedMatrix( substExp, concVect, expression.getExpression().getEqualityValue() );
			} catch( Exception e ) {
			System.out.println("Error! Convert.expression2SignedMatrix (instance of ADGPort, substExp)\n" + e.getMessage());
			}
			linearBound.getConstraints().insertRows( rowMat, -1 );
		}
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
	private final static Xml2PN _instance = new Xml2PN();

	private Hashtable _mappingHashT  = new Hashtable();
	
	private Hashtable _linModelHashT = new Hashtable();

	private static int _modInc = 1;
	
	private static int _divInc = 1;

	private ExpressionParser _expParser = new ExpressionParser();
	
	private Vector _parameterVector = new Vector();
}
