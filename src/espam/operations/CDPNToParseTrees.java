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

package espam.operations;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.OpdStatement;
import espam.datamodel.parsetree.statement.MemoryStatement;
import espam.datamodel.parsetree.statement.RootStatement;
import espam.datamodel.parsetree.statement.AssignStatement;
import espam.datamodel.parsetree.statement.ControlStatement;

import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGOutPort;

import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;

import espam.datamodel.domain.Polytope;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

import espam.operations.codegeneration.CodeGenerationException;
import espam.operations.codegeneration.Polytope2IfStatements;
import espam.operations.codegeneration.InputPort2MemoryStatement;
import espam.operations.codegeneration.OutputPort2OpdStatement;
import espam.operations.codegeneration.Node2AssignStatement;
import espam.operations.codegeneration.Node2ControlStatements;

/**
 *  This class converts a Compaan Dynamic Process Network (CDPN) data model into several parse
 *  trees. The parse trees that are generated represent the code of
 *  structred Kahn processes. The Kahn processes is structured in four main
 *  parts..............
 *
 * @author  Todor Stefanov, Joris Huizer
 * @version  $Id: PNToParseTree.java,v 1.15 2002/10/08 14:23:14 kienhuis Exp
 *      $
 */
public class CDPNToParseTrees {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/**
	*  Return the singleton instance of this class;
	*
	* @return  the instance.
	*/
	public final static CDPNToParseTrees getInstance() {
		return _instance;
	}

	/**
	 *  This class converts a CDPN into its parse tree representation.
	 *
	 * @param  pn Description of the Parameter
	 * @exception  EspamException MyException If such and such occurs
	 */
	public void cdpnToParseTrees(CDProcessNetwork pn, Mapping mapping) throws EspamException {

		System.out.println(" -- Add ParseTree descriptions to CDPN ...");

		try {
			// For every process create a parse tree of the Kahn process.
			Vector vectorOfTrees = new Vector();
			Vector vectorOfNames = new Vector();
			Iterator p = pn.getProcessList().iterator();
			while( p.hasNext() ) {

				_process = (CDProcess) p.next();
                                _numberOfNodes =_process.getAdgNodeList().size();

				MProcessor processor = mapping.getMProcessor(_process);
				if( processor != null ) {
					_scheduleType = processor.getScheduleType();
				} else {
					_scheduleType = 0;
				}
				
				// For every node in the process create a parse tree.
				ADGNode node;
				RootStatement tree;
				Iterator n = _process.getAdgNodeList().iterator();
				while( n.hasNext() ) {
					node = (ADGNode) n.next();
					tree = _processNode(node);
                                        vectorOfTrees.add( tree );
					vectorOfNames.add( node.getName() );
				}
				
				int pos = 0;
				Iterator i = _process.getSchedule().iterator();
				while ( i.hasNext() ) {
					tree = (RootStatement) i.next();
					_connectParseTrees(tree, vectorOfTrees, vectorOfNames);
					_process.getSchedule().setElementAt(tree, pos++);
				}
				
			}

			System.out.println(" -- Addition [Done]");

		} catch( Exception e ) {
			e.printStackTrace();
			System.out.println("\nCDPNToParseTree Exception: " + e.getMessage());
		}
	}

	/**
	 *  Add the list of statements to the current stitch node. The
	 *  statements are added in a linear order. That means that parent <->
	 *  child pair is created.
	 *
	 * @param  statements A list of statements that are added.
	 * @param  stitchNode point where statements are added.
	 * @return  Description of the Return Value
	 */
	public ParserNode addStatements( Vector statements, ParserNode stitchNode) {
		Iterator i = statements.iterator();
		while( i.hasNext() ) {
			ParserNode p = (ParserNode) i.next();
			stitchNode.addChild(p);
			p.setParent(stitchNode);
			stitchNode = p;
		}
		return stitchNode;
	}


	///////////////////////////////////////////////////////////////////
	////                         private methods                   ////

