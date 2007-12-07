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
--  File: zbt_port_from_design.vhd
--  created by Design Wizard: 10/18/02 13:24:16
--

--{{ Section below this comment is automatically maintained
--   and may be overwritten
--{entity {zbt_port_from_design} architecture {RTL}}

library IEEE;
use IEEE.std_logic_1164.all;

entity zbt_port_from_design is
	port (
		CLK: in STD_LOGIC;
		RST: in STD_LOGIC;
		WR: in STD_LOGIC;
		A: in STD_LOGIC_VECTOR (19 downto 0);
		D: in STD_LOGIC_VECTOR (31 downto 0);
		Q: out STD_LOGIC_VECTOR (31 downto 0);
		RA_O: out STD_LOGIC_VECTOR (19 downto 0); -- address out. 2 MSB bits are truncated 
		RC_O: out STD_LOGIC_VECTOR (8 downto 0);
		T_RD: out STD_LOGIC_VECTOR (31 downto 0);
		RD_O: out STD_LOGIC_VECTOR (31 downto 0);
		RD_I: in STD_LOGIC_VECTOR (31 downto 0)
	);
end zbt_port_from_design;

--}} End of automatically maintained section

architecture RTL of zbt_port_from_design is			 
  
	constant be_width    : natural := 4;

	type d_q_type is array(2 downto 0) of std_logic_vector(31 downto 0);
    signal sl_d_q     : d_q_type;										
	signal sl_oe_rd   : std_logic_vector(1 downto 0);

begin
  	--data write
    RD_O <= sl_d_q(2);    

	pr_data_out: process(rst, clk)	
    begin
        if RST = '1' then
            sl_d_q <= (others => (others => '0'));
            sl_oe_rd <= (others => '0');
            T_RD<= (others => '1');
        elsif (CLK'event and CLK = '1') then 
           sl_d_q(2 downto 1) <= sl_d_q(1 downto 0);
	       sl_d_q(0) <= D;
	       sl_oe_rd(1 downto 0) <= sl_oe_rd(0) & WR;
           T_RD <= (others => not sl_oe_rd(1));
        end if;
    end process;
	
	-- data read
	Q <= RD_I;
	
	
	--address and control
	pr_ctrl_out: process(clk, rst)
	begin
		if RST = '1' then
			RA_O <= (others => '0');
			RC_O <= (others => '1');
		elsif (CLK'event and CLK = '1') then 
			RA_O(19 downto 0) <= A;
    		RC_O(8 downto 5)  <= (others => '0');
			RC_O(3 downto 0)  <= (others => '0');
			RC_O(4) <= not WR;
		end if;
	end process;
    
end RTL;

										  