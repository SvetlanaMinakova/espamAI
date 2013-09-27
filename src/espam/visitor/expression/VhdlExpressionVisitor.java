
package espam.visitor.expression;

import java.util.Iterator;

import espam.utils.symbolic.expression.CeilTerm;
import espam.utils.symbolic.expression.DivTerm;
import espam.utils.symbolic.expression.Expression;
import espam.utils.symbolic.expression.FloorTerm;
import espam.utils.symbolic.expression.LinTerm;
import espam.utils.symbolic.expression.MaximumTerm;
import espam.utils.symbolic.expression.MinimumTerm;
import espam.utils.symbolic.expression.ModTerm;
import espam.visitor.ExpressionVisitor;

import espam.datamodel.domain.*;

/**
 * This interface describes a visitor that formats a linear expression in a
 * specific way for VHDL.
 *
 * @author Ying Tao
 * @version $Id: VhdlExpressionVisitor.java,v 1.3 2012/02/24 14:58:17 svhaastr Exp $
 *           
 * @stereotype Visitor Design Pattern
 */

public class VhdlExpressionVisitor implements ExpressionVisitor {
    
    public VhdlExpressionVisitor() {
        
    }
    
    
    /**
     * Visit the Expression Object and format.
     * 
     * @param x
     *            the Expression to format.
     * @return string representation of the expression.
     */
    public String visit(Expression x) {
        String R = "";
        
        if (x.getDenominator() != 1) {
            R += "(";
        }
        if (x.isZero() == false) {
            Iterator i = x.iterator();
            boolean first = true;
            while (i.hasNext()) {
                LinTerm term = (LinTerm) i.next();
                if (term.isPositive() && !first) {
                    R += " + ";
                }
                first = false;
                R += term.accept(this);
            }
        } else {
            R += "0";
        }
        if (x.getDenominator() != 1) {
            assert(x.getDenominator() > 0);
            // Assuming ceiled division
            R += " +" + (x.getDenominator()-1) + ")/" + x.getDenominator();
        }
        
        return R;
    }
    
    
    
    /**
     * Visit the Expression Object and format.
     * 
     * @param x
     *            the Expression to format.
     * @return string representation of the expression.
     */
    public String visit(Expression x, IndexVector indexVector, int flag) {
        
        String R = "";
        _indexVector = indexVector;
        _flag = flag;
        
        if (x.getDenominator() != 1) {
            R += "(";
        }
        if (x.isZero() == false) {
            Iterator i = x.iterator();
            boolean first = true;
            while (i.hasNext()) {
                LinTerm term = (LinTerm) i.next();
                if (term.isPositive() && !first) {
                    R += " + ";
                }
                first = false;
                R += term.accept(this);
            }
        } else {
            R += "0";
        }
        if (x.getDenominator() != 1) {
            assert(x.getDenominator() > 0);
            // XXX DIRT ALERT: we choose between ceil/floor by looking at the "flag"; this flag is intended to switch between
            // reg/non-reg iterator names, and not necessarily between lower/upper bound.
            if (flag == 0) {
                // Assuming ceiled division
                R += " +" + (x.getDenominator()-1) + ")/" + x.getDenominator();
            }
            else {
                // Assuming floored division
                R += ")/" + x.getDenominator();
            }
        }
        
        return R;
    }
    
    
    /**
     * Visit the Ceil Term Object and format.
     *
     * @param x
     *            the Ceil Term to format.
     * @return string representation of the ceil term.
     */
    public String visit(CeilTerm x) {
        String R = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "ceil(" + x.getExpression().accept(this) + ")";
            } else {
                R += x.getNumerator() + "*ceil("
                    + x.getExpression().accept(this) + ")";
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*ceil("
                + x.getExpression().accept(this) + ")";
        }
        return R;
    }
    
    /**
     * Visit the Div Term Object and format.
     *
     * @param x
     *            the Div Term to format.
     * @return string representation of the div term.
     */
    public String visit(DivTerm x) {
        String R = "";
        
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "(" + x.getExpression().accept(this) + ")/"
                    + x.getDivider();
            } else {
                R += x.getNumerator() + "*("
                    + x.getExpression().accept(this) + ")/" + x.getDivider();
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*("
                + x.getExpression().accept(this) + ")/" + x.getDivider();
        }
        return R;
    }
    
