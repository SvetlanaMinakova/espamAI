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
-- zbt_main.vhd - local bus interface giving host access to ZBT SSRAM
--                on an ADM-XRC series card
--
-- (c) Alpha Data Parallel Systems Ltd. 1999-2001
--
-- Example program for ADM-XRC/ADM-XRC-P/ADM-XRCII-L/ADM-XRCII
--
-- This module is the local bus interface to the ZBT SSRAM,
-- parameterisable to allow it to be mapped to the pinout of
-- any ADM-XRC series card.
--
-- The parameters are as follows:
--
--   num_bank     - number of logical banks of SSRAM
--   num_clock    - number of SSRAM clocks to output
--   addr_width   - (maximum) number of address bits to the SSRAM
--   ctl_width    - number of control bits to the SSRAM
--   data_width   - number of data bits per physical SSRAM bank
--   bank_group   - number of physical SSRAM banks to combine in
--                  order to produce a logical SSRAM bank
--
-- When the physical SSRAM banks on the card are less than 32 bits
-- wide, two or more banks (specified by 'bank_group') are combined
-- into a single logical bank. The product of 'bank_group' and
-- 'data_width' must equal 32.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;

entity zbt_main is
    generic(
        num_bank      : natural := 6;
        num_clock     : natural := 2;
        addr_width    : natural := 20;
        ctl_width     : natural := 9;
        data_width    : natural := 32;
        bank_group    : natural := 1);
    port(
        lclk          : in    std_logic;
        mclk          : in    std_logic;
        ramclko       : out   std_logic_vector(num_clock - 1 downto 0);
        ramclki       : in    std_logic_vector(num_clock - 1 downto 0);
        lreseto_l     : in    std_logic; 
        lwrite        : in    std_logic;
        lads_l        : in    std_logic;
        lblast_l      : in    std_logic;
        lbterm_l      : inout std_logic;
        ld            : inout std_logic_vector(31 downto 0);
        la            : in    std_logic_vector(23 downto 2);
        lreadyi_l     : out   std_logic; 
        lbe_l         : in    std_logic_vector(3 downto 0);
        fholda        : in    std_logic;
        global_lbo_l  : out   std_logic;
        flothru_l     : out   std_logic;		
		
        H_Data_R0     : in   std_logic_vector(31 downto 0); -- data from bank 0 (through the buffer) to the host 							
        H_Data_W0     : out  std_logic_vector(31 downto 0); -- data for write to bank 0 (from the host to the mux)							
        H_Tristate_0  : out  std_logic_vector(31 downto 0); -- third state signal to the buffer	(through the mux)
        H_RA0         : out  std_logic_vector(19 downto 0); -- addres from host to memory (through mux)
        H_RC0         : out  std_logic_vector(8 downto 0);  -- control from host to memory (through mux)

        H_Data_R1     : in   std_logic_vector(31 downto 0); 							
        H_Data_W1     : out  std_logic_vector(31 downto 0); 							
        H_Tristate_1  : out  std_logic_vector(31 downto 0);											
        H_RA1         : out  std_logic_vector(19 downto 0); 
        H_RC1         : out  std_logic_vector(8 downto 0); 

        H_Data_R2     : in   std_logic_vector(31 downto 0); 							
        H_Data_W2     : out  std_logic_vector(31 downto 0); 							
        H_Tristate_2  : out  std_logic_vector(31 downto 0);
        H_RA2         : out  std_logic_vector(19 downto 0); 
        H_RC2         : out  std_logic_vector(8 downto 0); 

        H_Data_R3     : in	  std_logic_vector(31 downto 0); 							
        H_Data_W3     : out  std_logic_vector(31 downto 0); 							
        H_Tristate_3  : out  std_logic_vector(31 downto 0);
        H_RA3         : out  std_logic_vector(19 downto 0); 
        H_RC3         : out  std_logic_vector(8 downto 0); 

        H_Data_R4     : in   std_logic_vector(31 downto 0); 							
        H_Data_W4     : out  std_logic_vector(31 downto 0); 							
        H_Tristate_4  : out  std_logic_vector(31 downto 0);
        H_RA4         : out  std_logic_vector(19 downto 0); 
        H_RC4         : out  std_logic_vector(8 downto 0); 

        H_Data_R5     : in   std_logic_vector(31 downto 0); 							
        H_Data_W5     : out  std_logic_vector(31 downto 0); 							
        H_Tristate_5  : out  std_logic_vector(31 downto 0);							   
        H_RA5         : out  std_logic_vector(19 downto 0); 
        H_RC5         : out  std_logic_vector(8 downto 0); 
		
        CLK_out         : out std_logic; -- CLK for the rest of the net
        RST_out         : out std_logic; -- RST for the rest of the net	

        COMMAND_REG     : out   std_logic_vector(31 downto 0);  -- commmand register from the host to our design  
	PARAMETER_REG   : out   std_logic_vector(31 downto 0);
        DESIGN_STAT_REG : in    std_logic_vector(31 downto 0)); -- status register from our design to the host
