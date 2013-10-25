
#include "xtft.h"
#include "xtft_charcode.h"


static u32 TFTColVal;
static u32 TFTRowVal;
static u32 TFTFgColor;
static u32 TFTBgColor;

static void XTft_WriteChar(u8 CharValue, u32 ColStartVal, u32 RowStartVal, u32 FgColor, u32 BgColor);

u32 XTft_GetRow()
{
	return TFTRowVal;
}

u32 XTft_GetColumn()
{
	return TFTColVal;
}

void XTft_Init()
{
	TFTColVal = XTFT_DEF_COLVAL;
	TFTRowVal = XTFT_DEF_ROWVAL;
	TFTFgColor = XTFT_DEF_FGCOLOR;
	TFTBgColor = XTFT_DEF_BGCOLOR;
	XTft_FillScreen(XTFT_DEF_COLVAL, XTFT_DEF_ROWVAL, (XTFT_DISPLAY_WIDTH - 1), (XTFT_DISPLAY_HEIGHT - 1), TFTBgColor);
}

void XTft_SetPos(u32 ColVal, u32 RowVal)
{
	TFTColVal = ColVal;
	TFTRowVal = RowVal;
}

void XTft_SetPosChar(u32 ColVal, u32 RowVal)
{
	/*
	 * If there is no space in the current line for the next char
	 * go to next line.
	 */
	if (ColVal > (XTFT_DISPLAY_WIDTH - 1) - XTFT_CHAR_WIDTH) {
		ColVal = XTFT_DEF_COLVAL;
		RowVal += XTFT_CHAR_HEIGHT;
	}

	/*
	 * If there is no space in the current line for the next char
	 * go to next line by deleting the first line.
	 */
	while (RowVal > (XTFT_DISPLAY_HEIGHT - 1) - XTFT_CHAR_HEIGHT) {
		XTft_Scroll();
		RowVal -= XTFT_CHAR_HEIGHT;
	}

	/*
	 * Update the column, row position values.
	 */
	TFTColVal = ColVal;
	TFTRowVal = RowVal;

}

void XTft_SetColor(u32 FgColor, u32 BgColor)
{
	TFTBgColor = BgColor;
	TFTFgColor = FgColor;

}

void XTft_SetPixel(u32 ColVal, u32 RowVal, u32 PixelVal)
{
	Xil_Out32(TFT_FRAME_BUFFER_BASE_ADDR + (4 * ((RowVal) * XTFT_DISPLAY_BUFFER_WIDTH + ColVal)), PixelVal);
}

void XTft_GetPixel(u32 ColVal, u32 RowVal, u32 *PixelVal)
{
	*PixelVal = Xil_In32(TFT_FRAME_BUFFER_BASE_ADDR + (4 *(RowVal * XTFT_DISPLAY_BUFFER_WIDTH) + ColVal));
}

void XTft_Write(u8 CharValue)
{
	/*
	 * First two cases handle the special input values
	 * and default case performs a character write operation
	 * and it updates the column position in the instance structure.
	 */
	switch (CharValue) {
		case 0xd:
			/*
			 * Action to be taken for carriage return.
			 */
			XTft_SetPos(XTFT_DEF_COLVAL, TFTRowVal);
			break;
		case 0xa:
			/*
			 * Action to be taken for line feed.
			 */
			XTft_SetPos(XTFT_DEF_COLVAL, TFTRowVal + XTFT_CHAR_HEIGHT);
			break;
		default:
			/*
			 * Set the position and write the character and
			 * update the column position by width of
			 * character.
			 */
			XTft_SetPosChar(TFTColVal, TFTRowVal);
			XTft_WriteChar( CharValue, TFTColVal, TFTRowVal, TFTFgColor, TFTBgColor);

			TFTColVal += XTFT_CHAR_WIDTH;
			break;
	}
}

void XTft_Scroll()
{
	u32 PixelVal;
	u32 ColIndex;
	u32 RowIndex;

	/*
	 * Takes each pixel value from the second line and puts in the first
	 * line. This process is repeated till the second line
	 * from bottom.
	 */
	for (RowIndex = 0;
		RowIndex < (XTFT_DISPLAY_HEIGHT - 1) - XTFT_CHAR_HEIGHT;
		RowIndex++) {
		for (ColIndex = 0; ColIndex < (XTFT_DISPLAY_WIDTH - 1);
					ColIndex++) {
			XTft_GetPixel(ColIndex, RowIndex + XTFT_CHAR_HEIGHT, &PixelVal);
			XTft_SetPixel(ColIndex, RowIndex, PixelVal);
		}
	}

	/*
	 * Fills the last line with the background color.
	 */
	XTft_FillScreen(XTFT_DEF_COLVAL,
			 (XTFT_DISPLAY_HEIGHT - 1)-XTFT_CHAR_HEIGHT,
			 (XTFT_DISPLAY_WIDTH - 1), (XTFT_DISPLAY_HEIGHT - 1),
			 TFTBgColor);

}

