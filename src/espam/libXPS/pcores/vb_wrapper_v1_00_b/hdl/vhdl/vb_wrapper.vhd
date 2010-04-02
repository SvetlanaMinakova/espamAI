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

use work.typedef.all;

entity vb_wrapper is
  generic ( 								  
    N_CH        : natural:=1; 	
	CH_SIZE_1   : natural := 512;
	CH_SIZE_2   : natural := 512;
	CH_SIZE_3   : natural := 512;
	CH_SIZE_4   : natural := 512;
	CH_SIZE_5   : natural := 512;
	CH_SIZE_6   : natural := 512;
	CH_SIZE_7   : natural := 512;
	CH_SIZE_8   : natural := 512;
	CH_SIZE_9   : natural := 512;
	CH_SIZE_10  : natural := 512;
	CH_SIZE_11  : natural := 512;
	CH_SIZE_12  : natural := 512;
	CH_SIZE_13  : natural := 512;
	CH_SIZE_14  : natural := 512;
	CH_SIZE_15  : natural := 512;
	CH_SIZE_16  : natural := 512;
	CH_SIZE_17  : natural := 512;
	CH_SIZE_18  : natural := 512;
	CH_SIZE_19  : natural := 512;
	CH_SIZE_20  : natural := 512;
	CH_SIZE_21  : natural := 512; 
	CH_SIZE_22  : natural := 512; 
	CH_SIZE_23  : natural := 512; 
	CH_SIZE_24  : natural := 512; 
	CH_SIZE_25  : natural := 512; 
	CH_SIZE_26  : natural := 512; 
	CH_SIZE_27  : natural := 512; 
	CH_SIZE_28  : natural := 512; 
	CH_SIZE_29  : natural := 512; 
	CH_SIZE_30  : natural := 512;
	CH_SIZE_31  : natural := 512;
	CH_SIZE_32  : natural := 512;
	CH_SIZE_33  : natural := 512;
	CH_SIZE_34  : natural := 512;
	CH_SIZE_35  : natural := 512;
	CH_SIZE_36  : natural := 512;
	CH_SIZE_37  : natural := 512;
	CH_SIZE_38  : natural := 512;
	CH_SIZE_39  : natural := 512;
	CH_SIZE_40  : natural := 512;
	CH_SIZE_41  : natural := 512;
	CH_SIZE_42  : natural := 512;
	CH_SIZE_43  : natural := 512;
	CH_SIZE_44  : natural := 512;
	CH_SIZE_45  : natural := 512;
	CH_SIZE_46  : natural := 512;
	CH_SIZE_47  : natural := 512;
	CH_SIZE_48  : natural := 512;
	CH_SIZE_49  : natural := 512;
	CH_SIZE_50  : natural := 512;
	CH_SIZE_51  : natural := 512;
	CH_SIZE_52  : natural := 512; 
	CH_SIZE_53  : natural := 512; 
	CH_SIZE_54  : natural := 512; 
	CH_SIZE_55  : natural := 512; 
	CH_SIZE_56  : natural := 512; 
	CH_SIZE_57  : natural := 512; 
	CH_SIZE_58  : natural := 512; 
	CH_SIZE_59  : natural := 512; 
	CH_SIZE_60  : natural := 512; 
	CH_SIZE_61  : natural := 512; 
	CH_SIZE_62  : natural := 512; 
	CH_SIZE_63  : natural := 512; 
	CH_SIZE_64  : natural := 512; 
	CH_SIZE_65  : natural := 512; 
	CH_SIZE_66  : natural := 512; 
	CH_SIZE_67  : natural := 512; 
	CH_SIZE_68  : natural := 512; 
	CH_SIZE_69  : natural := 512; 
	CH_SIZE_70  : natural := 512; 
	CH_SIZE_71  : natural := 512; 
	CH_SIZE_72  : natural := 512; 
	CH_SIZE_73  : natural := 512; 
	CH_SIZE_74  : natural := 512; 
	CH_SIZE_75  : natural := 512; 
	CH_SIZE_76  : natural := 512; 
	CH_SIZE_77  : natural := 512; 
	CH_SIZE_78  : natural := 512; 
	CH_SIZE_79  : natural := 512;
	CH_SIZE_80  : natural := 512; 
	CH_SIZE_81  : natural := 512; 
	CH_SIZE_82  : natural := 512; 
	CH_SIZE_83  : natural := 512; 
	CH_SIZE_84  : natural := 512; 
	CH_SIZE_85  : natural := 512; 
	CH_SIZE_86  : natural := 512; 
	CH_SIZE_87  : natural := 512; 
	CH_SIZE_88  : natural := 512; 
	CH_SIZE_89  : natural := 512; 
	CH_SIZE_90  : natural := 512; 
	CH_SIZE_91  : natural := 512; 
	CH_SIZE_92  : natural := 512; 
	CH_SIZE_93  : natural := 512; 
	CH_SIZE_94  : natural := 512; 
	CH_SIZE_95  : natural := 512; 
	CH_SIZE_96  : natural := 512; 
	CH_SIZE_97  : natural := 512; 
	CH_SIZE_98  : natural := 512; 
	CH_SIZE_99  : natural := 512; 
	CH_SIZE_100 : natural := 512; 
	CH_SIZE_101 : natural := 512; 
	CH_SIZE_102 : natural := 512; 
	CH_SIZE_103 : natural := 512; 
	CH_SIZE_104 : natural := 512; 
	CH_SIZE_105 : natural := 512; 
	CH_SIZE_106 : natural := 512; 
	CH_SIZE_107 : natural := 512; 
	CH_SIZE_108 : natural := 512; 
	CH_SIZE_109 : natural := 512; 
	CH_SIZE_110 : natural := 512; 
	CH_SIZE_111 : natural := 512; 
	CH_SIZE_112 : natural := 512; 
	CH_SIZE_113 : natural := 512; 
	CH_SIZE_114 : natural := 512; 
	CH_SIZE_115 : natural := 512; 
	CH_SIZE_116 : natural := 512; 
	CH_SIZE_117 : natural := 512; 
	CH_SIZE_118 : natural := 512; 
	CH_SIZE_119 : natural := 512; 
	CH_SIZE_120 : natural := 512; 
	CH_SIZE_121 : natural := 512; 
	CH_SIZE_122 : natural := 512; 
	CH_SIZE_123 : natural := 512; 
	CH_SIZE_124 : natural := 512; 
	CH_SIZE_125 : natural := 512; 
	CH_SIZE_126 : natural := 512; 
	CH_SIZE_127 : natural := 512; 
	CH_SIZE_128 : natural := 512;      
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
      P_ADDR     : in std_logic_vector(0 to 31);
      P_DATA_IN  : in std_logic_vector(31 downto 0);
      P_DATA_OUT : out std_logic_vector(31 downto 0);

      P_WRITE    : in std_logic;
      P_READ     : in std_logic;
      P_CS       : in std_logic
    );
