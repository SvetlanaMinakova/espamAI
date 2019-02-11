/*
 * dnnFunc.cpp
 *
 *  Created on: 14 Jan 2019
 *      Author: minakovas
 */

#include "dnnFunc.h"
#include <iostream>
#include <math.h>

using namespace std;

dnnFunc::dnnFunc() {
	// TODO Auto-generated constructor stub

}

dnnFunc::~dnnFunc() {
	// TODO Auto-generated destructor stub
}


/**
   void appFunc::execute (std::string function,int* input, int* weights, int* output, std::map<std::string,const int*>* int_params_ptr )
    {
	   /**
	    * (int *input, int *kernel, int *output, int input_h, int input_w,
		int output_h,int output_w, int k_h, int k_w, int stride)
	    *
	    *


	 //  *(params_ptr->at(std::string("input_h")))

	   if (function.find("CONV") != std::string::npos){
		   const int* stride;
		   const int* k_h;
		   const int* k_w;
		   const int* input_h;
		   const int* input_w;
		   const int* output_h;
		   const int* output_w;

		   k_h = int_params_ptr->at("k_h");
		   k_w = int_params_ptr->at("k_w");
		   stride =  int_params_ptr->at("stride");

		   output_h = int_params_ptr->at("input_dim_0");
		   output_w = int_params_ptr->at("input_dim_1");

		   if(*(int_params_ptr->at("input_dims"))==3){
			   output_h = int_params_ptr->at("input_dim_1");
			   output_w = int_params_ptr->at("input_dim_2");
		   }

		   /** conv over 2d input
		   if(*(int_params_ptr->at("input_dims"))==2){
			   input_h = int_params_ptr->at("input_dim_0");
			   input_w = int_params_ptr->at("input_dim_1");

			 dnnFunc::convolution(input, weights, output, *input_h, *input_w, *output_h, *output_w, *k_h, *k_w, *stride);


		   }


	   }
      // cout<<function<<endl;
    }
 */

/**
 *
 * Convolutional operation for input 2D MATRIX
 */
void dnnFunc::convolution_2d_inp(int *input, int *kernel, int *output, int input_h, int input_w,
		int output_h,int output_w, int k_h, int k_w, int stride){
		    int fold_cell = 0;
			int input_elem_ind = 0;
			int k_elem_ind = 0;
			int outp_elem_ind =0;

			//clean output
			init_zeros(output, output_w,output_h);
		   // fill in output matrix
		   for (int j = 0; j < output_h; j+=stride)
		   {
			   for (int i = 0; i < output_w; i+=stride)
			   {
				   //summ k,l
				   for (int l = 0; l < k_h; l++)
				   {

					   for (int k = 0; k < k_w; k++){

			           k_elem_ind = l + k * k_h;
			           input_elem_ind = (j+l) + (i+k)* input_w;
			           fold_cell += *(input + input_elem_ind) * *(kernel + k_elem_ind);
					   }
				   }
				   outp_elem_ind = i * output_w + j;
			       output[outp_elem_ind] += fold_cell;
			       fold_cell = 0;
			   }
		   }
	}

void dnnFunc::convolution(int *input, int *kernel, int *output, int channels, int neurons, int input_h, int input_w,
		int output_h,int output_w, int k_h, int k_w, int stride){

	cout<<"conv3D_nn_inside!"<<endl;
	int fold_cell = 0;
	int input_elem_ind = 0;
	int k_elem_ind = 0;
	int outp_elem_ind =0;

	//clean output
	init_zeros(output, output_w,output_h);
	// fill in output matrix
	for(int n=0; n< neurons; n++){
	for (int d=0; d<channels;d++){
		for (int j = 0; j < output_h; j+=stride)
			{
				for (int i = 0; i < output_w; i+=stride)
				{
				   //summ k,l
				   for (int l = 0; l < k_h; l++)
				   {
					   for (int k = 0; k < k_w; k++)
					   {
						   k_elem_ind = l + k * k_h + d * k_w * k_h + n * k_w * k_h * channels;
						   input_elem_ind = (j+l) + (i+k)*input_w + d*input_w*input_h;
						   fold_cell += *(input + input_elem_ind) * *(kernel + k_elem_ind);
					   }
				   }
				   outp_elem_ind = i * output_w + n * output_w * output_h;
			       output[outp_elem_ind] += fold_cell;
			       fold_cell = 0;
			   }
		   }
		}
	}
}

