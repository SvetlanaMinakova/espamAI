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

package espam.operations.codegeneration;

/**
 *
 * @author  Todor Stefanov
 * @version  $Id: CodeGenerationException.java,v 1.1 2000/05/26 00:50:07
 *      kienhuis Exp $
 */

public class CodeGenerationException extends Exception {

    /**
     *  Construct a new exception.
     *
     * @param  s Description of the Parameter
     */
    public CodeGenerationException(String s) {
        _message = s;
    }


    /**
     *  Get the exception Message.
     *
     * @return  the exception message.
     */
    public String getMessage() {
        return _message;
    }


    /**
     *  The exception message.
     */
    private String _message;
}
