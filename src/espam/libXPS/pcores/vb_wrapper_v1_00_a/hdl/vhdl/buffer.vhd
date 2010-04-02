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

--library packets;
--use packets.typedef.all;		   
--use packets.free_fifo.fifo_sync;
use work.typedef.all;

entity VirtBuffer is
  generic ( 								  
      N_CH : natural:=3; 	
  	  CH_SIZE : t_ch_size := ( 0=>9, 1=>10, 2=>9, others=>9 );
      PROCESSOR_TYPE : natural:=0 -- '0' - PowerPC; '1' - MicroBlaze
  );
  port (
      RST  : in std_logic;
      CLK  : in std_logic;

-- Crossbar switch interface (read from the buffer)
      CB_READ_IN   : in std_logic; -- other processors read from the BUFFER
      CB_DATA_OUT  : out std_logic_vector(31 downto 0);
      CB_EMPTY_OUT : out std_logic; -- empty signal corresponding to FIFO(CB_FIFO_SEL)

-- Crossbar switch interface (the procesor reads from the crossbar)
      CB_READ_OUT  : out std_logic; -- read signal (to the crossbar) the processor reads from port
      CB_EMPTY_IN  : in std_logic; -- empty signal (from the crossbar)
      CB_PORT_IN   : in std_logic_vector(31 downto 0); -- port for reading data (the processor reads from the crossbar)

-- Crossbar control interface
      CB_REQ_WR    : out std_logic;
      CB_REQ_ACKN  : in std_logic;
      CB_REQ_DATA  : out std_logic_vector(31 downto 0);
      CB_FIFO_SEL  : in std_logic_vector(7 downto 0);

-- Processor interface
      P_ADDR     : in std_logic_vector(31 downto 0);
      P_DATA_IN  : in std_logic_vector(31 downto 0);
      P_DATA_OUT : out std_logic_vector(31 downto 0);

      P_WRITE    : in std_logic;
      P_READ     : in std_logic;
      P_CS       : in std_logic
  );
end VirtBuffer;

architecture RTL of VirtBuffer is
-------------------------------------------------------------------------------
-- Component declarations
-------------------------------------------------------------------------------
component fifo_sync
	generic ( data_bits  :integer;
			  addr_bits  :integer;
			  block_type :integer );
	port (  reset		:in std_logic;
			clk		    :in std_logic;
			wr_en		:in std_logic;
			wr_data	    :in std_logic_vector (data_bits-1 downto 0);
			rd_en		:in std_logic;
			rd_data	    :out std_logic_vector (data_bits-1 downto 0);
			count		:out std_logic_vector (addr_bits-1 downto 0);
			full		:out std_logic;
			empty		:out std_logic
			);
end component;

-------------------------------------------------------------------------------
-- Signal declarations
-------------------------------------------------------------------------------  
  -- FIFOs 'FULL' signals
  signal sl_full : std_logic_vector(N_CH downto 0); -- The used bits are (N_CH-1 downto 0)

  -- FIFOs 'EMPTY' signals
  signal sl_empty : std_logic_vector(N_CH downto 0); -- The used bits are (N_CH-1 downto 0)

  -- FIFOs 'WRITE' signals
  signal sl_write : std_logic_vector(N_CH downto 0); -- The used bits are (N_CH-1 downto 0)

  -- FIFOs 'READ' signals
  signal sl_read   : std_logic_vector(N_CH downto 0); -- The used bits are (N_CH-1 downto 0)
  signal sl_ack_rd : std_logic; 

  -- FIFOs Read DATA BUSSes
  type tpDATABusses is array (N_CH downto 0) of std_logic_vector(31 downto 0);
  signal sl_fifo_data : tpDATABusses;
 
  signal sl_P_ADDR : std_logic_vector(7 downto 0);
  signal sl_ADDR_DIV2 : std_logic_vector(6 downto 0);  

begin
   
sl_P_ADDR <= P_ADDR(9 downto 2);	
sl_ADDR_DIV2 <= sl_P_ADDR(7 downto 1); -- using in the estimation of fifo indexes
	
---------------------------------------------------------------------------------------------
-- data and 'empty' signals from the FIFOs to the crossbar switch (output muxes)
---------------------------------------------------------------------------------------------
  CB_DATA_OUT  <= sl_fifo_data( CONV_INTEGER(CB_FIFO_SEL) );
  CB_EMPTY_OUT <= sl_empty( CONV_INTEGER(CB_FIFO_SEL) );