end zbt_main;

-- COMMAND_REG(comReg_bit_LdParam) - '1' -> a parameter (from PARAMETER_REG) to be loaded into the PN
-- currently comReg_bit_LdParam = 4

architecture mixed of zbt_main is

    function log2(
        x : in natural)
        return natural is
    begin
        for i in 0 to 31 loop
            if x <= 2**i then
                return i;
            end if;
        end loop;
        
        assert false
            report "zbt.vhd: log2() invalid argument"
            severity failure;
        return 0;
    end;

    function min(
        a, b : in integer)
        return integer is
    begin
        if a < b then
            return a;
        else
            return b;
        end if;
    end;

    --
    -- We support up to 8 logical banks of SSRAM.
    --
    constant max_bank         : natural := 8;

    --
    -- We have a 2MB window in local bus space where the SSRAM appears.
    -- This window is augmented with a page register.
    --
    constant page_order       : natural := 19;
    constant page_size        : natural := 2**page_order; -- in longwords

    --
    -- The number of address bits and maximum size (in longwords) of a
    -- logical SSRAM bank.
    --
    constant bank_order       : natural := addr_width;
    constant bank_size        : natural := 2**bank_order; -- in SSRAM words
	
    -- define bit locations in the command register
    constant comReg_bit_RESET   : natural := 0;  -- reset bit 
    constant comReg_bit_InitMEM : natural := 1;  -- initialize memory bit 
    constant comReg_bit_ReadMEM : natural := 2;  -- read memory bit
    constant comReg_bit_EXE     : natural := 3;  -- execute bit
    constant comReg_bit_LdParam : natural := 4;  -- load a parameter into the PN
    constant comReg_bit_bank0   : natural := 26; -- bank0 access bit
    constant comReg_bit_bank1   : natural := 27; -- bank1 access bit
    constant comReg_bit_bank2   : natural := 28; -- bank2 access bit
    constant comReg_bit_bank3   : natural := 29; -- bank3 access bit
    constant comReg_bit_bank4   : natural := 30; -- bank4 access bit
    constant comReg_bit_bank5   : natural := 31; -- bank5 access bit

    --
    -- We terminate bursts at a page boundary or the smallest possible bank
    -- boundary, whichever occurs more frequently.
    --
    constant bterm_bound      : natural := min(128*1024, page_size); -- in longwords
    constant bterm_order      : natural := log2(bterm_bound);
    constant bterm_bound_vec  : std_logic_vector(bterm_order - 1 downto 0) :=
        EXT(CONV_STD_LOGIC_VECTOR(bterm_bound - 1, bterm_order + 1), bterm_order);

    signal rst          : std_logic;
    signal clk          : std_logic;
    signal locked       : std_logic_vector(31 downto 0);

    signal ads_i        : std_logic;
    signal blast_i      : std_logic;
    signal bterm_in     : std_logic;
	signal bterm_i      : std_logic;
    signal qlads        : std_logic;
    signal write_i      : std_logic;
    signal readyi_o     : std_logic;
    signal lreadyi_oe   : std_logic;
    signal bterm_o      : std_logic;
    signal lbterm_oe    : std_logic;
    signal id           : std_logic_vector(31 downto 0);
    signal d_o          : std_logic_vector(31 downto 0);
    signal d_i          : std_logic_vector(31 downto 0);
    signal ld_oe        : std_logic;
    signal a_i          : std_logic_vector(23 downto 2);
    signal be_i         : std_logic_vector(3 downto 0);

    signal ds_xfer      : std_logic;
    signal ds_decode    : std_logic;
    signal ds_ready0    : std_logic;
    signal ds_ready1    : std_logic;
    signal ds_ready2    : std_logic;
    signal ds_ready     : std_logic;
    signal ds_stop0     : std_logic;
    signal ds_stop1     : std_logic;
    signal ds_stop      : std_logic;
    
    signal logic0       : std_logic;
    signal logic1       : std_logic;

    signal a_q          : std_logic_vector(23 downto 2);
    signal write_q      : std_logic;    

    signal oe_page_reg       : std_logic;    
    signal page_reg          : std_logic_vector(7 downto 0);
    --signal oe_pipeline_reg   : std_logic;    
    --signal pipeline_reg      : std_logic_vector(0 downto 0);
    --signal oe_size_reg       : std_logic;    
    --signal size_reg          : std_logic_vector(1 downto 0);
    --signal oe_info_reg       : std_logic;    
    --signal info_reg          : std_logic_vector(31 downto 0);
    signal oe_clk_status_reg : std_logic;    
    signal clk_status_reg    : std_logic_vector(31 downto 0);  

