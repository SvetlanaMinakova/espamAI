#ifndef MAXPOOL_FP16_H
#define MAXPOOL_FP16_H

#include "types2.h"
#include "arm_neon.h"
#include <math.h>

RET maxpool_fp16_forward(int16_t* output, int16_t* input, SIZE out_s[3], SIZE in_s[3], 
		SIZE kern_s[2], SIZE stride[2], SIZE pad[2]);

#endif

typedef struct Maxpool*     MAXPOOLS;
MAXPOOLS maxpool_create(void);

struct Maxpool   {

                  int     kern_max[2];
                  int     in_s[3];
                  int     out_s[3];
                  int     stride[2];
                  int     pad[2];
                  bool     activate;
};
