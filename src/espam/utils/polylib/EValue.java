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

import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // EValue

/**
 * This class implements the evalue type used in the PolyLib.
 * 
 * @author Edwin Rypkema
 * @version $Id: EValue.java,v 1.1 2007/12/07 22:06:46 stefanov Exp $
 */

public class EValue {
    
    /**
     * FIXME, needs to be clone
     * 
     * @param newENode
     *            Description of the Parameter
     * @deprecated use Clone instead.
     */
    public EValue(ENode newENode) {
        d = 0;
        n = 0;
        // n is not used, because d is zero
        p = newENode;
    }
    
    /**
     * Construct an EValue with a particular numerator and denominator value.
     * 
     * @param denominator
     *            Description of the Parameter
     * @param numerator
     *            The numerator.
     */
    public EValue(int denominator, int numerator) {
        d = denominator;
        n = numerator;
        p = null;
        // p is not used because d is not zero
        // if d would have been zero the other constructor
        // would have been used.
    }
    
    // cleaning up the data structure
    /**
     * Description of the Method
     */
    public void clear() {
        if (d == 0) {
            p.clear();
        }
        p = null;
    }
    
    /**
     * Gets the d attribute of the EValue object
     * 
     * @return The d value
     */
    public int getD() {
        return d;
    }
    
    /**
     * Gets the n attribute of the EValue object
     * 
     * @return The n value
     */
    public int getN() {
        return n;
    }
    
    /**
     * Gets the p attribute of the EValue object
     * 
     * @return The p value
     */
    public ENode getP() {
        return p;
    }
    
    /**
     * Check whether this ENode is pseudo.
     * 
     * @return true if pseudo, false otherwise.
     */
    public boolean isPseudo() {
        boolean isPseudo = true;
        if (d == 0) {
            isPseudo = p.isPseudo();
        } else {
            isPseudo = false;
        }
        return isPseudo;
    }
    
    /**
     * return an ehrhart polynomial in Polylib format.
     * 
     * @param parameter
     * @return An ehrhart polynomial in PolyLib format.
     */
    public String toOneLineString(Vector parameter) {
        String aString = "";
        if (d == 0) {
            aString += p.toOneLineString(parameter);
        } else {
            if ((n != 0) && (d != 1)) {
                aString += n + "/" + d;
            } else {
                aString += n;
            }
        }
        return aString;
    }
    
    /**
     * return a sybolic polynomial in the canonical, distributive (expanded),
     * sparse, zero-represented representation.
     * 
     * @param parameter
     * @param coeff
     * @return a string.
     */
    public String toPolynomial(Vector parameter, String coeff) {
        String aString = "";
        if (d == 0) {
            aString += p.toPolynomial(parameter, coeff);
        } else {
            if (d != 1) {
                aString = coeff + n + "/" + d;
            } else {
                aString = coeff + n;
            }
        }
        return aString;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    /**
     * return an ehrhart polynomial in Polylib format.
     * 
     * @param parameter
     * @return An ehrhart polynomial in PolyLib format.
     */
    public String toString(Vector parameter) {
        String aString = "";
        if (d == 0) {
            aString += p.toString(parameter);
        } else {
            if ((n != 0) && (d != 1)) {
                aString += n + "/" + d;
            } else {
                aString += n;
            }
        }
        return aString;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    
    /**
     */
    private int d;
    
    /**
     */
    private int n;
    
    /**
     */
    private ENode p = null;
    
}