--  Compaan design-flow related registers ----------------------- 
    signal oe_design_stat_reg : std_logic;    				  
    signal sl_design_stat_reg : std_logic_vector(31 downto 0);   
    signal sl_command_reg     : std_logic_vector(31 downto 0);  
	signal oe_command_reg  : std_logic;    				  
	signal sl_PN_param_reg : std_logic_vector(31 downto 0);	  
	signal oe_param_reg    : std_logic;
	signal oe_counter_reg  : std_logic;
	signal sl_counter_reg  : std_logic_vector(31 downto 0);	  
-----------------------------------------------------------------

    signal bank_dec        : std_logic_vector(max_bank - 1 downto 0);
    signal inpage_addr     : std_logic_vector(page_order - 1 downto 0);
    signal inc_inpage_addr : std_logic;
    signal burst_addr      : std_logic_vector(page_order + 8 - 1 downto 0);
    signal aug_addr        : std_logic_vector(page_order + 8 - 1 downto 0);
    signal banksel_addr    : std_logic_vector(2 downto 0);		

    type zbt_ra_type is array(0 to max_bank - 1) of
        std_logic_vector(addr_width * bank_group - 1 downto 0);
    type zbt_rc_type is array(0 to max_bank - 1) of
        std_logic_vector(ctl_width * bank_group - 1 downto 0);
    type zbt_rd_type is array(0 to max_bank - 1) of
        std_logic_vector(data_width * bank_group - 1 downto 0);
    type zbt_t_rd_type is array(0 to max_bank - 1) of
        std_logic_vector(data_width * bank_group - 1 downto 0);
    type zbt_q_type is array(0 to max_bank - 1) of std_logic_vector(31 downto 0);
    signal zbt_read     : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_write    : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_evalid   : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_flushed  : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_q        : zbt_q_type;
    signal oe_zbt_q     : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_oe_ra    : std_logic_vector(max_bank - 1 downto 0);
    signal zbt_ra_o     : zbt_ra_type;
    signal zbt_rc_o     : zbt_rc_type;
    signal zbt_t_rd     : zbt_t_rd_type;
    signal zbt_rd_o     : zbt_rd_type;
    signal zbt_rd_i     : zbt_rd_type;

    signal reading      : std_logic;
    signal writing      : std_logic;

    component clocks
        generic(
            num_clock:    in    natural);
        port(
            rst:          in    std_logic;
            lclk:         in    std_logic;
            mclk:         in    std_logic;
            clk:          out   std_logic;
            ramclko:      out   std_logic_vector(num_clock - 1 downto 0);
            ramclki:      in    std_logic_vector(num_clock - 1 downto 0);
            locked:       out   std_logic_vector(31 downto 0));
    end component;
        
    component plxdssm
        port(
            clk:	in	std_logic;
            rst:	in	std_logic;
            sr:		in	std_logic;
            qlads:	in	std_logic;
            lblast:	in	std_logic;
            lwrite:	in	std_logic;
            ld_oe:	out	std_logic;
            lreadyi:	out	std_logic;
            lreadyi_oe:	out	std_logic;
            lbterm:	out	std_logic;
            lbterm_oe:	out	std_logic;
            transfer:	out	std_logic;
            decode:	out	std_logic;
            ready:      in      std_logic;
            stop:	in	std_logic);
    end component;

    component zbt_port
        generic(
            addr_width : natural;
            data_width : natural;
            ctl_width  : natural;
            bank_group : natural);
        port(
            clk      : in     std_logic;
            rst      : in     std_logic;
            sr       : in     std_logic;
            pipeline : in     std_logic;
            read     : in     std_logic;
            write    : in     std_logic;
            a        : in     std_logic_vector(addr_width - 1 downto 0);
            d        : in     std_logic_vector(31 downto 0);
            q1        : out    std_logic_vector(31 downto 0);
            evalid   : out    std_logic;
            flushed  : out    std_logic;
            oe_ra    : out    std_logic;
            ra_o     : out    std_logic_vector(addr_width * bank_group - 1 downto 0);
            rc_o     : out    std_logic_vector(ctl_width  * bank_group - 1 downto 0);
            t_rd     : out    std_logic_vector(data_width * bank_group - 1 downto 0);
            rd_o     : out    std_logic_vector(data_width * bank_group - 1 downto 0);
            rd_i     : in     std_logic_vector(data_width * bank_group - 1 downto 0));
    end component;

	component IOBUF is
       port (
            I  : in std_logic;
            O  : out std_logic;
	        IO : inout std_logic;
            T  : in std_logic
            );
    end component;

