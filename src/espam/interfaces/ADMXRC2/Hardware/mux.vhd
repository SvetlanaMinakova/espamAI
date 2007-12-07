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
--  File: C:\raikin\fpga_template\active_hdl\top_level\top_level\src\mux.vhd
--  created by Design Wizard: 10/17/02 17:18:45
--

library IEEE;
use IEEE.std_logic_1164.all;

entity mux is
	port (
       H_DW0  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI0 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host 
       H_AD0  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO0  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW0  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI0 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from our design
       D_AD0  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO0  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW0    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI0   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra0    : out std_logic_vector (19 downto 0);
       rc0    : out std_logic_vector (8 downto 0);
		
		
       H_DW1  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI1 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host
       H_AD1  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO1  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW1  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI1 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from our design
       D_AD1  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO1  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW1    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI1   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra1    : out std_logic_vector (19 downto 0);
       rc1    : out std_logic_vector (8 downto 0);
		

       H_DW2  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI2 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host
       H_AD2  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO2  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW2  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI2 : in STD_LOGIC_VECTOR (31 downto 0);	-- tristate from our design
       D_AD2  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO2  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW2    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI2   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra2    : out std_logic_vector (19 downto 0);
       rc2    : out std_logic_vector (8 downto 0);
		
       H_DW3  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI3 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host
       H_AD3  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO3  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW3  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI3 : in STD_LOGIC_VECTOR (31 downto 0);	-- tristate from our design
       D_AD3  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO3  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW3    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI3   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra3    : out std_logic_vector (19 downto 0);
       rc3    : out std_logic_vector (8 downto 0);
		
       H_DW4  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI4 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host
       H_AD4  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO4  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW4  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI4 : in STD_LOGIC_VECTOR (31 downto 0);	-- tristate from our design
       D_AD4  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO4  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW4    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI4   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra4    : out std_logic_vector (19 downto 0);
       rc4    : out std_logic_vector (8 downto 0);
	   
       H_DW5  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from the host
       H_TRI5 : in STD_LOGIC_VECTOR (31 downto 0);  -- tristate from the host
       H_AD5  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from host 
       H_CO5  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from host
       D_DW5  : in STD_LOGIC_VECTOR (31 downto 0);  -- date write from our design
       D_TRI5 : in STD_LOGIC_VECTOR (31 downto 0);	-- tristate from our design
       D_AD5  : in STD_LOGIC_VECTOR (19 downto 0);  -- address from design 
       D_CO5  : in STD_LOGIC_VECTOR (8 downto 0);   -- control from design
       DW5    : out STD_LOGIC_VECTOR (31 downto 0);	-- data write to buffer (sram)
       TRI5   : out STD_LOGIC_VECTOR (31 downto 0); -- tristate to buffer 
       ra5    : out std_logic_vector (19 downto 0);
       rc5    : out std_logic_vector (8 downto 0);
		
       RST   : in std_logic;
       CNTRL : in STD_LOGIC_VECTOR(31 downto 0)
	);
end mux;

architecture mux of mux is 

-- the command register is subdevided in the following way:
-- bits 31 downto 26 are masks for the memory banks. '0' - host access. '1' - design access
-- bit 0 is RESET stage (active high) 
-- bit 1 is Initial (write) Memory stage (active high) 
-- bit 2 is Read Memory stage (active high)
-- bit 3 is Execute stage (active high)
-- define bit locations in the command register
constant comReg_bit_RESET   : natural := 0;  -- reset bit 
constant comReg_bit_InitMEM : natural := 1;  -- initialize memory bit 
constant comReg_bit_ReadMEM : natural := 2;  -- read memory bit
constant comReg_bit_EXE     : natural := 3;  -- execute bit
constant comReg_bit_bank0   : natural := 26; -- bank0 access bit
constant comReg_bit_bank1   : natural := 27; -- bank1 access bit
constant comReg_bit_bank2   : natural := 28; -- bank2 access bit
constant comReg_bit_bank3   : natural := 29; -- bank3 access bit
constant comReg_bit_bank4   : natural := 30; -- bank4 access bit
constant comReg_bit_bank5   : natural := 31; -- bank5 access bit

