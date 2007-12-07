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
-- zbt_dpins.vhd - instantiate IO buffers with 16ms drive strength for
--                 ZBT design
--
-- (c) Alpha Data Parallel Systems Ltd. 1999-2001
--
-- Example program for ADM-XRC/ADM-XRC-P/ADM-XRCII-L/ADM-XRCII
--

library ieee;
use ieee.std_logic_1164.all;

-- synopsys translate_off
library	virtex2;
use virtex2.all;
-- synopsys translate_on

-- synopsys translate_off
--library unisim;
--use unisim.vcomponents.all;
-- synopsys translate_on

entity zbt_dpins is
    port(
	o:           in         std_logic_vector(31 downto 0);
	i:           out        std_logic_vector(31 downto 0);
        t:           in         std_logic_vector(31 downto 0);
        io:          inout      std_logic_vector(31 downto 0));
end zbt_dpins;

architecture struct of zbt_dpins is

    component IOBUF_F_16
	port(O : out  	std_logic;
	     IO: inout 	std_logic;
	     I : in 	std_logic;
	     T : in  	std_logic);
    end component;

begin

    generate_io_buffers: for j in 0 to 31 generate
        buf0: IOBUF_F_16
            port map(
                O  => i(j),
                IO => io(j),
                I  => o(j),
                T  => t(j));
    end generate;

end struct;



		

