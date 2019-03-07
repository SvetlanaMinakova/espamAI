#ifndef SPLIT_JOIN_H_
#define SPLIT_JOIN_H_

#include <stdio.h>

#ifdef __MICROBLAZE__
#define mprint xil_printf
#else
#define mprint printf
#endif

void src(int *in)
{
	static int c = 1;
	*in = c;
	c += 2;
	if (c > 100)
		c = 1;
}

void filter1(int in, int *out)
{
	*out = in * 10;
}

void filter2(int in, int *out)
{
	*out = in * 10;
}

void snk(int in1, int in2)
{
	mprint("Split-join produced tokens (%d, %d)\n", in1, in2);
}

#endif
