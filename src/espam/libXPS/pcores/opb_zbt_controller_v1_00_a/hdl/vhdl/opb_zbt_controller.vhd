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

-- $Id: opb_zbt_controller.vhd,v 1.1 2007/12/07 22:08:52 stefanov Exp $

-- opb_zbt_controller.vhd
--   Generated by wzhong

library ieee;

use ieee.std_logic_1164.all;

-------------------------------------------------------------------------------
-- entity
-------------------------------------------------------------------------------

entity opb_zbt_controller is
  generic
  (
    C_BASEADDR      : std_logic_vector(0 to 31) := X"FA000000";
    C_HIGHADDR      : std_logic_vector(0 to 31) := X"FA3FFFFF";
    C_ZBT_ADDR_SIZE : integer                   := 20;
    -- If C_EXTERNAL_DLL is true (1) then
    -- ZBT_Clk is driven outside this module
    -- and ZBT_Clk_FBOut is also driven outside this module
    C_EXTERNAL_DLL  : integer                   := 0
  );
  port
  (
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
	
	-- ZBT control signal---------------------------------------------
	-- RC_O(0)      ------ ZBT_CKE_N
	-- RC_O(1)		------ ZBT_OE_N
	-- RC_O(2)		------ ZBT_ADV_LD_N
	-- RC_O(3)		------ ZBT_CE1_N
	-- RC_O(4)		------ ZBT_RW_N
	-- RC_O(5 to 8) ------ ZBT_BW_N(1 to 4)
	-- RC_O(9)		------ ZBT_CE2
	-- RC_O(10)		------ ZBT_CE2_N
	-- RC_O(11)		------ ZBT_LBO_N 
	------------------------------------------------------------------
	RC_O          : out std_logic_vector (0 to 8);

    RA_O          : out std_logic_vector(0 to C_ZBT_ADDR_SIZE-1);
    RD_I          : in  std_logic_vector(0 to 31);
    RD_O          : out std_logic_vector(0 to 31);
    T_RD          : out std_logic_vector(0 to 31)
  );
end entity opb_zbt_controller;

-------------------------------------------------------------------------------
-- architecture
-------------------------------------------------------------------------------

architecture imp of opb_zbt_controller is

  component Opb_zbt_controller_core is
    generic
    (
      C_BASEADDR      : std_logic_vector(0 to 31) := X"FA000000";
      C_HIGHADDR      : std_logic_vector(0 to 31) := X"FA3FFFFF";
      C_ZBT_ADDR_SIZE : integer                   := 20;
      -- If C_EXTERNAL_DLL is true (1) then
      -- ZBT_Clk is driven outside this module
      -- and ZBT_Clk_FBOut is also driven outside this module
      C_EXTERNAL_DLL  : integer                   := 0
    );
    port
    (
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
  end component Opb_zbt_controller_core;
  
  signal sl_T_RD        :  std_logic;
  signal sl_ZBT_IOP_I   :  std_logic_vector(1 to 4);
  signal sl_ZBT_IOP_O   :  std_logic_vector(1 to 4);
  signal sl_ZBT_IOP_T   :  std_logic;
  signal sl_ZBT_LBO_N	:  std_logic;
  signal sl_ZBT_CE2_N	:  std_logic;
  signal sl_ZBT_CE2  	:  std_logic;

begin  ------------------------------------------------------------------------

  OPB_ZBT_CONTROLLER_CORE_I : Opb_zbt_controller_core
    generic map
    (
      C_BASEADDR      =>  C_BASEADDR,
      C_HIGHADDR      =>  C_HIGHADDR,
      C_ZBT_ADDR_SIZE =>  C_ZBT_ADDR_SIZE,
      C_EXTERNAL_DLL  =>  C_EXTERNAL_DLL
    )
    port map
    (
      OPB_Clk       =>  OPB_Clk,
      OPB_Rst       =>  OPB_Rst,

      -- OPB signals
      OPB_ABus      =>  OPB_ABus,
      OPB_BE        =>  OPB_BE,
      OPB_RNW       =>  OPB_RNW,
      OPB_select    =>  OPB_select,
      OPB_seqAddr   =>  OPB_seqAddr,
      OPB_DBus      =>  OPB_DBus,
	  
      ZBT_DBus      =>  ZBT_DBus,
      ZBT_errAck    =>  ZBT_errAck,
      ZBT_retry     =>  ZBT_retry,
      ZBT_toutSup   =>  ZBT_toutSup,
      ZBT_xferAck   =>  ZBT_xferAck,

      -- ZBT Memory signals
      ZBT_Clk_FB    =>  ZBT_Clk_FB,
      ZBT_Clk_FBOut =>  ZBT_Clk_FBOut,
      ZBT_Clk       =>  ZBT_Clk,
	  
      ZBT_OE_N      =>  RC_O(7),
      ZBT_ADV_LD_N  =>  RC_O(6),
      ZBT_LBO_N     =>  sl_ZBT_LBO_N,
      ZBT_CE1_N     =>  RC_O(5),
      ZBT_CE2_N     =>  sl_ZBT_CE2_N,
      ZBT_CE2       =>  sl_ZBT_CE2,
      ZBT_RW_N      =>  RC_O(4),
      ZBT_CKE_N     =>  RC_O(8),
      ZBT_A         =>  RA_O,
      ZBT_BW_N      =>  RC_O(0 to 3),
      ZBT_IO_I      =>  RD_I,
      ZBT_IO_O      =>  RD_O,
      ZBT_IO_T      =>  sl_T_RD,
      ZBT_IOP_I     =>  sl_ZBT_IOP_I,
      ZBT_IOP_O     =>  sl_ZBT_IOP_O,
      ZBT_IOP_T     =>  sl_ZBT_IOP_T
    );

 T_RD <= (others => sl_T_RD); 

end architecture imp;

