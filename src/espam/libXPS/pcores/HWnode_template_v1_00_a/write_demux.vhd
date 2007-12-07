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
use IEEE.STD_LOGIC_UNSIGNED.all;

entity write_demux is
   generic(
      N_PORTS : natural := 1
   );
   port(
      WRITES  : out std_logic_vector(N_PORTS-1 downto 0);
      WRITE   : in  std_logic;

      FULLS   : in  std_logic_vector(N_PORTS-1 downto 0);
      FULL    : out std_logic;

      CONTROL : in  std_logic_vector(N_PORTS-1 downto 0)
   );
end write_demux;


architecture RTL of write_demux is

   signal sl_fulls : std_logic_vector(N_PORTS-1 downto 0);

begin

   GEN : for i in 0 to N_PORTS-1 generate
      WRITES(i)   <= CONTROL(i) and WRITE;
      sl_fulls(i) <= CONTROL(i) and FULLS(i);
   end generate;

   FULL <= '1' when sl_fulls /= 0 else '0';

end RTL;
