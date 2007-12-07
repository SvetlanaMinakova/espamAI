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
-- zbt_port.vhd - ZBT interface and IO pins
--
-- (c) Alpha Data Parallel Systems Ltd. 1999-2001
--
-- Example program for ADM-XRC/ADM-XRC-P/ADM-XRCII-L/ADM-XRCII
--
-- Module containing pins connected to a bank of ZBT SSRAM.
--
-- The 'pipeline' input selects flowthrough ZBT SSRAM mode ('0') or
-- pipelined ZBT SSRAM mode ('1').
-- 
-- The latency from 'wr' and 'd' to 'rd' valid is 3 clocks, regardless
-- whether pipelined or flowthrough mode is selected.
--
-- The latency from 'rd' to 'q' valid is 4 clocks, regardless of whether
-- pipelined or flowthrough mode is selected.
--
-- The latency from the 'rd', 'wr', 'a' signals to 'ra' and 'rc' valid
-- is 1 clock in pipelined mode and 2 clocks in flowthrough mode.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;

-- synopsys translate_off
--library unisim;
--use unisim.vcomponents.all;
-- synopsys translate_on

entity zbt_port is
    generic(
        addr_width : natural;   -- Number of address bits on SSRAM bank
        data_width : natural;   -- Number of data bits on SSRAM bank
        ctl_width  : natural;   -- Number of control bits on SSRAM bank
        bank_group : natural);  -- Number of SSRAM banks to combine to make 32 bit
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
        rc_o     : out    std_logic_vector(ctl_width * bank_group - 1 downto 0);
        t_rd     : out    std_logic_vector(data_width * bank_group - 1 downto 0);
        rd_o     : out    std_logic_vector(data_width * bank_group - 1 downto 0);
        rd_i     : in     std_logic_vector(data_width * bank_group - 1 downto 0));
end zbt_port;

architecture mixed of zbt_port is

    constant be_width    : natural := data_width / 8;

    type state_type1 is (
        s1_idle,
        s1_read0, s1_read1,
        s1_write0, s1_write1, s1_write2);
    signal state1, n_state1 : state_type1;

    signal a_q     : std_logic_vector(addr_width - 1 downto 0);
    signal read_q  : std_logic;
    signal write_q : std_logic;

    type d_q_type is array(2 downto 0) of std_logic_vector(31 downto 0);--data_width * bank_group - 1 downto 0);
    signal d_q     : d_q_type;
    signal oe_rd   : std_logic_vector(2 downto 0);
    signal ce_q    : std_logic_vector(2 downto 0);

    signal n_evalid  : std_logic;
    signal n_flushed : std_logic;	 
	signal dummy: std_logic_vector(31 downto 0);