begin

    assert addr_width >= 17 and addr_width <= 20
        report "*** zbt_main: generic 'addr_width' is invalid"
        severity failure;
    
    --
    --	define constant values
    --
    logic0 <= '0';
    logic1 <= '1';

    --
    --	Convert the inputs to active high.
    --	
    rst     <= not lreseto_l or sl_command_reg(comReg_bit_RESET);
    ads_i   <= not lads_l;
    blast_i <= not lblast_l;
    -- bterm_i <= not lbterm_l;
    write_i <= lwrite;
    a_i     <= la;
    -- d_i     <= ld;
    be_i    <= not lbe_l;					 

	 --
	 -- Data from the memory banks
	 --
     zbt_rd_i(0)  <= H_Data_R0;   
     zbt_rd_i(1)  <= H_Data_R1;  
     zbt_rd_i(2)  <= H_Data_R2;  
     zbt_rd_i(3)  <= H_Data_R3;
     zbt_rd_i(4)  <= H_Data_R4;  
     zbt_rd_i(5)  <= H_Data_R5;  

    --
    -- Generate a qualified address strobe, ie. 'lads_l' qualified by address
    -- decoding for the FPGA space.
    --
    qlads <= ads_i and not a_i(23) and not fholda;

    --
    -- 'bterm_l' should only be driven when the FPGA is addressed, since
    -- the control logic on the XRC might also drive it.
    --
    -- lbterm_l <= not bterm_o when lbterm_oe = '1' else 'Z'; 
	l3: IOBUF port map ( I => not bterm_o, IO => lbterm_l, T => not lbterm_oe, O => bterm_in );
	bterm_i <= not bterm_in;
	
    --
    -- 'lreadyi_l' should only be driven when the FPGA is addressed, since
    -- the control logic in the ADM-XRC might also drive it.
    --
    lreadyi_l <= not readyi_o when lreadyi_oe = '1' else 'Z';

    --
    -- Keep track of address within current page, so we can terminate
    -- bursts at the correct boundaries.
    --	
    inc_inpage_addr <= (reading and OR_reduce(bank_dec and zbt_flushed)) or
                       (ds_xfer and writing);
    gen_inpage_addr : process(rst, clk)
    begin
        if rst = '1' then
            inpage_addr <= (others => '0');
        elsif clk'event and clk = '1' then
            if ds_decode = '1' and a_q(22 downto 21) = "01" then
                inpage_addr <= a_q(page_order + 1 downto 2);
            else
                if inc_inpage_addr = '1' then
                    inpage_addr <= inpage_addr + 1;
                end if;
            end if;
        end if;
    end process;

    --
    -- Latch local bus address and write signals at beginning of burst
    --
    -- "a_q" mirrors "la" during the burst.
    --
    latch_on_ads : process(rst, clk)
    begin
        if rst = '1' then
            a_q <= (others => '0');
            write_q <= '0';
        elsif clk'event and clk = '1' then
            if ads_i = '1' then
                a_q <= a_i;
                write_q <= write_i;
            else
                if ds_xfer = '1' then
                    a_q <= a_q + 1;
                end if;
            end if;
        end if;
    end process;

    --
    -- Pick out the address bits that identify the bank being accessed
    --		  
	
    aug_addr <= page_reg & a_q(page_order + 1 downto 2);
	gen_banksel_addr: process(aug_addr)
	begin
		banksel_addr <= aug_addr(20 downto 18); -- SSRAMs are 256k by x
	end process;
	