    /**
     * Visit the Floor Term Object and format.
     * 
     * @param x
     *            the Floor Term to format.
     * @return string representation of the floor term.
     */
    public String visit(FloorTerm x) {
        String R = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "floor(" + x.getExpression().accept(this) + ")";
            } else {
                R += x.getNumerator() + "*floor("
                    + x.getExpression().accept(this) + ")";
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*floor("
                + x.getExpression().accept(this) + ")";
        }
        return R;
    }
    
    /**
     * Visit the Linear Term Object and format.
     * 
     * @param x
     *            the Linear Term to format.
     * @return string representation of the linear term
     */
    
    
    public String visit(LinTerm x) {
        String s = "";
        String name = x.getName();
        
        // System.out.println("linterm name "+ name);
        
        // System.out.println("linterm " + x.toString());
        if(name == ""){
            s = x.toString();
        }
        else{
            
            Iterator iterParamNames = _indexVector.getParameterVectorNames().iterator();
            while(iterParamNames.hasNext()){
                String paramName = (String) iterParamNames.next();
                if(paramName.equals(name)){
                    s = x.toString().replaceAll(name, "sl_" + name);
                }
            }
            
            Iterator iterIndexNames = _indexVector.getIterationVector().iterator();
            while(iterIndexNames.hasNext()){
                String indexName = (String) iterIndexNames.next();
                if(indexName.equals(name)){
                    if(_flag ==0){
                        s = x.toString().replaceAll(name, "sl_loop_" + name);
                    }
                    else{
                        s = x.toString().replaceAll(name, "sl_loop_" + name + "_rg");
                    }
                    
                }
            }
            
            
            // System.out.println("static control vector's size is " + _indexVector.getStaticCtrlVector().size());
            // System.out.println("Dynamic control vector's size is " + _indexVector.getDynamicCtrlVector().size());
            Iterator iterCtrls = _indexVector.getStaticCtrlVector().iterator();
            while(iterCtrls.hasNext()){
                ControlExpression ctrlEx = (ControlExpression) iterCtrls.next();
                //System.out.println("control vector " + ctrlEx.toString());
                
                if(ctrlEx.getName().equals(name)){
                    String ctrl = ctrlEx.getExpression().accept(this);
                    //System.out.println("cntrol expression is " + ctrl);
                    s = x.toString().replaceAll(name, ctrl);
                }
            }
            
        }
        
        // System.out.println("replaced linter " + s); 
        
        return s;
    }
    
    /**
     * Visit the Maximum Term Object and format.
     * 
     * @param x
     *            the Maximum Term to format.
     * @return string representation of the maximum term.
     */
    public String visit(MaximumTerm x) {
        String R = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "max(" + x.getExpressionOne().accept(this) + ","
                    + x.getExpressionTwo().accept(this) + ")";
            } else {
                R += x.getNumerator() + "*max("
                    + x.getExpressionOne().accept(this) + ","
                    + x.getExpressionTwo().accept(this) + ")";
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*max("
                + x.getExpressionOne().accept(this) + ","
                + x.getExpressionTwo().accept(this) + ")";
        }
        return R;
    }
    
    /**
     * Visit the Minimum Term Object and format.
     * 
     * @param x
     *            the Minimum Term to format.
     * @return string representation of the minimum term.
     */
    public String visit(MinimumTerm x) {
        String R = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "min(" + x.getExpressionOne().accept(this) + ","
                    + x.getExpressionTwo().accept(this) + ")";
            } else {
                R += x.getNumerator() + "*min("
                    + x.getExpressionOne().accept(this) + ","
                    + x.getExpressionTwo().accept(this) + ")";
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*min("
                + x.getExpressionOne().accept(this) + ","
                + x.getExpressionTwo().accept(this) + ")";
        }
        return R;
    }
    
    /**
     * Visit the Mod Term Object and format.
     * 
     * @param x
     *            the Mod Term to format.
     * @return string representation of the mod term.
     */
    public String visit(ModTerm x) {
        String R = "";
        if (x.getDenominator() == 1) {
            if (x.getNumerator() == 1) {
                R += "mod(" + x.getExpression().accept(this) + ","
                    + x.getDivider() + ")";
            } else {
                R += x.getNumerator() + "*mod("
                    + x.getExpression().accept(this) + "," + x.getDivider()
                    + ")";
            }
        } else {
            R += x.getNumerator() + "/" + x.getDenominator() + "*mod("
                + x.getExpression().accept(this) + "," + x.getDivider()
                + ")";
        }
        return R;
    }
    
    
///////////////////////////////////////////////////////////////////
//  // private variables /// 
    private  IndexVector _indexVector = null;
    
    private int _flag;   // 0 for iterator value before register, 1 for iterator value after register
    
}



