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

library work;
use work.ram_lib.all;
use work.typedef.all;

entity multi_fifo is
	generic (
		N_CH : natural := 1; 			     -- When only 1 channel is used one memory location is sacrified
		DEPTHS : t_ch_size := ( 0=>511, others=>1 ); -- In 512x32 configuration - 511 available locations 

		data_bits  : natural := 32;
		register_out_flag : natural := 1;
		block_type : natural := 2
		);
	port (  
		reset	    : in std_logic;
		clk	    : in std_logic;
		
		wr_data	    : in std_logic_vector (data_bits-1 downto 0);
		wr_sel      : in std_logic_vector(7 downto 0);  -- binary coded
		wr_en       : in std_logic; 
		
		rd_data	    : out std_logic_vector (data_bits-1 downto 0);
		rd_sel      : in std_logic_vector(7 downto 0);  -- binary coded
		rd_en      : in std_logic;  

		full	    : out std_logic_vector(N_CH-1 downto 0);
		empty	    : out std_logic_vector(N_CH-1 downto 0)
		);
end multi_fifo;

architecture arch_multi_fifo of multi_fifo is
-------------------------------------------------------------------------------
-- Function declarations
-------------------------------------------------------------------------------
    function fn_log2(
        x : in natural)
        return natural is
    begin
        for i in 0 to 31 loop
            if x <= 2**i then
	        if i = 0 then
                   return 1;
		else
                   return i;
		end if;                
            end if;
        end loop;
        
        assert false
            report "VirtBuffer_ver2.vhd: log2() invalid argument"
            severity failure;
        return 0;
    end;
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
    function fn_depth_size(  
        sizes : in t_ch_size; nmbr : in natural)
        return natural is

	variable vr_temp : natural :=0 ;
    begin
        for i in 0 to nmbr-1 loop
	   vr_temp := vr_temp + sizes(i);
	end loop;
	return vr_temp; -- Note: If we need 1 FIFO, max fifo size = memory size-1!!!!!!!!
    end;  
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
    function fn_base_addr(  
        sizes : in t_ch_size; nmbr : in natural)
        return natural is

	variable vr_temp : natural :=0 ;
    begin
        if( nmbr=0 ) then -- We assume that the first fifo channel starts from address ZERO
	   return 0;
	end if;

        for i in 0 to nmbr-1 loop
	   vr_temp := vr_temp + sizes(i);
	end loop;
	return vr_temp;
    end;  
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
   constant ADDRESS_BITS : natural := fn_log2( fn_depth_size( DEPTHS, N_CH ) );

	signal rd_allow	    : std_logic;
	signal sl_rd_allow    : std_logic_vector(N_CH-1 downto 0);

	signal wr_allow	      : std_logic;
	signal sl_wr_allow    : std_logic_vector(N_CH-1 downto 0);

	signal empty_count    : std_logic_vector (ADDRESS_BITS-1 downto 0);
	signal aempty_count   : std_logic_vector (ADDRESS_BITS-1 downto 0);

	signal sl_not_reg_out : std_logic; 
	signal sl_read 	    : std_logic;
	signal sl_rd_data     : std_logic_vector (data_bits-1 downto 0); 

	type tp_FIFOS_addr is array (N_CH-1 downto 0) of std_logic_vector(ADDRESS_BITS-1 downto 0);
	signal rd_addr        : tp_FIFOS_addr;
	signal wr_addr	      : tp_FIFOS_addr;
	signal count_int      : tp_FIFOS_addr;
	signal sl_rd_addr     : std_logic_vector (ADDRESS_BITS-1 downto 0);
	signal sl_wr_addr     : std_logic_vector (ADDRESS_BITS-1 downto 0);

	signal empty_int      : std_logic_vector(N_CH-1 downto 0);
	signal full_int	      : std_logic_vector(N_CH-1 downto 0);

	signal full_count     : tp_FIFOS_addr;
	signal afull_count    : tp_FIFOS_addr;		
	
	signal sl_rd_sel : natural range 0 to N_CH-1 := 0;	
	signal sl_wr_sel : natural range 0 to N_CH-1 := 0;	
	
