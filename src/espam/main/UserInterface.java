
package espam.main;

import java.net.URL;
import java.util.Map;
import java.util.Vector;


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
        copyright += "Copyright (c) 2004-2008 Leiden University " + "(LERC group at LIACS)\n";
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
//    public final String getADGFileName() {
//        return _adgFileName;
//    }
    
    /**
     * Set the name of the ADG file.
     *
     * @param adgFileName
     *            The new adgFileName value
     */
//    public final void setADGFileName(String adgFileName) {
//        _adgFileName = adgFileName;
//    }
    
    
    /**
     * Set the name of the ADG file.
     *
     * @param adgFileName
     *            The new adgFileName value
     */
    public final void setADGFileName( String adgFileName ) {
        _adgFileNames.add(adgFileName);
    }
    
    /**
     * Get the name of the ADG file.
     *
     * @return The adgFileName value
     */
    public final String getADGFileName( int pos ) {
        return _adgFileNames.get(pos);
    }
    
    /**
     *  Get the adgFileNames.
     *
     * @return  the adg file names
     */
    public Vector getADGFileNames() {
        return _adgFileNames;
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
     * Get the name of the implData file.
     *
     * @return The implDataFileName value
     */
    public final String getImplDataFileName() {
        return _implDataFileName;
    }
    
    /**
     * Set the name of the implData file.
     *
     * @param implDataFileName
     *            The new implDataFileName value
     */
    public final void setImplDataFileName(String implDataFileName) {
        _implDataFileName = implDataFileName;
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
     * Get the path of the sdk template files
     *
     * @return The sdkLibPath value
     */
    public final String getSDKLibPath() {
        return _sdkLibPath;
    }
    
    /**
     * Set the path of the sdk library file.
     *
     * @param sdkLibPath
     *            The new sdkLibPath value
     */
    public final void setSDKLibPath(String sdkLibPath) {
        _sdkLibPath = sdkLibPath;
    }     
    
    /**
     * Get the path of the directory containing the functional code
     *
     * @return The funcCodePath value
     */
    public final String getFuncCodePath() {
        return _funcCodePath;
    } 
    
    /**
     * Set the path of the functional code directory
     *
     * @param funcCodePath
     *            The new funcCodePath value
     */
    public final void setFuncCodePath(String funcCodePath) {
        _funcCodePath = funcCodePath;
    }      
    
    /**
     * Get the path of the hdpc library file.
     *
     * @return The hdpcLibPath value
     */
    public final String getHdpcLibPath() {
        return _hdpcLibPath;
    }
    
    /**
     * Set the path of the hdpc library file.
     *
     * @param hdpcLibPath
     *            The new hdpcLibPath value
     */
    public final void setHdpcLibPath(String hdpcLibPath) {
        _hdpcLibPath = hdpcLibPath;
    }
    
    /**
     * Get the path of the systemc library file.
     *
     * @return The systemcLibPath value
     */
    public final String getSystemcLibPath() {
        return _systemcLibPath;
    }
    
    /**
     * Set the path of the systemc library file.
     *
     * @param systemcLibPath
     *            The new systemcLibPath value
     */
    public final void setSystemcLibPath(String systemcLibPath) {
        _systemcLibPath = systemcLibPath;
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
     * Get the status of the SDK flag
     *
     * @return The sdkFlag value
     */
    public final boolean getSDKFlag() {
        return _sdk;
    }
    
    /**
     * Sets the SDK flag
     */
    
    public final void setSDKFlag() {
        _sdk = true;
    }    
    
    /**
     * Get the status of the ISE flag
     *
     * @return The iseFlag value
     */
    public final boolean getIseFlag() {
        return _ise;
    }
    
    /**
     * Sets the ISE flag
     */
    
    public final void setIseFlag() {
        _ise = true;
    }
    
    /**
     * Get the status of the IP-XACT flag
     *
     * @return The ipxactFlag value
     */
    public final boolean getIpxactFlag() {
        return _ipxact;
    }
    
    /**
     * Sets the IP-XACT flag
     */
    
    public final void setIpxactFlag() {
        _ipxact = true;
    }
    
    /**
     * Get the status of the SystemC untimed flag
     *
     * @return The scUntimedFlag value
     */
    public final boolean getScUntimedFlag() {
        return _scUntimed;
    }
    
    /**
     * Sets the SystemC untimed flag
     */
    
    public final void setScUntimedFlag() {
        _scUntimed = true;
    }
    
    /**
     * Get the status of the SystemC timed flag
     *
     * @return The scTimedFlag value
     */
    public final boolean getScTimedFlag() {
        return _scTimed;
    }
    
    /**
     * Sets the SystemC timed flag
     */
    
    public final void setScTimedFlag() {
        _scTimed = true;
    }
    
    /**
     * Sets the SystemC timed flag (computing period)
     */
    
    public final void setScTimedPeriodFlag() {
        // set the flag for computing period only in the systemC simulation
        if ( _scTimed ){
            _scTimedPeriod = true;
        }
    }
    
    /**
     * Gets the SystemC timed flag (computing period)
     */
    
    public final boolean getScTimedPeriodFlag() {
        return _scTimedPeriod;
    }
    
    /**
     * Get the status of the HDPC flag
     *
     * @return The hdpcFlag value
     */
    public final boolean getHdpcFlag() {
        return _hdpc;
    }
    
    /**
     * Sets the HDPC flag
     */
    public final void setHdpcFlag() {
        _hdpc = true;
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
    public final boolean getDotFlag() {
        return _dot;
    }
    
    /**
     * Sets the debugger flag
     */
    
    public final void setDotFlag() {
        _dot = true;
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
    //private String _adgFileName = null;
    
    private Vector<String> _adgFileNames = new Vector<String>();
    
    // the mapping file name
    private String _mappingFileName = null;
    
    // the scheduler file name
    private String _schedulerFileName = null;
    
    // the implementation data file name
    private String _implDataFileName = null;
    
    // the path of the xps library file
    private String _xpsLibPath = "";
    
    // the path of the sdk template files
    private String _sdkLibPath = "";
    
    // the path of the directory containing the functional code
    private String _funcCodePath = ""; 
    
    // the path of the hdpc library file
    private String _hdpcLibPath = "";
    
    // the path of the systemc library file
    private String _systemcLibPath = "";
    
    // the verbose flag
    private boolean _verbose = false;
    
    // the yapi flag
    private boolean _yapi = false;
    
    // the yml flag
    private boolean _yml = false;
    
    // the xps flag
    private boolean _xps = false;
    
    // the sdk flag
    private boolean _sdk = false;
    
    // the ise flag
    private boolean _ise = false;
    
    // the IP-XACT flag
    private boolean _ipxact = false;
    
    // the SystemC untimed flag
    private boolean _scUntimed = false;
    
    // the SystemC timed flag
    private boolean _scTimed = false;
    
    // in case of SystemC timed, an option to compute period of processes (networks)
    private boolean _scTimedPeriod = false;
    
    // the hdpc flag
    private boolean _hdpc = false;
    
    // the size flag
    private boolean _size = false;
    
    // the decompose flag
    private boolean _decompose = false;
    
    // the debug flag
    private boolean _debug = false;
    
    //  the debugger flag
    private boolean _debugger = false;
    
    //  the dot flag
    private boolean _dot = false;
    
    // the filename
    private String _fileName = "";
    
    // the basepath name
    private String _basePath = ".";
    
    // the filename
    private String _outputFileName = "";
}

