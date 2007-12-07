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
-- file C:\raikin\fpga_template\active_hdl\top_level\top_level\compile\top_level.vhd
-- generated Thu Nov  7 14:57:30 2002
-- from C:\raikin\fpga_template\active_hdl\top_level\top_level\src\top_level.bde
-- by BDE2VHDL generator version 1.10
--
library IEEE;
use IEEE.std_logic_1164.all;
--Library cypress;
--use cypress.cypress.all;

entity top_level is
  port(
       fholda : in STD_LOGIC;
       lads_l : in STD_LOGIC;
       lblast_l : in STD_LOGIC;
       lclk : in STD_LOGIC;
       lreseto_l : in STD_LOGIC;
       lwrite : in STD_LOGIC;
       mclk : in STD_LOGIC;
       la : in STD_LOGIC_VECTOR (23 downto 2);
       lbe_l : in STD_LOGIC_VECTOR (3 downto 0);
       ramclki : in STD_LOGIC_VECTOR (1 downto 0);
       flothru_l : out STD_LOGIC;
       global_lbo_l : out STD_LOGIC;
       lreadyi_l : out STD_LOGIC;
       ra0 : out STD_LOGIC_VECTOR (19 downto 0);
       ra1 : out STD_LOGIC_VECTOR (19 downto 0);
       ra2 : out STD_LOGIC_VECTOR (19 downto 0);
       ra3 : out STD_LOGIC_VECTOR (19 downto 0);
       ra4 : out STD_LOGIC_VECTOR (19 downto 0);
       ra5 : out STD_LOGIC_VECTOR (19 downto 0);
       ramclko : out STD_LOGIC_VECTOR (1 downto 0);
       rc0 : out STD_LOGIC_VECTOR (8 downto 0);
       rc1 : out STD_LOGIC_VECTOR (8 downto 0);
       rc2 : out STD_LOGIC_VECTOR (8 downto 0);
       rc3 : out STD_LOGIC_VECTOR (8 downto 0);
       rc4 : out STD_LOGIC_VECTOR (8 downto 0);
       rc5 : out STD_LOGIC_VECTOR (8 downto 0);
       lbterm_l : inout STD_LOGIC;
       ld : inout STD_LOGIC_VECTOR (31 downto 0);
       rd0 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd1 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd2 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd3 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd4 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd5 : inout STD_LOGIC_VECTOR (31 downto 0)
  );
end top_level;

architecture TOP_LEVEL of top_level is

---- Component declarations -----

component buffers
  port (
       O0 : in STD_LOGIC_VECTOR (31 downto 0);
       O1 : in STD_LOGIC_VECTOR (31 downto 0);
       O2 : in STD_LOGIC_VECTOR (31 downto 0);
       O3 : in STD_LOGIC_VECTOR (31 downto 0);
       O4 : in STD_LOGIC_VECTOR (31 downto 0);
       O5 : in STD_LOGIC_VECTOR (31 downto 0);
       T0 : in STD_LOGIC_VECTOR (31 downto 0);
       T1 : in STD_LOGIC_VECTOR (31 downto 0);
       T2 : in STD_LOGIC_VECTOR (31 downto 0);
       T3 : in STD_LOGIC_VECTOR (31 downto 0);
       T4 : in STD_LOGIC_VECTOR (31 downto 0);
       T5 : in STD_LOGIC_VECTOR (31 downto 0);
       I0 : out STD_LOGIC_VECTOR (31 downto 0);
       I1 : out STD_LOGIC_VECTOR (31 downto 0);
       I2 : out STD_LOGIC_VECTOR (31 downto 0);
       I3 : out STD_LOGIC_VECTOR (31 downto 0);
       I4 : out STD_LOGIC_VECTOR (31 downto 0);
       I5 : out STD_LOGIC_VECTOR (31 downto 0);
       rd0 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd1 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd2 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd3 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd4 : inout STD_LOGIC_VECTOR (31 downto 0);
       rd5 : inout STD_LOGIC_VECTOR (31 downto 0)
  );
