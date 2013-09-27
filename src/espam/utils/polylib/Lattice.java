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

import espam.parser.matrix.JMatrixParser;

import espam.utils.symbolic.matrix.JMatrix;

// //////////////////////////////////////////////////////////////////////////////////
// Lattice

/**
 * This class is used for the equivalence with the Lattice type used in PolyLib.
 * 
 * @author Hui Li
 * @version $Id: Lattice.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public class Lattice extends JMatrix {
    
    /**
     * Constructor for the Lattice object
     */
    public Lattice() {
        super();
    }
    
    /**
     * Constructor for the Lattice object
     * 
     * @param nRows
     *            Description of the Parameter
     * @param nColumns
     *            Description of the Parameter
     */
    public Lattice(int nRows, int nColumns) {
        super(nRows, nColumns);
    }
    
    /**
     * Constructor for the Lattice object
     * 
     * @param aJMat
     *            Description of the Parameter
     */
    public Lattice(JMatrix aJMat) {
        super(aJMat);
    }
    
    /**
     * Constructor for the Lattice object
     * 
     * @param matlabString
     *            Description of the Parameter
     */
    public Lattice(String matlabString) {
        JMatrix M = null;
        try {
            M = JMatrixParser.getJMatrix(matlabString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setMatrix(M);
    }
    
    /**
     * Constructor for the Lattice object
     * 
     * @param type
     *            Description of the Parameter
     * @param nRows
     *            Description of the Parameter
     * @param nColumns
     *            Description of the Parameter
     */
    public Lattice(String type, int nRows, int nColumns) {
        super(type, nRows, nColumns);
    }
}
