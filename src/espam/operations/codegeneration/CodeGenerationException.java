
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
