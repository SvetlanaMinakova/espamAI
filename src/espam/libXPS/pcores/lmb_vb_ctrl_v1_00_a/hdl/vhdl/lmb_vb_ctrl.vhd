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

library ieee;
use ieee.std_logic_1164.all;

LIBRARY proc_common_v3_00_a;
USE proc_common_v3_00_a.pselect;

entity lmb_vb_ctrl is 
  generic
  (
	-- generics for LMB interface
	C_HIGHADDR : STD_LOGIC_VECTOR(0 to 31) := X"0800000f";
    C_BASEADDR : STD_LOGIC_VECTOR(0 to 31) := X"08000000";
    C_AWIDTH   : INTEGER                   := 32;
    C_DWIDTH   : INTEGER                   := 32;
	C_AB       : INTEGER				   := 8

  );
  port
  (
       RST  : in std_logic;
       CLK  : in std_logic;

 	   -- Requred bus ports for LMB1
       LMB_ABus        : IN  STD_LOGIC_VECTOR(0 TO C_AWIDTH-1);
       LMB_WriteDBus   : IN  STD_LOGIC_VECTOR(0 TO C_DWIDTH-1);
       LMB_AddrStrobe  : IN  STD_LOGIC;
       LMB_ReadStrobe  : IN  STD_LOGIC;
       LMB_WriteStrobe : IN  STD_LOGIC;
       LMB_BE          : IN  STD_LOGIC_VECTOR(0 TO (C_DWIDTH/8 - 1));	  
       Sl_DBus         : OUT STD_LOGIC_VECTOR(0 TO C_DWIDTH-1);
       Sl_READY        : OUT STD_LOGIC;

       Bus2IP_Clk   : OUT  std_logic;
       Bus2IP_Reset : OUT  std_logic;
	   Bus2IP_Addr  : OUT  std_logic_vector(0 to C_AWIDTH-1);
       Bus2IP_CS    : OUT  std_logic;
       Bus2IP_Data  : OUT  std_logic_vector(0 to C_DWIDTH-1);
       Bus2IP_RdCE  : OUT  std_logic;
       Bus2IP_WrCE  : OUT  std_logic;
       IP2Bus_Data  : IN std_logic_vector(0 to C_DWIDTH-1)	   
  );
  
end lmb_vb_ctrl; 

architecture RTL of lmb_vb_ctrl is

	COMPONENT pselect IS
	GENERIC (
	      C_AW   : INTEGER                   := C_AWIDTH;
	      C_BAR  : STD_LOGIC_VECTOR(0 TO C_AWIDTH-1) := X"00000000";
	      C_AB   : integer                   := 8);
	PORT (
	      A     : in  STD_LOGIC_VECTOR(0 TO C_AWIDTH-1);
	      CS    : out STD_LOGIC;
	      AValid : in  STD_LOGIC);
	END COMPONENT; 

	signal sl_lmb_select : std_logic;

begin
-----------------------------------------------------------------------------
-- Handling the LMB bus interface
-----------------------------------------------------------------------------	

Bus2IP_Clk <= CLK;  
Bus2IP_Reset <= RST;
Bus2IP_Addr <= LMB_ABus;
Bus2IP_CS <= sl_lmb_select;	
Bus2IP_Data <= LMB_WriteDBus;
Bus2IP_RdCE <= LMB_ReadStrobe;
Bus2IP_WrCE <= LMB_WriteStrobe;		
Sl_DBus <= IP2Bus_Data;


Ready_Handling1 : PROCESS (CLK, RST) IS
BEGIN  -- PROCESS Ready_Handling
    IF (RST = '1') THEN
      Sl_READY <= '0';
    ELSIF (CLK'EVENT AND CLK = '1') THEN  -- rising clock edge
      Sl_READY <= LMB_AddrStrobe AND sl_lmb_select;	  
    END IF;
END PROCESS Ready_Handling1;

-- Do the LMB address decoding
pselect_lmb1 : pselect
generic map (
      C_AW   => LMB_ABUS'length,
      C_BAR  => C_BASEADDR,
      C_AB   => C_AB)
port map (
      A      => LMB_ABUS,
      CS     => sl_lmb_select,
      AValid => LMB_AddrStrobe);

end RTL;