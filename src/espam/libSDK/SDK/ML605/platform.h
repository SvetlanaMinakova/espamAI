#ifndef PLATFORM_H_
#define PLATFORM_H_
#include <FreeRTOS.h>
#include <timers.h>
#include <xtmrctr.h>
#include <stdio.h>
#define mainDONT_BLOCK   ( portTickType ) 0
#define TIMER_DEVICE_ID  XPAR_TMRCTR_0_DEVICE_ID
#define TIMER_FREQ_HZ    XPAR_TMRCTR_0_CLOCK_FREQ_HZ
#define TIMER_INTR_ID    XPAR_INTC_0_TMRCTR_0_VEC_ID
#if defined __cplusplus
extern "C" {
#endif

extern void vPortTickISR( void *pvUnused );
static XTmrCtr xTimer0Instance;

void vApplicationMallocFailedHook( void )
{
	taskDISABLE_INTERRUPTS();
	xil_printf("PANIC: malloc failed! Disabling interrupts...");
	for( ;; );
}

void vApplicationStackOverflowHook( xTaskHandle *pxTask, signed char *pcTaskName )
{
	( void ) pcTaskName;
	( void ) pxTask;
	taskDISABLE_INTERRUPTS();
	xil_printf("PANIC: Stack Overflow detected! Disabling interrupts...");
	for( ;; );
}

void vApplicationIdleHook( void ) { }
void vApplicationTickHook( void ) { }
void vSoftwareTimerCallback( xTimerHandle xTimer ) { }
void vApplicationSetupTimerInterrupt( void )
{
	portBASE_TYPE xStatus;
	const unsigned char ucTimerCounterNumber = ( unsigned char ) 0U;
	const unsigned long ulCounterValue = ( ( TIMER_FREQ_HZ / configTICK_RATE_HZ ) - 1UL );
	xStatus = XTmrCtr_Initialize( &xTimer0Instance, TIMER_DEVICE_ID );
	if( xStatus == XST_SUCCESS ) 
	{
		xStatus = xPortInstallInterruptHandler( TIMER_INTR_ID, vPortTickISR, NULL );
	}	
	if( xStatus == pdPASS ) 
	{
		vPortEnableInterrupt( TIMER_INTR_ID );
		XTmrCtr_SetHandler( &xTimer0Instance, (XTmrCtr_Handler)vPortTickISR, NULL );
		XTmrCtr_SetResetValue( &xTimer0Instance, ucTimerCounterNumber, ulCounterValue );
		XTmrCtr_SetOptions( &xTimer0Instance, ucTimerCounterNumber, ( XTC_INT_MODE_OPTION | XTC_AUTO_RELOAD_OPTION | XTC_DOWN_COUNT_OPTION ) );
		XTmrCtr_Start( &xTimer0Instance, ucTimerCounterNumber );
	}
	configASSERT( ( xStatus == pdPASS ) );
}

void vApplicationClearTimerInterrupt( void )
{
unsigned long ulCSR;
	ulCSR = XTmrCtr_GetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0 );
	XTmrCtr_SetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0, ulCSR );
}

void init_platform() { }
#define delayCheckDeadline(xLastReleaseTime, xPeriod) do {\
	portTickType ticks;\
    ticks = xTaskGetTickCount();\
    vTaskDelayUntil( xLastReleaseTime, xPeriod );\
    if (ticks > *xLastReleaseTime)\
    {\
		taskDISABLE_INTERRUPTS();\
        xil_printf("PANIC! Deadline miss");\
        for (;;);\
    }\
}while(0);

#if defined __cplusplus
}
#endif
#endif

