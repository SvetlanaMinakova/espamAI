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
            report "VB_Wrapper.vhd: log2() invalid argument"
            severity failure;
        return 0;
    end;
-------------------------------------------------------------------------------
-- Component declarations
-------------------------------------------------------------------------------
  component VirtBuffer
  generic ( 								  
      N_CH : natural:=3; 	
      CH_SIZE : t_ch_size := ( 0=>512, 1=>1024, 2=>512, others=>512 );
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
			fn_log2( CH_SIZE_1   ),
			fn_log2( CH_SIZE_2   ),
			fn_log2( CH_SIZE_3   ),
			fn_log2( CH_SIZE_4   ),
			fn_log2( CH_SIZE_5   ),
			fn_log2( CH_SIZE_6   ),
			fn_log2( CH_SIZE_7   ),
			fn_log2( CH_SIZE_8   ),
			fn_log2( CH_SIZE_9   ),
			fn_log2( CH_SIZE_10  ),
			fn_log2( CH_SIZE_11  ),
			fn_log2( CH_SIZE_12  ),
			fn_log2( CH_SIZE_13  ),
			fn_log2( CH_SIZE_14  ),
			fn_log2( CH_SIZE_15  ),
			fn_log2( CH_SIZE_16  ),
			fn_log2( CH_SIZE_17  ),
			fn_log2( CH_SIZE_18  ),
			fn_log2( CH_SIZE_19  ),
			fn_log2( CH_SIZE_20  ),
			fn_log2( CH_SIZE_21  ),
			fn_log2( CH_SIZE_22  ),
			fn_log2( CH_SIZE_23  ),
			fn_log2( CH_SIZE_24  ),
			fn_log2( CH_SIZE_25  ),
			fn_log2( CH_SIZE_26  ),
			fn_log2( CH_SIZE_27  ),
			fn_log2( CH_SIZE_28  ),
			fn_log2( CH_SIZE_29  ),
			fn_log2( CH_SIZE_30  ),
			fn_log2( CH_SIZE_31  ),
			fn_log2( CH_SIZE_32  ),
			fn_log2( CH_SIZE_33  ),
			fn_log2( CH_SIZE_34  ),
			fn_log2( CH_SIZE_35  ),
			fn_log2( CH_SIZE_36  ),
			fn_log2( CH_SIZE_37  ),
			fn_log2( CH_SIZE_38  ),
			fn_log2( CH_SIZE_39  ),
			fn_log2( CH_SIZE_40  ),
			fn_log2( CH_SIZE_41  ),
			fn_log2( CH_SIZE_42  ),
			fn_log2( CH_SIZE_43  ),
			fn_log2( CH_SIZE_44  ),
			fn_log2( CH_SIZE_45  ),
			fn_log2( CH_SIZE_46  ),
			fn_log2( CH_SIZE_47  ),
			fn_log2( CH_SIZE_48  ),
			fn_log2( CH_SIZE_49  ),
			fn_log2( CH_SIZE_50  ),
			fn_log2( CH_SIZE_51  ),
			fn_log2( CH_SIZE_52  ),
			fn_log2( CH_SIZE_53  ),
			fn_log2( CH_SIZE_54  ),
			fn_log2( CH_SIZE_55  ),
			fn_log2( CH_SIZE_56  ),
			fn_log2( CH_SIZE_57  ),
			fn_log2( CH_SIZE_58  ),
			fn_log2( CH_SIZE_59  ),
			fn_log2( CH_SIZE_60  ),
			fn_log2( CH_SIZE_61  ),
			fn_log2( CH_SIZE_62  ),
			fn_log2( CH_SIZE_63  ),
			fn_log2( CH_SIZE_64  ),
			fn_log2( CH_SIZE_65  ),
			fn_log2( CH_SIZE_66  ),
			fn_log2( CH_SIZE_67  ),
			fn_log2( CH_SIZE_68  ),
			fn_log2( CH_SIZE_69  ),
			fn_log2( CH_SIZE_70  ),
			fn_log2( CH_SIZE_71  ),
			fn_log2( CH_SIZE_72  ),
			fn_log2( CH_SIZE_73  ),
			fn_log2( CH_SIZE_74  ),
			fn_log2( CH_SIZE_75  ),
			fn_log2( CH_SIZE_76  ),
			fn_log2( CH_SIZE_77  ),
			fn_log2( CH_SIZE_78  ),
			fn_log2( CH_SIZE_79  ),
			fn_log2( CH_SIZE_80  ),
			fn_log2( CH_SIZE_81  ),
			fn_log2( CH_SIZE_82  ),
			fn_log2( CH_SIZE_83  ),
			fn_log2( CH_SIZE_84  ),
			fn_log2( CH_SIZE_85  ),
			fn_log2( CH_SIZE_86  ),
			fn_log2( CH_SIZE_87  ),
			fn_log2( CH_SIZE_88  ),
			fn_log2( CH_SIZE_89  ),
			fn_log2( CH_SIZE_90  ),
			fn_log2( CH_SIZE_91  ),
			fn_log2( CH_SIZE_92  ),
			fn_log2( CH_SIZE_93  ),
			fn_log2( CH_SIZE_94  ),
			fn_log2( CH_SIZE_95  ),
			fn_log2( CH_SIZE_96  ),
			fn_log2( CH_SIZE_97  ),
			fn_log2( CH_SIZE_98  ),
			fn_log2( CH_SIZE_99  ),
			fn_log2( CH_SIZE_100 ),
			fn_log2( CH_SIZE_101 ),
			fn_log2( CH_SIZE_102 ),
			fn_log2( CH_SIZE_103 ),
			fn_log2( CH_SIZE_104 ),
			fn_log2( CH_SIZE_105 ),
			fn_log2( CH_SIZE_106 ),
			fn_log2( CH_SIZE_107 ),
			fn_log2( CH_SIZE_108 ),
			fn_log2( CH_SIZE_109 ),
			fn_log2( CH_SIZE_110 ),
			fn_log2( CH_SIZE_111 ),
			fn_log2( CH_SIZE_112 ),
			fn_log2( CH_SIZE_113 ),
			fn_log2( CH_SIZE_114 ),
			fn_log2( CH_SIZE_115 ),
			fn_log2( CH_SIZE_116 ),
			fn_log2( CH_SIZE_117 ),
			fn_log2( CH_SIZE_118 ),
			fn_log2( CH_SIZE_119 ),
			fn_log2( CH_SIZE_120 ),
			fn_log2( CH_SIZE_121 ),				   			   
			fn_log2( CH_SIZE_122 ),
			fn_log2( CH_SIZE_123 ),
			fn_log2( CH_SIZE_124 ),
			fn_log2( CH_SIZE_125 ),
			fn_log2( CH_SIZE_126 ),
			fn_log2( CH_SIZE_127 ),
			fn_log2( CH_SIZE_128 ),	
			others=>9 );
begin		   
	
VIRTUAL_BUFFER: VirtBuffer
  generic map( 
      N_CH           => N_CH,
      CH_SIZE        => ch_size_arr,
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
	

