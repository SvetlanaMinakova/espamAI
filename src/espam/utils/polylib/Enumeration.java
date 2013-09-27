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

// ////////////////////////////////////////////////////////////////////////
// // MatlabVisitor

/**
 * This class implements the Enumeration type used in the PolyLib.
 * 
 * @author Edwin Rypkema
 * @version $Id: Enumeration.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public class Enumeration {
    
    /**
     * Constructor for the Enumeration object
     */
    public Enumeration() {
    }
    
    /**
     * Construct an Enumeration for a particular polyhedron and Evalue.
     * 
     * @param d
     *            The polyhedron.
     * @param ev
     *            The evalue.
     */
    public Enumeration(Polyhedron d, EValue ev) {
        ValidityDomain = d;
        EV = ev;
    }
    
    /**
     * Add an enumeration.
     * 
     * @param enumerate
     *            Description of the Parameter
     */
    public void add(Enumeration enumerate) {
        tail.next = enumerate;
        // first-last in list points to last
        tail = enumerate;
        // first tail in list point to last in list
        enumerate.head = head;
        // all heads point to head of first in list
    }
    
    /**
     * cleans up the enumeration in a tail recursive manner.
     */
    public void clear() {
        if (next != null) {
            next.clear();
            next = null;
        }
        ValidityDomain.clear();
        ValidityDomain = null;
        EV.clear();
        EV = null;
    }
    
    /**
     * Get an iterator.
     * 
     * @return an iterator.
     */
    public Iterator domains() {
        return new Iterator() {
            public boolean hasNext() {
                return (item != null);
            }
            
            public java.lang.Object next() {
                Enumeration Tmp = item;
                item = item.next;
                return Tmp;
            }
            
            public void remove() {
            }
            
            Enumeration item = head;
        };
    }
    
    /**
     * Enumerate the validity domains and print out the soluion for each
     * validity domain.
     * 
     * @return Description of the Return Value
     */
    public String eToString() {
        int cnt = 0;
        String result = "";
        Iterator i = domains();
        while (i.hasNext()) {
            result += "Ehrhart(" + cnt + "): \n";
            result += (i.next()).toString();
            result += "Validity(" + cnt++ + "): \n";
            // result += ((Enumeration)i.next()).ValidityDomain.toString();
        }
        return result;
    }
    
    /**
     * Set enumaration.
     * 
     * @return The internal value
     */
    public Enumeration getInternal() {
        return _enumeration;
    }
    
    /**
     * Check whether this Enumeration is Zero.
     * 
     * @return true if enumeration is zero, otherwise false;
     */
    public boolean isZero() {
        if (toString().equals("0")) {
            return true;
        }
        return false;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    // FIXME: What does this do?
    /**
     * Set enumaration.
     * 
     * @param enumeration
     *            The new internal value
     */
    public void setInternal(Enumeration enumeration) {
        _enumeration = enumeration;
    }
    
    /**
     * @param parameter
     * @return A string.
     */
    public String toPolynomial(Vector parameter) {
        String aString = "";
        aString += EV.toPolynomial(parameter, "");
        return aString;
    }
    
    /**
     * @return A string.
     */
    public String toString() {
        // System.out.println(" Enum -- 1 -- ");
        // aString += ValidityDomain.toString();
        String aString = EV.toString(_paramVector);
        return aString;
    }
    
    /**
     * @param parameter
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public String toString(Vector parameter) {
        String aString = "";
        // aString += ValidityDomain.toString();
        aString = EV.toString(parameter);
        return aString;
    }
    
    /**
     */
    public EValue EV;
    
    // FIXME: This?
    /**
     */
    public Enumeration head = this;
    
    // FIXME: are these really public?
    /**
     */
    public Enumeration next = null;
    
    // /////////////////////////////////////////////////////////////////
    // // public variables ////
    
    /**
     */
    public Enumeration tail = this;
    
    /**
     */
    public Polyhedron ValidityDomain;
    
    /**
     * FIXME: Paer of setInternal. Can be removed?
     */
    private Enumeration _enumeration = null;
    
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    
    /**
     * the parameter decode vector used in toString.
     */
    private static Vector _paramVector = new Vector(11);
    
    static {
        _paramVector.addElement("P");
        _paramVector.addElement("Q");
        _paramVector.addElement("R");
        _paramVector.addElement("S");
        _paramVector.addElement("T");
        _paramVector.addElement("U");
        _paramVector.addElement("V");
        _paramVector.addElement("W");
        _paramVector.addElement("X");
        _paramVector.addElement("Y");
        _paramVector.addElement("Z");
    }
    
}
