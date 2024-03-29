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
-- pragma translate_off
library	VIRTEX2;
use VIRTEX2.all;
-- pragma translate_on

package ram_lib is
	
	component ram_x1_dp_lut
		generic (
			addr_bits	:integer);
		port (
			clk		:in  std_logic;
			port1_wr	:in  std_logic;
			port1_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			port1_din	:in  std_logic;
			port1_dout	:out std_logic;
			port2_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			port2_dout	:out std_logic
			);
	end component;
	

	component ram_dp_block_v2
		generic (
			addr_bits			:integer;
			data_bits			:integer
			);
		port (
			reset		:in  std_logic;
			wr_clk	:in  std_logic;
			wr_en	   :in  std_logic;
			wr_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			wr_data	:in  std_logic_vector(data_bits-1 downto 0);
			rd_clk	:in  std_logic;
			rd_en	   :in  std_logic;
			rd_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			rd_data	:out std_logic_vector(data_bits-1 downto 0)
			);
	end component;

	function slv_to_integer(x : std_logic_vector)
	return integer;
	function integer_to_slv(n, bits : integer)
	return std_logic_vector;
	
end ram_lib;
----------------------------------------------------------------------------
----------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
library work;
use work.ram_lib.all;

package body ram_lib is

	function slv_to_integer(x : std_logic_vector)
		return integer is
		variable n	:integer range 0 to (2**30 - 1);
	begin
		n := 0;
		for i in x'range loop
			n := n * 2;
			case x(i) is
				when '1' | 'H' => n := n + 1;
				when '0' | 'L' => null;
				when others =>	  null;
			end case;
		end loop;
		
		return n;
	end slv_to_integer;
   -------------------------------------
	-------------------------------------
	function integer_to_slv(n, bits : integer)
		return std_logic_vector is
		variable x		:std_logic_vector(bits-1 downto 0);
		variable tempn	:integer;
	begin
		x := (others => '0');
		tempn := n;
		for i in x'reverse_range loop
			if (tempn mod 2) = 1 then
				x(i) := '1';
			end if;
			tempn := tempn / 2;
		end loop;
		
		return x;
	end integer_to_slv;

end ram_lib;
----------------------------------------------------------------------------
----------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all; 

-- synopsys translate_off
library	VIRTEX2;
use VIRTEX2.all;
-- synopsys translate_on

