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

-------------------------------------------------------------------------------
-- $Id: opb_zbt_controller_core.vhd,v 1.1 2007/12/07 22:08:52 stefanov Exp $
-------------------------------------------------------------------------------
-- opb_zbt_controller_core.vhd
-------------------------------------------------------------------------------
--
--                  ****************************
--                  ** Copyright Xilinx, Inc. **
--                  ** All rights reserved.   **
--                  ****************************
--
-------------------------------------------------------------------------------
-- Filename:        opb_zbt_controller_core.vhd
--
-- Description:     
--                  
-- VHDL-Standard:   VHDL'93
-------------------------------------------------------------------------------
-- Structure:   
--              opb_zbt_controller_core.vhd
-------------------------------------------------------------------------------
-- Naming Conventions:
--      active low signals:                     "*_n"
--      clock signals:                          "clk", "clk_div#", "clk_#x" 
--      reset signals:                          "rst", "rst_n" 
--      generics:                               "C_*" 
--      user defined types:                     "*_TYPE" 
--      state machine next state:               "*_ns" 
--      state machine current state:            "*_cs" 
--      combinatorial signals:                  "*_com" 
--      pipelined or register delay signals:    "*_d#" 
--      counter signals:                        "*cnt*"
--      clock enable signals:                   "*_ce" 
--      internal version of output port         "*_i"
--      device pins:                            "*_pin" 
--      ports:                                  - Names begin with Uppercase 
--      processes:                              "*_PROCESS" 
--      component instantiations:               "<ENTITY_>I_<#|FUNC>
-------------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;

library common_v1_00_a;
use common_v1_00_a.pselect;

library unisim;
use unisim.all;

entity Opb_zbt_controller_core is
  generic (
    C_BASEADDR      : std_logic_vector(0 to 31) := X"FA000000";
    C_HIGHADDR      : std_logic_vector(0 to 31) := X"FA3FFFFF";
    C_ZBT_ADDR_SIZE : integer                   := 20;
    -- If C_EXTERNAL_DLL is true (1) then
    -- ZBT_Clk is driven outside this module
    -- and ZBT_Clk_FBOut is also driven outside this module
    C_EXTERNAL_DLL  : integer                   := 0
    );
  port (
    OPB_Clk : in std_logic;
    OPB_Rst : in std_logic;

    -- OPB signals
    OPB_ABus    : in std_logic_vector(0 to 31);
    OPB_BE      : in std_logic_vector(0 to 3);
    OPB_RNW     : in std_logic;
    OPB_select  : in std_logic;
    OPB_seqAddr : in std_logic;
    OPB_DBus    : in std_logic_vector(0 to 31);

    ZBT_DBus    : out std_logic_vector(0 to 31);
    ZBT_errAck  : out std_logic;
    ZBT_retry   : out std_logic;
    ZBT_toutSup : out std_logic;
    ZBT_xferAck : out std_logic;

    -- ZBT Memory signals
    ZBT_Clk_FB    : in  std_logic;
    ZBT_Clk_FBOut : out std_logic;
    ZBT_Clk       : out std_logic;
    ZBT_OE_N      : out std_logic;
    ZBT_ADV_LD_N  : out std_logic;
    ZBT_LBO_N     : out std_logic;
    ZBT_CE1_N     : out std_logic;
    ZBT_CE2_N     : out std_logic;
    ZBT_CE2       : out std_logic;
    ZBT_RW_N      : out std_logic;
    ZBT_CKE_N     : out std_logic;
    ZBT_A         : out std_logic_vector(0 to C_ZBT_ADDR_SIZE-1);
    ZBT_BW_N      : out std_logic_vector(1 to 4);
    ZBT_IO_I      : in  std_logic_vector(0 to 31);
    ZBT_IO_O      : out std_logic_vector(0 to 31);
    ZBT_IO_T      : out std_logic;
    ZBT_IOP_I     : in  std_logic_vector(1 to 4);
    ZBT_IOP_O     : out std_logic_vector(1 to 4);
    ZBT_IOP_T     : out std_logic
    );

end entity Opb_zbt_controller_core;

