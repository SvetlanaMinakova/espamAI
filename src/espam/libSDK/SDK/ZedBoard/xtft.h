#ifndef XTFT_H
#define XTFT_H


#include <xstatus.h>
#include <xil_io.h>

#define TFT_FRAME_BUFFER_BASE_ADDR (0x11000000)

#define XTFT_ASCIICHAR_OFFSET 32

#define RED_COLOR_VALUE		(0x00FF0000)
#define GREEN_COLOR_VALUE	(0x0000FF00)
#define BLUE_COLOR_VALUE	(0x000000FF)
#define BLACK_COLOR_VALUE	(0x0)
#define WHITE_COLOR_VALUE 	(0x00FFFFFF)

#define XTFT_DEF_FGCOLOR	WHITE_COLOR_VALUE
#define XTFT_DEF_BGCOLOR 	BLACK_COLOR_VALUE
#define XTFT_DEF_COLVAL  	0x0
#define XTFT_DEF_ROWVAL	 	0x0

#define XTFT_CHAR_WIDTH				8    /**< Character width */
#define XTFT_CHAR_HEIGHT			12   /**< Character height */
#define XTFT_DISPLAY_WIDTH			640  /**< Width of the screen */
#define XTFT_DISPLAY_HEIGHT			480  /**< Height of the screen */
#define XTFT_DISPLAY_BUFFER_WIDTH	1024 /**< Buffer width of a line */


void XTft_Init();
u32 XTft_GetColumn();
u32 XTft_GetRow();
void XTft_SetPos(u32 ColVal, u32 RowVal);
void XTft_SetPosChar(u32 ColVal, u32 RowVal);
void XTft_SetColor(u32 FgColor, u32 BgColor);
void XTft_SetPixel(u32 ColVal, u32 RowVal, u32 PixelVal);
void XTft_GetPixel(u32 ColVal, u32 RowVal, u32* PixelVal);

void XTft_Write(u8 CharValue);

void XTft_Scroll();
void XTft_ClearScreen();
void XTft_FillScreen(u32 ColStartVal, u32 RowStartVal,u32 ColEndVal, u32 RowEndVal, u32 PixelVal);

int test_tft();


#endif /* XTFT_H */

