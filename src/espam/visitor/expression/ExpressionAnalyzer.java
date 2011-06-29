/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2011 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.expression;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;

import espam.datamodel.domain.IndexVector;
import espam.datamodel.domain.Polytope;
import espam.datamodel.graph.adg.*;

import espam.utils.symbolic.expression.*;


/**
 * Expression analyzer class.
 * @author Sven van Haastregt
 */
public class ExpressionAnalyzer {

  /**
   * Constructor.
   * @param indexList IndexVector of complete domain.
   */
  public ExpressionAnalyzer(IndexVector indexList) {
    Iterator paramIter;
    paramIter = indexList.getParameterVector().iterator();
    while(paramIter.hasNext()){
      ADGParameter param = (ADGParameter) paramIter.next();
      Vector<Integer> bounds = new Vector<Integer>();
      bounds.addElement(new Integer(param.getLowerBound()));
      bounds.addElement(new Integer(param.getUpperBound()));
      _parameters.put(param.getName(), bounds);
    }

  };


  /**
   * Computes the number of bits needed to store a counter counting from expr_lb to expr_ub.
   */
  public int computeCounterWidth(String index, Expression expr_lb, Expression expr_ub) {
    int bounds[] = findBounds(index, expr_lb, expr_ub);
    int lb = bounds[0];
    int ub = bounds[1];
    int maxval = Math.max(Math.abs(lb), Math.abs(ub+1)); // ub+1 to make sure one-ahead value also fits
    int counterWidth = (int) (Math.ceil(Math.log(maxval)/Math.log(2)));
    if (lb < 0 || ub < 0) {
      // Accomodate sign bit
      counterWidth++;
    }
    return counterWidth+1;  // TODO: +1 seems to be necessary for some reason
  }


  /**
   * Computes lower and upper bounds of given expressions. Should be called from outermost to innermost bounds.
   * @Return Returns a 2-element array, which is structured as follows:
   *         [0]  lower bound
   *         [1]  upper bound
   */
  public int[] findBounds(String index, Expression expr_lb, Expression expr_ub) {
    _findUpperBound(index, expr_lb, expr_ub);
    int ret[] = new int[2];
    ret[0] = _lb;
    ret[1] = _ub;

    return ret;
  }


