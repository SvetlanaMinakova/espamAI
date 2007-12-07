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

---------------------------------------------------------------------------------------------------
--
-- Title       : fin_ctrl
-- Design      : fin_ctrl
-- Author      : ttnn
-- Company     : tn
--
---------------------------------------------------------------------------------------------------
--
-- File        : fin_ctrl.vhd
-- Generated   : Sun Aug 28 15:24:42 2005
-- From        : interface description file
-- By          : Itf2Vhdl ver. 1.20
--
---------------------------------------------------------------------------------------------------
--
-- Description : 
--
---------------------------------------------------------------------------------------------------

--{{ Section below this comment is automatically maintained
--   and may be overwritten
--{entity {fin_ctrl} architecture {imp}}

library IEEE;
use IEEE.STD_LOGIC_1164.all;
USE ieee.std_logic_arith.all;
USE ieee.std_logic_unsigned.all;

LIBRARY proc_common_v1_00_c;
USE proc_common_v1_00_c.pselect;

entity fin_ctrl is
  GENERIC (
    C_HIGHADDR     : STD_LOGIC_VECTOR(0 to 31) := X"0900000f";
    C_BASEADDR     : STD_LOGIC_VECTOR(0 to 31) := X"09000000";
    C_AB           : INTEGER                   := 8;
    C_LMB_AWIDTH   : INTEGER                   := 32;
    C_LMB_DWIDTH   : INTEGER                   := 32
    );
  PORT (
    LMB_Clk : IN STD_LOGIC := '0';
    LMB_Rst : IN STD_LOGIC := '0';

    -- LMB Bus
    LMB_ABus        : IN  STD_LOGIC_VECTOR(0 TO C_LMB_AWIDTH-1);
    LMB_WriteDBus   : IN  STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    LMB_AddrStrobe  : IN  STD_LOGIC;
    LMB_ReadStrobe  : IN  STD_LOGIC;
    LMB_WriteStrobe : IN  STD_LOGIC;
    LMB_BE          : IN  STD_LOGIC_VECTOR(0 TO (C_LMB_DWIDTH/8 - 1));
    Sl_DBus      : OUT STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    Sl_Ready     : OUT STD_LOGIC;

    -- ports to host_design_ctrl interface       
    Sl_FinOut    : OUT STD_LOGIC
    );
end fin_ctrl;

--}} End of automatically maintained section

architecture imp of fin_ctrl is
--------------------------------------------------------------------------------
-- component declarations
--------------------------------------------------------------------------------
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

--------------------------------------------------------------------------------
-- internal signals
--------------------------------------------------------------------------------
signal lmb_select   : STD_LOGIC; 
signal Sl_Fin       : STD_LOGIC;

begin	   
	
PROCESS (LMB_Clk, LMB_Rst) IS
BEGIN  
    IF (LMB_Rst = '1') THEN
        Sl_Fin <= '0';
    ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
        IF LMB_WriteStrobe = '1' and lmb_select = '1' THEN
           Sl_Fin <= '1';
        END IF;
    END IF;
END PROCESS;
 
Sl_FinOut <= Sl_Fin; 	
	
--------------------------------------------------------------------------------
-- Do the LMB address decoding
--------------------------------------------------------------------------------
pselect_lmb : pselect
generic map (
      C_AW   => LMB_ABus'length,
      C_BAR  => C_BASEADDR,
      C_AB => C_AB)
port map (
      A     => LMB_ABus,
      CS    => lmb_select,
      AValid => LMB_AddrStrobe);
	  
Ready_Handling : PROCESS (LMB_Clk, LMB_Rst) IS
BEGIN  -- PROCESS Ready_Handling
    IF (LMB_Rst = '1') THEN
      Sl_Ready <= '0';
    ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN  -- rising clock edge
      Sl_Ready <= LMB_AddrStrobe AND lmb_select;
    END IF;
END PROCESS Ready_Handling;
	
end architecture imp;
