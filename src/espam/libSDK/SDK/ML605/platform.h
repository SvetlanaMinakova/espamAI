"#ifndef PLATFORM_H_\n" +
        "#define PLATFORM_H_\n\n" +
        "#include <FreeRTOS.h>\n" +
        "#include <timers.h>\n" +
        "#include <xtmrctr.h>\n" +
        "#include <stdio.h>\n\n" +
        "#define mainDONT_BLOCK   ( portTickType ) 0\n" +
        "#define TIMER_DEVICE_ID  XPAR_TMRCTR_0_DEVICE_ID\n" +
        "#define TIMER_FREQ_HZ    XPAR_TMRCTR_0_CLOCK_FREQ_HZ\n" +
        "#define TIMER_INTR_ID    XPAR_INTC_0_TMRCTR_0_VEC_ID\n\n" +
        "#if defined __cplusplus\n" +
        "extern \"C\" {\n" +
        "#endif\n\n" +
        "extern void vPortTickISR( void *pvUnused );\n" +
        "static XTmrCtr xTimer0Instance;\n\n" +
        "void vApplicationMallocFailedHook( void )\n" +
        "{\n" +
        "\ttaskDISABLE_INTERRUPTS();\n" +
        "\txil_printf(\"PANIC: malloc failed! Disabling interrupts...\\n\");\n" +
        "\tfor( ;; );\n" +
        "}\n" +
        "void vApplicationStackOverflowHook( xTaskHandle *pxTask, signed char *pcTaskName )\n" +
        "{\n" +
        "\t( void ) pcTaskName;\n" +
        "\t( void ) pxTask;\n" +
        "\ttaskDISABLE_INTERRUPTS();\n" +
        "\txil_printf(\"PANIC: Stack Overflow detected! Disabling interrupts...\\n\");\n" +
        "\tfor( ;; );\n" +
        "}\n" +
        "void vApplicationIdleHook( void ) { } \n" +
        "void vApplicationTickHook( void ) { } \n" +
        "void vSoftwareTimerCallback( xTimerHandle xTimer ) { } \n\n" +
        "void vApplicationSetupTimerInterrupt( void )\n" +
        "{\n" +
        "\tportBASE_TYPE xStatus;\n" +
        "\tconst unsigned char ucTimerCounterNumber = ( unsigned char ) 0U;\n" +
        "\tconst unsigned long ulCounterValue = ( ( TIMER_FREQ_HZ / configTICK_RATE_HZ ) - 1UL );\n" +
        "\txStatus = XTmrCtr_Initialize( &xTimer0Instance, TIMER_DEVICE_ID );\n" +
        "\tif( xStatus == XST_SUCCESS ) {\n" +
        "\t\txStatus = xPortInstallInterruptHandler( TIMER_INTR_ID, vPortTickISR, NULL );\n" +
        "\t}\n" +
        "\tif( xStatus == pdPASS ) { \n" +
        "\t\tvPortEnableInterrupt( TIMER_INTR_ID );\n" +
        "\t\tXTmrCtr_SetHandler( &xTimer0Instance, (XTmrCtr_Handler)vPortTickISR, NULL );\n" +
        "\t\tXTmrCtr_SetResetValue( &xTimer0Instance, ucTimerCounterNumber, ulCounterValue );\n" +
        "\t\tXTmrCtr_SetOptions( &xTimer0Instance, ucTimerCounterNumber, ( XTC_INT_MODE_OPTION | XTC_AUTO_RELOAD_OPTION | XTC_DOWN_COUNT_OPTION ) );\n" +
        "\t\tXTmrCtr_Start( &xTimer0Instance, ucTimerCounterNumber );\n" +
        "\t}\n" +
        "\tconfigASSERT( ( xStatus == pdPASS ) );\n" +
        "}\n" +
        "void vApplicationClearTimerInterrupt( void )\n" +
        "{\n" +
        "\tunsigned long ulCSR;\n" +
        "\tulCSR = XTmrCtr_GetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0 );\n" +
        "\tXTmrCtr_SetControlStatusReg( XPAR_TMRCTR_0_BASEADDR, 0, ulCSR );\n" +
        "}\n" +
        "void init_platform() { } \n" +
        "#define delayCheckDeadline(xLastReleaseTime, xPeriod) do {\\\n" +
        "                                                          portTickType ticks;\\\n" +
        "                                                          ticks = xTaskGetTickCount();\\\n" +
        "                                                          vTaskDelayUntil( xLastReleaseTime, xPeriod );\\\n" +
        "                                                          if (ticks > *xLastReleaseTime) {\\\n" +
        "                                                              taskDISABLE_INTERRUPTS();\\\n" +
        "                                                              xil_printf(\"PANIC! Deadline miss\\n\");\\\n" +
        "                                                              for (;;);\\\n" +
        "                                                          }\\\n" +
        "                                                      }while(0);\n" +
        "#if defined __cplusplus\n" +
        "}\n" +
        "#endif\n" +
        "#endif\n";