end component;
component compaan_design
  port (
       CLK : in STD_LOGIC;
       D_Data_R0 : in STD_LOGIC_VECTOR (31 downto 0);
       D_Data_R1 : in STD_LOGIC_VECTOR (31 downto 0);
       D_Data_R2 : in STD_LOGIC_VECTOR (31 downto 0);
       D_Data_R3 : in STD_LOGIC_VECTOR (31 downto 0);
       D_Data_R4 : in STD_LOGIC_VECTOR (31 downto 0);
       D_Data_R5 : in STD_LOGIC_VECTOR (31 downto 0);
       RST : in STD_LOGIC;
       D_Data_W0 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Data_W1 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Data_W2 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Data_W3 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Data_W4 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Data_W5 : out STD_LOGIC_VECTOR (31 downto 0);
       D_RA0 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RA1 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RA2 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RA3 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RA4 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RA5 : out STD_LOGIC_VECTOR (19 downto 0);
       D_RC0 : out STD_LOGIC_VECTOR (8 downto 0);
       D_RC1 : out STD_LOGIC_VECTOR (8 downto 0);
       D_RC2 : out STD_LOGIC_VECTOR (8 downto 0);
       D_RC3 : out STD_LOGIC_VECTOR (8 downto 0);
       D_RC4 : out STD_LOGIC_VECTOR (8 downto 0);
       D_RC5 : out STD_LOGIC_VECTOR (8 downto 0);
       D_Tristate_0 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Tristate_1 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Tristate_2 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Tristate_3 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Tristate_4 : out STD_LOGIC_VECTOR (31 downto 0);
       D_Tristate_5 : out STD_LOGIC_VECTOR (31 downto 0);

	   COMMAND_REG   : in STD_LOGIC_VECTOR(31 downto 0);   
	   PARAMETER_REG : in std_logic_vector(31 downto 0);
	   STAT_REG      : out STD_LOGIC_VECTOR(31 downto 0)
  );