--	gen_banksel_addr: process(
--        aug_addr,
--        size_reg)
--    begin
--        case size_reg is
--            when "00" =>
--                banksel_addr <= aug_addr(19 downto 17); -- SSRAMs are 128k by x
--            when "01" =>
--                banksel_addr <= aug_addr(20 downto 18); -- SSRAMs are 256k by x
--            when "10" =>
--                banksel_addr <= aug_addr(21 downto 19); -- SSRAMs are 512k by x
--            when "11" =>
--                banksel_addr <= aug_addr(22 downto 20); -- SSRAMs are 1M by x
--            when others =>
--                banksel_addr <= (others => '-');
--        end case;
--    end process;
         
    --
    -- Generate output enable signals for registers
    --
    gen_oe_regs : process(rst, clk)
    begin
        if rst = '1' then
            oe_zbt_q <= (others => '0');
            oe_page_reg <= '0';
--            oe_size_reg <= '0';
--            oe_pipeline_reg <= '0';
--            oe_info_reg <= '0';
            oe_clk_status_reg <= '0';
            oe_design_stat_reg <= '0';
	    oe_command_reg <= '0';
	    oe_param_reg <= '0';
	    oe_counter_reg <= '0';
        elsif clk'event and clk = '1' then
            if ds_decode = '1' then
                for i in 0 to max_bank - 1 loop
                    if a_q(22 downto 21) = "01" and write_q = '0' and banksel_addr = i then
                        oe_zbt_q(i) <= '1';
                    else
                        oe_zbt_q(i) <= '0';
                    end if;
                end loop;
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "000" then
		    		oe_page_reg <= '1';
                end if;
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "001" then
		    		oe_clk_status_reg <= '1';
                end if;
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "010" then
		    		oe_design_stat_reg <= '1';
                end if;
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "011" then
		    		oe_command_reg <= '1';
                end if;	   
---------------------------------------------------------------------------------------------------				
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "100" then
		    		oe_param_reg <= '1';
                end if; 
                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "101" then
		    		oe_counter_reg <= '1';
                end if; 
---------------------------------------------------------------------------------------------------

--                if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "101" then
--		    		oe_clk_status_reg <= '1';
--                end if;	
--				if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "110" then
--		    		oe_design_stat_reg <= '1';
--                end if;
--            	if a_q(22 downto 21) = "00" and write_q = '0' and a_q(4 downto 2) = "111" then
--		    		oe_command_reg <= '1';
--                end if;


	         else
                if ds_xfer = '1' and (blast_i = '1' or bterm_i = '1') then
                    oe_zbt_q <= (others => '0');
                    oe_page_reg <= '0';
                    oe_clk_status_reg <= '0';
                    oe_design_stat_reg <= '0';
	            oe_command_reg <= '0';
		    oe_param_reg <= '0';
                    oe_counter_reg <= '0';
                end if;
            end if;
        end if;
    end process;

    --
    -- Generate the one-hot 'bank_dec' vector, which indicates whether
    -- or not a particular bank is being accessed.
    --
    gen_bank_dec : process(rst, clk)
    begin
        if rst = '1' then

            bank_dec <= (others => '0');
            reading <= '0';
            writing <= '0';

        elsif clk'event and clk = '1' then

            if ds_decode = '1' then
                for i in 0 to max_bank - 1 loop
                    if a_q(22 downto 21) = "01" and banksel_addr = i then
                        bank_dec(i) <= '1';
                    else
                        bank_dec(i) <= '0';
                    end if;
                end loop;
            end if;

            if ds_xfer = '1' and (blast_i = '1' or bterm_i = '1') then
                reading <= '0';
                writing <= '0';
            else
                if ds_decode = '1' and write_q = '0' and a_q(22 downto 21) = "01" then
                    reading <= '1';
                end if;
                if ds_decode = '1' and write_q = '1' and a_q(22 downto 21) = "01" then
                    writing <= '1';
                end if;
            end if;

        end if;
    end process;

    --
    -- Generate the one-hot 'zbt_write' and 'zbt_read' vectors to
    -- the zbt_port modules
    --
    gen_zbt_write1: for i in 0 to max_bank - 1 generate
        zbt_write(i) <= ds_xfer and write_q and bank_dec(i);
        zbt_read(i)  <= reading and bank_dec(i) and zbt_flushed(i);
    end generate;

    --
    -- Drive the internal data bus on reads.
    --						
    gen_id: for i in 0 to 5 generate
      id <= zbt_q(i) when oe_zbt_q(i) = '1'  else (others => 'Z');
    end generate;
