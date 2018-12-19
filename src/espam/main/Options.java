
package espam.main;

import espam.main.cnnUI.DNNInitRepresentation;
import espam.main.cnnUI.UI;

import java.io.StringWriter;
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// Options
/**
 * This class handle the command line options. If checks if it is a valid
 * option, and if so, it calls the appropriate method in the UserInterface, that
 * reflect the global setting of ESPAM.
 *
 * @author Todor Stefanov
 * @version $Id: Options.java,v 1.11 2012/05/25 00:22:20 mohamed Exp $ $Name:  $
 */

public class Options {
    
    ////////////////////////////////////////////////////////////////////////
    //// public methods ////
    
    /**
     * Parse the command-line arguments, creating models as specified. Then
     * execute each model that contains a manager.
     *
     * @param args
     *            The command-line arguments.
     * @exception IllegalArgumentException
     *                MyException If such and such occurs
     * @throws IllegalArgumentException
     *             if an illegal argument is found on the command line.
     */
    public Options(String args[]) throws IllegalArgumentException, NumberFormatException {
        
        _ui = UserInterface.getInstance();
        _cnnui = UI.getInstance();

        if( args != null ) {
            _parseArgs(args);
        }
        
    }
    
    ////////////////////////////////////////////////////////////////////////
    //// protected methods ////
    
