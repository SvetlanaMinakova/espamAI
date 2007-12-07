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
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

package free_fifo is

-- Notes about fifo_sync --------------------------------------------------------------------------------------
--
--  generic parameters:
--		- data_bits : Data bus width
--		- addr_bits : Address bus width (defines the FIFO's depth)
--		- block_type : if '2', FIFOs are implemented with BRAMs (memory blocks)
--							if '1' (or different than '2'), FIFOs are implemented using LUTs (Look-Up Tables)
--		- register_out_flag : 
--      if '0', the read data is available on the read data bus at the same clock cycle when the read strobe is generated
--	         __    __    __    __    __    __    __	   __	 __
--    clk     __/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__
--                     ______      ______      ______
--    rd_en   ________/  D0  \____/  D1  \____/  D2  \________________
--            ____________________  __________  ______________________				
--    rd_data ______________D0____><____D1____><____D2________________
--                                                    ________________
--    empty   _______________________________________/
--
-- 	 		  Or, in the case of continuous read:
--	         __    __    __    __    __    __   
--    clk     __/  \__/  \__/  \__/  \__/  \__/  \__
--                     __________________
--    rd_en   ________/                  \__________
--            _____________  ____  _________________				
--    rd_data __________D0_><_D1_><_D2______________
--                                      ____________
--    empty   _________________________/
--
----------------------------------------------------------------------------------------------------------------
-- 	 if '1', the read data is available one clock cycle after the read strobe is generated (registered output)
--	         __    __    __    __    __    __    __	   __	 __
--    clk     __/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__
--                     ______      ______      ______
--    rd_en   ________/  D0  \____/  D1  \____/  D2  \________________
--            ______________  __________  __________  ________________				
--    rd_data ________Dn____><____D0____><____D1____><____D2__________
--                                                    ________________			 You sacrify one memory location!
--    empty   _______________________________________/
--
-- 	 		  Or, in the case of continuous read:
--	         __    __    __    __    __    __   
--    clk     __/  \__/  \__/  \__/  \__/  \__/  \__
--                     __________________
--    rd_en   ________/                  \__________
--            _____________  ____  ____  ___________				
--    rd_data __________Dn_><_D0_><_D1_><_D2________
--                                       ___________
--    empty   __________________________/
--
--  =============================================================================================================
-- 	 Write operation (not affected by the generic parameters):
--	         __    __    __    __    __    __    __	   __	 __
--    clk     __/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__/  \__
--                     ______      ______      ______
--    wr_en   ________/  D0  \____/  D1  \____/  D2  \________________
--            ____      _______      ______       ____________________			
--    wr_data ____>>><<<__D0___>>><<<__D1__>>><<<<____D2______________
--                                                    ________________			 
--    full    _______________________________________/
--
----------------------------------------------------------------------------------------------------------------
-- 	* For Microblaze FSL bus, 'register_out_flag' must be '0'
--	* When 'register_out_flag'='1', one memory location is sacrified (They say : "For better performance")
--	* When 'register_out_flag'='0', all memory locations should be available
----------------------------------------------------------------------------------------------------------------

	component fifo_sync
		generic (
			data_bits  :integer;
			addr_bits  :integer;
			register_out_flag : integer := 1;
			block_type :integer := 2);
		port (
			reset		:in std_logic;
			clk		:in std_logic;
			wr_en		:in std_logic;
			wr_data	:in std_logic_vector(data_bits-1 downto 0);
			rd_en		:in std_logic;
			rd_data	:out std_logic_vector(data_bits-1 downto 0);
			count		:out std_logic_vector(addr_bits-1 downto 0);
			full		:out std_logic;
			empty		:out std_logic
			);
	end component;
	
	
	component fifo_async
		generic (
			data_bits  :integer;
			addr_bits  :integer;
			register_out_flag : integer := 1;
			block_type :integer := 2);
		port (
			reset		:in  std_logic;
			wr_clk	:in  std_logic;
			wr_en		:in  std_logic;
			wr_data	:in  std_logic_vector (data_bits-1 downto 0);
			rd_clk	:in  std_logic;
			rd_en		:in  std_logic;
			rd_data	:out std_logic_vector (data_bits-1 downto 0);
			full		:out std_logic;
			empty		:out std_logic
			);
	end component;
	
		
end package;

----------------------------------------------------------------------------
----------------------------------------------------------------------------
	library ieee;
	use ieee.std_logic_1164.all;
	use ieee.std_logic_arith.all;
	use ieee.std_logic_unsigned.all;
	library work;
	use work.free_fifo.all;
	use work.ram_lib.all;

	entity fifo_sync is
		generic (
			data_bits  :integer;
			addr_bits  :integer;
			register_out_flag : integer := 1;
			block_type :integer := 2);
		port (
			reset		:in std_logic;
			clk		:in std_logic;
			wr_en		:in std_logic;
			wr_data	:in std_logic_vector (data_bits-1 downto 0);
			rd_en		:in std_logic;
			rd_data	:out std_logic_vector (data_bits-1 downto 0);
			count		:out std_logic_vector (addr_bits-1 downto 0);
			full		:out std_logic;
			empty		:out std_logic
			);
	end fifo_sync;


	architecture arch_fifo_sync of fifo_sync is

		signal wr_addr				: std_logic_vector (addr_bits-1 downto 0);
		signal rd_addr				: std_logic_vector (addr_bits-1 downto 0);
		signal rd_allow			: std_logic;
		signal wr_allow			: std_logic;
		signal count_int			: std_logic_vector (addr_bits-1 downto 0);
		signal empty_int			: std_logic;
		signal full_int			: std_logic;
		signal empty_count		: std_logic_vector (addr_bits-1 downto 0);
		signal aempty_count		: std_logic_vector (addr_bits-1 downto 0);
		signal full_count			: std_logic_vector (addr_bits-1 downto 0);
		signal afull_count		: std_logic_vector (addr_bits-1 downto 0);
		signal sl_not_reg_out	: std_logic; 
		signal sl_read 			: std_logic;
		signal sl_rd_data       : std_logic_vector (data_bits-1 downto 0); 

	begin

		count   <= count_int;
		full    <= full_int;
		empty   <= empty_int;
		----------------------------------------------
		-- Some constants
		----------------------------------------------
		empty_count <= (others=>'0');
		aempty_count <= empty_count + 1;  
		full_count <= (others=>'1');
		afull_count <= full_count - 1; 		
		----------------------------------------------
		-- Emulation of the timing if there is or NOT register at FIFO's output (for BRAM implementation)
		----------------------------------------------
		REG_OUT_BRAM : if register_out_flag = 1 generate
			begin
				  sl_not_reg_out <= '0';
         end generate REG_OUT_BRAM;

		NOT_REG_OUT : if register_out_flag = 0 generate
			begin
				  sl_not_reg_out <= '1';
         end generate NOT_REG_OUT;
		----------------------------------------------
		-- Some internal signals
		----------------------------------------------
		rd_allow <= '1' when rd_en='1' and empty_int='0' else '0';
		wr_allow <= '1' when wr_en='1' and full_int='0' else '0';
		sl_read <= sl_not_reg_out or rd_en;	
		----------------------------------------------
		-- FIFO with BRAMs
		---------------------------------------------- 		
		DP_RAMB : if block_type = 2 generate
			begin

				RAM_BLOCK0_V2: ram_dp_block_v2
				generic map (
					addr_bits, 
					data_bits 
					)
				port map (
					reset   => reset, 
					wr_clk  => clk, 
					wr_en   => wr_allow, 
					wr_addr => wr_addr, 
					wr_data => wr_data, 
				
					rd_clk  => clk, 
					rd_en   => sl_read, 
					rd_addr => rd_addr, 
					rd_data => rd_data
					);

		end generate DP_RAMB;
		----------------------------------------------
		-- FIFO with LUTs
		----------------------------------------------
		DP_LUT : if block_type /= 2 generate -- LUT if( block_type = 1 )
			begin

				RAM_DP:  for i in data_bits-1 downto 0 generate
					begin

						RAMX1: ram_x1_dp_lut
						generic map ( addr_bits )
						port map (
								clk		  => clk, 
								port1_wr	  => wr_allow, 
								port1_addr => wr_addr, 
								port1_din  => wr_data(i), 
								port1_dout => open,

								port2_addr => rd_addr, 
								port2_dout => sl_rd_data(i)
							);
				end generate RAM_DP;
	
				proc0: process (clk, reset)
				begin
					if( reset ='1') then
						rd_data <= (others =>'0');
					elsif( clk'event and clk = '1') then 
						if sl_read = '1' and empty_int = '0' then
							rd_data <= sl_rd_data;
						end if;
					end if;
				end process proc0;    
						
		end generate DP_LUT;
		----------------------------------------------
		-- Read address counter
		----------------------------------------------
		process (clk, reset)
		begin
			if reset='1' then
				rd_addr<= (others =>'0');
			elsif clk'event and clk='1' then
				if rd_allow='1' then
					rd_addr <= rd_addr + 1;
				end if;
			end if;
		end process;
		----------------------------------------------
		-- Write address counter
		----------------------------------------------
		process (clk, reset)
		begin
			if reset='1' then
				wr_addr <= (others=>'0');
			elsif clk'event and clk='1' then
				if wr_allow='1' then
					wr_addr <= wr_addr + 1;
				end if;
			end if;
		end process;
		----------------------------------------------
		-- Counter of the words residing in the FIFO
		----------------------------------------------
		process (clk, reset)
		begin
			if reset='1' then
				count_int <= (others=>'0');
			elsif clk'event and clk='1' then
				if wr_allow='0' and rd_allow='1' then
					count_int <= count_int-1;
				elsif wr_allow='1' and rd_allow='0' then
					count_int <= count_int+1;
				end if; -- else count<=count        
			end if;
		end process;
		----------------------------------------------
		-- 'Empty' signal generation
		----------------------------------------------
		process (clk, reset)
		begin
			if reset='1' then
				empty_int <= '1';
			elsif clk'event and clk='1' then
				if empty_int='1' and wr_allow='1' then
					empty_int <= '0';
				elsif count_int=aempty_count and rd_allow='1' and wr_allow='0' then
					empty_int <= '1';
				elsif count_int=empty_count then
					empty_int <= '1';
				else
					empty_int <= '0';
				end if;
			end if;
		end process;
		----------------------------------------------
		-- 'Full' signal generation
		----------------------------------------------
		process (clk, reset)
		begin
			if reset='1' then
				full_int <= '0';
			elsif clk'event and clk='1' then
				if full_int='1' and rd_allow='1' then
					full_int <= '0';
				elsif count_int=afull_count and rd_allow='0' and wr_allow='1' then
					full_int <= '1';
				elsif count_int=full_count then
					full_int <= '1';
				else
					full_int <= '0';
				end if;
			end if;
		end process;	
		
	end arch_fifo_sync;

