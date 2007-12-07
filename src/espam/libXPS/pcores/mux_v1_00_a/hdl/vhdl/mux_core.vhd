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

--
--  File: C:\raikin\fpga_template\active_hdl\top_level\top_level\src\mux_core.vhd
--  created by Design Wizard: 10/17/02 17:18:45
--

library IEEE;
use IEEE.std_logic_1164.all;

entity mux_core is
	generic
    (
      N_MUX  : integer                   := 1
    );
	port (
      H_DW  : in STD_LOGIC_VECTOR (191 downto 0);  -- date write from the host
      H_TRI : in STD_LOGIC_VECTOR (191 downto 0);  -- tristate from the host 
      H_AD  : in STD_LOGIC_VECTOR (119 downto 0);  -- address from host 
      H_CO  : in STD_LOGIC_VECTOR (53 downto 0);   -- control from host
      D_DW  : in STD_LOGIC_VECTOR (191 downto 0);  -- date write from our design
      D_TRI : in STD_LOGIC_VECTOR (191 downto 0);  -- tristate from our design
      D_AD  : in STD_LOGIC_VECTOR (119 downto 0);  -- address from design 
      D_CO  : in STD_LOGIC_VECTOR (53 downto 0);   -- control from design
      DW    : out STD_LOGIC_VECTOR (191 downto 0);	-- data write to buffer (sram)
      TRI   : out STD_LOGIC_VECTOR (191 downto 0); -- tristate to buffer 
      ra    : out std_logic_vector (119 downto 0);
      rc    : out std_logic_vector (53 downto 0);
		
      RST   : in std_logic;
      CNTRL : in STD_LOGIC_VECTOR(31 downto 0)
	);
end mux_core;

architecture mux_core of mux_core is 

-- the command register is subdevided in the following way:
-- bits 31 downto 26 are masks for the memory banks. '0' - host access. '1' - design access
-- bit 0 is RESET stage (active high) 
-- bit 1 is Initial (write) Memory stage (active high) 
-- bit 2 is Read Memory stage (active high)
-- bit 3 is Execute stage (active high)
-- bit 26 is bank0 access bit
-- bit 27 is bank1 access bit
-- bit 28 is bank2 access bit
-- bit 29 is bank3 access bit
-- bit 30 is bank4 access bit
-- bit 31 is bank5 access bit

begin

-- generate the multiplexers for memory banks
MUX_INSTANCES :
     for i in 0 to N_MUX-1 generate
     begin
        pr_mux: process( CNTRL, H_DW((i+1)*32-1 downto i*32), H_TRI((i+1)*32-1 downto i*32), H_AD((i+1)*20-1 downto i*20), D_CO((i+1)*9-1 downto i*9), D_AD((i+1)*20-1 downto i*20), D_TRI((i+1)*32-1 downto i*32), D_DW((i+1)*32-1 downto i*32), H_CO((i+1)*9-1 downto i*9) ) 
		begin		 
		  if CNTRL(26+i) = '0' then -- data from host
		    DW((i+1)*32-1 downto i*32)  <= H_DW((i+1)*32-1 downto i*32);
		    TRI((i+1)*32-1 downto i*32) <= H_TRI((i+1)*32-1 downto i*32);
		    ra((i+1)*20-1 downto i*20)  <= H_AD((i+1)*20-1 downto i*20);
		    rc((i+1)*9-1 downto i*9)  <= H_CO((i+1)*9-1 downto i*9);				
		  else					
		    DW((i+1)*32-1 downto i*32)  <= D_DW((i+1)*32-1 downto i*32);
		    TRI((i+1)*32-1 downto i*32) <= D_TRI((i+1)*32-1 downto i*32);
		    ra((i+1)*20-1 downto i*20)  <= D_AD((i+1)*20-1 downto i*20);
		    rc((i+1)*9-1 downto i*9)  <= D_CO((i+1)*9-1 downto i*9);   
		  end if;	
		end process;
     end generate;
 	   
end mux_core;
