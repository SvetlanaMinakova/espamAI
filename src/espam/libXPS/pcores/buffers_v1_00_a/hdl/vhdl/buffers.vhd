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
--  File: C:\raikin\fpga_template\active_hdl\top_level\top_level\src\buffers.vhd
--  created by Design Wizard: 10/18/02 12:13:32
--

--{{ Section below this comment is automatically maintained
--   and may be overwritten
--{entity {buffers} architecture {buffers}}

library IEEE;
use IEEE.std_logic_1164.all;

entity buffers is
	port (
		I0: out STD_LOGIC_VECTOR (31 downto 0); --date read from bank 0
		I1: out STD_LOGIC_VECTOR (31 downto 0);
		I2: out STD_LOGIC_VECTOR (31 downto 0);
		I3: out STD_LOGIC_VECTOR (31 downto 0);
		I4: out STD_LOGIC_VECTOR (31 downto 0);
		I5: out STD_LOGIC_VECTOR (31 downto 0);
		O0: in STD_LOGIC_VECTOR (31 downto 0);	-- date write to bank 0
		O1: in STD_LOGIC_VECTOR (31 downto 0);
		O2: in STD_LOGIC_VECTOR (31 downto 0);
		O3: in STD_LOGIC_VECTOR (31 downto 0);
		O4: in STD_LOGIC_VECTOR (31 downto 0);
		O5: in STD_LOGIC_VECTOR (31 downto 0);
		T0: in STD_LOGIC_VECTOR (31 downto 0);	-- tristate
		T1: in STD_LOGIC_VECTOR (31 downto 0);
		T2: in STD_LOGIC_VECTOR (31 downto 0);
		T3: in STD_LOGIC_VECTOR (31 downto 0);
		T4: in STD_LOGIC_VECTOR (31 downto 0);
		T5: in STD_LOGIC_VECTOR (31 downto 0);
		rd0: inout STD_LOGIC_VECTOR (31 downto 0); --inout to/from ssram
		rd1: inout STD_LOGIC_VECTOR (31 downto 0);
		rd2: inout STD_LOGIC_VECTOR (31 downto 0);
		rd3: inout STD_LOGIC_VECTOR (31 downto 0);
		rd4: inout STD_LOGIC_VECTOR (31 downto 0);
		rd5: inout STD_LOGIC_VECTOR (31 downto 0)
	);
end buffers;

--}} End of automatically maintained section

architecture buffers of buffers is

	component zbt_dpins is
        port(
            o:           in         std_logic_vector(31 downto 0);
            i:           out        std_logic_vector(31 downto 0);
            t:           in         std_logic_vector(31 downto 0);
            io:          inout      std_logic_vector(31 downto 0));
    end component;
	
	--signal sl_IO  : STD_LOGIC_VECTOR (31 downto 0);
	
begin
	 
    iobufs0: zbt_dpins
    port map(i => I0, o => O0, t => T0, io => rd0); 
	--iobufs0: zbt_dpins
    --port map(i => sl_IO, o => O0, t => T0, io => rd0); 
	--IO <= "00000000000000000000000000000111"; --debug for the zbt_if_ctrl

    iobufs1: zbt_dpins
    port map(i => I1, o => O1, t => T1, io => rd1);

    iobufs2: zbt_dpins
    port map(i => I2, o => O2, t => T2, io => rd2);

    iobufs3: zbt_dpins
    port map(i => I3, o => O3, t => T3, io => rd3);

    iobufs4: zbt_dpins
    port map(i => I4, o => O4, t => T4, io => rd4);

    iobufs5: zbt_dpins
    port map(i => I5, o => O5, t => T5, io => rd5);
		
end buffers;
