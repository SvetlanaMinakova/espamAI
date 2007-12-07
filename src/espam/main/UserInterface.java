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

package espam.main;

import java.net.URL;
import java.util.Map;


/**
 * Description of the Class
 *
 * @author Todor Stefanov
 */
public class UserInterface {

    ///////////////////////////////////////////////////////////////////
    //// public methods ////

    /**
     * returns the singleton instance of this class.
     *
     * @return The instance value
     */
    public final static UserInterface getInstance() {
        return _instance;
    }

    /**
     * Return the Copyright string;
     *
     * @return the copyright string.
     */
    public final String getCopyright() {
        String copyright = "";
        copyright += "Copyright (c) 2004-2006 Leiden University " + "(LERC group at LIACS)\n";
        copyright += "use '--copyright' to get the complete copyright statement\n";
        return copyright;
    }

    /**
     * Get the name of the platform file.
     *
     * @return The platformFileName value
     */
    public final String getPlatformFileName() {
        return _platformFileName;
    }

    /**
     * Set the name of the platform file.
     *
     * @param platformFileName
     *            The new platformFileName value
     */
    public final void setPlatformFileName(String platformFileName) {
        _platformFileName = platformFileName;
    }

    /**
     * Get the name of the network file.
     *
     * @return The networkFileName value
     */
    public final String getNetworkFileName() {
        return _networkFileName;
    }

    /**
     * Set the name of the network file.
     *
     * @param networkFileName
     *            The new networkFileName value
     */
    public final void setNetworkFileName(String networkFileName) {
        _networkFileName = networkFileName;
    }


    /**
     * Get the name of the ADG file.
     *
     * @return The adgFileName value
     */
    public final String getADGFileName() {
        return _adgFileName;
    }

    /**
     * Set the name of the ADG file.
     *
     * @param adgFileName
     *            The new adgFileName value
     */
    public final void setADGFileName(String adgFileName) {
        _adgFileName = adgFileName;
    }

    /**
     * Get the name of the mapping file.
     *
     * @return The mappingFileName value
     */
    public final String getMappingFileName() {
        return _mappingFileName;
    }

    /**
     * Set the name of the mapping file.
     *
     * @param mappingFileName
     *            The new mappingFileName value
     */
    public final void setMappingFileName(String mappingFileName) {
        _mappingFileName = mappingFileName;
    }


    /**
     * Get the name of the scheduler file.
     *
     * @return The schedulerFileName value
     */
    public final String getSchedulerFileName() {
        return _schedulerFileName;
    }

    /**
     * Set the name of the scheduler file.
     *
     * @param schedulerFileName
     *            The new schedulerFileName value
     */
    public final void setSchedulerFileName(String schedulerFileName) {
        _schedulerFileName = schedulerFileName;
    }
    
    /**
     * Get the path of the xps library file.
     *
     * @return The xpsLibPath value
     */
    public final String getXpsLibPath() {
        return _xpsLibPath;
    }

    /**
     * Set the path of the xps library file.
     *
     * @param xpsLibPath
     *            The new xpsLibPath value
     */
    public final void setXpsLibPath(String xpsLibPath) {
        _xpsLibPath = xpsLibPath;
    }


    /**
     * Get the status of the Verbose flag
     *
     * @return The verboseFlag value
     */
    public final boolean getVerboseFlag() {
        return _verbose;
    }

    /**
     * Sets the Verbose flag
     */

    public final void setVerboseFlag() {
        _verbose = true;
    }

    /**
     * Get the status of the decompose flag
     *
     * @return The decomposeFlag value
     */
    public final boolean getDecomposeFlag() {
        return _decompose;
    }

    /**
     * Sets the decompose flag
     */

    public final void setDecomposeFlag() {
        _decompose = true;
    }

    /**
     * Get the status of the size flag
     *
     * @return The sizeFlag value
     */
    public final boolean getSizeFlag() {
        return _size;
    }

    /**
     * Sets the size flag
     */

    public final void setSizeFlag() {
        _size = true;
    }

    /**
     * Get the status of the YAPI flag
     *
     * @return The yapiFlag value
     */
    public final boolean getYapiFlag() {
        return _yapi;
    }

    /**
     * Sets the YAPI flag
     */

    public final void setYapiFlag() {
        _yapi = true;
    }