void XTft_ClearScreen()
{
	/*
	 * Fills the screen with the background color of Instance structure.
	 */
	XTft_FillScreen(XTFT_DEF_COLVAL, XTFT_DEF_ROWVAL,
			(XTFT_DISPLAY_WIDTH - 1), (XTFT_DISPLAY_HEIGHT - 1),
			TFTBgColor);

	/*
	 * Initialize the column, row positions to (0, 0)origin.
	 */
	TFTColVal = XTFT_DEF_COLVAL;
	TFTRowVal = XTFT_DEF_ROWVAL;

}

void XTft_FillScreen(u32 ColStartVal, u32 RowStartVal,
			u32 ColEndVal, u32 RowEndVal, u32 PixelVal)
{
	u32 ColIndex;
	u32 RowIndex;

	/*
	 * Fills each pixel on the screen with the value of PixelVal.
	 */
	for (ColIndex = ColStartVal; ColIndex <= ColEndVal; ColIndex++) {
		for (RowIndex = RowStartVal; RowIndex <= RowEndVal;	RowIndex++) {
			XTft_SetPixel(ColIndex, RowIndex, PixelVal);
		}
	}

}
static void XTft_WriteChar(u8 CharValue, u32 ColStartVal,
			u32 RowStartVal, u32 FgColor, u32 BgColor)
{
	u32 PixelVal;
	u32 ColIndex;
	u32 RowIndex;
	u8 BitMapVal;

	/*
	 * Gets the 12 bit value from the character array defined in
	 * charcode.c file and regenerates the bitmap of that character.
	 * It draws that character on screen by setting the pixel either
	 * with the foreground or background color depending on
	 * whether value is 1 or 0.
	 */
	for (RowIndex = 0; RowIndex < XTFT_CHAR_HEIGHT; RowIndex++) {
		BitMapVal = XTft_VidChars[(u32) CharValue -
					XTFT_ASCIICHAR_OFFSET][RowIndex];
		for (ColIndex = 0; ColIndex < XTFT_CHAR_WIDTH; ColIndex++) {
			if (BitMapVal &
				(1 << (XTFT_CHAR_WIDTH - ColIndex - 1))) {
				PixelVal = FgColor;
			} else {
				PixelVal = BgColor;
			}

			/*
			 * Sets the color value to pixel.
			 */
			XTft_SetPixel(ColStartVal+ColIndex, RowStartVal+RowIndex, PixelVal);
		}
	}

}

int test_tft()
{
	xil_printf("Testing the VGA output has started...\r\n");
	XTft_Init();
	sleep(1);

	XTft_SetColor(WHITE_COLOR_VALUE, RED_COLOR_VALUE);
	XTft_ClearScreen();
	Xil_DCacheFlush();
	sleep(1);

	XTft_SetColor(WHITE_COLOR_VALUE, GREEN_COLOR_VALUE);
	XTft_ClearScreen();
	Xil_DCacheFlush();
	sleep(1);

	XTft_SetColor(WHITE_COLOR_VALUE, BLUE_COLOR_VALUE);
	XTft_ClearScreen();
	Xil_DCacheFlush();
	sleep(1);

	XTft_SetColor(WHITE_COLOR_VALUE, 0x00c0c0c0);
	XTft_ClearScreen();
	Xil_DCacheFlush();
	sleep(1);

	XTft_SetColor(BLACK_COLOR_VALUE, WHITE_COLOR_VALUE);
	XTft_ClearScreen();
	Xil_DCacheFlush();

	static char header_str[] = "\n\n  DAEDALUS^RT DEMONSTRATOR (http://daedalus.liacs.nl)\n\n"
						"  COPYRIGHTS (C) 2013 BY LEIDEN UNIVERSITY\n\n"
						"  ALL RIGHTS RESERVED\n\n"
						"  Zedboard rocks! ;-), yeah it does\n\n";
	u32 c;

	for (c = 0; c < strlen(header_str); c++) {
		XTft_Write(header_str[c]);
	}
	Xil_DCacheFlush();

	xil_printf("Testing the VGA output is done!\r\n");
	return XST_SUCCESS;
}

