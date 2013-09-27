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

// ////////////////////////////////////////////////////////////////////////
// // ParamDomain

/**
 * This class represents parameterized vertices in the Java programming language
 * and is used for the equivalence with the Param_Vertices type used in the
 * PolyLib. The class definitions are shown here, but are NOT yet implemented in
 * the interface.
 * 
 * @author Edwin Rypkema
 * @version $Id: ParamDomain.java,v 1.1 2007/12/07 22:06:48 stefanov Exp $
 */

public class ParamDomain {
    // ptr. to next domain in list, null terminated.
    Polyhedron domain;
    
    // ptr. to domain (constraints on parameters)
    boolean[] F;
    
    // /////////////////////////////////////////////////////////////////
    // // public variables ////
    
    ParamDomain next = null;
    // bit array of faces --> not used for Panda.
    
}
