/*******************************************************************\
  * 
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

/**
 * Holds metrics for a single implementation of a function.
 *
 * @author Sven van Haastregt
 */
public class Implementation {
    
    //////////////////////////////////////////////////////////////////////////
    //// Public enums
    
    /**
     * Processor type.
     */
    public enum Type {
        NONE,
            MICROBLAZE,
            POWERPC,
            LAURA
    }
    
    /**
     * Metric, such as latency or memory footprint.
     */
    public enum Metric {
        NONE,
            DELAY_AVG,               // Average delay (cycles)
            DELAY_WORST,             // Worst case delay (cycles)
            DELAY_BEST,              // Best case delay (cycles)
            II,                      // Initiation interval (cycles)
            SLICES,                  // Slice usage
            MEMORY_DATA,             // Data memory (bytes)
            MEMORY_CODE              // Code memory (bytes)
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    //// Public methods
    
    /**
     * Constructs an Implementation with given name and type.
     */
    public Implementation(String name, String type) {
        _name = name;
        _type = _string2Type(type);
        _metrics = new HashMap<Metric, Integer>();
    }
    
    /**
     * Constructs an Implementation with given name and type.
     */
    public Implementation(String name, Type type) {
        _name = name;
        _type = type;
    }
    
    /**
     * Adds metric,value pair.
     */
    public void addMetric(String metric, String value) {
        Metric m = _string2Metric(metric);
        int v = Integer.parseInt(value);
        setMetric(m, v);
    }
    
    /**
     * Returns name of implementation.
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Sets name of implementation.
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Returns type of implementation.
     */
    public Type getType() {
        return _type;
    }
    
    /**
     * Sets type of implementation.
     */
    public void setType(Type type) {
        _type = type;
    }
    
    /**
     * Returns value for metric m.
     */
    public int getMetric(Metric m) {
        if (!_metrics.containsKey(m)) {
            System.err.println("Warning: metric " + m + " for component '" + getName() + "' is not defined; using default value 1.");
            return 1;
        }
        else {
            return _metrics.get(m);
        }
    }
    
    /**
     * Sets value for metric m.
     */
    public void setMetric(Metric m, int value) {
        if (_metrics.put(m, value) != null) {
            System.err.println("Warning: multiple values given for component '" + getName() + "', metric " + m + ".");
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    //// Private methods
    
    /**
     * Converts string to Type.
     */
    private Type _string2Type(String s) {
        if (s.equals("MicroBlaze")) {
            return Type.MICROBLAZE;
        }
        else if (s.equals("PowerPC")) {
            return Type.POWERPC;
        }
        else if (s.equals("CompaanHWNode") || s.equals("LAURA")) {
            return Type.LAURA;
        }
        else {
            System.err.println("Unknown processor type '" + s + "'");
            return Type.NONE;
        }
    }
    
    
    /**
     * Converts string to Metric.
     */
    private Metric _string2Metric(String s) {
        if (s.equals("DELAY_AVG")) {
            return Metric.DELAY_AVG;
        }
        else if (s.equals("DELAY_WORST")) {
            return Metric.DELAY_WORST;
        }
        else if (s.equals("DELAY_BEST")) {
            return Metric.DELAY_BEST;
        }
        else if (s.equals("II")) {
            return Metric.II;
        }
        else if (s.equals("SLICES")) {
            return Metric.SLICES;
        }
        else if (s.equals("MEMORY_CODE")) {
            return Metric.MEMORY_CODE;
        }
        else if (s.equals("MEMORY_DATA")) {
            return Metric.MEMORY_DATA;
        }
        else {
            System.err.println("Unknown metric '" + s + "'");
            return Metric.NONE;
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    //// Private variables
    
    private String _name;
    private Type _type;
    private HashMap<Metric, Integer> _metrics;
}
