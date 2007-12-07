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

#include "FPGA.h"

FPGA::FPGA()
{
  /*********************
   * member assignment *
   *********************/

    // Set up the SSRAM size and offset
    ssramOffset = 0x200000U;     // In bytes
    pageSize    = 0x200000U / 4; // In longwords
    // Set up the clock frequencey of the board
    mclk_freq = 40.0f;
    freq      = 40.0f;
    // Set up DMA mode
    dma = TRUE;
    // Set up the FPGA configuration (bitstream) file
    filename = "top_level-xrc2-2v6000.bit";

    nrep = 1;
    speed = FALSE;

    // Set up expected options
	options[0].key = "banks";
	options[0].type = Option_hex;
	options[0].def.hexVal = 0xffffffff;
	options[0].meaning = "bitmask of banks to test";
	options[1].key = "card";
	options[1].type = Option_uint;
	options[1].def.uintVal = 0;
	options[1].meaning = "ID of card to use";
	options[2].key = "index";
	options[2].type = Option_uint;
	options[2].def.uintVal = 0;
	options[2].meaning = "index of card to use";
	options[3].key = "lclk";
	options[3].type = Option_float;
	options[3].def.floatVal = 33.0;
	options[3].meaning = "local bus clock speed (MHz)";
	options[4].key = "repeat";
	options[4].type = Option_uint;
	options[4].def.uintVal = 1;
	options[4].meaning = "number of repetitions";
	options[5].key = "speed";
	options[5].type = Option_boolean;
	options[5].def.booleanVal = TRUE;
	options[5].meaning = "TRUE => measure access speed";
	options[6].key = "usedma";
	options[6].type = Option_boolean;
	options[6].def.booleanVal = TRUE;
	options[6].meaning = "TRUE => use DMA for test";
	arguments.nOption = 7;
	arguments.option = options;
	arguments.nParamType = 0;
	arguments.minNParam = 0;


     printf("\nInitializing the card (FPGA board)...\n");

    /***********************************************/
    /* Open the PCI card (FPGA boeard)             */
    /***********************************************/
	cardID =  (unsigned long) 0;
	status = ADMXRC2_OpenCard(cardID, &card);
	if (status != ADMXRC2_SUCCESS) {
		printf("Failed to open card with ID %d: %s\n",
			cardID, ADMXRC2_GetStatusString(status));
		exit(-1);
	}

	/************************************************************/
	/* Get card information, specifically for device fitted and */
	/* number of SSRAM banks                                    */
	/************************************************************/
	status = ADMXRC2_GetCardInfo(card, &cardInfo);
	if (status != ADMXRC2_SUCCESS) {
		printf("Failed to get card info: %s\n", ADMXRC2_GetStatusString(status));
		exit(-1);
	}

	switch (cardInfo.BoardType) {
	case ADMXRC2_BOARD_ADMXRC:
	case ADMXRC2_BOARD_ADMXRC_P:
	case ADMXRC2_BOARD_ADMXRC2_LITE:
		min_freq = 25.0;
		max_freq = 40.0;
		break;
	case ADMXRC2_BOARD_ADMXRC2:
		min_freq = 24.0;
		max_freq = 66.0;
		break;
	}

	if (freq > max_freq || freq < min_freq) {
		printf("LCLK frequency of %.1f MHz is not supported\n", freq);
		exit(-1);
	}

    /*********************************/
	/* Get the address of FPGA space */
	/*********************************/
	status = ADMXRC2_GetSpaceInfo(card, 0, &spInfo);
	if (status != ADMXRC2_SUCCESS) {
		printf("Failed to get space 0 info: %s\n",
			ADMXRC2_GetStatusString(status));
		exit(-1);
	}
	fpgaSpace = (volatile DWORD*) spInfo.VirtualBase;
	printf("Virtual Base Address: %0x \n",spInfo.VirtualBase);

	/************************************************************/
	/* Check that the type, size, width etc. of each SSRAM bank */
	/* is the same.                                             */
	/************************************************************/
	for (i = 0; i < cardInfo.NumRAMBank; i++) {
		ADMXRC2_BANK_INFO	bankInfo2;

		if (i == 0) {
			status = ADMXRC2_GetBankInfo(card, 0, &bankInfo);
		} else {
			status = ADMXRC2_GetBankInfo(card, 0, &bankInfo2);
		}
		if (status != ADMXRC2_SUCCESS) {
			printf("Failed to get bank %d info: %s\n",
				i,
				ADMXRC2_GetStatusString(status));
			exit(-1);
		}

		if ((i == 0 && !bankInfo.Fitted) || (i != 0 && !bankInfo2.Fitted)) {
			printf("Not all SSRAM banks are fitted - aborting.\n");
			exit(-1);
		}

		if (i != 0) {
			if (bankInfo2.Type != bankInfo.Type ||
				bankInfo2.Width != bankInfo.Width ||
				bankInfo2.Size != bankInfo.Size) {
				printf("Not all SSRAM banks are the same - aborting.\n");
				exit(-1);
			}
		}
	}

	/*****************************************************/
	/* Calculate memory size in longwords (32-bit words) */
	/*****************************************************/
	numBank = cardInfo.NumRAMBank;
	bankSize = (bankInfo.Width / 8) * bankInfo.Size / 4;
	memorySize = numBank * bankSize;
	lastPageSize = memorySize & (pageSize - 1);


	/**********************************************/
	/* Determine which and how many banks to test */
	/**********************************************/
	if (!options[0].specified) {
		bankTestMask = cardInfo.RAMBanksFitted;
	} else {
		bankTestMask = options[0].value.hexVal & cardInfo.RAMBanksFitted;
	}
	numTestBank = 0;
	for (i = 0; i < 32; i++) {
		if (bankTestMask & (0x1UL << i))
			numTestBank++;
	}
	//testSize = numTestBank * bankSize;

	ssram = (volatile DWORD*) (((BYTE*) fpgaSpace) + ssramOffset);

	rambuf = (DWORD*) malloc(memorySize * sizeof(DWORD));
	if (rambuf == NULL) {
		printf("Failed to allocate RAM buffer - aborting\n");
		exit(-1);
	}


	printf("\nSetup the clock frequency of the card (FPGA board)\n");
    /***********************************************/
    /* Setup the Clock frequency of the board      */
	/***********************************************/
	status = ADMXRC2_SetClockRate(card, ADMXRC2_CLOCK_LCLK, freq * 1.0e6, NULL);
	if (status != ADMXRC2_SUCCESS)
	{
		printf("Failed to set LCLK to %.1fMHz: %s\n",
			freq, ADMXRC2_GetStatusString(status));
		exit(-1);
	}

	status = ADMXRC2_SetClockRate(card, 1, mclk_freq * 1.0e6, NULL);
	if (status != ADMXRC2_SUCCESS)
	{
		printf("Failed to set clock 1 to %.1fMHz: %s\n",
			mclk_freq, ADMXRC2_GetStatusString(status));
		exit(-1);
	}

	printf("\nConfigure the FPGA with the bit stream file...\n");
    /***********************************************/
    /* Configure the FPGA with the bit stream file */
	/***********************************************/
	status = ADMXRC2_ConfigureFromFile(card, filename);
	if (status != ADMXRC2_SUCCESS) {
		printf ("Failed to load the bitstream '%s': %s\n",
			filename, ADMXRC2_GetStatusString(status));
		exit(-1);
	}

    printf("\nSetup DMA mode of communication between HOST and FPGA\n\n\n");
    /***********************************************/
    /* Setup the DMA mode                          */
	/***********************************************/
	status = ADMXRC2_SetupDMA(card, rambuf, memorySize * sizeof(DWORD), 0, &dmadesc);
	if (status != ADMXRC2_SUCCESS) {
		printf ("Failed to get a DMA descriptor: %s\n",
			ADMXRC2_GetStatusString(status));
		exit(-1);
	}

	dmamode = ADMXRC2_BuildDMAModeWord(
		cardInfo.BoardType,
		ADMXRC2_IOWIDTH_32,
		0,
		ADMXRC2_DMAMODE_USEREADY | ADMXRC2_DMAMODE_USEBTERM | ADMXRC2_DMAMODE_BURSTENABLE);

	dmaevent = CreateEvent(NULL, TRUE, FALSE, NULL);

    /******************************/
	/* Wait for DLLs/DCMs to lock */
	/******************************/
	Sleep(2000);

}