library work;
use work.ram_lib.all;

	entity ram_x1_dp_lut is
		generic (addr_bits	:integer);
		port (
			clk		   :in std_logic;
			port1_wr	   :in std_logic;
			port1_addr  :in std_logic_vector (addr_bits-1 downto 0);
			port1_din	:in std_logic;
			port1_dout	:out std_logic;
			port2_addr	:in std_logic_vector (addr_bits-1 downto 0);
			port2_dout	:out std_logic
		);
	end ram_x1_dp_lut;

	architecture arch_ram_x1_dp_lut of ram_x1_dp_lut is

		signal d_port1_dout :std_logic_vector ((2**(addr_bits-4))-1 downto 0);
		signal d_port2_dout :std_logic_vector ((2**(addr_bits-4))-1 downto 0);
		signal write_enable :std_logic_vector ((2**(addr_bits-4))-1 downto 0);
	
		-- Xilinx Specific "components"
		component RAM16X1D
			port (D, WE, WCLK, A3, A2, A1, A0,
				DPRA3, DPRA2, DPRA1, DPRA0: in std_logic;
				SPO,DPO: out std_logic);
		end component;
	
		component RAMB4_S1_S1
			port (WEA, ENA, RSTA, CLKA	:in  std_logic;
				ADDRA			:in  std_logic_vector (11 downto 0);
				DIA			:in  std_logic_vector(0 downto 0);
				DOA			:out std_logic_vector(0 downto 0);
				WEB, ENB, RSTB, CLKB	:in  std_logic;
				ADDRB			:in  std_logic_vector (11 downto 0);
				DIB			:in  std_logic_vector(0 downto 0);
				DOB			:out std_logic_vector(0 downto 0)
				);
		end component;
	
		component RAMB4_S2_S2
			port (WEA, ENA, RSTA, CLKA	:in  std_logic;
				ADDRA			:in  std_logic_vector (10 downto 0);
				DIA			:in  std_logic_vector (1 downto 0);
				DOA			:out std_logic_vector (1 downto 0);
				WEB, ENB, RSTB, CLKB	:in  std_logic;
				ADDRB			:in  std_logic_vector (10 downto 0);
				DIB			:in  std_logic_vector (1 downto 0);
				DOB			:out std_logic_vector (1 downto 0)
				);
		end component;
	
		component RAMB4_S4_S4
			port (WEA, ENA, RSTA, CLKA	:in  std_logic;
				ADDRA			:in  std_logic_vector (9 downto 0);
				DIA			:in  std_logic_vector (3 downto 0);
				DOA			:out std_logic_vector (3 downto 0);
				WEB, ENB, RSTB, CLKB	:in  std_logic;
				ADDRB			:in  std_logic_vector (9 downto 0);
				DIB			:in  std_logic_vector (3 downto 0);
				DOB			:out std_logic_vector (3 downto 0)
				);
		end component;
	
		component RAMB4_S8_S8
			port (WEA, ENA, RSTA, CLKA	:in  std_logic;
				ADDRA			:in  std_logic_vector (8 downto 0);
				DIA			:in  std_logic_vector (7 downto 0);
				DOA			:out std_logic_vector (7 downto 0);
				WEB, ENB, RSTB, CLKB	:in  std_logic;
				ADDRB			:in  std_logic_vector (8 downto 0);
				DIB			:in  std_logic_vector (7 downto 0);
				DOB			:out std_logic_vector (7 downto 0)
				);
		end component;
	
		component RAMB4_S16_S16
			port (WEA, ENA, RSTA, CLKA	:in  std_logic;
				ADDRA			:in  std_logic_vector (7 downto 0);
				DIA			:in  std_logic_vector (15 downto 0);
				DOA			:out std_logic_vector (15 downto 0);
				WEB, ENB, RSTB, CLKB	:in  std_logic;
				ADDRB			:in  std_logic_vector (7 downto 0);
				DIB			:in  std_logic_vector (15 downto 0);
				DOB			:out std_logic_vector (15 downto 0)
				);
		end component;
	
	begin
		------------------------------------
		-- Array of RAM Blocks
		------------------------------------
		RAMX1_DP: for i in (2**(addr_bits-4))-1 downto 0 generate
			begin
			RAM_LUT: RAM16X1D 
				port map (
					D=>port1_din,
					WE=>write_enable(i),
					WCLK=>clk,
					A3=>port1_addr(3),
					A2=>port1_addr(2),
					A1=>port1_addr(1),
					A0=>port1_addr(0),
					DPRA3=>port2_addr(3),
					DPRA2=>port2_addr(2),
					DPRA1=>port2_addr(1),
					DPRA0=>port2_addr(0),
					SPO=>d_port1_dout(i),
					DPO=>d_port2_dout(i)
				);
		end generate RAMX1_DP;

		------------------------------------
		-- Generate the write enables
		------------------------------------
		WE_GEN_SMALL: if addr_bits<=4 generate
			begin
			write_enable(0) <= port1_wr;
		end generate WE_GEN_SMALL;
	
		WE_GEN_LARGE: if addr_bits>4 generate
			begin
			WE_GEN:  for i in (2**(addr_bits-4))-1 downto 0 generate
				begin
				process (port1_wr, port1_addr)
				begin
					if integer_to_slv(i, addr_bits-4) = port1_addr(addr_bits-1 downto 4) and port1_wr='1' then
						write_enable(i) <= '1';
					else
						write_enable(i) <= '0';
					end if;
				end process;
			end generate WE_GEN;
		end generate WE_GEN_LARGE;
	
		------------------------------------
		-- Mux the data outputs
		------------------------------------
		MUX_SMALL: if addr_bits<=4 generate
			begin
			port1_dout <= d_port1_dout(0);
			port2_dout <= d_port2_dout(0);
		end generate MUX_SMALL;
	
		MUX_LARGE: if addr_bits>4 generate
			begin
			port1_dout <= d_port1_dout(slv_to_integer(port1_addr(addr_bits-1 downto 4)));
			port2_dout <= d_port2_dout(slv_to_integer(port2_addr(addr_bits-1 downto 4)));
		end generate MUX_LARGE;
	
	end arch_ram_x1_dp_lut;
