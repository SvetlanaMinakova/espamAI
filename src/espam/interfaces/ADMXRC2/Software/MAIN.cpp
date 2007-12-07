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
#include <iostream>
#include <stdio.h>

int main(int argc, char **argv)
{

    FPGA  fpga;
    int imageIN[] = { 0,0,0,0,0,0,0,0,0,0,0,0,
                      0,2,1,4,3,2,3,6,8,5,2,0,
                      0,2,1,4,3,2,3,6,8,5,2,0,
                      0,2,1,4,3,2,3,6,8,5,2,0,
                      0,2,1,4,3,2,3,6,8,5,2,0,
                      0,2,1,4,3,2,3,6,8,5,2,0,
                      0,0,0,0,0,0,0,0,0,0,0,0
	};

    int imageIN_1[] = { 0,0,0,0,0,0,0,
                        0,2,2,2,2,2,0,
                        0,1,1,1,1,1,0,
                        0,4,4,4,4,4,0,
                        0,3,3,3,3,3,0,
                        0,2,2,2,2,2,0,
                        0,3,3,3,3,3,0,
                        0,6,6,6,6,6,0,
                        0,8,8,8,8,8,0,
                        0,5,5,5,5,5,0,
                        0,2,2,2,2,2,0,
                        0,0,0,0,0,0,0
	};

    int imageOUT[10]; // the maximum value for par1

    fpga.Sobel(imageIN,imageOUT, 0xA, 0x5);  // width, height


    printf("Results: \n\n");
	for (int j = 0; j < 10; j++) {
		//cout << imageOUT[j] << "\t";
		printf("%8x\t", imageOUT[j]);
    }
    printf("\n\n");

    fpga.Sobel(imageIN_1,imageOUT, 0x5, 0xA);

    printf("Results: \n\n");
    for (j = 0; j < 5; j++) {
		printf("%8x\t", imageOUT[j]);
    }

    printf("\n");

    return (0);
}
