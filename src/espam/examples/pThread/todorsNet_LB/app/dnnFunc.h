/*
 * dnnFunc.h
 *
 *  Created on: 14 Jan 2019
 *      Author: minakovas
 */

#ifndef dnnFunc_H_
#define dnnFunc_H_

#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>
#include <map>
#include "types.h"

class dnnFunc {
public:
	dnnFunc();
	virtual ~dnnFunc();

    static void execute_dense_block (std::string function,int* input, int* weights, int* output, std::map<std::string,int>* int_params_ptr );
    static void execute_conv (int* input, int* weights, int* output, std::map<std::string,int>* int_params_ptr );

	static void convolution_2d_inp(int *input, int *kernel, int *output, int input_h, int input_w,
			int output_h,int output_w, int k_h, int k_w, int stride);

	static void convolution_3d_inp(int *input, int *kernel, int *output, int channels, int input_h, int input_w,
				int output_h,int output_w, int k_h, int k_w, int stride);

	static float get_softmax_summ(int *non_activated_stages, int input_len);
	static void init_zeros(int *matrix, int h, int w);
	static void init_zeros(int *matrix, int d, int h, int w);
	static void softmax(int *non_activated_stages, int len);
	//element-wise multiplication on weights + summ*/
	static void weight_and_sum(int *input, int *weights, int *result, int input_len);
	static void weight_and_sum(int *input, int *weights, int *result, int input_len, int output_len);
};

#endif /* dnnFunc_H_ */