	/**
	 *  Convert a Node from the ADG into its parse tree equivalent.
	 *
	 * @param  node Description of the Parameter
	 * @exception  CodeGenerationException
	 */
	private RootStatement _processNode(ADGNode node) throws CodeGenerationException {

		Iterator i;
		RootStatement root = new RootStatement();
		String description = "Node: " + node.getName();
		root.setDescription(description);
		ParserNode stitch = root;

		try {

		        if (_numberOfNodes > 1 && _scheduleType == 0 ) {
                            //   (0) Add the control statements of a ADGNode to the parse tree
 		            Vector ctrlStatements = Node2ControlStatements.convert( node );
                            i = ctrlStatements.iterator();
		            while ( i.hasNext() ) {
                                 ControlStatement cs  = (ControlStatement) i.next();
                                 root.addChild( cs );
	                         cs.setParent( root );
		            }
                        }

			//   (1) Convert the input port domains to parse tree format.
			_ui.printVerbose("- start step 1");
			i = node.getInPorts().iterator();
			while( i.hasNext() ) {
				ADGInPort ip = (ADGInPort) i.next();
				_processInputPort(ip, root);
			}

			//   (2) Convert the function to parse tree format.
			_ui.printVerbose("- start step 2");
			_processFunction(node, root);

			//   (3) Convert the output port domains to parse tree format.
			_ui.printVerbose("- start step 3");
			i = node.getOutPorts().iterator();
			while( i.hasNext() ) {
				ADGOutPort op = (ADGOutPort) i.next();
				_processOutputPort(op, root);
			}
		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
				"Processing Node " + node.getName() + ": " + e.getMessage());
		}
		return root;
	}

	/**
	 *  (1) Converts an input port into its parse tree equivalent.
	 *
	 * @param  ip Description of the Parameter
	 * @param  stitch Description of the Parameter
	 * @exception  CodeGenerationException
	 */
	private void _processInputPort(ADGInPort ip, ParserNode parent)
		throws CodeGenerationException {

		Vector ifStatements;
		ParserNode stitch;
		MemoryStatement memoryStatement;

		try {
		        Polytope ndPolytope = (Polytope) ((ADGNode) ip.getNode()).getDomain().getLinearBound().get(0);

			memoryStatement = InputPort2MemoryStatement.convert( ip, _process );

			Iterator i = ip.getDomain().getLinearBound().iterator();
			while( i.hasNext() ) {
			        Polytope polytope = (Polytope) i.next();
				//simplify the port domain in the context of the node domain
				Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope );
				//convert the polytope to if-statements
				ifStatements = Polytope2IfStatements.convert( sPolytope );
			        stitch = addStatements(ifStatements, parent);
                                stitch.addChild( memoryStatement );
				memoryStatement.setParent(stitch);
			}

		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
				"Processing Input port "
					+ ip.getName()
					+ ": "
					+ e.getMessage());
		}
	}

	/**
	 * @param  node Description of the Parameter
	 * @param  stitch Description of the Parameter
	 * @exception  CodeGenerationException MyException If such and such
	 *      occurs
	 */
	private void _processFunction(ADGNode node, ParserNode parent)
		throws CodeGenerationException {

		AssignStatement assignStatement = Node2AssignStatement.convert( node );
		parent.addChild(assignStatement);
		assignStatement.setParent(parent);
	}

	/**
	 *  (3) Converts an output port into its parse tree equivalent.
	 *
	 * @param  op Description of the Parameter
	 * @param  stitch Description of the Parameter
	 * @exception  CodeGenerationException MyException If such and such
	 *      occurs
	 */
	private void _processOutputPort(ADGOutPort op, ParserNode parent)
		throws CodeGenerationException {

		Vector ifStatements;
		OpdStatement opdStatement;
		ParserNode stitch;

		try {
		        Polytope ndPolytope = (Polytope) ((ADGNode) op.getNode()).getDomain().getLinearBound().get(0);

			opdStatement = OutputPort2OpdStatement.convert( op, _process );

			Iterator i = op.getDomain().getLinearBound().iterator();
			while( i.hasNext() ) {
				Polytope polytope = (Polytope) i.next();
				//simplify the port domain in the context of the node domain
				Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope );
				//convert the polytope to if-statements
				ifStatements = Polytope2IfStatements.convert( sPolytope );
				stitch = addStatements(ifStatements, parent);
				stitch.addChild( opdStatement );
				opdStatement.setParent(stitch);
			}

		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
				"Processing Output port "
					+ op.getName()
					+ ": "
					+ e.getMessage());
		}
	}

	/**
	 *  Convert the schedule data structure of a process to its parser tree
	 *  equivalent
	 *
	 * @param  process Description of the Parameter
	 * @exception  CodeGenerationException MyException If such and such
	 *      occurs
	 */
	private RootStatement _scheduleToParseTree(Vector schedule) {

		RootStatement root = (RootStatement) schedule.get(0);
		return root;
	}

	/**
	 *  Connect the parse trees of the nodes to the parse tree of the
	 *  process.
	 *
	 * @param  process Description of the Parameter
	 * @exception  CodeGenerationException
	 */
	private void _connectParseTrees(RootStatement tree, Vector vectorOfTrees, Vector vectorOfNames)
		throws CodeGenerationException {

		try {
			// Creates a list (_markedList) of all leafs of the process parser tree
			_parseTreeWalk( tree );

			Iterator i = _markedList.iterator();
			while( i.hasNext() ) {

				AssignStatement nd = (AssignStatement) i.next();
				ParserNode parent = nd.getParent();
				String nodeName = nd.getNodeName();

				ParserNode nodeTreeRoot = null;

				// search in the list of nodes.............
				int pos = 0;
				Iterator p = vectorOfNames.iterator();
				while( p.hasNext() ) {
					String name = (String) p.next();
					if( name.equals(nodeName) ) {
						nodeTreeRoot = (ParserNode) vectorOfTrees.get(pos);
					}
					pos++;
				}
   
				int position = parent.removeChild(nd);
				parent.addChildAt(position, nodeTreeRoot);
				nodeTreeRoot.setParent(parent);
			}

			_markedList.clear();

		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
					" (ConnectParseTrees): "
					+ e.getMessage());
		}
	}

	/**
	 *  Add parser nodes in _markedList
	 *
	 * @param  node Description of the Parameter
	 */
	private void _parseTreeWalk(ParserNode node) {

		if( node instanceof AssignStatement) {
			_markedList.add(node);
		}
		Iterator i = node.getChildren();
		while( i.hasNext() ) {
			_parseTreeWalk((ParserNode) i.next());
		}
	}

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////

	/**
	 *  Create a unique instance of this class to implement a singleton
	 */
	private final static CDPNToParseTrees _instance = new CDPNToParseTrees();

	private Vector _markedList = new Vector();

	/** Get reference to the UserInterface. */
	private UserInterface _ui = UserInterface.getInstance();

	private CDProcess _process;
	private int _numberOfNodes;
	private int _scheduleType;
}

