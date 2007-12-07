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
-- clocks_dcm1.vhd - clock generator module for ZBT design, using
--                   Virtex-II DCMs
--
--                   Generates 1 SSRAM clocks.
--
-- (c) Alpha Data Parallel Systems Ltd. 1999-2001
--
-- Example program for ADM-XRCIIPro-Lite
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_unsigned.all;

entity myclkrst is
    generic(
        num_clock     : integer := 2);
    port(
	    -- clocks and reset from local bus
	    lreset_l      : in    std_logic;
        lclk          : in    std_logic;
        mclk          : in    std_logic;
	    -- differential clock = 125 MHz	
	    mgt_clk       : in    std_logic;
	    mgt_clkb      : in    std_logic;
		-- clocks and reset generated by the module
        rst           : out   std_logic;
        mclk_gen      : out   std_logic;
		lclk_gen	  :	out   std_logic;
	    mgt_clk_gen   : out   std_logic;		
        clk_gen       : out   std_logic_vector(num_clock - 1 downto 0);
		-- feedback clocks
		clk_fb        : in    std_logic_vector(num_clock - 1 downto 0);
		
		locked        : out   std_logic_vector(31 downto 0));
end myclkrst;

architecture synthesis of myclkrst is

    signal sl_lreset       : std_logic;
    signal sl_rst_bufg     : std_logic;
	    
    component BUFG
        port(
            I : in  std_logic;
            O : out std_logic);
    end component;
      
begin
	
	--
    -- Define output ports
    -- 
    rst          <= lreset_l;	
	mclk_gen     <= mclk;
	lclk_gen     <= lclk;
    mgt_clk_gen  <= mgt_clk and mgt_clkb;


l1:	for i in 0 to num_clock-1 generate
	  clk_gen(i) <= '1'; 	
	end generate;	
	
	locked <= (others => '1');
    
end synthesis;
