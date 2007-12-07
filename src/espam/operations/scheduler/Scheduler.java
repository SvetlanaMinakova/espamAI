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

package espam.operations.scheduler;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.parsetree.ParserNode;
import espam.datamodel.parsetree.statement.RootStatement;
import espam.datamodel.parsetree.statement.AssignStatement;
import espam.datamodel.parsetree.statement.ElseStatement;
import espam.datamodel.parsetree.statement.IfStatement;
import espam.datamodel.parsetree.statement.ControlStatement;

import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.domain.Polytope;

import espam.operations.codegeneration.Node2ForStatements;
import espam.operations.codegeneration.Node2IfStatements;
import espam.operations.codegeneration.Node2ControlStatements;
import espam.operations.CDPNToParseTrees;

import espam.utils.symbolic.expression.Expression;

import espam.operations.codegeneration.CodeGenerationException;

//////////////////////////////////////////////////////////////////////////
//// Scheduler Operations

/**
 * @author  Todor Stefanov, Hristo Nikolov
 * @version  $Id: Scheduler.java,v 1.1 2007/12/07 22:07:48 stefanov Exp $
 */

public class Scheduler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Return the singleton instance of this class;
     *
     * @return  The instance value
     */
    public final static Scheduler getInstance() {
        return _instance;
    }

    /**
     *  Get the global schedule tree
     *
     * @return  The scheduleTree value
     */
    public ParserNode getScheduleTree() {
        return _scheduleTree;
    }

    /**
     *  Set the global schedule tree, propagate the ifelse conditions,
     *  and set the Node names of the AssignStatements (according to the
     *  node names in ADG);
     *
     * @param  scheduleTree The new scheduleTree value
     * @param  type The type of the scheduleTree
     */
    public void setScheduleTree(ParserNode scheduleTree, String type) {
        _scheduleTree = scheduleTree;

	if ( type.equals("fromMatlab") ) {
           _ndCounter = 1;
           _propagateIfElseCondition(_scheduleTree);
           _setNodeNames(_scheduleTree);
	}

    }

    /**
     *  Return a schedule between functions associated with nodes
     *  which are placed in the vector adgNodes
     *
     * @param  adgNodes Description of the Parameter
     * @return  Description of the Return Value
     */
    public ParserNode doSchedule(Vector adgNodes) {

	ParserNode localScheduleTree;

	if( adgNodes.size() != 1 ) {

           localScheduleTree = (ParserNode) _scheduleTree.deepClone();
           _parseTreeMarking(localScheduleTree, adgNodes);
           _parseTreePruning(localScheduleTree);
	   _replaceElseNodesWithIfNodes( localScheduleTree );

	} else {

	        localScheduleTree = new RootStatement();
		ParserNode stitch;

		Polytope polytope = (Polytope)((ADGNode) adgNodes.get(0)).getDomain().getLinearBound().get(0);

		Vector forStatements = Node2ForStatements.convert( polytope );
		stitch = CDPNToParseTrees.getInstance().addStatements(forStatements, localScheduleTree);

		Vector ctrlStatements = Node2ControlStatements.convert( (ADGNode) adgNodes.get(0) );
		Iterator i = ctrlStatements.iterator();
		while ( i.hasNext() ) {
                     ControlStatement cs  = (ControlStatement) i.next();
                     stitch.addChild( cs );
	             cs.setParent( stitch );
		}

		Vector ifStatements = Node2IfStatements.convert( polytope );
		stitch = CDPNToParseTrees.getInstance().addStatements(ifStatements, stitch);

		AssignStatement assignStatement = new AssignStatement();
		assignStatement.setFunctionName( ((ADGNode) adgNodes.get(0)).getFunction().getName() );
		assignStatement.setNodeName( ((ADGNode) adgNodes.get(0)).getName() );
		stitch.addChild( assignStatement );
		assignStatement.setParent(stitch);

	}

        return localScheduleTree;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     *  Define a subtree according to the nodes in vec
     *
     * @param  node Description of the Parameter
     * @param  vec Description of the Parameter
     */
    private void _parseTreeMarking(ParserNode node, Vector nodeList) {

        if (node instanceof AssignStatement) {

            Iterator i = nodeList.iterator();
            while (i.hasNext()) {
                ADGNode an = (ADGNode) i.next();
                if ( ((AssignStatement) node).getNodeName().equals( an.getName() ) ) {
                    _setMark(node);
                }
            }
        }

        Iterator j = node.getChildren();
        while (j.hasNext()) {
            _parseTreeMarking((ParserNode) j.next(), nodeList);
        }
    }

    /**
     *  Mark backwards in Schedule Tree
     *
     * @param  node Description of the Parameter
     */
    private void _setMark(ParserNode node) {

        while ((node instanceof RootStatement) == false) {
            node.setParsedFlag(true);
            node = node.getParent();
        }
        node.setParsedFlag(true);
    }

    /**
     *  Create a Parse Tree correspondinf to a local schedule
     *
     * @param  node Description of the Parameter
     */
    private void _parseTreePruning(ParserNode node) {

        _prunePrepare(node);
        Iterator j = _markedList.iterator();
        while (j.hasNext()) {
            ParserNode tempNode = (ParserNode) j.next();
            ParserNode temp1Node = tempNode.getParent();
            tempNode.deepRemove();
            temp1Node.removeChild( tempNode );
        }
        _markedList.clear();
    }

    /**
     *  Add nodes in _markedList to remove tree rooted at that node
     *
     * @param  node Description of the Parameter
     */
    private void _prunePrepare(ParserNode node) {
        Iterator j = node.getChildren();
        while (j.hasNext()) {
            ParserNode tempNode = (ParserNode) j.next();
            if (tempNode.getParsedFlag() == true) {
                _prunePrepare( tempNode );
            } else {
                _markedList.add( tempNode );
            }
        }

    }


    /**
     *  Description of the Method
     *
     * @param  p Description of the Parameter
     */
    private void _propagateIfElseCondition(ParserNode p) {

        // Check this Statement First
        _setIfElseCondition(p);

        // Go down recursively, to check the Children
        Iterator i = p.getChildren();
        while (i.hasNext()) {
            _propagateIfElseCondition((ParserNode) i.next());
        }
    }

    /**
     *  Check the Children of the supplied Statement for an occurance of an
     *  If/Else pair. If such pair is found, set the opposite linear
     *  expression of the If-Statement to the ElseStatement.
     *
     * @param  p A Parse node.
     * @throws  Error If an Else statement is found, but without a
     *      corresponding If statement.
     */
    private void _setIfElseCondition(ParserNode p) {

        Iterator i = p.getChildren();
        Expression exp = null;

        // Check this node's children
        while (i.hasNext()) {
            ParserNode st = (ParserNode) i.next();

            if (st instanceof IfStatement) {
                exp = (Expression) (((IfStatement) st).getCondition()).clone();
                exp.negate();
                exp.addMinusOne();
		exp.simplify();
            }
            if (st instanceof ElseStatement) {
                if (exp != null) {
                    ((ElseStatement) st).setCondition(exp);
                } else {
                    throw new Error("\nThere is not Condition Set " +
                            "for the Else statement");
                }
            }
        }
    }


    /**
     *  Replace Else nodes in a parse three
     * with equivalen IF nodes when necessary.
     *
     * @param  node Root node of a parse tree.
     *
     */

    private void _replaceElseNodesWithIfNodes(ParserNode node) {

        // Check for Else nodes to be replaced by IF nodes.
	// The Else nodes to be replaced are stored in _markedList
	_markedList.clear();
        _checkForElseNodes(node);

	// Replace the Else nodes with IF nodes
        Iterator j = _markedList.iterator();
        while (j.hasNext()) {
            ElseStatement currentNode = (ElseStatement) j.next();
            ParserNode parent = currentNode.getParent();

             IfStatement ifStatement = new IfStatement(currentNode.getCondition(), 1);
 	     ifStatement.setParent( parent );

             Iterator i = currentNode.getChildren();
             while (i.hasNext()) {
	         ParserNode child = (ParserNode) i.next();
                 ifStatement.addChild( child );
	         child.setParent( ifStatement );
             }

            int pos = parent.removeChild( currentNode );
            parent.addChildAt(pos, ifStatement);

        }

        _markedList.clear();
    }


    /**
     * Check the Else nodes in a parse tree and find which of them
     * have to be replaced by equivalent IF nodes.
     *
     * @param  node Root node of a parse tree
     */
    private void _checkForElseNodes(ParserNode node) {

        ParserNode parent;
	ParserNode nextNode = null;
	ParserNode currentNode;
	int nodePosition;

	currentNode = node;
	// Check if this node is ElseStatement and add it for replacement with
	// IfStatement
        if ((currentNode instanceof RootStatement) == false) {
          parent = currentNode.getParent();
	   if( currentNode instanceof ElseStatement ) {

             nodePosition = parent.findChild( currentNode );
	      if( nodePosition > 0 ) {
	         nextNode = parent.getChild( nodePosition-1 );
              }

	      if( nodePosition == 0 ) {
                 // Add this Else statement for replacement
                 _markedList.add( currentNode );
	      } else if( (nextNode instanceof IfStatement) == false ) {
                 // Add this Else statement for replacement
                 _markedList.add( currentNode );
	      } else {
                  Expression exp = (Expression) (((IfStatement) nextNode).getCondition()).clone();
                  exp.negate();
                  exp.addMinusOne();
		  exp.simplify();
                  if( !exp.equals( ((ElseStatement) currentNode).getCondition() )  ) {
                     // Add this Else statement for replacement
                    _markedList.add( currentNode );
		  }
	      }

	   }
	}
	// Go down recursively to check the Children
        Iterator i = currentNode.getChildren();
        while (i.hasNext()) {
           ParserNode p = (ParserNode) i.next();
          _checkForElseNodes( p );
        }

    }


    /**
     *  Set unique Node names to the AssignStatements in the Scheduler tree
     *  in accordance with the names of the corresponding nodes in the ADG.
     *  The names have the following pattern: ND_1, ND_2, ............
     *
     * @param  node Description of the Parameter
     */

    private void _setNodeNames(ParserNode node) {

        if (node instanceof AssignStatement) {
            ((AssignStatement) node).setNodeName("ND_" + _ndCounter++);
        }

        Iterator i = node.getChildren();
        while (i.hasNext()) {
            _setNodeNames((ParserNode) i.next());
        }
    }




    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Create a unique instance of this class to implement a singleton
     */
    private final static Scheduler _instance = new Scheduler();

    /**
     *  Global Schedule Tree
     */
    private ParserNode _scheduleTree = null;

    /**
     *  Auxiliary variables
     */
    private int _ndCounter;
    private Vector _markedList = new Vector();

}




