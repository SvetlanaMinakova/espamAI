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

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.std_logic_arith.all;
USE ieee.std_logic_unsigned.all;

LIBRARY proc_common_v3_00_a;
USE proc_common_v3_00_a.pselect;

ENTITY lmb_host_ctrl IS
  GENERIC (
    -- LMB interface
    C_HIGHADDR     : STD_LOGIC_VECTOR(0 to 31) := X"0a00000F";
    C_BASEADDR     : STD_LOGIC_VECTOR(0 to 31) := X"0a000000";
    C_AB           : INTEGER                   := 8;
    C_LMB_AWIDTH   : INTEGER                   := 32;
    C_LMB_DWIDTH   : INTEGER                   := 32;

    PAR_WIDTH  : natural := 16;

    -- number of 'finished' signals collected from processors/HW_IPs
    N_FIN          : INTEGER                   := 1
  );
  PORT (

    LMB_Clk : IN STD_LOGIC := '0';
    LMB_Rst : IN STD_LOGIC := '0';

    -- LMB Bus
    LMB_ABus        : IN  STD_LOGIC_VECTOR(0 TO C_LMB_AWIDTH-1);
    LMB_WriteDBus   : IN  STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    LMB_AddrStrobe  : IN  STD_LOGIC;
    LMB_ReadStrobe  : IN  STD_LOGIC;
    LMB_WriteStrobe : IN  STD_LOGIC;
    LMB_BE          : IN  STD_LOGIC_VECTOR(0 TO (C_LMB_DWIDTH/8 - 1));
    Sl_DBus         : OUT STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
    Sl_Ready        : OUT STD_LOGIC;

    PARAM_DT : OUT  std_logic_vector(0 to PAR_WIDTH-1);
    PARAM_LD : OUT  std_logic;

    RST_OUT  : OUT std_logic; -- reset to the hw ip cores, active '0'

    ENABLE_MB : OUT STD_LOGIC;

    FIN_0  : IN STD_LOGIC;
    FIN_1  : IN STD_LOGIC;
    FIN_2  : IN STD_LOGIC;
    FIN_3  : IN STD_LOGIC;
    FIN_4  : IN STD_LOGIC;
    FIN_5  : IN STD_LOGIC;
    FIN_6  : IN STD_LOGIC;
    FIN_7  : IN STD_LOGIC;
    FIN_8  : IN STD_LOGIC;
    FIN_9  : IN STD_LOGIC;
    FIN_10 : IN STD_LOGIC;
    FIN_11 : IN STD_LOGIC;
    FIN_12 : IN STD_LOGIC;
    FIN_13 : IN STD_LOGIC;
    FIN_14 : IN STD_LOGIC;
    FIN_15 : IN STD_LOGIC;
    FIN_16 : IN STD_LOGIC;
    FIN_17 : IN STD_LOGIC;
    FIN_18 : IN STD_LOGIC;
    FIN_19 : IN STD_LOGIC;
    FIN_20 : IN STD_LOGIC;
    FIN_21 : IN STD_LOGIC;
    FIN_22 : IN STD_LOGIC;
    FIN_23 : IN STD_LOGIC;
    FIN_24 : IN STD_LOGIC;
    FIN_25 : IN STD_LOGIC;
    FIN_26 : IN STD_LOGIC;
    FIN_27 : IN STD_LOGIC;
    FIN_28 : IN STD_LOGIC;
    FIN_29 : IN STD_LOGIC;
    FIN_30 : IN STD_LOGIC;
    FIN_31 : IN STD_LOGIC
  );
	
END lmb_host_ctrl;


ARCHITECTURE imp OF lmb_host_ctrl IS

-- component declarations
COMPONENT pselect IS
GENERIC (
      C_AW   : INTEGER                   := 32;
      C_BAR  : STD_LOGIC_VECTOR(0 TO 31);
      C_AB   : INTEGER                   := 8);
PORT (
      A     : IN  STD_LOGIC_VECTOR(0 TO 31);
      CS    : OUT STD_LOGIC;
      AValid : IN  STD_LOGIC);
END COMPONENT;

-- internal signals
signal lmb_select   : STD_LOGIC;
signal sl_rdy_rd    : STD_LOGIC;
signal sl_status    : STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
signal sl_control   : STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
signal sl_parameter : STD_LOGIC_VECTOR(0 TO C_LMB_DWIDTH-1);
signal sl_counter   : STD_LOGIC_VECTOR(0 TO 31);
signal sl_address   : STD_LOGIC_VECTOR(0 TO 3);
signal sl_param_ld  : STD_LOGIC;

signal sl_fin       : STD_LOGIC_VECTOR(31 DOWNTO 0);
constant c_mask     : STD_LOGIC_VECTOR(N_FIN-1 DOWNTO 0) := (others => '1');


