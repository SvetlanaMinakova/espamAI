-- ***********************************************************************
--
-- The ESPAM Software Tool 
-- Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
-- All rights reserved.
--
-- The use and distribution terms for this software are covered by the 
-- Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
-- which can be found in the file LICENSE at the root of this distribution.
-- By using this software in any fashion, you are agreeing to be bound by 
-- the terms of this license.
--
-- You must not remove this notice, or any other, from this software.
--
-- ************************************************************************

library IEEE;
use IEEE.STD_LOGIC_1164.all;
USE ieee.std_logic_arith.all;
USE ieee.std_logic_unsigned.all;

LIBRARY proc_common_v3_00_a;
USE proc_common_v3_00_a.pselect;

entity fin_ctrl is
  GENERIC (
    C_HIGHADDR      : STD_LOGIC_VECTOR(0 to 31) := X"0900000f";
    C_BASEADDR      : STD_LOGIC_VECTOR(0 to 31) := X"09000000";
    C_AB            : INTEGER                   := 8;
    C_LMB_AWIDTH    : INTEGER                   := 32;
    C_LMB_DWIDTH    : INTEGER                   := 32
    );
  PORT (
    LMB_Clk         : IN STD_LOGIC := '0';
    LMB_Rst         : IN STD_LOGIC := '0';

    -- LMB Bus
    LMB_ABus        : IN  STD_LOGIC_VECTOR(0 TO C_LMB_AWIDTH-1);
    LMB_WriteDBus   : IN  STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    LMB_AddrStrobe  : IN  STD_LOGIC;
    LMB_ReadStrobe  : IN  STD_LOGIC;
    LMB_WriteStrobe : IN  STD_LOGIC;
    LMB_BE          : IN  STD_LOGIC_VECTOR(0 TO (C_LMB_DWIDTH/8 - 1));
    Sl_DBus         : OUT STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    Sl_Ready        : OUT STD_LOGIC;

    -- ports to host_design_ctrl interface       
    ENABLE          : IN STD_LOGIC;
    FinOut          : OUT STD_LOGIC
    );
end fin_ctrl;


architecture imp of fin_ctrl is
-----------------------------------------------------------------------
-- component declarations
-----------------------------------------------------------------------
COMPONENT pselect IS
GENERIC (
      C_AW   : INTEGER                   := 32;
      C_BAR  : STD_LOGIC_VECTOR(0 TO 31);
      C_AB   : integer                   := 29);
PORT (
      A     : in  STD_LOGIC_VECTOR(0 TO 31);
      CS    : out STD_LOGIC;
      AValid : in  STD_LOGIC);
END COMPONENT;

-----------------------------------------------------------------------
-- internal signals
-----------------------------------------------------------------------
signal lmb_select   : STD_LOGIC; 
signal sl_rdy_rd    : STD_LOGIC;
signal sl_counter   : STD_LOGIC_VECTOR(0 TO 31);
signal sl_Sl_DBus   : STD_LOGIC_VECTOR(0 TO 31);
signal sl_address   : STD_LOGIC_VECTOR(0 TO 3);
signal sl_param_ld  : STD_LOGIC;

begin
	
    ---------------------------
    -- Read the registers
    ---------------------------
    Sl_DBus  <= sl_Sl_DBus when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 0) else
                sl_counter when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 1) else
                X"a5a5a5a5"; -- others

    PROCESS (LMB_Clk, LMB_Rst) IS
    BEGIN  
        IF (LMB_Rst = '1') THEN

            FinOut <= '0';
            sl_counter <= (others=>'0');
            sl_Sl_DBus <= (others=>'0');

        ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
		  
		      sl_Sl_DBus(C_LMB_DWIDTH-1) <= ENABLE;
				
            IF LMB_WriteStrobe = '1' and lmb_select = '1' THEN

                 IF CONV_INTEGER(LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3)) = 0 THEN
                     FinOut <= LMB_WriteDBus(C_LMB_DWIDTH-1);
                 ELSIF CONV_INTEGER(LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3)) = 1 THEN
                     sl_counter <= LMB_WriteDBus;
                 END IF;

            ELSE
                 sl_counter  <= sl_counter + 1;
	    END IF;
        END IF;
    END PROCESS;
	
    ---------------------------------------------------
    -- Handling the LMB bus interface
    ---------------------------------------------------
    Ready_Handling : PROCESS (LMB_Clk, LMB_Rst) IS
    BEGIN  
        IF (LMB_Rst = '1') THEN
            sl_rdy_rd <= '0';
            Sl_Ready <= '0';
            sl_address <= (others=>'0');
        ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
            sl_rdy_rd  <= LMB_ReadStrobe AND lmb_select;
            Sl_Ready  <= LMB_AddrStrobe AND lmb_select;
            IF( LMB_AddrStrobe = '1' ) THEN -- delay the address (needed for reading)
            -- 4-bit address, the 2 LSBits are 'byte select'
                sl_address <= LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3);
            END IF;
        END IF;
    END PROCESS Ready_Handling;

    -----------------------------------------------------------------------
    -- Do the LMB address decoding
    -----------------------------------------------------------------------
    pselect_lmb : pselect
    generic map (
          C_AW   => LMB_ABus'length,
          C_BAR  => C_BASEADDR,
          C_AB => C_AB)
    port map (
          A     => LMB_ABus,
          CS    => lmb_select,
          AValid => LMB_AddrStrobe
    );
	
end architecture imp;
