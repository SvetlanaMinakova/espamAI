/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

#ifndef FPGA_H
#define FPGA_H


#include <windows.h>
#include <stdio.h>
#include <time.h>
#include <iostream>
using namespace std;

/********************************************
 * FPGA BOARD RELATED HEADERS AND CONSTANTS *
 ********************************************/
#include <admxrc2.h>
#include <common.h>

// define the addresses of the registers in the FPGA
#define ZBT_PAGE_REG		(0)
#define ZBT_STATUS_REG		(1)

// status register for the design
// Use only bit 0: value '1' - design finished
#define STATUS_REG			(2)
// define the status flags
#define stat_Finished       (1)

// command register for the design
#define COMMAND_REG			(3)
/*******************************************************************************************
  The command register is subdevided in the following way:                                 *
  bits 31 downto 26 are masks for the memory banks.'0' - host access. '1' - design access  *
  bit 0 is RESET stage (active high)                                                       *
  bit 1 is Initialize (write) Memory stage (active high)                                   *
  bit 2 is Read Memory stage (active high)                                                 *
  bit 3 is Execute stage (active high)                                                     *
  bit 4 is Load Parameters Strobe (active high)						   *
********************************************************************************************/
// define the commands in the command register COMMAND_REG
#define cmd_Initialize      ( (DWORD)0x00000002U ) // initialise memory mode + access to banks from host)
#define cmd_Read            ( (DWORD)0x00000004U ) // read memory mode + access to banks from host
#define cmd_Execute         ( (DWORD)0xff000008U ) // execute mode + access to banks from design
#define cmd_LoadPar         ( (DWORD)0x00000010U ) // load a parameter into the temp reg (in the nodes) + access to banks from host         

// parameter register for the design
#define PARAM_REG			(4)

/************************************************
 * COMPAAN DESIGN RELATED HEADERS AND CONSTANTS *
 ************************************************/
  
class FPGA
{
public:
	// constructor
	FPGA();
	// destructor
	~FPGA();
	
	// member functions
	void Sobel( int *input, int *output, int par1, int par2 );
private:
    /******************** 
	 * member functions * 
	 ********************/
    ADMXRC2_STATUS reverse(DWORD v);

    // read a block from the SSRAM of the FPGA board
    ADMXRC2_STATUS readSSRAM(void* buffer, unsigned long	offset, 
		                            unsigned long length, int useDMA    );
    // write a block to the SSRAM of the FPGA board
    ADMXRC2_STATUS writeSSRAM(const void* buffer, unsigned long	offset,
                                     unsigned long length, int useDMA   );
    /********************
	 * member variables *
	 ********************/           
	 //define some constants and variables related to the memory banks
     unsigned long  ssramOffset;
     unsigned long  pageSize;

     unsigned long  numBank;
     unsigned long  bankSize;
     unsigned long  memorySize;
     unsigned long  lastPageSize;

     DWORD          bankTestMask;
     unsigned long  numTestBank;
     unsigned long  testSize;

     volatile DWORD*	fpgaSpace;
     volatile DWORD*	ssram;

     DWORD*          rambuf;

     ADMXRC2_HANDLE	 card;
     HANDLE	         dmaevent;
     ADMXRC2_DMADESC dmadesc;
     DWORD           dmamode;

	 ADMXRC2_CARDID     cardID;
	 ADMXRC2_STATUS		status;
	 ADMXRC2_CARD_INFO	cardInfo;
	 ADMXRC2_SPACE_INFO	spInfo;
	 ADMXRC2_BANK_INFO	bankInfo;

	 char* filename;

     float mclk_freq, freq, min_freq, max_freq;

     Option				options[7];
     Arguments			arguments;
     DWORD				i;
     BOOLEAN            dma;
     BOOLEAN            speed;
     unsigned long      nrep;
	
};
#endif
