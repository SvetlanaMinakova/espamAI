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
//  	       int cntrAdg = 0;
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
                        } else {
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
               System.out.println("ESPAM version 0.0.1\n");
               System.exit(0);
            } else if( arg.equals("--copyright") ) {
               StringWriter writer = new StringWriter();
               Copyright.writeCopyright(writer);
               System.out.println( writer.toString() );
               System.exit(0);
            } else if( arg.equals("--verbose") || arg.equals("-V") ) {
               _ui.setVerboseFlag();
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
            } else if( arg.equals("") ) {
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
		result += "\nBoolean flags:\n";
		for( i = 0; i < _commandFlags.length; i++ ) {
			result += " " + _commandFlags[i][0] + "\tabbr["
					+ _commandFlags[i][1] + "]\n";
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
	 * The command-line options that take arguments.
	 */
	protected String _commandOptions[][] = {
			{ "--platform ", "-p", "<FileName>" },
			{ "--kpn      ", "-k", "<FileName>" },
			{ "--adg      ", "-a", "<FileName>"},
			{ "--mapping  ", "-m", "<FileName>" },
			{ "--scheduler", "-s", "<FileName>" },
			{ "--libxps", "-l", "<LibraryPath>" },
			{ "--libsdk", "none", "<LibraryPath>" },
			{ "--funcCodePath", "none", "<FunctionalCodePath>" },
			{ "--libhdpc", "none", "<LibraryPath>" },
			{ "--libsystemc", "none", "<LibraryPath>" } };

	/**
	 * The form of the command line.
	 */
	protected String _commandTemplate = "espam [ options ]";

	/**
	 * The UserInterface object.
	 */
	protected UserInterface _ui = null;
}
