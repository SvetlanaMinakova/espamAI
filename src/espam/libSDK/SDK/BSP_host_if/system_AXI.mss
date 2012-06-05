
 PARAMETER VERSION = 2.2.0


BEGIN OS
 PARAMETER OS_NAME = standalone
 PARAMETER OS_VER = 3.01.a
 PARAMETER PROC_INSTANCE = host_if_mb
 PARAMETER STDIN = host_if_mb_RS232_Uart
 PARAMETER STDOUT = host_if_mb_RS232_Uart
END


BEGIN PROCESSOR
 PARAMETER DRIVER_NAME = cpu
 PARAMETER DRIVER_VER = 1.13.a
 PARAMETER HW_INSTANCE = host_if_mb
END


BEGIN DRIVER
 PARAMETER DRIVER_NAME = v6_ddrx
 PARAMETER DRIVER_VER = 2.00.a
 PARAMETER HW_INSTANCE = DDR3_SDRAM
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = uartlite
 PARAMETER DRIVER_VER = 2.00.a
 PARAMETER HW_INSTANCE = host_if_mb_RS232_Uart
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = iic
 PARAMETER DRIVER_VER = 2.03.a
 PARAMETER HW_INSTANCE = host_if_mb_axi_iic_0
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = bram
 PARAMETER DRIVER_VER = 3.00.a
 PARAMETER HW_INSTANCE = host_if_mb_dlmb_ctrl_bram_0
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = bram
 PARAMETER DRIVER_VER = 3.00.a
 PARAMETER HW_INSTANCE = host_if_mb_ilmb_ctrl_bram_0
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = tmrctr
 PARAMETER DRIVER_VER = 2.03.a
 PARAMETER HW_INSTANCE = host_if_mb_timer
END

BEGIN DRIVER
 PARAMETER DRIVER_NAME = generic
 PARAMETER DRIVER_VER = 1.00.a
 PARAMETER HW_INSTANCE = lmb_host_interface_ctrl
END

