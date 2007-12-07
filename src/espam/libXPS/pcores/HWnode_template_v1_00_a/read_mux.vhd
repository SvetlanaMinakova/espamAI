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

entity read_mux is
   generic(
      N_PORTS    : natural := 1;
      PORT_WIDTH : natural := 32
   );
   port(
      IN_PORTS   : in  std_logic_vector(N_PORTS*PORT_WIDTH-1 downto 0);
      EXISTS     : in  std_logic_vector(N_PORTS-1 downto 0);
      READS      : out std_logic_vector(N_PORTS-1 downto 0);

      OUT_PORT   : out std_logic_vector(PORT_WIDTH-1 downto 0);
      EXIST      : out std_logic;
      READ       : in  std_logic;

      CONTROL    : in  std_logic_vector(N_PORTS-1 downto 0)
   );
end read_mux;

architecture RTL of read_mux is
begin

   DEMUX_GEN : for i in 0 to N_PORTS-1 generate
      READS(i) <= CONTROL(i) and READ;
   end generate;

   MUX_PRCSS :  process(EXISTS, CONTROL, IN_PORTS)
   begin

      OUT_PORT <= (others=>'0');
      -- The default value needs to be '1'. See the node's top-level: sl_exist <= sl_exist_1 and sl_exist_0;
      EXIST <= '1';
      
      for i in 0 to N_PORTS-1 loop
         if( CONTROL(i) = '1'  ) then
            OUT_PORT <= IN_PORTS((i+1)*PORT_WIDTH-1 downto (i)*PORT_WIDTH);
            EXIST <= EXISTS(i);
         end if;
      end loop;
   
   end process;

end RTL;
