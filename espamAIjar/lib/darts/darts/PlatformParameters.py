#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Platform parameters'

TIME_IN_CYCLES = True
"""
Periods are computed in clock cycles or OS clock ticks. If True, then
the periods are in clock cycles. Otherwise, in OS clock ticks
"""


T_RD_P2P = 1
"""Time (in cycles) needed to read a single data word (i.e., 32-bits) 
using HW FIFOs"""


T_WR_P2P = 1
"""Time (in cycles) needed to write a single data word (i.e., 32-bits) 
using HW FIFOs"""


T_RD_AXI_CB = 60
"""
Time (in cycles) needed to read a single data word on Xilinx ML605 board 
using AXI crossbar
"""


T_WR_AXI_CB = 30
"""
Time (in cycles) needed to write a single data word on Xilinx ML605 board 
using AXI crossbar
"""


P2P = 0
"""Point-to-Point interconnect using HW FIFOs"""


AXI_CB = 1
"""AXI crossbar interconnect"""


SYS_FREQUENCY = 100000000
"""The frequencey of the system on FPGA"""


SYS_TICK_RATE = 1000
"""OS clock tick rate in Hz"""


CYCLES_IN_TICK = SYS_FREQUENCY // SYS_TICK_RATE
"""Number of clock cycles in a single OS clock tick"""

