#include "maxpool.h"
#include "xassert.h"

#ifdef _ARM_
#define ARM_QUAD_WORD_NEON

#include "arm_neon.h"
#ifdef ARM_QUAD_WORD_NEON
#define NEON_BLOCK 4
#else
#define NEON_BLOCK 2
#endif
#define MAX_NEG_FP16 (-32768)
//#define MAX_NEG_FP16  (1.175494351e-38F)
#endif

MAXPOOLS maxpool_create(void) {
    MAXPOOLS mp = (MAXPOOLS) calloc(1, sizeof(struct Maxpool));
    return mp;
}

static inline RET maxpool_fp16_forward_sw_core(int16_t* input, int16_t* output, SIZE in_s[3], 
                SIZE out_s[3], SIZE kern_s[2], SIZE stride[2], SIZE pad[2]) {

	ITER plane = 0;
	ITER hout = 0;
	ITER wout = 0;
	ITER hkern = 0;
	ITER wkern = 0;
	ITER _stride0 = stride[0];
	ITER _stride1 = stride[1];
	ITER _of   = out_s[0];
	ITER _of_h = out_s[1];
	ITER _of_w = out_s[2];
	ITER _if   = in_s[0];
	ITER _if_h = in_s[1];
	ITER _if_w = in_s[2];
	ITER _k_h = kern_s[0];
	ITER _k_w = kern_s[1];
	ITER _pad_h = pad[0];
	ITER _pad_w = pad[1];

	int hin = 0;
	int win = 0;
	bool cond = 0;

	ASSERT((_k_h<=3 && _k_w<=3),"%s", "Error: we assume maxpool kernel size <=3");



int i;

/*printf("in_s[0] %d   in_s[1] %d  in_s[2] %d  out_s[0] %d   out_s[1] %d  out_s[2] %d  kern_s[0] %d  kern_s[1] %d  stride[0] %d  stride[1] %d \
        pad[0] %d  pad[1] %d \n", in_s[0], in_s[1], in_s[2], out_s[0], out_s[1], out_s[2], kern_s[0], kern_s[1], stride[0], stride[1], pad[0], pad[1]);*/
/*for(i=0;i<1000;i++)
printf("input %d:  %d \n", i, input[i]);*/



	/* for each plane */
	#pragma omp parallel for \
			firstprivate(_stride0, _stride1, _of, _of_h, _of_w, _if, _if_h, _if_w, _k_h, _k_w, _pad_h, _pad_w) \
			private(hout,wout) \
			collapse(2)
	for (plane = 0; plane < _of; plane++) {

		/* for output matrix */
		for (hout = 0; hout < _of_h; hout++) {
			ITER of_h_idx = (plane*_of_h + hout)*_of_w;

                         
			for (wout = 0; wout < _of_w; wout++) {
				bool first_element = true;
				int16_t max;
				int16_t current;

				/* for kernel matrix */
				for (hkern = 0; hkern < _k_h; hkern++) {
					/* calculate required input position */
					hin = _stride0 * hout + hkern - _pad_h;
					ITER if_h_idx = (plane*_if_h + hin)*_if_w;

					for (wkern = 0; wkern < _k_w; wkern++) {
						/* calculate required input position */
						win = _stride1 * wout + wkern - _pad_w;

						/* test if position is inside bounds*/
						cond = hin >=0 && win >= 0 && (ITER) win < _if_w && (ITER) hin < _if_h;

						/* if outside bounds => set to zero */
						DATA inVal;
						if (cond) {
							//inVal = in[plane][hin][win];
							inVal = input[if_h_idx + win];
						} else {
							inVal = 0;
						}
						current = inVal;

						if(first_element){
							max = current;
							first_element = false;
						} else {
							max = (max > current) ? max : current;
						}
					}
				}

				output[of_h_idx + wout] = max;
			}
//#endif
		}
	}

	return OK;
}




