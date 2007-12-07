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

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity CONTROLLER is 
   generic(
      N_STAGES  : natural := 1; -- number of pipeline stages or delay
      BLOCKING  : natural := 0  -- '1'-block the pipeline if there is no input data
   );
   port (
      READ      : out std_logic;
      EXIST     : in std_logic;
      WRITE     : out std_logic;
      FULL      : in std_logic;

      ENABLE_EX : out std_logic;
      IP_READ   : in std_logic;
      IP_WRITE  : in std_logic;

      DONE_WR   : in std_logic;
      DONE_RD   : in std_logic;

      CLK       : in std_logic;
      RST       : in std_logic
   );
end CONTROLLER;

architecture RTL of CONTROLLER is

   -- Signal List
   signal sl_read    : std_logic;
   signal sl_execute : std_logic;
   signal sl_write   : std_logic;
   signal delay_pipe : std_logic_vector(N_STAGES downto 0);

begin

   Pipe_Fill: process( CLK, RST )
   begin
      if( RST = '1' ) then
         delay_pipe <= (others => '0');
      elsif( rising_edge(CLK) ) then

         if( sl_execute = '1' ) then
            delay_pipe(0) <= sl_read;
            delay_pipe(N_STAGES downto 1) <= delay_pipe(N_STAGES-1 downto 0);
         end if;

      end if;
   end process Pipe_Fill;

   sl_read    <= EXIST and not( FULL ) and not( DONE_RD ) and not( RST );
   sl_write   <= delay_pipe(N_STAGES-1) and sl_execute;
   sl_execute <= sl_read when (DONE_RD='0' and BLOCKING > 0) else FULL nor DONE_WR;

   WRITE      <= sl_write and IP_WRITE;
   READ       <= sl_read  and IP_READ;
   ENABLE_EX  <= sl_execute;

end RTL;