--    id <= EXT(pipeline_reg, 32) when oe_pipeline_reg    = '1' else (others => 'Z');
    id <= EXT(page_reg, 32)     when oe_page_reg        = '1' else (others => 'Z');
--    id <= EXT(size_reg, 32)     when oe_size_reg        = '1' else (others => 'Z');
--    id <= info_reg              when oe_info_reg        = '1' else (others => 'Z');
    id <= clk_status_reg        when oe_clk_status_reg  = '1' else (others => 'Z');
    id <= sl_design_stat_reg    when oe_design_stat_reg = '1' else (others => 'Z');
	id <= sl_command_reg when oe_command_reg = '1' else (others => 'Z');	   
-------------------------------------------------------------------------------		
	id <= sl_PN_param_reg when oe_param_reg = '1' else (others => 'Z');
	id <= sl_counter_reg when oe_counter_reg = '1' else (others => 'Z');
-------------------------------------------------------------------------------
    --
    -- Generate the local bus output data
    --
    gen_d_o: process(rst, clk)
    begin
        if rst = '1' then
            d_o <= (others => '0');
        elsif clk'event and clk = '1' then
            d_o <= id;
        end if;
    end process;

    --
    -- Drive the local data bus on reads
    --
    -- ld <= d_o when ld_oe = '1' else (others => 'Z');
	l1:for i in 0 to 31 generate
    begin
	   l2: IOBUF port map ( I => d_o(i), IO => ld(i), T => not ld_oe, O => d_i(i) ); 
    end generate;

    --
    -- Implement the read only 'info_reg' register
    --
--    info_reg   <= CONV_STD_LOGIC_VECTOR(num_bank, 8) & CONV_STD_LOGIC_VECTOR(bank_size, 24);

    --
    -- Implement the read only 'clk_status_reg' register
    --		  
    gen_clk_status_reg: process(rst, clk)
    begin
        if rst = '1' then
            clk_status_reg <= (others => '0');
        elsif clk'event and clk = '1' then
            clk_status_reg <= locked;
        end if;
    end process;

    
    --
    -- Implement the read only 'design_status_reg' register
    --		  
    pr_gen_design_stat_reg: process(rst, clk)
    begin
        if rst = '1' then
            sl_design_stat_reg <= (others => '0');
        elsif clk'event and clk = '1' then
            sl_design_stat_reg <= DESIGN_STAT_REG;
        end if;
    end process;
	
    --
    -- Implement the 'page', 'pipeline' and 'size' registers and the command register
    --			   
	
--	size_reg <= "01";
	
    registers : process(rst, clk)
    begin
        if rst = '1' then
            page_reg <= (others => '0');
--            pipeline_reg <= (others => '0');
--            size_reg <= (others => '1'); 
            sl_command_reg <= (others => '0');	   
	    sl_PN_param_reg <= (others => '0');
	    sl_counter_reg <= (others => '0');
        elsif clk'event and clk = '1' then

   	-- CLK cycles counter
	    if( sl_design_stat_reg(0) = '0' ) then -- the system is running
	       sl_counter_reg <= sl_counter_reg + 1;
	    end if;

            if ds_xfer = '1' and write_q = '1' then
                if a_q(22 downto 21) = "00" and a_q(4 downto 2) = "000" then
                    if be_i(0) = '1' then
                        page_reg <= d_i(7 downto 0);
                    end if;
                end if;

				if a_q(22 downto 21) = "00" and a_q(4 downto 2) = "011" then
                    if be_i(0) = '1' then
                        sl_command_reg(7 downto 0) <= d_i(7 downto 0);
                    end if;	
					if be_i(1) = '1' then
                        sl_command_reg(15 downto 8) <= d_i(15 downto 8);
                    end if;
					if be_i(2) = '1' then
                        sl_command_reg(23 downto 16) <= d_i(23 downto 16);
                    end if;
					if be_i(3) = '1' then
                        sl_command_reg(31 downto 24) <= d_i(31 downto 24);
                    end if;
                end if;
