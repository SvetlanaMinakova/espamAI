/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2011 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.ise;

/**
 * ISE Visitor Configuration
 *
 * @author  Sven van Haastregt
 * @version  $Id: IseConfig.java,v 1.1 2012/02/23 16:12:37 svhaastr Exp $
 */
public class IseConfig {
  // Getters and setters for the ISE visitor options.
  // For more info on each option, see the private member variables below.

  public static boolean omitIONodes() {
    return _omitIONodes;
  }
  public static void setOmitIONodes(boolean omitIONodes) {
    _omitIONodes = omitIONodes;
  }

  public static boolean genTracing() {
    return _tbTracing;
  }
  public static void setTracing(boolean tbTracing) {
    _tbTracing = tbTracing;
  }

  public static boolean genSimul() {
    return _simul;
  }
  public static void setGenSimul(boolean simul) {
    _simul = simul;
  }

  public static boolean genSynth() {
    return _synth;
  }
  public static void setGenSynth(boolean synth) {
    _synth = synth;
  }

  public static int getResetHigh() {
    return _resetHigh;
  }
  public static void setResetHigh(int resetHigh) {
    _resetHigh = resetHigh;
  }



  ////////////////////////////////////////////////////////////////////
  //// Private member variables

  private static boolean _omitIONodes = false; // omit input and output nodes (only keep the transformer nodes of a network which have >= 1 input and >= 1 output port)
//  private boolean _omitIOEdges = false; // omit FIFOs connecting to input and output nodes (only keep the internal FIFOs of a network)
  private static boolean _tbTracing = false;  // Let simulation testbench handle input and output traces
  private static boolean _simul = true;   // Generate simulation files
  private static boolean _synth = !_simul;   // Make output suitable for synthesis
  private static int _resetHigh = 0;       // Active reset level
}
