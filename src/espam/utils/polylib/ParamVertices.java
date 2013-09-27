/*******************************************************************\
  * 
  This file is donated to ESPAM by Compaan Design BV (www.compaandesign.com) 
  Copyright (c) 2000 - 2005 Leiden University (LERC group at LIACS)
  Copyright (c) 2005 - 2007 CompaanDesign BV, The Netherlands
  All rights reserved.
  
  The use and distribution terms for this software are covered by the 
  Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
  which can be found in the file LICENSE at the root of this distribution.
  By using this software in any fashion, you are agreeing to be bound by 
  the terms of this license.
  
  You must not remove this notice, or any other, from this software.
  
  \*******************************************************************/

package espam.utils.polylib;

import java.util.Iterator;
import java.util.Vector;

import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.matrix.JMatrix;

// ////////////////////////////////////////////////////////////////////////
// // MatlabVisitor

/**
 * This class represents parameterized vertices in the Java programming language
 * and is used for the equivalence with the Param_Vertices type used in the
 * PolyLib. The class definitions are shown here, but are NOT yet implemented in
 * the interface.
 * 
 * @author Edwin Rypkema
 * @version $Id: ParamVertices.java,v 1.1 2007/12/07 22:06:48 stefanov Exp $
 */

public class ParamVertices {
    
    /**
     * @param paramVertex
     *            Description of the Parameter
     */
    public void add(ParamVertices paramVertex) {
        tail.next = paramVertex;
        // first last in list points to last
        tail = paramVertex;
        // first tail in list point to last in list
        paramVertex.head = head;
        // all heads point to head of first in list
    }
    
    public String dump() {
        return "<Vertex: " + vertex.toString() + " | Domain: "
            + domain.toString() + ">";
    }
    
    public Vector getCoordinates() {
        Vector linearExpressions = new Vector();
        try {
            JMatrix tmpMatrix = (JMatrix) vertex.clone();
            JMatrix matrix = tmpMatrix.removeColumn(vertex.getNbColumns() - 1);
            for (int row = 0; row < matrix.getNbRows(); row++) {
                Expression linExpression = new Expression();
                long value;
                for (int column = 0; column < matrix.getNbColumns() - 1; column++) {
                    value = matrix.getElement(row, column);
                    if (value != 0) {
                        String term = _parameters.get(column).toString();
                        linExpression.addVariable((int) value, 1, term);
                    }
                }
                value = matrix.getElement(row, matrix.getNbColumns() - 1);
                if (value != 0) {
                    linExpression.addNumber((int) value, 1);
                }
                linearExpressions.add(linExpression);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linearExpressions;
    }
    
    /**
     * @return Description of the Return Value
     */
    public boolean hasNext() {
        return (next != null);
    }
    
    /**
     * @return Description of the Return Value
     */
    public ParamVertices next() {
        return next;
    }
    
    /**
     * @param D
     *            The new domain value
     */
    public void setDomain(JMatrix D) {
        domain = D;
    }
    
    public void setParameters(Vector parameters) {
        _parameters = parameters;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    /**
     * @param V
     *            The new vertex value
     */
    public void setVertex(JMatrix V) {
        vertex = V;
    }
    
    public String toString() {
        return getCoordinates().toString();
    }
    
    /**
     * @return Description of the Return Value
     */
    public Iterator vertices() {
        return new Iterator() {
            
            public boolean hasNext() {
                return (item != null);
            }
            
            public java.lang.Object next() {
                ParamVertices Tmp = item;
                item = item.next;
                return Tmp;
            }
            
            public void remove() {
            }
            
            ParamVertices item = head;
        };
    }
    
    /**
     * Constraints on parameters Inequalities only. (Polyhedron format);
     */
    public JMatrix domain;
    
    /**
     */
    public ParamVertices head = this;
    
    // /////////////////////////////////////////////////////////////////
    // // public variables ////
    
    /**
     * ptr. to the next vertex, null terminated
     */
    public ParamVertices next = null;
    
    /**
     */
    public ParamVertices tail = this;
    
    /**
     * Each line is a coordinate of the vertex: the first m values of each line
     * are the coefficients of the parameters, the (m+1)th value is the
     * constant, the (m+2)th value is the common denom.
     */
    public JMatrix vertex;
    
    Vector _parameters;
    
}