void dnnFunc::convolution_3d_inp(int *input, int *kernel, int *output, int channels, int input_h, int input_w,
		int output_h,int output_w, int k_h, int k_w, int stride){
	int fold_cell = 0;
	int input_elem_ind = 0;
	int k_elem_ind = 0;
	int outp_elem_ind =0;

	//cout<<"conv3D!"<<endl;

	//cout<<"k_w = "<<k_w<<endl;


	//clean output
	init_zeros(output, output_w,output_h);
	// fill in output matrix

	int *sub_input;

	for (int d=0; d<channels;d++){
		sub_input = input + input_w * input_h * d;
       // cout<<sub_input<<sub_input<<endl;

		for (int j = 0; j < output_h; j+=stride)
			{
				for (int i = 0; i < output_w; i+=stride)
				{
				   //summ k,l
				   for (int l = 0; l < k_h; l++)
				   {
					   for (int k = 0; k < k_w; k++)
					   {
						   k_elem_ind = l + k * k_h;
						//   cout<<"k_elem_ind = "<<k_elem_ind<<endl;

						   input_elem_ind = (j+l) + (i+k)* input_w;
						   fold_cell += * (sub_input + input_elem_ind) * *(kernel + k_elem_ind);
					   }
				   }
				   outp_elem_ind = i * output_w + j;
			       output[outp_elem_ind] += fold_cell;
			       fold_cell = 0;
			   }
		   }
		}
	}


/**
 * element-wise mult input * weights, save result in result; Analogue to MatMul?
 */
void dnnFunc::weight_and_sum(int *input, int *weights, int *result, int input_len, int output_len){
	for (int j=0; j<output_len; j++){
	//std::cout<<"j = "<<j<<endl;
	*(result+j) = 0;
	for(int i=0; i<input_len; i++){
		//std::cout<<*(result+j)<<"+="<<*(input+i)<<" * "<<*(weights + i + j*input_len)<<endl;
		*(result+j) +=*(input + i) * *(weights + i + j*input_len);
	}
	}
}

/**
 * element-wise mult input * weights, save result in result;
 */
void dnnFunc::weight_and_sum(int *input, int *weights, int *result, int input_len){
	*result = 0;
	for(int i=0; i<input_len; i++)
		*result +=*(input+i) * *(weights+i);
}


void dnnFunc::softmax(int *non_activated_stages, int len)
{
	int softmax_summ = dnnFunc::get_softmax_summ(non_activated_stages, len);
	int id=0;
	int *sm_elem;
	for(int i=0; i<len; i++)
	{
		sm_elem = non_activated_stages + i;
		*(sm_elem)/=softmax_summ;
	}
}


float dnnFunc::get_softmax_summ(int *non_activated_stages, int len)
{
	float softmax_summ = 0;
	for( int i=0;i<len;i++)
		softmax_summ+=exp( *(non_activated_stages + i));
	return softmax_summ;
}

void dnnFunc::init_zeros(int *matrix, int h, int w){
	for (int j = 0; j < h; j++){
		for (int i = 0; i < w; i++){
			matrix[i * w + j]=0;
		}
	}}

void dnnFunc::init_zeros(int *matrix, int d, int h, int w){
	for (int k = 0; k < d; k++){
		for (int j = 0; j < h; j++){
			for (int i = 0; i < w; i++){
				matrix[i * w + j]=0;
				}
	}		}
}

