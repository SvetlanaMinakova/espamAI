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

package espam.parser.xml.implementationdata;

import java.util.ArrayList;
import java.util.Stack;

import espam.datamodel.implementationdata.Implementation;
import espam.datamodel.implementationdata.ImplementationTable;

import org.xml.sax.Attributes;

//////////////////////////////////////////////////////////////////////////
//// Xml2ImplementationTable

/**
 * Parser helper functions to convert an XML file into an ImplementationTable.
 *
 * @author Sven van Haastregt
 */

public class Xml2ImplementationTable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Returns the singleton instance of this class.
     *
     * @return  the instance.
     */
    public final static Xml2ImplementationTable getInstance() {
        return _instance;
    }

    /**
     * Processes start of an implementationMetrics tag in the XML.
     *
     * @param  attributes The attributes of the tag.
     * @return  an ImplementationTable object.
     */
    public Object processImplementationMetrics(Attributes attributes) {
        _impltable = new ImplementationTable();

        return null;
    }

    /**
     * Processes end of an implementationMetrics tag in the XML.
     */
    public void processImplementationMetrics(Stack<Object> stack) {
        stack.push((Object)_impltable);
    }


    /**
     * Processes start of a functions tag in the XML.
     *
     * @param  attributes The attributes of the tag.
     * @return  a platform object.
     */
    public Object processFunctions(Attributes attributes) {
        return null;
    }

    /**
     * Processes end of a functions tag in the XML.
     */
    public void processFunctions(Stack stack) {
    }


    /**
     * Processes start of a function tag in the XML.
     *
     * @param  attributes The attributes of the tag.
     * @return  the function name
     */
    public Object processFunction(Attributes attributes) {
        String name = (String) attributes.getValue("functionName");

        return name;
    }

    /**
     * Processes end of a function tag in the XML.
     */
    public void processFunction(Stack stack) {
        // Pop the function name
        stack.pop();
    }


    /**
     * Processes start of an implementation tag in the XML.
     *
     * @param  attributes The attributes of the tag.
     * @return an Implementation object.
     */
    public Object processImplementation(Attributes attributes) {
        String componentName = (String) attributes.getValue("componentName");
        String implementationType = (String) attributes.getValue("implementationType");
        Implementation impl = new Implementation(componentName, implementationType);

        return impl;
    }

    /**
     * Processes end of an implementation tag in the XML.
     */
    public void processImplementation(Stack stack) {
        // Pop the implementation and add it to the table.
        Implementation impl = (Implementation) stack.pop();
        String funcName = (String) stack.peek();
        _impltable.addImplementation(funcName, impl);
    }


    /**
     * Processes start of a metric tag in the XML.  This is a common routine for
     * all metric tags like ii, delay, slices, etc.
     *
     * @param  attributes The attributes of the tag.
     * @param  names List of names of the tags and the corresponding metrics.
     * @return array containing metric(s)
     */
    public Object processStartMetric(Attributes attributes, String[] names) {
        ArrayList<String> metrics = new ArrayList<String>();

        for (int i = 0; i < names.length; i+=2) {
            String metricName = names[i];
            String attrName = names[i+1];

            String value = (String) attributes.getValue(attrName);
            metrics.add(metricName);
            metrics.add(value);
        }

        return metrics;
    }


    /**
     * Processes end of a metric tag in the XML.  This is a common routine for
     * all metric tags like ii, delay, slices, etc.  Assumes the top element on
     * the stack is an array containing pairs of metrics, as produced by
     * processStartMetric().
     */
    public void processEndMetric(Stack stack) {
        @SuppressWarnings("unchecked")  // avoid type safety warning due to erasure of generics
        ArrayList<String> metric = (ArrayList<String>) stack.pop();

        Implementation impl = (Implementation) stack.peek();

        for (int i = 0; i < metric.size(); i+=2) {
            if (metric.get(i+1) != null) {
                // A value for the attribute was specified in the XML, so add the metric
                impl.addMetric(metric.get(i), metric.get(i+1));
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     *  Constructor that is private because only a single version has to
     *  exist.
     */
    private Xml2ImplementationTable() {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  Create a unique instance
     */
    private final static Xml2ImplementationTable _instance = new Xml2ImplementationTable();

    /**
     * The ImplementationTable that we are constructing.
     */
    private ImplementationTable _impltable;
}
