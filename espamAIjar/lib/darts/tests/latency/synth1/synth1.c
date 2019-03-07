#include <stdio.h>

#include "synth1.h"

#define M 11
#define N 4

int main()
{
    int i, j;
    int img[M][N], img1[M][N];

    while(1) {
        for(i=1;i<=(M-1);i++) {
            for(j=1;j<=(N-1);j++) {
                src(&img[i][j],&img1[i][j]);
            
                if(j<=2)
                    img[i][j]=f1(img[i][j]);
                else
                    img[i][j]=f2(img[i][j]);

                snk(img[i][j],img1[i][j]);
            }
        }
    }
    
    return 0;
}
