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
#include "types2.h"

 std::pair<int,int> get_max(DATA* input, int len);
 void classify(DATA* input, int len, std::vector<int> labels);
 static int conv_sw(DATA* input, DATA* output, DATA* kernel, DATA* bias, int input_ch, int input_h, int input_w, 
                     int output_ch, int output_h, int output_w, int k_h, int k_w, int stride);
 void relu(DATA* input, DATA* output, std::map<std::string,int>* int_params_ptr);
 void maxpool(DATA* input, DATA* output, std::map<std::string,int>* int_params_ptr);
 void execute_dense_block (std::string function, DATA* input, DATA* output, DATA* weights, 
                                  DATA* bias, std::map<std::string,int>* int_params_ptr);
 void execute_conv (DATA* input, DATA* weights, DATA* output, DATA* bias, std::map<std::string,int>* int_params_ptr);

 static void convolution_2d_inp(float *input, float *kernel, float *output, int input_h, int input_w,
			                            int output_h,int output_w, int k_h, int k_w, int stride);

 static void convolution_3d_inp(float *input, float *kernel, float *output, int channels, int input_h, int input_w,
				                          int output_h,int output_w, int k_h, int k_w, int stride);
 void transpose(DATA *input, int inp_h, int inp_w);
 static float get_softmax_summ(float *non_activated_stages, int input_len);
 static void init_zeros(DATA *matrix, int h, int w);
 static void init_zeros(DATA *matrix, int d, int h, int w);
 void softmax(DATA *input, int n, DATA *output);
 //element-wise multiplication on weights + summ*/
 static void weight_and_sum(DATA *input, DATA *weights, DATA *result, int input_len);
 static void weight_and_sum(DATA *input,  DATA *result, DATA *weights, DATA *bias, int input_len, int output_len);
 
static inline long long int saturate(long long int mac, const char* module);

#endif /* dnnFunc_H_ */