end component;
component mux
  port (
       CNTRL : in STD_LOGIC_VECTOR (31 downto 0);
       D_AD0 : in STD_LOGIC_VECTOR (19 downto 0);
       D_AD1 : in STD_LOGIC_VECTOR (19 downto 0);
       D_AD2 : in STD_LOGIC_VECTOR (19 downto 0);
       D_AD3 : in STD_LOGIC_VECTOR (19 downto 0);
       D_AD4 : in STD_LOGIC_VECTOR (19 downto 0);
       D_AD5 : in STD_LOGIC_VECTOR (19 downto 0);
       D_CO0 : in STD_LOGIC_VECTOR (8 downto 0);
       D_CO1 : in STD_LOGIC_VECTOR (8 downto 0);
       D_CO2 : in STD_LOGIC_VECTOR (8 downto 0);
       D_CO3 : in STD_LOGIC_VECTOR (8 downto 0);
       D_CO4 : in STD_LOGIC_VECTOR (8 downto 0);
       D_CO5 : in STD_LOGIC_VECTOR (8 downto 0);
       D_DW0 : in STD_LOGIC_VECTOR (31 downto 0);
       D_DW1 : in STD_LOGIC_VECTOR (31 downto 0);
       D_DW2 : in STD_LOGIC_VECTOR (31 downto 0);
       D_DW3 : in STD_LOGIC_VECTOR (31 downto 0);
       D_DW4 : in STD_LOGIC_VECTOR (31 downto 0);
       D_DW5 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI0 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI1 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI2 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI3 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI4 : in STD_LOGIC_VECTOR (31 downto 0);
       D_TRI5 : in STD_LOGIC_VECTOR (31 downto 0);
       H_AD0 : in STD_LOGIC_VECTOR (19 downto 0);
       H_AD1 : in STD_LOGIC_VECTOR (19 downto 0);
       H_AD2 : in STD_LOGIC_VECTOR (19 downto 0);
       H_AD3 : in STD_LOGIC_VECTOR (19 downto 0);
       H_AD4 : in STD_LOGIC_VECTOR (19 downto 0);
       H_AD5 : in STD_LOGIC_VECTOR (19 downto 0);
       H_CO0 : in STD_LOGIC_VECTOR (8 downto 0);
       H_CO1 : in STD_LOGIC_VECTOR (8 downto 0);
       H_CO2 : in STD_LOGIC_VECTOR (8 downto 0);
       H_CO3 : in STD_LOGIC_VECTOR (8 downto 0);
       H_CO4 : in STD_LOGIC_VECTOR (8 downto 0);
       H_CO5 : in STD_LOGIC_VECTOR (8 downto 0);
       H_DW0 : in STD_LOGIC_VECTOR (31 downto 0);
       H_DW1 : in STD_LOGIC_VECTOR (31 downto 0);
       H_DW2 : in STD_LOGIC_VECTOR (31 downto 0);
       H_DW3 : in STD_LOGIC_VECTOR (31 downto 0);
       H_DW4 : in STD_LOGIC_VECTOR (31 downto 0);
       H_DW5 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI0 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI1 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI2 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI3 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI4 : in STD_LOGIC_VECTOR (31 downto 0);
       H_TRI5 : in STD_LOGIC_VECTOR (31 downto 0);
       RST : in STD_LOGIC;
       DW0 : out STD_LOGIC_VECTOR (31 downto 0);
       DW1 : out STD_LOGIC_VECTOR (31 downto 0);
       DW2 : out STD_LOGIC_VECTOR (31 downto 0);
       DW3 : out STD_LOGIC_VECTOR (31 downto 0);
       DW4 : out STD_LOGIC_VECTOR (31 downto 0);
       DW5 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI0 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI1 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI2 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI3 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI4 : out STD_LOGIC_VECTOR (31 downto 0);
       TRI5 : out STD_LOGIC_VECTOR (31 downto 0);
       ra0 : out STD_LOGIC_VECTOR (19 downto 0);
       ra1 : out STD_LOGIC_VECTOR (19 downto 0);
       ra2 : out STD_LOGIC_VECTOR (19 downto 0);
       ra3 : out STD_LOGIC_VECTOR (19 downto 0);
       ra4 : out STD_LOGIC_VECTOR (19 downto 0);
       ra5 : out STD_LOGIC_VECTOR (19 downto 0);
       rc0 : out STD_LOGIC_VECTOR (8 downto 0);
       rc1 : out STD_LOGIC_VECTOR (8 downto 0);
       rc2 : out STD_LOGIC_VECTOR (8 downto 0);
       rc3 : out STD_LOGIC_VECTOR (8 downto 0);
       rc4 : out STD_LOGIC_VECTOR (8 downto 0);
       rc5 : out STD_LOGIC_VECTOR (8 downto 0)
  );
