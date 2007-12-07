###############################################################################
##
## Copyright (c) 2003 Xilinx, Inc. All Rights Reserved.
## DO NOT COPY OR MODIFY THIS FILE. 
## THE CONTENTS AND METHODOLOGY MAY CHANGE IN FUTURE RELEASES. 
##
## plb_core_ssp0_ref.tcl
##
###############################################################################

#***--------------------------------***------------------------------------***
#
#			     IPLEVEL_DRC_PROC
#
#***--------------------------------***------------------------------------***

#
# 1. check C_MIR_BASEADDR is not equal to the default MPD value of 0xFFFF_FFFF
#    C_MIR_BASEADDR != 0xFFFF_FFFF
#
# 2. check C_MIR_HIGHADDR is not equal to the default MPD value of 0x0000_0000
#    C_MIR_HIGHADDR != 0x0000_0000
#

proc check_iplevel_settings {mhsinst} {

    set base_param "C_MIR_BASEADDR"
    set high_param "C_MIR_HIGHADDR"
    set base_addr [xget_value $mhsinst "parameter" $base_param]
    set high_addr [xget_value $mhsinst "parameter" $high_param]

    if {[compare_unsigned_addr_strings $base_addr $base_param $high_addr $high_param] == 1} {

	set ipname [xget_value $mhsinst "option" "ipname"]
        error "You must set the value for $base_param and $high_param" "" "libgen_error"

    }

}