-- Write the parameters to be set in the network ------------------------------
				if a_q(22 downto 21) = "00" and a_q(4 downto 2) = "100" then
                    if be_i(0) = '1' then
                        sl_PN_param_reg(7 downto 0) <= d_i(7 downto 0);
                    end if;	
					if be_i(1) = '1' then
                        sl_PN_param_reg(15 downto 8) <= d_i(15 downto 8);
                    end if;
					if be_i(2) = '1' then
                        sl_PN_param_reg(23 downto 16) <= d_i(23 downto 16);
                    end if;
					if be_i(3) = '1' then
                        sl_PN_param_reg(31 downto 24) <= d_i(31 downto 24);
                    end if;
			    end if;
-- Write a value to the counter register (used to clear the register) ---------------------
		if a_q(22 downto 21) = "00" and a_q(4 downto 2) = "101" then
                    if be_i(0) = '1' then
                        sl_counter_reg(7 downto 0) <= d_i(7 downto 0);
                    end if;	
		    if be_i(1) = '1' then
                        sl_counter_reg(15 downto 8) <= d_i(15 downto 8);
                    end if;
		    if be_i(2) = '1' then
                        sl_counter_reg(23 downto 16) <= d_i(23 downto 16);
                    end if;
		    if be_i(3) = '1' then
                        sl_counter_reg(31 downto 24) <= d_i(31 downto 24);
                    end if;
	        end if;