begin

-- multiplexer for memory bank0					   
	

pr_mux0: process( CNTRL, H_DW0, H_TRI0, H_AD0, D_CO0, D_AD0, D_TRI0, D_DW0, H_CO0 ) 
begin		 
  if CNTRL(comReg_bit_bank0) = '0' then -- data from host
    DW0  <= H_DW0;
    TRI0 <= H_TRI0;
    ra0  <= H_AD0;
    rc0  <= H_CO0;				
  else					
    DW0  <= D_DW0;
    TRI0 <= D_TRI0;
    ra0  <= D_AD0;
    rc0  <= D_CO0;   
  end if;	
end process;

-- multiplexer for memory bank 1
pr_mux1: process( CNTRL, H_DW1, H_TRI1, H_AD1, D_CO1, D_AD1, D_TRI1, D_DW1, H_CO1 ) 
begin	
  if CNTRL(comReg_bit_bank1) = '0' then -- data from host
    DW1  <= H_DW1;
    TRI1 <= H_TRI1;   
    ra1  <= H_AD1;
    rc1  <= H_CO1;
  else					
    DW1  <= D_DW1;
    TRI1 <= D_TRI1;	       
    ra1  <= D_AD1;
    rc1  <= D_CO1;   
  end if;	
end process;

-- multiplexer for memory bank 2
pr_mux2: process( CNTRL, H_DW2, H_TRI2, H_AD2, D_CO2, D_AD2, D_TRI2, D_DW2, H_CO2 ) 
begin				
  if CNTRL(comReg_bit_bank2) = '0' then -- data from host
    DW2  <= H_DW2;
    TRI2 <= H_TRI2;
    ra2  <= H_AD2;
    rc2  <= H_CO2;   
  else					
    DW2  <= D_DW2;
    TRI2 <= D_TRI2;  
    ra2  <= D_AD2;
    rc2  <= D_CO2;
  end if;	 
end process;

-- multiplexer for memory bank 3
pr_mux3: process( CNTRL, H_DW3, H_TRI3, H_AD3, D_CO3, D_AD3, D_TRI3, D_DW3, H_CO3 ) 
begin	 	   
  if CNTRL(comReg_bit_bank3) = '0' then -- data from host
    DW3  <= H_DW3;
    TRI3 <= H_TRI3;
    ra3  <= H_AD3;
    rc3  <= H_CO3;
  else					
    DW3  <= D_DW3;
    TRI3 <= D_TRI3;  
    ra3  <= D_AD3;
    rc3  <= D_CO3;   
  end if;	
end process;

-- multiplexer for memory bank 4
pr_mux4: process( CNTRL, H_DW4, H_TRI4, H_AD4, D_CO4, D_AD4, D_TRI4, D_DW4, H_CO4 ) 
begin		   
  if CNTRL(comReg_bit_bank4) = '0' then -- data from host
    DW4  <= H_DW4;
    TRI4 <= H_TRI4;
    ra4  <= H_AD4;
    rc4  <= H_CO4;   
  else					
    DW4  <= D_DW4;
    TRI4 <= D_TRI4;  
    ra4  <= D_AD4;
    rc4  <= D_CO4;   
  end if;	  
end process;

-- multiplexer for memory bank 5
pr_mux5: process( CNTRL, H_DW5, H_TRI5, H_AD5, D_CO5, D_AD5, D_TRI5, D_DW5, H_CO5 ) 
begin				   
  if CNTRL(comReg_bit_bank5) = '0' then -- data from host
    DW5  <= H_DW5;
    TRI5 <= H_TRI5;
    ra5  <= H_AD5;
    rc5  <= H_CO5;   
  else					
    DW5  <= D_DW5;
    TRI5 <= D_TRI5;
    ra5  <= D_AD5;
    rc5  <= D_CO5;
  end if;	   
end process;
 	   
end mux;