end component;
component zbt_main
  generic(
       addr_width : NATURAL := 20;
       bank_group : NATURAL := 1;
       ctl_width : NATURAL := 9;
       data_width : NATURAL := 32;
       num_bank : NATURAL := 6;
       num_clock : NATURAL := 2
  );
  port (
       H_Data_R0 : in STD_LOGIC_VECTOR (31 downto 0);
       H_Data_R1 : in STD_LOGIC_VECTOR (31 downto 0);
       H_Data_R2 : in STD_LOGIC_VECTOR (31 downto 0);
       H_Data_R3 : in STD_LOGIC_VECTOR (31 downto 0);
       H_Data_R4 : in STD_LOGIC_VECTOR (31 downto 0);
       H_Data_R5 : in STD_LOGIC_VECTOR (31 downto 0);
       fholda : in STD_LOGIC;
       la : in STD_LOGIC_VECTOR (23 downto 2);
       lads_l : in STD_LOGIC;
       lbe_l : in STD_LOGIC_VECTOR (3 downto 0);
       lblast_l : in STD_LOGIC;
       lclk : in STD_LOGIC;
       lreseto_l : in STD_LOGIC;
       lwrite : in STD_LOGIC;
       mclk : in STD_LOGIC;
       ramclki : in STD_LOGIC_VECTOR (num_clock-1 downto 0);
       CLK_out : out STD_LOGIC;
       H_Data_W0 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Data_W1 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Data_W2 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Data_W3 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Data_W4 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Data_W5 : out STD_LOGIC_VECTOR (31 downto 0);
       H_RA0 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RA1 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RA2 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RA3 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RA4 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RA5 : out STD_LOGIC_VECTOR (19 downto 0);
       H_RC0 : out STD_LOGIC_VECTOR (8 downto 0);
       H_RC1 : out STD_LOGIC_VECTOR (8 downto 0);
       H_RC2 : out STD_LOGIC_VECTOR (8 downto 0);
       H_RC3 : out STD_LOGIC_VECTOR (8 downto 0);
       H_RC4 : out STD_LOGIC_VECTOR (8 downto 0);
       H_RC5 : out STD_LOGIC_VECTOR (8 downto 0);
       H_Tristate_0 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Tristate_1 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Tristate_2 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Tristate_3 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Tristate_4 : out STD_LOGIC_VECTOR (31 downto 0);
       H_Tristate_5 : out STD_LOGIC_VECTOR (31 downto 0);
       RST_out : out STD_LOGIC;
       flothru_l : out STD_LOGIC;
       global_lbo_l : out STD_LOGIC;
       lreadyi_l : out STD_LOGIC;
       ramclko : out STD_LOGIC_VECTOR (num_clock-1 downto 0);
       lbterm_l : inout STD_LOGIC;
       ld : inout STD_LOGIC_VECTOR (31 downto 0);
       
	   COMMAND_reg : out STD_LOGIC_VECTOR (31 downto 0);
	   PARAMETER_REG   : out   std_logic_vector(31 downto 0);
       DESIGN_STAT_REG : in STD_LOGIC_VECTOR (31 downto 0)
  );
end component;

---- Signal declarations used on the diagram ----

signal DA0 : STD_LOGIC_VECTOR (19 downto 0);
signal DA1 : STD_LOGIC_VECTOR (19 downto 0);
signal DA2 : STD_LOGIC_VECTOR (19 downto 0);
signal DA3 : STD_LOGIC_VECTOR (19 downto 0);
signal DA4 : STD_LOGIC_VECTOR (19 downto 0);
signal DA5 : STD_LOGIC_VECTOR (19 downto 0);
signal DC0 : STD_LOGIC_VECTOR (8 downto 0);
signal DC1 : STD_LOGIC_VECTOR (8 downto 0);
signal DC2 : STD_LOGIC_VECTOR (8 downto 0);
signal DC3 : STD_LOGIC_VECTOR (8 downto 0);
signal DC4 : STD_LOGIC_VECTOR (8 downto 0);
signal DC5 : STD_LOGIC_VECTOR (8 downto 0);
signal DI0 : STD_LOGIC_VECTOR (31 downto 0);
signal DI1 : STD_LOGIC_VECTOR (31 downto 0);
signal DI2 : STD_LOGIC_VECTOR (31 downto 0);
signal DI3 : STD_LOGIC_VECTOR (31 downto 0);
signal DI4 : STD_LOGIC_VECTOR (31 downto 0);
signal DI5 : STD_LOGIC_VECTOR (31 downto 0);
signal DT0 : STD_LOGIC_VECTOR (31 downto 0);
signal DT1 : STD_LOGIC_VECTOR (31 downto 0);
signal DT2 : STD_LOGIC_VECTOR (31 downto 0);
signal DT3 : STD_LOGIC_VECTOR (31 downto 0);
signal DT4 : STD_LOGIC_VECTOR (31 downto 0);
signal DT5 : STD_LOGIC_VECTOR (31 downto 0);
signal DW0 : STD_LOGIC_VECTOR (31 downto 0);
signal DW1 : STD_LOGIC_VECTOR (31 downto 0);
signal DW2 : STD_LOGIC_VECTOR (31 downto 0);
signal DW3 : STD_LOGIC_VECTOR (31 downto 0);
signal DW4 : STD_LOGIC_VECTOR (31 downto 0);
signal DW5 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS1085 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS1093 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS1125 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS1133 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1141 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS1149 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1157 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS1165 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1316 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS1324 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1332 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS14342 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14346 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14350 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14354 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14358 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14362 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS14366 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS1528 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1536 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS1544 : STD_LOGIC_VECTOR (19 downto 0);
signal BUS1552 : STD_LOGIC_VECTOR (8 downto 0);
signal BUS17478 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS19945 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS6561 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS6569 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS805 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS813 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS821 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS829 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS837 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS845 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS853 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS883 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS891 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS899 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS907 : STD_LOGIC_VECTOR (31 downto 0);
signal BUS915 : STD_LOGIC_VECTOR (31 downto 0);
signal NET14497 : STD_LOGIC;
signal NET17498 : STD_LOGIC; 
signal sl_parameter : std_logic_vector(31 downto 0);

