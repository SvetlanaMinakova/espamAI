#include "leaky.h"

#ifdef _ARM_
#include "arm_neon.h"
#define NEON_BLOCK 4
#endif

static inline RET leaky_forward_sw(DATA* input, DATA* output, SIZE size[3]) {
	ITER i = 0;
	SIZE memsize = size[0]*size[1]*size[2];
	for(i = 0; i < memsize; i++) {
		DATA v = input[i];
		v = v > 0 ? v : (v>>2); 
		output[i] = v;
	}
	return OK;
}

//FIXME use NEON HERE
static inline RET leaky_f32_forward_sw(float* input, float* output, SIZE size[3]) {
	ITER i = 0;
	SIZE memsize = size[0]*size[1]*size[2];
	for(i = 0; i < memsize; i++) {
		float v = input[i];
		v = v > 0 ? v : .1f*v;
		output[i] = v;
	}
	return OK;
}

//FIXME use NEON HERE
static inline RET leaky_fp16_forward_sw(int16_t* input, int16_t* output, SIZE size[3]) {
	ITER i = 0;
	SIZE memsize = size[0]*size[1]*size[2];
	for(i = 0; i < memsize; i++) {
		int16_t v = input[i];
		v = v > 0 ? v : (v>>3);
		output[i] = v;
	}
	return OK;
}

RET leaky_forward(DATA* input, DATA* output, SIZE size[3]) {

	_tcreate_(time);	
	leaky_forward_sw(input, output, size);
	_tprintf_("Leaky time: %5.3f ms\n", (get_wall_time()-time)/1000);

	return OK;
}

RET leaky_f32_forward(float* input, float* output, SIZE size[3]) {

	_tcreate_(time);	
	leaky_f32_forward_sw(input, output, size);
	_tprintf_("Leaky time: %5.3f ms\n", (get_wall_time()-time)/1000);

	return OK;
}

RET leaky_fp16_forward(int16_t* output, int16_t* input, SIZE size[3]) {

	_tcreate_(time);	
	leaky_fp16_forward_sw(input, output, size);
	_tprintf_("Leaky time: %5.3f ms\n", (get_wall_time()-time)/1000);

	return OK;
}
