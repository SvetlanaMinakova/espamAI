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

/**
 * This class represent an run-time exception that indicates that an exception
 * occured in Polylib.
 * 
 * @author Bart Kienhuis
 * @version $Id: PolylibException.java,v 1.1 2007/12/07 22:06:47 stefanov Exp $
 */

public class PolylibException extends Exception {
    
    /**
     * Construct a new exception.
     * 
     * @param s
     *            Description of the Parameter
     */
    public PolylibException(String s) {
        _message = s;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // public methods ///
    
    /**
     * Get the exception Message.
     * 
     * @return the exception message.
     */
    public String getMessage() {
        return _message;
    }
    
    // /////////////////////////////////////////////////////////////////
    // // private methods ///
    
    /**
     * The exception message.
     */
    private String _message;
}
