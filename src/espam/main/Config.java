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

package espam.main;


//////////////////////////////////////////////////////////////////////////
//// Config
/**
 * This class contains variables that are set by the configure script.
 * @author Sven van Haastregt
 */

public class Config {

  ////////////////////////////////////////////////////////////////////////
  //// public methods ////

  /**
   * Returns singleton instance of this class.
   */
  public final static Config getInstance() {
    return _instance;
  }

  /**
   * Returns path to SystemC installation.
   */
  public String getSystemCPath() {
    return _systemcPath;
  }

  /**
   * Returns path to DARTS installation.
   */
  public String getDartsPath() {
    return _dartsPath;
  }

  /**
   * Returns path to Python installation.
   */
  public String getPythonCall() {
    return _pythonCall;
  }

 /**
   * Returns path to project
   */
  public String getPrefix() {
    return _prefix;
  }

 /**
   * Returns path to output files.
   */
  public String getOutputDir() {
    return _outputDir;
  }
  /**
   * Set output directory
   * @param outputDir output directory
   */
  public void setOutputDir(String outputDir){
      _outputDir = outputDir;
  }

   /** get application absolute path*/
  public String getAppPath() { return _appPath; }

  ////////////////////////////////////////////////////////////////////////
  //// private methods ////

  /**
   * Private constructor.
   */
  private Config() {
  }


  ////////////////////////////////////////////////////////////////////////
  //// private members ////

  private final static Config _instance = new Config();

  // Path to SystemC simulation
  private String _systemcPath = "/vol/home/minakovas/systemc-2.2.0";

  // Path to DARTS
  private String _dartsPath = "/vol/home/minakovas/espam2/espam/lib/darts";

  // Path to Python
  private String _pythonCall = "/vol/home/minakovas/.pyenv/shims/python";

  // Path to the project
  private String _prefix = "/vol/home/minakovas/espam2/espam";

  // Output directory files
  private String _outputDir = "./output_models";

  //absolute path to the application sources
  private String _appPath = "/vol/home/minakovas/espam2/espam";
}
