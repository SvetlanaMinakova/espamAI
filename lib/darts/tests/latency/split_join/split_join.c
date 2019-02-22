#include "split_join.h"

int main()
{
    int i,j;
    int x, out1, out2;
    for (i = 0; i < 10; i++) {
        for (j = 0; j < 10; j++) {
            src(&x);
            filter1(x, &out1);
            filter2(x, &out2);
            snk(out1, out2);
        }
    }
    return 0;
}
