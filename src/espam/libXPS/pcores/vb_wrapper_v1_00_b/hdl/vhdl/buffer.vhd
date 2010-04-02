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
use IEEE.std_logic_unsigned.all;

use work.typedef.all;

entity VirtBuffer is
  generic ( 								  
      N_CH : natural:=4; 	
      CH_SIZE : t_ch_size := ( 0=>511, 1=>250, 2=>80, 3=>100, others=>1 );
      DATA_BITS : natural := 32;
      PROCESSOR_TYPE : natural := 0 -- '0' - PowerPC; '1' - MicroBlaze
  );
  port (
      RST  : in std_logic;
      CLK  : in std_logic;

-- Crossbar switch interface (read from the buffer)
      CB_READ_IN   : in std_logic; -- read strobe - other processors read from the BUFFER
      CB_DATA_OUT  : out std_logic_vector(DATA_BITS-1 downto 0);
      CB_EMPTY_OUT : out std_logic; -- empty signal corresponding to FIFO(CB_FIFO_SEL)

-- Crossbar switch interface (the procesor reads from the crossbar)
      CB_READ_OUT  : out std_logic; -- read signal (to the crossbar) the processor reads from port
      CB_EMPTY_IN  : in std_logic; -- empty signal (from the crossbar)
      CB_PORT_IN   : in std_logic_vector(DATA_BITS-1 downto 0); -- port for reading data (the processor reads from the crossbar)

-- Crossbar control interface
      CB_REQ_WR    : out std_logic;
      CB_REQ_ACKN  : in std_logic;
      CB_REQ_DATA  : out std_logic_vector(31 downto 0);
      CB_FIFO_SEL  : in std_logic_vector(7 downto 0);

-- Processor interface
      P_ADDR     : in std_logic_vector(31 downto 0);
      P_DATA_IN  : in std_logic_vector(DATA_BITS-1 downto 0);
      P_DATA_OUT : out std_logic_vector(DATA_BITS-1 downto 0);

      P_WRITE    : in std_logic;
      P_READ     : in std_logic;
      P_CS       : in std_logic
  );
end VirtBuffer;

