/******************************************************************************
 *                                                                            *
 *                   EOLAB @ DIEE - University of Cagliari                    *
 *                          Via Marengo, 2, 09123                             *
 *                       Cagliari - phone 070 675 5009                        *
 *                                                                            *
 * Author:       Gianfranco Deriu - gian.deriu@gmail.com                      *
 *                                                                            *
 * Project:     NEURAGHE - Accelerator for Convolutional neural network       *
 * File:                                                                      *
 * Description:                                                               *
 *                                                                            *
 ******************************************************************************/
#ifndef __SOC_DRIVERS_H_
#define __SOC_DRIVERS_H_

#include "types2.h"
#ifdef US
  #include "xparameters_us.h"
#else
  #include "xparameters.h"
#endif
#include "neumem.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <errno.h>

#ifndef BAREMETAL
 #include <sys/mman.h>
 #include <byteswap.h>
 #define PAGE_SIZE ((size_t)getpagesize())
 #define PAGE_MASK ((int)~(PAGE_SIZE - 1))
#endif

#define ZC706
//#define US
//#define ZED  

#define _MAXMEM_ 0x800000

#define NUM_BANKS 32


#define _NCLUSTER_ 1

#ifdef ZED
	#define DDR_REMAP 0x18000000
	#define DDR_SIZE  0x08000000
	#define _N_COL_ 2
	#define _N_ROW_ 2
#endif

#ifdef ZC706
	#define DDR_REMAP 0x30000000
	#define DDR_SIZE  0x10000000
  #define _N_COL_ 4
  #define _N_ROW_ 4
#endif

#define USE_HW_POOL 1

#define _DUALCONTROLLER_

#define SOC_CTRL_BASE_ADDR XPAR_SOC_AXI_CTRL_V1_0_0_BASEADDR

#define TCDM_BASE_ADDRESS XPAR_NURAGHE_SOC_0_BASEADDR
#define L2_BASE_ADDRESS (XPAR_NURAGHE_SOC_0_BASEADDR + 0xC000000)

#define EOC_ADDR (SOC_CTRL_BASE_ADDR + 0x4)
#define FETCH_ENABLE_ADDR (SOC_CTRL_BASE_ADDR)

#define CONV_DONE DDR_REMAP
#define CONV_SETUP_ADDR (TCDM_BASE_ADDRESS + 0xF7600) // this address must be the same chosen in PL code
#define OFFLOAD_READY (CONV_SETUP_ADDR + NUM_BANKS*4*11)

#define TCDM_BANK(n) (TCDM_BASE_ADDRESS + 4*n)

#define INTRA_BANK_STRIDE NUM_BANKS  //in words

#define CLUSTID_ADDR 0xFC


typedef unsigned long int neuADDR; 

typedef struct Socmap* SOCMAP;

struct Socmap {
	int* soc_addr;
	int* soc_cntr_addr;
	int* ddr_addr;
	int* ps7_slcr_addr;
	Conv_params* task_ptr;
	DATA* in;
	DATA* out;
	DATA* swap;
};


int * addr_linux(neuADDR addr, int size, int fd);

void init_soc(SOCMAP* socs, DATA** wPointer, int MAXMEM, int SWAPMEM, char* bitstream_file);
int mmap_soc (SOCMAP* socs);
int munmap_soc(SOCMAP* socs);
int load_bitstream(char* bitstream_file);
int bitstream_check(void);
int conv_setup (volatile Conv_params * task_ptr, int in_f, int out_f, int ih, int iw, int fs, int max_og,
		         int rectifier_activ, int pooling, int qf, int precision8, int zero_padding, int w_addr, int x_addr, int y_addr);
void load_instr (volatile int * soc_addr);
void fetch_enable (volatile int * soc_cntr_addr);
void fetch_disable (volatile int * soc_cntr_addr);
void wait_eoc (volatile int * soc_cntr_addr);
void wait_for (long int num);
void read_mem (volatile int *from, int len, int stride);
void lock_ps_regs(int* ps7_slcr_addr);
void unlock_ps_regs (int* ps7_slcr_addr);
void read_tcdm (volatile int *from, int nrow);
void set_fclk_div(int* ps7_slcr_addr,int div1, int div2);
void print_clk(int* ps7_slcr_addr);
void start_fclk(int* ps7_slcr_addr);
void stop_fclk(int* ps7_slcr_addr);
void use_default_conv_test(void);
void wait_for_conv_ddr(volatile int * ddr_addr);
void wait_for_mw_ready_ddr(volatile int * ddr_addr);
void wait_for_conv(volatile int * soc_cntr_addr, volatile int * soc_addr);
void wait_for_mw_ready(volatile int * ddr_addr);
void trigger_conv (volatile int * ddr_addr, volatile int * soc_addr);

void *memcpyNEON(void *dst, const void *src, size_t len);
#endif