---------------------------------------------------------------------------------------------
-- "wite to request register" in crossbar controller (at address 0)
---------------------------------------------------------------------------------------------
  CB_REQ_WR <= '1' when P_CS = '1' and P_WRITE = '1' and ( CONV_INTEGER( sl_P_ADDR ) = 0 )
          else '0'; 
  CB_REQ_DATA <= P_DATA_IN;

---------------------------------------------------------------------------------------------
-- read from crossbar process + special timing avoiding multiple reads from a FIFO
-- the FIFOs are read by the crossbar side
---------------------------------------------------------------------------------------------
  process( CLK )
  begin	
    if rising_edge( CLK ) then
       sl_ack_rd <= P_READ; 
    end if;		 
  end process; 

PPC: if( PROCESSOR_TYPE = 0 ) generate
    CB_READ_OUT <= '1' when P_CS = '1' and P_READ = '1' and sl_ack_rd = '0' and (CONV_INTEGER( sl_P_ADDR ) = 0) else '0';
  end generate;

MB:  if( PROCESSOR_TYPE = 1 ) generate
    CB_READ_OUT <= '1' when P_CS = '1' and P_READ = '1' and (CONV_INTEGER( sl_P_ADDR ) = 0) else '0'; 
  end generate;	  

 ----------------------------------------------------------------------------------------------  
 -- Read FIFOs DE-MUX
 ----------------------------------------------------------------------------------------------
    
  RD_CB_prcss : process(CB_FIFO_SEL, CB_READ_IN)
  begin
    for i in 0 to N_CH-1 loop
      if( i=CONV_INTEGER(CB_FIFO_SEL)  ) then
         sl_read(i) <= CB_READ_IN;
      else
         sl_read(i) <= '0';
      end if;
    end loop;
  end process;

---------------------------------------------------------------------------------------------
-- write FIFO signals (DE-MUX). The FIFOs are written by a processor
---------------------------------------------------------------------------------------------
  WR_CB_prcss : process(sl_P_ADDR, P_CS, P_WRITE)
  begin
    for i in 0 to N_CH-1 loop
    --for i in 1 to N_CH-1 loop
      if( i = CONV_INTEGER(sl_ADDR_DIV2-1)  ) then  -- write index
         sl_write(i) <= P_CS and P_WRITE;
      else
         sl_write(i) <= '0';
      end if;
    end loop;
  end process;

  --sl_write(0) <= P_CS and P_WRITE;
  
------------------------------------------------
-- |      Memory map of the buffer          | --
---+----------------------------------------|---
-- | address | action  description          | --
-- +---------+------------------------------| --
-- |       0 | write request for reading    | --
-- |       0 | read data from crossbar port | --
-- |       1 | read: 'empty' from crossbar  | -- 
-- |         |       'request acknowledge'  | --
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
-- The first one: CB_PORT, (CB_REQ_ACKN & CB_EMPTY), Input from the second mux
-- The second one is for the 'full' signals
---------------------------------------------------------------------------------------------

  OUT_MUX_prcs : process(sl_full, CB_PORT_IN, CB_EMPTY_IN, CB_REQ_ACKN, P_CS, P_READ, sl_P_ADDR, sl_ADDR_DIV2)
     variable vr_full : std_logic;
  begin
     vr_full := sl_full( CONV_INTEGER(sl_ADDR_DIV2-1) );  -- read index

     case( CONV_INTEGER(sl_P_ADDR) ) is
        when 0 => P_DATA_OUT <= CB_PORT_IN;
        when 1 => P_DATA_OUT(1 downto 0) <= CB_REQ_ACKN & CB_EMPTY_IN;
                  P_DATA_OUT(31 downto 2) <= (others => '0');
        when others => P_DATA_OUT(0) <= vr_full;
                       P_DATA_OUT(31 downto 1) <= (others => '0');
        end case;

  end process;

---------------------------------------------------------------------------------------------
-- Each FIFO occupies 1 BRAM (Virtex2 library) and has capacity 512 words by 32 bits
---------------------------------------------------------------------------------------------
  FIFO_INSTANCES :
     for i in 0 to N_CH-1 generate
     begin
        FIFO : fifo_sync
        generic map(
           block_type => 2, -- BRAMs used
           data_bits  => 32,
           addr_bits  => CH_SIZE(i) --9
        ) 
        port map (
           reset   => RST,
           clk     => CLK,
           wr_en   => sl_write(i),
           rd_en   => sl_read(i),
           wr_data => P_DATA_IN,
           rd_data => sl_fifo_data(i),
		   count    => open,
           full    => sl_full(i),
           empty   => sl_empty(i)
        );
     end generate;
---------------------------------------------------------------------------------------------
end RTL;
