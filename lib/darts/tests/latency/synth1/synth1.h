#ifndef SYNTH_H_
#define SYNTH_H_

void src(int *x, int *y)
{
    static int cx = 0, cy = 1;
    *x = cx;
    *y = cy;

    cx += 2;
    cy += 2;
}

int f1(int x)
{
    return x + 0;
}

int f2(int x)
{
    return x + 0;
}

void snk(int x, int y)
{
#ifdef __MICROBLAZE__
    xil_printf("x = %d, y = %d\n", x, y);
#else
    printf("x = %d, y = %d\n", x, y);
#endif
}

#endif 
