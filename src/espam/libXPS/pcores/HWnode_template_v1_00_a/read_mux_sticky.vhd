-- ***********************************************************************
--
-- The ESPAM Software Tool 
-- Copyright (c) 2004-2011 Leiden University (LERC group at LIACS).
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

-- $Id: read_mux_sticky.vhd,v 1.1 2011/07/01 12:04:57 svhaastr Exp $
-- Changelog:
-- 2011-07-01  Sven van Haastregt
--   * Introduced read_mux_sticky for arguments connected to one or more sticky FIFOs.

library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity read_mux_sticky is
   generic(
      N_PORTS    : natural := 1;
      PORT_WIDTH : natural := 32
   );
   port(
      RST        : in  std_logic;
      CLK        : in  std_logic;
      IN_PORTS   : in  std_logic_vector(N_PORTS*PORT_WIDTH-1 downto 0);
      EXISTS     : in  std_logic_vector(N_PORTS-1 downto 0);
      READS      : out std_logic_vector(N_PORTS-1 downto 0);

      OUT_PORT   : out std_logic_vector(PORT_WIDTH-1 downto 0);
      EXIST      : out std_logic;
      READ       : in  std_logic;

      CONTROL    : in  std_logic_vector(N_PORTS-1 downto 0)
   );
end read_mux_sticky;

architecture RTL of read_mux_sticky is

  signal outval   : std_logic_vector(PORT_WIDTH-1 downto 0);
  signal previous : std_logic_vector(PORT_WIDTH-1 downto 0);

begin

   DEMUX_GEN : for i in 0 to N_PORTS-1 generate
      READS(i) <= CONTROL(i) and READ;
   end generate;

   MUX_PRCSS :  process(EXISTS, CONTROL, IN_PORTS)
   begin

      -- Take previous output value as default
      outval <= previous;

      -- The default value needs to be '1'. See the node's top-level: sl_exist <= sl_exist_1 and sl_exist_0;
      EXIST <= '1';
      
      for i in 0 to N_PORTS-1 loop
         if( CONTROL(i) = '1'  ) then
            outval <= IN_PORTS((i+1)*PORT_WIDTH-1 downto (i)*PORT_WIDTH);
            EXIST <= EXISTS(i);
         end if;
      end loop;
   
   end process;

   process (RST,CLK) begin
     if (RST='1') then
       previous <= (others => '0');
     elsif (rising_edge(CLK)) then
       previous <= outval;
     end if;
   end process;

   OUT_PORT <= outval;

end RTL;