BEGIN  -- architecture imp

    ---------------------------
    -- Read the registers
    ---------------------------
    Sl_DBus  <= sl_status    when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 0) else
                sl_counter   when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 1) else
                sl_parameter when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 2) else -- just for debug (to be removed)
                sl_control   when sl_rdy_rd = '1' and (CONV_INTEGER(sl_address) = 3) else -- just for debug (to be removed)
                X"a5a5a5a5"; -- others

    -------------------------------------
    -- Parameter data bus (to the HW IPs)
    -------------------------------------
    PARAM_DT <= sl_parameter((C_LMB_DWIDTH-PAR_WIDTH) to C_LMB_DWIDTH-1);
    PARAM_LD <= sl_param_ld;

    RST_OUT  <= sl_control(C_LMB_DWIDTH-1); -- LSBit
    ENABLE_MB <= sl_control(C_LMB_DWIDTH-1);

    ------------------------------------------------
    -- Write to the controll and parameter registers
    ------------------------------------------------
    Control_Register : PROCESS (LMB_Clk, LMB_Rst) IS
    BEGIN  
        IF (LMB_Rst = '1') THEN

           sl_control <= (others=>'0');
           sl_counter <= (others=>'0');
           sl_parameter <= (others=>'1');
           sl_param_ld <= '0';

        ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
           IF LMB_WriteStrobe = '1' and lmb_select = '1' THEN

              IF CONV_INTEGER(LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3)) = 0 THEN
                 sl_control <= LMB_WriteDBus;
              ELSIF CONV_INTEGER(LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3)) = 1 THEN
                 sl_counter <= LMB_WriteDBus;
              ELSIF CONV_INTEGER(LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3)) = 2 THEN
                 sl_parameter <= LMB_WriteDBus;
                 sl_param_ld <= '1';  -- Parameter write strobe to the HW IP cores
              END IF;

           ELSE
              sl_param_ld <= '0';
              IF CONV_INTEGER(sl_status(C_LMB_DWIDTH-1)) = 0 THEN
                 sl_counter  <= sl_counter + 1;
              END IF;
           END IF;
        END IF;
    END PROCESS Control_Register;

    ---------------------------------------------------
    -- Handling the LMB bus interface
    ---------------------------------------------------
    Ready_Handling : PROCESS (LMB_Clk, LMB_Rst) IS
    BEGIN  
        IF (LMB_Rst = '1') THEN
            sl_rdy_rd <= '0';
            Sl_Ready <= '0';
            sl_address <= (others=>'0');
        ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
            sl_rdy_rd  <= LMB_ReadStrobe AND lmb_select;
            Sl_Ready  <= LMB_AddrStrobe AND lmb_select;
            IF( LMB_AddrStrobe = '1' ) THEN -- delay the address (needed for reading)
            -- 2-bit address, the 2 LSBits are 'byte select'
--                sl_address <= LMB_ABus(C_LMB_AWIDTH-4 to C_LMB_AWIDTH-3);
                sl_address <= LMB_ABus(C_LMB_AWIDTH-6 to C_LMB_AWIDTH-3);
            END IF;
        END IF;
    END PROCESS Ready_Handling;

    -- Do the LMB address decoding
    pselect_lmb : pselect
    generic map (
        C_AW   => LMB_ABus'length,
        C_BAR  => C_BASEADDR,
        C_AB   => 8)
    port map (
        A      => LMB_ABus,
        CS     => lmb_select,
        AValid => LMB_AddrStrobe);

    -----------------------------------------------
    -- Set the status register (execution finished)
    -----------------------------------------------
    Status_register : PROCESS (LMB_Clk, LMB_Rst) IS
    BEGIN  
        IF (LMB_Rst = '1') THEN
            sl_status <= (others => '0');
        ELSIF (LMB_Clk'EVENT AND LMB_Clk = '1') THEN
            IF( sl_fin(N_FIN-1 downto 0) = c_mask ) THEN
               sl_status(C_LMB_DWIDTH-1) <= '1';
            ELSE
               sl_status(C_LMB_DWIDTH-1) <= '0';
            END IF;
        END IF;
    END PROCESS;

    sl_fin(0)  <= FIN_0;
    sl_fin(1)  <= FIN_1;
    sl_fin(2)  <= FIN_2;
    sl_fin(3)  <= FIN_3;
    sl_fin(4)  <= FIN_4;
    sl_fin(5)  <= FIN_5;
    sl_fin(6)  <= FIN_6;
    sl_fin(7)  <= FIN_7;
    sl_fin(8)  <= FIN_8;
    sl_fin(9)  <= FIN_9;
    sl_fin(10) <= FIN_10;
    sl_fin(11) <= FIN_11;
    sl_fin(12) <= FIN_12;
    sl_fin(13) <= FIN_13;
    sl_fin(14) <= FIN_14;
    sl_fin(15) <= FIN_15;
    sl_fin(16) <= FIN_16;
    sl_fin(17) <= FIN_17;
    sl_fin(18) <= FIN_18;
    sl_fin(19) <= FIN_19;
    sl_fin(20) <= FIN_20;
    sl_fin(21) <= FIN_21;
    sl_fin(22) <= FIN_22;
    sl_fin(23) <= FIN_23;
    sl_fin(24) <= FIN_24;
    sl_fin(25) <= FIN_25;
    sl_fin(26) <= FIN_26;
    sl_fin(27) <= FIN_27;
    sl_fin(28) <= FIN_28;
    sl_fin(29) <= FIN_29;
    sl_fin(30) <= FIN_30;
    sl_fin(31) <= FIN_31;

END ARCHITECTURE imp;