begin

	full    <= full_int;
	empty   <= empty_int;	
	
	sl_rd_sel <= CONV_INTEGER( rd_sel ); 
	sl_wr_sel <= CONV_INTEGER( wr_sel ); 
	----------------------------------------------
	-- Some constants
	----------------------------------------------
	FULL_CNT_GEN : for i in 0 to N_CH-1 generate 
        begin  
	   full_count(i)  <= CONV_STD_LOGIC_VECTOR( DEPTHS(i),   ADDRESS_BITS );
	   afull_count(i) <= CONV_STD_LOGIC_VECTOR( DEPTHS(i)-1, ADDRESS_BITS ); 	
	end generate FULL_CNT_GEN;

	empty_count  <= (others=>'0');
	aempty_count <= CONV_STD_LOGIC_VECTOR( 1, ADDRESS_BITS );  
	----------------------------------------------
	----------------------------------------------
	rd_allow <= not(empty_int(sl_rd_sel)) and rd_en;
	wr_allow <= not(full_int( sl_wr_sel)) and wr_en;
	----------------------------------------------
	-- Emulation of the timing if there is or NOT register at FIFO's output
	----------------------------------------------
	REG_OUT_BRAM : if register_out_flag = 1 generate
	begin
	   sl_not_reg_out <= '0';
	end generate REG_OUT_BRAM;

	NOT_REG_OUT : if register_out_flag = 0 generate
	begin
	   sl_not_reg_out <= '1';
	end generate NOT_REG_OUT;

	sl_read <= sl_not_reg_out or rd_allow;	
	----------------------------------------------
	-- FIFO with BRAMs
	---------------------------------------------- 		
	DP_RAMB : if block_type = 2 generate
	begin

  	   RAM_BLOCK0_V2: ram_dp_block_v2
		generic map (
			addr_bits => ADDRESS_BITS, 
			data_bits => data_bits
			    )
		port map (
			reset   => reset, 
			wr_clk  => clk, 
			wr_en   => wr_allow, 
			wr_addr => sl_wr_addr, 
			wr_data => wr_data, 
			
			rd_clk  => clk, 
			rd_en   => sl_read, 
			rd_addr => sl_rd_addr, 
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
	 	     generic map ( ADDRESS_BITS )
		     port map (
			  clk	     => clk, 
			  port1_wr   => wr_allow, 
			  port1_addr => sl_wr_addr, 
			  port1_din  => wr_data(i), 
			  port1_dout => open,

			  port2_addr => sl_rd_addr, 
  			  port2_dout => sl_rd_data(i)
		     );
	   end generate RAM_DP;

	   proc0: process (clk, reset)
	   begin
	      if( reset ='1') then
	 	 rd_data <= (others =>'0');
	      elsif( clk'event and clk = '1') then 
		 if sl_read = '1' and rd_allow = '1' then
		    rd_data <= sl_rd_data;
		 end if;
	      end if;
	   end process proc0;    
					
	end generate DP_LUT;	  
	
	----------------------------------------------
	-- Read address counters
	----------------------------------------------
	process (clk, reset)
	begin
	   if reset='1' then

	      for i in 0 to N_CH-1 loop
	         rd_addr(i) <= CONV_STD_LOGIC_VECTOR( fn_base_addr(DEPTHS, i), ADDRESS_BITS );
	      end loop;
		
	   elsif clk'event and clk='1' then

	        if rd_allow='1' then
		        for i in 0 to N_CH-1 loop 
		 
	               if( sl_rd_sel=i ) then	
			             if rd_addr(i) = (fn_base_addr(DEPTHS, i) + DEPTHS(i) - 1) then
	  	 	                 rd_addr(i) <= CONV_STD_LOGIC_VECTOR( fn_base_addr(DEPTHS, i), ADDRESS_BITS );
                      else
	 		                 rd_addr(i) <= rd_addr(i) + 1;
                      end if; 
	               end if;

	            end loop; 
	        end if;		   
	   end if;
	end process;

	----------------------------------------------
	-- Write address counters
	----------------------------------------------
	process (clk, reset)
		variable vr_op_1 : std_logic_vector(ADDRESS_BITS-1 downto 0);
	begin
	   if reset='1' then

	      for i in 0 to N_CH-1 loop
	         wr_addr(i) <= CONV_STD_LOGIC_VECTOR( fn_base_addr(DEPTHS, i), ADDRESS_BITS );
	      end loop;

	   elsif clk'event and clk='1' then

	      if wr_allow='1' then
		      for i in 0 to N_CH-1 loop     

		          if( sl_wr_sel=i ) then	
			            if wr_addr(i) = (fn_base_addr(DEPTHS, i) + DEPTHS(i) - 1) then
	  	 	                  wr_addr(i) <= CONV_STD_LOGIC_VECTOR( fn_base_addr(DEPTHS, i), ADDRESS_BITS );
                     else
	 		                  wr_addr(i) <= wr_addr(i) + 1;
                     end if; 
	             end if;

	         end loop; 	
	      end if;
	   end if;
	end process;

	----------------------------------------------
	-- FIFO Read/Write address MUXes
	----------------------------------------------	
        sl_rd_addr <= rd_addr( sl_rd_sel );
        sl_wr_addr <= wr_addr( sl_wr_sel );

	----------------------------------------------
	-- Counter of the words residing in the FIFO
	----------------------------------------------
	process (clk, reset)
	begin
	   if( reset='1' ) then
	      count_int <= (others => (others=>'0'));
	   elsif clk'event and clk='1' then

		  if wr_allow='0' and rd_allow='1' then
		  
			  count_int( sl_rd_sel ) <= count_int( sl_rd_sel ) - 1;
   
		  elsif wr_allow='1' and rd_allow='0' then

       		  count_int( sl_wr_sel ) <= count_int( sl_wr_sel ) + 1;

		  end if;
	   end if;
	end process;
	----------------------------------------------
	-- 'Empty' signals generation
	----------------------------------------------
	process (clk, reset)
	begin
	   if( reset='1' ) then
	      empty_int <= (others=>'1');
	   elsif clk'event and clk='1' then

	      for i in 0 to N_CH-1 loop     

	         if empty_int(i)='1' and wr_allow='1' and sl_wr_sel=i then
		          empty_int(i) <= '0';
		      elsif count_int(i)=aempty_count and rd_allow='1' and sl_rd_sel=i and sl_wr_sel/=i then
		          empty_int(i) <= '1';
		      elsif count_int(i)=empty_count then
		          empty_int(i) <= '1';
	   	   else
		          empty_int(i) <= '0';
	 	      end if;

	      end loop;

	   end if;
	end process;
	----------------------------------------------
	-- 'Full' signals generation
	----------------------------------------------
	process (clk, reset)
	begin
	   if reset='1' then
	      full_int <= (others=>'0');
	   elsif clk'event and clk='1' then
	                
	      for i in 0 to N_CH-1 loop     
			
	         if full_int(i)='1' and rd_allow='1' and sl_rd_sel=i then
		    		full_int(i) <= '0';
		 		elsif count_int(i)=afull_count(i) and wr_allow='1' and sl_wr_sel=i and sl_rd_sel/=i then
		    		full_int(i) <= '1';
		 		elsif count_int(i)=full_count(i) then
		    		full_int(i) <= '1';
		 		else
		    		full_int(i) <= '0';
		 		end if;

	     end loop;
	   
	   end if;
	end process;	
	
end arch_multi_fifo;