    /**
     * Parse the command-line arguments.
     *
     * @param args The arguments to be parsed
     *
     * @throws IllegalArgumentException
     *             if an illegal argument is found on the command line.
     */
    protected void _parseArgs(String args[]) throws IllegalArgumentException,
        NumberFormatException {
        if( args.length > 0 ) {
//          int cntrAdg = 0;
            for( int i = 0; i < args.length; i++ ) {
                String arg = args[i];
                if( _parseArg(arg) == false ) {
                    if( arg.startsWith("-") && i < args.length - 1 ) {
                        if( arg.equals("--platform") || arg.equals("-p") ) {
                            _ui.setPlatformFileName(args[++i]);
                        } else if( arg.equals("--kpn") || arg.equals("-k") ) {
                            _ui.setNetworkFileName(args[++i]);
                        } else if( arg.equals("--adg") || arg.equals("-a") ) {
                            _ui.setADGFileName(args[++i]);
                            //_ui.setADGFileName(args[++i], cntrAdg++);
                        } else if( arg.equals("--mapping") || arg.equals("-m") ) {
                            _ui.setMappingFileName(args[++i]);
                        } else if( arg.equals("--scheduler") || arg.equals("-s") ) {
                            _ui.setSchedulerFileName(args[++i]);
                        } else if( arg.equals("--impldata") ) {
                            _ui.setImplDataFileName(args[++i]);
                        } else if( arg.equals("--libxps") || arg.equals("-l") ) {
                            _ui.setXpsLibPath(args[++i]);
                        } else if( arg.equals("--libsdk")) {
                            _ui.setSDKLibPath(args[++i]);
                        } else if( arg.equals("--funcCodePath")) {
                            _ui.setFuncCodePath(args[++i]);                         
                        } else if( arg.equals("--libhdpc") ) {
                            _ui.setHdpcLibPath(args[++i]);
                        } else if( arg.equals("--libsystemc") ) {
                            _ui.setSystemcLibPath(args[++i]);
                        }
                        /** CNNESPAM options*/

                        if (arg.equals("--evaluate") || arg.equals("-e")) {
                           // _cnnui.evaluate(args[++i]);
                            _cnnui.setSrcPath(args[++i]);
                            _cnnui.setEval(true);
                        }
                        else if( arg.equals("--generate") || arg.equals("-g") ) {
                            _cnnui.setSrcPath(args[++i]);
                            _cnnui.setGenerate(true);
                        }
                        else if(arg.equals("--block-based")||arg.equals("-bb")){
                            try { _cnnui.setBlocks(Integer.parseInt(args[++i])); }
                            catch (Exception e){ System.err.println("Invalid blocks number"); }
                        }
                         else if(arg.equals("--split-step")){
                            try { _cnnui.setSplitChldrenNum(Integer.parseInt(args[++i])); }
                            catch (Exception e){ System.err.println("Invalid split step"); }
                        }
                         else if(arg.equals("--safe-counter")){
                            try { _cnnui.setSplitSafeCounter(Integer.parseInt(args[++i])); }
                            catch (Exception e){ System.err.println("Invalid split safe counter"); }
                        }

                        else if(arg.equals("--img-w")){
                            try { _cnnui.setImgW(Integer.parseInt(args[++i])); }
                            catch (Exception e){ System.err.println("Invalid split safe counter"); }
                        }

                        else if(arg.equals("--time-spec")){
                            try { _cnnui.setExecTimesSpec(args[++i]); }
                            catch (Exception e){ System.err.println("Invalid path to CSDF model time (wcet) specification"); }
                        }

                         else if(arg.equals("--energy-spec")){
                            try { _cnnui.setEnergySpec(args[++i]); }
                            catch (Exception e){ System.err.println("Invalid path to CSDF model energy specification"); }
                        }

                        else {
                            // Unrecognized option.
                            throw new IllegalArgumentException("Unrecognized option: " + arg);
                        }
                    } else {
                        // Unrecognized option.
                        throw new IllegalArgumentException("Unrecognized option: " + arg);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(_usage());
        }
    }
    
    /**
     * Parse a command-line argument.
     *
     * @param arg
     *            Description of the Parameter
     * @return True if the argument is understood, false otherwise.
     * @throws IllegalArgumentException
     *             if an illegal argument is found on the command line.
     */
    protected boolean _parseArg(String arg) throws IllegalArgumentException {
        if( arg.equals("--help") || arg.equals("-h") ) {
            //throw new IllegalArgumentException(_usage());
            System.out.println( _usage() );
            System.exit(0);
        } else if( arg.equals("--version") || arg.equals("-v") ) {
            System.out.println("ESPAM version 0.0.2, extended by cnn processing\n");
            System.exit(0);
        } else if( arg.equals("--copyright") ) {
            StringWriter writer = new StringWriter();
            Copyright.writeCopyright(writer);
            System.out.println( writer.toString() );
            System.exit(0);
        } else if( arg.equals("--verbose") || arg.equals("-V") ) {
            _ui.setVerboseFlag();
            _cnnui.setVerboseFlag(true);
        } else if( arg.equals("--yapi") || arg.equals("-Y") ) {
            _ui.setYapiFlag();
        } else if( arg.equals("--yml") || arg.equals("-M") ) {
            _ui.setYmlFlag();
        } else if( arg.equals("--xps") || arg.equals("-X") ) {
            _ui.setXpsFlag();
        } else if( arg.equals("--sdk")) {
            _ui.setSDKFlag();
        } else if( arg.equals("--ise") || arg.equals("-I") ) {
            _ui.setIseFlag();
        } else if( arg.equals("--ipxact") ) {
            _ui.setIpxactFlag();
        } else if( arg.equals("--systemc") ) {
            _ui.setScUntimedFlag();
        } else if( arg.equals("--systemc-timed") ) {
            _ui.setScTimedFlag();
        } else if( arg.equals("--sc-timed-period") ) {
            _ui.setScTimedPeriodFlag();
        } else if( arg.equals("--hdpc") ) {
            _ui.setHdpcFlag();
        } else if( arg.equals("--size") || arg.equals("-S") ) {
            _ui.setSizeFlag();
        } else if( arg.equals("--decompose") ) {
            _ui.setDecomposeFlag();
        } else if( arg.equals("--debug") ) {
            _ui.setDebugFlag();
        } else if( arg.equals("--debugger") ) {
            _ui.setDebuggerFlag();
        } else if( arg.equals("--dot-ppn") ) {
            _ui.setDotFlag();
        }

        /** CNNESPAM arguments*/
        else if (arg.equals("--in-csdf")) {
            _cnnui.setInCSDF(true);
            _cnnui.setInDnn(false);
        }

        else if (arg.equals("--in-dnn")) {
            _cnnui.setInDnn(true);
            _cnnui.setInCSDF(false);
        }

        else if (arg.equals("--layer-based") || arg.equals("-lb")) {
            _cnnui.setDnnInitRepresentation(DNNInitRepresentation.LAYERBASED);
        }
        else if (arg.equals("--neuron-based") || arg.equals("-nb")) {
            _cnnui.setDnnInitRepresentation(DNNInitRepresentation.NEURONBASED);
        }

        else if (arg.equals("--multiple-models")|| arg.equals("-m")) {
            _cnnui.setMultipleModels(true);
        }
        else if (arg.equals("--json")) {
            _cnnui.setJson(true);
        }
        else if (arg.equals("--dot")) {
            _cnnui.setdot(true);
        }
        else if (arg.equals("--json-csdf")) {
            _cnnui.setCsdfgJson(true);
            _cnnui.setGenerateCsdfg(true);
        }
        else if (arg.equals("--xml-csdf")) {
            _cnnui.setCsdfgXml(true);
            _cnnui.setGenerateCsdfg(true);
        }
        else if (arg.equals("--dot-csdf")) {
            _cnnui.setCsdfgdot(true);
            _cnnui.setGenerateCsdfg(true);
        }

        else if (arg.equals("--sesame")) {
            _cnnui.setSesame(true);
            _cnnui.setGenerateCsdfg(true);
        }

        else if( arg.equals("") ) {
            // Ignore blank argument.
        } else {
            // Argument not recognized.
            return false;
        }
        return true;
    }
    
    /**
     * Return a string summarizing the command-line arguments.
     *
     * @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + _commandTemplate + "\n\n"
            + "Options that take values:\n";
        
        int i;
        for( i = 0; i < _commandOptions.length; i++ ) {
            result += " " + _commandOptions[i][0] + "\tabbr["
                + _commandOptions[i][1] + " " + _commandOptions[i][2]
                + "]\n";
        }
        result += "\nespamAI options:\n";
        
        for( i = 0; i < _CNNcommandOptions.length; i++ ) {
            result += " " + _CNNcommandOptions[i][0] + "\tabbr["
                + _CNNcommandOptions[i][1] + " " + _CNNcommandOptions[i][2]
                + "]\n";
        }

        result += "\nBoolean flags:\n";
        for( i = 0; i < _commandFlags.length; i++ ) {
            result += " " + _commandFlags[i][0] + "\tabbr["
                + _commandFlags[i][1] + "]\n";
        }
        result += "\nespamAI flags:\n";
        
        for( i = 0; i < _CNNcommandFlags.length; i++ ) {
            result += " " + _CNNcommandFlags[i][0] + "\tabbr["
                + _CNNcommandFlags[i][1] + "]\n";
        }

        return result;
    }
    
    ////////////////////////////////////////////////////////////////////////
    //// protected variables ////
    
    /**
     * The command-line options that are either present or not. Give the full
     * name preceded with '--' and abbreviated version.
     */
    protected String _commandFlags[][] = {
        { "--help      ", "-h" },
        { "--copyright ", "none" },
        { "--version   ", "-v" },
        { "--verbose   ", "-V" },
        { "--yapi      ", "-Y" },
        { "--yml       ", "-M" },
        { "--xps       ", "-X" },
        { "--sdk       ", "none" },
        { "--ise       ", "-I" },
        { "--ipxact    ", "none" },
        { "--systemc   ", "none" },
        { "--systemc-timed", "none" },
        { "--hdpc      ", "none" },
        { "--dot-ppn   ", "none" },
        { "--debug     ", "none" },
        { "--debugger  ", "none" }};

        /**
     * command flags for cnn model processing
     */
    protected String _CNNcommandFlags[][] = {
            /**input model flags. By default, input model is a dnn model*/
            {"--in-dnn          ", "none"},
            {"--in-csdf         ", "none"},

            /** initial model representation flags*/
            {"--layer-based     ", "-lb "},
            {"--neuron-based    ", "-nb "},

            /** generation flags*/
            {"--multiple-models ", "-m"},
            {"--sesame          ", "none"},
            {"--dot             ", "none"},
            {"--dot-csdf        ", "none"},
            {"--json            ", "none"},
            {"--json-csdf       ", "none"},
            {"--xml-csdf        ", "none"},
    };
    
    /**
     * The command-line options that take arguments.
     */
    protected String _commandOptions[][] = {
        { "--platform ", "-p", "<FileName>" },
        { "--kpn      ", "-k", "<FileName>" },
        { "--adg      ", "-a", "<FileName>"},
        { "--mapping  ", "-m", "<FileName>" },
        { "--scheduler", "-s", "<FileName>" },
        { "--impldata ", "none", "<FileName>" },
        { "--libxps", "-l", "<LibraryPath>" },
        { "--libsdk", "none", "<LibraryPath>" },
        { "--funcCodePath", "none", "<FunctionalCodePath>" },
        { "--libhdpc", "none", "<LibraryPath>" },
        { "--libsystemc", "none", "<LibraryPath>" } };



     /**
     * The command-line options that take arguments for cnn model processing
     */
    protected String _CNNcommandOptions[][] = {
        {"--evaluate       ", "-e  ", " <FileDirectory>"},
        {"--generate       ", "-g  ", " <FileDirectory>"},
        /** initial model representation flags*/
        {"--block-based    ", "-bb ", " <Integer>"},
        {"--split-step     ", "none", " <Integer>"},
        {"--img-w          ", "none", " <Integer>"},
        {"--time-spec      ", "none", " <FilePath>"},
        {"--energy-spec    ", "none", " <FilePath>"}
       };

    /**
     * The form of the command line.
     */
    protected String _commandTemplate = "espam [ options ]";
    
    /**
     * The UserInterface object.
     */
    protected UserInterface _ui = null;
    /**
     * The cnn User interface object
     */
    protected UI _cnnui = null;
}