architecture RTL of VirtBuffer is
-------------------------------------------------------------------------------
-- Component declarations
-------------------------------------------------------------------------------
component multi_fifo
	generic ( 
                N_CH : natural:=1; 	
		DEPTHS : t_ch_size := ( 0=>511, others=>1 );

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
end component;

-------------------------------------------------------------------------------
-- Signal declarations
-------------------------------------------------------------------------------  
  -- FIFOs 'FULL' signals
  signal sl_full : std_logic_vector(N_CH-1 downto 0);

  -- FIFOs 'EMPTY' signals
  signal sl_empty : std_logic_vector(N_CH-1 downto 0);

  -- FIFOs 'WRITE' signals
  --signal sl_write : std_logic_vector(N_CH-1 downto 0);
  signal sl_write : std_logic_vector(7 downto 0);
  signal sl_wr_en : std_logic;

  -- FIFOs 'READ' signals
  signal sl_read   : std_logic_vector(N_CH-1 downto 0);
  signal sl_ack_rd : std_logic; 

  signal sl_P_ADDR : std_logic_vector(7 downto 0);
  signal sl_ADDR_DIV2 : std_logic_vector(7 downto 0);  

begin


sl_P_ADDR <= P_ADDR(9 downto 2);	
sl_ADDR_DIV2 <= '0' & sl_P_ADDR(7 downto 1); -- using in the estimation of fifo indexes
	
---------------------------------------------------------------------------------------------
-- 'empty' signals from the FIFOs to the crossbar switch (output mux)
---------------------------------------------------------------------------------------------
  CB_EMPTY_OUT <= sl_empty( CONV_INTEGER(CB_FIFO_SEL) );

---------------------------------------------------------------------------------------------
-- "wite to request register" in crossbar controller (at address 0)
---------------------------------------------------------------------------------------------
  CB_REQ_WR <= '1' when P_CS = '1' and P_WRITE = '1' and ( CONV_INTEGER( sl_P_ADDR ) = 0 )
          else '0'; 
  CB_REQ_DATA <= P_DATA_IN;

---------------------------------------------------------------------------------------------
-- Read from crossbar process + special timing avoiding multiple reads from a FIFO
-- The FIFOs are read by the crossbar side
---------------------------------------------------------------------------------------------
  process( CLK )
  begin	
    if rising_edge( CLK ) then
       sl_ack_rd <= P_READ; 
    end if;		 
  end process; 

PPC: if( PROCESSOR_TYPE = 0 ) generate
    CB_READ_OUT <= '1' when P_CS = '1' and P_READ = '1' and sl_ack_rd = '0' and ( CONV_INTEGER( sl_P_ADDR ) = 0 ) 	  
              else '0';	  
  end generate;	  

MB:  if( PROCESSOR_TYPE = 1 ) generate
    CB_READ_OUT <= '1' when P_CS = '1' and P_READ = '1' and ( CONV_INTEGER( sl_P_ADDR ) = 0 ) 	  
              else '0';	  
  end generate;	  

  sl_write <=  sl_ADDR_DIV2-1;
  sl_wr_en <=  P_CS and P_WRITE;

------------------------------------------------
-- |      Memory map of the buffer          | --
---+----------------------------------------|---
-- | address | action  description          | --
-- +---------+------------------------------| --
-- |       0 | write request for reading    | --
-- |       0 | read data from crossbar port | --
-- |       1 | read 'empty' from crossbar   | -- 
-- +---------+------------------------------| --
-- |       2 | write data in FIFO 1         | -- 
-- |       3 | read 'full' of FIFO 1        | -- 
-- +---------+------------------------------| --
-- |       4 | write data in FIFO 2         | -- 
-- |       5 | read 'full' of FIFO 2        | -- 
-- +---------+------------------------------| --
-- |       6 | write data in FIFO 3         | -- 
-- |       7 | read 'full' of FIFO 3        | -- 
-- +---------+------------------------------| --
-- |    even | write data in a FIFO         | -- 
-- |     odd | read 'full' of a FIFO        | -- 
---------------------------------------------------------------------------------------------
-- write fifo index = address/2-1 
-- we have to write in the even addresses for example 3 channels, addresses 2,4,6. 
-- The FIFOs are (2 downto 0). So, the write indexes => 0=2/2-1; 1=4/2-1; 2=6/2-1.
---------------------------------------------------------------------------------------------
-- read from fifo index = address/2-1 
-- we have to read from addr. 0,1 and from the odd addr. for example 3 chan., addresses 3,5,7. 
-- The FIFOs are (2 downto 0). So, read indexes => 0=3/2-1; 1=5/2-1; 2=7/2-1.
---------------------------------------------------------------------------------------------


---------------------------------------------------------------------------------------------
-- Output muxes (processor side) process
-- In order to keep the generic structure it contains two cascaded muxes
-- The first one: CB_PORT, (CB_REQ_ACKN, CB_EMPTY), Output from the second mux
-- The second mux is for the 'full' signals
---------------------------------------------------------------------------------------------

  OUT_MUX_prcs : process(sl_full, CB_PORT_IN, CB_EMPTY_IN, CB_REQ_ACKN, P_CS, P_READ, sl_P_ADDR, sl_ADDR_DIV2)
     variable vr_full : std_logic;
  begin
     vr_full := sl_full( CONV_INTEGER(sl_ADDR_DIV2-1) );  -- read index (the second mux)

     case( CONV_INTEGER(sl_P_ADDR) ) is -- (the first mux)
        when 0 => P_DATA_OUT <= CB_PORT_IN;
        when 1 => P_DATA_OUT(1 downto 0) <= CB_REQ_ACKN & CB_EMPTY_IN;
                  P_DATA_OUT(31 downto 2) <= (others => '0');
        when others => P_DATA_OUT(0) <= vr_full; --(the second mux)
                       P_DATA_OUT(31 downto 1) <= (others => '0');
        end case;
  end process;

---------------------------------------------------------------------------------------------
-- All the FIFOs are mapped onto one piece of memory
---------------------------------------------------------------------------------------------
  FIFO_INSTANCES : multi_fifo 
	generic map (
		N_CH   => N_CH, 	
		DEPTHS => CH_SIZE, 

		data_bits         => DATA_BITS,
		register_out_flag => 1,
		block_type        => 2
		)
	port map (  
		reset	 => RST,   
		clk	 => CLK,   
			  
		wr_data	 => P_DATA_IN,   
		wr_sel   => sl_write,   
		wr_en    => sl_wr_en,   
			  
		rd_data	 => CB_DATA_OUT,   
		rd_sel   => CB_FIFO_SEL,  	
		rd_en    => CB_READ_IN,

		full	 => sl_full,
		empty	 => sl_empty
		);
---------------------------------------------------------------------------------------------
end RTL;
