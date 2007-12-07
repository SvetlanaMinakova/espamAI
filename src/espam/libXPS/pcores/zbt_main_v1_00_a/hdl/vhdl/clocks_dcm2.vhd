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
-- clocks_dcm2.vhd - clock generator module for ZBT design, using
--                   Virtex-II DCMs
--
--                   Generates 2 SSRAM clocks.
--
-- (c) Alpha Data Parallel Systems Ltd. 1999-2001
--
-- Example program for ADM-XRCII-L/ADM-XRCII
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_unsigned.all;

-- synopsys translate_off
library	virtex2;
use virtex2.all;
-- synopsys translate_on


entity clocks is
    generic(
        num_clock     : in    natural);
    port(
        rst           : in    std_logic;
        lclk          : in    std_logic;
        mclk          : in    std_logic;
        clk           : out   std_logic;
        ramclko       : out   std_logic_vector(num_clock - 1 downto 0);
        ramclki       : in    std_logic_vector(num_clock - 1 downto 0);
        locked        : out   std_logic_vector(31 downto 0));
end clocks;

architecture synthesis of clocks is

    signal lclk_ibufg     : std_logic;
    signal locked_lclk    : std_logic;
    signal locked_ramclk  : std_logic_vector(num_clock - 1 downto 0);
    signal ramclki_ibufg  : std_logic_vector(num_clock - 1 downto 0);
    signal ramclki_bufg   : std_logic_vector(num_clock - 1 downto 0);
    signal ramclki_i      : std_logic_vector(num_clock - 1 downto 0);
    signal i_ramclko      : std_logic_vector(num_clock - 1 downto 0);
    signal clk0           : std_logic;
    signal i_clk          : std_logic;
    
    signal logic0         : std_logic;
    signal logic1         : std_logic;

    signal dllrst         : std_logic;
    signal rstcnt         : std_logic_vector(2 downto 0); -- should be returned to 23 downto 0 !!!!!!!
    signal rst_ramclk     : std_logic;

    signal mclk_ibufg     : std_logic;
    signal mclk_bufg      : std_logic;

    component DCM
        port(
            CLKIN         : in  std_logic;
            CLKFB         : in  std_logic;
            DSSEN         : in  std_logic;
            PSINCDEC      : in  std_logic;
            PSEN          : in  std_logic;
            PSCLK         : in  std_logic;
            RST           : in  std_logic;
            CLK0          : out std_logic;
            CLK90         : out std_logic;
            CLK180        : out std_logic;
            CLK270        : out std_logic;
            CLK2X         : out std_logic;
            CLK2X180      : out std_logic;
            CLKDV         : out std_logic;
            CLKFX         : out std_logic;
            CLKFX180      : out std_logic;
            LOCKED        : out std_logic;
            PSDONE        : out std_logic;
            STATUS        : out std_logic_vector(7 downto 0));
    end component;

    component IBUFG
        port(
            I : in  std_logic;
            O : out std_logic);
    end component;
    
    component BUFG
        port(
            I : in  std_logic;
            O : out std_logic);
    end component;
    
    attribute DLL_FREQUENCY_MODE : string;
    attribute DUTY_CYCLE_CORRECTION : string;
    attribute STARTUP_WAIT : string;
    
    --
    -- We can't use STARTUP_WAIT = TRUE, because we might be
    -- targetting a Virtex-II ES device.
    --
    attribute DLL_FREQUENCY_MODE of dll_lclk : label is "LOW";
    attribute DUTY_CYCLE_CORRECTION of dll_lclk : label is "TRUE";
    attribute STARTUP_WAIT of dll_lclk : label is "FALSE";
    attribute DLL_FREQUENCY_MODE of dll0 : label is "LOW";
    attribute DUTY_CYCLE_CORRECTION of dll0 : label is "TRUE";
    attribute STARTUP_WAIT of dll0 : label is "FALSE";
    attribute DLL_FREQUENCY_MODE of dll1 : label is "LOW";
    attribute DUTY_CYCLE_CORRECTION of dll1 : label is "TRUE";
    attribute STARTUP_WAIT of dll1 : label is "FALSE";
    
begin
    
    --
    -- Define constant values
    --
    logic0 <= '0';
    logic1 <= '1';

    locked <= EXT(locked_ramclk & locked_lclk, 32);
    
    --
    -- Input MCLK
    --

    ibufg_mclk : IBUFG
        port map(
            I => mclk,
            O => mclk_ibufg);
    
    bufg_mclk : BUFG
        port map(
            I => mclk_ibufg,
            O => mclk_bufg);
    
    --
    -- Generate reset signal to DLLs/DCMs
    --
    gen_dllrst : process(rst, mclk_bufg)
    begin
        if rst = '1' then
            dllrst <= '1';
            rstcnt <= (others => '0');
        elsif mclk_bufg'event and mclk_bufg = '1' then
            if dllrst = '1' then
                rstcnt <= rstcnt + 1;
            end if;
            if AND_reduce(rstcnt) = '1' then
                dllrst <= '0';
            end if;
        end if;
    end process;
    
    --
    -- Input the local bus clock
    --

    ibufg_lclk : IBUFG
        port map(
            I => lclk,
            O => lclk_ibufg);
    
    dll_lclk : DCM
        port map(
            CLKIN    => lclk_ibufg,
            CLKFB    => i_clk,
            DSSEN    => logic0,
            PSINCDEC => logic0,
            PSEN     => logic0,
            PSCLK    => logic0,
            RST      => dllrst,
            CLK0     => clk0,
            LOCKED   => locked_lclk);

    bufg_lclk : BUFG
        port map(
            I => clk0,
            O => i_clk);

    clk <= i_clk;

    --
    -- Generate the SSRAM clocks
    --
    
    rst_ramclk <= not locked_lclk;
    ramclko    <= i_ramclko;
    
    ibufg0: IBUFG
        port map(
            I => ramclki(0),
            O => ramclki_ibufg(0));
            
    dll0 : DCM
        port map(
            CLKIN    => i_clk,
            CLKFB    => ramclki_ibufg(0),
            DSSEN    => logic0,
            PSINCDEC => logic0,
            PSEN     => logic0,
            PSCLK    => logic0,
            RST      => rst_ramclk,
            CLK0     => i_ramclko(0),
            LOCKED   => locked_ramclk(0));

    ibufg1: IBUFG
        port map(
            I => ramclki(1),
            O => ramclki_ibufg(1));

    dll1 : DCM
        port map(
            CLKIN    => i_clk,
            CLKFB    => ramclki_ibufg(1),
            DSSEN    => logic0,
            PSINCDEC => logic0,
            PSEN     => logic0,
            PSCLK    => logic0,
            RST      => rst_ramclk,
            CLK0     => i_ramclko(1),
            LOCKED   => locked_ramclk(1));

end synthesis;
