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

library IEEE;
use IEEE.STD_LOGIC_1164.all; 
use ieee.std_logic_unsigned.all;

entity counter is
   generic(
      C_WIDTH   : natural := 10
   );
   port (
      RST       : in  std_logic;
      CLK       : in  std_logic;

      ENABLE    : in  std_logic;

      LOWER_BND : in  std_logic_vector(C_WIDTH-1 downto 0);
      UPPER_BND : in  std_logic_vector(C_WIDTH-1 downto 0);
      ITERATOR  : out std_logic_vector(C_WIDTH-1 downto 0);
      REG_CNTR  : out std_logic_vector(C_WIDTH-1 downto 0);
      DONE      : out std_logic
   );
end counter;

architecture RTL of counter is

   signal sl_register   : std_logic_vector(C_WIDTH-1 downto 0);
   signal sl_counter    : std_logic_vector(C_WIDTH-1 downto 0);
   signal sl_LOWER_BND  : std_logic_vector(C_WIDTH-1 downto 0);
   signal sl_UPPER_BND  : std_logic_vector(C_WIDTH-1 downto 0);
   signal sl_last_count : boolean;
   signal sl_done       : std_logic;

begin	 

   ITERATOR(C_WIDTH-1 downto 0) <= sl_counter;
   REG_CNTR(C_WIDTH-1 downto 0) <= sl_register;

   sl_LOWER_BND  <= LOWER_BND(C_WIDTH-1 downto 0);
   sl_UPPER_BND  <= UPPER_BND(C_WIDTH-1 downto 0);

   sl_done       <= ENABLE when sl_last_count=true else '0';
   DONE          <= sl_done;
   sl_last_count <= (sl_register = sl_UPPER_BND);

   COUNTER  : sl_counter <= sl_LOWER_BND when sl_done='1' else sl_register+1;

   REG_PRCS : process(CLK, RST)
   begin
      if( RST='1' ) then
         sl_register <= sl_LOWER_BND;
      elsif rising_edge(CLK) then
         if( ENABLE='1' ) then
            sl_register <= sl_counter;
         end if;
      end if;
   end process;

end RTL;

--==============================================================================

library IEEE;
use IEEE.STD_LOGIC_1164.all;
use hw_node_pack.all;

entity gen_counter is
   generic (
      N_CNTRS      : natural := 1;
      QUANT        : natural := 32;
      CNTR_WIDTH   : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 )
   );
   port (
      RST          : in  std_logic;
      CLK          : in  std_logic;

      ENABLE       : in  std_logic;

      LOWER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);
      UPPER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);
      ITERATORS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);
      REG_CNTRS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);
      DONE         : out std_logic
   );
end gen_counter;

architecture RTL of gen_counter is

   component counter is
      generic(
         C_WIDTH   : natural := 10
      );
      port (
         RST       : in  std_logic;
         CLK       : in  std_logic;

         ENABLE    : in  std_logic;

         LOWER_BND : in  std_logic_vector(C_WIDTH-1 downto 0);
         UPPER_BND : in  std_logic_vector(C_WIDTH-1 downto 0);
         ITERATOR  : out std_logic_vector(C_WIDTH-1 downto 0);
         REG_CNTR  : out std_logic_vector(C_WIDTH-1 downto 0);
         DONE      : out std_logic
      );
   end component;

   signal sl_done : std_logic_vector(N_CNTRS-1 downto 0);

begin

   CNTR_1 : counter -- most inner loop
   generic map ( 
      C_WIDTH   => CNTR_WIDTH(0) 
   )
   port map (
      CLK       => CLK,
      RST       => RST,

      ENABLE    => ENABLE,

      LOWER_BND => LOWER_BND_IN(CNTR_WIDTH(0)-1 downto 0),
      UPPER_BND => UPPER_BND_IN(CNTR_WIDTH(0)-1 downto 0),
      ITERATOR  => ITERATORS(CNTR_WIDTH(0)-1 downto 0),
      REG_CNTR  => REG_CNTRS(CNTR_WIDTH(0)-1 downto 0),
      DONE      => sl_done(0)
   );

   GEN_LAB_2 : if( N_CNTRS > 1 ) generate
      GEN_LAB_3 : for i in 1 to N_CNTRS-1 generate

         CNTR : counter
         generic map ( 
            C_WIDTH   => CNTR_WIDTH(i)
         )
         port map (
            CLK       => CLK,
            RST       => RST,

            ENABLE    => sl_done(i-1),

            LOWER_BND => LOWER_BND_IN(i*QUANT+CNTR_WIDTH(i)-1 downto i*QUANT),
            UPPER_BND => UPPER_BND_IN(i*QUANT+CNTR_WIDTH(i)-1 downto i*QUANT),
            ITERATOR  => ITERATORS(i*QUANT+CNTR_WIDTH(i)-1 downto i*QUANT),
            REG_CNTR  => REG_CNTRS(i*QUANT+CNTR_WIDTH(i)-1 downto i*QUANT),
            DONE      => sl_done(i)
         );

      end generate;
   end generate;

   DONE_PRCS: process(CLK, RST)
   begin
     if( RST = '1' ) then
        DONE <= '0';
     elsif rising_edge(clk) then
        if( sl_done(N_CNTRS-1)='1' and ENABLE='1') then
           DONE <= '1';
        end if;
     end if;
   end process;

end RTL;
