
package espam.datamodel;

/**
 *  This class represent an run-time exception that indicates that an
 *  exception occured in ESPAM.
 *
 * @author  Todor Stefanov
 * @version  $Id: EspamException.java,v 1.1 2000/04/26 03:07:04 stefanov Exp
 *      $
 */

public class EspamException extends Exception {
    
    /**
     *  Construct a new exception.
     *
     * @param  s Description of the Parameter
     */
    public EspamException(String s) {
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