---- Configuration specifications for declared components 

--for U3 : mux use entity work.mux(mux);
--for U2 : buffers use entity work.buffers(buffers);

begin

----  Component instantiations  ----

U1 : zbt_main
  port map(
       CLK_out => NET14497,
       H_Data_R0 => DI0,
       H_Data_R1 => DI1,
       H_Data_R2 => DI2,
       H_Data_R3 => DI3,
       H_Data_R4 => DI4,
       H_Data_R5 => DI5,
       H_Data_W0 => BUS14342,
       H_Data_W1 => BUS14350,
       H_Data_W2 => BUS14358,
       H_Data_W3 => BUS14366,
       H_Data_W4 => BUS1093,
       H_Data_W5 => BUS6569,
       H_RA0 => BUS1133,
       H_RA1 => BUS1149,
       H_RA2 => BUS1165,
       H_RA3 => BUS1324,
       H_RA4 => BUS1528,
       H_RA5 => BUS1544,
       H_RC0 => BUS1141,
       H_RC1 => BUS1157,
       H_RC2 => BUS1316,
       H_RC3 => BUS1332,
       H_RC4 => BUS1536,
       H_RC5 => BUS1552,
       H_Tristate_0 => BUS14346,
       H_Tristate_1 => BUS14354,
       H_Tristate_2 => BUS14362,
       H_Tristate_3 => BUS1085,
       H_Tristate_4 => BUS6561,
       H_Tristate_5 => BUS1125,
       RST_out => NET17498,
       command_reg => BUS19945,
       design_stat_reg => BUS17478,	
	   PARAMETER_REG => sl_parameter,
       fholda => fholda,
       flothru_l => flothru_l,
       global_lbo_l => global_lbo_l,
       la => la,
       lads_l => lads_l,
       lbe_l => lbe_l,
       lblast_l => lblast_l,
       lbterm_l => lbterm_l,
       lclk => lclk,
       ld => ld,
       lreadyi_l => lreadyi_l,
       lreseto_l => lreseto_l,
       lwrite => lwrite,
       mclk => mclk,
       ramclki => ramclki( 1 downto 0 ),
       ramclko => ramclko( 1 downto 0 )
  );

U2 : buffers
  port map(
       I0 => DI0,
       I1 => DI1,
       I2 => DI2,
       I3 => DI3,
       I4 => DI4,
       I5 => DI5,
       O0 => BUS853,
       O1 => BUS883,
       O2 => BUS915,
       O3 => BUS907,
       O4 => BUS899,
       O5 => BUS891,
       T0 => BUS805,
       T1 => BUS813,
       T2 => BUS821,
       T3 => BUS829,
       T4 => BUS837,
       T5 => BUS845,
       rd0 => rd0,
       rd1 => rd1,
       rd2 => rd2,
       rd3 => rd3,
       rd4 => rd4,
       rd5 => rd5
  );

