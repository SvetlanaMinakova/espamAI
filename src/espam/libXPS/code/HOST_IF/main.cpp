
// Located in: microblaze_0/include/xparameters.h

#include "xparameters.h"
#include "xuartns550_l.h"

#include "aux_func.h"

/*==========================================================
Simple protocol to move data between the FPGA board and
the host PC using serial (UART) interface. Control packets
are used with the following format:

|command (1Byte)|address (4Bytes)|size (4Bytes)|

'command' is 'W'-write, 'R'-read, 'Q'-quit...to be extended
'address' is a 32-bit address where data to be written/read
'size' is a 32-bit value representing the size (in bytes)
of the data to be transfered
/*==========================================================*/

int main (void) {

	int i;
	//	volatile Xuint32 *DDR2_MEM = (volatile Xuint32*) 0x90000000;
	//   volatile Xuint32 *ZBT32 = (volatile Xuint32*) 0x8a400000;

   volatile Xuint8  *BRAM8  = (volatile Xuint8*)  0x00007ff0;
   volatile Xuint32 *BRAM32 = (volatile Xuint32*) 0x00007ff0;
   volatile Xuint32 *TIMER = (volatile Xuint32*) 0xf8000000;

   volatile Xuint32 *DONE_SIGNAL = (volatile Xuint32 *)0x0a000000;
   volatile Xuint32 *START = (volatile Xuint32 *)0x0a000000; // WR '1' to CTRL_REG
   volatile Xuint32 *TIME = (volatile Xuint32 *)0x0a000004;
   volatile Xuint32 *PARAM = (volatile Xuint32 *)0x0a000008;


	/* Initialize RS232_Uart_1 - Set baudrate and number of stop bits */
	XUartNs550_SetBaud(XPAR_RS232_UART_1_BASEADDR, XPAR_XUARTNS550_CLOCK_HZ, 115200);
	//   XUartNs550_SetBaud(XPAR_RS232_UART_1_BASEADDR, XPAR_XUARTNS550_CLOCK_HZ, 9600);

	// for earlier version of XPS, it is called
	// XUartNs550_mSetLineControlReg(XPAR_RS232_UART_1_BASEADDR, XUN_LCR_8_DATA_BITS);
	// for latest XPS (13.2 and later), it is called
	XUartNs550_SetLineControlReg(XPAR_RS232_UART_1_BASEADDR, XUN_LCR_8_DATA_BITS);


	Xuint8 command=0; // can be 'W'- write, 'R' - read, 'Q' - quit...
	Xuint32 address32;// 32-bit address
	Xuint32 size32;   // 32-bit size of data packet to be transfered
	Xuint32 status;

	Xuint32 time1, time2;
	*TIME=0;  // in the lmb host interface (STOPS when the system ends execution)
	*TIMER=0; // local timer

	do{
	   command = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		// recieve the address (MSB first)
		*(BRAM8+0) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+1) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+2) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+3) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		// recieve the size of data packet (MSB first)
		*(BRAM8+4) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+5) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+6) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
		*(BRAM8+7) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );

		address32 = *BRAM32;
		size32 = *(BRAM32+1);

		if( command == 'W' ) { // Write integers (32-bits)
		   for( i=0; i<size32; i+=4) {
         // Address to 8-bit data is needed (LITTLE-to-BIG endian conversion)
			   *((Xuint8*)(address32)+i+3) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *((Xuint8*)(address32)+i+2) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *((Xuint8*)(address32)+i+1) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *((Xuint8*)(address32)+i) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
         }
      } else if( command == 'w' ) { // Write Bytes
		   for( i=0; i<size32; i++) {
         // Address to 8-bit data is needed
			   *((Xuint8*)(address32)+i) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
         }
		} else if( command == 'A' ) { // Write to a 32-bit register
			// LITTLE_endian format
			   *(BRAM8+8) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *(BRAM8+9) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *(BRAM8+10) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
			   *(BRAM8+11) = XUartNs550_RecvByte( XPAR_RS232_UART_1_BASEADDR );
				*((Xuint32*)address32) = *(BRAM32+2);
		} else if( command == 'R' ) { // Read integers (32 bits)
		   for( i=0; i<size32; i+=4) {
         // Address to 8-bit data is needed (BIG-to-LITTLE endian conversion)
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(address32)+i+3) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(address32)+i+2) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(address32)+i+1) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(address32)+i) );
         }
		} else if( command == 'r' ) { // Read Bytes
		   for( i=0; i<size32; i++) {
				XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(address32)+i) );
			}
		} else if( command == 'Z' ) { // read a 32-bit register
			// LITTLE_endian format
            *(BRAM32+2) = *((Xuint32*)address32);
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(BRAM8+8)) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(BRAM8+9)) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(BRAM8+10)) );
			   XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, *((Xuint8*)(BRAM8+11)) );
		} else if( command == 'S' ) { // Start
			*START=1; // for the IP cores
		   *TIME=0;
		   //*TIMER=0;
		} else if( command == 'T' ) { // get time
		   time2 = *TIME;
//		   time2 = *TIMER;
			// send MSB first
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (time2>>24) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (time2>>16) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (time2>>8)  & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR,  time2      & 0xFF );

		} else if( command == 'F' ) { // get FINISHED status

			status = *DONE_SIGNAL;
			// send MSB first
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (status>>24) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (status>>16) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (status>>8)  & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR,  status      & 0xFF );

		} else if( command == 'H' ) { // echo

			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, command );
			// send MSB first
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (address32>>24) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (address32>>16) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (address32>>8)  & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR,  address32      & 0xFF );
			// send MSB first
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (size32>>24) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (size32>>16) & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, (size32>>8)  & 0xFF );
			XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR,  size32      & 0xFF );
      }

	} while( command != 'Q' );

	XUartNs550_SendByte( XPAR_RS232_UART_1_BASEADDR, 'E' ); // Exit...

   return 0;
}

