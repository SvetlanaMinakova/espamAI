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
-- plxdssm.vhd - PLX local bus direct slave access state machine
--
-- Modules defined:
--
--	 plxdssm           PLX local bus direct slave state machine
--
-- This state machine is designed to respond to direct slave transfers or
-- programmed DMA transfers, NOT demand mode DMA transfers.
--
-- One cycle of address decoding is permitted.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;

entity plxdssm is
    port(
        clk:		in      std_logic;
        rst:		in      std_logic;	-- this should reduce during synthesis to global reset
        sr:             in      std_logic;	-- synchronous reset
        qlads:		in	std_logic;	-- ADS qualified by address, !FHOLD and/or other decoding
        lblast:		in	std_logic;	-- local bus LBLAST signal
        lwrite:		in	std_logic;	-- local bus LWRITE
        ld_oe:		out	std_logic;	-- output enable for local bus data pins
        lreadyi:	out	std_logic;	-- to local bus
        lreadyi_oe:	out	std_logic;	-- to local bus
        lbterm:		out	std_logic;	-- to local bus
        lbterm_oe:	out	std_logic;	-- to local bus
        transfer:	out	std_logic;	-- data transfer on this clock edge
        decode:		out	std_logic;      -- application should do further address decoding
        write:          out     std_logic;      -- LWRITE# latched on LADS#
        ready:          in      std_logic;      -- apply pulse when ready
        stop:		in	std_logic);	-- assert to terminate burst
end plxdssm;

architecture rtl of plxdssm is
    
    type smtype is (s_idle, s_decode, s_wait, s_xfer);
    attribute enum_encoding: string;
    attribute enum_encoding of smtype:type is "0001 0010 0100 1000";
	
    signal state: smtype;
    signal n_state: smtype;
    signal i_transfer: std_logic;
    signal n_transfer: std_logic;
    signal i_lreadyi: std_logic;
    signal n_lreadyi: std_logic;
    signal i_lreadyi_oe: std_logic;
    signal n_lreadyi_oe: std_logic;
    signal i_lbterm: std_logic;
    signal n_lbterm: std_logic;
    signal i_decode: std_logic;
    signal n_decode: std_logic;
    signal i_ld_oe: std_logic;
    signal n_ld_oe: std_logic;
    signal i_write: std_logic;
    signal n_write: std_logic;
    signal stopping: std_logic;
    signal n_stopping: std_logic;
    
begin

    transfer <= i_transfer;
    lreadyi <= i_lreadyi;
    lreadyi_oe <= i_lreadyi_oe;
    lbterm <= i_lbterm;
    lbterm_oe <= i_lreadyi_oe;
    decode <= i_decode;
    ld_oe <= i_ld_oe;
    write <= i_write;

    machinestate : process(clk, rst)
    begin
        if rst = '1' then
            state        <= s_idle;
            i_transfer   <= '0';
            i_lreadyi    <= '0';
            i_lreadyi_oe <= '0';
            i_lbterm     <= '0';
            i_decode     <= '0';
            i_ld_oe      <= '0';
            i_write      <= '0';
            stopping     <= '0';
        elsif clk'event and clk = '1' then
            if sr = '1' then
                state        <= s_idle;
                i_transfer   <= '0';
                i_lreadyi    <= '0';
                i_lreadyi_oe <= '0';
                i_lbterm     <= '0';
                i_decode     <= '0';
                i_ld_oe      <= '0';
                i_write      <= '0';
                stopping     <= '0';
            else
                state        <= n_state;
                i_transfer   <= n_transfer;
                i_lreadyi    <= n_lreadyi;
                i_lreadyi_oe <= n_lreadyi_oe;
                i_lbterm     <= n_lbterm;
                i_decode     <= n_decode;
                i_ld_oe      <= n_ld_oe;
                i_write      <= n_write;
                stopping     <= n_stopping;
            end if;
        end if;
    end process machinestate;
	
    nextstate : process(
        state, 
        lblast, 
        qlads, 
        i_lbterm, 
        lwrite, 
        ready,
        stop,
        stopping,
        i_write)
    begin
        case state is
            when s_idle =>
                n_transfer <= '0';
                n_lreadyi <= '0';
                n_lbterm <= '0';
                n_ld_oe <= '0';
                n_stopping <= '0';
                if qlads = '1' then
                    n_state <= s_decode;
                    n_lreadyi_oe <= '1';
                    n_decode <= '1';
                    n_write <= lwrite;
                else
                    n_state <= s_idle;
                    n_lreadyi_oe <= '0';
                    n_decode <= '0';
                    n_write <= i_write;
                end if;
                
            when s_decode =>
                n_decode <= '0';
                n_ld_oe <= not lwrite;
                n_lreadyi_oe <= '1';
                n_write <= i_write;
                if ready = '1' then
                    n_state <= s_xfer;
                    n_lreadyi <= '1';
                    n_lbterm <= stop;
                    n_transfer <= '1';
                    n_stopping <= '0';
                else
                    n_state <= s_wait;
                    n_lreadyi <= '0';
                    n_lbterm <= '0';
                    n_transfer <= '0';
                    n_stopping <= stop;
                end if;

            when s_wait =>
                n_decode <= '0';
                n_ld_oe <= not lwrite;
                n_lreadyi_oe <= '1';
                n_write <= i_write;
                if ready = '1' then
                    n_state <= s_xfer;
                    n_lreadyi <= '1';
                    n_lbterm <= stop or stopping;
                    n_transfer <= '1';
                    n_stopping <= '0';
                else
                    n_state <= s_wait;
                    n_lreadyi <= '0';
                    n_lbterm <= '0';
                    n_transfer <= '0';
                    n_stopping <= stop or stopping;
                end if;
                
            when s_xfer =>
                n_decode <= '0';
                n_lreadyi_oe <= '1';
                n_stopping <= '0';
                n_write <= i_write;
                if lblast = '1' or i_lbterm = '1' then
                    n_state <= s_idle;
                    n_lreadyi <= '0';
                    n_lbterm <= '0';
                    n_transfer <= '0';
                    n_ld_oe <= '0';
                else
                    n_state <= s_xfer;
                    n_lreadyi <= '1';
                    n_lbterm <= stop;
                    n_transfer <= '1';
                    n_ld_oe <= not lwrite;
                end if;
                
        end case;
    end process nextstate;

end rtl;