U3 : mux
  port map(
       CNTRL => BUS19945,
       DW0 => BUS853,
       DW1 => BUS883,
       DW2 => BUS915,
       DW3 => BUS907,
       DW4 => BUS899,
       DW5 => BUS891,
       D_AD0 => DA0,
       D_AD1 => DA1,
       D_AD2 => DA2,
       D_AD3 => DA3,
       D_AD4 => DA4,
       D_AD5 => DA5,
       D_CO0 => DC0,
       D_CO1 => DC1,
       D_CO2 => DC2,
       D_CO3 => DC3,
       D_CO4 => DC4,
       D_CO5 => DC5,
       D_DW0 => DW0,
       D_DW1 => DW1,
       D_DW2 => DW2,
       D_DW3 => DW3,
       D_DW4 => DW4,
       D_DW5 => DW5,
       D_TRI0 => DT0,
       D_TRI1 => DT1,
       D_TRI2 => DT2,
       D_TRI3 => DT3,
       D_TRI4 => DT4,
       D_TRI5 => DT5,
       H_AD0 => BUS1133,
       H_AD1 => BUS1149,
       H_AD2 => BUS1165,
       H_AD3 => BUS1324,
       H_AD4 => BUS1528,
       H_AD5 => BUS1544,
       H_CO0 => BUS1141,
       H_CO1 => BUS1157,
       H_CO2 => BUS1316,
       H_CO3 => BUS1332,
       H_CO4 => BUS1536,
       H_CO5 => BUS1552,
       H_DW0 => BUS14342,
       H_DW1 => BUS14350,
       H_DW2 => BUS14358,
       H_DW3 => BUS14366,
       H_DW4 => BUS1093,
       H_DW5 => BUS6569,
       H_TRI0 => BUS14346,
       H_TRI1 => BUS14354,
       H_TRI2 => BUS14362,
       H_TRI3 => BUS1085,
       H_TRI4 => BUS6561,
       H_TRI5 => BUS1125,
       RST => NET17498,
       TRI0 => BUS805,
       TRI1 => BUS813,
       TRI2 => BUS821,
       TRI3 => BUS829,
       TRI4 => BUS837,
       TRI5 => BUS845,
       ra0 => ra0,
       ra1 => ra1,
       ra2 => ra2,
       ra3 => ra3,
       ra4 => ra4,
       ra5 => ra5,
       rc0 => rc0,
       rc1 => rc1,
       rc2 => rc2,
       rc3 => rc3,
       rc4 => rc4,
       rc5 => rc5
  );

U4 : compaan_design
  port map(
       CLK => NET14497,
       D_Data_R0 => DI0,
       D_Data_R1 => DI1,
       D_Data_R2 => DI2,
       D_Data_R3 => DI3,
       D_Data_R4 => DI4,
       D_Data_R5 => DI5,
       D_Data_W0 => DW0,
       D_Data_W1 => DW1,
       D_Data_W2 => DW2,
       D_Data_W3 => DW3,
       D_Data_W4 => DW4,
       D_Data_W5 => DW5,
       D_RA0 => DA0,
       D_RA1 => DA1,
       D_RA2 => DA2,
       D_RA3 => DA3,
       D_RA4 => DA4,
       D_RA5 => DA5,
       D_RC0 => DC0,
       D_RC1 => DC1,
       D_RC2 => DC2,
       D_RC3 => DC3,
       D_RC4 => DC4,
       D_RC5 => DC5,
       D_Tristate_0 => DT0,
       D_Tristate_1 => DT1,
       D_Tristate_2 => DT2,
       D_Tristate_3 => DT3,
       D_Tristate_4 => DT4,
       D_Tristate_5 => DT5,
       RST => NET17498,
       command_reg => BUS19945,	
	   PARAMETER_REG => sl_parameter,
       stat_reg => BUS17478
  );


end TOP_LEVEL;
