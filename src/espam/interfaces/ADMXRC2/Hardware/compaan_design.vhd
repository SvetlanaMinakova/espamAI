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
--  File: C:\raikin\fpga_template\active_hdl\top_level\top_level\src\design_interface_with_mem.vhd
--  created by Design Wizard: 10/18/02 16:36:41
--

--{{ Section below this comment is automatically maintained
--   and may be overwritten
--{entity {design_interface_with_mem} architecture {design_interface_with_mem}}

library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity compaan_design is
	 port(
		 CLK : in STD_LOGIC;
		 RST : in STD_LOGIC;
		 D_Data_R0 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_R1 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_R2 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_R3 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_R4 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_R5 : in STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_W0 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA0 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC0 : out STD_LOGIC_VECTOR(8 downto 0);
		 D_Tristate_0 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Data_W1 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Tristate_1 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA1 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC1 : out STD_LOGIC_VECTOR(8 downto 0);
		 D_Data_W2 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Tristate_2 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA2 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC2 : out STD_LOGIC_VECTOR(8 downto 0);
		 D_Data_W3 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Tristate_3 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA3 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC3 : out STD_LOGIC_VECTOR(8 downto 0);
		 D_Data_W4 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Tristate_4 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA4 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC4 : out STD_LOGIC_VECTOR(8 downto 0);
		 D_Data_W5 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_Tristate_5 : out STD_LOGIC_VECTOR(31 downto 0);
		 D_RA5 : out STD_LOGIC_VECTOR(19 downto 0);
		 D_RC5 : out STD_LOGIC_VECTOR(8 downto 0);	  
		 
		 COMMAND_REG   : in STD_LOGIC_VECTOR(31 downto 0);   
		 PARAMETER_REG : in std_logic_vector(31 downto 0);
		 STAT_REG      : out STD_LOGIC_VECTOR(31 downto 0)
	     );
end compaan_design;

--}} End of automatically maintained section

architecture RTL of compaan_design is
						  	
	type t_write is array (0 to 5) of std_logic;  
	type t_address is array (0 to 5) of std_logic_vector(19 downto 0);
	type t_data is array (0 to 5) of std_logic_vector(31 downto 0);
    signal sl_write       : t_write;
    signal sl_address  : t_address;
    signal sl_data_write  : t_data;
    signal sl_data_read   : t_data;	
	signal sl_rst : std_logic;
	
	constant comReg_bit_RESET   : natural := 0;  -- reset bit 
    constant comReg_bit_InitMEM : natural := 1;  -- initialize memory bit 
    constant comReg_bit_ReadMEM : natural := 2;  -- read memory bit
    constant comReg_bit_EXE     : natural := 3;  -- execute bit
    constant comReg_bit_LdParam : natural := 4;  -- load a parameter into the PN
	signal ng_clk: std_logic;  
	
component zbt_port_from_design 
	port (
		CLK: in STD_LOGIC;
		RST: in STD_LOGIC;
		WR: in STD_LOGIC;
		A: in STD_LOGIC_VECTOR (19 downto 0);
		D: in STD_LOGIC_VECTOR (31 downto 0);
		Q: out STD_LOGIC_VECTOR (31 downto 0);
		RA_O: out STD_LOGIC_VECTOR (19 downto 0);
		RC_O: out STD_LOGIC_VECTOR (8 downto 0);
		T_RD: out STD_LOGIC_VECTOR (31 downto 0);
		RD_O: out STD_LOGIC_VECTOR (31 downto 0);
		RD_I: in STD_LOGIC_VECTOR (31 downto 0)
	);
end component;



component AlphaData_If
	port(
		DATA_WR_B0: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B0: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B0: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B0: out STD_LOGIC;	
		
		DATA_WR_B1: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B1: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B1: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B1: out STD_LOGIC;
		
		DATA_WR_B2: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B2: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B2: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B2: out STD_LOGIC;
		
		DATA_WR_B3: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B3: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B3: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B3: out STD_LOGIC;
		
		DATA_WR_B4: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B4: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B4: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B4: out STD_LOGIC;
		
		DATA_WR_B5: out STD_LOGIC_VECTOR (31 downto 0);
		DATA_RD_B5: in STD_LOGIC_VECTOR (31 downto 0);
		ADRESS_B5: out STD_LOGIC_VECTOR (19 downto 0);
		WRITE_B5: out STD_LOGIC;
		
		PARAMETER_REG  : in std_logic_vector(31 downto 0);
        PAR_LOAD       : in std_logic;
		
		CLK:  in std_logic;
		RST:  in std_logic;
		STATUS: out std_logic_vector(31 downto 0)
	);