static inline void forward_maxpool_layer(int16_t* input, int16_t* output, SIZE in_s[3],
                                            SIZE out_s[3], SIZE kern_s[2], SIZE stride[2], SIZE pad[2])
{
    int i,j,k,m,n;
    int b=0;
    int z=0;
    ITER w_offset = pad[0]/2;
    ITER h_offset = pad[1]/2;

    ITER h = in_s[1];
    ITER w = in_s[2];
    ITER c = out_s[0];
    ITER h_out = out_s[1];
    ITER w_out = out_s[2];
    ITER _stride = stride[0];
    ITER size = kern_s[0];
    int16_t short_max = -pow(2,15);

   /* for each plane */
   #pragma omp parallel for \
			firstprivate(c, h, w, stride, size, h_offset, w_offset)\
			private(i, j, k, m, n) \
			collapse(2)
    //for(b = 0; b < l.batch; ++b){
/*
  
    printf("l.size: %d ,l.pad: %d, h: %d, w: %d, c: %d, l.batch: %d, l.stride: %d \n", size, 
                                                                            pad[0], h, w, c, b, _stride);
    for(i=0;i<100;i++)
     printf("input max pool: %d ---> %f \n", input[i], (((float) (input[i])) / (1<<10)));
  */  

        
        for(k = 0; k < c; ++k){
            for(i = 0; i < h_out; ++i){
                for(j = 0; j < w_out; ++j){
                    int out_index = j + w_out*(i + h_out*(k + c*b));
    //if(in_s[1]==26)
    //  printf("out_index %d = j %d + w %d * (i %d + h %d * (k %d + c %d * b %d)) \n", out_index, j, w, i, h, k, c, b);
                    int16_t max = -short_max;//-FLT_MAX;
                    int max_i = -1;
                    for(n = 0; n < size; ++n){
                        for(m = 0; m < size; ++m){
                            int cur_h = h_offset + i*_stride + n;
                            int cur_w = w_offset + j*_stride + m;
                            int index = cur_w + w*(cur_h + h*(k + b*c));
                            int valid = (cur_h >= 0 && cur_h < h &&
                                         cur_w >= 0 && cur_w < w);
                            int16_t val = (valid != 0) ? input[index] : -short_max;//-FLT_MAX;
                            //max_i = (val > max) ? index : max_i;
                            max   = (val > max) ? val   : max;


/*if(max == short_max){
 printf("[%d] cur_h %d , cur_w %d , index %d , valid %d , val %f , max %f \n", z, cur_h, cur_w, index, valid, (((float) (val)) / (1<<10)), 
                                                                                              (((float) (max)) / (1<<10)));
 printf("h %d, w %d, k %d,  b %d, c %d \n", h, w, k, b, c);
 printf("cur_h = h_offset %d + i %d * _stride %d + n %d \n", h_offset, i, _stride, n);
 printf("cur_w = w_offset %d + j %d * _stride %d + m %d \n", w_offset, j, _stride, m);
               z++; }*/
                        }
                    }
                     output[out_index] = max;
                   //  if(in_s[1]==26)
		    // 	printf("output[%d] %d \n", out_index, output[out_index]);
                     //if(output[out_index] == -short_max)
                    //  printf("output[%d] %f \n", out_index, (((float) (output[out_index])) / (1<<10)));
                    //l.indexes[out_index] = max_i;
                }
            }
        }
    //}
}






RET maxpool_fp16_forward(int16_t* output, int16_t* input, SIZE out_s[3], SIZE in_s[3], 
		SIZE kern_s[2], SIZE stride[2], SIZE pad[2]) {

	/*VARSIZE out_a[3] = { 0 };
 
	out_a[0] = in_s[0];
	out_a[1] = (in_s[1] + 2 * pad[0] - kern_s[0]) / stride[0] + 1;
	out_a[2] = (in_s[2] + 2 * pad[1] - kern_s[1]) / stride[1] + 1;




	ASSERT(equalSize(out_s, out_a, 3), "%s",
			"output size does not match parameterized pooling kernel");
*/
	_tcreate_(time);
	//maxpool_fp16_forward_sw_core(input, output, in_s, out_s, kern_s, stride, pad);
         forward_maxpool_layer(input, output, in_s, out_s, kern_s, stride, pad);
	_tprintf_("MaxPool time: %5.3f ms\n", (get_wall_time()-time)/1000);

	return OK;
}