begin

    assert (data_width * bank_group) = 32
        report "*** zbt_port: generic 'bank_group' or 'data_width' is invalid"
        severity failure;
    assert ctl_width = 6 or ctl_width = 9
        report "*** zbt_port: generic 'ctl_width' is invalid"
        severity failure;

    oe_ra <= '1';
    rd_o <= d_q(2);    

    --
    -- Drive 'rd' (SSRAM data bus) on writes
    --
    data_out: process(rst, clk)
    begin
        if rst = '1' then
            d_q <= (others => (others => '0'));
            oe_rd <= (others => '0');
            t_rd <= (others => '1');
        elsif clk'event and clk = '1' then
            if sr = '1' then
                d_q <= (others => (others => '0'));
                oe_rd <= (others => '0');
                t_rd <= (others => '1');
            else
               d_q(2 downto 1) <= d_q(1 downto 0);
	       d_q(0) <= d;
	       --d_q <= d_q(1 downto 0);--&d;
                oe_rd <= oe_rd(1 downto 0) & write;
                t_rd <= (others => not oe_rd(1));
            end if;
        end if;
    end process;

    --
    -- Generate 'q' (read SSRAM data bus on reads)
    --
    data_in: process(rst, clk)
    begin
        if rst = '1' then
            ce_q <= (others => '0');
            q1 <= (others => '0');	 
			dummy <= (others => '0');	 
        elsif clk'event and clk = '1' then
            if sr = '1' then
                ce_q <= (others => '0');
                q1 <= (others => '0');
            else
                ce_q <= ce_q(1 downto 0) & read;
                if ce_q(2) = '1' then
                    q1 <= rd_i;
					dummy <= rd_i;	   
                end if;
            end if;
        end if;
    end process;

    --
    -- Drive 'ra' (SSRAM address bus) and 'rc' (SSRAM control bus)
    --
    control : process(clk, rst)
        variable addr_out: std_logic_vector(addr_width - 1 downto 0);
        variable ctl_out:  std_logic_vector(ctl_width - 1 downto 0);
    begin
        if rst = '1' then
            a_q <= (others => '0');
            read_q <= '0';
            write_q <= '0';
            ra_o <= (others => '0');
            rc_o <= (others => '1');
        elsif clk'event and clk = '1' then
            if sr = '1' then
                a_q <= (others => '0');
                read_q <= '0';
                write_q <= '0';
                ra_o <= (others => '0');
                rc_o <= (others => '1');
            else
                a_q <= a;
                read_q <= read;
                write_q <= write;

                if pipeline = '0' then
                    addr_out := a_q;
                else
                    addr_out := a;
                end if;

                if pipeline = '0' then
                    if ctl_width = 6 then
                        ctl_out(5 downto 4) := "00";
                        ctl_out(3)          := not (read_q or write_q);
                        ctl_out(2)          := not write_q;
                        ctl_out(1 downto 0) := "00";
                    else
                        ctl_out(8 downto 6) := "000";
                        ctl_out(5)          := not (read_q or write_q);
                        ctl_out(4)          := not write_q;
                        ctl_out(3 downto 0) := "0000";
                    end if;
                else
                    if ctl_width = 6 then
                        ctl_out(5 downto 4) := "00";
                        ctl_out(3)          := not (read or write);
                        ctl_out(2)          := not write;
                        ctl_out(1 downto 0) := "00";
                    else
                        ctl_out(8 downto 6) := "000";
                        ctl_out(5)          := not (read or write);
                        ctl_out(4)          := not write;
                        ctl_out(3 downto 0) := "0000";
                    end if;
                end if;

                for i in 0 to bank_group - 1 loop
                    ra_o(addr_width * (i + 1) - 1 downto addr_width * i) <= addr_out;
                    rc_o(ctl_width * (i + 1) - 1 downto ctl_width * i)   <= ctl_out;
                end loop;

            end if;
        end if;
    end process;

    transition1: process(rst, clk)
    begin
        if rst = '1' then
            state1 <= s1_idle;
            evalid <= '0';
            flushed <= '1';
        elsif clk'event and clk = '1' then
            if sr = '1' then
                state1 <= s1_idle;
                evalid <= '0';
                flushed <= '1';
            else
                state1 <= n_state1;
                evalid <= n_evalid;
                flushed <= n_flushed;
            end if;
        end if;
    end process;

    nextstate1: process(
        state1,
        read,
        write)
    begin
        case state1 is
            when s1_idle =>
                if read = '1' then
                    n_state1 <= s1_read0;
                    n_flushed <= '1';
                else
                    if write = '1' then
                        n_state1 <= s1_write0;
                        n_flushed <= '0';
                    else
                        n_state1 <= s1_idle;
                        n_flushed <= '1';
                    end if;
                end if;
                n_evalid <= '0';

            when s1_read0 =>
                if read = '1' then
                    n_state1 <= s1_read1;
                else
                    n_state1 <= s1_idle;
                end if;
                n_evalid <= '0';
                n_flushed <= '1';

            when s1_read1 =>
                if read = '1' then
                    n_state1 <= s1_read1;
                    n_evalid <= '1';
                else
                    n_state1 <= s1_idle;
                    n_evalid <= '0';
                end if;
                n_flushed <= '1';

            when s1_write0 =>
                if write = '1' then
                    n_state1 <= s1_write0;
                else
                    n_state1 <= s1_write1;
                end if;
                n_evalid <= '0';
                n_flushed <= '0';

            when s1_write1 =>
                if write = '1' then
                    n_state1 <= s1_write0;
                else
                    n_state1 <= s1_write2;
                end if;
                n_evalid <= '0';
                n_flushed <= '0';
                
            when s1_write2 =>
                if write = '1' then
                    n_state1 <= s1_write0;
                    n_flushed <= '0';
                else
                    n_state1 <= s1_idle;
                    n_flushed <= '1';
                end if;
                n_evalid <= '0';
                
        end case;
    end process;

end mixed;
