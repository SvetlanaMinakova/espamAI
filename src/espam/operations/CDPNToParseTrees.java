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
import java.util.HashMap;

import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.OpdStatement;
import espam.datamodel.parsetree.statement.MemoryStatement;
import espam.datamodel.parsetree.statement.FifoMemoryStatement;
import espam.datamodel.parsetree.statement.RootStatement;
import espam.datamodel.parsetree.statement.AssignStatement;
import espam.datamodel.parsetree.statement.SimpleAssignStatement;
import espam.datamodel.parsetree.statement.ControlStatement;

import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGFunction;
import espam.datamodel.graph.adg.ADGVariable;
import espam.datamodel.graph.adg.ADGCtrlVariable;
import espam.datamodel.graph.adg.ADGInVar;

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
import espam.operations.codegeneration.Function2AssignStatement;
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
		_functionArgumentList = _getFunctionArguments( node );

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

                        // In case of dynamic control, we must set the boolean part to zero in the beggining of every iteration
			_boolCtrlVarList = _getBoolCtrlVars( node );
                        i = _boolCtrlVarList.iterator();
                        while( i.hasNext() ) {
			    String varName = (String)i.next();
                            SimpleAssignStatement sas = new SimpleAssignStatement();
               	   	    sas.setLHSVarName( varName ); 
			    sas.setRHSVarName( "0" );
                            root.addChild( sas );
                            sas.setParent( root );				
                        }

			//   (1) Convert the input port domains to parse tree format.
			_ui.printVerbose("- start step 1");
			i = node.getInPorts().iterator();
			while( i.hasNext() ) {
				ADGInPort ip = (ADGInPort) i.next();
				_processInputPort(ip, root);
			}

			//   (1.5) Convert the invar-s to parse tree format (in case of dynamic programs)
			_ui.printVerbose("- start step 1.5");
			i = node.getInVarList().iterator();
			while( i.hasNext() ) {
				ADGInVar invar = (ADGInVar) i.next();
				_processInVar(invar, root);
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
	 *  (1.5) Converts an invar into its parse tree equivalent.
	 *
	 * @param  invar Description of the Parameter
	 * @param  stitch Description of the Parameter
	 * @exception  CodeGenerationException
	 */
	private void _processInVar(ADGInVar invar, ParserNode parent)
		throws CodeGenerationException {

		Vector ifStatements;
		ParserNode stitch;

		try {
		        Polytope ndPolytope = (Polytope) invar.getNode().getDomain().getLinearBound().get(0);

			Iterator i = invar.getDomain().getLinearBound().iterator();
			while( i.hasNext() ) {
			        Polytope polytope = (Polytope) i.next();
				//simplify the invar domain in the context of the node domain
				Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope );
				//convert the polytope to if-statements
				ifStatements = Polytope2IfStatements.convert( sPolytope );
			        stitch = addStatements(ifStatements, parent);

				SimpleAssignStatement sas = new SimpleAssignStatement();

				sas.setLHSVarName( invar.getBindVariable().getName() ); 
				sas.setIndexListLHS( invar.getBindVariable().getIndexList() );
				sas.setRHSVarName( invar.getRealName() );
				sas.setNodeName( invar.getNode().getName() );
				
				stitch.addChild(sas);
				sas.setParent(stitch);
			}

		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
				"Processing Input port "
					+ invar.getName()
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

		Vector ifStatements;
		ParserNode stitch = null;
		MemoryStatement memoryStatement;
		ADGFunction nf = node.getFunction();

		try {
		        Polytope ndPolytope = node.getDomain().getLinearBound().get(0);

			AssignStatement assignStatement = Function2AssignStatement.convert( node );

			Iterator i = nf.getDomain().getLinearBound().iterator();
			while( i.hasNext() ) {
			        Polytope polytope = (Polytope) i.next();
				//simplify the function domain in the context of the node domain
				Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope );
				//convert the polytope to if-statements
				ifStatements = Polytope2IfStatements.convert( sPolytope );
		//		ifStatements = Polytope2IfStatements.convert( ndPolytope );

			        stitch = addStatements(ifStatements, parent);
				stitch.addChild(assignStatement);
				assignStatement.setParent(stitch);

				Iterator j = nf.getCtrlVarList().iterator();
				while( j.hasNext() ) {
					ADGCtrlVariable cVar = (ADGCtrlVariable) j.next();
					SimpleAssignStatement sas = new SimpleAssignStatement();

					sas.setLHSVarName( cVar.getName() );
					sas.setIndexListLHS( cVar.getIndexList() );
					sas.setRHSVarName( cVar.getIterator() );
			
					stitch.addChild(sas);
					sas.setParent(stitch);
				}
			}

		} catch( Exception e ) {
			e.printStackTrace();
			throw new CodeGenerationException(
				"Processing Function "
					+ node.getFunction().getName()
					+ ": "
					+ e.getMessage());
		}
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
		Vector opdStatements;
		ParserNode stitch;

		try {
		        Polytope ndPolytope = (Polytope) ((ADGNode) op.getNode()).getDomain().getLinearBound().get(0);

			opdStatements = OutputPort2OpdStatement.convert2list( op, _process );

			Iterator i = op.getDomain().getLinearBound().iterator();
			while( i.hasNext() ) {
				Polytope polytope = (Polytope) i.next();
				//simplify the port domain in the context of the node domain
				Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope );
				//convert the polytope to if-statements
				ifStatements = Polytope2IfStatements.convert( sPolytope );
				stitch = addStatements(ifStatements, parent);
				
				Iterator j = opdStatements.iterator();
				while( j.hasNext() ) {
					OpdStatement os = (OpdStatement) j.next();
					stitch.addChild( os );
					os.setParent(stitch);
				}
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



	/**
	 *  Add parser nodes in _markedList
	 *
	 * @param  node Description of the Parameter
	 */
	private Vector _getFunctionArguments( ADGNode node ) {
		Vector tmpArgumentList = new Vector();
	
		Iterator i = node.getFunction().getInArgumentList().iterator();
		while( i.hasNext() ) {
			ADGVariable adgVar = (ADGVariable) i.next();
			tmpArgumentList.add( adgVar );
		}

		i = node.getFunction().getOutArgumentList().iterator();
		while( i.hasNext() ) {
			ADGVariable adgVar = (ADGVariable) i.next();
			tmpArgumentList.add( adgVar );
		}
		return tmpArgumentList;
	}



	/**
	 *  Add parser nodes in _markedList
	 *
	 * @param  node Description of the Parameter
	 */
	private boolean _isDynamicCtrl( String name ) {

		Iterator i = _functionArgumentList.iterator();
		while( i.hasNext() ) {
			ADGVariable adgVar = (ADGVariable) i.next();
			if( adgVar.getName().equals( name ) ) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  Extract the bool dynamic variables in _boolCtrlVarList
	 *
	 * @param  node Description of the Parameter
	 */
	private Vector _getBoolCtrlVars( ADGNode node ) {
		Vector tmpVarList = new Vector();
	        HashMap  tmp = new HashMap();

		Iterator i = node.getInPorts().iterator();
		while( i.hasNext() ) {
			ADGInPort adgInPort = (ADGInPort) i.next();
                        Iterator j = adgInPort.getBindVariables().iterator();
                        while( j.hasNext() ) {
                             ADGVariable adgVar = (ADGVariable) j.next();
                             String varName = adgVar.getName();
                             if( varName.endsWith("_b") ) {
        			   if ( !tmp.containsKey(varName) ) {
			                tmp.put(varName, "");
   			                tmpVarList.add( varName );
                                   }
                             }
			}
		}

		i = node.getOutPorts().iterator();
		while( i.hasNext() ) {
			ADGOutPort adgOutPort = (ADGOutPort) i.next();
                        Iterator j = adgOutPort.getBindVariables().iterator();
                        while( j.hasNext() ) {
                             ADGVariable adgVar = (ADGVariable) j.next();
                             String varName = adgVar.getName();
                             if( varName.endsWith("_b") ) {
        			   if ( !tmp.containsKey(varName) ) {
			                tmp.put(varName, "");
   			                tmpVarList.add( varName );
                                   }
                             }
			}
		}

		return tmpVarList;
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
	private Vector _functionArgumentList = null;
        private Vector _boolCtrlVarList = null;
}