------------------------------------------------------------------------------
            end if;

        end if;
    end process;

    --
    -- Generate the 'ds_ready' signal to the PLX direct slave state machine.
    --
    -- For writes or reads to the registers, this is simply 'ds_decode'.
    -- For reads to the registers, it is generated from the 'zbt_evalid' vector
    -- from the 'zbt_port' modules.
    --
    gen_ds_ready0: process(rst, clk)
    begin
        if rst = '1' then
            ds_ready0 <= '0';
            ds_ready1 <= '0';
        elsif clk'event and clk = '1' then
            ds_ready0 <= OR_reduce(zbt_evalid and bank_dec);
            ds_ready1 <= ds_decode and not write_q and not a_q(21);
        end if;
    end process;
            
    ds_ready2 <= ds_decode and write_q;

    ds_ready <= (ds_ready0 and reading) or ds_ready1 or ds_ready2;

    --
    -- Terminate bursts at page or bank boundaries (whichever is more
    -- frequent).
    --
    generate_ds_stop0 : process(a_q, ds_decode)
    begin
        if  ds_decode = '1' and
            (a_q(21) = '0' or (a_q(21) = '1' and a_q(bterm_order + 1 downto 2) = bterm_bound_vec))
        then
            ds_stop0 <= '1';
        else
            ds_stop0 <= '0';
        end if;
    end process;

    generate_ds_stop1: process(
        burst_addr,
        write_q,
        a_q,
        ds_xfer)
    begin
        if  (ds_xfer = '1' and write_q = '1' and burst_addr(bterm_order - 1 downto 0) = (bterm_bound_vec - 1)) or
            (ds_xfer = '1' and write_q = '0' and a_q(bterm_order + 1 downto 2) = (bterm_bound_vec - 1))
        then
            ds_stop1 <= '1';
        else
            ds_stop1 <= '0';
        end if;
    end process;

    ds_stop <= ds_stop0 or ds_stop1;

    --
    -- Instantiate the state machine for responding to direct slave transfers
    --
    plxdssm0: plxdssm
        port map(
            clk        => clk,
            rst        => rst,
            sr         => logic0,
            qlads      => qlads,
            lblast     => blast_i,
            lwrite     => write_i,
            ld_oe      => ld_oe,
            lreadyi    => readyi_o,
            lreadyi_oe => lreadyi_oe,
            lbterm     => bterm_o,
            lbterm_oe  => lbterm_oe,
            transfer   => ds_xfer,
            decode     => ds_decode,
            ready      => ds_ready,
            stop       => ds_stop);
		
    --
    -- Instantiate the ZBT ports (bank interfaces)
    --
    burst_addr <= page_reg & inpage_addr;
    generate_zbt_ports: for i in 0 to num_bank - 1 generate
        zbt_port0: zbt_port
            generic map(
                addr_width => addr_width,
                data_width => data_width,
                ctl_width  => ctl_width,
                bank_group => bank_group)
            port map(
                clk      => clk,
                rst      => rst,
                sr       => logic0,
                pipeline => logic1,--pipeline_reg(0),
                read     => zbt_read(i),
                write    => zbt_write(i),
                a        => burst_addr(bank_order - 1 downto 0),
                d        => d_i,
                q1        => zbt_q(i),
                evalid   => zbt_evalid(i),
                flushed  => zbt_flushed(i),
                oe_ra    => zbt_oe_ra(i),
                ra_o     => zbt_ra_o(i),
                rc_o     => zbt_rc_o(i),
                t_rd     => zbt_t_rd(i),
                rd_o     => zbt_rd_o(i),
                rd_i     => zbt_rd_i(i));
    end generate;

    --
    -- For banks not present, generate dummy signals.
    --
    generate_zbt_dummy: for i in num_bank to max_bank - 1 generate
        zbt_oe_ra(i)   <= '0';
        zbt_ra_o(i)    <= (others => '0');
        zbt_rc_o(i)    <= (others => '1');
        zbt_t_rd(i)    <= (others => '1');
        zbt_rd_o(i)    <= (others => '0');
        zbt_evalid(i)  <= ds_decode;
		  zbt_flushed(i) <= '1';
    end generate;

    --
    -- Instantiate the clocks generator module
    --
    clocks0: clocks
        generic map(
            num_clock => num_clock)
        port map(
            rst       => rst,
            lclk      => lclk,
            mclk      => mclk,
            clk       => clk,
            ramclko   => ramclko,
            ramclki   => ramclki,
            locked    => locked);
				

    global_lbo_l <= '0';
    flothru_l    <= '1';--pipeline_reg(0);

    -- global clock for the design 
    CLK_out <= clk;		  
		
    -- global reset for the design
    RST_out <= rst;	-- not lreseto_l or sl_command_reg(comReg_bit_RESET)
	
    -- command reg to the mux and design
    COMMAND_REG <= sl_command_reg; 

    -- param reg to PN
    PARAMETER_REG <= sl_PN_param_reg; 

    -- signals for bank 0
    H_Data_W0    <= zbt_rd_o(0);
    H_Tristate_0 <= zbt_t_rd(0);
    H_RA0        <= zbt_ra_o(0);
    H_RC0        <= zbt_rc_o(0); 
    -- signals for bank 1 
    H_Data_W1    <= zbt_rd_o(1);
    H_Tristate_1 <= zbt_t_rd(1);
    H_RA1        <= zbt_ra_o(1);
    H_RC1        <= zbt_rc_o(1);  
    -- signals for bank 2
    H_Data_W2    <= zbt_rd_o(2);
    H_Tristate_2 <= zbt_t_rd(2);
    H_RA2        <= zbt_ra_o(2);
    H_RC2        <= zbt_rc_o(2);  
    -- signals for bank 3
    H_Data_W3    <= zbt_rd_o(3);
    H_Tristate_3 <= zbt_t_rd(3);
    H_RA3        <= zbt_ra_o(3);
    H_RC3        <= zbt_rc_o(3); 
    -- signals for bank 4
    H_Data_W4    <= zbt_rd_o(4);
    H_Tristate_4 <= zbt_t_rd(4);
    H_RA4        <= zbt_ra_o(4);
    H_RC4        <= zbt_rc_o(4); 
    -- signals for bank 5 
    H_Data_W5    <= zbt_rd_o(5);
    H_Tristate_5 <= zbt_t_rd(5);
    H_RA5        <= zbt_ra_o(5);
    H_RC5        <= zbt_rc_o(5);
            
end mixed;