architecture IMP of Opb_zbt_controller_core is

  component IBUFG is
    port (
      I : in  std_logic;
      O : out std_logic);
  end component IBUFG;

  component CLKDLL
    port (
      CLKIN  : in  std_logic := '0';
      CLKFB  : in  std_logic := '0';
      RST    : in  std_logic := '0';
      CLK0   : out std_logic := '0';
      CLK90  : out std_logic := '0';
      CLK180 : out std_logic := '0';
      CLK270 : out std_logic := '0';
      CLK2X  : out std_logic := '0';
      CLKDV  : out std_logic := '0';
      LOCKED : out std_logic := '0');
  end component;

  component FDRE is
    -- pragma translate_off
    generic (
      INIT : bit);
    -- pragma translate_on
    port (
      Q  : out std_logic;
      C  : in  std_logic;
      CE : in  std_logic;
      D  : in  std_logic;
      R  : in  std_logic);
  end component FDRE;
  
  component pselect is
    generic (
      C_AB  : integer;
      C_AW  : integer;
      C_BAR : std_logic_vector);
    port (
      A      : in  std_logic_vector(0 to C_AW-1);
      AValid : in  std_logic;
      ps     : out std_logic);
  end component pselect;

  constant C_OPB_AWIDTH : natural := 32;
  
  function Addr_Bits (x, y : std_logic_vector(0 to C_OPB_AWIDTH-1)) return integer is
    variable addr_nor : std_logic_vector(0 to C_OPB_AWIDTH-1);
  begin
    addr_nor := x xor y;
    for i in 0 to C_OPB_AWIDTH-1 loop
      if addr_nor(i) = '1' then return i;
      end if;
    end loop;
    return(C_OPB_AWIDTH);
  end function Addr_Bits;

  constant C_AB : integer := Addr_Bits(C_HIGHADDR, C_BASEADDR);

  signal zbt_Clk_FB_I : std_logic;

  signal CLK0   : std_logic;
  signal CLK90  : std_logic;
  signal CLK180 : std_logic;
  signal CLK270 : std_logic;
  signal CLK2X  : std_logic;
  signal CLKDV  : std_logic;
  signal LOCKED : std_logic;

  signal zbt_rw_I_n : std_logic;
  signal zbt_ce2_I  : std_logic;

  signal zbt_wrD_2  : std_logic_vector(OPB_DBus'range);
  signal zbt_rw_2_n : std_logic;

  signal xfer_Ack_1 : std_logic;
  signal xfer_Ack_2 : std_logic;
  signal xfer_Ack_3 : std_logic;
  signal xfer_Ack_4 : std_logic;
  signal xfer_Ack   : std_logic;

  signal clk_i : std_logic;
  
  signal zbt_cs_I        : std_logic;
  signal zbt_cs_Previous : std_logic;
  signal zbt_cs          : std_logic;

  signal cs_rst     : std_logic;
  
begin  -- architecture IMP

  -----------------------------------------------------------------------------
  -- Detecting an access to the ZBT memory
  -----------------------------------------------------------------------------
  pselect_I : pselect
    generic map (
      C_AB   => C_AB,                   -- [integer]
      C_AW   => OPB_ABus'length,        -- [integer]
      C_BAR  => C_BASEADDR)             -- [std_logic_vector]
    port map (
      A      => OPB_ABus,               -- [in  std_logic_vector(0 to C_AW-1)]
      AValid => OPB_select,             -- [in  std_logic]
      ps     => zbt_cs_I);              -- [out std_logic]

  cs_rst <= OPB_Rst or xfer_Ack;
  
  zbt_cs_I_DFF : process (OPB_Clk) is
  begin  -- process zbt_cs_I_DFF
    if OPB_Clk'event and OPB_Clk = '1' then  -- rising clock edge
      if cs_Rst = '1' then               -- asynchronous reset (active high)
        zbt_cs_Previous <= '0';
      else
        zbt_cs_Previous <= zbt_cs_I;
      end if;
    end if;
  end process zbt_cs_I_DFF;

  zbt_cs <= zbt_cs_I and not zbt_cs_Previous;

  ZBT_errAck  <= '0';
  ZBT_retry   <= '0';
  ZBT_toutSup <= '0';

  -----------------------------------------------------------------------------
  -- Tie all unused control signals to correct values
  -----------------------------------------------------------------------------
  ZBT_ADV_LD_N <= '0';
  ZBT_CKE_N    <= '0';
  ZBT_LBO_N    <= '0';
  ZBT_CE1_N    <= '0';
  ZBT_CE2_N    <= '0';
  ZBT_OE_N     <= '0';

  -----------------------------------------------------------------------------
  -- Generating the ZBT_Clk
  -----------------------------------------------------------------------------

  Instanciate_DLL : if (C_EXTERNAL_DLL = 0) generate

    CLKDLL_I : CLKDLL
      port map (
        CLKIN  => OPB_Clk,               -- [in  std_logic := '0']
        CLKFB  => zbt_Clk_FB,           -- [in  std_logic := '0']
        RST    => OPB_Rst,                -- [in  std_logic := '0']
        CLK0   => clk_i,                -- [out std_logic := '0']
        CLK90  => CLK90,                -- [out std_logic := '0']
        CLK180 => CLK180,               -- [out std_logic := '0']
        CLK270 => CLK270,               -- [out std_logic := '0']
        CLK2X  => CLK2X,                -- [out std_logic := '0']
        CLKDV  => CLKDV,                -- [out std_logic := '0']
        LOCKED => LOCKED);              -- [out std_logic := '0']

    ZBT_Clk       <= clk_i;
    ZBT_Clk_FBOut <= clk_i;
    
  end generate Instanciate_DLL;

  -----------------------------------------------------------------------------
  -- Driving the address and control signals
  -----------------------------------------------------------------------------

  -- First clock all signals, these will hopefully be place in IOB
  Addr_Ctrl_DFFs : process (OPB_Clk) is
  begin  -- process Addr_Ctrl_DFFs
    if OPB_Clk'event and OPB_Clk = '1' then     -- rising clock edge
      zbt_rw_I_n <= OPB_RNW or not(ZBT_CS);
      zbt_ce2_I  <= ZBT_CS;
      if (ZBT_CS = '1') then
        ZBT_A    <= OPB_ABus(30-C_ZBT_ADDR_SIZE to 29);
        ZBT_BW_N <= not OPB_BE;
      end if;
    end if;
  end process Addr_Ctrl_DFFs;

  ZBT_RW_N <= zbt_rw_I_n;
  ZBT_CE2  <= zbt_ce2_I;

  -----------------------------------------------------------------------------
  -- Handling the Data signals
  -----------------------------------------------------------------------------
  Write_Data_DFFs : process (OPB_Clk, OPB_Rst) is
  begin  -- process Write_Data_DFFs
    if OPB_Rst = '1' then                 -- asynchronous reset (active high)
      zbt_wrD_2  <= ( others => '0');
      zbt_rw_2_n <= '0';
    elsif OPB_Clk'event and OPB_Clk = '1' then  -- rising clock edge
      if (zbt_ce2_I = '1') then
        zbt_wrD_2 <= OPB_DBus;
      end if;
      ZBT_IO_O   <= zbt_wrD_2;
      zbt_rw_2_n <= not(not zbt_rw_I_n and xfer_Ack_1);  -- '0' means to drive Output (Write)
                                        -- and '1' means turn-off Output (Read)
      ZBT_IO_T   <= zbt_rw_2_n;
      ZBT_IOP_T  <= zbt_rw_2_n;
    end if;
  end process Write_Data_DFFs;

  ZBT_IOP_O <= (others => '1');

  ZBT_DBus_DFF : for I in ZBT_DBus'range generate
    ZBT_Bus_FDRE : FDRE
      -- pragma translate_off
      generic map (
        INIT => '0')                    -- [bit]
      -- pragma translate_on
      port map (
        Q  => ZBT_DBus(I),              -- [out std_logic]
        C  => OPB_Clk,                      -- [in  std_logic]
        CE => xfer_Ack_3,               -- [in  std_logic]
        D  => ZBT_IO_I(I),              -- [in  std_logic]
        R  => xfer_Ack);                -- [in std_logic]
  end generate ZBT_DBus_DFF;

 -----------------------------------------------------------------------------
  -- Handling the xferAck signal
  -----------------------------------------------------------------------------
  xferAck_DFF : process (OPB_Clk, OPB_Rst) is
  begin  -- process xferAck_DFF
    if OPB_Rst = '1' then                 -- asynchronous reset (active high)
      xfer_Ack_1 <= '0';
      xfer_Ack_2 <= '0';
      xfer_Ack_3 <= '0';
      xfer_Ack_4 <= '0';
    elsif OPB_Clk'event and OPB_Clk = '1' then  -- rising clock edge
      xfer_Ack_1 <= ZBT_CS;
      xfer_Ack_2 <= xfer_Ack_1 and zbt_rw_I_n;  -- Only pass along read transfers
      xfer_Ack_3 <= xfer_Ack_2;
      xfer_Ack_4 <= xfer_Ack_3;
    end if;
  end process xferAck_DFF;

  xfer_Ack <= (xfer_Ack_1 and not zbt_rw_I_n) or  -- Write transfer
              xfer_Ack_4;                         -- Read transfer

  ZBT_xferAck <= xfer_Ack;

  
end architecture IMP;
