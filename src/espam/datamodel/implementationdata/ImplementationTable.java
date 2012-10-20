/*******************************************************************\

The ESPAM Software Tool
Copyright (c) 2004-2012 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.datamodel.implementationdata;

import java.util.HashMap;
import java.util.Vector;

/**
 * Stores a set of functions, and for each function one or more implementations.
 *
 * @author Sven van Haastregt
 */
public class ImplementationTable {

    //////////////////////////////////////////////////////////////////////////
    //// Public methods

    /**
     * Constructor.
     */
    public ImplementationTable() {
        _table = new HashMap<String, Vector<Implementation>>();
    }

    /**
     * Adds implementation to table.
     *
     * @param  funcName Name of function to which the implementation belongs.
     * @param  impl The actual implementation.
     */
    public void addImplementation(String funcName, Implementation impl) {
        Vector<Implementation> impls = _table.get(funcName);
        if (impls == null) {
            impls = new Vector<Implementation>();
            _table.put(funcName, impls);
        }
        impls.add(impl);
    }

    /**
     * Returns number of implementations for given function.
     */
    public int getEntryCount(String funcName) {
        return _table.get(funcName).size();
    }

    /**
     * Returns i-th implementation of given function.
     */
    public Implementation getEntry(String funcName, int i) {
        return _table.get(funcName).get(i);
    }

    /**
     * Returns metric m for i-th implementation of given function.
     */
    public int getMetric(Implementation.Metric m, String funcName, int i) {
        Vector<Implementation> implList = _table.get(funcName);
        if (implList == null || implList.size() < 1) {
            System.err.println("Warning: metric " + m + " for function '" + funcName + "' is not defined; using default value 1.");
            return 1;
        }
        else if (i >= implList.size()) {
            System.err.println("Warning: implementation " + i + " function '" + funcName + "' does not exist; using default value 1.");
            return 1;
        }
        else {
            return implList.get(i).getMetric(m);
        }
    }


    //////////////////////////////////////////////////////////////////////////
    //// Private members

    private HashMap <String, Vector<Implementation> > _table;
}