end component;


begin	 
	
	ng_clk <= not CLK; 
	
	kpn_net: AlphaData_If
	port map(
		DATA_WR_B0 => sl_data_write(0),
		DATA_RD_B0 => D_Data_R0,
		ADRESS_B0 => sl_address(0),
		WRITE_B0 => sl_write(0),
		
		DATA_WR_B1 => sl_data_write(1),
		DATA_RD_B1 => sl_data_read(1),
		ADRESS_B1 => sl_address(1),
		WRITE_B1 => sl_write(1),
		
		DATA_WR_B2 => sl_data_write(2),
		DATA_RD_B2 => sl_data_read(2),
		ADRESS_B2 => sl_address(2),
		WRITE_B2 => sl_write(2),
		
		DATA_WR_B3 => sl_data_write(3),
		DATA_RD_B3 => sl_data_read(3),
		ADRESS_B3 => sl_address(3),
		WRITE_B3 => sl_write(3),
		
		DATA_WR_B4 => sl_data_write(4),
		DATA_RD_B4 => sl_data_read(4),
		ADRESS_B4 => sl_address(4),
		WRITE_B4 => sl_write(4),
		
		DATA_WR_B5 => sl_data_write(5),
		DATA_RD_B5 => sl_data_read(5),
		ADRESS_B5 => sl_address(5),
		WRITE_B5 => sl_write(5),
		
		PARAMETER_REG  => PARAMETER_REG,
        PAR_LOAD       => command_reg(comReg_bit_LdParam),

		CLK => CLK ,
		RST => sl_rst,
		STATUS => stat_reg
		
	);
	   
	port0: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(0),
		A => sl_address(0),
		D => sl_data_write(0),
		Q => sl_data_read(0),
		RA_O => D_RA0, 
		RC_O => D_RC0,
		T_RD =>	D_Tristate_0,
		RD_O => D_Data_W0,
		RD_I => D_Data_R0
		);					
		
	port1: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(1),
		A => sl_address(1),
		D => sl_data_write(1),
		Q => sl_data_read(1),
		RA_O => D_RA1, 
		RC_O => D_RC1,
		T_RD =>	D_Tristate_1,
		RD_O => D_Data_W1,
		RD_I => D_Data_R1
		);
		
	port2: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(2),
		A => sl_address(2),
		D => sl_data_write(2),
		Q => sl_data_read(2),
		RA_O => D_RA2, 
		RC_O => D_RC2,
		T_RD =>	D_Tristate_2,
		RD_O => D_Data_W2,
		RD_I => D_Data_R2
		);
		
	port3: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(3),
		A => sl_address(3),
		D => sl_data_write(3),
		Q => sl_data_read(3),
		RA_O => D_RA3, 
		RC_O => D_RC3,
		T_RD =>	D_Tristate_3,
		RD_O => D_Data_W3,
		RD_I => D_Data_R3
		);
		
	port4: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(4),
		A => sl_address(4),
		D => sl_data_write(4),
		Q => sl_data_read(4),
		RA_O => D_RA4, 
		RC_O => D_RC4,
		T_RD =>	D_Tristate_4,
		RD_O => D_Data_W4,
		RD_I => D_Data_R4
		);
		
	port5: zbt_port_from_design 
	port map(
		CLK => ng_clk,
		RST => RST,
		WR => sl_write(5),
		A => sl_address(5),
		D => sl_data_write(5),
		Q => sl_data_read(5),
		RA_O => D_RA5, 
		RC_O => D_RC5,
		T_RD =>	D_Tristate_5,
		RD_O => D_Data_W5,
		RD_I => D_Data_R5
		);
			  
--	stat_reg(31 downto 1) <= (others => '0');
	sl_rst <= RST or not command_reg(comReg_bit_EXE);

end RTL;

-- synopsys translate_off
--  configuration level of compaan_design is
--	for RTL
--		for kpn_net : kpn
--			use entity work.kpn(rtl);
--		end for;
--	end for;
--end level;
-- synopsys translate_on