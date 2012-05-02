
// Located in: microblaze_0/include/xparameters.h

#include <xparameters.h>
#include <xuartlite_l.h>
#include <xbasic_types.h>

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

   volatile Xuint8  *BRAM8  = (volatile Xuint8*)  0x00003ff0;
   volatile Xuint32 *BRAM32 = (volatile Xuint32*) 0x00003ff0;
   volatile Xuint32 *TIMER = (volatile Xuint32*) 0xf8000000;


   volatile Xuint32 *DONE_SIGNAL = (volatile Xuint32 *)0x0a000000;
   volatile Xuint32 *START = (volatile Xuint32 *)0x0a000000; // WR '1' to CTRL_REG
   volatile Xuint32 *TIME = (volatile Xuint32 *)0x0a000004;
   volatile Xuint32 *PARAM = (volatile Xuint32 *)0x0a000008;

	Xuint8 command=0; // can be 'W'- write, 'R' - read, 'Q' - quit...

	Xuint32 address32;// 32-bit address
	Xuint32 size32;   // 32-bit size of data packet to be transfered
	Xuint32 status;

	Xuint32 time1, time2;
	*TIME=0;  // in the lmb host interface (STOPS when the system ends execution)
	*TIMER=0; // local timer

	do{
	   command = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );

		// recieve the address (MSB first)
		*(BRAM8+3) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+2) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+1) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+0) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		// recieve the size of data packet (MSB first)
		*(BRAM8+7) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+6) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+5) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
		*(BRAM8+4) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );

		address32 = *BRAM32;
		size32 = *(BRAM32+1);

		if( command == 'W' ) { // Write integers (32-bits)
		   for( i=0; i<size32; i+=4) {
         // Address to 8-bit data is needed
			   *((Xuint8*)(address32)+i) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *((Xuint8*)(address32)+i+1) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *((Xuint8*)(address32)+i+2) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *((Xuint8*)(address32)+i+3) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
         }
      } else if( command == 'w' ) { // Write Bytes
		   for( i=0; i<size32; i++) {
         // Address to 8-bit data is needed
			   *((Xuint8*)(address32)+i) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
         }
		} else if( command == 'A' ) { // Write to a 32-bit register
			// LITTLE_endian format
			   *(BRAM8+11) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *(BRAM8+10) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *(BRAM8+9) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
			   *(BRAM8+8) = XUartLite_RecvByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR );
				*((Xuint32*)address32) = *(BRAM32+2);
		} else if( command == 'R' ) { // Read integers (32 bits)
		   for( i=0; i<size32; i+=4) {
         // Address to 8-bit data is needed
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(address32)+i) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(address32)+i+1) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(address32)+i+2) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(address32)+i+3) );
         }
		} else if( command == 'r' ) { // Read Bytes
		   for( i=0; i<size32; i++) {
				XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(address32)+i) );
			}
		} else if( command == 'Z' ) { // read a 32-bit register
			// LITTLE_endian format
            *(BRAM32+2) = *((Xuint32*)address32);
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(BRAM8+11)) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(BRAM8+10)) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(BRAM8+9)) );
			   XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, *((Xuint8*)(BRAM8+8)) );
		} else if( command == 'S' ) { // Start
			*START=1; // for the IP cores
		   *TIME=0;
		   //*TIMER=0;
		} else if( command == 'T' ) { // get time
		   time2 = *TIME;
//		   time2 = *TIMER;
			// send MSB first
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (time2>>24) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (time2>>16) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (time2>>8)  & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR,  time2      & 0xFF );

		} else if( command == 'F' ) { // get FINISHED status

			status = *DONE_SIGNAL;
			// send MSB first
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (status>>24) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (status>>16) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (status>>8)  & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR,  status      & 0xFF );

		} else if( command == 'H' ) { // echo

			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, command );

			// send MSB first
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (address32>>24) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (address32>>16) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (address32>>8)  & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR,  address32      & 0xFF );
			// send MSB first
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (size32>>24) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (size32>>16) & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, (size32>>8)  & 0xFF );
			XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR,  size32      & 0xFF );

      }

	} while( command != 'Q' );

	XUartLite_SendByte( XPAR_HOST_IF_MB_RS232_UART_BASEADDR, 'E' ); // Exit...

   return 0;
}