FPGA::~FPGA()
{
	free(rambuf);
	CloseHandle(dmaevent);
	ADMXRC2_UnsetupDMA(card, dmadesc);
	ADMXRC2_CloseCard(card);

}

/************************************************************************************/
/* This is the user defined method that controls the hardware interface and design. */
/* In this example the HW design does Sobel operator on an image.                   */
/* Parameter "input"  - a pointer to the input image                                */
/* Parameter "output" - a pointer to the resulting image after the Sobel operation  */
/* Parameters "par1" and "par2" define the size of the input image                  */
/************************************************************************************/
void FPGA::Sobel( int *input, int *output, int par1, int par2 )
{
    WORD temp;
	int LENGTH_DESIGN = (par1+2)*(par2+2);

    // store image
    for (int j = 0; j < LENGTH_DESIGN; j++) {
      rambuf[j] = (DWORD)(input[j]);
    }


    // write the image into to Bank0 of the FPGA board
    printf("change to initialise memory mode\n");
    fpgaSpace[COMMAND_REG] = cmd_Initialize; // initialise memory mode + access to banks to host
    fpgaSpace[COMMAND_REG];


    printf("Configuring parameters for an input image size %d x %d ...\n", par1,par2);
    //--- Load first parameter ---
	fpgaSpace[PARAM_REG]   = par1;
	fpgaSpace[COMMAND_REG] = cmd_LoadPar; // set the strobe
	fpgaSpace[COMMAND_REG] = 0x0;         // clear the strobe
    //--- Load second parameter ---
	fpgaSpace[PARAM_REG]   = par2;
	fpgaSpace[COMMAND_REG] = cmd_LoadPar; // set the strobe
	fpgaSpace[COMMAND_REG] = 0x0;         // clear the strobe

    // Initialize memory
    printf("writing memory BANK 0\n");
	status = writeSSRAM(rambuf , 0, LENGTH_DESIGN, dma);
	if (status != ADMXRC2_SUCCESS) {
		printf("exiting\n");
		exit(0);
	}
    printf("The input data is loaded.\n");

	// process the packet in the FPGA
	fpgaSpace[COMMAND_REG] = cmd_Execute; // execute mode + access to banks to design

	while(1){
	    temp = fpgaSpace[STATUS_REG];
            //printf("Waiting for status reg=1, status reg is 0x%8.8lx\n", temp);
            //printf("Status reg is %8x\n", temp);
            if (temp == stat_Finished) break;
	}

        // read the image from Bank2 of the FPGA board
	fpgaSpace[COMMAND_REG] = cmd_Read; // read memory mode + access to banks to host
	fpgaSpace[COMMAND_REG];

	status = readSSRAM(rambuf+2*bankSize, 2*bankSize+1, LENGTH_DESIGN, dma);
	if (status != ADMXRC2_SUCCESS) {
	  printf("Error: failed to read SSRAM\n");
	  exit(1);
	}


	//for (int k = 0; k < 10; k++) {
	for (int k = 0; k < par1; k++) {
           output[k] = rambuf[2*bankSize+1+k];
	}


	return;
}

