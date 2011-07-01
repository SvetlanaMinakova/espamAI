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
USE IEEE.numeric_std.all;

package hw_node_pack is

   type t_counter_width is array (0 to 10) of natural range 1 to 32; -- each number represents the bit-width of a counter
   type t_par_values is array (0 to 10) of integer; -- each number represents the default value of a parameter

   Function b2std(b : boolean) return std_logic;
   function int2slv(int_value : integer; size: integer) return std_logic_vector;
   function slv2int(vect : std_logic_vector; size : integer) return integer;
   Function modulo2(a:integer; b:integer) return integer;
   function max(a:integer; b:integer) return integer;
   function min(a:integer; b:integer) return integer;

end hw_node_pack;

package body hw_node_pack is

   Function b2std(b : boolean) return std_logic is
   begin
      if b then
         return '1';
      else
         return '0';
      end if;
   end b2std;

-----------------------------------------------------------------------------------

   Function int2slv(int_value : integer; size : integer) return std_logic_vector is
   variable result : std_logic_vector(size-1 downto 0);
   begin
      for i in 0 to size-1 loop
         if ((int_value/(2**i)) rem 2) = 0 then
            result(i) := '0';
         else
            result(i) := '1';
         end if;
      end loop;
      return result;
   end int2slv;

-----------------------------------------------------------------------------------

   function slv2int(vect : std_logic_vector; size : integer) return integer is

   variable result : integer range (2**size)-1 downto 0;
   begin
      result := 0;
      for i in 0 to size-1 loop

         if( vect(i) = '1' ) then
            result := result + 2**i;
         end if;

      end loop;
      return result;
   end slv2int;

----------------------------------------------------------------------------------- 

   Function modulo2(a:integer; b:integer) return integer is
   variable eval :std_logic_vector(7 downto 0);
   begin
      eval := (int2slv(a, 8) and int2slv(1, 8)) xor int2slv(b,8);
      return slv2int(eval,8);
   end modulo2;

----------------------------------------------------------------------------------- 

   function max(a:integer; b:integer) return integer is
   begin
     if (a > b) then
       return a;
     else
       return b;
     end if;
   end max;

----------------------------------------------------------------------------------- 

   function min(a:integer; b:integer) return integer is
   begin
     if (a < b) then
       return a;
     else
       return b;
     end if;
   end min;

end hw_node_pack;