----------------------------------------------------------------------------
----------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
-- pragma translate_off
library	VIRTEX2;
use VIRTEX2.all;
-- pragma translate_on

library work;
use work.ram_lib.all;

	entity ram_dp_block_v2 is
		generic (
			addr_bits			:integer;
			data_bits			:integer
			);
		port (
			reset		:in  std_logic;
			wr_clk	:in  std_logic;
			wr_en		:in  std_logic;
			wr_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			wr_data	:in  std_logic_vector (data_bits-1 downto 0);
			rd_clk	:in  std_logic;
			rd_en		:in  std_logic;
			rd_addr	:in  std_logic_vector (addr_bits-1 downto 0);
			rd_data	:out std_logic_vector (data_bits-1 downto 0)
			); 
	end ram_dp_block_v2;


	architecture arch_ram_dp_block_v2 of ram_dp_block_v2 is
	
		-- Xilinx Specific "components"
		component RAM16X1D
			port (D, WE, WCLK, A3, A2, A1, A0,
				DPRA3, DPRA2, DPRA1, DPRA0: in std_logic;
				SPO,DPO: out std_logic);
		end component;
	
		component RAMB16_S1_S1 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (0 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (0 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (13 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (13 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (0 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (0 downto 0));   
		end component;                                       
	
		component RAMB16_S2_S2 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (1 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (1 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (12 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (12 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (1 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (1 downto 0));   
		end component;                                       
	
		component RAMB16_S4_S4 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (3 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (3 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (11 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (11 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (3 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (3 downto 0));   
		end component;                                       
		
		component RAMB16_S9_S9 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (7 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (7 downto 0);    
				DIPA    : in  STD_LOGIC_VECTOR (0 downto 0);    
				DIPB    : in  STD_LOGIC_VECTOR (0 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (10 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (10 downto 0);    
				DOPA    : out  STD_LOGIC_VECTOR (0 downto 0);    
				DOPB    : out  STD_LOGIC_VECTOR (0 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (7 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (7 downto 0));   
		end component;                                       
		
		component RAMB16_S18_S18 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (15 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (15 downto 0);    
				DIPA    : in  STD_LOGIC_VECTOR (1 downto 0);    
				DIPB    : in  STD_LOGIC_VECTOR (1 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (9 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (9 downto 0);    
				DOPA    : out  STD_LOGIC_VECTOR (1 downto 0);    
				DOPB    : out  STD_LOGIC_VECTOR (1 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (15 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (15 downto 0));   
		end component;                                       
	
		component RAMB16_S36_S36 is
			-- synopsys translate_off
			generic (                                             
				WRITE_MODE_A : string := "WRITE_FIRST";
				WRITE_MODE_B : string := "WRITE_FIRST"
				);
			-- synopsys translate_on
			port (                                             
				DIA     : in  STD_LOGIC_VECTOR (31 downto 0);    
				DIB     : in  STD_LOGIC_VECTOR (31 downto 0);    
				DIPA    : in  STD_LOGIC_VECTOR (3 downto 0);    
				DIPB    : in  STD_LOGIC_VECTOR (3 downto 0);    
				SSRA    : in  STD_LOGIC;                         
				ENA     : in  STD_LOGIC;                         
				WEA     : in  STD_LOGIC;                         
				SSRB    : in  STD_LOGIC;                         
				ENB     : in  STD_LOGIC;                         
				WEB     : in  STD_LOGIC;                         
				CLKA    : in  STD_LOGIC;                         
				CLKB    : in  STD_LOGIC;                         
				ADDRA   : in  STD_LOGIC_VECTOR (8 downto 0);    
				ADDRB   : in  STD_LOGIC_VECTOR (8 downto 0);    
				DOPA    : out  STD_LOGIC_VECTOR (3 downto 0);    
				DOPB    : out  STD_LOGIC_VECTOR (3 downto 0);    
				DOA     : out STD_LOGIC_VECTOR (31 downto 0);    
				DOB     : out STD_LOGIC_VECTOR (31 downto 0));   
		end component;                                       
	
		signal always_one	:std_logic;
		signal always_zero	:std_logic;
	
		signal wr_dummy 	:std_logic_vector (data_bits-1 downto 0);
		signal rd_dummy 	:std_logic_vector (data_bits-1 downto 0);
	
		signal wr_dummy2 	:std_logic_vector (31 downto 0);
		signal rd_dummy2 	:std_logic_vector (31 downto 0);
	
		signal last_in	:std_logic_vector (31 downto 0);
		signal last_out	:std_logic_vector (31 downto 0);
	
		signal wr_addr2	:std_logic_vector (8 downto 0);
		signal rd_addr2	:std_logic_vector (8 downto 0);    
	
		signal parity_dummy   :STD_LOGIC_VECTOR(3 downto 0);
	
	begin

		always_one <= '1';
		always_zero <= '0';
		rd_dummy <= (others=>'0');
		rd_dummy2 <= (others=>'0');
		parity_dummy <= (others=>'0');
		--------------------------------------------
		-- Needs smaller than a 512xN RAM, use 512x32's anyway
		--------------------------------------------
		ADDRMIN:  if addr_bits<9 generate
			begin
			CLEARMIN_ADDR: for i in rd_addr2'high downto addr_bits generate
				begin
				rd_addr2(i) <= '0';
				wr_addr2(i) <= '0';
			end generate CLEARMIN_ADDR;
		
			rd_addr2(addr_bits-1 downto 0) <= rd_addr;
			wr_addr2(addr_bits-1 downto 0) <= wr_addr;
		
			RAMMIN:  for i in 0 to (data_bits/32)-1 generate
				begin
				RAMX16:  component RAMB16_S36_S36 
					port map (
						DIA     => wr_data(32*i+31 downto 32*i),
						DIB     => rd_dummy(32*i+31 downto 32*i),
						DIPA    => parity_dummy,
						DIPB    => parity_dummy,
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr2,
						ADDRB   => rd_addr2,
						DOB     => rd_data(32*i+31 downto 32*i)
					);
			
			end generate RAMMIN;
		
			RAMMINA:  if (data_bits mod 32) /= 0 generate
				begin
				CLEARMIN: for i in last_in'high downto (data_bits mod 32) generate
					begin
					last_in(i) <= '0';
				end generate CLEARMIN;
			
				last_in((data_bits mod 32)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 32));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 32)) <= last_out((data_bits mod 32)-1 downto 0);
			
				RAMX16A:  component RAMB16_S36_S36 
					port map (
						DIA     => last_in,
						DIB     => last_in,
						DIPA    => parity_dummy,
						DIPB    => parity_dummy,
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr2,
						ADDRB   => rd_addr2,
						DOB     => last_out
					);
			end generate RAMMINA;
		end generate ADDRMIN;
	
		--------------------------------------------
		-- Use 512x36 RAM's
		--------------------------------------------
		ADDR8:  if addr_bits=9 generate
			begin
			RAM8:  for i in 0 to (data_bits/32)-1 generate
				begin
				RAMX32:  component RAMB16_S36_S36 
					port map (
						DIA     => wr_data(32*i+31 downto 32*i),
						DIB     => rd_dummy(32*i+31 downto 32*i),
						DIPA    => parity_dummy,
						DIPB    => parity_dummy,
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(32*i+31 downto 32*i)
					);
			end generate RAM8;
		
			RAM8A:  if (data_bits mod 32) /= 0 generate
				begin
				CLEAR8: for i in last_in'high downto (data_bits mod 32) generate
					begin
					last_in(i) <= '0';
				end generate CLEAR8;
			
				last_in((data_bits mod 32)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 32));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 32)) <= last_out((data_bits mod 32)-1 downto 0);
			
				RAMX32A:  component RAMB16_S36_S36 
					port map (
						DIA     => last_in,
						DIB     => last_in,
						DIPA    => parity_dummy,
						DIPB    => parity_dummy,
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => last_out
					);
			end generate RAM8A;
		end generate ADDR8;
	
		--------------------------------------------
		-- Use 1Kx18 RAM's 
		--------------------------------------------
		ADDR9:  if addr_bits=10 generate
			begin
			RAM9:  for i in 0 to (data_bits/16)-1 generate
				begin     
				RAMX8:  component RAMB16_S18_S18 
					port map (
						DIA     => wr_data(16*i+15 downto 16*i),
						DIB     => rd_dummy(16*i+15 downto 16*i),
						DIPA    => parity_dummy(1 downto 0),
						DIPB    => parity_dummy(1 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(16*i+15 downto 16*i)
					);
			end generate RAM9;
		
			RAM9A:  if (data_bits mod 16) /= 0 generate
				begin
				CLEAR9: for i in last_in'high downto (data_bits mod 16) generate
					begin
					last_in(i) <= '0';
				end generate CLEAR9;
			
				last_in((data_bits mod 16)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 16));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 16)) <= last_out((data_bits mod 16)-1 downto 0);
			
				RAMX8A:  component RAMB16_S18_S18 
					port map (
						DIA     => last_in(15 downto 0),
						DIB     => last_in(15 downto 0),
						DIPA    => parity_dummy(1 downto 0),
						DIPB    => parity_dummy(1 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => last_out(15 downto 0)
					);
			end generate RAM9A;
		end generate ADDR9;
	
		--------------------------------------------
		-- Use 2k x 8 RAM's
		--------------------------------------------
		ADDR10:  if addr_bits=11 generate
			begin
			RAM10:  for i in 0 to (data_bits/8)-1 generate
				begin
				RAMX4:  component RAMB16_S9_S9 
					port map (
						DIA     => wr_data(8*i+7 downto 8*i),
						DIB     => rd_dummy(8*i+7 downto 8*i),
						DIPA    => parity_dummy(0 downto 0),
						DIPB    => parity_dummy(0 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(8*i+7 downto 8*i)
					);
			end generate RAM10;
		
			RAM10A:  if (data_bits mod 8) /= 0 generate
				begin
				CLEAR10: for i in last_in'high downto (data_bits mod 8) generate
					begin
					last_in(i) <= '0';
				end generate CLEAR10;
			
				last_in((data_bits mod 8)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 8));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 8)) <= last_out((data_bits mod 8)-1 downto 0);
			
				RAMX4A:  component RAMB16_S9_S9 
					port map (
						DIA     => last_in(7 downto 0),
						DIB     => last_in(7 downto 0),
						DIPA    => parity_dummy(0 downto 0),
						DIPB    => parity_dummy(0 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => last_out(7 downto 0)
					);
			end generate RAM10A;
		end generate ADDR10;
	
		--------------------------------------------
		-- Use 4k x 4 RAM's
		--------------------------------------------
		ADDR12:  if addr_bits=12 generate
			begin
			RAM11:  for i in 0 to (data_bits/4)-1 generate
				begin
				RAMX2:  component RAMB16_S2_S2 
					port map (
						DIA     => wr_data(4*i+3 downto 4*i),
						DIB     => rd_dummy(4*i+3 downto 4*i),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(4*i+3 downto 4*i)
					);
			end generate RAM11;
		
			RAM12A:  if (data_bits mod 4) /= 0 generate
				begin
				CLEAR11: for i in last_in'high downto (data_bits mod 4) generate
					begin
					last_in(i) <= '0';
				end generate CLEAR11;
			
				last_in((data_bits mod 4)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 4));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 4)) <= last_out((data_bits mod 4)-1 downto 0);
			
				RAMX2A:  component RAMB16_S2_S2 
					port map (
						DIA     => last_in(1 downto 0),
						DIB     => last_in(1 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => last_out(1 downto 0)
					);
			end generate RAM12A;
		end generate ADDR12;
	
		--------------------------------------------
		-- Use 8k x 2 RAM's
		--------------------------------------------
		ADDR13:  if addr_bits=13 generate
			begin
			RAM13:  for i in 0 to (data_bits/2)-1 generate
				begin
				RAMX2:  component RAMB16_S2_S2 
					port map (
						DIA     => wr_data(2*i+1 downto 2*i),
						DIB     => rd_dummy(2*i+1 downto 2*i),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(2*i+1 downto 2*i)
					);
			end generate RAM13;
		
			RAM13A:  if (data_bits mod 2) /= 0 generate
				begin
				CLEAR13: for i in last_in'high downto (data_bits mod 2) generate
					begin
					last_in(i) <= '0';
				end generate CLEAR13;
			
				last_in((data_bits mod 2)-1 downto 0) <= wr_data(data_bits-1 downto data_bits-(data_bits mod 2));
				rd_data(data_bits-1 downto data_bits-(data_bits mod 2)) <= last_out((data_bits mod 2)-1 downto 0);
			
				RAMX2A:  component RAMB16_S2_S2 
					port map (
						DIA     => last_in(1 downto 0),
						DIB     => last_in(1 downto 0),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => last_out(1 downto 0)
					);
			end generate RAM13A;
		end generate ADDR13;
	
		--------------------------------------------
		-- Use 16kx1 RAM's
		--------------------------------------------
		ADDR14:  if addr_bits=14 generate
			begin
			RAM14:  for i in 0 to data_bits-1 generate
				begin
				RAMX1:  component RAMB16_S1_S1 
					port map (
						DIA     => wr_data(i downto i),
						DIB     => rd_dummy(i downto i),
						SSRA    => always_zero,
						ENA     => always_one,
						WEA     => wr_en,
						SSRB    => always_zero,
						ENB     => rd_en,
						WEB     => always_zero,
						CLKA    => wr_CLK,
						CLKB    => rd_CLK,
						ADDRA   => wr_addr,
						ADDRB   => rd_addr,
						DOB     => rd_data(i downto i)
					);
			end generate RAM14;
		end generate ADDR14;
	
		--  --------------------------------------------
		--  -- Requires larger than a 16K x N RAM, use a 2-D array of 4Kx1's
		--  --------------------------------------------
		--  ADDRMAX:  if addr_bits>14 generate
		--      signal wr_en_col :std_logic_vector ((2**(addr_bits-14))-1 downto 0);
		--  begin
		--    RAMMAX:  for i in 0 to data_bits-1 generate
		--    begin
		--      RAMXMAXA:  component ram_x1_dp_block generic map
		--                    (addr_bits)
		--                  port map
		--                    (reset, wr_clk, wr_en, wr_addr, wr_data(i),
		--                            rd_clk, rd_addr, rd_data(i));
		--    end generate RAMMAX;
		--  end generate ADDRMAX;
	
	end arch_ram_dp_block_v2;
