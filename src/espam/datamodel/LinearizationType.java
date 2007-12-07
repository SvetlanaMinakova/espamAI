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

package espam.datamodel;

import java.util.Vector;

/**
 * LinearizationType represents the four mutually exclusive
 * linearization types.  Each in turn is represented by an
 * object of this class.
 *
 * @author Sven Verdoolaege, Todor Stefanov
 */

public class LinearizationType {

    /** In-order without Multiplicity. */
    public static LinearizationType fifo;

    /** Out-of-order without Multiplicity. */
    public static LinearizationType GenericOutOfOrder;

    /** In-order with Multiplicity (general case defined by Alex Turjan).*/
    public static LinearizationType BroadcastInOrder;

    /** Out-of-order with Multiplicity.*/
    public static LinearizationType BroadcastOutOfOrder;

    /** In-order with Multiplicity (special case defined by Sven Verdoolaege).*/
    public static LinearizationType sticky_fifo;

    /** In-order (special case defined by Sven Verdoolaege).*/
    public static LinearizationType shift_register;

    /** Looks for a linearization type named name.
     *  Returns null if not found.
     *  @param name name of linearization type to look for
     *  @return the linerization type or null
     */
    public static LinearizationType find(String name) {
	for (int i = 0; i < _allTypes.size(); ++i) {
	    if (_allTypes.elementAt(i)._name.equals(name)) {
		return _allTypes.elementAt(i);
	    }
	}
	return null;
    }

    /**
     *  Returns a description of the LinearizationType.
     *
     * @return  a description of the LinearizationType.
     */
    public String toString() {
        return _name;
    }

    private LinearizationType(String name) {
	_name = name;
	if (_allTypes == null) {
	    _allTypes = new Vector<LinearizationType>();
	}
	_allTypes.add(this);
    }

    private String _name;

    private static Vector<LinearizationType> _allTypes = null;

    static {
	_allTypes = new Vector<LinearizationType>();
	fifo = new LinearizationType("fifo");
	GenericOutOfOrder = new LinearizationType("GenericOutOfOrder");
	BroadcastInOrder = new LinearizationType("BroadcastInOrder");
	BroadcastOutOfOrder = new LinearizationType("BroadcastOutOfOrder");
	sticky_fifo = new LinearizationType("sticky_fifo");
	shift_register = new LinearizationType("shift_register");
    }
}