end vb_wrapper;		 

architecture elaborate of vb_wrapper is

  component VirtBuffer
  generic ( 								  
      N_CH : natural:=3; 	
      CH_SIZE : t_ch_size := ( 0=>512, 1=>512, 2=>512, others=>512 );
      DATA_BITS : natural := 32;
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
	end component;	 

    -- Channel size generics propagation
	constant ch_size_arr   : t_ch_size := ( 
			CH_SIZE_1  ,
			CH_SIZE_2  ,
			CH_SIZE_3  ,
			CH_SIZE_4  ,
			CH_SIZE_5  ,
			CH_SIZE_6  ,
			CH_SIZE_7  ,
			CH_SIZE_8  ,
			CH_SIZE_9  ,
			CH_SIZE_10 ,
			CH_SIZE_11 ,
			CH_SIZE_12 ,
			CH_SIZE_13 ,
			CH_SIZE_14 ,
			CH_SIZE_15 ,
			CH_SIZE_16 ,
			CH_SIZE_17 ,
			CH_SIZE_18 ,
			CH_SIZE_19 ,
			CH_SIZE_20 ,
			CH_SIZE_21 ,
			CH_SIZE_22 ,
			CH_SIZE_23 ,
			CH_SIZE_24 ,
			CH_SIZE_25 ,
			CH_SIZE_26 ,
			CH_SIZE_27 ,
			CH_SIZE_28 ,
			CH_SIZE_29 ,
			CH_SIZE_30 ,
			CH_SIZE_31 ,
			CH_SIZE_32 ,
			CH_SIZE_33 ,
			CH_SIZE_34 ,
			CH_SIZE_35 ,
			CH_SIZE_36 ,
			CH_SIZE_37 ,
			CH_SIZE_38 ,
			CH_SIZE_39 ,
			CH_SIZE_40 ,
			CH_SIZE_41 ,
			CH_SIZE_42 ,
			CH_SIZE_43 ,
			CH_SIZE_44 ,
			CH_SIZE_45 ,
			CH_SIZE_46 ,
			CH_SIZE_47 ,
			CH_SIZE_48 ,
			CH_SIZE_49 ,
			CH_SIZE_50 ,
			CH_SIZE_51 ,
			CH_SIZE_52 ,
			CH_SIZE_53 ,
			CH_SIZE_54 ,
			CH_SIZE_55 ,
			CH_SIZE_56 ,
			CH_SIZE_57 ,
			CH_SIZE_58 ,
			CH_SIZE_59 ,
			CH_SIZE_60 ,
			CH_SIZE_61 ,
			CH_SIZE_62 ,
			CH_SIZE_63 ,
			CH_SIZE_64 ,
			CH_SIZE_65 ,
			CH_SIZE_66 ,
			CH_SIZE_67 ,
			CH_SIZE_68 ,
			CH_SIZE_69 ,
			CH_SIZE_70 ,
			CH_SIZE_71 ,
			CH_SIZE_72 ,
			CH_SIZE_73 ,
			CH_SIZE_74 ,
			CH_SIZE_75 ,
			CH_SIZE_76 ,
			CH_SIZE_77 ,
			CH_SIZE_78 ,
			CH_SIZE_79 ,
			CH_SIZE_80 ,
			CH_SIZE_81 ,
			CH_SIZE_82 ,
			CH_SIZE_83 ,
			CH_SIZE_84 ,
			CH_SIZE_85 ,
			CH_SIZE_86 ,
			CH_SIZE_87 ,
			CH_SIZE_88 ,
			CH_SIZE_89 ,
			CH_SIZE_90 ,
			CH_SIZE_91 ,
			CH_SIZE_92 ,
			CH_SIZE_93 ,
			CH_SIZE_94 ,
			CH_SIZE_95 ,
			CH_SIZE_96 ,
			CH_SIZE_97 ,
			CH_SIZE_98 ,
			CH_SIZE_99 ,
			CH_SIZE_100,
			CH_SIZE_101,
			CH_SIZE_102,
			CH_SIZE_103,
			CH_SIZE_104,
			CH_SIZE_105,
			CH_SIZE_106,
			CH_SIZE_107,
			CH_SIZE_108,
			CH_SIZE_109,
			CH_SIZE_110,
			CH_SIZE_111,
			CH_SIZE_112,
			CH_SIZE_113,
			CH_SIZE_114,
			CH_SIZE_115,
			CH_SIZE_116,
			CH_SIZE_117,
			CH_SIZE_118,
			CH_SIZE_119,
			CH_SIZE_120,
			CH_SIZE_121,				   			   
			CH_SIZE_122,
			CH_SIZE_123,
			CH_SIZE_124,
			CH_SIZE_125,
			CH_SIZE_126,
			CH_SIZE_127,
			CH_SIZE_128,	
			others=>512 );
begin		   
	
VIRTUAL_BUFFER: VirtBuffer
  generic map( 
      N_CH           => N_CH,
      CH_SIZE        => ch_size_arr,
      DATA_BITS      => 32,
      PROCESSOR_TYPE => PROCESSOR_TYPE 
  )
  port map(
      RST  => RST,  
      CLK  => CLK,

-- Crossbar switch interface (read from the buffer)
      CB_READ_IN   => CB_READ_IN, -- other processors read from the BUFFER
      CB_DATA_OUT  => CB_DATA_OUT,
      CB_EMPTY_OUT => CB_EMPTY_OUT, -- empty signal corresponding to FIFO(CB_FIFO_SEL)

-- Crossbar switch interface (the procesor reads from the crossbar)
      CB_READ_OUT  => CB_READ_OUT,-- read signal (to the crossbar) the processor reads from port
      CB_EMPTY_IN  => CB_EMPTY_IN, -- empty signal (from the crossbar)
      CB_PORT_IN   => CB_PORT_IN, -- port for reading data (the processor reads from the crossbar)

-- Crossbar control interface
      CB_REQ_WR    => CB_REQ_WR,
      CB_REQ_ACKN  => CB_REQ_ACKN,
      CB_REQ_DATA  => CB_REQ_DATA,
      CB_FIFO_SEL  => CB_FIFO_SEL,
	  
-- Processor interface
      P_ADDR      => P_ADDR, 
      P_DATA_IN   => P_DATA_IN,
      P_DATA_OUT  => P_DATA_OUT,

      P_WRITE     => P_WRITE, 	  
      P_READ      => P_READ, 	  
      P_CS        => P_CS	  	
  );
	  
end elaborate;
	

