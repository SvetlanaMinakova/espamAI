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
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;	
use hw_node_pack.all;

entity PARAMETERS is
   generic (
      PAR_WIDTH  : natural;
      N_PAR      : natural;
      PAR_VALUES : t_par_values
   );
   port (
      RST        : in  std_logic;
      CLK        : in  std_logic;

      PARAM_DT   : in  std_logic_vector(PAR_WIDTH-1 downto 0);
      PARAM_LD   : in  std_logic;

      PARAMETERS : out std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0)
   );
end PARAMETERS;

architecture RTL of PARAMETERS is 

   signal sl_tmp_parameters : std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0);
   signal sl_load   : std_logic;
   signal sl_update : std_logic;
   signal sl_det_0, sl_det_1, sl_PARAM_LD : std_logic;

begin

   GenLabel1 : if N_PAR > 0 generate
      -- Rising_edge detection of PARAM_LD signal --------------------
      Edge_det_prcss : process(CLK)
      begin
         if falling_edge( CLK ) then
            sl_det_0 <= PARAM_LD;
            sl_det_1 <= sl_det_0;
         end if;
      end process;

      sl_PARAM_LD <= sl_det_0 and not(sl_det_1);

      -- Load the parameters into a temp buffer ----------------------
      GenLabel2 : if( N_PAR > 1) generate
         shift_reg: process (CLK, RST)
         begin
            if rising_edge(CLK) then  -- shift LEFT register
               if( sl_PARAM_LD='1' ) then

                  sl_tmp_parameters(PAR_WIDTH-1 downto 0) <= PARAM_DT;
                  sl_tmp_parameters(N_PAR*PAR_WIDTH-1 downto PAR_WIDTH) <= sl_tmp_parameters((N_PAR-1)*PAR_WIDTH-1 downto 0);

               end if;
            end if;
         end process;
      end generate;

      GenLable3 : if( N_PAR = 1) generate
         shift_reg: process (CLK, RST)
         begin
            if rising_edge(CLK) then  
               if( sl_PARAM_LD='1' ) then
                  sl_tmp_parameters(PAR_WIDTH-1 downto 0) <= PARAM_DT;
               end if;
            end if;
         end process;
      end generate;

      -- Update the parameters (from the temp buffer) ----------------
      Ld_ctrl_prcss : process(CLK, RST)
      begin
         if rising_edge(CLK) then
            if( RST='1' ) then

               sl_update <= '1';

               for i in 1 to N_PAR loop
                  PARAMETERS(i*PAR_WIDTH-1 downto (i-1)*PAR_WIDTH) <= CONV_STD_LOGIC_VECTOR(PAR_VALUES(i-1),PAR_WIDTH);
               end loop;

               if( sl_load='1' ) then
                  PARAMETERS <= sl_tmp_parameters;
               end if;

               if( sl_PARAM_LD='1' ) then
                  sl_load <= '1';
               end if;

            else

               if( sl_update='1' ) then
                  sl_load   <= '0';
                  sl_update <= '0';
               end if;

               if( sl_PARAM_LD='1' ) then
                  sl_load <= '1';
               end if;

            end if;
         end if;
      end process;

   end generate; -- GenLabel1

end RTL;
