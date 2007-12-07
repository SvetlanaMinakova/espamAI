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
-- Title       : host_design_ctrl_core
-- Design      : host_design_ctrl_core
-- Author      : ttnn
-- Company     : tn
--
---------------------------------------------------------------------------------------------------
--
-- File        : host_design_ctrl_core.vhd
-- Generated   : Sat May 28 15:54:07 2005
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
--{entity {host_design_ctrl_core} architecture {RTL}}

library IEEE;
use IEEE.STD_LOGIC_1164.all;



entity host_design_ctrl_core is
	generic
    (
      N_FIN  : integer                   := 1
    );
	 port(
		 RST : in STD_LOGIC;   --RST '1' reset to 0 (stop), '0' to 1 (run);
		 COMMAND_REG : in STD_LOGIC_VECTOR(31 downto 0);
         FIN_REG : in STD_LOGIC_VECTOR(19 downto 0);
		 RST_OUT : out STD_LOGIC;
		 STATUS_REG : out STD_LOGIC_VECTOR(31 downto 0)
	     );
end host_design_ctrl_core;

--}} End of automatically maintained section

architecture RTL of host_design_ctrl_core is	

-- the command register is subdevided in the following way:
-- bits 31 downto 26 are masks for the memory banks. '0' - host access. '1' - design access
-- bit 0 is RESET stage (active high) 
-- bit 1 is Initial (write) Memory stage (active high) 
-- bit 2 is Read Memory stage (active high)
-- bit 3 is Execute stage (active high)
-- define bit locations in the command register
constant comReg_bit_RESET : natural :=0;
constant comReg_bit_InitMEM : natural :=1;
constant comReg_bit_ReadMEM : natural :=2;
constant comReg_bit_EXE : natural :=3;
constant comReg_bit_LdParam : natural :=4; 

-- STATUS_REG Use only bit 0: value '1' - design finished
constant statReg_bit_Finished : natural :=0;

signal sl_FIN_REG   :  std_logic_vector(19 downto 0);

begin

	 -- enter your statements here -- 
  RST_OUT <= ( not RST ) and COMMAND_REG(comReg_bit_EXE);  --RST_OUT '0' to 0 (stop), '1' to 1 (run);
  
  FIN_INSTANCES :
     for i in 0 to N_FIN-1 generate
     begin
		sl_FIN_REG(i)  <= FIN_REG(i);
     end generate;
	 
  FIN_OTHERS_INSTANCES :
     for i in N_FIN to 19 generate
     begin
		sl_FIN_REG(i)  <= '1';
     end generate;
	 
  STATUS_REG(statReg_bit_Finished) <= sl_FIN_REG(0) and 
                                      sl_FIN_REG(1) and 
                                      sl_FIN_REG(2) and 
									  sl_FIN_REG(3) and
									  sl_FIN_REG(4) and
									  sl_FIN_REG(5) and
									  sl_FIN_REG(6) and
									  sl_FIN_REG(7) and
									  sl_FIN_REG(8) and
									  sl_FIN_REG(9) and
									  sl_FIN_REG(10) and
									  sl_FIN_REG(11) and
									  sl_FIN_REG(12) and
									  sl_FIN_REG(13) and
									  sl_FIN_REG(14) and
									  sl_FIN_REG(15) and
									  sl_FIN_REG(16) and
									  sl_FIN_REG(17) and
									  sl_FIN_REG(18) and
									  sl_FIN_REG(19); 
  
end RTL;
