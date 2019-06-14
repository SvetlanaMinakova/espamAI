#ifndef AVGPOOL_H
#define AVGPOOL_H

#include "types2.h"
#include "arm_neon.h"

RET avgpool_forward(DATA* input, DATA* output, SIZE in_s[3], SIZE out_s[3],
		SIZE kern_s[2], SIZE stride[2], SIZE pad[2]);

RET avgpool_f32_forward(float* input, float* output, SIZE in_s[3], SIZE out_s[3],
		SIZE kern_s[2], SIZE stride[2], SIZE pad[2], int QF);

RET avgpool_fp16_forward(int16_t* output, int16_t* input, SIZE in_s[3], SIZE out_s[3],
		SIZE kern_s[2], SIZE stride[2], SIZE pad[2], int QF);

#endif


typedef struct Avgpool*     AVGPOOLS;
AVGPOOLS avgpool_create(void);

struct Avgpool   {

                  int     kern_avg[2];
                  int     in_s[3];
                  int     out_s[3];
                  int     stride[2];
                  int     pad[2];
};

