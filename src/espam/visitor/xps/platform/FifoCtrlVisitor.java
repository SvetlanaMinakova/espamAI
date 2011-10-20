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

package espam.visitor.xps.platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import espam.datamodel.EspamException;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.controllers.FifosController;
import espam.main.UserInterface;

import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Platform FifoCtrl Visitor

/**
 *  This class is a class for a visitor that is used to generate
 *  Fifo Controller pcore for Xps tool.
 *
 * @author  Wei Zhong
 * @version  $Id: FifoCtrlVisitor.java,v 1.3 2011/10/20 12:08:44 mohamed Exp $
 */

public class FifoCtrlVisitor extends PlatformVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *  Constructor for the MhsVisitor object
     */
    public FifoCtrlVisitor()
        throws FileNotFoundException,EspamException { 
    	
    	_ui = UserInterface.getInstance();

        if (_ui.getOutputFileName() == "") {
	    _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getFileName();
        } else {
	    _codeDir = _ui.getBasePath() + File.separatorChar + _ui.getOutputFileName();
        }
        
        //      create the subdirectories
    	_coreName = "fifo_if_ctrl";
    	_moduleName = _coreName + "_v1_00_a";
    	_moduleDir = "pcores" + File.separatorChar + _moduleName;
    	_paoFile = _coreName + "_v2_1_0" + ".pao";
    	_mpdFile = _coreName + "_v2_1_0" + ".mpd";
    	_hdlFile = _coreName + ".vhd";

    	_codeDir = _codeDir + File.separatorChar + _moduleDir;
    	File dir = new File(_codeDir);
    	dir.mkdirs();
    	dir = new File(_codeDir + File.separatorChar + _dataDir);
    	dir.mkdirs();
    	dir = new File(_codeDir + File.separatorChar + _devlDir);
    	dir.mkdirs();
    	dir = new File(_codeDir + File.separatorChar + _hdlDir);
    	dir.mkdirs();
    	
    }
    
    /**
     *  Print a Mhs file in the correct format for MHS.
     *
     * @param  x The platform that needs to be rendered.
     */
    public void visitComponent(Platform x) {
    	try{
    	    Iterator i;
    	    Resource resource;
            i = x.getResourceList().iterator();
            int numInPorts = 0;
            int numOutPorts = 0;
            while( i.hasNext() ) {
                resource = (Resource) i.next();
                if (resource instanceof FifosController) {
                	FifosController fifoCtrl = (FifosController) resource;
                	numInPorts = fifoCtrl.getNumberFifoReadPorts();
                	if (_maxInPorts < numInPorts) {
                		_maxInPorts = numInPorts;
                	}
                	numOutPorts = fifoCtrl.getNumberFifoWritePorts();
            		if (_maxOutPorts < numOutPorts) {
            			_maxOutPorts = numOutPorts;
            		}
                }
            }
            
            if (_maxOutPorts > 0 || _maxInPorts > 0) {
        	    _writeHdlFile();
        	    _writeMpdFile();
        	    _writePaoFile();	
            }
            
    	} catch (Exception e) {
                System.out.println(" In FifoCtrl Visitor: exception " +
    			       "occured: " + e.getMessage());
                e.printStackTrace();
    	}

    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ///

    /**
     *  write .vhd file
     */
    private void _writeHdlFile() throws FileNotFoundException {
	PrintStream hdlPS = _openFile(_hdlDir + File.separatorChar + _hdlFile);
	hdlPS.println("-- File automatically generated by ESPAM");
	hdlPS.println("");
	hdlPS.println("");
	int i;

	hdlPS.println("LIBRARY ieee;");
	hdlPS.println("USE ieee.std_logic_1164.ALL;");
	hdlPS.println("USE ieee.std_logic_arith.all;");
	hdlPS.println("USE ieee.std_logic_unsigned.all;");
	hdlPS.println("");
	hdlPS.println("LIBRARY proc_common_v3_00_a;");
	hdlPS.println("USE proc_common_v3_00_a.pselect;");
	hdlPS.println("");
	hdlPS.println("ENTITY " + _coreName + " IS");

	hdlPS.println("  GENERIC (");
	hdlPS.print("    C_HIGHADDR     : STD_LOGIC_VECTOR(0 to 31) := X\"");
	hdlPS.println(_digitToStringHex(8, 8) + "\";");
	hdlPS.print("    C_BASEADDR     : STD_LOGIC_VECTOR(0 to 31) := X\"");
	hdlPS.println(_digitToStringHex(0, 8) + "\";");
	hdlPS.println("    C_AB           : INTEGER                   := 8;");
	hdlPS.println("    C_LMB_AWIDTH   : INTEGER                   := 32;");
	hdlPS.println("    C_LMB_DWIDTH   : INTEGER                   := 32;");
	hdlPS.println("    C_FIFO_WRITE   : INTEGER                   := 0;");
	hdlPS.println("    C_FIFO_READ    : INTEGER                   := 1");
	hdlPS.println("    );");
	hdlPS.println("  PORT (");
	hdlPS.println("    LMB_Clk : IN STD_LOGIC := '0';");
	hdlPS.println("    LMB_Rst : IN STD_LOGIC := '0';");
	hdlPS.println("    ");
	hdlPS.println("    -- LMB Bus");
	hdlPS.println("    LMB_ABus        : IN  STD_LOGIC_VECTOR(0 TO C_LMB_AWIDTH-1);");
	hdlPS.println("    LMB_WriteDBus   : IN  STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);");
	hdlPS.println("    LMB_AddrStrobe  : IN  STD_LOGIC;");
	hdlPS.println("    LMB_ReadStrobe  : IN  STD_LOGIC;");
	hdlPS.println("    LMB_WriteStrobe : IN  STD_LOGIC;");
	hdlPS.println("    LMB_BE          : IN  STD_LOGIC_VECTOR(0 TO (C_LMB_DWIDTH/8 - 1));");
	hdlPS.println("    Sl_DBus      : OUT STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);");
	hdlPS.print("    Sl_Ready     : OUT STD_LOGIC");
	if ( _maxOutPorts > 0 || _maxInPorts > 0)
	    hdlPS.print(";");

	hdlPS.println("");

	// Here we we must pre-set all the port with the max-ports of processors
	hdlPS.println("    -- ports to FIFO writing interface");
	for (i = 1; i <= _maxOutPorts; i++) {
	    hdlPS.println("    WR_Control_" + i + " : OUT  STD_LOGIC;");
	    hdlPS.println("    WR_Clk_" + i + " : OUT  STD_LOGIC;");
	    hdlPS.println("    WR_EN_" + i +"    : OUT  STD_LOGIC;    ");
	    hdlPS.println("    WR_Data_" + i + "  : OUT STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);");
	    hdlPS.print("    Full_" + i + "     : IN  STD_LOGIC");
	    if (i < _maxOutPorts) hdlPS.println(";"); 
	    else {
		if (_maxInPorts > 0) hdlPS.println(";");
	    }
	    hdlPS.println("");
	}

	hdlPS.println("    -- ports to FIFO reading interface");
	for (i = 1; i <= _maxInPorts; i++) {
	    hdlPS.println("    RD_Control_" + i + " : OUT  STD_LOGIC;");
	    hdlPS.println("    RD_Clk_" + i + " : OUT  STD_LOGIC;");

	    hdlPS.println("    RD_EN_" + i + "	: OUT  STD_LOGIC;");
	    hdlPS.println("    RD_Data_" + i + "  : IN STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);");
	    hdlPS.print("    Exists_" + i + "    : IN  STD_LOGIC");
	    if (i < _maxInPorts) hdlPS.println(";");
	    hdlPS.println("");
	}


	hdlPS.println("    );");
	hdlPS.println("");
	hdlPS.println("END " + _coreName + ";");
	hdlPS.println("");

	hdlPS.println("ARCHITECTURE imp OF " + _coreName + " IS");
	hdlPS.println("");
	hdlPS.println("-- component declarations");
	hdlPS.println("COMPONENT pselect IS");
	hdlPS.println("  GENERIC (");
	hdlPS.println("    C_AW   : INTEGER                   := 32;");
	hdlPS.println("    C_BAR  : STD_LOGIC_VECTOR(0 TO 31);");
	hdlPS.println("    C_AB   : integer                   := 8);");
	hdlPS.println("  PORT (");
	hdlPS.println("    A      : in  STD_LOGIC_VECTOR(0 TO 31);");
	hdlPS.println("    CS     : out STD_LOGIC;");
	hdlPS.println("    AValid : in  STD_LOGIC);");
	hdlPS.println("END COMPONENT;");
	hdlPS.println("");

	hdlPS.println("-- internal signals");
	hdlPS.println("signal lmb_select   : STD_LOGIC;");
	hdlPS.println("");

	if (_maxOutPorts > 0) {
	hdlPS.println("SIGNAL  sl_wr_en   : STD_LOGIC_VECTOR(0 TO " + (_maxOutPorts-1) + ");");
	hdlPS.println("SIGNAL  sl_wr_data : STD_LOGIC_VECTOR(0 TO "+(_maxOutPorts*32-1)+");");
	hdlPS.println("SIGNAL  sl_full    : STD_LOGIC_VECTOR(0 TO " + (_maxOutPorts-1) + ");");
	hdlPS.println("");
	}

	if (_maxInPorts > 0) {
	hdlPS.println("SIGNAL  sl_rd_en	  : STD_LOGIC_VECTOR(0 TO " + (_maxInPorts-1) + ");");
	hdlPS.println("SIGNAL  sl_rd_data : STD_LOGIC_VECTOR(0 TO "+(_maxInPorts*32-1)+");");
	hdlPS.println("SIGNAL  sl_empty   : STD_LOGIC_VECTOR(0 TO " + (_maxInPorts-1) + ");");
	hdlPS.println("");
	}

	hdlPS.println("SIGNAL  sl_rdy_rd    : STD_LOGIC;");
	hdlPS.println("SIGNAL  sl_address   : STD_LOGIC_VECTOR(0 TO 11);");
	hdlPS.println("SIGNAL  sl_address_8 : STD_LOGIC;");
	hdlPS.println("");

	hdlPS.println("BEGIN  -- architecture imp");
	hdlPS.println("");

	for (i = 1; i <= _maxOutPorts; i++) {
	    hdlPS.println("  WR_Control_" + i + " <= '0';");
	    hdlPS.println("  WR_Clk_" + i + " <= LMB_Clk;");
	    hdlPS.println("  WR_EN_" + i + " <= sl_wr_en(" + (i-1) + ");");
	    hdlPS.println("  WR_Data_" + i + " <= sl_wr_data("+ ((i-1)*32)+" to "+ ((i*32)-1) +");");
	    hdlPS.println("  sl_full(" + (i-1) + ") <= Full_" + i + ";");
	    hdlPS.println("");
	}
	hdlPS.println("");

	for (i = 1; i <= _maxInPorts; i++) {
	    hdlPS.println("  RD_Clk_" + i + " <= LMB_Clk;");
	    hdlPS.println("  RD_EN_" + i + " <= sl_rd_en(" + (i-1) + ");");
	    hdlPS.println("  sl_rd_data(" + ((i-1)*32)+ " to " + ((i*32)-1) + ") <= RD_Data_" + i + ";");
	    hdlPS.println("  sl_empty(" + (i-1) + ") <= not Exists_" + i + ";");
	    hdlPS.println("");
	}
	hdlPS.println("");

	hdlPS.println("  -- Handling the FIFO writing interface");
	if ( _maxOutPorts > 0) {
	hdlPS.println("  G_1 : for I in 0 to C_FIFO_WRITE-1 generate  ");
	hdlPS.println("    sl_wr_en(I) <= '1' when lmb_select = '1' and LMB_ABus(8) = '1' and LMB_ABus(18 to 29) = CONV_STD_LOGIC_VECTOR(2*I, 12) and LMB_WriteStrobe = '1'");
	hdlPS.println("                   else '0';");
	hdlPS.println("    sl_wr_data(I*32 to I*32 + 31) <= LMB_WriteDBus;");
	hdlPS.println("  end generate;");
	hdlPS.println("");
	}

	hdlPS.println("  -- Handling the FIFO reading interface");
	if (_maxInPorts > 0) {
	hdlPS.println("  G_2 : for I in 0 to C_FIFO_READ - 1 generate");
//	hdlPS.println("    sl_rd_en(I) <= '1' when lmb_select = '1' and LMB_ABus(8) = '0' and LMB_ABus(18 to 29) = CONV_STD_LOGIC_VECTOR(2*I, 12) and LMB_ReadStrobe = '1'");
	hdlPS.println("    sl_rd_en(I) <= '1' when sl_address_8 = '0' and sl_address = CONV_STD_LOGIC_VECTOR(2*I, 12) and sl_rdy_rd = '1'");
	hdlPS.println("                       else '0';");
	hdlPS.println("  end generate;");
	hdlPS.println("");
	}

	hdlPS.println("  readMUX_MB: process (LMB_ABus) is");
	hdlPS.println("  begin ");
//	hdlPS.println("    if (LMB_ABus(8) = '1') then");
	hdlPS.println("    if (sl_address_8 = '1' and sl_rdy_rd = '1') then");
	if (_maxOutPorts > 0) {
//		hdlPS.println("      case LMB_ABus(18 to 29) is");
		hdlPS.println("      case sl_address is");
		for (i = 0; i < _maxOutPorts; i++) {
		    String s = _digitToStringHex( i*2 + 1, 3);
		    hdlPS.println("        when X\"" + s + "\" => Sl_DBus(31) <= sl_full(" + i + ");");
		    hdlPS.println("                         Sl_DBus(0 to 30) <= (others => '0');");
		}
		hdlPS.println("        when others =>   Sl_DBus <= X\"a5a5a5a5\";");
		hdlPS.println("      end case;");
	} else {
		hdlPS.println("      Sl_DBus <= X\"a5a5a5a5\";");
	}
	hdlPS.println("    else");
	if (_maxInPorts > 0) {
//		hdlPS.println("      case LMB_ABus(18 to 29) is");
		hdlPS.println("      case sl_address is");
		for (i = 0; i < _maxInPorts; i++) {
		    String s = _digitToStringHex( i*2 + 1, 3);
		    String ss = _digitToStringHex( i*2, 3);
		    hdlPS.println("        when X\"" + ss + "\" => Sl_DBus <= sl_rd_data(" + (i*32) + " to " + ((i+1)*32 -1) + ");");
		    hdlPS.println("        when X\"" + s + "\" => Sl_DBus(31) <= sl_empty(" + i + ");");
		    hdlPS.println("                         Sl_DBus(0 to 30) <= (others => '0');");
		}
		hdlPS.println("        when others =>   Sl_DBus <= X\"a5a5a5a5\";");
		hdlPS.println("      end case;");
	} else {
		hdlPS.println("      Sl_DBus <= X\"a5a5a5a5\";");
	}
	hdlPS.println("    end if;");
	hdlPS.println("  end process readMUX_MB;");
	hdlPS.println("");
	hdlPS.println("");

	hdlPS.println("  -- Handling the LMB bus interface");
	hdlPS.println("  Ready_Handling : PROCESS (LMB_Clk, LMB_Rst) IS");
	hdlPS.println("  BEGIN  -- PROCESS Ready_Handling");
	hdlPS.println("    IF (LMB_Rst = '1') THEN");
	hdlPS.println("      sl_rdy_rd <= '0';");
	hdlPS.println("      Sl_Ready <= '0';");
	hdlPS.println("      sl_address <= (others=>'0');");
	hdlPS.println("      sl_address_8 <= '0';");
	hdlPS.println("    ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN");
	hdlPS.println("      sl_rdy_rd  <= LMB_ReadStrobe AND lmb_select;");
	hdlPS.println("      Sl_Ready  <= LMB_AddrStrobe AND lmb_select;");
	hdlPS.println("      IF( LMB_AddrStrobe = '1' ) THEN -- delay the address (needed for reading)");
	hdlPS.println("      -- 12-bit address, the 2 LSBits are 'byte select'");
	hdlPS.println("         sl_address <= LMB_ABus(18 to 29);");
	hdlPS.println("         sl_address_8 <= LMB_ABus(8);");
	hdlPS.println("      END IF;");
	hdlPS.println("    END IF;");
	hdlPS.println("  END PROCESS Ready_Handling;");
	hdlPS.println("");

	hdlPS.println("  -- Do the LMB address decoding");
	hdlPS.println("  pselect_lmb : pselect");
	hdlPS.println("  generic map (");
	hdlPS.println("    C_AW   => LMB_ABus'length,");
	hdlPS.println("    C_BAR  => C_BASEADDR,");
	hdlPS.println("    C_AB   => C_AB)");
	hdlPS.println("  port map (");
	hdlPS.println("    A      => LMB_ABus,");
	hdlPS.println("    CS     => lmb_select,");
	hdlPS.println("    AValid => LMB_AddrStrobe);  ");
	hdlPS.println("");
	hdlPS.println("END ARCHITECTURE imp;");
    }

    /**
     *  Write .mpd file
     */
    private void _writeMpdFile() throws FileNotFoundException {
	PrintStream mpdPS = _openFile(_dataDir + File.separatorChar + _mpdFile);
	mpdPS.println("## File automatically generated by ESPAM");
	mpdPS.println("");
	mpdPS.println("");
	int i;

	mpdPS.println("BEGIN " + _coreName);
	mpdPS.println("");
	mpdPS.println("## Peripheral Options");
	mpdPS.println("OPTION IPTYPE = PERIPHERAL");
	mpdPS.println("OPTION IMP_NETLIST = TRUE");
	mpdPS.println("OPTION HDL = VHDL");
	mpdPS.println("OPTION SIM_MODELS = BEHAVIORAL : STRUCTURAL");
	mpdPS.println("OPTION USAGE_LEVEL = BASE_USER");
	mpdPS.println("OPTION CORE_STATE = ACTIVE");
	mpdPS.println("OPTION IP_GROUP = USER");
	mpdPS.println("");

	mpdPS.println("## Bus Interfaces");
	mpdPS.println("BUS_INTERFACE BUS = SLMB, BUS_STD = LMB, BUS_TYPE = SLAVE");
	for (i = 1; i <= _maxOutPorts; i++) {
	    mpdPS.print("BUS_INTERFACE BUS = FIFO_WRITE_" + i);
	    mpdPS.println(", BUS_STD = FSL, BUS_TYPE = MASTER");
	}
	for (i = 1; i <= _maxInPorts; i++) {
	    mpdPS.print("BUS_INTERFACE BUS = FIFO_READ_" + i);
	    mpdPS.println(", BUS_STD = FSL, BUS_TYPE = SLAVE");
	}
	mpdPS.println("");

	mpdPS.println("## Generics for VHDL or Parameters for Verilog");
	mpdPS.print ("PARAMETER C_HIGHADDR = 0x");
	mpdPS.print(_digitToStringHex(0, 8) + ", ");
	mpdPS.print("DT = std_logic_vector(0 to 31), BUS = SLMB, ");
	mpdPS.println("ADDRESS = HIGH, PAIR = C_BASEADDR");
	mpdPS.print("PARAMETER C_BASEADDR = 0x");
	mpdPS.print(_digitToStringHex(8, 8) + ", ");
	mpdPS.print("DT = std_logic_vector(0 to 31), BUS = SLMB, ");
	mpdPS.print("ADDRESS = BASE, PAIR = C_HIGHADDR, ");
	mpdPS.println("MIN_SIZE = 0x" + _digitToStringHex(8, 8));
	mpdPS.println("PARAMETER C_AB = 8, DT = INTEGER");
	mpdPS.println("PARAMETER C_LMB_AWIDTH = 32, DT = INTEGER, BUS = SLMB");
	mpdPS.println("PARAMETER C_LMB_DWIDTH = 32, DT = INTEGER, BUS = SLMB");
	mpdPS.println("PARAMETER C_FIFO_WRITE = " + _maxOutPorts + ", DT = INTEGER");
	mpdPS.println("PARAMETER C_FIFO_READ = " + _maxInPorts + ", DT = INTEGER");
	mpdPS.println("");

	mpdPS.println("## Ports");
	mpdPS.println("PORT LMB_Clk = \"\", DIR = I, SIGIS = CLK, BUS = SLMB");
	mpdPS.println("PORT LMB_Rst = OPB_Rst, DIR = I, BUS = SLMB");
	mpdPS.print("PORT LMB_ABus = LMB_ABus, DIR = I, ");
	mpdPS.println("VEC = [0:(C_LMB_AWIDTH-1)], BUS = SLMB");
	mpdPS.print("PORT LMB_WriteDBus = LMB_WriteDBus, DIR = I, ");
	mpdPS.println("VEC = [0:(C_LMB_DWIDTH-1)], BUS = SLMB");
	mpdPS.println("PORT LMB_AddrStrobe = LMB_AddrStrobe, DIR = I, BUS = SLMB");
	mpdPS.println("PORT LMB_ReadStrobe = LMB_ReadStrobe, DIR = I, BUS = SLMB");
	mpdPS.println("PORT LMB_WriteStrobe = LMB_WriteStrobe, DIR = I, BUS = SLMB");
	mpdPS.print("PORT LMB_BE = LMB_BE, DIR = I, VEC = ");
	mpdPS.println("[0:((C_LMB_DWIDTH/8)-1)], BUS = SLMB");
	mpdPS.print("PORT Sl_DBus = Sl_DBus, DIR = O, VEC = ");
	mpdPS.println("[0:(C_LMB_DWIDTH-1)], BUS = SLMB");
	mpdPS.println("PORT Sl_Ready = Sl_Ready, DIR = O, BUS = SLMB");
	mpdPS.println("");

	for (i = 1; i <= _maxOutPorts; i++) {
	    mpdPS.print("PORT WR_Clk_" + i + " = FSL_M_Clk, ");
	    mpdPS.println("DIR = O, BUS = FIFO_WRITE_" + i);
	    mpdPS.print("PORT WR_Control_" + i + " = FSL_M_Control, ");
	    mpdPS.println("DIR = O, BUS = FIFO_WRITE_" + i);
	    mpdPS.print("PORT WR_EN_" + i + " = FSL_M_Write, DIR = O, ");
	    mpdPS.println("BUS = FIFO_WRITE_" + i);
	    mpdPS.print("PORT WR_Data_" + i + " = FSL_M_Data, DIR = O, ");
	    mpdPS.println("VEC = [0:C_LMB_DWIDTH-1], BUS = FIFO_WRITE_" + i);
	    mpdPS.print("PORT Full_" + i + " = FSL_M_Full, DIR = I, ");
	    mpdPS.println("BUS = FIFO_WRITE_" + i);
	    mpdPS.println("");
	}
	for (i = 1; i <= _maxInPorts; i++) {
	    mpdPS.print("PORT RD_Clk_" + i + " = FSL_S_Clk, ");
	    mpdPS.println("DIR = O, BUS = FIFO_READ_" + i);
	    mpdPS.print("PORT RD_Control_" + i + " = FSL_S_Control, ");
	    mpdPS.println("DIR = O, BUS = FIFO_READ_" + i);
	    mpdPS.print("PORT RD_EN_" + i + " = FSL_S_Read, DIR = O, ");
	    mpdPS.println("BUS = FIFO_READ_" + i);
	    mpdPS.print("PORT RD_Data_" + i + " = FSL_S_Data, DIR = I, ");
	    mpdPS.println("VEC = [0:C_LMB_DWIDTH-1], BUS = FIFO_READ_" + i);
	    mpdPS.print("PORT Exists_" + i + " = FSL_S_Exists, DIR = I, ");
	    mpdPS.println("BUS = FIFO_READ_" + i);
	    mpdPS.println("");
	}
	mpdPS.println("");
	mpdPS.println("END");
    }
    
    /**
     *  Write .pao file
     */
    private void _writePaoFile() throws FileNotFoundException {
	PrintStream paoPS = _openFile(_dataDir + "/" + _paoFile);
	paoPS.println("## File automatically generated by ESPAM");
	paoPS.println("");
	paoPS.println("");
	paoPS.println("lib proc_common_v3_00_a pselect");
	paoPS.println("lib " + _coreName + "_v1_00_a " + _coreName);
    }
    
    /**
     *  conver to hexical string
     *  @param xLong long value to be changed
     *  @param format length of the digit format
     */
    private String _digitToStringHex(int xInt, int format) {
        String binStr = Integer.toHexString(xInt);
        int binStrlength = binStr.length();
        if (format < binStrlength) {
            System.out.println(
                "Error!!!!: The value can not be represented as " + format + " digit hex");
        }
        String returnStr = new String();
        for (int i = 0; i < (format - binStrlength); i++) {
            returnStr = returnStr + '0';
        }
        returnStr = returnStr + binStr;
        return returnStr;
    }
    
    /**
     *  Open a file to write
     *  @param fileName the fullpath file name
     */
    private PrintStream _openFile(String fileName) throws FileNotFoundException {
        PrintStream ps = null;
        String fn = "";

        System.out.println(" -- OPEN FILE: " + fileName);

	    fn = _codeDir + File.separatorChar + fileName;
        if (fileName.equals(""))
            ps = new PrintStream(System.out);
        else
            ps = new PrintStream(new FileOutputStream(fn));

        return ps;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    private String _coreName;
    
    //dir name
    private String _moduleName;
    private String _moduleDir;
    private static String _dataDir = "data";
    private static String _devlDir = "devl";
    private static  String _hdlDir = "hdl/vhdl";
		
    //The name of these two files must be the same as version of mhs!!!!
    //file name
    private String _paoFile;
    private String _mpdFile;
    private String _hdlFile;

    private int _maxInPorts = 0;
    private int _maxOutPorts = 0;
    
    private UserInterface _ui = null;
    private String _codeDir;
    
}
