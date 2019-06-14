#ifndef LEAKY_H
#define LEAKY_H

#include "types2.h"

RET leaky_forward(DATA* input, DATA* output, SIZE size[3]);
RET leaky_f32_forward(float* input, float* output, SIZE size[3]);
RET leaky_fp16_forward(int16_t* input, int16_t* output, SIZE size[3]);

#endif
