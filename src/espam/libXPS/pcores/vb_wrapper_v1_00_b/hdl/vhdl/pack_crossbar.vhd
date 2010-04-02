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
use IEEE.std_logic_1164.all;

package typedef is						 		   
   constant ports_num : integer := 5;  -- ports_num should not be a power of 2!!!
   constant ports_num_log2 : integer := 3;
   constant data_bits     : integer := 32;
   type t_data is array (0 to ports_num-1) of std_logic_vector (data_bits-1 downto 0);
   type t_ctrl is array (0 to ports_num-1) of std_logic_vector (ports_num_log2-1 downto 0);
   type t_fifo_sel is array (0 to ports_num-1) of std_logic_vector (7 downto 0);   
   --type t_ch_size is array (0 to 255) of natural range 1 to 32; -- each number represents the address width of a channel   
   type t_ch_size is array (0 to 255) of natural; -- each number represents the channel depth in words (memory locations)   
end typedef;
