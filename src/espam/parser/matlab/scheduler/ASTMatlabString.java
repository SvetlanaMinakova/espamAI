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

package espam.parser.matlab.scheduler;

/**
 *  Description of the Class
 *
 * @author Todor Stefanov
 */
public class ASTMatlabString extends SimpleNode {

    /**
     *  Constructor for the ASTMatlabString object
     *
     * @param  id Description of the Parameter
     */
    public ASTMatlabString(int id) {
        super(id);
    }


    /**
     *  Constructor for the ASTMatlabString object
     *
     * @param  p Description of the Parameter
     * @param  id Description of the Parameter
     */
    public ASTMatlabString(Parser p, int id) {
        super(p, id);
    }


    /**
     *  Gets the name attribute of the ASTMatlabString object
     *
     * @return  The name value
     */
    public String getName() {
        return _name;
    }


    /**
     *  Sets the name attribute of the ASTMatlabString object
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }


    private String _name;
}