ADMXRC2_STATUS FPGA::reverse(DWORD v)
{
	DWORD	t = 0;
	int		i;

	for (i = 0; i < 32; i++) {
		t <<= 1;
		t |= v & 0x1U;
		v >>= 1;
	}

	return ADMXRC2_SUCCESS;
}

// read a block from the SSRAM
ADMXRC2_STATUS FPGA::readSSRAM(
		  void*			buffer,
		  unsigned long	offset,
		  unsigned long	length,
		  int				useDMA    )
{
	ADMXRC2_STATUS		status;
	volatile DWORD*		d = (DWORD*) buffer;
	volatile DWORD*		s;
	DWORD				pgidx;
	DWORD				pgoffs;
	DWORD				chunk;
	DWORD				i;
	
	while (length) {
		pgidx = offset & ~(pageSize - 1);
		pgoffs = offset & (pageSize - 1);
		
		chunk = pageSize - pgoffs;
		if ((pgidx + chunk) > memorySize) {
			chunk = lastPageSize - pgoffs;
		}
		if (chunk > length) {
			chunk = length;
		}
		
		/* Set the page register */
		fpgaSpace[ZBT_PAGE_REG] = pgidx / pageSize;
		fpgaSpace[ZBT_PAGE_REG]; /* make sure it's got there */
		
		if (useDMA) {
			status = ADMXRC2_DoDMA(
				card,
				dmadesc,
				offset * sizeof(DWORD),
				chunk * sizeof(DWORD),
				ssramOffset + (pgoffs * sizeof(DWORD)),
				ADMXRC2_LOCALTOPCI,
				0,
				dmamode,
				0,
				NULL,
				dmaevent);
			if (status != ADMXRC2_SUCCESS) {
				return status;
			}
		} else {
			s = ssram + pgoffs;
			for	(i = 0; i < chunk; i++) {
				*d++ = *s++;
			}
		}
		
		offset += chunk;
		length -= chunk;
	}
	
	return ADMXRC2_SUCCESS;
}

// write a block to the SSRAM 
ADMXRC2_STATUS FPGA::writeSSRAM(
		   const void*		buffer,
		   unsigned long	offset,
		   unsigned long	length,
		   int				useDMA )
{
	ADMXRC2_STATUS		status;
	volatile DWORD*		d;
	volatile DWORD*		s = (DWORD*) buffer;
	DWORD				pgidx;
	DWORD				pgoffs;
	DWORD				chunk;
	DWORD				i;
	
	while (length) {
		pgidx = offset & ~(pageSize - 1);
		pgoffs = offset & (pageSize - 1);
		
		chunk = pageSize - pgoffs;
		if ((pgidx + chunk) > memorySize) {
			chunk = lastPageSize - pgoffs;
		}
		if (chunk > length) {
			chunk = length;
		}
		
		/* Set the page register */
		fpgaSpace[ZBT_PAGE_REG] = pgidx / pageSize;
		fpgaSpace[ZBT_PAGE_REG]; /* make sure it's got there */
		
		if (useDMA) {
			status = ADMXRC2_DoDMA(
				card,
				dmadesc,
				offset * sizeof(DWORD),
				chunk * sizeof(DWORD),
				ssramOffset + (pgoffs * sizeof(DWORD)),
				ADMXRC2_PCITOLOCAL,
				0,
				dmamode,
				0,
				NULL,
				dmaevent);
			if (status != ADMXRC2_SUCCESS) {
				return status;
			}
		} else {
			d = ssram + pgoffs;
			for	(i = 0; i < chunk; i++) {
				*d++ = *s++;
			}
			*(d-1); /* make sure it's got there */
		}
		
		offset += chunk;
		length -= chunk;
	}
	
	return ADMXRC2_SUCCESS;
}