	//find the upper bounds for indexes, for deciding the counter bit width
	private void _findUpperBound(String index, Expression expr_lb, Expression expr_ub) {
    _lb = 0;
    _ub = 0;
    HashMap <String, Integer> ttParam_ub = new HashMap <String,Integer>();
    HashMap <String, Integer> ttParam_lb = new HashMap <String,Integer>();;
    //	boolean allAddition = true;

    //	ttParam.putAll(_parameters);
    //	ttParam.putAll(_boundsLinks);

    //		System.out.println("Upper bound:" + expr);


    Iterator i = _parameters.keySet().iterator();
    while(i.hasNext()){
      String param_name = (String)i.next();

      //System.out.println("param name is " + param_name);

      Iterator expr_it = expr_ub.iterator();
      LinTerm j;
      while (expr_it.hasNext()) {
        j = (LinTerm) expr_it.next();

        //System.out.println("terms for upperbound " + j.toString());

        if( j.getName().equals(param_name) && j.getSign() == 1){
          ttParam_ub.put(param_name, _parameters.get(param_name).get(1));
          //continue;
          //System.out.println("param name is " + param_name + "is positive");
          //System.out.println(ttParam_ub.get(param_name).intValue());
        }
        else if(j.getName().equals(param_name) && j.getSign() == -1){
          ttParam_ub.put(param_name, _parameters.get(param_name).get(0));
          //continue;
          //System.out.println("param name is " + param_name + "is negetive");
        }
      }

    }

    ttParam_ub.putAll(_findIteratorBounds(expr_ub, true));
    Iterator j;

    /*	Iterator i = expr.iterator();
        LinTerm j;
        while (i.hasNext()) {
        j = (LinTerm) i.next();
    //when the term is minus, we shouldn't use upperbound
    //we assume currently lower bound can't be less than 0, so we just simply remove this term.
    if( j.getSign() == -1){
    j.remove();
    }
    }*/

    Vector<Integer> point = new Vector<Integer>();
    point.addAll(ttParam_ub.values());

    Vector<String> indice = new Vector<String>();
    indice.addAll(ttParam_ub.keySet());

    //System.out.println("indice_ub " + indice.toString());
    //System.out.println("point_ub " + point.toString());

    _ub = expr_ub.evaluate(point, indice);

    //when upper bound is 0, it cannot have log2 operation
    if (_ub == 0){
      _ub = 1;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////		
    // Lower bound
    //System.out.println("upperbound expression is " + expr_ub.toString());
    //System.out.println("lowerbound expression is " + expr_lb.toString());

    i = _parameters.keySet().iterator();
    while(i.hasNext()){
      String param_name = (String)i.next();

      //System.out.println("param name is " + param_name);

      Iterator expr_it = expr_lb.iterator();
      LinTerm term;
      while (expr_it.hasNext()) {
        term = (LinTerm) expr_it.next();

        //System.out.println("lowerbound term " + term.toString());

        if( term.getName().equals(param_name) && term.getSign() == -1){
          ttParam_lb.put(param_name, _parameters.get(param_name).get(1));
          //continue;
        }
        else if(term.getName().equals(param_name) && term.getSign() == 1){
          ttParam_lb.put(param_name, _parameters.get(param_name).get(0));
          //System.out.println("param name is " + param_name + " is positive");
          //System.out.println(ttParam_lb.get(param_name).intValue());
          //continue;
        }
      }

    }

    ttParam_lb.putAll(_findIteratorBounds(expr_lb, false));

    Vector<Integer> point_lb = new Vector<Integer>();
    point_lb.addAll(ttParam_lb.values());

    Vector<String> indice_lb = new Vector<String>();
    indice_lb.addAll(ttParam_lb.keySet());

    //System.out.println("indice_lb " + indice_lb.toString());
    //System.out.println("point_lb " + point_lb.toString());
    _lb = expr_lb.evaluate(point_lb, indice_lb);
    //System.out.println(_lb);

    Vector<Integer> lb_ub = new Vector <Integer>();
    lb_ub.addElement(new Integer(_lb));
    lb_ub.addElement(new Integer(_ub));

    _boundsLinks.put(index, lb_ub);
  }


  /**
   * Tries to find bound values for outer iterators.
   * @param isUpperbound indicates if we're establishing upper bounds (true) or lower bounds (false).
   */
  private HashMap<String,Integer> _findIteratorBounds(Expression expr, boolean isUpperbound) {
    HashMap<String,Integer> values = new HashMap <String,Integer>();
    int sign = isUpperbound ? 1 : -1;

    Iterator j = _boundsLinks.keySet().iterator();
    while(j.hasNext()){
      String index_name = (String)j.next();
      //System.out.println(expr + " contains " + index_name + "?? " + expr.containsVariable(index_name));
      Iterator expr_it = expr.iterator();
      LinTerm term;
      while (expr_it.hasNext()) {
        term = (LinTerm) expr_it.next();
        if (term instanceof MinimumTerm) {
          values.putAll(_findIteratorBounds(((MinimumTerm)term).getExpressionOne(), isUpperbound));
          values.putAll(_findIteratorBounds(((MinimumTerm)term).getExpressionTwo(), isUpperbound));
        }
        else if (term instanceof MaximumTerm) {
          values.putAll(_findIteratorBounds(((MaximumTerm)term).getExpressionOne(), isUpperbound));
          values.putAll(_findIteratorBounds(((MaximumTerm)term).getExpressionTwo(), isUpperbound));
        }
        else if (term.getName().equals(index_name) && term.getSign() == sign) {
          values.put(index_name, _boundsLinks.get(index_name).get(1));
          //continue;
        }
        else if (term.getName().equals(index_name) && term.getSign() == -sign) {
          values.put(index_name, _boundsLinks.get(index_name).get(0));
          //continue;
        }
      }
    }

    return values;
  }



  /////////////////////////////////////////////////////////////////
  // private variables //

  private int _lb;
  private int _ub;

  private HashMap <String, Vector<Integer>> _parameters = new HashMap <String, Vector<Integer>>();

  private HashMap <String, Vector<Integer>> _boundsLinks = new HashMap<String, Vector<Integer>>(); //hash map with index/param names as keys, and lower and upper bounds vector as values.
};