    /**
     * Get the status of the YML flag
     *
     * @return The ymlFlag value
     */
    public final boolean getYmlFlag() {
        return _yml;
    }

    /**
     * Sets the YML flag
     */

    public final void setYmlFlag() {
        _yml = true;
    }

    /**
     * Get the status of the XPS flag
     *
     * @return The xpsFlag value
     */
    public final boolean getXpsFlag() {
        return _xps;
    }

    /**
     * Sets the XPS flag
     */

    public final void setXpsFlag() {
        _xps = true;
    }

    /**
     * Get the status of the Debug flag
     *
     * @return The debugFlag value
     */
    public final boolean getDebugFlag() {
        return _debug;
    }

    /**
     * Sets the debug flag
     */

    public final void setDebugFlag() {
        _debug = true;
    }
    
    /**
     * Get the status of the Debugger flag
     *
     * @return The debuggerFlag value
     */
    public final boolean getDebuggerFlag() {
        return _debugger;
    }

    /**
     * Sets the debugger flag
     */

    public final void setDebuggerFlag() {
        _debugger = true;
    }

    /**
     * Print a message to screen if the verbose flag has been selected with an
     * end-of-line.
     *
     * @param s
     *            description that needs to printed.
     */
    public void printVerbose(String s) {
        if ( getVerboseFlag() ) {
            System.out.print(s);
        }
    }

    /**
     * Print a message to screen if the verbose flag has been selected.
     *
     * @param s
     *            description that needs to printed.
     */
    public void printlnVerbose(String s) {
        if( getVerboseFlag() ) {
            System.out.println(s);
        }
    }

	/**
	 * DebugFuncion.
	 * 
	 * @param obj
	 *            Description of the Parameter
	 * @param e
	 *            Description of the Parameter
	 * @param message
	 *            Description of the Parameter
	 */
	public static void printException(Object obj, Exception e, String message) {
		if (obj != null) {
			System.out.println("In class: " + obj.getClass().getName());
		}
		System.out.println("Exception Occured: " + e.getMessage());
		System.out.println("Message: " + message);
	}

// stuff used in yapi visitor -----------------------------------------

	/**
	 * get the name of the output file name.
	 *
	 * @return The outputFileName value
	 */
	public final String getOutputFileName() {
		return _outputFileName;
	}

	/**
	 * set the name of the input file name.
	 *
	 * @param filename
	 *            The new outputFileName value
	 */
	public final void setOutputFileName(String filename) {
		_outputFileName = filename;
	}

	/**
	 * get the base path name.
	 *
	 * @return The basePath value
	 */
	public final String getBasePath() {
		return _basePath;
	}
	
	/**
	 * sets the base path name.
	 *
	 * @param name
	 *            The new basePath value
	 */
	public final void setBasePath(String name) {
		_basePath = name;
	}

	/**
	 * get the filename.
	 *
	 * @return The fileName value
	 */
	public final String getFileName() {
		return _fileName;
	}

	/**
	 * sets the Filename name.
	 *
	 * @param name
	 *            The new fileName value
	 */
	public final void setFileName(String name) {
		_fileName = name;
	}


    ///////////////////////////////////////////////////////////////////
    //// private methods ////

    /**
     * Constructor. Private since only a single version may exist.
     */
    private UserInterface() {

    }

    ///////////////////////////////////////////////////////////////////
    //// private variables ////

    /**
     * Get a single instance of the UserInterface object.
     */
    private final static UserInterface _instance = new UserInterface();

    // the platform file name
    private String _platformFileName = null;

    // the network file name
    private String _networkFileName = null;

    // the adg file name
    private String _adgFileName = null;

    // the mapping file name
    private String _mappingFileName = null;

    // the scheduler file name
    private String _schedulerFileName = null;

    // the path of the xps library file
    private String _xpsLibPath = "";

    // the verbose flag
    private boolean _verbose = false;

    // the yapi flag
    private boolean _yapi = false;

    // the yml flag
    private boolean _yml = false;

    // the xps flag
    private boolean _xps = false;

    // the size flag
    private boolean _size = false;

    // the decompose flag
    private boolean _decompose = false;

    // the debug flag
    private boolean _debug = false;

    //  the debugger flag
    private boolean _debugger = false;

    // the filename
    private String _fileName = "";

    // the basepath name
    private String _basePath = ".";

    // the filename
    private String _outputFileName = "";
}

